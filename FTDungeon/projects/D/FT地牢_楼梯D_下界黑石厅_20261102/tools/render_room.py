import functools
import hashlib
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

import texture_engine as engine
from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]
CLIENT = Path('C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
ORIGINAL_DEFINITIONS = engine.definitions


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


def definitions(name, properties):
    if name == 'lava':
        return [(engine.box([0, 0, 0], [16, 16, 16], 'block/lava_still'), {})]
    if name == 'red_wall_banner':
        return [(engine.box([1, 0, 14], [15, 16, 15], 'block/red_wool'), {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    return ORIGINAL_DEFINITIONS(name, properties)


def opaque(block):
    if block is None:
        return False
    name = block.name.removeprefix('minecraft:')
    return name not in {'air', 'lava', 'orange_stained_glass', 'lantern', 'soul_lantern', 'chain', 'red_wall_banner'} and not name.endswith('_stairs')


def render(room, camera, label, exterior=False, native=False, section=False):
    size = (1800, 1450)
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 1.05
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale
    visible = {}
    for position, block in room.blocks.items():
        pos_x, pos_y, pos_z = position
        if block.name == 'minecraft:air':
            continue
        near_x = pos_x == (0 if camera_x < 0 else 28)
        near_z = pos_z == (0 if camera_z < 0 else 32)
        if not exterior:
            if pos_y >= 21 or pos_y >= 1 and (near_x or near_z):
                continue
            if camera_x > 0 and 25 <= pos_x <= 28 and 1 <= pos_y <= 5 and pos_z >= 14:
                continue
        if section and not (19 <= pos_x <= 25 and pos_y <= 13):
            continue
        if section and pos_x == 25 and pos_y > 0:
            continue
        visible[position] = block

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

    faces = []
    for position, block in visible.items():
        name = block.name.removeprefix('minecraft:')
        for points, normal, cull, identifier, bounds, rotation in engine.geometry(name, block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            order = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / 4
            brightness = 1.18 if normal[1] > 0.5 else 0.98 if normal[0] < 0 else 1.08
            if name == 'gold_block':
                brightness = 0.94 if normal[1] > 0.5 else 0.82
            elif name in {'lava', 'lantern', 'soul_lantern'}:
                brightness *= 1.2
            faces.append((order, [project(point) for point in world], identifier, bounds, rotation, round(brightness, 3)))
    projected = [point for face in faces for point in face[1]]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((size[0] - 150) / (maximum_x - minimum_x), (size[1] - 255) / (maximum_y - minimum_y))
    offset_x = size[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 175 + (size[1] - 255) / 2 - (minimum_y + maximum_y) * scale / 2
    image = Image.new('RGBA', size, '#17151c')
    for order, points, identifier, bounds, rotation, brightness in sorted(faces, key=lambda entry: entry[0]):
        coordinates = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        engine.warp(image, engine.texture(identifier, bounds, rotation, brightness), coordinates)
    draw = ImageDraw.Draw(image)
    draw.text((42, 24), 'D  下界黑石楼梯厅  /  ' + label, font=font(36), fill='#f2d7a0')
    source = 'Forge 放置后 200 tick 回采 NBT' if native else '最终交付 room.nbt'
    draw.text((42, 84), source + '  ·  29×23×33  ·  ' + ('完整屋顶与外壳' if exterior else '仅显示剖切，交付模板仍完整封闭'), font=font(23), fill='#b8a9ad')
    draw.text((42, size[1] - 54), '原版模型/材质软件预览，非游戏截图；灯光和玻璃为软件近似，红旗按色块简化。', font=font(21), fill='#a4a0aa')
    return image.convert('RGB')


def logic_plan(room, blueprint):
    image = Image.new('RGB', (1800, 1380), '#17151c')
    draw = ImageDraw.Draw(image)
    draw.text((40, 22), 'D  三层通行与安全逻辑 / 实际 NBT 坐标', font=font(34), fill='#f2d7a0')
    draw.text((40, 76), '北 ↑  ·  X 向右 / Z 向下  ·  所有标注为玩家脚部坐标  ·  玻璃顶封闭真实熔岩源', font=font(22), fill='#bbb0b6')
    cell, top = 23, 155
    for panel, height, title in ((0, 7, '主层 Y=7'), (1, 13, '平台 Y=13'), (2, 1, '下层 Y=1')):
        left = 42 + panel * 590
        draw.text((left, top - 40), title, font=font(26), fill='#d8c08d')
        for pos_z in range(33):
            for pos_x in range(29):
                block = room.get((pos_x, height, pos_z))
                below = room.get((pos_x, height - 1, pos_z))
                name = block.name.removeprefix('minecraft:')
                color = '#323039' if name != 'air' else '#4f5d5b' if below.name != 'minecraft:air' else '#19191f'
                if name.endswith('_stairs') or below.name.endswith('_stairs'):
                    color = '#c2a15d'
                if below.name == 'minecraft:orange_stained_glass':
                    color = '#ed883b'
                draw.rectangle((left + pos_x * cell * 0.76, top + pos_z * cell, left + (pos_x + 1) * cell * 0.76 - 1, top + (pos_z + 1) * cell - 1), fill=color)
        for specification in blueprint['routes']:
            points = [(left + point[0] * cell * 0.76, top + point[1] * cell) for point in specification['waypoints']]
            draw.line(points, fill='#79c5c8' if specification['end_y'] != 1 else '#e0a05b', width=3)
        for point in blueprint['safe_points']:
            if point[1] == height:
                center_x, center_y = left + (point[0] + 0.5) * cell * 0.76, top + (point[2] + 0.5) * cell
                draw.ellipse((center_x - 5, center_y - 5, center_x + 5, center_y + 5), fill='#b9ebc7')
    draw.text((42, 965), '北主入口 (14,7,0)    →    9 格宽大台阶    →    平台脚位 Y=13    →    7 格宽二段台阶    →    上接口 (14,17,32)', font=font(25), fill='#d8c08d')
    draw.text((42, 1023), '主层东侧 (22,7,7)    →    5 格宽下行梯段    →    下层净高 ≥3    →    下接口 (22,1,32)', font=font(25), fill='#d8c08d')
    draw.text((42, 1090), '橙色：六面封闭熔岩渠；青线：上行路径；橙线：下行路径；浅绿：安全点。', font=font(24), fill='#bbb0b6')
    draw.text((42, 1150), '所有入口为 3×3；台阶均有完整砌体支撑；平台边缘、梯段两侧及下行洞口设连续黑石金顶实体护栏。', font=font(23), fill='#bbb0b6')
    draw.text((42, 1210), '安全点：(14,7,4) / (12,13,18) / (14,17,30) / (22,1,29)   ·   完整基础与屋顶   ·   无实体 / 奖励 / 裸火', font=font(23), fill='#bbb0b6')
    draw.text((42, 1270), '软件示意不是整合包实机截图；实际路由、原生碰撞与稳定性结果见 qa/static.json 和 qa/native.json。', font=font(22), fill='#9996a1')
    return image


def profile(room):
    image = Image.new('RGB', (1800, 1170), '#17151c')
    draw = ImageDraw.Draw(image)
    draw.text((40, 24), 'D  下行梯段 / X=22 真实方块纵剖', font=font(35), fill='#f2d7a0')
    draw.text((40, 84), '北 ← Z → 南  ·  黑石楼梯高侧朝北  ·  每半格升降 0.5  ·  非 AI / 非实机截图', font=font(23), fill='#bbb0b6')
    cell, left, top = 43, 150, 200
    colors = {'air': '#32353e', 'polished_blackstone_bricks': '#655b6d', 'polished_blackstone': '#655b6d',
              'red_nether_bricks': '#6f2734', 'gilded_blackstone': '#907b42', 'gold_block': '#c3a04e', 'lantern': '#ffba63', 'chain': '#a5a4ad'}
    for pos_z in range(33):
        for pos_y in range(14):
            block = room.get((22, pos_y, pos_z))
            name = block.name.removeprefix('minecraft:')
            pos_left, pos_top = left + pos_z * cell, top + (13 - pos_y) * cell
            rectangle = (pos_left, pos_top, pos_left + cell - 1, pos_top + cell - 1)
            draw.rectangle(rectangle, fill=colors.get(name, '#645b6c'), outline='#26232c')
            if name.endswith('_stairs'):
                draw.rectangle(rectangle, fill='#32353e')
                draw.polygon([(pos_left, pos_top), (pos_left + cell / 2, pos_top), (pos_left + cell / 2, pos_top + cell / 2),
                              (pos_left + cell, pos_top + cell / 2), (pos_left + cell, pos_top + cell), (pos_left, pos_top + cell)], fill='#c3a04e')
        if pos_z % 2 == 0:
            draw.text((left + pos_z * cell + 7, top + 14 * cell + 12), str(pos_z), font=font(18), fill='#b3a3a9')
    for pos_y in range(14):
        draw.text((left - 50, top + (13 - pos_y) * cell + 8), str(pos_y), font=font(19), fill='#b3a3a9')
    path = [(7, 7), (8, 7), (8.5, 6.5), (9, 6), (9.5, 5.5), (10, 5), (10.5, 4.5), (11, 4),
            (11.5, 3.5), (12, 3), (12.5, 2.5), (13, 2), (13.5, 1.5), (14, 1), (31.5, 1)]
    draw.line([(left + point[0] * cell, top + (14 - point[1]) * cell) for point in path], fill='#79d4cb', width=4)
    draw.text((left + 4 * cell, top + 4 * cell), '主层脚位 Y=7', font=font(23), fill='#eee2cf')
    draw.text((left + 18 * cell, top + 8 * cell), '下层脚位 Y=1 / 净空 Y=1…5', font=font(24), fill='#b4ead9')
    draw.text((42, 920), '金色阶形：实际下行台阶；青线：逐级通行轨迹；灰紫：实体支撑；深灰：真实空气；暖色：吊灯。', font=font(24), fill='#d8c08d')
    draw.text((42, 980), '楼梯区域 Z=8…13；下层走廊 Z=14…31；南接口 Z=32 仅中心3格×3格开放，完整地板支撑。', font=font(24), fill='#bbb0b6')
    draw.text((42, 1040), '正反方向和每一列均经原生 0.6×1.8 碰撞体、0.25 格采样验证；不是只检测端点连通。', font=font(24), fill='#bbb0b6')
    return image


def lava_detail(room):
    image = Image.new('RGB', (1500, 1000), '#17151c')
    draw = ImageDraw.Draw(image)
    draw.text((40, 24), 'D  完全密封真实熔岩渠 / 两侧对称', font=font(34), fill='#f2d7a0')
    cell, left, top = 135, 270, 210
    colors = {'lava': '#ee6d24', 'orange_stained_glass': '#ca8d55', 'polished_blackstone': '#5b5363',
              'polished_blackstone_bricks': '#655b6d', 'gilded_blackstone': '#9e8243', 'air': '#32353e'}
    for pos_x in range(0, 5):
        for pos_y in range(4, 8):
            block = room.get((pos_x, pos_y, 8))
            name = block.name.removeprefix('minecraft:')
            pos_left, pos_top = left + pos_x * cell, top + (7 - pos_y) * cell
            draw.rectangle((pos_left, pos_top, pos_left + cell - 3, pos_top + cell - 3), fill=colors.get(name, '#655b6d'))
            label = {'lava': '熔岩源', 'orange_stained_glass': '玻璃顶', 'air': '空气', 'gilded_blackstone': '矮墙'}.get(name, '黑石')
            draw.text((pos_left + 12, pos_top + 52), label, font=font(25), fill='#f8e4cd')
    draw.text((1010, 260), 'Y=7  矮墙', font=font(26), fill='#d6c29a')
    draw.text((1010, 395), 'Y=6  全方块玻璃', font=font(26), fill='#d6c29a')
    draw.text((1010, 530), 'Y=5  真实熔岩源', font=font(26), fill='#ee9e54')
    draw.text((1010, 665), 'Y=4  完整黑石底', font=font(26), fill='#d6c29a')
    draw.text((40, 800), '横剖位置 Z=8；每条渠 X=2 / X=26，Z=5…12，前后端 Z=4、13 由完整黑石封口。', font=font(24), fill='#bbb0b6')
    draw.text((40, 860), '底、两侧、前后端、顶部六面全封闭。16 源方块 × 四向；主动邻居更新/熔岩 tick 后稳定 200 tick。', font=font(23), fill='#bbb0b6')
    draw.text((40, 920), '直接读取最终 NBT 的材质标识示意；不移除玻璃顶来伪装成露天熔岩，不使用岩浆块替代熔岩。', font=font(23), fill='#bbb0b6')
    return image


def main():
    engine.ASSETS = zipfile.ZipFile(CLIENT)
    engine.definitions = definitions
    room = Structure.read(ROOT / 'out/room.nbt')
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    outputs = []
    for name, camera, label, exterior, section in [('northeast', (1, -1.25), '东北剖切 / 大台阶 + 下行洞口', False, False),
                                                   ('northwest', (-1, -1.25), '西北剖切 / 金饰平台', False, False),
                                                   ('southeast', (1, 1.25), '东南剖切 / 上下接口', False, False),
                                                   ('southwest', (-1, 1.25), '西南剖切 / 柱廊与平台背部', False, False),
                                                   ('exterior', (1, -1.25), '完整外壳', True, False),
                                                   ('lower-section', (1.7, -0.8), '下行梯段纵向剖面', False, True)]:
        path = ROOT / f'preview/{name}.png'
        render(room, camera, label, exterior=exterior, section=section).save(path)
        outputs.append(path)
    Image.open(ROOT / 'preview/northeast.png').save(ROOT / 'out/preview.png')
    logic_plan(room, blueprint).save(ROOT / 'preview/logic-plan.png')
    profile(room).save(ROOT / 'preview/lower-profile.png')
    lava_detail(room).save(ROOT / 'preview/lava-enclosure.png')
    sheet = Image.new('RGB', (1800, 1450), '#17151c')
    for index, path in enumerate(outputs[:4]):
        thumbnail = Image.open(path).resize((900, 725), Image.Resampling.LANCZOS)
        sheet.paste(thumbnail, ((index % 2) * 900, (index // 2) * 725))
    sheet.save(ROOT / 'preview/four-views.png')
    native = ROOT / 'qa/native-placed-none.nbt'
    if native.is_file():
        render(Structure.read(native), (1, -1.25), '原生回采 / 东北剖切', native=True).save(ROOT / 'preview/native-world-capture.png')
    paths = [ROOT / 'out/preview.png', *sorted((ROOT / 'preview').glob('*.png'))]
    manifest = {'source_nbt_sha256': hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest(),
                'renderer': 'Actual NBT + Minecraft 1.20.1 blockstate/model/texture software projection; not AI or client screenshot',
                'client_assets': str(CLIENT), 'outputs': [{'file': str(path.relative_to(ROOT)), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
                                                         'size': list(Image.open(path).size)} for path in paths]}
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'rendered': len(paths), 'native_capture_present': native.is_file()}))


if __name__ == '__main__':
    main()
