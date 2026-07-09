# API Reference

Public SDK and CLI surface for `docslight`.

## SDK Entry

```python
from docslight import DocSlight
```

### Constructor

```python
client = DocSlight(
    mode: str | None = None,         # "cloud" | "local" (default: "cloud")
    api_key: str | None = None,      # ComPDF Cloud API key
    base_url: str | None = None,     # Cloud API base URL
    timeout: float | None = None,    # Request timeout in seconds
    local_parser: str | None = None, # Local parser selector (optional)
    local_llm: dict | None = None,   # Local LLM provider configuration
)
```

Environment variable fallbacks:

| Variable | Maps to |
|----------|---------|
| `DOCSLIGHT_API_KEY` | `api_key` |
| `DOCSLIGHT_MODE` | `mode` |

### local_llm Configuration

```python
# Ollama (default provider)
client = DocSlight(mode="local", local_llm={
    "provider": "ollama",        # Optional, defaults to "ollama"
    "model": "llama3.1",         # Required
})

# OpenAI-compatible (e.g., DashScope, vLLM, etc.)
client = DocSlight(mode="local", local_llm={
    "provider": "openai-compatible",
    "base_url": "https://your-endpoint/v1",
    "model": "your-model",
    "api_key": "your-api-key",
    "extra_body": {"enable_thinking": False},  # Optional extra params
})
```

## Parse

```python
result: ParseResult = client.parse(
    path: Path | str,
    output: str = "markdown",         # "markdown" | "json"
    **options,
)
```

`ParseResult` fields:

| Field | Type | Description |
|-------|------|-------------|
| `markdown` | `str` | Parsed Markdown content |
| `pages` | `list[dict]` | Page-level data (when returned by cloud API) |
| `metadata` | `dict` | Processing metadata |
| `raw_archive` | `bytes \| None` | Raw parse ZIP archive when returned by cloud/local parse |

Methods:

- `to_markdown() -> str` — Return parsed content as Markdown.
- `to_json() -> dict` — Return serializable dict with `markdown`, `pages`, `metadata`.

## Extract

```python
result: ExtractResult = client.extract(
    path: Path | str,
    fields: list[str] | str | dict | None = None,
    schema: dict | None = None,
    document_types: list[str] | None = None,
    **options,
)
```

### Fields Parameter

Three formats are accepted:

**1. List of field names:**
```python
result = client.extract("invoice.pdf", fields=["invoice_number", "total_amount"])
```

**2. Comma-separated string (CLI-style):**
```python
result = client.extract("invoice.pdf", fields="invoice_number,total_amount")
```

**3. Structured object with keys/tableHeaders:**
```python
result = client.extract("invoice.pdf", fields={
    "name": "Invoice",
    "keys": {
        "Title": {"prompt": null, "mapping": null},
    },
    "tableHeaders": {
        "Items": {
            "Unit Price": {"prompt": "Unit price", "mapping": null},
            "Quantity": {"prompt": "Quantity", "mapping": null},
        },
    },
})
```

### Schema Parameter

```python
result = client.extract("invoice.pdf", schema={
    "type": "object",
    "properties": {
        "invoice_number": {"type": "string"},
        "total_amount": {"type": "number"},
    },
    "required": ["invoice_number"],
})
```

### Document Types Parameter

```python
result = client.extract("invoice.pdf", fields=["total"], document_types=["invoice"])
```

### normalize_fields Utility

```python
from docslight.schemas import normalize_fields

fields = normalize_fields("field1,field2")          # -> ["field1", "field2"]
fields = normalize_fields(["a", "b"])               # -> ["a", "b"]
fields = normalize_fields({"name": "X", "keys": ...})  # -> structured dict
```

## Batch Processing

```python
results: list[ParseResult] = client.parse_batch(
    paths: list[Path | str],
    **options,
)

results: list[ExtractResult] = client.extract_batch(
    paths: list[Path | str],
    **options,
)
```

Processes documents sequentially. Each result is independent.

## Result Objects

### ParseResult

```python
result = client.parse("doc.pdf")
result.to_markdown()  # str
result.to_json()      # dict: {markdown, pages, metadata}
```

### ExtractResult

```python
result = client.extract("doc.pdf", fields=["field"])
result.to_json()  # dict: {data, metadata, raw_response}
```

## Exception Hierarchy

All exceptions inherit from `DocSlightError`:

| Exception | HTTP-like Status | Description |
|-----------|-----------------|-------------|
| `AuthenticationError` | 401 | Invalid or missing API key |
| `RateLimitError` | 429 | Rate limit exceeded |
| `CloudAPIError` | varies | ComPDF Cloud API error |
| `ConfigurationError` | 400 | Invalid configuration |
| `DependencyMissingError` | - | Missing optional dependencies |
| `LocalProcessingError` | - | Local parsing/LLM failure |
| `UnsupportedFormatError` | - | Unsupported file format |

## Cloud Client

```python
from docslight.cloud import CloudClient

client = CloudClient(api_key="your-api-key", base_url=None, timeout=120)
result = client.parse("doc.pdf")
result = client.extract("doc.pdf", fields=["field_name"])
```

## Local Pipeline

```python
from docslight.local.pipeline import LocalPipeline

pipeline = LocalPipeline.from_config(config)
result = pipeline.parse("doc.pdf")
result = pipeline.extract("doc.pdf", fields=["field_name"])
```

## CLI

```bash
docslight parse <file> [options]
docslight extract <file> [options]
docslight web [options]
```

Cloud parse can also save the raw parse ZIP archive:

```bash
docslight parse invoice.pdf --mode cloud --format zip -o invoice.zip
```

### Global Options

| Option | Description |
|--------|-------------|
| `--mode` | `cloud` or `local` |
| `--api-key` | ComPDF Cloud API key |
| `--base-url` | Cloud API base URL |
| `--local-parser` | Local parser selector |
| `--local-llm-provider` | Local LLM provider (`ollama`, `openai-compatible`) |
| `--local-llm-model` | Local LLM model name |
| `--local-llm-base-url` | Local LLM endpoint base URL |
| `--local-llm-api-key` | Local LLM API key |

### Parse Options

| Option | Description |
|--------|-------------|
| `--output, -o` | Output file path (defaults to stdout) |
| `--format` | Output format: `markdown` (default), `json`, `standard-json`, or `zip` |

### Extract Options

| Option | Description |
|--------|-------------|
| `--fields` | Comma-separated field names or JSON string |
| `--schema` | JSON Schema file path |
| `--document-types` | Document types JSON file path |
| `--output, -o` | Output file path (defaults to stdout) |

## Providers

```python
# Ollama
from docslight.providers import OllamaProvider
provider = OllamaProvider(model="llama3.1")

# OpenAI-compatible
from docslight.providers import OpenAICompatibleProvider
provider = OpenAICompatibleProvider(
    model="your-model",
    base_url="https://your-endpoint/v1",
    api_key="your-api-key",
    extra_body={"enable_thinking": False},
)
```

## Supported Inputs

| Mode | Formats |
|------|---------|
| Cloud | PDF, PNG, JPG, JPEG, TIFF, BMP, WebP, DOCX, PPTX, XLSX, plus additional formats via ComPDF Cloud API |
| Local | PDF, PNG, JPG, JPEG, TIFF, BMP, WebP, DOCX, PPTX, XLSX |

> Legacy Office formats (`.doc`, `.ppt`, `.xls`) must be converted to DOCX/PPTX/XLSX for local processing.
