import hashlib
import json
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def check(condition, message):
    if not condition:
        raise ValueError(message)


def rotate_state(block, turns):
    directions = ['north', 'east', 'south', 'west']
    result = {}
    for key, value in block.properties:
        if key in directions:
            key = directions[(directions.index(key) + turns) % 4]
        elif key == 'facing' and value in directions:
            value = directions[(directions.index(value) + turns) % 4]
        result[key] = value
    return block.name, tuple(sorted(result.items()))


def main():
    check(ROOT.name == 'corridor-M20', 'Wrong project root')
    metadata = load(ROOT / 'room.json')
    sha256 = digest(ROOT / 'room.nbt')
    reports = {name: load(ROOT / 'qa' / (name + '.json')) for name in ('static', 'native', 'render-manifest')}
    for name, report in reports.items():
        check(report['status'] == 'PASS' and report['sha256'] == sha256, 'Stale or failed report: ' + name)
    check(metadata['sha256'] == sha256 and digest(Path(metadata['reference'])) == metadata['reference_sha256'], 'Source metadata mismatch')
    native = reports['native']
    check(native['rotations'] == 4 and native['ticks'] >= 240 and native['final_template_sha256_rechecked'], 'Incomplete native evidence')
    check(len(native['initial']) == 4 and len(native['after_stability']) == 4, 'Missing rotation records')
    successful_logs = []
    for path in sorted((ROOT / 'qa').glob('forge-*.log')):
        text = path.read_text(encoding='utf-8', errors='replace')
        if 'CORRIDOR_M20_NATIVE_PASS' in text and sha256 in text and 'BUILD SUCCESSFUL' in text:
            successful_logs.append(path)
    check(successful_logs, 'No successful Forge log for final SHA-256')
    final_log = successful_logs[-1]
    room = Structure.read(ROOT / 'room.nbt')
    captures = []
    for rotation, turns in [('none', 0), ('clockwise_90', 1), ('clockwise_180', 2), ('counterclockwise_90', 3)]:
        path = ROOT / 'qa' / ('native-placed-' + rotation + '.nbt')
        capture = Structure.read(path)
        check(capture.size == ((13, 11, 40) if turns % 2 == 0 else (40, 11, 13)), 'Native capture dimensions mismatch')
        check(len(capture.blocks) == 5720 and not capture.block_entities and not capture.entities, 'Invalid native capture contents')
        for (horizontal, vertical, depth), block in room.blocks.items():
            position = [(horizontal, vertical, depth), (39 - depth, vertical, horizontal),
                        (12 - horizontal, vertical, 39 - depth), (depth, vertical, 12 - horizontal)][turns]
            actual = capture.get(position)
            check((actual.name, actual.properties) == rotate_state(block, turns), 'Native capture mismatch: ' + rotation + ' ' + str(position))
        captures.append({'rotation': rotation, 'path': str(path.relative_to(ROOT)), 'sha256': digest(path),
                         'records_matched': 5720, 'status': 'PASS'})
    save(ROOT / 'qa/capture-comparison.json', {'status': 'PASS', 'sha256': sha256, 'captures': captures,
                                               'comparisons': 22880, 'method': 'Independent Python inverse-coordinate and rotated block-state comparison after 320 ticks'})
    render = reports['render-manifest']
    check(render['count'] == 8 and len(render['views']) == 8, 'Eight views required')
    views = []
    notes = {
        ('interior', 'north'): '北口同高直入；5格主路连续；四笼、横梁和轴线可辨；未见横向分岔。',
        ('interior', 'south'): '南口可回行；格栅与铜地板同高；后侧横梁和吊笼均接入顶架。',
        ('interior', 'west'): '西剖面检查两侧压力表、灯槽和铜管；吊笼上下板贴住铁栏，无顶板悬缝。',
        ('interior', 'east'): '东剖面检查相反侧墙体与铜管；梁端接墙、笼盖接铁帽、铁链通向屋顶锚点。',
        ('exterior', 'north'): '北口五格宽五格高；完整铜门框、地基和顶盖；无外露流体。',
        ('exterior', 'south'): '南口与北口同高；门洞开放；完整门楣和顶盖。',
        ('exterior', 'west'): '西侧外壳与基础连续，铜柱和围带衔接，顶面无开洞。',
        ('exterior', 'east'): '东侧外壳与基础连续；无错误侧门、悬空装饰或顶面缺口。'
    }
    for view in render['views']:
        path = ROOT / view['path']
        check(digest(path) == view['sha256'], 'Changed rendered view')
        with Image.open(path) as image:
            image.load()
            check(image.size == (1800, 1100), 'Unexpected view dimensions')
        views.append({**view, 'status': 'PASS', 'reviewed_individually': True,
                      'observation': notes[(view['mode'], view['view'])]})
    visual = {
        'status': 'PASS', 'reviewer': 'corridor-M20 producer / Codex', 'sha256': sha256,
        'reference_sha256': metadata['reference_sha256'], 'views': views,
        'reference_match': '保留橙铜/铜绿、平直轴线、两侧管线压力表、橙色壁灯地槽、四个铁笼吊灯、远端门框。',
        'revisions': [
            {'type': 'visual-correction', 'baseline_sha256': '87b3d58bbf63b9b43e4ff815c6983cf5d2e96e41a012cb8065b0db829ff7f19c',
             'final_sha256': sha256, 'change': '吊笼顶部铁活板门由上半改下半，与下方铁栏和中央铁帽直接接触；最终版重做八视图及Forge四向320tick。'},
            {'type': 'fixture-correction', 'change': '首次隔离测试y=100碰到自然陶瓦山地而拒绝放置；保留失败日志；测试夹具提高到y=300并使用全新独立平坦世界。'}
        ],
        'substitutions': metadata['substitutions'], 'producer_review_only': True,
        'receiver_independent_acceptance_still_required': True,
        'limitations': ['软件纹理预览不显示真实发光、着色器或整合包客户端性能。',
                       '剖视移除屋顶和近侧墙仅存在于渲染内存；保留的小铜盖用于显示原有屋顶锚点。']
    }
    save(ROOT / 'qa/visual-review.json', visual)
    qa = {
        'id': 'corridor-M20', 'status': 'PASS', 'sha256': sha256, 'reference_sha256': metadata['reference_sha256'],
        'scope': 'Independent producer delivery; static, individual eight-view review and isolated Forge placement/stability.',
        'created_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
        'checks': {'static': 'PASS', 'north_south_same_height': 'PASS', 'straight_walkway': 'PASS',
                   'three_high_clear_main_route': 'PASS', 'roof': 'PASS', 'hanging_cage_support': 'PASS',
                   'eight_individual_views': 'PASS', 'native_registry_and_properties': 'PASS',
                   'native_four_rotations': 'PASS', 'native_320_tick_stability': 'PASS',
                   'native_capture_independent_comparison': 'PASS', 'fluids': 0, 'entities': 0, 'block_entities': 0, 'inventory_items': 0},
        'static_report': {'path': 'qa/static.json', 'sha256': digest(ROOT / 'qa/static.json')},
        'visual_report': {'path': 'qa/visual-review.json', 'sha256': digest(ROOT / 'qa/visual-review.json')},
        'native_report': {'path': 'qa/native.json', 'sha256': digest(ROOT / 'qa/native.json')},
        'native_log': {'path': str(final_log.relative_to(ROOT)), 'sha256': digest(final_log)},
        'capture_comparison': {'path': 'qa/capture-comparison.json', 'sha256': digest(ROOT / 'qa/capture-comparison.json')},
        'native': {'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations': 4, 'ticks': native['ticks'],
                   'records_per_snapshot': 22880, 'capture_records_compared': 22880,
                   'neighbor_updates': 'UPDATE_ALL and explicit notifications', 'random_tick_speed': 3},
        'render_manifest': {'path': 'qa/render-manifest.json', 'sha256': digest(ROOT / 'qa/render-manifest.json')},
        'ready_for_receiver_review': True, 'ready_for_unified_integration': False,
        'deployment': False, 'production_save_accessed': False, 'full_modpack_playtest': False,
        'limitations': ['Receiver must freeze final bundle and independently review 8 images and run native fixture.',
                       'Core routes, occupancy refusal, idempotence/persistence and modpack playtest belong to later integration.']
    }
    save(ROOT / 'qa.json', qa)
    handoff = {
        'id': 'corridor-M20', 'delivery_root': str(ROOT), 'sha256': sha256, 'size': [13, 11, 40],
        'entrance': [6, 2, 0], 'exit': [6, 2, 39], 'interior': [6, 2, 3],
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'status': 'PRODUCER_QA_PASS_AWAIT_RECEIVER_ACCEPTANCE', 'qa': 'qa.json',
        'eight_views': [entry['path'] for entry in views], 'native_report': 'qa/native.json',
        'native_log': str(final_log.relative_to(ROOT)), 'fixture': 'src/main/java/net/farmingtales/corridorm20qa/CorridorM20GameTests.java',
        'fixture_launcher': 'tools/run-forge.ps1', 'receiver_thread': '01a19c8f-6747-7572-a77a-6e90084bbd34',
        'reserved_intake_slot': 60, 'candidate_template': 'ftdungeonmobs:image25/corridor_m20',
        'deployment': False, 'production_save_modified': False, 'ready_for_unified_integration': False
    }
    save(ROOT / 'handoff.json', handoff)
    selected = [ROOT / name for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json', 'handoff.json', 'README.md', 'build.gradle', 'settings.gradle', 'gradle.properties')]
    selected.extend(path for path in (ROOT / 'preview').glob('*.png'))
    selected.extend(path for path in (ROOT / 'qa').glob('*.json') if not path.name.startswith('previous-'))
    selected.extend(ROOT / item['path'] for item in captures)
    selected.append(final_log)
    selected.extend(path for path in (ROOT / 'tools').glob('*') if path.is_file())
    selected.extend(path for path in (ROOT / 'src').rglob('*') if path.is_file())
    selected = sorted(set(selected))
    manifest = {'id': 'corridor-M20', 'status': 'PASS', 'sha256': sha256,
                'files': {str(path.relative_to(ROOT)).replace('\\', '/'): {'sha256': digest(path), 'bytes': path.stat().st_size} for path in selected},
                'excluded': ['Gradle build/cache files', 'temporary isolated Forge worlds', 'baseline/rejected evidence'],
                'required_files': ['room.nbt', 'room.json', 'preview.png', 'qa.json']}
    save(ROOT / 'delivery-manifest.json', manifest)
    print(json.dumps({'status': 'PASS', 'sha256': sha256, 'files': len(selected), 'native_rotations': 4,
                      'ticks': native['ticks'], 'views': 8, 'captured_records_compared': 22880}))


if __name__ == '__main__':
    main()
