from __future__ import annotations

from pathlib import Path
from typing import Any

import pytest

from docslight.config import DocSlightConfig
from docslight.result import ExtractResult, ParseResult


@pytest.fixture(autouse=True)
def clear_docslight_env(monkeypatch: pytest.MonkeyPatch) -> None:
    for name in (
        "DOCSLIGHT_MODE",
        "DOCSLIGHT_API_KEY",
        "DOCSLIGHT_BASE_URL",
        "DOCSLIGHT_TIMEOUT",
        "DOCSLIGHT_LOCAL_PARSER",
    ):
        monkeypatch.delenv(name, raising=False)


class FakeProcessor:
    def __init__(self) -> None:
        self.parse_calls: list[tuple[Path | str, dict[str, Any]]] = []
        self.extract_calls: list[tuple[Path | str, dict[str, Any]]] = []

    def parse(self, path: Path | str, **options: Any) -> ParseResult:
        self.parse_calls.append((path, options))
        return ParseResult(markdown=f"parsed:{path}")

    def extract(self, path: Path | str, **options: Any) -> ExtractResult:
        self.extract_calls.append((path, options))
        return ExtractResult(data={"path": str(path)})


def get_docslight() -> type:
    try:
        from docslight.client import DocSlight
    except ModuleNotFoundError as exc:
        pytest.fail(f"DocSlight SDK router is not importable: {exc}")
    return DocSlight


def test_default_mode_cloud_parse_delegates_to_cloud_client() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)

    result = client.parse("sample.pdf", language="en")

    assert result == ParseResult(markdown="parsed:sample.pdf")
    assert fake_cloud.parse_calls == [("sample.pdf", {"language": "en"})]


def test_extract_passes_fields_schema_document_types_and_options() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)
    schema = {"properties": {"invoice_id": {"type": "string"}}}

    result = client.extract(
        "invoice.pdf",
        fields=["invoice_id"],
        schema=schema,
        document_types=["invoice"],
        temperature=0,
        strict=True,
    )

    assert result == ExtractResult(data={"path": "invoice.pdf"})
    assert fake_cloud.extract_calls == [
        (
            "invoice.pdf",
            {
                "fields": ["invoice_id"],
                "schema": schema,
                "document_types": ["invoice"],
                "temperature": 0,
                "strict": True,
            },
        )
    ]


def test_extract_omits_empty_fields_schema_and_document_types() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)

    result = client.extract("invoice.pdf", language="en")

    assert result == ExtractResult(data={"path": "invoice.pdf"})
    assert fake_cloud.extract_calls == [("invoice.pdf", {"language": "en"})]


def test_extract_derives_schema_from_structured_fields() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)
    fields = {
        "keys": {"Title": {"prompt": None, "mapping": None}},
        "tableHeaders": {"Items": {"Name": {"prompt": None, "mapping": None}}},
        "name": "Invoice",
    }

    client.extract("invoice.pdf", fields=fields)

    _, kwargs = fake_cloud.extract_calls[0]
    assert kwargs["fields"] == fields
    assert kwargs["schema"]["type"] == "object"
    assert "Title" in kwargs["schema"]["properties"]
    assert "tables" in kwargs["schema"]["properties"]


def test_local_mode_delegates_to_local_pipeline() -> None:
    DocSlight = get_docslight()
    fake_pipeline = FakeProcessor()
    client = DocSlight(mode="local", local_pipeline=fake_pipeline)

    parse_result = client.parse("local.pdf", language="en")
    extract_result = client.extract("local.pdf", fields=["title"])

    assert parse_result == ParseResult(markdown="parsed:local.pdf")
    assert extract_result == ExtractResult(data={"path": "local.pdf"})
    assert fake_pipeline.parse_calls == [("local.pdf", {"language": "en"})]
    assert fake_pipeline.extract_calls[0][0] == "local.pdf"
    assert fake_pipeline.extract_calls[0][1]["fields"] == ["title"]
    assert fake_pipeline.extract_calls[0][1]["schema"]["properties"]["title"]


def test_parse_batch_processes_in_order_and_preserves_options() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)

    results = client.parse_batch(["a.pdf", "b.pdf"], language="en", ocr=True)

    assert results == [ParseResult(markdown="parsed:a.pdf"), ParseResult(markdown="parsed:b.pdf")]
    assert fake_cloud.parse_calls == [
        ("a.pdf", {"language": "en", "ocr": True}),
        ("b.pdf", {"language": "en", "ocr": True}),
    ]


def test_extract_batch_processes_in_order_and_preserves_fields_and_options() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)

    results = client.extract_batch(
        ["a.pdf", "b.pdf"],
        fields=["total"],
        document_types=["invoice"],
        language="en",
    )

    assert results == [ExtractResult(data={"path": "a.pdf"}), ExtractResult(data={"path": "b.pdf"})]
    assert fake_cloud.extract_calls == [
        (
            "a.pdf",
            {
                "fields": ["total"],
                "document_types": ["invoice"],
                "language": "en",
                "schema": {
                    "type": "object",
                    "properties": {
                        "total": {"type": ["string", "number", "boolean", "null", "object", "array"]},
                    },
                    "additionalProperties": True,
                },
            },
        ),
        (
            "b.pdf",
            {
                "fields": ["total"],
                "document_types": ["invoice"],
                "language": "en",
                "schema": {
                    "type": "object",
                    "properties": {
                        "total": {"type": ["string", "number", "boolean", "null", "object", "array"]},
                    },
                    "additionalProperties": True,
                },
            },
        ),
    ]


def test_parse_non_markdown_output_is_added_to_options() -> None:
    DocSlight = get_docslight()
    fake_cloud = FakeProcessor()
    client = DocSlight(cloud_client=fake_cloud)

    client.parse("sample.pdf", output="json", language="en")

    assert fake_cloud.parse_calls == [("sample.pdf", {"language": "en", "output": "json"})]


def test_constructor_arguments_build_config_and_default_cloud_client(
    monkeypatch,
) -> None:
    DocSlight = get_docslight()
    created: list[dict[str, Any]] = []

    class FakeCloudClient(FakeProcessor):
        def __init__(self, api_key: str | None, base_url: str, timeout: float) -> None:
            super().__init__()
            created.append(
                {
                    "api_key": api_key,
                    "base_url": base_url,
                    "timeout": timeout,
                }
            )

    monkeypatch.setattr("docslight.client.CloudClient", FakeCloudClient)
    monkeypatch.setattr(
        "docslight.client.DocSlightConfig.from_sources",
        lambda **kwargs: DocSlightConfig(
            mode=kwargs["mode"] or "cloud",
            api_key=kwargs["api_key"],
            base_url=kwargs["base_url"] or "https://api.example.com",
            timeout=kwargs["timeout"] or 7.5,
            local_parser=kwargs["local_parser"],
            local_llm=kwargs["local_llm"],
        ),
    )

    client = DocSlight(
        mode="cloud",
        api_key="secret",
        base_url="https://api.example.com",
        timeout=7.5,
        local_parser="pymupdf",
        local_llm={"provider": "fake"},
    )

    client.parse("sample.pdf")

    assert client.config == DocSlightConfig(
        mode="cloud",
        api_key="secret",
        base_url="https://api.example.com",
        timeout=7.5,
        local_parser="pymupdf",
        local_llm={"provider": "fake"},
    )
    assert created == [
        {"api_key": "secret", "base_url": "https://api.example.com", "timeout": 7.5}
    ]
