from __future__ import annotations

import os
import sys
import types
from pathlib import Path
from typing import Any

import pytest

from docslight.exceptions import LocalProcessingError, UnsupportedFormatError
from docslight.local import FileLoader, PaddleOCRParser
from docslight.local.paddle_parser import DEFAULT_PPSTRUCTUREV3_OPTIONS


class FakeStructureResult:
    def __init__(self, markdown: Any, payload: dict[str, Any]) -> None:
        self.markdown = markdown
        self.json = payload


class FakeStructurePipeline:
    def __init__(
        self,
        results: list[FakeStructureResult] | None = None,
        *,
        combined_markdown: Any | None = None,
    ) -> None:
        self.results = results or []
        self.combined_markdown = combined_markdown
        self.predicted_inputs: list[str] = []
        self.concatenated_pages: list[Any] | None = None

    def predict(self, *, input: str) -> list[FakeStructureResult]:
        self.predicted_inputs.append(input)
        return self.results

    def concatenate_markdown_pages(self, markdown_pages: list[Any]) -> Any:
        self.concatenated_pages = markdown_pages
        return self.combined_markdown


def test_file_loader_rejects_html_with_unsupported_format_message(tmp_path: Path) -> None:
    path = tmp_path / "sample.html"
    path.write_text("<html></html>")

    with pytest.raises(
        UnsupportedFormatError,
        match="Unsupported local format|FileLoader supports only PDF and image",
    ):
        FileLoader().load(path)


def test_paddle_ocr_parser_accepts_injected_structure_pipeline(tmp_path: Path) -> None:
    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")
    pipeline = FakeStructurePipeline(
        [
            FakeStructureResult(
                {"markdown_texts": "# Invoice\n\nTotal 100"},
                {
                    "res": {
                        "page_number": 1,
                        "parsing_res_list": [{"block_label": "text", "block_content": "Invoice"}],
                        "layout_parsing_result": {"model": "PP-StructureV3"},
                    }
                },
            )
        ]
    )

    result = PaddleOCRParser(pipeline=pipeline).parse(source)

    assert pipeline.predicted_inputs == [str(source)]
    assert result.markdown == "# Invoice\n\nTotal 100"
    assert result.pages == [
        {
            "page_number": 1,
            "parsing_res_list": [{"block_label": "text", "block_content": "Invoice"}],
            "layout_parsing_result": {"model": "PP-StructureV3"},
        }
    ]


def test_paddle_ocr_parser_instantiates_ppstructurev3_cpu_and_uses_predict(
    monkeypatch: pytest.MonkeyPatch,
    capsys: pytest.CaptureFixture[str],
    tmp_path: Path,
) -> None:
    created: list[Any] = []

    class FakePPStructureV3:
        def __init__(self, **kwargs: Any) -> None:
            self.kwargs = kwargs
            self.predicted_inputs: list[str] = []
            created.append(self)

        def predict(self, *, input: str) -> list[FakeStructureResult]:
            self.predicted_inputs.append(input)
            return [
                FakeStructureResult(
                    "# Invoice",
                    {
                        "res": {
                            "page_number": 1,
                            "parsing_res_list": [{"block_content": "Invoice"}],
                        }
                    }
                )
            ]

    _install_fake_paddle(monkeypatch, cuda=False, device_count=0)
    _install_fake_paddleocr(monkeypatch, FakePPStructureV3)
    source = tmp_path / "invoice.pdf"
    source.write_bytes(b"%PDF")

    result = PaddleOCRParser().parse(source)

    assert len(created) == 1
    assert created[0].kwargs == {**DEFAULT_PPSTRUCTUREV3_OPTIONS, "device": "cpu"}
    assert created[0].predicted_inputs == [str(source)]
    assert result.markdown == "# Invoice"
    assert result.pages[0]["parsing_res_list"] == [{"block_content": "Invoice"}]
    assert "DocSlight local PP-StructureV3 inference device: CPU" in capsys.readouterr().err


def test_paddle_ocr_parser_disables_paddlex_mkldnn_by_default(
    monkeypatch: pytest.MonkeyPatch,
    tmp_path: Path,
) -> None:
    class FakePPStructureV3:
        def __init__(self, **kwargs: Any) -> None:
            pass

        def predict(self, *, input: str) -> list[FakeStructureResult]:
            return [FakeStructureResult("", {"res": {"page_number": 1}})]

    monkeypatch.delenv("PADDLE_PDX_ENABLE_MKLDNN_BYDEFAULT", raising=False)
    _install_fake_paddle(monkeypatch, cuda=False, device_count=0)
    _install_fake_paddleocr(monkeypatch, FakePPStructureV3)
    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")

    PaddleOCRParser().parse(source)

    assert os.environ["PADDLE_PDX_ENABLE_MKLDNN_BYDEFAULT"] == "0"


def test_paddle_ocr_parser_concatenates_multipage_markdown(tmp_path: Path) -> None:
    source = tmp_path / "invoice.pdf"
    source.write_bytes(b"%PDF")
    first_markdown = {"markdown_texts": "# Page 1"}
    second_markdown = {"markdown_texts": "# Page 2"}
    pipeline = FakeStructurePipeline(
        [
            FakeStructureResult(first_markdown, {"res": {"page_number": 1}}),
            FakeStructureResult(second_markdown, {"res": {"page_number": 2}}),
        ],
        combined_markdown={"markdown_texts": "# Page 1\n\n# Page 2"},
    )

    result = PaddleOCRParser(pipeline=pipeline).parse(source)

    assert pipeline.concatenated_pages == [first_markdown, second_markdown]
    assert result.markdown == "# Page 1\n\n# Page 2"
    assert [page["page_number"] for page in result.pages] == [1, 2]


def test_paddle_ocr_parser_uses_gpu_when_paddle_reports_cuda(
    monkeypatch: pytest.MonkeyPatch,
    capsys: pytest.CaptureFixture[str],
    tmp_path: Path,
) -> None:
    created_kwargs: list[dict[str, Any]] = []

    class FakePPStructureV3:
        def __init__(self, **kwargs: Any) -> None:
            created_kwargs.append(kwargs)

        def predict(self, *, input: str) -> list[FakeStructureResult]:
            return [FakeStructureResult("", {"res": {"page_number": 1}})]

    _install_fake_paddle(monkeypatch, cuda=True, device_count=1)
    _install_fake_paddleocr(monkeypatch, FakePPStructureV3)
    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")

    result = PaddleOCRParser().parse(source)

    assert result.markdown == ""
    assert created_kwargs[0]["device"] == "gpu"
    assert "DocSlight local PP-StructureV3 inference device: GPU" in capsys.readouterr().err


def test_paddle_ocr_parser_uses_cpu_when_cuda_has_no_devices(
    monkeypatch: pytest.MonkeyPatch,
    capsys: pytest.CaptureFixture[str],
    tmp_path: Path,
) -> None:
    created_kwargs: list[dict[str, Any]] = []

    class FakePPStructureV3:
        def __init__(self, **kwargs: Any) -> None:
            created_kwargs.append(kwargs)

        def predict(self, *, input: str) -> list[FakeStructureResult]:
            return [FakeStructureResult("", {"res": {"page_number": 1}})]

    _install_fake_paddle(monkeypatch, cuda=True, device_count=0)
    _install_fake_paddleocr(monkeypatch, FakePPStructureV3)
    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")

    result = PaddleOCRParser().parse(source)

    assert result.markdown == ""
    assert created_kwargs[0]["device"] == "cpu"
    assert "DocSlight local PP-StructureV3 inference device: CPU" in capsys.readouterr().err


def test_paddle_ocr_parser_rejects_bad_result_format(tmp_path: Path) -> None:
    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")
    parser = PaddleOCRParser(pipeline=FakeStructurePipeline([object()]))

    with pytest.raises(LocalProcessingError, match="Unexpected PP-StructureV3 result format"):
        parser.parse(source)


def test_paddle_ocr_parser_wraps_pipeline_predict_failures(tmp_path: Path) -> None:
    class FailingPipeline:
        @staticmethod
        def predict(*, input: str) -> list[Any]:
            raise NotImplementedError("oneDNN failure")

    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")
    parser = PaddleOCRParser(pipeline=FailingPipeline())

    with pytest.raises(LocalProcessingError, match="Local PaddleOCR parsing failed"):
        parser.parse(source)


def test_file_loader_loads_small_png_with_dimensions(tmp_path: Path) -> None:
    from PIL import Image

    path = tmp_path / "sample.png"
    Image.new("RGB", (3, 2), color="white").save(path)

    [page] = FileLoader().load(path)

    assert page.page_number == 1
    assert page.width == 3
    assert page.height == 2
    assert page.source_path == path


def test_paddle_ocr_parser_treats_empty_results_as_empty_parse(tmp_path: Path) -> None:
    source = tmp_path / "invoice.png"
    source.write_bytes(b"image")
    parser = PaddleOCRParser(pipeline=FakeStructurePipeline([]))

    result = parser.parse(source)

    assert result.markdown == ""
    assert result.pages == []


def _install_fake_paddle(
    monkeypatch: pytest.MonkeyPatch,
    *,
    cuda: bool,
    device_count: int,
) -> None:
    module = types.ModuleType("paddle")

    class FakeCuda:
        @staticmethod
        def device_count() -> int:
            return device_count

    class FakeDevice:
        cuda = FakeCuda()

        @staticmethod
        def is_compiled_with_cuda() -> bool:
            return cuda

    module.device = FakeDevice()  # type: ignore[attr-defined]
    module.is_compiled_with_cuda = lambda: cuda  # type: ignore[attr-defined]
    monkeypatch.setitem(sys.modules, "paddle", module)


def _install_fake_paddleocr(
    monkeypatch: pytest.MonkeyPatch,
    ppstructure_cls: type[Any],
) -> None:
    module = types.ModuleType("paddleocr")
    module.PPStructureV3 = ppstructure_cls  # type: ignore[attr-defined]
    monkeypatch.setitem(sys.modules, "paddleocr", module)
