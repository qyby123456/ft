from __future__ import annotations

import argparse
import base64
import hashlib
import json
import math
import shutil
import uuid
from pathlib import Path

import numpy as np
from PIL import Image


ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
OUTPUT = PROJECT / "art" / "compost_crown"
PACK = OUTPUT / "preview_resourcepack"
ASSETS = PACK / "assets" / "ftboss"
MODEL_ID = "compost_crown"
TEXTURE_SIZE = 512
PIXELS_PER_UNIT = 3
PALETTE = {
    "compost": (111, 70, 48), "ridge": (140, 91, 57),
    "iron": (42, 52, 64), "steel": (88, 107, 117),
    "brass": (155, 114, 66), "leather": (83, 43, 52),
    "toxic": (169, 230, 71), "pink": (221, 74, 132),
    "amber": (240, 164, 64), "cyan": (58, 203, 188),
    "blue": (76, 138, 225), "violet": (160, 104, 218),
}
GEMS = {"toxic", "pink", "amber", "cyan", "blue", "violet"}
PARTS = []


def cube(name, center, size, material, group, axis=None, angle=0):
    element = {
        "name": name,
        "from": [round(value - extent / 2, 5) for value, extent in zip(center, size)],
        "to": [round(value + extent / 2, 5) for value, extent in zip(center, size)],
    }
    if angle:
        element["rotation"] = {"origin": list(center), "axis": axis, "angle": angle}
    PARTS.append({"element": element, "material": material, "group": group})


def octagonal_layer(name, center, size, bevel, material, group):
    center_x, center_y, center_z = center
    width, height, depth = size
    cube(name + "_span_x", center, [width, height, depth - 2 * bevel], material, group)
    cube(name + "_span_z", center, [width - 2 * bevel, height - .025, depth], material, group)
    for side_x in (-1, 1):
        for side_z in (-1, 1):
            corner = [center_x + side_x * (width / 2 - bevel), center_y,
                      center_z + side_z * (depth / 2 - bevel)]
            cube(f"{name}_bevel_{side_x}_{side_z}", corner,
                 [bevel * math.sqrt(2), height - .05, bevel * math.sqrt(2)], material, group, "y", 45)


def make_geometry():
    cube("full_tang", [8, 3.0, 8], [1.7, 23.0, 1.7], "iron", "01_handle")
    cube("grip_core", [8, -0.5, 8], [2.35, 12.2, 2.35], "leather", "01_handle")
    for wrap_index in range(9):
        wrap_y = -5.7 + wrap_index * 1.3
        cube(f"wrap_{wrap_index:02d}", [8, wrap_y, 8], [2.57, .48, 2.57],
             "leather", "01_handle", "y", 22.5 if wrap_index % 2 else -22.5)
        cube(f"stitch_{wrap_index:02d}", [7.72, wrap_y + .05, 6.64], [.28, .18, .15],
             "brass", "01_handle")
    for collar_y in (-6.7, 5.8):
        octagonal_layer("grip_collar_" + str(collar_y), [8, collar_y, 8], [3.0, .72, 3.0],
                        .5, "steel", "01_handle")
    octagonal_layer("pommel", [8, -8.0, 8], [3.7, 1.7, 3.7], .75, "iron", "01_handle")
    cube("pommel_rune_socket", [8, -8.0, 6.02], [1.65, 1.65, .28], "brass", "01_handle", "z", 45)
    cube("pommel_rune", [8, -8.0, 5.8], [.95, .95, .3], "toxic", "01_handle", "z", 45)
    cube("neck_front_inlay", [8, 9.3, 7.08], [.4, 5.3, .16], "cyan", "02_socket")
    cube("neck_back_inlay", [8, 9.3, 8.92], [.4, 5.3, .16], "violet", "02_socket")
    octagonal_layer("neck_socket", [8, 12.1, 8], [4.5, 2.1, 4.5], .9, "iron", "02_socket")
    octagonal_layer("socket_trim", [8, 11.5, 8], [4.85, .48, 4.85], 1, "brass", "02_socket")
    octagonal_layer("lower_cage", [8, 14.25, 8], [12.8, 2.2, 11.6], 2.1, "iron", "03_cage")
    octagonal_layer("cage_lower_lip", [8, 13.35, 8], [13.15, .42, 11.95], 2.15, "steel", "03_cage")
    octagonal_layer("cage_upper_lip", [8, 15.25, 8], [13.0, .38, 11.8], 2.15, "brass", "03_cage")
    layers = [
        ([8, 16.8, 8], [12.0, 3.4, 10.7], 2.1),
        ([7.7, 19.7, 8.0], [10.6, 3.2, 9.3], 1.9),
        ([8.15, 22.35, 8.05], [8.65, 2.8, 7.6], 1.55),
        ([7.7, 24.65, 8.12], [6.55, 2.35, 5.85], 1.2),
        ([7.05, 26.55, 8.15], [4.4, 1.8, 4.2], .85),
    ]
    for layer_index, (center, size, bevel) in enumerate(layers):
        octagonal_layer(f"coil_{layer_index}", center, size, bevel, "compost", "04_compost_coil")
        rim_center = [center[0], center[1] + size[1] / 2 - .35, center[2]]
        octagonal_layer(f"coil_ridge_{layer_index}", rim_center,
                        [size[0] + .12, .4, size[2] + .12], bevel, "ridge", "04_compost_coil")
    cube("curl_base", [6.7, 27.95, 8.15], [2.75, 2.0, 2.9], "compost", "04_compost_coil", "z", -22.5)
    cube("curl_hook", [7.15, 29.0, 8.15], [2.05, 1.6, 2.05], "ridge", "04_compost_coil", "z", -45)
    cube("curl_tip", [8.0, 29.62, 8.15], [1.65, .88, 1.45], "compost", "04_compost_coil")
    for side in (-1, 1):
        for depth_side in (-1, 1):
            brace_x = 8 + side * 4.35
            brace_z = 8 + depth_side * 4.5
            cube(f"cage_talon_{side}_{depth_side}", [brace_x, 17.8, brace_z], [1.05, 5.9, .95],
                 "iron", "03_cage", "z", -side * 22.5)
            cube(f"talon_edge_{side}_{depth_side}", [brace_x + side * .12, 17.8, brace_z + depth_side * .5],
                 [.3, 5.6, .16], "steel", "03_cage", "z", -side * 22.5)
            cube(f"cage_rivet_{side}_{depth_side}", [8 + side * 4.2, 14.3, 8 + depth_side * 5.78],
                 [.62, .62, .35], "brass", "03_cage")
        cube(f"impact_tooth_{side}", [8 + side * 6.45, 15.2, 8], [2.35, 2.35, 2.2],
             "steel", "03_cage", "z", side * 45)
        cube(f"tooth_dark_face_{side}", [8 + side * 6.45, 15.2, 6.82], [1.45, 1.45, .2],
             "iron", "03_cage", "z", side * 45)
    for depth_side in (-1, 1):
        gem_z = 8 + depth_side * 6.02
        for gem_index, material in enumerate(("pink", "amber", "toxic", "cyan", "blue", "violet")):
            gem_x = 4.4 + gem_index * 1.44
            cube(f"prism_socket_{depth_side}_{gem_index}", [gem_x, 14.25, gem_z], [1.19, 1.39, .35],
                 "iron", "05_rainbow_prisms")
            cube(f"prism_{depth_side}_{gem_index}", [gem_x, 14.35, gem_z + depth_side * .26], [.7, .94, .4],
                 material, "05_rainbow_prisms")
    cube("seal_backing", [8, 18.4, 2.38], [3.1, 3.1, .52], "iron", "06_toxic_seal", "z", 45)
    cube("seal_rim", [8, 18.4, 2.03], [2.55, 2.55, .32], "brass", "06_toxic_seal", "z", 45)
    cube("seal_core", [8, 18.4, 1.78], [1.95, 1.95, .3], "toxic", "06_toxic_seal", "z", 45)
    cube("seal_slit_left", [7.64, 18.58, 1.56], [.22, .78, .13], "iron", "06_toxic_seal")
    cube("seal_slit_right", [8.36, 18.58, 1.56], [.22, .78, .13], "iron", "06_toxic_seal")
    cube("seal_mouth", [8, 17.96, 1.56], [.6, .18, .13], "iron", "06_toxic_seal")
    for drip_index, (drip_x, drip_y, length) in enumerate(((3.0, 16.0, 2.0), (12.8, 16.1, 1.25))):
        cube(f"toxic_drip_{drip_index}", [drip_x, drip_y, 3.36], [.43, length, .32],
             "toxic", "06_toxic_seal")
    cube("rear_service_plate", [8, 18.15, 13.46], [2.75, 2.5, .35], "iron", "03_cage")
    for mark_index in range(3):
        cube(f"rear_vent_{mark_index}", [8, 17.55 + mark_index * .58, 13.69], [1.65, .2, .15],
             "toxic", "06_toxic_seal")


def material_tile(material, width, height, face):
    digest = hashlib.sha256(f"{material}:{width}:{height}:{face}".encode()).digest()
    random = np.random.default_rng(int.from_bytes(digest[:8], "little"))
    row, column = np.mgrid[0:height, 0:width]
    horizontal = (column + .5) / width
    vertical = (row + .5) / height
    edge = np.minimum.reduce((column, width - 1 - column, row, height - 1 - row))
    brightness = np.full((height, width), 1.0)
    brightness += random.uniform(-.035, .035, (height, width))
    if material in {"compost", "ridge"}:
        brightness += .19 * (1 - vertical) - .12 * vertical
        brightness += .08 * np.sin(horizontal * 13 + np.sin(vertical * 8))
        brightness -= .1 * (edge == 0)
        flecks = random.random((height, width))
        brightness += np.where(flecks > .967, .16, 0)
        brightness -= np.where(flecks < .023, .20, 0)
    elif material == "leather":
        brightness += .12 * (1 - horizontal) - .08 * vertical
        brightness -= .18 * (edge == 0)
        brightness += .07 * ((column + row * 2) % 7 == 0)
    elif material in GEMS:
        brightness += .36 * (1 - vertical) + .12 * (horizontal < .3)
        brightness -= .23 * (horizontal > .8)
        brightness += .17 * (column == row % width)
        brightness -= .16 * (edge == 0)
    else:
        brightness += .14 * (1 - vertical) + .09 * np.cos(horizontal * math.pi)
        brightness -= .18 * (edge == 0)
        brightness += .25 * ((row == 1) | (column == 1))
        brightness += .16 * ((row * 7 + column * 13) % 61 == 0)
    if face == "up":
        brightness += .06
    elif face == "down":
        brightness -= .08
    colors = np.clip(np.array(PALETTE[material])[None, None, :] * brightness[:, :, None], 0, 255).astype(np.uint8)
    alpha = np.full((height, width, 1), 255, dtype=np.uint8)
    return Image.fromarray(np.concatenate((colors, alpha), axis=2))


def create_texture():
    atlas = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
    cache = {}
    cursor_x, cursor_y, row_height = 1, 1, 0
    for part in PARTS:
        element = part["element"]
        width, height, depth = [end - start for start, end in zip(element["from"], element["to"])]
        face_sizes = {"north": (width, height), "south": (width, height), "east": (depth, height),
                      "west": (depth, height), "up": (width, depth), "down": (width, depth)}
        element["faces"] = {}
        for face, (span_u, span_v) in face_sizes.items():
            pixels_u = max(2, round(span_u * PIXELS_PER_UNIT))
            pixels_v = max(2, round(span_v * PIXELS_PER_UNIT))
            face_kind = face if face in {"up", "down"} else "side"
            cache_key = (part["material"], pixels_u, pixels_v, face_kind)
            if cache_key not in cache:
                if cursor_x + pixels_u + 1 > TEXTURE_SIZE:
                    cursor_x, cursor_y, row_height = 1, cursor_y + row_height + 2, 0
                if cursor_y + pixels_v + 1 > TEXTURE_SIZE:
                    raise ValueError("Texture atlas overflow")
                tile = material_tile(part["material"], pixels_u, pixels_v, face_kind)
                atlas.paste(tile, (cursor_x, cursor_y))
                atlas.paste(tile.crop((0, 0, pixels_u, 1)), (cursor_x, cursor_y - 1))
                atlas.paste(tile.crop((0, pixels_v - 1, pixels_u, pixels_v)), (cursor_x, cursor_y + pixels_v))
                atlas.paste(tile.crop((0, 0, 1, pixels_v)), (cursor_x - 1, cursor_y))
                atlas.paste(tile.crop((pixels_u - 1, 0, pixels_u, pixels_v)), (cursor_x + pixels_u, cursor_y))
                cache[cache_key] = [value * 16 / TEXTURE_SIZE for value in
                                    (cursor_x, cursor_y, cursor_x + pixels_u, cursor_y + pixels_v)]
                cursor_x += pixels_u + 2
                row_height = max(row_height, pixels_v)
            element["faces"][face] = {"uv": cache[cache_key], "texture": "#0"}
    return atlas, len(cache)


def display_settings():
    return {
        "thirdperson_righthand": {"rotation": [0, 90, -10], "translation": [0, 5, 1], "scale": [.63, .63, .63]},
        "thirdperson_lefthand": {"rotation": [0, -90, 10], "translation": [0, 5, 1], "scale": [.63, .63, .63]},
        "firstperson_righthand": {"rotation":[0,145,-15],"translation":[0.727102,3.419752,1.09587],"scale":[0.5,0.5,0.5]},
        "firstperson_lefthand": {"rotation":[0,145,-15],"translation":[0.727102,3.419752,1.09587],"scale":[0.5,0.5,0.5]},
        "gui": {"rotation": [15, -35, -30], "translation": [0, -1.0, 0], "scale": [.38, .38, .38]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 5, 0], "scale": [.33, .33, .33]},
        "fixed": {"rotation": [0, 180, 0], "translation": [0, -1, -1], "scale": [.38, .38, .38]},
        "head": {"rotation": [0, 0, 0], "translation": [0, 13, 0], "scale": [.4, .4, .4]},
    }


def save_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def stable_uuid(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "ftboss:compost_crown/" + name))


def export_blockbench(model, atlas_path):
    elements, groups = [], {}
    for part in PARTS:
        element = part["element"]
        identifier = stable_uuid(element["name"])
        faces = {face: {"uv": [value * TEXTURE_SIZE / 16 for value in data["uv"]], "texture": 0}
                 for face, data in element["faces"].items()}
        entry = {"name": element["name"], "uuid": identifier, "type": "cube", "box_uv": False,
                 "from": element["from"], "to": element["to"], "faces": faces,
                 "origin": [(start + end) / 2 for start, end in zip(element["from"], element["to"])],
                 "autouv": 0, "color": list(PALETTE).index(part["material"]) % 8}
        if "rotation" in element:
            entry["rotation"] = [element["rotation"]["angle"] if axis == element["rotation"]["axis"] else 0
                                 for axis in ("x", "y", "z")]
        elements.append(entry)
        groups.setdefault(part["group"], []).append(identifier)
    project = {
        "meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
        "name": "污秽虹冠 · Compost Crown", "model_identifier": MODEL_ID,
        "visible_box": [1, 1, 0], "resolution": {"width": TEXTURE_SIZE, "height": TEXTURE_SIZE},
        "ambientocclusion": False, "front_gui_light": True,
        "elements": elements,
        "outliner": [{"name": name, "uuid": stable_uuid(name), "origin": [8, 8, 8],
                      "isOpen": False, "children": children} for name, children in groups.items()],
        "textures": [{"path": "", "name": MODEL_ID + ".png", "folder": "item", "namespace": "ftboss",
                      "id": "0", "uuid": stable_uuid("texture"), "particle": True,
                      "width": TEXTURE_SIZE, "height": TEXTURE_SIZE,
                      "uv_width": TEXTURE_SIZE, "uv_height": TEXTURE_SIZE,
                      "source": "data:image/png;base64," + base64.b64encode(atlas_path.read_bytes()).decode()}],
        "display": model["display"],
    }
    save_json(OUTPUT / (MODEL_ID + ".bbmodel"), project)


def validate(model, atlas):
    assert "parent" not in model
    assert len(model["display"]) == 8
    allowed_angles = {-45, -22.5, 0, 22.5, 45}
    for element in model["elements"]:
        assert all(-16 <= value <= 32 for value in element["from"] + element["to"])
        assert all(end > start for start, end in zip(element["from"], element["to"]))
        if "rotation" in element:
            assert element["rotation"]["angle"] in allowed_angles
        for face in element["faces"].values():
            assert all(0 <= value <= 16 for value in face["uv"])
            pixel_rect = tuple(round(value * TEXTURE_SIZE / 16) for value in face["uv"])
            assert atlas.crop(pixel_rect).getchannel("A").getextrema() == (255, 255)
    return {"elements": len(model["elements"]), "faces": len(model["elements"]) * 6,
            "texture": [TEXTURE_SIZE, TEXTURE_SIZE], "display_slots": 8,
            "valid_java_model": True, "opaque_uv_faces": True, "minecraft_version": "1.20.1",
            "artifact_type": "static_item_model", "preview_custom_model_data": 160916}


def main():
    parser = argparse.ArgumentParser(description="Generate only the Compost Crown model and editable project.")
    parser.add_argument("--install", action="store_true", help="Also copy this weapon's model and texture into src/main/resources.")
    arguments = parser.parse_args()
    OUTPUT.mkdir(parents=True, exist_ok=True)
    make_geometry()
    atlas, tile_count = create_texture()
    texture_dir = ASSETS / "textures" / "item"
    texture_dir.mkdir(parents=True, exist_ok=True)
    atlas_path = texture_dir / (MODEL_ID + ".png")
    atlas.save(atlas_path)
    atlas.save(OUTPUT / (MODEL_ID + ".png"))
    model = {"credit": "FTBoss / Compost Crown / 2026-09-16", "ambientocclusion": False, "gui_light": "front",
             "textures": {"0": "ftboss:item/compost_crown", "particle": "ftboss:item/compost_crown"},
             "display": display_settings(), "elements": [part["element"] for part in PARTS]}
    report = validate(model, atlas)
    report["atlas_tiles"] = tile_count
    save_json(ASSETS / "models" / "item" / (MODEL_ID + ".json"), model)
    save_json(OUTPUT / (MODEL_ID + ".json"), model)
    export_blockbench(model, atlas_path)
    save_json(PACK / "pack.mcmeta", {"pack": {"pack_format": 15,
              "description": "FTBoss · 污秽虹冠 | 仅模型预览 · 木斧 CustomModelData 160916"}})
    save_json(ASSETS / "models" / "item" / "preview_wooden_axe.json",
              {"parent": "minecraft:item/handheld", "textures": {"layer0": "minecraft:item/wooden_axe"}})
    save_json(PACK / "assets" / "minecraft" / "models" / "item" / "wooden_axe.json", {
        "parent": "ftboss:item/preview_wooden_axe",
        "overrides": [{"predicate": {"custom_model_data": 160916}, "model": "ftboss:item/compost_crown"},
                      {"predicate": {"custom_model_data": 160917}, "model": "ftboss:item/preview_wooden_axe"}]})
    save_json(OUTPUT / "validation.json", report)
    if arguments.install:
        destination = PROJECT / "src" / "main" / "resources" / "assets" / "ftboss"
        for relative in ("models/item/compost_crown.json", "textures/item/compost_crown.png"):
            target = destination / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ASSETS / relative, target)
        print("Installed model and texture only:", destination)
    print(json.dumps(report, ensure_ascii=False, indent=2))
    print("Model:", OUTPUT / (MODEL_ID + ".bbmodel"))
    print("Preview pack (not enabled):", PACK)


if __name__ == "__main__":
    main()
