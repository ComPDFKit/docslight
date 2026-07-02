[English](README.md) | [繁體中文](README_TW.md) | [简体中文](README_CN.md)

# DocSlight - 开源文档解析与文档数据提取引擎

使用 [DocSlight](https://www.compdf.com/ai/docslight?utm_source=github_ai_docslight_open_cn&utm_medium=referral&utm_campaign=github_ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn)，精准解析并提取任意文档中的数据——包括 PDF、扫描件、图片和 Office 文件。这是来自 ComPDF（KDAN 生态系统）的开源 AI 项目。

> - 如果你觉得 DocSlight 有帮助，欢迎在 GitHub 上给我们一个 ⭐ **Star**——这能帮助我们成长和改进！
> - 有问题或想法？欢迎加入我们的 [Discussions](https://github.com/ComPDFKit/docslight/discussions) 参与讨论。

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/license-GNU--LGPL--v3-green" alt="License"></a>
  <a href="#"><img src="https://img.shields.io/badge/python-3.10--3.13-blue" alt="Python"></a>
  <a href="#"><img src="https://img.shields.io/github/stars/compdfkit/docslight" alt="GitHub Stars" style="max-width: 100%;"></a>
  <a href="#"><img src="https://img.shields.io/pypi/v/docslight" alt="PyPI"></a>
  <a href="#"><img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="PRs Welcome"></a>
</p>

<p align="center">
  <a href="#快速开始"><b>快速开始</b></a> •
  <a href="#产品版本对比"><b>产品版本对比</b></a> •
  <a href="#使用方式"><b>使用方式</b></a> •
  <a href="#效果测试与竞品对比"><b>效果测试与竞品对比</b></a> •
  <a href="https://compdf.com" target="_blank"><b>Cloud API →</b></a> •
  <a href="https://www.compdf.com/zh-cn/guides/api-reference/v2/ai/overview?utm_source=github_ai_docslight_open_cn&utm_medium=referral&utm_campaign=ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn" target="_blank"><b>文档</b></a>
</p>

## 为什么选择 DocSlight？

与传统 OCR 工具不同，DocSlight 将 AI 驱动的文档解析、80+ 语言的 OCR 以及结构化数据提取整合到一个开源平台中——既可本地部署，也可通过云端 API 使用，准确率更高。

### 核心优势

- 开源文档数据提取引擎——无厂商锁定
- 支持 80+ 语言 OCR，具备多语言自动检测
- 结构化字段提取，支持边界框（Bounding-Box）溯源
- Markdown / JSON 输出，便于下游处理
- 提供 Web UI + CLI + Python SDK
- 支持本地部署或 Cloud API
- 专为 RAG、AI Agent 和企业文档工作流打造

### 适用场景

- RAG 流水线与知识库构建
- 发票处理与文档信息提取
- 合同分析与条款解析
- AI 助手与 AI Agent 工具集成
- 企业文档自动化与智能文档处理（IDP）

无论你是在构建个人 RAG 项目，还是大型企业文档自动化系统，DocSlight 都能为文档理解提供可扩展的坚实基础。

<img title="" src="Images/demo.gif" alt="DocSlight Demo" width="720">

---

## 快速开始

### 云端模式（更高精度，提供免费额度）

```bash
# 1. 安装
pip install docslight

# 2. 设置 API 密钥
export COMPDF_API_KEY="your_public_key"    # 前往 https://compdf.com 获取

# 3. 使用云端引擎解析
docslight parse invoice.pdf --mode cloud --output invoice.md
```

**获取API Key：** [登录 ComPDF 控制台](https://www.compdf.com/compdf-portal/signin?utm_source=github_ai_docslight_open&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdfkit)。在 API 密钥页面创建或复制您的 publicKey。

![get-license-cn](Images/get-license-cn.png)

### 本地模式（免费，无需注册）

```bash
# 1. 安装
pip install docslight

# 2. 解析文档
docslight parse invoice.pdf --mode local --output invoice.md

# 3. 获取结构化结果
ls invoice.zip
```


### Web UI（浏览器）

```bash
# 启动 Web 界面
git clone https://github.com/ComPDFKit/docslight.git

docker compose -f docker/docker-compose.yml up
# 打开 http://localhost:3022 → 拖拽文件即可
```
以上功能均可在 [ComPDF](https://www.compdf.com/?utm_source=github_ai_docslight_open&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdfkit) 上体验使用，→[体验地址](https://www.compdf.com/demo/idp/document-parsing?utm_source=github_ai_docslight_open&utm_medium=referral&utm_campaign=ai_docslight_open&ref_platform_id=github_compdfkit)

---

## 产品版本对比

> 需要工作流自动化、RBAC、审计日志、私有部署或专属支持？ **了解企业版：** [https://www.compdf.com/ai/docslight](https://www.compdf.com/ai/docslight?utm_source=ai_docslight_open_cn&utm_medium=referral&utm_campaign=ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn)

| 功能特性                              | DocSlight Lite（本地） | DocSlight-Lite（云端） | DocSlight Enterprise（SaaS） | DocSlight Enterprise（私有化部署） |
| --------------------------------- |:------------------:|:------------------:|:--------------------------:|:---------------------------:|
| 本地文件上传                            | ✅                  | ✅                  | ✅                          | ✅                           |
| 云端文件上传                            | ❌                  | ❌                  | ✅                          | ✅                           |
| DMS 文件上传                          | ❌                  | ❌                  | ✅                          | ✅                           |
| 扫描仪文件上传                           | ❌                  | ❌                  | ✅                          | ✅                           |
| PDF 解析                            | ✅                  | ✅                  | ✅                          | ✅                           |
| 图片解析                              | ✅                  | ✅                  | ✅                          | ✅                           |
| Word / PPT / Excel 解析             | ✅                  | ✅                  | ✅                          | ✅                           |
| Markdown 输出                       | ✅                  | ✅                  | ✅                          | ✅                           |
| JSON 输出                           | ✅                  | ✅                  | ✅                          | ✅                           |
| PDF 数据提取                          | 需要本地 LLM           | ✅                  | ✅                          | ✅                           |
| 图片数据提取                            | 需要本地 LLM           | ✅                  | ✅                          | ✅                           |
| Word / PPT / Excel 数据提取           | 需要本地 LLM           | ✅                  | ✅                          | ✅                           |
| 旧版 Office 格式解析与提取（.doc/.ppt/.xls） | ❌                  | ✅                  | ✅                          | ✅                           |
| 批量处理                              | ✅                  | ❌                  | ✅                          | ✅                           |
| 自动分类                              | ❌                  | ❌                  | ✅                          | ✅                           |
| 人工审核工作流                           | ❌                  | ❌                  | ✅                          | ✅                           |
| 复杂版面分析                            | 基础                 | 高级                 | 高级                         | 高级                          |
| OCR 优化                            | 基础                 | 高级                 | 高级                         | 高级                          |
| 结果溯源                              | ❌                  | ❌                  | ✅                          | ✅                           |
| 结果后处理                             | ❌                  | ❌                  | ✅                          | ✅                           |
| 智能结果审核                            | ❌                  | ❌                  | ✅                          | ✅                           |
| 自定义规则告警                           | ❌                  | ❌                  | ✅                          | ✅                           |
| Webhook 集成                        | ❌                  | ❌                  | ✅                          | ✅                           |
| API 管理                            | ❌                  | 有限                 | ✅                          | ✅                           |
| 知识库集成                             | ❌                  | ❌                  | ✅                          | ✅                           |
| 审计日志                              | ❌                  | ❌                  | ✅                          | ✅                           |
| RBAC                              | ❌                  | ❌                  | ✅                          | ✅                           |
| 多租户支持                             | ❌                  | ❌                  | ❌                          | ✅                           |
| 私有化部署                             | 仅限本地               | ❌                  | ❌                          | ✅                           |
| 专用 GPU                            | ❌                  | ❌                  | 可选                         | ✅                           |

---

## 使用场景

- **RAG 流水线** — 解析文档 → 向量化嵌入 → LLM 查询
- **发票处理** — 提取发票号、日期、金额、明细项
- **合同分析** — 解析条款、当事方、日期，支持边界框溯源
- **文档数字化** — 批量将扫描档案转换为可搜索文本
- **AI Agent 集成** — 为 Claude / ChatGPT 提供 MCP 文档读取服务

可运行的示例代码位于 [`examples/`](docslight_lite/examples/)：

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

# 本地模式 — 开源 OCR 和文档解析
parser = Parser(mode="local")
result = parser.parse("contract.pdf")
print(result.text)                    # 完整 Markdown 文本
print(result.metadata)                # 页码、区块、边界框

# 云端模式 — 更高精度的 PDF 解析
parser = Parser(mode="cloud", api_key="your_key")
result = parser.parse("invoice.pdf")
print(result.text)
print(result.tables)                  # 结构化表格数据
print(result.blocks[0].bbox)          # 边界框溯源
```

### CLI

```bash
# 解析 PDF 为 Markdown
docslight parse document.pdf -o md

# 解析图片为 JSON（含边界框）
docslight parse scan.png -o json --bbox

# 字段提取（云端模式）
docslight extract invoice.pdf --schema '{"fields": ["invoice_no", "date", "total"]}'

# 监听目录中的新文件
docslight watch ./incoming/ --cloud
```

#### Parse | extract 命令行参数

```bash
docslight parse [options] <input>
docslight extract [options] <input>
```

| 选项                                           | 描述                                                       |
| -------------------------------------------- | -------------------------------------------------------- |
| `input`                                      | 必需的输入文件路径。                                               |
| `--mode {cloud,local}`                       | 必需的处理模式。使用 `cloud` 对应 ComPDF Cloud，或使用 `local` 进行离线本地处理。 |
| `--api-key API_KEY`                          | 云端 API 密钥。在云端模式下必需（除非已设置 `DOCSLIGHT_API_KEY`）。           |
| `--base-url BASE_URL`                        | 可选的自定义云端 API 基础 URL。                                     |
| `--output, -o OUTPUT`                        | 输出文件路径。对于文本格式，默认为标准输出 (stdout)；对于 ZIP 输出，则为必需或推荐项。       |
| `--format {markdown,json,standard-json,zip}` | 解析输出格式。若未指定，默认使用 Markdown（除非输出路径以 `.zip` 结尾）。            |
| `--local-parser LOCAL_PARSER`                | 本地模式下的可选本地解析器选择器。                                        |
| `--local-llm-provider LOCAL_LLM_PROVIDER`    | 本地 LLM 提供商设置。仅解析的工作流无需此项。                                |
| `--local-llm-model LOCAL_LLM_MODEL`          | 本地 LLM 模型名称。仅解析的工作流无需此项。                                 |
| `--local-llm-base-url LOCAL_LLM_BASE_URL`    | 本地 LLM 端点基础 URL（适用于需要此项的提供商）。                            |
| `--local-llm-api-key LOCAL_LLM_API_KEY`      | 本地 LLM API 密钥（适用于需要此项的提供商）。                              |

### Docker

```bash
git clone https://github.com/ComPDFKit/docslight.git

docker compose -f docker/docker-compose.yml up
# 打开 http://127.0.0.1:3022
```

---

## 竞品对比

| 能力          | DocSlight | MinerU | PDF-Extract-Kit | ExtractThinker |
| ----------- |:---------:|:------:|:---------------:|:--------------:|
| PDF 解析      | ✅         | ✅      | ✅               | ⚠️             |
| OCR 支持      | ✅         | ⚠️     | ✅               | ❌              |
| 数据提取        | ✅         | ❌      | ❌               | ✅              |
| Web UI      | ✅         | ❌      | ❌               | ❌              |
| CLI         | ✅         | ✅      | ✅               | ❌              |
| Python SDK  | ✅         | ✅      | ✅               | ⚠️             |
| Cloud API   | ✅         | ❌      | ❌               | ❌              |
| 企业部署        | ✅         | ❌      | ❌               | ❌              |
| Markdown 输出 | ✅         | ✅      | ✅               | ⚠️             |
| JSON 输出     | ✅         | ✅      | ✅               | ✅              |
| 多语言 OCR     | ✅         | ⚠️     | ⚠️              | ❌              |
| 商业支持        | ✅         | ❌      | ❌               | ❌              |

---

## 架构

```text
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           DocSlight 开源文档解析与提取引擎                        │
│                          （LGPL 协议 | 本地 + 云端双模式）                       │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        接入方式层（入口）                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────────────────┐   ┌────────────────────────┐  ┌───────────────────┐ │
│  │     Docker Web UI（主推）  │  │           CLI           │  │     Python SDK    │ │
│  │  一键容器化部署，开箱即用     │  │        命令行工具         │  │  原生代码集成      │ │
│  │                           │  │                         │  │                  │ │
│  │  docker compose -f        │  │docslight parse <file>   │  │  from docslight  │ │
│  │  docker/compose.yml up    │  │                         │  │  import Parser   │ │
│  │                           │  │docslight extract <file> │  │  parser.parse()  │ │
│  │  浏览器访问 http://localhost│  │                        │  │                   │ │
│  │  :3022                    │  │  docslight web         │  │                   │ │
│  └───────────────────────────┘  └────────────────────────┘  └───────────────────┘ │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           核心处理路由层（Router）                               │
│                   根据 --mode / 配置 自动切换 本地 / 云端 引擎                    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┴─────────────────┐
                    │                                   │
                    ▼                                   ▼
┌───────────────────────────────────┐   ┌─────────────────────────────────────┐
│        🖥️ 本地模式（Lite Local）   │   │        ☁️ 云端模式（Lite Cloud）    │
│   （免费，离线运行，CPU 支持）     │   │   （高精度，需 API Key，GPU 加速） │
├───────────────────────────────────┤   ├─────────────────────────────────────┤
│  • 输入格式：                      │   │  • 输入格式：                       │
│    PDF / 图片 / 新版 Office       │   │    + 老格式 Office (.doc/.xls等)   │
│    (.docx/.pptx/.xlsx)           │   │                                     │
│  • 基础 OCR：PaddleOCR            │   │  • 高精度 VLM OCR 引擎             │
│  • 基础版面分析                   │   │  • 复杂版面分析（表格/公式/多栏）   │
│  • 字段抽取：需自配本地 LLM       │   │  • 内置 AI 字段抽取                │
│    （Ollama/OpenAI兼容）          │   │  • 边界框（BBox）精准溯源          │
│  • 输出：Markdown / JSON / Text   │   │  • 输出：Markdown / JSON / Text    │
└───────────────────────────────────┘   └─────────────────────────────────────┘
                    │                                   │
                    └─────────────────┬─────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          AI 能力层（引擎模块）                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────────────────┐  │
│  │   OCR 引擎       │  │   结构分析器     │  │   字段抽取模块               │  │
│  │  • 本地：         │  │  • 区块分类      │  │  • 模板抽取                 │  │
│  │  PaddleOCR       │  │  • 表格检测      │  │  • 自定义字段               │  │
│  │  • 云端：         │  │  • 键值对映射    │  │  • 规则 + LLM 结合          │  │
│  │  VLM 引擎        │  │  • 公式识别      │  │  • BBox 溯源                │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────────────────┘  │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           输出与生态层（Output & Ecosystem）                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   ┌───────────────┐   ┌───────────────┐   ┌───────────────────────────────┐  │
│   │  标准输出格式  │   │  AI 生态集成  │   │  企业版扩展（SaaS / 私有化）  │  │
│   │  • Markdown   │   │  • LangChain  │   │  • 工作流编排                 │  │
│   │  • JSON       │   │  • LlamaIndex │   │  • 知识库 / DMS               │  │
│   │  • Text       │   │  • CrewAI     │   │  • RBAC / 审计日志            │  │
│   │  • 含 BBox    │   │  • AutoGen    │   │  • 智能审核 / 自定义规则      │  │
│   │    坐标溯源   │   │  • Haystack   │   │  • 多租户 / 私有化部署        │  │
│   └───────────────┘   └───────────────┘   └───────────────────────────────┘  │
│                                                                                 │
│   ┌──────────────────────────────────────────────────────────────────────────┐ │
│   │  目标场景：RAG 流水线 / AI Agent / 企业文档自动化 / 智能文档处理（IDP） │ │
│   └──────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 专为 AI Agent 与 RAG 打造

DocSlight 帮助开发者使用开源 PDF 解析和开源文档数据提取技术，构建现代化的 AI 文档工作流。

### 常见应用

- RAG 系统
- AI 助手
- 企业知识库
- AI Agent 工作流
- 文档搜索引擎
- MCP 应用
- 智能文档处理（IDP）——适用于任意规模的开源 IDP

### 兼容生态

- OpenAI
- Claude
- Ollama
- LangChain
- LlamaIndex
- CrewAI
- AutoGen
- Haystack

### 典型工作流

```
PDF / 图片 / Office 文档
            ↓
        docslight
            ↓
Markdown / JSON 输出
            ↓
向量数据库
            ↓
LLM / AI Agent
            ↓
答案与自动化
```

---

## 效果测试与竞品对比

| 模型类型              | 方法               | 参数量  | 综合得分↑     | TextEdit↓ | FormulaCDM↑ | TableTEDS↑ | TableTEDS-S↑ | Read OrderEdit↓ |
| ----------------- | ---------------- | ---- | --------- | --------- | ----------- | ---------- | ------------ | --------------- |
| **DocSlight（云端）** | Specialized VLMs | 0.9B | **96.45** | 0.0321    | **97.76**   | **94.80**  | **97.02**    | 0.131           |
| MinerU2.5-Pro     | Specialized VLMs | 1.2B | 95.75     | 0.036     | 97.45       | 93.42      | 95.92        | 0.120           |
| GLM-OCR           | Specialized VLMs | 0.9B | 95.22     | 0.044     | 97.18       | 92.83      | 95.39        | 0.133           |
| PaddleOCR-VL-1.5  | Specialized VLMs | 0.9B | 94.93     | 0.038     | 96.89       | 91.67      | 94.37        | 0.130           |
| Ovis2.6-30B-A3B   | Specialized VLMs | 30B  | 93.70     | 0.035     | 95.17       | 89.44      | 92.40        | 0.135           |
| Logics-Parsing-v2 | Specialized VLMs | 4B   | 93.33     | 0.041     | 95.65       | 88.42      | 91.98        | 0.137           |
| HunyuanOCR        | Specialized VLMs | 1B   | 89.95     | 0.088     | 87.68       | 91.01      | 93.23        | 0.171           |
| Qwen3-VL-235B     | General VLMs     | 235B | 89.78     | 0.063     | 92.55       | 83.07      | 86.75        | 0.166           |
| Dolphin-v2        | Specialized VLMs | 3B   | 89.50     | 0.069     | 91.01       | 84.40      | 87.44        | 0.150           |
| GPT-5.2           | 通用 VLM           | -    | 86.59     | 0.114     | 88.21       | 82.95      | 87.93        | 0.193           |
| Mistral OCR       | Specialized VLMs | -    | 85.66     | 0.097     | 89.91       | 76.78      | 80.93        | 0.171           |
| Nanonets-OCR-s    | Specialized VLMs | 3B   | 83.61     | 0.108     | 81.46       | 80.18      | 84.51        | 0.213           |
| Marker            | Pipeline Tools   | -    | 78.44     | 0.157     | 85.24       | 65.77      | 73.24        | 0.243           |

> **方法说明：** 基于人工标注的真实数据，以字符级准确率衡量，测试集涵盖 500+ 份企业文档（发票、合同、表格、报告）。测试集可在 [benchmarks/dataset](https://github.com/opendatalab/OmniDocBench) 获取。

---

## 安装包说明

| 包                | 描述                  |
|------------------|---------------------|
| `docslight`      | 核心 CLI + Python SDK |   

```bash
pip install "docslight"
```

---

## 支持

有建议？立即[发起讨论](https://github.com/ComPDFKit/docslight/discussions)。如果你觉得 DocSlight 有帮助，欢迎在 GitHub 上给我们一个 ⭐ **Star**——这能帮助我们成长和改进！

---

## 📄 许可证

DocSlight 采用 [LGPL](LICENSE) 开源协议发布。

商业 / 企业许可证（支持 GPU 私有化部署）可在 [compdf.com](https://www.compdf.com/compdf-portal/signin?utm_source=github_ai_docslight_open_cn&utm_medium=referral&utm_campaign=ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn) 获取。

---

<p align="center">
  <b>由 ComPDF 团队打造。</b><br>
  <a href="https://compdf.com?utm_source=github_ai_docslight_open_cn&utm_medium=referral&utm_campaign=ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn">官网</a> ·
  <a href="https://www.compdf.com/zh-cn/guides/api-reference/v2/ai/overview?utm_source=ai_docslight_open_cn&utm_medium=referral&utm_campaign=ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn">文档</a> ·
  <a href="https://www.compdf.com/contact-sales?utm_source=ai_docslight_open_cn&utm_medium=referral&utm_campaign=ai_docslight_open_cn&ref_platform_id=github_compdfkit_cn">企业咨询</a>
</p>
