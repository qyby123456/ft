import hashlib
import json
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from build_room import ROOT, build, digest, write_json
from nbtio import AIR, Structure
from qa_static import run as static_qa


def main():
    files = ('room.nbt', 'room.json', 'blueprint.json', 'materials.json')
    before = {name: digest(ROOT / 'out' / name) for name in files}
    build()
    after = {name: digest(ROOT / 'out' / name) for name in files}
    assert before == after, 'Rebuild changed delivered source'
    reproducibility = {'status': 'PASS', 'same_bytes_after_rebuild': before == after, 'sha256': after}
    write_json(ROOT / 'qa/reproducibility.json', reproducibility)
    static = static_qa()
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    rendered = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    assert static['status'] == native['status'] == visual['status'] == 'PASS'
    assert static['structure_sha256'] == native['structure_sha256'] == rendered['structure_sha256'] == metadata['sha256']
    assert visual['structure_sha256'] == metadata['sha256']
    for artifact in visual['artifacts']:
        assert digest(Path(artifact['path'])) == artifact['sha256'], 'Reviewed image changed after inspection'
    assert len(native['rotations']) == 4 and native['records_checked_after_ticks'] == 10200 and native['stabilization_ticks'] == 100
    assert all(item['capture_matches_source'] for item in native['rotations'])
    assert native['negative_missing_support_probes'] == native['negative_blocked_aisle_probes'] == 4
    source = Structure.read(ROOT / 'out/room.nbt')
    captured = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    assert source.size == captured.size and source.blocks == captured.blocks
    native_files = []
    for rotation in native['rotations']:
        path = ROOT / rotation['world_capture']
        native_files.append({'path': str(path), 'sha256': digest(path), 'size': list(Structure.read(path).size), 'capture_matches_source': rotation['capture_matches_source']})
    assert len(rendered['native_captures']) == 1
    with Image.open(ROOT / 'out/preview.png') as image:
        image.load()
        preview_size = list(image.size)
        assert preview_size == [1536, 1200]
    report = {
        'status': 'PASS', 'id': metadata['id'], 'variant': 'G',
        'completed_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
        'scope': 'Standalone G Frost Shop candidate; not core registration, deployment or full modpack client verification.',
        'structure': {'path': str(ROOT / 'out/room.nbt'), 'sha256': metadata['sha256'], 'size': metadata['size'], 'data_version': 3465, 'records': len(source.blocks), 'nonair_blocks': sum(block != AIR for block in source.blocks.values()), 'entities': 0, 'block_entities': len(source.block_entities)},
        'reference': {'path': metadata['reference'], 'sha256': metadata['reference_sha256'], 'copied_reference': str(ROOT / 'reference/frost-shop-design.png')},
        'static': {'status': static['status'], 'passed': static['passed'], 'failed': static['failed'], 'reachable_ground_cells': static['navigation']['reachable_ground_cells'], 'report': str(ROOT / 'qa/static.json')},
        'native': {'status': native['status'], 'minecraft': native['minecraft'], 'forge': native['forge'], 'java': native['java'], 'rotations': 4, 'stabilization_ticks': native['stabilization_ticks'], 'records_checked_after_ticks': native['records_checked_after_ticks'], 'missing_support_negative_probes': native['negative_missing_support_probes'], 'blocked_aisle_negative_probes': native['negative_blocked_aisle_probes'], 'world_captures': native_files, 'report': str(ROOT / 'qa/native.json'), 'log': str(ROOT / 'qa/forge-gametest.log')},
        'visual': {'status': visual['status'], 'report': str(ROOT / 'qa/visual-review.json'), 'preview_size': preview_size, 'preview_sha256': digest(ROOT / 'out/preview.png'), 'four_views': str(ROOT / 'preview/four-views.png'), 'native_world_capture_render': str(ROOT / 'preview/native-world-capture.png'), 'not_in_game_screenshot': True},
        'reproducibility': {'status': 'PASS', 'report': str(ROOT / 'qa/reproducibility.json')},
        'safety': {'reward_items': 0, 'loot_tables': 0, 'preplaced_entities': 0, 'fluid_blocks': 0, 'independent_pillars': 4, 'pillar_full_height_air_ring': 1, 'counter_air_ring': 1, 'hanging_cold_lamps': 2, 'frost_icicles': 7, 'floor_display_cases': 2},
        'attempts': [{'attempt': 1, 'status': 'FAIL', 'reason': 'Verification fixture retained a square-room world-capture count and bounding box. Native placement/support/route checks succeeded; final capture assertion failed. Fixed only verification code; delivered NBT unchanged.', 'evidence': 'qa/previous-20261102-103603-native.json'}, {'attempt': 2, 'status': 'FAIL', 'reason': 'Added snow light check ran synchronously before light-engine propagation completed, reporting melting-strength light. Deferred this asynchronous check to the existing 100-tick verification, without changing source NBT.', 'evidence': 'qa/previous-20261102-104014-native.json'}, {'attempt': 3, 'status': 'PASS', 'reason': 'Native four-rotation placement, neighbor notifications, 100 tick survival and snow light, inventories and complete rectangular world captures all passed.'}],
        'parallel_boundaries': {'writes_only': str(ROOT), 'modified_other_rooms': False, 'modified_core': False, 'deployed_jar': False, 'opened_or_modified_production_save': False, 'paid_image_generation': False},
        'integration': metadata['integration'], 'design_adaptations': metadata['design_adaptations'],
        'limitations': native['limitations'] + ['17×10×15 XYZ, intentionally larger than the concept sketch to preserve detached pillars and legal 3×3 glass display cases.', 'G shop visual architecture only; no shopkeeper, real stock, trade UI, FT catalog slot or corridor deployed.'],
    }
    write_json(ROOT / 'out/qa.json', report)
    archive = ROOT / 'release/FT_Dungeon_Shop_G_Frost_Standalone_20261102.zip'
    archive.parent.mkdir(exist_ok=True)
    inputs = [ROOT / 'README.md', ROOT / 'HANDOFF.md', ROOT / 'build.gradle', ROOT / 'settings.gradle', ROOT / 'gradle.properties']
    for directory in ('out', 'reference', 'preview', 'tools', 'src'):
        inputs.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    inputs.extend(path for path in (ROOT / 'qa').glob('*') if path.is_file() and path.name != 'package-verification.json')
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as package:
        for path in sorted(set(inputs)):
            info = zipfile.ZipInfo(path.relative_to(ROOT).as_posix(), (2026, 11, 2, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            package.writestr(info, path.read_bytes())
    with zipfile.ZipFile(archive) as package:
        assert package.testzip() is None
        for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json'):
            assert hashlib.sha256(package.read('out/' + name)).hexdigest() == digest(ROOT / 'out' / name)
    manifest = {'status': 'PASS', 'archive': str(archive), 'archive_sha256': digest(archive), 'archive_bytes': archive.stat().st_size, 'delivered_file_count': len(set(inputs)), 'quartet': {name: {'path': str(ROOT / 'out' / name), 'sha256': digest(ROOT / 'out' / name)} for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')}, 'qa_report_binds_to_structure': report['structure']['sha256'] == metadata['sha256']}
    write_json(ROOT / 'qa/package-verification.json', manifest)
    print(json.dumps({'status': 'PASS', 'id': metadata['id'], 'structure_sha256': metadata['sha256'], 'archive': str(archive), 'archive_sha256': manifest['archive_sha256']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
