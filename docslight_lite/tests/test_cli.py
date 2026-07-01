from __future__ import annotations

import importlib
import json
import subprocess
import sys
from pathlib import Path
from typing import Any

import pytest

from docslight import cli
from docslight.exceptions import CloudAPIError


class FakeResult:
    def __init__(
        self,
        data: dict[str, Any] | None = None,
        markdown: str = "",
        raw_archive: bytes | None = b"zip-bytes",
    ) -> None:
        self.data = data or {}
        self.markdown = markdown
        self.raw_archive = raw_archive

    def to_markdown(self) -> str:
        return self.markdown

    def to_json(self) -> dict[str, Any]:
        return self.data

    def to_standard_json(self) -> dict[str, Any]:
        return {"result": self.data, "code": 200, "message": "Success"}


class FakeDocSlight:
    instances: list[FakeDocSlight] = []

    def __init__(self, **kwargs: Any) -> None:
        self.init_kwargs = kwargs
        self.parse_calls: list[tuple[str, dict[str, Any]]] = []
        self.extract_calls: list[tuple[str, dict[str, Any]]] = []
        FakeDocSlight.instances.append(self)

    def parse(self, input_path: str, **kwargs: Any) -> FakeResult:
        self.parse_calls.append((input_path, kwargs))
        return FakeResult(
            {
                "markdown": "# Parsed",
                "path": input_path,
                "options": kwargs,
            },
            markdown="# Parsed",
        )

    def extract(self, input_path: str, **kwargs: Any) -> FakeResult:
        self.extract_calls.append((input_path, kwargs))
        return FakeResult({"path": input_path, "options": kwargs})


def install_fake_docslight(monkeypatch: Any) -> None:
    FakeDocSlight.instances = []
    monkeypatch.setattr(cli, "DocSlight", FakeDocSlight)


def test_parse_command_writes_markdown_file(
    tmp_path: Path,
    monkeypatch: Any,
    capsys: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    output_path = tmp_path / "parsed.md"

    exit_code = cli.main(["parse", "sample.pdf", "--output", str(output_path)])

    assert exit_code == 0
    assert output_path.read_text(encoding="utf-8") == "# Parsed"
    assert FakeDocSlight.instances[0].parse_calls == [("sample.pdf", {"output": "markdown"})]
    assert str(output_path.resolve()) in capsys.readouterr().err


def test_parse_command_writes_zip_archive_when_requested(
    tmp_path: Path,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    output_path = tmp_path / "parsed.zip"

    exit_code = cli.main(["parse", "sample.pdf", "--format", "zip", "--output", str(output_path)])

    assert exit_code == 0
    assert output_path.read_bytes() == b"zip-bytes"
    assert FakeDocSlight.instances[0].parse_calls == [("sample.pdf", {"output": "markdown"})]


def test_parse_command_infers_zip_format_from_output_extension(
    tmp_path: Path,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    output_path = tmp_path / "parsed.zip"

    exit_code = cli.main(["parse", "sample.pdf", "--output", str(output_path)])

    assert exit_code == 0
    assert output_path.read_bytes() == b"zip-bytes"


def test_parse_command_reports_missing_zip_archive(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    class NoArchiveDocSlight(FakeDocSlight):
        def parse(self, input_path: str, **kwargs: Any) -> FakeResult:
            return FakeResult(raw_archive=None)

    FakeDocSlight.instances = []
    monkeypatch.setattr(cli, "DocSlight", NoArchiveDocSlight)

    exit_code = cli.main(["parse", "sample.pdf", "--format", "zip", "-o", "out.zip"])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "ZIP archive" in captured.err
    assert "Traceback" not in captured.err


def test_parse_command_reports_cloud_errors_without_traceback(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    class FailingDocSlight(FakeDocSlight):
        def parse(self, input_path: str, **kwargs: Any) -> FakeResult:
            raise CloudAPIError("Cloud API error 404", status_code=404)

    FakeDocSlight.instances = []
    monkeypatch.setattr(cli, "DocSlight", FailingDocSlight)

    exit_code = cli.main(["parse", "sample.pdf", "--mode", "cloud"])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "Cloud API error 404" in captured.err
    assert "Traceback" not in captured.err


def test_extract_command_prints_json_and_parses_fields(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)

    exit_code = cli.main(["extract", "invoice.pdf", "--fields", "invoice_no,total"])

    assert exit_code == 0
    assert json.loads(capsys.readouterr().out) == {
        "path": "invoice.pdf",
        "options": {"fields": ["invoice_no", "total"]},
    }


def test_parse_command_prints_standard_json(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)

    exit_code = cli.main(["parse", "sample.pdf", "--format", "standard-json"])

    assert exit_code == 0
    assert json.loads(capsys.readouterr().out) == {
        "result": {
            "markdown": "# Parsed",
            "path": "sample.pdf",
            "options": {"output": "json"},
        },
        "code": 200,
        "message": "Success",
    }


def test_convert_parse_json_command_writes_standard_json(tmp_path: Path) -> None:
    input_path = tmp_path / "parse.json"
    input_path.write_text(
        json.dumps(
            {
                "markdown": "Invoice",
                "metadata": {"document_type": "image"},
                "pages": [
                    {
                        "page_number": 1,
                        "width": 10,
                        "height": 20,
                        "parsing_res_list": [
                            {
                                "block_id": 0,
                                "block_label": "text",
                                "block_content": "Invoice",
                                "block_bbox": [1, 2, 3, 4],
                            }
                        ],
                    }
                ],
            }
        ),
        encoding="utf-8",
    )
    output_path = tmp_path / "standard.json"

    exit_code = cli.main(["convert-parse-json", str(input_path), "-o", str(output_path)])

    assert exit_code == 0
    converted = json.loads(output_path.read_text(encoding="utf-8"))
    assert converted["file_type"] == "IMAGE"
    assert converted["result"]["pages"][0]["structured"][0]["pos"] == [
        1,
        2,
        3,
        2,
        3,
        4,
        1,
        4,
    ]


def test_extract_reads_schema_file_and_writes_json_output(
    tmp_path: Path,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    schema_path = tmp_path / "schema.json"
    schema_path.write_text(
        json.dumps({"type": "object", "properties": {"total": {"type": "number"}}}),
        encoding="utf-8",
    )
    output_path = tmp_path / "extract.json"

    exit_code = cli.main(
        [
            "extract",
            "invoice.pdf",
            "--schema",
            str(schema_path),
            "--output",
            str(output_path),
        ]
    )

    assert exit_code == 0
    assert json.loads(output_path.read_text(encoding="utf-8")) == {
        "path": "invoice.pdf",
        "options": {
            "schema": {"type": "object", "properties": {"total": {"type": "number"}}}
        },
    }


def test_extract_passes_local_llm_options_with_local_mode(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)

    exit_code = cli.main(
        [
            "extract",
            "local.pdf",
            "--mode",
            "local",
            "--local-parser",
            "pymupdf",
            "--local-llm-model",
            "llama3",
            "--local-llm-base-url",
            "http://localhost:11434",
            "--local-llm-api-key",
            "local-key",
        ]
    )

    assert exit_code == 0
    assert FakeDocSlight.instances[0].init_kwargs == {
        "mode": "local",
        "api_key": None,
        "base_url": None,
        "local_parser": "pymupdf",
        "local_llm": {
            "provider": "ollama",
            "model": "llama3",
            "base_url": "http://localhost:11434",
            "api_key": "local-key",
        },
    }
    assert json.loads(capsys.readouterr().out)["options"] == {}


def test_web_command_calls_run_web_app(monkeypatch: Any) -> None:
    calls: list[tuple[str, int, bool]] = []
    monkeypatch.setattr(
        cli,
        "run_web_app",
        lambda host, port, debug: calls.append((host, port, debug)),
    )

    exit_code = cli.main(["web", "--host", "0.0.0.0", "--port", "9000", "--debug"])

    assert exit_code == 0
    assert calls == [("0.0.0.0", 9000, True)]


def test_missing_schema_file_returns_cli_error(
    tmp_path: Path,
    capsys: Any,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    missing_path = tmp_path / "missing-schema.json"

    exit_code = cli.main(["extract", "sample.pdf", "--schema", str(missing_path)])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "schema" in captured.err
    assert str(missing_path) in captured.err
    assert "Traceback" not in captured.err


def test_invalid_schema_json_returns_cli_error(
    tmp_path: Path,
    capsys: Any,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    schema_path = tmp_path / "schema.json"
    schema_path.write_text("{invalid", encoding="utf-8")

    exit_code = cli.main(["extract", "sample.pdf", "--schema", str(schema_path)])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "Invalid JSON" in captured.err
    assert "Traceback" not in captured.err


def test_document_types_json_must_be_list(
    tmp_path: Path,
    capsys: Any,
    monkeypatch: Any,
) -> None:
    install_fake_docslight(monkeypatch)
    document_types_path = tmp_path / "types.json"
    document_types_path.write_text(json.dumps({"type": "invoice"}), encoding="utf-8")

    exit_code = cli.main(
        ["extract", "sample.pdf", "--document-types", str(document_types_path)]
    )

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "document-types JSON must be a list" in captured.err
    assert "Traceback" not in captured.err


def test_web_without_web_ui_returns_cli_error(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    original_find_spec = importlib.util.find_spec

    def fake_find_spec(name: str, package: str | None = None) -> Any:
        if name == "docslight.web_app":
            return None
        return original_find_spec(name, package)

    monkeypatch.setattr(importlib.util, "find_spec", fake_find_spec)

    exit_code = cli.main(["web"])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "docslight[web]" in captured.err or "web command" in captured.err
    assert "Traceback" not in captured.err


@pytest.mark.parametrize("missing_module", ["flask", "werkzeug"])
def test_web_missing_optional_dependency_returns_cli_error(
    capsys: Any,
    monkeypatch: Any,
    missing_module: str,
) -> None:
    monkeypatch.setattr(importlib.util, "find_spec", lambda name: object())

    def fake_import_module(name: str) -> Any:
        raise ModuleNotFoundError(f"No module named '{missing_module}'", name=missing_module)

    monkeypatch.setattr(importlib, "import_module", fake_import_module)

    exit_code = cli.main(["web"])

    captured = capsys.readouterr()
    assert exit_code == 2
    assert "docslight[web]" in captured.err
    assert "Traceback" not in captured.err


def test_parse_internal_value_error_is_not_cli_usage_error(
    capsys: Any,
    monkeypatch: Any,
) -> None:
    class BuggyDocSlight:
        def __init__(self, **kwargs: Any) -> None:
            pass

        def parse(self, input_path: str, **kwargs: Any) -> FakeResult:
            raise ValueError("internal bug")

    monkeypatch.setattr(cli, "DocSlight", BuggyDocSlight)

    with pytest.raises(ValueError, match="internal bug"):
        cli.main(["parse", "sample.pdf"])

    assert "docslight: error" not in capsys.readouterr().err


def test_web_app_internal_import_error_propagates(monkeypatch: Any) -> None:
    monkeypatch.setattr(importlib.util, "find_spec", lambda name: object())

    def fake_import_module(name: str) -> Any:
        raise ModuleNotFoundError("missing_dependency")

    monkeypatch.setattr(importlib, "import_module", fake_import_module)

    with pytest.raises(ModuleNotFoundError, match="missing_dependency"):
        cli.run_web_app("127.0.0.1", 8000, False)


def test_module_help_includes_subcommands() -> None:
    result = subprocess.run(
        [sys.executable, "-m", "docslight.cli", "--help"],
        check=False,
        capture_output=True,
        text=True,
    )

    assert result.returncode == 0
    assert "parse" in result.stdout
    assert "extract" in result.stdout
    assert "web" in result.stdout
