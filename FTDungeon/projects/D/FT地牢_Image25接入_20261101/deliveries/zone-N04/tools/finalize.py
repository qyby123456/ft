import hashlib
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_SHA = '14c978b39244c763c15356388bd1176b1b2452ef37f3a5bed6963890be7cc8d5'
REFERENCE_SHA = '81f17850087057a9a0c1925f1fe747220c5a8e163003799c55fa5a97a1b756c3'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def write(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def entry(relative):
    file_path = ROOT / relative
    return {'path': relative, 'bytes': file_path.stat().st_size, 'sha256': digest(file_path)}


metadata = read('room.json')
assert metadata['id'] == 'zone-N04' and metadata['sha256'] == EXPECTED_SHA == digest(ROOT / 'room.nbt')
assert metadata['reference_sha256'] == REFERENCE_SHA == digest(Path(metadata['reference']))
assert metadata['size'] == [35, 16, 35] and metadata['runtime_teleport'] is False and metadata['deployment'] == 'not_deployed'
static = read('qa/static.json')
native = read('qa/forge-native.json')
result = read('qa/forge-result.json')
visual = read('qa/visual-review.json')
render = read('views/manifest.json')
provenance = read('qa/source-provenance.json')
for report in [static, native, result, visual, render]:
    assert report['id'] == 'zone-N04' and report['status'] == 'PASS' and report['sha256'] == EXPECTED_SHA
assert not static['errors'] and static['four_ports_reachable'] and static['safe_point_reachable']
assert static['records'] == 19600 and static['cross_route_cells'] == 325 and static['reachable_feet'] == 813
assert result['exit_code'] == 0 and native['ticks_observed'] == 240 and native['forced_neighbor_updates']
assert {rotation['rotation'] for rotation in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}
for rotation in native['rotations']:
    assert rotation['block_states_unchanged'] == 19600 and rotation['origin'][1] == 200
    assert rotation['native_collision_reachable_feet'] == 813 and rotation['four_ports_reachable'] and rotation['five_safe_points_reachable']
    assert rotation['dropped_items'] == rotation['fluids'] == rotation['runtime_portals'] == rotation['block_entities'] == 0
log = (ROOT / 'qa/forge-run.log').read_text(encoding='utf-8', errors='replace')
assert 'N04_FORGE_PASS:' in log and 'BUILD SUCCESSFUL' in log and 'All 1 required tests passed' in log
assert render['source_unchanged'] and len(render['images']) == 13
assert {(image['side'], image['mode']) for image in render['images'] if image['path'] != 'preview.png'} == {(side, mode) for side in ['north', 'east', 'south', 'west'] for mode in ['eye', 'cutaway', 'exterior']}
assert {(image['path'], image['sha256']) for image in visual['images_reviewed']} == {(image['path'], image['sha256']) for image in render['images']}
for image in render['images']:
    assert digest(ROOT / image['path']) == image['sha256']
    with Image.open(ROOT / image['path']) as decoded:
        decoded.load()
        assert decoded.size == (1536, 1024)
assert digest(ROOT / 'preview.png') == digest(ROOT / 'views/north-interior.png')
for image in render['derived_images']:
    assert digest(ROOT / image['path']) == image['sha256']
for source in provenance['documents']:
    assert digest(Path(source['path'])) == source['sha256']
assert provenance['manifest_record']['sha256'] == provenance['verify_record']['sha256'] == REFERENCE_SHA
limits = ['未部署；未修改正式实例、核心源码、玩家世界或并行ID。', '无运行时传送；寻门仅为紫晶罗盘、门廊与方向材质的静态主题。', '软件视觉与独立Forge测试不等于完整整合包客户端、多人或FPS验收。', '大厅外接、占用拒绝、持久幂等与槽位由统一接入负责人独立实现并复核。']
qa = {'id': 'zone-N04', 'name': '寻门枢纽', 'status': 'PASS', 'producer_status': 'PASS', 'consumer_acceptance': 'PENDING_INDEPENDENT_REVIEW',
      'sha256': EXPECTED_SHA, 'reference_sha256': REFERENCE_SHA, 'size': metadata['size'], 'data_version': 3465,
      'static': {'status': 'PASS', 'report': 'qa/static.json', 'records': 19600, 'reachable_feet': 813, 'cross_route_width': 5, 'cross_route_clear_height': 5},
      'visual': {'status': 'PASS', 'report': 'qa/visual-review.json', 'renderer': 'Minecraft Builder / Lodestone', 'images': 13, 'directional_images': 12, 'client_screenshot': False},
      'forge': {'status': 'PASS', 'report': 'qa/forge-native.json', 'result': 'qa/forge-result.json', 'log': 'qa/forge-run.log', 'minecraft': '1.20.1', 'version': '47.4.10',
                'rotations': 4, 'ticks_observed': 240, 'forced_neighbor_updates': True, 'exit_code': 0, 'dropped_items': 0, 'target_y': 200},
      'rejected_attempt': {'path': 'qa/rejected-first-run/', 'reason': 'Natural terrain fixture produced rail/torch/amethyst item drops absent from template; unchanged NBT passed clean flat-world retest.'},
      'reference': 'qa/source-provenance.json', 'entities': 0, 'block_entities': 0, 'fluids': 0, 'runtime_teleport': False, 'deployed': False, 'limits': limits}
write('qa.json', qa)
four_files = [entry(relative) for relative in ['room.nbt', 'room.json', 'preview.png', 'qa.json']]
handoff = {'id': 'zone-N04', 'name': '寻门枢纽', 'status': 'READY_FOR_INDEPENDENT_REVIEW', 'producer_qa': 'PASS', 'deployed': False,
           'directory': str(ROOT), 'sha256': EXPECTED_SHA, 'reference_sha256': REFERENCE_SHA, 'size': metadata['size'], 'files': four_files,
           'entrance': metadata['entrance'], 'interior': metadata['interior'], 'openings': metadata['openings'], 'safe_points': metadata['safe_points'],
           'runtime_teleport': False, 'runtime_slot': None, 'evidence_manifest': 'delivery-manifest.json', 'readme': 'README.md',
           'requirements': metadata['integration_requirements'], 'limits': limits}
write('handoff.json', handoff)
files = set(['README.md', 'room.nbt', 'room.json', 'preview.png', 'qa.json', 'handoff.json', 'summary.json', 'forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties'])
for folder in ['tools', 'views', 'qa', 'forge/src']:
    files.update(file_path.relative_to(ROOT).as_posix() for file_path in (ROOT / folder).rglob('*') if file_path.is_file() and '__pycache__' not in file_path.parts)
files.discard('delivery-manifest.json')
manifest = {'id': 'zone-N04', 'status': 'PRODUCER_QA_PASS', 'sha256': EXPECTED_SHA, 'reference_sha256': REFERENCE_SHA,
            'consumer_acceptance': 'PENDING_INDEPENDENT_REVIEW', 'deployed': False, 'files': [entry(relative) for relative in sorted(files)],
            'excluded': ['forge/.gradle/', 'forge/build/', 'forge/run/'], 'scope': 'delivery files, sources, source provenance, actual renders and accepted/rejected QA evidence'}
write('delivery-manifest.json', manifest)
for record in manifest['files']:
    assert digest(ROOT / record['path']) == record['sha256']
print(json.dumps({'id': 'zone-N04', 'status': 'PASS', 'sha256': EXPECTED_SHA, 'files': len(manifest['files']),
                  'manifest_sha256': digest(ROOT / 'delivery-manifest.json'), 'four_files': four_files}, ensure_ascii=False, indent=2))
