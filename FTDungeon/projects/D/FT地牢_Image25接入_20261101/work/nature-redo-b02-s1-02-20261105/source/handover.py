import hashlib
import importlib.util
import json
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
FOLDER = ROOT / 'nature-redo-b02-s1-02'


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
    cutaway = {'roof': 'Display only: hide y>=12.', 'hero': 'Additionally hide x<=1 or z>=19 at y>=2.', 'directional': 'At y>=2 hide north z<=1, south z>=19, west x<=1 or east x>=21 respectively.', 'full': 'No blocks removed, including player-interior and four exterior views.', 'native_nbt_modified': False}
    save(FOLDER / 'qa/cutaway-rules.json', cutaway)
    save(FOLDER / 'qa.json', {'status': 'PRODUCER_TECHNICAL_PASS_PENDING_INDEPENDENT_REVIEW', 'source_nbt_sha256': fingerprint, 'static': static, 'support': support, 'native_reports': native, 'native_capture_exact_comparisons': captures, 'test_count': 4, 'render_views': 11, 'random_tick_speed': 0, 'scope': '400 forced fluid/neighbor ticks per rotation; no natural random tick stability claim. No deployment.'})
    save(FOLDER / 'reference-review.json', {'status': 'PENDING_INDEPENDENT_REVIEW', 'reference': record(reference), 'structure': record(FOLDER / 'room.nbt'), 'image_landmarks': ['Low central square basin', 'Back-left desk and barrel', 'Back-right bookshelf and chest niche', 'Wall roots with hanging lamps and leaves', 'Front-side water pools'], 'adaptations': ['23x13x21 integer interpretation, not exact image measurement', 'Full roof and walls; south connector added', 'Short waterfall represented by a sealed glazed source-water column'], 'cutaway_rules': cutaway, 'next_image_authorized': False})
    save(ROOT / 'SERIAL_QUEUE.json', {'group': 'S1', 'accepted': [{'image': 'S1-01', 'revision': 'R3', 'sha256': group['previous_accepted_sha256'], 'authority': 'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603', 'evidence_readonly': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S1-01-R3-independent'}], 'current': {'image': 'S1-02', 'sha256': fingerprint, 'status': 'PENDING_INDEPENDENT_REVIEW'}, 'next': {'image': 'S1-03', 'authorized_to_start': False}, 'production_deployed': False})
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 22)
    canvas = Image.new('RGB', (1800, 790), '#151d29')
    draw = ImageDraw.Draw(canvas)
    draw.text((25, 15), 'S1-02 原图 / 实际NBT剖视 / 完整屋顶下玩家内景 · 待独立复核', font=font, fill='white')
    for column, (path, label) in enumerate([(reference, '参考图'), (FOLDER / 'preview.png', '实际NBT；仅显示裁切'), (FOLDER / 'views/player-interior.png', '完整结构内景；无裁切')]):
        with Image.open(path) as image:
            image = image.convert('RGB')
            image.thumbnail((590, 660))
            canvas.paste(image, (column * 600 + (600 - image.width) // 2, 85 + (660 - image.height) // 2))
        draw.text((column * 600 + 15, 55), label, font=font, fill='#d2e6cf')
    canvas.save(FOLDER / 'reference-comparison.png')
    handover = ROOT / 'producer-handover-20261105'
    handover.mkdir(exist_ok=True)
    content = '\n'.join(['# S1-02 制作方交接', '', '- 状态：制作方技术检查通过，待独立逐图复核；非候选，不自动解锁03。', '- 仅制作第二张；S1-01 R3已接受的登记见SERIAL_QUEUE.json。', f'- room.nbt SHA-256：{fingerprint}', '- 尺寸23×13×21；6279条含空气记录；北/南连接口，主路绕低方泉两侧通行。', '- 四方向原生放置各400tick，强制流体/邻居更新，randomTickSpeed=0；不宣称自然随机刻稳定。', '- 4份世界回捕逐方块、旋转状态、空容器检查均匹配最终NBT；六面连接无离地装饰簇。', '- 11张实际NBT Lodestone预览；非游戏截图。裁切只影响渲染副本，详见模块qa/cutaway-rules.json。', '- 右前短瀑改为玻璃封闭源水槽，完整墙顶和南出口为适配；构图和尺度仍待独立判定。', '- audit/包含本图、已接受01及现有交付的重复度快照，非全量终审；不以调色板差异替代布局差异。', '- source/、ANALYSIS.md、group.json、模块room.json、qa.json和reference-review.json均位于本修订目录。', '- 未部署，未触碰正式存档/mods/config或独立复核目录。', ''])
    (handover / 'README.md').write_text(content, encoding='utf-8')
    files = [path for path in (ROOT / 'source').glob('*') if path.is_file()]
    files += [ROOT / name for name in ['ANALYSIS.md', 'group.json', 'SERIAL_QUEUE.json', 'native-tests.xml', 'audit/summary.json', 'audit/pairs.json', 'audit/descriptors.json', 'audit/similarity-matrix.csv', 'audit/REPORT.md', 'qa/native-console.log']]
    files += [path for path in FOLDER.rglob('*') if path.is_file()]
    files += [handover / 'README.md', helper]
    save(handover / 'FILE-MANIFEST.json', {'source_nbt_sha256': fingerprint, 'files': [record(path) for path in files]})
    print(json.dumps({'status': 'READY_FOR_INDEPENDENT_REVIEW', 'sha256': fingerprint, 'native_tests': len(tests), 'capture_comparisons': len(captures), 'file_manifest_entries': len(files), 'next_image_authorized': False}, ensure_ascii=False))


if __name__ == '__main__':
    main()
