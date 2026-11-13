import collections
import hashlib
import json
import pathlib
import subprocess
import sys
import shutil

ROOT = pathlib.Path(__file__).parent
TOOLS = pathlib.Path(r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
sys.path.insert(0, str(TOOLS))
from nbtio import Structure

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def main():
    folder = ROOT / sys.argv[1]
    qa = folder / 'qa'
    qa.mkdir(exist_ok=True)
    design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    candidate = next(folder.glob('*.nbt'))
    sha = hashlib.sha256(candidate.read_bytes()).hexdigest()
    structure = Structure.read(candidate)
    counts = collections.Counter(block.name for block in structure.blocks.values())
    inventories = []
    for position, tag in structure.block_entities.items():
        inventory = tag.value.get('Items')
        assert inventory is None or len(inventory.value) == 0
        assert 'LootTable' not in tag.value
        inventories.append({'position': position, 'id': tag.value['id'].value, 'empty': True})
    for port in design['ports']:
        x, y, z = port['feet']
        for across in range(-(port['width']//2), port['width']//2+1):
            for rise in range(port['clearHeight']):
                pos = (x, y+rise, z+across) if port['side'] in ('west','east') else (x+across,y+rise,z)
                assert structure.get(pos).name == 'minecraft:air', ('blocked port', pos)
    hazards = {name: count for name, count in counts.items() if name in ('minecraft:lava','minecraft:fire','minecraft:magma_block','minecraft:campfire','minecraft:cactus')}
    assert not hazards
    save(folder / 'inventory.json', {'source_sha256':sha,'size':structure.size,'data_version':structure.data_version,'block_counts':dict(counts),'block_entities':inventories,'explicit_air':counts['minecraft:air'],'hazards':hazards,'water_blocks':counts['minecraft:water']})
    specification = {'group':int(design['id'].split('-')[1]),'candidate':str(candidate),'sha256':sha,'start':design['start'],'targets':design['targets'],'swimming':False}
    save(qa / 'route-input.json', [specification])
    save(qa / 'file-audit.json', {'groups':[specification]})
    dependencies = pathlib.Path(r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines()
    classpath = ';'.join([r'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\classes',r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar']+[entry for entry in dependencies if entry and 'minecraft_user_repo' not in entry])
    commands = []
    source_audit = pathlib.Path(r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106')
    shutil.copyfile(source_audit / 'S1-preliminary-20261106-151412-051525' / 'vanilla-block-tags.json',qa / 'vanilla-block-tags.json')
    classpath = str(source_audit / 'classes') + ';' + classpath
    for classname in ['IndependentNativeAudit','IndependentRouteAudit','IndependentAllSupportAudit']:
        command = [r'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe','-Xmx1536m','-Djava.awt.headless=true','-cp',classpath,classname,str(qa)]
        commands.append(command)
        result = subprocess.run(command,cwd=qa,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
        (qa / (classname+'.log')).write_bytes(result.stdout)
        if result.returncode:
            raise RuntimeError(classname+' failed: '+result.stdout.decode(errors='replace')[-1800:])
    save(qa / 'commands.json',commands)
    native = json.loads((qa / 'independent-native.json').read_text())[0]
    routes = json.loads((qa / 'independent-routes.json').read_text())[0]
    assert native['registry_properties'] == 'PASS', native
    assert native['template_roundtrip_block_count'] == 'PASS'
    assert not native['survival_failures'], native
    all_support = json.loads((qa / 'independent-all-support.json').read_text(encoding='utf-8'))[0]
    assert not all_support['failures'] and not all_support['unsupported'], all_support
    assert routes['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in routes['targets']), routes
    render = json.loads((folder / 'render.json').read_text())
    assert render['source_sha256'] == sha
    history_path = folder / 'candidate-history.json'
    history = json.loads(history_path.read_text(encoding='utf-8')) if history_path.exists() else []
    old_check = folder / 'self-check.json'
    if old_check.exists():
        old_sha = json.loads(old_check.read_text(encoding='utf-8'))['source_sha256']
        if old_sha != sha and old_sha not in [entry['sha256'] for entry in history]:
            history.append({'sha256':old_sha,'superseded_reason':'explicit renderer block-state defaults and natural root geometry correction'})
    save(history_path,history)
    save(folder / 'self-check.json', {'source_sha256':sha,'status':'maker_static_self_check_pass_not_independent_acceptance','registry_properties':True,'template_roundtrip':True,'native_fragile_survival':True,'ports_clear':True,'empty_inventories':True,'collision_routes':routes['targets'],'not_executed':['world placement and ticks','gameplay or combat','independent visual acceptance'],'scope_limits':['route shapes use EmptyBlockGetter; ladders and jumps not certified','native survival checks only selected fragile types, tags not loaded; no general support certification','no world lighting or neighbor/fluid/random tick simulation']})
    progress_path = ROOT / 'progress.json'
    progress = json.loads(progress_path.read_text(encoding='utf-8')) if progress_path.exists() else {'style':'S1','rooms':{},'merge':'not_started','independent_acceptance':False}
    progress['rooms'][design['id']] = {'candidate':str(candidate),'sha256':sha,'maker_static_self_check':'passed','visual_review':'pending','next':'inspect actual cutaway before next reference'}
    progress['current'] = design['id']
    save(progress_path,progress)
    print(design['id'],sha,'static self-check passed')

if __name__ == '__main__':
    main()
