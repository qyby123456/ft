from __future__ import annotations

import math

from PIL import Image, ImageDraw, ImageFont

from common import Structure, state
from inspect_references import FONT, color_for, noise_color, render_isometric, shade


def color(block):
    name = block.name
    if name == "minecraft:lava":
        return (247, 106, 30)
    if name == "minecraft:cobweb":
        return (220, 225, 220)
    if any(token in name for token in ("calcite", "bone_block", "turtle_egg", "skull")):
        return (204, 201, 175)
    if "mossy" in name or "moss_block" in name:
        return (105, 126, 96)
    if "amethyst" in name or "purple_" in name:
        return (151, 103, 185)
    if "green_" in name:
        return (97, 145, 80)
    if any(token in name for token in ("iron_block", "iron_trapdoor", "iron_bars")):
        return (145, 156, 162)
    if "basalt" in name:
        return (105, 113, 116)
    if "blackstone" in name or "deepslate" in name:
        return (67, 69, 77)
    return color_for(block)


def partial_shapes(block):
    name = block.name
    if "_slab" in name:
        return [((0, 0.5 if block.get("type") == "top" else 0, 0), (1, 1 if block.get("type") == "top" else 0.5, 1))]
    if "_stairs" in name:
        facing = block.get("facing")
        upper = {"north": ((0, 0.5, 0), (1, 1, 0.5)), "south": ((0, 0.5, 0.5), (1, 1, 1)), "west": ((0, 0.5, 0), (0.5, 1, 1)), "east": ((0.5, 0.5, 0), (1, 1, 1))}[facing]
        return [((0, 0, 0), (1, 0.5, 1)), upper]
    if "anvil" in name:
        return [((0.1, 0, 0.1), (0.9, 0.25, 0.9)), ((0.35, 0.25, 0.25), (0.65, 0.7, 0.75)), ((0, 0.7, 0.1), (1, 1, 0.9))]
    if "cauldron" in name:
        return [((0, 0, 0), (1, 0.25, 1)), ((0, 0.25, 0), (0.15, 1, 1)), ((0.85, 0.25, 0), (1, 1, 1)), ((0.15, 0.25, 0), (0.85, 1, 0.15)), ((0.15, 0.25, 0.85), (0.85, 1, 1))]
    if "lantern" in name:
        return [((0.25, 0.1, 0.25), (0.75, 0.65, 0.75)), ((0.35, 0.65, 0.35), (0.65, 0.9, 0.65))]
    if "chain" in name:
        return [((0.44, 0, 0.44), (0.56, 1, 0.56))]
    if "iron_bars" in name:
        shapes = [((0.45, 0, 0.45), (0.55, 1, 0.55))]
        if block.get("east") == "true" or block.get("west") == "true":
            shapes.append(((0, 0, 0.46), (1, 1, 0.54)))
        if block.get("north") == "true" or block.get("south") == "true":
            shapes.append(((0.46, 0, 0), (0.54, 1, 1)))
        return shapes
    if "rail" in name:
        return [((0, 0.02, 0.15), (1, 0.08, 0.25)), ((0, 0.02, 0.75), (1, 0.08, 0.85))]
    if "_button" in name:
        return [((0.3, 0.35, 0), (0.7, 0.65, 0.2))]
    if "trapdoor" in name:
        return [((0, 0, 0), (0.15, 1, 1))] if block.get("open") == "true" else [((0, 0, 0), (1, 0.15, 1))]
    if "candle" in name:
        return [((0.35, 0, 0.35), (0.48, 0.55, 0.48)), ((0.6, 0, 0.5), (0.73, 0.4, 0.63))]
    if "skull" in name:
        return [((0.25, 0, 0.25), (0.75, 0.5, 0.75))]
    if "turtle_egg" in name:
        return [((0.1, 0, 0.2), (0.5, 0.6, 0.6)), ((0.5, 0, 0.4), (0.85, 0.5, 0.75))]
    if "pointed_dripstone" in name or "amethyst_cluster" in name:
        return [((0.3, 0, 0.3), (0.7, 0.65, 0.7)), ((0.4, 0.65, 0.4), (0.6, 0.95, 0.6))]
    if "brewing_stand" in name:
        return [((0.1, 0, 0.1), (0.9, 0.15, 0.9)), ((0.45, 0.15, 0.45), (0.55, 0.95, 0.55))]
    if "lectern" in name:
        return [((0.1, 0, 0.1), (0.9, 0.15, 0.9)), ((0.35, 0.15, 0.35), (0.65, 0.7, 0.65)), ((0, 0.7, 0), (1, 0.9, 1))]
    if "potted_" in name:
        return [((0.3, 0, 0.3), (0.7, 0.35, 0.7)), ((0.2, 0.35, 0.2), (0.8, 0.75, 0.8))]
    if "grindstone" in name:
        return [((0.2, 0, 0.2), (0.8, 0.2, 0.8)), ((0.15, 0.2, 0.25), (0.85, 0.9, 0.75))]
    if "campfire" in name:
        return [((0.05, 0, 0.1), (0.95, 0.3, 0.9))]
    return [((0, 0, 0), (1, 1, 1))]


def rotated(structure):
    result = Structure(structure.size)
    opposite = {"north": "south", "south": "north", "east": "west", "west": "east"}
    for (pos_x, pos_y, pos_z), block in structure.blocks.items():
        properties = dict(block.properties)
        if "facing" in properties:
            properties["facing"] = opposite.get(properties["facing"], properties["facing"])
        for first, second in (("north", "south"), ("east", "west")):
            if first in properties and second in properties:
                properties[first], properties[second] = properties[second], properties[first]
        result.blocks[(structure.size[0] - 1 - pos_x, pos_y, structure.size[2] - 1 - pos_z)] = state(block.name, **properties)
    return result


def cutaway(structure, size):
    structure = rotated(structure)
    width, height, depth = structure.size
    visible = {position: block for position, block in structure.blocks.items() if block.name != "minecraft:air" and position[1] < height - 1 and not (position[1] > 0 and (position[0] == width - 1 or position[2] == depth - 1))}
    image = Image.new("RGB", size, "#101a24")
    draw = ImageDraw.Draw(image)
    scale = min((size[0] - 50) / (width + depth), (size[1] - 50) / ((width + depth) / 2 + height))
    offset_x = size[0] / 2
    offset_y = 25 + height * scale

    def project(position):
        pos_x, pos_y, pos_z = position
        return offset_x + (pos_x - pos_z) * scale, offset_y + (pos_x + pos_z) * scale / 2 - pos_y * scale

    def cube(position, lower, upper, material, outline=False):
        left_x, lower_y, near_z = [coordinate + offset for coordinate, offset in zip(position, lower)]
        right_x, upper_y, far_z = [coordinate + offset for coordinate, offset in zip(position, upper)]
        top = [project(point) for point in ((left_x, upper_y, near_z), (right_x, upper_y, near_z), (right_x, upper_y, far_z), (left_x, upper_y, far_z))]
        right = [top[1], project((right_x, lower_y, near_z)), project((right_x, lower_y, far_z)), top[2]]
        left = [top[2], project((right_x, lower_y, far_z)), project((left_x, lower_y, far_z)), top[3]]
        if outline:
            for points in (top, right, left):
                draw.line([*points, points[0]], fill=(142, 161, 170), width=1)
        else:
            draw.polygon(right, fill=shade(material, 0.65))
            draw.polygon(left, fill=shade(material, 0.82))
            draw.polygon(top, fill=shade(material, 1.12))

    for position in sorted(visible, key=lambda entry: (entry[0] + entry[2], entry[1], entry[0])):
        block = visible[position]
        material = shade(color(block), 0.96 + noise_color(position) * 0.018)
        if "glass" in block.name:
            cube(position, (0, 0, 0), (1, 1, 1), material, outline=True)
            continue
        if block.name == "minecraft:cobweb":
            for lower, upper in (((0, 0, 0.5), (1, 1, 0.5)), ((0, 1, 0.5), (1, 0, 0.5)), ((0.5, 0, 0), (0.5, 1, 1)), ((0.5, 1, 0), (0.5, 0, 1)), ((0, 0.5, 0.5), (1, 0.5, 0.5)), ((0.5, 0, 0.5), (0.5, 1, 0.5))):
                draw.line((project(tuple(coordinate + offset for coordinate, offset in zip(position, lower))), project(tuple(coordinate + offset for coordinate, offset in zip(position, upper)))), fill=material, width=1)
            continue
        for lower, upper in partial_shapes(block):
            cube(position, lower, upper, material)
        if "campfire" in block.name or "candle" in block.name and block.get("lit") == "true":
            flame = (91, 216, 218) if "soul" in block.name else (255, 178, 63)
            cube(position, (0.35, 0.3, 0.35), (0.65, 0.95, 0.65), flame)
        if "brewing_stand" in block.name:
            for index, tint in enumerate(((100, 191, 84), (162, 91, 205), (94, 187, 204))):
                if block.get(f"has_bottle_{index}") == "true":
                    offset = index * 0.28
                    cube(position, (0.1 + offset, 0.15, 0.15), (0.26 + offset, 0.65, 0.4), tint)
        if "cauldron" in block.name:
            cube(position, (0.16, 0.55, 0.16), (0.84, 0.6, 0.84), (64, 142, 191))
        if "lectern" in block.name:
            cube(position, (0.08, 0.9, 0.1), (0.92, 0.98, 0.9), (222, 208, 166))
    return image


def floorplan(structure, visited, size):
    image = Image.new("RGB", size, "#101a24")
    draw = ImageDraw.Draw(image)
    width, _, depth = structure.size
    cell = min((size[0] - 24) // width, (size[1] - 24) // depth)
    offset_x, offset_z = (size[0] - width * cell) // 2, (size[1] - depth * cell) // 2
    for pos_z in range(depth):
        for pos_x in range(width):
            block = structure.get((pos_x, 1, pos_z))
            if block.name == "minecraft:air":
                material = (68, 113, 122) if (pos_x, 1, pos_z) in visited else (57, 65, 73)
            else:
                material = color(block)
            left, top = offset_x + pos_x * cell, offset_z + pos_z * cell
            draw.rectangle((left, top, left + cell - 1, top + cell - 1), fill=material)
    entrance_x = offset_x + width // 2 * cell
    draw.rectangle((entrance_x, offset_z, entrance_x + cell - 1, offset_z + cell - 1), fill="#f6d88d")
    return image


def make_preview(room, structure, visited, destination):
    image = Image.new("RGB", (1536, 1024), "#0b121c")
    draw = ImageDraw.Draw(image)
    title = ImageFont.truetype(FONT, 38)
    normal = ImageFont.truetype(FONT, 21)
    small = ImageFont.truetype(FONT, 18)
    draw.text((28, 18), room.room_id + " · " + room.spec["name"] + "  |  实际 NBT 方块预览", font=title, fill="#efd3a0")
    size_text = "×".join(map(str, structure.size))
    draw.text((30, 75), size_text + "（X×Y×Z）  北入口 3×3，无门扇/凸石  |  完整封顶、零奖励箱  |  非游戏实拍", font=normal, fill="#c3d1da")
    image.paste(cutaway(structure, (1110, 770)), (14, 122))
    draw.text((1150, 127), "完整屋顶与墙体", font=normal, fill="#dce8ed")
    image.paste(render_isometric(rotated(structure), (365, 315), floor=0, cutaway=False), (1140, 165))
    draw.text((1150, 507), "可达地面（青色）", font=normal, fill="#dce8ed")
    image.paste(floorplan(structure, visited, (365, 340)), (1140, 546))
    draw.text((32, 904), "左图仅为看清内部移除屋顶与近侧墙；room.nbt 保留完整结构。", font=normal, fill="#d1e1e9")
    for index, feature in enumerate(room.spec["features"]):
        left, top = 32 + index % 2 * 748, 949 + index // 2 * 31
        draw.text((left, top), "· " + feature, font=small, fill="#a1bac9")
    image.save(destination)
