"""Reversibly separate legacy generated material from the working asset library."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
from datetime import datetime

ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parents[1]
ARCHIVE = WORKSPACE / 'project-archives/FarmingTales-Drink-Animations/2026-09-17-materials'
LIBRARY = ROOT / '素材库'
PLAN = LIBRARY / '整理计划.json'
REPORT = LIBRARY / '整理结果.json'

def native(path):
    return Path('\\\\?\\' + str(path.absolute())) if os.name == 'nt' else path

def sha(path):
    with native(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def files(folder):
    for base, directories, names in os.walk(folder, followlinks=False):
        assert not any((Path(base) / d).is_symlink() for d in directories)
        for name in sorted(names):
            path = Path(base) / name
            assert not path.is_symlink()
            yield path

def guarded(path, root):
    resolved = path.resolve()
    assert resolved.is_relative_to(root.resolve()) and resolved != root.resolve(), str(path)
    return resolved

def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def metadata(path):
    stat = native(path).stat()
    return dict(bytes=stat.st_size, mtime_ns=stat.st_mtime_ns, file_id=stat.st_ino, volume=stat.st_dev)

def plan():
    assert not REPORT.exists(), 'This dated organization has already completed.'
    assert not ARCHIVE.exists(), 'Archive destination already exists; inspect it before retrying.'
    roots = []
    # Preserve current editing sets, the original approved jam, and v20/v34 model baselines.
    for path in sorted((ROOT / 'blockbench').iterdir()):
        if not path.is_dir():
            continue
        name = path.name
        match = re.fullmatch(r'(酒水扩展|酱罐动画|上古水果果酱|纸鸢酒水动画)-v(\d+)', name)
        if not match:
            continue
        family, version = match.group(1), int(match.group(2))
        keep = {'酒水扩展': {20,34,54}, '酱罐动画': {54},
                '上古水果果酱': {49,54}, '纸鸢酒水动画': {68,69}}[family]
        if version in keep:
            continue
        roots.append((path, 'superseded_blockbench'))
        if path.with_suffix('.zip').exists():
            roots.append((path.with_suffix('.zip'), 'superseded_blockbench'))
    # Exact screenshot references in release reports and scripts remain immediately available.
    referenced = set()
    for parent in (ROOT, ROOT / 'preview'):
        for path in parent.iterdir():
            if path.is_file() and path.suffix in ('.json','.py','.md','.txt','.ps1'):
                text = path.read_text(encoding='utf-8-sig', errors='replace')
                referenced.update(re.findall(r'[A-Za-z0-9_.-]+\.png', text))
    for path in sorted((ROOT / 'run-smoke/screenshots').iterdir()):
        if not path.is_file() or path.name in referenced:
            continue
        # Dense motion captures from old clients; v184/v1858 and all named keyframes stay.
        old = re.match(r'(?:shader-)?v(\d+)-catalog-', path.name)
        if re.search(r'-motion-\d+\.png$', path.name) and (
                path.name.startswith('catalog-') or old and int(old.group(1)) < 184):
            roots.append((path, 'historical_motion_frames'))
    for name in ('hand-clipping-before', 'shader-before'):
        path = ROOT / 'preview' / name
        if path.exists():
            roots.append((path, 'historical_render_comparison'))
    actions, inventory = [], []
    for path, category in roots:
        guarded(path, ROOT)
        rel = path.relative_to(ROOT).as_posix()
        target = guarded(ARCHIVE / rel, WORKSPACE / 'project-archives')
        assert not target.exists()
        records = []
        for item in files(path) if path.is_dir() else (path,):
            records.append(dict(path=item.relative_to(ROOT).as_posix(), **metadata(item)))
        actions.append(dict(path=rel, category=category, directory=path.is_dir(),
                            files=len(records), bytes=sum(row['bytes'] for row in records)))
        inventory.extend(records)
    protected = {}
    for folder in ('references', 'src/main', 'src/test', 'src/smoke'):
        for path in files(ROOT / folder):
            protected[path.relative_to(ROOT).as_posix()] = sha(path)
    installed = next((ROOT.parents[2] / 'mods').glob('*farmingtales_drink_anim*.jar'))
    baseline = dict(installed=str(installed), installed_sha256=sha(installed), protected=protected)
    write_json(LIBRARY / '保留文件校验.json', baseline)
    write_json(LIBRARY / '归档文件清单.json', inventory)
    data = dict(project=str(ROOT), archive=str(ARCHIVE), created=datetime.now().isoformat(),
                files=len(inventory), bytes=sum(a['bytes'] for a in actions), actions=actions)
    write_json(PLAN, data)
    print(json.dumps(dict(actions=len(actions),files=data['files'],GiB=round(data['bytes']/2**30,2),
                          protected_files=len(protected)), ensure_ascii=False), flush=True)

def apply():
    assert not REPORT.exists()
    data = json.loads(PLAN.read_text(encoding='utf-8'))
    assert Path(data['project']) == ROOT and Path(data['archive']) == ARCHIVE
    if ARCHIVE.exists():
        assert json.loads((ARCHIVE / '整理计划.json').read_text(encoding='utf-8')) == data
    inventory = json.loads((LIBRARY / '归档文件清单.json').read_text(encoding='utf-8'))
    for record in inventory:
        path = guarded(ROOT / record['path'], ROOT)
        if not native(path).exists():
            path = guarded(ARCHIVE / record['path'], ARCHIVE)
        assert metadata(path) == {k: record[k] for k in ('bytes','mtime_ns','file_id','volume')}, path
    ARCHIVE.mkdir(parents=True, exist_ok=True)
    shutil.copy2(PLAN, ARCHIVE / '整理计划.json')
    shutil.copy2(LIBRARY / '归档文件清单.json', ARCHIVE / '归档文件清单.json')
    shutil.copy2(ROOT / 'restore_material_archive.py', ARCHIVE / 'restore_material_archive.py')
    for index, action in enumerate(data['actions']):
        source = guarded(ROOT / action['path'], ROOT)
        target = guarded(ARCHIVE / action['path'], ARCHIVE)
        native(target.parent).mkdir(parents=True, exist_ok=True)
        if native(target).exists():
            assert not native(source).exists()
            continue
        assert native(source).stat().st_dev == native(target.parent).stat().st_dev, 'Require same-volume rename.'
        native(source).rename(native(target))
        if index % 5000 == 0:
            print(f'Archived {index + 1}/{len(data["actions"])} entries', flush=True)
    for record in inventory:
        target = guarded(ARCHIVE / record['path'], ARCHIVE)
        assert metadata(target) == {k: record[k] for k in ('bytes','mtime_ns','file_id','volume')}, target
    # Import the three known user reference videos with their existing source hashes.
    original_index = json.loads((ROOT / 'references/2026-09-13/index.json').read_text(encoding='utf-8'))
    imported = []
    for video in original_index['videos']:
        source = Path(video['file'])
        assert source.is_file() and sha(source) == video['sha256'], source
        target = ROOT / 'references/original-videos' / source.name
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            shutil.copy2(source, target)
        assert sha(target) == video['sha256']
        imported.append(dict(path=target.relative_to(ROOT).as_posix(), original_file=str(source),
                             sha256=video['sha256'], duration_seconds=video['duration_seconds']))
    # Check all preserved assets before changing any reference index paths.
    baseline = json.loads((LIBRARY / '保留文件校验.json').read_text(encoding='utf-8'))
    for name, digest in baseline['protected'].items():
        assert sha(ROOT / name) == digest, name
    assert sha(Path(baseline['installed'])) == baseline['installed_sha256']
    write_json(REPORT, dict(completed=datetime.now().isoformat(), archived_files=data['files'],
                           archived_bytes=data['bytes'], archive=str(ARCHIVE), deleted_files=0,
                           original_reference_files_preserved=sum(s.startswith('references/') for s in baseline['protected']),
                           imported_videos=imported, installed_jar_unchanged=True,
                           source_files_unchanged=True, retained_file_hashes_verified=True,
                           archive_file_ids_sizes_timestamps_verified=True))
    print(f'Completed: {data["files"]} files archived, all originals retained, three reference videos imported.', flush=True)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('mode', choices=('plan','apply'))
    args = parser.parse_args()
    plan() if args.mode == 'plan' else apply()
