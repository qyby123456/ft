import json
import zipfile
from pathlib import Path

from build_room import REFERENCE, REFERENCE_HASH, ROOT, construct, digest, write_json
from nbtio import Structure, plain, state


def load(path):
    return json.loads((ROOT / path).read_text(encoding='utf-8'))


def require(condition, message):
    if not condition:
        raise ValueError(message)


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = load('room.json')
    require(metadata['sha256'] == fingerprint, 'Metadata fingerprint mismatch')
    for report_path in ('qa/static.json', 'qa/native.json', 'qa/render-manifest.json', 'qa/visual-review.json'):
        evidence = load(report_path)
        require(evidence['status'] == 'PASS' and evidence['source_nbt_sha256'] == fingerprint, 'Missing/stale/failed evidence: ' + report_path)
    native = load('qa/native.json')
    invocation = load('qa/native-invocation.json')
    require(invocation['fresh_test_world'] is True and invocation['run_directory'] == native['isolated_run_directory'], 'Native run is not the fresh isolated invocation')
    require(native['bootstrap'] is False and len(native['rotations']) == 4, 'Final strict four-direction native test required')
    require(all(entry['fixture_world_origin'][1] == 256 for entry in native['rotations']), 'Final fixtures must be isolated above natural terrain')
    require(native['stabilization_ticks'] == 240 and native['stable_snapshot_ticks'] == [200, 240], 'Missing stability snapshots')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    require('R4_NATIVE_GAME_TEST_PASS:' in log and 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log, 'Native log is not a passing completed run')
    require(digest(REFERENCE) == REFERENCE_HASH and digest(ROOT / 'reference/R4-underground-spring-cave.png') == REFERENCE_HASH, 'Reference fingerprint mismatch')
    source = Structure.read(ROOT / 'room.nbt')
    captures = []
    source_entities = {position: plain(data) for position, data in source.block_entities.items()}
    for rotation in ('none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90'):
        relative = f'qa/native-placed-{rotation}.nbt'
        capture = Structure.read(ROOT / relative)
        require(capture.size == source.size and capture.blocks == source.blocks, 'Capture states differ: ' + rotation)
        require({position: plain(data) for position, data in capture.block_entities.items()} == source_entities, 'Capture inventories differ: ' + rotation)
        require(not capture.entities, 'Capture contains entities')
        captures.append({'rotation': rotation, 'records': len(capture.blocks), 'all_states_equal': True, 'empty_block_entities_equal': True, 'path': relative, 'sha256': digest(ROOT / relative)})
    write_json(ROOT / 'qa/native-capture-comparison.json', {'status': 'PASS', 'source_nbt_sha256': fingerprint, 'records_compared': 4 * len(source.blocks), 'captures': captures, 'note': 'World states inverse-rotated into template coordinates; empty-list NBT subtype/order is not gameplay content.'})
    rebuilt, _ = construct()
    equilibration = load('qa/equilibration.json')
    for change in equilibration['changes']:
        after = change['after']
        rebuilt.set(tuple(change['position']), state(after['name'], **after['properties']))
    rebuilt.write(ROOT / 'qa/rebuilt-final.nbt')
    require(digest(ROOT / 'qa/rebuilt-final.nbt') == fingerprint, 'Deterministic rebuild differs')
    write_json(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'source_nbt_sha256': fingerprint, 'rebuilt_sha256': digest(ROOT / 'qa/rebuilt-final.nbt'), 'method': 'Construct geometry and apply nine native-verified equilibration substitutions; canonical empty barrel payloads; deterministic gzip mtime=0.'})
    evidence_paths = ['room.nbt', 'room.json', 'preview.png', 'blueprint.json', 'materials.json', 'qa/static.json', 'qa/native.json', 'qa/native-invocation.json', 'qa/forge-gametest.log', 'qa/bootstrap-source.nbt', 'qa/bootstrap-native.json', 'qa/bootstrap-forge-gametest.log', 'qa/equilibration.json', 'qa/render-manifest.json', 'qa/visual-review.json', 'qa/native-capture-comparison.json', 'qa/reproducibility.json', 'qa/rebuilt-final.nbt', 'preview/four-views.png', 'preview/exterior.png', 'preview/plan.png', 'preview/native-capture.png', 'reference/R4-underground-spring-cave.png']
    evidence_paths.extend(entry['path'] for entry in captures)
    evidence_paths.append('qa/failed-underground-moat-diagnostics.json')
    for evidence in evidence_paths:
        require((ROOT / evidence).is_file(), 'Missing evidence: ' + evidence)
    write_json(ROOT / 'qa.json', {
        'status': 'PASS', 'room_id': 'R4', 'source_nbt_sha256': fingerprint,
        'reference': {'path': str(REFERENCE), 'sha256': REFERENCE_HASH, 'pixels': [1536, 1024]},
        'dimensions': metadata['size'], 'entrance_feet': metadata['entrance'], 'safe_interior_feet': metadata['interior'], 'facing': metadata['facing'],
        'static': load('qa/static.json')['checks'], 'static_negative_probes_rejected': len(load('qa/static.json')['negative_probes']),
        'native': {key: native[key] for key in ('minecraft', 'forge', 'java', 'stabilization_ticks', 'stable_snapshot_ticks', 'records_checked_after_ticks', 'rotations', 'negative_blocked_path_probes', 'negative_perforated_pool_probes', 'negative_missing_dripstone_anchor_probes')},
        'native_capture_comparison': 'PASS', 'deterministic_rebuild': 'PASS', 'visual_review': 'PASS',
        'water_containment': 'Native bounded envelope, complete foundation, dry routes and external one-block liquid moat checked in four rotations after 240 scheduled ticks.',
        'support_roof_entrance': 'Every placed state canSurvive, four full-height columns, six piers, complete roof and north 3x3 opening pass.',
        'attempts': ['First bootstrap compile failed due to wrong StructureTemplate.load lookup argument; corrected; log retained.', 'Bootstrap native four-way run passed; seven water-fill cells and two fence connections were adopted as explicit states.', 'Two strict reruns correctly rejected old teardown drops, including asynchronous loading of saved fixture entities. Clearing suppresses shape/drop updates and the runner now creates a fresh timestamped isolated world for every run; no old world is deleted.', 'Another fresh underground fixture run found external moat water from natural terrain. Final fixtures are at world Y=256, isolated from terrain; the same full external-moat and zero-new-entity/drop checks remain enforced.', 'Final canonical source rebuilt deterministically and strict four-way QA rerun.'],
        'scope': {'independent_delivery': True, 'ft_core_modified': False, 'shared_incoming_modified': False, 'production_save_modified': False, 'production_jar_deployed': False, 'paid_image_generation_called': False},
        'limitations': native['limitations'] + load('qa/render-manifest.json')['limitations'],
        'evidence': {path: {'sha256': digest(ROOT / path), 'bytes': (ROOT / path).stat().st_size} for path in evidence_paths},
    })
    formal_paths = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'HANDOFF.md']
    hashes = '\n'.join(f'{digest(ROOT / path)}  {path}' for path in formal_paths) + '\n'
    (ROOT / 'SHA256SUMS.txt').write_text(hashes, encoding='utf-8')
    release = ROOT / 'release/FT_R4_UndergroundSpring_20261102.zip'
    release.parent.mkdir(parents=True, exist_ok=True)
    allowed = set(formal_paths + ['SHA256SUMS.txt', 'blueprint.json', 'materials.json', 'build.gradle', 'settings.gradle', 'gradle.properties'])
    allowed.update(path.relative_to(ROOT).as_posix() for base in ('tools', 'src', 'reference', 'preview') for path in (ROOT / base).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    allowed.update(path.relative_to(ROOT).as_posix() for path in (ROOT / 'qa').iterdir() if path.is_file() and path.suffix in ('.json', '.nbt', '.log'))
    with zipfile.ZipFile(release, 'w', zipfile.ZIP_DEFLATED) as archive:
        for relative in sorted(allowed):
            archive.write(ROOT / relative, relative)
    write_json(ROOT / 'release/receipt.json', {'status': 'PASS', 'zip': release.name, 'sha256': digest(release), 'bytes': release.stat().st_size, 'room_nbt_sha256': fingerprint, 'excludes': ['GameTest world', 'Gradle caches/build outputs', 'installable mod JAR', 'all production save and mod files']})
    print(json.dumps({'status': 'DELIVERY_PASS', 'room_nbt_sha256': fingerprint, 'zip_sha256': digest(release)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
