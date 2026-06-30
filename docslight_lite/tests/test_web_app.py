from __future__ import annotations

import builtins
import json
import logging
import sys
import tempfile
from pathlib import Path
from types import SimpleNamespace
from typing import Any

import pytest

from docslight.exceptions import (
    AuthenticationError,
    DependencyMissingError,
    LocalProcessingError,
    RateLimitError,
)
from docslight.preview import render_pdf_preview
from docslight.result import ExtractResult, ParseResult
from docslight import web_app
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
            markdown="# Parsed\n\nWorkbench result",
            pages=[{"page": 1, "text": "Workbench result"}],
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
    from io import BytesIO

    return BytesIO(content), filename


def test_root_redirects_to_parse(client: Any) -> None:
    response = client.get("/")

    assert response.status_code == 302
    assert response.headers["Location"].endswith("/parse")


def test_parse_page_loads(client: Any) -> None:
    response = client.get("/parse")

    assert response.status_code == 200
    html = response.get_data(as_text=True)
    assert "DocSlight" in html
    assert 'data-page="parse"' in html
    assert 'href="/parse"' in html
    assert 'href="/extract"' in html
    assert 'class="workbench"' in html
    assert 'id="parseForm"' in html
    assert 'id="cloudConfig"' in html
    assert 'name="base_url"' in html
    assert 'name="api_key"' in html
    assert 'id="localParseNote"' in html
    assert 'id="formError"' in html
    assert 'id="submitButton"' in html
    assert 'id="downloadButton"' in html
    assert 'id="highlightStatus"' in html
    assert 'id="officePreviewNotice"' in html
    assert 'id="parseResultTabs"' in html
    assert 'id="blocksPanel"' in html
    assert 'id="markdownPanel"' in html
    assert 'id="jsonPanel"' in html
    assert 'data-result-panel="blocks"' in html
    assert 'data-result-panel="markdown"' in html
    assert 'data-result-panel="json"' in html
    assert 'src="/static/app/parse.js"' in html


def test_parse_upload_control_is_in_config_column(client: Any) -> None:
    html = client.get("/parse").get_data(as_text=True)

    form_start = html.index('id="parseForm"')
    form_end = html.index("</form>", form_start)
    specimen_start = html.index('class="panel specimen-panel"')

    assert form_start < html.index('id="dropZone"') < form_end
    assert form_start < html.index('id="fileInput"') < form_end
    assert html.index('id="dropZone"') < html.index('id="submitButton"') < form_end
    assert specimen_start > form_end


def test_parse_upload_appears_before_mode_select(client: Any) -> None:
    """Upload control should be the first interactive field in the config form."""
    html = client.get("/parse").get_data(as_text=True)

    assert html.index('id="dropZone"') < html.index('id="modeSelect"')


def test_extract_upload_appears_before_mode_select(client: Any) -> None:
    """Upload control should sit inside the extract form, above mode select."""
    html = client.get("/extract").get_data(as_text=True)

    form_start = html.index('id="extractForm"')
    form_end = html.index("</form>", form_start)
    drop_zone_pos = html.index('id="dropZone"')

    assert form_start < drop_zone_pos < form_end
    assert drop_zone_pos < html.index('id="modeSelect"')


def test_base_layout_has_no_side_rail(client: Any) -> None:
    """The side rail duplicates the topbar logo and should be removed."""
    html = client.get("/parse").get_data(as_text=True)

    assert 'class="side-rail"' not in html
    assert 'class="rail-mark"' not in html
    # Topbar logo must still be present.
    assert 'class="brand-mark"' in html


def test_styles_drop_side_rail_and_left_offset() -> None:
    """After removing the side rail, layout padding/offsets should reset to the
    standard 28px gutter, not the 88-96px reserved for the rail.
    """
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    assert ".side-rail" not in css
    assert ".rail-mark" not in css
    assert "padding: 14px 28px 14px 96px" not in css
    assert "padding: 28px 28px 42px 96px" not in css


def test_styles_workbench_columns_are_equal_height() -> None:
    """The three workbench columns should share a viewport-bound height so they
    line up rather than letting each panel grow to its own content size.
    """
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    import re

    match = re.search(r"\.workbench\s*\{([^}]*)\}", css)
    assert match, ".workbench rule is missing"
    body = match.group(1)
    assert "align-items: stretch" in body, (
        ".workbench must stretch its columns so the three panels are equal height."
    )

    panel_match = re.search(
        r"\.config-panel,\s*\.specimen-panel,\s*\.result-panel\s*\{([^}]*)\}",
        css,
    )
    assert panel_match, "shared panel rule is missing"
    panel_body = panel_match.group(1)
    assert "height: calc(100vh" in panel_body, (
        "All three panels must declare the same viewport-based height."
    )
    assert "overflow: hidden" in panel_body, (
        "Panels must clip overflow so inner regions handle scrolling."
    )


def test_styles_config_panel_body_scrolls_internally() -> None:
    """When the config form grows beyond the viewport (e.g., Extract page with
    Local LLM + Fields builder), the form body must scroll inside the panel.
    """
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    import re

    match = re.search(r"\.config-panel\s*\{([^}]*)\}", css)
    assert match, ".config-panel rule is missing"
    body = match.group(1)
    assert "overflow-y: auto" in body


def test_styles_use_light_document_workbench_theme() -> None:
    """The Web UI should match the attached light document workbench palette."""
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    assert "color-scheme: light" in css
    assert "--bg-base: #f5f7fb" in css
    assert "--bg-surface: #ffffff" in css
    assert "--accent: #4f7dff" in css
    assert "radial-gradient" not in css


def test_extract_page_loads(client: Any) -> None:
    response = client.get("/extract")

    assert response.status_code == 200
    html = response.get_data(as_text=True)
    assert "DocSlight" in html
    assert 'data-page="extract"' in html
    assert 'class="workbench"' in html
    assert 'id="extractForm"' in html
    assert 'id="cloudConfig"' in html
    assert 'id="cloudExtractMode"' in html
    assert 'id="groundingToggle"' in html
    assert 'name="base_url"' in html
    assert 'name="api_key"' in html
    assert 'id="fieldsBuilder"' in html
    assert 'id="localLlmBlock"' in html
    assert 'name="local_llm_provider"' in html
    assert 'name="local_llm_model"' in html
    assert 'name="local_llm_base_url"' in html
    assert 'name="local_llm_api_key"' in html
    assert 'name="fields"' in html
    assert 'id="formError"' in html
    assert 'id="submitButton"' in html
    assert 'id="downloadButton"' in html
    assert 'id="highlightStatus"' in html
    assert 'id="officePreviewNotice"' in html
    assert 'id="extractResultTabs"' in html
    assert 'id="fieldsPanel"' in html
    assert 'id="jsonPanel"' in html
    assert 'data-result-panel="fields"' in html
    assert 'data-result-panel="json"' in html
    assert 'src="/static/app/extract.js"' in html


def test_parse_page_does_not_render_extract_controls(client: Any) -> None:
    html = client.get("/parse").get_data(as_text=True)

    assert 'id="fieldsBuilder"' not in html
    assert 'id="localLlmBlock"' not in html
    assert 'name="local_llm_provider"' not in html
    assert 'name="schema"' not in html
    assert 'name="document_types"' not in html


def test_extract_page_hides_schema_and_document_type_controls(client: Any) -> None:
    html = client.get("/extract").get_data(as_text=True)

    assert "Schema JSON" not in html
    assert "Document types JSON" not in html
    assert 'name="schema"' not in html
    assert 'name="document_types"' not in html


def test_page_scripts_are_isolated(client: Any) -> None:
    parse_html = client.get("/parse").get_data(as_text=True)
    extract_html = client.get("/extract").get_data(as_text=True)

    assert 'src="/static/app/parse.js"' in parse_html
    assert 'src="/static/app/extract.js"' not in parse_html
    assert 'src="/static/app/extract.js"' in extract_html
    assert 'src="/static/app/parse.js"' not in extract_html


def test_base_layout_has_language_switcher(client: Any) -> None:
    html = client.get("/parse").get_data(as_text=True)

    assert 'id="languageSelect"' in html
    assert 'aria-label="Language"' in html
    assert 'option value="en"' in html
    assert 'option value="zh-CN"' in html
    assert 'option value="zh-TW"' in html
    assert 'data-i18n="nav.parse"' in html
    assert 'data-i18n="nav.extract"' in html


def test_page_templates_mark_static_copy_for_i18n(client: Any) -> None:
    parse_html = client.get("/parse").get_data(as_text=True)
    extract_html = client.get("/extract").get_data(as_text=True)

    for key in (
        "parse.eyebrow",
        "parse.title",
        "parse.description",
        "parse.run",
        "parse.resultsTitle",
    ):
        assert f'data-i18n="{key}"' in parse_html

    for key in (
        "extract.eyebrow",
        "extract.title",
        "extract.description",
        "extract.run",
        "extract.resultsTitle",
        "fields.title",
    ):
        assert f'data-i18n="{key}"' in extract_html


def test_i18n_js_declares_builtin_languages_and_core_keys() -> None:
    js_path = Path(__file__).parents[1] / "docslight" / "static" / "app" / "i18n.js"
    source = js_path.read_text(encoding="utf-8")

    for text in (
        "supportedLanguages",
        'code: "en"',
        'code: "zh-CN"',
        'code: "zh-TW"',
        "export function initI18n",
        "export function t",
        "localStorage",
        "document.documentElement.lang",
        "parse.title",
        "extract.title",
        "error.cloudApiKeyRequired",
        "preview.empty",
    ):
        assert text in source


def test_styles_include_dark_theme_tokens() -> None:
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    for token in (
        "--bg-base",
        "--bg-surface",
        "--bg-elevated",
        "--text-primary",
        "--text-secondary",
        "--accent",
        "--hl-cloud",
        "--hl-local",
        "--hl-parse",
    ):
        assert token in css


def test_styles_include_focus_layout_and_highlight_styles() -> None:
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    for selector in (
        "[hidden]",
        ":focus-visible",
        ".topbar",
        ".workbench",
        "grid-template-columns: 320px minmax(0, 1fr) minmax(0, 1fr)",
        ".config-panel",
        ".specimen-panel",
        ".result-panel",
        ".drop-zone",
        ".drop-zone:focus-within",
        ".preview-canvas",
        ".preview-page",
        "aspect-ratio: var(--preview-page-ratio)",
        ".preview-page-label",
        ".highlight-box.source-cloud",
        ".highlight-box.source-local",
        ".highlight-box.source-parse",
        ".fields-builder",
        ".field-row-card",
        ".table-field-card",
        ".result-block-card",
        ".result-field-card",
        ".result-table-card",
        "@media (max-width: 1279px)",
        "@media (max-width: 959px)",
    ):
        assert selector in css


def test_preview_page_image_uses_block_flow_not_absolute() -> None:
    """Regression: when .preview-page img is position:absolute the parent
    collapses (aspect-ratio is ignored in grid rows), so multi-page PDFs
    stack on top of each other. The image must flow normally.
    """
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    import re

    match = re.search(r"\.preview-page img\s*\{([^}]*)\}", css)
    assert match, ".preview-page img rule is missing"
    body = match.group(1)
    assert "position: absolute" not in body, (
        ".preview-page img must not be absolutely positioned, otherwise PDF "
        "pages collapse to 0 height in the grid container."
    )
    assert "width: 100%" in body
    assert "height: auto" in body


def test_result_panel_pre_has_max_height_for_scrolling() -> None:
    """Regression: long JSON used to stretch the result column.

    The result column is now bounded by the shared panel height
    (`.config-panel, .specimen-panel, .result-panel { height: calc(100vh - …) }`)
    and `[data-result-panel]` participates in that flex column, so the inner
    pre only needs `overflow: auto` and `min-height: 0` to scroll inside the
    bounded panel rather than declaring its own max-height.
    """
    css_path = Path(__file__).parents[1] / "docslight" / "static" / "styles.css"
    css = css_path.read_text(encoding="utf-8")

    import re

    panel_match = re.search(r"\[data-result-panel\]\s*\{([^}]*)\}", css)
    assert panel_match, "[data-result-panel] rule is missing"
    panel_body = panel_match.group(1)
    assert "min-height: 0" in panel_body, (
        "[data-result-panel] must allow the flex parent to shrink it."
    )

    pre_match = re.search(
        r"\[data-result-panel\][^{]*>\s*pre[^{]*\{([^}]*)\}",
        css,
    )
    assert pre_match, "[data-result-panel] > pre rule is missing"
    assert "min-height: 0" in pre_match.group(1)

    style_match = re.search(
        r"\.result-preview,\s*\[data-result-panel\] > pre\s*\{([^}]*)\}",
        css,
    )
    assert style_match, "result-panel inner content rule is missing"
    assert "overflow: auto" in style_match.group(1)


def test_common_js_exports_preview_and_highlight_helpers() -> None:
    js_path = Path(__file__).parents[1] / "docslight" / "static" / "app" / "common.js"
    source = js_path.read_text(encoding="utf-8")

    for export_name in (
        "initHealthBadge",
        "bindDropzone",
        "loadPreview",
        "renderPreview",
        "clearHighlight",
        "highlightBboxes",
        "withInheritedDimensions",
        "renderBlocksView",
        "renderMarkdownView",
        "renderJsonView",
        "renderExtractCards",
        "bindResultTabs",
        "downloadText",
        "postForm",
        "setFormError",
        "noop",
    ):
        assert f"export function {export_name}" in source

    assert "normalizeHighlightEntries" in source
    assert "findPreviewPage" in source
    assert 'if (bboxes && typeof bboxes === "object")' in source
    assert "normalizeHighlightEntries(withInheritedDimensions(bboxes.bboxes, bboxes))" in source
    assert "page.style.setProperty(\"--preview-page-ratio\"" in source
    assert "preview-page-label" in source
    assert '"value" in field' in source
    assert "field.bboxes" in source
    assert "field.block_bbox" in source
    assert "field.box" in source
    assert "Number.isFinite" in source
    assert "right <= left" in source
    assert "bottom <= top" in source
    assert "source-cloud" in source
    assert "source-local" in source
    assert "source-parse" in source


def test_page_js_imports_shared_module() -> None:
    static_dir = Path(__file__).parents[1] / "docslight" / "static" / "app"

    parse_source = (static_dir / "parse.js").read_text(encoding="utf-8")
    extract_source = (static_dir / "extract.js").read_text(encoding="utf-8")

    assert 'from "./common.js"' in parse_source
    assert 'from "./common.js"' in extract_source
    assert 'from "./i18n.js"' in parse_source
    assert 'from "./i18n.js"' in extract_source


def test_parse_js_handles_parse_only_behavior() -> None:
    js_path = Path(__file__).parents[1] / "docslight" / "static" / "app" / "parse.js"
    source = js_path.read_text(encoding="utf-8")
    common_path = Path(__file__).parents[1] / "docslight" / "static" / "app" / "common.js"
    common_source = common_path.read_text(encoding="utf-8")

    assert 'postForm("/api/parse", body)' in source
    assert 'body.set("file", fileInput.files[0])' in source
    assert "renderBlocksView" in source
    assert "renderMarkdownView" in source
    assert "renderJsonView" in source
    assert "bindResultTabs(parseResultTabs, (tab) => {" in source
    assert "state.currentTab = tab" in source
    assert "cloudConfig.hidden = !isCloud" in source
    assert "localParseNote.hidden = isCloud" in source
    assert "initI18n()" in source
    assert "onLanguageChange(refreshLocalizedDynamicCopy)" in source
    assert 't("error.cloudApiKeyRequired")' in source
    assert 't("parse.placeholder")' in source
    assert 't("parse.failed")' in source
    assert "docslight-parse.md" in source
    assert "docslight-parse.json" in source
    assert "export function bindResultTabs(tabList, onChange)" in common_source
    assert "onChange?.(tabName)" in common_source


def test_extract_js_handles_extract_only_behavior() -> None:
    js_path = Path(__file__).parents[1] / "docslight" / "static" / "app" / "extract.js"
    source = js_path.read_text(encoding="utf-8")

    assert 'postForm("/api/extract", body)' in source
    assert "function buildFieldsPayload" in source
    assert "function addTextField" in source
    assert "function addTableField" in source
    assert "renderExtractCards" in source
    assert "renderJsonView" in source
    assert "cloudConfig.hidden = !isCloud" in source
    assert 'groundingToggle.hidden = !isCloud || cloudExtractMode?.value !== "integrate"' in source
    assert "localLlmBlock.hidden = isCloud" in source
    assert "initI18n()" in source
    assert "onLanguageChange(refreshLocalizedDynamicCopy)" in source
    assert 't("error.cloudApiKeyRequired")' in source
    assert 't("error.fieldsRequired")' in source
    assert 't("error.localLlmRequired")' in source
    assert 't("extract.placeholder")' in source
    assert 't("extract.failed")' in source
    assert "docslight-extract.json" in source
    assert "fieldTemplateName?.value.trim()" in source
    assert 'body.set("file", fileInput.files[0])' in source
    assert 'body.set("fields", JSON.stringify(fields))' in source
    assert 'body.set("cloud_extract_mode", cloudExtractMode?.value || "vlm")' in source
    assert 'body.delete("enable_grounding")' in source


def test_health_endpoint(client: Any) -> None:
    response = client.get("/api/health")

    assert response.status_code == 200
    assert response.get_json() == {"status": "healthy", "service": "docslight-web"}


def test_web_app_main_uses_defaults(monkeypatch: Any) -> None:
    calls: list[tuple[str, int, bool]] = []
    monkeypatch.setattr(web_app, "run_web_app", lambda host, port, debug: calls.append((host, port, debug)))

    exit_code = web_app.main([])

    assert exit_code == 0
    assert calls == [("127.0.0.1", 8000, False)]


def test_web_app_main_passes_host_port_and_debug(monkeypatch: Any) -> None:
    calls: list[tuple[str, int, bool]] = []
    monkeypatch.setattr(web_app, "run_web_app", lambda host, port, debug: calls.append((host, port, debug)))

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
    assert payload["result"]["pages"] == [{"page": 1, "text": "Workbench result"}]
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


def test_parse_endpoint_returns_raw_cloud_parse_archive(client: Any) -> None:
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
        data={"file": upload("sample.pdf"), "mode": "cloud", "api_key": "secret"},
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


def test_extract_endpoint_accepts_structured_fields_json(client: Any) -> None:
    fields = {
        "keys": {"Title": {"prompt": None, "mapping": None}},
        "tableHeaders": {
            "表1": {
                "Unit Price": {"prompt": "Unit price of the item", "mapping": None},
                "Quantity": {"prompt": "Quantity of the item", "mapping": None},
            }
        },
        "name": "Invoice",
    }

    response = client.post(
        "/api/extract",
        data={"file": upload("invoice.pdf"), "fields": json.dumps(fields)},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    _, kwargs = FakeDocSlight.instances[0].extract_calls[0]
    assert kwargs["fields"] == fields
    assert kwargs["schema"]["type"] == "object"
    assert "Title" in kwargs["schema"]["properties"]
    assert "document_types" not in kwargs


def test_extract_endpoint_rejects_invalid_structured_fields(client: Any) -> None:
    response = client.post(
        "/api/extract",
        data={"file": upload("invoice.pdf"), "fields": json.dumps({"name": "Invoice"})},
        content_type="multipart/form-data",
    )

    assert response.status_code == 400
    assert response.get_json()["success"] is False
    assert "keys or tableHeaders" in response.get_json()["error"]


def test_preview_endpoint_returns_office_unsupported_message(client: Any) -> None:
    response = client.post(
        "/api/preview",
        data={"file": upload("sample.docx")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"] == {
        "kind": "unsupported",
        "message": "Office files can be processed, but preview and positioning highlight are not supported in this version.",
    }


def test_preview_endpoint_returns_image_preview(client: Any) -> None:
    response = client.post(
        "/api/preview",
        data={"file": upload("sample.png", b"image-bytes")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"]["kind"] == "image"
    assert payload["result"]["mime_type"] == "image/png"
    assert payload["result"]["data_url"] == "data:image/png;base64,aW1hZ2UtYnl0ZXM="


def test_preview_endpoint_accepts_non_ascii_image_filename(client: Any) -> None:
    """Regression: secure_filename strips Chinese chars, losing the .png suffix
    and causing _preview_payload to raise 'Unsupported file preview extension.'
    """
    response = client.post(
        "/api/preview",
        data={"file": upload("截图.png", b"image-bytes")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["success"] is True
    assert payload["result"]["kind"] == "image"
    assert payload["result"]["mime_type"] == "image/png"


def test_preview_endpoint_image_payload_includes_dimensions(
    client: Any,
    monkeypatch: Any,
) -> None:
    """Image preview payload must include width/height so the front end can
    apply the correct aspect-ratio before <img> finishes loading.
    """
    monkeypatch.setattr(
        "docslight.web_app._probe_image_size",
        lambda path: (640, 480),
    )
    response = client.post(
        "/api/preview",
        data={"file": upload("photo.png", b"image-bytes")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    payload = response.get_json()
    assert payload["result"]["width"] == 640
    assert payload["result"]["height"] == 480


def test_preview_endpoint_uses_pdf_renderer(client: Any, monkeypatch: Any) -> None:
    def fake_render_pdf_preview(path: Path) -> dict[str, Any]:
        assert path.suffix == ".pdf"
        return {
            "kind": "pdf",
            "pages": [
                {
                    "page_id": 1,
                    "page_index": 0,
                    "width": 100,
                    "height": 200,
                    "image": "data:image/png;base64,cGRm",
                }
            ],
        }

    monkeypatch.setattr("docslight.web_app.render_pdf_preview", fake_render_pdf_preview)
    response = client.post(
        "/api/preview",
        data={"file": upload("sample.pdf", b"%PDF")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert response.get_json()["result"]["pages"][0]["width"] == 100


def test_render_pdf_preview_returns_page_image_data(monkeypatch: Any, tmp_path: Path) -> None:
    class FakePixmap:
        def tobytes(self, image_format: str) -> bytes:
            assert image_format == "png"
            return b"png-bytes"

    class FakePage:
        rect = SimpleNamespace(width=100, height=200)

        def get_pixmap(self, **kwargs: Any) -> FakePixmap:
            assert kwargs == {"matrix": (1, 1), "alpha": False}
            return FakePixmap()

    class FakeDocument:
        def __enter__(self) -> FakeDocument:
            return self

        def __exit__(self, *args: Any) -> None:
            return None

        def __len__(self) -> int:
            return 1

        def load_page(self, index: int) -> FakePage:
            assert index == 0
            return FakePage()

    fake_fitz = SimpleNamespace(
        Matrix=lambda width, height: (width, height),
        open=lambda path: FakeDocument(),
    )
    monkeypatch.setitem(sys.modules, "fitz", fake_fitz)

    result = render_pdf_preview(tmp_path / "sample.pdf")

    assert result == {
        "kind": "pdf",
        "pages": [
            {
                "page_id": 1,
                "page_index": 0,
                "width": 100.0,
                "height": 200.0,
                "image": "data:image/png;base64,cG5nLWJ5dGVz",
            }
        ],
    }


def test_render_pdf_preview_renders_all_pages_by_default(
    monkeypatch: Any,
    tmp_path: Path,
) -> None:
    class FakePixmap:
        def __init__(self, index: int) -> None:
            self.index = index

        def tobytes(self, image_format: str) -> bytes:
            assert image_format == "png"
            return f"png-{self.index}".encode()

    class FakePage:
        rect = SimpleNamespace(width=100, height=200)

        def __init__(self, index: int) -> None:
            self.index = index

        def get_pixmap(self, **kwargs: Any) -> FakePixmap:
            assert kwargs == {"matrix": (1, 1), "alpha": False}
            return FakePixmap(self.index)

    class FakeDocument:
        def __enter__(self) -> FakeDocument:
            return self

        def __exit__(self, *args: Any) -> None:
            return None

        def __len__(self) -> int:
            return 4

        def load_page(self, index: int) -> FakePage:
            return FakePage(index)

    fake_fitz = SimpleNamespace(
        Matrix=lambda width, height: (width, height),
        open=lambda path: FakeDocument(),
    )
    monkeypatch.setitem(sys.modules, "fitz", fake_fitz)

    result = render_pdf_preview(tmp_path / "sample.pdf")

    assert [page["page_id"] for page in result["pages"]] == [1, 2, 3, 4]


def test_render_pdf_preview_respects_explicit_max_pages(
    monkeypatch: Any,
    tmp_path: Path,
) -> None:
    class FakePixmap:
        def tobytes(self, image_format: str) -> bytes:
            assert image_format == "png"
            return b"png-bytes"

    class FakePage:
        rect = SimpleNamespace(width=100, height=200)

        def get_pixmap(self, **kwargs: Any) -> FakePixmap:
            assert kwargs == {"matrix": (1, 1), "alpha": False}
            return FakePixmap()

    class FakeDocument:
        def __enter__(self) -> FakeDocument:
            return self

        def __exit__(self, *args: Any) -> None:
            return None

        def __len__(self) -> int:
            return 4

        def load_page(self, index: int) -> FakePage:
            assert index in {0, 1}
            return FakePage()

    fake_fitz = SimpleNamespace(
        Matrix=lambda width, height: (width, height),
        open=lambda path: FakeDocument(),
    )
    monkeypatch.setitem(sys.modules, "fitz", fake_fitz)

    result = render_pdf_preview(tmp_path / "sample.pdf", max_pages=2)

    assert [page["page_id"] for page in result["pages"]] == [1, 2]
    assert [page["page_index"] for page in result["pages"]] == [0, 1]


def test_render_pdf_preview_missing_pymupdf_raises_dependency_error(
    monkeypatch: Any,
    tmp_path: Path,
) -> None:
    original_import = builtins.__import__

    def fake_import(name: str, *args: Any, **kwargs: Any) -> Any:
        if name == "fitz":
            raise ModuleNotFoundError(name="fitz")
        return original_import(name, *args, **kwargs)

    monkeypatch.setattr(builtins, "__import__", fake_import)

    with pytest.raises(DependencyMissingError):
        render_pdf_preview(tmp_path / "sample.pdf")


def test_render_pdf_preview_render_failure_raises_local_processing_error(
    monkeypatch: Any,
    tmp_path: Path,
) -> None:
    def fail_open(path: Path) -> Any:
        raise RuntimeError("bad pdf")

    fake_fitz = SimpleNamespace(open=fail_open)
    monkeypatch.setitem(sys.modules, "fitz", fake_fitz)

    with pytest.raises(LocalProcessingError):
        render_pdf_preview(tmp_path / "sample.pdf")


def test_parse_endpoint_requires_file(client: Any) -> None:
    response = client.post("/api/parse", data={"mode": "cloud"})

    assert response.status_code == 400
    assert response.get_json()["success"] is False
    assert "file" in response.get_json()["error"].lower()


def test_unsupported_extension_returns_400(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={"file": upload("notes.txt")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 400
    assert response.get_json()["success"] is False
    assert "unsupported" in response.get_json()["error"].lower()


def test_extract_local_llm_form_fields_build_local_llm_dict(client: Any) -> None:
    response = client.post(
        "/api/extract",
        data={
            "file": upload("invoice.pdf"),
            "mode": "local",
            "local_llm_model": "llama3.1",
            "local_llm_base_url": "http://localhost:11434",
            "local_llm_api_key": "local-secret",
        },
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert FakeDocSlight.instances[0].init_kwargs["local_llm"] == {
        "provider": "ollama",
        "model": "llama3.1",
        "base_url": "http://localhost:11434",
        "api_key": "local-secret",
    }


def test_temp_file_is_cleaned_after_parse(client: Any) -> None:
    response = client.post(
        "/api/parse",
        data={"file": upload("cleanup.pdf")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 200
    assert FakeDocSlight.seen_paths
    assert not FakeDocSlight.seen_paths[0].exists()


def test_temp_file_is_cleaned_when_upload_save_fails(
    client: Any,
    monkeypatch: Any,
    tmp_path: Path,
) -> None:
    created_paths: list[Path] = []
    original_named_temporary_file = tempfile.NamedTemporaryFile

    def fake_named_temporary_file(*args: Any, **kwargs: Any) -> Any:
        temp_file = original_named_temporary_file(
            mode="w+b",
            delete=False,
            dir=tmp_path,
            prefix="failed-save",
            suffix=".pdf",
        )
        created_paths.append(Path(temp_file.name))
        return temp_file

    def fail_save(self: Any, dst: Any, buffer_size: int = 16384) -> None:
        Path(dst.name).write_bytes(b"partial upload")
        raise RuntimeError("save failed")

    monkeypatch.setattr("docslight.web_app.tempfile.NamedTemporaryFile", fake_named_temporary_file)
    monkeypatch.setattr("werkzeug.datastructures.FileStorage.save", fail_save)

    response = client.post(
        "/api/parse",
        data={"file": upload("cleanup.pdf")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 500
    assert created_paths
    assert all(not path.exists() for path in created_paths)


@pytest.mark.parametrize(
    ("error", "status_code"),
    [
        (AuthenticationError("bad key"), 401),
        (RateLimitError("slow down"), 429),
    ],
)
def test_parse_maps_authentication_and_rate_limit_errors(
    client: Any,
    error: Exception,
    status_code: int,
) -> None:
    FakeDocSlight.parse_error = error

    response = client.post(
        "/api/parse",
        data={"file": upload("sample.pdf")},
        content_type="multipart/form-data",
    )

    assert response.status_code == status_code
    assert response.get_json() == {"success": False, "error": str(error)}
