"""Provider integrations for document parsing workflows."""

from docslight.providers.ollama import OllamaProvider
from docslight.providers.openai_compatible import OpenAICompatibleProvider

__all__ = ["OllamaProvider", "OpenAICompatibleProvider"]
