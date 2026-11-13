import hashlib
import importlib.util
import json
import os
import sys
from collections import Counter, deque
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b07-s1-07'
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def record(path):
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}


def main():
    group = load(ROOT / 'group.json')
    metadata = load(FOLDER / 'room.json')
    structure = Structure.read(FOLDER / 'room.nbt')
    fingerprint = record(FOLDER / 'room.nbt')['sha256']
    assert fingerprint == metadata['sha256'] == group['rooms'][0]['sha256']
    assert structure.size == (27, 17, 29) and structure.data_version == 3465
    assert not structure.entities and not structure.block_entities
    cases = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(cases) == 4 and all(not any(case.find(tag) is not None for tag in ['failure', 'error', 'skipped']) for case in cases)
    assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/native-console.log').read_text(encoding='utf-8')
    helper_path = ROOT.parent / 'nature-redo-b05-s1-05-20261105/source/handover.py'
    spec = importlib.util.spec_from_file_location('captures', helper_path)
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    helper.FOLDER = FOLDER
    captures = helper.compare_captures(structure)
    native = []
    analyses = []
    for rotation in helper.ROTATIONS:
        path = FOLDER / ('forge-' + rotation + '.json')
        report = load(path)
        assert report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint
        assert report['random_tick_speed'] == 0 and report['stabilization_ticks'] == 400
        assert report['initial'] == report['settled']
        native.append(record(path))
        analysis_path = FOLDER / ('entrance-analysis-' + rotation + '.json')
        analysis = load(analysis_path)
        assert analysis['status'] == 'PASS' and analysis['source_nbt_sha256'] == fingerprint
        assert analysis['closed_interior_targets_reached'] == 0 and analysis['blocked_door_columns'] == 2
        assert analysis['open_analysis_targets_reached'] == analysis['route_target_count'] == len(metadata['route']) == 80
        assert analysis['closed_source_restored_before_capture']
        analyses.append(record(analysis_path))
    save(FOLDER / 'qa/capture-comparisons.json', {'status': 'PASS', 'source_nbt_sha256': fingerprint, 'closed_source_captures': captures})
    static = load(FOLDER / 'qa/static.json')
    support = load(FOLDER / 'qa/support.json')
    render = load(FOLDER / 'qa/render.json')
    assert static['status'] == 'PASS_STATIC_ONLY' and support['status'] == 'PASS'
    assert static['source_nbt_sha256'] == support['source_nbt_sha256'] == render['source_nbt_sha256'] == fingerprint
    assert render['source_nbt_unchanged'] and len(render['views']) == 14
    for view in render['views']:
        assert record(FOLDER / view['file'])['sha256'] == view['sha256']
    reference = Path(metadata['reference'])
    assert record(reference)['sha256'] == metadata['reference_sha256'] == group['group_fingerprint']
    audit = load(ROOT / 'audit/summary.json')
    assert not audit['errors'] and not audit['current_group_high_similarity']
    save(FOLDER / 'materials.json', {'source_nbt_sha256': fingerprint, 'records_including_air': len(structure.blocks), 'counts_by_block_id': dict(sorted(Counter(state.name for state in structure.blocks.values()).items())), 'palette_states': [{'name': name, 'properties': dict(properties), 'count': count} for (name, properties), count in sorted(Counter((state.name, state.properties) for state in structure.blocks.values()).items())], 'block_entities': 0, 'entities': 0})
    entrance = tuple(metadata['route'][0])
    parents = {entrance: None}
    frontier = deque([entrance])
    allowed = {tuple(point) for point in metadata['route']}
    while frontier:
        current = frontier.popleft()
        for candidate in allowed:
            if candidate in parents:
                continue
            if abs(candidate[0] - current[0]) + abs(candidate[2] - current[2]) == 1 and abs(candidate[1] - current[1]) <= .5:
                parents[candidate] = current
                frontier.append(candidate)
    paths = []
    for target in metadata['route']:
        current = tuple(target)
        path = []
        while current is not None:
            path.append(list(current))
            current = parents[current]
        path.reverse()
        paths.append({'target': target, 'open_analysis_grid_witness': path})
    save(FOLDER / 'qa/body-routes.json', {'status': 'PASS_OPEN_ANALYSIS_CLOSED_BLOCKED', 'source_nbt_sha256': fingerprint, 'candidate_default_closed': True, 'body': {'width': .6, 'height': 1.8, 'max_step_rise': .5}, 'ports': metadata['ports'], 'steps': metadata['steps'], 'doors': metadata['doors'], 'target_count': len(paths), 'paths': paths, 'proof': 'Four native reports prove closed barrier and open-only access with native AABB support and piecewise raised sweeps. Grid witnesses are not client input playback. Only open changes in memory; source restored and captured closed.', 'native_reports': analyses})
    cutaway = {'source_nbt_sha256': fingerprint, 'candidate_state': 'All four door halves closed; all screenshots render this same complete source.', 'analysis_derivative': 'Native world memory only: exactly four open booleans set true, all other states compared, restored before capture. No open NBT candidate or open screenshot.', 'roof': 'For non-full display views only hide y>=16.', 'hero': 'Additionally hide x>=25 or z>=27 at y>=2.', 'directional': 'At y>=2 hide north z<=1, south z>=27, west x<=1 or east x>=25 respectively.', 'full': 'No removals in reverse-overview, all four human/detail views and four exterior views. The front gate barrier z23 remains even in hero.', 'derived_display_nbt_files': [], 'traceability': 'qa/render.json records source SHA, camera, mode, displayed records and image SHA; in-memory display structures, source rehashed after rendering.'}
    save(FOLDER / 'qa/cutaway-rules.json', cutaway)
    save(FOLDER / 'qa.json', {'status': 'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW', 'source_nbt_sha256': fingerprint, 'static': static, 'support': support, 'native_reports': native, 'closed_open_analysis': analyses, 'closed_capture_comparisons': captures, 'render_views': 14, 'scope': '400 forced fluid/neighbor ticks per rotation, randomTickSpeed=0; native geometric closed/open/stair route. No natural random tick certification, actual player use, gameplay or deployment.'})
    save(FOLDER / 'reference-review.json', {'status': 'PENDING_INDEPENDENT_REVIEW', 'reference': record(reference), 'structure': record(FOLDER / 'room.nbt'), 'landmarks': ['Closed front double gate and external half-steps', 'Raised courtyard and rear half-steps to taller portal', 'Left decorative supply crates', 'Right map mosaic and warm light pedestal', 'Root-covered walls with side pool and hanging vines'], 'adaptations': metadata['deviations'], 'cutaway_rules': cutaway, 'next_image_authorized': False})
    previous = load(ROOT.parent / 'nature-redo-b06-s1-06-20261105/SERIAL_QUEUE.json')
    accepted = previous['accepted'] + [{'image': 'S1-06', 'sha256': group['previous_accepted_sha256'], 'authority': 'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603', 'evidence_readonly': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S1-06-independent/independent-details.json', 'scope': 'Independent static/files/body-route; not producer second-server or natural random tick certification. Actual interaction/gameplay/deployment NOT_RUN.'}]
    save(ROOT / 'SERIAL_QUEUE.json', {'group': 'S1', 'accepted': accepted, 'current': {'image': 'S1-07', 'sha256': fingerprint, 'status': 'PENDING_INDEPENDENT_REVIEW'}, 'next': {'image': 'S1-08', 'authorized_to_start': False}, 'production_deployed': False})
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 22)
    canvas = Image.new('RGB', (1800, 790), '#151d29')
    draw = ImageDraw.Draw(canvas)
    draw.text((25, 15), 'S1-07 原图 / 完整关门源：门阶 / 完整关门源：前庭 · 待独立复核', font=font, fill='white')
    for column, (path, label) in enumerate([(reference, '参考图'), (FOLDER / 'views/gate-step-detail.png', '完整源双门与半阶；无裁切'), (FOLDER / 'views/player-interior.png', '完整源内庭与后阶；无裁切')]):
        with Image.open(path) as image:
            image = image.convert('RGB')
            image.thumbnail((590, 660))
            canvas.paste(image, (column * 600 + (600 - image.width) // 2, 85 + (660 - image.height) // 2))
        draw.text((column * 600 + 15, 55), label, font=font, fill='#d2e6cf')
    canvas.save(FOLDER / 'reference-comparison.png')
    handover = ROOT / 'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    lines = ['# S1-07 制作方交接', '', '- 制作方技术通过，待独立逐图复核；不解锁08，01..06登记见SERIAL_QUEUE.json。', f'- 关门候选room.nbt SHA-256：{fingerprint}', '- Java1.20.1 DataVersion3465；27×17×29；13311含空气记录；无容器、实体或方块实体。', '- 南外口[12,2,28]宽2，北后口[12,4,0]宽3，接口净高至少3；门净空2×2，庭内主路3宽，不声称门三宽三高。', '- 高差：外feet2→z24 feet2.5→门/庭feet3→z5 feet3.5→北廊feet4，总高差2，各级0.5。80路线目标及路径见qa/body-routes.json。', '- 候选双门默认关且无红石供电；四向原生分别证明关闭阻断内庭、两跨门路线碰撞受阻。', '- 分析只在隔离测试世界临时改变四半门open=true，逐块确认无其他变化，80目标均以0.6×1.8身体、支撑切片、半阶抬升/平移/下降扫掠通过；随后恢复全部原状态才回捕。没有开门候选NBT或用分析态冒充源。', '- 四方向各400tick强制流体与邻居更新，randomTickSpeed=0，四份完整关门回捕逐块和旋转状态匹配；不称自然随机刻认证或玩家点击交互测试。', '- 14张真实源NBT预览：全景、完整背面、双向内景、双门外阶、内阶细节及四向内外图；所有人眼和细节无裁切，剖视不等于完整源。规则及源SHA/相机/显示记录/图SHA分别在qa/cutaway-rules.json与qa/render.json。', '- 按图构图：关门前墙、台阶前庭、高门廊、左物资堆、右地图板/暖灯台、根墙边池。实心装饰箱替代容器、木石地图近似、灯笼替代火台、完整顶墙是声明适配。', '- 原生检查初轮针对门格心误判已修正为跨门两边缘扫掠；attempt-01/02保留失败XML/log/检查源码，不作为验收结果。源几何未因该分析检查修改。', '- 源码、材料、状态、正确参考哈希和全交接哈希齐备。audit是已交付快照代理，非全组体验终审，台阶向上取整代理不替代原生分析。', '- 自然随机刻、实际玩家交互、玩法、部署NOT_RUN；不动正式存档/mods/config，不提前08。', '']
    (handover / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    files = [path for path in (ROOT / 'source').glob('*') if path.is_file()]
    files += [ROOT / name for name in ['ANALYSIS.md', 'group.json', 'SERIAL_QUEUE.json', 'native-tests.xml', 'audit/summary.json', 'audit/pairs.json', 'audit/descriptors.json', 'audit/similarity-matrix.csv', 'audit/REPORT.md', 'audit/high-similarity.json', 'qa/native-console.log', 'qa/native-invocation.json']]
    files += [path for path in FOLDER.rglob('*') if path.is_file()]
    files += [ROOT / name for name in ['native-test/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'native-test/src/main/resources/data/ftzonereceiver/structures/empty.nbt', 'native-test/src/main/resources/META-INF/mods.toml', 'native-test/src/main/resources/pack.mcmeta', 'server/server.properties']]
    files += [path for path in (ROOT / 'history').rglob('*') if path.is_file()]
    files += [reference, handover / 'README.md', helper_path]
    save(handover / 'FILE-MANIFEST.json', {'source_nbt_sha256': fingerprint, 'manifest_self_excluded': True, 'files': [record(path) for path in files]})
    print(json.dumps({'status': 'READY_FOR_INDEPENDENT_REVIEW', 'sha256': fingerprint, 'native_tests': 4, 'closed_capture_comparisons': 4, 'closed_blocked_open_only_pass': True, 'route_targets': len(paths), 'file_manifest_entries': len(files), 'next_image_authorized': False}))


if __name__ == '__main__':
    main()
