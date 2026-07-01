[English](README.md) | [繁體中文](README_TW.md) | [简体中文](README_CN.md)

# DocSlight - 開源文件解析與文件資料提取引擎

使用 [DocSlight](https://www.compdf.com/ai/docslight?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit)，精準解析並擷取任意文件中的資料，包括 PDF、掃描檔、圖片和 Office 文件。這是來自 ComPDF（KDAN 生態系統）的開源 AI 專案。

> - 如果你覺得 DocSlight 有幫助，歡迎在 GitHub 上給我們一個 ⭐ **Star**，這能幫助我們成長與改進。
> - 有問題或想法？歡迎加入我們的 [Discussions](https://github.com/ComPDFKit/docslight/discussions) 參與討論。

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/license-GNU--LGPL--v3-green" alt="License"></a>
  <a href="#"><img src="https://img.shields.io/badge/python-3.10--3.13-blue" alt="Python"></a>
  <a href="#"><img src="https://img.shields.io/github/stars/compdfkit/docslight" alt="GitHub Stars" style="max-width: 100%;"></a>
  <a href="#"><img src="https://img.shields.io/pypi/v/docslight" alt="PyPI"></a>
  <a href="#"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="PRs Welcome"></a>
</p>

<p align="center">
  <a href="#快速開始"><b>快速開始</b></a> •
  <a href="#產品版本對比"><b>產品版本對比</b></a> •
  <a href="#使用方式"><b>使用方式</b></a> •
  <a href="#基準測試"><b>基準測試</b></a> •
  <a href="https://compdf.com" target="_blank"><b>Cloud API →</b></a> •
  <a href="https://www.compdf.com/guides/api-reference/v2/ai/overview?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw" target="_blank"><b>文件</b></a>
</p>

## 為什麼選擇 DocSlight？

與傳統 OCR 工具不同，DocSlight 將 AI 驅動的文件解析、80+ 語言的 OCR，以及結構化資料擷取整合到一個開源平台中。你可以選擇地端部署，或透過雲端 API 使用以獲得更高準確率。

### 核心優勢

- 開源文件資料擷取引擎，沒有廠商鎖定
- 支援 80+ 語言 OCR，具備多語言自動偵測
- 結構化欄位擷取，支援邊界框（Bounding Box）溯源
- Markdown / JSON 輸出，便於下游處理
- 提供 Web UI + CLI + Python SDK
- 支援地端部署或 Cloud API
- 專為 RAG、AI Agent 與企業文件工作流程打造

### 適用場景

- RAG 管線與知識庫建構
- 發票處理與文件資訊擷取
- 合約分析與條款解析
- AI 助手與 AI Agent 工具整合
- 企業文件自動化與智慧文件處理（IDP）

無論你是在建構個人 RAG 專案，還是大型企業文件自動化系統，DocSlight 都能為文件理解提供可擴展的堅實基礎。

<img title="" src="Images/demo.gif" alt="DocSlight Demo" width="720">

---

## 快速開始

### 雲端模式（更高精確度，提供免費額度）

```bash
# 1. 安裝
pip install docslight

# 2. 設定 API 金鑰
export COMPDF_API_KEY="your_public_key"    # 前往 https://compdf.com 取得

# 3. 使用雲端引擎解析
docslight parse invoice.pdf --mode cloud --output json
```

### 地端模式（免費，無需註冊）

```bash
# 1. 安裝
pip install docslight

# 2. 解析文件
docslight parse invoice.pdf --mode local --output invoice.md

# 3. 取得結構化結果
ls invoice.zip
```

**獲取API Key：** [登入 ComPDF Console](https://www.compdf.com/compdf-portal/signin?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw)。在 API Key 頁面建立或複製你的 publicKey。

![get-license-en](Images/get-license-en.png)

### Web UI（瀏覽器）

```bash
# 啟動 Web 介面
docslight web

git clone https://github.com/ComPDFKit/docslight.git

docker compose -f docker/docker-compose.yml up
# 打開 http://localhost:3022 並拖曳檔案即可
```

以上功能均可於 [ComPDF](https://www.compdf.com/?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw) 上體驗使用，→ [體驗地址](https://www.compdf.com/demo/idp/document-parsing?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw)

---

## 產品版本對比

> 需要工作流程自動化、RBAC、稽核日誌、私有部署或專屬支援？**了解企業版：** [https://www.compdf.com/ai/docslight](https://www.compdf.com/ai/docslight?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw)

| 功能特性                              | DocSlight Lite（地端） | DocSlight-Lite（雲端） | DocSlight Enterprise（SaaS） | DocSlight Enterprise（私有化部署） |
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
| 結果後處理                             | ❌                  | ❌                  | ✅                          | ✅                           |
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

---

## 使用場景

- **RAG 管線** — 解析文件 -> 向量化嵌入 -> LLM 查詢
- **發票處理** — 擷取發票號碼、日期、金額與明細項
- **合約分析** — 解析條款、當事方與日期，支援邊界框溯源
- **文件數位化** — 批次將掃描檔案轉換為可搜尋文字
- **AI Agent 整合** — 為 Claude / ChatGPT 提供 MCP 文件讀取服務

可執行的範例程式碼位於 [`examples/`](docslight_lite/examples/)：

- [`cloud_parse.py`](docslight_lite/examples/cloud_parse.py)
- [`cloud_extract.py`](docslight_lite/examples/cloud_extract.py)
- [`local_parse.py`](docslight_lite/examples/local_parse.py)
- [`local_extract_ollama.py`](docslight_lite/examples/local_extract_ollama.py)
- [`local_extract_openai_compatible.py`](docslight_lite/examples/local_extract_openai_compatible.py)
- [`path_examples.py`](docslight_lite/examples/path_examples.py)

---

## 使用方式

### Python SDK

```python
from docslight import Parser

# 地端模式 — 開源 OCR 與文件解析
parser = Parser(mode="local")
result = parser.parse("contract.pdf")
print(result.text)                    # 完整 Markdown 文字
print(result.metadata)                # 頁碼、區塊、邊界框

# 雲端模式 — 更高精確度的 PDF 解析
parser = Parser(mode="cloud", api_key="your_key")
result = parser.parse("invoice.pdf")
print(result.text)
print(result.tables)                  # 結構化表格資料
print(result.blocks[0].bbox)          # 邊界框溯源
```

### CLI

```bash
# 解析 PDF 為 Markdown
docslight parse document.pdf -o md

# 解析圖片為 JSON（含邊界框）
docslight parse scan.png -o json --bbox

# 欄位擷取（雲端模式）
docslight extract invoice.pdf --schema '{"fields": ["invoice_no", "date", "total"]}'

# 監聽目錄中的新檔案
docslight watch ./incoming/ --cloud
```

#### Parse | extract 命令列參數

```bash
docslight parse [options] <input>
docslight extract [options] <input>
```

| 選項                                           | 描述                                                       |
| -------------------------------------------- | -------------------------------------------------------- |
| `input`                                      | 必填的輸入檔案路徑。                                               |
| `--mode {cloud,local}`                       | 必填的處理模式。使用 `cloud` 對應 ComPDF Cloud，或使用 `local` 進行離線地端處理。 |
| `--api-key API_KEY`                          | 雲端 API 金鑰。雲端模式下必填，除非已設定 `DOCSLIGHT_API_KEY`。             |
| `--base-url BASE_URL`                        | 可選的自訂雲端 API 基礎 URL。                                      |
| `--output, -o OUTPUT`                        | 輸出檔案路徑。對於文字格式，預設輸出到標準輸出；對於 ZIP 輸出則為必填或建議指定。              |
| `--format {markdown,json,standard-json,zip}` | 解析輸出格式。若未指定，預設為 Markdown，除非輸出路徑以 `.zip` 結尾。              |
| `--local-parser LOCAL_PARSER`                | 地端模式下可選的本地解析器選擇器。                                        |
| `--local-llm-provider LOCAL_LLM_PROVIDER`    | 地端 LLM 提供商設定。僅解析流程不需要此參數。                                |
| `--local-llm-model LOCAL_LLM_MODEL`          | 地端 LLM 模型名稱。僅解析流程不需要此參數。                                 |
| `--local-llm-base-url LOCAL_LLM_BASE_URL`    | 地端 LLM 端點基礎 URL，適用於需要此參數的提供商。                            |
| `--local-llm-api-key LOCAL_LLM_API_KEY`      | 地端 LLM API 金鑰，適用於需要此參數的提供商。                              |

### Docker

```bash
pip install "docslight"
docslight web

git clone https://github.com/ComPDFKit/docslight.git

docker compose -f docker/docker-compose.yml up
# 打開 http://127.0.0.1:3022
```

---

## 競品對比

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

---

## 架構

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                         DocSlight 開源文件解析與擷取引擎                          │
│                        （LGPL 協議 | 地端 + 雲端雙模式）                         │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        接入方式層（入口）                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────────────────┐   ┌────────────────────────┐  ┌───────────────────┐ │
│  │   Docker Web UI（主推）   │  │           CLI           │  │    Python SDK     │ │
│  │  一鍵容器化部署，開箱即用    │  │       命令列工具        │  │  原生程式碼整合   │ │
│  │                           │  │                         │  │                  │ │
│  │  docker compose -f        │  │docslight parse <file>   │  │  from docslight  │ │
│  │  docker/compose.yml up    │  │                         │  │  import Parser   │ │
│  │                           │  │docslight extract <file> │  │  parser.parse()  │ │
│  │  瀏覽器訪問 http://localhost│  │                        │  │                   │ │
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
│   （免費，離線執行，CPU 支援）     │   │   （高精度，需 API Key，GPU 加速） │
├───────────────────────────────────┤   ├─────────────────────────────────────┤
│  • 輸入格式：                      │   │  • 輸入格式：                       │
│    PDF / 圖片 / 新版 Office       │   │    + 舊版 Office (.doc/.xls等)     │
│    (.docx/.pptx/.xlsx)           │   │                                     │
│  • 基礎 OCR：PaddleOCR            │   │  • 高精度 VLM OCR 引擎             │
│  • 基礎版面分析                   │   │  • 複雜版面分析（表格/公式/多欄）   │
│  • 欄位抽取：需自配地端 LLM       │   │  • 內建 AI 欄位抽取                │
│    （Ollama/OpenAI相容）          │   │  • 邊界框（BBox）精準溯源          │
│  • 輸出：Markdown / JSON / Text   │   │  • 輸出：Markdown / JSON / Text    │
└───────────────────────────────────┘   └─────────────────────────────────────┘
                    │                                   │
                    └─────────────────┬─────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          AI 能力層（引擎模組）                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────────────────┐  │
│  │   OCR 引擎       │  │   結構分析器     │  │   欄位抽取模組               │  │
│  │  • 地端：         │  │  • 區塊分類      │  │  • 範本抽取                 │  │
│  │  PaddleOCR       │  │  • 表格偵測      │  │  • 自訂欄位                 │  │
│  │  • 雲端：         │  │  • 鍵值對映射    │  │  • 規則 + LLM 結合          │  │
│  │  VLM 引擎        │  │  • 公式識別      │  │  • BBox 溯源                │  │
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
│   │  標準輸出格式  │   │  AI 生態整合  │   │  企業版擴充（SaaS / 私有化）  │  │
│   │  • Markdown   │   │  • LangChain  │   │  • 工作流編排                 │  │
│   │  • JSON       │   │  • LlamaIndex │   │  • 知識庫 / DMS               │  │
│   │  • Text       │   │  • CrewAI     │   │  • RBAC / 稽核日誌            │  │
│   │  • 含 BBox    │   │  • AutoGen    │   │  • 智慧審核 / 自訂規則        │  │
│   │    座標溯源   │   │  • Haystack   │   │  • 多租戶 / 私有化部署        │  │
│   └───────────────┘   └───────────────┘   └───────────────────────────────┘  │
│                                                                                 │
│   ┌──────────────────────────────────────────────────────────────────────────┐ │
│   │  目標場景：RAG 流水線 / AI Agent / 企業文件自動化 / 智慧文件處理（IDP） │ │
│   └──────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 專為 AI Agent 與 RAG 打造

DocSlight 幫助開發者利用開源 PDF 解析與開源文件資料擷取技術，建構現代化的 AI 文件工作流程。

### 常見應用

- RAG 系統
- AI 助手
- 企業知識庫
- AI Agent 工作流程
- 文件搜尋引擎
- MCP 應用
- 智慧文件處理（IDP），適用於任意規模的開源工作流程

### 相容生態

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
答案與自動化
```

---

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

> **方法說明：** 基於人工標註的真實資料，以字元級準確率衡量。測試集涵蓋 500+ 份企業文件，包括發票、合約、表格與報告。資料集可於 [benchmarks/dataset](https://github.com/opendatalab/OmniDocBench) 取得。

---

## 安裝套件說明

| 套件           | 描述                  |
| -------------- | --------------------- |
| `docslight`    | 核心 CLI + Python SDK |

```bash
pip install "docslight"
```

---

## 支援

有建議？[發起討論](https://github.com/ComPDFKit/docslight/discussions)。如果你覺得 DocSlight 有幫助，歡迎在 GitHub 上給我們一個 ⭐ **Star**，這能幫助我們成長與改進。

---

## 授權條款

DocSlight 採用 [LGPL](LICENSE) 開源授權發布。

商業 / 企業授權（支援 GPU 私有化部署）可在 [compdf.com](https://www.compdf.com/compdf-portal/signin?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw) 取得。

---

<p align="center">
  <b>由 ComPDF 團隊打造。</b><br>
  <a href="https://compdf.com?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw">官網</a> ·
  <a href="https://www.compdf.com/guides/api-reference/v2/ai/overview?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw">文件</a> ·
  <a href="https://www.compdf.com/contact-sales?utm_source=github_ai_docslight_open_tw&utm_medium=referral&utm_campaign=ai_docslight_open_tw&ref_platform_id=github_compdfkit_tw">企業諮詢</a>
</p>


