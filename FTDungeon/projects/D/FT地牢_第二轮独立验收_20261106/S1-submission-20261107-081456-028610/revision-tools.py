import hashlib
import json
import pathlib
import subprocess
import sys

ROOT=pathlib.Path(__file__).parent

def save(path,value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def reproduce_merge():
    output=ROOT/'merge-reproduction'
    subprocess.run([sys.executable,str(ROOT/'merge.py'),str(output)],check=True)
    original=ROOT/'merged'/'s1-mossy-functional-11-20.nbt'
    rebuilt=output/original.name
    assert original.read_bytes()==rebuilt.read_bytes()
    chunks=[]
    for path in sorted((ROOT/'merged'/'chunks').glob('*.nbt')):
        other=output/'chunks'/path.name
        assert path.read_bytes()==other.read_bytes()
        chunks.append({'file':path.name,'sha256':digest(path),'byte_identical':True})
    save(ROOT/'merge-reproduction-report.json',{'candidate_sha256':digest(original),'rebuilt_sha256':digest(rebuilt),'byte_identical':True,'chunks':chunks,'recipe_sha256':digest(ROOT/'merge.py'),'scenery_recipe_sha256':digest(ROOT/'scenery.mjs'),'scenery_sha256':digest(ROOT/'scenery'/'s1-19-forest-depth.nbt'),'command':[sys.executable,str(ROOT/'merge.py'),str(output)]})

def manifest():
    excluded={'revision-history','reproduction','merge-reproduction','.gradle','build','server'}
    records=[]
    for path in sorted(ROOT.rglob('*')):
        if not path.is_file() or any(part in excluded for part in path.relative_to(ROOT).parts) or path.name=='delivery-file-manifest.json':
            continue
        records.append({'path':str(path),'relative':str(path.relative_to(ROOT)),'bytes':path.stat().st_size,'sha256':digest(path)})
    references=json.loads((ROOT/'references.json').read_text(encoding='utf-8'))['references']
    external=[]
    for reference in references:
        for key in ['image_path','prompt_path']:
            path=pathlib.Path(reference[key]);external.append({'room':reference['id'],'role':key,'path':str(path),'bytes':path.stat().st_size,'sha256':digest(path)})
    tool_paths=[pathlib.Path(r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools\nbtio.py'),pathlib.Path(r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs'),pathlib.Path(r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\preview.mjs'),pathlib.Path(r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\classes\IndependentAllSupportAudit.class'),pathlib.Path(r'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\classes\IndependentNativeAudit.class'),pathlib.Path(r'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\classes\IndependentRouteAudit.class'),pathlib.Path(r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar')]
    tools=[{'path':str(path),'bytes':path.stat().st_size,'sha256':digest(path)} for path in tool_paths]
    save(ROOT/'delivery-file-manifest.json',{'files':records,'external_references':external,'external_tools':tools,'excluded_generated_or_history_directories':sorted(excluded),'contract':'All delivered source, recipes, designs, previews, tools, QA reports, world evidence NBT, chunks and reference hashes. This manifest excludes itself to avoid recursive hashing.'})

if sys.argv[1]=='reproduce':
    reproduce_merge()
else:
    manifest()
