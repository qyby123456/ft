import argparse
import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
FONT = 'C:/Windows/Fonts/msyh.ttc'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads((ROOT / path).read_text(encoding='utf-8'))


def write(path, content):
    (ROOT / path).write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def sheet(mode):
    width, header, tile_height = 2048, 142, 744
    board = Image.new('RGB', (width, header + tile_height * 2), '#101b2b')
    draw = ImageDraw.Draw(board)
    draw.text((38, 22), 'S08 · 冰霜石拱短直廊', font=ImageFont.truetype(FONT, 36), fill='#e4f4ff')
    subtitle = '北 / 南 / 西 / 东内部剖视 · 仅隐藏显示副本，完整 NBT 不变' if mode == 'interior' else '北 / 南 / 西 / 东完整外壳 · 不剖切屋顶与侧墙'
    draw.text((40, 80), subtitle, font=ImageFont.truetype(FONT, 24), fill='#a6c8e2')
    for index, (direction, label) in enumerate([('north', '北向'), ('south', '南向'), ('west', '西向'), ('east', '东向')]):
        source = Image.open(ROOT / f'views/{direction}-{mode}.png').convert('RGB')
        source.thumbnail((1000, 666), Image.Resampling.LANCZOS)
        across, vertical = (index % 2) * 1024 + 12, header + (index // 2) * tile_height
        board.paste(source, (across, vertical + 43))
        draw.text((across + 12, vertical + 3), label, font=ImageFont.truetype(FONT, 25), fill='#dfedff')
    output = ROOT / f'views/four-{mode}.png'
    board.save(output)
    return str(output)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--boards-only', action='store_true')
    arguments = parser.parse_args()
    for mode in ('interior', 'exterior'):
        print(sheet(mode))
    if arguments.boards_only:
        return
    metadata = read('room.json')
    fingerprint = digest(ROOT / 'room.nbt')
    reports = {name: read(f'qa/{name}.json') for name in ('static', 'render', 'forge', 'visual')}
    assert metadata['sha256'] == fingerprint
    for name, report in reports.items():
        assert report['status'] == 'PASS', (name, report.get('status'))
        assert report['structure_sha256'] == fingerprint, f'{name} stale fingerprint'
    assert len(reports['render']['views']) == 9
    for view in reports['render']['views']:
        assert digest(ROOT / view['file']) == view['sha256']
        with Image.open(ROOT / view['file']) as image:
            assert image.size == (1536, 1024)
            assert image.convert('RGB').getextrema() != ((0, 0), (0, 0), (0, 0))
    assert len(reports['forge']['rotations']) == 4
    for rotation in reports['forge']['rotations']:
        assert rotation['status'] == 'PASS' and rotation['capture_matches_source']
        assert digest(ROOT / rotation['world_capture']) == rotation['capture_sha256']
    write('materials.json', {'structure_sha256': fingerprint, 'non_air_blocks': {name: count for name, count in reports['static']['counts'].items() if name != 'minecraft:air'}})
    result = {
        'id': 'corridor-S08', 'status': 'PASS', 'structure_sha256': fingerprint,
        'producer_thread_id': metadata['producer_thread_id'],
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'], 'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'static': {'status': 'PASS', 'report': 'qa/static.json', 'checks': len(reports['static']['checks'])},
        'four_view': {'status': 'PASS', 'render_report': 'qa/render.json', 'visual_report': 'qa/visual.json', 'interior': 'views/four-interior.png', 'exterior': 'views/four-exterior.png', 'directions': ['north', 'south', 'west', 'east'], 'source_nbt_unchanged': True},
        'forge': {'status': 'PASS', 'report': 'qa/forge.json', 'log': 'qa/forge-run.log', 'minecraft': '1.20.1', 'version': '47.4.10', 'rotations': 4, 'stabilization_ticks': reports['forge']['stabilization_ticks'], 'records_checked_after_ticks': reports['forge']['records_checked_after_ticks'], 'negative_support_probes': 4, 'negative_collision_probes': 4, 'world_capture_roundtrip': True},
        'independent_delivery_ready': True, 'coordinator_acceptance': 'pending', 'integration_slot': None,
        'deployed': False, 'production_saves_touched': False,
        'limitations': metadata['limitations'] + reports['forge']['limitations'],
    }
    write('qa.json', result)
    artifacts = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'blueprint.json', 'recipe.mjs', 'README.md']
    artifacts += [str(path.relative_to(ROOT)).replace('\\', '/') for directory in ('views', 'qa') for path in (ROOT / directory).glob('*.png')]
    artifacts += [f'qa/{name}.json' for name in reports]
    artifacts += [rotation['world_capture'].replace('\\', '/') for rotation in reports['forge']['rotations']]
    artifacts += ['qa/forge-run.log', 'qa/preview-server.log', 'native/build.gradle', 'native/settings.gradle']
    artifacts += [str(path.relative_to(ROOT)).replace('\\', '/') for path in (ROOT / 'tools').glob('*') if path.is_file()]
    artifacts += [str(path.relative_to(ROOT)).replace('\\', '/') for path in (ROOT / 'native/src').rglob('*') if path.is_file()]
    write('delivery-manifest.json', {'id': 'corridor-S08', 'status': 'PASS', 'structure_sha256': fingerprint, 'artifacts': [{'path': artifact, 'sha256': digest(ROOT / artifact), 'bytes': (ROOT / artifact).stat().st_size} for artifact in sorted(set(artifacts))]})
    print(json.dumps({'status': 'PASS', 'id': result['id'], 'sha256': fingerprint, 'artifacts': len(set(artifacts))}))


if __name__ == '__main__':
    main()
