import base64
import copy
import importlib.util
import io
import json
import zipfile
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
SOURCE = Path('D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1')
ASSETS = SOURCE / 'src/main/resources/assets/ftdungeonmobs'
PACK = ROOT / 'resourcepack'
TARGET = PACK / 'assets/ftdungeonmobs'
NAME = 'ft_teleport_gate_cyan'

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')

def recolor(image, kind):
    result = image.copy()
    for pixel_y in range(result.height):
        for pixel_x in range(result.width):
            red, green, blue, alpha = result.getpixel((pixel_x, pixel_y))
            luminance = (red * .299 + green * .587 + blue * .114) / 255
            tile = pixel_x // 16 + pixel_y // 16 * 8
            palette = (183, 211, 237)
            if kind == 'body' and tile in (7, 8):
                palette = (230, 245, 255)
            elif kind == 'body' and tile in (13, 14, 15, 16):
                palette = (95, 255, 295)
            elif kind != 'body':
                palette = (105, 280, 330) if kind == 'flame' else (70, 320, 390)
            result.putpixel((pixel_x, pixel_y), (*[min(255, int(luminance * value)) for value in palette], alpha))
    return result

def main():
    texture_dir = TARGET / 'textures/block'
    texture_dir.mkdir(parents=True, exist_ok=True)
    preview_dir = ROOT / 'preview'
    preview_dir.mkdir(exist_ok=True)
    files = {'body': 'dungeon_gate.png', 'portal': 'dungeon_return_gate_portal.png', 'flame': 'dungeon_gate_flame.png'}
    originals = {kind: Image.open(ASSETS / 'textures/block' / filename).convert('RGBA') for kind, filename in files.items()}
    textures = {kind: recolor(image, kind) for kind, image in originals.items()}
    for kind, image in textures.items():
        image.save(texture_dir / f'{NAME}_{kind}.png')
        if kind != 'body':
            write_json(texture_dir / f'{NAME}_{kind}.png.mcmeta', json.loads((ASSETS / 'textures/block' / (files[kind] + '.mcmeta')).read_text()))
    baseline = json.loads((SOURCE / 'model_sources/dungeon_gates/dungeon_return_gate.bbmodel').read_text(encoding='utf-8'))
    candidate = copy.deepcopy(baseline)
    candidate['name'] = NAME
    for texture, kind in zip(candidate['textures'], ('body', 'portal', 'flame')):
        buffer = io.BytesIO()
        textures[kind].crop((0, 0, textures[kind].width, textures[kind].width)).save(buffer, format='PNG')
        texture.update(name=f'{NAME}_{kind}.png', source='data:image/png;base64,' + base64.b64encode(buffer.getvalue()).decode(), saved=False)
    write_json(ROOT / f'{NAME}.bbmodel', candidate)
    assert candidate['elements'] == baseline['elements'] and candidate['outliner'] == baseline['outliner']
    exported = []
    for relative in [*(f'models/block/dungeon_return_gate_{part}.json' for part in range(12)), 'models/item/dungeon_return_gate.json']:
        original = json.loads((ASSETS / relative).read_text(encoding='utf-8'))
        model = copy.deepcopy(original)
        model['textures'] = {kind: f'ftdungeonmobs:block/{NAME}_{kind}' for kind in ('body', 'portal', 'flame')}
        model['textures']['particle'] = model['textures']['body']
        for element in model['elements']:
            for face in element['faces'].values():
                if face.get('texture') == '#portal':
                    face.pop('tintindex', None)
        write_json(TARGET / relative, model)
        exported.append({'path': relative, 'elements': len(model['elements']), 'faces': sum(len(element['faces']) for element in model['elements'])})
    write_json(PACK / 'pack.mcmeta', {'pack': {'pack_format': 15, 'description': 'FT return gate - cyan teleport palette / 1.20.1'}})
    write_json(ROOT / 'verification.json', {'editable_elements': len(candidate['elements']), 'geometry_identical': True, 'uv_identical': True, 'added_geometry': 0, 'exported': exported, 'textures': {kind: list(image.size) for kind, image in textures.items()}, 'animation_frames': 16, 'ticks_per_frame': 3, 'minecraft_tested': False, 'fps_measured': False})
    spec = importlib.util.spec_from_file_location('gate_preview', SOURCE / 'tools/build-dungeon-gates.py')
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    boxes = helper.boxes(True)
    opened = [box for box in boxes if not helper.moving(box)]
    material = dict(textures, portal_tint=(255, 255, 255))
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 23)
    sheet = Image.new('RGB', (1500, 790), '#12151d')
    drawing = ImageDraw.Draw(sheet)
    panels = [('FT 原版 / 开启态', opened, dict(originals, portal_tint=(85, 204, 235))), ('青蓝传送门 / 开启态', opened, material), ('青蓝传送门 / 闭合态', boxes, material)]
    for index, (label, geometry, colors) in enumerate(panels):
        panel = helper.render_model(geometry, colors, 'isometric')
        panel.thumbnail((480, 650), Image.Resampling.NEAREST)
        sheet.paste(panel, (index * 500 + (500 - panel.width) // 2, 85))
        drawing.text((index * 500 + 22, 30), label, fill='#bdebf5', font=font)
    drawing.text((30, 735), '保留原结构与 UV / 冷灰石框、冰骨白颅、青蓝魂焰 / 软件预览，非游戏截图', font=font, fill='#a2adbd')
    sheet.save(preview_dir / 'comparison.png')
    views = Image.new('RGB', (1540, 1120), '#12151d')
    drawing = ImageDraw.Draw(views)
    for row, geometry in enumerate((opened, boxes)):
        for column, view in enumerate(('front', 'back', 'left', 'right')):
            panel = helper.render_model(geometry, material, view)
            views.paste(panel, (column * 385 + (385 - panel.width) // 2, row * 555 + 45))
            drawing.text((column * 385 + 20, row * 555 + 10), ('OPEN / ' if row == 0 else 'CLOSED / ') + view, font=font, fill='#bdebf5')
    views.save(preview_dir / 'orthographic.png')
    frames = []
    for frame_index in range(16):
        animated = dict(material)
        for kind in ('portal', 'flame'):
            animated[kind] = textures[kind].crop((0, frame_index * 16, 16, frame_index * 16 + 16))
        frames.append(helper.render_model(opened, animated, 'front'))
    frames[0].save(preview_dir / 'portal_animation.gif', save_all=True, append_images=frames[1:], duration=150, loop=0, disposal=2)
    with zipfile.ZipFile(ROOT / 'FT_青蓝传送地牢门_可选资源包_1.20.1.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(PACK.rglob('*')):
            if path.is_file():
                archive.write(path, path.relative_to(PACK).as_posix())
    print('Created', ROOT, 'elements:', len(candidate['elements']))

if __name__ == '__main__':
    main()
