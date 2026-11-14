import json
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont


DIRECTORY = Path(sys.argv[1]).resolve()
POSES = json.loads((DIRECTORY / 'render-poses.json').read_text(encoding='utf-8'))
TEXTURE = np.asarray(Image.open(DIRECTORY / 'sugar_queen_v3.png').convert('RGBA'))
FACE_CORNERS = {
    'north': [6, 2, 0, 4], 'south': [3, 7, 5, 1],
    'east': [7, 6, 4, 5], 'west': [2, 3, 1, 0],
    'up': [7, 3, 2, 6], 'down': [4, 0, 1, 5],
}


def render(pose, azimuth, elevation, size=(620, 760), scale=14, target=(0, 22, 0)):
    azimuth, elevation = np.radians([azimuth, elevation])
    camera = np.array([math.sin(azimuth) * math.cos(elevation), math.sin(elevation), -math.cos(azimuth) * math.cos(elevation)])
    right = np.array([math.cos(azimuth), 0, math.sin(azimuth)])
    up = np.cross(right, camera)
    light = np.array([-0.35, 0.75, -0.6])
    light /= np.linalg.norm(light)
    width, height = size
    pixels = np.zeros((height, width, 4), dtype=np.uint8)
    depth = np.full((height, width), -np.inf)
    for element in POSES[pose]:
        corners = np.array(element['corners'])
        center = corners.mean(axis=0)
        for face, indices in FACE_CORNERS.items():
            vertices = corners[indices]
            normal = np.cross(vertices[1] - vertices[0], vertices[2] - vertices[0])
            normal /= np.linalg.norm(normal)
            if np.dot(normal, vertices.mean(axis=0) - center) < 0:
                normal = -normal
            if np.dot(normal, camera) <= 0.00001:
                continue
            shade = 0.66 + 0.34 * max(0, np.dot(normal, light))
            shifted = vertices - target
            screen = np.column_stack((width / 2 + shifted @ right * scale, height / 2 - shifted @ up * scale, vertices @ camera))
            area = element['uv'][face]
            uv_origin, uv_size = np.array(area['uv']), np.array(area['uv_size'])
            texture_coordinates = np.array([[0, 0], [1, 0], [1, 1], [0, 1]]) * uv_size + uv_origin
            for triangle in ([0, 1, 2], [0, 2, 3]):
                points = screen[triangle]
                minimum = np.maximum([0, 0], np.floor(points[:, :2].min(axis=0)).astype(int))
                maximum = np.minimum([width - 1, height - 1], np.ceil(points[:, :2].max(axis=0)).astype(int))
                if np.any(maximum < minimum):
                    continue
                horizontal, vertical = np.meshgrid(np.arange(minimum[0], maximum[0] + 1) + 0.5, np.arange(minimum[1], maximum[1] + 1) + 0.5)
                first, second, third = points
                divisor = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
                if abs(divisor) < 0.00001:
                    continue
                weight_first = ((second[1] - third[1]) * (horizontal - third[0]) + (third[0] - second[0]) * (vertical - third[1])) / divisor
                weight_second = ((third[1] - first[1]) * (horizontal - third[0]) + (first[0] - third[0]) * (vertical - third[1])) / divisor
                weight_third = 1 - weight_first - weight_second
                distance = weight_first * first[2] + weight_second * second[2] + weight_third * third[2]
                region = np.s_[minimum[1]:maximum[1] + 1, minimum[0]:maximum[0] + 1]
                mask = (weight_first >= -0.000001) & (weight_second >= -0.000001) & (weight_third >= -0.000001) & (distance > depth[region])
                uv_triangle = texture_coordinates[triangle]
                coordinates = weight_first[..., None] * uv_triangle[0] + weight_second[..., None] * uv_triangle[1] + weight_third[..., None] * uv_triangle[2]
                coordinates = np.floor(coordinates).astype(int)
                coordinates[..., 0] = np.clip(coordinates[..., 0], 0, TEXTURE.shape[1] - 1)
                coordinates[..., 1] = np.clip(coordinates[..., 1], 0, TEXTURE.shape[0] - 1)
                colors = TEXTURE[coordinates[..., 1], coordinates[..., 0]].copy()
                mask &= colors[..., 3] > 127
                colors[..., :3] = np.clip(colors[..., :3] * shade, 0, 255).astype(np.uint8)
                pixels[region][mask] = colors[mask]
                depth[region][mask] = distance[mask]
    return Image.fromarray(pixels)


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


hero = render('idle', -30, 10, (740, 900), 17.2)
hero.save(DIRECTORY / 'hero.png')
front = render('idle', 0, 0, (440, 680), 13.3)
back = render('idle', 180, 0, (440, 680), 13.3)
side = render('idle', 90, 0, (440, 680), 13.3)
cast = render('cast', -25, 10, (700, 760), 14.5)
render('cast', -90, 0, (640, 760), 14.5).save(DIRECTORY / 'cast-side.png')
for name, image in [('front', front), ('back', back), ('side', side), ('cast', cast)]:
    image.save(DIRECTORY / (name + '.png'))

board = Image.new('RGB', (1760, 1100), '#1c2029')
draw = ImageDraw.Draw(board)
draw.rectangle((36, 36, 1724, 1064), outline='#565160', width=1)
draw.text((70, 58), '糖霜女王', font=font(54), fill='#f7e7ca')
draw.text((73, 131), 'SUGAR QUEEN  /  PATISSERIE SOVEREIGN', font=font(18), fill='#d5a8b9')
draw.text((73, 173), '奶油糖霜 · 莓果礼裙 · 糖晶王冠', font=font(23), fill='#c9c8cf')
draw.line((745, 75, 745, 1010), fill='#464550', width=1)
board.paste(hero, (13, 190), hero)
for position, image, label in [((765, 220), front, '正面'), ((1210, 220), back, '背面')]:
    board.paste(image, position, image)
    draw.text((position[0] + 183, 890), label, font=font(24), fill='#e8d7bf')
draw.text((808, 948), '33 骨骼  /  209 方块  /  256 × 256 像素图集', font=font(23), fill='#e5becd')
draw.text((808, 990), '实际模型离线渲染 · 非游戏截图', font=font(19), fill='#a7a7b3')
for index, color in enumerate(['#f9edd3', '#eaa2b4', '#c65c85', '#6d284d', '#e2b263', '#a3dfdd']):
    draw.rectangle((1410 + index * 41, 116, 1435 + index * 41, 141), fill=color)
board.save(DIRECTORY / 'sugar-queen-design.png')

contact = Image.new('RGB', (1860, 1360), '#20232b')
labels = [('idle', '待机', 0), ('walk', '行进', -20), ('cast', '施法', -25), ('slam', '杖击', -25), ('hurt', '受击', 0), ('death', '倒下', -30)]
for index, (pose, label, angle) in enumerate(labels):
    target = (0, 10, 16) if pose == 'death' else (0, 22, 0)
    image = render(pose, angle, 18 if pose == 'death' else 8, (620, 620), 10, target)
    left, top = (index % 3) * 620, (index // 3) * 680
    contact.paste(image, (left, top), image)
    ImageDraw.Draw(contact).text((left + 268, top + 625), label, font=font(24), fill='#e8d7bf')
contact.save(DIRECTORY / 'animation-poses.png')
if 'raise_0' in POSES:
    stages = [('raise_0', '准备'), ('raise_0.24', '肩部外展'), ('raise_0.5', '向前抬手'), ('raise_0.8', '施法姿态'), ('cast_hold', '持续施法'), ('cast_return', '收手过渡')]
    review = Image.new('RGB', (1920, 1080), '#20232b')
    for index, (pose, label) in enumerate(stages):
        for row, angle in enumerate([-25, -90]):
            image = render(pose, angle, 4, (320, 480), 11.5, (0, 29, 0))
            left, top = index * 320, row * 540
            review.paste(image, (left, top), image)
            ImageDraw.Draw(review).text((left + 75, top + 487), label, font=font(22), fill='#e8d7bf')
    review.save(DIRECTORY / 'arm-motion-review.png')
print('Rendered textured model: design board, six action poses, and transparent views.')
