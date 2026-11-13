import hashlib
import importlib.util
import json
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b04-s1-04'


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
    fingerprint = record(FOLDER / 'room.nbt')['sha256']
    assert fingerprint == metadata['sha256'] == group['rooms'][0]['sha256']
    tests = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(tests) == 4 and all(not any(test.find(tag) is not None for tag in ['failure', 'error', 'skipped']) for test in tests)
    assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/native-console.log').read_text(encoding='utf-8')
    native = []
    for rotation in ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']:
        path = FOLDER / ('forge-' + rotation + '.json')
        report = load(path)
        assert report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint
        assert report['random_tick_speed'] == 0 and report['stabilization_ticks'] == 400
        assert report['initial'] == report['settled']
        native.append(record(path))
    helper = ROOT.parent / 'nature-b1-s1-20261104/source/finalize.py'
    spec = importlib.util.spec_from_file_location('capture_helper', helper)
    helper_module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper_module)
    captures = helper_module.captures(FOLDER)
    source_structure = helper_module.Structure.read(FOLDER / 'room.nbt')
    banner_checks = []
    for turns, rotation in enumerate(helper_module.ROTATIONS):
        captured = helper_module.Structure.read(FOLDER / ('native-placed-' + rotation + '.nbt'))
        for point, entity in source_structure.block_entities.items():
            content = helper_module.plain(entity)
            if content['id'] != 'minecraft:banner':
                continue
            across, vertical, forward = point
            width, height, depth = source_structure.size
            target = [(across, vertical, forward), (depth - 1 - forward, vertical, across), (width - 1 - across, vertical, depth - 1 - forward), (forward, vertical, width - 1 - across)][turns]
            assert helper_module.plain(captured.block_entities[target])['Patterns'] == content['Patterns']
            banner_checks.append({'rotation': rotation, 'source_position': point, 'pattern_preserved': True})
    assert len(banner_checks) == 8
    save(FOLDER / 'qa/banner-captures.json', {'status': 'PASS', 'source_nbt_sha256': fingerprint, 'banner_count': 2, 'rotation_checks': banner_checks, 'container_count': 0})
    support = load(FOLDER / 'qa/support.json')
    static = load(FOLDER / 'qa/static.json')
    render = load(FOLDER / 'qa/render.json')
    assert support['status'] == static['status'] == 'PASS'
    assert support['source_nbt_sha256'] == render['source_nbt_sha256'] == fingerprint
    assert render['source_nbt_unchanged'] and len(render['views']) == 11
    for view in render['views']:
        assert record(FOLDER / view['file'])['sha256'] == view['sha256']
    reference = Path(group['rooms'][0]['reference'])
    assert record(reference)['sha256'] == group['group_fingerprint']
    audit = load(ROOT / 'audit/summary.json')
    assert not audit['errors'] and not audit['current_group_high_similarity']
    cutaway = {'roof': 'Display only: hide y>=17.', 'hero': 'Additionally hide x<=1 or z>=33 at y>=2.', 'directional': 'At y>=2 hide north z<=1, south z>=33, west x<=1 or east x>=29 respectively.', 'full': 'No blocks removed, including player-interior and four exterior views.', 'native_nbt_modified': False}
    save(FOLDER / 'qa/cutaway-rules.json', cutaway)
    save(FOLDER / 'qa.json', {'status': 'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW', 'source_nbt_sha256': fingerprint, 'static': static, 'support': support, 'native_reports': native, 'native_capture_exact_comparisons': captures, 'test_count': 4, 'render_views': 11, 'random_tick_speed': 0, 'scope': '400 forced fluid/neighbor ticks per rotation; no natural random tick stability claim. No deployment.'})
    save(FOLDER / 'reference-review.json', {'status': 'PENDING_INDEPENDENT_REVIEW', 'reference': record(reference), 'structure': record(FOLDER / 'room.nbt'), 'image_landmarks': ['Long straight central aisle and moss floor medallion', 'Three pairs of thick columns with stepped plinths', 'Tall stepped arch at north end', 'Twin front-side shallow pools', 'Side-wall roots, overhead foliage and red/gold wall banners'], 'adaptations': metadata['deviations'], 'preview_limitation': 'Installed Lodestone renders banners as plain red; yellow border/stripe patterns are present in final NBT and validated in four native captures. No altered pixels or painted overlay.', 'cutaway_rules': cutaway, 'next_image_authorized': False})
    previous_queue = load(ROOT.parent / 'nature-redo-b03-s1-03-20261105/SERIAL_QUEUE.json')
    accepted = previous_queue['accepted'] + [{'image': 'S1-03', 'sha256': group['previous_accepted_sha256'], 'authority': 'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603', 'evidence_readonly': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S1-03-independent', 'scope': 'Independent static/file/body-route review; interaction/gameplay/deployment NOT_RUN, no natural random tick certification.'}]
    save(ROOT / 'SERIAL_QUEUE.json', {'group': 'S1', 'accepted': accepted, 'current': {'image': 'S1-04', 'sha256': fingerprint, 'status': 'PENDING_INDEPENDENT_REVIEW'}, 'next': {'image': 'S1-05', 'authorized_to_start': False}, 'production_deployed': False})
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 22)
    canvas = Image.new('RGB', (1800, 790), '#151d29')
    draw = ImageDraw.Draw(canvas)
    draw.text((25, 15), 'S1-04 原图 / 实际NBT剖视 / 完整屋顶下玩家内景 · 待独立复核', font=font, fill='white')
    for column, (path, label) in enumerate([(reference, '参考图'), (FOLDER / 'preview.png', '实际NBT；仅显示裁切'), (FOLDER / 'views/player-interior.png', '完整结构内景；无裁切')]):
        with Image.open(path) as image:
            image = image.convert('RGB')
            image.thumbnail((590, 660))
            canvas.paste(image, (column * 600 + (600 - image.width) // 2, 85 + (660 - image.height) // 2))
        draw.text((column * 600 + 15, 55), label, font=font, fill='#d2e6cf')
    canvas.save(FOLDER / 'reference-comparison.png')
    handover = ROOT / 'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    content = '\n'.join(['# S1-04 制作方交接', '', '- 状态：制作方技术检查通过，待独立逐图复核；非候选，不自动解锁05。', '- 仅制作第四张；01/02/03接受登记见SERIAL_QUEUE.json。', f'- room.nbt SHA-256：{fingerprint}', '- 尺寸31×18×35；19530条含空气记录。三对厚柱、宽中轴、北端高拱、双前池、苔藓纹章和红金壁旗。', '- 四方向原生放置各400tick，强制流体/邻居更新，randomTickSpeed=0；不宣称自然随机刻稳定。', '- 4份世界回捕逐方块和旋转状态匹配最终NBT；两面壁旗图案四向共8次另核，旗幡是非容器方块实体，实际无容器。六面连接无离地装饰簇。', '- 原版方块碰撞形状检查通路与地面承托；声明路线额外验证第三格净空。不是玩家手动交互或玩法测试。', '- 11张实际NBT Lodestone预览；非游戏截图。裁切只影响渲染副本，详见模块qa/cutaway-rules.json。', '- 完整顶墙、平齐前阈口、整方块拱头/根部属于已声明适配；图中体积光和外部森林未制作。', '- audit/包含本图、已接受01/02/03及现有交付的重复度快照，非全量终审；不以调色板差异替代布局差异。', '- source/、ANALYSIS.md、group.json、模块room.json、qa.json和reference-review.json均位于本修订目录。', '- 未部署，未触碰正式存档/mods/config或独立复核目录。', ''])
    (handover / 'README.md').write_text(content, encoding='utf-8')
    with (handover / 'README.md').open('a', encoding='utf-8') as stream:
        stream.write('- 预览限制：当前Lodestone壁旗只显示红底，黄色边框/竖纹在真实NBT与四向原生回捕数据中保留；未对截图补绘图案。\n')
    files = [path for path in (ROOT / 'source').glob('*') if path.is_file()]
    files += [ROOT / name for name in ['ANALYSIS.md', 'group.json', 'SERIAL_QUEUE.json', 'native-tests.xml', 'audit/summary.json', 'audit/pairs.json', 'audit/descriptors.json', 'audit/similarity-matrix.csv', 'audit/REPORT.md', 'qa/native-console.log']]
    files += [path for path in FOLDER.rglob('*') if path.is_file()]
    files += [handover / 'README.md', helper]
    files += [ROOT / name for name in ['native-test/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'qa/native-invocation.json']]
    save(handover / 'FILE-MANIFEST.json', {'source_nbt_sha256': fingerprint, 'files': [record(path) for path in files]})
    print(json.dumps({'status': 'READY_FOR_INDEPENDENT_REVIEW', 'sha256': fingerprint, 'native_tests': len(tests), 'capture_comparisons': len(captures), 'file_manifest_entries': len(files), 'next_image_authorized': False}, ensure_ascii=False))


if __name__ == '__main__':
    main()
