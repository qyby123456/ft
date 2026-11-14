from __future__ import annotations

import hashlib
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont


PROJECT = Path(__file__).resolve().parents[4]
ASSETS = PROJECT / "src/main/resources/assets/ftboss"
OUTPUT = PROJECT / "art/boss_materials/refine/spore"
WORK = PROJECT / "_work/boss-materials-refine-20261021/spore"
NORMALS = {"north": [0, 0, -1], "south": [0, 0, 1], "east": [1, 0, 0],
           "west": [-1, 0, 0], "up": [0, 1, 0], "down": [0, -1, 0]}


def load_final():
    wrapper = json.loads((ASSETS / "models/item/spore_material.json").read_text(encoding="utf-8"))
    parent = wrapper["base"]["parent"].split(":", 1)[1]
    model = json.loads((ASSETS / "models" / (parent + ".json")).read_text(encoding="utf-8"))
    texture = model["textures"]["atlas"].split(":", 1)[1]
    return model, np.array(Image.open(ASSETS / "textures" / (texture + ".png")).convert("RGBA"))


def rotation_matrix(axis, degrees):
    radians = math.radians(degrees)
    cosine, sine = math.cos(radians), math.sin(radians)
    return np.array({
        "x": [[1, 0, 0], [0, cosine, -sine], [0, sine, cosine]],
        "y": [[cosine, 0, sine], [0, 1, 0], [-sine, 0, cosine]],
        "z": [[cosine, -sine, 0], [sine, cosine, 0], [0, 0, 1]],
    }[axis], dtype=float)


def element_matrix(element):
    rotation = element.get("rotation", {})
    matrix = rotation_matrix(rotation.get("axis", "y"), rotation.get("angle", 0))
    if rotation.get("rescale", False):
        scale = np.full(3, 1 / math.cos(math.radians(rotation["angle"])))
        scale["xyz".index(rotation["axis"])] = 1
        matrix = np.diag(scale) @ matrix
    return matrix, np.array(rotation.get("origin", [0, 0, 0]), dtype=float)


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
    if abs(denominator) < 1e-9:
        return
    grid_y, grid_x = np.mgrid[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
    sample_x, sample_y = grid_x + .5, grid_y + .5
    first_weight = ((second[1] - third[1]) * (sample_x - third[0]) +
                    (third[0] - second[0]) * (sample_y - third[1])) / denominator
    second_weight = ((third[1] - first[1]) * (sample_x - third[0]) +
                     (first[0] - third[0]) * (sample_y - third[1])) / denominator
    third_weight = 1 - first_weight - second_weight
    depth = first_weight * first[2] + second_weight * second[2] + third_weight * third[2]
    depth_region = depth_buffer[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1]
    visible = (first_weight >= -1e-8) & (second_weight >= -1e-8) & (third_weight >= -1e-8) & (depth > depth_region)
    if not visible.any():
        return
    coord_u = first_weight * texture_uv[0, 0] + second_weight * texture_uv[1, 0] + third_weight * texture_uv[2, 0]
    coord_v = first_weight * texture_uv[0, 1] + second_weight * texture_uv[1, 1] + third_weight * texture_uv[2, 1]
    texel_u = np.clip(np.floor(coord_u * texture.shape[1] / 16).astype(int), 0, texture.shape[1] - 1)
    texel_v = np.clip(np.floor(coord_v * texture.shape[0] / 16).astype(int), 0, texture.shape[0] - 1)
    colors = texture[texel_v, texel_u].copy()
    visible &= colors[:, :, 3] > 0
    colors[:, :, :3] = np.clip(colors[:, :, :3].astype(float) * brightness, 0, 255).astype(np.uint8)
    pixels[minimum_y:maximum_y + 1, minimum_x:maximum_x + 1][visible] = colors[visible]
    depth_region[visible] = depth[visible]


def render(model, texture, size, yaw=0, pitch=0, margin=25, outline=False, common_scale=None):
    image_width, image_height = size
    yaw_radians, pitch_radians = math.radians(yaw), math.radians(pitch)
    toward_camera = np.array([math.sin(yaw_radians) * math.cos(pitch_radians), math.sin(pitch_radians),
                              -math.cos(yaw_radians) * math.cos(pitch_radians)])
    right = np.array([math.cos(yaw_radians), 0, math.sin(yaw_radians)])
    up = np.cross(right, toward_camera)
    camera_matrix = np.array([right, up, toward_camera])
    light = toward_camera * .35 + up * .75 - right * .50
    light /= np.linalg.norm(light)
    faces, all_points = [], []
    for element in model["elements"]:
        matrix, origin = element_matrix(element)
        for face, specification in element["faces"].items():
            corners = (face_corners(element, face) - origin) @ matrix.T + origin
            projected = corners @ camera_matrix.T
            all_points.extend(projected)
            normal = np.linalg.inv(matrix).T @ np.array(NORMALS[face])
            normal /= np.linalg.norm(normal)
            if np.dot(normal, toward_camera) <= 1e-8:
                continue
            minimum_u, minimum_v, maximum_u, maximum_v = specification["uv"]
            texture_uv = np.array([[minimum_u, minimum_v], [maximum_u, minimum_v],
                                   [maximum_u, maximum_v], [minimum_u, maximum_v]])
            texture_uv = np.roll(texture_uv, -specification.get("rotation", 0) // 90, axis=0)
            brightness = .74 + .28 * max(0, np.dot(normal, light))
            faces.append((projected, texture_uv, brightness))
    bounds = np.array(all_points)
    minimum, maximum = bounds.min(axis=0), bounds.max(axis=0)
    center = (minimum + maximum) / 2
    scale = common_scale or min((image_width - margin * 2) / (maximum[0] - minimum[0]),
                                (image_height - margin * 2) / (maximum[1] - minimum[1]))
    pixels = np.zeros((image_height, image_width, 4), dtype=np.uint8)
    depth_buffer = np.full((image_height, image_width), -np.inf)
    for projected, texture_uv, brightness in faces:
        screen = projected.copy()
        screen[:, 0] = image_width / 2 + (projected[:, 0] - center[0]) * scale
        screen[:, 1] = image_height / 2 - (projected[:, 1] - center[1]) * scale
        for indices in ([0, 1, 2], [0, 2, 3]):
            raster_triangle(pixels, depth_buffer, screen[indices], texture_uv[indices], texture, brightness)
    result = Image.fromarray(pixels)
    if outline:
        expanded = result.getchannel("A").filter(ImageFilter.MaxFilter(3))
        border = Image.new("RGBA", size, (28, 22, 35, 0))
        border.putalpha(expanded)
        border.alpha_composite(result)
        result = border
    return result


def font(size, bold=False):
    return ImageFont.truetype("C:/Windows/Fonts/" + ("msyhbd.ttc" if bold else "msyh.ttc"), size)


def checker(size, colors=((36, 40, 47), (42, 46, 53)), cell=16):
    image = Image.new("RGB", size, colors[0])
    draw = ImageDraw.Draw(image)
    for pixel_y in range(0, size[1], cell):
        for pixel_x in range(0, size[0], cell):
            if (pixel_x // cell + pixel_y // cell) % 2:
                draw.rectangle((pixel_x, pixel_y, pixel_x + cell - 1, pixel_y + cell - 1), fill=colors[1])
    return image


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    model, texture = load_final()
    sheet = Image.new("RGB", (1440, 850), "#1d232b")
    draw = ImageDraw.Draw(sheet)
    draw.text((34, 21), "虹彩孢子  /  最终运行时资源 · 四视图", font=font(29, True), fill="#e9e0cf")
    draw.text((36, 68), "LAYERED MYCELIAL MATERIAL   ·   FT BOSS / COMPOST   ·   2026.10.21", font=font(16), fill="#9bafa8")
    for index, (name, label, yaw) in enumerate([
        ("front", "正面 / NORTH", 0), ("back", "背面 / SOUTH", 180),
        ("left", "左侧 / WEST", -90), ("right", "右侧 / EAST", 90),
    ]):
        origin_x, origin_y = (index % 2) * 720, 112 + (index // 2) * 365
        preview = render(model, texture, (650, 305), yaw=yaw, margin=12, common_scale=23)
        preview.save(OUTPUT / (name + ".png"))
        sheet.paste(preview, (origin_x + 34, origin_y + 22), preview)
        draw.text((origin_x + 37, origin_y + 329), label, font=font(19), fill="#c4c8bf")
    sheet.save(OUTPUT / "four_views.png")
    hero = render(model, texture, (820, 820), yaw=-28, pitch=16, margin=58)
    hero.save(OUTPUT / "hero.png")
    underside = render(model, texture, (650, 500), yaw=30, pitch=-23, margin=38)
    underside.save(OUTPUT / "gill_detail.png")
    icon = Image.open(ASSETS / "textures/item/spore_material.png").convert("RGBA")
    old_icon = Image.open(WORK / "backup/src/main/resources/assets/ftboss/textures/item/spore_material.png").convert("RGBA")
    icons = Image.new("RGB", (1060, 440), "#1d232b")
    icon_draw = ImageDraw.Draw(icons)
    icon_draw.text((25, 20), "同件材料 · 图标与三维外形对照", font=font(26, True), fill="#e9e0cf")
    for image, position, title in [
        (icon, (26, 99), "32 px / 原尺寸"),
        (icon.resize((256, 256), Image.Resampling.NEAREST), (138, 99), "8× 最近邻放大"),
        (old_icon.resize((192, 192), Image.Resampling.NEAREST), (440, 99), "旧图标"),
        (render(model, texture, (340, 285), yaw=-25, pitch=12, margin=12), (675, 87), "最终模型 / 同角度"),
    ]:
        background = checker(image.size)
        background.paste(image, (0, 0), image)
        icons.paste(background, position)
        icon_draw.text((position[0], 384), title, font=font(14), fill="#bcc8c5")
    icons.save(OUTPUT / "icon_comparison.png")
    board = Image.new("RGB", (1600, 1180), "#ece8de")
    board_draw = ImageDraw.Draw(board)
    board_draw.text((45, 30), "FTBOSS   /   MATERIAL STUDY 07", font=font(19), fill="#6f7d75")
    board_draw.text((42, 67), "虹彩孢子", font=font(58, True), fill="#303c3b")
    board_draw.text((385, 108), "MYCELIAL SPORE CLUSTER", font=font(22), fill="#6f7d75")
    board_draw.text((46, 158), "炫彩答辩超人  ·  配件原材料  ·  ftboss:spore_material", font=font(21), fill="#5a6962")
    board.paste(hero, (0, 200), hero)
    board_draw.line((820, 223, 820, 1050), fill="#c9cec4", width=2)
    board_draw.text((869, 224), "菌盖 / 菌褶 / 孢晶", font=font(31, True), fill="#3a504d")
    board.paste(underside, (855, 275), underside)
    for index, text in enumerate([
        "01  堆肥棕菌盖与灰绿菌缘，保留 Boss 材质身份",
        "02  两层错落菌体、放射菌褶与纵向菌丝连接",
        "03  青绿主孢晶，紫 / 琥珀 / 粉色少量伴生晶",
        "04  每面独立 UV · 2 px/单位 · 独立背包图标",
    ]):
        board_draw.text((869, 798 + index * 45), text, font=font(20), fill="#53655c")
    zoomed_icon = icon.resize((128, 128), Image.Resampling.NEAREST)
    board.paste(zoomed_icon, (1375, 965), zoomed_icon)
    board_draw.text((45, 1100), f'{len(model["elements"])} 个有结构部件  /  256² 图集  /  32² 图标  /  可编辑 Blockbench',
                    font=font(19), fill="#64746a")
    board_draw.text((45, 1137), "最终资源离线渲染 · 已验证元素旋转与逐面 UV · 不代表游戏内实测", font=font(16), fill="#778078")
    board.save(OUTPUT / "presentation.png")
    paths = [ASSETS / "models/item/boss_materials/spore_material.json",
             ASSETS / "textures/item/boss_materials/spore.png", ASSETS / "textures/item/spore_material.png"]
    manifest = {"source": "final runtime resources via forge separate_transforms base",
                "renderer": "orthographic z-buffer, Java FaceInfo UV, single-axis rotation, inverse-transpose normals, rescale, nearest texels",
                "directions": ["north", "south", "west", "east"],
                "files": {str(target): hashlib.sha256(target.read_bytes()).hexdigest() for target in paths}}
    (WORK / "render-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print("Saved final runtime views, hero, gill detail, icon comparison and presentation:", OUTPUT)


if __name__ == "__main__":
    main()
