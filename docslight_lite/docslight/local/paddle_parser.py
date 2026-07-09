"""PaddleOCR PP-StructureV3 parser adapter for local files."""

from __future__ import annotations

import os
import sys
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from docslight.exceptions import DependencyMissingError, LocalProcessingError
from docslight.local.loaders import LOCAL_DEPS_MESSAGE
from docslight.result import ParseResult

DEFAULT_PPSTRUCTUREV3_OPTIONS: dict[str, Any] = {
    "use_doc_orientation_classify": False,
    "use_doc_unwarping": False,
    "use_textline_orientation": False,
    "use_formula_recognition": False,
    "use_chart_recognition": False,
    # "layout_detection_model_name": "PP-DocLayout-M",
    "use_region_detection": False,
    "text_recognition_model_name": "PP-OCRv5_mobile_rec",
    "text_detection_model_name": "PP-OCRv5_mobile_det",

}


@dataclass(frozen=True)
class OCRLine:
    """One OCR text line."""

    text: str
    bbox: list[Any] | None
    confidence: float | None

    def to_json(self) -> dict[str, Any]:
        """Return a JSON-serializable representation."""
        return {
            "text": self.text,
            "bbox": self.bbox,
            "confidence": self.confidence,
        }


@dataclass(frozen=True)
class OCRPage:
    """OCR result for one page."""

    page_number: int
    lines: list[OCRLine]

    def to_json(self) -> dict[str, Any]:
        """Return a JSON-serializable representation."""
        return {
            "page_number": self.page_number,
            "lines": [line.to_json() for line in self.lines],
        }


class PaddleOCRParser:
    """Run PP-StructureV3 over local PDF and image files."""

    def __init__(self, pipeline: Any | None = None, **pipeline_options: Any) -> None:
        self.pipeline_options = {**DEFAULT_PPSTRUCTUREV3_OPTIONS, **pipeline_options}
        self._pipeline: Any = pipeline
        self._device_label: str | None = None

    def parse(self, path: Path | str) -> ParseResult:
        """Parse a local PDF or image into structured Markdown and page JSON."""
        pipeline = self._load_pipeline()
        raw_results = self._predict(pipeline, Path(path))
        markdown_pages, pages = self._normalize_results(raw_results)
        return ParseResult(
            markdown=self._build_markdown(pipeline, markdown_pages),
            pages=pages,
        )

    def _load_pipeline(self) -> Any:
        if self._pipeline is not None:
            return self._pipeline
        os.environ.setdefault("PADDLE_PDX_ENABLE_MKLDNN_BYDEFAULT", "0")
        try:
            from paddleocr import PPStructureV3
        except (ImportError, ModuleNotFoundError) as exc:  # pragma: no cover - depends on env
            raise DependencyMissingError(LOCAL_DEPS_MESSAGE) from exc
        self.pipeline_options.setdefault("device", _detect_device())
        self._device_label = (
            "GPU" if str(self.pipeline_options["device"]).startswith("gpu") else "CPU"
        )
        print(
            f"DocSlight local PP-StructureV3 inference device: {self._device_label}",
            file=sys.stderr,
        )
        self._pipeline = PPStructureV3(**self.pipeline_options)
        return self._pipeline

    def _predict(self, pipeline: Any, path: Path) -> list[Any]:
        try:
            raw_results = pipeline.predict(input=str(path))
        except LocalProcessingError:
            raise
        except Exception as exc:  # noqa: BLE001
            raise LocalProcessingError(f"Local PaddleOCR parsing failed: {exc}") from exc
        if raw_results is None:
            return []
        if isinstance(raw_results, list):
            return raw_results
        if isinstance(raw_results, Iterable) and not isinstance(raw_results, (str, bytes, dict)):
            return list(raw_results)
        raise LocalProcessingError("Unexpected PP-StructureV3 result format")

    def _normalize_results(self, results: list[Any]) -> tuple[list[Any], list[dict[str, Any]]]:
        markdown_pages: list[Any] = []
        pages: list[dict[str, Any]] = []
        for result in results:
            if result is None:
                continue
            if not hasattr(result, "markdown") or not hasattr(result, "json"):
                raise LocalProcessingError("Unexpected PP-StructureV3 result format")
            markdown_pages.append(result.markdown)
            pages.append(self._normalize_page_payload(result.json))
        return markdown_pages, pages

    def _normalize_page_payload(self, payload: Any) -> dict[str, Any]:
        if callable(payload):
            payload = payload()
        if not isinstance(payload, dict):
            raise LocalProcessingError("Unexpected PP-StructureV3 result format")
        data = payload.get("res", payload)
        if not isinstance(data, dict):
            raise LocalProcessingError("Unexpected PP-StructureV3 result format")
        return data

    def _build_markdown(self, pipeline: Any, markdown_pages: list[Any]) -> str:
        if not markdown_pages:
            return ""
        if len(markdown_pages) == 1:
            return self._markdown_to_text(markdown_pages[0])
        combined_markdown = pipeline.concatenate_markdown_pages(markdown_pages)
        return self._markdown_to_text(combined_markdown)

    def _markdown_to_text(self, markdown: Any) -> str:
        if markdown is None:
            return ""
        if isinstance(markdown, str):
            return markdown
        if isinstance(markdown, dict):
            text = markdown.get("markdown_texts", markdown.get("markdown", ""))
            if isinstance(text, str):
                return text
        raise LocalProcessingError("Unexpected PP-StructureV3 result format")


def _detect_device() -> str:
    try:
        import paddle
    except ModuleNotFoundError:
        return "cpu"

    try:
        device = getattr(paddle, "device", None)
        cuda = getattr(device, "cuda", None) if device is not None else None
        if cuda is None or cuda.device_count() <= 0:
            return "cpu"
        if device is not None and device.is_compiled_with_cuda():
            return "gpu"
        if hasattr(paddle, "is_compiled_with_cuda") and paddle.is_compiled_with_cuda():
            return "gpu"
    except Exception:
        return "cpu"
    return "cpu"
