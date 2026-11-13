from __future__ import annotations

import copy
import hashlib
import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from build_s6 import OUT, ROOT, dump
from nbtio import Structure


sys.path.insert(0, str(Path(__file__).resolve().parent / "vendor"))
import review


original_definitions = review.definitions
original_opaque = review.opaque


def definitions(name, properties):
    if name == "skeleton_wall_skull":
        definition, choice = review.renderer.synthetic_model("skeleton_skull", {"rotation": "0"})
        definition = copy.deepcopy(definition)
        definition["elements"][0]["from"] = [4, 4, 8]
        definition["elements"][0]["to"] = [12, 12, 16]
        choice = {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]}
        return [(definition, choice)]
    return original_definitions(name, properties)


def opaque(block):
    if block is not None and block.name.removeprefix("minecraft:") in ("glass", "skeleton_wall_skull", "soul_fire", "pointed_dripstone"):
        return False
    return original_opaque(block)


review.definitions = definitions
review.opaque = opaque


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def render_view(structure, metadata, camera, title, exterior=False, size=(1300, 1000)):
    image = review.render(structure, metadata, camera, title, exterior, size=size)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, size[1] - 52, size[0], size[1]), fill="#101a20")
    draw.text((35, size[1] - 40), "实际 NBT 软件渲染 · 非游戏截图 · 骷髅方块实体外观为近似几何", font=font(17), fill="#9bacb8")
    return image


def floor_plan(structure, reachable):
    image = Image.new("RGB", (450, 470), "#131f28")
    draw = ImageDraw.Draw(image)
    draw.text((22, 16), "北入口 / 干燥通路", font=font(24), fill="#e4ecee")
    scale = 42
    for block_x in range(9):
        for block_z in range(9):
            position = block_x, 1, block_z
            name = structure.get(position).name
            color = "#30424e"
            if position in reachable:
                color = "#265d60"
            elif "air" not in name:
                color = "#775247" if "nether" in name else "#454952"
            if block_z == 7 and block_x in (1, 7):
                color = "#eb803c"
            if (block_x, block_z) == (4, 4):
                color = "#76e7eb"
            left, top = 36 + block_x * scale, 62 + block_z * scale
            draw.rectangle((left, top, left + scale - 3, top + scale - 3), fill=color)
            if position in ((4, 1, 0), (4, 1, 2)):
                draw.text((left + 8, top + 4), "E" if block_z == 0 else "I", font=font(24), fill="#ffffff")
    draw.text((36, 444), "E 入口  I 安全点  青绿：无火无液体的行走区", font=font(14), fill="#9bb6bd")
    return image


def main():
    metadata = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    structure = Structure.read(OUT / "room.nbt")
    reachable = {tuple(position) for position in json.loads((ROOT / "qa/reachable.json").read_text(encoding="utf-8"))}
    destination = ROOT / "qa/views"
    destination.mkdir(parents=True, exist_ok=True)
    sheet = Image.new("RGB", (2400, 1800), "#101a20")
    for index, (direction, title, camera) in enumerate(review.VIEWS):
        image = render_view(structure, metadata, camera, title, size=(1200, 900))
        image.save(destination / (direction + ".png"))
        sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
    sheet.save(ROOT / "qa/four-views.png")
    exterior = render_view(structure, metadata, (-0.8, -1), "完整屋顶与北接口", exterior=True, size=(1100, 850))
    exterior.save(ROOT / "qa/exterior.png")
    hero = render_view(structure, metadata, (-0.8, -1), "9×9 占地 · 高 7 格", size=(1280, 1100))
    preview = Image.new("RGB", (1760, 1100), "#101a20")
    preview.paste(hero, (0, 0))
    preview.paste(floor_plan(structure, reachable), (1290, 95))
    draw = ImageDraw.Draw(preview)
    draw.text((1312, 595), "S6 / 地狱魂火祭坛", font=font(26), fill="#76e7eb")
    lines = ["· 1 处魂火 / 7 盏灵魂灯", "· 9 节锁链，逐段验证顶锚", "· 3 颗原版骷髅，实体无库存", "· 西：封闭熔岩落流", "· 东：储液 + 实体支撑 + 滴石", "· 三格宽高北接口，完整屋顶", "· 仅原版方块 / 无奖励箱"]
    for index, line in enumerate(lines):
        draw.text((1312, 650 + index * 43), line, font=font(21), fill="#c3cdd2")
    draw.text((1312, 1010), "SHA-256 " + metadata["sha256"][:18] + "…", font=font(15), fill="#7e9ba8")
    preview.save(OUT / "preview.png")
    assert Image.open(OUT / "preview.png").size == (1760, 1100)
    dump(ROOT / "qa/render.json", {"status": "PASS", "source": str(OUT / "room.nbt"), "sha256": hashlib.sha256((OUT / "room.nbt").read_bytes()).hexdigest(), "render_kind": "actual NBT, vanilla block models/textures, software rasterization", "client_screenshot": False, "cutaway_only_in_preview": True, "all_model_states_rendered": len({block for block in structure.blocks.values()}), "models": sorted(review.renderer.USED_MODELS), "textures": sorted(review.renderer.USED_TEXTURES), "texture_archive": str(review.renderer.CLIENT), "limitations": ["Skull block-entity mesh/UV is approximate", "No dynamic lava particles or full Minecraft lighting", "Cutaway temporarily removes near walls/roof in renders only"]})
    print("S6 previews rendered from room.nbt")


if __name__ == "__main__":
    main()
