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
docslight parse invoice.pdf --output invoice.md
docslight parse invoice.pdf --format zip --output invoice.zip
```

Extract specific fields:

```bash
docslight extract invoice.pdf --fields invoice_number,total_amount
```

Launch the local API server:

```bash
docslight web
# Health: http://127.0.0.1:8000/api/health

# Or run the same API server directly as a module
python -m docslight.web_app --host 0.0.0.0 --port 8000 --debug
```

## Features

- **Dual mode** — ComPDF Cloud for production-grade results, or local CPU parsing for offline evaluation
- **Parse → Markdown** — Convert PDF, DOCX, PPTX, XLSX, and images (PNG, JPG, TIFF, BMP, WebP) to clean Markdown
- **Extract → JSON** — Pull structured data by field list, JSON Schema, or structured template (key-value + table extraction)
- **CLI first** — Full-featured command-line interface, script-friendly
- **API server** — Local Flask backend exposing parse, extract, preview, health, and system-info endpoints
- **Batch processing** — `parse_batch()` / `extract_batch()` for multiple files
- **Local LLM extraction** — Ollama or any OpenAI-compatible provider for offline extraction
- **Document types** — Classify and route documents by type for cloud extraction
- **Error-safe** — Typed result objects, structured error hierarchy, no credential leaks

## Install

| Scenario | Command |
|----------|---------|
| Core SDK & CLI | `pip install docslight` |
| + Local parsing (OCR, Office) | `pip install "docslight[local]"` |
| + API server | `pip install "docslight[web]"` |
| + Local parsing and API server | `pip install "docslight[local,web]"` |

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
for r in results:
    print(r.to_markdown()[:200])
```

## CLI Usage

```bash
# Parse
docslight parse invoice.pdf --mode cloud -o invoice.zip
docslight parse invoice.pdf --mode cloud --format zip -o invoice.zip
docslight parse invoice.pdf --mode local -o invoice.zip

# Extract
docslight extract invoice.pdf --mode cloud --fields invoice_number,total_amount
docslight extract invoice.pdf --mode local --fields invoice_number --local-llm-provider ollama --local-llm-model llama3.1
docslight extract "D:\pdf\invoice\1.pdf" --mode local --fields invoice_number --local-llm-provider ollama --local-llm-model llama3.1

# Extract with schema
docslight extract invoice.pdf --schema schema.json

# API server
docslight web --host 127.0.0.1 --port 8000
```

## API Server

DocSlight includes a local Flask API server for document processing. Frontend assets are not bundled in this package.

```bash
docslight web
python -m docslight.web_app
```

- `GET /api/health`
- `GET /api/system-info`
- `POST /api/parse`
- `POST /api/extract`
- `POST /api/preview`

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DOCSLIGHT_API_KEY` | API key for cloud mode |
| `DOCSLIGHT_MODE` | Processing mode: `cloud` or `local` (default: `cloud`) |

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
