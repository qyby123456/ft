"""Physically organize Blockbench material and delete user-authorized obsolete outputs."""
from pathlib import Path
from collections import Counter
import ast
import csv
import hashlib
import json
import os
import re
import shutil
import sys
from datetime import datetime

ROOT = Path(__file__).resolve().parent
BB = ROOT / 'blockbench'
LIB = ROOT / '素材库'
ARCHIVE = ROOT.parents[1] / 'project-archives/FarmingTales-Drink-Animations/2026-09-17-materials'
AUDIT = LIB / '整理记录/2026-09-17-直接清理'
GROUPS = {
    '01-常用工程': ['酒水扩展-v54','酱罐动画-v54','巧克力酱补充-v55','鸡尾酒动画-v56','杯底修复-v57',
                  '纸鸢酒水动画-v68','纸鸢酒水动画-v69','葡园精酿酒水动画-v70'],
    '02-模板与基准': ['肘部联动与原创音效-v14','纤细手臂与玻璃瓶颈-v9','上古水果果酱-v54',
                    '上古水果果酱-v49','酒水扩展-v20','酒水扩展-v34'],
    '03-参考素材': ['木勺画风参考','用户持握与精简关键帧-v6','果酱小木勺-v2'],
}
ACTIVE_TOOLS = {'export_drinks_v54.py','export_jam_v54.py','export_jar_family_v54.py',
                'export_chocolate_v55.py','export_cocktails_v56.py','export_papercraft_v68.py',
                'export_guang_v69.py','export_letsdo_v70.py','export_elbow_v14.py',
                'make_wooden_spoon_v2.py','package_timing_v54.py','package_preview_v14.py'}
KEEP_INSTRUCTIONS = {34,49,54,56,68,69,70}

def native(path):
    return Path('\\\\?\\' + str(path.absolute())) if os.name == 'nt' else path

def check(path, root):
    path=path.resolve();root=root.resolve()
    assert path != root and path.is_relative_to(root), str(path)
    assert not path.is_symlink(), str(path)
    return path

def files(path):
    for base,ds,fs in os.walk(native(path),followlinks=False):
        assert not any((Path(base)/d).is_symlink() for d in ds)
        for name in fs:
            item=Path(base)/name
            yield Path(str(item).removeprefix('\\\\?\\'))

def digest(path):
    with native(path).open('rb') as stream:
        return hashlib.file_digest(stream,'sha256').hexdigest()

def write(path,data):
    native(path.parent).mkdir(parents=True,exist_ok=True)
    native(path).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def records(path):
    return [dict(path=str(f),bytes=native(f).stat().st_size) for f in (files(path) if native(path).is_dir() else [path])]

def build_plan():
    assert BB.exists() and ARCHIVE.exists()
    assert not (AUDIT/'完成.json').exists()
    mapping={}
    for category,names in GROUPS.items():
        for name in names:
            assert (BB/name).is_dir(),name
            mapping[name]=f'{category}/{name}'
    mapping['camera-source']='04-导出工具/camera-source'
    deleted=[]
    for path in sorted(BB.iterdir()):
        name=path.name
        if path.is_dir():
            if name not in mapping:
                deleted.append(path)
        elif path.suffix in ('.py','.js'):
            mapping[name]=f'04-导出工具/{name}' if name in ACTIVE_TOOLS else f'04-导出工具/旧版脚本/{name}'
        elif path.suffix=='.zip':
            keep=path.stem in mapping or name=='农场物语-橙子果酒-动画工程-v14.zip'
            if keep:mapping[name]='05-打包文件/'+name
            else:deleted.append(path)
        elif re.fullmatch(r'使用说明-v\d+\.txt',name):
            if int(re.search(r'v(\d+)',name).group(1)) in KEEP_INSTRUCTIONS:
                mapping[name]='06-使用说明/'+name
            else:deleted.append(path)
        else:
            deleted.append(path)
    # Preserve the current release and immediately preceding ornament/aged regression evidence.
    keep_shots=set()
    for version in ('1.8.57','1.8.58','1.8.59'):
        report=json.loads((ROOT/f'verification-{version}.json').read_text(encoding='utf-8-sig'))
        keep_shots.update(Path(s).name for s in report.get('captures',[]))
    for path in (ROOT/'run-smoke/screenshots').iterdir():
        if path.is_file() and path.name not in keep_shots:
            deleted.append(path)
    # Only the three known archived payloads are recursive deletion targets.
    archive_targets=[ARCHIVE/name for name in ('blockbench','run-smoke','preview')]
    assert all(p.is_dir() for p in archive_targets)
    for path in deleted:check(path,ROOT)
    for path in archive_targets:check(path,ARCHIVE)
    all_records=[]
    for path in deleted+archive_targets:all_records.extend(records(path))
    protected={}
    for folder in ('references','src/main','src/test','src/smoke'):
        for path in files(ROOT/folder):protected[path.relative_to(ROOT).as_posix()]=digest(path)
    installed=next((ROOT.parents[2]/'mods').glob('*farmingtales_drink_anim*.jar'))
    data=dict(created=datetime.now().isoformat(),project=str(ROOT),archive=str(ARCHIVE),
              moves=mapping,delete=[str(p) for p in deleted+archive_targets],
              delete_files=len(all_records),delete_bytes=sum(r['bytes'] for r in all_records),
              preserved_screenshots=sorted(keep_shots),protected=protected,
              installed=str(installed),installed_sha256=digest(installed))
    write(AUDIT/'计划.json',data);write(AUDIT/'删除文件清单.json',all_records)
    print(json.dumps(dict(move_entries=len(mapping),delete_files=data['delete_files'],
                          GiB=round(data['delete_bytes']/2**30,2),keep_shots=len(keep_shots)),ensure_ascii=False),flush=True)

def apply():
    plan=json.loads((AUDIT/'计划.json').read_text(encoding='utf-8'))
    assert Path(plan['project'])==ROOT and Path(plan['archive'])==ARCHIVE
    assert not (AUDIT/'完成.json').exists()
    mapping=plan['moves'];moved_hashes={}
    # Record retained models and textures before changing directory names.
    for old,new in mapping.items():
        path=check(BB/old,BB);target=check(BB/new,BB)
        assert native(path).exists() and not native(target).exists(),old
        for f in files(path) if path.is_dir() else [path]:
            if f.suffix in ('.bbmodel','.png','.ogg','.mp4','.zip','.js'):
                relative=Path(new)/(f.relative_to(path) if path.is_dir() else Path())
                moved_hashes[relative.as_posix()]=digest(f)
    for old,new in mapping.items():
        path=check(BB/old,BB);target=check(BB/new,BB)
        native(target.parent).mkdir(parents=True,exist_ok=True)
        native(path).rename(native(target))
    print(f'Moved {len(mapping)} retained entries into six categories.',flush=True)
    # Adjust tool roots and literal path references to retained material.
    # Historical source scripts remain as small references; their deleted old outputs are not restored.
    def remap(text):
        for old,new in sorted(mapping.items(),key=lambda pair:-len(pair[0])):
            text=re.sub(r'(?<=[/\x27\x22])'+re.escape(old)+r'(?=[/\x27\x22])',lambda m:new,text)
        return text
    edited=[]
    scripts=list((BB/'04-导出工具').rglob('*.py'))
    for parent in (ROOT,ROOT/'preview'):
        scripts.extend(path for path in parent.glob('*.py') if path.name not in {
            'finish_blockbench_cleanup.py','build_material_catalog.py','verify_material_library.py',
            'organize_material_library.py','restore_material_archive.py'})
    moved_by_new={new:old for old,new in mapping.items()}
    for path in scripts:
        before=path.read_text(encoding='utf-8');text=before
        if path.is_relative_to(BB/'04-导出工具'):
            depth=2 if path.parent.name=='旧版脚本' else 1
            text=re.sub(r'Path\(__file__\)\.(resolve|absolute)\(\)\.(parent\b|parents\[1\])',
                        lambda m:f'Path(__file__).{m.group(1)}().parents[{depth+(m.group(2)!="parent")}]',text)
        if path.name in ('export_drinks_v54.py','export_jam_v54.py'):
            # v54 already contains exactly the unchanged v53 tracks used as this export's basis.
            family='酒水扩展' if path.name=='export_drinks_v54.py' else '上古水果果酱'
            text=text.replace(f"ROOT/f'{family}-v53/{{item}}-{{label}}-v53.bbmodel'",
                              f"ROOT/f'{family}-v54/{{item}}-{{label}}-v54.bbmodel'")
        text=remap(text)
        if path.name in ACTIVE_TOOLS:
            text=text.replace("OUT.with_suffix('.zip')", "OUT.parents[1]/'05-打包文件'/(OUT.name+'.zip')")
            text=text.replace("folder.with_suffix('.zip')", "ROOT/'05-打包文件'/(folder.name+'.zip')")
        if text!=before:
            ast.parse(text)
            backup=AUDIT/'脚本修改前'/path.relative_to(ROOT)
            backup.parent.mkdir(parents=True,exist_ok=True);backup.write_text(before,encoding='utf-8')
            path.write_text(text,encoding='utf-8');edited.append(path.relative_to(ROOT).as_posix())
    write(AUDIT/'移动素材校验.json',moved_hashes)
    write(AUDIT/'脚本路径更新.json',edited)
    for relative,expected in moved_hashes.items():
        assert digest(BB/relative)==expected,relative
    for relative,expected in plan['protected'].items():
        assert digest(ROOT/relative)==expected,relative
    # Preserve only the small audit record of reference-index edits from the first organization.
    before=check(ARCHIVE/'index-before',ARCHIVE)
    destination=check(LIB/'整理记录/首次索引修改前',LIB)
    assert before.is_dir() and not destination.exists()
    destination.parent.mkdir(parents=True,exist_ok=True);before.rename(destination)
    # Every delete target is resolved, checked against its intended root, then deleted in Python only.
    removed_files=0;removed_bytes=0
    for i,name in enumerate(plan['delete']):
        path=Path(name)
        bound=ARCHIVE if path.is_relative_to(ARCHIVE) else ROOT
        path=check(path,bound)
        current=records(path)
        removed_files+=len(current);removed_bytes+=sum(r['bytes'] for r in current)
        if native(path).is_dir():shutil.rmtree(native(path))
        else:native(path).unlink()
        if i%5000==0:print(f'Deleted {i+1}/{len(plan["delete"])} targets',flush=True)
    assert removed_files==plan['delete_files'] and removed_bytes==plan['delete_bytes']
    # The payload is gone; remove the obsolete restore tool and retain only existing audit JSONs in LIB.
    allowed={'整理计划.json','归档文件清单.json','restore_material_archive.py'}
    assert {p.name for p in ARCHIVE.iterdir()}==allowed
    for path in ARCHIVE.iterdir():check(path,ARCHIVE).unlink()
    check(ARCHIVE,ROOT.parents[1]/'project-archives').rmdir()
    for name in ('restore_material_archive.py',):
        path=check(ROOT/name,ROOT)
        if path.exists():path.unlink()
    # Preserve first-pass records as historical documents instead of leaving them as current status.
    history=LIB/'整理记录/首次归档';history.mkdir(parents=True,exist_ok=True)
    for name in ('整理计划.json','整理结果.json','归档文件清单.json','完整性验证.json'):
        source=check(LIB/name,LIB)
        if source.exists():source.rename(history/name)
    for name in ('organize_material_library.py','build_material_catalog.py'):
        source=ROOT/name
        if source.exists():source.rename(history/name)
    report=dict(completed=datetime.now().isoformat(),deleted_files=removed_files,deleted_bytes=removed_bytes,
                deleted_GiB=round(removed_bytes/2**30,2),retained_reference_videos=27,
                moved_entries=len(mapping),updated_scripts=len(edited),
                archive_deleted=not ARCHIVE.exists(),installed_jar_unchanged=digest(Path(plan['installed']))==plan['installed_sha256'])
    write(AUDIT/'完成.json',report);write(LIB/'整理结果.json',report)
    print(json.dumps(report,ensure_ascii=False),flush=True)

if __name__=='__main__':
    build_plan() if sys.argv[1]=='plan' else apply()
