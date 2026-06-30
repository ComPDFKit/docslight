"""Configuration loading for docslight."""

from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from typing import Any

try:  # pragma: no cover - Python 3.11+ path
    import tomllib
except ModuleNotFoundError:  # pragma: no cover - Python 3.10 path
    import tomli as tomllib

from docslight.exceptions import ConfigurationError

DEFAULT_BASE_URL = "https://api-server.compdf.com"
DEFAULT_CONFIG_PATH = Path.home() / ".docslight" / "config.toml"
VALID_MODES = {"cloud", "local"}


@dataclass(frozen=True)
class DocSlightConfig:
    """Runtime configuration for docslight."""

    mode: str = "cloud"
    api_key: str | None = None
    base_url: str = DEFAULT_BASE_URL
    timeout: float = 30.0
    local_parser: str | None = None
    local_llm: dict[str, Any] | None = None

    @classmethod
    def from_sources(
        cls,
        *,
        config_path: Path | str | None = DEFAULT_CONFIG_PATH,
        mode: str | None = None,
        api_key: str | None = None,
        base_url: str | None = None,
        timeout: float | None = None,
        local_parser: str | None = None,
        local_llm: dict[str, Any] | None = None,
    ) -> DocSlightConfig:
        """Build configuration from defaults, config file, environment, and explicit values."""
        values: dict[str, Any] = {
            "mode": "cloud",
            "api_key": None,
            "base_url": DEFAULT_BASE_URL,
            "timeout": 30.0,
            "local_parser": None,
            "local_llm": None,
        }

        file_values = _load_config_file(config_path)
        values.update(file_values)
        values.update(_env_values())

        explicit_values = {
            "mode": mode,
            "api_key": api_key,
            "base_url": base_url,
            "timeout": timeout,
            "local_parser": local_parser,
            "local_llm": local_llm,
        }
        values.update({key: value for key, value in explicit_values.items() if value is not None})

        if values["mode"] not in VALID_MODES:
            allowed = ", ".join(sorted(VALID_MODES))
            raise ConfigurationError(f"mode must be one of: {allowed}")
        if values["local_llm"] is not None and not isinstance(values["local_llm"], dict):
            raise ConfigurationError("local_llm must be a table/object")
        values["timeout"] = _parse_timeout(values["timeout"])

        return cls(**values)


def _load_config_file(config_path: Path | str | None) -> dict[str, Any]:
    if config_path is None:
        return {}
    path = Path(config_path)
    if not path.exists():
        return {}
    with path.open("rb") as file_obj:
        data = tomllib.load(file_obj)
    return _known_values(data)


def _env_values() -> dict[str, Any]:
    env_map = {
        "mode": "DOCSLIGHT_MODE",
        "api_key": "DOCSLIGHT_API_KEY",
        "base_url": "DOCSLIGHT_BASE_URL",
        "timeout": "DOCSLIGHT_TIMEOUT",
        "local_parser": "DOCSLIGHT_LOCAL_PARSER",
    }
    values = {key: os.environ[name] for key, name in env_map.items() if name in os.environ}
    return values


def _known_values(data: dict[str, Any]) -> dict[str, Any]:
    values = {
        key: data[key]
        for key in ("mode", "api_key", "base_url", "timeout", "local_parser", "local_llm")
        if key in data
    }
    if "local_llm" in values and not isinstance(values["local_llm"], dict):
        raise ConfigurationError("local_llm must be a table/object")
    return values


def _parse_timeout(value: Any) -> float:
    try:
        return float(value)
    except (TypeError, ValueError) as exc:
        raise ConfigurationError("timeout must be a number") from exc
