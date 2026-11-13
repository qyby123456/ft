import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from build_room import REFERENCE, REFERENCE_HASH, ROOT, digest, write_json
from nbtio import Structure


def main():
    stamp = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    reports = {name: json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8')) for name in ('static', 'native', 'render')}
    fingerprint = digest(ROOT / 'room.nbt')
    if metadata['sha256'] != fingerprint or digest(REFERENCE) != REFERENCE_HASH:
        raise ValueError('Final source/reference fingerprint mismatch')
    for name, report in reports.items():
        if report['status'] != 'PASS' or report['structure_sha256'] != fingerprint:
            raise ValueError(f'Cannot finalize: {name} report stale or failed')
    native = reports['native']
    if len(native['rotations']) != 4 or native['stabilization_ticks'] < 200 or native['records_checked_after_ticks'] != 11560:
        raise ValueError('Missing final four-rotation 200-tick evidence')
    for rotation in native['rotations']:
        capture = ROOT / rotation['world_capture']
        if not rotation['capture_matches_source'] or digest(capture) != rotation['world_capture_sha256']:
            raise ValueError('Native capture hash mismatch')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    if f'R3_BARRACKS_NATIVE_PASS: rotations=4/4 records=11560 ticks=200 stock=0 source={fingerprint}' not in log or 'BUILD SUCCESSFUL' not in log:
        raise ValueError('Native Gradle log not successful for this exact final NBT')
    room = Structure.read(ROOT / 'room.nbt')
    room.write(ROOT / 'qa/python-roundtrip.nbt')
    if digest(ROOT / 'qa/python-roundtrip.nbt') != fingerprint:
        raise ValueError('Deterministic NBT roundtrip mismatch')
    core = Path('D:/ft工程文件/FT-Dungeon-Mobs-Forge-1.20.1')
    dimension_path = core / 'src/main/resources/data/ftdungeonmobs/dimension_type/dungeon.json'
    rules_path = core / 'ROOM_RULES.md'
    dimension = json.loads(dimension_path.read_text(encoding='utf-8'))
    dangerous = [block.name for block in room.blocks.values() if block.name.endswith('_bed') or block.name == 'minecraft:respawn_anchor']
    if dangerous:
        raise ValueError('Unsafe interactive bed/anchor found')
    environment = {'reviewed_at': stamp, 'scope': 'Read-only target integration environment review, not core integration or runtime event testing',
                   'dimension_source': str(dimension_path), 'dimension_sha256': digest(dimension_path), 'dimension_snapshot': dimension,
                   'rules_source': str(rules_path), 'rules_sha256': digest(rules_path), 'source_documents_say_block_right_click_remains_open': True,
                   'interactive_bed_blocks_in_final_template': 0, 'respawn_anchor_blocks_in_final_template': 0,
                   'decision': 'Actual beds can explode where bed_works=false. Use supported dark-oak platform, red-carpet mattress and white-carpet pillow; no core change or interaction dependency.'}
    write_json(ROOT / 'qa/environment.json', environment)
    attempts = []
    for prior in sorted((ROOT / 'qa').glob('previous-*-native.json')):
        data = json.loads(prior.read_text(encoding='utf-8'))
        attempts.append({'report': str(prior.relative_to(ROOT)), 'status': data['status'], 'source_sha256': data.get('structure_sha256'),
                         'error': data.get('error'), 'counts_as_final_acceptance': False})
    required = ['room.nbt', 'room.json', 'preview.png', 'blueprint.json', 'qa/static.json', 'qa/native.json', 'qa/render.json', 'qa/environment.json', 'qa/forge-gametest.log', 'qa/python-roundtrip.nbt',
                'preview/four-views.png', 'preview/exterior.png', 'preview/plan.png', 'preview/native-world-capture.png']
    file_hashes = {name: digest(ROOT / name) for name in required}
    for entry in native['rotations']:
        name = Path(entry['world_capture']).as_posix()
        file_hashes[name] = digest(ROOT / name)
    qa = {'id': 'R3', 'name': '守卫营房·废弃兵营', 'status': 'PASS', 'finalized_at': stamp, 'structure_sha256': fingerprint,
          'size': [17, 10, 17], 'records': len(room.blocks), 'non_air': reports['static']['non_air'], 'block_entities': len(room.block_entities),
          'reference': {'path': str(REFERENCE), 'sha256': REFERENCE_HASH, 'pixels': [1536, 1024]},
          'static': reports['static'], 'native': native, 'render': reports['render'], 'target_environment_review': environment,
          'python_nbt_roundtrip_identical': True, 'artifact_sha256': file_hashes, 'earlier_attempts': attempts,
          'scope': {'writes': str(ROOT), 'deployed': False, 'ft_core_modified': False, 'player_saves_opened_or_modified': False,
                    'shared_catalog_or_queue_modified': False, 'parallel_deliveries_modified': False, 'new_paid_image_generation': False},
          'limitations': ['Forge QA uses the isolated overworld fixture at Y=288, not the production FT dimension or a full-modpack client.',
                          'No FT catalog/slot, lobby walkway, production occupancy rejection, generation idempotence, existing player modifications, return gate or multiplayer verification.',
                          'Actual sleep/respawn and weapons/items are intentionally not provided. Patterned banners use simplified software-render geometry.',
                          'Preview removes roof/ceiling beams/near walls in rendering memory only; final NBT keeps the complete roof and all supports.']}
    write_json(ROOT / 'qa.json', qa)
    manifest_files = dict(file_hashes)
    manifest_files['qa.json'] = digest(ROOT / 'qa.json')
    for name in ('README.md', 'HANDOFF.md'):
        if (ROOT / name).is_file():
            manifest_files[name] = digest(ROOT / name)
    (ROOT / 'SHA256SUMS.txt').write_text(''.join(f'{value}  {name}\n' for name, value in manifest_files.items()), encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'sha256': fingerprint, 'static_checks': len(reports['static']['checks']), 'rotations': 4,
                      'ticks': 200, 'records_checked': 11560, 'files_hashed': len(manifest_files)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
