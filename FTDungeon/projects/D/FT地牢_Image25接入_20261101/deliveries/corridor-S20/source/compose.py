import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent.parent
PREVIEW = ROOT / "preview"
BACKGROUND = "#101a20"
TEXT = "#dde6d8"
MUTED = "#a8bbb6"


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def content(name):
    return Image.open(PREVIEW / f"{name}.png").convert("RGB").crop((0, 82, 1536, 980))


def tile_sheet(kind):
    sheet = Image.new("RGB", (2304, 1536), BACKGROUND)
    for ordinal, direction in enumerate(("north", "east", "south", "west")):
        image = Image.open(PREVIEW / f"{kind}-{direction}.png").convert("RGB")
        image = image.resize((1152, 768), Image.Resampling.LANCZOS)
        sheet.paste(image, ((ordinal % 2) * 1152, (ordinal // 2) * 768))
    sheet.save(PREVIEW / f"four-{kind}.png")


def main():
    digest = hashlib.sha256((ROOT / "room.nbt").read_bytes()).hexdigest()
    for report_name in ("static.json", "forge-native.json", "views.json"):
        report = json.loads((ROOT / "qa" / report_name).read_text(encoding="utf-8"))
        assert report["status"] == "PASS" and report["sha256"] == digest
    for kind in ("interior", "exterior"):
        tile_sheet(kind)
    image = Image.new("RGB", (1920, 1320), BACKGROUND)
    draw = ImageDraw.Draw(image)
    draw.text((32, 22), "S20 / 废弃矿道 · 浅水短廊", font=font(40), fill="#e3c18a")
    draw.text((34, 78), "11 × 9 × 16 方块  |  一字贯通 · 两端同高 3 × 3 接口  |  Minecraft 1.20.1", font=font(22), fill=MUTED)
    image.paste(content("interior-north").resize((1248, 730), Image.Resampling.LANCZOS), (32, 127))
    draw.text((40, 873), "北入口内视 / 原始 NBT，无剖切", font=font(24), fill=TEXT)
    image.paste(content("cutaway").resize((592, 346), Image.Resampling.LANCZOS), (1296, 127))
    draw.text((1305, 484), "剖切仅作用于预览，不改 NBT", font=font(21), fill=MUTED)
    image.paste(content("exterior-north").resize((592, 346), Image.Resampling.LANCZOS), (1296, 531))
    draw.text((1305, 888), "完整封顶 / 石墙与木门框", font=font(21), fill=MUTED)
    draw.rounded_rectangle((32, 939, 1888, 1242), radius=14, fill="#182c31", outline="#38564e", width=2)
    draw.text((62, 963), "交付与验证", font=font(28), fill="#9ddbc4")
    lines = [
        "4 道木支架、2 处破拱、8 支壁火把；矿脉和苔痕保留参考图主题。",
        "44 格明水双槽，侧面与底面密封；中央 3 格宽干燥轨道，全程不转弯、不分岔、不升降。",
        "隔离 Forge 47.4.10：4 方向放置、400 tick、6336 条方块记录逐条核对，液体与通路通过。",
        "无脚本奖励、刷怪或预放实体；未部署、未修改玩家存档；非整合包客户端实机截图。",
    ]
    for line_index, line in enumerate(lines):
        draw.text((62, 1014 + line_index * 47), line, font=font(24), fill=TEXT)
    draw.text((34, 1267), f"ACTUAL NBT / Lodestone 0.6.0 软件预览  ·  指纹 {digest[:16]}…", font=font(21), fill=MUTED)
    image.save(ROOT / "preview.png")
    gallery = [
        ("interior-north", "北入口向南 / 原始 NBT"), ("interior-south", "南入口向北 / 原始 NBT"),
        ("interior-east", "东侧内部 / 展示剖切"), ("interior-west", "西侧内部 / 展示剖切"),
        ("exterior-north", "北外视 / 完整结构"), ("exterior-east", "东外视 / 完整结构"),
        ("exterior-south", "南外视 / 完整结构"), ("exterior-west", "西外视 / 完整结构"),
        ("cutaway", "轴测内部 / 展示剖切"), ("plan", "水槽与通道俯视 / 展示剖切"),
    ]
    cards = "".join(f'<figure><a href="{name}.png"><img src="{name}.png" alt="{title}"></a><figcaption>{title}</figcaption></figure>' for name, title in gallery)
    html = f'''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>S20 · 实际 NBT 验收图集</title><style>body{{margin:32px;background:#101a20;color:#dde6d8;font:16px/1.7 system-ui,"Microsoft YaHei",sans-serif}}h1{{color:#e3c18a}}a{{color:#9ddbc4}}main{{display:grid;grid-template-columns:repeat(auto-fit,minmax(440px,1fr));gap:24px}}figure{{margin:0;background:#182c31;padding:12px;border:1px solid #38564e;border-radius:12px}}img{{width:100%;height:auto}}figcaption{{padding:8px 4px}}.hero{{max-width:1200px;display:block}}code{{word-break:break-all}}</style><h1>S20 / 废弃矿道 · 浅水短廊</h1><p>11 × 9 × 16 · 两端 3 × 3 同高接口 · 中央干燥轨道 · 44 格密封积水</p><p><a href="interactive.html">交互 NBT 预览（需运行本地预览服务）</a> · <a href="../README.md">交付说明</a> · <a href="../qa/forge-native.json">Forge 原生验收</a></p><p>以下全部为实际 NBT 的 Lodestone 软件预览，非客户端实机截图。剖切不改变交付 NBT。未部署，不改玩家存档。</p><p><code>NBT SHA-256: 341dbcfa3178fba8d8cf9501309d10fe89a4c1cd39cfcc2e51f9d3879dc4e4c1</code></p><img class="hero" src="../preview.png" alt="S20 交付总览"><main>{cards}</main></html>'''
    html = html.replace("341dbcfa3178fba8d8cf9501309d10fe89a4c1cd39cfcc2e51f9d3879dc4e4c1", digest)
    (PREVIEW / "index.html").write_text(html, encoding="utf-8")
    receipt = {"status": "PASS", "sha256": digest, "derived_from": "qa/views.json", "images": []}
    for name in ("preview.png", "preview/four-interior.png", "preview/four-exterior.png"):
        file = ROOT / name
        with Image.open(file) as rendered:
            receipt["images"].append({"file": name, "size": list(rendered.size), "sha256": hashlib.sha256(file.read_bytes()).hexdigest()})
    (ROOT / "qa/composites.json").write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("S20 overview, four-direction sheets and offline gallery created")


if __name__ == "__main__":
    main()
