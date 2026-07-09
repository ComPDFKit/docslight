"""Local document parsing utilities."""

from docslight.local.loaders import (
    IMAGE_EXTENSIONS,
    OFFICE_EXTENSIONS,
    RASTER_EXTENSIONS,
    SUPPORTED_EXTENSIONS,
    FileLoader,
    LoadedPage,
    LoadedTextDocument,
)
from docslight.local.markdown import MarkdownBuilder
from docslight.local.office_loader import OfficeMarkdownLoader
from docslight.local.paddle_parser import OCRLine, OCRPage, PaddleOCRParser
from docslight.local.pipeline import LocalPipeline

__all__ = [
    "FileLoader",
    "IMAGE_EXTENSIONS",
    "LoadedPage",
    "LoadedTextDocument",
    "LocalPipeline",
    "MarkdownBuilder",
    "OCRLine",
    "OCRPage",
    "OFFICE_EXTENSIONS",
    "OfficeMarkdownLoader",
    "PaddleOCRParser",
    "RASTER_EXTENSIONS",
    "SUPPORTED_EXTENSIONS",
]
