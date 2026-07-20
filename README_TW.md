[English](README.md) | [繁體中文](README_TW.md) | [简体中文](README_CN.md)

# DocSlight - 開源文件解析與文件資料擷取引擎

[DocSlight](https://www.compdf.com/ai/docslight?utm_source=github_ai_docslight_newopen_tw&utm_medium=referral&utm_campaign=github_ai_docslight_newopen_tw&ref_platform_id=github_compdf) 是 KDAN 生態系的一部分，提供文件解析、OCR 與資料擷取功能，可將 PDF、掃描檔、圖片及 Office 文件轉換為結構化輸出，適用於 RAG、AI Agent 與企業文件自動化。

> - 如果您覺得 DocSlight 實用，歡迎在 GitHub 上為我們點亮一顆 ⭐ **Star**，支持我們持續成長與改進。
> - 有任何問題或想法？歡迎前往  [Discussions](https://github.com/ComPDF/docslight/discussions) 與我們交流。

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/license-GNU--LGPL--v3-green" alt="License"></a>
  <a href="#"><img src="https://img.shields.io/badge/python-3.10--3.13-blue" alt="Python"></a>
  <a href="#"><img src="https://img.shields.io/github/stars/compdf/docslight" alt="GitHub Stars" style="max-width: 100%;"></a>
  <a href="#"><img src="https://img.shields.io/pypi/v/docslight" alt="PyPI"></a>
  <a href="#"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="PRs Welcome"></a>
</p>

<p align="center">
  <a href="#快速開始"><b>快速開始</b></a> •
  <a href="#產品版本比較"><b>產品版本比較</b></a> •
  <a href="#使用方式"><b>使用方式</b></a> •
  <a href="#基準測試"><b>基準測試</b></a> •
  <a href="https://compdf.com" target="_blank"><b>Cloud API →</b></a> •
  <a href="https://www.compdf.com/guides/api-reference/v2/ai/overview?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw" target="_blank"><b>技術文件</b></a>
</p>

## 為什麼選擇 DocSlight？

不同於傳統 OCR 工具，DocSlight 將 AI 驅動的文件解析、支援 80 多種語言的 OCR，以及結構化資料擷取功能整合於單一開源平台。您可以選擇地端部署，也可以透過雲端 API 取得更高的準確度。

### 核心優勢

- 開源文件資料擷取引擎，避免受特定供應商限制
- OCR 支援 80 多種語言，並具備多語言自動偵測功能
- 支援結構化欄位擷取與邊界框（Bounding Box）溯源
- 提供 Markdown 與 JSON 輸出，方便串接後續處理流程
- 提供 Web UI + CLI + Python SDK
- 支援地端部署或 Cloud API
- 專為 RAG、AI Agent 與企業文件工作流程打造

### 適用情境

- RAG 管線與知識庫建置
- 發票處理與文件資訊擷取
- 合約分析與條款解析
- AI 助手與 AI Agent 工具整合
- 企業文件自動化與智慧文件處理（IDP）

無論您正在建置個人 RAG 專案，或是大型企業文件自動化系統，DocSlight 都能為文件理解工作提供穩固且易於擴充的基礎。

<img title="" src="Images/demo.gif" alt="DocSlight Demo" width="720">


## 快速開始

### 雲端模式（準確度更高，提供免費額度）

```bash
# 1. 安裝
pip install docslight

# 2. 設定 API Key
export COMPDF_API_KEY="your_public_key"    # 前往 https://compdf.com 取得

# 3. 使用雲端引擎解析
docslight parse invoice.pdf --mode cloud --output invoice.md
```

**取得 API Key：** [登入 ComPDF Console](https://www.compdf.com/compdf-portal/signin?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw)，並在 API Key 頁面建立或複製您的 publicKey。

![get-license-en](Images/get-license-en.png)

### 地端模式（免費，無需註冊）

```bash
# 1. 安裝
pip install docslight

# 2. 解析文件
docslight parse invoice.pdf --mode local --output invoice.md

# 3. 取得結構化結果
ls invoice.zip
```

### Web UI（瀏覽器）

```bash
# 啟動 Web 介面
git clone https://github.com/ComPDF/docslight.git

cd docslight

docker compose -f docker/docker-compose.yml up
# 打開 http://localhost:3022 並將檔案拖曳至頁面即可
```

以上功能皆可於 [ComPDF](https://www.compdf.com/?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw) 線上體驗，→ [體驗連結](https://www.compdf.com/demo/idp/document-parsing?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw)


## 產品版本比較

> 需要工作流程自動化、RBAC、稽核日誌、私有化部署或專屬支援嗎？**了解企業版：** [https://www.compdf.com/ai/docslight](https://www.compdf.com/ai/docslight?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw)

| 功能                              | DocSlight Lite（地端） | DocSlight-Lite（雲端） | DocSlight Enterprise（SaaS） | DocSlight Enterprise（私有化部署） |
| --------------------------------- |:------------------:|:------------------:|:--------------------------:|:---------------------------:|
| 地端檔案上傳                            | ✅                  | ✅                  | ✅                          | ✅                           |
| 雲端檔案上傳                            | ❌                  | ❌                  | ✅                          | ✅                           |
| DMS 檔案上傳                          | ❌                  | ❌                  | ✅                          | ✅                           |
| 掃描器檔案上傳                           | ❌                  | ❌                  | ✅                          | ✅                           |
| PDF 解析                            | ✅                  | ✅                  | ✅                          | ✅                           |
| 圖片解析                              | ✅                  | ✅                  | ✅                          | ✅                           |
| Word / PPT / Excel 解析             | ✅                  | ✅                  | ✅                          | ✅                           |
| Markdown 輸出                       | ✅                  | ✅                  | ✅                          | ✅                           |
| JSON 輸出                           | ✅                  | ✅                  | ✅                          | ✅                           |
| PDF 資料擷取                          | 需要地端 LLM           | ✅                  | ✅                          | ✅                           |
| 圖片資料擷取                            | 需要地端 LLM           | ✅                  | ✅                          | ✅                           |
| Word / PPT / Excel 資料擷取           | 需要地端 LLM           | ✅                  | ✅                          | ✅                           |
| 舊版 Office 格式解析與擷取（.doc/.ppt/.xls） | ❌                  | ✅                  | ✅                          | ✅                           |
| 批次處理                              | ✅                  | ❌                  | ✅                          | ✅                           |
| 自動分類                              | ❌                  | ❌                  | ✅                          | ✅                           |
| 人工審核工作流程                          | ❌                  | ❌                  | ✅                          | ✅                           |
| 複雜版面分析                            | 基礎                 | 進階                 | 進階                         | 進階                          |
| OCR 最佳化                           | 基礎                 | 進階                 | 進階                         | 進階                          |
| 結果溯源                              | ❌                  | ❌                  | ✅                          | ✅                           |
| 結果後續處理                             | ❌                  | ❌                  | ✅                          | ✅                           |
| 智慧結果審核                            | ❌                  | ❌                  | ✅                          | ✅                           |
| 自訂規則警示                            | ❌                  | ❌                  | ✅                          | ✅                           |
| Webhook 整合                        | ❌                  | ❌                  | ✅                          | ✅                           |
| API 管理                            | ❌                  | 有限                 | ✅                          | ✅                           |
| 知識庫整合                             | ❌                  | ❌                  | ✅                          | ✅                           |
| 稽核日誌                              | ❌                  | ❌                  | ✅                          | ✅                           |
| RBAC                              | ❌                  | ❌                  | ✅                          | ✅                           |
| 多租戶支援                             | ❌                  | ❌                  | ❌                          | ✅                           |
| 私有化部署                             | 僅限地端               | ❌                  | ❌                          | ✅                           |
| 專用 GPU                            | ❌                  | ❌                  | 可選                         | ✅                           |


## 輸入/輸出格式對照表

| 輸入類型 | 副檔名 | 雲端解析 | 地端解析 | 雲端擷取 | 地端擷取 | 解析輸出 | 擷取輸出 | 說明 |
| -------- | ------ |:--------:|:--------:|:--------:|:--------:| -------- | -------- | ---- |
| PDF | `.pdf` | ✅ | ✅ | ✅ | ✅ 需要地端 LLM | Markdown、JSON、標準 JSON、ZIP | JSON | 地端 PDF 解析會使用柵格化/OCR 流程。 |
| 圖片 | `.png`、`.jpg`、`.jpeg`、`.tif`、`.tiff`、`.bmp`、`.webp` | ✅ | ✅ | ✅ | ✅ 需要地端 LLM | Markdown、JSON、標準 JSON、ZIP | JSON | 地端圖片解析會將每張圖片視為一頁。 |
| Word | `.docx` | ✅ | ✅ | ✅ | ✅ 需要地端 LLM | Markdown、JSON、標準 JSON、ZIP | JSON | 地端不支援舊版 `.doc`。 |
| PowerPoint | `.pptx` | ✅ | ✅ | ✅ | ✅ 需要地端 LLM | Markdown、JSON、標準 JSON、ZIP | JSON | 地端不支援舊版 `.ppt`。 |
| Excel | `.xlsx` | ✅ | ✅ | ✅ | ✅ 需要地端 LLM | Markdown、JSON、標準 JSON、ZIP | JSON | 地端不支援舊版 `.xls`。 |
| 舊版 Office | `.doc`、`.ppt`、`.xls` | 取決於雲端 API 支援 | ❌ | 取決於雲端 API 支援 | ❌ | 雲端結果格式 | JSON | 地端處理前請先轉換為 `.docx`、`.pptx` 或 `.xlsx`。 |

`docslight convert-parse-json` 會接收地端解析產生的 JSON 物件，並輸出符合標準解析 JSON Schema 的結果；此指令不會直接處理原始文件。


## 安裝與首次執行

DocSlight 支援 Python 3.10 到 3.13。

```bash
pip install "docslight"
```

雲端模式需要網路連線及有效的 ComPDF Cloud API Key。地端模式預設使用 CPU；OCR 與 LLM 的處理時間會依文件大小、硬體效能及所選模型而異。


## 使用情境

- **RAG 管線** — 解析文件 -> 向量化嵌入 -> LLM 查詢
- **發票處理** — 擷取發票號碼、日期、金額與明細項目
- **合約分析** — 解析條款、當事人與日期，並支援邊界框溯源
- **文件數位化** — 批次將掃描檔轉換為可搜尋文字
- **AI Agent 整合** — 為 Claude / ChatGPT 提供 MCP 文件讀取服務

可執行的範例程式碼位於 [`examples/`](docslight_lite/examples/)：

- [`cloud_parse.py`](docslight_lite/examples/cloud_parse.py)
- [`cloud_extract.py`](docslight_lite/examples/cloud_extract.py)
- [`local_parse.py`](docslight_lite/examples/local_parse.py)
- [`local_extract_ollama.py`](docslight_lite/examples/local_extract_ollama.py)
- [`local_extract_openai_compatible.py`](docslight_lite/examples/local_extract_openai_compatible.py)
- [`path_examples.py`](docslight_lite/examples/path_examples.py)


## 使用方式

### Python SDK

```python
from docslight import Parser

# 地端模式 — 開源 OCR 與文件解析
parser = Parser(mode="local")
result = parser.parse("contract.pdf")
print(result.text)                    # 完整 Markdown 文字
print(result.metadata)                # 頁碼、區塊、邊界框

# 雲端模式 — 準確度更高的 PDF 解析
parser = Parser(mode="cloud", api_key="your_key")
result = parser.parse("invoice.pdf")
print(result.text)
print(result.tables)                  # 結構化表格資料
print(result.blocks[0].bbox)          # 邊界框溯源
```

### CLI

```bash
# 將 PDF 解析為 Markdown
docslight parse document.pdf --mode cloud -o document.md

# 解析為 JSON 或Standard JSON
docslight parse scan.png --mode cloud --format json -o scan.json
docslight parse scan.png --mode cloud --format standard-json -o standard.json

# 解析並輸出 ZIP 封存檔
docslight parse invoice.pdf --mode local --format zip -o invoice.zip

# 欄位擷取（雲端模式）
docslight extract invoice.pdf --mode cloud --fields invoice_no,date,total
docslight extract invoice.pdf --schema schema.json
docslight extract invoice.pdf --document-types document-types.json

# 使用地端 LLM 擷取資料
docslight extract invoice.pdf --mode local --fields invoice_no,total --local-llm-provider ollama --local-llm-model llama3.1

# 將地端解析 JSON 轉換為 Standard JSON
docslight convert-parse-json parse.json -o standard.json

# 啟動地端 API 服務
docslight web --host 127.0.0.1 --port 8000 --debug
```

#### CLI 指令

```bash
docslight parse INPUT [OPTIONS]
docslight extract INPUT [OPTIONS]
docslight convert-parse-json INPUT [OPTIONS]
```

#### parse/extract 通用參數

| 參數 | 可選值 / 預設值 | 說明 |
| ---- | --------------- | ---- |
| `INPUT` | 檔案路徑 | 要處理的文件路徑。 |
| `--mode` | `cloud`、`local`；預設值來自設定檔、環境變數或 `cloud` | 選擇使用 ComPDF Cloud 或地端離線處理。 |
| `--api-key` | 字串 | 雲端 API Key，覆寫 `COMPDF_API_KEY`。 |
| `--base-url` | URL；預設為 `https://api-server.compdf.com` | 雲端 API 基礎 URL，覆寫 `DOCSLIGHT_BASE_URL`。 |
| `--local-parser` | 字串 | 地端解析器選擇器，目前保留供地端解析器設定使用。|
| `--local-llm-provider` | `ollama`、`openai`、`openai-compatible`；使用任一地端 LLM 參數時預設為 `ollama` | 地端結構化擷取使用的 LLM 提供商。 |
| `--local-llm-model` | 字串 | 地端結構化擷取所使用的模型；使用地端 LLM 擷取時為必填。 |
| `--local-llm-base-url` | URL | 地端 LLM 端點。Ollama 預設為 `http://localhost:11434`；OpenAI-compatible 提供商必須指定。 |
| `--local-llm-api-key` | 字串 | 地端 LLM API Key。Ollama 預設為 `ollama`。 |

#### parse 參數

| 參數 | 可選值 / 預設值 | 說明 |
| ---- | --------------- | ---- |
| `--output`、`-o` | 檔案路徑 | 將輸出寫入檔案；未指定時輸出至標準輸出。 |
| `--format` | `markdown`、`json`、`standard-json`、`zip`；預設為 `markdown` | 指定解析輸出格式。若省略此參數，且 `--output` 以 `.zip` 結尾，系統會自動判斷為 `zip`。 |

`markdown` 會輸出解析後的 Markdown；`json` 會輸出 SDK 解析結果；`standard-json` 會輸出標準解析 JSON schema 的結果；`zip` 會輸出原始解析封存檔，通常應搭配 `output` 使用。

#### extract 參數

| 參數 | 可選值 / 預設值 | 說明 |
| ---- | --------------- | ---- |
| `--output`、`-o` | 檔案路徑 | 將擷取結果 JSON 寫入檔案；未指定時輸出至標準輸出。 |
| `--fields` | 以逗號分隔的欄位名稱，例如 `invoice_no,total` | 指定需要擷取的欄位。 |
| `--schema` | JSON 檔案路徑 | 擷取 schema 的 JSON 檔案。CLI 會讀取此檔案，並把 JSON 物件傳入 `extract`；常見內容為 `{"fields": ["invoice_no", "date", "total"]}`。同時支援 `properties` 的 JSON Schema 格式物件。 |
| `--document-types` | JSON 檔案路徑 | 文件類型路由設定檔；JSON 根節點必須是列表，例如 `["invoice", "receipt"]`。 |

`schema.json` 範例：

```json
{
  "fields": ["invoice_no", "date", "total"]
}
```

#### convert-parse-json 參數

| 參數 | 可選值 / 預設值 | 說明 |
| ---- | --------------- | ---- |
| `INPUT` | JSON 檔案路徑 | 要轉換的地端解析 JSON；JSON 根節點必須是物件。 |
| `--output`、`-o` | 檔案路徑 | 將轉換後的Standard JSON 寫入檔案；未指定時輸出至標準輸出。 |

#### 環境變數與設定檔

| 變數 | 說明 |
| ---- | ---- |
| `COMPDF_API_KEY` | 雲端模式使用的 API Key。 |
| `DOCSLIGHT_MODE` | 處理模式：`cloud` 或 `local`，預設為 `cloud`。 |
| `DOCSLIGHT_BASE_URL` | 雲端 API Base URL，預設為 `https://api-server.compdf.com`。 |
| `DOCSLIGHT_TIMEOUT` | 雲端請求逾時時間，單位為秒，預設為 `30`。 |
| `DOCSLIGHT_LOCAL_PARSER` | 地端解析器選擇器。 |

DocSlight 也會讀取 `~/.docslight/config.toml`。設定套用順序為：內建預設值、設定檔、環境變數、明確傳入的 SDK 或 CLI 參數。

```toml
mode = "cloud"
api_key = "your-api-key"
base_url = "https://api-server.compdf.com"
timeout = 30
local_parser = "paddleocr"  # 地端解析器設定保留

[local_llm]
provider = "ollama"
model = "llama3.1"
base_url = "http://localhost:11434"
api_key = "ollama"
timeout = 120
```

CLI 提供主要的地端 LLM 設定。`extra_body` 等進階地端 LLM 供應商設定，可透過 SDK 或 ~/.docslight/config.toml 設定。

### Docker

```bash
git clone https://github.com/ComPDF/docslight.git

cd docslight

docker compose -f docker/docker-compose.yml up
# 打開 http://127.0.0.1:3022
```

---

## 競品比較

| 能力          | DocSlight | MinerU | PDF-Extract-Kit | ExtractThinker |
| ----------- |:---------:|:------:|:---------------:|:--------------:|
| PDF 解析      | ✅         | ✅      | ✅               | ⚠️             |
| OCR 支援      | ✅         | ⚠️     | ✅               | ❌              |
| 資料擷取        | ✅         | ❌      | ❌               | ✅              |
| Web UI      | ✅         | ❌      | ❌               | ❌              |
| CLI         | ✅         | ✅      | ✅               | ❌              |
| Python SDK  | ✅         | ✅      | ✅               | ⚠️             |
| Cloud API   | ✅         | ❌      | ❌               | ❌              |
| 企業部署        | ✅         | ❌      | ❌               | ❌              |
| Markdown 輸出 | ✅         | ✅      | ✅               | ⚠️             |
| JSON 輸出     | ✅         | ✅      | ✅               | ✅              |
| 多語言 OCR     | ✅         | ⚠️     | ⚠️              | ❌              |
| 商業支援        | ✅         | ❌      | ❌               | ❌              |


## 系統架構

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         DocSlight 開源文件解析與擷取引擎                          │
│                        （LGPL 授權 | 地端 + 雲端雙模式）                         │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        存取方式層（入口）                                         │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────────────────┐   ┌────────────────────────┐  ┌───────────────────┐ │
│  │   Docker Web UI（建議）   │  │           CLI           │  │    Python SDK     │ │
│  │  容器化部署，快速開始使用    │  │       命令列工具        │  │  原生程式碼整合   │ │
│  │                           │  │                         │  │                  │ │
│  │  docker compose -f        │  │docslight parse <file>   │  │  from docslight  │ │
│  │  docker/compose.yml up    │  │                         │  │  import Parser   │ │
│  │                           │  │docslight extract <file> │  │  parser.parse()  │ │
│  │  瀏覽器開啟 http://localhost│  │                        │  │                   │ │
│  │  :3022                    │  │  docslight web         │  │                   │ │
│  └───────────────────────────┘  └────────────────────────┘  └───────────────────┘ │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           核心處理路由層（Router）                               │
│                   根據 --mode / 配置 自動切換 地端 / 雲端 引擎                   │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┴─────────────────┐
                    │                                   │
                    ▼                                   ▼
┌───────────────────────────────────┐   ┌─────────────────────────────────────┐
│        🖥️ 地端模式（Lite Local）   │   │        ☁️ 雲端模式（Lite Cloud）    │
│   （免費、離線執行、支援CPU）     │   │   （高精確度、需要 API Key，GPU 加速） │
├───────────────────────────────────┤   ├─────────────────────────────────────┤
│  • 輸入格式：                      │   │  • 輸入格式：                       │
│    PDF / 圖片 / 新版 Office       │   │    + 加入舊版 Office (.doc/.xls等)     │
│    (.docx/.pptx/.xlsx)           │   │                                     │
│  • 基礎 OCR：PaddleOCR            │   │  • 高準確度 VLM OCR 引擎             │
│  • 基礎版面分析                   │   │  • 複雜版面分析（表格/公式/多欄）   │
│  • 欄位抽取：需自行設定地端 LLM       │   │  • 內建 AI 欄位抽取                │
│    （Ollama/OpenAI-compatible）          │   │  • 邊界框（BBox）精準溯源          │
│  • 輸出：Markdown / JSON / Text   │   │  • 輸出：Markdown / JSON / Text    │
└───────────────────────────────────┘   └─────────────────────────────────────┘
                    │                                   │
                    └─────────────────┬─────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          AI 功能層（引擎模組）                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────────────────┐  │
│  │   OCR 引擎       │  │   結構分析器     │  │   欄位抽取模組               │  │
│  │  • 地端：         │  │  • 區塊分類      │  │  • 範本抽取                 │  │
│  │  PaddleOCR       │  │  • 表格偵測      │  │  • 自訂欄位                 │  │
│  │  • 雲端：         │  │  • 鍵值對映    │  │  • 規則 + LLM 結合          │  │
│  │  VLM 引擎        │  │  • 公式辨識      │  │  • BBox 溯源                │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────────────────┘  │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           輸出與生態層（Output & Ecosystem）                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   ┌───────────────┐   ┌───────────────┐   ┌───────────────────────────────┐  │
│   │  標準輸出格式  │   │  AI 生態系整合  │   │  企業版擴充（SaaS / 私有化部署）  │  │
│   │  • Markdown   │   │  • LangChain  │   │  • 工作流程編排                 │  │
│   │  • JSON       │   │  • LlamaIndex │   │  • 知識庫 / DMS               │  │
│   │  • Text       │   │  • CrewAI     │   │  • RBAC / 稽核日誌            │  │
│   │  • 含 BBox    │   │  • AutoGen    │   │  • 智慧審核 / 自訂規則        │  │
│   │    座標溯源   │   │  • Haystack   │   │  • 多租戶 / 私有化部署        │  │
│   └───────────────┘   └───────────────┘   └───────────────────────────────┘  │
│                                                                                 │
│   ┌──────────────────────────────────────────────────────────────────────────┐ │
│   │  適用情境：RAG 管線 / AI Agent / 企業文件自動化 / 智慧文件處理（IDP） │ │
│   └──────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
```


## 專為 AI Agent 與 RAG 打造

DocSlight 協助開發者運用開源 PDF 解析與開源文件資料擷取技術，建置現代化的 AI 文件工作流程。

### 常見應用

- RAG 系統
- AI 助手
- 企業知識庫
- AI Agent 工作流程
- 文件搜尋引擎
- MCP 應用
- 智慧文件處理（IDP），適用於不同規模的開源工作流程

### 相容生態系

- OpenAI
- Claude
- Ollama
- LangChain
- LlamaIndex
- CrewAI
- AutoGen
- Haystack

### 典型工作流程

```text
PDF / 圖片 / Office 文件
            ↓
        docslight
            ↓
Markdown / JSON 輸出
            ↓
向量資料庫
            ↓
LLM / AI Agent
            ↓
產生答案並執行自動化流程
```


## 基準測試

| 模型類型              | 方法               | 參數量  | 綜合得分↑     | TextEdit↓ | FormulaCDM↑ | TableTEDS↑ | TableTEDS-S↑ | Read OrderEdit↓ |
| ----------------- | ---------------- | ---- | --------- | --------- | ----------- | ---------- | ------------ | --------------- |
| **DocSlight（雲端）** | Specialized VLMs | 0.9B | **96.45** | 0.0321    | **97.76**   | **94.80**  | **97.02**    | 0.131           |
| MinerU2.5-Pro     | Specialized VLMs | 1.2B | 95.75     | 0.036     | 97.45       | 93.42      | 95.92        | 0.120           |
| GLM-OCR           | Specialized VLMs | 0.9B | 95.22     | 0.044     | 97.18       | 92.83      | 95.39        | 0.133           |
| PaddleOCR-VL-1.5  | Specialized VLMs | 0.9B | 94.93     | 0.038     | 96.89       | 91.67      | 94.37        | 0.130           |
| Ovis2.6-30B-A3B   | Specialized VLMs | 30B  | 93.70     | 0.035     | 95.17       | 89.44      | 92.40        | 0.135           |
| Logics-Parsing-v2 | Specialized VLMs | 4B   | 93.33     | 0.041     | 95.65       | 88.42      | 91.98        | 0.137           |
| HunyuanOCR        | Specialized VLMs | 1B   | 89.95     | 0.088     | 87.68       | 91.01      | 93.23        | 0.171           |
| Qwen3-VL-235B     | General VLMs     | 235B | 89.78     | 0.063     | 92.55       | 83.07      | 86.75        | 0.166           |
| Dolphin-v2        | Specialized VLMs | 3B   | 89.50     | 0.069     | 91.01       | 84.40      | 87.44        | 0.150           |
| GPT-5.2           | General VLMs     | -    | 86.59     | 0.114     | 88.21       | 82.95      | 87.93        | 0.193           |
| Mistral OCR       | Specialized VLMs | -    | 85.66     | 0.097     | 89.91       | 76.78      | 80.93        | 0.171           |
| Nanonets-OCR-s    | Specialized VLMs | 3B   | 83.61     | 0.108     | 81.46       | 80.18      | 84.51        | 0.213           |
| Marker            | Pipeline Tools   | -    | 78.44     | 0.157     | 85.24       | 65.77      | 73.24        | 0.243           |

> **方法說明：** 基於人工標註的真實資料，以字元級準確率進行評估。測試集包含超過 500 份企業文件，涵蓋發票、合約、表格及報告。資料集可於 [benchmarks/dataset](https://github.com/opendatalab/OmniDocBench) 取得。



## 安裝套件說明

| 套件           | 描述                  |
| -------------- | --------------------- |
| `docslight`    | 核心 CLI + Python SDK |

```bash
pip install "docslight"
```



## 支援

有任何建議？歡迎[發起討論](https://github.com/ComPDF/docslight/discussions)。如果您覺得 DocSlight 實用，歡迎在 GitHub 上為我們點亮一顆 ⭐ **Star**，支持我們持續成長與改進。



## 授權條款

DocSlight 採用 [LGPL](LICENSE) 開源授權條款。

如需商業版或企業版授權，包括支援 GPU 的私有化部署方案，請前往 [compdf.com](https://www.compdf.com/compdf-portal/signin?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw) 洽詢。



<p align="center">
  <b>由 ComPDF 團隊打造</b><br>
  <a href="https://compdf.com?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw">官網</a> ·
  <a href="https://www.compdf.com/guides/api-reference/v2/ai/overview?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw">文件</a> ·
  <a href="https://www.compdf.com/contact-sales?utm_source=github_ai_docslight_newopen&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdf_tw">企業諮詢</a>
</p>


