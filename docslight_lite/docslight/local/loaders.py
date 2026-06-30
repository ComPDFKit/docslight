"""Local file loading utilities."""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any

from docslight.exceptions import DependencyMissingError, UnsupportedFormatError

LOCAL_DEPS_MESSAGE = "Install local dependencies with: pip install 'docslight[local]'"

IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".webp"}
RASTER_EXTENSIONS = {".pdf", *IMAGE_EXTENSIONS}
OFFICE_EXTENSIONS = {".docx", ".pptx", ".xlsx"}
SUPPORTED_EXTENSIONS = RASTER_EXTENSIONS | OFFICE_EXTENSIONS


def _open_pillow_image(path: Path) -> Any:
    """Open an image with Pillow while keeping the dependency optional."""
    try:
        from PIL import Image
    except ModuleNotFoundError as exc:  # pragma: no cover - depends on environment
        raise DependencyMissingError(LOCAL_DEPS_MESSAGE) from exc
    return Image.open(path)


@dataclass(frozen=True)
class LoadedPage:
    """Rasterized page ready for OCR."""

    page_number: int
    image: Any
    width: int
    height: int
    source_path: Path


@dataclass(frozen=True)
class LoadedTextDocument:
    """Text document loaded directly as Markdown."""

    markdown: str
    metadata: dict[str, Any]


class FileLoader:
    """Load PDFs and images for local OCR."""

    def load(self, path: Path | str) -> list[LoadedPage]:
        """Load a PDF or image path into OCR pages."""
        source_path = Path(path)
        suffix = source_path.suffix.lower()
        if suffix == ".pdf":
            return self._load_pdf(source_path)
        if suffix in IMAGE_EXTENSIONS:
            return [self._load_image(source_path)]
        if suffix in OFFICE_EXTENSIONS:
            raise UnsupportedFormatError("Office files are handled by OfficeMarkdownLoader")
        raise UnsupportedFormatError(f"Unsupported local format: {suffix or source_path.name}")

    def _load_pdf(self, path: Path) -> list[LoadedPage]:
        try:
            import fitz
            from PIL import Image
        except ModuleNotFoundError as exc:  # pragma: no cover - depends on environment
            raise DependencyMissingError(LOCAL_DEPS_MESSAGE) from exc

        pages: list[LoadedPage] = []
        with fitz.open(path) as document:
            for index, page in enumerate(document, start=1):
                pixmap = page.get_pixmap()
                mode = "RGBA" if pixmap.alpha else "RGB"
                image = Image.frombytes(mode, (pixmap.width, pixmap.height), pixmap.samples)
                pages.append(
                    LoadedPage(
                        page_number=index,
                        image=image,
                        width=image.width,
                        height=image.height,
                        source_path=path,
                    )
                )
        return pages

    def _load_image(self, path: Path) -> LoadedPage:
        with _open_pillow_image(path) as image:
            rgb_image = image.convert("RGB").copy()
        return LoadedPage(
            page_number=1,
            image=rgb_image,
            width=rgb_image.width,
            height=rgb_image.height,
            source_path=path,
        )
