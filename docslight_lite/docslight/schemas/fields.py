"""Helpers for extraction field schemas."""

from __future__ import annotations

from typing import Any

from docslight.exceptions import ConfigurationError

StructuredFields = dict[str, Any]
NormalizedFields = list[str] | StructuredFields | None
ExtractSchema = dict[str, Any]


def normalize_fields(fields: list[str] | str | StructuredFields | None) -> NormalizedFields:
    """Normalize extraction fields from SDK, CLI, or API inputs."""
    if fields is None:
        return None
    if isinstance(fields, str):
        normalized = [field.strip() for field in fields.split(",") if field.strip()]
        return normalized or None
    if isinstance(fields, dict):
        return _normalize_structured_fields(fields)
    if not isinstance(fields, list):
        raise ConfigurationError(
            "fields must be a list of strings, comma-separated string, object, or None"
        )

    normalized = []
    for field in fields:
        if not isinstance(field, str):
            raise ConfigurationError(
                "fields must be a list of strings, comma-separated string, object, or None"
            )
        stripped = field.strip()
        if stripped:
            normalized.append(stripped)
    return normalized or None


def build_extract_schema(fields: NormalizedFields) -> ExtractSchema | None:
    """Build a stable JSON schema for extract outputs."""
    if not isinstance(fields, dict):
        if isinstance(fields, list):
            return {
                "type": "object",
                "properties": {
                    field: {"type": ["string", "number", "boolean", "null", "object", "array"]}
                    for field in fields
                },
                "additionalProperties": True,
            }
        return None

    properties: dict[str, Any] = {}
    required: list[str] = []
    keys = fields.get("keys", {})
    tables = fields.get("tableHeaders", {})

    for field_name in keys:
        properties[field_name] = {
            "type": "object",
            "properties": {
                "value": {"type": ["string", "number", "boolean", "null"]},
                "bboxes": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "page_id": {"type": "number"},
                            "bbox": {
                                "type": "array",
                                "items": {"type": "number"},
                                "minItems": 4,
                                "maxItems": 4,
                            },
                        },
                        "required": ["page_id", "bbox"],
                        "additionalProperties": True,
                    },
                },
            },
            "required": ["value"],
            "additionalProperties": True,
        }
        required.append(field_name)

    table_properties: dict[str, Any] = {}
    for table_name, columns in tables.items():
        table_properties[table_name] = {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    column_name: {"type": ["string", "number", "boolean", "null", "object", "array"]}
                    for column_name in columns
                },
                "additionalProperties": True,
            },
        }
        required.append(table_name)

    if table_properties:
        properties["tables"] = {
            "type": "object",
            "properties": table_properties,
            "additionalProperties": False,
        }
        properties["_table_bboxes"] = {
            "type": "object",
            "additionalProperties": {
                "type": "array",
                "items": {
                    "type": "object",
                    "properties": {
                        "page_id": {"type": "number"},
                        "bbox": {
                            "type": "array",
                            "items": {"type": "number"},
                            "minItems": 4,
                            "maxItems": 4,
                        },
                    },
                    "required": ["page_id", "bbox"],
                    "additionalProperties": True,
                },
            },
        }

    return {
        "type": "object",
        "properties": properties,
        "required": required,
        "additionalProperties": True,
    }


def _normalize_structured_fields(fields: dict[str, Any]) -> dict[str, Any]:
    name = fields.get("name")
    if not isinstance(name, str) or not name.strip():
        raise ConfigurationError("fields.name is required")

    normalized: dict[str, Any] = {"name": name.strip()}
    keys = _normalize_field_group(fields.get("keys", {}), "fields.keys")
    tables = _normalize_tables(fields.get("tableHeaders", {}))
    if keys:
        normalized["keys"] = keys
    if tables:
        normalized["tableHeaders"] = tables
    if "keys" not in normalized and "tableHeaders" not in normalized:
        raise ConfigurationError("fields must include at least one keys or tableHeaders entry")
    return normalized


def _normalize_tables(value: Any) -> dict[str, dict[str, Any]]:
    if value in (None, ""):
        return {}
    if not isinstance(value, dict):
        raise ConfigurationError("fields.tableHeaders must be an object")
    normalized: dict[str, dict[str, Any]] = {}
    for table_name, columns in value.items():
        if not isinstance(table_name, str) or not table_name.strip():
            raise ConfigurationError("fields.tableHeaders table names must be non-empty strings")
        table_columns = _normalize_field_group(
            columns,
            f"fields.tableHeaders.{table_name}",
        )
        if table_columns:
            normalized[table_name.strip()] = table_columns
    return normalized


def _normalize_field_group(value: Any, label: str) -> dict[str, dict[str, Any]]:
    if value in (None, ""):
        return {}
    if not isinstance(value, dict):
        raise ConfigurationError(f"{label} must be an object")
    normalized: dict[str, dict[str, Any]] = {}
    for field_name, field_def in value.items():
        if not isinstance(field_name, str) or not field_name.strip():
            raise ConfigurationError(f"{label} field names must be non-empty strings")
        if not isinstance(field_def, dict):
            raise ConfigurationError(f"{label}.{field_name} must be an object")
        prompt = field_def.get("prompt")
        if prompt is not None and not isinstance(prompt, str):
            raise ConfigurationError(f"{label}.{field_name}.prompt must be a string or null")
        normalized[field_name.strip()] = {
            "prompt": prompt,
            "mapping": field_def.get("mapping"),
        }
    return normalized
