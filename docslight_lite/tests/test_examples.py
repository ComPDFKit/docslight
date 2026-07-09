import re
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parents[1]
OPENAI_COMPATIBLE_EXAMPLE = ROOT_DIR / "examples" / "local_extract_openai_compatible.py"


def test_openai_compatible_example_uses_placeholders() -> None:
    assert OPENAI_COMPATIBLE_EXAMPLE.exists(), (
        "OpenAI-compatible local extraction example should exist"
    )

    source = OPENAI_COMPATIBLE_EXAMPLE.read_text(encoding="utf-8")

    assert '"provider": "openai-compatible"' in source
    assert '"base_url": "your-base-url"' in source
    assert '"model": "your-model"' in source
    assert '"api_key": "your-api-key"' in source
    assert '"extra_body": {"enable_thinking": False}' in source
    assert re.search(r"sk-[A-Za-z0-9]{20,}", source) is None
