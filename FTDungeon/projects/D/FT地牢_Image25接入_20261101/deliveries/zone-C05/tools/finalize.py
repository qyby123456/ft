import hashlib
import json
import shutil
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, 'D:/ft工程文件/FT地牢_R5_石匠工坊_20261102/tools')
from nbtio import Structure


def check(condition, message):
    if not condition:
        raise ValueError(message)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def rotate_state(block, turns):
    directions = ['north', 'east', 'south', 'west']
    result = {}
    for key, value in block.properties:
        if key in directions:
            key = directions[(directions.index(key) + turns) % 4]
        elif key == 'facing' and value in directions:
            value = directions[(directions.index(value) + turns) % 4]
        elif key == 'shape' and turns % 2 and value in {'east_west', 'north_south'}:
            value = 'north_south' if value == 'east_west' else 'east_west'
        result[key] = value
    return block.name, tuple(sorted(result.items()))


def rotated_position(position, turns):
    horizontal, vertical, depth = position
    if turns == 0:
        return horizontal, vertical, depth
    if turns == 1:
        return 24 - depth, vertical, horizontal
    if turns == 2:
        return 24 - horizontal, vertical, 24 - depth
    return depth, vertical, 24 - horizontal


def main():
    check(ROOT.name == 'zone-C05', 'Wrong delivery root')
    metadata = load(ROOT / 'room.json')
    sha256 = digest(ROOT / 'room.nbt')
    check(metadata['sha256'] == sha256, 'Stale metadata')
    reports = {name: load(ROOT / 'qa' / (name + '.json')) for name in ('static', 'native', 'render-manifest', 'visual-review')}
    for name, report in reports.items():
        check(report['status'] == 'PASS' and report['sha256'] == sha256, 'Stale or failed report: ' + name)
    native = reports['native']
    check(native['rotations'] == 4 and native['ticks'] >= 320 and native['final_template_sha256_rechecked'], 'Insufficient native evidence')
    check(len(native['initial']) == 4 and len(native['after_stability']) == 4, 'Missing rotation evidence')
    room = Structure.read(ROOT / 'room.nbt')
    captures = []
    for rotation, turns in [('none', 0), ('clockwise_90', 1), ('clockwise_180', 2), ('counterclockwise_90', 3)]:
        path = ROOT / 'qa' / ('native-placed-' + rotation + '.nbt')
        capture = Structure.read(path)
        check(capture.size == (25, 14, 25) and len(capture.blocks) == 8750, 'Invalid capture dimensions or count')
        check(not capture.block_entities and not capture.entities, 'Unexpected captured entities')
        for position, block in room.blocks.items():
            actual = capture.get(rotated_position(position, turns))
            check((actual.name, actual.properties) == rotate_state(block, turns), f'Capture drift {rotation} {position}: {block} -> {actual}')
        captures.append({'rotation': rotation, 'records_compared': 8750, 'path': str(path.relative_to(ROOT)), 'sha256': digest(path), 'status': 'PASS'})
    save(ROOT / 'qa/capture-comparison.json', {'status': 'PASS', 'sha256': sha256, 'captures': captures, 'records_compared': 35000, 'comparison': 'Independent Python coordinate and directional-state comparison against actual Forge world captures, including rail orientation.'})
    render = reports['render-manifest']
    check(render['count'] == 8 and len(render['views']) == 8, 'Missing views')
    for view in render['views']:
        path = ROOT / view['path']
        check(digest(path) == view['sha256'], 'Changed rendered view')
        with Image.open(path) as image:
            image.load()
            check(image.size == (1800, 1300), 'View dimensions changed')
    check(digest(ROOT / 'preview.png') == render['preview_sha256'], 'Changed preview')
    reference = Path(metadata['reference'])
    check(digest(reference) == metadata['reference_sha256'], 'Changed reference image')
    batch = reference.parents[2]
    entries = []
    for filename, key in [('manifest.json', 'files'), ('verify.json', 'records')]:
        report = load(batch / 'docs' / filename)
        entry = next(item for item in report[key] if item['file'].endswith(reference.name))
        check(entry['ok'] and entry['sha256'] == metadata['reference_sha256'], 'Source batch mismatch')
        entries.append({'document': str(batch / 'docs' / filename), 'document_sha256': digest(batch / 'docs' / filename), 'entry': entry})
    (ROOT / 'reference').mkdir(exist_ok=True)
    shutil.copyfile(reference, ROOT / 'reference/C05-design.png')
    save(ROOT / 'reference/source.json', {'id': 'C05', 'reference': str(reference), 'reference_sha256': metadata['reference_sha256'], 'batch_documents': entries, 'image_generation_invoked': False})
    successful_logs = []
    for path in sorted((ROOT / 'qa').glob('forge-*.log')):
        text = path.read_text(encoding='utf-8-sig', errors='replace')
        if 'ZONE_C05_NATIVE_PASS' in text and sha256 in text and 'BUILD SUCCESSFUL' in text:
            successful_logs.append(path)
    check(successful_logs, 'No final successful Forge log')
    final_log = successful_logs[-1]
    qa = {
        'id': 'zone-C05', 'status': 'PASS', 'sha256': sha256, 'reference_sha256': metadata['reference_sha256'],
        'created_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
        'scope': 'Independent producer delivery: static, eight individual software views, isolated Forge four rotations and tick stability.',
        'checks': {'static': 'PASS', 'complete_roof': 'PASS', 'supported_crystals_and_lanterns': 'PASS', 'five_wide_three_high_dry_corner_route': 'PASS', 'two_five_wide_five_high_ports': 'PASS', 'water_containment': 'PASS', 'eight_individual_views': 'PASS', 'native_registry_and_properties': 'PASS', 'native_four_rotations': 'PASS', 'native_320_tick_stability': 'PASS', 'native_capture_independent_comparison': 'PASS', 'entities': 0, 'block_entities': 0, 'inventory_items': 0},
        'reports': {name: {'path': 'qa/' + name + '.json', 'sha256': digest(ROOT / 'qa' / (name + '.json'))} for name in ('static', 'visual-review', 'native', 'render-manifest', 'capture-comparison')},
        'native_log': {'path': str(final_log.relative_to(ROOT)), 'sha256': digest(final_log)},
        'native': {'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations': 4, 'ticks': native['ticks'], 'records_per_snapshot': 35000, 'captured_records_compared': 35000, 'water_sources_per_rotation': 33, 'neighbor_updates': 'UPDATE_ALL and explicit notifications', 'random_tick_speed': 3},
        'correction': {'rejected_template_sha256': digest(ROOT / 'qa/rejected-lantern-overlap/room.nbt'), 'final_template_sha256': sha256, 'issue': 'A decorative crystal bud replaced the second crate top, so its lantern dropped during native placement.', 'fix': 'Move second crate/lantern to x=18,z=22; strengthen static standing-lantern check; rerun all final views and native QA.'},
        'ready_for_receiver_review': True, 'ready_for_unified_integration': False,
        'deployment': False, 'production_save_accessed': False, 'full_modpack_playtest': False,
        'deviations': metadata['deviations'],
        'limitations': ['Software views are not in-game screenshots and do not reproduce shader glow.', 'Isolated Forge GameTest is not full modpack client or multiplayer playtesting.', 'Hall connections, occupancy/persistence/idempotence and deployment require subsequent receiver integration acceptance.']
    }
    save(ROOT / 'qa.json', qa)
    save(ROOT / 'handoff.json', {'id': 'zone-C05', 'source_id': 'C05', 'delivery_root': str(ROOT), 'size': metadata['size'], 'sha256': sha256, 'reference_sha256': metadata['reference_sha256'], 'entrance': metadata['entrance'], 'exit': metadata['exit'], 'interior': metadata['interior'], 'facing': 'north', 'status': 'PASS', 'four_piece_bundle': ['room.nbt', 'room.json', 'preview.png', 'qa.json'], 'native': qa['native'], 'producer_evidence_only': True, 'ready_for_receiver_review': True, 'ready_for_unified_integration': False, 'deployed': False})
    files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'handoff.json', 'README.md', 'reference/C05-design.png', 'reference/source.json', 'qa/static.json', 'qa/native.json', 'qa/visual-review.json', 'qa/render-manifest.json', 'qa/capture-comparison.json']
    files.extend(view['path'] for view in render['views'])
    files.extend(capture['path'] for capture in captures)
    files.extend(['preview/four-interior.png', 'preview/four-exterior.png', 'preview/layout.png', str(final_log.relative_to(ROOT))])
    save(ROOT / 'delivery-manifest.json', {'id': 'zone-C05', 'status': 'PASS', 'sha256': sha256, 'files': [{'path': name, 'bytes': (ROOT / name).stat().st_size, 'sha256': digest(ROOT / name)} for name in files]})
    print(json.dumps({'status': 'PASS', 'id': 'zone-C05', 'sha256': sha256, 'captures_compared': 35000, 'views': 8}))


if __name__ == '__main__':
    main()
