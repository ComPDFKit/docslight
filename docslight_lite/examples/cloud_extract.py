"""
云端信息抽取示例

将待处理的 PDF 文件放入 sample_files/ 目录，
然后修改下方的文件路径即可。
"""

from pathlib import Path

from docslight import DocSlight

client = DocSlight(mode="cloud")

# 文件路径: 相对于脚本所在目录
script_dir = Path(__file__).parent
file_path = script_dir / "sample_files" / "invoice.pdf"

result = client.extract(
    file_path,
    fields=["invoice_number", "invoice_date", "total_amount"],
)

print(result.to_json())
