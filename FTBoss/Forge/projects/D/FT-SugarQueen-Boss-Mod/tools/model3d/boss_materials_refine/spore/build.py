from __future__ import annotations

import base64
import hashlib
import json
import math
import uuid
from pathlib import Path

from PIL import Image


PROJECT = Path(__file__).resolve().parents[4]
ASSETS = PROJECT / "src/main/resources/assets/ftboss"
OUTPUT = PROJECT / "art/boss_materials/refine/spore"
WORK = PROJECT / "_work/boss-materials-refine-20261021/spore"
SIZE = 256
DENSITY = 2
PARTS = []
PALETTE = {
    "compost": [(47, 30, 37), (78, 46, 47), (116, 75, 62), (156, 108, 79), (189, 144, 103)],
    "cap": [(40, 26, 48), (68, 39, 65), (106, 60, 89), (143, 88, 108), (182, 123, 133)],
    "rim": [(28, 61, 63), (43, 96, 94), (73, 137, 126), (125, 173, 145), (187, 202, 164)],
    "gill": [(36, 24, 44), (61, 39, 67), (93, 62, 94), (129, 91, 123), (167, 126, 145)],
    "fiber": [(66, 48, 55), (104, 80, 81), (146, 119, 109), (185, 158, 132), (218, 193, 157)],
    "cyan": [(25, 63, 77), (36, 112, 132), (51, 162, 166), (119, 207, 193), (215, 245, 213)],
    "violet": [(57, 30, 79), (91, 51, 132), (136, 88, 176), (180, 140, 211), (231, 211, 238)],
    "amber": [(100, 48, 45), (155, 78, 52), (203, 125, 67), (233, 175, 99), (252, 218, 153)],
    "pink": [(90, 31, 69), (139, 53, 93), (183, 85, 123), (217, 133, 160), (246, 195, 204)],
    "lime": [(58, 73, 39), (91, 114, 49), (132, 154, 64), (175, 192, 103), (218, 230, 157)],
}


def save_json(target, data):
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def stable_id(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "ftboss:spore_material/refine/" + name))


def cube(name, center, size, material, group, axis=None, angle=0):
    element = {
        "name": name,
        "from": [round(value - extent / 2, 5) for value, extent in zip(center, size)],
        "to": [round(value + extent / 2, 5) for value, extent in zip(center, size)],
    }
    if angle:
        element["rotation"] = {"origin": list(center), "axis": axis, "angle": angle, "rescale": False}
    PARTS.append({"element": element, "material": material, "group": group})


def cap(prefix, center, width, depth, height):
    center_x, center_y, center_z = center
    for band in range(7):
        relative = (band - 3) / 3.6
        span = math.sqrt(1 - relative * relative)
        band_z = center_z + (band - 3) * depth / 7
        offset_x = .28 * math.cos(band * .9) + .15 * relative
        lift = .20 * math.cos(band * .7) + .07 * (band - 3)
        cube(f"{prefix}_flesh_{band}", [center_x + offset_x, center_y + lift, band_z],
             [width * span, height, depth / 7 + .13], "gill", prefix + "_flesh")
        cube(f"{prefix}_celadon_edge_{band}", [center_x + offset_x - .05, center_y + .30 + lift, band_z + .025],
             [width * span + .30, .38, depth / 7 + .20], "rim", prefix + "_rim")
        cube(f"{prefix}_velvet_{band}", [center_x + offset_x + .10, center_y + .85 + lift, band_z + .075],
             [width * span * .58, .75, depth / 7 + .055], "cap", prefix + "_skin")
        for side in [-1, 1]:
            cube(f"{prefix}_sloping_velvet_{band}_{side}",
                 [center_x + offset_x + .10 + side * width * span * .36,
                  center_y + .63 + lift, band_z + .075],
                 [width * span * .30, .47, depth / 7 + .07], "cap", prefix + "_skin", "z", side * -22.5)
    for band in range(5):
        relative = (band - 2) / 2.75
        span = math.sqrt(1 - relative * relative)
        cube(f"{prefix}_crown_{band}", [center_x + .30, center_y + 1.13 + .13 * math.cos(band),
                                      center_z + (band - 2) * depth / 7],
             [width * .49 * span, .40, depth / 7 + .11], "compost", prefix + "_skin")
    for index, angle in enumerate([-90, -67.5, -45, -22.5, 0, 22.5, 45, 67.5, 90]):
        radians = math.radians(angle)
        length = depth * .44 if abs(angle) < 67.5 else width * .43
        pivot_x = center_x + math.sin(radians) * length * .40
        pivot_z = center_z - math.cos(radians) * length * .40
        rotation = angle if abs(angle) <= 45 else (angle - 90 if angle > 0 else angle + 90)
        crosswise = abs(angle) > 45
        dimensions = [length, .68, .23] if crosswise else [.23, .68, length]
        cube(f"{prefix}_radial_gill_front_{index}", [pivot_x, center_y - height / 2 - .19, pivot_z],
             dimensions, "gill", prefix + "_gills", "y", rotation)
    for index, angle in enumerate([-45, 0, 45]):
        radians = math.radians(angle)
        length = depth * .37
        cube(f"{prefix}_radial_gill_back_{index}",
             [center_x + math.sin(radians) * length * .38, center_y - height / 2 - .16,
              center_z + math.cos(radians) * length * .38],
             [.25, .63, length], "gill", prefix + "_gills", "y", angle)


def crystal(prefix, center, width, height, depth, material, angle):
    center_x, base_y, center_z = center
    radians = math.radians(angle)
    levels = [(0, .18, .68), (.14, .66, 1), (.64, .86, .66), (.84, .97, .30), (.95, 1.02, .12)]
    for index, (bottom, top, span) in enumerate(levels):
        local_y = (bottom + top) * height / 2
        part_center = [center_x - math.sin(radians) * local_y,
                       base_y + math.cos(radians) * local_y, center_z]
        for segment, (size_x, size_z) in enumerate([(width * span, depth * span * .72),
                                                   (width * span * .68, depth * span)]):
            cube(f"{prefix}_facet_{index}_{segment}", part_center,
                 [size_x, (top - bottom) * height + .045 * (segment + 1), size_z],
                 material, "05_embedded_spore_crystals", "z", angle)
    cube(prefix + "_mycelial_socket", [center_x, base_y + .07, center_z],
         [width * 1.36, .63, depth * 1.38], "fiber", "04_crystal_sockets")


def geometry():
    cube("cut_mycelial_heart", [7.8, 4.8, 8.2], [3.0, 4.7, 2.9], "fiber", "01_fibrous_heart", "z", -22.5)
    cube("compost_matrix_front", [7.5, 3.1, 7.8], [4.7, 1.4, 3.7], "compost", "00_substrate", "y", -22.5)
    cube("compost_matrix_back", [8.4, 3.3, 9], [4.1, 1.5, 3.7], "compost", "00_substrate", "y", 22.5)
    cube("compost_torn_lobe", [5.7, 3.45, 7.15], [2.3, 1.45, 2.2], "compost", "00_substrate", "z", 22.5)
    cube("compost_torn_lobe_back", [10.1, 3.35, 9.1], [2.3, 1.15, 1.9], "compost", "00_substrate", "z", -22.5)
    for index, (center, dimensions, angle) in enumerate([
        ([5.4, 2.7, 6.7], [4.2, .8, 1.1], -22.5),
        ([9.7, 2.6, 7.1], [4.7, .9, 1.3], 22.5),
        ([5.9, 2.9, 9.5], [3.8, .7, 1.2], 22.5),
        ([9.8, 2.8, 10], [3.9, .8, 1], -22.5),
    ]):
        cube(f"torn_radial_root_{index}", center, dimensions, "fiber", "01_fibrous_heart", "y", angle)
    for index, (center, dimensions, angle) in enumerate([
        ([6.1, 4.7, 6.5], [.60, 3.5, .63], -22.5),
        ([8.4, 5.1, 6.6], [.67, 3.1, .56], 22.5),
        ([9.8, 4.4, 8.4], [.62, 3.2, .63], 22.5),
        ([7.1, 4.7, 9.9], [.70, 3.6, .60], -22.5),
    ]):
        cube(f"surface_mycelial_vein_{index}", center, dimensions, "fiber", "01_fibrous_heart", "z", angle)
    cap("lower", [7, 5.8, 6.8], 8.8, 5.8, .93)
    cap("upper", [8.4, 8.7, 8.7], 10.3, 6.3, 1.15)
    cube("shelf_branch_front", [6.8, 6.9, 7], [1.3, 3.4, 1.3], "fiber", "01_fibrous_heart", "z", -22.5)
    cube("shelf_branch_back", [9.2, 6.6, 9.7], [1.4, 4.3, 1.5], "fiber", "01_fibrous_heart", "z", 22.5)
    crystal("dominant_cyan", [9.9, 9.55, 8.6], 1.9, 4.2, 1.65, "cyan", -22.5)
    crystal("secondary_violet", [5.5, 6.65, 6.3], 1.55, 2.8, 1.4, "violet", 22.5)
    crystal("back_amber", [7.9, 9.6, 10.25], 1.05, 2.3, 1.1, "amber", 22.5)
    crystal("lower_pink", [10.2, 6.65, 6.55], 1.1, 1.7, .95, "pink", -22.5)
    for index, (center, size, material) in enumerate([
        ([6.9, 10.02, 8], [.87, .43, .70], "lime"),
        ([8.4, 10.11, 7.1], [.64, .44, .56], "lime"),
        ([4.2, 6.71, 7.4], [.70, .42, .64], "violet"),
        ([10.9, 9.5, 10.9], [.63, .40, .64], "cyan"),
        ([7.4, 6.91, 4.9], [.56, .34, .57], "pink"),
    ]):
        cube(f"seed_spore_{index}", center, size, material, "06_seed_spores", "y", 45)
    for index, (center, size, material) in enumerate([
        ([7.0, 3.7, 5.9], [.83, .48, .48], "lime"),
        ([10.2, 4.3, 9.5], [.72, .50, .44], "cyan"),
        ([6.6, 4.4, 10.25], [.58, .52, .41], "violet"),
    ]):
        cube(f"matrix_inclusion_{index}", center, size, material, "00_substrate", "z", 22.5)


def face_dimensions(element, face):
    extent = [finish - start for start, finish in zip(element["from"], element["to"])]
    axes = {"north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1),
            "up": (0, 2), "down": (0, 2)}[face]
    return [extent[axis] for axis in axes]


def paint_island(atlas, rectangle, part, face):
    start_x, start_y, width, height = rectangle
    colors = PALETTE[part["material"]]
    identifier = int(hashlib.sha256((part["element"]["name"] + face).encode()).hexdigest()[:8], 16)
    material = part["material"]
    for pixel_y in range(-1, math.ceil(height) + 1):
        for pixel_x in range(-1, math.ceil(width) + 1):
            local_x = min(max(pixel_x, 0), max(0, width - .01))
            local_y = min(max(pixel_y, 0), max(0, height - .01))
            fraction_x, fraction_y = local_x / width, local_y / height
            level = 2
            if material in {"cyan", "violet", "amber", "pink", "lime"}:
                level = {"north": 2, "south": 1, "east": 1, "west": 3, "up": 3, "down": 0}[face]
                if fraction_x < .24:
                    level += 1
                if .37 < fraction_x < .56 and .10 < fraction_y < .7:
                    level += 1
                if fraction_y > .8:
                    level -= 1
            elif material == "gill":
                level = 1 if face == "down" else 2
                if (pixel_x + identifier) % 3 == 0:
                    level += 1
                if fraction_y > .7:
                    level -= 1
            elif material == "fiber":
                direction = pixel_y if face in {"up", "down"} else pixel_x
                level = 3 if (direction + identifier) % 4 == 0 else 2
                if (direction + identifier) % 4 == 3:
                    level = 1
                if fraction_y < .18:
                    level += 1
            elif material == "rim":
                level = 3 if fraction_y < .35 else 2
                if (pixel_x + identifier) % 7 == 0:
                    level += 1
                if face == "down":
                    level -= 1
            else:
                level = 3 if face == "up" else 2
                wave = math.sin(local_x * .55 + identifier % 17) + math.sin(local_y * .8)
                if wave > 1.1:
                    level += 1
                if wave < -.9:
                    level -= 1
                if fraction_y > .85:
                    level -= 1
                if face in {"north", "south", "east", "west"} and fraction_y < .18:
                    level += 1
                if material == "cap" and (pixel_x * 3 + pixel_y * 5 + identifier) % 29 == 0:
                    level += 1
            color = colors[min(4, max(0, level))]
            atlas.putpixel((start_x + pixel_x, start_y + pixel_y), (*color, 255))


def texture_atlas():
    requests = []
    for part in PARTS:
        part["element"]["faces"] = {}
        for face in ("north", "south", "east", "west", "up", "down"):
            width, height = [value * DENSITY for value in face_dimensions(part["element"], face)]
            requests.append((part, face, width, height))
    requests.sort(key=lambda request: (-math.ceil(request[3]), -math.ceil(request[2])))
    rows = []
    atlas = Image.new("RGBA", (SIZE, SIZE))
    islands = []
    for part, face, width, height in requests:
        cell_width, cell_height = math.ceil(width) + 3, math.ceil(height) + 3
        row = next((row for row in rows if row[2] >= cell_height and row[0] + cell_width <= SIZE - 1), None)
        if row is None:
            start_y = 1 if not rows else rows[-1][1] + rows[-1][2]
            if start_y + cell_height > SIZE - 1:
                raise ValueError("Texture atlas full")
            row = [1, start_y, cell_height]
            rows.append(row)
        start_x, start_y = row[0] + 1, row[1] + 1
        row[0] += cell_width
        rectangle = (start_x, start_y, width, height)
        paint_island(atlas, rectangle, part, face)
        part["element"]["faces"][face] = {
            "uv": [round(value * 16 / SIZE, 7) for value in
                   (start_x, start_y, start_x + width, start_y + height)], "texture": "#atlas"}
        islands.append({"element": part["element"]["name"], "face": face,
                        "pixels": list(rectangle), "material": part["material"]})
    target = ASSETS / "textures/item/boss_materials/spore.png"
    atlas.save(target)
    atlas.resize((768, 768), Image.Resampling.NEAREST).save(OUTPUT / "atlas_3x.png")
    save_json(WORK / "uv-islands.json", islands)
    return target


def blockbench(model, texture_path):
    entries, groups = [], {}
    for part in PARTS:
        element = part["element"]
        rotation = element.get("rotation", {})
        center = [(start + finish) / 2 for start, finish in zip(element["from"], element["to"])]
        entry = {"name": element["name"], "uuid": stable_id(element["name"]), "type": "cube",
                 "from": element["from"], "to": element["to"], "origin": rotation.get("origin", center),
                 "box_uv": False, "autouv": 0,
                 "rotation": [rotation.get("angle", 0) if rotation.get("axis") == axis else 0 for axis in "xyz"],
                 "faces": {face: {"uv": [value * SIZE / 16 for value in data["uv"]], "texture": 0}
                           for face, data in element["faces"].items()}}
        entries.append(entry)
        groups.setdefault(part["group"], []).append(entry["uuid"])
    project = {
        "meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
        "name": "虹彩孢子 · 层生孢晶菌核", "model_identifier": "spore_material",
        "resolution": {"width": SIZE, "height": SIZE}, "visible_box": [1, 1, 0],
        "ambientocclusion": False, "front_gui_light": False, "elements": entries,
        "outliner": [{"name": name, "uuid": stable_id(name), "origin": [8, 8, 8], "children": children}
                     for name, children in groups.items()],
        "textures": [{"name": "spore.png", "path": "", "folder": "item/boss_materials", "namespace": "ftboss",
                      "id": "0", "uuid": stable_id("atlas"), "width": SIZE, "height": SIZE,
                      "uv_width": SIZE, "uv_height": SIZE, "particle": True,
                      "source": "data:image/png;base64," + base64.b64encode(texture_path.read_bytes()).decode()}],
        "display": model["display"],
    }
    save_json(PROJECT / "art/boss_materials/blockbench/spore_material.bbmodel", project)


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    WORK.mkdir(parents=True, exist_ok=True)
    geometry()
    atlas_path = texture_atlas()
    model = {
        "ambientocclusion": False, "gui_light": "side",
        "textures": {"atlas": "ftboss:item/boss_materials/spore", "particle": "ftboss:item/spore_material"},
        "elements": [part["element"] for part in PARTS],
        "display": {
            "thirdperson_righthand": {"rotation": [0, 90, -15], "translation": [0, 2.5, 1], "scale": [.63] * 3},
            "thirdperson_lefthand": {"rotation": [0, -90, 15], "translation": [0, 2.5, 1], "scale": [.63] * 3},
            "firstperson_righthand": {"rotation": [0, 135, -12], "translation": [1.2, 2.5, 1], "scale": [.65] * 3},
            "firstperson_lefthand": {"rotation": [0, -135, 12], "translation": [1.2, 2.5, 1], "scale": [.65] * 3},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2.5, 0], "scale": [.5] * 3},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, .5, 0], "scale": [.8] * 3},
            "head": {"rotation": [0, 0, 0], "translation": [0, 8, 0], "scale": [.65] * 3},
            "gui": {"rotation": [12, -25, 0], "translation": [0, 0, 0], "scale": [.9] * 3},
        },
    }
    save_json(ASSETS / "models/item/boss_materials/spore_material.json", model)
    blockbench(model, atlas_path)
    from preview import render, load_final
    final_model, final_texture = load_final()
    icon = render(final_model, final_texture, (32, 32), yaw=-25, pitch=12, margin=2, outline=True)
    icon.save(ASSETS / "textures/item/spore_material.png")
    print(json.dumps({"elements": len(PARTS), "faces": len(PARTS) * 6,
                      "atlas": [SIZE, SIZE], "density": DENSITY, "icon": [32, 32]}))


if __name__ == "__main__":
    main()
