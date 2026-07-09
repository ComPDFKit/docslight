"""Local Flask web application for DocSlight."""

from __future__ import annotations

import argparse
import base64
import json
import logging
import sys
import tempfile
from collections.abc import Callable
from io import BytesIO
from json import JSONDecodeError
from pathlib import Path
from typing import Any, cast

from flask import Flask, Response, jsonify, request, send_file
from werkzeug.datastructures import FileStorage
from werkzeug.utils import secure_filename

from docslight import DocSlight
from docslight.exceptions import (
    AuthenticationError,
    CloudAPIError,
    ConfigurationError,
    DocSlightError,
    RateLimitError,
)
from docslight.preview import render_pdf_preview
from docslight.schemas import build_extract_schema, normalize_fields

ALLOWED_EXTENSIONS = {
    "pdf",
    "png",
    "jpg",
    "jpeg",
    "tif",
    "tiff",
    "bmp",
    "webp",
    "docx",
    "pptx",
    "xlsx",
}
IMAGE_MIME_TYPES = {
    "png": "image/png",
    "jpg": "image/jpeg",
    "jpeg": "image/jpeg",
    "tif": "image/tiff",
    "tiff": "image/tiff",
    "bmp": "image/bmp",
    "webp": "image/webp",
}
OFFICE_EXTENSIONS = {"docx", "pptx", "xlsx"}
OFFICE_PREVIEW_UNSUPPORTED_MESSAGE = (
    "Office files can be processed, but preview and positioning highlight are not supported in this version."
)
LOG_FORMAT = "%(levelname)s:%(name)s:%(message)s"


def create_app(docslight_factory: Callable[..., Any] = DocSlight) -> Flask:
    """Create the local DocSlight Flask application."""
    app = Flask(__name__)

    @app.get("/")
    def index() -> Any:
        return jsonify(
            {
                "status": "healthy",
                "service": "docslight-web",
            }
        )

    @app.get("/api/health")
    def health() -> Any:
        return jsonify({"status": "healthy", "service": "docslight-web"})

    @app.get("/api/system-info")
    def system_info() -> Any:
        return jsonify(
            {
                "modes": ["cloud", "local"],
                "supported_extensions": sorted(ALLOWED_EXTENSIONS),
            }
        )

    @app.post("/api/parse")
    def parse_document() -> Any:
        file_response = _require_upload()
        if not isinstance(file_response, FileStorage):
            return file_response

        return _with_temp_upload(
            file_response,
            lambda path: _parse_response_payload(
                docslight_factory(**_client_kwargs(include_local_llm=False)).parse(path)
            ),
        )

    @app.post("/api/extract")
    def extract_document() -> Any:
        file_response = _require_upload()
        if not isinstance(file_response, FileStorage):
            return file_response

        def operation(path: Path) -> dict[str, Any]:
            extract_options: dict[str, Any] = {}
            fields = _parse_fields_form_field()
            if fields is not None:
                extract_options["fields"] = fields
                derived_schema = build_extract_schema(fields)
                if derived_schema is not None:
                    extract_options["schema"] = derived_schema

            schema = _parse_json_form_field("schema")
            if schema is not None:
                extract_options["schema"] = schema

            document_types = _parse_json_form_field("document_types")
            if document_types is not None:
                if not isinstance(document_types, list):
                    raise ValueError("document_types must be a JSON list")
                extract_options["document_types"] = document_types

            if _blank_to_none(request.form.get("mode")) != "local":
                extract_mode = _blank_to_none(request.form.get("cloud_extract_mode")) or "vlm"
                extract_options["mode"] = extract_mode
                enable_grounding = _parse_bool_form_field("enable_grounding")
                if extract_mode == "integrate" and enable_grounding is not None:
                    extract_options["enable_grounding"] = enable_grounding

            payload = docslight_factory(**_client_kwargs()).extract(path, **extract_options).to_json()
            return cast(dict[str, Any], payload)

        return _with_temp_upload(file_response, operation, wrap_result=False)

    @app.post("/api/preview")
    def preview_document() -> Any:
        file_response = _require_upload()
        if not isinstance(file_response, FileStorage):
            return file_response

        return _with_temp_upload(file_response, _preview_payload)

    def _client_kwargs(include_local_llm: bool = True) -> dict[str, Any]:
        kwargs = {
            "mode": _blank_to_none(request.form.get("mode")),
            "api_key": _blank_to_none(request.form.get("api_key")),
            "base_url": _blank_to_none(request.form.get("base_url")),
        }
        if include_local_llm:
            kwargs["local_llm"] = local_llm_from_form(request.form)
        else:
            kwargs["local_llm"] = None
        return kwargs

    return app


def run_web_app(
    host: str = "127.0.0.1",
    port: int = 8000,
    debug: bool = False,
) -> None:
    """Run the local DocSlight web application."""
    _configure_web_logging(debug)
    create_app().run(host=host, port=port, debug=debug)


def _configure_web_logging(debug: bool) -> None:
    if not debug:
        return
    root_logger = logging.getLogger()
    if not root_logger.handlers:
        logging.basicConfig(level=logging.INFO, format=LOG_FORMAT)
    else:
        root_logger.setLevel(logging.INFO)
    logging.getLogger("docslight").setLevel(logging.INFO)


def build_parser() -> argparse.ArgumentParser:
    """Build the standalone web application argument parser."""
    parser = argparse.ArgumentParser(
        prog="python -m docslight.web_app",
        description="Run the DocSlight web application.",
    )
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8000)
    parser.add_argument("--debug", action="store_true")
    return parser


def main(argv: list[str] | None = None) -> int:
    """Run the standalone DocSlight web application entrypoint."""
    args = build_parser().parse_args(argv)
    run_web_app(args.host, args.port, args.debug)
    return 0


def local_llm_from_form(form: Any) -> dict[str, str] | None:
    """Build local LLM settings from web form values."""
    values = {
        "provider": _blank_to_none(form.get("local_llm_provider")),
        "model": _blank_to_none(form.get("local_llm_model")),
        "base_url": _blank_to_none(form.get("local_llm_base_url")),
        "api_key": _blank_to_none(form.get("local_llm_api_key")),
    }
    if not any(values.values()):
        return None
    if values["provider"] is None:
        values["provider"] = "ollama"
    return {key: value for key, value in values.items() if value is not None}


def _parse_response_payload(result: Any) -> Any:
    raw_archive = getattr(result, "raw_archive", None)
    if isinstance(raw_archive, bytes) and raw_archive:
        metadata = getattr(result, "metadata", {})
        filename = "docslight-parse.zip"
        if isinstance(metadata, dict):
            filename = str(metadata.get("downFileName") or metadata.get("taskId") or filename)
            if not filename.endswith(".zip"):
                filename = f"{filename}.zip"
        return send_file(
            BytesIO(raw_archive),
            mimetype="application/zip",
            as_attachment=True,
            download_name=filename,
        )
    raw_response = getattr(result, "raw_response", None)
    if isinstance(raw_response, dict):
        return raw_response
    return cast(dict[str, Any], result.to_json())


def _require_upload() -> FileStorage | Any:
    upload = request.files.get("file")
    if upload is None or upload.filename is None or upload.filename == "":
        return _error_response("A file upload is required.", 400)
    if not _is_allowed_filename(upload.filename):
        return _error_response("Unsupported file extension.", 400)
    return upload


def _with_temp_upload(
    upload: FileStorage,
    operation: Callable[[Path], Any],
    wrap_result: bool = True,
) -> Any:
    temp_path: Path | None = None
    try:
        suffix = _safe_upload_suffix(upload.filename)
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
            temp_path = Path(temp_file.name)
            upload.save(temp_file)

        result = operation(temp_path)
        if isinstance(result, Response):
            return result
        if not wrap_result:
            return jsonify({"success": True, **result})
        return jsonify({"success": True, "result": result})
    except Exception as exc:  # noqa: B902
        return _exception_response(exc)
    finally:
        if temp_path is not None:
            temp_path.unlink(missing_ok=True)


def _safe_upload_suffix(filename: str | None) -> str:
    """Extract a lowercase ASCII suffix from the original upload filename.

    ``secure_filename`` strips non-ASCII characters wholesale, so filenames
    like ``"截图.png"`` collapse to ``"png"`` and lose the ``.png`` extension.
    The temp file then has no suffix and ``_preview_payload`` rejects it as
    "Unsupported file preview extension.". We therefore inspect the original
    filename ourselves and only fall back to ``secure_filename`` if the
    extracted suffix is allowed.
    """
    if not filename:
        return ""
    suffix = Path(filename).suffix.lower()
    bare = suffix.lstrip(".")
    if bare and bare in ALLOWED_EXTENSIONS:
        return suffix
    fallback = Path(secure_filename(filename) or "upload").suffix.lower()
    return fallback


def _parse_json_form_field(name: str) -> Any:
    value = _blank_to_none(request.form.get(name))
    if value is None:
        return None
    return json.loads(value)


def _parse_fields_form_field() -> Any:
    value = _blank_to_none(request.form.get("fields"))
    if value is None:
        return None
    stripped = value.strip()
    if stripped.startswith("{"):
        return normalize_fields(json.loads(stripped))
    return normalize_fields(stripped)


def _parse_bool_form_field(name: str) -> bool | None:
    value = _blank_to_none(request.form.get(name))
    if value is None:
        return None
    normalized = value.strip().lower()
    if normalized in {"1", "true", "yes", "on"}:
        return True
    if normalized in {"0", "false", "no", "off"}:
        return False
    raise ValueError(f"{name} must be a boolean value")


def _preview_payload(path: Path) -> dict[str, Any]:
    suffix = path.suffix.lower().lstrip(".")
    if suffix == "pdf":
        return render_pdf_preview(path)
    if suffix in IMAGE_MIME_TYPES:
        encoded = base64.b64encode(path.read_bytes()).decode("ascii")
        width, height = _probe_image_size(path)
        payload: dict[str, Any] = {
            "kind": "image",
            "mime_type": IMAGE_MIME_TYPES[suffix],
            "data_url": f"data:{IMAGE_MIME_TYPES[suffix]};base64,{encoded}",
        }
        if width is not None and height is not None:
            payload["width"] = width
            payload["height"] = height
        return payload
    if suffix in OFFICE_EXTENSIONS:
        return {"kind": "unsupported", "message": OFFICE_PREVIEW_UNSUPPORTED_MESSAGE}
    raise ValueError("Unsupported file preview extension.")


def _probe_image_size(path: Path) -> tuple[int | None, int | None]:
    """Best-effort image dimension probe. Returns (None, None) on failure so
    the front end can fall back to <img>.naturalWidth/naturalHeight.
    """
    try:
        from PIL import Image
    except ImportError:
        return None, None
    try:
        with Image.open(path) as image:
            return int(image.width), int(image.height)
    except Exception:  # noqa: BLE001
        return None, None


def _is_allowed_filename(filename: str) -> bool:
    suffix = Path(filename).suffix.lower().lstrip(".")
    return suffix in ALLOWED_EXTENSIONS


def _blank_to_none(value: str | None) -> str | None:
    if value is None:
        return None
    stripped = value.strip()
    return stripped or None


def _exception_response(error: Exception) -> Any:
    if isinstance(error, AuthenticationError):
        return _error_response(str(error), 401)
    if isinstance(error, RateLimitError):
        return _error_response(str(error), 429)
    if isinstance(error, CloudAPIError) and error.status_code is not None:
        return _error_response(str(error), error.status_code)
    if isinstance(error, (ConfigurationError, ValueError, JSONDecodeError)):
        return _error_response(str(error), 400)
    if isinstance(error, DocSlightError):
        return _error_response(str(error), 400)
    return _error_response("Internal server error.", 500)


def _error_response(message: str, status_code: int) -> Any:
    return jsonify({"success": False, "error": message}), status_code


if __name__ == "__main__":
    sys.exit(main())

