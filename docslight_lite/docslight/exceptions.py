"""Exception types for docslight."""

from __future__ import annotations


class DocSlightError(Exception):
    """Base exception for all docslight errors."""


class UnsupportedFormatError(DocSlightError):
    """Raised when a document format is not supported."""


class ConfigurationError(DocSlightError):
    """Raised when configuration is invalid."""


class AuthenticationError(DocSlightError):
    """Raised when cloud API authentication fails."""

    def __init__(
        self,
        message: str,
        status_code: int | None = None,
        request_id: str | None = None,
    ) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.request_id = request_id


class RateLimitError(DocSlightError):
    """Raised when a cloud API rate limit is exceeded."""

    def __init__(
        self,
        message: str,
        status_code: int | None = None,
        request_id: str | None = None,
    ) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.request_id = request_id


class DependencyMissingError(DocSlightError):
    """Raised when an optional dependency is required but missing."""


class LocalProcessingError(DocSlightError):
    """Raised when local document processing fails."""


class CloudAPIError(DocSlightError):
    """Raised when a cloud API request fails."""

    def __init__(
        self,
        message: str,
        status_code: int | None = None,
        request_id: str | None = None,
    ) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.request_id = request_id
