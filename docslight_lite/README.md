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

### First Run And Dependencies

DocSlight supports Python 3.10 through 3.13.

```bash
pip install "docslight"
```

Cloud mode requires network access and a valid ComPDF Cloud API key. Local mode runs on CPU by default; OCR and LLM latency depends on document size, hardware, and the selected model.

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
docslight parse invoice.pdf --mode cloud --format json -o invoice.json
docslight parse invoice.pdf --mode cloud --format standard-json -o standard.json
docslight parse invoice.pdf --mode local -o invoice.zip

# Extract
docslight extract invoice.pdf --mode cloud --fields invoice_number,total_amount
docslight extract invoice.pdf --mode local --fields invoice_number --local-llm-provider ollama --local-llm-model llama3.1
docslight extract "D:\pdf\invoice\1.pdf" --mode local --fields invoice_number --local-llm-provider ollama --local-llm-model llama3.1

# Extract with schema
docslight extract invoice.pdf --schema schema.json

# Extract with document type routing
docslight extract invoice.pdf --document-types document-types.json

# Convert local parse JSON to the standard parse JSON schema
docslight convert-parse-json parse.json -o standard.json

# API server
docslight web --host 127.0.0.1 --port 8000 --debug
```

### CLI Reference

```bash
docslight parse INPUT [OPTIONS]
docslight extract INPUT [OPTIONS]
docslight convert-parse-json INPUT [OPTIONS]
```

#### Common parse/extract options

| Option | Values / default | Description |
|--------|------------------|-------------|
| `INPUT` | File path | Document path to process. |
| `--mode` | `cloud`, `local`; default from config/env or `cloud` | Select ComPDF Cloud or local offline processing. |
| `--api-key` | String | Cloud API key. Overrides `COMPDF_API_KEY`. |
| `--base-url` | URL; default `https://api-server.compdf.com` | Cloud API base URL. Overrides `DOCSLIGHT_BASE_URL`. |
| `--local-parser` | String | Local parser selector. Currently reserved for local parser configuration. |
| `--local-llm-provider` | `ollama`, `openai`, `openai-compatible`; default `ollama` when any local LLM option is used | Local extraction LLM provider. |
| `--local-llm-model` | String | Local extraction LLM model. Required for local LLM extraction. |
| `--local-llm-base-url` | URL | Local LLM endpoint. Ollama defaults to `http://localhost:11434`; OpenAI-compatible providers require this value. |
| `--local-llm-api-key` | String | Local LLM API key. Ollama defaults to `ollama`. |

#### Parse options

| Option | Values / default | Description |
|--------|------------------|-------------|
| `--output`, `-o` | File path | Write output to a file instead of stdout. |
| `--format` | `markdown`, `json`, `standard-json`, `zip`; default `markdown` | Output format. If omitted and `--output` ends with `.zip`, DocSlight infers `zip`. |

`markdown` writes parsed Markdown. `json` writes the SDK parse result. `standard-json` writes the standard parse JSON schema. `zip` writes the raw parse archive and should normally be used with `--output`.

#### Extract options

| Option | Values / default | Description |
|--------|------------------|-------------|
| `--output`, `-o` | File path | Write extracted JSON to a file instead of stdout. |
| `--fields` | Comma-separated names, for example `invoice_number,total_amount` | Fields to extract. |
| `--schema` | JSON file path | Extraction schema JSON file. The CLI reads this file and passes the JSON object to `extract`; a common schema is `{"fields": ["invoice_no", "date", "total"]}`. JSON Schema-style objects with `properties` are also accepted. |
| `--document-types` | JSON file path | Document type routing file. The JSON root must be a list, for example `["invoice", "receipt"]`. |

Example `schema.json`:

```json
{
  "fields": ["invoice_no", "date", "total"]
}
```

#### Convert parse JSON options

| Option | Values / default | Description |
|--------|------------------|-------------|
| `INPUT` | JSON file path | Local parse JSON payload to convert. The JSON root must be an object. |
| `--output`, `-o` | File path | Write converted standard parse JSON to a file instead of stdout. |

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
| `COMPDF_API_KEY` | API key for cloud mode |
| `DOCSLIGHT_MODE` | Processing mode: `cloud` or `local` (default: `cloud`) |
| `DOCSLIGHT_BASE_URL` | Cloud API base URL (default: `https://api-server.compdf.com`) |
| `DOCSLIGHT_TIMEOUT` | Cloud request timeout in seconds (default: `30`) |
| `DOCSLIGHT_LOCAL_PARSER` | Local parser selector |

DocSlight also reads `~/.docslight/config.toml`. Values are applied in this order: built-in defaults, config file, environment variables, then explicit SDK or CLI arguments.

```toml
mode = "cloud"
api_key = "your-api-key"
base_url = "https://api-server.compdf.com"
timeout = 30
local_parser = "paddleocr"  # reserved for local parser configuration

[local_llm]
provider = "ollama"
model = "llama3.1"
base_url = "http://localhost:11434"
api_key = "ollama"
timeout = 120
```

The CLI exposes the main local LLM settings as flags. Advanced local LLM provider settings such as `extra_body` are available through the SDK or `~/.docslight/config.toml`.

## Input/Output Format Matrix

| Input type | Extensions | Cloud parse | Local parse | Cloud extract | Local extract | Parse outputs | Extract outputs | Notes |
|------------|------------|-------------|-------------|---------------|---------------|---------------|-----------------|-------|
| PDF | `.pdf` | Yes | Yes | Yes | Yes, with local LLM | Markdown, JSON, standard JSON, ZIP | JSON | Local PDF parsing uses raster/OCR processing. |
| Images | `.png`, `.jpg`, `.jpeg`, `.tif`, `.tiff`, `.bmp`, `.webp` | Yes | Yes | Yes | Yes, with local LLM | Markdown, JSON, standard JSON, ZIP | JSON | Local image parsing treats each image as one page. |
| Word | `.docx` | Yes | Yes | Yes | Yes, with local LLM | Markdown, JSON, standard JSON, ZIP | JSON | Local legacy `.doc` is not supported. |
| PowerPoint | `.pptx` | Yes | Yes | Yes | Yes, with local LLM | Markdown, JSON, standard JSON, ZIP | JSON | Local legacy `.ppt` is not supported. |
| Excel | `.xlsx` | Yes | Yes | Yes | Yes, with local LLM | Markdown, JSON, standard JSON, ZIP | JSON | Local legacy `.xls` is not supported. |
| Legacy Office | `.doc`, `.ppt`, `.xls` | Supported by ComPDF Cloud API when available | No | Supported by ComPDF Cloud API when available | No | Cloud result formats | JSON | Convert to `.docx`, `.pptx`, or `.xlsx` before local processing. |

`docslight convert-parse-json` accepts a local parse JSON object and writes the standard parse JSON schema. It does not process original document files.

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
