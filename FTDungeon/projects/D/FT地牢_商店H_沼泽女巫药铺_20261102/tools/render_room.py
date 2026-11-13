import json
import zipfile

from PIL import Image, ImageDraw

import render_engine as engine
from build_room import ROOT, digest, write_json
from nbtio import AIR, Structure


def plan(room, blueprint):
    image = Image.new('RGB', (1500, 1250), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), 'H 沼泽女巫药铺 / 实际 NBT 坐标平面', font=engine.font(32), fill='#e9e5ce')
    draw.text((35, 80), '北 ↑    玩家脚部 Y=1    柜台四边与四柱周围均留走道', font=engine.font(22), fill='#a0c2a3')
    left, top, cell = 90, 170, 60
    categories = {}
    styles = {
        'pillars': ('#859377', '柱'), 'counter': ('#8c623e', '柜'),
        'shelves': ('#645243', '架'), 'back_shelves': ('#645243', '架'),
        'barrels': ('#a38855', '桶'), 'cauldron_plinth': ('#498d69', '锅'),
        'basin_water': ('#287a9a', '水'), 'basin_rim': ('#526d55', '岸'),
        'recipe_stand': ('#9c8056', '谱'), 'broom': ('#a99b59', '帚'),
    }
    for group in styles:
        for pos_x, pos_y, pos_z in blueprint['features'].get(group, []):
            if pos_y == 1:
                categories[(pos_x, pos_z)] = group
    ring = {(position[0], position[2]) for position in blueprint['counter_clearance']}
    for pos_z in range(15):
        for pos_x in range(15):
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            category = categories.get((pos_x, pos_z))
            color, label = styles.get(category, ('#35423c' if room.get((pos_x, 1, pos_z)) == AIR else '#5a6052', ''))
            if 6 <= pos_x <= 8 and pos_z <= 6:
                color = '#326c65'
            draw.rectangle(rectangle, fill=color)
            if (pos_x, pos_z) in ring:
                draw.rectangle(rectangle, outline='#a3d69e', width=3)
            if label:
                draw.text((rectangle[0] + 17, rectangle[1] + 14), label, font=engine.font(22), fill='#f6ebcf')
    for coordinate in range(15):
        draw.text((left + coordinate * cell + 19, top - 32), str(coordinate), font=engine.font(18), fill='#a0b4a6')
        draw.text((left - 42, top + coordinate * cell + 17), str(coordinate), font=engine.font(18), fill='#a0b4a6')
    legend = ['北入口 (7,1,0)', '安全点 (7,1,3)', '三格宽 × 三格高', '柜台 X=5…9 / Z=8', '四边净空 ≥1格', '四柱落地到顶', '双吊笼 / 双吊灯', '9个空方块实体', '1格封闭水盆', '无库存、无交易逻辑']
    for index, text in enumerate(legend):
        draw.text((1040, 195 + index * 67), text, font=engine.font(23), fill='#d5ddc0')
    draw.text((35, 1130), '笼、灯位于高处，不占脚部通道；绿色边框为柜台环通。完整屋顶保存在最终结构内。', font=engine.font(24), fill='#acc3a3')
    draw.text((35, 1190), '此图读取最终交付 NBT，不是 AI 设计图或游戏客户端截图。', font=engine.font(22), fill='#96a99e')
    return image


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    views = [('north', '北侧 / 药锅与睡莲盆', (-0.85, -1)), ('south', '南侧 / 墙边标本架', (0.85, 1)), ('west', '西侧 / 吊笼与柜台', (-1, 0.65)), ('east', '东侧 / 草药与空酿造台', (1, -0.65))]
    panels = []
    for name, label, camera in views:
        image = engine.render(room, camera, label)
        image.save(ROOT / 'preview' / (name + '.png'))
        panels.append(image)
    hero_camera = (-0.35, -1)
    hero = engine.render(room, hero_camera, '主视图 / 标本柜台与四周走道', elevation=0.48)
    hero.save(ROOT / 'out/preview.png')
    hero.save(ROOT / 'preview/hero.png')
    sheet = Image.new('RGB', (3200, 2500), '#121d23')
    for index, panel in enumerate(panels):
        sheet.paste(panel, ((index % 2) * 1600, (index // 2) * 1250))
    sheet.save(ROOT / 'preview/four-views.png')
    engine.render(room, (-0.85, -1), '完整屋顶 / 北侧入口', exterior=True).save(ROOT / 'preview/exterior.png')
    plan(room, blueprint).save(ROOT / 'preview/plan.png')
    captures = []
    native = ROOT / 'qa/native-placed-none.nbt'
    if native.exists():
        engine.render(Structure.read(native), hero_camera, '原生世界100 tick后回采', native=True, elevation=0.48).save(ROOT / 'preview/native-world-capture.png')
        captures.append({'nbt': 'qa/native-placed-none.nbt', 'sha256': digest(native), 'preview': 'preview/native-world-capture.png'})
    write_json(ROOT / 'qa/render-manifest.json', {
        'status': 'PASS', 'source_nbt_sha256': digest(ROOT / 'out/room.nbt'),
        'source': 'Actual final NBT, vanilla 1.20.1 block models and textures; not AI substitute',
        'client_jar': str(engine.CLIENT), 'client_jar_sha256': digest(engine.CLIENT),
        'views': 4, 'native_captures': captures,
        'limitations': ['Software orthographic projection, not a player/client screenshot.', 'No shader or actual emitted-light simulation; foliage and water use an approximate green/blue tint, not sampled biome colors.', 'Water is an approximate static volume; sign text is not rendered and the sign board is approximated.', 'Only preview memory removes roof and near exterior walls; all four independent pillars and furniture remain.'],
    })
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
