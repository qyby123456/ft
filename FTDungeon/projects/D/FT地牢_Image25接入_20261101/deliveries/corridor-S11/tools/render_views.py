import importlib.util
import json
from pathlib import Path

from PIL import Image

from check_delivery import ROOT, Structure, digest, dump


SHARED_RENDERER = ROOT.parents[1] / 'tools/review_four_views.py'
specification = importlib.util.spec_from_file_location('s11_four_view_renderer', SHARED_RENDERER)
review = importlib.util.module_from_spec(specification)
specification.loader.exec_module(review)


def filtered(structure, metadata, camera, exterior):
    result = {}
    for position, block in structure.blocks.items():
        name = block.name.removeprefix('minecraft:')
        if name in review.INVISIBLE:
            continue
        horizontal, height, depth = position
        if not exterior:
            if height == 11:
                continue
            near_side = horizontal <= 1 if camera[0] < 0 else horizontal >= 11
            near_end = depth == 0 if camera[1] < 0 else depth == 15
            if height >= 3 and (near_side or near_end):
                continue
        result[position] = block
    return result


def main():
    review.filtered = filtered
    room = Structure.read(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    assert metadata['sha256'] == digest(ROOT / 'room.nbt')
    views = [('north', '北面 / 正视', (-0.35, -1)), ('south', '南面 / 后视', (0.35, 1)),
             ('west', '西面 / 左视', (-1, 0.35)), ('east', '东面 / 右视', (1, -0.35))]
    evidence = []
    for exterior in [False, True]:
        kind = 'exterior' if exterior else 'interior'
        sheet = Image.new('RGB', (2400, 1880), '#101a20')
        for index, (direction, title, camera) in enumerate(views):
            image = review.render(room, metadata, camera, title, exterior, size=(1200, 940))
            filename = f'previews/{kind}-{direction}.png'
            target = ROOT / filename
            target.parent.mkdir(parents=True, exist_ok=True)
            image.save(target)
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 940))
            evidence.append({'file': filename, 'sha256': digest(target), 'direction': direction, 'kind': kind, 'source_nbt_sha256': metadata['sha256']})
        sheet.save(ROOT / f'{kind}-four-views.png')
        evidence.append({'file': f'{kind}-four-views.png', 'sha256': digest(ROOT / f'{kind}-four-views.png'), 'kind': kind, 'source_nbt_sha256': metadata['sha256']})
    dump(ROOT / 'qa/renders.json', {
        'id': 'S11', 'status': 'RENDERED_PENDING_VISUAL_REVIEW', 'sha256': metadata['sha256'],
        'renderer': 'Actual final NBT; shared 1.20.1 vanilla-textured software four-view renderer',
        'interior_cutaway': 'Only roof y=11 and near-side exterior walls/end plane are hidden in memory. NBT unchanged.',
        'exterior': 'Complete final NBT including roof; no cutaway', 'game_client_screenshot': False,
        'files': evidence,
    })
    print(json.dumps({'status': 'RENDERED', 'source_nbt_sha256': metadata['sha256'], 'images': len(evidence)}))


if __name__ == '__main__':
    main()
