import json
import zipfile

from PIL import Image, ImageDraw

import render_engine as engine
from build_r2 import ROOT, digest, write_json
from nbtio import AIR, Structure


def plan(room):
    image = Image.new('RGB', (1250, 1120), '#121d23')
    draw = ImageDraw.Draw(image)
    draw.text((35, 25), 'R2 / 北入口、双餐桌与中央净空', font=engine.font(32), fill='#efe4cc')
    reachable = {tuple(position) for position in json.loads((ROOT / 'qa/reachable.json').read_text(encoding='utf-8'))}
    cell = 53
    for block_x in range(15):
        for block_z in range(15):
            position = block_x, 1, block_z
            name = room.get(position).name.split(':')[1]
            color = '#31575c' if position in reachable else '#4a4b50'
            label = ''
            if name in ('spruce_slab', 'spruce_stairs') and block_z >= 5:
                color, label = '#9e713b', '桌'
            elif name == 'dark_oak_slab':
                color = '#655031'
            elif name == 'barrel':
                color, label = '#927440', '桶'
            elif name == 'smoker':
                color, label = '#6d7480', '灶'
            elif name == 'campfire':
                color, label = '#bb623a', '火'
            elif name == 'water_cauldron':
                color, label = '#477d97', '盆'
            elif name == 'air' and 6 <= block_x <= 8 and block_z <= 11:
                color = '#438b7b'
            left, top = 66 + block_x * cell, 135 + block_z * cell
            draw.rectangle((left, top, left + cell - 3, top + cell - 3), fill=color)
            if label:
                draw.text((left + 12, top + 11), label, font=engine.font(22), fill='#f7f0d9')
    for coordinate in range(15):
        draw.text((78 + coordinate * cell, 98), str(coordinate), font=engine.font(17), fill='#9fafb5')
        draw.text((25, 146 + coordinate * cell), str(coordinate), font=engine.font(17), fill='#9fafb5')
    for index, line in enumerate(['北入口 [7,1,0]', '安全点 [7,1,3]', '中央 3宽 × 3高', '双餐桌 / 四长凳', '后墙石罩灶台', '空桶 / 空烟熏炉', '吊锅 / 草药架', '角落蛛网与断凳', '完整 15×15 屋顶']):
        draw.text((905, 164 + index * 65), line, font=engine.font(23), fill='#c7d5cb')
    draw.text((42, 985), '青绿：脚位净空及主路；所有坐标按模板 XYZ，图上方为北。', font=engine.font(25), fill='#b2c9c6')
    draw.text((42, 1040), '实际交付 NBT 俯视分析，不是 AI 设计图或游戏截图。', font=engine.font(22), fill='#91a5b0')
    return image


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    cameras = [('north', '北侧 / 炉灶与双餐桌', (-0.7, -1)), ('south', '南侧 / 北接口与通道', (0.7, 1)), ('west', '西侧 / 空桶与餐具架', (-1, 0.5)), ('east', '东侧 / 草药与洗涤盆', (1, -0.5))]
    sheet = Image.new('RGB', (3200, 2500), '#121d23')
    for index, (name, label, camera) in enumerate(cameras):
        image = engine.render(room, camera, label)
        image.save(ROOT / ('preview/' + name + '.png'))
        sheet.paste(image, ((index % 2) * 1600, (index // 2) * 1250))
    sheet.save(ROOT / 'preview/four-views.png')
    engine.render(room, (-0.65, -1), '主视 / 凌乱但不堵路', elevation=0.95).save(ROOT / 'out/preview.png')
    engine.render(room, (-0.65, -1), '完整屋顶与北接口', exterior=True).save(ROOT / 'preview/exterior.png')
    plan(room).save(ROOT / 'preview/plan.png')
    captures = []
    native = ROOT / 'qa/native-placed-none.nbt'
    if native.exists():
        engine.render(Structure.read(native), (-0.65, -1), '原生放置 240 tick 后回采', native=True, elevation=0.95).save(ROOT / 'preview/native-world.png')
        captures.append({'nbt': str(native), 'sha256': digest(native), 'preview': 'preview/native-world.png'})
    write_json(ROOT / 'qa/render.json', {
        'status': 'PASS', 'sha256': digest(ROOT / 'out/room.nbt'),
        'source': str(ROOT / 'out/room.nbt'), 'renderer': 'NBT + vanilla 1.20.1 blockstate/model/texture software rasterization',
        'client_jar': str(engine.CLIENT), 'client_jar_sha256': digest(engine.CLIENT),
        'cutaway_in_memory_only': True, 'views': 4, 'native_captures': captures,
        'model_assets': sorted(engine.USED_MODELS), 'texture_assets': sorted(engine.USED_TEXTURES),
        'client_screenshot': False,
        'limitations': ['Software projection is not a Minecraft client screenshot.', 'No emitted-light simulation, fire animation or smoke particles; animated textures use their first frame.', 'Foliage has approximate vanilla green tint; no resource-pack/shader validation.', 'Only the rendered copy removes the roof and near exterior wall layers; the delivered NBT retains them.'],
    })
    print(json.dumps({'status': 'RENDERED', 'views': 4, 'native_captures': len(captures), 'models': len(engine.USED_MODELS)}))


if __name__ == '__main__':
    main()
