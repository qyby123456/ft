import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
from collections import Counter

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
OLD = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, plain


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    folder = ROOT / sys.argv[1]
    design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    candidate = folder / design['nbt']
    structure = Structure.read(candidate)
    sha256 = digest(candidate)
    qa = folder / 'qa'
    qa.mkdir(exist_ok=True)
    inventory = Counter(block.name for block in structure.blocks.values())
    payloads = [{'position': position, 'payload': plain(payload)} for position, payload in structure.block_entities.items()]
    hazards = {name: count for name, count in inventory.items() if name in ['minecraft:lava','minecraft:fire','minecraft:magma_block','minecraft:cactus','minecraft:campfire']}
    nonempty = [entry for entry in payloads if entry['payload'].get('Items') or entry['payload'].get('LootTable')]
    assert structure.size == tuple(design['size'])
    assert structure.data_version == 3465
    assert not hazards and not nonempty
    for port in design['ports']:
        east, up, south = port['feet']
        for offset in range(-(port['width']//2), port['width']//2+1):
            for rise in range(port['clearHeight']):
                position = (east+offset, up+rise, south) if port['side'] in ('north','south') else (east,up+rise,south+offset)
                assert structure.get(position).name == 'minecraft:air', position
    write(qa / 'inventory.json', {'source_sha256': sha256, 'size': structure.size, 'blocks': len(structure.blocks), 'palette_inventory': inventory, 'block_entities': payloads, 'entities': len(structure.entities), 'hazards': hazards, 'nonempty_containers': nonempty})
    specification = [{'group': 2, 'candidate': str(candidate), 'sha256': sha256, 'start': design['ports'][0]['feet'], 'swimming': False, 'targets': design['targets']}]
    write(qa / 'route-input.json', specification)
    shutil.copyfile(OLD / 'vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(OLD / 'classes'), str(mapped), *dependencies])
    (qa / 'classpath.txt').write_text(classpath, encoding='utf-8')
    classes = ROOT / 'qa/classes'
    classes.mkdir(parents=True, exist_ok=True)
    compile_command = ['C:/Program Files/Common Files/Oracle/Java/javapath/javac.exe', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(ROOT / 'SupportAudit.java')]
    compiled = subprocess.run(compile_command, cwd=qa, capture_output=True, text=True, encoding='utf-8', errors='replace', creationflags=subprocess.CREATE_NO_WINDOW)
    (qa / 'compile-support.log').write_text(compiled.stdout+compiled.stderr, encoding='utf-8')
    assert compiled.returncode == 0
    classpath = str(classes)+os.pathsep+classpath
    commands = [{'argv': compile_command, 'cwd': str(qa), 'exit_code': compiled.returncode}]
    for label, arguments in [('native', ['IndependentNativeAudit', str(qa), 'frozen']), ('route', ['IndependentRouteAudit', str(qa)]), ('support', ['SupportAudit', str(qa)])]:
        command = ['C:/Program Files/Common Files/Oracle/Java/javapath/java.exe', '-Xmx1536M', '-Djava.awt.headless=true', '-cp', classpath, *arguments]
        result = subprocess.run(command, cwd=qa, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=300, creationflags=subprocess.CREATE_NO_WINDOW)
        (qa / (label+'.log')).write_text(result.stdout+result.stderr, encoding='utf-8')
        commands.append({'argv': command, 'cwd': str(qa), 'exit_code': result.returncode})
        write(qa / 'commands.json', commands)
        assert result.returncode == 0, (qa / (label+'.log'))
    native = json.loads((qa / 'independent-native-frozen.json').read_text())[0]
    route = json.loads((qa / 'independent-routes.json').read_text())[0]
    support_result = json.loads((qa / 'support-all.json').read_text())
    assert native['registry_properties'] == 'PASS' and native['template_roundtrip_block_count'] == 'PASS'
    assert not native['survival_failures'] and not native['survival_unsupported'], native
    assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets']), route
    assert not support_result['failures'] and not support_result['unsupported'], support_result
    assert digest(candidate) == sha256
    write(qa / 'self-check.json', {'source_sha256': sha256, 'status': 'SELF_CHECK_PASS_NOT_INDEPENDENT_ACCEPTANCE', 'native_registry_properties': 'PASS', 'native_support_selected_fragile_blocks': 'PASS', 'native_collision_route': 'PASS', 'mapped_jar_sha256': digest(mapped), 'classpath_source_sha256': digest(support), 'check_class_sha256': {name: digest(OLD / 'classes' / (name+'.class')) for name in ('IndependentNativeAudit','IndependentRouteAudit')}, 'not_run': ['world placement', 'neighbor/random/fluid ticks', 'runtime lighting', 'combat/gameplay', 'independent human acceptance'], 'support_scope': 'Native canSurvive checker covers selected fragile types; other support reviewed separately from recipe and actual preview.'})
    print(design['id'], sha256, 'SELF_CHECK_PASS', len(route['targets']))


if __name__ == '__main__':
    main()
