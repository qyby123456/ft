from __future__ import annotations

import json
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from static_qa import ROOT, digest, write_json


def read_json(filename):
    return json.loads((ROOT / filename).read_text(encoding='utf-8'))


def main():
    metadata = read_json('room.json')
    static = read_json('qa/static.json')
    native = read_json('qa/native.json')
    render = read_json('qa/render.json')
    source_hash = digest(ROOT / 'room.nbt')
    assert source_hash == metadata['sha256'] == static['sha256'] == native['sha256'] == render['source_sha256']
    assert static['status'] == native['status'] == render['status'] == 'PASS'
    assert native['rotations'] == 4 and len(native['initial']) == len(native['after_400_ticks']) == 4
    for result in native['initial'] + native['after_400_ticks']:
        assert result['records'] == 2288 and result['native_supported_blocks'] == 1624
        assert result['valid_block_entities'] == result['supported_skulls'] == 10
        assert result['lit_candle_clusters'] == result['supported_end_rods'] == 8
        assert result['anchored_hanging_cages'] == 4 and result['roof_full_blocks'] == 208
        assert result['straight_route_length'] == 16 and result['straight_route_width'] == 3
        assert result['reward_inventory'] == 0
    log = (ROOT / 'qa/gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'S01_NATIVE_GAME_TEST_PASS rotations=4/4 records=9152 ticks=400 sha256=' + source_hash in log
    assert 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    assert render['interior_directions'] == render['exterior_directions'] == 4
    for view in render['views']:
        assert digest(ROOT / view['file']) == view['sha256']
    image_paths = ['preview.png', 'qa/interior-four-views.png', 'qa/exterior-four-views.png', 'qa/floorplan.png'] + [view['file'] for view in render['views']]
    for image_path in image_paths:
        with Image.open(ROOT / image_path) as image:
            image.verify()
    created = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds')
    result = {'id': 'S01', 'status': 'PASS', 'completed_at': created, 'sha256': source_hash, 'reference_sha256': metadata['reference_sha256'], 'minecraft': '1.20.1', 'forge': '47.4.10', 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'], 'checks': {'static_nbt': 'PASS', 'full_volume_explicit_2288_cells': 'PASS', 'one_chunk_16_block_length': 'PASS', 'straight_dry_supported_3x3_corridor': 'PASS', 'north_and_south_ports_open': 'PASS', 'no_turns_branches_or_route_stairs': 'PASS', 'sealed_sidewalls_full_roof': 'PASS', 'native_registry_properties_and_block_entity_decode': 'PASS', 'forge_actual_serverlevel_four_rotations': 'PASS', 'explicit_neighbor_updates': 'PASS', 'exact_state_equality_after_400_ticks': 'PASS', 'native_support_and_collision_routes': 'PASS', 'no_entities_drops_or_boundary_leaks': 'PASS', 'four_interior_and_four_exterior_nbt_renders': 'PASS', 'image_decode_and_render_hashes': 'PASS'}, 'native': native, 'static': static, 'render': render, 'visual_review': {'status': 'PASS', 'method': 'Viewed final preview and both four-view contact sheets; confirmed open axial passage, four cages, sealed sidewalls, and intact roof in exterior views.', 'not_full_modpack_gameplay': True}, 'scope': {'production_save_used': False, 'production_mods_modified': False, 'deployment_performed': False, 'core_source_modified': False, 'other_rooms_modified': False, 'image_api_called': False, 'isolated_world': 'qa/forge-run/world', 'full_modpack_playtest': False}, 'preserved_attempts': [{'file': 'qa/gametest-attempt1-java25.log', 'reason': 'Host default Java 25 was incompatible with Gradle 8.8; rerun uses process-local Java 17 only.'}, {'file': 'qa/gametest-attempt2-terrain-refused.log', 'reason': 'High terrain occupied a test fixture at y=100; occupied volume was refused rather than cleared. Final test uses verified-empty y=300.'}], 'evidence': {'static': 'qa/static.json', 'forge': 'qa/native.json', 'forge_log': 'qa/gametest.log', 'render': 'qa/render.json', 'interior_views': 'qa/interior-four-views.png', 'exterior_views': 'qa/exterior-four-views.png', 'floorplan': 'qa/floorplan.png'}}
    write_json(ROOT / 'qa.json', result)
    includes = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json', 'build.gradle', 'settings.gradle', 'gradle.properties']
    includes += [str(item.relative_to(ROOT)) for item in sorted((ROOT / 'tools').rglob('*')) if item.is_file() and '__pycache__' not in item.parts]
    includes += [str(item.relative_to(ROOT)) for item in sorted((ROOT / 'qa').glob('*')) if item.is_file()]
    for filename in includes:
        assert (ROOT / filename).is_file()
    handoff = {'id': 'S01', 'status': 'PASS', 'root': str(ROOT), 'sha256': source_hash, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'], 'length_blocks': 16, 'main_files': ['room.nbt', 'room.json', 'preview.png', 'qa.json'], 'deployment': 'not deployed', 'full_modpack_playtest': False, 'files': {filename.replace('\\', '/'): {'sha256': digest(ROOT / filename), 'bytes': (ROOT / filename).stat().st_size} for filename in includes}}
    write_json(ROOT / 'handoff.json', handoff)
    package = ROOT / 'S01-bone-crypt-overgrown-handoff.zip'
    with zipfile.ZipFile(package, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for filename in includes + ['handoff.json']:
            archive.write(ROOT / filename, arcname=filename.replace('\\', '/'))
    with zipfile.ZipFile(package) as archive:
        assert archive.testzip() is None
    write_json(ROOT / 'package-receipt.json', {'id': 'S01', 'status': 'PASS', 'path': str(package), 'sha256': digest(package), 'bytes': package.stat().st_size, 'files': len(includes) + 1, 'excluded': ['.gradle/', 'build/', 'qa/forge-run/', 'tools/__pycache__/'], 'world_files_in_package': False})
    print(json.dumps({'id': 'S01', 'status': 'PASS', 'sha256': source_hash, 'package': str(package), 'package_sha256': digest(package)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
