import argparse
import io
import json
from pathlib import Path
import re
import zipfile

from PIL import Image, ImageDraw, ImageFont


PROJECT = Path(__file__).resolve().parents[1]
ASSETS = PROJECT / "src/main/resources/assets/qys_event"
SIZE = (300, 236)
GOLD = "#e8cb88"
TEXT = "#dce5cc"
MUTED = "#8ca79a"


def panel(draw, bounds, fill, edge):
    draw.rectangle(bounds, fill=fill, outline=edge)
    left, top, right, bottom = bounds
    draw.line((left + 1, top + 1, right - 1, top + 1), fill="#345448")
    draw.point((left, top), fill="#101e1b")
    draw.point((right, top), fill="#101e1b")
    draw.point((left, bottom), fill="#101e1b")
    draw.point((right, bottom), fill="#101e1b")


def star(draw, center, color, radius=2):
    left, top = center
    draw.line((left - radius, top, left + radius, top), fill=color)
    draw.line((left, top - radius, left, top + radius), fill=color)


def background():
    image = Image.new("RGBA", SIZE, (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.rectangle((2, 2, 297, 233), fill="#0c1816", outline="#090f0e", width=2)
    draw.rectangle((4, 4, 295, 231), fill="#182d27", outline="#a48b57")
    draw.rectangle((6, 6, 293, 229), outline="#364d3c")
    draw.rectangle((7, 7, 292, 33), fill="#213a30")
    draw.line((12, 35, 287, 35), fill="#665c3c")
    draw.line((12, 36, 287, 36), fill="#0f201c")
    panel(draw, (231, 10, 286, 25), "#314c34", "#6b8153")
    panel(draw, (13, 41, 113, 225), "#142821", "#3d5843")
    draw.rectangle((17, 45, 109, 127), fill="#112722")
    draw.rectangle((18, 46, 108, 125), outline="#213e33")
    draw.line((21, 130, 105, 130), fill="#72633f")
    draw.line((21, 190, 105, 190), fill="#39503c")
    panel(draw, (20, 158, 106, 187), "#203c2c", "#455c3c")
    for center, radius, color in [((28, 60), 2, "#8caa7a"), ((96, 58), 2, GOLD),
                                   ((36, 79), 1, "#52846c"), ((92, 86), 1, "#90b793"),
                                   ((64, 47), 2, "#e3dfaa"), ((101, 106), 1, "#799b73")]:
        star(draw, center, color, radius)
    draw.ellipse((84, 45, 94, 55), fill="#a8bea0")
    draw.ellipse((81, 43, 90, 52), fill="#112722")
    draw.polygon(((24, 112), (63, 100), (103, 113), (63, 127)), fill="#0b1d19")
    draw.polygon(((29, 108), (63, 96), (98, 108), (63, 122)), fill="#425c48")
    draw.polygon(((29, 108), (63, 119), (98, 108), (98, 113), (63, 125), (29, 113)), fill="#293f35")
    draw.polygon(((34, 97), (63, 88), (92, 97), (92, 109), (63, 120), (34, 109)), fill="#738171")
    draw.polygon(((34, 97), (63, 106), (63, 120), (34, 109)), fill="#52695a")
    draw.polygon(((63, 106), (92, 97), (92, 109), (63, 120)), fill="#3d574b")
    draw.polygon(((34, 97), (63, 87), (92, 97), (63, 108)), fill="#a5ad86")
    draw.polygon(((42, 97), (63, 91), (84, 97), (63, 104)), fill="#163d3b")
    draw.line((45, 98, 63, 103, 81, 98), fill="#5bbba1")
    draw.line((53, 98, 62, 100, 74, 97), fill="#a0e2bb")
    for left, top in [(37, 72), (84, 72)]:
        draw.rectangle((left, top, left + 5, 98), fill="#7c8b6e")
        draw.rectangle((left + 3, top, left + 6, 98), fill="#435f4c")
        draw.line((left, top + 12, left + 5, top + 12), fill="#304c3c")
        draw.rectangle((left - 1, top - 2, left + 7, top + 1), fill="#b2b28b")
    draw.polygon(((27, 72), (63, 53), (100, 72), (63, 87)), fill="#364e39")
    draw.polygon(((27, 68), (63, 49), (100, 68), (63, 82)), fill="#85a27a")
    draw.polygon(((27, 68), (63, 80), (63, 84), (27, 72)), fill="#507457")
    draw.polygon(((63, 80), (100, 68), (100, 72), (63, 84)), fill="#2f5743")
    draw.line((30, 68, 63, 51, 97, 68), fill="#bfca96", width=1)
    draw.line((45, 63, 78, 76), fill="#5f825c")
    draw.line((53, 58, 87, 71), fill="#5f825c")
    draw.polygon(((55, 61), (63, 57), (71, 61), (63, 65)), fill="#b2ecaf")
    draw.polygon(((55, 61), (63, 65), (63, 72), (55, 68)), fill="#459e73")
    draw.polygon(((63, 65), (71, 61), (71, 68), (63, 72)), fill="#24765c")
    draw.line((63, 84, 63, 92), fill="#a38b56")
    draw.rectangle((60, 89, 66, 94), fill="#dbbc74")
    draw.rectangle((62, 90, 64, 92), fill="#fff0b2")
    star(draw, (64, 99), "#d0f0c0", 2)
    for left, top in [(23, 105), (96, 114), (31, 120)]:
        draw.line((left, top + 5, left + 3, top - 4), fill="#597553")
        draw.rectangle((left - 2, top, left + 1, top + 1), fill="#73905c")
        draw.rectangle((left + 2, top - 3, left + 5, top - 2), fill="#8aa565")
    for product in range(11):
        left = 123 + product % 6 * 26
        top = 54 + product // 6 * 24
        panel(draw, (left, top, left + 21, top + 21), "#253e32", "#61724a")
        draw.rectangle((left + 2, top + 2, left + 19, top + 19), outline="#152b24")
    star(draw, (263, 88), "#465c41", 3)
    for left in (124, 208):
        panel(draw, (left, 115, left + 77, 140), "#2c3e30", "#9c8551")
        draw.line((left + 24, 119, left + 24, 136), fill="#566343")
    for row in range(4):
        top = 152 + row * 18 if row < 3 else 210
        for column in range(9):
            left = 125 + column * 18
            draw.rectangle((left, top, left + 17, top + 17), fill="#0d201b", outline="#3e5946")
            draw.line((left + 1, top + 1, left + 16, top + 1), fill="#081713")
            draw.line((left + 1, top + 16, left + 16, top + 16), fill="#52664a")
    for left, top in [(5, 5), (290, 5), (5, 226), (290, 226)]:
        draw.rectangle((left, top, left + 4, top + 4), fill="#685b3a")
        draw.point((left + 2, top + 2), fill="#eed599")
    return image


def preview(image, destination, instance):
    scale = 4
    result = image.resize((SIZE[0] * scale, SIZE[1] * scale), Image.Resampling.NEAREST)
    draw = ImageDraw.Draw(result)
    labels = json.loads((ASSETS / "lang/zh_cn.json").read_text(encoding="utf-8"))
    source = (PROJECT / "src/main/java/cn/farmingtales/qysevent/client/WishingWellShopScreen.java").read_text(encoding="utf-8")
    pattern = r'(smallLabel|label)\(graphics, "([a-z_]+)", (\d+), (\d+), (GOLD|TEXT|MUTED|0x[0-9A-Fa-f]+)(?:, ([0-9.]+)F)?\);'
    for method, key, left, top, color, factor in re.findall(pattern, source):
        factor = float(factor or 1)
        font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", round(9 * scale * factor))
        color = {"GOLD": GOLD, "TEXT": TEXT, "MUTED": MUTED}.get(color, color.replace("0x", "#"))
        draw.text((int(left) * scale, int(top) * scale), labels["screen.qys_event.wishing_well." + key], font=font, fill=color, anchor="lt")
    if instance:
        script = (PROJECT / "integration/kubejs/server_scripts/block_events/wishing_well.js").read_text(encoding="utf-8")
        summons = re.findall(r"'ftboss:([^']+)'", script.split("const FTWW_SUMMONS = [", 1)[1].split("]", 1)[0])
        with zipfile.ZipFile(instance / "mods/qys-bosses.jar") as archive:
            for product, name in enumerate(summons):
                model_path = "assets/ftboss/models/item/" + name + ".json"
                model = json.loads(archive.read(model_path))
                textures = model.get("textures", {})
                texture = textures.get("layer0") or textures.get("particle")
                if not texture:
                    continue
                namespace, path = texture.split(":")
                icon = Image.open(io.BytesIO(archive.read("assets/" + namespace + "/textures/" + path + ".png"))).convert("RGBA")
                icon = icon.crop((0, 0, icon.width, min(icon.width, icon.height))).resize((64, 64), Image.Resampling.NEAREST)
                result.alpha_composite(icon, ((126 + product % 6 * 26) * scale, (57 + product // 6 * 24) * scale))
        with zipfile.ZipFile(instance / (instance.name + ".jar")) as archive:
            icon = Image.open(io.BytesIO(archive.read("assets/minecraft/textures/item/netherite_sword.png"))).convert("RGBA")
            result.alpha_composite(icon.resize((64, 64), Image.Resampling.NEAREST), (130 * scale, 120 * scale))
        spellbooks = next((instance / "mods").glob("*irons_spellbooks*.jar"))
        with zipfile.ZipFile(spellbooks) as archive:
            icon = Image.open(io.BytesIO(archive.read("assets/irons_spellbooks/textures/item/scroll.png"))).convert("RGBA")
            result.alpha_composite(icon.resize((64, 64), Image.Resampling.NEAREST), (214 * scale, 120 * scale))
        draw = ImageDraw.Draw(result)
        draw.polygon([(952, 324), (936, 344), (936, 368), (952, 380), (968, 368), (968, 344)], fill="#b9c69a", outline="#50675a", width=4)
        draw.rectangle((944, 340, 952, 348), fill="#61785e")
        draw.rectangle((956, 356, 964, 364), fill="#738960")
    destination.parent.mkdir(parents=True, exist_ok=True)
    result.save(destination)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", type=Path)
    parser.add_argument("--instance", type=Path)
    args = parser.parse_args()
    image = background()
    destination = ASSETS / "textures/gui/wishing_well_shop.png"
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination)
    if args.preview:
        preview(image, args.preview, args.instance)
    print(destination)


if __name__ == "__main__":
    main()
