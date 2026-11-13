import hashlib
import importlib.util
import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools/review_four_views.py')
specification = importlib.util.spec_from_file_location('corridor_review', SOURCE)
review = importlib.util.module_from_spec(specification)
specification.loader.exec_module(review)


def filtered(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        if block.name.removeprefix('minecraft:') in review.INVISIBLE:
            continue
        positionX, positionY, positionZ = position
        if not exterior:
            if positionY == structure.size[1] - 1:
                continue
            near_side = positionX <= 2 if camera[0] < 0 else positionX >= structure.size[0] - 3
            near_end = positionZ == 0 if camera[1] < 0 else positionZ == structure.size[2] - 1
            if positionY >= 1 and (near_side or near_end):
                continue
        result[position] = block
    return result


def main():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    fingerprint = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    if metadata['sha256'] != fingerprint:
        raise ValueError('Structure hash mismatch')
    structure = review.Structure.read(ROOT / 'room.nbt')
    review.filtered = filtered
    target = ROOT / 'qa/four-views'
    target.mkdir(parents=True, exist_ok=True)
    files = []
    for exterior in [False, True]:
        mode = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2400, 1880), '#101a20')
        for index, (direction, title, camera) in enumerate(review.VIEWS):
            image = review.render(structure, metadata, camera, title, exterior, size=(1200, 940))
            image_draw = ImageDraw.Draw(image)
            image_draw.rectangle((0, 890, 1200, 940), fill='#101a20')
            image_draw.text((35, 898), '最终 NBT 数据检查图；剖切仅用于渲染，非游戏截图。', font=review.font(18), fill='#99a4aa')
            destination = target / f'{mode}-{direction}.png'
            image.save(destination)
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 940))
            files.append(str(destination.relative_to(ROOT)))
            print(f'RENDERED {mode} {direction}', flush=True)
        sheet.save(target / f'{mode}-four-views.png')
        files.append(str((target / f'{mode}-four-views.png').relative_to(ROOT)))
    plan = Image.new('RGB', (1100, 1110), '#101a20')
    draw = ImageDraw.Draw(plan)
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 24)
    draw.text((50, 24), 'S17 / 暗铁悬笼短廊 · 最终 NBT 平面', font=font, fill='#f2d496')
    draw.text((50, 63), '北 ↑  15×13×16 / 脚点Y=1 / 中央三格直通', font=font, fill='#a7c6c5')
    colors = {'gold_block': '#d5a843', 'redstone_block': '#a72b36', 'red_nether_bricks': '#592836', 'obsidian': '#292433', 'polished_deepslate': '#69717b', 'chiseled_polished_blackstone': '#555566'}
    for positionZ in range(16):
        for positionX in range(15):
            floor = structure.get((positionX, 0, positionZ)).name.split(':')[1]
            air = structure.get((positionX, 1, positionZ)).name == 'minecraft:air'
            left, top = 100 + positionX * 52, 150 + positionZ * 52
            draw.rectangle((left, top, left + 50, top + 50), fill=colors.get(floor, '#363c45') if air else '#101115')
            if positionX in range(6, 9):
                draw.rectangle((left, top, left + 50, top + 50), outline='#75dfcc', width=2)
    for position in metadata['entrance'], metadata['interior'], metadata['exit']:
        positionX, positionY, positionZ = position
        left, top = 100 + positionX * 52, 150 + positionZ * 52
        draw.ellipse((left + 13, top + 13, left + 37, top + 37), fill='#f4e5ad')
    for centerX, centerZ in [(4, 3), (10, 3), (4, 12), (10, 12)]:
        draw.rectangle((100 + (centerX - 1) * 52, 150 + (centerZ - 1) * 52, 100 + (centerX + 2) * 52 - 2, 150 + (centerZ + 2) * 52 - 2), outline='#b6a891', width=4)
    draw.text((70, 1010), '金：地饰边线 / 红：固体嵌纹 / 绿框：净空保护路 / 米框：高空悬笼', font=font, fill='#bfcaca')
    plan.save(target / 'floorplan.png')
    files.append(str((target / 'floorplan.png').relative_to(ROOT)))
    overview = review.render(structure, metadata, (-1, -1), '实际 NBT 剖面总览', False, size=(1536, 1180))
    overview_draw = ImageDraw.Draw(overview)
    overview_draw.rectangle((0, 1130, 1536, 1180), fill='#101a20')
    overview_draw.text((35, 1138), '最终 NBT 剖视；完整屋顶保留在结构中。非 AI 替图，非客户端截图。', font=review.font(20), fill='#99a4aa')
    overview.save(ROOT / 'preview.png')
    files.append('preview.png')
    result = {'status': 'RENDERED_PENDING_VISUAL_REVIEW', 'structure_sha256': fingerprint, 'source': str(ROOT / 'room.nbt'), 'source_type': 'Actual final gzip structure NBT, not concept PNG', 'renderer': 'Existing 1.20.1 vanilla model/texture software renderer', 'directions': ['north', 'south', 'west', 'east'], 'modes': ['interior cutaway', 'complete exterior'], 'cutaway_rule': 'Only in renderer: hide y=12 cap, camera-near side x<=2 or x>=12, and camera-near end portal; preserve interior cages/ribs. Final NBT unchanged.', 'files': files, 'limitations': ['Not Minecraft client screenshots or physical light simulation.', 'Iron bars/chains/trapdoors use vanilla JSON model geometry; complete roof and source NBT remain intact.']}
    (ROOT / 'qa/render-manifest.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('RENDERED all final-NBT views', flush=True)


if __name__ == '__main__':
    main()
