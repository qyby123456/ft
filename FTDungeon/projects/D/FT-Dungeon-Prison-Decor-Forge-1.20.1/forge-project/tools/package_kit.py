import argparse
import hashlib
import json
import shutil
import zipfile
from pathlib import Path

from export_kit import PROJECT, RESOURCES, ROOT, resolve_native_model


JAR_NAME = 'FT地牢装饰-指定12款重制-Forge-1.20.1-1.1.0.jar'
ZIP_NAME = 'FT地牢装饰-指定12款重制-完整交付-20261103.zip'
MODEL_ZIP_NAME = 'FT地牢装饰-重制12款-可编辑模型-20261103.zip'


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--minecraft-jar', type=Path, required=True)
    arguments = parser.parse_args()
    for filename in ('delivery-audit.json', 'forge-native.json', 'browser-preview.json', 'remake-scope.json'):
        report = read(ROOT / 'qa' / filename)
        if report['status'].lower() != 'pass':
            raise ValueError('Not passed: ' + filename)
    independent = read(ROOT / 'qa/independent/report.json')
    if not independent['status'].upper().startswith('PASS'):
        raise ValueError('Independent QA not passed')
    scope = read(ROOT / 'qa/remake-scope.json')
    if len(scope['remade']) != 12 or scope['unchanged_models_count'] != 12:
        raise ValueError('Requested remake scope not verified')
    native = read(ROOT / 'qa/forge-native.json')
    if native['native_states_checked'] != 190 or native['native_model_parses'] != 59:
        raise ValueError('Native complete state/model coverage missing')
    skills = read(ROOT / 'qa/skill-audit.json')['reports']
    if len(skills) != 83 or any(report['errors'] or report['warnings'] for report in skills):
        raise ValueError('Skill audit not clean')
    for manifest_name, relative_base in (('authoring-hashes.json', ROOT), ('resource-hashes.json', RESOURCES)):
        for filename, expected in read(ROOT / 'qa' / manifest_name).items():
            if sha256(relative_base / filename) != expected:
                raise ValueError('Frozen input changed: ' + filename)
    built_jar = PROJECT / 'build/libs/ft-dungeon-prison-decor-forge-1.20.1-1.1.0.jar'
    if read(ROOT / 'qa/delivery-audit.json')['jar_sha256'] != sha256(built_jar):
        raise ValueError('Final JAR has not been audited')
    blocks = []
    with zipfile.ZipFile(arguments.minecraft_jar) as vanilla:
        for path in sorted((RESOURCES / 'assets/ftdungeonprops/models/block').glob('*.json')):
            model = resolve_native_model(read(path), vanilla)
            blocks.append({'model': path.stem, 'boxes': len(model['elements']),
                           'declared_faces': sum(len(element['faces']) for element in model['elements'])})
    costs = read(ROOT / 'qa/export-costs.json')
    performance = {'default_one_of_each_block': {'boxes': sum(entry['default_selected_boxes'] for entry in costs['blocks']),
                                               'declared_faces': sum(entry['default_selected_faces'] for entry in costs['blocks'])},
                   'all_declared_block_models': {'models': len(blocks), 'boxes': sum(entry['boxes'] for entry in blocks),
                                                 'declared_faces': sum(entry['declared_faces'] for entry in blocks)},
                   'per_model': blocks, 'sprites': len(costs['sprites']), 'pixels': len(costs['sprites']) * 1024,
                   'rgba_base_bytes_estimate': costs['rgba_base_bytes'],
                   'block_entities': 0, 'animation_frames_per_sprite': 1,
                   'notes': ['All-declared includes mutually exclusive lit/off variants and native shape families.',
                             'Default-selected is one selected default model for each of the 24 block types.',
                             'Sprites enter the vanilla block atlas. No measured draw calls, GPU memory, client lighting or FPS.']}
    (ROOT / 'qa/performance-summary.json').write_text(json.dumps(performance, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    delivered_jar = ROOT / JAR_NAME
    shutil.copy2(built_jar, delivered_jar)
    resource_folders = ('model_sources', 'preview', 'lanes', 'revisions', 'qa')
    files = [ROOT / 'README.md', ROOT / 'CREDITS.md', ROOT / 'REMAKE.md', ROOT / 'catalog.json', delivered_jar]
    for folder in resource_folders:
        for path in (ROOT / folder).rglob('*'):
            relative = path.relative_to(ROOT)
            if not path.is_file() or any(part in ('work', '__pycache__') for part in relative.parts):
                continue
            if path.name == 'skill-audit-sample.json':
                continue
            files.append(path)
    for path in PROJECT.rglob('*'):
        relative = path.relative_to(PROJECT)
        if path.is_file() and not any(part in ('build', 'run', '.gradle', '__pycache__') for part in relative.parts):
            files.append(path)
    audited_inputs = read(ROOT / 'qa/independent/final-input-hashes.json')
    if audited_inputs['jar_sha256'] != sha256(built_jar):
        raise ValueError('Independent QA does not bind the final JAR')
    for filename, expected in audited_inputs['files'].items():
        path = (ROOT / filename).resolve()
        if not path.is_relative_to(ROOT.resolve()) or sha256(path) != expected:
            raise ValueError('Independent audited input changed: ' + filename)
        files.append(path)
    for filename, expected in read(ROOT / 'qa/independent/uv-before-evidence.json')['snapshots'].items():
        path = (ROOT / filename).resolve()
        if not path.is_relative_to(ROOT.resolve()) or sha256(path) != expected:
            raise ValueError('Repair-history evidence changed: ' + filename)
        files.append(path)
    files = sorted(set(files), key=lambda path: path.relative_to(ROOT).as_posix())
    contents = {path.relative_to(ROOT).as_posix(): sha256(path) for path in files}
    contents_path = ROOT / 'CONTENTS-SHA256.json'
    contents_path.write_text(json.dumps(contents, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    files.append(contents_path)
    archive_path = ROOT / ZIP_NAME
    with zipfile.ZipFile(archive_path, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in files:
            information = zipfile.ZipInfo(path.relative_to(ROOT).as_posix(), date_time=(2026, 11, 3, 0, 0, 0))
            information.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(information, path.read_bytes())
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None:
            raise ValueError('Delivery ZIP failed CRC')
        for filename, expected in contents.items():
            if hashlib.sha256(archive.read(filename)).hexdigest() != expected:
                raise ValueError('Delivery ZIP mismatch: ' + filename)
    model_archive = ROOT / MODEL_ZIP_NAME
    selected_entries = [entry for entry in read(ROOT / 'catalog.json') if 'material_set' in entry]
    model_files = [ROOT / 'REMAKE.md', ROOT / 'README.md', ROOT / 'CREDITS.md',
                   ROOT / 'preview/remade-12.png', ROOT / 'preview/before-after-12.png',
                   ROOT / 'preview/slab-native-forms.png']
    for entry in selected_entries:
        identifier = entry['id']
        model_files.extend((ROOT / f'model_sources/{identifier}.bbmodel',
                            ROOT / f'preview/four-view/{identifier}-sheet.png'))
    with zipfile.ZipFile(model_archive, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        archive.writestr('打开模型说明.txt', '此精简包包含本次12份自带贴图的.bbmodel与真实导出预览。用Blockbench打开model_sources里的文件即可编辑。它不是可安装资源包；游戏模组与全部24款运行资源在完整交付包中。06保留原生台阶形状，重做材质；其余11款重做几何和材质。\n')
        for path in model_files:
            archive.writestr(path.relative_to(ROOT).as_posix(), path.read_bytes())
    with zipfile.ZipFile(model_archive) as archive:
        if archive.testzip() is not None or sum(name.endswith('.bbmodel') for name in archive.namelist()) != 12:
            raise ValueError('Selected editable-model ZIP failed validation')
        for path in model_files:
            if archive.read(path.relative_to(ROOT).as_posix()) != path.read_bytes():
                raise ValueError('Selected editable model differs from final source')
    release = {'status': 'pass', 'jar': JAR_NAME, 'jar_sha256': sha256(delivered_jar),
               'archive': ZIP_NAME, 'archive_sha256': sha256(archive_path),
               'archive_files': len(files), 'archive_bytes': archive_path.stat().st_size,
               'blocks': 24, 'model_sources': 24, 'native_states': 190, 'software_views': 96,
               'remade_numbers': [6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 21, 23],
               'unchanged_models_verified': 12, 'version': '1.1.0',
               'selected_model_archive': MODEL_ZIP_NAME, 'selected_model_archive_sha256': sha256(model_archive),
               'deployment': 'not installed; no existing mod, world or shared Blockbench project modified',
               'client_test': 'not performed'}
    (ROOT / 'RELEASE.json').write_text(json.dumps(release, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(release, ensure_ascii=False))


if __name__ == '__main__':
    main()
