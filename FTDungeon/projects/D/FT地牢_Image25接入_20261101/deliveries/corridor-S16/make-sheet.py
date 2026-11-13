import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps


ROOT = Path(__file__).resolve().parent
FONT_PATH = Path('C:/Windows/Fonts/msyh.ttc')
BACKGROUND = '#101b27'
FOREGROUND = '#d8edf4'
MUTED = '#87b2c4'
VIEWS = [('north', '北向内部剖视'), ('south', '南向内部剖视'), ('west', '西向内部剖视'), ('east', '东向内部剖视'), ('exterior', '完整外壳 · 未剖切'), ('roof', '完整屋顶 · 未剖切')]


def main():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    canvas = Image.new('RGB', (2048, 2052), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    title = ImageFont.truetype(str(FONT_PATH), 38)
    heading = ImageFont.truetype(str(FONT_PATH), 23)
    body = ImageFont.truetype(str(FONT_PATH), 18)
    draw.text((40, 28), 'S16 / 冰封牢狱·蔓生短廊', font=title, fill=FOREGROUND)
    draw.text((42, 88), '由最终 room.nbt 渲染  |  11 × 10 × 16  |  剖切仅影响预览副本', font=body, fill=MUTED)
    for index, (identity, label) in enumerate(VIEWS):
        left = 32 + index % 2 * 1000
        top = 146 + index // 2 * 612
        draw.rounded_rectangle((left, top, left + 984, top + 594), radius=12, fill='#172735', outline='#365264', width=2)
        draw.text((left + 22, top + 15), label, font=heading, fill=FOREGROUND)
        image = Image.open(ROOT / 'preview' / f'{identity}.png').convert('RGB')
        image = image.crop((0, 102, 1536, 980))
        panel = ImageOps.contain(image, (940, 530), Image.Resampling.LANCZOS)
        canvas.paste(panel, (left + (984 - panel.width) // 2, top + 53 + (530 - panel.height) // 2))
    draw.text((42, 2006), f'NBT SHA-256 {metadata["sha256"]}', font=body, fill=MUTED)
    canvas.save(ROOT / 'preview' / 'four-view-sheet.png', optimize=True)
    print(ROOT / 'preview' / 'four-view-sheet.png')


if __name__ == '__main__':
    main()
