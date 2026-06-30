"""
本地文档解析示例

需要本地模型环境，将待处理的 PDF 文件放入 sample_files/ 目录。
"""

import json
from pathlib import Path

from docslight import DocSlight

client = DocSlight(mode="local")

# 文件路径: 相对于脚本所在目录
script_dir = Path(__file__).parent
file_path = script_dir / "sample_files" / "invoice.pdf"

result = client.parse(file_path)

print(result.to_markdown())

# save json output to file
json_output_path = script_dir / "parsed_output.json"
with open(json_output_path, "w", encoding="utf-8") as f:
    json.dump(result.to_json(), f, ensure_ascii=False, indent=2)
