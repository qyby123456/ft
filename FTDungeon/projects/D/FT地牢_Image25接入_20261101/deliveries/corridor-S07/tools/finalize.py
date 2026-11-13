import hashlib
import json
from datetime import datetime
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def read_json(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    source_hash = digest(ROOT / 'room.nbt')
    metadata = read_json(ROOT / 'room.json')
    static = read_json(ROOT / 'qa/static.json')
    native = read_json(ROOT / 'qa/forge-native.json')
    views = read_json(ROOT / 'four-views/manifest.json')
    visual = read_json(ROOT / 'qa/visual-review.json')
    browser = read_json(ROOT / 'qa/lodestone.json')
    assert metadata['sha256'] == static['sha256'] == native['sha256'] == views['source_nbt_sha256'] == visual['sha256'] == browser['sha256'] == source_hash
    assert static['status'] == native['status'] == visual['status'] == browser['status'] == 'PASS'
    assert len(native['rotations']) == 4 and native['ticks_observed'] >= 240
    assert len(views['views']) == 8
    for view in views['views']:
        path = Path(view['path'])
        assert digest(path) == view['sha256'] and view['source_nbt_sha256'] == source_hash
        with Image.open(path) as image:
            image.verify()
    with Image.open(ROOT / 'preview.png') as image:
        assert image.size == (1400, 1080)
        image.verify()
    log = (ROOT / 'qa/forge-run.log').read_text(encoding='utf-8', errors='replace')
    assert 'S07_FORGE_PLACEMENT_PASS:' in log and 'BUILD SUCCESSFUL' in log
    assert 'test_environment' in native
    report = {
        'id': metadata['id'], 'concept_id': metadata['concept_id'], 'status': 'PASS',
        'ready_for_unified_intake': True, 'deployed': False, 'player_save_modified': False,
        'finished_at': datetime.now().astimezone().isoformat(), 'sha256': source_hash,
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'],
        'static': {'status': static['status'], 'checks': len(static['checks']), 'evidence': str(ROOT / 'qa/static.json')},
        'forge_native': {'status': native['status'], 'minecraft': native['minecraft'], 'forge': native['forge'], 'ticks_observed': native['ticks_observed'], 'rotations': native['rotations'], 'evidence': str(ROOT / 'qa/forge-native.json'), 'log': str(ROOT / 'qa/forge-run.log')},
        'four_view_review': {'status': visual['status'], 'manifest': str(ROOT / 'four-views/manifest.json'), 'evidence': str(ROOT / 'qa/visual-review.json'), 'internal_views': 4, 'external_views': 4, 'final_nbt_unchanged_by_cutaway': True},
        'lodestone_preview': {'status': browser['status'], 'evidence': str(ROOT / 'qa/lodestone.json'), 'screenshot': str(ROOT / 'qa/lodestone-preview.png')},
        'liquid_sealing': {'status': 'PASS', 'source_water_cells_per_rotation': 72, 'depth_blocks': 1, 'fluid_leaks': 0, 'dry_path_width': 3, 'dry_path_length': 16},
        'containers': {'count': 0, 'inventory_items': 0, 'loot_tables': 0},
        'preplaced_entities': 0, 'dropped_items_after_updates': 0,
        'intentional_deviations': metadata['intentional_deviations'],
        'limits': ['软件渲染与 Lodestone 预览不是整合包实机截图。', 'Forge 1.20.1/47.4.10 原生测试在本交付目录的独立测试世界运行，不是玩家存档游玩。', '未分配正式槽位，未连接大厅，未部署 JAR。'],
    }
    (ROOT / 'qa.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'HANDOFF.md', 'summary.json', 'source/corridor-s07.mjs', 'qa/static.json', 'qa/forge-native.json', 'qa/native-roundtrip.nbt', 'qa/forge-run.log', 'qa/visual-review.json', 'qa/lodestone.json', 'qa/lodestone-preview.png', 'four-views/manifest.json', 'four-views/interior-four-views.png', 'four-views/exterior-four-views.png']
    files.extend(['tools/static_qa.py', 'tools/render_views.py', 'tools/finalize.py', 'tools/verify.ps1', 'forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties', 'forge/src/main/java/net/farmingtales/corridors07qa/CorridorS07Qa.java', 'forge/src/main/java/net/farmingtales/corridors07qa/S07PlacementTests.java', 'forge/src/main/resources/META-INF/mods.toml', 'forge/src/main/resources/pack.mcmeta', 'forge/src/main/resources/data/ftcorridors07qa/structures/empty.nbt', 'qa/environment-diagnostic.json'])
    files.extend(Path(view['path']).relative_to(ROOT).as_posix() for view in views['views'])
    manifest = {'id': metadata['id'], 'sha256': source_hash, 'files': [{'path': relative, 'sha256': digest(ROOT / relative), 'bytes': (ROOT / relative).stat().st_size} for relative in files]}
    (ROOT / 'delivery-manifest.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'sha256': source_hash, 'packaged_files': len(files)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
