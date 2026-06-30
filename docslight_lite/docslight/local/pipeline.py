"""Local document parsing pipeline."""

from __future__ import annotations

import importlib
import inspect
import io
import json
import zipfile
from dataclasses import replace
from pathlib import Path
from typing import Any, cast

from docslight.config import DocSlightConfig
from docslight.exceptions import ConfigurationError, UnsupportedFormatError
from docslight.local.layout_blocks import build_layout_blocks
from docslight.local.loaders import OFFICE_EXTENSIONS, RASTER_EXTENSIONS, FileLoader
from docslight.local.markdown import MarkdownBuilder
from docslight.local.office_loader import LEGACY_OFFICE_EXTENSIONS, OfficeMarkdownLoader
from docslight.local.paddle_parser import PaddleOCRParser
from docslight.result import ExtractResult, ParseResult

FIXED_LLM_PARAMETERS = {"markdown", "fields", "schema", "document_types"}


def _cloud_extract_result(data: dict[str, Any], *, include_tables: bool = True) -> dict[str, Any]:
    if _is_page_grouped_result(data):
        return _filter_tables(data) if not include_tables else data

    table_bboxes = data.get("_table_bboxes") if isinstance(data.get("_table_bboxes"), dict) else {}
    grouped: dict[str, dict[str, Any]] = {}
    for key, value in data.items():
        if key == "_table_bboxes":
            continue
        if key == "tables" and isinstance(value, dict):
            if not include_tables:
                continue
            for table_name, rows in value.items():
                page_key = _page_key(table_bboxes.get(table_name))
                page = grouped.setdefault(page_key, {})
                tables = page.setdefault("tables", {})
                if isinstance(tables, dict):
                    tables[table_name] = rows
            continue
        page_key = _page_key(value)
        grouped.setdefault(page_key, {})[key] = _cloud_extract_value(value)
    return grouped or {"Page_1": {}}


def _filter_tables(data: dict[str, Any]) -> dict[str, Any]:
    filtered: dict[str, Any] = {}
    for key, value in data.items():
        if key == "_table_bboxes":
            continue
        if isinstance(value, dict):
            page = {page_key: page_value for page_key, page_value in value.items() if page_key != "tables"}
            filtered[key] = page
        else:
            filtered[key] = value
    return filtered


def _should_include_tables(fields: list[str] | dict[str, Any] | None, schema: dict[str, Any] | None) -> bool:
    if isinstance(fields, dict) and isinstance(fields.get("tableHeaders"), dict):
        return bool(fields["tableHeaders"])
    if isinstance(schema, dict):
        properties = schema.get("properties")
        return isinstance(properties, dict) and "tables" in properties
    return False


def _is_page_grouped_result(data: dict[str, Any]) -> bool:
    return bool(data) and all(isinstance(key, str) and key.startswith("Page_") for key in data)


def _page_key(value: Any) -> str:
    if isinstance(value, dict):
        page_id = value.get("page_id") or value.get("page")
        if page_id is not None:
            return f"Page_{page_id}"
        bboxes = value.get("bboxes") or value.get("bbox")
        if isinstance(bboxes, list):
            candidates = bboxes if bboxes and isinstance(bboxes[0], dict) else [value]
            for candidate in candidates:
                if isinstance(candidate, dict):
                    page_id = candidate.get("page_id") or candidate.get("page")
                    if page_id is not None:
                        return f"Page_{page_id}"
    return "Page_1"


def _cloud_extract_value(value: Any) -> Any:
    if isinstance(value, dict) and "value" in value:
        return "" if value["value"] is None else value["value"]
    return "" if value is None else value


def _with_local_parse_outputs(result: ParseResult) -> ParseResult:
    raw_response = result.to_standard_json()
    return replace(
        result,
        raw_response=raw_response,
        raw_archive=_build_parse_archive(result.markdown, raw_response),
    )


def _build_parse_archive(markdown: str, payload: dict[str, Any]) -> bytes:
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("result.md", markdown)
        archive.writestr(
            "result.json",
            json.dumps(payload, ensure_ascii=False, indent=2),
        )
    return buffer.getvalue()


def _build_llm_extractor(config: dict[str, Any]) -> Any:
    """Build the local LLM extractor while keeping Task 8 dependency lazy."""
    module = importlib.import_module("docslight.local.llm_extractor")
    return module.LocalLLMExtractor(module.provider_from_config(config))


class LocalPipeline:
    """Parse and extract documents using local components."""

    def __init__(
        self,
        loader: Any | None = None,
        parser: Any | None = None,
        office_loader: Any | None = None,
        markdown_builder: MarkdownBuilder | None = None,
        llm_extractor: Any | None = None,
    ) -> None:
        self.loader = loader or FileLoader()
        self.parser = parser or PaddleOCRParser()
        self.office_loader = office_loader or OfficeMarkdownLoader()
        self.markdown_builder = markdown_builder or MarkdownBuilder()
        self.llm_extractor = llm_extractor

    @classmethod
    def from_config(cls, config: DocSlightConfig) -> LocalPipeline:
        """Build a local pipeline from SDK configuration."""
        llm_extractor = None
        if config.local_llm:
            try:
                llm_extractor = _build_llm_extractor(config.local_llm)
            except ModuleNotFoundError as exc:
                if exc.name == "docslight.local.llm_extractor":
                    raise ConfigurationError("local_llm support is not available") from exc
                raise
        return cls(llm_extractor=llm_extractor)

    def parse(self, path: Path | str, **options: Any) -> ParseResult:
        """Parse a local document into Markdown."""
        source_path = Path(path)
        suffix = source_path.suffix.lower()
        if suffix in LEGACY_OFFICE_EXTENSIONS:
            raise UnsupportedFormatError("Legacy Office files must convert to DOCX, PPTX, or XLSX")
        if suffix in OFFICE_EXTENSIONS:
            return self._parse_office(source_path, options)
        if suffix in RASTER_EXTENSIONS:
            return self._parse_raster(source_path, suffix, options)
        raise UnsupportedFormatError(f"Unsupported local format: {suffix or source_path.name}")

    def extract(
        self,
        path: Path | str,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
        **options: Any,
    ) -> ExtractResult:
        """Extract structured data from a local document."""
        llm_extractor = self.llm_extractor
        if llm_extractor is None:
            raise ConfigurationError("local_llm must be configured")
        parsed = self.parse(path, **options)
        candidate_options = dict(options)
        layout_blocks = build_layout_blocks(parsed.pages)
        if layout_blocks:
            candidate_options["layout_blocks"] = layout_blocks
        llm_options = self._supported_llm_options(llm_extractor, candidate_options)
        extracted = cast(ExtractResult, llm_extractor.extract(
            parsed.markdown,
            fields=fields,
            schema=schema,
            document_types=document_types,
            **llm_options,
        ))
        return replace(
            extracted,
            data=_cloud_extract_result(
                extracted.data,
                include_tables=_should_include_tables(fields, schema),
            ),
            metadata={**extracted.metadata, **parsed.metadata},
        )

    def _parse_raster(self, path: Path, suffix: str, options: dict[str, Any]) -> ParseResult:
        parsed = self.parser.parse(path)
        document_type = "pdf" if suffix == ".pdf" else "image"
        metadata = self._merge_metadata(
            {
                "engine": "ppstructurev3-local",
                "mode": "local",
                "document_type": document_type,
                "page_count": len(parsed.pages),
            },
            {**parsed.metadata, **options},
        )
        result = replace(parsed, metadata=metadata)
        return _with_local_parse_outputs(result)

    def _parse_office(self, path: Path, options: dict[str, Any]) -> ParseResult:
        document = self.office_loader.load(path)
        metadata = self._merge_metadata({"mode": "local", **document.metadata}, options)
        result = ParseResult(markdown=document.markdown, pages=[], metadata=metadata)
        return _with_local_parse_outputs(result)

    def _merge_metadata(
        self,
        base_metadata: dict[str, Any],
        options: dict[str, Any],
    ) -> dict[str, Any]:
        metadata = dict(base_metadata)
        for key, value in options.items():
            if key not in metadata:
                metadata[key] = value
        return metadata

    def _supported_llm_options(self, llm_extractor: Any, options: dict[str, Any]) -> dict[str, Any]:
        candidate_options = {
            key: value for key, value in options.items() if key not in FIXED_LLM_PARAMETERS
        }
        signature = inspect.signature(llm_extractor.extract)
        parameters = signature.parameters
        if any(parameter.kind == inspect.Parameter.VAR_KEYWORD for parameter in parameters.values()):
            return candidate_options
        return {key: value for key, value in candidate_options.items() if key in parameters}
