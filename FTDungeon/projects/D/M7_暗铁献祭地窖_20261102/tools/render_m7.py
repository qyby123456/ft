import copy
import hashlib
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

from build_m7 import DELIVERY, ROOT
from structure_io import load_nbt, write_json
import voxel_engine as ENGINE


ENGINE_PATH = ROOT / 'tools/voxel_engine.py'
ENGINE.PARTIAL.update({'air', 'black_candle', 'lantern', 'orange_stained_glass', 'red_stained_glass', 'polished_blackstone_brick_wall', 'lava'})
BACKGROUND = '#141116'
TEXT = '#eedec6'
GOLD = '#e4af5e'
MUTED = '#ad9b99'


def rotate_blocks(blocks, count):
    result = {}
    directions = ('north', 'east', 'south', 'west')
    for position, original in blocks.items():
        block_x, block_y, block_z = position
        state = copy.deepcopy(original)
        properties = state.get('Properties', {})
        if 'facing' in properties:
            properties['facing'] = directions[(directions.index(properties['facing']) + count) % 4]
        if count % 2 and properties.get('axis') in ('x', 'z'):
            properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
        directional = {name: properties[name] for name in directions if name in properties}
        for name, value in directional.items():
            properties[directions[(directions.index(name) + count) % 4]] = value
        if 'rotation' in properties:
            properties['rotation'] = str((int(properties['rotation']) + count * 4) % 16)
        for _ in range(count):
            block_x, block_z = 18 - block_z, block_x
        result[(block_x, block_y, block_z)] = state
    return result


def render(blocks, rotation=0, exterior=False, size=(1800, 1450)):
    visible = rotate_blocks(blocks, rotation)
    if not exterior:
        visible = {position: state for position, state in visible.items() if position[1] < 13 and not (position[1] >= 3 and (position[0] == 0 or position[2] == 0))}
    projected = [(block_x - block_z, (block_x + block_z) * -.53 - block_y * .96) for block_x, block_y, block_z in visible]
    minimum_x, maximum_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    minimum_y, maximum_y = min(point[1] for point in projected) - 1.2, max(point[1] for point in projected) + 1
    scale = min((size[0] - 100) / (maximum_x - minimum_x), (size[1] - 210) / (maximum_y - minimum_y))
    offset_x = size[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = (size[1] + 45) / 2 - (minimum_y + maximum_y) * scale / 2

    def project(point):
        block_x, block_y, block_z = point
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale * .53 - block_y * scale * .96

    canvas = Image.new('RGBA', size, BACKGROUND)
    faces = []
    for position, state in visible.items():
        faces.extend(ENGINE.block_faces(position, state, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 1.1 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, texture_rotation, brightness in faces:
        ENGINE.parallelogram(canvas, ENGINE.texture(identifier, uv, texture_rotation, brightness * .92), [project(point) for point in points])
    glow = Image.new('RGBA', size)
    draw = ImageDraw.Draw(glow)
    for position, state in visible.items():
        if state['Name'] not in ('minecraft:lantern', 'minecraft:black_candle', 'minecraft:campfire') or exterior:
            continue
        center_x, center_y = project((position[0] + .5, position[1] + .45, position[2] + .5))
        radius = scale * (.55 if state['Name'] == 'minecraft:campfire' else .29)
        draw.ellipse((center_x - radius, center_y - radius, center_x + radius, center_y + radius), fill=(255, 154, 46, 58))
    canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(scale * .20)))
    draw = ImageDraw.Draw(canvas)
    draw.text((48, 28), 'M7  /  暗铁献祭地窖', font=ENGINE.font(38), fill=TEXT)
    draw.text((50, 84), '19 × 19 · 高 14 格    /    原创暗金哥特结构    /    ' + ('完整屋顶' if exterior else f'{rotation * 90}° 轴测剖面'), font=ENGINE.font(24), fill=GOLD)
    draw.text((48, size[1] - 66), '来自最终 room.nbt 的软件纹理预览，非游戏截图。' + ('' if exterior else '仅移除屋顶与近侧最外墙。'), font=ENGINE.font(22), fill=MUTED)
    draw.text((48, size[1] - 34), '红玻璃祭池 · 地下密封熔岩 · 三道拱肋 · 四盏悬灯 · 三格北接口', font=ENGINE.font(20), fill=MUTED)
    return canvas.convert('RGB')


def floorplan(blocks):
    canvas = Image.new('RGB', (1220, 1060), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((45, 30), 'M7 / 安全通路与隐藏熔岩', font=ENGINE.font(36), fill=TEXT)
    scale, left, top = 42, 60, 165
    colors = {'minecraft:air': '#302b32', 'minecraft:polished_blackstone_bricks': '#555057', 'minecraft:cracked_polished_blackstone_bricks': '#4c454d', 'minecraft:orange_stained_glass': '#e28a3c', 'minecraft:gilded_blackstone': '#a78042', 'minecraft:chiseled_polished_blackstone': '#706775', 'minecraft:polished_basalt': '#434650'}
    for block_x in range(19):
        for block_z in range(19):
            floor = blocks.get((block_x, 2, block_z), {'Name': 'minecraft:air'})['Name']
            above = blocks.get((block_x, 3, block_z), {'Name': 'minecraft:air'})['Name']
            color = colors.get(floor, '#635c64') if above == 'minecraft:air' else colors.get(above, '#615464')
            box = (left + block_x * scale, top + block_z * scale, left + (block_x + 1) * scale - 1, top + (block_z + 1) * scale - 1)
            draw.rectangle(box, fill=color)
    draw.rectangle((left + 6 * scale, top + 7 * scale, left + 13 * scale - 1, top + 14 * scale - 1), fill='#50454c', outline='#8d765e', width=2)
    draw.rectangle((left + 7 * scale, top + 8 * scale, left + 12 * scale - 1, top + 13 * scale - 1), fill='#302632', outline='#a47b46', width=2)
    draw.rectangle((left + 8 * scale, top + 9 * scale, left + 11 * scale - 1, top + 12 * scale - 1), fill='#953e42', outline='#c5776b', width=2)
    draw.text((left + 9.5 * scale, top + 10.5 * scale), '祭池', font=ENGINE.font(23), fill=TEXT, anchor='mm')
    route = [(9, 0), (9, 4), (4, 4), (4, 14), (9, 14), (9, 15), (12, 15), (12, 14), (14, 14), (14, 4), (9, 4)]
    draw.line([(left + (block_x + .5) * scale, top + (block_z + .5) * scale) for block_x, block_z in route], fill='#8accbd', width=5, joint='curve')
    draw.text((left + 9.5 * scale, top - 34), 'N / 北入口', font=ENGINE.font(22), fill=GOLD, anchor='mm')
    for index, text in enumerate(('入口脚位 [9,3,0]', '安全内点 [9,3,4]', '绿线：两侧干燥环路', '橙色：覆玻璃熔岩', '中心：红玻璃祭池', '北口：三格宽×三格高', '最终结构包含完整屋顶')):
        draw.text((900, 220 + index * 70), text, font=ENGINE.font(21), fill=TEXT if index < 2 else MUTED)
    draw.text((60, 1000), '平面示意；通路由原生碰撞检测复核。不含奖励、刷怪实体或自动施工。', font=ENGINE.font(23), fill=MUTED)
    return canvas


def main():
    ENGINE.ASSETS = zipfile.ZipFile(ENGINE.CLIENT)
    template = load_nbt(DELIVERY / 'room.nbt')
    blocks = {tuple(record['pos']): template['palette'][record['state']] for record in template['blocks'] if template['palette'][record['state']]['Name'] != 'minecraft:air'}
    preview = ROOT / 'preview'
    preview.mkdir(exist_ok=True)
    rendered = []
    for rotation in range(4):
        image = render(blocks, rotation)
        image.save(preview / f'cutaway-{rotation * 90:03}.png')
        if rotation == 0:
            image.save(DELIVERY / 'preview.png')
        rendered.append(image)
    render(blocks, exterior=True).save(preview / 'exterior.png')
    floorplan(blocks).save(preview / 'floorplan.png')
    sheet = Image.new('RGB', (2200, 1810), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    draw.text((40, 25), 'M7  /  四向结构复核 · 最终 NBT 软件剖切', font=ENGINE.font(36), fill=TEXT)
    for index, image in enumerate(rendered):
        image.thumbnail((1080, 850), Image.Resampling.LANCZOS)
        sheet.paste(image, (20 + (index % 2) * 1100, 85 + (index // 2) * 860))
    sheet.save(preview / 'four-views.png')
    write_json(ROOT / 'qa/render.json', {'status': 'PASS', 'source_structure_sha256': hashlib.sha256((DELIVERY / 'room.nbt').read_bytes()).hexdigest(), 'preview_sha256': hashlib.sha256((DELIVERY / 'preview.png').read_bytes()).hexdigest(), 'renderer': ENGINE_PATH.as_posix(), 'renderer_sha256': hashlib.sha256(ENGINE_PATH.read_bytes()).hexdigest(), 'textures_jar': ENGINE.CLIENT.as_posix(), 'used_models': sorted(ENGINE.USED_MODELS), 'used_textures': sorted(ENGINE.USED_TEXTURES), 'outputs': ['delivery/M7/preview.png', 'preview/four-views.png', 'preview/exterior.png', 'preview/floorplan.png'], 'scope': 'Software preview from exact NBT, vanilla models/textures; skull/banner simplified block-entity geometry and banner patterns omitted. Not Minecraft gameplay screenshots. Cutaway only removes roof and nearest outer wall, never inner furnishings.'})
    print('M7_RENDER_PASS views=4 exterior=1 floorplan=1')


if __name__ == '__main__':
    main()
