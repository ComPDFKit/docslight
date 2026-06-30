"""Convert local parse payloads into the ComPDF-style standard JSON shape."""

from __future__ import annotations

import math
from html.parser import HTMLParser
from numbers import Real
from typing import Any

IGNORED_STATUS_LABELS = {
    "aside_text",
    "footer",
    "footer_image",
    "footnote",
    "header",
    "header_image",
    "number",
}


def convert_parse_payload(payload: dict[str, Any]) -> dict[str, Any]:
    """Convert a serialized ParseResult payload into standard parse JSON."""
    if _looks_like_standard_payload(payload):
        return payload
    metadata = payload.get("metadata") if isinstance(payload.get("metadata"), dict) else {}
    pages = payload.get("pages") if isinstance(payload.get("pages"), list) else []
    markdown = payload.get("markdown") if isinstance(payload.get("markdown"), str) else ""
    return build_standard_parse_json(markdown=markdown, pages=pages, metadata=metadata)


def build_standard_parse_json(
    *,
    markdown: str,
    pages: list[dict[str, Any]],
    metadata: dict[str, Any] | None = None,
) -> dict[str, Any]:
    """Build standard parse JSON from ParseResult parts."""
    metadata = metadata or {}
    standard_pages: list[dict[str, Any]] = []
    detail: list[dict[str, Any]] = []
    metrics: list[dict[str, Any]] = []

    for fallback_page_index, page in enumerate(pages):
        if not isinstance(page, dict):
            continue
        standard_page, page_detail, metric = _convert_page(
            page,
            fallback_page_index,
            first_paragraph_id=len(detail) + 1,
        )
        standard_pages.append(standard_page)
        detail.extend(page_detail)
        metrics.append(metric)

    success_count = sum(1 for page in standard_pages if page.get("status") == "Success")
    return {
        "result": {
            "pages": standard_pages,
            "detail": detail,
            "total_count": len(standard_pages),
            "valid_page_number": success_count,
            "total_page_number": len(standard_pages),
            "catalog": {},
            "excel_base64": "",
            "success_count": success_count,
            "markdown": markdown,
        },
        "x_request_id": _string_value(metadata.get("x_request_id")),
        "file_type": _file_type(metadata),
        "metrics": metrics,
        "message": "Success",
        "code": 200,
        "image_process": [],
    }


def _looks_like_standard_payload(payload: dict[str, Any]) -> bool:
    result = payload.get("result")
    return isinstance(result, dict) and isinstance(result.get("pages"), list)


def _convert_page(
    page: dict[str, Any],
    fallback_page_index: int,
    *,
    first_paragraph_id: int,
) -> tuple[dict[str, Any], list[dict[str, Any]], dict[str, Any]]:
    page_index = _int_value(page.get("page_index"), fallback_page_index)
    page_id = _page_id(page, page_index)
    width = _number_or_zero(page.get("width"), page.get("page_width"))
    height = _number_or_zero(page.get("height"), page.get("page_height"))
    angle = _page_angle(page)
    duration = _number_or_zero(page.get("durations"), page.get("duration"))

    structured: list[dict[str, Any]] = []
    content: list[dict[str, Any]] = []
    detail: list[dict[str, Any]] = []
    layout_boxes = _layout_boxes(page)

    for fallback_block_index, block in enumerate(_blocks(page)):
        block_id = _int_value(block.get("block_id"), fallback_block_index)
        label = (
            _string_value(block.get("block_label"))
            or _string_value(block.get("block_type"))
            or _string_value(block.get("type"))
            or "text"
        )
        text = _string_value(block.get("block_content")) or _string_value(block.get("text"))
        pos = _bbox_to_quad(block.get("block_bbox") or block.get("bbox") or block.get("pos"))
        status = 0 if label in IGNORED_STATUS_LABELS else 1

        structured_item: dict[str, Any] = {
            "pos": pos,
            "id": block_id,
            "content": [block_id],
            "text": text,
            "type": label,
            "outline_level": _int_value(block.get("outline_level"), -1),
        }
        if label == "table":
            rows, cols = _table_dimensions(text)
            structured_item.update({"rows": rows, "cols": cols, "sub_type": "bordered"})
        structured.append(structured_item)

        content_item: dict[str, Any] = {
            "id": block_id,
            "status": status,
            "pos": pos,
            "type": _content_type(label),
            "text": text,
        }
        score = _score_for_block(block, label, layout_boxes)
        if label != "table":
            content_item["score"] = score
            content_item["angle"] = angle
        content.append(content_item)

        detail_item: dict[str, Any] = {
            "paragraph_id": first_paragraph_id + len(detail),
            "page_id": page_id,
            "status": status,
            "type": _detail_type(label),
            "position": pos,
            "outline_level": structured_item["outline_level"],
            "sub_type": "bordered" if label == "table" else label,
            "content": 0,
            "text": text,
        }
        if label == "table":
            detail_item["caption_id"] = block_id
        else:
            detail_item["tags"] = []
        detail.append(detail_item)

    standard_page = {
        "angle": angle,
        "page_id": page_id,
        "image_id": _string_value(page.get("image_id")),
        "height": height,
        "width": width,
        "durations": duration,
        "structured": structured,
        "status": "Success",
        "content": content,
    }
    metric = {
        "angle": angle,
        "status": "Success",
        "dpi": _int_value(page.get("dpi"), 144),
        "page_id": page_id,
        "image_id": standard_page["image_id"],
        "duration": duration,
        "page_image_height": height,
        "page_image_width": width,
    }
    return standard_page, detail, metric


def _blocks(page: dict[str, Any]) -> list[dict[str, Any]]:
    blocks = page.get("parsing_res_list")
    if isinstance(blocks, list):
        return [block for block in blocks if isinstance(block, dict)]
    structured = page.get("structured")
    if isinstance(structured, list):
        return [block for block in structured if isinstance(block, dict)]
    return []


def _layout_boxes(page: dict[str, Any]) -> list[dict[str, Any]]:
    layout = page.get("layout_det_res")
    boxes = layout.get("boxes") if isinstance(layout, dict) else None
    if not isinstance(boxes, list):
        return []
    return [box for box in boxes if isinstance(box, dict)]


def _score_for_block(
    block: dict[str, Any],
    label: str,
    layout_boxes: list[dict[str, Any]],
) -> float:
    bbox = _bbox4(block.get("block_bbox") or block.get("bbox") or block.get("pos"))
    if bbox is None:
        return _float_value(block.get("score"), 0.0)

    best_score = _float_value(block.get("score"), 0.0)
    best_overlap = 0.0
    for candidate in layout_boxes:
        if candidate.get("label") != label:
            continue
        candidate_bbox = _bbox4(candidate.get("coordinate"))
        if candidate_bbox is None:
            continue
        overlap = _iou(bbox, candidate_bbox)
        if overlap > best_overlap:
            best_overlap = overlap
            best_score = _float_value(candidate.get("score"), best_score)
    return best_score


def _page_id(page: dict[str, Any], page_index: int) -> int:
    for key in ("page_id", "page_number"):
        value = page.get(key)
        if isinstance(value, int) and not isinstance(value, bool):
            return value
    return page_index + 1


def _page_angle(page: dict[str, Any]) -> int:
    for value in (
        page.get("angle"),
        page.get("doc_preprocessor_res", {}).get("angle")
        if isinstance(page.get("doc_preprocessor_res"), dict)
        else None,
    ):
        if isinstance(value, Real) and not isinstance(value, bool) and math.isfinite(value):
            return int(value) if value >= 0 else 0
    return 0


def _file_type(metadata: dict[str, Any]) -> str:
    explicit = _string_value(metadata.get("file_type"))
    if explicit:
        return explicit
    document_type = _string_value(metadata.get("document_type"))
    return document_type.upper() if document_type else ""


def _content_type(label: str) -> str:
    return "image" if label.endswith("_image") else label


def _detail_type(label: str) -> str:
    if label == "table":
        return "table"
    if label.endswith("_image"):
        return "image"
    return "paragraph"


def _bbox_to_quad(value: Any) -> list[int]:
    bbox = _bbox4(value)
    if bbox is None:
        return []
    x1, y1, x2, y2 = [_rounded_int(item) for item in bbox]
    return [x1, y1, x2, y1, x2, y2, x1, y2]


def _bbox4(value: Any) -> list[float] | None:
    if not isinstance(value, list):
        return None
    if len(value) >= 8:
        numbers = [_finite_float(item) for item in value[:8]]
        if any(item is None for item in numbers):
            return None
        xs = [item for item in numbers[0::2] if item is not None]
        ys = [item for item in numbers[1::2] if item is not None]
        return [min(xs), min(ys), max(xs), max(ys)]
    if len(value) >= 4:
        numbers = [_finite_float(item) for item in value[:4]]
        if any(item is None for item in numbers):
            return None
        return [item for item in numbers if item is not None]
    return None


def _iou(left: list[float], right: list[float]) -> float:
    left_x1, left_y1, left_x2, left_y2 = left
    right_x1, right_y1, right_x2, right_y2 = right
    x1 = max(left_x1, right_x1)
    y1 = max(left_y1, right_y1)
    x2 = min(left_x2, right_x2)
    y2 = min(left_y2, right_y2)
    intersection = max(0.0, x2 - x1) * max(0.0, y2 - y1)
    left_area = max(0.0, left_x2 - left_x1) * max(0.0, left_y2 - left_y1)
    right_area = max(0.0, right_x2 - right_x1) * max(0.0, right_y2 - right_y1)
    union = left_area + right_area - intersection
    return intersection / union if union else 0.0


def _table_dimensions(html: str) -> tuple[int, int]:
    parser = _TableDimensionParser()
    parser.feed(html)
    parser.close()
    return parser.rows, parser.cols


class _TableDimensionParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.rows = 0
        self.cols = 0
        self._current_cols = 0
        self._in_row = False

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        if tag == "tr":
            self._in_row = True
            self._current_cols = 0
            self.rows += 1
        elif self._in_row and tag in {"td", "th"}:
            colspan = dict(attrs).get("colspan")
            self._current_cols += _positive_int(colspan, 1)

    def handle_endtag(self, tag: str) -> None:
        if tag == "tr":
            self.cols = max(self.cols, self._current_cols)
            self._in_row = False
            self._current_cols = 0


def _positive_int(value: Any, default: int) -> int:
    try:
        parsed = int(value)
    except (TypeError, ValueError):
        return default
    return parsed if parsed > 0 else default


def _int_value(value: Any, default: int) -> int:
    return value if isinstance(value, int) and not isinstance(value, bool) else default


def _number_or_zero(*values: Any) -> int | float:
    for value in values:
        if isinstance(value, Real) and not isinstance(value, bool) and math.isfinite(value):
            return int(value) if float(value).is_integer() else float(value)
    return 0


def _float_value(value: Any, default: float) -> float:
    parsed = _finite_float(value)
    return parsed if parsed is not None else default


def _finite_float(value: Any) -> float | None:
    if isinstance(value, Real) and not isinstance(value, bool) and math.isfinite(value):
        return float(value)
    return None


def _rounded_int(value: float) -> int:
    return int(round(value))


def _string_value(value: Any) -> str:
    return value if isinstance(value, str) else ""
