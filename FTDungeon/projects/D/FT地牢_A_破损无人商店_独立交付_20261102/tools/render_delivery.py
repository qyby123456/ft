import json
import zipfile

from PIL import Image, ImageDraw

import voxel_renderer as renderer
from build_room import ROOT, digest, write_json
from nbtio import AIR, Structure


def plan(room, blueprint):
    image = Image.new('RGB', (1380, 1330), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((40, 24), 'A · 空间与废墟落地点 / 最终 NBT 俯视平面', font=renderer.font(32), fill='#efe4cc')
    draw.text((40, 81), '北 ↑   入口 (7,1,0)   安全内点 (7,1,3)   玩家脚部 Y=1', font=renderer.font(22), fill='#a9cfc5')
    cell, left, top = 65, 170, 155
    groups = {name: set(map(tuple, positions)) for name, positions in blueprint['features'].items()}
    palette = {'counter': ('#ae804a', '柜'), 'shelves': ('#6f593e', '架'), 'empty_chests': ('#b79b55', '箱'), 'empty_barrels': ('#877349', '桶'), 'fallen_beam': ('#83603a', '梁'), 'fallen_shelves': ('#765a42', '倒'), 'rubble': ('#82928a', '碎'), 'ground_lamp': ('#c7ad65', '灯'), 'pillars': ('#718684', '柱')}
    ring = {(position[0], position[2]) for position in blueprint['counter_clearance']}
    for pos_z in range(room.size[2]):
        draw.text((left - 40, top + pos_z * cell + 16), str(pos_z), font=renderer.font(18), fill='#a7bcb8')
        for pos_x in range(room.size[0]):
            if pos_z == 0:
                draw.text((left + pos_x * cell + 20, top - 33), str(pos_x), font=renderer.font(18), fill='#a7bcb8')
            rectangle = (left + pos_x * cell, top + pos_z * cell, left + (pos_x + 1) * cell - 2, top + (pos_z + 1) * cell - 2)
            color, letter = '#354b4f', ''
            if room.get((pos_x, 1, pos_z)) != AIR:
                color = '#26373b'
            if 6 <= pos_x <= 8 and pos_z <= 13:
                color, letter = '#346b60', '路'
            elif (pos_x, pos_z) in ring:
                color = '#4f6565'
            for name, (feature_color, feature_letter) in palette.items():
                if (pos_x, 1, pos_z) in groups.get(name, set()):
                    color, letter = feature_color, feature_letter
            draw.rectangle(rectangle, fill=color, outline='#1c2f33')
            if letter:
                draw.text((rectangle[0] + 18, rectangle[1] + 15), letter, font=renderer.font(24), fill='#f1e8d5')
    draw.text((40, 1175), '绿色 = 贯通的三格宽主路；灰色 = 每段柜台一格环路；碎块全部贴完整地板。', font=renderer.font(23), fill='#b9cfca')
    draw.text((40, 1225), '蛛网贴边墙 / 货架，不进主路；地面破洞仅用材质表现，不挖坑。', font=renderer.font(23), fill='#b9cfca')
    draw.text((40, 1275), '独立候选：未分配正式槽位、未部署、未改玩家存档。', font=renderer.font(23), fill='#b9cfca')
    return image


def main():
    room_path = ROOT / 'out/room.nbt'
    before = digest(room_path)
    room = Structure.read(room_path)
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    preview = ROOT / 'preview'
    views = [('north', '北侧 / 入口与断柜台', (-0.7, -1)), ('south', '南侧 / 背墙货架', (0.7, 1)), ('west', '西侧 / 空箱与碎石', (-1, 0.6)), ('east', '东侧 / 塌梁与倒架', (1, -0.6))]
    renderer.ASSETS = zipfile.ZipFile(renderer.CLIENT)
    panels = []
    for name, label, camera in views:
        image = renderer.render(room, camera, label)
        image.save(preview / (name + '.png'))
        panels.append(image)
    panels[0].save(ROOT / 'out/preview.png')
    four = Image.new('RGB', (3200, 2500), '#121d23')
    for index, panel in enumerate(panels):
        four.paste(panel, ((index % 2) * 1600, (index // 2) * 1250))
    four.save(preview / 'four-views.png')
    renderer.render(room, (-0.7, -1), '完整屋顶与北接口', exterior=True).save(preview / 'exterior.png')
    renderer.render(room, (-0.7, -1), '断柜台 / 塌梁 / 倒架细节', furniture_focus=True).save(preview / 'furniture-detail.png')
    plan(room, blueprint).save(preview / 'plan.png')
    captures = []
    for name, unused, camera in views[:1]:
        native_path = ROOT / 'qa/native-placed-none.nbt'
        if native_path.exists():
            native_room = Structure.read(native_path)
            renderer.render(native_room, camera, 'Forge 放置稳定 100 tick 后回采', native=True).save(preview / 'native-world-capture.png')
            captures.append({'path': str(native_path.relative_to(ROOT)), 'sha256': digest(native_path)})
    sheet = Image.new('RGB', (2400, 1710), '#121d23')
    draw = ImageDraw.Draw(sheet)
    draw.text((45, 20), 'FT 地牢 · A 破损无人商店 / 独立制作交付', font=renderer.font(40), fill='#efe4cc')
    draw.text((45, 85), '概念特征：断柜台 / 蛛网 / 塌梁 / 空箱       落地修正：完整顶板 / 三格通路 / 碎块贴地', font=renderer.font(24), fill='#a9cfc5')
    sheet.paste(panels[0].resize((1440, 1125)), (10, 150))
    reference = Image.open(ROOT / 'reference/shop-variant-A-broken-unmanned.png').convert('RGB')
    reference.thumbnail((885, 590))
    sheet.paste(reference, (1480, 180))
    draw.text((1490, 790), '经指纹核验的 A 概念图（非成品）', font=renderer.font(26), fill='#c9d3c5')
    draw.text((1490, 865), '① 断口改成三格可走缺口', font=renderer.font(27), fill='#e4d7ba')
    draw.text((1490, 925), '② 13 块塌梁 / 碎石 / 倒架均贴地', font=renderer.font(27), fill='#e4d7ba')
    draw.text((1490, 985), '③ 10 处蛛网贴墙，不进主路', font=renderer.font(27), fill='#e4d7ba')
    draw.text((1490, 1045), '④ 空箱空桶，无店主、奖励或刷怪', font=renderer.font(27), fill='#e4d7ba')
    draw.text((45, 1350), '左：从最终 room.nbt 原版方块状态与材质渲染；仅预览剖开屋顶及近侧墙。最终结构完整封顶。', font=renderer.font(27), fill='#b6cdc5')
    draw.text((45, 1420), '15×9×15  |  北接口 (7,1,0)  |  三格宽 × 三格高中央通路贯穿后墙  |  所有碎块 Y=1', font=renderer.font(27), fill='#b6cdc5')
    draw.text((45, 1490), '证据分层：软件预览 ≠ 实机截图；Forge 隔离放置 ≠ FT 正式世界接入或完整整合包游玩验收。', font=renderer.font(27), fill='#a8b6b5')
    draw.text((45, 1575), '未部署。须由协调线程 01a19758-b0e5-75c2-822a-4860527a1e8b 接收后再安排核心接入。', font=renderer.font(25), fill='#d7ba85')
    sheet.save(preview / 'delivery-sheet.png')
    assert digest(room_path) == before, 'Rendering modified the source NBT'
    write_json(ROOT / 'qa/render-manifest.json', {'status': 'PASS', 'source_nbt_sha256': before, 'source': 'Delivered NBT block states, vanilla 1.20.1 models/textures, software orthographic render', 'client_jar': str(renderer.CLIENT), 'client_jar_sha256': digest(renderer.CLIENT), 'native_captures': captures, 'detail_cutaway': 'furniture-detail.png additionally hides Y>=6 and complete hanging lamp assemblies in memory to expose countertops and debris; four standard views preserve beams and hanging lamps.', 'limitations': ['Not a player screenshot; no actual lighting, shader or client behavior tested.', 'Chest and sign entity visuals approximated; sign text not rendered.', 'Cutaway occurs in memory only; complete roof retained in final NBT.']})
    renderer.ASSETS.close()
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures)}))


if __name__ == '__main__':
    main()
