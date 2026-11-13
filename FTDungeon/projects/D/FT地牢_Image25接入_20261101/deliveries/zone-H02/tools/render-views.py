import hashlib
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw
import render_engine as models
from nbtio import Structure

ROOT = Path(__file__).resolve().parents[1]
VIEWS = [('north', '北向', (-0.60, -1)), ('south', '南向', (0.60, 1)),
         ('west', '西向', (-1, 0.44)), ('east', '东向', (1, -0.44))]
INVISIBLE = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}


def opaque(block):
    return block is not None and block.name not in INVISIBLE and not any(
        fragment in block.name for fragment in ('glass', 'chain', 'lantern', 'trapdoor', 'carpet', 'candle', '_stairs', '_slab'))


def visible_blocks(room, camera, exterior):
    result = {}
    for (horizontal, vertical, depth), block in room.blocks.items():
        if block.name in INVISIBLE:
            continue
        if not exterior:
            near_side = horizontal <= 1 if camera[0] < 0 else horizontal >= 27
            near_end = depth <= 1 if camera[1] < 0 else depth >= 31
            roof = vertical >= 13
            retained_anchor = vertical == 13 and (horizontal in [3, 25] and depth in [11, 19, 27] or
                              depth == 2 and horizontal in [10, 18] or
                              horizontal in [12, 16] and depth in [15, 19])
            if roof and not retained_anchor or vertical >= 1 and (near_side or near_end):
                continue
        result[(horizontal, vertical, depth)] = block
    return result


def render(room, camera, direction, exterior, size=(1600, 1250)):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 1.05
    vertical_factor = 1 / math.hypot(1, camera_y)
    visible = visible_blocks(room, camera, exterior)

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_factor * (camera_y * horizontal - point[1])

    faces = []
    for position, block in visible.items():
        for vertices, normal, cull, texture, uv, rotation in models.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in vertices]
            order = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / len(world)
            brightness = 1.12 if normal[1] > 0.5 else 0.96 if normal[0] < 0 else 1.04
            faces.append((order, [project(point) for point in world], texture, uv, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    bounds = [min(point[0] for point in projected), min(point[1] for point in projected),
              max(point[0] for point in projected), max(point[1] for point in projected)]
    scale = min((size[0] - 110) / (bounds[2] - bounds[0]), (size[1] - 200) / (bounds[3] - bounds[1]))
    offset_x = size[0] / 2 - (bounds[0] + bounds[2]) * scale / 2
    offset_y = 128 + (size[1] - 205) / 2 - (bounds[1] + bounds[3]) * scale / 2
    image = Image.new('RGBA', size, '#151b23')
    for order, points, texture, uv, rotation, brightness in sorted(faces, key=lambda item: item[0]):
        screen = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        models.warp(image, models.texture(texture, uv, rotation, brightness), screen)
    draw = ImageDraw.Draw(image)
    draw.text((32, 20), 'H02 废墟宴会厅 · ' + direction + ('完整外观' if exterior else '内部剖视'), font=models.font(32), fill='#ead4ad')
    draw.text((32, 70), '29 × 16 × 33 / ' + ('完整顶盖、四壁与北向入口' if exterior else '剖切仅用于展示；保留梁与吊灯锚点'), font=models.font(22), fill='#c98e76')
    draw.text((32, size[1] - 42), '实际 NBT + Java 1.20.1 原版模型纹理软件渲染；非游戏截图，不模拟动态光照。', font=models.font(18), fill='#a7afb6')
    return image.convert('RGB')


def floorplan(room):
    image = Image.new('RGB', (1150, 1290), '#151b23')
    draw = ImageDraw.Draw(image)
    draw.text((30, 18), 'H02 地面布局与三格主路', font=models.font(32), fill='#ead4ad')
    route = {tuple(position) for position in json.loads((ROOT / 'qa/route-cells.json').read_text(encoding='utf-8'))}
    for depth in range(33):
        for horizontal in range(29):
            blocked = any(room.get((horizontal, vertical, depth)).name != 'minecraft:air' for vertical in [1, 2])
            color = '#586165' if blocked else '#afa691'
            if (horizontal, 1, depth) in route:
                color = '#68b49b'
            if room.get((horizontal, 2, depth)).name == 'minecraft:dark_oak_planks':
                color = '#7e4231'
            rectangle = [66 + horizontal * 35, 100 + depth * 33, 99 + horizontal * 35, 131 + depth * 33]
            draw.rectangle(rectangle, fill=color)
    for label, horizontal, depth in [('北入口', 14, 0), ('安全点', 14, 5)]:
        draw.ellipse((65 + horizontal * 35, 100 + depth * 33, 100 + horizontal * 35, 132 + depth * 33), fill='#f5d370')
        draw.text((102 + horizontal * 35, 100 + depth * 33), label, font=models.font(19), fill='#f5d370')
    draw.text((30, 1210), '绿色：216 个主路脚位；棕色：宴桌；灰色：家具/柱脚/墙。', font=models.font(21), fill='#d2d1ca')
    return image


def main():
    source = ROOT / 'room.nbt'
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    room = Structure.read(source)
    models.ASSETS = zipfile.ZipFile(models.CLIENT)
    records = []
    for exterior in [False, True]:
        sheet = Image.new('RGB', (3200, 2500), '#151b23')
        for index, (name, label, camera) in enumerate(VIEWS):
            image = render(room, camera, label, exterior)
            filename = f'qa/views/{name}-' + ('exterior.png' if exterior else 'interior.png')
            image.save(ROOT / filename)
            records.append({'file': filename, 'direction': name, 'type': 'exterior' if exterior else 'interior_cutaway',
                            'source_sha256': digest, 'image_sha256': hashlib.sha256((ROOT / filename).read_bytes()).hexdigest(),
                            'visible_blocks': len(visible_blocks(room, camera, exterior))})
            sheet.paste(image, (index % 2 * 1600, index // 2 * 1250))
            if name == 'north' and not exterior:
                image.save(ROOT / 'preview.png')
        sheet.save(ROOT / ('qa/four-exterior-views.png' if exterior else 'qa/four-interior-views.png'))
    floorplan(room).save(ROOT / 'qa/floor-plan.png')
    assert hashlib.sha256(source.read_bytes()).hexdigest() == digest
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps({'status': 'PASS', 'source_sha256': digest,
        'renderer': 'Java 1.20.1 vanilla block model/texture software orthographic renderer',
        'client_assets': str(models.CLIENT), 'client_sha256': hashlib.sha256(models.CLIENT.read_bytes()).hexdigest(),
        'actual_nbt_modified_for_cutaway': False, 'dynamic_lighting': False, 'views': records}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'render': 'PASS', 'views': len(records), 'sha256': digest}))


if __name__ == '__main__':
    main()
