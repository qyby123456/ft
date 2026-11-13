import copy
import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

import vanilla_models as renderer
from build_room import ROOT, write_json
from nbtio import AIR, Structure


def cut(room, mode):
    result = copy.deepcopy(room)
    removed = []
    for position, block in room.blocks.items():
        pos_x, pos_y, pos_z = position
        keep_last_lower_step = pos_y == 6 and 6 <= pos_x <= 8 and pos_z == 13
        remove = mode == 'lower' and pos_y >= 6 and not keep_last_lower_step
        if mode == 'section':
            remove = pos_x <= 5 and pos_y >= 1 or pos_z >= 21 and pos_y >= 1
        if remove and block != AIR:
            result.set(position, AIR)
            removed.append(list(position))
    return result, removed


def plans(room):
    image = Image.new('RGB', (1800, 800), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'A · 实际 NBT 分层平面 / 北侧三层接口', font=renderer.font(32), fill='#e4d3ff')
    palette = {'air': '#26343c', 'stone_brick_stairs': '#dfb97c', 'iron_bars': '#70cbd4', 'red_carpet': '#983d49', 'barrel': '#ad834f', 'lantern': '#ffcc67', 'chain': '#7c8185', 'cobweb': '#d1d6cd'}
    size = 22
    for index, feet_y in enumerate((1, 7, 13)):
        offset_x, offset_y = 35 + index * 585, 135
        draw.text((offset_x, 82), f'脚部 Y={feet_y} · ' + ('下层' if index == 0 else '中层' if index == 1 else '上层'), font=renderer.font(25), fill='#d4dee2')
        for pos_x in range(23):
            for pos_z in range(25):
                block = room.get((pos_x, feet_y, pos_z))
                name = block.name.removeprefix('minecraft:')
                color = palette.get(name, '#758181')
                if name == 'air' and room.get((pos_x, feet_y - 1, pos_z)).name.removeprefix('minecraft:') == 'air':
                    color = '#101519'
                bounds = (offset_x + pos_x * size, offset_y + pos_z * size, offset_x + (pos_x + 1) * size - 1, offset_y + (pos_z + 1) * size - 1)
                draw.rectangle(bounds, fill=color)
        entrance_x = (7, 11, 15)[index]
        draw.text((offset_x + entrance_x * size - 35, offset_y - 30), '北接口', font=renderer.font(18), fill='#75dde3')
    draw.text((35, 725), '浅灰：石构 / 金：台阶 / 青：铁栏 / 红：地毯 / 深底：空气或层间洞口；只读取 room.nbt。', font=renderer.font(23), fill='#b5c6cf')
    return image


def main():
    room = Structure.read(ROOT / 'room.nbt')
    digest_before = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    outputs = []
    with zipfile.ZipFile(renderer.CLIENT) as assets:
        renderer.ASSETS = assets
        for name, camera in [('south-west', (-1, 1)), ('south-east', (1, 1)), ('north-west', (-1, -1)), ('north-east', (1, -1))]:
            path = ROOT / f'preview/{name}.png'
            renderer.render(room, camera, name + ' 剖切视角', size=(1600, 1250)).save(path)
            outputs.append(path)
        path = ROOT / 'preview/exterior.png'
        renderer.render(room, (1, -1), '完整屋顶与外壳', exterior=True, size=(1600, 1250)).save(path)
        outputs.append(path)
        for name, camera in [('lower', (-1, -1)), ('section', (-1, 1))]:
            clipped, removed = cut(room, name)
            path = ROOT / f'preview/{name}.png'
            renderer.render(clipped, camera, '下层梯段详图（预览隐藏 Y≥6）' if name == 'lower' else '西侧结构剖面（仅显示裁切）', size=(1600, 1250)).save(path)
            outputs.append(path)
            write_json(ROOT / f'qa/render-{name}-cuts.json', {'source_sha256': digest_before, 'removed_only_in_memory': removed})
        native_path = ROOT / 'qa/native-placed-none.nbt'
        if native_path.is_file():
            native = Structure.read(native_path)
            path = ROOT / 'preview/native-world-capture.png'
            renderer.render(native, (-1, 1), 'Forge 原生世界回采 · 100 tick', native=True, size=(1600, 1250)).save(path)
            outputs.append(path)
    path = ROOT / 'preview/level-plans.png'
    plans(room).save(path)
    outputs.append(path)
    image = Image.new('RGB', (1800, 1720), '#10191f')
    draw = ImageDraw.Draw(image)
    draw.text((38, 22), 'A / 石砖折返楼梯间', font=renderer.font(40), fill='#f0e6d0')
    draw.text((38, 78), '23×19×25 · 两段三格宽楼梯 · 下 / 中 / 上脚部 Y=1 / 7 / 13', font=renderer.font(25), fill='#a8c3cf')
    main_image = Image.open(ROOT / 'preview/south-west.png')
    lower_image = Image.open(ROOT / 'preview/lower.png')
    main_image = main_image.crop((20, 115, 1580, 1190)).resize((1160, 800), Image.Resampling.LANCZOS)
    lower_image = lower_image.crop((20, 115, 1580, 1190)).resize((600, 415), Image.Resampling.LANCZOS)
    image.paste(main_image, (20, 145))
    image.paste(lower_image, (1180, 220))
    draw.text((1200, 655), '下层实际 NBT 详图', font=renderer.font(25), fill='#e3d5bb')
    draw.text((1200, 700), '仅预览隐藏上层；', font=renderer.font(21), fill='#a8c3cf')
    draw.text((1200, 735), '交付外壳、楼板与屋顶均保留。', font=renderer.font(21), fill='#a8c3cf')
    level = Image.open(ROOT / 'preview/level-plans.png').resize((1740, 773), Image.Resampling.LANCZOS)
    level = level.crop((0, 60, 1740, 690))
    image.paste(level, (30, 950))
    draw.text((38, 1667), '软件材质渲染，非游戏截图。双向通行、支撑、栏杆与四向稳定性以 qa/native.json 为准。', font=renderer.font(23), fill='#a8c3cf')
    image.save(ROOT / 'preview.png')
    outputs.append(ROOT / 'preview.png')
    assert digest_before == hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    write_json(ROOT / 'qa/render-manifest.json', {'source_sha256': digest_before, 'source': 'actual room.nbt and optional actual Forge capture', 'renderer': 'vanilla 1.20.1 blockstate/model/texture software projection', 'not_client_screenshots': True, 'files': [{'file': str(path.relative_to(ROOT)), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'size': list(Image.open(path).size)} for path in outputs]})
    print(json.dumps({'rendered': len(outputs), 'source_unchanged': True}))


if __name__ == '__main__':
    main()
