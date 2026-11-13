import base64
import copy
import hashlib
import io
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'output/creature-import'
ASSETS = ROOT / 'src/main/resources/assets/ftdungeonmobs'
MODELS = [('幽灵尖啸.bbmodel', 'ghost_screamer'), ('矿石怪 - Converted.bbmodel', 'ore_monster'), ('骷髅商人.bbmodel', 'skeleton_merchant')]


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    manifest = []
    for filename, model_id in MODELS:
        original = (Path.home() / 'Downloads' / filename).read_bytes()
        (OUTPUT / filename).write_bytes(original)
        model = json.loads(original.decode('utf-8-sig'))
        textures = model['textures']
        images = [Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',', 1)[1]))).convert('RGBA') for texture in textures]
        used = sorted({face['texture'] for element in model['elements'] for face in element['faces'].values() if face.get('texture') is not None})
        width = max(images[index].width for index in used)
        height = sum(images[index].height for index in used)
        atlas = Image.new('RGBA', (width, height))
        offsets = {}
        offset_y = 0
        for index in used:
            offsets[index] = offset_y
            atlas.paste(images[index], (0, offset_y))
            offset_y += images[index].height
        for element in model['elements']:
            for face in element['faces'].values():
                index = face.get('texture')
                if index is None:
                    continue
                texture = textures[index]
                scale_x = images[index].width / texture.get('uv_width', images[index].width)
                scale_y = images[index].height / texture.get('uv_height', images[index].height)
                face['uv'] = [value * (scale_x if axis % 2 == 0 else scale_y) + (0 if axis % 2 == 0 else offsets[index]) for axis, value in enumerate(face['uv'])]
                face['texture'] = 0
        buffer = io.BytesIO()
        atlas.save(buffer, format='PNG')
        atlas_path = ASSETS / 'textures/entity' / (model_id + '.png')
        atlas_path.parent.mkdir(parents=True, exist_ok=True)
        atlas_path.write_bytes(buffer.getvalue())
        texture = copy.deepcopy(textures[0])
        texture.update(width=width, height=height, uv_width=width, uv_height=height, name=model_id + '.png', source='data:image/png;base64,' + base64.b64encode(buffer.getvalue()).decode())
        model['textures'] = [texture]
        model['resolution'] = dict(width=width, height=height)
        for animation, name in zip(model['animations'], ['idle', 'walk', 'trade' if model_id == 'skeleton_merchant' else 'attack']):
            animation['name'] = 'animation.' + model_id + '.' + name
        (OUTPUT / (model_id + '.bbmodel')).write_text(json.dumps(model, ensure_ascii=False), encoding='utf-8')
        for language, names in [('zh_cn', ['幽灵尖啸', '矿石怪', '骷髅商人']), ('en_us', ['Ghost Screamer', 'Ore Monster', 'Skeleton Merchant'])]:
            path = ASSETS / 'lang' / (language + '.json')
            translations = json.loads(path.read_text(encoding='utf-8-sig'))
            name = names[[entry[1] for entry in MODELS].index(model_id)]
            translations['entity.ftdungeonmobs.' + model_id] = name
            translations['item.ftdungeonmobs.' + model_id + '_spawn_egg'] = name + ('刷怪蛋' if language == 'zh_cn' else ' Spawn Egg')
            path.write_text(json.dumps(translations, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        egg_path = ASSETS / 'models/item' / (model_id + '_spawn_egg.json')
        egg_path.write_text('{"parent":"minecraft:item/template_spawn_egg"}\n', encoding='utf-8')
        manifest.append(dict(id=model_id, source=filename, source_sha256=hashlib.sha256(original).hexdigest(), cubes=len(model['elements']), groups=len(model['groups']), animations=len(model['animations']), atlas=[width, height], used_textures=used))
    (OUTPUT / 'manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(manifest, ensure_ascii=True))


if __name__ == '__main__':
    main()
