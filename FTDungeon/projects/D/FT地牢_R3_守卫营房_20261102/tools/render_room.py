import json
import zipfile

from PIL import Image, ImageDraw

import vanilla_renderer as renderer
from build_room import ROOT, digest, write_json
from nbtio import Structure


ORIGINAL_DEFINITIONS = renderer.definitions
renderer.PARTIAL.update({'red_carpet', 'red_wall_banner', 'dark_oak_fence', 'ladder', 'lectern', 'campfire'})


def definitions(name, properties):
    if name == 'red_wall_banner':
        definition = renderer.box([2, -9, 14], [14, 16, 15], 'block/red_wool')
        definition['elements'].extend(renderer.box([6, -5, 13.8], [10, 12, 14], 'block/yellow_wool')['elements'])
        definition['elements'].extend(renderer.box([3, 3, 13.8], [13, 7, 14], 'block/yellow_wool')['elements'])
        definition['elements'].extend(renderer.box([1, 15, 14], [15, 16, 16], 'block/dark_oak_planks')['elements'])
        return [(definition, {'y': {'north': 0, 'east': 90, 'south': 180, 'west': 270}[properties['facing']]})]
    return ORIGINAL_DEFINITIONS(name, properties)


renderer.definitions = definitions


def plan(room, blueprint):
    image = Image.new('RGB', (1520, 1160), '#172128')
    draw = ImageDraw.Draw(image)
    draw.text((35, 20), 'R3 守卫营房 / 北入口与实际 NBT 平面', font=renderer.font(33), fill='#f2dfb9')
    scale, offset_x, offset_z = 50, 65, 125
    route = {tuple(position) for position in blueprint['circulation']}
    for pos_z in range(17):
        for pos_x in range(17):
            block = room.get((pos_x, 1, pos_z))
            color = '#606568' if pos_x in (0, 16) or pos_z in (0, 16) else '#90918b'
            if block.name in {'minecraft:red_carpet', 'minecraft:white_carpet'}:
                color = '#a33e37'
            elif block.name in {'minecraft:stripped_dark_oak_log', 'minecraft:dark_oak_planks', 'minecraft:dark_oak_slab'}:
                color = '#68452d'
            elif block.name in {'minecraft:chest', 'minecraft:barrel'}:
                color = '#b58b41'
            if (pos_x, 1, pos_z) in route or 7 <= pos_x <= 9 and pos_z <= 6:
                color = '#67a394'
            if 6 <= pos_x <= 10 and 8 <= pos_z <= 10:
                color = '#87603c'
            if pos_z == 15 and pos_x in (2, 3, 4, 12, 13, 14):
                color = '#a3a5a1'
            left, top = offset_x + pos_x * scale, offset_z + pos_z * scale
            draw.rectangle((left, top, left + scale - 1, top + scale - 1), fill=color, outline='#3c4546')
    for index in range(17):
        draw.text((offset_x + index * scale + 13, offset_z - 35), str(index), font=renderer.font(19), fill='#c1bcae')
        draw.text((offset_x - 35, offset_z + index * scale + 12), str(index), font=renderer.font(19), fill='#c1bcae')
    for position, label in (([8, 1, 0], '入'), ([8, 1, 3], '安')):
        draw.text((offset_x + position[0] * scale + 10, offset_z + position[2] * scale + 10), label, font=renderer.font(26), fill='#ffffff')
    labels = ['北 -Z ↑，X 向右', '模板：17×10×17', '北入口：(8,1,0)', '安全点：(8,1,3)', '入口三宽 × 三高', '两侧双层八组铺位', '木平台 / 红毯与白枕', '后墙落地兵器剪影架', '中央议事桌与空讲台', '前后长凳，外围环通', '空箱桶、黄色十字旗', '右前石炉：熄灭无火', '完整石顶 / 四组链灯']
    for index, label in enumerate(labels):
        draw.text((970, 140 + 60 * index), label, font=renderer.font(25), fill='#dfcfb0')
    draw.text((40, 1035), '绿色为北入口主路及家具环路；红色床铺，棕色家具，金色空容器，银色为兵器架。', font=renderer.font(24), fill='#b6c5bc')
    draw.text((40, 1090), '从最终 NBT 回读；非游戏截图。完整屋顶与顶梁仅在剖切渲染内存中隐藏。', font=renderer.font(23), fill='#9caeae')
    return image


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    room = Structure.read(ROOT / 'room.nbt')
    blueprint = json.loads((ROOT / 'blueprint.json').read_text(encoding='utf-8'))
    preview = ROOT / 'preview'
    preview.mkdir(exist_ok=True)
    panels = []
    with zipfile.ZipFile(renderer.CLIENT) as archive:
        renderer.ASSETS = archive
        for name, label, camera in [('north', '北西剖面 / 入口', (-0.7, -1)), ('south', '南东剖面 / 后墙', (0.7, 1)), ('west', '西侧床铺', (-1, 0.6)), ('east', '东侧床铺', (1, -0.6))]:
            image = renderer.render(room, camera, label)
            image.save(preview / (name + '.png'))
            panels.append(image)
        panels[0].resize((1536, 1200), Image.Resampling.LANCZOS).save(ROOT / 'preview.png')
        four = Image.new('RGB', (3200, 2500), '#172128')
        for index, image in enumerate(panels):
            four.paste(image, ((index % 2) * 1600, (index // 2) * 1250))
        four.save(preview / 'four-views.png')
        renderer.render(room, (-0.7, -1), '完整屋顶与北入口外观', exterior=True).save(preview / 'exterior.png')
        native = ROOT / 'qa/native-placed-none.nbt'
        if native.exists():
            renderer.render(Structure.read(native), (-0.7, -1), '原生放置稳定 200 tick 后回采', native=True).save(preview / 'native-world-capture.png')
    plan(room, blueprint).save(preview / 'plan.png')
    if fingerprint != digest(ROOT / 'room.nbt'):
        raise ValueError('Rendering changed source NBT')
    write_json(ROOT / 'qa/render.json', {'status': 'PASS', 'structure_sha256': fingerprint, 'source': 'Final delivered NBT, read back from disk',
               'client_jar': str(renderer.CLIENT), 'client_jar_sha256': digest(renderer.CLIENT), 'views': ['north', 'south', 'west', 'east'],
               'roof_and_beam_cut_only_in_rendering_memory': True, 'native_capture_rendered': native.exists(),
               'limitations': ['Software render, not in-game screenshot or full modpack client QA.', 'Patterned banner block-entity geometry is explicitly simplified; bedding is actual vanilla carpets/platforms, not an interactive bed.', 'No exact light simulation, biome tint, shader or transparency rasterization.']})
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'sha256': fingerprint}))


if __name__ == '__main__':
    main()
