from __future__ import annotations

import json
import logging
import zipfile
from io import BytesIO
from pathlib import Path
from typing import Any

import pytest
import requests

from docslight.cloud import CloudClient
from docslight.exceptions import AuthenticationError, CloudAPIError, RateLimitError
from docslight.result import ExtractResult, ParseResult


class FakeResponse:
    def __init__(
        self,
        payload: Any,
        status_code: int = 200,
        headers: dict[str, str] | None = None,
        json_error: Exception | None = None,
        text: str = "",
        content: bytes = b"",
    ) -> None:
        self.payload = payload
        self.status_code = status_code
        self.headers = headers or {}
        self.json_error = json_error
        self.text = text
        self.content = content

    def json(self) -> Any:
        if self.json_error is not None:
            raise self.json_error
        return self.payload


class FakeSession:
    def __init__(
        self,
        response: FakeResponse | None = None,
        responses: list[FakeResponse] | None = None,
    ) -> None:
        self.response = response or FakeResponse({})
        self.responses = list(responses or [])
        self.calls: list[dict[str, Any]] = []
        self.closed = False

    def _response(self) -> FakeResponse:
        if self.responses:
            return self.responses.pop(0)
        return self.response

    def post(self, url: str, **kwargs: Any) -> FakeResponse:
        files = kwargs.get("files")
        if files and "file" in files:
            kwargs["file_content"] = files["file"][1].read()
        self.calls.append({"method": "POST", "url": url, "kwargs": kwargs})
        return self._response()

    def get(self, url: str, **kwargs: Any) -> FakeResponse:
        self.calls.append({"method": "GET", "url": url, "kwargs": kwargs})
        return self._response()

    def close(self) -> None:
        self.closed = True


class RaisingSession:
    def __init__(self, error: requests.RequestException) -> None:
        self.error = error

    def post(self, url: str, **kwargs: Any) -> FakeResponse:
        raise self.error

    def get(self, url: str, **kwargs: Any) -> FakeResponse:
        raise self.error


def _zip_bytes(files: dict[str, str]) -> bytes:
    buffer = BytesIO()
    with zipfile.ZipFile(buffer, "w") as archive:
        for name, content in files.items():
            archive.writestr(name, content)
    return buffer.getvalue()


def test_parse_posts_multipart_file_options_headers_and_timeout(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(
        FakeResponse(
            {
                "markdown": "# Invoice",
                "pages": [{"page": 1}],
                "metadata": {"source": "cloud"},
            }
        )
    )
    client = CloudClient(
        api_key="secret",
        base_url="https://api.example.com/",
        timeout=3.5,
        session=session,
    )

    result = client.parse(document, language="en", ocr=True, skip=None)

    assert result == ParseResult(
        markdown="# Invoice",
        pages=[{"page": 1}],
        metadata={"source": "cloud"},
        raw_response={
            "markdown": "# Invoice",
            "pages": [{"page": 1}],
            "metadata": {"source": "cloud"},
        },
    )
    assert len(session.calls) == 1
    call = session.calls[0]
    assert call["method"] == "POST"
    assert call["url"] == "https://api.example.com/server/v2/process/idp/documentParsing"
    kwargs = call["kwargs"]
    assert kwargs["data"] == {"language": "en", "ocr": "true"}
    assert kwargs["headers"]["x-api-key"] == "secret"
    assert "User-Agent" in kwargs["headers"]
    assert kwargs["timeout"] == 3.5
    assert kwargs["files"]["file"][0] == "sample.pdf"
    assert kwargs["file_content"] == b"pdf bytes"


def test_parse_non_string_markdown_returns_empty_string(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(FakeResponse({"markdown": None}))
    client = CloudClient(session=session)

    result = client.parse(document)

    assert result.markdown == ""


def test_default_cloud_client_uses_online_api_host(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(FakeResponse({"markdown": ""}))
    client = CloudClient(session=session)

    client.parse(document)

    assert session.calls[0]["url"] == (
        "https://api-server.compdf.com/server/v2/process/idp/documentParsing"
    )


def test_parse_logs_document_parsing_endpoint(
    tmp_path: Path,
    caplog: pytest.LogCaptureFixture,
) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(FakeResponse({"markdown": ""}))
    client = CloudClient(base_url="https://api.example.com", session=session)

    caplog.set_level(logging.INFO, logger="docslight.cloud.client")

    client.parse(document)

    assert (
        "Calling ComPDF Cloud parse endpoint: POST "
        "https://api.example.com/server/v2/process/idp/documentParsing"
    ) in caplog.text


def test_extract_posts_compact_json_options_and_preserves_raw_response(
    tmp_path: Path,
) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    payload = {
        "data": {"invoice_no": "INV-001"},
        "metadata": {"confidence": 0.98},
        "trace": {"id": "trace-1"},
    }
    session = FakeSession(FakeResponse(payload))
    client = CloudClient(base_url="https://api.example.com", session=session)
    schema = {"properties": {"invoice_no": {"type": "string"}}}

    result = client.extract(
        document,
        fields=["invoice_no"],
        schema=schema,
        document_types=["invoice"],
        mode="strict",
    )

    assert result == ExtractResult(
        data={"invoice_no": "INV-001"},
        metadata={"confidence": 0.98},
        raw_response=payload,
    )
    call = session.calls[0]
    assert call["url"] == "https://api.example.com/server/v2/process/idp/documentExtract"
    assert call["kwargs"]["data"] == {
        "extract_fields": '{"keys":{"invoice_no":{}},"tableHeaders":{},"name":"Document"}',
        "document_types": '["invoice"]',
        "mode": "strict",
    }
    assert "Authorization" not in call["kwargs"]["headers"]
    assert "x-api-key" not in call["kwargs"]["headers"]


def test_parse_accepts_full_operation_url_and_zip_payload(tmp_path: Path) -> None:
    import io
    import zipfile

    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, "w") as archive:
        archive.writestr(
            "result_json.json",
            json.dumps(
                {
                    "result": {
                        "pages": [
                            {
                                "page_id": 1,
                                "width": 100,
                                "height": 200,
                                "structured": [
                                    {
                                        "text": "Invoice",
                                        "type": "text",
                                        "pos": [10, 20, 30, 20, 30, 40, 10, 40],
                                    }
                                ],
                            }
                        ]
                    }
                }
            ),
        )
        archive.writestr("result.md", "# Invoice")

    response = FakeResponse({}, headers={"content-type": "application/zip"})
    response.content = buffer.getvalue()
    session = FakeSession(response)
    client = CloudClient(
        api_key="secret",
        base_url="http://example.test/parse",
        session=session,
    )

    result = client.parse(document)

    assert result.markdown == "# Invoice"
    assert result.pages == [
        {
            "page_id": 1,
            "page_index": 0,
            "width": 100,
            "height": 200,
            "parsing_res_list": [
                {
                    "block_content": "Invoice",
                    "block_type": "text",
                    "block_bbox": [10.0, 20.0, 30.0, 40.0],
                }
            ],
        }
    ]
    assert result.metadata["response_format"] == "zip"
    call = session.calls[0]
    assert call["url"] == "http://example.test/parse"
    assert call["kwargs"]["headers"]["Authorization"] == "Bearer secret"
    assert call["kwargs"]["headers"]["x-api-key"] == "secret"


def test_extract_uses_custom_extract_fields_contract_for_full_operation_url(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(FakeResponse({"results": {"Page_1": {"invoice_no": "INV-001"}}}))
    client = CloudClient(base_url="http://example.test/extract", session=session)

    result = client.extract(document, fields=["invoice_no"])

    assert result.data == {"invoice_no": "INV-001"}
    assert result.metadata["page_key"] == "Page_1"
    call = session.calls[0]
    assert call["url"] == "http://example.test/extract"
    assert call["kwargs"]["data"] == {
        "extract_fields": '{"keys":{"invoice_no":{}},"tableHeaders":{},"name":"Document"}'
    }


def test_extract_uses_results_payload_when_custom_endpoint_returns_non_page_results(
    tmp_path: Path,
) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    payload = {
        "message": "OK",
        "results": {
            "invoice_number": {
                "value": "1024",
                "bboxes": [{"page_id": 1, "bbox": [1, 2, 3, 4]}],
            }
        },
    }
    session = FakeSession(FakeResponse(payload))
    client = CloudClient(base_url="http://example.test/extract", session=session)

    result = client.extract(document, fields=["invoice_number"], mode="integrate")

    assert result.data == payload["results"]
    assert result.raw_response["data"] == payload["results"]


def test_parse_online_api_response_downloads_result_zip(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    archive = _zip_bytes(
        {
            "result.md": "# Invoice",
            "result.json": json.dumps(
                {
                    "pages": [{"page": 1}],
                    "metadata": {"confidence": 0.97},
                }
            ),
        }
    )
    session = FakeSession(
        responses=[
            FakeResponse(
                {
                    "code": "200",
                    "msg": "success",
                    "data": {
                        "fileKey": "file-1",
                        "taskId": "task-1",
                        "fileName": "sample.pdf",
                        "downFileName": "task-1.zip",
                        "downloadUrl": "https://download.example.com/task-1.zip",
                        "status": "success",
                    },
                }
            ),
            FakeResponse({}, content=archive),
        ]
    )
    client = CloudClient(base_url="https://api.example.com", session=session)

    result = client.parse(document)

    assert result == ParseResult(
        markdown="# Invoice",
        pages=[{"page": 1}],
        metadata={
            "confidence": 0.97,
            "response_format": "zip",
            "archive_entries": ["result.md", "result.json"],
            "api_code": "200",
            "api_message": "success",
            "fileKey": "file-1",
            "taskId": "task-1",
            "fileName": "sample.pdf",
            "downFileName": "task-1.zip",
            "downloadUrl": "https://download.example.com/task-1.zip",
            "status": "success",
        },
        raw_response={
            "markdown": "# Invoice",
            "pages": [{"page": 1}],
            "metadata": {
                "confidence": 0.97,
                "response_format": "zip",
                "archive_entries": ["result.md", "result.json"],
            },
        },
        raw_archive=archive,
    )
    assert session.calls[1] == {
        "method": "GET",
        "url": "https://download.example.com/task-1.zip",
        "kwargs": {"timeout": 120.0},
    }


def test_parse_online_api_response_reads_json_from_result_zip(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    archive = _zip_bytes(
        {
            "result.json": json.dumps(
                {
                    "pages": [
                        {
                            "page_id": 1,
                            "parsing_res_list": [
                                {"block_content": "INVOICE"},
                                {"block_content": "TOTAL"},
                            ],
                        }
                    ],
                    "metadata": {"source": "private-parser-json"},
                }
            ),
        }
    )
    session = FakeSession(
        responses=[
            FakeResponse(
                {
                    "code": "200",
                    "msg": "success",
                    "data": {
                        "taskId": "task-1",
                        "download_url": "https://download.example.com/task-1.zip",
                        "status": "success",
                    },
                }
            ),
            FakeResponse({}, content=archive),
        ]
    )
    client = CloudClient(base_url="https://api.example.com", session=session)

    result = client.parse(document)

    assert result.markdown == "INVOICE\n\nTOTAL"
    assert result.pages == [
        {
            "page_id": 1,
            "parsing_res_list": [
                {"block_content": "INVOICE"},
                {"block_content": "TOTAL"},
            ],
        }
    ]
    assert result.metadata["source"] == "private-parser-json"
    assert result.metadata["taskId"] == "task-1"


def test_extract_online_api_response_downloads_result_json(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    result_json = json.dumps(
        {
            "data": {"invoice_no": "INV-001"},
            "metadata": {"confidence": 0.98},
        }
    ).encode()
    response_payload = {
        "code": "200",
        "msg": "success",
        "data": {
            "fileKey": "file-1",
            "taskId": "task-1",
            "downloadUrl": "https://download.example.com/task-1.zip",
            "status": "success",
        },
    }
    session = FakeSession(
        responses=[
            FakeResponse(response_payload),
            FakeResponse({}, content=result_json),
        ]
    )
    client = CloudClient(base_url="https://api.example.com", session=session)

    result = client.extract(document, fields=["invoice_no"])

    assert result == ExtractResult(
        data={"invoice_no": "INV-001"},
        metadata={
            "confidence": 0.98,
            "api_code": "200",
            "api_message": "success",
            "fileKey": "file-1",
            "taskId": "task-1",
            "downloadUrl": "https://download.example.com/task-1.zip",
            "status": "success",
        },
        raw_response=response_payload,
    )


def test_online_api_response_accepts_snake_case_download_url(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    result_json = json.dumps({"data": {"invoice_no": "INV-001"}}).encode()
    session = FakeSession(
        responses=[
            FakeResponse(
                {
                    "code": "200",
                    "msg": "success",
                    "data": {
                        "taskId": "task-1",
                        "download_url": "https://download.example.com/result.json",
                    },
                }
            ),
            FakeResponse({}, content=result_json),
        ]
    )
    client = CloudClient(base_url="https://api.example.com", session=session)

    result = client.extract(document)

    assert result.data == {"invoice_no": "INV-001"}
    assert session.calls[1]["url"] == "https://download.example.com/result.json"


def test_online_api_non_success_code_maps_to_cloud_api_error(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(FakeResponse({"code": "500", "msg": "convert failed"}))
    client = CloudClient(session=session)

    with pytest.raises(CloudAPIError) as exc_info:
        client.parse(document)

    assert str(exc_info.value) == "convert failed"
    assert exc_info.value.status_code == 500


def test_online_api_success_false_maps_to_cloud_api_error(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    session = FakeSession(FakeResponse({"error": "未知用户类型！", "success": False}))
    client = CloudClient(session=session)

    with pytest.raises(CloudAPIError) as exc_info:
        client.parse(document)

    assert str(exc_info.value) == "未知用户类型！"
    assert exc_info.value.status_code == 400


def test_unauthorized_response_maps_to_authentication_error_with_request_id() -> None:
    session = FakeSession(
        FakeResponse(
            {"message": "invalid api key"},
            status_code=401,
            headers={"x-request-id": "req-1"},
        )
    )
    client = CloudClient(session=session)

    with pytest.raises(AuthenticationError) as exc_info:
        client.health()

    assert str(exc_info.value) == "invalid api key"
    assert exc_info.value.status_code == 401
    assert exc_info.value.request_id == "req-1"


def test_unauthorized_invalid_json_maps_to_authentication_error_with_request_id() -> None:
    session = FakeSession(
        FakeResponse(
            {},
            status_code=401,
            headers={"x-request-id": "req-invalid-json"},
            json_error=ValueError("not json"),
            text="<html>unauthorized</html>",
        )
    )
    client = CloudClient(session=session)

    with pytest.raises(AuthenticationError) as exc_info:
        client.health()

    assert exc_info.value.status_code == 401
    assert exc_info.value.request_id == "req-invalid-json"
    assert str(exc_info.value) == "Cloud API error 401"
    assert "<html>" not in str(exc_info.value)
    assert "unauthorized" not in str(exc_info.value)


def test_unauthorized_json_html_message_is_not_exposed() -> None:
    session = FakeSession(
        FakeResponse(
            {"message": "<html>invalid key sk-secret traceback</html>"},
            status_code=401,
            headers={"x-request-id": "req-json-html"},
        )
    )
    client = CloudClient(session=session)

    with pytest.raises(AuthenticationError) as exc_info:
        client.health()

    assert str(exc_info.value) == "Cloud API error 401"
    assert "<html>" not in str(exc_info.value)
    assert "sk-secret" not in str(exc_info.value)
    assert "traceback" not in str(exc_info.value)
    assert exc_info.value.request_id == "req-json-html"


def test_uppercase_request_id_header_is_preserved_for_errors() -> None:
    session = FakeSession(
        FakeResponse(
            {"error": "bad token"},
            status_code=401,
            headers={"X-Request-ID": "req-2"},
        )
    )
    client = CloudClient(session=session)

    with pytest.raises(AuthenticationError) as exc_info:
        client.health()

    assert str(exc_info.value) == "bad token"
    assert exc_info.value.request_id == "req-2"


def test_rate_limit_response_maps_to_rate_limit_error() -> None:
    session = FakeSession(FakeResponse({"message": "too many requests"}, status_code=429))
    client = CloudClient(session=session)

    with pytest.raises(RateLimitError) as exc_info:
        client.health()

    assert str(exc_info.value) == "too many requests"
    assert exc_info.value.status_code == 429


def test_rate_limit_non_object_json_maps_to_rate_limit_error() -> None:
    session = FakeSession(
        FakeResponse(
            ["too", "many"],
            status_code=429,
            headers={"X-Request-ID": "req-rate"},
        )
    )
    client = CloudClient(session=session)

    with pytest.raises(RateLimitError) as exc_info:
        client.health()

    assert str(exc_info.value) == "Cloud API error 429"
    assert exc_info.value.status_code == 429
    assert exc_info.value.request_id == "req-rate"


def test_server_error_response_maps_to_cloud_api_error() -> None:
    session = FakeSession(FakeResponse({"message": "server failed"}, status_code=500))
    client = CloudClient(session=session)

    with pytest.raises(CloudAPIError) as exc_info:
        client.health()

    assert str(exc_info.value) == "server failed"
    assert exc_info.value.status_code == 500


def test_server_invalid_json_maps_to_cloud_api_error_with_status_and_request_id() -> None:
    session = FakeSession(
        FakeResponse(
            {},
            status_code=500,
            headers={"x-request-id": "req-server"},
            json_error=ValueError("not json"),
            text="<html>server exploded</html>",
        )
    )
    client = CloudClient(session=session)

    with pytest.raises(CloudAPIError) as exc_info:
        client.health()

    assert exc_info.value.status_code == 500
    assert exc_info.value.request_id == "req-server"
    assert str(exc_info.value) == "Cloud API error 500"
    assert "<html>" not in str(exc_info.value)
    assert "server exploded" not in str(exc_info.value)


def test_health_gets_health_endpoint_and_returns_payload() -> None:
    session = FakeSession(FakeResponse({"ok": True, "version": "1"}))
    client = CloudClient(base_url="https://api.example.com/", timeout=9.0, session=session)

    result = client.health()

    assert result == {"ok": True, "version": "1"}
    assert session.calls == [
        {
            "method": "GET",
            "url": "https://api.example.com/v1/health",
            "kwargs": {
                "headers": session.calls[0]["kwargs"]["headers"],
                "timeout": 9.0,
            },
        }
    ]
    assert "User-Agent" in session.calls[0]["kwargs"]["headers"]


def test_unserializable_options_map_to_cloud_api_error(tmp_path: Path) -> None:
    document = tmp_path / "sample.pdf"
    document.write_bytes(b"pdf bytes")
    client = CloudClient(session=FakeSession(FakeResponse({})))

    with pytest.raises(CloudAPIError, match="bad.*not JSON serializable"):
        client.parse(document, bad={"x"})


def test_close_closes_owned_session(monkeypatch: pytest.MonkeyPatch) -> None:
    session = FakeSession(FakeResponse({}))
    monkeypatch.setattr(requests, "Session", lambda: session)
    client = CloudClient()

    client.close()

    assert session.closed is True


def test_close_does_not_close_injected_session() -> None:
    session = FakeSession(FakeResponse({}))
    client = CloudClient(session=session)

    client.close()

    assert session.closed is False


def test_context_manager_closes_owned_session(monkeypatch: pytest.MonkeyPatch) -> None:
    session = FakeSession(FakeResponse({}))
    monkeypatch.setattr(requests, "Session", lambda: session)

    with CloudClient() as client:
        assert client is not None

    assert session.closed is True


@pytest.mark.parametrize(
    "error",
    [requests.Timeout("timed out"), requests.ConnectionError("connection failed")],
)
def test_request_exceptions_map_to_cloud_api_error(
    error: requests.RequestException,
) -> None:
    client = CloudClient(session=RaisingSession(error))

    with pytest.raises(CloudAPIError) as exc_info:
        client.health()

    assert str(exc_info.value) == f"Cloud API request failed: {error}"


@pytest.mark.parametrize(
    "response",
    [
        FakeResponse({}, json_error=ValueError("invalid json")),
        FakeResponse(["not", "object"]),
    ],
)
def test_invalid_or_non_object_json_maps_to_cloud_api_error(
    response: FakeResponse,
) -> None:
    client = CloudClient(session=FakeSession(response))

    with pytest.raises(CloudAPIError):
        client.health()
