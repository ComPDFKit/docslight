import { onLanguageChange, t } from "./i18n.js";

const highlightSourceClasses = {
  cloud: "source-cloud",
  local: "source-local",
  parse: "source-parse",
};

export function noop() {}

function textValue(value, fallback = "") {
  if (value === null || value === undefined) return fallback;
  if (typeof value === "string") return value;
  return String(value);
}

function setPaneText(pane, text) {
  if (!pane) return;
  pane.replaceChildren();
  const pre = document.createElement("pre");
  pre.className = "result-preview";
  pre.textContent = textValue(text);
  pane.append(pre);
}

function actionButton(text, onClick) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "ghost-button";
  button.textContent = text;
  button.addEventListener("click", onClick);
  return button;
}

function pageSourceDimensions(page) {
  return {
    width: page?.width || page?.source_width || page?.page_width || null,
    height: page?.height || page?.source_height || page?.page_height || null,
  };
}

function isPositiveFinite(value) {
  return Number.isFinite(value) && value > 0;
}

function applyPageGeometry(page, width, height) {
  if (width) page.dataset.width = String(width);
  if (height) page.dataset.height = String(height);
  if (isPositiveFinite(Number(width)) && isPositiveFinite(Number(height))) {
    page.style.setProperty("--preview-page-ratio", `${Number(width)} / ${Number(height)}`);
  }
}

function appendPageLabel(page, pageNumber) {
  const label = document.createElement("span");
  label.className = "preview-page-label";
  label.textContent = t("preview.pageLabel", { page: pageNumber });
  page.append(label);
}

function normalizeHighlightEntries(bboxes) {
  if (bboxes && typeof bboxes === "object" && !Array.isArray(bboxes) && bboxes.bboxes) {
    return normalizeHighlightEntries(withInheritedDimensions(bboxes.bboxes, bboxes));
  }

  const entries = Array.isArray(bboxes) && bboxes.length >= 4 && bboxes.every((value) => typeof value === "number")
    ? [{ page_id: 1, bbox: bboxes }]
    : (Array.isArray(bboxes) ? bboxes : []);
  if (bboxes && typeof bboxes === "object" && !Array.isArray(bboxes)) {
    entries.push(bboxes);
  }

  return entries
    .map((entry) => {
      if (Array.isArray(entry)) return { page_id: 1, bbox: entry };
      if (!entry || typeof entry !== "object") return null;
      return {
        page_id: entry.page_id || entry.page || 1,
        page_index: entry.page_index,
        bbox: entry.bbox || entry.bboxes || entry.block_bbox || entry.box,
        source_width: entry.source_width || entry.width || entry.page_width,
        source_height: entry.source_height || entry.height || entry.page_height,
      };
    })
    .filter((entry) => entry && Array.isArray(entry.bbox) && entry.bbox.length >= 4);
}

function previewFallbackMessage(message) {
  if (!message || message === "Office files can be processed, but preview and positioning highlight are not supported in this version.") {
    return t("preview.officeUnsupported");
  }
  return message;
}

function findPreviewPage(canvas, entry) {
  const pageId = entry.page_id ?? entry.page ?? 1;
  const pageIndex = entry.page_index ?? Number(pageId) - 1;
  const pageIdText = String(pageId);
  const pageIndexText = String(pageIndex);

  return Array.from(canvas.querySelectorAll(".preview-page")).find((page) => (
    page.dataset.pageId === pageIdText || page.dataset.pageIndex === pageIndexText
  )) || null;
}

export function initHealthBadge(healthStatus, endpoint = "/api/health") {
  if (!healthStatus) return;

  const renderHealthStatus = () => {
    if (healthStatus.dataset.healthUnavailable === "true") {
      healthStatus.textContent = t("health.unavailable");
      return;
    }
    if (healthStatus.dataset.healthService && healthStatus.dataset.healthState) {
      healthStatus.textContent = t("health.status", {
        service: healthStatus.dataset.healthService,
        status: healthStatus.dataset.healthState,
      });
      return;
    }
    healthStatus.textContent = t("health.checking");
  };

  onLanguageChange(renderHealthStatus);

  fetch(endpoint)
    .then((response) => response.json())
    .then((payload) => {
      healthStatus.dataset.healthService = payload.service;
      healthStatus.dataset.healthState = payload.status;
      delete healthStatus.dataset.healthUnavailable;
      renderHealthStatus();
    })
    .catch(() => {
      healthStatus.dataset.healthUnavailable = "true";
      renderHealthStatus();
    });
}

export function bindDropzone({ dropZone, fileInput, fileName, onFileChange } = {}) {
  if (!dropZone || !fileInput) return;

  const updateFileName = () => {
    if (fileName) {
      fileName.textContent = fileInput.files.length ? fileInput.files[0].name : t("drop.none");
    }
  };
  const notifyChange = () => {
    updateFileName();
    if (typeof onFileChange === "function") onFileChange(fileInput.files[0] || null);
  };

  dropZone.addEventListener("dragover", (event) => {
    event.preventDefault();
    dropZone.classList.add("is-dragging");
  });
  dropZone.addEventListener("dragleave", () => {
    dropZone.classList.remove("is-dragging");
  });
  dropZone.addEventListener("drop", (event) => {
    event.preventDefault();
    dropZone.classList.remove("is-dragging");
    if (event.dataTransfer.files.length) {
      fileInput.files = event.dataTransfer.files;
      notifyChange();
    }
  });
  fileInput.addEventListener("change", notifyChange);
  onLanguageChange(updateFileName);
  updateFileName();
}

export function loadPreview({ fileInput, previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state } = {}) {
  const requestState = state || {};
  const requestId = (requestState.previewRequestId || 0) + 1;
  requestState.previewRequestId = requestId;

  if (!fileInput?.files?.length) {
    renderPreview(null, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state: requestState });
    return Promise.resolve(null);
  }

  const body = new FormData();
  body.set("file", fileInput.files[0]);
  if (previewTitle) previewTitle.textContent = t("preview.loadingTitle");
  if (previewCanvas) previewCanvas.textContent = t("preview.rendering");
  if (officePreviewNotice) officePreviewNotice.hidden = true;

  return fetch("/api/preview", { method: "POST", body })
    .then(async (response) => {
      const payload = await response.json();
      if (requestId !== requestState.previewRequestId) return null;
      if (!response.ok || !payload.success) {
        if (previewCanvas) previewCanvas.textContent = payload.error || t("preview.failedHttp", { status: response.status });
        return null;
      }
      renderPreview(payload.result, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state: requestState });
      return payload.result;
    })
    .catch((error) => {
      if (requestId !== requestState.previewRequestId) return null;
      if (previewCanvas) previewCanvas.textContent = error instanceof Error ? error.message : t("preview.unexpectedError");
      return null;
    });
}

export function renderPreview(preview, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state } = {}) {
  if (!previewCanvas) return;
  if (state) state.preview = preview;
  previewCanvas.replaceChildren();
  if (officePreviewNotice) officePreviewNotice.hidden = true;
  clearHighlight(previewCanvas, highlightStatus);

  if (!preview) {
    if (previewTitle) previewTitle.textContent = t("preview.title");
    const empty = document.createElement("p");
    empty.className = "preview-empty";
    empty.textContent = t("preview.empty");
    previewCanvas.append(empty);
    return;
  }

  if (previewTitle) previewTitle.textContent = t("preview.title");

  if (preview.kind === "unsupported") {
    if (officePreviewNotice) officePreviewNotice.hidden = false;
    const empty = document.createElement("p");
    empty.className = "preview-empty";
    empty.textContent = previewFallbackMessage(preview.message) || t("preview.unavailable");
    previewCanvas.append(empty);
    return;
  }

  if (preview.kind === "image") {
    const page = document.createElement("div");
    page.className = "preview-page";
    page.dataset.pageId = "1";
    page.dataset.pageIndex = "0";
    applyPageGeometry(page, preview.width, preview.height);

    const image = document.createElement("img");
    image.alt = "Document preview page 1";
    image.src = preview.data_url;
    image.addEventListener("load", () => {
      if (!page.dataset.width || !page.dataset.height) {
        applyPageGeometry(page, image.naturalWidth, image.naturalHeight);
      }
    });
    page.append(image);
    appendPageLabel(page, 1);
    previewCanvas.append(page);
    return;
  }

  if (preview.kind === "pdf") {
    (preview.pages || []).forEach((previewPage, index) => {
      const page = document.createElement("div");
      page.className = "preview-page";
      page.dataset.pageId = String(previewPage.page_id || index + 1);
      page.dataset.pageIndex = String(previewPage.page_index ?? index);
      applyPageGeometry(page, previewPage.width, previewPage.height);

      const image = document.createElement("img");
      image.alt = `Document preview page ${index + 1}`;
      image.src = previewPage.image;
      page.append(image);
      appendPageLabel(page, index + 1);
      previewCanvas.append(page);
    });
    return;
  }

  const empty = document.createElement("p");
  empty.className = "preview-empty";
  empty.textContent = t("preview.unavailable");
  previewCanvas.append(empty);
}

export function clearHighlight(previewCanvas, highlightStatus) {
  previewCanvas?.querySelectorAll(".highlight-box").forEach((box) => box.remove());
  if (highlightStatus) highlightStatus.textContent = t("preview.noHighlight");
}

export function highlightBboxes(bboxes, source = "parse", { previewCanvas, highlightStatus } = {}) {
  if (!previewCanvas) return 0;
  clearHighlight(previewCanvas, highlightStatus);
  const entries = normalizeHighlightEntries(bboxes);
  let rendered = 0;

  entries.forEach((entry) => {
    const page = findPreviewPage(previewCanvas, entry);
    if (!page) return;

    const pageWidth = Number(page.dataset.width);
    const pageHeight = Number(page.dataset.height);
    const [left, top, right, bottom] = entry.bbox.map(Number);
    const sourceWidth = Number(entry.source_width || entry.width || pageWidth);
    const sourceHeight = Number(entry.source_height || entry.height || pageHeight);
    if (
      !isPositiveFinite(pageWidth)
      || !isPositiveFinite(pageHeight)
      || !isPositiveFinite(sourceWidth)
      || !isPositiveFinite(sourceHeight)
      || ![left, top, right, bottom].every(Number.isFinite)
      || right <= left
      || bottom <= top
    ) {
      return;
    }

    const box = document.createElement("span");
    box.className = `highlight-box ${highlightSourceClasses[source] || highlightSourceClasses.parse}`;
    box.style.left = `${(left / sourceWidth) * 100}%`;
    box.style.top = `${(top / sourceHeight) * 100}%`;
    box.style.width = `${((right - left) / sourceWidth) * 100}%`;
    box.style.height = `${((bottom - top) / sourceHeight) * 100}%`;
    page.append(box);
    rendered += 1;
  });

  if (highlightStatus) {
    if (!rendered) {
      highlightStatus.textContent = t("highlight.noPositioning");
    } else if (source === "cloud") {
      highlightStatus.textContent = t("highlight.cloud", { count: rendered });
    } else if (source === "local") {
      highlightStatus.textContent = t("highlight.local", { count: rendered });
    } else {
      highlightStatus.textContent = t("highlight.parse", { count: rendered });
    }
  }

  return rendered;
}

export function withInheritedDimensions(bboxes, owner) {
  if (!owner || typeof owner !== "object") return bboxes;

  const inheritedWidth = owner.source_width || owner.width || owner.page_width;
  const inheritedHeight = owner.source_height || owner.height || owner.page_height;
  const inherit = (entry) => ({
    page_id: entry.page_id || owner.page_id || owner.page || 1,
    page_index: entry.page_index ?? owner.page_index,
    bbox: entry.bbox || entry.block_bbox || entry.box || entry,
    source_width: entry.source_width || entry.width || entry.page_width || inheritedWidth,
    source_height: entry.source_height || entry.height || entry.page_height || inheritedHeight,
  });

  if (Array.isArray(bboxes)) {
    if (bboxes.length >= 4 && bboxes.every((value) => typeof value === "number")) return [inherit(bboxes)];
    return bboxes.map((entry) => (Array.isArray(entry) ? inherit(entry) : inherit(entry || {})));
  }

  if (bboxes && typeof bboxes === "object") {
    if (bboxes.bboxes) return withInheritedDimensions(bboxes.bboxes, { ...owner, ...bboxes });
    return [inherit(bboxes)];
  }

  return [];
}

export function renderBlocksView(result, panel, { onPick } = {}) {
  if (!panel) return;
  panel.replaceChildren();
  const normalized = normalizeParsePayload(result);
  const blocks = [];
  (normalized.pages || []).forEach((page, pageIndex) => {
    const pageDimensions = pageSourceDimensions(page);
    (page.parsing_res_list || []).forEach((block) => {
      blocks.push({
        page_id: page.page_id || page.page || pageIndex + 1,
        block,
        pageDimensions,
      });
    });
  });

  if (!blocks.length) {
    setPaneText(panel, normalized.markdown || JSON.stringify(result || {}, null, 2));
    return;
  }

  blocks.forEach(({ page_id, block, pageDimensions }) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "result-block-card parse-block";
    button.textContent = block.block_content || block.block_text || block.text || block.markdown || JSON.stringify(block);
    button.addEventListener("click", () => {
      if (block.block_bbox) {
        onPick?.([{
          page_id,
          bbox: block.block_bbox,
          source_width: pageDimensions.width,
          source_height: pageDimensions.height,
        }]);
      } else {
        onPick?.(null);
      }
    });
    panel.append(button);
  });
}

export function normalizeParsePayload(result) {
  const payload = result && typeof result === "object" ? result : {};
  const standardResult = payload.result && typeof payload.result === "object" ? payload.result : null;
  if (!standardResult) {
    return {
      markdown: typeof payload.markdown === "string" ? payload.markdown : "",
      pages: Array.isArray(payload.pages) ? payload.pages : [],
      metadata: payload.metadata && typeof payload.metadata === "object" ? payload.metadata : {},
      full: payload,
    };
  }

  return {
    markdown: typeof standardResult.markdown === "string" ? standardResult.markdown : "",
    pages: normalizeStandardParsePages(standardResult.pages),
    metadata: normalizeStandardParseMetadata(payload),
    full: payload,
  };
}

function normalizeStandardParseMetadata(payload) {
  const metadata = {};
  ["code", "message", "file_type", "x_request_id", "metrics", "image_process"].forEach((key) => {
    if (payload[key] !== undefined) metadata[key] = payload[key];
  });
  return metadata;
}

function normalizeStandardParsePages(pages) {
  if (!Array.isArray(pages)) return [];
  return pages
    .filter((page) => page && typeof page === "object")
    .map((page, index) => ({
      page_id: page.page_id || index + 1,
      page_index: index,
      width: page.width,
      height: page.height,
      parsing_res_list: normalizeStandardParseBlocks(page.structured || page.content),
    }));
}

function normalizeStandardParseBlocks(blocks) {
  if (!Array.isArray(blocks)) return [];
  return blocks
    .filter((block) => block && typeof block === "object")
    .map((block, index) => ({
      block_id: block.id ?? index,
      block_label: block.type,
      block_content: block.text,
      block_bbox: quadToBbox(block.pos),
    }));
}

function quadToBbox(pos) {
  if (!Array.isArray(pos) || pos.length < 4) return undefined;
  const numbers = pos.map(Number).filter(Number.isFinite);
  if (numbers.length < 4) return undefined;
  if (numbers.length >= 8) {
    const xs = numbers.filter((_, index) => index % 2 === 0);
    const ys = numbers.filter((_, index) => index % 2 === 1);
    return [Math.min(...xs), Math.min(...ys), Math.max(...xs), Math.max(...ys)];
  }
  return numbers.slice(0, 4);
}

export function renderMarkdownView(markdown, panel) {
  setPaneText(panel, markdown || "");
}

export function renderJsonView(value, panel) {
  setPaneText(panel, JSON.stringify(value || {}, null, 2));
}

export function normalizeExtractPayload(result) {
  const payload = result && typeof result === "object" ? result : {};
  const metadata = payload.metadata && typeof payload.metadata === "object" ? payload.metadata : {};
  const resultsSource = payload.results && typeof payload.results === "object"
    ? payload.results
    : (payload.data && typeof payload.data === "object" ? payload.data : payload);
  const results = resultsSource && typeof resultsSource === "object" ? { ...resultsSource } : {};

  if (results.fields && typeof results.fields === "object") {
    const fields = results.fields;
    delete results.fields;
    Object.assign(results, fields);
  }

  ["source_width", "source_height"].forEach((key) => {
    if (results[key] !== undefined && metadata[key] === undefined) {
      metadata[key] = results[key];
      delete results[key];
    }
  });

  return {
    results,
    metadata,
    full: {
      results,
      metadata,
    },
  };
}

export function renderPlaceholder(panel, message) {
  if (!panel) return;
  panel.replaceChildren();
  const div = document.createElement("div");
  div.className = "result-placeholder";
  div.textContent = message;
  panel.append(div);
}

function renderFieldCard(name, field, source, onPick) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "result-field-card field-card";
  const title = document.createElement("strong");
  title.textContent = name;
  const value = document.createElement("span");
  value.textContent = typeof field === "object" && field !== null && "value" in field
    ? textValue(field.value)
    : textValue(field);
  button.append(title, value);
  button.addEventListener("click", () => {
    const boxes = field && typeof field === "object"
      ? withInheritedDimensions(field.bboxes || field.bbox || field.block_bbox || field.box, field)
      : null;
    onPick?.(boxes && boxes.length ? boxes : null, source);
  });
  return button;
}

export function renderExtractCards(result, panel, { source = "cloud", onPick } = {}) {
  if (!panel) return;
  panel.replaceChildren();
  const data = normalizeExtractPayload(result).results;
  const tableBboxes = data._table_bboxes || {};
  let rendered = 0;

  Object.entries(data).forEach(([key, value]) => {
    if (key === "tables" || key === "_table_bboxes") return;
    panel.append(renderFieldCard(key, value, source, onPick));
    rendered += 1;
  });

  Object.entries(data.tables || {}).forEach(([tableName, rows]) => {
    const section = document.createElement("section");
    section.className = "result-table-card table-card";
    const title = actionButton(tableName, () => {
      if (tableBboxes[tableName]) {
        onPick?.(withInheritedDimensions(
          tableBboxes[tableName].bboxes || tableBboxes[tableName].bbox || tableBboxes[tableName],
          tableBboxes[tableName],
        ), source);
      } else {
        onPick?.(null, source);
      }
    });
    const pre = document.createElement("pre");
    pre.textContent = JSON.stringify(rows, null, 2);
    section.append(title, pre);
    panel.append(section);
    rendered += 1;
  });

  if (!rendered) setPaneText(panel, JSON.stringify(data, null, 2));
}

export function bindResultTabs(tabList, onChange) {
  if (!tabList) return;
  const buttons = Array.from(tabList.querySelectorAll("[data-result-tab]"));
  const panels = buttons
    .map((button) => document.querySelector(`[data-result-panel="${button.dataset.resultTab}"]`))
    .filter(Boolean);

  const setActiveTab = (tabName) => {
    buttons.forEach((button) => {
      const isActive = button.dataset.resultTab === tabName;
      button.classList.toggle("is-active", isActive);
      button.setAttribute("aria-selected", String(isActive));
      button.tabIndex = isActive ? 0 : -1;
    });
    panels.forEach((panel) => {
      panel.hidden = panel.dataset.resultPanel !== tabName;
    });
    onChange?.(tabName);
  };

  const handleKeydown = (event) => {
    const currentIndex = buttons.indexOf(event.currentTarget);
    let nextIndex;
    if (event.key === "ArrowRight" || event.key === "ArrowDown") {
      nextIndex = (currentIndex + 1) % buttons.length;
    } else if (event.key === "ArrowLeft" || event.key === "ArrowUp") {
      nextIndex = (currentIndex - 1 + buttons.length) % buttons.length;
    } else if (event.key === "Home") {
      nextIndex = 0;
    } else if (event.key === "End") {
      nextIndex = buttons.length - 1;
    } else {
      return;
    }
    event.preventDefault();
    const nextButton = buttons[nextIndex];
    setActiveTab(nextButton.dataset.resultTab);
    nextButton.focus();
  };

  buttons.forEach((button) => {
    button.setAttribute("role", "tab");
    button.addEventListener("click", () => setActiveTab(button.dataset.resultTab));
    button.addEventListener("keydown", handleKeydown);
  });
  panels.forEach((panel) => panel.setAttribute("role", "tabpanel"));
  if (buttons.length) setActiveTab(buttons[0].dataset.resultTab);
}

export function downloadText(text, filename = "docslight-result.txt") {
  if (!text) return;
  const blob = new Blob([text], { type: "text/plain;charset=utf-8" });
  downloadBlob(blob, filename);
}

export function downloadBlob(blob, filename = "docslight-result.bin") {
  if (!blob) return;
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

export function postForm(endpoint, body) {
  return fetch(endpoint, { method: "POST", body }).then(async (response) => {
    const contentType = response.headers.get("content-type") || "";
    if (response.ok && contentType.includes("application/zip")) {
      const disposition = response.headers.get("content-disposition") || "";
      const filenameMatch = disposition.match(/filename="?([^";]+)"?/i);
      return {
        success: true,
        blob: await response.blob(),
        filename: filenameMatch?.[1] || "docslight-parse.zip",
      };
    }

    let payload = null;
    try {
      payload = await response.json();
    } catch {
      payload = {};
    }
    if (!response.ok || payload.success === false) {
      throw new Error(payload.error || `Request failed with HTTP ${response.status}`);
    }
    return payload;
  });
}

export function setFormError(formError, message) {
  if (!formError) return;
  formError.textContent = message || "";
  formError.hidden = !message;
}
