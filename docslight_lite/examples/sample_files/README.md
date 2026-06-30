# Sample Files

Put your test documents (PDF, Word, Excel, PPT, images) in this directory, then run the example scripts.

## Directory Structure

```
examples/
├── sample_files/
│   └── invoice.pdf          # Replace with your actual file
├── cloud_extract.py
├── cloud_parse.py
├── local_extract_ollama.py
├── local_extract_openai_compatible.py
├── local_parse.py
└── path_examples.py
```

## Supported Formats

| Type | Formats |
|------|---------|
| PDF | `.pdf` |
| Word | `.docx` |
| Excel | `.xlsx` |
| PowerPoint | `.pptx` |
| Images | `.png`, `.jpg`, `.jpeg`, `.tiff` |

## Quick Start

1. Copy your test file into `sample_files/`
2. Run an example script:

```bash
cd examples
python cloud_parse.py
```
