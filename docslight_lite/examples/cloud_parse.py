"""
云端文档解析示例

将待处理的 PDF 文件放入 sample_files/ 目录，
然后修改下方的文件路径即可。
"""

from pathlib import Path

from docslight import DocSlight

client = DocSlight(mode="cloud")

# 方式 1: 相对于脚本所在目录 (推荐)
script_dir = Path(__file__).parent
file_path = script_dir / "sample_files" / "invoice.pdf"

# 方式 2: 直接写绝对路径
# file_path = "/Users/yourname/Documents/invoice.pdf"

result = client.parse(file_path)

print(result.to_markdown())
