from PIL import Image, ImageDraw, ImageFont

from check_delivery import ROOT


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


def main():
    canvas = Image.new('RGB', (2400, 1480), '#140f10')
    draw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 0, 2399, 135), fill='#281518')
    draw.rectangle((40, 0, 50, 135), fill='#e87436')
    draw.text((83, 29), 'S11  /  烬狱吊笼短廊', font=font(47), fill='#f5e7d8')
    draw.text((86, 90), '16 格短直廊 · Minecraft Java 1.20.1 · 来自最终 NBT 的预览', font=font(23), fill='#c8aa97')
    hero = Image.open(ROOT / 'previews/lodestone-north.png').convert('RGB')
    canvas.paste(hero, (44, 201))
    draw.text((45, 156), '北入口 → 南出口  /  Lodestone 一点透视', font=font(25), fill='#f0c5a2')
    draw.rectangle((43, 200, 1580, 1225), outline='#604438', width=2)
    cutaway = Image.open(ROOT / 'previews/interior-north.png').convert('RGB').crop((115, 115, 1115, 850))
    cutaway = cutaway.resize((716, 526), Image.Resampling.LANCZOS)
    canvas.paste(cutaway, (1640, 201))
    draw.text((1640, 156), '结构剖视  /  完整屋顶保留在 NBT 内', font=font(24), fill='#f0c5a2')
    lines = [
        ('规格', '13 × 12 × 16 方块'),
        ('主路', '五格宽，三格通行净空'),
        ('接口', '北 [6,3,0]  /  南 [6,3,15]'),
        ('主题', '红地狱砖、焦黑石砖、四组吊笼'),
        ('照明', '六支壁火把、封护熔岩侧槽'),
        ('验收', '静态通过 · 四视图复核'),
        ('原生', 'Forge 四旋转 × 240 tick 通过'),
    ]
    for index, (label, value) in enumerate(lines):
        vertical = 765 + index * 58
        draw.text((1640, vertical), label, font=font(24), fill='#b5886d')
        draw.text((1730, vertical), value, font=font(24), fill='#eadbcc')
    draw.line((44, 1280, 2356, 1280), fill='#624235', width=2)
    draw.text((45, 1312), '安全适配：24 格真实熔岩均有底、有封边，并以橙色玻璃齐平封盖；无转弯、分岔或地面台阶。', font=font(27), fill='#e1ba99')
    draw.text((45, 1372), '软件渲染，非游戏客户端截图。独立候选交付；未部署、未接入核心、未写入玩家存档。', font=font(24), fill='#aa9281')
    canvas.save(ROOT / 'preview.png')
    print('Saved preview.png 2400x1480')


if __name__ == '__main__':
    main()
