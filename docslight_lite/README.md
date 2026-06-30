<p align="center">
  <h1 align="center">DocSlight</h1>
  <p align="center">Lightweight Python SDK & CLI for document parsing and structured extraction</p>
  <p align="center">
    <a href="https://pypi.org/project/docslight/"><img src="https://img.shields.io/pypi/v/docslight" alt="PyPI"></a>
    <a href="https://pypi.org/project/docslight/"><img src="https://img.shields.io/pypi/pyversions/docslight" alt="Python versions"></a>
    <a href="LICENSE"><img src="https://img.shields.io/github/license/kdanmobile/docslight" alt="License"></a>
  </p>
</p>

## What is DocSlight?

A lightweight Python library that turns PDFs, images, and Office documents into clean Markdown or structured JSON — with one line of code. Works with ComPDF Cloud (recommended) or fully offline with local parsers.

```python
from docslight import DocSlight

client = DocSlight(api_key="your-api-key")
result = client.parse("invoice.pdf")
print(result.to_markdown())
```

## Quick Start

```bash
pip install docslight
```

Parse any document:

```bash
docslight parse invoice.pdf --mode local --output invoice.md
docslight parse invoice.pdf --mode cloud --api-key your-api-key --format zip --output invoice.zip
```

Extract specific fields:

```bash
docslight extract invoice.pdf --mode cloud --api-key your-api-key --fields invoice_number,total_amount
```

## Launch the local Web UI workbench:

💡 Want a graphical interface? Runs a simple drag-and-drop local web interface to enable private offline document processing.

For users who prefer graphical interfaces, DocSlight includes a powerful self-hosted web interface. This allows you to drag and drop PDFs, DOCX, and other files directly in your browser, achieving 100% private and offline processing.

### How to Get Started?
Install the webpage via [docker-compose.yml](docker/docker-compose.yml):
```bash
pip install "docslight[web]"
docslight web

python -m docslight.web_app --host 0.0.0.0 --port 8000 --debug

docker compose -f docker/docker-compose.yml up
# Open http://127.0.0.1:3022
```

## Features

- **Dual mode** — ComPDF Cloud for production-grade results, or local CPU parsing for offline evaluation
- **Parse → Markdown** — Convert PDF, DOCX, PPTX, XLSX, and images (PNG, JPG, TIFF, BMP, WebP) to clean Markdown
- **Extract → JSON** — Pull structured data by field list, JSON Schema, or structured template (key-value + table extraction)
- **CLI first** — Full-featured command-line interface, script-friendly
- **Web UI** — Local Flask workbench with drag-and-drop, live preview with bbox highlights, and a Fields Builder UI
- **Batch processing** — `parse_batch()` / `extract_batch()` for multiple files
- **Local LLM extraction** — Ollama or any OpenAI-compatible provider for offline extraction
- **Document types** — Classify and route documents by type for cloud extraction
- **Error-safe** — Typed result objects, structured error hierarchy, no credential leaks

## Install

| Scenario | Command |
|----------|---------|
| Core SDK & CLI | `pip install docslight` |
| + Local parsing (OCR, Office) | `pip install "docslight[local]"` |
| + Local LLM extraction | `pip install "docslight[local,local-llm]"` |
| + Web UI workbench | `pip install "docslight[web]"` |

> Local CPU parsing is experimental. Validate accuracy and latency on your own documents before production use.

## SDK Usage

### Cloud — Parse

```python
from docslight import DocSlight

client = DocSlight(mode="cloud", api_key="your-api-key")

result = client.parse("invoice.pdf")
print(result.to_markdown())   # Clean markdown
print(result.to_json())       # Full result with pages + metadata
```

### Cloud — Extract

```python
result = client.extract(
    "invoice.pdf",
    fields=["invoice_number", "invoice_date", "total_amount"],
)
print(result.to_json())
```

With a JSON Schema:

```python
schema = {
    "type": "object",
    "properties": {
        "invoice_number": {"type": "string"},
        "total_amount": {"type": "number"},
    },
    "required": ["invoice_number"],
}
result = client.extract("invoice.pdf", schema=schema)
```

With document type classification:

```python
result = client.extract(
    "invoice.pdf",
    fields=["invoice_number"],
    document_types=["invoice"],
)
```

### Local — Parse (Offline)

```python
client = DocSlight(mode="local")
result = client.parse("invoice.pdf")
print(result.to_markdown())
```

### Local — Extract with Ollama

```python
client = DocSlight(
    mode="local",
    local_llm={"provider": "ollama", "model": "llama3.1"},
)
result = client.extract(
    "invoice.pdf",
    fields=["invoice_number", "invoice_date"],
)
```

### Local — Extract with OpenAI-Compatible API

```python
client = DocSlight(
    mode="local",
    local_llm={
        "provider": "openai-compatible",
        "base_url": "https://your-endpoint/v1",
        "model": "your-model",
        "api_key": "your-api-key",
        "extra_body": {"enable_thinking": False},  # e.g., DashScope qwen3
    },
)
```

### Batch Processing

```python
results = client.parse_batch(["doc1.pdf", "doc2.pdf", "doc3.pdf"])
for r in results:[release.ps1](scripts/release.ps1)
    print(r.to_markdown()[:200])
```

## CLI Usage

DocSlight CLI supports two processing modes. You must choose one explicitly with `--mode cloud` or `--mode local`.

- **Cloud mode**: requires an API key. Pass it with `--api-key` or set `DOCSLIGHT_API_KEY`. Get your API key from https://www.compdf.com/compdf-portal/signin.
- **Local mode**: runs local parsers. For local extraction, configure a local LLM provider with `--local-llm-provider` and `--local-llm-model`.

```bash
# Parse
docslight parse invoice.pdf --mode cloud --api-key your-api-key --format zip -o invoice.zip
docslight parse invoice.pdf --mode cloud --api-key your-api-key --format json -o invoice.json
docslight parse invoice.pdf --mode local --format markdown -o invoice.md
docslight parse invoice.pdf --mode local --format standard-json -o invoice.standard.json

# Extract
docslight extract invoice.pdf --mode cloud --api-key your-api-key --fields invoice_number,total_amount
docslight extract invoice.pdf --mode local --fields invoice_number --local-llm-provider ollama --local-llm-model llama3.1
docslight extract "invoice.pdf" --mode local --fields invoice_number --local-llm-provider ollama

# Extract with schema
docslight extract invoice.pdf --mode cloud --api-key your-api-key --schema schema.json

# Extract with document types
docslight extract invoice.pdf --mode cloud --api-key your-api-key --document-types document_types.json

# Web UI
docslight web --host 127.0.0.1 --port 8000
```

### Parse command options

```bash
docslight parse [options] <input>
```

| Option | Description |
|--------|-------------|
| `input` | Required input file path. |
| `--mode {cloud,local}` | Required processing mode. Use `cloud` for ComPDF Cloud or `local` for offline local parsing. |
| `--api-key API_KEY` | Cloud API key. Required in cloud mode unless `DOCSLIGHT_API_KEY` is set. |
| `--base-url BASE_URL` | Optional custom Cloud API base URL. |
| `--output, -o OUTPUT` | Output file path. Defaults to stdout for text formats. Required/recommended for ZIP output. |
| `--format {markdown,json,standard-json,zip}` | Parse output format. If omitted, Markdown is used unless the output path ends in `.zip`. |
| `--local-parser LOCAL_PARSER` | Optional local parser selector for local mode. |
| `--local-llm-provider LOCAL_LLM_PROVIDER` | Local LLM provider setting. Not required for parse-only workflows. |
| `--local-llm-model LOCAL_LLM_MODEL` | Local LLM model name. Not required for parse-only workflows. |
| `--local-llm-base-url LOCAL_LLM_BASE_URL` | Local LLM endpoint base URL, for providers that need it. |
| `--local-llm-api-key LOCAL_LLM_API_KEY` | Local LLM API key, for providers that need it. |

### Extract command options

```bash
docslight extract [options] <input>
```

| Option | Description |
|--------|-------------|
| `input` | Required input file path. |
| `--mode {cloud,local}` | Required processing mode. Use `cloud` for ComPDF Cloud or `local` for offline local extraction. |
| `--api-key API_KEY` | Cloud API key. Required in cloud mode unless `DOCSLIGHT_API_KEY` is set. |
| `--base-url BASE_URL` | Optional custom Cloud API base URL. |
| `--output, -o OUTPUT` | Output JSON file path. Defaults to stdout. |
| `--fields FIELDS` | Comma-separated field list, for example `invoice_number,total_amount`. |
| `--schema SCHEMA` | Path to a JSON Schema file. |
| `--document-types DOCUMENT_TYPES` | Path to a JSON file containing a document type list. Cloud extraction can use this to classify or route documents. |
| `--local-parser LOCAL_PARSER` | Optional local parser selector for local mode. |
| `--local-llm-provider LOCAL_LLM_PROVIDER` | Local LLM provider for local extraction, for example `ollama` or `openai-compatible`. Defaults to `ollama` when other local LLM options are provided. |
| `--local-llm-model LOCAL_LLM_MODEL` | Local LLM model name. Required for local LLM extraction. |
| `--local-llm-base-url LOCAL_LLM_BASE_URL` | Local LLM endpoint base URL, for providers that need it. |
| `--local-llm-api-key LOCAL_LLM_API_KEY` | Local LLM API key, for providers that need it. |

## Web UI Workbench

DocSlight Workbench is a local Flask app for visual document processing.

```bash
pip install "docslight[web]"
docslight web
python -m docslight.web_app
```

- **Parse & Extract tabs** — Switch between parsing and extraction workflows
- **Drag-and-drop upload** — PDF, images, DOCX, PPTX, XLSX
- **Live preview** — PDF page rendering with bbox highlight overlays
- **Fields Builder** — Structured UI for building key-value and table extraction templates
- **Download results** — One-click download of Markdown or JSON output

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DOCSLIGHT_API_KEY` | API key for cloud mode. Get one from https://www.compdf.com/compdf-portal/signin |
| `DOCSLIGHT_MODE` | Processing mode: `cloud` or `local`. The CLI examples recommend passing `--mode` explicitly. |

## Supported Inputs

| Mode | Formats |
|------|---------|
| Cloud | PDF, images (PNG/JPG/TIFF/BMP/WebP), DOCX, PPTX, XLSX, and more via ComPDF Cloud API |
| Local | PDF, images (PNG/JPG/TIFF/BMP/WebP), DOCX, PPTX, XLSX |

> Legacy Office formats (`.doc`, `.ppt`, `.xls`) must be converted to DOCX/PPTX/XLSX for local processing.

## Development

```bash
pip install -e ".[dev]"
ruff check .
mypy docslight
pytest
python -m build
```

## License

MIT License. See `LICENSE`.
