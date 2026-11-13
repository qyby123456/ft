import json
import zipfile

from PIL import Image, ImageDraw

import vanilla_renderer as renderer
from build_room import ROOT, digest, write_json
from nbtio import AIR, Structure


def plan(room, blueprint):
    image = Image.new('RGB', (1536, 1100), '#101e2c')
    draw = ImageDraw.Draw(image)
    font = renderer.font
    draw.text((35, 25), 'G 冰霜商店 / NBT 方块与净空平面', font=font(34), fill='#d5f0ff')
    cell, left, top = 52, 70, 125
    ring = {(position[0], position[2]) for position in blueprint['counter_clearance']}
    pillar_ring = {(position[0], position[2]) for position in blueprint['pillar_clearance']}
    feature_columns = {name: {(position[0], position[2]) for position in positions} for name, positions in blueprint['features'].items()}
    for pos_z in range(room.size[2]):
        for pos_x in range(room.size[0]):
            tile = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell, top + (pos_z + 1) * cell)
            fill = '#384857' if pos_x in (0, room.size[0] - 1) or pos_z in (0, room.size[2] - 1) else '#7fa4ba'
            if 7 <= pos_x <= 9 and pos_z <= 8:
                fill = '#69ac9e'
            if (pos_x, pos_z) in pillar_ring:
                fill = '#9cbfce'
            for feature, color in (('counter', '#65462c'), ('shelves', '#8a603b'), ('display_west', '#53c7e8'), ('display_east', '#53c7e8'), ('pillars', '#d4eafd'), ('barrel', '#99673c'), ('ice_stock', '#2978b8')):
                if (pos_x, pos_z) in feature_columns.get(feature, set()):
                    fill = color
            if pos_z == 0 and 7 <= pos_x <= 9:
                fill = '#63c6a0'
            draw.rectangle(tile, fill=fill, outline='#203845', width=1)
            if (pos_x, pos_z) in ring:
                draw.rectangle(tuple(value + (3 if index < 2 else -3) for index, value in enumerate(tile)), outline='#b7f2a3', width=3)
            if (pos_x, pos_z) in feature_columns['pillars']:
                draw.text((tile[0] + 14, tile[1] + 9), '柱', font=font(23), fill='#1e4869')
    for coordinate in range(room.size[0]):
        draw.text((left + coordinate * cell + 16, top - 30), str(coordinate), font=font(17), fill='#a2bdd2')
    for coordinate in range(room.size[2]):
        draw.text((left - 33, top + coordinate * cell + 14), str(coordinate), font=font(17), fill='#a2bdd2')
    for offset, text in enumerate(('北入口 (8,1,0)', '安全内点 (8,1,3)', '三宽三高干燥主路', '柜台 7×1 / 四周环通', '四柱均有全高一格空隙', '落地双玻璃展柜', '双灯在柜台上方悬挂', '蓝冰洼仅在侧角', '无水、粉雪和奖励库存')):
        draw.text((1000, 160 + offset * 65), text, font=font(25), fill='#c2deef')
    draw.text((55, 970), 'X 向右 / Z 向下 / 北为上方；绿色主通道，浅绿边柜台环路，浅蓝四柱八邻域。', font=font(24), fill='#bdd7e7')
    draw.text((55, 1025), '从实际最终 NBT 回读。软件示意，不是游戏截图；结构屋顶完整，正式槽位待统一接入。', font=font(23), fill='#92b2ca')
    return image


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    renderer.ASSETS = zipfile.ZipFile(renderer.CLIENT)
    preview = ROOT / 'preview'
    views = [('north', '北侧 / 门口方向', (-0.7, -1)), ('south', '南侧 / 后墙方向', (0.7, 1)), ('west', '西侧 / 冰霜展柜', (-1, 0.6)), ('east', '东侧 / 冰斧浮雕', (1, -0.6))]
    panels = []
    for name, label, camera in views:
        image = renderer.render(room, camera, label)
        image.save(preview / (name + '.png'))
        panels.append(image)
    panels[0].resize((1536, 1200), Image.Resampling.LANCZOS).save(ROOT / 'out/preview.png')
    four = Image.new('RGB', (3200, 2500), '#121d23')
    for index, image in enumerate(panels):
        four.paste(image, ((index % 2) * 1600, (index // 2) * 1250))
    four.save(preview / 'four-views.png')
    renderer.render(room, (-0.7, -1), '北向外壳 / 完整屋顶', exterior=True).save(preview / 'exterior.png')
    plan(room, blueprint).save(preview / 'plan.png')
    captures = []
    native_path = ROOT / 'qa/native-placed-none.nbt'
    if native_path.exists():
        native = Structure.read(native_path)
        renderer.render(native, views[0][2], '原生放置稳定 100 tick 后回采', native=True).save(preview / 'native-world-capture.png')
        captures.append({'nbt': str(native_path), 'sha256': digest(native_path), 'preview': 'preview/native-world-capture.png'})
    manifest = {'structure_sha256': digest(ROOT / 'out/room.nbt'), 'source': 'Serialized delivered NBT; vanilla 1.20.1 client.jar geometry and textures', 'client_jar': str(renderer.CLIENT), 'client_jar_sha256': digest(renderer.CLIENT), 'views': [name for name, _, _ in views], 'cutaway': 'Roof and near exterior walls removed in rendering memory only; all four detached pillars and furniture preserved', 'native_captures': captures, 'limitations': ['Software render, not in-game screenshot, biome tint, client shader or lighting simulation.', 'Transparent materials use face sorting rather than exact Minecraft rasterization.']}
    write_json(ROOT / 'qa/render-manifest.json', manifest)
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures)}))


if __name__ == '__main__':
    main()
