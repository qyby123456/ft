import base64
import copy
import hashlib
import json
from pathlib import Path
import random
import uuid
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ftdungeontrading"
MODEL_ROOT = ROOT / "model_sources"
TILES = {
    "wood": (0, 0, 32, 32), "plank": (32, 0, 32, 32),
    "iron": (64, 0, 16, 32), "gold": (80, 0, 16, 32),
    "cloth": (96, 0, 32, 32), "cloth_front": (0, 32, 16, 32),
    "stone": (16, 32, 16, 16), "tray": (32, 32, 32, 32),
    "ledger": (64, 32, 16, 16), "pages": (80, 32, 16, 16),
    "leather": (96, 32, 16, 16), "sign": (0, 64, 64, 16),
    "seal": (112, 32, 16, 16),
}
FACES = ["north", "south", "east", "west", "up", "down"]
ELEMENTS = []
GROUPS = {}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def make_texture():
    image = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    for name, (left, top, width, height) in TILES.items():
        tile = Image.new("RGBA", (width, height), (70, 49, 33, 255))
        draw = ImageDraw.Draw(tile)
        if name in ("wood", "plank"):
            tile.paste((83, 52, 30, 255), (0, 0, width, height))
            for row, color in [(0, (138, 94, 50)), (2, (103, 65, 33)), (8, (47, 34, 26)), (10, (109, 70, 34)), (17, (60, 39, 26)), (22, (116, 77, 38)), (29, (45, 32, 23))]:
                draw.line((0, row, width - 1, row), fill=color, width=1 if row % 2 else 2)
            for column, row in [(5, 7), (21, 19), (13, 27)]:
                draw.line((column, row, column + 7, row), fill=(41, 29, 23))
                draw.point((column + 6, row + 1), fill=(144, 98, 51))
            if name == "wood":
                tile = tile.rotate(90)
        elif name == "iron":
            tile.paste((46, 49, 53, 255), (0, 0, width, height))
            draw.line((0, 0, width - 1, 0), fill=(117, 120, 116))
            draw.line((0, 1, 0, height - 1), fill=(83, 89, 90))
            draw.line((width - 1, 1, width - 1, height - 1), fill=(27, 30, 34))
            for row in [6, 23]:
                draw.rectangle((6, row, 9, row + 3), fill=(28, 31, 34))
                draw.line((6, row, 8, row), fill=(154, 139, 109))
                draw.rectangle((7, row + 1, 8, row + 2), fill=(86, 77, 63))
        elif name == "gold":
            tile.paste((153, 113, 50, 255), (0, 0, width, height))
            draw.rectangle((1, 1, 14, 3), fill=(227, 193, 108))
            draw.line((0, 0, 0, height - 1), fill=(235, 201, 121))
            draw.line((15, 0, 15, height - 1), fill=(85, 66, 38))
            draw.rectangle((3, 8, 5, 10), fill=(182, 140, 63))
        elif name in ("cloth", "cloth_front"):
            tile.paste((70, 41, 86, 255), (0, 0, width, height))
            draw.rectangle((0, 0, 1, height - 1), fill=(158, 119, 65))
            draw.rectangle((width - 2, 0, width - 1, height - 1), fill=(158, 119, 65))
            draw.line((3, 0, 3, height - 1), fill=(114, 76, 130))
            draw.line((width - 4, 0, width - 4, height - 1), fill=(40, 28, 58))
            if name == "cloth_front":
                draw.rectangle((4, 6, 11, 14), outline=(216, 183, 105))
                draw.line((7, 8, 7, 22), fill=(216, 183, 105), width=2)
                draw.line((4, 19, 11, 19), fill=(216, 183, 105))
                draw.line((2, 29, 13, 29), fill=(216, 183, 105), width=2)
        elif name == "stone":
            tile.paste((58, 61, 65, 255), (0, 0, width, height))
            draw.rectangle((1, 1, 13, 3), fill=(104, 106, 105))
            draw.rectangle((2, 4, 5, 6), fill=(73, 76, 76))
            draw.rectangle((10, 9, 13, 12), fill=(36, 41, 47))
        elif name == "tray":
            tile.paste((32, 47, 47, 255), (0, 0, width, height))
            draw.rectangle((0, 0, 31, 31), outline=(181, 143, 77), width=2)
            draw.rectangle((3, 3, 28, 28), outline=(74, 99, 89), width=1)
            draw.line((16, 10, 16, 22), fill=(107, 142, 119), width=2)
            draw.line((10, 16, 22, 16), fill=(107, 142, 119), width=2)
            for column, row in [(7, 7), (24, 7), (7, 24), (24, 24)]:
                draw.point((column, row), fill=(181, 143, 77))
        elif name == "ledger":
            tile.paste((48, 61, 58, 255), (0, 0, width, height))
            draw.rectangle((1, 1, 14, 14), outline=(173, 137, 67))
            draw.rectangle((4, 4, 11, 11), outline=(110, 127, 97))
            draw.line((7, 5, 7, 10), fill=(215, 184, 112), width=2)
        elif name == "pages":
            tile.paste((190, 175, 139, 255), (0, 0, width, height))
            for row in range(1, 16, 3):
                draw.line((0, row, 15, row), fill=(133, 120, 93))
            draw.line((1, 0, 14, 0), fill=(225, 212, 171))
        elif name == "leather":
            tile.paste((89, 51, 31, 255), (0, 0, width, height))
            draw.line((1, 1, 13, 1), fill=(145, 96, 56))
            draw.line((1, 1, 1, 14), fill=(129, 80, 45))
            draw.line((14, 2, 14, 14), fill=(48, 31, 24))
        elif name == "sign":
            tile.paste((31, 35, 38, 255), (0, 0, width, height))
            draw.rectangle((0, 0, 63, 15), outline=(144, 114, 60))
            for center, color in [(17, (217, 184, 103)), (47, (112, 178, 153))]:
                draw.rectangle((center - 4, 5, center + 4, 12), outline=color)
                draw.line((center, 2, center, 12), fill=color)
            draw.line((29, 7, 35, 7), fill=(201, 175, 108), width=2)
            draw.point((35, 6), fill=(201, 175, 108))
            draw.point((35, 9), fill=(201, 175, 108))
        elif name == "seal":
            tile.paste((123, 31, 42, 255), (0, 0, width, height))
            draw.rectangle((1, 1, 13, 3), fill=(188, 65, 70))
            draw.line((5, 5, 10, 10), fill=(225, 146, 109))
            draw.line((10, 5, 5, 10), fill=(225, 146, 109))
        image.alpha_composite(tile, (left, top))
    destination = ASSETS / "textures/block/trading_table.png"
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination)
    return image, destination


def box(name, start, end, material, group, face_materials=None, rotation=None):
    faces = {}
    face_materials = face_materials or {}
    for face in FACES:
        tile = TILES[face_materials.get(face, material)]
        left, top, width, height = tile
        faces[face] = {"uv": [left / 8, top / 8, (left + width) / 8, (top + height) / 8], "texture": "#body"}
    element = {"name": name, "from": start, "to": end, "faces": faces}
    if rotation: element["rotation"] = rotation
    ELEMENTS.append(element)
    GROUPS[name] = group


def make_geometry():
    for column in [1.2, 12.3]:
        for depth in [1.2, 12.3]:
            label = f"Leg {column} {depth}"
            box(label + " / timber", [column, 1, depth], [column + 2.5, 12.2, depth + 2.5], "wood", "frame")
            box(label + " / iron shoe", [column - .12, 0, depth - .12], [column + 2.62, 1.5, depth + 2.62], "iron", "frame", {"down": "stone"})
            box(label + " / upper collar", [column - .10, 9.5, depth - .10], [column + 2.6, 10.25, depth + 2.6], "iron", "frame")
    for label, start, end in [
        ("front", [2, 2.5, 2], [14, 3.7, 3]),
        ("rear", [2, 2.5, 13], [14, 3.7, 14]),
        ("left", [2, 2.5, 3], [3, 3.7, 13]),
        ("right", [13, 2.5, 3], [14, 3.7, 13])
    ]:
        box("Lower rail / " + label, start, end, "wood", "frame")
    box("Storage shelf", [2.1, 3.7, 2.1], [13.9, 4.2, 13.9], "plank", "frame")
    for label, start, end in [
        ("front", [1, 10.5, .7], [15, 12.3, 1.5]),
        ("back", [1, 10.5, 14.5], [15, 12.3, 15.3]),
        ("left", [.7, 10.5, 1.5], [1.5, 12.3, 14.5]),
        ("right", [14.5, 10.5, 1.5], [15.3, 12.3, 14.5])
    ]:
        box("Apron / " + label, start, end, "wood", "frame")
    box("Desktop / iron subframe", [0, 12, 0], [16, 12.6, 16], "iron", "desktop")
    for plank in range(4):
        box(f"Desktop / continuous plank {plank}", [plank * 4 + .04, 12.6, 0], [plank * 4 + 3.96, 13.7, 16], "plank", "desktop")
    for column in [0, 14.8]:
        for depth in [0, 14.8]:
            box(f"Desktop / iron corner {column} {depth}", [column, 12.55, depth], [column + 1.2, 13.85, depth + 1.2], "iron", "desktop")
    for side, column in [("pay", 1.25), ("goods", 9.25)]:
        box("Tray " + side + " / felt and brass base", [column, 13.71, 2.3], [column + 5.5, 13.95, 12.7], "iron", "trays", {"up": "tray"})
        for edge, start, end in [
            ("front", [column, 13.95, 2.3], [column + 5.5, 14.3, 2.6]),
            ("rear", [column, 13.95, 12.4], [column + 5.5, 14.3, 12.7]),
            ("left", [column, 13.95, 2.6], [column + .3, 14.3, 12.4]),
            ("right", [column + 5.2, 13.95, 2.6], [column + 5.5, 14.3, 12.4])
        ]:
            box("Tray " + side + " / " + edge, start, end, "gold", "trays")
    box("Cloth / top runner", [6.95, 13.72, .35], [9.05, 13.86, 15.65], "cloth", "cloth")
    box("Cloth / front hanging pennant", [6.95, 7.0, .2], [9.05, 13.75, .38], "cloth_front", "cloth")
    box("Cloth / back hanging pennant", [6.95, 8.7, 15.62], [9.05, 13.75, 15.8], "cloth_front", "cloth")
    box("Front / transaction icon plaque", [2.4, 10.6, .43], [13.6, 12.1, .69], "iron", "sign", {"north": "sign"})
    for column in [2.6, 13.0]:
        box(f"Front / plaque stud {column}", [column, 11.08, .27], [column + .4, 11.48, .44], "gold", "sign")
    rotation = {"axis": "y", "angle": 22.5, "origin": [8, 14, 13.5]}
    box("Ledger / bottom leather cover", [6.8, 13.87, 12.3], [9.2, 14.07, 14.9], "leather", "accessories", rotation=rotation)
    box("Ledger / bound pages", [6.9, 14.07, 12.4], [9.1, 14.6, 14.8], "pages", "accessories", rotation=rotation)
    box("Ledger / embossed cover", [6.8, 14.6, 12.3], [9.2, 14.8, 14.9], "leather", "accessories", {"up": "ledger"}, rotation)
    box("Ledger / clasp", [7.75, 14.8, 12.25], [8.25, 14.95, 13.0], "gold", "accessories", rotation=rotation)
    box("Receipt / folded parchment", [7.1, 13.87, 5.4], [8.9, 14.04, 8.7], "pages", "accessories")
    box("Receipt / red wax seal", [7.55, 14.04, 6.25], [8.45, 14.28, 7.15], "seal", "accessories")
    box("Shelf / supply chest", [8.8, 4.2, 8.4], [12.6, 6.5, 12.3], "leather", "accessories")
    box("Shelf / supply chest lid", [8.65, 6.5, 8.25], [12.75, 7.0, 12.45], "wood", "accessories")
    box("Shelf / chest latch", [10.4, 5.8, 8.2], [11.0, 6.8, 8.4], "gold", "accessories")
    box("Shelf / rolled contract", [3.6, 4.2, 8.6], [6.4, 5.1, 9.5], "pages", "accessories")
    box("Shelf / contract ribbon", [4.75, 4.15, 8.55], [5.25, 5.15, 9.55], "cloth", "accessories")


def optimize_hidden():
    source = copy.deepcopy(ELEMENTS)
    removals = []
    directions = {"north": (2, -1), "south": (2, 1), "east": (0, 1), "west": (0, -1), "up": (1, 1), "down": (1, -1)}
    for element in source:
        if "rotation" in element: continue
        for face, (axis, sign) in directions.items():
            coordinate = element["to" if sign > 0 else "from"][axis]
            for other in source:
                if "rotation" in other or other["name"] == element["name"]: continue
                beyond = other["to"][axis] > coordinate + 1e-7 if sign > 0 else other["from"][axis] < coordinate - 1e-7
                covered = all(other["from"][other_axis] <= element["from"][other_axis] and other["to"][other_axis] >= element["to"][other_axis] for other_axis in range(3) if other_axis != axis)
                if other["from"][axis] <= coordinate <= other["to"][axis] and beyond and covered:
                    removals.append({"element": element["name"], "face": face, "opaque_occluder": other["name"]})
                    break
    lookup = {element["name"]: element for element in ELEMENTS}
    for proof in removals: del lookup[proof["element"]]["faces"][proof["face"]]
    return removals


def main():
    image, texture = make_texture()
    make_geometry()
    removed = optimize_hidden()
    display = {
        "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [.60, .60, .60]},
        "ground": {"translation": [0, 3, 0], "scale": [.25, .25, .25]},
        "fixed": {"rotation": [0, 180, 0], "scale": [.5, .5, .5]},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [.375, .375, .375]},
        "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 2.5, 0], "scale": [.375, .375, .375]},
        "firstperson_righthand": {"rotation": [0, 45, 0], "scale": [.4, .4, .4]},
        "firstperson_lefthand": {"rotation": [0, 225, 0], "scale": [.4, .4, .4]},
    }
    runtime = {"credit": "FT original dungeon trading table, 2026-11-01", "textures": {"body": "ftdungeontrading:block/trading_table", "particle": "#body"}, "elements": ELEMENTS, "display": display}
    write_json(ASSETS / "models/block/trading_table.json", runtime)
    write_json(ASSETS / "models/item/trading_table.json", {"parent": "ftdungeontrading:block/trading_table"})
    write_json(ASSETS / "blockstates/trading_table.json", {"variants": {"facing=" + direction: {"model": "ftdungeontrading:block/trading_table", "y": rotation} for direction, rotation in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]}})
    cubes = []
    outliner = {}
    for element in ELEMENTS:
        identifier = str(uuid.uuid5(uuid.NAMESPACE_URL, "fttrading/" + element["name"]))
        rotation = element.get("rotation", {"axis": "y", "angle": 0, "origin": [8, 0, 8]})
        angles = [0, 0, 0]
        angles[{"x": 0, "y": 1, "z": 2}[rotation["axis"]]] = rotation["angle"]
        faces = {face: {"texture": None, "uv": [0, 0, 0, 0]} for face in FACES}
        faces.update({face: {"texture": 0, "uv": data["uv"]} for face, data in element["faces"].items()})
        cubes.append({"name": element["name"], "uuid": identifier, "type": "cube", "from": element["from"], "to": element["to"], "origin": rotation["origin"], "rotation": angles, "autouv": 0, "box_uv": False, "faces": faces})
        outliner.setdefault(GROUPS[element["name"]], []).append(identifier)
    editable = {"meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False}, "name": "暮铁契约_地牢交易桌", "resolution": {"width": 16, "height": 16}, "elements": cubes,
                "outliner": [{"name": group, "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, "fttrading/group/" + group)), "origin": [8, 0, 8], "rotation": [0, 0, 0], "children": children} for group, children in outliner.items()],
                "textures": [{"name": "trading_table.png", "id": "0", "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, "fttrading/texture")), "folder": "block", "namespace": "ftdungeontrading", "width": 128, "height": 128, "uv_width": 16, "uv_height": 16, "mode": "bitmap", "source": "data:image/png;base64," + base64.b64encode(texture.read_bytes()).decode("ascii")}], "display": display}
    write_json(MODEL_ROOT / "地牢交易桌.bbmodel", editable)
    write_json(MODEL_ROOT / "budget.json", {"budgets": {"components": 100, "declared_faces": 600, "texture_pixels": 16384}, "lanes": []})
    faces = sum(len(element["faces"]) for element in ELEMENTS)
    assert len(ELEMENTS) <= 100 and faces <= 600
    assert len({element["name"] for element in ELEMENTS}) == len(ELEMENTS)
    for element in ELEMENTS:
        assert all(0 <= value <= 16 for value in element["from"] + element["to"])
        assert all(element["from"][axis] < element["to"][axis] for axis in range(3))
    write_json(MODEL_ROOT / "model-report.json", {"components": len(ELEMENTS), "declared_faces": faces, "textures": 1, "texture_pixels": 16384, "transparent_shells": 0, "geometry_animation_copies": 0, "removed_hidden_faces": len(removed), "dynamic_product_previews_maximum": 6, "client_tested": False})
    write_json(MODEL_ROOT / "hidden-face-proofs.json", removed)
    image.resize((768, 768), Image.Resampling.NEAREST).save(MODEL_ROOT / "图集放大.png")
    print(json.dumps({"components": len(ELEMENTS), "faces": faces, "hidden_faces_removed": len(removed)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
