import collections
import hashlib
import json
import os
import sys
from datetime import datetime
from pathlib import Path
from xml.etree import ElementTree
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parent.parent
ROOM = ROOT / 'nature-redo-b01-s1-01'
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def record(path):
    content = path.read_bytes()
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(content).hexdigest(), 'bytes': len(content)}


def main():
    metadata = load(ROOM / 'room.json')
    nbt = record(ROOM / 'room.nbt')
    assert nbt['sha256'] == metadata['sha256'] == 'ca9efd56f98374bdb17a7154e8a8dc67e3d2d5578ad4be90a1734e2bdfebee7c'
    structure = Structure.read(ROOM / 'room.nbt')
    assert structure.size == (25, 15, 25) and structure.data_version == 3465
    reference = record(Path(metadata['reference']))
    assert reference['sha256'] == metadata['reference_sha256']
    render = load(ROOM / 'qa/render.json')
    assert render['source_nbt_sha256'] == nbt['sha256']
    previews = []
    for view in render['views']:
        evidence = record(ROOM / view['file'])
        assert evidence['sha256'] == view['sha256']
        previews.append({'view': view['id'], **evidence})
    tests = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(tests) == 4 and not any(test.find(tag) is not None for test in tests for tag in ['failure', 'error', 'skipped'])
    native = []
    for rotation in ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']:
        path = ROOM / ('forge-' + rotation + '.json')
        report = load(path)
        assert report['status'] == 'PASS' and report['source_nbt_sha256'] == nbt['sha256'] and report['stabilization_ticks'] == 400
        native.append({'rotation': rotation, 'report': record(path), 'capture': record(ROOM / ('native-placed-' + rotation + '.nbt')), 'settled_checks': report['settled']})
    def passable(point):
        across, vertical, forward = point
        return all(structure.get((across, layer, forward)).name == 'minecraft:air' for layer in [vertical, vertical + 1, vertical + 2]) and structure.get((across, vertical - 1, forward)).name not in {'minecraft:air', 'minecraft:water', 'minecraft:lava'}
    entrance = tuple(metadata['entrance'])
    parents = {entrance: None}
    pending = collections.deque([entrance])
    while pending:
        across, vertical, forward = pending.popleft()
        for deltaX, deltaZ in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            point = (across + deltaX, vertical, forward + deltaZ)
            if 0 <= point[0] < 25 and 0 <= point[2] < 25 and point not in parents and passable(point):
                parents[point] = (across, vertical, forward)
                pending.append(point)
    routes = []
    for port in metadata['ports']:
        target = tuple(port['feet'])
        assert target in parents
        path = []
        cursor = target
        while cursor is not None:
            path.append(list(cursor))
            cursor = parents[cursor]
        routes.append({'destination': port['side'], 'feet_centerline': list(reversed(path)), 'player_width': 0.6, 'player_height': 1.8, 'reserved_clear_height_blocks': 3, 'model': 'Player AABB centered in each voxel; straight axis steps stay inside checked full-air columns. Solid-floor/support shapes independently checked by native reports.'})
    for point in metadata['route']:
        assert tuple(point) in parents and passable(point)
    failed = []
    for old in [ROOT.parent / 'nature-b1-s1-20261104', ROOT.parent / 'nature-b1-s1-01-serial-20261104']:
        for path in sorted((old / 'qa').glob('*attempt*')):
            failed.append(record(path))
    audit = load(ROOT / 'audit/summary.json')
    result = {'checked_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'producer_thread': '01a1a5d9-5140-7b70-98a8-87038e91e268', 'reviewer_thread_as_reported': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'status': 'TECHNICAL_EVIDENCE_RECHECKED_PENDING_INDEPENDENT_ACCEPTANCE', 'canonical_nbt': nbt, 'reference': reference, 'size_xyz': list(structure.size), 'records': len(structure.blocks), 'previews': previews, 'anchors': metadata['ports'], 'anchor_rules': {'axes': '+X east,+Y up,+Z south', 'north': '-Z', 'feet_y': 2, 'rotation_pivot': [0, 0, 0], 'join': 'Opposing outward faces; transform feet and full bounding box under rotation; supported dry 3-wide/3-high connector; refuse occupied volume before placement.'}, 'player_routes': routes, 'required_route_cells_checked': len(metadata['route']), 'native_reports': native, 'native_test_xml': record(ROOT / 'native-tests.xml'), 'native_log': record(ROOT / 'qa/native-console.log'), 'reference_consistency': {'status': 'PENDING', 'report': record(ROOT / 'reference-review.json'), 'scale': '25x15x25 is an unaccepted interpretation, not a measured image dimension.'}, 'duplicate_audit': {'status': 'SNAPSHOT_NOT_FINAL', 'rooms': audit['rooms'], 'pairs': audit['pairs'], 'high_current': audit['current_group_high_similarity'], 'summary': record(ROOT / 'audit/summary.json')}, 'failure_history': failed, 'excluded_versions': record(ROOT / 'EXCLUDED_VERSIONS.json'), 'scope_limits': ['No new game simulation run; this pass checks existing native evidence hashes and independently re-decodes NBT and routes.', 'Not a pure unmodded server test: isolated Forge 47.4.10 using Minecraft 1.20.1 vanilla block/structure APIs.', 'Appearance and scale remain unaccepted; technical PASS cannot unlock S1-02.', 'No hall integration, occupancy refusal, persistence/idempotence, multiplayer, puzzle or wave-runtime acceptance.'], 'candidate_allowlist': [], 'next_image_locked': True, 'reviewer_directory_written': False, 'messages_sent': False}
    inventory = []
    selected = [ROOM, ROOT / 'source', ROOT / 'audit', ROOT / 'qa']
    for directory in selected:
        for path in sorted(directory.rglob('*')):
            if path.is_file() and '__pycache__' not in path.parts: inventory.append(record(path))
    for name in ['ACCEPTANCE.json', 'ANALYSIS.md', 'BATCH_PLAN.md', 'EXCLUDED_VERSIONS.json', 'SERIAL_QUEUE.json', 'reference-comparison.png', 'reference-review.json', 'native-tests.xml']:
        inventory.append(record(ROOT / name))
    destination = ROOT / 'producer-handover-20261105'
    destination.mkdir(exist_ok=True)
    (destination / 'evidence.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    (destination / 'file-manifest.json').write_text(json.dumps({'scope': 'Canonical module plus reproducibility and review evidence; excludes disposable test worlds/build caches and frozen zip duplicates.', 'files': inventory}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    lines = ['# S1-01 制作方交接核对（2026-11-05）', '', '- 复核接收方线程（消息提供，未在本线程代登记）：01a1a685-045b-76d2-aa2b-2a3f24520603。', '- 本次只读核对原候选与证据，新增汇总仅在本方目录；不写独立复核目录、不发送消息、不启动S1-02。', f'- 唯一制作候选NBT：`{nbt["path"]}`', f'- SHA-256：`{nbt["sha256"]}`', '- 25×15×25，9375条显式记录；DataVersion=3465，gzip原版结构格式。其他native-placed NBT仅是测试回捕，不是并行候选。', f'- 原图：`{reference["path"]}`', f'- 原图SHA-256：`{reference["sha256"]}`', '- 预览：模块preview.png及views/北南西东内外视图；每份实际图像哈希与Lodestone报告一致。', '- 锚点：北[12,2,0]、西[0,2,12]、东[24,2,12]；玩家脚位Y=2，旋转绕[0,0,0]，至少3×3中央开口。', '- 汇总中的player_routes为由当前NBT重新求得的北入口至三端口路线，按0.6宽/1.8高玩家及3格净高检查；原生报告另核验实际碰撞与支撑。', '- 既有隔离Forge原生测试4/4，每方向400tick，最终哈希一致；本次没有重跑服务器，避免将旧日志称为新测试。', '- 旧版失败日志、邻接/树叶传播修正历史见evidence.json.failure_history；旧成果封存排除，不能混入候选。', '- 图像一致性与尺度仍未验收，古树形态、装饰密度和拱门比例有已知偏差。重复度仅76模块/2850对快照，不等于最终跨组审计。', '- 文件清单见file-manifest.json，每文件绝对路径、SHA-256和字节数；技术证据和玩家路线见evidence.json。', '- 结论：可供独立复核，非审计已通过、非接入已完成。S1-02继续锁定。', '']
    (destination / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    print(json.dumps({'status': result['status'], 'nbt_sha256': nbt['sha256'], 'preview_files': len(previews), 'native_rotations': len(native), 'player_routes': len(routes), 'manifest_files': len(inventory), 'historical_failure_files': len(failed), 'handover_directory': str(destination)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
