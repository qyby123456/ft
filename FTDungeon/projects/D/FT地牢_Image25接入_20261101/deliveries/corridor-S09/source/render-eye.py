import importlib.util
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw

from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]
specification = importlib.util.spec_from_file_location('s09_render', ROOT / 'source/render-s09.py')
render_module = importlib.util.module_from_spec(specification)
specification.loader.exec_module(render_module)
review = render_module.review


def solve(matrix):
    for column in range(8):
        pivot = max(range(column, 8), key=lambda row: abs(matrix[row][column]))
        matrix[column], matrix[pivot] = matrix[pivot], matrix[column]
        scale = matrix[column][column]
        if abs(scale) < 1e-9:
            raise ValueError('Degenerate perspective face')
        matrix[column] = [value / scale for value in matrix[column]]
        for row in range(8):
            if row == column:
                continue
            scale = matrix[row][column]
            matrix[row] = [matrix[row][index] - scale * matrix[column][index] for index in range(9)]
    return tuple(matrix[index][8] for index in range(8))


def draw_face(canvas, texture, points):
    minimum_x = max(0, math.floor(min(point[0] for point in points)))
    maximum_x = min(canvas.width, math.ceil(max(point[0] for point in points)))
    minimum_y = max(0, math.floor(min(point[1] for point in points)))
    maximum_y = min(canvas.height, math.ceil(max(point[1] for point in points)))
    if maximum_x <= minimum_x or maximum_y <= minimum_y:
        return
    matrix = []
    corners = [(0, 0), (texture.width, 0), (texture.width, texture.height), (0, texture.height)]
    local = [(point[0] - minimum_x, point[1] - minimum_y) for point in points]
    for (pixel_x, pixel_y), (tex_x, tex_y) in zip(local, corners):
        matrix.append([pixel_x, pixel_y, 1, 0, 0, 0, -tex_x * pixel_x, -tex_x * pixel_y, tex_x])
        matrix.append([0, 0, 0, pixel_x, pixel_y, 1, -tex_y * pixel_x, -tex_y * pixel_y, tex_y])
    try:
        coefficients = solve(matrix)
    except ValueError:
        return
    warped = texture.transform((maximum_x - minimum_x, maximum_y - minimum_y), Image.Transform.PERSPECTIVE,
                               coefficients, Image.Resampling.NEAREST)
    mask = Image.new('L', warped.size)
    ImageDraw.Draw(mask).polygon(local, fill=255)
    warped.putalpha(Image.composite(warped.getchannel('A'), Image.new('L', warped.size), mask))
    canvas.alpha_composite(warped, (minimum_x, minimum_y))


def main():
    structure = Structure.read(ROOT / 'room.nbt')
    size = (1536, 1024)
    eye = (5.5, 2.62, 1.0)
    focal = 840
    center = (768, 478)
    canvas = Image.new('RGBA', size, '#0c1115')
    faces = []
    for position, block in structure.blocks.items():
        if position[2] < 2 or block.name.removeprefix('minecraft:') in review.INVISIBLE:
            continue
        for points, normal, cull, identifier, uv, rotation in review.geometry(block.name.removeprefix('minecraft:'), block.properties):
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            midpoint = tuple(sum(point[axis] for point in world) / len(world) for axis in range(3))
            if sum(normal[axis] * (eye[axis] - midpoint[axis]) for axis in range(3)) <= 0.0001:
                continue
            if cull and review.opaque(structure.blocks.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            if min(point[2] for point in world) <= eye[2] + 0.1:
                continue
            projected = [(center[0] + focal * (point[0] - eye[0]) / (point[2] - eye[2]),
                          center[1] - focal * (point[1] - eye[1]) / (point[2] - eye[2])) for point in world]
            distance = midpoint[2] - eye[2]
            brightness = 0.68 + 0.12 * max(0, normal[1])
            if identifier.startswith('block/soul_lantern') or identifier == 'block/shroomlight':
                brightness = 0.95
            texture = review.renderer.texture(identifier, uv, rotation, brightness)
            faces.append((distance, texture, projected))
    for distance, texture, projected in sorted(faces, key=lambda face: face[0], reverse=True):
        draw_face(canvas, texture, projected)
    draw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 0, size[0], 72), fill='#101a20')
    draw.text((28, 18), 'S09 焚痕蛛巢短廊  ·  16 格一字贯通', font=review.font(29), fill='#e3ece7')
    draw.rectangle((0, size[1] - 58, size[0], size[1]), fill='#101a20')
    draw.text((28, size[1] - 43), '最终 NBT 方块模型软件透视图 / 非游戏截图 / 保留完整屋顶 / 主路 3 宽 × 3 高', font=review.font(22), fill='#91b7bc')
    canvas.convert('RGB').save(ROOT / 'views/eye-north.png')
    canvas.convert('RGB').save(ROOT / 'preview.png')
    print(json.dumps({'eye_view': 'views/eye-north.png', 'preview': 'preview.png', 'visible_faces': len(faces)}))


if __name__ == '__main__':
    main()
