import sys
import zipfile
from pathlib import Path

from PIL import ImageDraw

import build_archive_cell as builder


sys.path.insert(0, "D:/ft工程文件/墓狱档案地牢_20261101/tools")
import render_archive as renderer


def cutaway(blocks):
    return {position: state for position, state in blocks.items() if position[1] < 7 and not (position[1] > 0 and (position[0] == 0 or position[2] == 0)) and not (position[0] == 1 and 2 <= position[2] <= 6 and position[1] >= 3) and not (position[1] == 6 and position[2] == 3)}


def main():
    renderer.ASSETS = zipfile.ZipFile(Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar")
    renderer.SIZE = builder.SIZE
    renderer.cutaway = cutaway
    template = builder.load_structure(builder.OUTPUT / "room.nbt")
    blocks = {tuple(record["pos"]): template["palette"][record["state"]] for record in template["blocks"]}
    hero = renderer.isometric(blocks, size=(1500, 1150))
    draw = ImageDraw.Draw(hero)
    draw.rectangle((0, 0, hero.width, 89), fill=renderer.BACKGROUND)
    draw.text((48, 22), "S1  墓狱档案囚室 / 9×11 占地 / 高8格", font=renderer.font(28), fill=renderer.TEXT)
    draw.text((48, 62), "原版书架 · 石台与可读手抄本 · 铁栅档案龛 · 青灯 · 无奖励箱", font=renderer.font(19), fill=renderer.MUTED)
    hero.save(builder.OUTPUT / "preview.png")
    renderer.isometric(blocks, size=(1300, 1050), exterior=True).save(builder.OUTPUT / "exterior.png")
    builder.write_json(builder.OUTPUT / "render.json", {"source": "room.nbt", "cutaway_only_in_preview": True, "real_roof_present": True, "models": sorted(renderer.USED_MODELS), "textures": sorted(renderer.USED_TEXTURES), "limits": "Software-rendered vanilla block meshes; simplified book and skull block-entity appearance; not a game screenshot."})
    print("S1 preview and closed exterior rendered from final room.nbt")


if __name__ == "__main__":
    main()
