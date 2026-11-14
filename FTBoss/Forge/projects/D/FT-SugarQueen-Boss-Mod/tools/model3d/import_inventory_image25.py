import colorsys
import hashlib
import json
import re
from collections import deque
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT / '_work/item-icons-image25-20261026'
ASSETS = ROOT / 'src/main/resources/assets/ftboss'


def cutout(image, size=32):
    image = image.convert('RGBA')
    pixels = image.load()
    pending = deque((horizontal, vertical) for horizontal in range(image.width)
                    for vertical in (0, image.height - 1))
    pending.extend((horizontal, vertical) for vertical in range(image.height) for horizontal in (0, image.width - 1))
    seen = set()
    while pending:
        horizontal, vertical = pending.popleft()
        if not (0 <= horizontal < image.width and 0 <= vertical < image.height) or (horizontal, vertical) in seen:
            continue
        seen.add((horizontal, vertical))
        red, green, blue, alpha = pixels[horizontal, vertical]
        if red < 150 or blue < 140 or green > 105 or min(red, blue) - green < 85:
            continue
        pixels[horizontal, vertical] = (0, 0, 0, 0)
        pending.extend(((horizontal - 1, vertical), (horizontal + 1, vertical),
                        (horizontal, vertical - 1), (horizontal, vertical + 1)))
    for vertical in range(image.height):
        for horizontal in range(image.width):
            red, green, blue, alpha = pixels[horizontal, vertical]
            if red >= 225 and blue >= 225 and green <= 45 and abs(red - blue) < 32:
                pixels[horizontal, vertical] = (0, 0, 0, 0)
    bounds = image.getbbox()
    assert bounds, 'Empty source cell'
    image = image.crop(bounds)
    image.thumbnail((size - 4, size - 4), Image.Resampling.NEAREST)
    result = Image.new('RGBA', (size, size))
    result.alpha_composite(image, ((size - image.width) // 2, (size - image.height) // 2))
    return result


def recolor(image, hue, saturation):
    result = image.copy()
    for vertical in range(32):
        for horizontal in range(32):
            red, green, blue, alpha = result.getpixel((horizontal, vertical))
            if not alpha or red < 55 or red <= green * 1.07 or green <= blue * 1.15:
                continue
            value = max(red, green, blue) / 255
            color = colorsys.hsv_to_rgb(hue, saturation, value)
            result.putpixel((horizontal, vertical), (*[round(channel * 255) for channel in color], alpha))
    return result


def arrow(image, stage):
    result = image.copy()
    draw = ImageDraw.Draw(result)
    start = 11 - stage
    end = 21 + stage
    draw.line((start - 1, 32 - start, end, 31 - end), fill='#272135', width=3)
    draw.line((start, 31 - start, end, 31 - end), fill='#E9D8AE', width=1)
    draw.polygon(((end + 1, 29 - end), (end + 1, 33 - end), (end - 3, 29 - end)), fill='#C4EEF0')
    draw.line((start - 2, 31 - start, start, 33 - start), fill='#B35668', width=2)
    return result


def json_file(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def inventory_identity_from_sheet(identity):
    return {'dora_nova_launcher': 'dora_pulse_gun', 'dora_pulse_gun': 'dora_nova_launcher'}.get(identity, identity)


def main():
    sheets = json.loads((WORK / 'sheets.json').read_text(encoding='utf-8'))
    audit = json.loads((WORK / 'audit.json').read_text(encoding='utf-8'))
    sprites = {}
    provenance = {}
    for sheet in sheets:
        path = ROOT / f"output/imagegen/ft-items-{sheet['name']}-20261026.png"
        image = Image.open(path).convert('RGBA')
        cells = sheet['cells']
        if sheet['name'] == 'statues_eggs':
            cells = [cell for cell in cells if cell[0] != 'statue_youzi']
        for index, (identity, description) in enumerate(cells):
            identity = inventory_identity_from_sheet(identity)
            horizontal, vertical = index % 4, index // 4
            cell = image.crop((round(horizontal * image.width / 4), round(vertical * image.height / 4),
                               round((horizontal + 1) * image.width / 4), round((vertical + 1) * image.height / 4)))
            sprites[identity] = cutout(cell)
            provenance[identity] = dict(sheet=path.relative_to(ROOT).as_posix(), cell=index,
                                        source_sha256=hashlib.sha256(path.read_bytes()).hexdigest())
    sprites['statue_youzi'] = recolor(sprites['statue_saya'], 0.12, 0.18)
    provenance['statue_youzi'] = dict(derived_from='statue_saya', adjustment='pale warm stone palette')
    aliases = {'butcher_statue': 'statue_butcher', 'statue': 'statue_blank', 'compost_crown': 'rainbow_spore_charm',
               'dora_nova_launcher_v1': 'dora_pulse_gun', 'dora_smg_v1': 'dora_nova_launcher'}
    for identity, source in aliases.items():
        sprites[identity] = sprites[source].copy()
        provenance[identity] = dict(derived_from=source, adjustment='legacy alias')
    correction = ROOT / 'output/imagegen/ft-ui-expanded-components-20261026.png'
    if correction.exists():
        sheet = Image.open(correction).convert('RGBA')
        for identity, index in (('statue_youzi', 12), ('compost_crown', 13)):
            horizontal, vertical = index % 4, index // 4
            sprites[identity] = cutout(sheet.crop((round(horizontal * sheet.width / 4), round(vertical * sheet.height / 4),
                                                 round((horizontal + 1) * sheet.width / 4), round((vertical + 1) * sheet.height / 4))))
            provenance[identity] = dict(sheet=correction.relative_to(ROOT).as_posix(), cell=index,
                                       source_sha256=hashlib.sha256(correction.read_bytes()).hexdigest())
    materials = {'wooden': None, 'stone': (0.10, 0.10), 'iron': (0.57, 0.12), 'golden': (0.12, 0.78),
                 'diamond': (0.51, 0.67), 'netherite': (0.76, 0.25)}
    for material, palette in materials.items():
        for weapon in ('bow', 'crossbow'):
            identity = material + '_' + weapon
            source = sprites['wooden_' + weapon]
            sprites[identity] = recolor(source, *palette) if palette else source.copy()
            provenance[identity] = dict(derived_from='wooden_' + weapon, material=material)
            if weapon == 'bow':
                for stage in range(3):
                    variant = identity + '_pulling_' + str(stage)
                    sprites[variant] = arrow(sprites[identity], stage)
                    provenance[variant] = dict(derived_from=identity, pull_stage=stage)
    for material in (*materials, 'barrel'):
        identity = material + '_crossbow'
        sprites[identity + '_charged'] = arrow(sprites[identity], 2)
        provenance[identity + '_charged'] = dict(derived_from=identity, adjustment='loaded arrow')
    sprites['barrel_crossbow_ready'] = sprites['barrel_crossbow_charged'].copy()
    provenance['barrel_crossbow_ready'] = dict(derived_from='barrel_crossbow_charged')
    disc_sources = {'apple': 'golden_apple_brooch', 'butcher': 'butcher_fang_charm', 'benger': 'benger_spawn_egg',
                    'compost': 'rainbow_spore_charm', 'defect': 'defect_core', 'gingerbread': 'sugar_queen_crystal',
                    'rat': 'rat_bait', 'saya': 'tea_seed', 'tavern': 'aged_wine_seal', 'tide_fish': 'tide_pearl',
                    'manor_battle': 'shadow_sword', 'manor_cue': 'silver_key', 'manor_dual': 'qingyi_confluence_trident',
                    'manor_guignol': 'qizai_prayer_staff'}
    for row in audit:
        identity = row['id']
        if not identity.startswith('music_disc_'):
            continue
        theme = identity.removeprefix('music_disc_').removesuffix('_p2')
        result = recolor(sprites['music_disc'], (sum(identity.encode()) % 255) / 255, 0.65)
        emblem = sprites[disc_sources[theme]].resize((10, 10), Image.Resampling.NEAREST)
        result.alpha_composite(emblem, (11, 11))
        if identity.endswith('_p2'):
            ImageDraw.Draw(result).line((20, 21, 24, 17), fill='#E2EDF0', width=2)
        sprites[identity] = result
        provenance[identity] = dict(derived_from='music_disc', emblem=disc_sources[theme], second_phase=identity.endswith('_p2'))
    expected = {row['id'] for row in audit} - {'black_cleaver'}
    assert expected <= sprites.keys(), f'Missing sprites: {expected - sprites.keys()}'
    java = (ROOT / 'src/main/java/net/farmingtales/ftboss/client/model/InventoryItemIconModel.java').read_text(encoding='utf-8')
    definition = java.split('static final String[] IDS = (', 1)[1].split(').split', 1)[0]
    registered = set(''.join(re.findall(r'"([^"]*)"', definition)).split())
    assert registered == expected, 'Client registration does not match audited model set'
    exported = sorted(expected | {material + '_crossbow_charged' for material in (*materials, 'barrel')})
    for identity in exported:
        image = sprites[identity]
        assert image.mode == 'RGBA' and image.size == (32, 32)
        assert set(image.getchannel('A').get_flattened_data()) <= {0, 255}
        assert image.getbbox() and min(image.getbbox()[:2]) >= 1 and max(image.getbbox()[2:]) <= 31
        texture = ASSETS / 'textures/item/inventory_redraw' / (identity + '.png')
        texture.parent.mkdir(parents=True, exist_ok=True)
        image.save(texture)
        json_file(ASSETS / 'models/item/inventory_redraw' / (identity + '.json'),
                  dict(parent='minecraft:item/generated', gui_light='front',
                       textures=dict(layer0='ftboss:item/inventory_redraw/' + identity)))
    json_file(WORK / 'import-manifest.json', dict(model='gpt-image-2.5-flare', excluded=['black_cleaver'],
                                               source_sheets=6, exported_icons=len(exported), identities=exported,
                                               provenance={identity: provenance[identity] for identity in exported}))
    render(sprites, audit, expected)
    print(f'PASS: {len(expected)} model identities + 7 charged variants; original models and UV textures untouched')


def render(sprites, audit, expected):
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 13)
    rows = [row for row in audit if row['id'] in expected]
    sheet = Image.new('RGB', (1400, ((len(rows) + 9) // 10) * 127 + 38), '#1C202A')
    draw = ImageDraw.Draw(sheet)
    draw.text((12, 8), 'FT 物品栏重绘 · image2.5 衍生图标 · 黑切未改 · 离线素材预览', font=font, fill='#F5EBD6')
    for index, row in enumerate(rows):
        horizontal, vertical = index % 10 * 140, index // 10 * 127 + 38
        draw.rectangle((horizontal + 28, vertical + 3, horizontal + 111, vertical + 86), fill='#393B43', outline='#63606A')
        image = sprites[row['id']].resize((80, 80), Image.Resampling.NEAREST)
        sheet.paste(image, (horizontal + 30, vertical + 5), image)
        draw.text((horizontal + 3, vertical + 91), row['label'][:9], font=font, fill='#E9E5DD')
    sheet.save(WORK / 'after-contact-sheet.png')
    selected = ['shadow_sword', 'candy_cane', 'apple_stem', 'puffer_bat', 'tide_trident', 'cheese_wheel_mace',
                'dora_lance', 'dora_pulse_gun', 'qingyi_confluence_trident', 'qizai_prayer_staff', 'xueli_rolling_pin', 'xueli_skillet',
                'sugar_queen_crystal', 'forbidden_apple', 'tea_seed', 'defect_core', 'golden_apple_brooch', 'rainbow_spore_charm',
                'thorn_chestplate', 'thorn_boots', 'statue_sugar_queen', 'statue_rat_swarm', 'easter_egg_qingyi', 'silver_key']
    hero = Image.new('RGB', (1200, 540), '#191D26')
    draw = ImageDraw.Draw(hero)
    draw.text((18, 12), 'FT ITEM ICONS / image2.5 / Black Cleaver excluded / OFFLINE ASSET PREVIEW',
              font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 17), fill='#E8DBBD')
    labels = {row['id']: row['label'] for row in rows}
    for index, identity in enumerate(selected):
        horizontal, vertical = index % 6 * 200, index // 6 * 122 + 42
        image = sprites[identity].resize((96, 96), Image.Resampling.NEAREST)
        hero.paste(image, (horizontal + 52, vertical), image)
        draw.text((horizontal + 8, vertical + 97), labels[identity][:12], font=font, fill='#E4E0D5')
    hero.save(WORK / 'selected-preview.png')


if __name__ == '__main__':
    main()
