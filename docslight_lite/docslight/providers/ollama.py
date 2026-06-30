"""Ollama local chat completion provider."""

from __future__ import annotations

from docslight.providers.openai_compatible import OpenAICompatibleProvider


class OllamaProvider(OpenAICompatibleProvider):
    """OpenAI-compatible provider configured for Ollama."""

    def __init__(
        self,
        model: str,
        base_url: str = "http://localhost:11434",
        api_key: str = "ollama",
        timeout: float = 120.0,
    ) -> None:
        super().__init__(
            model=model,
            base_url=_normalize_ollama_base_url(base_url),
            api_key=api_key,
            timeout=timeout,
        )


def _normalize_ollama_base_url(base_url: str) -> str:
    normalized = base_url.rstrip("/")
    if normalized.endswith("/v1"):
        return normalized
    return f"{normalized}/v1"
