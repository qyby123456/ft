from __future__ import annotations

import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from build_model import OUTPUT, PACK, PALETTE


NORMALS = {"north": [0, 0, -1], "south": [0, 0, 1], "east": [1, 0, 0],
           "west": [-1, 0, 0], "up": [0, 1, 0], "down": [0, -1, 0]}


def rotation_matrix(axis, degrees):
    radians = math.radians(degrees)
    cosine, sine = math.cos(radians), math.sin(radians)
    if axis == "x":
        return np.array([[1, 0, 0], [0, cosine, -sine], [0, sine, cosine]])
    if axis == "y":
        return np.array([[cosine, 0, sine], [0, 1, 0], [-sine, 0, cosine]])
    return np.array([[cosine, -sine, 0], [sine, cosine, 0], [0, 0, 1]])


def face_corners(element, face):
    low_x, low_y, low_z = element["from"]
    high_x, high_y, high_z = element["to"]
    return np.array({
        "north": [[high_x, high_y, low_z], [low_x, high_y, low_z], [low_x, low_y, low_z], [high_x, low_y, low_z]],
        "south": [[low_x, high_y, high_z], [high_x, high_y, high_z], [high_x, low_y, high_z], [low_x, low_y, high_z]],
        "east": [[high_x, high_y, high_z], [high_x, high_y, low_z], [high_x, low_y, low_z], [high_x, low_y, high_z]],
        "west": [[low_x, high_y, low_z], [low_x, high_y, high_z], [low_x, low_y, high_z], [low_x, low_y, low_z]],
        "up": [[low_x, high_y, low_z], [high_x, high_y, low_z], [high_x, high_y, high_z], [low_x, high_y, high_z]],
        "down": [[low_x, low_y, high_z], [high_x, low_y, high_z], [high_x, low_y, low_z], [low_x, low_y, low_z]],
    }[face], dtype=float)


def raster_triangle(pixels, depth_buffer, points, texture_uv, texture, brightness):
    image_height, image_width = pixels.shape[:2]
    minimum_x = max(0, math.floor(np.min(points[:, 0])))
    maximum_x = min(image_width - 1, math.ceil(np.max(points[:, 0])))
    minimum_y = max(0, math.floor(np.min(points[:, 1])))
    maximum_y = min(image_height - 1, math.ceil(np.max(points[:, 1])))
    if maximum_x < minimum_x or maximum_y < minimum_y:
        return
    first, second, third = points
    denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
    if abs(denominator) < 1e-8:
        return
    grid_y, grid_x = np.mgrid[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
    sample_x, sample_y = grid_x + .5, grid_y + .5
    weight_first = ((second[1] - third[1]) * (sample_x - third[0]) + (third[0] - second[0]) * (sample_y - third[1])) / denominator
    weight_second = ((third[1] - first[1]) * (sample_x - third[0]) + (first[0] - third[0]) * (sample_y - third[1])) / denominator
    weight_third = 1 - weight_first - weight_second
    depth = weight_first * first[2] + weight_second * second[2] + weight_third * third[2]
    depth_region = depth_buffer[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
    visible = (weight_first >= -1e-7) & (weight_second >= -1e-7) & (weight_third >= -1e-7) & (depth > depth_region)
    if not visible.any():
        return
    coord_u = weight_first * texture_uv[0, 0] + weight_second * texture_uv[1, 0] + weight_third * texture_uv[2, 0]
    coord_v = weight_first * texture_uv[0, 1] + weight_second * texture_uv[1, 1] + weight_third * texture_uv[2, 1]
    texel_u = np.clip(np.floor(coord_u * texture.shape[1] / 16).astype(int), 0, texture.shape[1] - 1)
    texel_v = np.clip(np.floor(coord_v * texture.shape[0] / 16).astype(int), 0, texture.shape[0] - 1)
    colors = texture[texel_v, texel_u].copy()
    visible &= colors[:, :, 3] > 0
    colors[:, :, :3] = np.clip(colors[:, :, :3].astype(float) * brightness, 0, 255).astype(np.uint8)
    pixels[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1][visible] = colors[visible]
    depth_region[visible] = depth[visible]


def render_model(model, texture, size, yaw=0, pitch=0, roll=0):
    image_width, image_height = size
    yaw_radians, pitch_radians = math.radians(yaw), math.radians(pitch)
    toward_camera = np.array([math.sin(yaw_radians) * math.cos(pitch_radians), math.sin(pitch_radians),
                              -math.cos(yaw_radians) * math.cos(pitch_radians)])
    right = np.array([math.cos(yaw_radians), 0, math.sin(yaw_radians)])
    up = np.cross(right, toward_camera)
    camera_matrix = np.array([right, up, toward_camera])
    model_rotation = rotation_matrix("z", roll)
    light = np.array([-.4, .72, -.57])
    light /= np.linalg.norm(light)
    faces = []
    for element in model["elements"]:
        local_rotation = np.eye(3)
        origin = np.zeros(3)
        if "rotation" in element:
            local_rotation = rotation_matrix(element["rotation"]["axis"], element["rotation"]["angle"])
            origin = np.array(element["rotation"]["origin"])
        for face, specification in element["faces"].items():
            normal = model_rotation @ local_rotation @ np.array(NORMALS[face])
            if np.dot(normal, toward_camera) <= 1e-8:
                continue
            corners = face_corners(element, face)
            corners = (corners - origin) @ local_rotation.T + origin
            corners = (corners - [8, 10.5, 8]) @ model_rotation.T
            projected = corners @ camera_matrix.T
            minimum_u, minimum_v, maximum_u, maximum_v = specification["uv"]
            texture_uv = np.array([[minimum_u, minimum_v], [maximum_u, minimum_v],
                                   [maximum_u, maximum_v], [minimum_u, maximum_v]])
            brightness = .72 + .30 * max(0, np.dot(normal, light))
            faces.append((projected, texture_uv, brightness))
    bounds = np.concatenate([face[0] for face in faces], axis=0)
    minimum, maximum = bounds.min(axis=0), bounds.max(axis=0)
    center = (minimum + maximum) / 2
    scale = min((image_width - 40) / (maximum[0] - minimum[0]), (image_height - 40) / (maximum[1] - minimum[1]))
    pixels = np.zeros((image_height, image_width, 4), dtype=np.uint8)
    depth_buffer = np.full((image_height, image_width), -np.inf)
    for projected, texture_uv, brightness in faces:
        screen = projected.copy()
        screen[:, 0] = image_width / 2 + (projected[:, 0] - center[0]) * scale
        screen[:, 1] = image_height / 2 - (projected[:, 1] - center[1]) * scale
        for indices in ([0, 1, 2], [0, 2, 3]):
            raster_triangle(pixels, depth_buffer, screen[indices], texture_uv[indices], texture, brightness)
    return Image.fromarray(pixels)


def font(size, bold=False):
    return ImageFont.truetype(str(Path("C:/Windows/Fonts") / ("msyhbd.ttc" if bold else "msyh.ttc")), size)


def main():
    model = json.loads((OUTPUT / "compost_crown.json").read_text(encoding="utf-8"))
    texture = np.array(Image.open(OUTPUT / "compost_crown.png").convert("RGBA"))
    board = Image.new("RGB", (1800, 1240), "#ece9e0")
    draw = ImageDraw.Draw(board)
    ink, muted = "#263238", "#72776c"
    draw.text((65, 38), "FTBOSS   /   WEAPON DESIGN 01", fill=muted, font=font(21))
    draw.text((60, 74), "污秽虹冠", fill=ink, font=font(72, True))
    draw.text((425, 120), "COMPOST CROWN", fill=muted, font=font(26))
    draw.text((66, 179), "炫彩答辩超人  /  专属重锤模型提案", fill=ink, font=font(25))
    draw.text((1415, 61), "MODEL STUDY / V1", fill=muted, font=font(21))
    draw.text((1415, 94), "2026.09.16", fill=muted, font=font(21))
    draw.line((65, 229, 1735, 229), fill="#c9ccc1", width=2)
    draw.ellipse((170, 340, 840, 1010), fill="#e2dfd5")
    draw.ellipse((360, 1085, 690, 1114), fill="#d4d2c7")
    hero = render_model(model, texture, (910, 890), yaw=32, pitch=14, roll=-18)
    board.paste(hero, (30, 238), hero)
    hero.save(OUTPUT / "hero.png")
    draw.text((65, 265), "01 / THREE-QUARTER", fill=muted, font=font(18))
    draw.line((960, 265, 960, 1085), fill="#c9ccc1", width=2)
    for view_name, yaw, pos_x in (("02 / 正面", 0, 1005), ("03 / 背面", 180, 1385)):
        view = render_model(model, texture, (340, 550), yaw=yaw)
        board.paste(view, (pos_x, 302), view)
        view.save(OUTPUT / ("front.png" if yaw == 0 else "back.png"))
        draw.text((pos_x + 15, 265), view_name, fill=muted, font=font(20))
    details = [("01", "螺旋堆肥锤头", "不对称卷尖，保留 Boss 的恶趣味轮廓。"),
               ("02", "黑铁骨架 × 六色结晶", "撞角、束爪和虹色嵌晶强化重武器观感。"),
               ("03", "荧绿毒印 × 缠革长柄", "压低大面积彩度，让毒核成为视觉焦点。")]
    for detail_index, (number, title, description) in enumerate(details):
        pos_y = 869 + detail_index * 76
        draw.text((1020, pos_y), number, fill="#7e944f", font=font(23, True))
        draw.text((1070, pos_y - 2), title, fill=ink, font=font(24, True))
        draw.text((1070, pos_y + 31), description, fill=muted, font=font(19))
    draw.line((65, 1138, 1735, 1138), fill="#c9ccc1", width=2)
    for palette_index, material in enumerate(("compost", "iron", "brass", "toxic", "pink", "amber", "cyan", "blue", "violet")):
        pos_x = 66 + palette_index * 44
        draw.rectangle((pos_x, 1164, pos_x + 31, 1195), fill=PALETTE[material])
    draw.text((500, 1163), f'{len(model["elements"])} 部件  /  512² 像素图集  /  Java 1.20.1  /  可编辑 Blockbench 工程',
              fill=muted, font=font(20))
    draw.text((65, 1205), "真实几何与 UV 离线渲染 · 非游戏截图 · 正式物品 ftboss:compost_crown · 无动态发光", fill=muted, font=font(14))
    board.save(OUTPUT / "compost_crown_design.png")
    head_model = dict(model)
    head_model["elements"] = [element for element in model["elements"] if element["from"][1] > 12]
    icon = Image.new("RGBA", (128, 128), "#252c34")
    icon_model = render_model(head_model, texture, (128, 128), yaw=30, pitch=12)
    icon.alpha_composite(icon_model)
    icon.convert("RGB").save(PACK / "pack.png")
    print("Design preview:", OUTPUT / "compost_crown_design.png")


if __name__ == "__main__":
    main()
