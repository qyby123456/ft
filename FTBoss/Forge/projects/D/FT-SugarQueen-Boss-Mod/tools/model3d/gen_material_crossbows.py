import base64
import json
import math
from pathlib import Path
import random
import uuid

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/ftboss"
DATA = ROOT / "src/main/resources/data/ftboss"
ART = ROOT / "art/material_crossbows"
PARTS = ("body", "left", "right", "latch", "string_left", "string_right", "arrow", "firework")
VARIANTS = (
    ("wooden", "木弩", "Wooden", "#966737", "#c29857", "#563d26"),
    ("stone", "石弩", "Stone", "#777b7b", "#a8abaa", "#424847"),
    ("iron", "铁弩", "Iron", "#bdcbd0", "#eef5f2", "#56636b"),
    ("golden", "金弩", "Golden", "#edb72f", "#fff19a", "#9b5f17"),
    ("diamond", "钻石弩", "Diamond", "#21bdb0", "#a2fff3", "#087b85"),
    ("netherite", "下界合金弩", "Netherite", "#49404c", "#887381", "#27232c"),
)
DISPLAY = {
    "gui": {"rotation": [28, 135, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.4, 0.4, 0.4]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "firstperson_righthand": {"rotation": [0, 0, 0], "translation": [2, -1.75, -3.5], "scale": [0.65, 0.65, 0.65]},
    "firstperson_lefthand": {"rotation": [0, 0, 0], "translation": [-2, -1.75, -3.5], "scale": [0.65, 0.65, 0.65]},
    "thirdperson_righthand": {"rotation": [16.75, 0, 0], "translation": [0, 3, -3.5], "scale": [0.65, 0.65, 0.65]},
    "thirdperson_lefthand": {"rotation": [16.75, 0, 0], "translation": [0, 3, -3.5], "scale": [0.65, 0.65, 0.65]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 0], "scale": [0.65, 0.65, 0.65]},
}


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def identity(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "ftboss/material_crossbows/" + name))


def atlas(variant):
    material, _, _, main, light, dark = variant
    palette = [main, light, dark, "#72502f", "#ba8b50", "#3e2d22", "#c5c4ae", "#46494d",
               "#e8debb", "#ab3038", "#f7edd8", "#c9a04b", "#416b65", "#96794c", "#302c32", "#ccc3b4"]
    image = Image.new("RGBA", (64, 64))
    draw = ImageDraw.Draw(image)
    rng = random.Random(material)
    for tile, color in enumerate(palette):
        tile_x, tile_y = tile % 4 * 16, tile // 4 * 16
        base = tuple(bytes.fromhex(color[1:]))
        draw.rectangle((tile_x, tile_y, tile_x + 15, tile_y + 15), fill=base)
        for row in range(16):
            for column in range(16):
                shade = rng.choice((-10, -5, 0, 0, 0, 5, 10))
                if tile in (3, 4, 5) or (material == "wooden" and tile < 3):
                    shade += -14 if column % 5 == 0 else 0
                if tile in (0, 1, 2) and material == "stone":
                    shade += -17 if (column + 2 * row) % 13 == 0 else 0
                pixel = tuple(max(0, min(255, channel + shade)) for channel in base)
                image.putpixel((tile_x + column, tile_y + row), (*pixel, 255))
        draw.line((tile_x, tile_y, tile_x + 15, tile_y), fill=tuple(min(255, channel + 23) for channel in base))
        draw.line((tile_x, tile_y, tile_x, tile_y + 15), fill=tuple(min(255, channel + 13) for channel in base))
    path = ASSETS / "textures/item" / f"{material}_crossbow.png"
    image.save(path)
    return path


def geometry(material):
    parts = {part: [] for part in PARTS}

    def cube(part, name, start, end, tile):
        texture_x, texture_y = tile % 4 * 4, tile // 4 * 4
        dimensions = [end[axis] - start[axis] for axis in range(3)]
        faces = {}
        for face in ("north", "south", "east", "west", "up", "down"):
            width = dimensions[0] if face in ("north", "south", "up", "down") else dimensions[2]
            height = dimensions[2] if face in ("up", "down") else dimensions[1]
            faces[face] = {"uv": [texture_x, texture_y, texture_x + min(4, width / 2), texture_y + min(4, height / 2)], "texture": "#0"}
        parts[part].append({"name": name, "from": start, "to": end, "faces": faces})

    heavy = material in ("stone", "netherite")
    half = 2.3 if heavy else 1.8
    cube("body", "walnut_stock", [8 - half, 6, 0], [8 + half, 8.8, 18], 3 if material != "netherite" else 5)
    cube("body", "foregrip", [6.6, 4.8, 2], [9.4, 6.2, 8], 5)
    cube("body", "pistol_grip", [6.7, 1.8, 13], [9.3, 6.3, 16], 5)
    for band in range(3):
        cube("body", f"grip_wrap_{band}", [6.6, 2.2 + band * 1.15, 13.1], [9.4, 2.6 + band * 1.15, 16.1], 3)
    cube("body", "heel", [6.2, 5.7, 18], [9.8, 9, 20.5], 0)
    cube("body", "butt_plate", [5.9, 5.4, 20], [10.1, 9.3, 21], 2)
    cube("body", "left_rail", [6.5, 8.8, -3], [7.55, 9.3, 12], 0)
    cube("body", "right_rail", [8.45, 8.8, -3], [9.5, 9.3, 12], 0)
    cube("body", "dark_bolt_channel", [7.55, 8.75, -3], [8.45, 9.1, 12], 7)
    cube("body", "front_mount", [3.5, 6.6, -0.1], [12.5, 8.1, 2.7], 2)
    cube("body", "trigger", [7.65, 3.8, 10], [8.35, 5.9, 10.8], 7)
    cube("body", "trigger_guard_front", [7.5, 3.8, 8.5], [8.5, 5.9, 9.2], 2)
    cube("body", "trigger_guard_bottom", [7.5, 3.1, 8.5], [8.5, 3.8, 13.2], 2)
    cube("body", "rear_sight_left", [6.7, 8.8, 13], [7.5, 10.7, 14], 2)
    cube("body", "rear_sight_right", [8.5, 8.8, 13], [9.3, 10.7, 14], 2)
    cube("body", "fore_sight_left", [6.55, 9.3, -3.1], [6.95, 10.7, -2.5], 1)
    cube("body", "fore_sight_right", [9.05, 9.3, -3.1], [9.45, 10.7, -2.5], 1)
    cube("body", "bolt_saddle", [7.6, 9.1, -1.2], [8.4, 9.35, -0.4], 2)
    for side in ("left", "right"):
        def limb(name, start, end, tile):
            if side == "right":
                start, end = [16 - end[0], start[1], start[2]], [16 - start[0], end[1], end[2]]
            cube(side, side + "_" + name, start, end, tile)

        thickness = 1.9 if heavy else 1.35
        limb("inner_arm", [-2, 8, -0.5], [4.3, 8 + thickness, 1.6], 0)
        limb("outer_arm", [-6.6, 8, -2.7], [-2, 8 + thickness, -0.2], 0)
        limb("tip_cap", [-6.8, 7.9, -2.8], [-5.7, 8 + thickness + 0.15, -0.1], 2)
        limb("pivot_plate", [2.7, 7.75, -0.8], [4.6, 8 + thickness + 0.3, 1.8], 2)
        limb("pivot_pin", [3.25, 8 + thickness + 0.3, 0], [4.05, 8 + thickness + 0.55, 0.8], 1)
        if material == "wooden":
            limb("binding", [-2.9, 7.9, -0.65], [-1.6, 9.55, 1.65], 8)
        if material == "stone":
            limb("chipped_ridge", [-5.8, 9.9, -2.4], [-3.5, 10.7, -0.6], 1)
            limb("basalt_shoulder", [-0.4, 9.9, -0.3], [2.1, 10.5, 1.3], 2)
        if material == "iron":
            limb("steel_edge", [-5.9, 9.35, -2.5], [-1.8, 9.75, -0.5], 1)
            limb("steel_rib", [-1.5, 9.35, -0.2], [2.6, 9.75, 1.3], 1)
        if material == "golden":
            limb("crown_fin", [-5.4, 9.35, -2.4], [-3.9, 11.3, -0.5], 1)
            limb("engraved_edge", [-1.5, 9.35, -0.2], [2.6, 9.8, 1.3], 11)
            limb("emerald_rivet", [-0.3, 9.8, 0.1], [0.6, 10.35, 0.95], 12)
        if material == "diamond":
            limb("crystal_edge", [-6, 9.35, -2.55], [-2.1, 9.95, -0.4], 1)
            limb("crystal_shoulder", [-1.3, 9.35, -0.3], [2.3, 10.2, 1.4], 1)
            limb("crystal_peak", [-0.6, 10.2, 0], [1.4, 10.65, 1.1], 0)
        if material == "netherite":
            limb("armored_edge", [-5.8, 9.9, -2.6], [-2, 10.6, -0.4], 1)
            limb("gilded_seam", [-1.5, 9.9, -0.3], [2.5, 10.15, 1.4], 11)
            limb("armored_shoulder", [0, 10.15, -0.1], [2.2, 11.1, 1.2], 2)
    cube("latch", "sliding_latch_base", [7.48, 9, -2.5], [8.52, 9.35, -1.5], 2)
    cube("latch", "sliding_latch_left", [7.48, 9.35, -2.5], [7.7, 9.4, -1.5], 2)
    cube("latch", "sliding_latch_right", [8.3, 9.35, -2.5], [8.52, 9.4, -1.5], 2)
    cube("latch", "string_catch", [7.82, 9.35, -1.9], [8.18, 9.65, -1.65], 2)
    cube("string_left", "bowstring_left", [-6, 9.45, -2.12], [8, 9.65, -1.88], 8)
    cube("string_right", "bowstring_right", [8, 9.45, -2.12], [22, 9.65, -1.88], 8)
    if material != "wooden":
        cube("body", "receiver_armor", [5.8, 6.4, 8], [10.2, 8.95, 11.9], 0)
        cube("body", "left_inlay", [5.65, 7.4, 8.7], [5.85, 8, 11.3], 1 if material != "netherite" else 11)
        cube("body", "right_inlay", [10.15, 7.4, 8.7], [10.35, 8, 11.3], 1 if material != "netherite" else 11)
    if material in ("diamond", "netherite"):
        cube("body", "butt_upper_armor", [6.1, 9, 17], [9.9, 9.9, 20.5], 0)
        cube("body", "butt_crest", [7, 9.9, 18], [9, 10.3, 20], 1 if material == "diamond" else 11)
    cube("arrow", "bolt_shaft", [7.8, 9.35, -8], [8.2, 9.75, 7], 4)
    cube("arrow", "bolt_head", [7.4, 9.25, -8.8], [8.6, 9.85, -7.2], 6)
    cube("arrow", "bolt_point", [7.75, 9.35, -9.5], [8.25, 9.75, -8.8], 1)
    cube("arrow", "bolt_vanes_horizontal", [6.8, 9.85, 4.5], [9.2, 10.05, 6.3], 10)
    cube("arrow", "bolt_vanes_vertical", [7.85, 9.7, 4.45], [8.15, 10.8, 6.35], 10)
    cube("firework", "rocket_stick", [7.85, 9.35, -3], [8.15, 9.9, 7], 4)
    cube("firework", "rocket_paper", [7.1, 9.8, -6], [8.9, 11.6, 1], 10)
    for band in range(3):
        cube("firework", f"rocket_red_band_{band}", [7.05, 9.75, -5.7 + band * 2.2], [8.95, 11.65, -4.8 + band * 2.2], 9)
    cube("firework", "rocket_cap", [6.9, 9.8, -6.8], [9.1, 12, -6], 9)
    cube("firework", "rocket_tip", [7.45, 10.35, -7.6], [8.55, 11.45, -6.8], 9)
    for side in ("left", "right"):
        for element in parts[side]:
            element["from"][1] -= 1.95
            element["to"][1] -= 1.95
        anchor_x = [-6.2, -5.8] if side == "left" else [21.8, 22.2]
        cube(side, side + "_string_anchor", [anchor_x[0], 7.3, -2.15], [anchor_x[1], 9.65, -1.85], 2)
    return parts


def blockbench(item_id, parts, texture):
    elements, groups = [], []
    for part, cubes in parts.items():
        children = []
        pivot = [4, 8, 1] if part == "left" else [12, 8, 1] if part == "right" else [8, 9.55, -2]
        for index, cube in enumerate(cubes):
            element = json.loads(json.dumps(cube))
            element["uuid"] = identity(f"{item_id}/{part}/{index}")
            element.update({"type": "cube", "origin": pivot, "box_uv": False, "rescale": False})
            for face in element["faces"].values():
                face["uv"] = [value * 4 for value in face["uv"]]
                face["texture"] = 0
            if part in ("arrow", "firework"):
                element["visibility"] = False
            elements.append(element)
            children.append(element["uuid"])
        groups.append({"name": part, "origin": pivot, "uuid": identity(f"{item_id}/{part}"), "children": children})
    animators = {}
    for part in PARTS[1:6]:
        keyframes = []
        for fraction in (0, 0.25, 0.5, 0.75, 1):
            progress = fraction * fraction * (3 - 2 * fraction)
            channels = {"position": [0, 0, 0], "rotation": [0, 0, 0], "scale": [1, 1, 1]}
            if part in ("left", "right"):
                channels["rotation"][1] = (1 if part == "left" else -1) * 22.5 * progress
            elif part == "latch":
                channels["position"][2] = 9 * progress
            else:
                angle = math.radians(22.5 * progress)
                tip_x = 4 - 10 * math.cos(angle) - 3 * math.sin(angle)
                tip_z = 1 + 10 * math.sin(angle) - 3 * math.cos(angle)
                delta_z = tip_z - (-2 + 9 * progress)
                channels["position"][2] = 9 * progress
                channels["rotation"][1] = (1 if part == "string_left" else -1) * math.degrees(math.atan2(delta_z, 8 - tip_x))
                channels["scale"][0] = math.hypot(8 - tip_x, delta_z) / 14
            for channel, values in channels.items():
                keyframes.append({"channel": channel, "time": fraction * 1.25, "interpolation": "linear",
                                  "data_points": [dict(zip(("x", "y", "z"), map(str, values)))],
                                  "uuid": identity(f"{item_id}/{part}/{fraction}/{channel}")})
        animators[identity(f"{item_id}/{part}")] = {"name": part, "type": "bone", "keyframes": keyframes}
    source = "data:image/png;base64," + base64.b64encode(texture.read_bytes()).decode("ascii")
    save(ART / "blockbench" / f"{item_id}.bbmodel", {
        "meta": {"format_version": "4.10", "model_format": "free", "box_uv": False},
        "name": item_id, "model_identifier": item_id, "resolution": {"width": 64, "height": 64},
        "elements": elements, "outliner": groups, "display": DISPLAY,
        "textures": [{"name": texture.name, "id": "0", "uuid": identity(item_id + "/texture"), "source": source,
                      "width": 64, "height": 64, "uv_width": 64, "uv_height": 64}],
        "animations": [{"name": "reload", "uuid": identity(item_id + "/reload"), "loop": "hold", "length": 1.25, "animators": animators}],
    })


def generate(models_only=False):
    ART.mkdir(parents=True, exist_ok=True)
    for variant in VARIANTS:
        material, chinese, english, *_ = variant
        item_id = material + "_crossbow"
        texture = atlas(variant)
        parts = geometry(material)
        textures = {"0": "ftboss:item/" + item_id, "particle": "ftboss:item/" + item_id}
        for part, cubes in parts.items():
            save(ASSETS / "models/item" / (item_id + "_parts") / (part + ".json"), {"textures": textures, "elements": cubes})
        save(ASSETS / "models/item" / (item_id + ".json"), {"textures": textures, "display": DISPLAY,
             "elements": [cube for part in PARTS[:6] for cube in parts[part]]})
        blockbench(item_id, parts, texture)
        if models_only:
            print(item_id, sum(map(len, parts.values())), "cubes")
            continue
        for language, title in (("zh_cn", chinese), ("en_us", english + " Crossbow")):
            lang_path = ASSETS / "lang" / (language + ".json")
            entries = json.loads(lang_path.read_text(encoding="utf-8-sig"))
            entries["item.ftboss." + item_id] = title
            save(lang_path, entries)
        ingredient = {"tag": "minecraft:planks"} if material == "wooden" else {"tag": "minecraft:stone_tool_materials"} if material == "stone" else {"item": "minecraft:" + {"iron": "iron_ingot", "golden": "gold_ingot", "diamond": "diamond", "netherite": "netherite_ingot"}[material]}
        recipe = {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": ["MIM", "STS", " R "],
                  "key": {"M": ingredient, "I": {"item": "minecraft:stick"}, "S": {"item": "minecraft:string"},
                          "T": {"item": "minecraft:tripwire_hook"}, "R": {"item": "minecraft:stick"}},
                  "result": {"item": "ftboss:" + item_id}}
        save(DATA / "recipes" / (item_id + ".json"), recipe)
        unlock = ingredient
        if "item" in unlock:
            unlock = {"items": [unlock["item"]]}
        save(DATA / "advancements/recipes/combat" / (item_id + ".json"), {
            "parent": "minecraft:recipes/root",
            "criteria": {
                "has_material": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [unlock]}},
                "has_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": "ftboss:" + item_id}},
            },
            "requirements": [["has_material", "has_recipe"]], "rewards": {"recipes": ["ftboss:" + item_id]},
        })
        print(item_id, sum(map(len, parts.values())), "cubes")
    if models_only:
        return
    for tag in ("forge/crossbows", "forge/tools/crossbows", "minecraft/crossbows"):
        namespace, tag_path = tag.split("/", 1)
        target = ROOT / "src/main/resources/data" / namespace / "tags/items" / (tag_path + ".json")
        contents = json.loads(target.read_text(encoding="utf-8-sig")) if target.exists() else {"replace": False, "values": []}
        for variant in VARIANTS:
            value = "ftboss:" + variant[0] + "_crossbow"
            if value not in contents["values"]:
                contents["values"].append(value)
        save(target, contents)


if __name__ == "__main__":
    import sys
    generate(models_only="--models-only" in sys.argv)
