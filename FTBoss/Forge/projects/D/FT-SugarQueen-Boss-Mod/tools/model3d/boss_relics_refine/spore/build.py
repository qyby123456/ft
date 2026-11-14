from __future__ import annotations

import base64
import importlib.util
import math
import sys
from pathlib import Path


PROJECT = Path(__file__).resolve().parents[4]
MATERIAL_TOOLS = PROJECT / "tools/model3d/boss_materials_refine/spore"
SPEC = importlib.util.spec_from_file_location("spore_material_authoring", MATERIAL_TOOLS / "build.py")
LIBRARY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(LIBRARY)
ASSETS = PROJECT / "src/main/resources/assets/ftboss"
OUTPUT = PROJECT / "art/boss_relics/refine/spore"
WORK = PROJECT / "_work/boss-relics-refine-20261021/spore"
IDENTIFIER = "rainbow_spore_charm"
LIBRARY.OUTPUT = OUTPUT
LIBRARY.WORK = WORK
LIBRARY.PALETTE["bronze"] = [(56, 43, 39), (100, 73, 48), (151, 112, 67), (195, 155, 94), (231, 200, 132)]
LIBRARY.PALETTE["patina"] = [(29, 42, 49), (41, 62, 65), (59, 88, 87), (90, 122, 110), (142, 159, 129)]
LIBRARY.PALETTE["parchment"] = [(71, 56, 57), (117, 92, 80), (165, 137, 105), (206, 182, 141), (237, 218, 173)]
LIBRARY.PALETTE["seal"] = [(29, 27, 36), (44, 36, 49), (66, 48, 65), (89, 64, 82), (119, 89, 103)]
cube = LIBRARY.cube
save_json = LIBRARY.save_json


def segment(name, first, second, width, depth, material, group, center_z):
    center = [(first[axis] + second[axis]) / 2 for axis in range(2)] + [center_z]
    delta_x, delta_y = second[0] - first[0], second[1] - first[1]
    angle = math.degrees(math.atan2(delta_y, delta_x)) % 180
    if angle > 90:
        angle -= 180
    length = math.hypot(delta_x, delta_y) + .18
    if abs(angle) > 45.01:
        angle = angle - 90 if angle > 0 else angle + 90
        dimensions = [width, length, depth]
    else:
        dimensions = [length, width, depth]
    angle = round(angle, 5)
    assert angle in {-45, -22.5, 0, 22.5, 45}
    cube(name, center, dimensions, material, group, "z", angle)


def loop(prefix, center_x, center_y, radius_x, radius_y, center_z, thickness, material, group):
    corner = min(radius_x, radius_y) * .59
    points = [[center_x - radius_x + corner, center_y + radius_y],
              [center_x + radius_x - corner, center_y + radius_y],
              [center_x + radius_x, center_y + radius_y - corner],
              [center_x + radius_x, center_y - radius_y + corner],
              [center_x + radius_x - corner, center_y - radius_y],
              [center_x - radius_x + corner, center_y - radius_y],
              [center_x - radius_x, center_y - radius_y + corner],
              [center_x - radius_x, center_y + radius_y - corner]]
    for index, first in enumerate(points):
        segment(f"{prefix}_{index}", first, points[(index + 1) % len(points)], thickness,
                thickness * 1.38, material, group, center_z + (index % 2) * .23)


def geometry():
    for index, (width, center_y) in enumerate([(4.4, 4.95), (6.45, 6.0), (7.35, 7.05),
                                              (7.35, 8.10), (6.45, 9.15), (4.4, 10.20)]):
        cube(f"forged_octagonal_back_{index}", [8, center_y, 8.2 + index * .012],
             [width, 1.055, 1.10], "patina", "00_forged_pendant_body")
    loop("beveled_brass_frame", 8, 7.55, 3.65, 3.30, 7.43, .63, "bronze", "01_burnished_frame")
    for index, (width, center_y) in enumerate([(3.2, 5.20), (5.3, 6.15), (6.1, 7.1),
                                              (6.1, 8.05), (5.3, 9.0), (3.2, 9.95)]):
        cube(f"pressed_velvet_seal_{index}", [8, center_y, 7.31 + index * .007],
             [width, .96, .53], "seal", "02_recessed_fungal_seal")
    for index, (center_x, top_y, width, center_z) in enumerate([
        (5.70, 8.91, 1.03, 6.64), (6.75, 9.60, 1.16, 6.43),
        (8, 9.93, 1.43, 6.23), (9.25, 9.60, 1.16, 6.43), (10.3, 8.91, 1.03, 6.64),
    ]):
        bottom_y = 8.14 + .06 * abs(center_x - 8)
        cube(f"carved_mushroom_cap_{index}", [center_x, (top_y + bottom_y) / 2, center_z],
             [width, top_y - bottom_y, .67], "compost", "03_carved_mushroom_symbol")
        cube(f"celadon_enamel_lip_{index}", [center_x + .035, bottom_y, center_z - .16],
             [width + .08, .31, .67], "rim", "03_carved_mushroom_symbol")
    for side in [-1, 1]:
        cube(f"cap_beveled_shoulder_{side}", [8 + side * 1.85, 9.27, 6.5], [1.8, .43, .73],
             "cap", "03_carved_mushroom_symbol", "z", -side * 22.5)
    for index in range(7):
        center_x = 5.65 + index * .78
        angle = -45 if index < 2 else 45 if index > 4 else -22.5 if index == 2 else 22.5 if index == 4 else 0
        cube(f"engraved_gill_{index}", [center_x, 7.98, 6.76], [.22, .71, .39],
             "gill", "04_gill_engraving", "z", angle)
    cube("carved_ivory_stem", [8, 6.85, 6.65], [1.20, 2.45, 1.10], "fiber", "03_carved_mushroom_symbol", "z", -22.5)
    cube("stem_base_collar", [8.35, 5.87, 6.55], [1.93, .42, 1.30], "bronze", "05_gem_setting")
    cube("stem_top_collar", [7.63, 7.69, 6.53], [1.7, .30, 1.27], "bronze", "05_gem_setting")
    for index, (center_x, center_y, angle) in enumerate([(5.62, 6.17, -22.5), (10.38, 6.17, 22.5)]):
        cube(f"scroll_vine_{index}", [center_x, center_y, 6.96], [.37, 1.85, .39], "bronze", "06_metal_filagree", "z", angle)
        cube(f"scroll_leaf_{index}", [center_x + (.42 if index else -.42), center_y + .25, 6.91],
             [.55, .92, .43], "rim", "06_metal_filagree", "z", -45 if index else 45)
    for index, (center_x, center_y) in enumerate([(4.42, 7.5), (11.58, 7.5), (8, 4.3)]):
        cube(f"frame_rivet_socket_{index}", [center_x, center_y, 7.00], [.76, .76, .43],
             "patina", "05_gem_setting", "z", 45)
        cube(f"frame_rivet_{index}", [center_x, center_y, 6.79], [.35, .35, .42],
             "bronze", "05_gem_setting", "z", 45)
    for index, (center_x, center_y, material) in enumerate([(6.60, 9.00, "violet"), (9.38, 8.94, "pink"),
                                                         (7.98, 9.52, "lime")]):
        cube(f"spore_cabochon_socket_{index}", [center_x, center_y, 5.99], [.87, .87, .56],
             "bronze", "05_gem_setting", "z", 45)
        cube(f"spore_cabochon_{index}", [center_x, center_y, 5.67], [.56, .56, .48],
             material, "07_iridescent_spore_inlays", "z", 45)
    cube("central_cyan_bezel", [8.12, 6.58, 5.95], [1.30, 1.30, .60], "bronze", "05_gem_setting", "z", 45)
    cube("central_cyan_spore_cut", [8.12, 6.58, 5.58], [.93, .93, .57], "cyan", "07_iridescent_spore_inlays", "z", 45)
    cube("central_cyan_table", [8.12, 6.58, 5.26], [.48, .48, .16], "cyan", "07_iridescent_spore_inlays", "z", 45)
    loop("top_soldered_eyelet", 8, 11.57, .76, 1.08, 8.05, .43, "bronze", "08_wearable_bail_and_link")
    loop("chain_link", 8, 13.05, .85, .78, 8.28, .30, "bronze", "08_wearable_bail_and_link")
    cube("bail_joint_front", [8, 10.75, 7.37], [1.50, .50, 1.18], "bronze", "08_wearable_bail_and_link")
    cube("bail_joint_patina", [8, 10.75, 6.74], [.81, .27, .35], "rim", "08_wearable_bail_and_link")
    loop("bottom_jump_ring", 8, 3.55, .42, .63, 7.82, .31, "bronze", "09_suspended_spore_drop")
    cube("drop_gem_cage", [8, 2.50, 7.83], [1.20, 1.40, .82], "bronze", "09_suspended_spore_drop", "z", 45)
    cube("drop_gem_body", [8, 2.50, 7.39], [.83, 1.07, .62], "violet", "09_suspended_spore_drop", "z", 45)
    cube("drop_gem_table", [8, 2.50, 7.04], [.44, .65, .14], "pink", "09_suspended_spore_drop", "z", 45)
    loop("reverse_stamped_border", 8, 7.55, 2.65, 2.42, 8.88, .23, "bronze", "10_reverse_hallmark")
    cube("reverse_parchment_field", [8, 7.5, 8.91], [2.9, 3.1, .34], "parchment", "10_reverse_hallmark")
    for index, (center, dimensions) in enumerate([
        ([8, 7.3, 9.15], [.40, 1.7, .24]), ([8, 8.2, 9.15], [1.70, .32, .24]),
        ([8, 8.52, 9.15], [1.05, .28, .24]), ([8, 6.38, 9.15], [1.55, .30, .24]),
    ]):
        cube(f"reverse_spore_sigill_{index}", center, dimensions, "patina", "10_reverse_hallmark")
    for side in [-1, 1]:
        cube(f"back_riveted_lug_{side}", [8 + side * 1.91, 9.12, 8.98], [.48, .48, .43],
             "bronze", "10_reverse_hallmark", "z", 45)


def export_blockbench(model, texture_path):
    entries, groups = [], {}
    for part in LIBRARY.PARTS:
        element = part["element"]
        rotation = element.get("rotation", {})
        entry = {"name": element["name"], "uuid": LIBRARY.stable_id("relic/" + element["name"]), "type": "cube",
                 "from": element["from"], "to": element["to"], "box_uv": False, "autouv": 0,
                 "origin": rotation.get("origin", [(start + finish) / 2 for start, finish in zip(element["from"], element["to"])]),
                 "rotation": [rotation.get("angle", 0) if rotation.get("axis") == axis else 0 for axis in "xyz"],
                 "faces": {face: {"uv": [value * LIBRARY.SIZE / 16 for value in data["uv"]], "texture": 0}
                           for face, data in element["faces"].items()}}
        entries.append(entry)
        groups.setdefault(part["group"], []).append(entry["uuid"])
    project = {"meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
               "name": "虹彩孢子符 · 镶晶菌印", "model_identifier": IDENTIFIER,
               "resolution": {"width": LIBRARY.SIZE, "height": LIBRARY.SIZE}, "visible_box": [1, 1, 0],
               "ambientocclusion": False, "front_gui_light": False, "elements": entries,
               "outliner": [{"name": name, "uuid": LIBRARY.stable_id("relic/" + name), "origin": [8, 8, 8], "children": children}
                            for name, children in groups.items()],
               "textures": [{"name": IDENTIFIER + ".png", "path": "", "folder": "item/boss_relics", "namespace": "ftboss",
                             "id": "0", "uuid": LIBRARY.stable_id("relic_texture"), "width": LIBRARY.SIZE, "height": LIBRARY.SIZE,
                             "uv_width": LIBRARY.SIZE, "uv_height": LIBRARY.SIZE,
                             "source": "data:image/png;base64," + base64.b64encode(texture_path.read_bytes()).decode()}],
               "display": model["display"]}
    save_json(PROJECT / "art/boss_relics/blockbench" / (IDENTIFIER + ".bbmodel"), project)


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    WORK.mkdir(parents=True, exist_ok=True)
    geometry()
    original_assets = LIBRARY.ASSETS
    LIBRARY.ASSETS = WORK / "staging"
    temporary_atlas = LIBRARY.ASSETS / "textures/item/boss_materials/spore.png"
    temporary_atlas.parent.mkdir(parents=True, exist_ok=True)
    LIBRARY.texture_atlas()
    LIBRARY.ASSETS = original_assets
    atlas_path = ASSETS / "textures/item/boss_relics" / (IDENTIFIER + ".png")
    atlas_path.write_bytes(temporary_atlas.read_bytes())
    model = {"ambientocclusion": False, "gui_light": "side",
             "textures": {"atlas": "ftboss:item/boss_relics/" + IDENTIFIER, "particle": "ftboss:item/" + IDENTIFIER},
             "elements": [part["element"] for part in LIBRARY.PARTS],
             "display": {
                 "thirdperson_righthand": {"rotation": [0, 90, -10], "translation": [0, 1.5, 1], "scale": [.65] * 3},
                 "thirdperson_lefthand": {"rotation": [0, -90, 10], "translation": [0, 1.5, 1], "scale": [.65] * 3},
                 "firstperson_righthand": {"rotation": [0, 145, -12], "translation": [1, 2, 1], "scale": [.7] * 3},
                 "firstperson_lefthand": {"rotation": [0, -145, 12], "translation": [1, 2, 1], "scale": [.7] * 3},
                 "ground": {"rotation": [0, 0, 0], "translation": [0, 2.6, 0], "scale": [.5] * 3},
                 "fixed": {"rotation": [0, 180, 0], "translation": [0, .5, 0], "scale": [.85] * 3},
                 "head": {"rotation": [0, 0, 0], "translation": [0, 7, 0], "scale": [.6] * 3},
                 "gui": {"rotation": [0, -12, 0], "translation": [0, 0, 0], "scale": [.95] * 3},
             }}
    save_json(ASSETS / "models/item/boss_relics" / (IDENTIFIER + ".json"), model)
    save_json(ASSETS / "models/item/boss_relics" / (IDENTIFIER + "_gui.json"), {
        "parent": "minecraft:item/generated", "textures": {"layer0": "ftboss:item/" + IDENTIFIER}})
    save_json(ASSETS / "models/item" / (IDENTIFIER + ".json"), {
        "loader": "forge:separate_transforms", "ambientocclusion": False, "gui_light": "front",
        "textures": {"particle": "ftboss:item/" + IDENTIFIER},
        "base": {"parent": "ftboss:item/boss_relics/" + IDENTIFIER},
        "perspectives": {"gui": {"parent": "ftboss:item/boss_relics/" + IDENTIFIER + "_gui"}}})
    export_blockbench(model, atlas_path)
    sys.path.insert(0, str(MATERIAL_TOOLS))
    import preview
    import numpy as np
    from PIL import Image
    texture = np.array(Image.open(atlas_path).convert("RGBA"))
    preview.render(model, texture, (32, 32), yaw=-8, pitch=3, margin=2, outline=True).save(
        ASSETS / "textures/item" / (IDENTIFIER + ".png"))
    print("Relic generated:", len(LIBRARY.PARTS), "elements")


if __name__ == "__main__":
    main()
