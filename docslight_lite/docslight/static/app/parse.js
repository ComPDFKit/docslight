import {
  bindDropzone,
  bindResultTabs,
  downloadBlob,
  downloadText,
  highlightBboxes,
  initHealthBadge,
  loadPreview,
  postForm,
  renderBlocksView,
  renderJsonView,
  renderMarkdownView,
  renderPlaceholder,
  renderPreview,
  normalizeParsePayload,
  setFormError,
} from "./common.js";
import { initI18n, onLanguageChange, t } from "./i18n.js";

const parseForm = document.querySelector("#parseForm");
const modeSelect = document.querySelector("#modeSelect");
const cloudConfig = document.querySelector("#cloudConfig");
const localParseNote = document.querySelector("#localParseNote");
const fileInput = document.querySelector("#fileInput");
const dropZone = document.querySelector("#dropZone");
const fileName = document.querySelector("#fileName");
const previewTitle = document.querySelector("#previewTitle");
const previewCanvas = document.querySelector("#previewCanvas");
const officePreviewNotice = document.querySelector("#officePreviewNotice");
const highlightStatus = document.querySelector("#highlightStatus");
const formError = document.querySelector("#formError");
const submitButton = document.querySelector("#submitButton");
const downloadButton = document.querySelector("#downloadButton");
const metadataPreview = document.querySelector("#metadataPreview");
const blocksPanel = document.querySelector("#blocksPanel");
const markdownPanel = document.querySelector("#markdownPanel");
const jsonPanel = document.querySelector("#jsonPanel");
const parseResultTabs = document.querySelector("#parseResultTabs");
const healthStatus = document.querySelector("#healthStatus");

const state = {
  currentTab: "blocks",
  hasResult: false,
  latestMarkdown: "",
  latestJson: "",
  previewRequestId: 0,
};

function syncRuntimeControls() {
  const isCloud = modeSelect?.value !== "local";
  if (cloudConfig) cloudConfig.hidden = !isCloud;
  if (localParseNote) localParseNote.hidden = isCloud;
}

function refreshPreview() {
  return loadPreview({
    fileInput,
    previewTitle,
    previewCanvas,
    officePreviewNotice,
    highlightStatus,
    state,
  });
}

function renderEmptyResult() {
  const placeholder = t("parse.placeholder");
  renderPlaceholder(blocksPanel, placeholder);
  renderPlaceholder(markdownPanel, placeholder);
  renderPlaceholder(jsonPanel, placeholder);
  state.hasResult = false;
  if (metadataPreview) metadataPreview.textContent = t("parse.metadataEmpty");
  if (downloadButton) downloadButton.disabled = true;
}

function renderParseResult(result) {
  state.hasResult = true;
  const normalized = normalizeParsePayload(result);
  state.latestMarkdown = normalized.markdown || "";
  state.latestJson = JSON.stringify(result || {}, null, 2);

  renderBlocksView(result, blocksPanel, {
    onPick: (boxes) => {
      if (!boxes) {
        highlightBboxes(null, "parse", { previewCanvas, highlightStatus });
        return;
      }
      highlightBboxes(boxes, "parse", { previewCanvas, highlightStatus });
    },
  });
  renderMarkdownView(state.latestMarkdown, markdownPanel);
  renderJsonView(result, jsonPanel);

  if (metadataPreview) {
    metadataPreview.textContent = JSON.stringify(normalized.metadata || {}, null, 2);
  }
  if (downloadButton) downloadButton.disabled = false;
}

function validateForm() {
  if (!fileInput?.files?.length) return t("error.selectDocument");
  const isCloud = modeSelect?.value !== "local";
  const apiKey = parseForm?.querySelector('[name="api_key"]')?.value?.trim();
  if (isCloud && !apiKey) return t("error.cloudApiKeyRequired");
  return "";
}

function refreshLocalizedDynamicCopy() {
  if (!state.hasResult) renderEmptyResult();
  renderPreview(state.preview || null, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state });
  setFormError(formError, formError?.hidden ? "" : formError?.textContent || "");
}

modeSelect?.addEventListener("change", syncRuntimeControls);

parseForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  setFormError(formError, "");

  const validationError = validateForm();
  if (validationError) {
    setFormError(formError, validationError);
    return;
  }

  const body = new FormData(parseForm);
  body.set("file", fileInput.files[0]);
  if (submitButton) submitButton.disabled = true;

  try {
    const payload = await postForm("/api/parse", body);
    if (payload.blob) {
      downloadBlob(payload.blob, payload.filename || "docslight-parse.zip");
      return;
    }
    renderParseResult(payload.result || payload);
  } catch (error) {
    setFormError(formError, error instanceof Error ? error.message : t("parse.failed"));
  } finally {
    if (submitButton) submitButton.disabled = false;
  }
});

downloadButton?.addEventListener("click", () => {
  if (state.currentTab === "json") {
    downloadText(state.latestJson, "docslight-parse.json");
    return;
  }
  downloadText(state.latestMarkdown, "docslight-parse.md");
});

initI18n();
initHealthBadge(healthStatus);
bindDropzone({ dropZone, fileInput, fileName, onFileChange: refreshPreview });
bindResultTabs(parseResultTabs, (tab) => {
  state.currentTab = tab;
});
onLanguageChange(refreshLocalizedDynamicCopy);
syncRuntimeControls();
renderPreview(null, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state });
renderEmptyResult();
