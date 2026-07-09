"""Result objects returned by docslight operations."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

EXTRACT_DIMENSION_KEYS = ("source_width", "source_height")


@dataclass(frozen=True)
class ParseResult:
    """Parsed document content."""

    markdown: str
    pages: list[dict[str, Any]] = field(default_factory=list)
    metadata: dict[str, Any] = field(default_factory=dict)
    raw_response: Any = None
    raw_archive: bytes | None = None

    def to_markdown(self) -> str:
        """Return the parsed markdown content."""
        return self.markdown

    def to_json(self) -> dict[str, Any]:
        """Return a JSON-serializable representation."""
        return {
            "markdown": self.markdown,
            "pages": self.pages,
            "metadata": self.metadata,
        }

    def to_standard_json(self) -> dict[str, Any]:
        """Return the ComPDF-style standard parse JSON representation."""
        from docslight.standard_json import build_standard_parse_json

        return build_standard_parse_json(
            markdown=self.markdown,
            pages=self.pages,
            metadata=self.metadata,
        )


@dataclass(frozen=True)
class ExtractResult:
    """Structured extraction result."""

    data: dict[str, Any]
    metadata: dict[str, Any] = field(default_factory=dict)
    raw_response: Any = None

    def __post_init__(self) -> None:
        """Normalize extract payloads into a fixed {results, metadata} shape."""
        results, metadata = normalize_extract_payload(self.data, self.metadata)
        object.__setattr__(self, "data", results)
        object.__setattr__(self, "metadata", metadata)

    def to_json(self) -> dict[str, Any]:
        """Return a JSON-serializable representation."""
        return {
            "results": self.data,
            "metadata": self.metadata,
        }


def normalize_extract_payload(
    data: dict[str, Any] | None,
    metadata: dict[str, Any] | None = None,
) -> tuple[dict[str, Any], dict[str, Any]]:
    """Normalize extract payloads from cloud/local providers."""
    payload = dict(data) if isinstance(data, dict) else {}
    normalized_metadata = dict(payload.get("metadata", {})) if isinstance(payload.get("metadata"), dict) else {}
    if isinstance(metadata, dict):
        normalized_metadata.update(metadata)

    if isinstance(payload.get("results"), dict):
        results = dict(payload["results"])
    else:
        results = {key: value for key, value in payload.items() if key not in {"message", "metadata"}}

    fields = results.pop("fields", None)
    if isinstance(fields, dict):
        results = {**fields, **results}

    for key in EXTRACT_DIMENSION_KEYS:
        if key in results and key not in normalized_metadata:
            normalized_metadata[key] = results.pop(key)

    return results, normalized_metadata
