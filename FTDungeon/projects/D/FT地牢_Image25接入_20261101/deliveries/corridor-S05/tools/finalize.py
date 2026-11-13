import json
from datetime import datetime
from zoneinfo import ZoneInfo

from PIL import Image

from qa_static import ROOT, REFERENCE, REFERENCE_HASH, digest, write_json
from nbtio import Structure


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = load('room.json')
    static = load('qa/static.json')
    native = load('qa/native.json')
    render = load('qa/render-manifest.json')
    lodestone = load('qa/lodestone.json')
    visual = load('qa/visual-review.json')
    reports = {'static': static, 'native_forge': native, 'four_view_render': render, 'lodestone': lodestone, 'visual_review': visual}
    assert metadata['sha256'] == fingerprint and digest(REFERENCE) == REFERENCE_HASH
    assert all(report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint for report in reports.values())
    assert len(native['rotations']) == 4 and native['records_checked_after_ticks'] == 7040 and native['stable_snapshot_ticks'] == [200, 240]
    assert render['native_rotation_renders'] == 4
    source = Structure.read(ROOT / 'room.nbt')
    capture_comparisons = []
    for rotation in ('none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90'):
        relative = 'qa/native-placed-' + rotation + '.nbt'
        capture = Structure.read(ROOT / relative)
        assert capture.size == source.size and capture.blocks.keys() == source.blocks.keys()
        for position, wanted in source.blocks.items():
            actual = capture.get(position)
            assert actual.name == wanted.name and all(dict(actual.properties).get(key) == value for key, value in wanted.properties)
        capture_comparisons.append({'path': relative, 'sha256': digest(ROOT / relative), 'records_match_source_with_native_defaults': 1760})
    for output in render['outputs'] + lodestone['outputs']:
        path = ROOT / output['path']
        assert path.is_file() and digest(path) == output['sha256']
        with Image.open(path) as image:
            image.verify()
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'S05_NATIVE_GAME_TEST_PASS: rotations=4/4 records=7040 ticks=240 sha256=' + fingerprint in log and 'BUILD SUCCESSFUL' in log
    result = {
        'id': metadata['id'], 'design_id': 'S05', 'name': metadata['name'], 'status': 'PASS',
        'verified_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(),
        'sha256': fingerprint, 'source_nbt_sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'delivery_files': ['room.nbt', 'room.json', 'preview.png', 'qa.json'],
        'size': [11, 10, 16], 'entrance': [5, 2, 0], 'interior': [5, 2, 3], 'exit': [5, 2, 15],
        'checks': {name: {'status': report['status'], 'report': {'static': 'qa/static.json', 'native_forge': 'qa/native.json', 'four_view_render': 'qa/render-manifest.json', 'lodestone': 'qa/lodestone.json', 'visual_review': 'qa/visual-review.json'}[name]} for name, report in reports.items()},
        'static_checks_passed': len(static['checks']),
        'forge': {'minecraft': native['minecraft'], 'version': native['forge'], 'rotation_count': 4, 'records_checked': 7040, 'stable_snapshot_ticks': [200, 240], 'water_sources_per_rotation': 52, 'dry_feet_per_rotation': 48, 'native_captures': capture_comparisons, 'negative_probes': native['negative_probes']},
        'liquid_sealing': 'PASS: 52 one-block-deep source blocks remain within bed and side/threshold seals; external 1-block moat is dry after scheduled water updates.',
        'walkability': 'PASS: 3-wide by at least 3-high dry level passage, all 48 feet positions connected north to south in all rotations.',
        'supports': 'PASS: eight grounded beam columns, four roof-anchored cage chains, six supported wall torches; no state drift, unsupported blocks or item drops.',
        'four_view_evidence': ['preview/north.png', 'preview/south.png', 'preview/west.png', 'preview/east.png', 'preview/four-views.png', 'preview/exterior.png', 'preview/exterior-south.png', 'preview/native-four-views.png', 'preview/lodestone-interior.png', 'preview/plan.png'],
        'prior_failed_attempt': {'log': 'qa/forge-20261103-101323.log', 'cause': 'Missing pack.mcmeta excluded the QA fixture data pack, so GameTest could not find empty.nbt.', 'resolved_by': 'Added pack_format 15 metadata, relaunched a fresh isolated world; subsequent test and Gradle build PASS.'},
        'deployment': {'deployed': False, 'production_jar_changed': False, 'player_saves_opened': False, 'core_slot_assigned': False, 'ready_for_coordinator_review': True},
        'limitations': ['Software and Lodestone previews are rendered from actual final or native-capture NBT, not AI replacements or Minecraft client screenshots.', 'Forge evidence is an isolated 1.20.1/47.4.10 server; full-modpack gameplay, FPS and multiplayer were not tested.', 'No core integration, hallway connection, production-world occupancy/idempotence or deployment was performed.', 'Water simulation ran with scheduled ticks; random ticks and mob spawning were disabled.', 'Reference adaptation: dry central route replaces fully flooded floor and open arch replaces closed far door.'],
    }
    write_json(ROOT / 'qa.json', result)
    included = [*ROOT.glob('*.json'), ROOT / 'room.nbt', ROOT / 'preview.png', ROOT / 'README.md']
    included.extend(path for folder in ('tools', 'preview', 'qa') for path in (ROOT / folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    included.extend((ROOT / 'forge' / name) for name in ('build.gradle', 'settings.gradle', 'gradle.properties'))
    included.extend(path for path in (ROOT / 'forge/src').rglob('*') if path.is_file())
    paths = sorted(set(path for path in included if path.is_file()), key=lambda path: str(path.relative_to(ROOT)))
    (ROOT / 'SHA256SUMS.txt').write_text(''.join(digest(path) + '  ' + path.relative_to(ROOT).as_posix() + '\n' for path in paths), encoding='utf-8')
    print('S05_DELIVERY_PASS sha256=' + fingerprint + ' files_hashed=' + str(len(paths)))


if __name__ == '__main__':
    main()
