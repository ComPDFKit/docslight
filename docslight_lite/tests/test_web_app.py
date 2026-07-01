from __future__ import annotations

import json
import logging
from io import BytesIO
from pathlib import Path
from typing import Any

import pytest

from docslight import web_app
from docslight.exceptions import (
    AuthenticationError,
    DependencyMissingError,
    LocalProcessingError,
    RateLimitError,
)
from docslight.preview import render_pdf_preview
from docslight.result import ExtractResult, ParseResult
from docslight.web_app import create_app


class FakeDocSlight:
    instances: list[FakeDocSlight] = []
    seen_paths: list[Path] = []
    parse_error: Exception | None = None
    parse_result: ParseResult | None = None

    def __init__(self, **kwargs: Any) -> None:
        self.init_kwargs = kwargs
        self.parse_calls: list[Path] = []
        self.extract_calls: list[tuple[Path, dict[str, Any]]] = []
        FakeDocSlight.instances.append(self)

    def parse(self, path: str | Path) -> ParseResult:
        path_obj = Path(path)
        self.parse_calls.append(path_obj)
        FakeDocSlight.seen_paths.append(path_obj)
        if FakeDocSlight.parse_error is not None:
            raise FakeDocSlight.parse_error
        if FakeDocSlight.parse_result is not None:
            return FakeDocSlight.parse_result
        return ParseResult(
            markdown="# Parsed\n\nAPI result",
            pages=[{"page": 1, "text": "API result"}],
            metadata={"filename": path_obj.name, "mode": self.init_kwargs.get("mode")},
        )

    def extract(self, path: str | Path, **kwargs: Any) -> ExtractResult:
        path_obj = Path(path)
        self.extract_calls.append((path_obj, kwargs))
        FakeDocSlight.seen_paths.append(path_obj)
        return ExtractResult(
            data={"invoice_number": "INV-100", "total": 42},
            metadata={"options": kwargs, "local_llm": self.init_kwargs.get("local_llm")},
        )


@pytest.fixture(autouse=True)
def reset_fake_docslight() -> None:
    FakeDocSlight.instances = []
    FakeDocSlight.seen_paths = []
    FakeDocSlight.parse_error = None
    FakeDocSlight.parse_result = None


@pytest.fixture()
def client() -> Any:
    app = create_app(FakeDocSlight)
    app.config.update(TESTING=True)
    return app.test_client()


def upload(filename: str, content: bytes = b"content") -> tuple[Any, str]:
    return BytesIO(content), filename


def test_root_returns_api_status(client: Any) -> None:
    response = client.get("/")

    assert response.status_code == 200
    assert response.get_json() == {"service": "docslight-web", "status": "healthy"}


def test_page_routes_are_not_served(client: Any) -> None:
    assert client.get("/parse").status_code == 404
    assert client.get("/extract").status_code == 404


def test_health_endpoint(client: Any) -> None:
    response = client.get("/api/health")

    assert response.status_code == 200
    assert response.get_json() == {"status": "healthy", "service": "docslight-web"}


def test_web_app_main_uses_defaults(monkeypatch: Any) -> None:
    calls: list[tuple[str, int, bool]] = []
    monkeypatch.setattr(
        web_app,
        "run_web_app",
        lambda host, port, debug: calls.append((host, port, debug)),
    )

    exit_code = web_app.main([])

    assert exit_code == 0
    assert calls == [("127.0.0.1", 8000, False)]


def test_web_app_main_passes_host_port_and_debug(monkeypatch: Any) -> None:
    calls: list[tuple[str, int, bool]] = []
    monkeypatch.setattr(
        web_app,
        "run_web_app",
        lambda host, port, debug: calls.append((host, port, debug)),
    )

    exit_code = web_app.main(["--host", "0.0.0.0", "--port", "9000", "--debug"])

    assert exit_code == 0
    assert calls == [("0.0.0.0", 9000, True)]


def test_debug_web_logging_enables_docslight_info_logs(monkeypatch: Any) -> None:
    logger = logging.getLogger("docslight")
    monkeypatch.setattr(logger, "setLevel", lambda level: setattr(logger, "_seen_level", level))

    web_app._configure_web_logging(debug=True)

    assert logger._seen_level == logging.INFO


def test_system_info_includes_modes_and_extensions(client: Any) -> None:
    response = client.get("/api/system-info")

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["modes"] == ["cloud", "local"]
    assert "pdf" in payload["supported_extensions"]
    assert "xlsx" in payload["supported_extensions"]


def test_parse_endpoint_returns_parse_result(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={"file": upload("sample.pdf"), "mode": "cloud", "api_key": "secret"},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"]["markdown"].startswith("# Parsed")
    assert payload["result"]["pages"] == [{"page": 1, "text": "API result"}]
    assert payload["result"]["metadata"]["mode"] == "cloud"
    assert FakeDocSlight.instances[0].init_kwargs["api_key"] == "secret"


def test_parse_endpoint_returns_raw_cloud_parse_json(client: Any) -> None:
    raw_parse_json = {
        "markdown": "# From downloaded json",
        "pages": [{"page_id": 1, "parsing_res_list": [{"block_content": "A"}]}],
    }
    FakeDocSlight.parse_result = ParseResult(
        markdown="# Normalized",
        pages=[],
        metadata={"downloadUrl": "https://download.example.com/task.zip"},
        raw_response=raw_parse_json,
    )

    response = client.post(
        "/api/parse",
        data={"file": upload("sample.pdf"), "mode": "cloud", "api_key": "secret"},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert response.get_json() == {"success": True, "result": raw_parse_json}


def test_parse_endpoint_returns_raw_parse_archive(client: Any) -> None:
    archive = b"zip bytes"
    FakeDocSlight.parse_result = ParseResult(
        markdown="# Normalized",
        pages=[],
        metadata={"downFileName": "task-1.zip"},
        raw_response={"markdown": "# Normalized"},
        raw_archive=archive,
    )

    response = client.post(
        "/api/parse",
        data={"file": upload("sample.pdf"), "mode": "local"},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert response.data == archive
    assert response.mimetype == "application/zip"
    assert "task-1.zip" in response.headers["Content-Disposition"]


def test_parse_accepts_xlsx_upload_in_local_mode(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={"file": upload("ledger.xlsx"), "mode": "local"},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert response.get_json()["success"] is True
    assert FakeDocSlight.instances[0].init_kwargs["mode"] == "local"


def test_parse_ignores_hidden_local_llm_provider_default(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={
            "file": upload("ledger.xlsx"),
            "mode": "local",
            "local_llm_provider": "ollama",
            "local_llm_model": "",
            "local_llm_base_url": "",
            "local_llm_api_key": "",
        },
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert response.get_json()["success"] is True
    assert FakeDocSlight.instances[0].init_kwargs["local_llm"] is None


def test_extract_endpoint_parses_fields_schema_and_document_types(client: Any) -> None:
    schema = {"type": "object", "properties": {"total": {"type": "number"}}}
    document_types = ["invoice", "receipt"]

    response = client.post(
        "/api/extract",
        data={
            "file": upload("invoice.pdf"),
            "fields": "invoice_number, total",
            "schema": json.dumps(schema),
            "document_types": json.dumps(document_types),
        },
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert "result" not in payload
    assert payload["results"]["invoice_number"] == "INV-100"
    _, kwargs = FakeDocSlight.instances[0].extract_calls[0]
    assert kwargs == {
        "fields": ["invoice_number", "total"],
        "schema": schema,
        "document_types": document_types,
        "mode": "vlm",
    }


def test_extract_endpoint_supports_integrate_mode_and_grounding(client: Any) -> None:
    response = client.post(
        "/api/extract",
        data={
            "file": upload("invoice.pdf"),
            "fields": "invoice_number, total",
            "mode": "cloud",
            "cloud_extract_mode": "integrate",
            "enable_grounding": "true",
        },
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    _, kwargs = FakeDocSlight.instances[0].extract_calls[0]
    assert kwargs["mode"] == "integrate"
    assert kwargs["enable_grounding"] is True


def test_extract_local_mode_does_not_send_cloud_options(client: Any) -> None:
    response = client.post(
        "/api/extract",
        data={
            "file": upload("invoice.pdf"),
            "fields": "invoice_number",
            "mode": "local",
            "cloud_extract_mode": "integrate",
            "enable_grounding": "true",
        },
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    _, kwargs = FakeDocSlight.instances[0].extract_calls[0]
    assert "mode" not in kwargs
    assert "enable_grounding" not in kwargs


def test_extract_local_llm_form_fields_build_local_llm_dict(client: Any) -> None:
    response = client.post(
        "/api/extract",
        data={
            "file": upload("invoice.pdf"),
            "fields": "invoice_number",
            "mode": "local",
            "local_llm_provider": "openai-compatible",
            "local_llm_model": "llama3.1",
            "local_llm_base_url": "http://localhost:11434",
            "local_llm_api_key": "local-secret",
        },
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert FakeDocSlight.instances[0].init_kwargs["local_llm"] == {
        "provider": "openai-compatible",
        "model": "llama3.1",
        "base_url": "http://localhost:11434",
        "api_key": "local-secret",
    }


def test_parse_preserves_non_ascii_upload_suffix(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={"file": upload("截图.png"), "mode": "local"},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert FakeDocSlight.seen_paths[0].suffix == ".png"


def test_preview_preserves_non_ascii_upload_suffix(client: Any) -> None:
    response = client.post(
        "/api/preview",
        data={"file": upload("截图.png", _png_bytes())},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"]["kind"] == "image"


def test_preview_pdf_endpoint_returns_rendered_preview(client: Any) -> None:
    response = client.post(
        "/api/preview",
        data={"file": upload("sample.pdf", _tiny_pdf_bytes())},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"]["kind"] == "pdf"
    assert payload["result"]["pages"]


def test_preview_office_returns_unsupported_notice(client: Any) -> None:
    response = client.post(
        "/api/preview",
        data={"file": upload("sample.docx")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"]["kind"] == "unsupported"


def test_parse_requires_upload(client: Any) -> None:
    response = client.post("/api/parse", data={"mode": "cloud"})

    assert response.status_code == 400
    assert response.get_json()["error"] == "A file upload is required."


def test_parse_rejects_unsupported_extension(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={"file": upload("sample.exe")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 400
    assert response.get_json()["error"] == "Unsupported file extension."


@pytest.mark.parametrize(
    ("error", "status"),
    [
        (AuthenticationError("bad key"), 401),
        (RateLimitError("too many"), 429),
        (DependencyMissingError("missing optional dependency"), 400),
        (LocalProcessingError("local failed"), 400),
    ],
)
def test_api_errors_return_json(client: Any, error: Exception, status: int) -> None:
    FakeDocSlight.parse_error = error

    response = client.post(
        "/api/parse",
        data={"file": upload("sample.pdf")},
        content_type="multipart/form-data",
    )

    assert response.status_code == status
    payload = response.get_json()
    assert payload["success"] is False
    assert payload["error"] == str(error)


def test_preview_render_pdf_preview_render_failure_raises_local_processing_error(
    tmp_path: Path,
) -> None:
    with pytest.raises(LocalProcessingError):
        render_pdf_preview(tmp_path / "missing.pdf")


def _png_bytes() -> bytes:
    from PIL import Image

    buffer = BytesIO()
    Image.new("RGB", (2, 1), color="white").save(buffer, format="PNG")
    return buffer.getvalue()


def _tiny_pdf_bytes() -> bytes:
    import fitz

    document = fitz.open()
    page = document.new_page(width=20, height=20)
    page.insert_text((2, 10), "Hi")
    payload = document.tobytes()
    document.close()
    return payload
