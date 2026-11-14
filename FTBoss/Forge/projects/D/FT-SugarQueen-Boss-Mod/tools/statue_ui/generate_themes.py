import json
import re
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/ftboss/textures/gui/statue_themes"
OUTPUT = ROOT / "_work/statue-ui-restyle-20261024/themes"
FONT = ImageFont.truetype("C:/Windows/Fonts/consola.ttf", 10)


def palettes():
    source = (ROOT / "src/main/java/net/farmingtales/ftboss/client/StatueUiTheme.java").read_text(encoding="utf-8")
    pattern = r'new StatueUiTheme\("([a-z_]+)", ([^\n]+)\)'
    result = []
    for identity, colors in re.findall(pattern, source):
        values = [int(color.strip(), 16) for color in colors.split(",")]
        result.append((identity, dict(zip(("background", "panel", "card", "text", "muted", "accent", "bright", "border"), values))))
    return result


def rgba(color, alpha=255):
    return (color >> 16 & 255, color >> 8 & 255, color & 255, alpha)


def mix(first, second, weight):
    channels = [round((first >> shift & 255) * (1 - weight) + (second >> shift & 255) * weight) for shift in (16, 8, 0)]
    return channels[0] << 16 | channels[1] << 8 | channels[2]


def crest(identity, palette):
    image = Image.new("RGBA", (32, 32))
    draw = ImageDraw.Draw(image)
    ink = "#090E13"
    accent, bright = rgba(palette["accent"]), rgba(palette["bright"])
    border, base = rgba(palette["border"]), rgba(palette["card"])
    ivory = rgba(palette["text"])
    green, red, metal = "#6B965A", "#BA525F", "#A6ADA5"
    draw.polygon(((16, 1), (28, 7), (29, 23), (23, 29), (9, 29), (3, 23), (4, 7)), fill=ink)
    draw.polygon(((16, 2), (27, 8), (28, 22), (22, 28), (10, 28), (4, 22), (5, 8)), fill=border)
    draw.polygon(((16, 4), (25, 9), (26, 21), (21, 26), (11, 26), (6, 21), (7, 9)), fill=base)
    draw.line(((8, 10), (8, 20), (12, 24)), fill=accent)
    draw.point((16, 3), fill=bright)
    if identity == "sugar_queen":
        draw.polygon(((8, 13), (11, 16), (16, 8), (20, 16), (24, 13), (22, 22), (10, 22)), fill=ink)
        draw.polygon(((9, 13), (12, 18), (16, 10), (20, 18), (23, 13), (21, 21), (11, 21)), fill=accent)
        draw.polygon(((16, 10), (19, 18), (16, 20), (14, 18)), fill=bright)
        draw.rectangle((10, 22, 22, 23), fill=ivory)
        draw.point((8, 11), fill=ivory)
        draw.point((24, 11), fill=ivory)
    elif identity == "butcher":
        draw.polygon(((9, 7), (22, 10), (22, 19), (17, 22), (8, 17)), fill=ink)
        draw.polygon(((10, 8), (21, 11), (21, 18), (17, 20), (9, 16)), fill=metal)
        draw.line(((10, 9), (20, 12)), fill=ivory)
        draw.rectangle((14, 18, 17, 25), fill=ink)
        draw.rectangle((15, 20, 16, 24), fill=accent)
        draw.rectangle((18, 12, 19, 13), fill=base)
        draw.line(((10, 15), (17, 18), (19, 17)), fill=red, width=2)
    elif identity == "saya":
        draw.line(((10, 24), (13, 19), (21, 11)), fill=green, width=2)
        draw.polygon(((12, 21), (8, 19), (8, 16), (12, 18)), fill=accent)
        draw.polygon(((19, 20), (23, 16), (24, 19), (20, 22)), fill=accent)
        for center in ((16, 10), (12, 14), (20, 14), (14, 18), (18, 18)):
            draw.rectangle((center[0] - 2, center[1] - 2, center[0] + 2, center[1] + 2), fill=ivory)
        draw.rectangle((14, 13, 18, 16), fill=bright)
        draw.rectangle((15, 14, 17, 15), fill="#CBB56E")
        draw.point((23, 21), fill=ivory)
    elif identity == "wine_barrel":
        draw.polygon(((10, 8), (21, 8), (24, 12), (24, 21), (21, 25), (10, 25), (7, 21), (7, 12)), fill=ink)
        draw.polygon(((11, 9), (20, 9), (22, 12), (22, 21), (20, 24), (11, 24), (9, 21), (9, 12)), fill="#795336")
        draw.line(((13, 10), (12, 22)), fill="#A77742")
        draw.line(((18, 10), (19, 22)), fill="#4E3429")
        draw.rectangle((9, 12, 22, 14), fill=accent)
        draw.rectangle((9, 20, 22, 22), fill=accent)
        draw.line(((10, 12), (21, 12)), fill=bright)
        for center in ((15, 16), (18, 16), (16, 19)):
            draw.rectangle((center[0], center[1], center[0] + 2, center[1] + 2), fill="#A44C75")
        draw.point((15, 15), fill=green)
    elif identity == "rat_swarm":
        draw.polygon(((8, 20), (10, 10), (22, 10), (25, 21), (18, 24)), fill=ink)
        draw.polygon(((9, 19), (11, 11), (21, 11), (24, 20), (18, 22)), fill=accent)
        draw.polygon(((11, 11), (21, 11), (20, 15), (9, 19)), fill=bright)
        draw.rectangle((21, 12, 24, 14), fill=base)
        draw.rectangle((22, 15, 25, 16), fill=base)
        for box in ((12, 14, 14, 15), (17, 17, 19, 19), (11, 19, 12, 20)):
            draw.rectangle(box, fill=border)
        draw.line(((17, 24), (22, 25), (26, 22), (26, 19)), fill="#C58F87")
    elif identity == "defect":
        for offset in (10, 15, 20):
            draw.line(((offset, 7), (offset, 10)), fill=bright)
            draw.line(((offset, 22), (offset, 25)), fill=bright)
            draw.line(((6, offset), (10, offset)), fill=accent)
            draw.line(((22, offset), (26, offset)), fill=accent)
        draw.rectangle((10, 10, 22, 22), fill=ink)
        draw.rectangle((11, 11, 21, 21), fill=border)
        draw.rectangle((13, 13, 19, 19), fill=accent)
        draw.line(((16, 12), (14, 16), (18, 16), (16, 20)), fill=ivory, width=2)
        draw.rectangle((23, 7, 25, 9), fill=bright)
    elif identity == "compost":
        draw.rectangle((13, 17, 18, 24), fill=ivory)
        draw.rectangle((11, 23, 20, 24), fill=accent)
        draw.polygon(((7, 17), (9, 12), (13, 9), (19, 9), (23, 12), (25, 17)), fill=ink)
        draw.polygon(((8, 16), (10, 12), (13, 10), (19, 10), (22, 12), (24, 16)), fill=bright)
        draw.rectangle((10, 17, 22, 18), fill="#785B76")
        for point in ((12, 13), (17, 12), (21, 15)):
            draw.rectangle((point[0], point[1], point[0] + 1, point[1] + 1), fill=ivory)
        draw.point((8, 10), fill=accent)
        draw.point((23, 7), fill=bright)
        draw.point((25, 11), fill=bright)
        draw.line(((9, 23), (7, 20), (10, 21)), fill=accent)
    elif identity == "apple":
        draw.polygon(((16, 11), (11, 10), (7, 14), (8, 21), (13, 25), (18, 25), (23, 20), (24, 14), (20, 10)), fill=ink)
        draw.polygon(((16, 12), (11, 11), (8, 15), (9, 20), (13, 24), (18, 24), (22, 20), (23, 15), (20, 11)), fill=red)
        draw.line(((11, 13), (10, 16), (10, 19)), fill=accent, width=2)
        draw.rectangle((15, 7, 16, 11), fill=bright)
        draw.polygon(((17, 9), (18, 6), (23, 6), (21, 9)), fill=green)
        draw.line(((18, 8), (21, 7)), fill="#AFC17A")
        draw.point((15, 23), fill=bright)
    elif identity == "tide_fish":
        draw.polygon(((7, 12), (11, 13), (15, 8), (19, 12), (23, 14), (25, 11), (25, 22), (21, 20), (16, 23), (10, 20), (7, 17)), fill=ink)
        draw.polygon(((8, 14), (11, 14), (15, 10), (18, 13), (22, 15), (24, 13), (24, 20), (21, 18), (16, 21), (11, 18), (8, 17)), fill=accent)
        draw.line(((11, 16), (17, 16), (21, 17)), fill=bright, width=2)
        draw.point((11, 14), fill=ivory)
        draw.point((12, 14), fill=ink)
        draw.line(((8, 23), (12, 24), (17, 24), (21, 23)), fill=bright)
        draw.rectangle((20, 8, 22, 10), fill=ivory)
    elif identity == "benger":
        for box in ((8, 10, 11, 13), (13, 7, 16, 11), (19, 8, 22, 12), (23, 12, 26, 15)):
            draw.rectangle(box, fill=ink)
            draw.rectangle((box[0], box[1], box[2] - 1, box[3] - 1), fill=ivory)
        draw.polygon(((11, 20), (14, 14), (19, 14), (22, 20), (21, 23), (18, 23), (16, 22), (13, 23), (10, 22)), fill=ink)
        draw.polygon(((12, 20), (15, 15), (18, 15), (21, 20), (20, 22), (17, 21), (13, 22), (11, 21)), fill=bright)
        draw.line(((12, 25), (21, 25)), fill=accent)
    else:
        draw.polygon(((8, 14), (16, 7), (24, 14)), fill=accent)
        draw.line(((8, 15), (24, 15)), fill=bright)
        draw.rectangle((9, 16, 12, 23), fill=ivory)
        draw.rectangle((19, 16, 22, 23), fill=ivory)
        draw.rectangle((14, 17, 17, 23), fill=border)
        draw.rectangle((8, 24, 24, 25), fill=accent)
        draw.point((16, 11), fill=ivory)
    return image


def panel(identity, palette):
    image = Image.new("RGBA", (32, 32))
    draw = ImageDraw.Draw(image)
    ink = rgba(palette["background"])
    accent, bright, border = (rgba(palette[key]) for key in ("accent", "bright", "border"))
    cut = {"neutral": 3, "sugar_queen": 5, "butcher": 1, "saya": 4, "wine_barrel": 4,
           "rat_swarm": 2, "defect": 5, "compost": 3, "apple": 3, "tide_fish": 5, "benger": 2}[identity]
    draw.polygon(((cut, 0), (31 - cut, 0), (31, cut), (31, 31 - cut), (31 - cut, 31),
                  (cut, 31), (0, 31 - cut), (0, cut)), fill=ink)
    draw.polygon(((cut, 1), (31 - cut, 1), (30, cut), (30, 31 - cut), (31 - cut, 30),
                  (cut, 30), (1, 31 - cut), (1, cut)), fill=border)
    draw.rectangle((3, 3, 28, 28), fill=rgba(palette["panel"]))
    draw.rectangle((5, 5, 26, 26), outline=ink)
    draw.line(((6, 2), (25, 2)), fill=accent)
    draw.line(((6, 29), (25, 29)), fill=border)
    corner = Image.new("RGBA", (6, 6))
    detail = ImageDraw.Draw(corner)
    if identity == "sugar_queen":
        detail.polygon(((1, 5), (4, 1), (5, 4), (4, 5)), fill=bright)
        detail.line(((4, 2), (4, 5)), fill=accent)
    elif identity == "butcher":
        detail.rectangle((1, 1, 5, 5), fill=border)
        detail.line(((1, 1), (5, 1)), fill="#B2AAA0")
        detail.rectangle((3, 3, 4, 4), fill=bright)
    elif identity == "saya":
        detail.line(((1, 5), (3, 3), (5, 2)), fill=accent)
        detail.polygon(((1, 3), (3, 1), (3, 3)), fill=bright)
        detail.point((4, 4), fill=accent)
    elif identity == "wine_barrel":
        detail.rectangle((1, 3, 5, 4), fill=accent)
        detail.rectangle((3, 1, 4, 5), fill=accent)
        detail.point((3, 3), fill=bright)
    elif identity == "rat_swarm":
        detail.polygon(((1, 5), (1, 2), (5, 1), (5, 5)), fill=accent)
        detail.point((2, 3), fill=ink)
        detail.point((4, 2), fill=border)
    elif identity == "defect":
        detail.line(((1, 5), (3, 3), (5, 3)), fill=accent)
        detail.rectangle((3, 1, 4, 2), fill=bright)
        detail.point((5, 5), fill=accent)
    elif identity == "compost":
        detail.rectangle((1, 3, 3, 5), fill=accent)
        detail.rectangle((3, 1, 4, 2), fill=bright)
        detail.point((5, 4), fill=bright)
    elif identity == "apple":
        detail.line(((1, 5), (3, 3), (5, 2)), fill=bright)
        detail.polygon(((1, 3), (2, 1), (4, 1), (3, 3)), fill="#7A9955")
        detail.point((4, 4), fill=accent)
    elif identity == "tide_fish":
        detail.line(((1, 5), (1, 4), (4, 1), (5, 1)), fill=accent)
        detail.rectangle((3, 3, 4, 4), fill=bright)
    elif identity == "benger":
        detail.rectangle((1, 1, 5, 5), fill=border)
        detail.line(((2, 2), (4, 2), (4, 4)), fill=bright)
        detail.point((2, 4), fill=accent)
    else:
        detail.line(((1, 5), (1, 2), (2, 1), (5, 1)), fill=accent)
        detail.rectangle((3, 3, 4, 4), fill=bright)
    image.alpha_composite(corner, (0, 0))
    image.alpha_composite(corner.transpose(Image.Transpose.FLIP_LEFT_RIGHT), (26, 0))
    image.alpha_composite(corner.transpose(Image.Transpose.FLIP_TOP_BOTTOM), (0, 26))
    image.alpha_composite(corner.transpose(Image.Transpose.ROTATE_180), (26, 26))
    if identity == "butcher":
        for coordinate in range(7, 26, 4):
            draw.rectangle((coordinate, 3, coordinate + 1, 4), fill=border)
    elif identity == "wine_barrel":
        for coordinate in range(9, 25, 6):
            draw.point((coordinate, 3), fill=rgba(0x9A7249))
    elif identity == "defect":
        draw.line(((8, 3), (11, 3), (12, 4), (16, 4)), fill=border)
        draw.point((17, 4), fill=accent)
    elif identity == "tide_fish":
        for coordinate in range(8, 25, 6):
            draw.line(((coordinate, 3), (coordinate + 1, 4), (coordinate + 2, 3)), fill=border)
    elif identity == "benger":
        for coordinate in range(8, 25, 4):
            draw.rectangle((coordinate, 3, coordinate + 1, 3), fill=border)
    return image


def card(palette, selected):
    image = Image.new("RGBA", (16, 16))
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 15, 15), fill=rgba(palette["background"]))
    draw.rectangle((1, 1, 14, 14), outline=rgba(palette["accent"] if selected else palette["border"]))
    draw.rectangle((2, 2, 13, 13), fill=rgba(palette["card"]))
    draw.line(((3, 2), (12, 2)), fill=rgba(mix(palette["card"], palette["accent"], 0.22)))
    if selected:
        draw.line(((2, 3), (2, 12)), fill=rgba(palette["bright"]))
        draw.point((13, 13), fill=rgba(palette["accent"]))
    return image


def button(identity, palette, state):
    image = Image.new("RGBA", (64, 20))
    draw = ImageDraw.Draw(image)
    disabled, focused, hovered = state == 3, state == 2, state == 1
    edge = palette["border"] if disabled or state == 0 else palette["accent"]
    fill = mix(palette["panel"], palette["card"], 0.5) if disabled else mix(palette["card"], palette["accent"], 0.15 if hovered or focused else 0)
    draw.rectangle((1, 0, 62, 19), fill=rgba(palette["background"]))
    draw.rectangle((0, 1, 63, 18), fill=rgba(palette["background"]))
    draw.rectangle((1, 1, 62, 18), outline=rgba(edge))
    draw.rectangle((2, 2, 61, 17), fill=rgba(fill))
    draw.line(((3, 2), (60, 2)), fill=rgba(mix(fill, edge, 0.5)))
    draw.line(((3, 17), (60, 17)), fill=rgba(palette["background"]))
    marker = rgba(palette["border"] if disabled else palette["bright"])
    if identity in ("sugar_queen", "saya", "tide_fish"):
        draw.point((2, 3), fill=marker)
        draw.point((61, 16), fill=marker)
    elif identity in ("butcher", "wine_barrel", "benger"):
        draw.line(((2, 3), (2, 4)), fill=marker)
        draw.line(((61, 15), (61, 16)), fill=marker)
    elif identity in ("compost", "apple", "rat_swarm"):
        draw.point((2, 4), fill=marker)
        draw.point((61, 15), fill=marker)
    else:
        draw.line(((2, 3), (2, 5)), fill=marker)
        draw.point((61, 16), fill=marker)
    if focused:
        for coordinate in range(5, 59, 4):
            draw.point((coordinate, 0), fill=rgba(palette["bright"]))
            draw.point((coordinate, 19), fill=rgba(palette["bright"]))
    if disabled:
        draw.line(((3, 18), (60, 18)), fill=rgba(palette["border"]))
    return image


def nine_slice(tile, size, inset):
    width, height = size
    source_width, source_height = tile.size
    left = min(inset, width // 2)
    right = min(inset, width // 2)
    top = min(inset, height // 2)
    bottom = min(inset, height // 2)
    output = Image.new("RGBA", size)
    source_x = (0, left, source_width - right, source_width)
    source_y = (0, top, source_height - bottom, source_height)
    target_x = (0, left, width - right, width)
    target_y = (0, top, height - bottom, height)
    for row in range(3):
        for column in range(3):
            target_width = target_x[column + 1] - target_x[column]
            target_height = target_y[row + 1] - target_y[row]
            if target_width <= 0 or target_height <= 0:
                continue
            piece = tile.crop((source_x[column], source_y[row], source_x[column + 1], source_y[row + 1]))
            for offset_y in range(0, target_height, piece.height):
                for offset_x in range(0, target_width, piece.width):
                    stamp = piece.crop((0, 0, min(piece.width, target_width - offset_x), min(piece.height, target_height - offset_y)))
                    output.alpha_composite(stamp, (target_x[column] + offset_x, target_y[row] + offset_y))
    return output


def preview(identity, palette, atlas, emblem):
    image = Image.new("RGBA", (324, 160), "#0A0D12")
    image.alpha_composite(nine_slice(atlas.crop((0, 0, 32, 32)), (312, 148), 6), (6, 6))
    image.alpha_composite(emblem, (17, 16))
    draw = ImageDraw.Draw(image)
    draw.text((57, 16), identity.upper().replace("_", " "), fill=rgba(palette["text"]), font=FONT)
    draw.text((57, 31), "STATUE WORKSHOP / 10 BOSSES", fill=rgba(palette["muted"]), font=FONT)
    draw.line(((18, 51), (305, 51)), fill=rgba(palette["border"]))
    for index in range(2):
        tile = atlas.crop((60 if index == 0 else 40, 0, 76 if index == 0 else 56, 16))
        image.alpha_composite(nine_slice(tile, (140, 42), 3), (18 + index * 148, 60))
    draw.text((28, 68), "SELECTED", fill=rgba(palette["text"]), font=FONT)
    draw.text((28, 82), "Quiet reading field", fill=rgba(palette["muted"]), font=FONT)
    draw.text((176, 68), "INSET CARD", fill=rgba(palette["text"]), font=FONT)
    draw.text((176, 82), "Items / details", fill=rgba(palette["muted"]), font=FONT)
    for state, label in enumerate(("READY", "HOVER", "FOCUS", "LOCKED")):
        tile = atlas.crop((0, 40 + state * 20, 64, 60 + state * 20))
        image.alpha_composite(nine_slice(tile, (68, 20), 3), (18 + state * 74, 114))
        draw.text((20 + state * 74 + (64 - draw.textlength(label, font=FONT)) // 2, 118), label,
                  fill=rgba(palette["muted"] if state == 3 else palette["text"]), font=FONT)
    return image


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    theme_palettes = palettes()
    sheet = Image.new("RGB", (972, 640), "#0A0D12")
    records = []
    for index, (identity, palette) in enumerate(theme_palettes):
        directory = ASSETS / identity
        directory.mkdir(parents=True, exist_ok=True)
        atlas = Image.new("RGBA", (256, 256))
        atlas.alpha_composite(panel(identity, palette))
        atlas.alpha_composite(card(palette, False), (40, 0))
        atlas.alpha_composite(card(palette, True), (60, 0))
        for state in range(4):
            atlas.alpha_composite(button(identity, palette, state), (0, 40 + state * 20))
        emblem = crest(identity, palette)
        atlas.save(directory / "panels.png", optimize=True)
        emblem.save(directory / "crest.png", optimize=True)
        sample = preview(identity, palette, atlas, emblem)
        sample.save(OUTPUT / (identity + "_preview.png"))
        sheet.paste(sample.convert("RGB"), (index % 3 * 324, index // 3 * 160))
        records.append({"id": identity, "palette": palette})
    sheet.resize((1944, 1280), Image.Resampling.NEAREST).save(OUTPUT / "theme_contact_sheet.png")
    (OUTPUT / "theme_manifest.json").write_text(json.dumps(records, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(records)} themes / {len(records) * 2} textures")
    print(OUTPUT / "theme_contact_sheet.png")


if __name__ == "__main__":
    main()
