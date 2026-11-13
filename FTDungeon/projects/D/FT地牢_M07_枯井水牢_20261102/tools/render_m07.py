from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from build_m07 import ENTRANCE, FEET, INTERIOR, OUT, ROOT, TARGETS, dump
from nbtio import Structure


sys.path.insert(0, str(Path(__file__).resolve().parent / "vendor"))
import review


original_filtered = review.filtered
original_opaque = review.opaque


def filtered(structure, metadata, camera, exterior):
    visible = original_filtered(structure, metadata, camera, exterior)
    if not exterior:
        visible = {position: block for position, block in visible.items() if position[1] != 11}
    return visible


def opaque(block):
    if block is not None and block.name.removeprefix("minecraft:") in ("iron_trapdoor", "stone_brick_slab", "tripwire_hook", "lever", "grindstone", "cauldron", "lantern", "wall_torch", "skeleton_skull"):
        return False
    return original_opaque(block)


review.filtered = filtered
review.opaque = opaque


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def render_view(structure, metadata, camera, title, exterior=False, size=(1300, 1000)):
    image = review.render(structure, metadata, camera, title, exterior, size=size)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, size[1] - 55, size[0], size[1]), fill="#101a20")
    draw.text((32, size[1] - 42), "实际 NBT 软件预览 · 非实机截图 · 骷髅为近似实体外观", font=font(17), fill="#a0b4be")
    return image


def floor_plan(structure, reachable):
    image = Image.new("RGB", (470, 600), "#13212b")
    draw = ImageDraw.Draw(image)
    draw.text((22, 15), "N 北入口 / Y=4 脚位", font=font(23), fill="#dfe9e4")
    scale = 27
    for block_x in range(15):
        for block_z in range(17):
            position = block_x, FEET, block_z
            name = structure.get(position).name.removeprefix("minecraft:")
            color = "#2f414b"
            if position in reachable:
                color = "#306a65"
            elif name == "air":
                color = "#20303c"
            elif name in ("iron_bars", "iron_trapdoor"):
                color = "#74898b"
            elif name == "cauldron":
                color = "#9a7451"
            elif name == "skeleton_skull":
                color = "#d8ccac"
            else:
                color = "#555a55"
            left, top = 32 + block_x * scale, 62 + block_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=color)
            if (block_x, block_z) == (12, 9):
                draw.line((left + 7, top + 12, left + scale - 7, top + 12), fill="#78b7ce", width=3)
            if position in (ENTRANCE, INTERIOR):
                draw.text((left + 4, top), "E" if position == ENTRANCE else "I", font=font(19), fill="#ffffff")
            if position in TARGETS:
                draw.ellipse((left + 10, top + 9, left + 15, top + 14), fill="#e8d382")
    draw.text((32, 535), "青绿：干燥可达  E 入口  I 安全点", font=font(17), fill="#a2babd")
    draw.text((32, 566), "黄点：环路检查点；东侧为有盖暗渠", font=font(17), fill="#a2babd")
    return image


def main():
    metadata = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    structure = Structure.read(OUT / "room.nbt")
    digest = hashlib.sha256((OUT / "room.nbt").read_bytes()).hexdigest()
    assert digest == metadata["sha256"]
    reachable = {tuple(position) for position in json.loads((ROOT / "qa/reachable.json").read_text(encoding="utf-8"))}
    views = ROOT / "qa/views"
    views.mkdir(parents=True, exist_ok=True)
    sheet = Image.new("RGB", (2400, 1900), "#101a20")
    for index, (direction, title, camera) in enumerate(review.VIEWS):
        image = render_view(structure, metadata, camera, title, size=(1200, 950))
        image.save(views / (direction + ".png"))
        sheet.paste(image, ((index % 2) * 1200, (index // 2) * 950))
    sheet.save(ROOT / "qa/four-views.png")
    render_view(structure, metadata, (-0.35, -1), "完整石顶 / 唯一北接口", exterior=True, size=(1400, 1100)).save(ROOT / "qa/exterior.png")
    plan = floor_plan(structure, reachable)
    plan.save(ROOT / "qa/floor-plan.png")
    hero = render_view(structure, {**metadata, "view_elevation": 2.2}, (-0.23, -1), "15×17 占地 · 高13 · 完整屋顶仅在图中剖切", size=(1420, 1250))
    preview = Image.new("RGB", (1920, 1250), "#101a20")
    preview.paste(hero, (0, 0))
    preview.paste(plan, (1435, 94))
    draw = ImageDraw.Draw(preview)
    draw.text((1458, 730), "M07 / 枯井水牢", font=font(30), fill="#86d7c6")
    lines = ["· 中央5×5石井 / 闭合铁格盖", "· 33格水源，封闭井腔与暗渠", "· 双牢龛、4道墙链与锚钩", "· 4盏吊灯 / 30节逐段锚定锁链", "· 干燥绕井环路 / 3×3北接口", "· 无奖励、预放实体或刷怪笼", "· 入口脚位Y=4，接入时需阶梯"]
    for index, line in enumerate(lines):
        draw.text((1458, 788 + index * 48), line, font=font(21), fill="#c4d4d5")
    draw.text((1458, 1180), "NBT SHA-256  " + digest[:16] + "…", font=font(16), fill="#809da9")
    preview.save(OUT / "preview.png")
    dump(ROOT / "qa/render.json", {"id": "M07", "status": "PASS", "sha256": digest, "source": str(OUT / "room.nbt"), "render_kind": "Actual NBT, Minecraft 1.20.1 vanilla JSON models and textures, software rasterization", "preview_size": [1920, 1250], "models": sorted(review.renderer.USED_MODELS), "textures": sorted(review.renderer.USED_TEXTURES), "views": ["north", "south", "east", "west", "exterior", "floor_plan"], "limitations": ["Not an in-game screenshot; no Minecraft lighting, fluid or shader simulation", "Skull block entity appearance is approximated", "Both roof layers are hidden for cutaway images only; NBT unchanged"]})
    print(json.dumps({"status": "PASS", "sha256": digest, "preview": str(OUT / "preview.png")}))


if __name__ == "__main__":
    main()
