"""Lightweight ComPDF document parsing and extraction SDK."""

from docslight.client import DocSlight
from docslight.config import (
    DEFAULT_BASE_URL,
    DEFAULT_CONFIG_PATH,
    VALID_MODES,
    DocSlightConfig,
)
from docslight.exceptions import (
    AuthenticationError,
    CloudAPIError,
    ConfigurationError,
    DependencyMissingError,
    DocSlightError,
    LocalProcessingError,
    RateLimitError,
    UnsupportedFormatError,
)
from docslight.result import ExtractResult, ParseResult

__version__ = "0.1.4"

__all__ = [
    "AuthenticationError",
    "CloudAPIError",
    "ConfigurationError",
    "DEFAULT_BASE_URL",
    "DEFAULT_CONFIG_PATH",
    "DependencyMissingError",
    "DocSlight",
    "DocSlightConfig",
    "DocSlightError",
    "ExtractResult",
    "LocalProcessingError",
    "ParseResult",
    "RateLimitError",
    "UnsupportedFormatError",
    "VALID_MODES",
    "__version__",
]
