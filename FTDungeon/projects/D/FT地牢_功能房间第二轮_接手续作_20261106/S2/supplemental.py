import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, State


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    folder = ROOT / '12-prison-block'
    design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    source = folder / design['nbt']
    structure = Structure.read(source)
    changes = []
    for position, block in list(structure.blocks.items()):
        if block.name == 'minecraft:iron_door':
            properties = dict(block.properties)
            properties['open'] = 'true'
            properties['powered'] = 'true'
            structure.set(position, State(block.name, tuple(sorted(properties.items()))))
            changes.append(list(position))
    output = ROOT / 'qa/prison-open-door-routes'
    output.mkdir(parents=True, exist_ok=True)
    fixture = output / 'all-doors-open-fixture.nbt'
    structure.write(fixture)
    targets = list(design['targets'])
    for east in [5, 21]:
        for south in [4, 11, 18, 25]:
            targets.append({'label': f'cell-interior-{east}-{south}', 'position': [east, 2, south]})
    targets += [{'label': f'control-{east}-{south}', 'position': [east, 2, south]} for east in [10,16] for south in [3,10,17,24]]
    specification = [{'group': 2, 'candidate': str(fixture), 'sha256': digest(fixture), 'start': design['ports'][0]['feet'], 'swimming': False, 'targets': targets}]
    (output / 'route-input.json').write_text(json.dumps(specification, indent=2), encoding='utf-8')
    shutil.copyfile(folder / 'qa/vanilla-block-tags.json', output / 'vanilla-block-tags.json')
    command = ['C:/Program Files/Common Files/Oracle/Java/javapath/java.exe', '-Xmx1536M', '-cp', (folder / 'qa/classpath.txt').read_text(encoding='utf-8'), 'IndependentRouteAudit', str(output)]
    result = subprocess.run(command, cwd=output, capture_output=True)
    (output / 'route.log').write_bytes(result.stdout + result.stderr)
    assert result.returncode == 0
    report = json.loads((output / 'independent-routes.json').read_text(encoding='utf-8'))[0]
    assert report['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in report['targets'])
    (output / 'provenance.json').write_text(json.dumps({'original_source_sha256': digest(source), 'fixture_sha256': digest(fixture), 'changes_only': '16 lower/upper door states open=true powered=true in QA copy; source unchanged', 'door_state_positions': changes, 'targets': len(targets), 'status': 'PRODUCER_OFFLINE_COLLISION_PASS', 'scope': 'Eight cell interiors and eight controller approaches plus original five targets on an explicitly open-door state fixture. Combined with original eight Lever.use open/close evidence; no real-player travel or GUI certificate.'}, indent=2), encoding='utf-8')
    print('PRISON_OPEN_FIXTURE', len(targets), 'TARGETS_PASS')


if __name__ == '__main__':
    main()
