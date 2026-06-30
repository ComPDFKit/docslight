"""
路径使用示例

演示如何正确指定要处理的文件路径。
文件可以放在任意位置，只要路径正确即可。
"""

from pathlib import Path

from docslight import DocSlight

# ============================================================
# 方式 1: 相对于当前工作目录的相对路径
# ============================================================
# 适用场景: 脚本和文件在同一目录或子目录
# 运行方式: cd examples && python path_examples.py

client = DocSlight(mode="cloud")

# 文件在脚本同级目录
result = client.parse("sample_files/invoice.pdf")
print(result.to_markdown())

# ============================================================
# 方式 2: 相对于脚本所在目录的相对路径 (推荐)
# ============================================================
# 适用场景: 无论从哪里运行脚本，路径都正确

script_dir = Path(__file__).parent
file_path = script_dir / "sample_files" / "invoice.pdf"

result = client.parse(file_path)
print(result.to_markdown())

# ============================================================
# 方式 3: 绝对路径
# ============================================================
# 适用场景: 文件在固定位置，如 /tmp, 用户目录等

file_path = Path("/Users/yourname/Documents/invoice.pdf")
result = client.parse(file_path)
print(result.to_markdown())

# ============================================================
# 方式 4: 字符串路径
# ============================================================
# Path 对象和字符串都可以，SDK 内部会自动转换

result = client.parse(str(script_dir / "sample_files" / "invoice.pdf"))
print(result.to_markdown())

# ============================================================
# 方式 5: 批量处理多个文件
# ============================================================

sample_dir = script_dir / "sample_files"
files = list(sample_dir.glob("*.pdf"))  # 获取所有 PDF 文件

for file_path in files:
    print(f"\n--- Processing {file_path.name} ---")
    result = client.parse(file_path)
    print(result.to_markdown()[:500])  # 只打印前 500 字符
