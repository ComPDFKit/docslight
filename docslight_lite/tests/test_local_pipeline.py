from __future__ import annotations

import json
import zipfile
from io import BytesIO
from pathlib import Path
from typing import Any

import pytest

from docslight.config import DocSlightConfig
from docslight.exceptions import ConfigurationError, UnsupportedFormatError
from docslight.local import (
    FileLoader,
    LoadedTextDocument,
    LocalPipeline,
    MarkdownBuilder,
    OCRLine,
    OCRPage,
)
from docslight.local.layout_blocks import build_layout_blocks
from docslight.result import ExtractResult, ParseResult


class FakeLoader:
    def __init__(self) -> None:
        self.loaded_path: Path | None = None

    def load(self, path: Path) -> list[Any]:
        self.loaded_path = path
        return [object()]


class FakeParser:
    def __init__(self) -> None:
        self.parsed_path: Path | None = None

    def parse(self, path: Path) -> ParseResult:
        self.parsed_path = path
        return ParseResult(
            markdown="# Invoice\n\nTotal 100.00",
            pages=[
                {
                    "page_number": 1,
                    "parsing_res_list": [
                        {
                            "block_id": 0,
                            "block_label": "text",
                            "block_content": "Invoice",
                            "block_bbox": [10, 20, 110, 60],
                        },
                        {
                            "block_id": 1,
                            "block_label": "text",
                            "block_content": "Total 100.00",
                            "block_bbox": [20, 80, 180, 120],
                        },
                    ],
                }
            ],
        )


class FakeOfficeLoader:
    def __init__(self) -> None:
        self.loaded_path: Path | None = None

    def load(self, path: Path) -> LoadedTextDocument:
        self.loaded_path = path
        return LoadedTextDocument(
            markdown="office markdown",
            metadata={"document_type": path.suffix.removeprefix("."), "page_count": 3},
        )


class FakeOfficeLoaderWithSheetMetadata:
    def load(self, path: Path) -> LoadedTextDocument:
        return LoadedTextDocument(
            markdown="office markdown",
            metadata={
                "document_type": "xlsx",
                "page_count": 1,
                "sheet_count": 1,
                "sheet_names": ["Invoices"],
            },
        )


class FakeLLMExtractor:
    def __init__(self) -> None:
        self.call: dict[str, Any] | None = None

    def extract(
        self,
        markdown: str,
        *,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
    ) -> ExtractResult:
        self.call = {
            "markdown": markdown,
            "fields": fields,
            "schema": schema,
            "document_types": document_types,
        }
        return ExtractResult(data={"invoice_id": "INV-001"}, raw_response={"ok": True})


class FakeLLMExtractorWithOptions:
    def __init__(self) -> None:
        self.call: dict[str, Any] | None = None

    def extract(
        self,
        markdown: str,
        *,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
        temperature: float | None = None,
        **options: Any,
    ) -> ExtractResult:
        self.call = {
            "markdown": markdown,
            "fields": fields,
            "schema": schema,
            "document_types": document_types,
            "temperature": temperature,
            "options": options,
        }
        return ExtractResult(data={"total": "100.00"})


class FakeLLMExtractorWithMetadata:
    def extract(
        self,
        markdown: str,
        *,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
    ) -> ExtractResult:
        return ExtractResult(
            data={"invoice_id": {"value": "INV-001"}},
            metadata={"source_width": 100, "source_height": 200},
        )


class FakeLLMExtractorWithBboxes:
    def extract(
        self,
        markdown: str,
        *,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
    ) -> ExtractResult:
        return ExtractResult(
            data={
                "amount": {
                    "value": "$26,250.00",
                    "bboxes": [{"page_id": 1, "bbox": [0, 1, 1000, 1438]}],
                },
                "rate": {
                    "value": "5.00%",
                    "bboxes": [{"page_id": 1, "bbox": [0, 1, 1000, 1438]}],
                },
                "字段": {"value": None, "bboxes": []},
            },
            metadata={"source_width": 1000, "source_height": 1442},
        )


class FakeLLMExtractorWithPageResults:
    def extract(
        self,
        markdown: str,
        *,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
    ) -> ExtractResult:
        return ExtractResult(data={"Page_1": {"amount": "$26,250.00"}})


class FakeLLMExtractorWithTables:
    def extract(
        self,
        markdown: str,
        *,
        fields: list[str] | dict[str, Any] | None = None,
        schema: dict[str, Any] | None = None,
        document_types: list[str] | None = None,
    ) -> ExtractResult:
        return ExtractResult(
            data={
                "Customer Name": {"value": "Youna", "bboxes": [{"page_id": 1}]},
                "Total Amount": {"value": "428.40", "bboxes": [{"page_id": 1}]},
                "tables": {
                    "Table_1": [
                        {
                            "Amount": "$ 408.00",
                            "Description": "ComPDFKit API",
                            "Quantity": "1",
                            "Unit Price": "5000",
                        }
                    ]
                },
                "_table_bboxes": {"Table_1": [{"page_id": 1, "bbox": [0, 1, 10, 20]}]},
            }
        )


def test_markdown_builder_renders_page_heading_and_lines() -> None:
    page = OCRPage(
        page_number=1,
        lines=[
            OCRLine(text="Invoice", bbox=None, confidence=None),
            OCRLine(text="Total 100.00", bbox=None, confidence=None),
        ],
    )

    assert MarkdownBuilder().build([page]) == "# Page 1\n\nInvoice\n\nTotal 100.00"


def test_build_layout_blocks_skips_invalid_pages_and_block_lists() -> None:
    blocks = build_layout_blocks(
        [
            "bad",
            {"page_number": 1, "parsing_res_list": None},
            {"page_number": 2, "parsing_res_list": "bad"},
            {
                "page_number": 3,
                "parsing_res_list": [
                    {
                        "block_id": 4,
                        "block_label": "text",
                        "block_content": "Total 100.00",
                        "block_bbox": [20, 80, 180, 120],
                    }
                ],
            },
        ]
    )

    assert blocks == [
        {
            "ref_id": "p3b4",
            "page_id": 3,
            "page_index": 3,
            "block_id": 4,
            "label": "text",
            "text": "Total 100.00",
            "bbox": [20, 80, 180, 120],
        }
    ]


def test_build_layout_blocks_truncates_bbox_to_four_values() -> None:
    blocks = build_layout_blocks(
        [
            {
                "page_number": 1,
                "parsing_res_list": [
                    {
                        "block_id": 0,
                        "block_label": "text",
                        "block_content": "Invoice",
                        "block_bbox": [10, 20, 30, 40, 50, 60],
                    }
                ],
            }
        ]
    )

    assert blocks[0]["bbox"] == [10, 20, 30, 40]


def test_build_layout_blocks_includes_page_source_dimensions() -> None:
    blocks = build_layout_blocks(
        [
            {
                "page_number": 1,
                "width": 612,
                "height": 792,
                "parsing_res_list": [
                    {
                        "block_id": 0,
                        "block_label": "text",
                        "block_content": "Invoice",
                        "block_bbox": [10, 20, 110, 60],
                    }
                ],
            }
        ]
    )

    assert blocks[0]["source_width"] == 612
    assert blocks[0]["source_height"] == 792


def test_build_layout_blocks_skips_invalid_bbox_coordinates() -> None:
    blocks = build_layout_blocks(
        [
            {
                "page_number": 1,
                "parsing_res_list": [
                    {
                        "block_id": 0,
                        "block_label": "text",
                        "block_content": "Boolean bbox",
                        "block_bbox": [True, 20, 30, 40],
                    },
                    {
                        "block_id": 1,
                        "block_label": "text",
                        "block_content": "NaN bbox",
                        "block_bbox": [10, float("nan"), 30, 40],
                    },
                    {
                        "block_id": 2,
                        "block_label": "text",
                        "block_content": "Infinity bbox",
                        "block_bbox": [10, 20, float("inf"), 40],
                    },
                    {
                        "block_id": 3,
                        "block_label": "text",
                        "block_content": "Valid bbox",
                        "block_bbox": [10, 20, 30, 40],
                    },
                ],
            }
        ]
    )

    assert blocks == [
        {
            "ref_id": "p1b3",
            "page_id": 1,
            "page_index": 0,
            "block_id": 3,
            "label": "text",
            "text": "Valid bbox",
            "bbox": [10, 20, 30, 40],
        }
    ]


def test_local_pipeline_parse_pdf_returns_markdown_pages_and_metadata(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    loader = FakeLoader()
    parser = FakeParser()
    pipeline = LocalPipeline(loader=loader, parser=parser)

    result = pipeline.parse(pdf)

    assert isinstance(result, ParseResult)
    assert loader.loaded_path is None
    assert parser.parsed_path == pdf
    assert "Invoice" in result.markdown
    assert "Total 100.00" in result.markdown
    assert result.pages[0]["page_number"] == 1
    assert result.metadata == {
        "engine": "ppstructurev3-local",
        "mode": "local",
        "document_type": "pdf",
        "page_count": 1,
    }
    assert result.raw_response["code"] == 200
    assert result.raw_response["message"] == "Success"
    assert result.raw_response["file_type"] == "PDF"
    assert result.raw_response["result"]["markdown"] == result.markdown
    assert result.raw_response["result"]["pages"][0]["structured"][0]["text"] == "Invoice"
    assert result.raw_archive is not None
    with zipfile.ZipFile(BytesIO(result.raw_archive)) as archive:
        assert sorted(archive.namelist()) == ["result.json", "result.md"]
        assert archive.read("result.md").decode() == result.markdown
        archived_json = json.loads(archive.read("result.json").decode())
    assert archived_json == result.raw_response


def test_local_pipeline_parse_raster_merges_options_into_metadata(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(loader=FakeLoader(), parser=FakeParser())

    result = pipeline.parse(pdf, language="en")

    assert result.metadata["language"] == "en"


def test_local_pipeline_parse_raster_does_not_allow_options_to_override_reserved_metadata(
    tmp_path: Path,
) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(loader=FakeLoader(), parser=FakeParser())

    result = pipeline.parse(pdf, mode="cloud", page_count=99, language="en")

    assert result.metadata["mode"] == "local"
    assert result.metadata["page_count"] == 1
    assert result.metadata["language"] == "en"


@pytest.mark.parametrize("suffix", [".xlsx", ".docx", ".pptx"])
def test_local_pipeline_routes_office_extensions_to_office_loader(
    tmp_path: Path,
    suffix: str,
) -> None:
    path = tmp_path / f"sample{suffix}"
    path.write_bytes(b"office")
    office_loader = FakeOfficeLoader()
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        office_loader=office_loader,
    )

    result = pipeline.parse(path)

    assert office_loader.loaded_path == path
    assert result.markdown == "office markdown"
    assert result.pages == []
    assert result.metadata == {
        "mode": "local",
        "document_type": suffix.removeprefix("."),
        "page_count": 3,
    }
    assert result.raw_response["code"] == 200
    assert result.raw_response["result"]["markdown"] == "office markdown"
    assert result.raw_archive is not None
    with zipfile.ZipFile(BytesIO(result.raw_archive)) as archive:
        assert archive.read("result.md").decode() == "office markdown"


def test_local_pipeline_parse_office_merges_options_into_metadata(tmp_path: Path) -> None:
    path = tmp_path / "sample.xlsx"
    path.write_bytes(b"office")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        office_loader=FakeOfficeLoader(),
    )

    result = pipeline.parse(path, language="en")

    assert result.metadata["language"] == "en"


def test_local_pipeline_parse_office_options_do_not_override_loader_metadata(
    tmp_path: Path,
) -> None:
    path = tmp_path / "sample.xlsx"
    path.write_bytes(b"office")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        office_loader=FakeOfficeLoaderWithSheetMetadata(),
    )

    result = pipeline.parse(path, sheet_count=999, sheet_names=["Override"], language="en")

    assert result.metadata["sheet_count"] == 1
    assert result.metadata["sheet_names"] == ["Invoices"]
    assert result.metadata["language"] == "en"


@pytest.mark.parametrize("suffix", [".doc", ".ppt", ".xls"])
def test_local_pipeline_rejects_legacy_office_formats(tmp_path: Path, suffix: str) -> None:
    path = tmp_path / f"legacy{suffix}"
    path.write_bytes(b"legacy")
    pipeline = LocalPipeline(loader=FakeLoader(), parser=FakeParser())

    with pytest.raises(UnsupportedFormatError, match="convert to DOCX, PPTX, or XLSX"):
        pipeline.parse(path)


def test_local_pipeline_extract_requires_local_llm(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(loader=FakeLoader(), parser=FakeParser())

    with pytest.raises(ConfigurationError, match="local_llm must be configured"):
        pipeline.extract(pdf)


def test_local_pipeline_extract_parses_then_calls_llm_extractor(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    llm_extractor = FakeLLMExtractor()
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=llm_extractor,
    )

    result = pipeline.extract(
        pdf,
        fields=["total"],
        schema={"type": "object"},
        document_types=["invoice"],
    )

    assert result == ExtractResult(
        data={"Page_1": {"invoice_id": "INV-001"}},
        metadata={
            "engine": "ppstructurev3-local",
            "mode": "local",
            "document_type": "pdf",
            "page_count": 1,
        },
        raw_response={"ok": True},
    )
    assert llm_extractor.call == {
        "markdown": "# Invoice\n\nTotal 100.00",
        "fields": ["total"],
        "schema": {"type": "object"},
        "document_types": ["invoice"],
    }


def test_local_pipeline_extract_passes_supported_extra_llm_options(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    llm_extractor = FakeLLMExtractorWithOptions()
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=llm_extractor,
    )

    result = pipeline.extract(pdf, fields=["total"], temperature=0)

    assert llm_extractor.call is not None
    assert llm_extractor.call["temperature"] == 0
    assert sorted(llm_extractor.call["options"]) == ["layout_blocks"]
    assert result.metadata["temperature"] == 0


def test_local_pipeline_extract_preserves_llm_metadata(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=FakeLLMExtractorWithMetadata(),
    )

    result = pipeline.extract(pdf, fields=["invoice_id"])

    assert result.metadata == {
        "source_width": 100,
        "source_height": 200,
        "engine": "ppstructurev3-local",
        "mode": "local",
        "document_type": "pdf",
        "page_count": 1,
    }


def test_local_pipeline_extract_converts_local_bbox_results_to_cloud_pages(
    tmp_path: Path,
) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=FakeLLMExtractorWithBboxes(),
    )

    result = pipeline.extract(pdf, fields=["amount", "rate", "字段"])

    assert result.to_json()["results"] == {
        "Page_1": {
            "amount": "$26,250.00",
            "rate": "5.00%",
            "字段": "",
        }
    }
    assert result.metadata["source_width"] == 1000
    assert result.metadata["source_height"] == 1442


def test_local_pipeline_extract_keeps_cloud_page_results(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=FakeLLMExtractorWithPageResults(),
    )

    result = pipeline.extract(pdf, fields=["amount"])

    assert result.to_json()["results"] == {"Page_1": {"amount": "$26,250.00"}}


def test_local_pipeline_extract_drops_unrequested_tables(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=FakeLLMExtractorWithTables(),
    )

    result = pipeline.extract(pdf, fields=["Customer Name"])

    assert result.to_json()["results"] == {
        "Page_1": {
            "Customer Name": "Youna",
            "Total Amount": "428.40",
        }
    }


def test_local_pipeline_extract_nests_requested_tables_under_cloud_page(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=FakeLLMExtractorWithTables(),
    )

    result = pipeline.extract(
        pdf,
        fields={
            "keys": {"Customer Name": {"prompt": None, "mapping": None}},
            "tableHeaders": {
                "Table_1": {
                    "Amount": {"prompt": None, "mapping": None},
                    "Description": {"prompt": None, "mapping": None},
                    "Quantity": {"prompt": None, "mapping": None},
                    "Unit Price": {"prompt": None, "mapping": None},
                }
            },
            "name": "Invoice",
        },
    )

    assert result.to_json()["results"] == {
        "Page_1": {
            "Customer Name": "Youna",
            "Total Amount": "428.40",
            "tables": {
                "Table_1": [
                    {
                        "Amount": "$ 408.00",
                        "Description": "ComPDFKit API",
                        "Quantity": "1",
                        "Unit Price": "5000",
                    }
                ]
            },
        }
    }


def test_local_pipeline_extract_passes_layout_blocks_to_llm(tmp_path: Path) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    llm_extractor = FakeLLMExtractorWithOptions()
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=llm_extractor,
    )

    pipeline.extract(pdf, fields={"keys": {"Total": {"prompt": None, "mapping": None}}, "name": "Invoice"})

    assert llm_extractor.call is not None
    assert llm_extractor.call["options"]["layout_blocks"] == [
        {
            "ref_id": "p1b0",
            "page_id": 1,
            "page_index": 0,
            "block_id": 0,
            "label": "text",
            "text": "Invoice",
            "bbox": [10, 20, 110, 60],
        },
        {
            "ref_id": "p1b1",
            "page_id": 1,
            "page_index": 0,
            "block_id": 1,
            "label": "text",
            "text": "Total 100.00",
            "bbox": [20, 80, 180, 120],
        },
    ]


def test_local_pipeline_extract_does_not_forward_fixed_parameters_as_llm_options(
    tmp_path: Path,
) -> None:
    pdf = tmp_path / "invoice.pdf"
    pdf.write_bytes(b"%PDF")
    llm_extractor = FakeLLMExtractorWithOptions()
    pipeline = LocalPipeline(
        loader=FakeLoader(),
        parser=FakeParser(),
        llm_extractor=llm_extractor,
    )

    result = pipeline.extract(pdf, markdown="override", temperature=0)

    assert llm_extractor.call is not None
    assert llm_extractor.call["markdown"] == "# Invoice\n\nTotal 100.00"
    assert llm_extractor.call["temperature"] == 0
    assert "markdown" not in llm_extractor.call["options"]
    assert result.metadata["temperature"] == 0


def test_local_pipeline_from_config_without_local_llm_does_not_import_extractor() -> None:
    pipeline = LocalPipeline.from_config(DocSlightConfig(mode="local"))

    assert isinstance(pipeline, LocalPipeline)


def test_local_pipeline_from_config_wraps_missing_local_llm_module(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    from docslight.local import pipeline as pipeline_module

    def missing_llm_extractor(config: dict[str, Any]) -> Any:
        raise ModuleNotFoundError(
            "No module named 'docslight.local.llm_extractor'",
            name="docslight.local.llm_extractor",
        )

    monkeypatch.setattr(pipeline_module, "_build_llm_extractor", missing_llm_extractor)
    config = DocSlightConfig(mode="local", local_llm={"provider": "ollama", "model": "x"})

    with pytest.raises(ConfigurationError, match="local_llm"):
        LocalPipeline.from_config(config)


def test_file_loader_load_image_closes_source_and_returns_copy(
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    from docslight.local import loaders

    class FakeRGBImage:
        width = 2
        height = 3

        def __init__(self) -> None:
            self.copied = False

        def copy(self) -> FakeRGBImage:
            copied = FakeRGBImage()
            copied.copied = True
            return copied

    class FakeSourceImage:
        def __init__(self) -> None:
            self.closed = False
            self.rgb_image = FakeRGBImage()

        def __enter__(self) -> FakeSourceImage:
            return self

        def __exit__(self, *args: Any) -> None:
            self.closed = True

        def convert(self, mode: str) -> FakeRGBImage:
            assert mode == "RGB"
            return self.rgb_image

    source_image = FakeSourceImage()
    monkeypatch.setattr(loaders, "_open_pillow_image", lambda path: source_image)
    path = tmp_path / "sample.png"

    [page] = FileLoader().load(path)

    assert source_image.closed is True
    assert page.image.copied is True
    assert page.width == 2
    assert page.height == 3


def test_office_xlsx_loader_opens_read_only_and_closes_workbook(
    tmp_path: Path,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    from docslight.local import office_loader

    class FakeWorksheet:
        title = "Sheet1"

        def iter_rows(self, values_only: bool = False) -> list[tuple[str]]:
            assert values_only is True
            return [("Invoice",)]

    class FakeWorkbook:
        def __init__(self) -> None:
            self.worksheets = [FakeWorksheet()]
            self.closed = False

        def close(self) -> None:
            self.closed = True

    workbook = FakeWorkbook()
    calls: list[dict[str, Any]] = []

    def fake_load_workbook(path: Path, **kwargs: Any) -> FakeWorkbook:
        calls.append({"path": path, **kwargs})
        return workbook

    monkeypatch.setattr(office_loader, "load_workbook", fake_load_workbook)
    path = tmp_path / "sample.xlsx"

    result = office_loader.OfficeMarkdownLoader().load(path)

    assert calls == [{"path": path, "data_only": True, "read_only": True}]
    assert workbook.closed is True
    assert result.markdown == "## Sheet: Sheet1\n| Invoice |\n| --- |"
