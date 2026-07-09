from __future__ import annotations

import pytest

from docslight.exceptions import ConfigurationError
from docslight.schemas import normalize_fields


def test_normalize_fields_strips_list_items() -> None:
    assert normalize_fields([" invoice_no ", "total"]) == ["invoice_no", "total"]


def test_normalize_fields_splits_comma_string() -> None:
    assert normalize_fields("invoice_no,total") == ["invoice_no", "total"]


def test_normalize_fields_returns_none_for_empty_inputs() -> None:
    assert normalize_fields(None) is None
    assert normalize_fields("") is None
    assert normalize_fields("   ") is None
    assert normalize_fields([]) is None
    assert normalize_fields(["", "  "]) is None


def test_normalize_fields_rejects_non_string_list_items() -> None:
    with pytest.raises(ConfigurationError, match="fields must be"):
        normalize_fields(["invoice_no", 3])  # type: ignore[list-item]


def test_normalize_fields_rejects_invalid_type() -> None:
    with pytest.raises(ConfigurationError, match="fields must be"):
        normalize_fields(3)  # type: ignore[arg-type]


def test_normalize_fields_preserves_order_without_deduplicating() -> None:
    assert normalize_fields(["total", "invoice_no", "total"]) == [
        "total",
        "invoice_no",
        "total",
    ]


def test_normalize_fields_accepts_structured_keys_tables_and_name() -> None:
    fields = {
        "keys": {
            "Title": {"prompt": None, "mapping": None},
            "Total": {"prompt": "Total amount", "mapping": "total_amount"},
        },
        "tableHeaders": {
            "表1": {
                "Unit Price": {"prompt": "Unit price of the item", "mapping": None},
                "Quantity": {"prompt": "Quantity of the item", "mapping": None},
            },
            "表2": {
                "Unit": {"prompt": "Unit of measure", "mapping": None},
            },
        },
        "name": " Invoice ",
    }

    assert normalize_fields(fields) == {
        "keys": {
            "Title": {"prompt": None, "mapping": None},
            "Total": {"prompt": "Total amount", "mapping": "total_amount"},
        },
        "tableHeaders": {
            "表1": {
                "Unit Price": {"prompt": "Unit price of the item", "mapping": None},
                "Quantity": {"prompt": "Quantity of the item", "mapping": None},
            },
            "表2": {
                "Unit": {"prompt": "Unit of measure", "mapping": None},
            },
        },
        "name": "Invoice",
    }


def test_normalize_fields_accepts_table_only_structured_fields() -> None:
    fields = {
        "keys": {},
        "tableHeaders": {
            "Items": {
                "Name": {"prompt": None, "mapping": None},
            },
        },
        "name": "Invoice",
    }

    assert normalize_fields(fields) == {
        "tableHeaders": {
            "Items": {
                "Name": {"prompt": None, "mapping": None},
            },
        },
        "name": "Invoice",
    }


def test_normalize_fields_rejects_structured_fields_without_name() -> None:
    with pytest.raises(ConfigurationError, match="fields.name"):
        normalize_fields({"keys": {"Title": {"prompt": None, "mapping": None}}})


def test_normalize_fields_rejects_structured_fields_without_targets() -> None:
    with pytest.raises(ConfigurationError, match="keys or tableHeaders"):
        normalize_fields({"keys": {}, "tableHeaders": {}, "name": "Invoice"})


def test_normalize_fields_rejects_invalid_structured_prompt() -> None:
    with pytest.raises(ConfigurationError, match="prompt"):
        normalize_fields(
            {
                "keys": {"Title": {"prompt": 3, "mapping": None}},
                "name": "Invoice",
            }
        )
