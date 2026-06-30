from __future__ import annotations

from pathlib import Path

import pytest

from docslight.config import DEFAULT_BASE_URL, DocSlightConfig
from docslight.exceptions import AuthenticationError, ConfigurationError, RateLimitError
from docslight.result import ExtractResult, ParseResult


def test_parse_result_serializes_markdown_pages_and_metadata() -> None:
    result = ParseResult(
        markdown="# Title",
        pages=[{"page": 1, "markdown": "# Title"}],
        metadata={"source": "sample.pdf"},
    )

    assert result.to_markdown() == "# Title"
    assert result.to_json() == {
        "markdown": "# Title",
        "pages": [{"page": 1, "markdown": "# Title"}],
        "metadata": {"source": "sample.pdf"},
    }


def test_parse_result_serializes_standard_parse_json() -> None:
    result = ParseResult(
        markdown="Invoice\n\n<table><tr><td>A</td><td>B</td></tr></table>",
        pages=[
            {
                "page_number": 1,
                "width": 100,
                "height": 200,
                "doc_preprocessor_res": {"angle": -1},
                "layout_det_res": {
                    "boxes": [
                        {
                            "label": "text",
                            "coordinate": [10, 20, 40, 50],
                            "score": 0.9,
                        }
                    ]
                },
                "parsing_res_list": [
                    {
                        "block_id": 0,
                        "block_label": "text",
                        "block_content": "Invoice",
                        "block_bbox": [10, 20, 40, 50],
                    },
                    {
                        "block_id": 1,
                        "block_label": "table",
                        "block_content": "<table><tr><td colspan='2'>A</td></tr></table>",
                        "block_bbox": [10, 60, 90, 120],
                    },
                    {
                        "block_id": 2,
                        "block_label": "footer",
                        "block_content": "Page 1",
                        "block_bbox": [10, 180, 50, 190],
                    },
                ],
            }
        ],
        metadata={"document_type": "pdf", "x_request_id": "req_123"},
    )

    standard = result.to_standard_json()

    assert standard["code"] == 200
    assert standard["message"] == "Success"
    assert standard["x_request_id"] == "req_123"
    assert standard["file_type"] == "PDF"
    assert standard["result"]["markdown"] == result.markdown
    assert standard["result"]["total_page_number"] == 1
    assert standard["result"]["success_count"] == 1
    assert standard["metrics"] == [
        {
            "angle": 0,
            "status": "Success",
            "dpi": 144,
            "page_id": 1,
            "image_id": "",
            "duration": 0,
            "page_image_height": 200,
            "page_image_width": 100,
        }
    ]

    page = standard["result"]["pages"][0]
    assert page["structured"][0] == {
        "pos": [10, 20, 40, 20, 40, 50, 10, 50],
        "id": 0,
        "content": [0],
        "text": "Invoice",
        "type": "text",
        "outline_level": -1,
    }
    assert page["content"][0]["score"] == 0.9
    assert page["structured"][1]["rows"] == 1
    assert page["structured"][1]["cols"] == 2
    assert page["content"][2]["status"] == 0
    assert standard["result"]["detail"][1]["type"] == "table"
    assert standard["result"]["detail"][1]["caption_id"] == 1


def test_extract_result_serializes_data_metadata_and_raw_response() -> None:
    result = ExtractResult(
        data={"invoice_id": "INV-001"},
        metadata={"confidence": 0.98},
        raw_response={"choices": []},
    )

    assert result.to_json() == {
        "results": {"invoice_id": "INV-001"},
        "metadata": {"confidence": 0.98},
    }


def test_extract_result_normalizes_results_fields_and_dimension_metadata() -> None:
    result = ExtractResult(
        data={
            "results": {
                "fields": {"invoice_id": {"value": "INV-001"}},
                "tables": {"items": [{"name": "Book"}]},
                "_table_bboxes": {"items": [{"page_id": 1, "bbox": [1, 2, 3, 4]}]},
                "source_width": 1024,
                "source_height": 768,
            }
        }
    )

    assert result.data == {
        "invoice_id": {"value": "INV-001"},
        "tables": {"items": [{"name": "Book"}]},
        "_table_bboxes": {"items": [{"page_id": 1, "bbox": [1, 2, 3, 4]}]},
    }
    assert result.metadata == {"source_width": 1024, "source_height": 768}


def test_authentication_error_preserves_cloud_metadata() -> None:
    error = AuthenticationError("bad", status_code=401, request_id="req")

    assert str(error) == "bad"
    assert error.status_code == 401
    assert error.request_id == "req"


def test_rate_limit_error_preserves_cloud_metadata() -> None:
    error = RateLimitError("too many", status_code=429, request_id="req")

    assert str(error) == "too many"
    assert error.status_code == 429
    assert error.request_id == "req"


def test_config_from_sources_uses_explicit_env_file_defaults_priority(
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    config_path = tmp_path / "config.toml"
    config_path.write_text(
        "\n".join(
            [
                'mode = "local"',
                'api_key = "file-key"',
                'base_url = "https://file.example.com"',
                "timeout = 5.5",
                'local_parser = "pymupdf"',
                "[local_llm]",
                'provider = "file-llm"',
            ]
        ),
        encoding="utf-8",
    )
    monkeypatch.setenv("DOCSLIGHT_MODE", "cloud")
    monkeypatch.setenv("DOCSLIGHT_API_KEY", "env-key")
    monkeypatch.setenv("DOCSLIGHT_TIMEOUT", "9.5")
    monkeypatch.setenv("DOCSLIGHT_LOCAL_PARSER", "env-parser")
    monkeypatch.delenv("DOCSLIGHT_BASE_URL", raising=False)

    config = DocSlightConfig.from_sources(
        config_path=config_path,
        mode="local",
        api_key="explicit-key",
        base_url=None,
    )

    assert config.mode == "local"
    assert config.api_key == "explicit-key"
    assert config.base_url == "https://file.example.com"
    assert config.timeout == 9.5
    assert config.local_parser == "env-parser"
    assert config.local_llm == {"provider": "file-llm"}


def test_config_from_sources_uses_defaults_when_no_sources(
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    for name in (
        "DOCSLIGHT_MODE",
        "DOCSLIGHT_API_KEY",
        "DOCSLIGHT_BASE_URL",
        "DOCSLIGHT_TIMEOUT",
        "DOCSLIGHT_LOCAL_PARSER",
    ):
        monkeypatch.delenv(name, raising=False)

    config = DocSlightConfig.from_sources(config_path=tmp_path / "missing.toml")

    assert config.mode == "cloud"
    assert config.api_key is None
    assert config.base_url == DEFAULT_BASE_URL


def test_config_from_sources_rejects_invalid_mode(tmp_path: Path) -> None:
    with pytest.raises(ConfigurationError, match="mode must be one of"):
        DocSlightConfig.from_sources(config_path=tmp_path / "missing.toml", mode="hybrid")


def test_config_from_sources_wraps_invalid_env_timeout(
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setenv("DOCSLIGHT_TIMEOUT", "not-number")

    with pytest.raises(ConfigurationError, match="timeout must be a number"):
        DocSlightConfig.from_sources(config_path=tmp_path / "missing.toml")
