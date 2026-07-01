"""OpenAI-compatible chat completion provider."""

from __future__ import annotations

from typing import Any

from docslight.exceptions import DependencyMissingError, LocalProcessingError

INSTALL_LOCAL_LLM_MESSAGE = (
    "Install local dependencies with: pip install 'docslight[local]'"
)
NO_TEXT_CONTENT_MESSAGE = "OpenAI-compatible provider returned no text content"
REQUEST_FAILED_MESSAGE = "OpenAI-compatible provider request failed"


class OpenAICompatibleProvider:
    """Provider for OpenAI-compatible chat completion APIs."""

    def __init__(
        self,
        model: str,
        base_url: str,
        api_key: str = "",
        timeout: float = 120.0,
        extra_body: dict[str, Any] | None = None,
    ) -> None:
        self.model = model
        self.base_url = base_url
        self.api_key = api_key
        self.timeout = timeout
        self.extra_body = extra_body or {}

    def complete(self, messages: list[dict[str, str]]) -> str:
        """Return chat completion content from an OpenAI-compatible endpoint."""
        try:
            from openai import OpenAI
        except ImportError as exc:
            raise DependencyMissingError(INSTALL_LOCAL_LLM_MESSAGE) from exc

        client = OpenAI(
            api_key=self.api_key,
            base_url=self.base_url,
            timeout=self.timeout,
        )
        try:
            request_kwargs: dict[str, Any] = {
                "model": self.model,
                "messages": messages,
                "temperature": 0,
            }
            if self.extra_body:
                request_kwargs["extra_body"] = self.extra_body
            response = client.chat.completions.create(**request_kwargs)
        except Exception as exc:
            raise LocalProcessingError(REQUEST_FAILED_MESSAGE) from exc
        try:
            choice = response.choices[0]
            message = choice.message
            content: Any = message.content
        except (AttributeError, IndexError, TypeError) as exc:
            raise LocalProcessingError(NO_TEXT_CONTENT_MESSAGE) from exc
        if not isinstance(content, str):
            raise LocalProcessingError(NO_TEXT_CONTENT_MESSAGE)
        return content
