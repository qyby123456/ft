import collections
import hashlib
import importlib.util
import json
import os
import shutil
from pathlib import Path
import subprocess
import sys

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[1]
REFERENCES = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S5-coral-ocean/out')
sys.path.insert(0, str(PROJECT / 'tools'))
import review_four_views as views
from nbtio import Structure


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def inspect(folder):
    metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
    structure = Structure.read(folder / 'room.nbt')
    width, height, depth = structure.size
    blocks = structure.blocks

    def name(position):
        return blocks[position].name.removeprefix('minecraft:') if position in blocks else 'air'

    def require(condition, message):
        if not condition:
            raise ValueError(metadata['id'] + ': ' + message)

    require(list(structure.size) == metadata['size'], 'dimensions')
    require(structure.data_version == 3465, 'DataVersion')
    require(not structure.entities and not structure.block_entities, 'unexpected entities')
    require(len(blocks) == width * height * depth, 'explicit volume includes air')
    require(all(0 <= position[0] < width and 0 <= position[1] < height and 0 <= position[2] < depth for position in blocks), 'bounds')
    require(all(name((blockX, 0, blockZ)) != 'air' for blockX in range(width) for blockZ in range(depth)), 'floor coverage')
    require(all(name((blockX, height - 1, blockZ)) != 'air' for blockX in range(width) for blockZ in range(depth)), 'roof coverage')
    dry = {(blockX, 1, blockZ) for blockX in range(width) for blockZ in range(depth) if name((blockX, 1, blockZ)) == 'air' and name((blockX, 2, blockZ)) == 'air' and name((blockX, 0, blockZ)) not in ('air', 'water')}
    entrance = tuple(metadata['entrance'])
    pending = collections.deque([entrance])
    visited = {entrance}
    while pending:
        current = pending.popleft()
        for offset in ((1,0,0),(-1,0,0),(0,0,1),(0,0,-1)):
            candidate = tuple(current[axis] + offset[axis] for axis in range(3))
            if candidate in dry and candidate not in visited:
                visited.add(candidate)
                pending.append(candidate)
    require(tuple(metadata['interior']) in visited, 'interior reachable')
    for port in metadata['ports']:
        require(tuple(port['position']) in visited, 'port unreachable ' + port['direction'])
        portX, portY, portZ = port['position']
        for offset in (-1, 0, 1):
            for blockY in range(portY, portY + 3):
                position = (portX + offset, blockY, portZ) if port['direction'] in ('north','south') else (portX, blockY, portZ + offset)
                require(name(position) == 'air', '3x3 port clearance ' + str(position))
    water_count = 0
    coral_count = 0
    for position, block in blocks.items():
        block_name = block.name.removeprefix('minecraft:')
        require(block.name.startswith('minecraft:'), 'non-vanilla namespace')
        require(block_name not in ('lava','fire','magma_block','spawner','command_block','structure_block','jigsaw'), 'forbidden block')
        if block_name == 'water':
            water_count += 1
            for offset in ((1,0,0),(-1,0,0),(0,0,1),(0,0,-1),(0,-1,0),(0,1,0)):
                neighbor = tuple(position[axis]+offset[axis] for axis in range(3))
                require(name(neighbor) not in ('air','cave_air','void_air'), 'unsealed water')
        if block_name.endswith('_coral_block') and not block_name.startswith('dead_'):
            coral_count += 1
            require(any(name(tuple(position[axis]+offset[axis] for axis in range(3))) == 'water' for offset in ((1,0,0),(-1,0,0),(0,0,1),(0,0,-1),(0,-1,0),(0,1,0))), 'coral lacks water')
    reference = next(REFERENCES.glob('S5-' + folder.name[-2:] + '-*.png'))
    metadata.update(sha256=digest(folder / 'room.nbt'), reference=str(reference), reference_sha256=digest(reference))
    write(folder / 'room.json', metadata)
    materials = collections.Counter(block.name for block in blocks.values() if block.name != 'minecraft:air')
    write(folder / 'materials.json', dict(sorted(materials.items())))
    report = dict(id=metadata['id'], status='PASS', sha256=metadata['sha256'], checks=['bounds','data_version','full_explicit_volume','solid_floor','complete_roof','three_by_three_ports','dry_path_to_every_port','sealed_water','wet_live_coral','no_entities_or_loot','vanilla_namespace'], water_blocks=water_count, coral_blocks=coral_count, dry_reachable_cells=len(visited), native_registry='PENDING', world_placement='NOT_RUN')
    write(folder / 'qa.json', report)
    return metadata, structure


def native_review(folders):
    observation = ROOT / 'qa/mezzanine-room'
    observation.mkdir(parents=True, exist_ok=True)
    courtyard = ROOT / 'rooms/NAT-S5-08'
    shutil.copy2(courtyard / 'room.nbt', observation / 'room.nbt')
    metadata = json.loads((courtyard / 'room.json').read_text(encoding='utf-8'))
    metadata['interior'] = [3, 4, 10]
    write(observation / 'room.json', metadata)
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(mapped), *dependencies])
    classes = ROOT / 'qa/classes'
    classes.mkdir(parents=True, exist_ok=True)
    commands = [
        [str(java / 'javac.exe'), '-encoding','UTF-8','-classpath',classpath,'-d',str(classes),str(PROJECT / 'tools/VerifyDeliveries.java'),str(ROOT / 'NativeS5.java')],
        [str(java / 'java.exe'),'-Djava.awt.headless=true','-classpath',str(classes)+os.pathsep+classpath,'NativeS5', *[str(folder) for folder in folders],str(observation)],
    ]
    for index, command in enumerate(commands):
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=150, creationflags=subprocess.CREATE_NO_WINDOW, cwd=ROOT)
        (ROOT / f'qa/native-{index}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(result.stdout[-2000:] + result.stderr[-2000:])


def main():
    folders = sorted((ROOT / 'rooms').glob('NAT-S5-*'))
    records = [inspect(folder) for folder in folders]
    native_review(folders)
    for folder, (metadata, structure) in zip(folders, records):
        qa = json.loads((folder / 'qa.json').read_text(encoding='utf-8'))
        qa['native_registry'] = 'PASS'
        qa['native_scope'] = 'Vanilla 1.20.1 registry, state validation, actual collision shapes, StructureTemplate roundtrip; not Forge world placement.'
        write(folder / 'qa.json', qa)
        for direction, title, camera in views.VIEWS:
            views.render(structure, metadata, camera, title, False, size=(900,700)).save(folder / f'interior-{direction}.png')
        views.render(structure, metadata, (-0.7,-1), '完整外壳', True, size=(900,700)).save(folder / 'exterior.png')
        Image.open(folder / 'interior-north.png').save(folder / 'preview.png')
        print('VERIFIED AND RENDERED ' + metadata['id'], flush=True)
    sheet = Image.new('RGB',(1800,1400),'#101a20')
    for index, folder in enumerate(folders):
        thumbnail = Image.open(folder / 'preview.png').resize((600,466))
        sheet.paste(thumbnail,((index%3)*600,(index//3)*466))
    sheet.save(ROOT / 'overview.png')
    write(ROOT / 'manifest.json',dict(group='S5-coral-ocean',thread='01a1a5df-a60b-7f02-877f-856a3b8f501e',status='STATIC_AND_NATIVE_PASS_WORLD_PLACEMENT_PENDING',rooms=[metadata for metadata, _ in records],layout_reference=str(REFERENCES / 'S5-10-overview.png'),notes='No image API invoked; source images untouched. Not deployed. Explicit air requires full-volume occupancy refusal before placement.'))


if __name__ == '__main__':
    main()
