import json
import subprocess
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from import_models import ART, ASSETS, MATERIALS, PROJECT, SUFFIXES, WORK, digest, write_json


VIEWS = {"north": [0, 0, -1], "south": [0, 0, 1], "west": [-1, 0, 0],
         "east": [1, 0, 0], "perspective": [-1, 0.12, -0.4]}
LABELS = ["木弓", "石弓", "铁弓", "金弓", "钻石弓", "下界合金弓"]
STATES = ["静置", "拉弦Ⅰ", "拉弦Ⅱ", "满弓"]


def render(mesh, textures, view, width=300, height=340):
    camera = np.array(VIEWS[view], dtype=float)
    camera /= np.linalg.norm(camera)
    horizontal = np.cross(camera, [0, 1, 0])
    horizontal /= np.linalg.norm(horizontal)
    vertical = np.cross(horizontal, camera)
    pixels = np.zeros((height, width, 3), dtype=np.uint8)
    pixels[:] = [30, 39, 52]
    zbuffer = np.full((height, width), -np.inf)
    scale = min(6, (width - 24) / 46, (height - 50) / 48)
    for face in mesh:
        texture = textures[face["texture"]]
        texture_pixels = np.asarray(texture)
        vertices = np.array(face["vertices"])
        normal = np.cross(vertices[1] - vertices[0], vertices[2] - vertices[0])
        length = np.linalg.norm(normal)
        if length < 0.000001 or np.dot(normal / length, camera) <= 0.00001:
            continue
        centered = vertices - [8, 9, 9]
        screen = np.column_stack([width / 2 + centered @ horizontal * scale, height / 2 - centered @ vertical * scale])
        depth, uv = centered @ camera, np.array(face["uv"])
        brightness = {"north": 1, "south": 0.94, "west": 0.9, "east": 1, "up": 1, "down": 0.7}[face["face"]]
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
            first_weight = ((second[1] - third[1]) * (grid_x + 0.5 - third[0])
                            + (third[0] - second[0]) * (grid_y + 0.5 - third[1])) / denominator
            second_weight = ((third[1] - first[1]) * (grid_x + 0.5 - third[0])
                             + (first[0] - third[0]) * (grid_y + 0.5 - third[1])) / denominator
            weights = np.stack([first_weight, second_weight, 1 - first_weight - second_weight], axis=-1)
            interpolated_depth, coordinates = weights @ depth[indices], weights @ uv[indices]
            tex_x = np.clip((coordinates[:, :, 0] * texture.width / 16).astype(int), 0, texture.width - 1)
            tex_y = np.clip((coordinates[:, :, 1] * texture.height / 16).astype(int), 0, texture.height - 1)
            sampled = texture_pixels[tex_y, tex_x]
            visible = ((weights.min(axis=-1) >= -0.00001) & (interpolated_depth > zbuffer[grid_y, grid_x])
                       & (sampled[:, :, 3] > 0))
            zbuffer[grid_y[visible], grid_x[visible]] = interpolated_depth[visible]
            pixels[grid_y[visible], grid_x[visible]] = np.clip(sampled[visible, :3] * brightness, 0, 255).astype(np.uint8)
    return Image.fromarray(pixels)


def main():
    ART.mkdir(parents=True, exist_ok=True)
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 18)
    title = ImageFont.truetype("C:/Windows/Fonts/msyhbd.ttc", 26)
    views = [("正面 / -Z", "north"), ("背面 / +Z", "south"), ("左侧 / -X", "west"), ("右侧 / +X", "east")]
    overview = Image.new("RGB", (1200, 1160), "#171e29")
    heading = ImageDraw.Draw(overview)
    heading.text((22, 12), "六种材料弓 · bow.zip 原模型导入", font=title, fill="#eee7d6")
    heading.text((22, 53), "每格：静置 / 满弓；原 PNG 与 UV 保留，拉弦箭矢已补齐", font=font, fill="#9cacc2")
    reports = []
    animation_frames = []
    for state, suffix in enumerate(SUFFIXES):
        sheet = Image.new("RGB", (1200, 90 + 350 * 6), "#171e29")
        draw = ImageDraw.Draw(sheet)
        draw.text((22, 10), "材料弓导入 · " + STATES[state], font=title, fill="#eee7d6")
        for column, (label, _) in enumerate(views):
            draw.text((column * 300 + 70, 55), label, font=font, fill="#9cacc2")
        for row, (material, label) in enumerate(zip(MATERIALS, LABELS)):
            model_path = ASSETS / f"models/item/{material}_bow{suffix}.json"
            texture_path = ASSETS / f"textures/item/{material}_bow.png"
            mesh = json.loads(subprocess.check_output([
                "node", str(PROJECT / "tools/model3d/material_bow_mesh.cjs"), str(model_path)]))
            model = json.loads(model_path.read_text(encoding="utf-8"))
            bindings = [face["texture"] for element in model["elements"] for face in element["faces"].values()]
            for face, binding in zip(mesh, bindings):
                face["texture"] = binding
            textures = {"#" + key: Image.open(ASSETS / ("textures/" + resource.split(":")[1] + ".png")).convert("RGBA")
                        for key, resource in model["textures"].items() if key != "particle"}
            for column, (_, view) in enumerate(views):
                image = render(mesh, textures, view)
                ImageDraw.Draw(image).text((12, 310), label, font=font, fill="#eee7d6")
                sheet.paste(image, (column * 300, 90 + row * 350))
            if state in (0, 3):
                image = render(mesh, textures, "perspective", 200, 480)
                ImageDraw.Draw(image).text((12, 435), label + " · " + STATES[state], font=font, fill="#eee7d6")
                overview.paste(image, (row % 3 * 400 + (200 if state == 3 else 0), 100 + row // 3 * 530))
            if material == "diamond":
                image = render(mesh, textures, "west", 400, 400)
                ImageDraw.Draw(image).text((15, 12), "钻石弓 · " + STATES[state], font=font, fill="#eee7d6")
                animation_frames.append(image)
            reports.append({"model": str(model_path), "modelSha256": digest(model_path.read_bytes()),
                            "textureSha256": digest(texture_path.read_bytes()), "views": list(VIEWS)[:4]})
        sheet.save(ART / f"four_views_{state}.png")
    overview.save(ART / "overview.png")
    sequence = animation_frames + list(reversed(animation_frames[1:-1]))
    sequence[0].save(ART / "drawing.gif", save_all=True, append_images=sequence[1:], duration=300, loop=0)
    write_json(WORK / "render-manifest.json", reports)
    print(json.dumps({"models": len(reports), "directions": len(reports) * 4, "output": str(ART)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
