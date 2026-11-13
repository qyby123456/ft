from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
FONT = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 32)
SMALL = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 21)


def main():
    canvas = Image.new('RGB', (2400, 1850), '#11101b')
    draw = ImageDraw.Draw(canvas)
    draw.text((40, 26), 'M09 黑曜试炼残拱长廊 / 四向内外视图', font=FONT, fill='#f0dcc5')
    draw.text((42, 83), '来自最终 room.nbt · 15×12×41 · 上：内视剖切，下：完整外观 · 软件渲染，非游戏客户端截图', font=SMALL, fill='#b6abbf')
    labels = {'north': '北 N', 'south': '南 S', 'west': '西 W', 'east': '东 E'}
    for row, kind in enumerate(['interior', 'exterior']):
        for index, direction in enumerate(['north', 'south', 'west', 'east']):
            pos_x = index % 2 * 1200
            pos_y = 135 + row * 840 + index // 2 * 420
            image = Image.open(ROOT / 'views' / f'{kind}-{direction}.png').convert('RGB')
            image.thumbnail((1180, 382))
            canvas.paste(image, (pos_x + (1200 - image.width) // 2, pos_y + 26))
            draw.text((pos_x + 22, pos_y), labels[direction] + (' / 内视剖切' if kind == 'interior' else ' / 完整外观'), font=SMALL, fill='#e2c49a')
    draw.text((40, 1813), '剖切只影响显示；交付结构保留完整封顶、侧墙、北入口与南出口。中央连续三格净宽，不转弯、不分岔。', font=SMALL, fill='#b6abbf')
    canvas.save(ROOT / 'views/four-direction-review.png')
    print('M09_CONTACT_SHEET_PASS')


if __name__ == '__main__':
    main()
