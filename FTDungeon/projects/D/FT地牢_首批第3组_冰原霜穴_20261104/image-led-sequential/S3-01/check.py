import collections
import hashlib
import importlib.util
import json
import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, str(Path.home() / 'AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure

def save(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def main():
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-01-large-chamber.png')
    digest = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    metadata = dict(id='S3-01-image-led-r1', name='冰晶环庭主厅', size=[23,14,23], entrance=[11,1,0], interior=[11,1,2], facing='north',
        connections=[dict(facing=facing, position=position, width=3, height=4) for facing, position in [('north',[11,1,0]),('west',[0,1,12]),('east',[22,1,12])]],
        reference=str(reference), reference_sha256=hashlib.sha256(reference.read_bytes()).hexdigest(), sha256=digest,
        geometry_authority='Single reference PNG; interpretive discretization, not measured block counts', runtime_slot=None,
        safety='Dense explicit air; refuse whole-volume occupancy before placement. No automatic clearing.',
        old_batch_status='Previous nine batch candidates are historical; not accepted as sequential-image deliveries.')
    save(ROOT / 'room.json', metadata)
    structure = Structure.read(ROOT / 'room.nbt')
    assert structure.size == (23,14,23) and structure.data_version == 3465
    assert len(structure.blocks) == 23 * 14 * 23
    assert not structure.block_entities and not structure.entities
    counts = collections.Counter(block.name for block in structure.blocks.values())
    assert all(identifier.startswith('minecraft:') for identifier in counts)
    assert not any(identifier in counts for identifier in ['minecraft:water','minecraft:lava','minecraft:spawner','minecraft:command_block','minecraft:jigsaw'])
    def clear(position):
        across, elevation, along = position
        return structure.get(position).name == 'minecraft:air' and structure.get((across,elevation+1,along)).name == 'minecraft:air' and structure.get((across,elevation-1,along)).name in {'minecraft:stone_bricks','minecraft:polished_andesite','minecraft:blue_ice','minecraft:snow_block'}
    origin = tuple(metadata['entrance'])
    distance = {origin: 0}
    pending = collections.deque([origin])
    while pending:
        across, elevation, along = pending.popleft()
        for offsetAcross, offsetAlong in [(1,0),(-1,0),(0,1),(0,-1)]:
            neighbor = (across+offsetAcross,elevation,along+offsetAlong)
            if neighbor in distance or not (0 <= neighbor[0] < 23 and 0 <= neighbor[2] < 23) or not clear(neighbor):
                continue
            distance[neighbor] = distance[(across,elevation,along)] + 1
            pending.append(neighbor)
    for connection in metadata['connections']:
        across, elevation, along = connection['position']
        for offset in [-1,0,1]:
            feet = (across + (offset if connection['facing'] == 'north' else 0),elevation,along + (0 if connection['facing'] == 'north' else offset))
            assert feet in distance
            assert all(structure.get((feet[0],height,feet[2])).name == 'minecraft:air' for height in range(1,5))
    for across in range(23):
        for along in range(23):
            ceiling = 10 + min(2, across, along,22-across,22-along)
            assert structure.get((across,ceiling,along)).name == 'minecraft:stone_bricks'
    cutaway = Structure(structure.size, data_version=3465)
    for position, block in structure.blocks.items():
        across,elevation,along = position
        ceiling = 10 + min(2, across,along,22-across,22-along)
        if elevation >= ceiling or (elevation >= 1 and (across == 0 or along == 22)):
            continue
        cutaway.set(position, block)
    cutaway.write(ROOT / 'preview-only-cutaway.nbt')
    save(ROOT / 'blocks.json', dict(counts.most_common()))
    classes = ROOT / 'qa/classes'
    classes.mkdir(parents=True, exist_ok=True)
    mapped = 'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar'
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(classes),mapped,*dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    for command, log in [([str(java/'javac.exe'),'-encoding','UTF-8','-cp',classpath,'-d',str(classes),str(SHARED/'VerifyDeliveries.java'),str(ROOT/'SingleNative.java')],'compile'),
                         ([str(java/'java.exe'),'-Djava.awt.headless=true','-cp',classpath,'SingleNative',str(ROOT)],'run')]:
        result = subprocess.run(command,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=180,creationflags=subprocess.CREATE_NO_WINDOW)
        (ROOT / ('qa/native-'+log+'.log')).write_text(result.stdout+result.stderr,encoding='utf-8')
        if result.returncode:
            raise RuntimeError(result.stderr[-2000:])
    specification = importlib.util.spec_from_file_location('four_views',SHARED/'review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    for exterior in [False,True]:
        for direction,title,camera in renderer.VIEWS:
            image = renderer.render(structure,metadata,camera,title,exterior,size=(1400,1080))
            image.save(ROOT / (('exterior-' if exterior else 'interior-')+direction+'.png'))
    save(ROOT/'qa.json',dict(status='PASS_STATIC_AND_NATIVE_TEMPLATE',sha256=digest,reference_sha256=metadata['reference_sha256'],
        scope='This single S3-01 only',reachable_positions=len(distance),all_connectors_reachable=True,
        connector_steps={connection['facing']:distance[tuple(connection['position'])] for connection in metadata['connections']},
        roof='Complete stepped vault checked at all 529 columns',native_registry_collision_template='PASS',lodestone_roundtrip='PASS bundled run-build',
        native_world_placement='NOT_RUN',ticks='NOT_RUN',next_image='NOT_STARTED'))
    print('PASS S3-01 only: all three ports reachable, roof intact, native registry/collision/template roundtrip; eight views rendered')

if __name__ == '__main__':
    main()
