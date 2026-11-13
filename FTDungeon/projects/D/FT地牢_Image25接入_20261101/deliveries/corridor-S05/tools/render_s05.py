import zipfile

from PIL import Image, ImageDraw

from qa_static import ROOT, SHARED, digest, write_json
from nbtio import Structure
import render_engine as engine


VIEWS = [('north', '北向内部 / 南端通拱', (-0.42, -1)), ('south', '南向内部 / 北入口', (0.42, 1)), ('west', '西向内部 / 东壁火把', (-1, 0.32)), ('east', '东向内部 / 西壁吊笼', (1, -0.32))]


def render(room, camera, label, exterior=False, native=False):
    shown = Structure(room.size)
    for position, block in room.blocks.items():
        pos_x, pos_y, pos_z = position
        near_x = pos_x <= 1 if camera[0] < 0 else pos_x >= 9
        near_z = pos_z == (0 if camera[1] < 0 else 15)
        if not exterior and (pos_y == 9 or pos_y >= 1 and (near_x or near_z)):
            continue
        shown.blocks[position] = block
    image = engine.render(shown, camera, label, exterior=True, native=native, elevation=0.72)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, image.width, 124), fill='#121d23')
    draw.text((35, 22), 'S05 黑石积水吊笼短廊 / ' + label, font=engine.font(30), fill='#eee4d0')
    source = 'Forge 原生 240 tick 回采' if native else '最终 room.nbt'
    cut = '完整外壳；无剖切' if exterior else '仅剖开顶盖和近侧双层外墙；柱梁、吊笼、火把不移位'
    draw.text((35, 76), source + ' · 11×10×16 · ' + cut, font=engine.font(19), fill='#9fc9bd')
    return image


def plan(room):
    image = Image.new('RGB', (1600, 1300), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 25), 'S05 / 最终 NBT 通路与水封平面', font=engine.font(34), fill='#eee4d0')
    draw.text((35, 80), '北 ↑   Y=1 地面层；北南直通；无分支 / 无台阶 / 轴向 16 格', font=engine.font(24), fill='#9fc9bd')
    cell, left, top = 58, 100, 190
    for pos_z in range(16):
        draw.text((left - 45, top + pos_z * cell + 15), str(pos_z), font=engine.font(18), fill='#a0b4b9')
        for pos_x in range(11):
            block = room.get((pos_x, 1, pos_z)).name.removeprefix('minecraft:')
            color = '#356a8d' if block == 'water' else '#777566' if pos_x in (4, 5, 6) else '#38434c'
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            draw.rectangle(rectangle, fill=color)
            if pos_x in (4, 5, 6):
                draw.rectangle(rectangle, outline='#d4c989', width=2)
    for pos_x in range(11):
        draw.text((left + pos_x * cell + 20, top - 33), str(pos_x), font=engine.font(18), fill='#a0b4b9')
    for pos_z, label in ((0, '入'), (3, '安'), (15, '出')):
        draw.text((left + 5 * cell + 14, top + pos_z * cell + 13), label, font=engine.font(25), fill='#fff1ba')
    labels = ['北入口脚位 (5,2,0)', '安全点脚位 (5,2,3)', '南出口脚位 (5,2,15)', '干路：X=4…6，Y=2', '净宽 3 / 净高至少 3', '封水：Y=1，共 52 水源', '水深 1 格，路基高 1 格', '首尾封水端坎 / 两侧实墙', '吊笼不侵入三格主路', '空容器 0 / 实体 0', '四链锚固于完整顶盖', '核验整个体积后方可接入']
    for index, label in enumerate(labels):
        draw.text((850, 200 + index * 70), label, font=engine.font(25), fill='#c4d3c7')
    draw.text((35, 1215), '实际 NBT 数据绘制，非 AI 图、非整合包游戏截图。浅蓝为真水，金框为干路。', font=engine.font(24), fill='#9eafb4')
    return image


def main():
    preview = ROOT / 'preview'
    preview.mkdir(exist_ok=True)
    room = Structure.read(ROOT / 'room.nbt')
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    outputs = []
    sheet = Image.new('RGB', (3200, 2500), '#121d23')
    for index, (name, label, camera) in enumerate(VIEWS):
        image = render(room, camera, label)
        path = preview / (name + '.png')
        image.save(path)
        outputs.append(path)
        sheet.paste(image, (index % 2 * 1600, index // 2 * 1250))
    sheet.save(preview / 'four-views.png')
    outputs.append(preview / 'four-views.png')
    render(room, (-0.42, -1), '主剖视 / 三格干路与两侧积水').save(ROOT / 'preview.png')
    outputs.append(ROOT / 'preview.png')
    render(room, (-0.8, -1), '北西完整顶盖与外壳', exterior=True).save(preview / 'exterior.png')
    render(room, (0.8, 1), '南东完整顶盖与外壳', exterior=True).save(preview / 'exterior-south.png')
    outputs.extend([preview / 'exterior.png', preview / 'exterior-south.png'])
    plan(room).save(preview / 'plan.png')
    outputs.append(preview / 'plan.png')
    native_sheet = Image.new('RGB', (3200, 2500), '#121d23')
    native_outputs = []
    for index, rotation in enumerate(('none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90')):
        source = ROOT / 'qa' / ('native-placed-' + rotation + '.nbt')
        if source.exists():
            captured = Structure.read(source)
            image = render(captured, VIEWS[index][2], '原生旋转 ' + rotation, native=True)
            destination = preview / ('native-' + rotation + '.png')
            image.save(destination)
            native_outputs.append(destination)
            native_sheet.paste(image, (index % 2 * 1600, index // 2 * 1250))
    if len(native_outputs) == 4:
        native_sheet.save(preview / 'native-four-views.png')
        outputs.extend([*native_outputs, preview / 'native-four-views.png'])
    for name in ('lodestone-interior.png', 'lodestone-exterior.png'):
        if (preview / name).exists():
            outputs.append(preview / name)
    write_json(ROOT / 'qa/render-manifest.json', {
        'status': 'PASS', 'source_nbt_sha256': digest(ROOT / 'room.nbt'),
        'renderer': str(SHARED / 'render_engine.py'), 'renderer_sha256': digest(SHARED / 'render_engine.py'),
        'vanilla_assets': str(engine.CLIENT), 'vanilla_assets_sha256': digest(engine.CLIENT),
        'outputs': [{'path': str(path.relative_to(ROOT)), 'sha256': digest(path), 'size': list(Image.open(path).size)} for path in outputs],
        'native_rotation_renders': len(native_outputs),
        'limitations': ['Software rendering from actual final NBT and vanilla 1.20.1 models/textures, not an AI image or game screenshot.', 'Interior images remove roof lid and near exterior wall layers only. Cutaway makes roof-anchored chains appear above the exposed volume; full shell remains in room.nbt.', 'Static water geometry, no shader, torch-light bounce, reflection or animation.'],
    })
    print('S05_RENDER_PASS outputs=' + str(len(outputs)))


if __name__ == '__main__':
    main()
