import json
import math

from PIL import Image, ImageDraw

from build_assets import Model, PALETTES, ROOT, make_atlas, tile_origin


NAME = "wishing_well_core"
NAMESPACE = "farmingtales"
ASSETS = ROOT / "payload/assets" / NAMESPACE


def textures():
    PALETTES[NAME] = [
        (93, 105, 104), (79, 92, 87), (127, 114, 73), (202, 162, 77),
        (59, 123, 99), (186, 202, 176), (26, 61, 53), (40, 105, 79),
        (147, 175, 135), (83, 109, 99), (88, 154, 121), (49, 170, 112),
        (32, 104, 76), (54, 98, 76), (75, 165, 117), (79, 110, 85),
    ]
    make_atlas(NAME, NAMESPACE)
    atlas_path = ASSETS / "textures/block" / f"{NAME}_atlas.png"
    atlas = Image.open(atlas_path).convert("RGBA")
    for material in ("stone", "wood", "crystal", "inlay", "rune", "eye"):
        left, top = tile_origin(material)
        tile = atlas.crop((left, top, left + 32, top + 32))
        draw = ImageDraw.Draw(tile)
        if material == "stone":
            for row in (8, 24):
                draw.line([(0, row), (31, row)], fill=(58, 73, 70))
                draw.line([(0, row + 1), (31, row + 1)], fill=(122, 133, 125))
            for column, start, end in ((11, 0, 8), (24, 9, 24), (8, 25, 31)):
                draw.line([(column, start), (column, end)], fill=(58, 73, 70))
            draw.rectangle((2, 4, 6, 5), fill=(116, 128, 118))
            draw.rectangle((17, 20, 21, 21), fill=(71, 96, 73))
        elif material == "wood":
            draw.rectangle((0, 0, 31, 31), fill=(84, 99, 91))
            draw.rectangle((1, 1, 30, 30), outline=(119, 136, 119))
            draw.rectangle((4, 4, 27, 27), outline=(58, 77, 69), width=2)
            draw.line([(7, 24), (16, 16), (24, 8)], fill=(142, 157, 130), width=2)
            for column, row in ((8, 22), (13, 17), (19, 11)):
                draw.polygon([(column, row), (column - 3, row - 4), (column + 1, row - 3)], fill=(124, 147, 120))
                draw.polygon([(column, row), (column + 4, row + 1), (column + 3, row + 4)], fill=(102, 131, 101))
        elif material == "crystal":
            draw.rectangle((0, 0, 31, 31), fill=(43, 155, 102))
            draw.polygon([(0, 0), (12, 0), (17, 16), (0, 28)], fill=(72, 197, 137))
            draw.polygon([(12, 0), (31, 0), (17, 16)], fill=(116, 226, 164))
            draw.polygon([(31, 0), (31, 31), (17, 16)], fill=(34, 125, 85))
            draw.polygon([(0, 28), (17, 16), (31, 31), (0, 31)], fill=(39, 146, 95))
            draw.line([(12, 0), (17, 16), (31, 31)], fill=(162, 246, 189))
            draw.rectangle((5, 6, 12, 7), fill=(218, 255, 221))
            draw.rectangle((8, 3, 9, 10), fill=(218, 255, 221))
        elif material == "inlay":
            draw.rectangle((0, 0, 31, 31), fill=(29, 89, 66))
            draw.polygon([(9, 1), (22, 1), (30, 9), (30, 22), (22, 30), (9, 30), (1, 22), (1, 9)], outline=(177, 153, 86), width=2)
            draw.polygon([(11, 6), (20, 6), (25, 11), (25, 20), (20, 25), (11, 25), (6, 20), (6, 11)], outline=(68, 151, 107))
            for angle in range(0, 360, 45):
                radians = math.radians(angle)
                start = (16 + round(7 * math.sin(radians)), 16 + round(7 * math.cos(radians)))
                end = (16 + round(11 * math.sin(radians)), 16 + round(11 * math.cos(radians)))
                draw.line([start, end], fill=(199, 180, 111))
            draw.rectangle((13, 13, 18, 18), fill=(67, 175, 116))
        elif material == "rune":
            draw.rectangle((0, 0, 31, 31), fill=(43, 77, 60))
            draw.rectangle((1, 1, 30, 30), outline=(164, 142, 85))
            draw.line([(4, 25), (27, 25)], fill=(119, 150, 105))
            for column in (6, 14, 22):
                draw.line([(column, 7), (column, 21)], fill=(195, 194, 126))
                draw.line([(column - 3, 10), (column, 13), (column + 3, 10)], fill=(195, 194, 126))
                draw.line([(column - 2, 18), (column, 16), (column + 2, 18)], fill=(124, 187, 128))
        elif material == "eye":
            draw.rectangle((0, 0, 31, 31), fill=(35, 80, 62))
            draw.polygon([(16, 4), (27, 16), (16, 27), (4, 16)], outline=(199, 167, 93), width=2)
            draw.polygon([(16, 9), (22, 16), (16, 22), (9, 16)], fill=(80, 198, 131))
            draw.line([(16, 10), (16, 21)], fill=(178, 235, 159))
            draw.rectangle((17, 12, 18, 14), fill=(230, 252, 202))
        atlas.paste(tile, (left, top))
    atlas.save(atlas_path)
    left, top = tile_origin("stone")
    atlas.crop((left, top, left + 32, top + 32)).save(atlas_path.with_name(f"{NAME}_particle.png"))


def octagonal_solid(model, name, radius, bottom, top, material):
    inner = radius / (1 + math.sqrt(2))
    cut = radius - inner
    model.box(name, (8 - radius, bottom, 8 - inner), (8 + radius, top, 8 + inner), material)
    model.box(name + " north", (8 - inner, bottom, 8 - radius), (8 + inner, top, 8 - inner), material)
    model.box(name + " south", (8 - inner, bottom, 8 + inner), (8 + inner, top, 8 + radius), material)
    half_side = cut / math.sqrt(2)
    for column_sign in (-1, 1):
        for depth_sign in (-1, 1):
            column, depth = 8 + column_sign * inner, 8 + depth_sign * inner
            model.box(f"{name} bevel {column_sign} {depth_sign}",
                      (column - half_side, bottom + .001, depth - half_side),
                      (column + half_side, top - .001, depth + half_side), material,
                      rotation=((column, bottom, depth), "y", 45))


def ring(model, name, radius, bottom, top, thickness, material):
    span = 2 * radius * math.tan(math.pi / 8)
    for index in range(8):
        radians = index * math.pi / 4
        column, depth = 8 + radius * math.sin(radians), 8 + radius * math.cos(radians)
        length = span + .08
        angle = index * 45
        if angle % 90 == 0:
            half_column = length / 2 if index % 4 == 0 else thickness / 2
            half_depth = thickness / 2 if index % 4 == 0 else length / 2
            rotation = None
        else:
            half_column, half_depth = length / 2, thickness / 2
            rotation = ((column, bottom, depth), "y", 45 if index % 4 == 1 else -45)
        model.box(f"{name} segment {index}", (column - half_column, bottom, depth - half_depth),
                  (column + half_column, top, depth + half_depth), material, rotation=rotation)


def main():
    textures()
    model = Model(NAME, NAMESPACE)
    box = model.box
    box("stone_base", (0, 0, 0), (16, 1.15, 16), "stone")
    box("foundation moss stone step", (.4, 1.15, .4), (15.6, 1.65, 15.6), "patina")
    box("carved foundation masonry", (.75, 1.65, .75), (15.25, 3.45, 15.25), "wood")
    box("foundation antique gold reveal", (.5, 3.45, .5), (15.5, 3.7, 15.5), "brass")
    box("foundation upper stone cornice", (.2, 3.7, .2), (15.8, 4, 15.8), "stone")
    box("carved_pedestal", (3.12, 4, 3.12), (12.88, 5.75, 12.88), "wood")
    box("pedestal jade shoulder", (3.15, 5.75, 3.15), (12.85, 6.25, 12.85), "patina")
    for direction in ("north", "east", "south", "west"):
        if direction == "north":
            minimum, maximum = (5.2, 1.95, .58), (10.8, 3.2, .75)
            upper_min, upper_max = (5.5, 4.35, 3), (10.5, 5.35, 3.1)
        elif direction == "south":
            minimum, maximum = (5.2, 1.95, 15.25), (10.8, 3.2, 15.42)
            upper_min, upper_max = (5.5, 4.35, 12.9), (10.5, 5.35, 13)
        elif direction == "west":
            minimum, maximum = (.58, 1.95, 5.2), (.75, 3.2, 10.8)
            upper_min, upper_max = (3, 4.35, 5.5), (3.1, 5.35, 10.5)
        else:
            minimum, maximum = (15.25, 1.95, 5.2), (15.42, 3.2, 10.8)
            upper_min, upper_max = (12.9, 4.35, 5.5), (13, 5.35, 10.5)
        box(f"foundation wishing plaque {direction}", minimum, maximum, "dark", {direction: "rune"})
        box(f"pedestal relief {direction}", upper_min, upper_max, "dark", {direction: "eye"})
    octagonal_solid(model, "antique gold octagonal offering tray", 4.65, 6.25, 6.6, "brass")
    octagonal_solid(model, "emerald_offering_plate", 4.35, 6.6, 6.83, "patina")
    box("engraved jade wishing mandala", (5.05, 6.83, 5.05), (10.95, 6.87, 10.95), "patina", {"up": "inlay"})
    ring(model, "octagonal gilded wishing rim", 4.25, 6.83, 7.2, .43, "brass")
    box("central emerald seal setting", (6.35, 6.83, 6.35), (9.65, 7.12, 9.65), "brass", rotation=((8, 7, 8), "y", 45))
    box("faceted jade wishing seal", (6.65, 7.12, 6.65), (9.35, 7.6, 9.35), "crystal", rotation=((8, 7.12, 8), "y", 45))
    box("jade seal crown facet", (7.1, 7.6, 7.1), (8.9, 7.9, 8.9), "crystal", rotation=((8, 7.6, 8), "y", 45))
    for index, (column, depth, axis, angle) in enumerate(((8, 11.75, "x", -22.5), (8, 4.25, "x", 22.5), (11.75, 8, "z", 22.5), (4.25, 8, "z", -22.5))):
        half_column, half_depth = (1.0, .32) if axis == "x" else (.32, 1.0)
        box(f"golden lotus petal {index}", (column - half_column, 6.95, depth - half_depth),
            (column + half_column, 7.87, depth + half_depth), "brass", rotation=((column, 7.41, depth), axis, angle))
        box(f"lotus jade bead {index}", (column - .25, 7.2, depth - .25), (column + .25, 7.68, depth + .25), "crystal")
    for column in (3.25, 11.85):
        for depth in (3.25, 11.85):
            box(f"corner jade boss {column} {depth}", (column, 4.2, depth), (column + .9, 5.55, depth + .9), "brass")
            box(f"corner mossy cap {column} {depth}", (column + .15, 5.55, depth + .15), (column + .75, 6, depth + .75), "patina")
    generated = model.save()
    report = {"name": NAME, "namespace": NAMESPACE, "elements": len(generated["elements"]), "unchanged_collision": [[0, 0, 0, 16, 4, 16], [3, 4, 3, 13, 8, 13]]}
    (ROOT / "core-build-report.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
