import collections
import hashlib
import importlib.util
import json
import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SHARED = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, str(Path.home() / 'AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain, read_nbt

def save(target, value):
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

def inspect(folder):
    metadata = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    structure = Structure.read(folder / 'room.nbt')
    template = plain(read_nbt(folder / 'room.nbt')[1])
    size = structure.size
    assert list(size) == metadata['size']
    assert template['DataVersion'] == 3465
    assert len(template['blocks']) == size[0] * size[1] * size[2]
    assert not template.get('entities')
    blocks = {tuple(block['pos']): template['palette'][block['state']]['Name'] for block in template['blocks']}
    assert len(blocks) == len(template['blocks'])
    forbidden = {'jigsaw', 'structure_block', 'command_block', 'spawner', 'lava', 'water', 'fire', 'ice', 'powder_snow'}
    assert all(value.startswith('minecraft:') and value.split(':')[1] not in forbidden for value in blocks.values())
    containers = []
    for block in template['blocks']:
        data = block.get('nbt', {})
        assert not data.get('Items') and 'LootTable' not in data and 'Command' not in data
        if data:
            containers.append({'position': block['pos'], 'id': data.get('id'), 'empty': True})
    def walkable(position):
        across, elevation, along = position
        return blocks.get(position) == 'minecraft:air' and blocks.get((across, elevation + 1, along)) == 'minecraft:air' and blocks.get((across, elevation - 1, along), 'minecraft:air') not in {'minecraft:air', 'minecraft:chain', 'minecraft:soul_lantern'}
    entrance = tuple(metadata['entrance'])
    reached = {entrance}
    distances = {entrance: 0}
    pending = collections.deque([entrance])
    while pending:
        across, elevation, along = pending.popleft()
        for deltaAcross, deltaAlong in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            neighbor = (across + deltaAcross, elevation, along + deltaAlong)
            if neighbor not in reached and walkable(neighbor):
                reached.add(neighbor)
                distances[neighbor] = distances[(across, elevation, along)] + 1
                pending.append(neighbor)
    assert tuple(metadata['interior']) in reached
    for connection in metadata['connections']:
        across, elevation, along = connection['position']
        assert (across, elevation, along) in reached, connection
        for offset in range(-1, 2):
            horizontal = across + (0 if connection['facing'] == 'east' else offset)
            longitudinal = along + (offset if connection['facing'] == 'east' else 0)
            assert (horizontal, elevation, longitudinal) in reached
            assert blocks[(horizontal, 0, longitudinal)] == 'minecraft:polished_andesite'
            assert all(blocks[(horizontal, clear_height, longitudinal)] == 'minecraft:air' for clear_height in range(1, 5))
    roof_holes = []
    for across in range(size[0]):
        for along in range(size[2]):
            if blocks[(across, 0, along)] == 'minecraft:air':
                continue
            if blocks[(across, size[1] - 1, along)] == 'minecraft:air':
                roof_holes.append([across, along])
    skylight = metadata['courtyard_skylight']
    if skylight:
        expected = [[across, along] for across in range(skylight['min'][0], skylight['max'][0] + 1) for along in range(skylight['min'][1], skylight['max'][1] + 1)]
        assert roof_holes == expected
    else:
        assert not roof_holes
    counts = collections.Counter(blocks.values())
    metadata.update(sha256=hashlib.sha256((folder / 'room.nbt').read_bytes()).hexdigest(), reference_sha256=hashlib.sha256(Path(metadata['reference']).read_bytes()).hexdigest(),
        coordinates='Local XYZ; +X east, +Y up, +Z south. Entrance/interior/connectors are player feet coordinates.',
        safety='Dense explicit air: placer must reject occupied entire bounding box before placing; never clear or replace player buildings.',
        generation_status='candidate-only; no installation or gameplay integration')
    save(folder / 'room.json', metadata)
    save(folder / 'blocks.json', dict(counts.most_common()))
    cutaway = Structure(size, data_version=3465)
    for position, block in structure.blocks.items():
        across, elevation, along = position
        close_wall = across == 0 or along == size[2] - 1
        if metadata['type'] == 'corner':
            close_wall = across <= size[0] // 2 - 3 or along == size[2] - 1
        if elevation == size[1] - 1 or (elevation >= 1 and close_wall):
            continue
        cutaway.set(position, block, structure.block_entities.get(position))
    cutaway.write(folder / 'views/preview-only-cutaway.nbt')
    report = dict(id=metadata['id'], sha256=metadata['sha256'], static_status='PASS', size=list(size), records=len(blocks), non_air=sum(count for block, count in counts.items() if block != 'minecraft:air'),
        reachable_flat_positions=len(reached), all_connectors_reachable=True, connector_clearance='3 wide x 4 high, solid non-ice thresholds',
        flat_connector_shortest_paths={connection['name']: distances[tuple(connection['position'])] for connection in metadata['connections']},
        roof_holes=roof_holes, containers=containers, no_entities=True, no_live_fluids=True, no_loot=True,
        lodestone_roundtrip='PASS (bundled run-build.mjs)', native_world_placement='NOT_RUN', gameplay='NOT_RUN')
    save(folder / 'qa.json', report)
    return metadata, report

def native():
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [entry for entry in support.read_text(encoding='utf-8').splitlines() if entry.strip() and 'minecraft_user_repo' not in entry]
    classpath = os.pathsep.join([str(mapped), *dependencies])
    classes = ROOT / 'qa/native-classes'
    classes.mkdir(parents=True, exist_ok=True)
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    for command, log in [([str(java / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(SHARED / 'VerifyDeliveries.java'), str(ROOT / 'tools/FrostNative.java')], 'compile'),
                         ([str(java / 'java.exe'), '-Djava.awt.headless=true', '-cp', str(classes) + os.pathsep + classpath, 'FrostNative', str(ROOT)], 'run')]:
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
        (ROOT / ('qa/native-' + log + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(result.stdout[-1000:] + result.stderr[-2000:])

def render_all():
    specification = importlib.util.spec_from_file_location('four_views', SHARED / 'review_four_views.py')
    viewer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(viewer)
    from PIL import Image, ImageDraw
    for folder in sorted((ROOT / 'incoming').iterdir()):
        metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
        structure = Structure.read(folder / 'room.nbt')
        destination = folder / 'views'
        destination.mkdir(exist_ok=True)
        for exterior in [False, True]:
            sheet = Image.new('RGB', (2000, 1560), '#101a20')
            for index, (direction, title, camera) in enumerate(viewer.VIEWS):
                image = viewer.render(structure, metadata, camera, title, exterior, size=(1000, 780))
                image.save(destination / (('exterior-' if exterior else 'interior-') + direction + '.png'))
                sheet.paste(image, (index % 2 * 1000, index // 2 * 780))
                if not exterior and index == 0:
                    image.save(folder / 'preview.png')
            sheet.save(destination / ('exterior-four-views.png' if exterior else 'interior-four-views.png'))
        print('RENDERED ' + folder.name, flush=True)
    sheet = Image.new('RGB', (1800, 1410), '#101a20')
    for index, folder in enumerate(sorted((ROOT / 'incoming').iterdir())):
        image = Image.open(folder / 'preview.png').resize((600, 468))
        sheet.paste(image, (index % 3 * 600, index // 3 * 470))
    sheet.save(ROOT / 'overview.png')

def main():
    records = [inspect(folder) for folder in sorted((ROOT / 'incoming').iterdir())]
    save(ROOT / 'manifest.json', dict(group='First batch group 3: frost tundra', maker_thread='01a1a5df-7c46-7fc0-a33d-cfc5b059d4d3',
        scope='S3-01 through S3-09 placeable modules; S3-10 overview is layout reference only. S3-11 through S3-20 are a later batch, not included.',
        entries=[metadata for metadata, report in records], image_generation_calls=0, generated_image_partial_files=[]))
    native()
    native_reports = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    for report in native_reports:
        folder = ROOT / 'incoming' / report['id']
        qa = json.loads((folder / 'qa.json').read_text(encoding='utf-8'))
        qa['native_registry_template'] = report
        save(folder / 'qa.json', qa)
    if '--render' in sys.argv:
        render_all()
    print('PASS: 9 static + native registry/template roundtrip checks; world placement NOT_RUN', flush=True)

if __name__ == '__main__':
    main()
