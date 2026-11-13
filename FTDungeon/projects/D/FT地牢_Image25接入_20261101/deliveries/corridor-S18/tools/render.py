import hashlib
import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, 'D:/ft工程文件/S7-Dark-Iron-Cell-20261102/tools')
import render_s7 as depth_renderer
from PIL import Image, ImageDraw

viewer = depth_renderer.viewer
original_opaque = viewer.opaque
original_texture = viewer.renderer.texture


def filtered(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        blockX, blockY, blockZ = position
        if block.name.removeprefix('minecraft:') in viewer.INVISIBLE:
            continue
        if not exterior:
            if blockY >= 9:
                continue
            near_x = blockX <= 0 if camera[0] < 0 else blockX >= 14
            near_z = blockZ == 0 if camera[1] < 0 else blockZ == 15
            if blockY >= 3 and (near_x or near_z):
                continue
            visible_lava = block.name == 'minecraft:lava'
            near_wall = blockX <= 2 if camera[0] < 0 else blockX >= 12
            if blockY >= 3 and near_wall and not visible_lava:
                continue
        result[position] = block
    return result


def opaque(block):
    if block is not None and block.name in ('minecraft:glass', 'minecraft:water', 'minecraft:lava'):
        return False
    return original_opaque(block)


def texture(identifier, uv, rotation, brightness):
    result = original_texture(identifier, uv, rotation, brightness)
    if identifier == 'block/water_still':
        result = result.copy()
        red, green, blue, alpha = result.split()
        result = Image.merge('RGBA', (red.point(lambda value: int(value * .18)), green.point(lambda value: int(value * .60)), blue.point(lambda value: int(value * .82)), alpha.point(lambda value: int(value * .78))))
    return result


viewer.filtered = filtered
viewer.opaque = opaque
viewer.renderer.texture = texture


def render(structure, metadata, camera, title, exterior=False, size=(1400, 1080)):
    image = depth_renderer.render(structure, metadata, camera, title, exterior, size)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, size[0], 103), fill='#101a20')
    draw.rectangle((0, size[1] - 53, size[0], size[1]), fill='#101a20')
    draw.text((35, 22), 'S18 浸水熔铸短廊 · ' + title, font=viewer.font(28), fill='#ede8d6')
    draw.text((35, 65), '最终 NBT / 15×11×16 / ' + ('完整双层屋顶、双端贯通' if exterior else '剖去屋顶及近侧墙炉壳以显露封闭熔岩；原结构不变'), font=viewer.font(19), fill='#8ebbb9')
    draw.text((35, size[1] - 40), '真实 NBT 与 1.20.1 材质软件投影；非游戏截图。浅水色为显式冷蓝显示色，不模拟光照。', font=viewer.font(17), fill='#99a4aa')
    return image


def main():
    source = ROOT / 'room.nbt'
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    fingerprint = hashlib.sha256(source.read_bytes()).hexdigest()
    assert fingerprint == metadata['sha256']
    structure = depth_renderer.builder.Structure.read(source)
    output = ROOT / 'preview'
    output.mkdir(exist_ok=True)
    render(structure, metadata, (-.65, -1), '北西总览 / 干道与双侧水槽', size=(1600, 1200)).save(ROOT / 'preview.png')
    manifests = []
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2400, 1800), '#101a20')
        for index, (direction, title, camera) in enumerate(viewer.VIEWS):
            image = render(structure, metadata, camera, title, exterior, (1200, 900))
            destination = output / f'{mode}-{direction}.png'
            image.save(destination)
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
            manifests.append({'file': str(destination), 'sha256': hashlib.sha256(destination.read_bytes()).hexdigest()})
        sheet.save(output / (mode + '-four-views.png'))
    image = Image.new('RGB', (1100, 1070), '#101a20')
    draw = ImageDraw.Draw(image)
    draw.text((40, 26), 'S18 / 液体隔离与干燥通路平面', font=viewer.font(28), fill='#eee4cf')
    draw.text((40, 75), '北在上，南端亦开放；图中按不同 Y 层叠加标注，不把炉槽当作地板。', font=viewer.font(18), fill='#9db9c3')
    cell, offsetX, offsetY = 48, 58, 151
    for blockZ in range(16):
        for blockX in range(15):
            floor = structure.get((blockX, 2, blockZ)).name
            forge = structure.get((blockX, 4, blockZ)).name
            color = '#293340'
            if floor == 'minecraft:water':
                color = '#196d97'
            if blockX in (6, 7, 8):
                color = '#32816e'
            if forge == 'minecraft:lava':
                color = '#e57b29'
            if forge == 'minecraft:glass':
                color = '#a8a19c'
            left, top = offsetX + blockX * cell, offsetY + blockZ * cell
            draw.rectangle((left, top, left + cell - 2, top + cell - 2), fill=color)
            if [blockX, 3, blockZ] in (metadata['entrance'], metadata['interior'], metadata['exit']):
                label = '入' if blockZ == 0 else '出' if blockZ == 15 else '安'
                draw.text((left + 10, top + 8), label, font=viewer.font(24), fill='#fff2ad')
    for blockX in range(15):
        draw.text((offsetX + blockX * cell + 15, 119), str(blockX), font=viewer.font(15), fill='#9eacb5')
    for blockZ in range(16):
        draw.text((22, offsetY + blockZ * cell + 12), str(blockZ), font=viewer.font(15), fill='#9eacb5')
    lines = ['绿色：Y=3 三宽干道', '蓝色：Y=2 凹入水槽', '橙色：Y=4 真熔岩', '灰色：完整玻璃封面', '', '底板：Y=0～1', '路面：Y=2', '脚位：Y=3', '屋顶：Y=9～10', '', '熔岩 12 源 / 六面封闭', '水 48 源 / 底与侧封闭', '中央 3×16 格始终干燥', '', '没有转弯、分岔、楼梯', '没有关闭门或必经跳跃']
    for index, line in enumerate(lines):
        draw.text((805, 166 + index * 38), line, font=viewer.font(17), fill='#cad6d2')
    draw.text((46, 953), '入口 [7,3,0] → 安全点 [7,3,2] → 出口 [7,3,15]；北南各至少三宽三高。', font=viewer.font(21), fill='#c9e4d8')
    draw.text((46, 1008), '封液、支撑和碰撞以最终 NBT 与 Forge 原生四向测试为准；图仅用于核查。', font=viewer.font(19), fill='#99aab4')
    image.save(output / 'floorplan.png')
    report = {'id': 'CORRIDOR_S18', 'status': 'RENDERED_PENDING_VISUAL_REVIEW', 'sha256': fingerprint, 'preview_from_final_nbt': True, 'roof_and_walls_only_hidden_for_cutaway': True, 'views': manifests, 'floorplan': str(output / 'floorplan.png'), 'texture_source': str(viewer.renderer.CLIENT), 'renderer': 'Shared read-only vanilla model loader and per-pixel depth/alpha renderer, with S18-only cutaway masks', 'limits': 'Software projections, not in-game screenshots; no runtime lighting. Water texture is explicitly tinted cold blue for inspection.'}
    (ROOT / 'qa/render.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('S18 final-NBT preview, eight directional views and floorplan rendered')


if __name__ == '__main__':
    main()
