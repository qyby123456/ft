import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

import vanilla_renderer as renderer
from nbtio import Structure
from static_qa import digest, require, write_json


ROOT = Path(__file__).resolve().parents[1]


def plan(room):
    image = Image.new('RGB', (1330, 1150), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 25), 'S15 平面 / Y=1 / 北 -Z ↑', font=renderer.font(32), fill='#f0e1bc')
    colors = {'stone_bricks': '#777f7b', 'mossy_stone_bricks': '#6c7755', 'cracked_stone_bricks': '#626865', 'iron_bars': '#b1bac0', 'iron_door': '#8799a3', 'dark_oak_planks': '#674a37', 'cauldron': '#40434e'}
    for pos_z in range(16):
        for pos_x in range(15):
            name = room.get((pos_x, 1, pos_z)).name.removeprefix('minecraft:')
            color = colors.get(name, '#273943')
            if 6 <= pos_x <= 8:
                color = '#447f76'
            left, top = 65 + pos_x * 53, 130 + pos_z * 53
            draw.rectangle((left, top, left + 52, top + 52), fill=color, outline='#152128')
    for index in range(16):
        draw.text((25, 139 + index * 53), str(index), font=renderer.font(17), fill='#c0c8c2')
    for index in range(15):
        draw.text((78 + index * 53, 95), str(index), font=renderer.font(17), fill='#c0c8c2')
    for pos_z, label in ((0, '北'), (2, '安'), (13, '安'), (15, '南')):
        draw.text((65 + 7 * 53 + 13, 138 + pos_z * 53), label, font=renderer.font(25), fill='#ffffff')
    lines = ['15×10×16 / 短型一字贯通', '主路 X=6..8 / Y=1..3', '北口 (7,1,0)', '南口 (7,1,15)', '两口三宽三高 / 常开', '安全点 (7,1,2) 与 (7,1,13)', '左右共六间封闭装饰牢房', '四盏魂灯 / 两只四链吊笼', '主路无台阶、液体、蛛网', '无容器、物品、实体或战利品', '牢房内木台+毯作装饰床', '顶层完整 / 外墙封闭', '绿色仅表示已验通路', '软件平面图，非游戏截图']
    for index, line in enumerate(lines):
        draw.text((900, 135 + index * 58), line, font=renderer.font(21), fill='#c3d3ca')
    draw.text((45, 1030), '两侧牢房为封闭装饰空间；进出均沿Z轴直行，无岔路、转弯或楼梯。', font=renderer.font(25), fill='#ccdacb')
    return image


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    room = Structure.read(ROOT / 'room.nbt')
    preview = ROOT / 'preview'
    preview.mkdir(exist_ok=True)
    views = []
    with zipfile.ZipFile(renderer.CLIENT) as archive:
        renderer.ASSETS = archive
        for direction, label, camera in [('north', '北向内部剖视 / 向南看', (0.02, -1)), ('south', '南向内部剖视 / 向北看', (-0.02, 1)), ('west', '西向纵剖 / 向东看', (-1, 0.08)), ('east', '东向纵剖 / 向西看', (1, -0.08))]:
            image = renderer.render(room, camera, label, section=direction)
            image.save(preview / f'{direction}.png')
            views.append(image)
        renderer.render(room, (-0.8, -1), '完整外壳 / 北西', exterior=True).save(preview / 'exterior.png')
        renderer.render(room, (0.8, 1), '完整外壳 / 南东', exterior=True).save(preview / 'exterior-south.png')
        native = ROOT / 'qa/native-placed-none.nbt'
        if native.is_file():
            renderer.render(Structure.read(native), (-0.45, -1), 'Forge 放置200tick后回采', native=True, section='north').save(preview / 'native-world-capture.png')
    sheet = Image.new('RGB', (3200, 2500), '#121d23')
    for index, image in enumerate(views):
        sheet.paste(image, ((index % 2) * 1600, (index // 2) * 1250))
    sheet.save(preview / 'four-views.png')
    views[0].resize((1536, 1200), Image.Resampling.LANCZOS).save(ROOT / 'preview.png')
    plan(room).save(preview / 'plan.png')
    require(digest(ROOT / 'room.nbt') == fingerprint, 'Rendering changed source structure')
    write_json(ROOT / 'qa/render.json', {
        'status': 'PASS', 'structure_sha256': fingerprint, 'source': 'Final delivered room.nbt read from disk',
        'client_jar': str(renderer.CLIENT), 'client_jar_sha256': digest(renderer.CLIENT),
        'views': ['preview/north.png', 'preview/south.png', 'preview/west.png', 'preview/east.png'],
        'full_shell_views': ['preview/exterior.png', 'preview/exterior-south.png'],
        'four_view_sheet': 'preview/four-views.png', 'plan': 'preview/plan.png',
        'native_capture_rendered': native.is_file(), 'cutaway_only_in_memory': True,
        'limitations': ['Software orthographic textured model renders, not Minecraft client screenshots.', 'No emissive propagation, shader, exact transparency sorting, biome tint or full modpack rendering.', 'West/east sections remove the near cell row only in memory; north/south retain both rows.']
    })
    print(json.dumps({'status': 'RENDERED', 'sha256': fingerprint}))


if __name__ == '__main__':
    main()
