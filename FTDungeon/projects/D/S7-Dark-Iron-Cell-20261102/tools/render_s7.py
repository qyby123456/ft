import hashlib
import json
import math
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'tools'))
import build_s7 as builder
sys.path.insert(0, 'D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
import review_four_views as viewer
from PIL import Image, ImageDraw
import numpy as np


def render(structure, metadata, camera, title, exterior, size):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.8
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

    visible = viewer.filtered(structure, metadata, camera, exterior)
    faces = []
    for position, block in visible.items():
        for points, normal, cull, identifier, uv, rotation in viewer.geometry(block.name.removeprefix('minecraft:'), block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and viewer.opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            depths = [camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world]
            brightness = 1.1 if normal[1] > 0.5 else 0.9 if normal[0] < 0 else 1.0
            faces.append(([project(point) for point in world], depths, identifier, uv, rotation, brightness))
    projected = [point for face in faces for point in face[0]]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((size[0] - 100) / (maximum_x - minimum_x), (size[1] - 190) / (maximum_y - minimum_y))
    offset_x = size[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 115 + (size[1] - 190) / 2 - (minimum_y + maximum_y) * scale / 2
    colors = np.empty((size[1], size[0], 3), dtype=np.float32)
    colors[:] = (16, 26, 32)
    depth_buffer = np.full((size[1], size[0]), -np.inf, dtype=np.float32)
    translucent = []

    def raster(face):
        points, depths, identifier, uv, rotation, brightness = face
        points = np.array([(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points])
        left = max(0, math.floor(min(points[:, 0])))
        right = min(size[0], math.ceil(max(points[:, 0])) + 1)
        top = max(0, math.floor(min(points[:, 1])))
        bottom = min(size[1], math.ceil(max(points[:, 1])) + 1)
        transform = np.column_stack((points[1] - points[0], points[3] - points[0]))
        if abs(np.linalg.det(transform)) < 0.00001 or left >= right or top >= bottom:
            return None
        inverse = np.linalg.inv(transform)
        pixel_y, pixel_x = np.mgrid[top:bottom, left:right]
        delta_x = pixel_x + 0.5 - points[0, 0]
        delta_y = pixel_y + 0.5 - points[0, 1]
        horizontal = inverse[0, 0] * delta_x + inverse[0, 1] * delta_y
        vertical = inverse[1, 0] * delta_x + inverse[1, 1] * delta_y
        inside = (horizontal >= 0) & (horizontal < 1) & (vertical >= 0) & (vertical < 1)
        texture = np.array(viewer.renderer.texture(identifier, uv, rotation, brightness))
        sample_x = np.clip((horizontal * texture.shape[1]).astype(int), 0, texture.shape[1] - 1)
        sample_y = np.clip((vertical * texture.shape[0]).astype(int), 0, texture.shape[0] - 1)
        sampled = texture[sample_y, sample_x]
        depths = depths[0] + horizontal * (depths[1] - depths[0]) + vertical * (depths[3] - depths[0])
        return (slice(top, bottom), slice(left, right)), inside, sampled, depths

    for face in faces:
        fragment = raster(face)
        if fragment is None:
            continue
        region, inside, sampled, depths = fragment
        visible_pixels = inside & (sampled[:, :, 3] >= 250) & (depths >= depth_buffer[region] - 0.0001)
        colors[region][visible_pixels] = sampled[:, :, :3][visible_pixels]
        depth_buffer[region][visible_pixels] = depths[visible_pixels]
        if np.any(inside & (sampled[:, :, 3] > 0) & (sampled[:, :, 3] < 250)):
            translucent.append(face)
    for face in sorted(translucent, key=lambda entry: sum(entry[1]) / 4):
        region, inside, sampled, depths = raster(face)
        visible_pixels = inside & (sampled[:, :, 3] > 0) & (sampled[:, :, 3] < 250) & (depths >= depth_buffer[region] - 0.0001)
        alpha = sampled[:, :, 3:4] / 255
        blended = sampled[:, :, :3] * alpha + colors[region] * (1 - alpha)
        colors[region][visible_pixels] = blended[visible_pixels]
    image = Image.fromarray(np.clip(colors, 0, 255).astype(np.uint8))
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'S7 暗金囚笼 · ' + title, font=viewer.font(29), fill='#ede8d6')
    draw.text((35, 69), '最终 NBT / 原版 1.20.1 材质 / ' + ('完整外壳，北接口无门扇' if exterior else '仅剖去屋顶与近侧外墙；锁链上端在实际屋顶内固定'), font=viewer.font(20), fill='#8ebbb9')
    draw.text((35, size[1] - 42), '真实方块网格软件投影，非游戏截图；头骨与旗帜为简化方块实体外观。', font=viewer.font(18), fill='#99a4aa')
    return image


def main():
    source = builder.OUTPUT / 'room.nbt'
    structure = builder.Structure.read(source)
    metadata = json.loads((builder.OUTPUT / 'room.json').read_text(encoding='utf-8'))
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    if digest != metadata['sha256']:
        raise ValueError('Preview source must match final room metadata')
    views = ROOT / 'preview'
    views.mkdir(parents=True, exist_ok=True)
    render(structure, metadata, (-1, -1), '北西剖视 / 封闭熔岩缝与锁链刑台', False, size=(1600, 1200)).save(builder.OUTPUT / 'preview.png')
    render(structure, metadata, (-1, -1), '完整外壳 / 三格宽北接口', True, size=(1400, 1080)).save(views / 'exterior.png')
    sheet = Image.new('RGB', (2400, 1800), '#101a20')
    for index, (direction, title, camera) in enumerate(viewer.VIEWS):
        rendered = render(structure, metadata, camera, title, False, size=(1200, 900))
        rendered.save(views / ('interior-' + direction + '.png'))
        sheet.paste(rendered, ((index % 2) * 1200, (index // 2) * 900))
    sheet.save(views / 'four-views.png')
    static = json.loads((ROOT / 'qa/static-qa.json').read_text(encoding='utf-8'))
    reachable = {tuple(position) for position in static['reachable_feet_positions']}
    image = Image.new('RGB', (1100, 980), '#101a20')
    draw = ImageDraw.Draw(image)
    draw.text((44, 30), 'S7 暗金囚笼 / 实际 NBT 通行与封液平面', font=viewer.font(29), fill='#e8e0cd')
    draw.text((44, 76), '北在上；坐标标注为脚部 Y=3；格位须同时满足头部净空与完整地板。', font=viewer.font(20), fill='#a7b6c2')
    start_x, start_y, cell = 65, 143, 76
    for block_z in range(9):
        for block_x in range(9):
            position = (block_x, 3, block_z)
            block = structure.get(position).name.removeprefix('minecraft:')
            color = '#222c34'
            label = ''
            if position in reachable:
                color = '#276759'
            if structure.get((block_x, 2, block_z)).name == 'minecraft:orange_stained_glass':
                color = '#a34e25' if position in reachable else '#574026'
                label = '封'
            if block == 'iron_bars':
                color, label = '#7f8190', '笼'
            elif block in {'redstone_block', 'gilded_blackstone'} and block_x in range(3, 6) and block_z in (3, 4):
                color, label = '#8e3342', '刑'
            elif block != 'air':
                color = '#37404a'
            if position == builder.ENTRANCE:
                color, label = '#c4a24a', '入'
            elif position == builder.INTERIOR:
                color, label = '#407bc4', '安'
            elif position in builder.STATIONS:
                color, label = '#368d76', '途'
            left, top = start_x + block_x * cell, start_y + block_z * cell
            draw.rectangle((left, top, left + cell - 3, top + cell - 3), fill=color)
            if label:
                draw.text((left + 24, top + 18), label, font=viewer.font(26), fill='#f6efe0')
    for index in range(9):
        draw.text((start_x + index * cell + 29, 114), str(index), font=viewer.font(18), fill='#9eacb5')
        draw.text((32, start_y + index * cell + 23), str(index), font=viewer.font(18), fill='#9eacb5')
    legend = [
        '占地 9 × 9 / 净内室 7 × 7', '模板高度 9 / 净高 5', '完整屋顶 Y=8', '脚部平面 Y=3', '',
        '入：北接口 [4,3,0]', '安：安全点 [4,3,2]', '途：刑台绕行检查点', '封：熔岩上方完整玻璃', '笼：南端铁栅陈设',
        '', '地层从下到上：', 'Y=0 完整石底', 'Y=1 封闭熔岩槽', 'Y=2 石材 / 橙色玻璃', 'Y=3 玩家脚部',
    ]
    for index, line in enumerate(legend):
        draw.text((785, 153 + index * 37), line, font=viewer.font(19), fill='#c7cbd0')
    draw.text((57, 866), '绿色连通区可往返：入口 → 安全点 → 刑台两侧 → 囚笼前。无必经跳跃或裸露熔岩。', font=viewer.font(22), fill='#93cbb9')
    draw.text((57, 913), '方块数据投影与保守静态通路图，非游戏截图。四方向原生碰撞验收见 qa/native-*.json。', font=viewer.font(20), fill='#a4aab3')
    image.save(views / 'floorplan.png')
    builder.write_json(ROOT / 'qa/render.json', {
        'id': 'S7', 'source': str(source), 'sha256': digest, 'preview': str(builder.OUTPUT / 'preview.png'),
        'preview_from_final_nbt': True, 'cutaway_only_in_preview': True, 'roof_retained_in_nbt': True,
        'texture_source': str(viewer.renderer.CLIENT), 'renderer': 'Read-only vanilla block models/textures; local per-pixel depth buffer and alpha composition prevent thin-chain occlusion errors',
        'views': ['hero north-west cutaway', 'closed exterior', 'north', 'south', 'west', 'east', 'floorplan'],
        'limits': 'Software-rendered real vanilla textures/geometry. No game lighting or particles. Skulls/banner are simplified block-entity visualizations. Not an in-game screenshot.',
    })
    print('S7 final NBT preview, exterior, four directions, and floorplan rendered')


if __name__ == '__main__':
    main()
