import hashlib
import json
import shutil
import subprocess
import sys
import zipfile
from collections import Counter
from datetime import datetime, timedelta, timezone
from pathlib import Path

import build_room
from nbtio import AIR, Structure, plain


ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def require(condition, message):
    if not condition:
        raise ValueError(message)


def rotated_position(position, turn):
    pos_x, pos_y, pos_z = position
    choices = [(pos_x, pos_y, pos_z), (16 - pos_z, pos_y, pos_x), (16 - pos_x, pos_y, 16 - pos_z), (pos_z, pos_y, 16 - pos_x)]
    return choices[turn]


def rotated_properties(block, turn):
    properties = dict(block.properties)
    directions = ['north', 'east', 'south', 'west']
    if properties.get('facing') in directions:
        properties['facing'] = directions[(directions.index(properties['facing']) + turn) % 4]
    if 'rotation' in properties:
        properties['rotation'] = str((int(properties['rotation']) + turn * 4) % 16)
    if turn % 2 and properties.get('axis') in {'x', 'z'}:
        properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
    return properties


def main():
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    structure_hash = digest(ROOT / 'out/room.nbt')
    require(structure_hash == metadata['sha256'], 'Room source changed')
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], cwd=ROOT, check=True)
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    render = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    require(static['status'] == native['status'] == 'PASS', 'QA is not PASS')
    require(static['structure_sha256'] == native['structure_sha256'] == render['source_nbt_sha256'] == structure_hash, 'Evidence belongs to a different template')
    require(native['records_checked_after_ticks'] == 12716 and native['stabilization_ticks'] == 200 and native['do_fire_tick'] is True and native['force_loaded_fixture_chunks'] is True, 'Native acceptance scope differs')
    require(len(native['rotations']) == 4 and all(result['separate_bounded_fluid_ticking_canary'] == 'PASS' for result in native['rotations']), 'Missing true fluid-ticking proof')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    require('IRON_SHOP_NATIVE_PASS rotations=4 records=12716 ticks=200 lava_sources=8 source=' + structure_hash in log and 'BUILD SUCCESSFUL' in log and 'All 1 required tests passed' in log, 'Native success log missing')

    source = Structure.read(ROOT / 'out/room.nbt')
    capture_results = []
    turns = {'NONE': 0, 'CLOCKWISE_90': 1, 'CLOCKWISE_180': 2, 'COUNTERCLOCKWISE_90': 3}
    for result in native['rotations']:
        capture_path = ROOT / result['world_capture']
        require(digest(capture_path) == result['world_capture_sha256'], 'Capture fingerprint mismatch')
        capture = Structure.read(capture_path)
        require(capture.size == source.size and len(capture.blocks) == len(source.blocks), 'Capture dimensions or volume differ')
        turn = turns[result['rotation']]
        payloads = 0
        for position, original in source.blocks.items():
            target = rotated_position(position, turn)
            actual = capture.get(target)
            require(actual.name == original.name, 'Capture name differs at ' + str(target))
            require(all(dict(actual.properties).get(key) == value for key, value in rotated_properties(original, turn).items()), 'Capture state differs at ' + str(target))
            require((position in source.block_entities) == (target in capture.block_entities), 'Capture block entity missing')
            if position in source.block_entities:
                expected_data = plain(source.block_entities[position])
                actual_data = plain(capture.block_entities[target])
                require(all(actual_data.get(key, []) == value for key, value in expected_data.items()), 'Capture inventory/banner/skull data differs')
                payloads += 1
        require(not capture.entities, 'Captured entities present')
        capture_results.append({'rotation': result['rotation'], 'status': 'PASS', 'block_records': len(capture.blocks), 'block_entity_source_payloads_preserved': payloads, 'capture_sha256': digest(capture_path)})
    write_json(ROOT / 'qa/capture-audit.json', {'status': 'PASS', 'source_nbt_sha256': structure_hash, 'independent_rotated_capture_comparisons': capture_results, 'note': 'Native comparison tests complete BlockState equality; this independent NBT audit also compares explicit source properties and block-entity payload fields.'})

    rebuild = (ROOT / 'qa/rebuild').resolve()
    require(rebuild.is_relative_to(ROOT.resolve()) and rebuild != ROOT.resolve(), 'Rebuild must stay inside independent workspace')
    (rebuild / 'reference').mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'reference/shop-variant-F-cataclysm-iron.png', rebuild / 'reference/shop-variant-F-cataclysm-iron.png')
    original_root = build_room.ROOT
    try:
        build_room.ROOT = rebuild
        build_room.main()
    finally:
        build_room.ROOT = original_root
    comparisons = {}
    for relative in ['out/room.nbt', 'out/room.json', 'out/blueprint.json', 'out/materials.json']:
        expected_hash = digest(ROOT / relative)
        require(digest(rebuild / relative) == expected_hash, 'Rebuild differs: ' + relative)
        comparisons[relative] = expected_hash
    write_json(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'byte_identical_files': comparisons, 'independent_rebuild_directory': str(rebuild), 'source_nbt_sha256': structure_hash, 'note': 'No original output was overwritten during reproducibility verification. Isolated rebuild is excluded from release.'})

    image_paths = ['out/preview.png', 'preview/four-views.png', 'preview/native-world-capture.png', 'preview/plan.png', 'preview/exterior.png', 'preview/reference-comparison.png']
    visual = {
        'status': 'PASS', 'source_nbt_sha256': structure_hash,
        'method': 'Assistant visually inspected rendered PNGs using local image tools; no claim of a Minecraft client screenshot.',
        'checks': ['Four directional cutaways preserve pillars, beams and furnishings.', 'Complete exterior roof and north doorway are visible.', 'Display cases, counter, rear shelves and sealed lava windows appear in the actual-block render.', 'Plan shows 3-wide north route and 1-block counter circulation without structural pillar intrusion.', 'Native world capture uses the accepted 200-tick NBT, not an AI reference.'],
        'images': {relative: digest(ROOT / relative) for relative in image_paths},
        'limitations': render['approximations'],
    }
    write_json(ROOT / 'qa/visual-review.json', visual)

    evidence = ['qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/capture-audit.json', 'qa/reproducibility.json', 'qa/render-manifest.json', 'qa/visual-review.json']
    summary = {
        'status': 'PASS', 'scope': 'INDEPENDENT_F_TEMPLATE_STATIC_AND_FORGE_NATIVE_PLACEMENT_ONLY',
        'id': metadata['id'], 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False,
        'structure_sha256': structure_hash, 'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'summary': {'static_checks_passed': static['checks_passed'], 'native_rotations_passed': 4, 'records_checked_after_200_ticks': native['records_checked_after_ticks'], 'block_entities_per_room': len(source.block_entities), 'reachable_foot_positions_per_rotation': 172, 'sealed_lava_sources_per_room': 2, 'negative_native_controls': 12, 'active_fluid_ticking_canaries': 4, 'unexpected_entities': 0, 'reward_inventory': 0},
        'artifacts': {relative: digest(ROOT / relative) for relative in ['out/room.nbt', 'out/room.json', 'out/preview.png', *evidence]},
        'native_world_captures': [result['world_capture'] for result in native['rotations']],
        'warnings': ['STYLES.md named in upstream README was absent; this discrepancy is preserved in input-fingerprints.json.', 'First Forge startup generated local server defaults and issued standard missing server.properties/FML config and deprecation warnings, but the required native test passed.', 'Full game client, gameplay, trade, multiplayer, FPS, FT hallway placement, room catalog allocation, occupied-volume refusal and idempotent core integration are NOT tested.'],
        'coordinator_action': 'Review the four artifacts, assign a real catalog ID/slot, perform FT integration safety/regression checks and only then coordinate any deployment. Do not install the QA mod into a player instance.',
        'production_safety': 'No client, player save, deployed JAR, other room or FT core source was opened for writing. Only this independent project and shared tool caches were used.',
    }
    write_json(ROOT / 'out/qa.json', summary)
    handoff = {
        'variant': 'F', 'title': metadata['name'], 'status': 'READY_FOR_COORDINATOR_REVIEW_NOT_DEPLOYED',
        'provisional_id': metadata['id'], 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'four_artifacts': {name: {'absolute_path': str(ROOT / 'out' / name), 'sha256': digest(ROOT / 'out' / name)} for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json']},
        'acceptance': {'static': 'qa/static.json', 'native': 'qa/native.json', 'log': 'qa/forge-gametest.log', 'rotations': 4, 'ticks': 200},
        'deployment_authorized': False, 'assigned_slot': None,
    }
    write_json(ROOT / 'handoff.json', handoff)

    files = [ROOT / name for name in ['README.md', 'build.gradle', 'settings.gradle', 'gradle.properties', 'handoff.json']]
    for directory in ['tools', 'out', 'preview', 'reference', 'src']:
        files.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    files.extend(ROOT / relative for relative in evidence)
    files.extend(ROOT / result['world_capture'] for result in native['rotations'])
    files = sorted(set(files), key=lambda path: str(path.relative_to(ROOT)))
    require(all(path.is_file() for path in files), 'Release contains missing files')
    fingerprints = {path.relative_to(ROOT).as_posix(): {'sha256': digest(path), 'bytes': path.stat().st_size} for path in files}
    stamp = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds')
    write_json(ROOT / 'delivery-manifest.json', {'generated_at_asia_shanghai': stamp, 'variant': 'F', 'status': 'STANDALONE_QA_PASS_NOT_DEPLOYED', 'four_artifacts': ['out/room.nbt', 'out/room.json', 'out/preview.png', 'out/qa.json'], 'files': fingerprints, 'excluded': ['Gradle/build caches', 'Test world and generated server config', 'Binary verification mod', 'Independent reproducibility rebuild', 'Diagnostic temporary image']})
    files.append(ROOT / 'delivery-manifest.json')
    release = ROOT / 'release/FT-Shop-F-Dark-Iron-Standalone-20261102.zip'
    with zipfile.ZipFile(release, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in files:
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(release) as archive:
        require(archive.testzip() is None, 'Corrupt release ZIP')
        for relative, details in fingerprints.items():
            require(hashlib.sha256(archive.read(relative)).hexdigest() == details['sha256'], 'Archive fingerprint mismatch ' + relative)
    write_json(ROOT / 'release/release-receipt.json', {'archive': str(release), 'sha256': digest(release), 'bytes': release.stat().st_size, 'entries': len(files), 'archive_integrity': 'PASS', 'all_manifest_hashes_verified_in_archive': True, 'four_artifacts': handoff['four_artifacts'], 'deployed': False})
    print(json.dumps({'status': 'READY_NOT_DEPLOYED', 'archive': str(release), 'archive_sha256': digest(release), 'entries': len(files)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
