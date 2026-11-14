import io
import json
from pathlib import Path
import sys
import zipfile
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[3]
INSTANCE = Path.home() / 'AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2'
OUTPUT = ROOT / '_work/statue-weapon-bond-guide-20261020/preview'
OUTPUT.mkdir(parents=True, exist_ok=True)
CATALOG = json.loads((ROOT / 'src/main/resources/assets/ftboss/weapon_bonds/guide.json').read_text(encoding='utf-8'))
LANG = json.loads((ROOT / 'src/main/resources/assets/ftboss/lang/zh_cn.json').read_text(encoding='utf-8'))
PREFIX = 'guide.ftboss.bonds.'
TEXT = '#E4EAF0'
MUTED = '#ACC1CF'
ACCENT = '#E9C57F'
GREEN = '#94DDB3'
FONT = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 10)
archives = []
for filename in [INSTANCE / 'FarmingTales_ForgeⅡ v1.5.2.jar', *sorted((INSTANCE / 'mods').glob('*.jar'))]:
    archives.append(zipfile.ZipFile(filename))


def resource(name):
    for archive in archives:
        try:
            return archive.read(name)
        except KeyError:
            pass
    raise AssertionError('Missing runtime resource: ' + name)


def icon(item):
    namespace, name = item.split(':')
    model = json.loads(resource(f'assets/{namespace}/models/item/{name}.json'))
    textures = model.get('textures', {})
    if not textures and model.get('parent', '').startswith(namespace + ':'):
        parent = model['parent'].split(':')[1]
        textures = json.loads(resource(f'assets/{namespace}/models/{parent}.json')).get('textures', {})
    location = textures.get('layer0') or textures.get('all') or textures.get('side')
    if not location:
        raise AssertionError('Preview needs model texture: ' + item)
    texture_namespace, texture_path = location.split(':') if ':' in location else ('minecraft', location)
    image = Image.open(io.BytesIO(resource(f'assets/{texture_namespace}/textures/{texture_path}.png'))).convert('RGBA')
    if image.height > image.width:
        image = image.crop((0, 0, image.width, image.width))
    return image.resize((24, 24), Image.Resampling.NEAREST)


def text(key):
    return LANG[PREFIX + key]


def fitted(value, width):
    while value and FONT.getlength(value) > width:
        value = value[:-1]
    return value


def wrapped(value, width):
    lines = []
    current = ''
    for character in value:
        if current and FONT.getlength(current + character) > width:
            lines.append(current)
            current = ''
        current += character
    if current:
        lines.append(current)
    return lines


def render(page_id, width=854, height=480, at_end=False):
    entry = next(entry for entry in CATALOG if entry['id'] == page_id)
    panel_width = min(620, width - 12)
    panel_height = min(420, height - 12)
    left = (width - panel_width) // 2
    top = (height - panel_height) // 2
    viewport = panel_height - 115
    viewport_top = top + 82
    image = Image.new('RGB', (width, height), '#080F16')
    draw = ImageDraw.Draw(image)
    draw.rectangle((left - 1, top - 1, left + panel_width, top + panel_height), fill=ACCENT)
    draw.rectangle((left, top, left + panel_width - 1, top + panel_height - 1), fill='#14232F')
    draw.text((left + 12, top + 8), text('title'), font=FONT, fill=ACCENT)
    draw.text((left + 12, top + 21), fitted(text('subtitle'), panel_width - 24), font=FONT, fill=MUTED)
    tab_width = (panel_width - 24) // 4
    for tab in range(4):
        position = left + 12 + tab * tab_width
        draw.rectangle((position, top + 36, position + tab_width - 4, top + 55), fill='#384D5A' if tab == entry['tab'] else '#263844', outline='#60717B')
        label = text('tab.' + str(tab))
        draw.text((position + (tab_width - FONT.getlength(label)) // 2, top + 39), label, font=FONT, fill=ACCENT if tab == entry['tab'] else TEXT)
    draw.text((left + 18, top + 63), text(page_id + '.title'), font=FONT, fill=ACCENT)
    lines = []
    for value, color in [('激活要求：' + text(page_id + '.requirement'), TEXT), ('羁绊效果：' + text(page_id + '.effect'), GREEN), (text(page_id + '.note'), MUTED), (text('tab_note.' + str(entry['tab'])), ACCENT)]:
        lines.extend((line, color) for line in wrapped(value, panel_width - 50))
        lines.append(('', color))
    content_height = 64 + len(lines) * 12
    scroll = max(0, content_height - viewport) if at_end else 0
    content = Image.new('RGB', (panel_width - 26, max(content_height, viewport)), '#0E1923')
    content_draw = ImageDraw.Draw(content)
    group_width = ((panel_width - 48) - (len(entry['groups']) - 1) * 16) // len(entry['groups'])
    for group_index, group in enumerate(entry['groups']):
        group_x = 11 + group_index * (group_width + 16)
        content_draw.rectangle((group_x, 3, group_x + group_width - 1, 55), fill='#253746')
        icon_width = len(group['items']) * 24 + (len(group['items']) - 1) * 12
        icon_left = group_x + (group_width - icon_width) // 2
        for icon_index, item in enumerate(group['items']):
            icon_x = icon_left + icon_index * 36
            texture = icon(item)
            content.paste(texture, (icon_x, 6), texture)
            if icon_index:
                content_draw.text((icon_x - 9, 12), '/', font=FONT, fill=MUTED)
        label = text('group.' + group['label']) + ' ×' + str(group['count'])
        label = fitted(label, group_width - 6)
        content_draw.text((group_x + (group_width - FONT.getlength(label)) // 2, 38), label, font=FONT, fill=TEXT)
        if group_index:
            content_draw.text((group_x - 12, 22), '+', font=FONT, fill=ACCENT)
    for index, (line, color) in enumerate(lines):
        content_draw.text((11, 62 + index * 12), line, font=FONT, fill=color)
    image.paste(content.crop((0, scroll, panel_width - 26, scroll + viewport)), (left + 13, viewport_top))
    if content_height > viewport:
        thumb_height = max(12, viewport * viewport // content_height)
        thumb_y = viewport_top + scroll * (viewport - thumb_height) // (content_height - viewport)
        draw.rectangle((left + panel_width - 16, thumb_y, left + panel_width - 14, thumb_y + thumb_height), fill=ACCENT)
    draw.rectangle((left + 12, top + panel_height - 29, left + 81, top + panel_height - 10), fill='#263844', outline='#60717B')
    draw.text((left + 20, top + panel_height - 27), text('close'), font=FONT, fill=TEXT)
    matching = [candidate for candidate in CATALOG if candidate['tab'] == entry['tab']]
    draw.text((left + panel_width - 106, top + panel_height - 26), '<', font=FONT, fill=TEXT)
    draw.text((left + panel_width - 82, top + panel_height - 25), f'{matching.index(entry) + 1}/{len(matching)}', font=FONT, fill=TEXT)
    draw.text((left + panel_width - 30, top + panel_height - 26), '>', font=FONT, fill=TEXT)
    return image


checked_items = set()
for entry in CATALOG:
    for group in entry['groups']:
        for item in group['items']:
            namespace, name = item.split(':')
            resource(f'assets/{namespace}/models/item/{name}.json')
            checked_items.add(item)

previews = [render(name) for name in ['frost_edge', 'arcane_lattice', 'flight', 'pack', 'resonance', 'set_meat']]
contact = Image.new('RGB', (854 * 2, 480 * 3), '#080F16')
for index, preview in enumerate(previews):
    contact.paste(preview, ((index % 2) * 854, (index // 2) * 480))
contact.save(OUTPUT / 'guide-contact-sheet.png')
small = Image.new('RGB', (427 * 2, 240 * 2), '#080F16')
for index, preview in enumerate([render('arcane_lattice', 427, 240), render('arcane_lattice', 427, 240, True), render('resonance', 427, 240), render('resonance', 427, 240, True)]):
    small.paste(preview, ((index % 2) * 427, (index // 2) * 240))
small.resize((1708, 960), Image.Resampling.NEAREST).save(OUTPUT / 'guide-small-screen.png')
(OUTPUT / 'report.json').write_text(json.dumps({'status': 'PASS', 'runtimeItemModelsChecked': len(checked_items), 'pages': len(CATALOG), 'offlineLayoutPreview': True, 'actualGameScreenshot': False}, indent=2) + '\n', encoding='utf-8')
for archive in archives:
    archive.close()
print('PASS runtime icon model availability:', len(checked_items), 'Preview:', OUTPUT)
