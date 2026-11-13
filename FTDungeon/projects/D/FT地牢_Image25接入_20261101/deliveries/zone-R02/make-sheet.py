from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parent
font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 24)
small = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 17)
views = [
    ("north", "北向内部"), ("shell_north", "北向完整外壳"),
    ("south", "南向内部"), ("shell_south", "南向完整外壳"),
    ("west", "西向内部"), ("shell_west", "西向完整外壳"),
    ("east", "东向内部"), ("shell_east", "东向完整外壳"),
]
sheet = Image.new("RGB", (1920, 2720), "#202428")
draw = ImageDraw.Draw(sheet)
draw.text((32, 24), "R02 废弃前哨 · 四向内部 / 完整外壳", fill="#f3eadb", font=font)
draw.text((32, 63), "实际 room.nbt 软件渲染；内部仅剖切渲染副本，完整 NBT 不裁顶。", fill="#bdb9b0", font=small)
for index, (name, label) in enumerate(views):
    horizontal = index % 2 * 960
    vertical = 110 + index // 2 * 650
    image = Image.open(root / "preview" / f"{name}.png").convert("RGB")
    image.thumbnail((948, 632), Image.Resampling.LANCZOS)
    sheet.paste(image, (horizontal + 6, vertical))
    draw.text((horizontal + 24, vertical + 608), label, fill="#e8bd83", font=font)
sheet.save(root / "preview/four-view-sheet.png")
sheet = Image.new("RGB", (2304, 1536), "#202428")
for index, name in enumerate(["perspective", "roof", "entrance"]):
    image = Image.open(root / "preview" / f"{name}.png").convert("RGB")
    image.thumbnail((1152, 768), Image.Resampling.LANCZOS)
    sheet.paste(image, ((index % 2) * 1152, (index // 2) * 768))
draw = ImageDraw.Draw(sheet)
draw.text((1184, 850), "R02 废弃前哨", fill="#eee5d6", font=font)
draw.text((1184, 912), "21 × 11 × 21 / 4,851 条显式方块记录", fill="#d3c9b8", font=font)
draw.text((1184, 968), "北入口 [10,1,0] → 安全点 [10,1,6]", fill="#d3c9b8", font=font)
draw.text((1184, 1024), "双层完整石顶 / 四根落地立柱 / 原版陈设", fill="#d3c9b8", font=font)
draw.text((1184, 1080), "Forge 1.20.1 四旋转 + 200 tick：PASS", fill="#b8d3b3", font=font)
draw.text((1184, 1136), "未部署 / 未修改正式实例 / 未接入地牢槽位", fill="#bdb9b0", font=font)
sheet.save(root / "preview/overview-roof-entrance.png")
print("Created four-view and roof/entry review sheets")
