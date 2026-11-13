import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
PREVIEW = ROOT / "preview"

def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)

def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    digest = sha256(ROOT / "room.nbt")
    reports = {}
    for report_name in ("static", "forge-native", "views"):
        reports[report_name] = json.loads((ROOT / "qa" / f"{report_name}.json").read_text(encoding="utf-8"))
        assert reports[report_name]["status"] == "PASS" and reports[report_name]["sha256"] == digest
    for kind in ("interior", "exterior"):
        sheet = Image.new("RGB", (2304, 1536), "#131b20")
        for ordinal, direction in enumerate(("north", "east", "south", "west")):
            with Image.open(PREVIEW / f"{kind}-{direction}.png") as image:
                sheet.paste(image.convert("RGB").resize((1152, 768), Image.Resampling.LANCZOS), ((ordinal % 2) * 1152, (ordinal // 2) * 768))
        sheet.save(PREVIEW / f"four-{kind}.png")
    image = Image.new("RGB", (1920, 1500), "#131b20")
    draw = ImageDraw.Draw(image)
    draw.text((34, 24), "zone-R01 / 塌塔基座", font=font(42), fill="#d9be80")
    draw.text((36, 87), "25 × 17 × 25  |  双柱木梁 · 护栏残层 · 三宽实体石阶  |  Minecraft 1.20.1", font=font(24), fill="#b3c0b0")
    with Image.open(PREVIEW / "cutaway.png") as hero:
        image.paste(hero.convert("RGB").crop((0, 88, 1536, 982)).resize((1256, 782), Image.Resampling.LANCZOS), (30, 140))
    for filename, caption, top in (("exterior-north", "北外视 / 完整残墙与接口", 140), ("entrance", "北入口 / 原始 NBT", 540)):
        with Image.open(PREVIEW / f"{filename}.png") as detail:
            image.paste(detail.convert("RGB").crop((0, 88, 1536, 982)).resize((574, 334), Image.Resampling.LANCZOS), (1314, top))
        draw.text((1320, top + 343), caption, font=font(23), fill="#b3c0b0")
    draw.text((38, 941), "轴测展示剖切：只隐藏观察侧墙，不修改交付 NBT。", font=font(25), fill="#dce1d0")
    draw.rounded_rectangle((30, 1004, 1888, 1394), radius=15, fill="#21302a", outline="#607055", width=2)
    draw.text((59, 1026), "实际结构与独立验收", font=font(31), fill="#bcd6a4")
    lines = [
        "北口 [12,2,0]：3宽4高；首层3宽干路与5×5安全落点，双向可达。",
        "24格残层缺口有护栏；九级三宽石阶与落点完整，阶下实心支撑。",
        "静态、四向内外视图、Forge 47.4.10 四方向实际放置与400 tick稳定性通过。",
        "42,500 条放置记录逐条核对；六类缺陷负向探针被拒绝。",
        "偏差：断阶改为安全完整阶；细碎斜梁/残布/垂藤做方块化简化，保留露天残顶。",
        "独立候选未部署；软件预览与隔离服务器证据，不冒充整合包客户端游玩验收。",
    ]
    for ordinal, text in enumerate(lines):
        draw.text((59, 1081 + ordinal * 46), text, font=font(25), fill="#dce1d0")
    draw.text((35, 1430), f"NBT SHA-256  {digest}", font=font(22), fill="#b3c0b0")
    image.save(ROOT / "preview.png")
    cards = "".join(f'<figure><a href="{entry["name"]}.png"><img src="{entry["name"]}.png" alt="{entry["title"]}"></a><figcaption>{entry["title"]}</figcaption></figure>' for entry in reports["views"]["views"])
    gallery = f'''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>zone-R01 塌塔基座 · 验收图集</title><style>body{{margin:32px;background:#131b20;color:#dce1d0;font:16px/1.7 system-ui,"Microsoft YaHei",sans-serif}}h1{{color:#d9be80}}a{{color:#bcd6a4}}main{{display:grid;grid-template-columns:repeat(auto-fit,minmax(440px,1fr));gap:24px}}figure{{margin:0;padding:12px;background:#21302a;border:1px solid #607055;border-radius:12px}}img{{width:100%;height:auto}}.hero{{max-width:1200px;display:block}}code{{word-break:break-all}}</style><h1>zone-R01 / 塌塔基座</h1><p>独立ID，与既有R1不同。25 × 17 × 25 · 北3×4入口 · 双柱残层 · 护栏与安全石阶</p><p><a href="interactive.html">交互预览（需本地服务）</a> · <a href="../README.md">交付说明</a> · <a href="../qa.json">验收索引</a></p><p>所有视图从实际交付NBT渲染；外视保留完整残墙，内视按标注隐藏观察侧墙。真实残顶露天；未部署、未打开玩家存档。</p><p><code>NBT SHA-256: {digest}</code></p><img class="hero" src="../preview.png" alt="实际NBT交付总览"><main>{cards}</main></html>'''
    (PREVIEW / "index.html").write_text(gallery, encoding="utf-8")
    receipt = {"status": "PASS", "id": "zone-R01", "sha256": digest, "images": []}
    for filename in ("preview.png", "preview/four-interior.png", "preview/four-exterior.png"):
        with Image.open(ROOT / filename) as rendered:
            receipt["images"].append({"file": filename, "size": list(rendered.size), "sha256": sha256(ROOT / filename)})
    (ROOT / "qa/composites.json").write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("zone-R01 overview, four-view sheets and offline gallery created")

if __name__ == "__main__":
    main()
