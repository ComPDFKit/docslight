const storageKey = "docslight.language";

export const supportedLanguages = [
  { code: "en", label: "English", htmlLang: "en" },
  { code: "zh-CN", label: "简体中文", htmlLang: "zh-CN" },
  { code: "zh-TW", label: "繁體中文", htmlLang: "zh-TW" },
];

const translations = {
  en: {
    "app.title": "DocSlight Workbench",
    "nav.parse": "Parse",
    "nav.extract": "Extract",
    "language.label": "Language",
    "health.checking": "Checking service...",
    "health.status": "{service}: {status}",
    "health.unavailable": "Local service unavailable",
    "mode.label": "Processing mode",
    "mode.cloud": "Cloud",
    "mode.local": "Local",
    "cloud.baseUrl": "Cloud Base URL",
    "cloud.apiKey": "API key",
    "cloud.apiKeyPlaceholder": "Cloud API key",
    "cloud.extractMode": "Cloud model",
    "cloud.enableGrounding": "Enable grounding",
    "drop.choose": "Choose document",
    "drop.formats": "PDF, image, DOCX, PPTX, XLSX",
    "drop.none": "No file selected",
    "common.download": "Download",
    "common.metadataPreview": "Metadata preview",
    "preview.title": "Document preview",
    "preview.specimen": "Document specimen",
    "preview.noHighlight": "No highlight selected",
    "preview.empty": "Select a file to load a preview.",
    "preview.officeUnsupported": "Office files can be processed, but preview and positioning highlight are not supported in this version.",
    "preview.loadingTitle": "Loading preview",
    "preview.rendering": "Rendering document preview...",
    "preview.failedHttp": "Preview failed with HTTP {status}",
    "preview.unexpectedError": "Unexpected preview error",
    "preview.unavailable": "Preview is not available for this document type.",
    "preview.pageLabel": "Page {page}",
    "highlight.noPositioning": "No positioning data available for this selection.",
    "highlight.cloud": "Precise cloud positioning highlighted {count} region(s).",
    "highlight.local": "Coarse local positioning highlighted {count} region(s).",
    "highlight.parse": "Parse block positioning highlighted {count} region(s).",
    "parse.pageTitle": "Parse | DocSlight Workbench",
    "parse.workbench": "Parse workbench",
    "parse.eyebrow": "Parse setup",
    "parse.title": "Parse documents",
    "parse.description": "Convert documents into layout blocks, Markdown, and raw JSON.",
    "parse.localNote": "Local parsing uses the configured local runtime.",
    "parse.run": "Run parse",
    "parse.resultsTitle": "Parse results",
    "parse.tabs.blocks": "Blocks",
    "parse.tabs.markdown": "Markdown",
    "parse.tabs.json": "JSON",
    "parse.placeholder": "Parse results will appear here after you run a parse.",
    "parse.metadataEmpty": "No parse metadata yet.",
    "parse.failed": "Parse failed.",
    "extract.pageTitle": "Extract | DocSlight Workbench",
    "extract.workbench": "Extract workbench",
    "extract.eyebrow": "Extract setup",
    "extract.title": "Extract fields",
    "extract.description": "Define fields and tables, then extract structured values from a document.",
    "extract.localLlm": "Local LLM",
    "extract.provider": "Provider",
    "extract.model": "Model",
    "extract.baseUrl": "Base URL",
    "extract.apiKey": "API key",
    "extract.optionalPlaceholder": "optional",
    "extract.run": "Run extract",
    "extract.resultsTitle": "Extract results",
    "extract.tabs.fields": "Fields",
    "extract.tabs.json": "JSON",
    "extract.placeholder": "Extract results will appear here after you run an extract.",
    "extract.metadataEmpty": "No extract metadata yet.",
    "extract.failed": "Extract failed.",
    "fields.title": "Fields",
    "fields.templateName": "Template name",
    "fields.templatePlaceholder": "Invoice",
    "fields.addField": "Add field",
    "fields.addTable": "Add table",
    "fields.field": "Field",
    "fields.name": "Name",
    "fields.prompt": "Prompt",
    "fields.mapping": "Mapping",
    "fields.remove": "Remove",
    "fields.column": "Column",
    "fields.removeColumn": "Remove column",
    "fields.table": "Table",
    "fields.tableName": "Table name",
    "fields.addColumn": "Add column",
    "fields.removeTable": "Remove table",
    "fields.namePlaceholder": "Title",
    "fields.promptPlaceholder": "Optional extraction prompt",
    "fields.mappingPlaceholder": "Optional mapping key",
    "fields.tableNamePlaceholder": "Table_1",
    "fields.columnNamePlaceholder": "Unit Price",
    "fields.defaultFieldName": "Field",
    "fields.initialFieldName": "Title",
    "fields.defaultColumnName": "Column",
    "fields.initialColumnName": "Unit Price",
    "error.selectDocument": "Please select a document first.",
    "error.cloudApiKeyRequired": "Cloud mode requires an API key.",
    "error.localLlmRequired": "Please complete Local LLM provider, model, and base URL.",
    "error.fieldsRequired": "Please add at least one field or table column.",
  },
  "zh-CN": {
    "app.title": "DocSlight 工作台",
    "nav.parse": "解析",
    "nav.extract": "抽取",
    "language.label": "语言",
    "health.checking": "正在检查服务...",
    "health.status": "{service}: {status}",
    "health.unavailable": "本地服务不可用",
    "mode.label": "处理模式",
    "mode.cloud": "云端",
    "mode.local": "本地",
    "cloud.baseUrl": "云端 Base URL",
    "cloud.apiKey": "API 密钥",
    "cloud.apiKeyPlaceholder": "云端 API 密钥",
    "cloud.extractMode": "云端模型",
    "cloud.enableGrounding": "启用 grounding",
    "drop.choose": "选择文档",
    "drop.formats": "PDF、图片、DOCX、PPTX、XLSX",
    "drop.none": "未选择文件",
    "common.download": "下载",
    "common.metadataPreview": "元数据预览",
    "preview.title": "文档预览",
    "preview.specimen": "文档样本",
    "preview.noHighlight": "未选择高亮区域",
    "preview.empty": "选择文件后加载预览。",
    "preview.officeUnsupported": "Office 文件可以处理，但当前版本不支持预览和定位高亮。",
    "preview.loadingTitle": "正在加载预览",
    "preview.rendering": "正在渲染文档预览...",
    "preview.failedHttp": "预览失败，HTTP {status}",
    "preview.unexpectedError": "预览出现意外错误",
    "preview.unavailable": "此文档类型暂不支持预览。",
    "preview.pageLabel": "第 {page} 页",
    "highlight.noPositioning": "当前选择没有可用的定位数据。",
    "highlight.cloud": "已高亮 {count} 个云端精准定位区域。",
    "highlight.local": "已高亮 {count} 个本地粗略定位区域。",
    "highlight.parse": "已高亮 {count} 个解析块定位区域。",
    "parse.pageTitle": "解析 | DocSlight 工作台",
    "parse.workbench": "解析工作台",
    "parse.eyebrow": "解析设置",
    "parse.title": "解析文档",
    "parse.description": "将文档转换为版面块、Markdown 和原始 JSON。",
    "parse.localNote": "本地解析会使用已配置的本地运行环境。",
    "parse.run": "开始解析",
    "parse.resultsTitle": "解析结果",
    "parse.tabs.blocks": "区块",
    "parse.tabs.markdown": "Markdown",
    "parse.tabs.json": "JSON",
    "parse.placeholder": "运行解析后，结果会显示在这里。",
    "parse.metadataEmpty": "暂无解析元数据。",
    "parse.failed": "解析失败。",
    "extract.pageTitle": "抽取 | DocSlight 工作台",
    "extract.workbench": "抽取工作台",
    "extract.eyebrow": "抽取设置",
    "extract.title": "抽取字段",
    "extract.description": "定义字段和表格，并从文档中抽取结构化值。",
    "extract.localLlm": "本地 LLM",
    "extract.provider": "提供商",
    "extract.model": "模型",
    "extract.baseUrl": "Base URL",
    "extract.apiKey": "API 密钥",
    "extract.optionalPlaceholder": "可选",
    "extract.run": "开始抽取",
    "extract.resultsTitle": "抽取结果",
    "extract.tabs.fields": "字段",
    "extract.tabs.json": "JSON",
    "extract.placeholder": "运行抽取后，结果会显示在这里。",
    "extract.metadataEmpty": "暂无抽取元数据。",
    "extract.failed": "抽取失败。",
    "fields.title": "字段",
    "fields.templateName": "模板名称",
    "fields.templatePlaceholder": "发票",
    "fields.addField": "添加字段",
    "fields.addTable": "添加表格",
    "fields.field": "字段",
    "fields.name": "名称",
    "fields.prompt": "提示词",
    "fields.mapping": "映射",
    "fields.remove": "移除",
    "fields.column": "列",
    "fields.removeColumn": "移除列",
    "fields.table": "表格",
    "fields.tableName": "表格名称",
    "fields.addColumn": "添加列",
    "fields.removeTable": "移除表格",
    "fields.namePlaceholder": "标题",
    "fields.promptPlaceholder": "可选抽取提示词",
    "fields.mappingPlaceholder": "可选映射键",
    "fields.tableNamePlaceholder": "表格_1",
    "fields.columnNamePlaceholder": "单价",
    "fields.defaultFieldName": "字段",
    "fields.initialFieldName": "标题",
    "fields.defaultColumnName": "列",
    "fields.initialColumnName": "单价",
    "error.selectDocument": "请先选择一个文档。",
    "error.cloudApiKeyRequired": "云端模式需要 API 密钥。",
    "error.localLlmRequired": "请完整填写本地 LLM 提供商、模型和 Base URL。",
    "error.fieldsRequired": "请至少添加一个字段或表格列。",
  },
  "zh-TW": {
    "app.title": "DocSlight 工作台",
    "nav.parse": "解析",
    "nav.extract": "擷取",
    "language.label": "語言",
    "health.checking": "正在檢查服務...",
    "health.status": "{service}: {status}",
    "health.unavailable": "本機服務無法使用",
    "mode.label": "處理模式",
    "mode.cloud": "雲端",
    "mode.local": "本機",
    "cloud.baseUrl": "雲端 Base URL",
    "cloud.apiKey": "API 金鑰",
    "cloud.apiKeyPlaceholder": "雲端 API 金鑰",
    "cloud.extractMode": "雲端模型",
    "cloud.enableGrounding": "啟用 grounding",
    "drop.choose": "選擇文件",
    "drop.formats": "PDF、圖片、DOCX、PPTX、XLSX",
    "drop.none": "尚未選擇檔案",
    "common.download": "下載",
    "common.metadataPreview": "中繼資料預覽",
    "preview.title": "文件預覽",
    "preview.specimen": "文件樣本",
    "preview.noHighlight": "尚未選擇高亮區域",
    "preview.empty": "選擇檔案後載入預覽。",
    "preview.officeUnsupported": "Office 檔案可以處理，但目前版本不支援預覽和定位高亮。",
    "preview.loadingTitle": "正在載入預覽",
    "preview.rendering": "正在渲染文件預覽...",
    "preview.failedHttp": "預覽失敗，HTTP {status}",
    "preview.unexpectedError": "預覽發生未預期錯誤",
    "preview.unavailable": "此文件類型暫不支援預覽。",
    "preview.pageLabel": "第 {page} 頁",
    "highlight.noPositioning": "目前選擇沒有可用的定位資料。",
    "highlight.cloud": "已高亮 {count} 個雲端精準定位區域。",
    "highlight.local": "已高亮 {count} 個本機粗略定位區域。",
    "highlight.parse": "已高亮 {count} 個解析區塊定位區域。",
    "parse.pageTitle": "解析 | DocSlight 工作台",
    "parse.workbench": "解析工作台",
    "parse.eyebrow": "解析設定",
    "parse.title": "解析文件",
    "parse.description": "將文件轉換為版面區塊、Markdown 和原始 JSON。",
    "parse.localNote": "本機解析會使用已設定的本機執行環境。",
    "parse.run": "開始解析",
    "parse.resultsTitle": "解析結果",
    "parse.tabs.blocks": "區塊",
    "parse.tabs.markdown": "Markdown",
    "parse.tabs.json": "JSON",
    "parse.placeholder": "執行解析後，結果會顯示在這裡。",
    "parse.metadataEmpty": "暫無解析中繼資料。",
    "parse.failed": "解析失敗。",
    "extract.pageTitle": "擷取 | DocSlight 工作台",
    "extract.workbench": "擷取工作台",
    "extract.eyebrow": "擷取設定",
    "extract.title": "擷取欄位",
    "extract.description": "定義欄位和表格，並從文件中擷取結構化值。",
    "extract.localLlm": "本機 LLM",
    "extract.provider": "提供商",
    "extract.model": "模型",
    "extract.baseUrl": "Base URL",
    "extract.apiKey": "API 金鑰",
    "extract.optionalPlaceholder": "選填",
    "extract.run": "開始擷取",
    "extract.resultsTitle": "擷取結果",
    "extract.tabs.fields": "欄位",
    "extract.tabs.json": "JSON",
    "extract.placeholder": "執行擷取後，結果會顯示在這裡。",
    "extract.metadataEmpty": "暫無擷取中繼資料。",
    "extract.failed": "擷取失敗。",
    "fields.title": "欄位",
    "fields.templateName": "範本名稱",
    "fields.templatePlaceholder": "發票",
    "fields.addField": "新增欄位",
    "fields.addTable": "新增表格",
    "fields.field": "欄位",
    "fields.name": "名稱",
    "fields.prompt": "提示詞",
    "fields.mapping": "映射",
    "fields.remove": "移除",
    "fields.column": "欄",
    "fields.removeColumn": "移除欄",
    "fields.table": "表格",
    "fields.tableName": "表格名稱",
    "fields.addColumn": "新增欄",
    "fields.removeTable": "移除表格",
    "fields.namePlaceholder": "標題",
    "fields.promptPlaceholder": "選填擷取提示詞",
    "fields.mappingPlaceholder": "選填映射鍵",
    "fields.tableNamePlaceholder": "表格_1",
    "fields.columnNamePlaceholder": "單價",
    "fields.defaultFieldName": "欄位",
    "fields.initialFieldName": "標題",
    "fields.defaultColumnName": "欄",
    "fields.initialColumnName": "單價",
    "error.selectDocument": "請先選擇一份文件。",
    "error.cloudApiKeyRequired": "雲端模式需要 API 金鑰。",
    "error.localLlmRequired": "請完整填寫本機 LLM 提供商、模型和 Base URL。",
    "error.fieldsRequired": "請至少新增一個欄位或表格欄。",
  },
};

let currentLanguage = "en";

function normalizeLanguage(language) {
  if (!language) return null;
  const normalized = String(language).replace("_", "-");
  if (translations[normalized]) return normalized;
  const lower = normalized.toLowerCase();
  if (lower === "zh-cn" || lower === "zh-hans") return "zh-CN";
  if (lower === "zh-tw" || lower === "zh-hk" || lower === "zh-mo" || lower === "zh-hant") return "zh-TW";
  if (lower.startsWith("zh")) return "zh-CN";
  if (lower.startsWith("en")) return "en";
  return null;
}

function browserLanguage() {
  const candidates = [navigator.language, ...(navigator.languages || [])];
  return candidates.map(normalizeLanguage).find(Boolean) || "en";
}

function storedLanguage() {
  try {
    return normalizeLanguage(localStorage.getItem(storageKey));
  } catch {
    return null;
  }
}

function saveLanguage(language) {
  try {
    localStorage.setItem(storageKey, language);
  } catch {
    // Ignore storage errors in private or restricted browser contexts.
  }
}

function interpolate(message, values) {
  return message.replace(/\{(\w+)\}/g, (match, key) => String(values?.[key] ?? match));
}

function setLanguage(language, { persist = false } = {}) {
  currentLanguage = normalizeLanguage(language) || "en";
  const metadata = supportedLanguages.find((entry) => entry.code === currentLanguage) || supportedLanguages[0];
  document.documentElement.lang = metadata.htmlLang;
  if (persist) saveLanguage(currentLanguage);
}

function applyElementTranslation(element, attributeName, key) {
  const value = t(key);
  if (attributeName === "text") {
    element.textContent = value;
    return;
  }
  element.setAttribute(attributeName, value);
}

export function t(key, values = {}) {
  const message = translations[currentLanguage]?.[key] ?? translations.en[key] ?? key;
  return interpolate(message, values);
}

export function getCurrentLanguage() {
  return currentLanguage;
}

export function applyTranslations(root = document) {
  root.querySelectorAll("[data-i18n]").forEach((element) => {
    applyElementTranslation(element, "text", element.dataset.i18n);
  });
  root.querySelectorAll("[data-i18n-placeholder]").forEach((element) => {
    applyElementTranslation(element, "placeholder", element.dataset.i18nPlaceholder);
  });
  root.querySelectorAll("[data-i18n-aria-label]").forEach((element) => {
    applyElementTranslation(element, "aria-label", element.dataset.i18nAriaLabel);
  });

  const page = document.body?.dataset.page;
  if (page === "parse") document.title = t("parse.pageTitle");
  else if (page === "extract") document.title = t("extract.pageTitle");
  else document.title = t("app.title");
}

export function onLanguageChange(callback) {
  const handler = (event) => callback(event.detail.language);
  window.addEventListener("docslight:languagechange", handler);
  return () => window.removeEventListener("docslight:languagechange", handler);
}

export function initI18n() {
  const languageSelect = document.querySelector("#languageSelect");
  setLanguage(storedLanguage() || browserLanguage());
  if (languageSelect) {
    languageSelect.value = currentLanguage;
    languageSelect.addEventListener("change", () => {
      setLanguage(languageSelect.value, { persist: true });
      applyTranslations();
      window.dispatchEvent(new CustomEvent("docslight:languagechange", { detail: { language: currentLanguage } }));
    });
  }
  applyTranslations();
}
