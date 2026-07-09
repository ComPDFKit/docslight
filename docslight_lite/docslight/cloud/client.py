"""ComPDF Cloud API client."""

from __future__ import annotations

import io
import json
import logging
import zipfile
from pathlib import Path
from typing import Any
from urllib.parse import urlparse, urlunparse

import requests

from docslight.config import DEFAULT_BASE_URL
from docslight.exceptions import AuthenticationError, CloudAPIError, RateLimitError
from docslight.result import ExtractResult, ParseResult
from docslight.schemas import normalize_fields

UNSAFE_ERROR_MESSAGE_MARKERS = ("<", ">", "\n", "\r", "traceback", "bearer ", "sk-")
MAX_ERROR_MESSAGE_LENGTH = 200
SUCCESS_API_CODES = {"0", "200"}
RESULT_PAYLOAD_KEYS = {"data", "markdown", "metadata", "pages", "results"}
logger = logging.getLogger(__name__)


class CloudClient:
    """Client for ComPDF Cloud document parsing and extraction APIs."""

    def __init__(
        self,
        api_key: str | None = None,
        base_url: str = DEFAULT_BASE_URL,
        timeout: float = 120.0,
        session: requests.Session | None = None,
    ) -> None:
        self.api_key = api_key
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self._owns_session = session is None
        self.session = session if session is not None else requests.Session()

    def __enter__(self) -> CloudClient:
        """Return this client for use as a context manager."""
        return self

    def __exit__(self, *exc_info: object) -> None:
        """Close owned resources when leaving a context manager."""
        self.close()

    def close(self) -> None:
        """Close the internally-owned HTTP session."""
        if self._owns_session:
            self.session.close()

    def parse(self, path: str | Path, **options: Any) -> ParseResult:
        """Parse a document into markdown."""
        request_options = dict(options)
        download_result = _pop_bool_option(request_options, "download_result", True)
        payload, direct_archive = self._post_file("parse", path, request_options)
        process_payload, envelope_metadata = _unwrap_process_payload(payload)
        result_payload, downloaded_archive = self._result_payload(
            process_payload,
            download_result,
        )
        raw_archive = direct_archive or downloaded_archive
        pages = result_payload.get("pages")
        metadata = _merge_metadata(result_payload, envelope_metadata)
        markdown = _parse_markdown(result_payload)
        return ParseResult(
            markdown=markdown,
            pages=pages if isinstance(pages, list) else [],
            metadata=metadata,
            raw_response=result_payload,
            raw_archive=raw_archive,
        )

    def extract(
        self,
        path: str | Path,
        fields: Any = None,
        schema: Any = None,
        document_types: Any = None,
        **options: Any,
    ) -> ExtractResult:
        """Extract structured data from a document."""
        download_result = _pop_bool_option(options, "download_result", True)
        request_options = {
            "extractFields": fields,
            "schema": schema,
            "document_types": document_types,
            **options,
        }
        payload, _direct_archive = self._post_file("extract", path, request_options)
        process_payload, envelope_metadata = _unwrap_process_payload(payload)
        result_payload, _downloaded_archive = self._result_payload(
            process_payload,
            download_result,
        )
        data = _extract_data(result_payload)
        metadata = _merge_metadata(result_payload, envelope_metadata)
        return ExtractResult(
            data=data if isinstance(data, dict) else {},
            metadata=metadata,
            raw_response=payload,
        )

    def health(self) -> dict[str, Any]:
        """Return the Cloud API health payload."""
        try:
            response = self.session.get(
                self._endpoint_url("health"),
                headers=self._headers(),
                timeout=self.timeout,
            )
        except requests.RequestException as exc:
            raise CloudAPIError(f"Cloud API request failed: {exc}") from exc

        payload = self._response_json(response, allow_invalid_error=True)
        self._raise_for_error(response, payload)
        return payload

    def _post_file(
        self,
        operation: str,
        path: str | Path,
        options: dict[str, Any],
    ) -> tuple[dict[str, Any], bytes | None]:
        file_path = Path(path)
        prepared_options = self._prepare_options(operation, options)
        endpoint_url = self._endpoint_url(operation)
        logger.info("Calling ComPDF Cloud %s endpoint: POST %s", operation, endpoint_url)
        try:
            with file_path.open("rb") as file_obj:
                response = self.session.post(
                    endpoint_url,
                    files={"file": (file_path.name, file_obj)},
                    data=self._compact_options(prepared_options),
                    headers=self._headers(),
                    timeout=self.timeout,
                )
        except requests.RequestException as exc:
            raise CloudAPIError(f"Cloud API request failed: {exc}") from exc

        return self._decode_response(response, operation)

    def _result_payload(
        self,
        process_payload: dict[str, Any],
        download_result: bool,
    ) -> tuple[dict[str, Any], bytes | None]:
        if _has_result_content(process_payload):
            return process_payload, None
        download_url = process_payload.get("downloadUrl") or process_payload.get("download_url")
        if download_result and isinstance(download_url, str) and download_url:
            return self._download_result_payload(download_url)
        return {}, None

    def _download_result_payload(self, url: str) -> tuple[dict[str, Any], bytes | None]:
        try:
            response = self.session.get(url, timeout=self.timeout)
        except requests.RequestException as exc:
            raise CloudAPIError(f"Cloud API result download failed: {exc}") from exc

        if response.status_code >= 400:
            payload = self._response_json(response, allow_invalid_error=True)
            self._raise_for_error(response, payload)

        content = getattr(response, "content", b"")
        if isinstance(content, bytes) and content:
            return _read_downloaded_result_payload(content)
        return self._response_json(response), None

    def _prepare_options(self, operation: str, options: dict[str, Any]) -> dict[str, Any]:
        if operation != "extract":
            return options

        prepared = dict(options)
        if "extract_fields" in prepared:
            return prepared

        fields = prepared.pop("fields", None)
        if fields is None:
            fields = prepared.pop("extractFields", None)
        else:
            prepared.pop("extractFields", None)
        schema = prepared.pop("schema", None)
        document_types = prepared.pop("document_types", None)
        extract_fields = _to_extract_fields_payload(fields, schema)

        if extract_fields is not None:
            prepared["extract_fields"] = extract_fields
        elif schema is not None:
            prepared["schema"] = schema

        if document_types is not None:
            prepared["document_types"] = document_types

        return prepared

    def _compact_options(self, options: dict[str, Any]) -> dict[str, str]:
        compacted: dict[str, str] = {}
        for key, value in options.items():
            if value is None:
                continue
            if isinstance(value, str):
                compacted[key] = value
            else:
                try:
                    compacted[key] = json.dumps(
                        value,
                        ensure_ascii=False,
                        separators=(",", ":"),
                    )
                except TypeError as exc:
                    raise CloudAPIError(
                        f"Cloud API option '{key}' is not JSON serializable"
                    ) from exc
        return compacted

    def _headers(self) -> dict[str, str]:
        headers = {"User-Agent": "docslight/0.1.4"}
        if self.api_key:
            headers["Authorization"] = f"Bearer {self.api_key}"
            headers["x-api-key"] = self.api_key
        return headers

    def _decode_response(
        self,
        response: requests.Response,
        operation: str,
    ) -> tuple[dict[str, Any], bytes | None]:
        if _looks_like_zip_response(response):
            payload = _parse_zip_payload(response.content)
            self._raise_for_error(response, payload)
            return payload, response.content

        payload = self._response_json(response, allow_invalid_error=True)
        self._raise_for_error(response, payload)
        if operation == "extract":
            payload = _normalize_extract_response(payload)
        return payload, None

    def _endpoint_url(self, operation: str) -> str:
        if operation not in {"parse", "extract", "health"}:
            raise CloudAPIError(f"Unsupported cloud operation: {operation}")

        if not self._uses_custom_operation_urls():
            if operation == "health":
                return f"{self.base_url}/v1/health"
            if operation == "parse":
                return f"{self.base_url}/server/v2/process/idp/documentParsing"
            return f"{self.base_url}/server/v2/process/idp/documentExtract"

        parsed = urlparse(self.base_url)
        segments = [segment for segment in parsed.path.split("/") if segment]
        if not segments:
            path = f"/{operation}"
        elif segments[-1] in {"parse", "extract", "health"}:
            segments[-1] = operation
            path = "/" + "/".join(segments)
        else:
            path = parsed.path.rstrip("/") + f"/{operation}"
        return urlunparse(parsed._replace(path=path))

    def _uses_custom_operation_urls(self) -> bool:
        path = urlparse(self.base_url).path.rstrip("/")
        return path.endswith("/parse") or path.endswith("/extract") or path.endswith("/health")

    def _response_json(
        self,
        response: requests.Response,
        allow_invalid_error: bool = False,
    ) -> dict[str, Any]:
        try:
            payload = response.json()
        except ValueError as exc:
            if allow_invalid_error and response.status_code >= 400:
                return {}
            raise CloudAPIError("Cloud API returned invalid JSON") from exc
        if not isinstance(payload, dict):
            if allow_invalid_error and response.status_code >= 400:
                return {}
            raise CloudAPIError("Cloud API returned non-object JSON")
        return payload

    def _raise_for_error(
        self,
        response: requests.Response,
        payload: dict[str, Any],
    ) -> None:
        status_code = response.status_code
        api_status_code = _api_status_code(payload)
        if status_code < 400 and api_status_code is None:
            return

        error_status_code = api_status_code or status_code
        if status_code < 400 and str(error_status_code) in SUCCESS_API_CODES:
            return

        message = _safe_error_message(payload, error_status_code)
        request_id = response.headers.get("x-request-id") or response.headers.get(
            "X-Request-ID"
        )
        if error_status_code == 401:
            raise AuthenticationError(
                message,
                status_code=error_status_code,
                request_id=request_id,
            )
        if error_status_code == 429:
            raise RateLimitError(
                message,
                status_code=error_status_code,
                request_id=request_id,
            )
        raise CloudAPIError(message, status_code=error_status_code, request_id=request_id)


def _safe_error_message(payload: dict[str, Any], status_code: int) -> str:
    fallback = f"Cloud API error {status_code}"
    raw_message = payload.get("message") or payload.get("msg") or payload.get("error")
    if not isinstance(raw_message, str):
        return fallback
    message = raw_message.strip()
    if not message or len(message) > MAX_ERROR_MESSAGE_LENGTH:
        return fallback
    lowered = message.lower()
    if any(marker in lowered for marker in UNSAFE_ERROR_MESSAGE_MARKERS):
        return fallback
    return message


def _unwrap_process_payload(
    payload: dict[str, Any],
) -> tuple[dict[str, Any], dict[str, Any]]:
    if "code" not in payload:
        return payload, {}
    data = payload.get("data")
    process_payload = data if isinstance(data, dict) else {}
    envelope_metadata: dict[str, Any] = {}
    code = payload.get("code")
    msg = payload.get("msg")
    if code is not None:
        envelope_metadata["api_code"] = code
    if isinstance(msg, str) and msg:
        envelope_metadata["api_message"] = msg
    envelope_metadata.update(_process_metadata(process_payload))
    return process_payload, envelope_metadata


def _process_metadata(process_payload: dict[str, Any]) -> dict[str, Any]:
    return {
        key: value
        for key, value in process_payload.items()
        if key not in RESULT_PAYLOAD_KEYS
    }


def _has_result_content(payload: dict[str, Any]) -> bool:
    if isinstance(payload.get("markdown"), str):
        return True
    if isinstance(payload.get("pages"), list):
        return True
    if isinstance(payload.get("results"), dict):
        return True
    if "code" in payload:
        return False
    if isinstance(payload.get("data"), dict):
        return True
    return False


def _extract_data(payload: dict[str, Any]) -> dict[str, Any]:
    data = payload.get("data")
    if isinstance(data, dict):
        return data
    results = payload.get("results")
    if isinstance(results, dict):
        return results
    return payload


def _parse_markdown(payload: dict[str, Any]) -> str:
    markdown = payload.get("markdown")
    if isinstance(markdown, str):
        return markdown
    markdown_texts = payload.get("markdown_texts")
    if isinstance(markdown_texts, str):
        return markdown_texts
    pages = payload.get("pages")
    if not isinstance(pages, list):
        return ""
    page_markdowns = [_page_markdown(page) for page in pages if isinstance(page, dict)]
    return "\n\n".join(markdown for markdown in page_markdowns if markdown)


def _page_markdown(page: dict[str, Any]) -> str:
    markdown = page.get("markdown")
    if isinstance(markdown, str):
        return markdown
    markdown_texts = page.get("markdown_texts")
    if isinstance(markdown_texts, str):
        return markdown_texts
    blocks = page.get("parsing_res_list")
    if not isinstance(blocks, list):
        return ""
    parts = []
    for block in blocks:
        if not isinstance(block, dict):
            continue
        text = block.get("block_content") or block.get("block_text") or block.get("text")
        if isinstance(text, str) and text.strip():
            parts.append(text.strip())
    return "\n\n".join(parts)


def _merge_metadata(
    result_payload: dict[str, Any],
    envelope_metadata: dict[str, Any],
) -> dict[str, Any]:
    metadata = {}
    payload_metadata = result_payload.get("metadata")
    if isinstance(payload_metadata, dict):
        metadata.update(payload_metadata)
    metadata.update(envelope_metadata)
    return metadata


def _read_downloaded_result_payload(content: bytes) -> tuple[dict[str, Any], bytes | None]:
    json_payload = _read_json_payload(content)
    if json_payload is not None:
        return json_payload, None
    return _parse_zip_payload(content), content


def _read_json_payload(content: bytes) -> dict[str, Any] | None:
    stripped = content.lstrip()
    if not stripped or stripped[:1] not in {b"{", b"["}:
        return None
    try:
        payload = json.loads(content.decode("utf-8-sig"))
    except ValueError:
        return None
    if not isinstance(payload, dict):
        raise CloudAPIError("Cloud API result download returned non-object JSON")
    return _unwrap_result_file_payload(payload)


def _unwrap_result_file_payload(payload: dict[str, Any]) -> dict[str, Any]:
    data = payload.get("data")
    if "code" in payload and isinstance(data, dict):
        return data
    if isinstance(data, dict) and any(
        key in data for key in ("markdown", "metadata", "pages", "results")
    ):
        result_payload = dict(data)
        metadata = payload.get("metadata")
        if isinstance(metadata, dict) and "metadata" not in result_payload:
            result_payload["metadata"] = metadata
        return result_payload
    return payload


def _pop_bool_option(options: dict[str, Any], key: str, default: bool) -> bool:
    value = options.pop(key, default)
    return value if isinstance(value, bool) else default


def _api_status_code(payload: dict[str, Any]) -> int | None:
    code = payload.get("code")
    if code is None and payload.get("success") is False:
        return 400
    if code is None:
        return None
    if str(code) in SUCCESS_API_CODES:
        return None
    try:
        return int(str(code))
    except ValueError:
        return 400


def _looks_like_zip_response(response: requests.Response) -> bool:
    content_type = response.headers.get("content-type", "").lower()
    return "application/zip" in content_type or "application/x-zip-compressed" in content_type


def _parse_zip_payload(content: bytes) -> dict[str, Any]:
    try:
        archive = zipfile.ZipFile(io.BytesIO(content))
    except zipfile.BadZipFile as exc:
        raise CloudAPIError("Cloud API returned an invalid ZIP response") from exc

    json_payload: dict[str, Any] = {}
    markdown = ""

    for name in archive.namelist():
        if name.endswith(".json") and not json_payload:
            with archive.open(name) as file_obj:
                loaded = json.load(file_obj)
                if isinstance(loaded, dict):
                    json_payload = loaded
        elif name.endswith(".md") and not markdown:
            with archive.open(name) as file_obj:
                markdown = file_obj.read().decode("utf-8", errors="replace")

    result = json_payload.get("result") if isinstance(json_payload.get("result"), dict) else json_payload
    result = _unwrap_result_file_payload(result)
    pages = _zip_parse_pages(result.get("pages"))
    metadata = result.get("metadata") if isinstance(result.get("metadata"), dict) else {}
    metadata = {
        **metadata,
        "response_format": "zip",
        "archive_entries": archive.namelist(),
    }
    payload = {
        **result,
        "pages": pages,
        "metadata": metadata,
    }
    return {
        **payload,
        "markdown": markdown or _parse_markdown(payload),
    }


def _zip_parse_pages(value: Any) -> list[dict[str, Any]]:
    if not isinstance(value, list):
        return []
    if any(
        isinstance(page, dict) and isinstance(page.get("parsing_res_list"), list)
        for page in value
    ):
        return [page for page in value if isinstance(page, dict)]
    if any(
        isinstance(page, dict) and isinstance(page.get("structured"), list)
        for page in value
    ):
        return _normalize_parse_pages(value)
    return [page for page in value if isinstance(page, dict)]


def _normalize_parse_pages(value: Any) -> list[dict[str, Any]]:
    if not isinstance(value, list):
        return []

    pages: list[dict[str, Any]] = []
    for index, page in enumerate(value):
        if not isinstance(page, dict):
            continue
        blocks = []
        for item in page.get("structured", []):
            if not isinstance(item, dict):
                continue
            bbox = _quad_to_bbox(item.get("pos"))
            block = {
                "block_content": item.get("text") or "",
                "block_type": item.get("type"),
            }
            if bbox is not None:
                block["block_bbox"] = bbox
            blocks.append(block)
        pages.append(
            {
                "page_id": page.get("page_id") or page.get("page") or index + 1,
                "page_index": index,
                "width": page.get("width"),
                "height": page.get("height"),
                "parsing_res_list": blocks,
            }
        )
    return pages


def _quad_to_bbox(value: Any) -> list[float] | None:
    if not isinstance(value, list) or len(value) < 8:
        return None
    numbers = [float(item) for item in value[:8]]
    xs = numbers[0::2]
    ys = numbers[1::2]
    return [min(xs), min(ys), max(xs), max(ys)]


def _to_extract_fields_payload(fields: Any, schema: Any) -> dict[str, Any] | None:
    normalized_fields = normalize_fields(fields)
    if isinstance(normalized_fields, dict):
        return normalized_fields
    if isinstance(normalized_fields, list):
        return {
            "keys": {name: {} for name in normalized_fields},
            "tableHeaders": {},
            "name": "Document",
        }
    if isinstance(schema, dict):
        properties = schema.get("properties")
        if isinstance(properties, dict):
            return {
                "keys": {name: {} for name in properties},
                "tableHeaders": {},
                "name": schema.get("title") or "Document",
            }
    return None


def _normalize_extract_response(payload: dict[str, Any]) -> dict[str, Any]:
    results = payload.get("results")
    if not isinstance(results, dict):
        return payload

    normalized_payload = {
        **payload,
        "data": results,
    }
    page_keys = [key for key in results if isinstance(key, str) and key.startswith("Page_")]
    if len(page_keys) == 1 and isinstance(results[page_keys[0]], dict):
        flattened = results[page_keys[0]]
        return {
            **normalized_payload,
            "results": flattened,
            "data": flattened,
            "metadata": {
                **(payload.get("metadata") if isinstance(payload.get("metadata"), dict) else {}),
                "page_key": page_keys[0],
            },
        }
    return normalized_payload
