"""Helpers for exposing parser layout blocks to local LLM extraction."""

from __future__ import annotations

import math
from numbers import Real
from typing import Any


def build_layout_blocks(pages: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Convert parser page JSON into compact block refs with bboxes."""
    blocks: list[dict[str, Any]] = []
    for fallback_page_index, page in enumerate(pages):
        if not isinstance(page, dict):
            continue
        page_index = _int_value(page.get("page_index"), fallback_page_index)
        page_id = _page_id(page, page_index)
        parsing_res_list = page.get("parsing_res_list", [])
        if not isinstance(parsing_res_list, list):
            continue
        source_dimensions = _source_dimensions(page)
        for fallback_block_index, block in enumerate(parsing_res_list):
            if not isinstance(block, dict):
                continue
            text = block.get("block_content")
            bbox = block.get("block_bbox")
            if not isinstance(text, str) or not _is_bbox(bbox):
                continue
            block_id = _int_value(block.get("block_id"), fallback_block_index)
            layout_block = {
                "ref_id": f"p{page_id}b{block_id}",
                "page_id": page_id,
                "page_index": page_index,
                "block_id": block_id,
                "label": block.get("block_label", ""),
                "text": text,
                "bbox": list(bbox[:4]),
            }
            if source_dimensions is not None:
                layout_block.update(source_dimensions)
            blocks.append(layout_block)
    return blocks


def _page_id(page: dict[str, Any], page_index: int) -> int:
    for key in ("page_id", "page_number"):
        value = page.get(key)
        if isinstance(value, int):
            return value
    return page_index + 1


def _int_value(value: Any, default: int) -> int:
    return value if isinstance(value, int) else default


def _is_bbox(value: Any) -> bool:
    return isinstance(value, list) and len(value) >= 4 and all(
        _is_finite_number(item) for item in value[:4]
    )


def _source_dimensions(page: dict[str, Any]) -> dict[str, Real] | None:
    width = _first_positive_finite(page, ("source_width", "width", "page_width"))
    height = _first_positive_finite(page, ("source_height", "height", "page_height"))
    if width is None or height is None:
        return None
    return {"source_width": width, "source_height": height}


def _first_positive_finite(page: dict[str, Any], keys: tuple[str, ...]) -> Real | None:
    for key in keys:
        value = page.get(key)
        if _is_finite_number(value) and value > 0:
            return value
    return None


def _is_finite_number(value: Any) -> bool:
    return isinstance(value, Real) and not isinstance(value, bool) and math.isfinite(value)
