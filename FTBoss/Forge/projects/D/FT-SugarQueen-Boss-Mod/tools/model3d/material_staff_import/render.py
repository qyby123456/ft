import copy
import json
import subprocess
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from import_models import MATERIALS, PROJECT, WORK, digest, write_json

sys.path.insert(0, str(PROJECT / "tools/model3d"))
from render_material_bows import render


def main():
    resources = Path(sys.argv[1]) if len(sys.argv) > 1 else WORK / "resources"
    output = Path(sys.argv[2]) if len(sys.argv) > 2 else WORK / "verification"
    output.mkdir(parents=True, exist_ok=True)
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 18)
    title = ImageFont.truetype("C:/Windows/Fonts/msyhbd.ttc", 26)
    labels = ["木法杖", "石法杖", "铁法杖", "金法杖", "钻石法杖", "下界合金法杖"]
    directions = [("正面 / FRONT", "north"), ("背面 / BACK", "south"),
                  ("左侧 / LEFT", "west"), ("右侧 / RIGHT", "east")]
    overview = Image.new("RGB", (1500, 1000), "#171e29")
    overview_draw = ImageDraw.Draw(overview)
    overview_draw.text((24, 12), "六种材料法杖 · staff.zip 模型导入", font=title, fill="#eee7d6")
    overview_draw.text((24, 54), "原模型、原贴图、原 UV 与手持姿态保留；每种下方列出四个方向", font=font, fill="#a9b7ca")
    reports = []
    for row, (material, label) in enumerate(zip(MATERIALS, labels)):
        item_name = material + "_staff"
        model_path = resources / f"assets/ftboss/models/item/{item_name}.json"
        texture_path = resources / f"assets/ftboss/textures/item/{item_name}.png"
        mesh = json.loads(subprocess.check_output(["node", str(PROJECT / "tools/model3d/material_bow_mesh.cjs"), str(model_path)]))
        vertices = np.array([point for face in mesh for point in face["vertices"]])
        center = (vertices.min(axis=0) + vertices.max(axis=0)) / 2
        adjusted = copy.deepcopy(mesh)
        for face in adjusted:
            face["vertices"] = ((np.array(face["vertices"]) - center) * 0.68 + [8, 8 + (118 - 220) / 13, 8]).tolist()
        with Image.open(texture_path) as source_texture:
            texture = source_texture.convert("RGBA")
        sheet = Image.new("RGB", (1000, 520), "#171e29")
        draw = ImageDraw.Draw(sheet)
        draw.text((20, 8), label + " · 四视图", font=title, fill="#eee7d6")
        for column, (direction, view) in enumerate(directions):
            image = render(adjusted, texture, view, 250, 440)
            sheet.paste(image, (column * 250, 75))
            draw.text((column * 250 + 38, 48), direction, font=font, fill="#a9b7ca")
        sheet.save(output / f"{item_name}-four-views.png")
        overview.paste(sheet.resize((500, 260), Image.Resampling.LANCZOS), (row % 3 * 500, 460 + row // 3 * 265))
        perspective = render(adjusted, texture, "perspective", 250, 440)
        perspective.thumbnail((250, 340), Image.Resampling.LANCZOS)
        overview.paste(perspective, (row * 250 + (250 - perspective.width) // 2, 88))
        overview_draw.text((row * 250 + 40, 420), label, font=font, fill="#eee7d6")
        reports.append({"id": "ftboss:" + item_name, "modelSha256": digest(model_path.read_bytes()),
                        "textureSha256": digest(texture_path.read_bytes()), "directions": [view for _, view in directions],
                        "sourceBounds": [vertices.min(axis=0).tolist(), vertices.max(axis=0).tolist()]})
    overview.save(output / "overview.png")
    write_json(output / "render-manifest.json", reports)
    print(json.dumps({"models": len(reports), "views": len(reports) * 4, "output": str(output)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
