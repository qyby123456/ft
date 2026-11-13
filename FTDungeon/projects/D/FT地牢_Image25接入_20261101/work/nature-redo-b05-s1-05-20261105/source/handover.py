import hashlib
import json
import os
import sys
from collections import Counter
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b05-s1-05'
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure

ROTATIONS = ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']
DIRECTIONS = ['north', 'east', 'south', 'west']


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def record(path):
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}


def compare_captures(structure):
    width, height, depth = structure.size
    results = []
    assert not structure.block_entities and not structure.entities
    for turns, rotation in enumerate(ROTATIONS):
        path = FOLDER / ('native-placed-' + rotation + '.nbt')
        capture = Structure.read(path)
        assert len(capture.blocks) == len(structure.blocks) and not capture.block_entities and not capture.entities
        expected_size = (width, height, depth) if turns % 2 == 0 else (depth, height, width)
        assert capture.size == expected_size
        for point, state in structure.blocks.items():
            across, vertical, forward = point
            target = [(across, vertical, forward), (depth - 1 - forward, vertical, across), (width - 1 - across, vertical, depth - 1 - forward), (forward, vertical, width - 1 - across)][turns]
            properties = dict(state.properties)
            if properties.get('facing') in DIRECTIONS:
                properties['facing'] = DIRECTIONS[(DIRECTIONS.index(properties['facing']) + turns) % 4]
            if properties.get('axis') in ['x', 'z'] and turns % 2:
                properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
            actual = capture.blocks.get(target)
            assert actual is not None and actual.name == state.name and dict(actual.properties) == properties, str((rotation, point, state, actual))
        results.append({'rotation': rotation, **record(path), 'matches_rotated_source_nbt': True, 'exact_records_compared': len(structure.blocks)})
    return results


def main():
    group = load(ROOT / 'group.json')
    metadata = load(FOLDER / 'room.json')
    structure = Structure.read(FOLDER / 'room.nbt')
    fingerprint = record(FOLDER / 'room.nbt')['sha256']
    assert fingerprint == metadata['sha256'] == group['rooms'][0]['sha256']
    assert structure.size == (15, 12, 37) and structure.data_version == 3465
    cases = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(cases) == 4 and all(not any(case.find(tag) is not None for tag in ['failure', 'error', 'skipped']) for case in cases)
    assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/native-console.log').read_text(encoding='utf-8')
    native = []
    for rotation in ROTATIONS:
        path = FOLDER / ('forge-' + rotation + '.json')
        report = load(path)
        assert report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint
        assert report['random_tick_speed'] == 0 and report['stabilization_ticks'] == 400
        assert '0.6 x 1.8 native AABB' in report['body_route']
        assert report['initial'] == report['settled'] and report['initial']['required_lane_feet'] == 185
        native.append(record(path))
    captures = compare_captures(structure)
    save(FOLDER / 'qa/capture-comparisons.json', {'status': 'PASS', 'source_nbt_sha256': fingerprint, 'rotations': captures})
    support = load(FOLDER / 'qa/support.json')
    static = load(FOLDER / 'qa/static.json')
    render = load(FOLDER / 'qa/render.json')
    assert support['status'] == static['status'] == 'PASS'
    assert support['source_nbt_sha256'] == render['source_nbt_sha256'] == fingerprint
    assert render['source_nbt_unchanged'] and len(render['views']) == 13
    for view in render['views']:
        assert record(FOLDER / view['file'])['sha256'] == view['sha256']
    reference = Path(metadata['reference'])
    assert record(reference)['sha256'] == group['group_fingerprint'] == metadata['reference_sha256']
    audit = load(ROOT / 'audit/summary.json')
    assert not audit['errors'] and not audit['current_group_high_similarity']
    palette = Counter(state.name for state in structure.blocks.values())
    save(FOLDER / 'materials.json', {'source_nbt_sha256': fingerprint, 'size': structure.size, 'records_including_air': len(structure.blocks), 'counts_by_block_id': dict(sorted(palette.items())), 'palette_states': [{'name': name, 'properties': dict(properties), 'count': count} for (name, properties), count in sorted(Counter((state.name, state.properties) for state in structure.blocks.values()).items())], 'block_entities': 0, 'entities': 0})
    paths = []
    for target in metadata['route']:
        across, vertical, forward = target
        path = [[7, 2, offset] for offset in range(36, forward - 1, -1)]
        if across != 7:
            direction = 1 if across > 7 else -1
            path += [[offset, 2, forward] for offset in range(7 + direction, across + direction, direction)]
        assert path[0] == metadata['entrance'] and path[-1] == target
        for point in path:
            assert all(structure.get((point[0], point[1] + offset, point[2])).name == 'minecraft:air' for offset in range(3))
            assert structure.get((point[0], point[1] - 1, point[2])).name not in {'minecraft:air', 'minecraft:water', 'minecraft:lava'}
        paths.append({'target': target, 'source_grid_path': path})
    save(FOLDER / 'qa/body-routes.json', {'status': 'PASS', 'source_nbt_sha256': fingerprint, 'body': {'width': 0.6, 'height': 1.8, 'feet_y': 2}, 'native_scope': 'Native collision-shape AABB at every flood cell and swept union on each traversed neighbor edge; supported floor, no fluids/cobweb, all 185 declared targets reached in four rotations. Exported paths are source-grid witnesses, not a recorded client playthrough.', 'ports': metadata['ports'], 'entrance': metadata['entrance'], 'target_count': len(paths), 'paths': paths})
    cutaway = {'source_nbt_sha256': fingerprint, 'source_is_final_complete_nbt': True, 'derived_nbt_files': [], 'roof': 'Display copy only: hide y>=11 for non-full views.', 'hero': 'Additionally hide x<=1 or z>=35 at y>=2.', 'reverse_overview': 'Additionally hide x>=13 or z<=1 at y>=2.', 'directional': 'At y>=2 hide north z<=1, south z>=35, west x<=1 or east x>=13 respectively.', 'full': 'No blocks hidden: both player views, roots-pool-detail and four exterior views.', 'traceability': 'qa/render.json records source SHA, camera, cut mode, displayed records and screenshot SHA. Renderer constructs display structures in memory; final source is rehashed after rendering.', 'native_nbt_modified': False}
    save(FOLDER / 'qa/cutaway-rules.json', cutaway)
    save(FOLDER / 'qa.json', {'status': 'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW', 'source_nbt_sha256': fingerprint, 'static': static, 'support': support, 'native_reports': native, 'native_capture_exact_comparisons': captures, 'test_count': 4, 'render_views': 13, 'random_tick_speed': 0, 'scope': '400 forced fluid/neighbor ticks per rotation and native body-route checks; no natural random tick certification, manual interaction, gameplay or deployment.'})
    save(FOLDER / 'reference-review.json', {'status': 'PENDING_INDEPENDENT_REVIEW', 'reference': record(reference), 'structure': record(FOLDER / 'room.nbt'), 'landmarks': ['Closed low stone ceiling with crossbeams', 'Straight dry central route and far arched opening', 'Alternating side-root and masonry supports', 'Asymmetric shallow side pools', 'Hanging berry vines and mushrooms on side plinths'], 'adaptations': metadata['deviations'], 'cutaway_rules': cutaway, 'next_image_authorized': False})
    previous_queue = load(ROOT.parent / 'nature-redo-b04-s1-04-20261105/SERIAL_QUEUE.json')
    accepted = previous_queue['accepted'] + [{'image': 'S1-04', 'sha256': group['previous_accepted_sha256'], 'authority': 'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603', 'evidence_readonly': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S1-04-independent/independent-details.json', 'scope': 'Independent static/files/body-route; not natural random ticks or independent world certification. Interaction/gameplay/deployment NOT_RUN.'}]
    save(ROOT / 'SERIAL_QUEUE.json', {'group': 'S1', 'accepted': accepted, 'current': {'image': 'S1-05', 'sha256': fingerprint, 'status': 'PENDING_INDEPENDENT_REVIEW'}, 'next': {'image': 'S1-06', 'authorized_to_start': False}, 'production_deployed': False})
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 22)
    canvas = Image.new('RGB', (1800, 790), '#151d29')
    draw = ImageDraw.Draw(canvas)
    draw.text((25, 15), 'S1-05 原图 / 完整NBT正向人眼 / 完整NBT反向人眼 · 待独立复核', font=font, fill='white')
    for column, (path, label) in enumerate([(reference, '参考图'), (FOLDER / 'views/player-interior.png', '完整源NBT内景；无裁切'), (FOLDER / 'views/player-reverse.png', '完整源NBT反面；无裁切')]):
        with Image.open(path) as image:
            image = image.convert('RGB')
            image.thumbnail((590, 660))
            canvas.paste(image, (column * 600 + (600 - image.width) // 2, 85 + (660 - image.height) // 2))
        draw.text((column * 600 + 15, 55), label, font=font, fill='#d2e6cf')
    canvas.save(FOLDER / 'reference-comparison.png')
    handover = ROOT / 'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    lines = ['# S1-05 制作方交接', '', '- 制作方技术通过，待独立逐图复核；不解锁06。01/02/03/04接受登记见SERIAL_QUEUE.json。', f'- room.nbt SHA-256：{fingerprint}', '- Java1.20.1 DataVersion3465；15×12×37；6660条含空气记录，单调色板，无实体或方块实体、无容器。材料表含全部状态与数量。', '- 南口[7,2,36]，北口[7,2,0]，均5宽3高，入口朝南；主路x5..9、y2、z0..36共185格连续三高净空。接口须同高、干燥、承托并变换完整包围盒。', '- 原版支撑/身体路线：四向各400tick强制流体与邻居更新，randomTickSpeed=0；完整0.6×1.8身体AABB、相邻步进扫掠并集及地面承托均检查。185目标路径见qa/body-routes.json。不是客户端游玩录像。', '- 四向回捕逐方块及旋转状态匹配最终源NBT，六面支撑图无离地簇；无水外泄或掉落实体。', '- 13张真实源NBT Lodestone图含全景/反面、正反人眼、四向内外图及水池树根细节；非游戏截图。', '- 所有裁切只作用于内存显示副本，无另造展示NBT。正反人眼与细节不裁切；每视图源SHA、相机、显示记录、截图SHA见qa/render.json，精确裁切规则见qa/cutaway-rules.json。', '- 构图：低顶直廊、侧墙错位盘根与石扶柱、木梁、边池、垂藤及蘑菇；盆栽蘑菇和整格斜根近似属明确适配，光束/外森林不制作。', '- audit/是本图、已接受01..04及当前规范交付的重复度快照，非所有组终审，不以缺少机关/波次或换材质加分。', '- 原配方与生成/验证/渲染/交接源码在source/；材料与状态在materials.json；原图路径与SHA载于room.json和reference-review.json。', '- 自然随机刻认证、实际交互、玩法、部署NOT_RUN；未写正式存档/mods/config，不并做06。', '']
    (handover / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    files = [path for path in (ROOT / 'source').glob('*') if path.is_file()]
    files += [ROOT / name for name in ['ANALYSIS.md', 'group.json', 'SERIAL_QUEUE.json', 'native-tests.xml', 'audit/summary.json', 'audit/pairs.json', 'audit/descriptors.json', 'audit/similarity-matrix.csv', 'audit/REPORT.md', 'audit/high-similarity.json', 'qa/native-console.log', 'qa/native-invocation.json']]
    files += [path for path in FOLDER.rglob('*') if path.is_file()]
    files += [ROOT / name for name in ['native-test/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'native-test/src/main/resources/data/ftzonereceiver/structures/empty.nbt', 'native-test/src/main/resources/META-INF/mods.toml', 'native-test/src/main/resources/pack.mcmeta', 'server/server.properties']]
    files += [reference, handover / 'README.md']
    save(handover / 'FILE-MANIFEST.json', {'source_nbt_sha256': fingerprint, 'manifest_self_excluded': True, 'files': [record(path) for path in files]})
    print(json.dumps({'status': 'READY_FOR_INDEPENDENT_REVIEW', 'sha256': fingerprint, 'native_tests': len(cases), 'capture_comparisons': len(captures), 'body_route_targets': len(paths), 'render_views': len(render['views']), 'file_manifest_entries': len(files), 'next_image_authorized': False}))


if __name__ == '__main__':
    main()
