import argparse
import hashlib
import json
import math
from pathlib import Path
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from gen_material_crossbows import ASSETS, ART, PARTS, VARIANTS

FONT = "C:/Windows/Fonts/msyh.ttc"
STATES = (("empty", "未装填", 0, None), ("pulling", "半拉弦", 0.5, None),
          ("charged", "箭矢装填", 1, "arrow"), ("firework", "烟花装填", 1, "firework"))
VIEWS = (("正面", 0), ("背面", 180), ("左侧", -90), ("右侧", 90))


def font(size):
    return ImageFont.truetype(FONT, size)


def parts_for(item_id):
    return {part: json.loads((ASSETS / "models/item" / (item_id + "_parts") / (part + ".json")).read_text(encoding="utf-8"))["elements"] for part in PARTS}


def transform(points, part, progress):
    result = np.array(points, dtype=float)
    if part in ("left", "right"):
        side = 1 if part == "left" else -1
        angle = math.radians(side * 22.5 * progress)
        pivot = np.array([4 if side == 1 else 12, 0, 1])
        matrix = np.array([[math.cos(angle), 0, math.sin(angle)], [0, 1, 0], [-math.sin(angle), 0, math.cos(angle)]])
        result = (result - pivot) @ matrix.T + pivot
    elif part == "latch":
        result[:, 2] += 9 * progress
    elif part.startswith("string_"):
        left = part == "string_left"
        tip = transform([[-6 if left else 22, 0, -2]], "left" if left else "right", progress)[0]
        along = (result[:, 0] + 6) / 14 if left else (22 - result[:, 0]) / 14
        result[:, 0] = tip[0] + (8 - tip[0]) * along
        result[:, 2] += 2 + tip[2] + (-2 + 9 * progress - tip[2]) * along
    return result


def faces(cube):
    start_x, start_y, start_z = cube["from"]
    end_x, end_y, end_z = cube["to"]
    return {
        "north": [[start_x, end_y, start_z], [end_x, end_y, start_z], [end_x, start_y, start_z], [start_x, start_y, start_z]],
        "south": [[end_x, end_y, end_z], [start_x, end_y, end_z], [start_x, start_y, end_z], [end_x, start_y, end_z]],
        "west": [[start_x, end_y, end_z], [start_x, end_y, start_z], [start_x, start_y, start_z], [start_x, start_y, end_z]],
        "east": [[end_x, end_y, start_z], [end_x, end_y, end_z], [end_x, start_y, end_z], [end_x, start_y, start_z]],
        "up": [[start_x, end_y, end_z], [end_x, end_y, end_z], [end_x, end_y, start_z], [start_x, end_y, start_z]],
        "down": [[start_x, start_y, start_z], [end_x, start_y, start_z], [end_x, start_y, end_z], [start_x, start_y, end_z]],
    }


def triangle(image, depth, projected, uv, texture, shade):
    height, width = depth.shape
    minimum = np.maximum(np.floor(projected[:, :2].min(axis=0)).astype(int), [0, 0])
    maximum = np.minimum(np.ceil(projected[:, :2].max(axis=0)).astype(int), [width - 1, height - 1])
    if np.any(minimum > maximum):
        return
    first, second, third = projected
    denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
    if abs(denominator) < 1e-8:
        return
    pixel_x, pixel_y = np.meshgrid(np.arange(minimum[0], maximum[0] + 1) + 0.5, np.arange(minimum[1], maximum[1] + 1) + 0.5)
    weight_a = ((second[1] - third[1]) * (pixel_x - third[0]) + (third[0] - second[0]) * (pixel_y - third[1])) / denominator
    weight_b = ((third[1] - first[1]) * (pixel_x - third[0]) + (first[0] - third[0]) * (pixel_y - third[1])) / denominator
    weight_c = 1 - weight_a - weight_b
    distance = weight_a * first[2] + weight_b * second[2] + weight_c * third[2]
    region = np.s_[minimum[1]:maximum[1] + 1, minimum[0]:maximum[0] + 1]
    mask = (weight_a >= -1e-6) & (weight_b >= -1e-6) & (weight_c >= -1e-6) & (distance > depth[region])
    texture_u = np.clip((weight_a * uv[0, 0] + weight_b * uv[1, 0] + weight_c * uv[2, 0]).astype(int), 0, 63)
    texture_v = np.clip((weight_a * uv[0, 1] + weight_b * uv[1, 1] + weight_c * uv[2, 1]).astype(int), 0, 63)
    colors = texture[texture_v, texture_u, :3].astype(float) * shade
    image[region][mask] = np.clip(colors[mask], 0, 255).astype(np.uint8)
    depth[region][mask] = distance[mask]


def render(item_id, yaw, fraction, ammunition, size=(360, 245)):
    width, height = size
    image = np.full((height, width, 3), [29, 35, 42], dtype=np.uint8)
    depth = np.full((height, width), -np.inf)
    texture = np.array(Image.open(ASSETS / "textures/item" / (item_id + ".png")).convert("RGBA"))
    radians = math.radians(yaw)
    elevation = math.radians(24 if yaw in (0, 180, -90, 90) else 32)
    camera = np.array([math.sin(radians) * math.cos(elevation), math.sin(elevation), -math.cos(radians) * math.cos(elevation)])
    right = np.array([math.cos(radians), 0, math.sin(radians)])
    up = np.cross(right, camera)
    basis = np.array([right, -up, camera])
    scale = min(width / 37, height / 28)
    progress = fraction * fraction * (3 - 2 * fraction)
    light = np.array([-0.3, 0.85, -0.45])
    light /= np.linalg.norm(light)
    for part, cubes in parts_for(item_id).items():
        if part in ("arrow", "firework") and part != ammunition:
            continue
        for cube in cubes:
            for direction, points in faces(cube).items():
                world = transform(points, part, progress)
                normal = np.cross(world[1] - world[0], world[2] - world[0])
                normal /= max(1e-8, np.linalg.norm(normal))
                if np.dot(normal, camera) < 0:
                    normal = -normal
                shade = 0.62 + 0.38 * max(0, np.dot(normal, light))
                projected = (world - [8, 7, 6]) @ basis.T
                projected[:, :2] = projected[:, :2] * scale + [width / 2, height / 2]
                texture_left, texture_top, texture_right, texture_bottom = cube["faces"][direction]["uv"]
                uv = np.array([[texture_left, texture_top], [texture_right, texture_top], [texture_right, texture_bottom], [texture_left, texture_bottom]]) * 4
                for indices in ((0, 1, 2), (0, 2, 3)):
                    triangle(image, depth, projected[list(indices)], uv[list(indices)], texture, shade)
    assert not np.any(np.isfinite(depth[:3])) and not np.any(np.isfinite(depth[-3:])), item_id + " vertical clipping"
    assert not np.any(np.isfinite(depth[:, :3])) and not np.any(np.isfinite(depth[:, -3:])), item_id + " horizontal clipping"
    return Image.fromarray(image)


def verify_geometry():
    for material, *_ in VARIANTS:
        item_id = material + "_crossbow"
        parts = parts_for(item_id)
        texture = Image.open(ASSETS / "textures/item" / (item_id + ".png"))
        assert texture.size == (64, 64)
        for cubes in parts.values():
            for cube in cubes:
                assert all(-16 <= start < end <= 32 for start, end in zip(cube["from"], cube["to"]))
                assert set(cube["faces"]) == {"north", "south", "east", "west", "up", "down"}
                assert all(0 <= value <= 16 for face in cube["faces"].values() for value in face["uv"])
        for step in range(101):
            progress = step / 100
            for side, tip_x in (("left", -6), ("right", 22)):
                limb_tip = transform([[tip_x, 9.55, -2]], side, progress)[0]
                string_tip = transform([[tip_x, 9.55, -2]], "string_" + side, progress)[0]
                center = transform([[8, 9.55, -2]], "string_" + side, progress)[0]
                np.testing.assert_allclose(limb_tip, string_tip, atol=1e-7)
                np.testing.assert_allclose(center, [8, 9.55, -2 + 9 * progress], atol=1e-7)
        editor = json.loads((ART / "blockbench" / (item_id + ".bbmodel")).read_text(encoding="utf-8"))
        assert len(editor["elements"]) == sum(map(len, parts.values()))
        assert editor["animations"][0]["length"] == 1.25
        print(item_id, "PASS: bounds, faces, UV, 101 string/limb poses, editable source")


def main():
    global ASSETS
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", type=Path)
    args = parser.parse_args()
    if args.jar:
        extracted = ART.parents[1] / "_work/material-crossbows-reload-20261019/runtime-render-assets/assets/ftboss"
        verified = []
        with zipfile.ZipFile(args.jar) as archive:
            for material, *_ in VARIANTS:
                item_id = material + "_crossbow"
                files = ["models/item/" + item_id + ".json", "textures/item/" + item_id + ".png"]
                files.extend("models/item/" + item_id + "_parts/" + part + ".json" for part in PARTS)
                for relative in files:
                    data = archive.read("assets/ftboss/" + relative)
                    assert data == (ASSETS / relative).read_bytes(), "Packaged resource differs from reviewed source: " + relative
                    target = extracted / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(data)
                    verified.append({"resource": relative, "sha256": hashlib.sha256(data).hexdigest()})
        (ART / "runtime_resource_proof.json").write_text(json.dumps({"jar": str(args.jar),
            "jarSha256": hashlib.sha256(args.jar.read_bytes()).hexdigest(), "resources": verified}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        ASSETS = extracted
    ART.mkdir(parents=True, exist_ok=True)
    verify_geometry()
    for state_id, state_title, fraction, ammunition in STATES:
        sheet = Image.new("RGB", (1440, 6 * 292 + 85), "#111820")
        draw = ImageDraw.Draw(sheet)
        draw.text((24, 14), "六阶材质弩 / " + state_title + " / 最终运行时资源四视图", font=font(24), fill="#edf0e7")
        for row, (material, title, *_) in enumerate(VARIANTS):
            for column, (view_title, yaw) in enumerate(VIEWS):
                sheet.paste(render(material + "_crossbow", yaw, fraction, ammunition), (column * 360, row * 292 + 85))
                draw.text((column * 360 + 14, row * 292 + 338), title + " · " + view_title, font=font(17), fill="#c9d2d7")
        sheet.save(ART / ("four_views_" + state_id + ".png"))
    overview = Image.new("RGB", (1620, 1010), "#111820")
    draw = ImageDraw.Draw(overview)
    draw.text((32, 18), "六阶材质弩", font=font(38), fill="#f1e6ca")
    draw.text((32, 72), "木 · 石 · 铁 · 金 · 钻石 · 下界合金  |  原版风格 · 独立几何 · 弓类组件", font=font(21), fill="#aebec7")
    descriptions = ("木纹与绑绳", "粗石加固弩臂", "精钢双导轨", "金冠与绿铆钉", "阶梯晶刃", "重甲与鎏金嵌线")
    for index, (material, title, *_) in enumerate(VARIANTS):
        column, row = index % 3, index // 3
        overview.paste(render(material + "_crossbow", -38, 1, "arrow", (520, 330)), (10 + column * 540, 135 + row * 435))
        draw.text((28 + column * 540, 475 + row * 435), f"0{index + 1}  {title}", font=font(26), fill="#f0eadb")
        draw.text((28 + column * 540, 514 + row * 435), descriptions[index], font=font(18), fill="#a6b9bf")
    overview.save(ART / "overview.png")
    frames = []
    for fraction, ammunition in [(step / 12, None) for step in range(13)] + [(1, "arrow")] * 7 + [(0, None)] * 4:
        frame = render("diamond_crossbow", -38, fraction, ammunition, (600, 420))
        ImageDraw.Draw(frame).text((18, 16), "钻石弩 / 拉弦 → 装填 → 发射复位", font=font(21), fill="#e9e5d9")
        frames.append(frame)
    frames[0].save(ART / "reload.gif", save_all=True, append_images=frames[1:], duration=90, loop=0)
    print("PASS: four states x six variants x four views; overview and reload GIF")


if __name__ == "__main__":
    main()
