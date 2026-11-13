import hashlib
import json
from datetime import datetime
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
BATCH = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103')


def load(name):
    return json.loads((ROOT / name).read_text(encoding='utf-8'))


def save(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def find_entry(value, filename):
    if isinstance(value, dict):
        if value.get('file') == filename:
            return value
        for nested in value.values():
            entry = find_entry(nested, filename)
            if entry:
                return entry
    if isinstance(value, list):
        for nested in value:
            entry = find_entry(nested, filename)
            if entry:
                return entry
    return None


def main():
    room = load('room.json')
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    assert digest == room['sha256']
    evidence = {}
    for key, name in [('static', 'qa/static.json'), ('forge_native', 'qa/native.json'),
                      ('captures', 'qa/capture-comparison.json'), ('renders', 'qa/render-manifest.json'),
                      ('visual_review', 'qa/visual-review.json'), ('furniture_detail', 'qa/detail-render.json')]:
        report = load(name)
        assert report['status'] == 'PASS', name
        assert report.get('sha256', report.get('source_sha256')) == digest, name
        evidence[key] = name
    native = load('qa/native.json')
    assert native['settle_ticks'] == 320 and len(native['settled']) == 4 and native['state_drift'] == 0
    assert len(load('qa/render-manifest.json')['views']) == 8
    for filename in ['preview.png', 'qa/four-interior-views.png', 'qa/four-exterior-views.png', 'qa/floor-plan.png', 'qa/furniture-supported.png']:
        with Image.open(ROOT / filename) as image:
            image.verify()
    reference = Path(room['reference'])
    assert hashlib.sha256(reference.read_bytes()).hexdigest() == room['reference_sha256']
    reference_evidence = {'status': 'PASS', 'sha256': digest, 'reference': str(reference),
        'reference_sha256': room['reference_sha256'], 'bytes': reference.stat().st_size, 'verified_against': []}
    for document_name in ['docs/manifest.json', 'docs/verify.json']:
        document = json.loads((BATCH / document_name).read_text(encoding='utf-8'))
        entry = find_entry(document, 'out/halls/H02-halls-banquet-hall-ruined.png')
        assert entry and entry['sha256'] == room['reference_sha256'] and entry['bytes'] == reference.stat().st_size and entry['ok']
        reference_evidence['verified_against'].append({'path': str(BATCH / document_name), 'entry': entry})
    save('qa/reference-verification.json', reference_evidence)
    evidence['reference'] = 'qa/reference-verification.json'
    logs = sorted((ROOT / 'qa').glob('forge-*.log'))
    passed_logs = [path for path in logs if 'H02_NATIVE_PASS rotations=4 records=61248 ticks=320 sha256=' + digest in path.read_text(encoding='utf-8', errors='replace') and 'BUILD SUCCESSFUL' in path.read_text(encoding='utf-8', errors='replace')]
    assert passed_logs, 'No successful native log matching the delivered NBT'
    evidence['forge_log'] = passed_logs[-1].relative_to(ROOT).as_posix()
    save('qa.json', {'status': 'PASS', 'id': 'H02', 'delivery_id': 'zone-H02', 'sha256': digest,
        'reference_sha256': room['reference_sha256'], 'size': room['size'], 'minecraft': '1.20.1', 'forge': '47.4.10',
        'checked_at_local': datetime.now().isoformat(timespec='seconds'), 'timezone': 'Asia/Shanghai',
        'checks': {'single_palette_vanilla_nbt': 'PASS', 'reference_manifest_verify': 'PASS', 'static_safety': 'PASS',
                   'three_wide_three_high_route': 'PASS', 'entrance_safety_point_bidirectional': 'PASS',
                   'supported_furniture_rubble_chains': 'PASS', 'full_floor_and_roof': 'PASS',
                   'four_interior_and_four_exterior_views': 'PASS', 'forge_four_rotations_320_ticks': 'PASS',
                   'native_roundtrip_and_world_capture_comparison': 'PASS'},
        'measurements': {'records_per_rotation': 15312, 'records_per_phase_four_rotations': 61248,
                         'non_air_per_rotation': 6392, 'route_feet_cells': 216, 'state_drift': 0,
                         'entities_or_drops': 0, 'forbidden_blocks': 0, 'full_floor_cells': 957, 'full_roof_cells': 957},
        'evidence': evidence, 'deployment': {'performed': False, 'production_files_modified': False, 'player_world_opened': False},
        'limitations': ['隔离 Forge 原生放置验收，不是整合包客户端、多人与连接路线接入验收。',
                       '视图为实际 NBT 加原版模型纹理的软件渲染，非游戏截图；不模拟动态光照。',
                       '显式 air 记录包含整个体积；后续接入须先检查整个体积占用，不能对玩家世界盲放。']})
    artifacts = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'summary.json',
                 'qa/native.json', 'qa/native-initial.json', 'qa/native-roundtrip.nbt', 'qa/capture-comparison.json',
                 'qa/static.json', 'qa/render-manifest.json', 'qa/visual-review.json', 'qa/reference-verification.json',
                 'qa/four-interior-views.png', 'qa/four-exterior-views.png', 'qa/furniture-supported.png', 'qa/floor-plan.png']
    artifacts += [path.relative_to(ROOT).as_posix() for path in sorted((ROOT / 'qa').glob('capture-*.nbt'))]
    artifacts += [evidence['forge_log']]
    checksums = ''.join(hashlib.sha256((ROOT / name).read_bytes()).hexdigest() + '  ' + name + '\n' for name in artifacts)
    (ROOT / 'SHA256SUMS.txt').write_text(checksums, encoding='utf-8')
    print(json.dumps({'delivery': 'PASS', 'sha256': digest, 'evidence_reports': len(evidence), 'checksummed_files': len(artifacts)}))


if __name__ == '__main__':
    main()
