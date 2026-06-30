# Local Web UI (DocSlight Workbench)

A local Flask application for parsing documents to Markdown and extracting structured JSON from a browser.

## Install

```bash
# Core web UI
pip install "docslight[web]"

# With local parsing
pip install "docslight[web,local]"

# With local parsing + local LLM extraction
pip install "docslight[web,local,local-llm]"
```

## Run

```bash
docslight web
```

Open `http://127.0.0.1:8000`.

You can also launch the same app directly as a module:

```bash
python -m docslight.web_app
python -m docslight.web_app --host 0.0.0.0 --port 8000 --debug
```

Optional flags:

```bash
docslight web --host 127.0.0.1 --port 8080 --debug
```

## Features

### Workflow Tabs

- **Parse tab** — Upload and parse documents to Markdown. Clickable parser blocks when page bbox data is available.
- **Extract tab** — Upload and extract structured JSON with field/schema configuration.

### Upload

Drag-and-drop or click upload. Supported file types:

- PDF (`.pdf`)
- Images (`.png`, `.jpg`, `.jpeg`, `.tif`, `.tiff`, `.bmp`, `.webp`)
- Office (`.docx`, `.pptx`, `.xlsx`)

### Mode Selection

- **Cloud mode** — Uses ComPDF Cloud API. Requires an API key.
- **Local mode** — Uses local parsers and optionally a local LLM provider.

### Preview System

The Web UI auto-detects the file type and renders a preview:

- **PDF** — Page-by-page rendering with bbox highlight overlays.
- **Images** — Base64 data URL rendering.
- **Office files** — Preview not supported; a notice is shown.

### Fields Builder

The extraction tab includes a Fields Builder that supports:

- **Key-value fields** — Define field names with optional prompts and mapping.
- **Table extraction** — Define named tables with column headers.
- **Structured request format**:

```json
{
  "keys": {
    "Title": {"prompt": null, "mapping": null}
  },
  "tableHeaders": {
    "表1": {
      "Unit Price": {"prompt": "Unit price of the item", "mapping": null},
      "Quantity": {"prompt": "Quantity of the item", "mapping": null}
    }
  },
  "name": "Invoice"
}
```

### Highlight Overlays

- **Cloud extraction** — Uses precise bbox data from Cloud API (`bboxes`, `_table_bboxes`).
- **Local extraction** — Uses parser layout blocks for coarse bbox highlights.
- **Office files** — Highlight not supported.

### Download

Each result tab has a download button for the current Markdown or JSON output.

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Main application page |
| `/api/health` | GET | Health check |
| `/api/system-info` | GET | Supported modes and file extensions |
| `/api/parse` | POST | Parse a document |
| `/api/extract` | POST | Extract structured data |
| `/api/preview` | POST | Generate file preview |

### System Info

```
GET /api/system-info
Response:
{
  "modes": ["cloud", "local"],
  "supported_extensions": ["bmp", "docx", "jpeg", "jpg", "pdf", "png", "pptx", "tif", "tiff", "webp", "xlsx"]
}
```

## Security Notes

- The Web UI binds to `127.0.0.1` by default. Keep this unless you explicitly intend to expose it.
- Uploaded files are written to temporary files for the duration of the request only, then deleted in cleanup.
- API keys and local LLM credentials are passed to the SDK in memory; the bundled app does not persist them.
- Cloud mode sends document content to the configured ComPDF Cloud endpoint.
- Local mode keeps document processing on the host. Installed local parsers and local LLM services define the actual security boundary.
