import hashlib
import json
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    metadata = read('room.json')
    source_hash = digest(ROOT / 'room.nbt')
    assert metadata['sha256'] == source_hash
    static = read('qa/static.json')
    native = read('qa/native.json')
    review = read('qa/four-view-review.json')
    reference = read('qa/reference.json')
    render = read('qa/render.json')
    assert all(report['status'] == 'PASS' for report in (static, native, review, reference))
    assert all(report['room_sha256'] == source_hash for report in (static, native, review))
    assert native['stabilization_ticks'] >= 240
    assert native['records_checked_after_ticks'] == 9152
    assert native['negative_probes'] == 12
    assert {result['rotation'] for result in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}
    assert all(result['status'] == 'PASS' and result['state_drift'] == 0 for result in native['rotations'])
    assert len(render['views']) == 9 and all(view['sourceHash'] == source_hash for view in render['views'])
    assert len(review['findings']) == 8 and review['all_eight_images_individually_opened'] is True
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'CORRIDOR_S10_NATIVE_PASS rotations=4 ticks=240 records=9152 negative_probes=12 sha256=' + source_hash in log
    assert 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    for view in render['views']:
        with Image.open(ROOT / view['path']) as image:
            image.verify()
    evidence_paths = [
        'room.nbt', 'room.json', 'preview.png', 'summary.json', 'README.md',
        'qa/static.json', 'qa/native.json', 'qa/reference.json', 'qa/render.json', 'qa/materials.json',
        'qa/four-view-review.json', 'qa/forge-gametest.log',
        'build.gradle', 'settings.gradle', 'gradle.properties',
        *[view['path'] for view in render['views'] if view['path'] != 'preview.png'],
        'views/interior-four-views.png', 'views/exterior-four-views.png', 'views/reference-comparison.png',
        *[path.relative_to(ROOT).as_posix() for parent in ('tools', 'src') for path in (ROOT / parent).rglob('*') if path.is_file()],
    ]
    evidence = {relative: {'sha256': digest(ROOT / relative), 'bytes': (ROOT / relative).stat().st_size} for relative in sorted(set(evidence_paths))}
    result = {
        'id': 'corridor-S10', 'status': 'PASS', 'producer_qa_status': 'PASS',
        'completed_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'room_sha256': source_hash, 'reference_sha256': metadata['reference_sha256'],
        'reference': metadata['reference'], 'size': metadata['size'],
        'static': {'status': 'PASS', 'report': 'qa/static.json', 'explicit_records': 2288, 'non_air_records': 1355},
        'four_views': {'status': 'PASS', 'review': 'qa/four-view-review.json', 'renderer': render['renderer'],
                       'interior': {side: f'views/interior-{side}.png' for side in ('north', 'south', 'west', 'east')},
                       'exterior': {side: f'views/exterior-{side}.png' for side in ('north', 'south', 'west', 'east')},
                       'source_nbt_hash_bound': True, 'separate_images_individually_reviewed': True},
        'forge_native': {'status': 'PASS', 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log',
                         'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations_passed': 4,
                         'stabilization_ticks': 240, 'records_checked_after_ticks': 9152,
                         'state_drift': 0, 'negative_probes_detected': 12, 'isolated_test_world_only': True},
        'ready_for_receiver_review': True, 'ready_for_unified_integration': False,
        'receiver_acceptance_required': True, 'deployed': False, 'production_save_access': False,
        'limitations': ['Software renders are not Minecraft client screenshots.',
                        'Independent receiving-side frozen-bundle visual/native acceptance remains required.',
                        'Hall connections, full-volume occupation rejection, persistent idempotence and deployment are not tested.'],
        'evidence': evidence,
    }
    (ROOT / 'qa.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    manifest = {**evidence, 'qa.json': {'sha256': digest(ROOT / 'qa.json'), 'bytes': (ROOT / 'qa.json').stat().st_size}}
    (ROOT / 'qa/evidence-manifest.json').write_text(json.dumps({'id': 'corridor-S10', 'room_sha256': source_hash, 'files': manifest}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    archive = ROOT / 'corridor-S10-delivery.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as output:
        for relative in sorted({*manifest, 'qa/evidence-manifest.json'}):
            output.write(ROOT / relative, 'corridor-S10/' + relative)
    with zipfile.ZipFile(archive) as output:
        assert output.testzip() is None
    (ROOT / 'corridor-S10-delivery.zip.sha256').write_text(digest(archive) + '  corridor-S10-delivery.zip\n', encoding='utf-8')
    print(json.dumps({'id': 'corridor-S10', 'status': 'PASS', 'room_sha256': source_hash, 'package': str(archive), 'package_bytes': archive.stat().st_size}, ensure_ascii=False))


if __name__ == '__main__':
    main()
