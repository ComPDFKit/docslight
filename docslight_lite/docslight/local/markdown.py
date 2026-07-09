"""Markdown rendering for local OCR pages."""

from __future__ import annotations

from docslight.local.paddle_parser import OCRPage


class MarkdownBuilder:
    """Build simple page-oriented Markdown from OCR output."""

    def build(self, pages: list[OCRPage]) -> str:
        """Render pages as headings followed by OCR lines."""
        parts: list[str] = []
        for page in pages:
            page_parts = [f"# Page {page.page_number}"]
            page_parts.extend(line.text for line in page.lines if line.text)
            parts.append("\n\n".join(page_parts))
        return "\n\n".join(parts)
