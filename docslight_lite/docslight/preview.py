"""Preview rendering helpers for the local API server."""

from __future__ import annotations

import base64
from pathlib import Path
from typing import Any

from docslight.exceptions import DependencyMissingError, LocalProcessingError

PDF_PREVIEW_DEPENDENCY_MESSAGE = (
    "Install docslight[local] to enable PDF preview rendering."
)


def render_pdf_preview(path: Path, max_pages: int | None = None) -> dict[str, Any]:
    """Render PDF pages to PNG data URLs for browser overlay highlighting."""
    try:
        import fitz  # type: ignore[import-not-found]
    except ModuleNotFoundError as exc:
        raise DependencyMissingError(PDF_PREVIEW_DEPENDENCY_MESSAGE) from exc

    pages: list[dict[str, Any]] = []
    try:
        with fitz.open(path) as document:
            page_count = len(document) if max_pages is None else min(len(document), max_pages)
            for page_index in range(page_count):
                page = document.load_page(page_index)
                pixmap = page.get_pixmap(matrix=fitz.Matrix(1, 1), alpha=False)
                image = base64.b64encode(pixmap.tobytes("png")).decode("ascii")
                rect = page.rect
                pages.append(
                    {
                        "page_id": page_index + 1,
                        "page_index": page_index,
                        "width": float(rect.width),
                        "height": float(rect.height),
                        "image": f"data:image/png;base64,{image}",
                    }
                )
    except Exception as exc:
        if isinstance(exc, DependencyMissingError):
            raise
        raise LocalProcessingError("PDF preview rendering failed") from exc

    return {"kind": "pdf", "pages": pages}
