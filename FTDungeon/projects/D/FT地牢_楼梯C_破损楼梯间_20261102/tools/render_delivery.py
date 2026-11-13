import copy
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

import voxel_renderer as renderer
from build_room import ROOT, fingerprint, save_json
from nbtio import AIR, Structure


def plan_image(room, blueprint):
    scale = 22
    image = Image.new('RGB', (1860, 810), '#131e24')
    draw = ImageDraw.Draw(image)
    draw.text((28, 20), 'C · 破损楼梯间  /  三层安全动线与接口', font=renderer.font(30), fill='#e5e1d2')
    levels = [(1, '下层 · 脚位 Y=1', '#8fc8dd'), (6, '主层 · 脚位 Y=6', '#e7ba73'), (12, '上层 · 脚位 Y=12', '#ace1b6')]
    for index, (feet_y, title, route_color) in enumerate(levels):
        origin_x = 36 + index * 610
        origin_y = 138
        draw.text((origin_x, 85), title, font=renderer.font(24), fill=route_color)
        for pos_x in range(23):
            for pos_z in range(25):
                floor = room.get((pos_x, feet_y - 1, pos_z))
                obstacle = room.get((pos_x, feet_y, pos_z))
                color = '#3d4b50' if floor != AIR else '#182126'
                if obstacle != AIR:
                    color = '#857a65' if obstacle.name.endswith('_stairs') else '#5b6667'
                if obstacle.name.endswith('_fence'):
                    color = '#8a6847'
                position = (origin_x + pos_x * scale, origin_y + pos_z * scale)
                draw.rectangle((*position, position[0] + scale - 1, position[1] + scale - 1), fill=color)
        for pos_x, node_y, pos_z in blueprint['required_walkable']:
            if node_y == feet_y:
                center_x, center_y = origin_x + pos_x * scale, origin_y + pos_z * scale
                draw.rectangle((center_x + 2, center_y + 2, center_x + scale - 3, center_y + scale - 3), fill=route_color)
        for portal in blueprint['portals']:
            if portal['feet'][1] == feet_y:
                pos_x, _, pos_z = portal['feet']
                center_x, center_y = origin_x + pos_x * scale, origin_y + pos_z * scale
                draw.ellipse((center_x + 3, center_y + 3, center_x + 18, center_y + 18), fill='#f2edb5')
        draw.text((origin_x, 710), 'X →   Z ↓ ；彩色为三格净宽指定路线', font=renderer.font(18), fill='#abbec7')
    draw.text((36, 760), '各层图只截取该脚位平面；上下梯段另见剖视。安全点坐标均为玩家脚部，并非地板。', font=renderer.font(21), fill='#bdcbd2')
    return image


def run():
    room = Structure.read(ROOT / 'out/room.nbt')
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    output = ROOT / 'preview'
    output.mkdir(exist_ok=True)
    with zipfile.ZipFile(renderer.CLIENT) as assets:
        renderer.ASSETS = assets
        views = []
        for camera, name, label in [((-1, -1), 'northwest', '西北剖视 · 上行残阶与半堵下行梯'), ((1, -1), 'northeast', '东北剖视 · 三层上下梯'), ((1, 1), 'southeast', '东南剖视 · 封闭外壳与接口'), ((-1, 1), 'southwest', '西南剖视 · 断梁支撑与上层平台')]:
            rendered = renderer.render(room, camera, label, size=(1700, 1400))
            rendered.save(output / (name + '.png'))
            views.append(rendered)
        views[0].save(ROOT / 'out/preview.png')
        sheet = Image.new('RGB', (2400, 1976), '#131e24')
        for index, rendered in enumerate(views):
            sheet.paste(rendered.resize((1200, 988), Image.Resampling.LANCZOS), ((index % 2) * 1200, (index // 2) * 988))
        sheet.save(output / 'four-views.png')
        renderer.render(room, (1, -1), '完整外壳 · 屋顶未在交付结构中开洞', exterior=True, size=(1700, 1400)).save(output / 'exterior.png')
        lower = copy.deepcopy(room)
        for position in lower.blocks:
            if position[1] >= 10:
                lower.blocks[position] = AIR
        renderer.render(lower, (1, 1), '下层通行细节 · 预览隐藏 Y≥10 的上层楼板', size=(1700, 1400)).save(output / 'lower-cutaway.png')
        for name, bounds, label in [('upper-break-detail', (2, 8, 9, 16), '上行断口 · 局部裁取 X2..8 / Z9..16'), ('lower-gap-detail', (14, 20, 9, 16), '下行半堵 · 局部裁取 X14..20 / Z9..16')]:
            detail = copy.deepcopy(room)
            for position in detail.blocks:
                if not (bounds[0] <= position[0] <= bounds[1] and bounds[2] <= position[2] <= bounds[3]):
                    detail.blocks[position] = AIR
            renderer.render(detail, (-1, -1), label, size=(1500, 1300)).save(output / (name + '.png'))
        native = ROOT / 'qa/native-placed-none.nbt'
        if native.exists():
            renderer.render(Structure.read(native), (-1, -1), '100 tick 后原生回采', native=True, size=(1700, 1400)).save(output / 'native-world-capture.png')
    plan_image(room, blueprint).save(output / 'plan.png')
    elevation = Image.new('RGB', (1740, 920), '#131e24')
    draw = ImageDraw.Draw(elevation)
    draw.text((35, 22), 'C · 接口立面图  /  实际 NBT 边界平面，黑格为空气', font=renderer.font(30), fill='#e5e1d2')
    for index, (pos_z, label) in enumerate(((0, '北墙 Z=0 · 主入口脚位 Y=6'), (24, '南墙 Z=24 · 下层 Y=1 / 上层 Y=12'))):
        origin_x = 45 + index * 850
        draw.text((origin_x, 95), label, font=renderer.font(24), fill='#b7d7cf')
        for pos_y in range(18):
            for pos_x in range(23):
                block = room.get((pos_x, pos_y, pos_z))
                color = '#090f14' if block == AIR else '#8acbe2' if block.name.endswith('stained_glass') else '#ede4b0' if block.name.endswith('sea_lantern') else '#70817a'
                left, top = origin_x + pos_x * 33, 165 + (17 - pos_y) * 33
                draw.rectangle((left, top, left + 31, top + 31), fill=color)
        draw.text((origin_x, 790), 'X → ；三格宽×三格高开口，黑格不代表模板遗漏。', font=renderer.font(20), fill='#aebfc6')
    draw.text((45, 850), '冷蓝破洞以封闭玻璃转译；完整屋顶、底板与三处门洞均在最终 NBT 中核验。', font=renderer.font(22), fill='#abbec7')
    elevation.save(output / 'interfaces.png')
    evidence = {'status': 'RENDERED_AWAITING_VISUAL_REVIEW', 'source_nbt_sha256': fingerprint(ROOT / 'out/room.nbt'), 'renderer': 'Actual NBT geometry and local Minecraft 1.20.1 vanilla block models/textures, Pillow software rasterizer', 'not_a_game_screenshot': True, 'cutaway_policy': 'Main views hide only roof and near walls, preserve geometry; lower-cutaway additionally omits all Y>=10 blocks for visibility, without changing the template.', 'files': {path.relative_to(ROOT).as_posix(): fingerprint(path) for path in sorted(output.glob('*.png'))}}
    evidence['files']['out/preview.png'] = fingerprint(ROOT / 'out/preview.png')
    save_json(ROOT / 'qa/render.json', evidence)
    print(json.dumps({'status': evidence['status'], 'files': len(evidence['files'])}))


if __name__ == '__main__':
    run()
