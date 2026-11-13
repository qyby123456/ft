import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
FONT = 'C:/Windows/Fonts/msyh.ttc'


def font(size):
    return ImageFont.truetype(FONT, size)


for mode, title in [('interior', 'P03 熔岩泡跳台 · 四向室内剖视'), ('exterior', 'P03 熔岩泡跳台 · 四向完整外壳')]:
    sheet = Image.new('RGB', (1800, 1490), '#171c24')
    draw = ImageDraw.Draw(sheet)
    draw.text((30, 16), title, font=font(34), fill='#ffe7b5')
    draw.text((30, 65), '实际 room.nbt / Minecraft Builder Lodestone / 剖切仅作用于渲染内存', font=font(20), fill='#a8b9ce')
    for index, (direction, caption) in enumerate([('north', '北 / NORTH'), ('east', '东 / EAST'), ('south', '南 / SOUTH'), ('west', '西 / WEST')]):
        image = Image.open(ROOT / 'views' / f'{mode}-{direction}.png').convert('RGB').resize((900, 660), Image.Resampling.LANCZOS)
        offset = ((index % 2) * 900, 110 + (index // 2) * 690)
        sheet.paste(image, offset)
        draw.text((offset[0] + 20, offset[1] + 8), caption, font=font(24), fill='#ffffff', stroke_width=1, stroke_fill='#0a0e14')
    sheet.save(ROOT / 'views' / f'{mode}-sheet.png')

plan = Image.new('RGB', (1400, 1180), '#171c24')
draw = ImageDraw.Draw(plan)
draw.text((32, 18), 'P03 路线与失败恢复 · 模板俯视坐标', font=font(30), fill='#ffe7b5')
draw.text((32, 65), '四座 3×3 跳台；五次等高跳跃；失足落到玻璃安全面，返回西北阶梯', font=font(20), fill='#bbcadb')
scale = 29
origin = (80, 130)


def point(east, south):
    return (origin[0] + east * scale, origin[1] + south * scale)


def rectangle(east_min, south_min, east_max, south_max, color):
    draw.rectangle((*point(east_min, south_min), *point(east_max, south_max)), fill=color, outline='#91a0b0', width=1)


rectangle(0, 0, 27, 29, '#30333e')
rectangle(2, 2, 25, 27, '#90abb5')
rectangle(5, 6, 22, 23, '#dc6926')
rectangle(2, 0, 25, 6, '#64616a')
rectangle(2, 23, 25, 29, '#64616a')
for index, (east, south) in enumerate([(10, 7), (13, 11), (10, 15), (13, 19)]):
    rectangle(east, south, east + 3, south + 3, '#333846')
    draw.text(point(east + 0.8, south + 0.7), str(index + 1), font=font(28), fill='#ffd27d')
rectangle(5, 5, 8, 7, '#63d7b0')
route = json.loads((ROOT / 'qa/route.json').read_text(encoding='utf-8'))
for jump in route['jumps']:
    takeoff, landing = jump['takeoff'], jump['landing']
    draw.line([point(takeoff[0], takeoff[2]), point(landing[0], landing[2])], fill='#ffe0a3', width=6)
    for position in [takeoff, landing]:
        center = point(position[0], position[2])
        draw.ellipse((center[0] - 4, center[1] - 4, center[0] + 4, center[1] + 4), fill='#ffffff')
draw.line([point(9, 22), point(6.5, 22), point(6.5, 7.5), point(6.5, 4)], fill='#72ffcb', width=5)
for east in range(0, 28, 3):
    draw.text((point(east, 0)[0] - 5, 105), str(east), font=font(16), fill='#a8b9ce')
for south in range(0, 30, 3):
    draw.text((40, point(0, south)[1] - 9), str(south), font=font(16), fill='#a8b9ce')
draw.text((930, 150), '入口（北）', font=font(24), fill='#ffe7b5')
draw.text((930, 192), '[13,5,0]', font=font(22), fill='white')
draw.text((930, 260), '出口（南）', font=font(24), fill='#ffe7b5')
draw.text((930, 302), '[13,5,28]', font=font(22), fill='white')
draw.text((930, 380), '最长跳段 2.236 格', font=font(23), fill='white')
draw.text((930, 430), '台面脚位 y=5', font=font(23), fill='white')
draw.text((930, 480), '安全落点脚位 y=3', font=font(23), fill='#72ffcb')
draw.text((930, 530), '最大下落 2 格', font=font(23), fill='#72ffcb')
draw.text((930, 610), '橙：封闭熔岩 + 玻璃盖', font=font(20), fill='#ffb166')
draw.text((930, 655), '金：起跳 → 落点', font=font(20), fill='#ffe0a3')
draw.text((930, 700), '绿：失败恢复示意', font=font(20), fill='#72ffcb')
draw.text((930, 800), '尺寸 27×13×29', font=font(23), fill='white')
draw.text((930, 850), '入口出口 3 宽 × 4 高', font=font(20), fill='white')
draw.text((40, 1030), '图为路线说明；实体几何与原生碰撞报告见 room.nbt、qa/native.json。', font=font(22), fill='#a8b9ce')
draw.text((40, 1080), '所有坐标均为局部 XYZ 玩家脚部坐标。无需疾跑、药水、命令或传送恢复。', font=font(21), fill='#a8b9ce')
plan.save(ROOT / 'views/route-plan.png')
images = []
for image_path in sorted((ROOT / 'views').glob('*.png')):
    with Image.open(image_path) as image:
        image.load()
        images.append({'file': image_path.relative_to(ROOT).as_posix(), 'size': list(image.size), 'sha256': hashlib.sha256(image_path.read_bytes()).hexdigest(), 'decoded': True})
(ROOT / 'qa/image-decode.json').write_text(json.dumps({'status': 'PASS', 'source_sha256': hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest(), 'images': images}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('P03 sheets, route plan and image decode report created')
