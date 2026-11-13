import datetime
import json
import sys
import zipfile
from pathlib import Path

sys.dont_write_bytecode = True
import build_s7 as builder
from PIL import Image

ROOT = builder.ROOT
OUTPUT = builder.OUTPUT
TARGET_THREAD = '01a19758-b0e5-75c2-822a-4860527a1e8b'


def read_json(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def main():
    metadata = read_json(OUTPUT / 'room.json')
    static = read_json(ROOT / 'qa/static-qa.json')
    render = read_json(ROOT / 'qa/render.json')
    structure_hash = builder.digest(OUTPUT / 'room.nbt')
    if metadata['sha256'] != structure_hash or static['sha256'] != structure_hash or render['sha256'] != structure_hash:
        raise ValueError('Refusing to combine stale evidence')
    if static['status'] != 'PASS' or not all(static['checks'].values()):
        raise ValueError('Static QA incomplete')
    if builder.digest(builder.REFERENCE) != builder.EXPECTED_IMAGE:
        raise ValueError('Reference fingerprint mismatch')
    native = []
    for direction in ('north', 'east', 'south', 'west'):
        result = read_json(ROOT / ('qa/native-' + direction + '.json'))
        if result['status'] != 'PASS' or result['sha256'] != structure_hash or result['observed_ticks'] != 100:
            raise ValueError('Missing or stale native placement evidence: ' + direction)
        native.append(result)
    log = ROOT / 'qa/forge-gametest.log'
    log_text = log.read_text(encoding='utf-8', errors='replace')
    if 'BUILD SUCCESSFUL' not in log_text or 'All 4 required tests passed' not in log_text:
        raise ValueError('Final Forge run did not complete successfully')
    if sum('S7_NATIVE_PLACEMENT_PASS rotation=' + direction in log_text for direction in ('north', 'east', 'south', 'west')) != 4:
        raise ValueError('Four native pass markers are required')
    image_paths = [OUTPUT / 'preview.png', ROOT / 'preview/exterior.png', ROOT / 'preview/four-views.png', ROOT / 'preview/floorplan.png']
    for path in image_paths:
        with Image.open(path) as image:
            image.verify()
    timestamp = datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(timespec='seconds')
    evidence_paths = [ROOT / 'qa/static-qa.json', ROOT / 'qa/features.json', ROOT / 'qa/render.json', log]
    evidence_paths += [ROOT / ('qa/native-' + direction + '.json') for direction in ('north', 'east', 'south', 'west')]
    report = {
        'id': 'S7', 'status': 'PASS_STATIC_AND_NATIVE_PLACEMENT', 'completed_at': timestamp,
        'sha256': structure_hash, 'reference': str(builder.REFERENCE), 'reference_sha256': builder.EXPECTED_IMAGE,
        'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'facing': 'north',
        'static': static, 'native': {'status': 'PASS', 'rotations': native, 'gametests_passed': 4, 'gametests_failed': 0},
        'preview': render, 'visual_review': {'completed': True, 'views': ['hero', 'floorplan', 'closed exterior', 'four-direction sheet'], 'type': 'software render from final NBT, not a game screenshot'},
        'evidence': [{'path': str(path), 'sha256': builder.digest(path)} for path in evidence_paths],
        'setup_attempts': [
            {'path': str(ROOT / 'qa/attempt-01-resource-loading.log'), 'result': 'FAIL_TEST_HARNESS_SETUP', 'cause': 'Missing pack.mcmeta prevented the isolated fixture data pack from loading; corrected.'},
            {'path': str(ROOT / 'qa/attempt-02-template-namespace.log'), 'result': 'FAIL_TEST_HARNESS_SETUP', 'cause': 'Explicit template namespace was duplicated by Forge GameTestHolder; corrected.'},
        ],
        'scope': {'other_rooms_changed': False, 'shared_incoming_written': False, 'core_modified': False, 'player_saves_opened_or_modified': False, 'deployed_jars_changed': False, 'paid_image_calls': 0, 'integration': 'NOT_PERFORMED', 'deployment': 'NOT_PERFORMED'},
        'limits': ['原生 QA 使用独立 Minecraft 1.20.1 + Forge 47.4.10 GameTest 世界，不是整合包客户端游玩或截图。', '未验证接入后的 FT 大厅道路、核心持久标记与正式模组组合；须由接入负责人统一验证。', '北接口脚部 Y=3；接入时必须匹配地板 Y=2 并保留三格高净空，不可按 Y=1 平移入口。', '囚笼为不可进入的装饰陈设；安全区与刑台绕行路线不要求穿过囚笼。'],
    }
    builder.write_json(OUTPUT / 'qa.json', report)
    files = {name: builder.digest(OUTPUT / name) for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')}
    handoff = {
        'id': 'S7', 'name': metadata['name'], 'status': 'READY_FOR_COORDINATED_INTEGRATION_NOT_DEPLOYED',
        'intended_recipient_thread': TARGET_THREAD, 'notification': 'REPORT_PREPARED_NOT_SENT_TO_OTHER_CHAT',
        'delivery_directory': str(OUTPUT), 'project_directory': str(ROOT), 'reference_sha256': builder.EXPECTED_IMAGE,
        'structure_sha256': structure_hash, 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'static_checks_passed': len(static['checks']), 'native_gametests': '4/4', 'native_observed_ticks_per_rotation': 100,
        'native_reachable_positions': native[0]['collision_reachable_positions'], 'loot_inventory': 'none', 'files': files,
        'evidence_directory': str(ROOT / 'qa'), 'deployment': False,
    }
    builder.write_json(ROOT / 'handoff-report.json', handoff)
    archive_path = ROOT / 'S7-delivery.zip'
    with zipfile.ZipFile(archive_path, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for name in files:
            archive.write(OUTPUT / name, 'S7/' + name)
        for path in evidence_paths:
            archive.write(path, 'evidence/' + path.name)
        for path in image_paths[1:]:
            archive.write(path, 'preview/' + path.name)
        for name in ('README.md', 'HANDOFF.md', 'handoff-report.json'):
            archive.write(ROOT / name, name)
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None:
            raise ValueError('Delivery zip checksum failure')
        import hashlib
        if hashlib.sha256(archive.read('S7/room.nbt')).hexdigest() != structure_hash:
            raise ValueError('Archived NBT does not match tested structure')
    package = {'status': 'PASS', 'completed_at': timestamp, 'archive': str(archive_path), 'sha256': builder.digest(archive_path), 'room_files': files, 'zip_integrity': 'PASS', 'tested_structure_in_archive': True}
    builder.write_json(ROOT / 'qa/package-check.json', package)
    print(json.dumps({'status': report['status'], 'sha256': structure_hash, 'native': '4/4', 'delivery': str(OUTPUT), 'archive_sha256': package['sha256']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
