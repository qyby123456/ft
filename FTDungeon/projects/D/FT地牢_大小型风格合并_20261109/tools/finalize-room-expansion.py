from pathlib import Path
import hashlib
import importlib.util
import json
import zipfile

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / 'output/room-expansion-v035'
SPEC = importlib.util.spec_from_file_location('expansion', ROOT / 'tools/verify-room-expansion.py')
EXPANSION = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(EXPANSION)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def rotate(signature, turns):
    result = signature
    for iteration in range(turns):
        result = {(15 - south, level, east) for east, level, south in result}
    return result


def main():
    plan = json.loads((WORK / 'plan.json').read_text(encoding='utf-8'))
    signatures, rooms, files = [], [], set()
    for batch in range(1, 6):
        report_path = WORK / f'batch-{batch:02}-acceptance.json'
        report = json.loads(report_path.read_text(encoding='utf-8'))
        preview_path = WORK / f'batch-{batch:02}-preview.json'
        preview = json.loads(preview_path.read_text(encoding='utf-8'))
        assert report['status'] == preview['status'] == 'PASS'
        assert len(report['rooms']) == 16 and len(preview['galleries']) == 8
        accepted = {(room['style'], room['key']): room for room in report['rooms']}
        files.update([report_path, preview_path])
        for style in plan['threads']:
            directory = WORK / style['style'] / f'batch-{batch:02}'
            manifest_path = directory / 'manifest.json'
            freeze_path = directory / 'freeze.json'
            manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
            freeze = json.loads(freeze_path.read_text(encoding='utf-8'))
            assert freeze['status'] == 'FROZEN_FOR_PARENT_INTEGRATION'
            frozen = freeze.get('files', freeze.get('artifacts', {}))
            if isinstance(frozen, list):
                frozen = {entry['file']: entry['sha256'] for entry in frozen}
            for filename, sha256 in frozen.items():
                path = (directory / filename).resolve()
                assert directory.resolve() in path.parents and digest(path) == sha256
                files.add(path)
            files.update([manifest_path, freeze_path])
            for entry in manifest['templates']:
                path = directory / entry['file']
                assert digest(path) == entry['sha256'] == accepted[style['style'], entry['key']]['sha256']
                signature = EXPANSION.features(EXPANSION.cells(path))
                signatures.append((style['style'] + '/' + entry['key'], [rotate(signature, turns) for turns in range(4)]))
                rooms.append({'style': style['style'], 'batch': batch, **entry})
    assert len(rooms) == 80 and len({(room['style'], room['key']) for room in rooms}) == 80
    assert len({room['sha256'] for room in rooms}) == 80, 'Byte-identical additions'
    native_log = ROOT / 'verification/room-expansion-native.log'
    native_text = native_log.read_text(encoding='utf-8', errors='replace')
    assert 'FT_ROOM_EXPANSION_PASS:80 rooms;' in native_text and 'BUILD SUCCESSFUL' in native_text
    assert 'All 3 required tests passed' in native_text, 'Native room/generator checks did not all pass'
    native_root = ROOT / 'src/eclipseStyleTest/resources/data/fteclipsestylesqa/expansion'
    native_inventory = json.loads((native_root / 'inventory.json').read_text(encoding='utf-8'))
    native_hashes = {(room['style'], room['key']): room['sha256'] for room in native_inventory['rooms']}
    assert len(native_hashes) == 80
    for room in rooms:
        assert digest(native_root / room['style'] / (room['key'] + '.nbt')) == room['sha256'] == native_hashes[room['style'], room['key']]
    pairs, issues = [], []
    for index, (first_key, orientations) in enumerate(signatures):
        first = orientations[0]
        for second_key, alternatives in signatures[index + 1:]:
            similarities = [len(first & second) / len(first | second) for second in alternatives]
            maximum = max(similarities)
            record = {'first': first_key, 'second': second_key, 'maximumRotationInvariantJaccard': round(maximum, 6), 'rotationDegrees': similarities.index(maximum) * 90}
            pairs.append(record)
            if maximum >= 0.85:
                issues.append(record)
    baseline = json.loads((WORK / 'formal-baseline.json').read_text(encoding='utf-8'))
    formal = ROOT / 'src/main/resources/data/fteclipsegen/roomsets'
    actual_files = {path.relative_to(formal).as_posix() for path in formal.rglob('*') if path.is_file()}
    assert actual_files == baseline.keys()
    assert all(digest(formal / filename) == sha256 for filename, sha256 in baseline.items())
    report = {'schema': 1, 'status': 'FAIL' if issues else 'PASS', 'rooms': 80, 'styles': 8, 'batches': 5, 'perStyle': {style['style']: 10 for style in plan['threads']}, 'uniqueNbtHashes': 80, 'pairComparisons': len(pairs), 'orientationComparisons': len(pairs) * 4, 'method': 'Interior occupied-voxel Jaccard, materials ignored, common outer walls/floor/roof removed; compare 0/90/180/270 degrees for every pair across all batches and styles.', 'highestSimilarities': sorted(pairs, key=lambda record: record['maximumRotationInvariantJaccard'], reverse=True)[:40], 'issues': issues, 'nativePlacement': {'status': 'PASS', 'rooms': 80, 'requiredTests': 3, 'logSha256': digest(native_log)}, 'formalResourcesUnchanged': len(baseline), 'runtimeIntegrated': False, 'deployed': False, 'roomInventory': rooms}
    report_path = WORK / 'acceptance.json'
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    assert not issues, 'Global repetition review required'
    readme = WORK / 'README.md'
    readme.write_text('# 八风格新增80房间\n\n八风格各10间，分五批完成。每批先下发父线程逐房提示词，再由风格线程生成、四视图检查、风格及重复度自检，父线程独立解码NBT复核后推进。\n\n- 独立编号 addition/01–10；原有每风格27间保留。\n- 每间16×16×16，继承manifest内baseKey的原生门口与四组刷怪接口。\n- 刷怪位置保持空气、完整支撑及净空；运行时再放置符印。\n- 结构验收覆盖4096个完整方块坐标、门口/刷怪位净空与支撑、步行可达及墙顶封闭。\n- 四视图从导出的实际NBT投影，允许切顶/剖墙并有说明；浏览器检查无脚本错误及坏图。\n- 全80间两两比较并检查四种旋转，忽略材质以识别换皮或旋转复用。阈值0.85，详见acceptance.json。\n- 保留NBT、逐房提示词、配方、视图、自检、冻结和父线程报告。\n\n本交付为新增房间素材及验收包。尚未接入运行时随机房间选择器，尚未替换已安装0.34.0；离线检查不代表游戏内逐房验收。\n\n主入口：index.html。完整设计：全部房间提示词.md。配方入口在项目tools/expand-STYLE.mjs，按各配方支持的批次参数重放；依赖本机minecraft-builder-skill的Lodestone工具及原有只读渲染器。\n', encoding='utf-8')
    files.update([report_path, readme, WORK / 'index.html', WORK / 'plan.json', WORK / '全部房间提示词.md', WORK / 'formal-baseline.json', WORK / '父线程复核记录.md'])
    files.update(path for path in (WORK / 'previews').rglob('*') if path.is_file())
    for style in plan['threads']:
        files.update((WORK / style['style']).glob('*-prompt.md'))
        files.add(WORK / style['style'] / 'prompts.json')
    package_path = ROOT / 'output/八风格新增80房间-分批验收素材包.zip'
    with zipfile.ZipFile(package_path, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(files):
            archive.write(path, 'room-expansion-v035/' + path.relative_to(WORK).as_posix())
        for style in plan['threads']:
            for prefix in ['expand', 'rebuild']:
                recipe = ROOT / f'tools/{prefix}-{style["style"]}.mjs'
                archive.write(recipe, recipe.relative_to(ROOT).as_posix())
        for filename in ['tools/prepare-room-expansion.mjs', 'tools/verify-room-expansion.py', 'tools/finalize-room-expansion.py', 'tools/integrate-style-rebuild.py', 'tools/room-expansion-overview.mjs', 'tools/prepare-expansion-native.py', 'verification/preview_expansion.mjs', 'verification/room-expansion-native.log', 'src/eclipseStyleTest/java/eclipse/generator/ExpansionGameTests.java', 'src/main/resources/data/fteclipsegen/catalog.json']:
            archive.write(ROOT / filename, filename)
    print(json.dumps({'status': 'PASS', 'rooms': 80, 'pairComparisons': len(pairs), 'highestJaccard': report['highestSimilarities'][0]['maximumRotationInvariantJaccard'], 'package': str(package_path), 'sha256': digest(package_path)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
