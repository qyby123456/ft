import hashlib
import json
import subprocess
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont


def render(mesh, texture, view, width=260, height=255):
    cameras = {"north": [0, 0, -1], "south": [0, 0, 1], "west": [-1, 0, 0], "east": [1, 0, 0], "perspective": [-0.32, 0.13, -1]}
    camera = np.array(cameras[view], dtype=float)
    camera /= np.linalg.norm(camera)
    horizontal = np.cross(camera, [0, 1, 0])
    horizontal /= np.linalg.norm(horizontal)
    vertical = np.cross(horizontal, camera)
    pixels = np.zeros((height, width, 3), dtype=np.uint8)
    pixels[:] = [30, 39, 52]
    zbuffer = np.full((height, width), -np.inf)
    texture_pixels = np.array(texture)
    scale = 13
    for face in mesh:
        vertices = np.array(face["vertices"])
        normal = np.cross(vertices[1] - vertices[0], vertices[2] - vertices[0])
        normal /= np.linalg.norm(normal)
        if np.dot(normal, camera) <= 0.00001:
            continue
        centered = vertices - [8, 8, 8]
        screen = np.column_stack([width / 2 + centered @ horizontal * scale, 118 - centered @ vertical * scale])
        depth = centered @ camera
        uv = np.array(face["uv"])
        brightness = {"north": 1, "south": 0.94, "west": 0.8, "east": 0.88, "up": 1, "down": 0.65}[face["face"]]
        for indices in ([0, 1, 2], [0, 2, 3]):
            triangle = screen[indices]
            minimum = np.maximum(np.floor(triangle.min(axis=0)).astype(int), [0, 0])
            maximum = np.minimum(np.ceil(triangle.max(axis=0)).astype(int), [width - 1, height - 1])
            if np.any(maximum < minimum):
                continue
            grid_y, grid_x = np.mgrid[minimum[1]:maximum[1] + 1, minimum[0]:maximum[0] + 1]
            first, second, third = triangle
            denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
            if abs(denominator) < 0.00001:
                continue
            first_weight = ((second[1] - third[1]) * (grid_x + 0.5 - third[0]) + (third[0] - second[0]) * (grid_y + 0.5 - third[1])) / denominator
            second_weight = ((third[1] - first[1]) * (grid_x + 0.5 - third[0]) + (first[0] - third[0]) * (grid_y + 0.5 - third[1])) / denominator
            third_weight = 1 - first_weight - second_weight
            weights = np.stack([first_weight, second_weight, third_weight], axis=-1)
            interpolated_depth = weights @ depth[indices]
            coordinates = weights @ uv[indices]
            tex_x = np.clip((coordinates[:, :, 0] * texture.width / 16).astype(int), 0, texture.width - 1)
            tex_y = np.clip((coordinates[:, :, 1] * texture.height / 16).astype(int), 0, texture.height - 1)
            sampled = texture_pixels[tex_y, tex_x]
            visible = (weights.min(axis=-1) >= -0.00001) & (interpolated_depth > zbuffer[grid_y, grid_x]) & (sampled[:, :, 3] > 0)
            zbuffer[grid_y[visible], grid_x[visible]] = interpolated_depth[visible]
            pixels[grid_y[visible], grid_x[visible]] = np.clip(sampled[visible, :3] * brightness, 0, 255).astype(np.uint8)
    return Image.fromarray(pixels)


def main():
    project = Path(__file__).resolve().parents[2]
    resources = Path(sys.argv[1]) if len(sys.argv) > 1 else project / "src/main/resources"
    output = Path(sys.argv[2]) if len(sys.argv) > 2 else project / "art/material_bows"
    output.mkdir(parents=True, exist_ok=True)
    assets = resources / "assets/ftboss"
    names = ["wooden_bow", "stone_bow", "iron_bow", "golden_bow", "diamond_bow", "netherite_bow"]
    labels = ["木 · 绑绳反曲", "石 · 拼甲短弓", "铁 · 铆接战弓", "金 · 镂饰礼弓", "钻石 · 晶刃长弓", "合金 · 重甲战弓"]
    views = [("正面 / FRONT", "north"), ("背面 / BACK", "south"), ("左侧 / LEFT", "west"), ("右侧 / RIGHT", "east")]
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 15)
    title_font = ImageFont.truetype("C:/Windows/Fonts/msyhbd.ttc", 25)
    cell_width, cell_height = 260, 255
    reports = []
    overview = Image.new("RGB", (cell_width * 6, 610), "#171e29")
    heading = ImageDraw.Draw(overview)
    heading.text((25, 15), "FarmingTales · 材质弓重制 / ORIGINAL RECURVE SERIES", font=title_font, fill="#eee7d6")
    heading.text((25, 52), "上：静置立体视图     下：满弓正面 / 六种独立造型 · 原版像素密度", font=font, fill="#9cacc2")
    for state in range(4):
        sheet = Image.new("RGB", (cell_width * 4, 90 + cell_height * 6), "#171e29")
        draw = ImageDraw.Draw(sheet)
        draw.text((22, 10), "材质弓重制 · " + ["静置", "拉弦Ⅰ", "拉弦Ⅱ", "满弓"][state], font=title_font, fill="#e9e8df")
        for column, (label, face) in enumerate(views):
            draw.text((column * cell_width + 45, 55), label, font=font, fill="#96a9c2")
        for row, (name, label) in enumerate(zip(names, labels)):
            suffix = "" if state == 0 else "_pulling_" + str(state - 1)
            model_path = assets / ("models/item/" + name + suffix + ".json")
            model = json.loads(model_path.read_text(encoding="utf-8"))
            mesh = json.loads(subprocess.check_output(["node", str(project / "tools/model3d/material_bow_mesh.cjs"), str(model_path)]))
            texture_path = resources / "assets" / (model["textures"]["bow"].replace(":", "/textures/") + ".png")
            texture = Image.open(texture_path).convert("RGBA")
            for column, (_, face) in enumerate(views):
                image = render(mesh, texture, face)
                ImageDraw.Draw(image).text((12, 234), label, font=font, fill="#e8e5dc")
                sheet.paste(image, (column * cell_width, 90 + row * cell_height))
                if state == 3 and column == 0:
                    overview.paste(image, (row * cell_width, 348))
            if state == 0:
                image = render(mesh, texture, "perspective")
                ImageDraw.Draw(image).text((12, 234), label, font=font, fill="#e8e5dc")
                overview.paste(image, (row * cell_width, 86))
            reports.append({"model": str(model_path), "modelSha256": hashlib.sha256(model_path.read_bytes()).hexdigest(),
                            "textureSha256": hashlib.sha256(texture_path.read_bytes()).hexdigest(), "views": [face for _, face in views]})
        sheet.save(output / ("four_view_" + str(state) + ".png"))
    overview.save(output / "overview.png")
    (output / "manifest.json").write_text(json.dumps(reports, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"models": len(reports), "views": len(reports) * 4, "output": str(output)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
