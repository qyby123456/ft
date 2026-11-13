from __future__ import annotations

import json
import zipfile
from datetime import datetime, timezone, timedelta
from pathlib import Path

from PIL import Image

from static_qa import ROOT, REFERENCE_HASH, digest, dump


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = read('room.json')
    static = read('qa/static-qa.json')
    registry = read('qa/native-registry-qa.json')
    native = read('qa/native-world-qa.json')
    visual = read('qa/four-view-review.json')
    rendered = read('qa/render-manifest.json')
    lodestone = read('qa/lodestone-preview.json')
    for label, report in (('room', metadata), ('static', static), ('registry', registry), ('native', native), ('visual', visual), ('render', rendered), ('lodestone', lodestone)):
        require(report['sha256'] == fingerprint, f'{label}: stale NBT fingerprint')
    require(metadata['reference_sha256'] == REFERENCE_HASH, 'Wrong design reference')
    for label, report in (('static', static), ('registry', registry), ('native', native), ('visual', visual), ('lodestone', lodestone)):
        require(report['status'] == 'PASS', f'{label}: QA not passed')
    require(len(static['checks']) == 16 and all(check['status'] == 'PASS' for check in static['checks'].values()), 'Static checks incomplete')
    require(native['stable_ticks'] >= 600 and len(native['rotations']) == 4, 'Native rotation/stability coverage incomplete')
    for rotation in native['rotations']:
        for phase in ('initial', 'after_600_ticks'):
            check = rotation[phase]
            require(check['status'] == 'PASS' and check['state_changes'] == 0 and check['records_exact'] == 9061, 'Native exact-state proof incomplete')
            require(check['water_sources'] == 174 and check['lava_sources'] == 30 and check['outside_halo_cells'] == 7004, 'Native liquid proof incomplete')
        require(rotation['forced_fluid_tick_schedules'] == 204 and rotation['fixture_rollback'] == 'PASS', 'Missing fluid scheduling or fixture rollback')
    require(len(visual['reviewed_views']) == 8, 'All eight QA views required')
    for view in visual['reviewed_views']:
        require(digest(Path(view['path'])) == view['sha256'], 'A visually reviewed image changed')
    for image in rendered['views']:
        require(digest(Path(image['path'])) == image['sha256'], 'A rendered image changed')
        with Image.open(image['path']) as decoded:
            require(list(decoded.size) == image['dimensions'], 'Wrong image dimensions')
            decoded.verify()
    require(digest(ROOT / 'preview.png') == lodestone['captures'][0]['sha256'], 'Primary preview must be verified north Lodestone capture')
    with Image.open(ROOT / 'preview.png') as preview:
        require(preview.size == (1536, 1024), 'Primary preview dimensions')
        preview.verify()
    successful_logs = []
    for path in sorted((ROOT / 'evidence').glob('forge-gametest-*.log')):
        text = path.read_text(encoding='utf-8', errors='replace')
        if 'M17_NATIVE_QA_PASS' in text and 'BUILD SUCCESSFUL' in text:
            successful_logs.append(path)
    require(bool(successful_logs), 'Missing real successful Forge log')
    rendered['status'] = 'PASS'
    rendered['visual_review_report'] = str(ROOT / 'qa/four-view-review.json')
    rendered['primary_preview_renderer'] = 'Minecraft Builder / Lodestone 0.6.0'
    rendered['primary_preview_sha256'] = digest(ROOT / 'preview.png')
    rendered['cutaway_renderer'] = 'Minecraft 1.20.1 textures; software orthographic projection'
    dump(ROOT / 'qa/render-manifest.json', rendered)
    summary = {
        'id': 'M17', 'status': 'PASS', 'sha256': fingerprint, 'reference_sha256': REFERENCE_HASH,
        'timestamp': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'acceptance_level': 'independent_structure_qa_pass',
        'ready_for_integrator_review': True, 'global_slot_assigned': False,
        'dimensions': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'],
        'static': {'status': 'PASS', 'checks': 16, 'report': str(ROOT / 'qa/static-qa.json')},
        'four_view': {'status': 'PASS', 'source_is_final_nbt': True, 'interior_views': 4, 'full_shell_views': 4, 'visual_review': str(ROOT / 'qa/four-view-review.json'), 'interior_sheet': str(ROOT / 'views/interior-four-views.png'), 'exterior_sheet': str(ROOT / 'views/exterior-four-views.png')},
        'native_forge': {'status': 'PASS', 'minecraft': '1.20.1', 'forge': '47.4.10', 'facings': ['north', 'east', 'south', 'west'], 'stable_ticks': 600, 'exact_states_per_rotation': 9061, 'neighbor_notifications_per_rotation': 9061, 'scheduled_fluid_ticks_per_rotation': 204, 'state_changes': 0, 'report': str(ROOT / 'qa/native-world-qa.json'), 'registry_report': str(ROOT / 'qa/native-registry-qa.json'), 'successful_log': str(successful_logs[-1]), 'fixture_rollback': 'PASS'},
        'liquid_seals': {'status': 'PASS', 'water_sources': 174, 'lava_sources': 30, 'water_depth': 1, 'lava_fully_glazed': True, 'outside_halo_cells_checked_per_rotation': 7004, 'leaks_or_outside_changes': 0},
        'route': {'status': 'PASS', 'dry_width': 5, 'length': 41, 'feet_y': 3, 'native_player_volume': [0.6, 1.8], 'swept_collision_checked': True, 'reachable_feet': 209, 'both_directions': True, 'turns': 0, 'branches': 0, 'stairs_in_route': 0},
        'containers': 0, 'template_entities': 0, 'block_entities': 0,
        'preview': {'status': 'PASS', 'path': str(ROOT / 'preview.png'), 'sha256': digest(ROOT / 'preview.png'), 'renderer': 'Minecraft Builder / Lodestone 0.6.0', 'nbt_server_payload_sha256_verified': True, 'report': str(ROOT / 'qa/lodestone-preview.json'), 'is_gameplay_screenshot': False},
        'known_limitations': ['Preview lighting, liquid coloring and transparency are approximations.', 'FT core integration, global placement and client gameplay remain with the integration owner.'],
        'historical_attempts': [{'status': 'REJECTED_OCCUPIED_QA_FIXTURE', 'report': str(ROOT / 'evidence/native-attempt1-fixture-occupied.json'), 'resolved': 'Changed only the isolated test fixture from terrain height to verified empty air at y=248; the final NBT was unchanged.'}],
        'deployed': False, 'player_worlds_modified': False, 'production_jar_modified': False, 'production_integration_tested': False, 'client_gameplay_tested': False,
    }
    dump(ROOT / 'qa.json', summary)
    counts = static['block_counts']
    materials = ['# M17 材料数量', '', '最终 NBT SHA-256：`' + fingerprint + '`。按模板实际方块计数；水、熔岩另列为空间体积，不是生存材料配方。', '', '| 方块 | 数量 |', '| --- | ---: |']
    materials.extend(f'| `{name}` | {count} |' for name, count in sorted(counts.items()) if name != 'minecraft:air')
    materials.extend(['', f'显式空气：{counts.get("minecraft:air", 0)} 格。总记录 9,061 格。', ''])
    (ROOT / 'MATERIALS.md').write_text('\n'.join(materials), encoding='utf-8')
    payload = [ROOT / name for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'REPORT_FOR_COORDINATOR.md', 'MATERIALS.md', 'Run-QA.ps1', 'summary.json')]
    for directory in ('qa', 'views', 'evidence'):
        payload.extend(path for path in (ROOT / directory).rglob('*') if path.is_file())
    payload.extend(path for path in (ROOT / 'tools').glob('*') if path.is_file() and path.suffix in ('.py', '.mjs'))
    payload.extend(ROOT / 'native-test' / name for name in ('build.gradle', 'settings.gradle', 'gradle.properties'))
    payload.extend(path for path in (ROOT / 'native-test/src').rglob('*') if path.is_file())
    payload.extend(path for path in (ROOT / 'native-test/gradle').rglob('*') if path.is_file())
    payload.append(ROOT / 'native-test/run/m17-isolated-gametest/server.properties')
    payload = sorted(set(payload))
    manifest = {'id': 'M17', 'status': 'PASS', 'sha256': fingerprint, 'reference_sha256': REFERENCE_HASH, 'files': [{'path': path.relative_to(ROOT).as_posix(), 'sha256': digest(path), 'bytes': path.stat().st_size} for path in payload], 'excluded': ['Gradle caches and builds', 'generated isolated QA worlds', 'compiled QA classes', 'all production assets and player worlds']}
    dump(ROOT / 'delivery-manifest.json', manifest)
    package = ROOT / 'corridor-M17-delivery.zip'
    with zipfile.ZipFile(package, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in [*payload, ROOT / 'delivery-manifest.json']:
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(package) as archive:
        require(archive.testzip() is None, 'ZIP integrity failure')
        require(len(archive.namelist()) == len(payload) + 1, 'ZIP payload count mismatch')
    dump(ROOT / 'package-receipt.json', {'status': 'PASS', 'id': 'M17', 'room_sha256': fingerprint, 'zip': str(package), 'zip_sha256': digest(package), 'zip_bytes': package.stat().st_size, 'payload_files': len(payload) + 1, 'deployed': False, 'player_worlds_modified': False})
    print(json.dumps({'status': 'PASS', 'id': 'M17', 'sha256': fingerprint, 'package_files': len(payload) + 1, 'zip_sha256': digest(package)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
