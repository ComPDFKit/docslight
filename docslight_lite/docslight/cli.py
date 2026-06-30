"""Command line interface for docslight."""

from __future__ import annotations

import argparse
import importlib
import importlib.util
import json
import sys
from collections.abc import Sequence
from pathlib import Path
from typing import Any, cast

from docslight import DocSlight
from docslight.exceptions import DocSlightError
from docslight.schemas import normalize_fields
from docslight.standard_json import convert_parse_payload

WEB_EXTRA_ERROR = "Install docslight[web] to use the web command."


class CLIUsageError(Exception):
    """Expected command line usage error."""


def _add_common_options(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--mode", choices=("cloud", "local"))
    parser.add_argument("--api-key")
    parser.add_argument("--base-url")
    parser.add_argument("--local-parser")
    parser.add_argument("--local-llm-provider")
    parser.add_argument("--local-llm-model")
    parser.add_argument("--local-llm-base-url")
    parser.add_argument("--local-llm-api-key")


def _load_json_file(path: str | None, label: str) -> Any:
    if path is None:
        return None
    try:
        with Path(path).open(encoding="utf-8") as file:
            return json.load(file)
    except FileNotFoundError as exc:
        raise CLIUsageError(f"{label} file not found: {path}") from exc
    except json.JSONDecodeError as exc:
        raise CLIUsageError(f"Invalid JSON in {label} file {path}: {exc.msg}") from exc
    except UnicodeDecodeError as exc:
        raise CLIUsageError(f"Cannot read {label} file {path}: {exc}") from exc


def _load_document_types(path: str | None) -> list[Any] | None:
    document_types = _load_json_file(path, "document-types")
    if document_types is None:
        return None
    if not isinstance(document_types, list):
        raise CLIUsageError("document-types JSON must be a list")
    return document_types


def _local_llm_from_args(args: argparse.Namespace) -> dict[str, str] | None:
    values = {
        "provider": args.local_llm_provider,
        "model": args.local_llm_model,
        "base_url": args.local_llm_base_url,
        "api_key": args.local_llm_api_key,
    }
    if not any(values.values()):
        return None
    if values["provider"] is None:
        values["provider"] = "ollama"
    return {key: value for key, value in values.items() if value is not None}


def _client_from_args(args: argparse.Namespace) -> DocSlight:
    return DocSlight(
        mode=args.mode,
        api_key=args.api_key,
        base_url=args.base_url,
        local_parser=args.local_parser,
        local_llm=_local_llm_from_args(args),
    )


def _write_output(content: str, output_path: str | None) -> None:
    if output_path is None:
        sys.stdout.write(content)
        if not content.endswith("\n"):
            sys.stdout.write("\n")
        return
    path = Path(output_path)
    path.write_text(content, encoding="utf-8")
    sys.stderr.write(f"Wrote {path.resolve()}\n")


def _write_binary_output(content: bytes, output_path: str | None) -> None:
    if output_path is None:
        output_buffer = getattr(sys.stdout, "buffer", None)
        if output_buffer is None:
            raise CLIUsageError("binary output requires --output")
        output_buffer.write(content)
        return
    path = Path(output_path)
    path.write_bytes(content)
    sys.stderr.write(f"Wrote {path.resolve()}\n")


def _to_pretty_json(data: Any) -> str:
    return json.dumps(data, ensure_ascii=False, indent=2)


def run_web_app(host: str, port: int, debug: bool) -> None:
    """Run the optional Flask web application."""
    if importlib.util.find_spec("docslight.web_app") is None:
        raise CLIUsageError(WEB_EXTRA_ERROR)

    try:
        web_app = importlib.import_module("docslight.web_app")
    except ModuleNotFoundError as exc:
        if exc.name in {"flask", "werkzeug"}:
            raise CLIUsageError(WEB_EXTRA_ERROR) from exc
        raise
    _run_web_app = web_app.run_web_app
    _run_web_app(host, port, debug)


def _print_cli_error(error: Exception) -> int:
    sys.stderr.write(f"docslight: error: {error}\n")
    return 2


def build_parser() -> argparse.ArgumentParser:
    """Build the docslight command line parser."""
    parser = argparse.ArgumentParser(
        prog="docslight",
        description="Lightweight ComPDF document parsing and extraction SDK.",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    parse_parser = subparsers.add_parser("parse", help="Parse a document")
    parse_parser.add_argument("input")
    parse_parser.add_argument("--output", "-o")
    parse_parser.add_argument(
        "--format",
        choices=("markdown", "json", "standard-json", "zip"),
        default=None,
    )
    _add_common_options(parse_parser)
    parse_parser.set_defaults(func=_run_parse)

    convert_parser = subparsers.add_parser(
        "convert-parse-json",
        help="Convert local parse JSON to the standard parse JSON schema",
    )
    convert_parser.add_argument("input")
    convert_parser.add_argument("--output", "-o")
    convert_parser.set_defaults(func=_run_convert_parse_json)

    extract_parser = subparsers.add_parser("extract", help="Extract structured data")
    extract_parser.add_argument("input")
    extract_parser.add_argument("--output", "-o")
    extract_parser.add_argument("--fields")
    extract_parser.add_argument("--schema")
    extract_parser.add_argument("--document-types")
    _add_common_options(extract_parser)
    extract_parser.set_defaults(func=_run_extract)

    web_parser = subparsers.add_parser("web", help="Run the web application")
    web_parser.add_argument("--host", default="127.0.0.1")
    web_parser.add_argument("--port", type=int, default=8000)
    web_parser.add_argument("--debug", action="store_true")
    web_parser.set_defaults(func=_run_web)

    return parser


def _run_parse(args: argparse.Namespace) -> int:
    parse_format = _resolve_parse_format(args.format, args.output)
    parse_output = "json" if parse_format == "standard-json" else "markdown"
    result = _client_from_args(args).parse(args.input, output=parse_output)
    if parse_format == "zip":
        raw_archive = getattr(result, "raw_archive", None)
        if not isinstance(raw_archive, bytes):
            raise CLIUsageError("parse result did not include a ZIP archive")
        _write_binary_output(raw_archive, args.output)
    elif parse_format == "json":
        content = _to_pretty_json(result.to_json())
        _write_output(content, args.output)
    elif parse_format == "standard-json":
        content = _to_pretty_json(result.to_standard_json())
        _write_output(content, args.output)
    else:
        content = result.to_markdown()
        _write_output(content, args.output)
    return 0


def _resolve_parse_format(parse_format: str | None, output_path: str | None) -> str:
    if parse_format is not None:
        return parse_format
    if output_path is not None and Path(output_path).suffix.lower() == ".zip":
        return "zip"
    return "markdown"


def _run_convert_parse_json(args: argparse.Namespace) -> int:
    payload = _load_json_file(args.input, "parse-json")
    if not isinstance(payload, dict):
        raise CLIUsageError("parse-json JSON must be an object")
    _write_output(_to_pretty_json(convert_parse_payload(payload)), args.output)
    return 0


def _run_extract(args: argparse.Namespace) -> int:
    extract_options: dict[str, Any] = {}
    fields = normalize_fields(args.fields)
    if fields is not None:
        extract_options["fields"] = fields
    schema = _load_json_file(args.schema, "schema")
    if schema is not None:
        extract_options["schema"] = schema
    document_types = _load_document_types(args.document_types)
    if document_types is not None:
        extract_options["document_types"] = document_types

    result = _client_from_args(args).extract(args.input, **extract_options)
    _write_output(_to_pretty_json(result.to_json()), args.output)
    return 0


def _run_web(args: argparse.Namespace) -> int:
    run_web_app(args.host, args.port, args.debug)
    return 0


def main(argv: Sequence[str] | None = None) -> int:
    """Run the docslight command line interface."""
    parser = build_parser()
    args = parser.parse_args(argv)
    try:
        return cast(int, args.func(args))
    except (CLIUsageError, DocSlightError) as exc:
        return _print_cli_error(exc)


if __name__ == "__main__":
    sys.exit(main())
