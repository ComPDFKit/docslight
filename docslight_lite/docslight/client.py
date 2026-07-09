"""Public SDK router for document parsing and extraction."""

from __future__ import annotations

from pathlib import Path
from typing import Any, cast

from docslight.cloud import CloudClient
from docslight.config import DocSlightConfig
from docslight.result import ExtractResult, ParseResult
from docslight.schemas import build_extract_schema, normalize_fields


class DocSlight:
    """Route SDK calls to cloud or local document processors."""

    def __init__(
        self,
        mode: str | None = None,
        api_key: str | None = None,
        base_url: str | None = None,
        timeout: float | None = None,
        local_parser: str | None = None,
        local_llm: dict[str, Any] | None = None,
        cloud_client: Any = None,
        local_pipeline: Any = None,
    ) -> None:
        self.config = DocSlightConfig.from_sources(
            mode=mode,
            api_key=api_key,
            base_url=base_url,
            timeout=timeout,
            local_parser=local_parser,
            local_llm=local_llm,
        )
        self._cloud_client = cloud_client
        self._local_pipeline = local_pipeline

    def parse(self, path: Path | str, output: str = "markdown", **options: Any) -> ParseResult:
        """Parse a document into markdown by default."""
        if output != "markdown":
            options["output"] = output
        return cast(ParseResult, self._processor().parse(path, **options))

    def extract(
        self,
        path: Path | str,
        fields: list[str] | str | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
        **options: Any,
    ) -> ExtractResult:
        """Extract structured data from a document."""
        normalized_fields = normalize_fields(fields)
        extract_kwargs = dict(options)
        if normalized_fields is not None:
            extract_kwargs["fields"] = normalized_fields
            derived_schema = build_extract_schema(normalized_fields)
            if schema is None and derived_schema is not None:
                extract_kwargs["schema"] = derived_schema
        if schema is not None:
            extract_kwargs["schema"] = schema
        if document_types is not None:
            extract_kwargs["document_types"] = document_types
        return cast(ExtractResult, self._processor().extract(path, **extract_kwargs))

    def parse_batch(self, paths: list[Path | str], **options: Any) -> list[ParseResult]:
        """Parse documents sequentially."""
        return [self.parse(path, **options) for path in paths]

    def extract_batch(self, paths: list[Path | str], **options: Any) -> list[ExtractResult]:
        """Extract data from documents sequentially."""
        return [self.extract(path, **options) for path in paths]

    def _processor(self) -> Any:
        if self.config.mode == "local":
            if self._local_pipeline is None:
                self._local_pipeline = self._build_local_pipeline()
            return self._local_pipeline

        if self._cloud_client is None:
            self._cloud_client = CloudClient(
                self.config.api_key,
                self.config.base_url,
                self.config.timeout,
            )
        return self._cloud_client

    def _build_local_pipeline(self) -> Any:
        from docslight.local.pipeline import LocalPipeline

        return LocalPipeline.from_config(self.config)
