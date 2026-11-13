import functools
import hashlib
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from nbtio import Structure
import asset_engine as engine


ROOT = Path(__file__).resolve().parents[1]
engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
INVISIBLE = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
VIEWS = [('north', '北', (-0.48, -1)), ('south', '南', (0.48, 1)), ('west', '西', (-1, 0.48)), ('east', '东', (1, -0.48))]


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


@functools.lru_cache(maxsize=None)
def opaque(block):
    if block is None or block.name in INVISIBLE:
        return False
    geometry = engine.geometry(block.name.removeprefix('minecraft:'), block.properties)
    return len(geometry) == 6 and all(all(abs(value - round(value)) < 0.001 for value in point) for face in geometry for point in face[0])


def selected_blocks(room, camera, exterior):
    visible = {}
    for position, block in room.blocks.items():
        if block.name in INVISIBLE:
            continue
        across, elevation, depth = position
        if not exterior:
            near_x = across <= 1 if camera[0] < 0 else across >= room.size[0] - 2
            near_z = depth <= 1 if camera[1] < 0 else depth >= room.size[2] - 2
            if elevation >= 9 or (elevation > 0 and (near_x or near_z)):
                continue
        visible[position] = block
    return visible


def render(room, camera, label, exterior):
    dimensions = (1600, 1200)
    camera_length = math.hypot(*camera)
    camera_x, camera_z = (coordinate / camera_length for coordinate in camera)
    camera_y = 0.90
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale
    visible = selected_blocks(room, camera, exterior)

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

    faces = []
    for position, block in visible.items():
        for points, normal, cull, identifier, uv, rotation in engine.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            depth = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / 4
            brightness = 1.13 if normal[1] > 0.5 else 0.88 if normal[0] < 0 else 0.99
            if block.name == 'minecraft:lantern':
                brightness = 1.25
            faces.append((depth, [project(point) for point in world], identifier, uv, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((dimensions[0] - 100) / (maximum_x - minimum_x), (dimensions[1] - 185) / (maximum_y - minimum_y))
    offset_x = dimensions[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 105 + (dimensions[1] - 185) / 2 - (minimum_y + maximum_y) * scale / 2
    image = Image.new('RGBA', dimensions, '#151b22')
    for depth, points, identifier, uv, rotation, brightness in sorted(faces, key=lambda entry: entry[0]):
        destination = [(offset_x + point[0] * scale, offset_y + point[1] * scale) for point in points]
        engine.warp(image, engine.texture(identifier, uv, rotation, brightness), destination)
    draw = ImageDraw.Draw(image)
    draw.text((32, 20), 'zone-M03  塌方矿道  /  ' + label + ('侧完整外壳' if exterior else '侧内部剖视'), fill='#f0d9ad', font=font(29))
    draw.text((32, 63), '23×12×25  |  最终 room.nbt 原版材质软件渲染  |  非实机截图', fill='#aeb9c4', font=font(19))
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    draw.text((32, dimensions[1] - 55), ('两层完整屋顶 · 北南五格入口' if exterior else '屋顶及近侧外墙仅在渲染中隐藏 · 原始结构完整') + '  |  SHA-256 ' + digest[:16], fill='#aeb9c4', font=font(17))
    return image.convert('RGB')


def main():
    room = Structure.read(ROOT / 'room.nbt')
    records = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2400, 1800), '#151b22')
        for index, (direction, label, camera) in enumerate(VIEWS):
            image = render(room, camera, label, exterior)
            target = ROOT / 'views' / (mode + '-' + direction + '.png')
            image.save(target)
            sheet.paste(image.resize((1200, 900), Image.Resampling.LANCZOS), ((index % 2) * 1200, (index // 2) * 900))
            records.append({'path': target.relative_to(ROOT).as_posix(), 'direction': direction, 'kind': mode})
            if not exterior and direction == 'north':
                image.save(ROOT / 'preview.png')
        target = ROOT / 'views' / (mode + '-four-directions.png')
        sheet.save(target)
        records.append({'path': target.relative_to(ROOT).as_posix(), 'direction': 'sheet', 'kind': mode})
    overview = Image.new('RGB', (2400, 1800), '#151b22')
    for index, direction in enumerate(('north', 'south', 'west', 'east')):
        interior = Image.open(ROOT / 'views' / ('interior-' + direction + '.png')).resize((900, 675), Image.Resampling.LANCZOS)
        exterior = Image.open(ROOT / 'views' / ('exterior-' + direction + '.png')).resize((300, 225), Image.Resampling.LANCZOS)
        panel = Image.new('RGB', (1200, 900), '#151b22')
        panel.paste(interior, (0, 110))
        panel.paste(exterior, (900, 330))
        overview.paste(panel, ((index % 2) * 1200, (index // 2) * 900))
    overview.save(ROOT / 'views/four-direction-review.png')
    records.append({'path': 'views/four-direction-review.png', 'kind': 'combined sheet', 'direction': 'all'})
    records.append({'path': 'preview.png', 'kind': 'interior', 'direction': 'north'})
    for record in records:
        target = ROOT / record['path']
        record['sha256'] = hashlib.sha256(target.read_bytes()).hexdigest()
        with Image.open(target) as image:
            record['size'] = list(image.size)
            image.verify()
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps({'status': 'RENDERED', 'structure_sha256': hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest(), 'source': 'final room.nbt', 'renderer': 'Minecraft 1.20.1 client.jar block models/textures, affine software projection', 'game_screenshot': False, 'final_nbt_modified_for_cutaway': False, 'lighting': 'readable fixed face shading; not a game light simulation', 'records': records}, ensure_ascii=False, indent=2), encoding='utf-8')
    print('Rendered 4 interior + 4 exterior views, preview and contact sheets from final NBT')


if __name__ == '__main__':
    main()
