import {
  bindDropzone,
  bindResultTabs,
  downloadText,
  highlightBboxes,
  initHealthBadge,
  loadPreview,
  normalizeExtractPayload,
  postForm,
  renderExtractCards,
  renderJsonView,
  renderPlaceholder,
  renderPreview,
  setFormError,
} from "./common.js";
import { initI18n, onLanguageChange, t } from "./i18n.js";

const extractForm = document.querySelector("#extractForm");
const modeSelect = document.querySelector("#modeSelect");
const cloudConfig = document.querySelector("#cloudConfig");
const cloudExtractMode = document.querySelector("#cloudExtractMode");
const groundingToggle = document.querySelector("#groundingToggle");
const localLlmBlock = document.querySelector("#localLlmBlock");
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
const fieldTemplateName = document.querySelector("#fieldTemplateName");
const fieldsRows = document.querySelector("#fieldsRows");
const addFieldButton = document.querySelector("#addFieldButton");
const addTableButton = document.querySelector("#addTableButton");
const fieldsPanel = document.querySelector("#fieldsPanel");
const jsonPanel = document.querySelector("#jsonPanel");
const extractResultTabs = document.querySelector("#extractResultTabs");
const healthStatus = document.querySelector("#healthStatus");

const state = {
  fieldRows: [],
  currentTab: "fields",
  hasResult: false,
  latestJson: "",
  resultSource: "cloud",
  previewRequestId: 0,
};

function syncRuntimeControls() {
  const isCloud = modeSelect?.value !== "local";
  if (cloudConfig) cloudConfig.hidden = !isCloud;
  if (localLlmBlock) localLlmBlock.hidden = isCloud;
  if (groundingToggle) groundingToggle.hidden = !isCloud || cloudExtractMode?.value !== "integrate";
}

function normalizedMeta(row) {
  const prompt = row.prompt?.trim() || null;
  const mapping = row.mapping?.trim() || null;
  return { prompt, mapping };
}

function nextTableNumber() {
  const numbers = state.fieldRows
    .filter((row) => row.type === "table")
    .map((row) => Number(row.name.replace(/^Table_/, "")))
    .filter(Number.isFinite);
  return numbers.length ? Math.max(...numbers) + 1 : 1;
}

function inputControl(value, placeholder, onInput) {
  const input = document.createElement("input");
  input.type = "text";
  input.value = value || "";
  input.placeholder = placeholder;
  input.addEventListener("input", () => onInput(input.value));
  return input;
}

function labeledCell(labelText, control) {
  const label = document.createElement("label");
  label.className = "field-label";
  const span = document.createElement("span");
  span.textContent = labelText;
  label.append(span, control);
  return label;
}

function actionButton(text, onClick) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "ghost-button";
  button.textContent = text;
  button.addEventListener("click", onClick);
  return button;
}

const PROMPT_ICON_SVG = '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M11.5 1.5l3 3L5 14H2v-3L11.5 1.5z"/></svg>';

function closeAllPopovers() {
  document.querySelectorAll(".prompt-popover-backdrop").forEach((el) => el.remove());
  document.querySelectorAll(".prompt-popover").forEach((el) => el.remove());
}

function nameWithPromptCell(row, labelText, namePlaceholder, updateName, updatePrompt) {
  const wrapper = document.createElement("div");
  wrapper.className = "prompt-cell";

  const nameInput = inputControl(row.name, namePlaceholder, updateName);
  const label = document.createElement("label");
  label.className = "field-label";
  const labelSpan = document.createElement("span");
  labelSpan.textContent = labelText;
  label.append(labelSpan, nameInput);

  const trigger = document.createElement("button");
  trigger.type = "button";
  trigger.className = "prompt-trigger";
  trigger.setAttribute("aria-label", t("fields.prompt"));
  trigger.innerHTML = PROMPT_ICON_SVG;
  if (row.prompt?.trim()) trigger.dataset.active = "true";

  trigger.addEventListener("click", (event) => {
    event.stopPropagation();
    if (document.body.contains(document.querySelector(".prompt-popover"))) {
      closeAllPopovers();
      return;
    }
    closeAllPopovers();

    const backdrop = document.createElement("div");
    backdrop.className = "prompt-popover-backdrop";
    backdrop.addEventListener("click", closeAllPopovers);

    const popover = document.createElement("div");
    popover.className = "prompt-popover";
    popover.addEventListener("click", (e) => e.stopPropagation());
    popover.addEventListener("mousedown", (e) => e.stopPropagation());

    const header = document.createElement("div");
    header.className = "prompt-popover-header";
    header.textContent = t("fields.prompt");
    popover.append(header);

    const textarea = document.createElement("textarea");
    textarea.value = row.prompt || "";
    textarea.placeholder = t("fields.promptPlaceholder");
    textarea.addEventListener("input", () => {
      row.prompt = textarea.value;
      trigger.dataset.active = textarea.value.trim() ? "true" : "false";
    });
    popover.append(textarea);

    const rect = trigger.getBoundingClientRect();
    popover.style.top = `${rect.bottom + 8}px`;
    popover.style.left = `${Math.max(8, rect.right - 260)}px`;

    document.body.append(backdrop, popover);
    textarea.focus();
  });

  wrapper.append(label, trigger);
  return wrapper;
}

function renderTextRow(row) {
  const card = document.createElement("article");
  card.className = "field-row-card";
  const title = document.createElement("strong");
  title.textContent = t("fields.field");
  card.append(
    title,
    nameWithPromptCell(row, t("fields.name"), t("fields.namePlaceholder"), (value) => { row.name = value; }, (value) => { row.prompt = value; }),
    labeledCell(t("fields.mapping"), inputControl(row.mapping, t("fields.mappingPlaceholder"), (value) => { row.mapping = value; })),
    actionButton(t("fields.remove"), () => {
      state.fieldRows = state.fieldRows.filter((candidate) => candidate !== row);
      renderFieldsBuilder();
    }),
  );
  return card;
}

function renderColumnRow(table, column) {
  const row = document.createElement("div");
  row.className = "field-row-card";
  row.append(
    nameWithPromptCell(column, t("fields.column"), t("fields.columnNamePlaceholder"), (value) => { column.name = value; }, (value) => { column.prompt = value; }),
    labeledCell(t("fields.mapping"), inputControl(column.mapping, t("fields.mappingPlaceholder"), (value) => { column.mapping = value; })),
    actionButton(t("fields.removeColumn"), () => {
      table.columns = table.columns.filter((candidate) => candidate !== column);
      renderFieldsBuilder();
    }),
  );
  return row;
}

function renderTableRow(row) {
  const card = document.createElement("article");
  card.className = "table-field-card";
  const title = document.createElement("strong");
  title.textContent = t("fields.table");
  const columns = document.createElement("div");
  columns.className = "table-columns";
  row.columns.forEach((column) => columns.append(renderColumnRow(row, column)));
  card.append(
    title,
    labeledCell(t("fields.tableName"), inputControl(row.name, t("fields.tableNamePlaceholder"), (value) => { row.name = value; })),
    columns,
    actionButton(t("fields.addColumn"), () => {
      row.columns.push({ name: t("fields.defaultColumnName"), prompt: "", mapping: "" });
      renderFieldsBuilder();
    }),
    actionButton(t("fields.removeTable"), () => {
      state.fieldRows = state.fieldRows.filter((candidate) => candidate !== row);
      renderFieldsBuilder();
    }),
  );
  return card;
}

function renderFieldsBuilder() {
  if (!fieldsRows) return;
  fieldsRows.replaceChildren();
  state.fieldRows.forEach((row) => {
    fieldsRows.append(row.type === "table" ? renderTableRow(row) : renderTextRow(row));
  });
}

function addTextField(name = t("fields.initialFieldName")) {
  state.fieldRows.push({ type: "field", name, prompt: "", mapping: "" });
  renderFieldsBuilder();
}

function addTableField(name = `Table_${nextTableNumber()}`, columnName = t("fields.initialColumnName")) {
  state.fieldRows.push({
    type: "table",
    name,
    columns: [{ name: columnName, prompt: "", mapping: "" }],
  });
  renderFieldsBuilder();
}

function buildFieldsPayload() {
  const payload = {
    keys: {},
    tableHeaders: {},
    name: fieldTemplateName?.value.trim() || "Document",
  };

  state.fieldRows.forEach((row) => {
    if (row.type === "field") {
      const name = row.name.trim();
      if (name) payload.keys[name] = normalizedMeta(row);
      return;
    }

    const tableName = row.name.trim();
    const columns = {};
    row.columns.forEach((column) => {
      const columnName = column.name.trim();
      if (columnName) columns[columnName] = normalizedMeta(column);
    });
    if (tableName && Object.keys(columns).length) payload.tableHeaders[tableName] = columns;
  });

  if (!Object.keys(payload.keys).length) delete payload.keys;
  if (!Object.keys(payload.tableHeaders).length) delete payload.tableHeaders;
  return payload;
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
  const placeholder = t("extract.placeholder");
  renderPlaceholder(fieldsPanel, placeholder);
  renderPlaceholder(jsonPanel, placeholder);
  state.hasResult = false;
  state.latestJson = "";
  if (metadataPreview) metadataPreview.textContent = t("extract.metadataEmpty");
  if (downloadButton) downloadButton.disabled = true;
}

function renderExtractResult(result) {
  state.hasResult = true;
  const normalized = normalizeExtractPayload(result);
  const displayData = normalized.results;
  state.latestJson = JSON.stringify(normalized.full, null, 2);
  state.resultSource = modeSelect?.value === "local" ? "local" : "cloud";

  renderExtractCards(displayData, fieldsPanel, {
    source: state.resultSource,
    onPick: (boxes, source) => {
      if (!boxes) {
        highlightBboxes(null, source || state.resultSource, { previewCanvas, highlightStatus });
        return;
      }
      highlightBboxes(boxes, source || state.resultSource, { previewCanvas, highlightStatus });
    },
  });
  renderJsonView(normalized.full, jsonPanel);

  if (metadataPreview) {
    metadataPreview.textContent = JSON.stringify(normalized.metadata, null, 2);
  }
  if (downloadButton) downloadButton.disabled = false;
}

function validateForm(fields) {
  if (!fileInput?.files?.length) return t("error.selectDocument");

  const isCloud = modeSelect?.value !== "local";
  const apiKey = extractForm?.querySelector('[name="api_key"]')?.value?.trim();
  if (isCloud && !apiKey) return t("error.cloudApiKeyRequired");

  const localProvider = extractForm?.querySelector('[name="local_llm_provider"]')?.value?.trim();
  const localModel = extractForm?.querySelector('[name="local_llm_model"]')?.value?.trim();
  const localBaseUrl = extractForm?.querySelector('[name="local_llm_base_url"]')?.value?.trim();
  if (!isCloud && (!localProvider || !localModel || !localBaseUrl)) {
    return t("error.localLlmRequired");
  }

  if (!fields.keys && !fields.tableHeaders) return t("error.fieldsRequired");
  return "";
}

function refreshLocalizedDynamicCopy() {
  renderFieldsBuilder();
  if (!state.hasResult) renderEmptyResult();
  renderPreview(state.preview || null, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state });
}

modeSelect?.addEventListener("change", syncRuntimeControls);
cloudExtractMode?.addEventListener("change", syncRuntimeControls);
addFieldButton?.addEventListener("click", () => addTextField(t("fields.defaultFieldName")));
addTableButton?.addEventListener("click", () => addTableField());

extractForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  setFormError(formError, "");

  const fields = buildFieldsPayload();
  const validationError = validateForm(fields);
  if (validationError) {
    setFormError(formError, validationError);
    return;
  }

  const body = new FormData(extractForm);
  body.set("file", fileInput.files[0]);
  body.set("fields", JSON.stringify(fields));
  if (modeSelect?.value !== "local") {
    body.set("cloud_extract_mode", cloudExtractMode?.value || "vlm");
    if (cloudExtractMode?.value !== "integrate") body.delete("enable_grounding");
  }
  if (submitButton) submitButton.disabled = true;

  try {
    const payload = await postForm("/api/extract", body);
    renderExtractResult(payload.result || payload);
  } catch (error) {
    setFormError(formError, error instanceof Error ? error.message : t("extract.failed"));
  } finally {
    if (submitButton) submitButton.disabled = false;
  }
});

downloadButton?.addEventListener("click", () => {
  downloadText(state.latestJson, "docslight-extract.json");
});

initI18n();
initHealthBadge(healthStatus);
bindDropzone({ dropZone, fileInput, fileName, onFileChange: refreshPreview });
bindResultTabs(extractResultTabs, (tab) => {
  state.currentTab = tab;
});
onLanguageChange(refreshLocalizedDynamicCopy);
syncRuntimeControls();
renderPreview(null, { previewTitle, previewCanvas, officePreviewNotice, highlightStatus, state });
renderEmptyResult();
addTextField(t("fields.initialFieldName"));
addTableField("Table_1", t("fields.initialColumnName"));
