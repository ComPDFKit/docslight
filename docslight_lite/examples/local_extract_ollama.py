"""
本地信息抽取示例 (使用 Ollama)

需要本地运行 Ollama 服务，将待处理的 PDF 文件放入 sample_files/ 目录。
"""

from pathlib import Path

from docslight import DocSlight

client = DocSlight(
    mode="local",
    local_llm={
        "provider": "ollama",
        "model": "llama3.1",
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
