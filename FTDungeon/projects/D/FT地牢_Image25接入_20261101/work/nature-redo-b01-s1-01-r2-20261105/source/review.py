import hashlib
import importlib.util
import json
import zipfile
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
ROOM = ROOT / 'nature-redo-b01-s1-01'


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def record(path):
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}


def main():
    tests = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(tests) == 4 and not any(test.find(tag) is not None for test in tests for tag in ['failure', 'error', 'skipped'])
    assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/native-console.log').read_text(encoding='utf-8')
    metadata = load(ROOM / 'room.json')
    fingerprint = record(ROOM / 'room.nbt')['sha256']
    assert metadata['sha256'] == fingerprint
    render = load(ROOM / 'qa/render.json')
    assert render['source_nbt_sha256'] == fingerprint and render['source_nbt_unchanged']
    for view in render['views']:
        assert record(ROOM / view['file'])['sha256'] == view['sha256']
    reports = []
    for rotation in ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']:
        path = ROOM / ('forge-' + rotation + '.json')
        report = load(path)
        assert report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint and report['stabilization_ticks'] == 400
        reports.append(record(path))
    previous_verifier = ROOT.parent / 'nature-b1-s1-20261104/source/finalize.py'
    specification = importlib.util.spec_from_file_location('previous_capture_verifier', previous_verifier)
    verifier = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(verifier)
    captures = verifier.captures(ROOM)
    audit = load(ROOT / 'audit/summary.json')
    assert not audit['errors']
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 23)
    small = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 17)
    sheet = Image.new('RGB', (1800, 820), '#111822')
    draw = ImageDraw.Draw(sheet)
    draw.text((25, 18), 'B01 逐图重做 / 左：参考图 / 右：最终 NBT 的 Lodestone 软件剖视', font=font, fill='white')
    draw.text((25, 60), '25×15×25 / 四向原生400tick通过 / 外观一致性待验收 / 非统一候选 / B02锁定', font=small, fill='#b9cba5')
    for left, path in [(0, Path(metadata['reference'])), (900, ROOM / 'preview.png')]:
        with Image.open(path) as image:
            image.thumbnail((890, 675))
            sheet.paste(image.convert('RGB'), (left + (900 - image.width) // 2, 105 + (675 - image.height) // 2))
    sheet.save(ROOT / 'reference-comparison.png')
    views = Image.new('RGB', (1600, 710), '#111822')
    view_draw = ImageDraw.Draw(views)
    for index, view in enumerate([entry for entry in render['views'] if entry['id'].endswith('-interior') and entry['id'] != 'player-interior' or entry['id'].endswith('-exterior')]):
        with Image.open(ROOM / view['file']) as image:
            image.thumbnail((400, 300))
            left, top = index % 4 * 400, index // 4 * 350 + 35
            views.paste(image.convert('RGB'), (left, top))
            view_draw.text((left + 8, top - 29), view['id'], font=small, fill='white')
    views.save(ROOM / 'views/eight-views.png')
    checklist = [
        {'feature': 'Central stepped altar, front/right steps and four lamps', 'status': 'IMPLEMENTED_APPROXIMATION', 'difference': 'Integer block stairs replace the finer proportions of the concept image.'},
        {'feature': 'Rear ancient tree with asymmetric branches, roots and crown', 'status': 'IMPLEMENTED_APPROXIMATION', 'difference': 'Tree crown/roots remain less irregular and dense than the reference.'},
        {'feature': 'Left/right arched openings', 'status': 'IMPLEMENTED_APPROXIMATION', 'difference': 'Block-stepped arches; north entrance is an explicit FT safety addition.'},
        {'feature': 'Front-left sealed pool and dry bypass', 'status': 'IMPLEMENTED', 'difference': 'Left/right appearance depends on the inspection camera; metadata defines fixed world axes.'},
        {'feature': 'Mossy masonry, dark roots, warm light and hanging greenery', 'status': 'IMPLEMENTED_APPROXIMATION', 'difference': 'Material coverage and fine decorative density differ; no shader rays or volumetric effects.'},
        {'feature': 'Original-image full appearance match', 'status': 'PENDING_ACCEPTANCE', 'difference': 'No claim of exact reproduction. Side-by-side review required before promotion.'},
    ]
    save(ROOT / 'reference-review.json', {'reference': record(Path(metadata['reference'])), 'nbt': record(ROOM / 'room.nbt'), 'status': 'PENDING_ACCEPTANCE', 'checklist': checklist})
    qa = {'status': 'TECHNICAL_PASS_VISUAL_ACCEPTANCE_PENDING_NOT_A_CANDIDATE', 'id': metadata['id'], 'sha256': fingerprint, 'static': load(ROOM / 'qa/static.json'), 'native': {'status': 'PASS', 'tests': 4, 'stability_ticks': 400, 'reports': reports, 'captures': captures, 'capture_verifier': record(previous_verifier), 'random_tick_speed': 0, 'fluid_ticks_forced': True}, 'render': render, 'reference_consistency': {'status': 'PENDING_ACCEPTANCE', 'report': record(ROOT / 'reference-review.json')}, 'diversity': {'snapshot_rooms': audit['rooms'], 'snapshot_pairs': audit['pairs'], 'high_similarity_current': audit['current_group_high_similarity'], 'status': 'SNAPSHOT_ONLY_FINAL_GLOBAL_AUDIT_PENDING'}, 'ready_for_unified_integration': False, 'next_batch_locked': True, 'production_saves_accessed': False, 'limits': ['No actual client or multiplayer playtest.', 'No occupied-volume refusal or persistence/idempotence test.', 'No puzzle, wave or reward runtime.', 'Reference fidelity is explicitly unaccepted; previous results are excluded.']}
    save(ROOM / 'qa.json', qa)
    save(ROOT / 'ACCEPTANCE.json', {'batch': 'B01', 'reference_id': 'S1-01', 'status': 'PENDING_REFERENCE_CONSISTENCY_ACCEPTANCE', 'technical_tests': 'PASS', 'candidate_allowlist': [], 'ready_for_unified_integration': False, 'next_batch_locked': True, 'old_versions': record(ROOT / 'EXCLUDED_VERSIONS.json')})
    text = '# B01 第1张R2构图修订：待独立验收，非候选\n\n- 仅处理S1-01，25×15×25。原ca9efd版本保留原目录且不再提交为当前版本。\n- 重做单株主导粗厚古树、非对称弯折分枝、多层椭球不规则树冠、后墙盘根及跨主路上方连接祭坛的根桥；移除四角细直木柱和原薄叶板。\n- 增加墙边落地叶丛和双格厚柱；祭坛、三端口和受保护身体路线保留。\n- 内景：views/player-interior.png；古树近景：views/tree-closeup.png；全壳及四向剖视、原图对照均来自本次最终NBT。\n- 原生4方向各400tick，randomTickSpeed=0，强制流体/邻居更新；不称为自然随机刻稳定性认证，不验证长期随机生长。\n- 四向世界回捕逐方块匹配本次NBT；详细结果见qa.json。\n- 尺度仍为参考图的整数解释；构图还原及尺度须独立复核，不自批、不放行02、不部署。\n- 文件：source/为完整生成与验证源码；ANALYSIS.md为解释；reference-review.json为待验收对照；audit/为重复度快照，不是最终体验审计。\n'
    (ROOT / 'README.md').write_text(text, encoding='utf-8')
    archive_path = ROOT / 'B01-review-only-NOT-A-CANDIDATE.zip'
    with zipfile.ZipFile(archive_path, 'w', zipfile.ZIP_DEFLATED) as archive:
        for directory in [ROOM, ROOT / 'source', ROOT / 'audit', ROOT / 'qa']:
            for path in sorted(directory.rglob('*')):
                if path.is_file() and '__pycache__' not in path.parts: archive.write(path, path.relative_to(ROOT).as_posix())
        for name in ['ANALYSIS.md', 'BATCH_PLAN.md', 'EXCLUDED_VERSIONS.json', 'ACCEPTANCE.json', 'README.md', 'reference-comparison.png', 'reference-review.json', 'group.json', 'native-tests.xml']:
            archive.write(ROOT / name, name)
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
    save(ROOT / 'review-package.json', {'status': 'REVIEW_ONLY_NOT_A_CANDIDATE', **record(archive_path)})
    print('REVIEW_READY_NOT_ACCEPTED', archive_path, fingerprint)


if __name__ == '__main__':
    main()
