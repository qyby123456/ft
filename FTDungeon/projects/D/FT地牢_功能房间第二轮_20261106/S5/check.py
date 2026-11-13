import hashlib
import importlib.util
import json
import subprocess
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OLD = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
SPEC = importlib.util.spec_from_file_location('audit_reader', OLD / 'audit.py')
READER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(READER)

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def run(identifier):
    folder = ROOT / identifier
    qa = folder / 'qa'
    qa.mkdir(exist_ok=True)
    if (qa/'self-check.json').exists():
        previous = json.loads((qa/'self-check.json').read_text(encoding='utf-8'))
        save(qa/('history-'+previous['sha256']+'.json'),previous)
    candidate = folder / (identifier.lower() + '.nbt')
    sha256 = hashlib.sha256(candidate.read_bytes()).hexdigest()
    design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    template = READER.decode(candidate)
    blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
    inventory = Counter(state['Name'] for state in blocks.values())
    entities = [entry for entry in template['blocks'] if 'nbt' in entry]
    assert len(blocks) == len(template['blocks'])
    assert template['DataVersion'] == 3465
    assert all(not entry['nbt'].get('Items', []) and not entry['nbt'].get('LootTable') for entry in entities)
    ports = design['ports']
    for port in ports:
        east, up, south = port['feet']
        for offset in range(-(port['width']//2), port['width']//2 + 1):
            horizontal = (east, south+offset) if port['facing'] in ['east','west'] else (east+offset,south)
            assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in ['minecraft:air','minecraft:water']
            assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(port['height']))
    water_openings = []
    for (east, up, south), state in blocks.items():
        if state['Name'] != 'minecraft:water' and state.get('Properties',{}).get('waterlogged') != 'true':
            continue
        for delta in [(1,0,0),(-1,0,0),(0,-1,0),(0,0,1),(0,0,-1)]:
            neighbor = tuple(value+change for value,change in zip((east,up,south),delta))
            if blocks.get(neighbor,{'Name':'minecraft:air'})['Name'] == 'minecraft:air':
                water_openings.append({'water':[east,up,south],'air':neighbor})
    save(qa/'inventory.json', {'sha256':sha256,'size':template['size'],'block_counts':dict(inventory),'block_entities':entities,'empty_inventories':True,'fluid_adjacent_air_excluding_up':water_openings})
    targets = [{'label':port['name'],'position':port['feet']} for port in ports[1:]] + design['targets']
    specification = [{'group':5,'candidate':str(candidate),'sha256':sha256,'swimming':False,'start':ports[0]['feet'] if ports else design['start'],'targets':targets}]
    save(qa/'route-input.json', specification)
    (qa/'vanilla-block-tags.json').write_bytes(Path('D:/ft工程文件/S8_紫晶洞穴_连接地牢_20261106/qa/vanilla-block-tags.json').read_bytes())
    classpath = Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip()
    classpath = str(OLD/'classes') + ';' + ';'.join(classpath.splitlines())
    java = Path('C:/Program Files/Java/jdk-25.0.2/bin/java.exe')
    invocations = []
    for checker in ['IndependentNativeAudit','IndependentRouteAudit']:
        command = [str(java),'-Xmx1536m','-cp',classpath,checker,str(qa)]
        if checker == 'IndependentNativeAudit':
            command.append('frozen')
        result = subprocess.run(command, cwd=qa, capture_output=True, timeout=240)
        (qa/(checker+'.log')).write_bytes(result.stdout+result.stderr)
        invocations.append({'checker':checker,'returncode':result.returncode,'max_heap':'1536m','cwd':str(qa)})
        assert result.returncode == 0, (checker,result.stderr[-1000:])
    native = json.loads((qa/'independent-native-frozen.json').read_text())[0]
    routes = json.loads((qa/'independent-routes.json').read_text())[0]
    passed = native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported'] and routes['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in routes['targets']) and not water_openings
    report = {'sha256':sha256,'self_check_passed':passed,'independent_acceptance':False,'native':native,'routes':routes,'invocations':invocations,'fluid_open_faces_count':len(water_openings),'not_tested':['world/fluid/random ticks','actual survival gameplay','oxygen depletion simulation','external ocean placement','container interaction','ladder climbing physics']}
    save(qa/'self-check.json',report)
    print(json.dumps({'id':identifier,'sha256':sha256,'passed':passed,'support_failures':native['survival_failures'],'route_failures':[target for target in routes['targets'] if not target['reachable'] or not target['standing_clear']],'fluid_open_faces':len(water_openings)},ensure_ascii=False))
    progress_path = ROOT/'progress.json'
    progress = json.loads(progress_path.read_text(encoding='utf-8')) if progress_path.exists() else {'group':'S5','completed':[],'current':identifier,'merged':False,'independent_acceptance':False}
    progress['current']=identifier
    if passed and identifier not in progress['completed']:
        progress['completed'].append(identifier)
    save(progress_path,progress)

if __name__ == '__main__':
    run(sys.argv[1])
