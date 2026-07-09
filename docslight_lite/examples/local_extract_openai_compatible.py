"""
本地信息抽取示例 (使用 OpenAI-compatible API)

需要安装 local-llm 依赖，并将待处理的 PDF 文件放入 sample_files/ 目录。
将 your-base-url、your-model、your-api-key 替换为实际服务配置后运行。
DashScope qwen3 非 streaming 调用需要保留 enable_thinking=False。
"""

from pathlib import Path

from docslight import DocSlight

client = DocSlight(
    mode="local",
    local_llm={
        "provider": "openai-compatible",
        "base_url": "your-base-url",
        "model": "your-model",
        "api_key": "your-api-key",
        "extra_body": {"enable_thinking": False},
    },
)

# 文件路径: 相对于脚本所在目录
script_dir = Path(__file__).parent
file_path = script_dir / "sample_files" / "invoice.pdf"

result = client.extract(
    file_path,
    fields=["invoice_number", "invoice_date", "total_amount"],
)

print(result.to_json())
