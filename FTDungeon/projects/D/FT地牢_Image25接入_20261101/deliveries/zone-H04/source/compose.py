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
    return Image.open(PREVIEW / (name + ".png")).convert("RGB").crop((0, 96, 1600, 1056))


def main():
    digest = hashlib.sha256((ROOT / "room.nbt").read_bytes()).hexdigest()
    for name in ("static.json", "forge-native.json", "views.json"):
        report = json.loads((ROOT / "qa" / name).read_text(encoding="utf-8"))
        assert report["status"] == "PASS" and report["sha256"] == digest
    for kind in ("interior", "exterior", "eye"):
        sheet = Image.new("RGB", (2560, 1760), BACKGROUND)
        for ordinal, direction in enumerate(("north", "east", "south", "west")):
            tile = Image.open(PREVIEW / (kind + "-" + direction + ".png")).convert("RGB")
            assert tile.size == (1600, 1100)
            sheet.paste(tile.resize((1280, 880), Image.Resampling.LANCZOS), ((ordinal % 2) * 1280, (ordinal // 2) * 880))
        sheet.save(PREVIEW / ("four-" + kind + ".png"))
    image = Image.new("RGB", (2080, 1560), BACKGROUND)
    draw = ImageDraw.Draw(image)
    draw.text((34, 20), "H04 / 柱列中庭", font=font(42), fill="#e3c18a")
    draw.text((36, 82), "33 × 23 × 37  |  四根落地柱 · 双梯回廊 · 密封喷泉 · 完整天窗屋顶", font=font(25), fill=MUTED)
    image.paste(content("interior-north").resize((1320, 792), Image.Resampling.LANCZOS), (32, 138))
    draw.text((42, 938), "北向内视 / 只在展示中移除北墙与屋顶", font=font(23), fill=TEXT)
    image.paste(content("cutaway").resize((664, 398), Image.Resampling.LANCZOS), (1384, 138))
    draw.text((1392, 547), "实际 NBT 轴测剖视", font=font(23), fill=MUTED)
    image.paste(content("exterior-north").resize((664, 398), Image.Resampling.LANCZOS), (1384, 588))
    draw.text((1392, 997), "外视 / 屋顶完整，入口清空", font=font(23), fill=MUTED)
    draw.rounded_rectangle((32, 1060, 2048, 1474), radius=14, fill="#182c31", outline="#38564e", width=2)
    draw.text((62, 1080), "结构与验收", font=font(30), fill="#9ddbc4")
    lines = [
        "四根 3×3 石柱 / 独立 5×5 柱基和柱头 / 连续梁柱衔接；双梯各 3 格宽、8 级。",
        "北入口 [16,2,0] → 安全点 [16,2,7]；四向 3×4 入口连通，喷泉外保留环行通路。",
        "40 格水源封闭在池内；上层溢流造型用青色玻璃表达；26 盏灯全部落地或连顶。",
        "Forge 47.4.10：四旋转实际放置 / 112332 条记录 / 邻居与流体更新后 400 tick 无变化。",
        "原生 0.6×1.8 玩家碰撞与楼梯采样通过；缺柱、断灯链、破水坝、破屋顶等 6 项故障检出。",
        "仅独立交付：未部署，未写正式实例或玩家存档；非完整整合包客户端实机验收。",
    ]
    for ordinal, line in enumerate(lines):
        draw.text((62, 1132 + ordinal * 51), line, font=font(24), fill=TEXT)
    draw.text((36, 1504), "ACTUAL NBT / LODESTONE SOFTWARE RENDER · SHA-256 " + digest[:24] + "…", font=font(22), fill=MUTED)
    image.save(ROOT / "preview.png")
    gallery = []
    for kind, label in (("exterior", "完整外视"), ("interior", "展示剖切内视"), ("eye", "原模板入口内视")):
        for direction, translation in (("north", "北"), ("east", "东"), ("south", "南"), ("west", "西")):
            gallery.append((kind + "-" + direction, translation + " / " + label))
    gallery.extend((("cutaway", "轴测剖视"), ("plan", "地面俯视")))
    cards = "".join('<figure><a href="' + name + '.png"><img src="' + name + '.png" alt="' + title + '"></a><figcaption>' + title + '</figcaption></figure>' for name, title in gallery)
    html = '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>H04 柱列中庭 · NBT 验收图集</title><style>body{margin:32px;background:#101a20;color:#dde6d8;font:16px/1.7 system-ui,"Microsoft YaHei",sans-serif}h1{color:#e3c18a}a{color:#9ddbc4}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(440px,1fr));gap:24px}figure{margin:0;background:#182c31;padding:12px;border:1px solid #38564e;border-radius:12px}img{width:100%;height:auto}figcaption{padding:8px}.hero{max-width:1400px;display:block}code{word-break:break-all}</style><h1>H04 / 柱列中庭</h1><p>33 × 23 × 37 · 四柱双梯 · 中央封闭水池 · 四向入口 · 完整屋顶</p><p><a href="interactive.html">交互 NBT 预览（需启动本地服务）</a> · <a href="../README.md">交付说明</a> · <a href="../qa/forge-native.json">Forge 原生验收</a></p><p>全部图片来自实际 room.nbt；Lodestone 软件渲染，非游戏实机截图。剖切只用于预览。未部署，不修改正式实例。</p><p><code>NBT SHA-256: ' + digest + '</code></p><img class="hero" src="../preview.png" alt="H04 交付总览"><main>' + cards + '</main></html>'
    (PREVIEW / "index.html").write_text(html, encoding="utf-8")
    report = {"status": "PASS", "sha256": digest, "images": []}
    for name in ("preview.png", "preview/four-interior.png", "preview/four-exterior.png", "preview/four-eye.png"):
        asset = ROOT / name
        with Image.open(asset) as rendered:
            report["images"].append({"file": name, "size": list(rendered.size), "sha256": hashlib.sha256(asset.read_bytes()).hexdigest()})
    (ROOT / "qa/composites.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("H04 overview, three four-direction sheets and offline gallery created")


if __name__ == "__main__":
    main()
