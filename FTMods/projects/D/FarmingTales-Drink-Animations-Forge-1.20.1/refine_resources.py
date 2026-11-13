"""Retain the original stopper geometry/UVs and recolor its texture for the open state."""
import copy
import json
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parent
assets = root / 'src/main/resources/assets/farmingtales_drink_anim'
original = root.parents[2] / 'kubejs/assets/farmingtales/models/block/drinks/orange_wine.json'
model = json.loads(original.read_text(encoding='utf-8-sig'))
model['textures'] = {key: value.replace('farmingtales:block/drinks/orange_wine', 'farmingtales_drink_anim:orange_wine') for key, value in model['textures'].items()}
neck = model['elements'][1]
cork = copy.deepcopy(neck)
model['elements'] = [model['elements'][i] for i in (0, 2, 3, 5, 7)]
stopper = copy.deepcopy(model)
stopper['elements'] = [cork]
mouth = copy.deepcopy(model)
mouth['elements'] = [copy.deepcopy(neck)]
mouth['textures'] = {key:'farmingtales_drink_anim:orange_wine_open' for key in mouth['textures']}
mouth['render_type'] = 'minecraft:translucent'
texture=Image.open(assets/'textures/orange_wine.png').convert('RGBA')
glass_dark,glass_light=texture.getpixel((0,0)),texture.getpixel((1,1))
for x0,y0,x1,y1 in [(11,3,13,8),(12,8,14,10)]:
    pixels=[texture.getpixel((x,y)) for x in range(x0,x1) for y in range(y0,y1)]
    luminances=[.2126*r+.7152*g+.0722*b for r,g,b,a in pixels]
    low,high=min(luminances),max(luminances)
    for x in range(x0,x1):
        for y in range(y0,y1):
            r,g,b,a=texture.getpixel((x,y))
            t=(.2126*r+.7152*g+.0722*b-low)/max(1,high-low)
            color=tuple(round(glass_dark[c]+(glass_light[c]-glass_dark[c])*t) for c in range(3))
            texture.putpixel((x,y),(*color,217 if a else 0))
texture.save(assets/'textures/orange_wine_open.png')
for name, data in [('orange_bottle', model), ('orange_cork', stopper), ('orange_open_neck', mouth)]:
    (assets / 'models/animation' / (name + '.json')).write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
atlas=root/'src/main/resources/assets/minecraft/atlases/blocks.json'
atlas.write_text(json.dumps({'sources':[{'type':'single','resource':'farmingtales_drink_anim:'+name,'sprite':'farmingtales_drink_anim:'+name} for name in ('orange_wine','orange_wine_open')]}),encoding='utf-8')
sounds = json.loads((assets / 'sounds.json').read_text(encoding='utf-8'))
sounds['cork'] = {'subtitle': 'subtitles.farmingtales_drink_anim.cork', 'sounds': ['farmingtales_drink_anim:cork']}
sounds['cork_pull'] = {'subtitle': 'subtitles.farmingtales_drink_anim.cork_pull', 'sounds': ['farmingtales_drink_anim:cork_pull']}
sounds['gulp']['sounds'] = [{'name':'minecraft:random/drink','volume':.55}]
sounds['swallow']['sounds'] = [{'name':'minecraft:random/drink','volume':.25,'pitch':.86}]
sounds['slosh']['sounds'] = [{'name':'minecraft:item/bottle/empty1','volume':.30}, {'name':'minecraft:item/bottle/empty2','volume':.30}]
sounds['cloth']['sounds'] = [{'name':'minecraft:step/cloth1','volume':.3}, {'name':'minecraft:step/cloth3','volume':.3}]
(assets / 'sounds.json').write_text(json.dumps(sounds, indent=2) + '\n', encoding='utf-8')
for lang, subtitle in [('zh_cn', '瓶塞：拔出'), ('en_us', 'Bottle uncorks')]:
    path = assets / 'lang' / (lang + '.json')
    data = json.loads(path.read_text(encoding='utf-8'))
    data['subtitles.farmingtales_drink_anim.cork'] = subtitle
    data['subtitles.farmingtales_drink_anim.cork_pull'] = '木塞：摩擦' if lang=='zh_cn' else 'Cork rubs'
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
