import collections
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parent
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-03-small-room.png')
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, data):
    (ROOT / relative).write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-03'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-03-rootbound-cache.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    connections = [{'facing': 'north', 'position': [6, 4, 0], 'width': 3, 'height': 4, 'inward_depth': 3}, {'facing': 'east', 'position': [20, 4, 14], 'width': 3, 'height': 4, 'inward_depth': 3}]
    waypoints = [[6, 4, 0], [20, 4, 14], [6, 4, 3], [17, 4, 14], [15, 4, 11], [12, 5, 11], [11, 5, 12], [5, 4, 9], [3, 4, 9], [15, 4, 17], [5, 4, 17], [13, 4, 3]]
    metadata = {
        'id': 'S4-03', 'name': '盘根水窟藏宝小室', 'size': [21, 17, 21], 'sha256': digest,
        'entrance': [6, 4, 0], 'interior': [11, 5, 12], 'facing': 'north',
        'connections': connections, 'route_waypoints': waypoints,
        'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'},
        'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(),
        'previous_acceptance': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-02-ACCEPTANCE.json',
        'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE',
        'world_placement': 'NOT_RUN', 'world_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN',
        'deviations': ['21x17x21 is an estimated grid from visible image blocks, not prompt nominal 9x9.', 'North opening maps to image left/back doorway; east opening maps to narrow right doorway.', 'One-block waterlogged top-slab walkways are added at basin edge and on one approach to chest island; fluid below connects, player stays dry.', 'No illustrated external swamp forest or shader fog is built; roots use vanilla logs and waterlogged mangrove_roots.', 'Chest is empty decorative storage, without LootTable, rewards or spawns.', 'Open-air ruin silhouette matches reference; no hidden roof or preview-only stripped shell.'],
        'assembly': 'Single NBT; align feet Y4. Full-volume occupancy rejection required; sparse missing blocks are not terrain clearing.'
    }
    save('incoming/S4-03/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    palette = data['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in data['blocks']}
    names = {position: state['Name'] for position, state in blocks.items()}
    counts = collections.Counter(names.values())
    failures = []
    for (east, up, south), name in names.items():
        if name == 'minecraft:lily_pad' and names.get((east, up - 1, south)) != 'minecraft:water':
            failures.append(['lily_support', east, up, south])
        if name == 'minecraft:water':
            for delta in [(1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)]:
                neighbor = (east + delta[0], up + delta[1], south + delta[2])
                if names.get(neighbor, 'minecraft:air') in {'minecraft:air', 'minecraft:lily_pad', 'minecraft:vine'}:
                    failures.append(['water_boundary', east, up, south, delta])
        if name == 'minecraft:chain' and names.get((east, up + 1, south)) not in {'minecraft:dark_oak_log', 'minecraft:chain'}:
            failures.append(['chain_support', east, up, south])
        if name == 'minecraft:ochre_froglight' and names.get((east, up + 1, south)) != 'minecraft:chain' and names.get((east, up - 1, south), 'minecraft:air') in {'minecraft:air', 'minecraft:water'}:
            failures.append(['lamp_support', east, up, south])
    assert not failures, failures[:20]
    assert data['size'] == [21, 17, 21] and not data['entities']
    forbidden = {'minecraft:' + name for name in ['spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void', 'lava', 'fire']}
    assert not forbidden.intersection(counts)
    chests = [record for record in data['blocks'] if palette[record['state']]['Name'] == 'minecraft:chest']
    assert len(chests) == 1 and chests[0]['nbt']['id'] == 'minecraft:chest'
    assert not chests[0]['nbt'].get('Items') and not chests[0]['nbt'].get('LootTable')
    fluid = {position for position, state in blocks.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    remaining = set(fluid)
    components = []
    while remaining:
        queue = [remaining.pop()]
        count = 0
        while queue:
            east, up, south = queue.pop()
            count += 1
            for delta in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
                neighbor = (east + delta[0], up + delta[1], south + delta[2])
                if neighbor in remaining:
                    remaining.remove(neighbor)
                    queue.append(neighbor)
        components.append(count)
    assert len(components) == 1, components
    surface = sum(names.get((east, 3, south)) == 'minecraft:water' for east in range(3, 18) for south in range(3, 18))
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'water_bottom_side_air_edges': 0, 'fluid_components_including_waterlogged': components, 'exposed_water_columns': surface, 'basin_columns': 225, 'lily_lamp_chain_support': 'PASS', 'empty_chests': 1, 'size': data['size'], 'scope': 'Static water adjacency and supports; random/fluid ticks NOT_RUN.'})
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
    shutil.copy2('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-01-revision1-independent-final/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    source = source.replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of("S4-03")')
    extra = '''for (var element : metadata.getAsJsonArray("connections")) {
            JsonObject connection = element.getAsJsonObject();
            BlockPos anchor = coordinate(connection, "position");
            String facing = connection.get("facing").getAsString();
            for (int offset = -1; offset <= 1; offset++) {
                for (int depth = 0; depth < 3; depth++) {
                    BlockPos feet = facing.equals("north") ? anchor.offset(offset, 0, depth) : anchor.offset(-depth, 0, offset);
                    require(walkable(stateMap, feet), "Unsafe port floor: " + feet);
                    for (int height = 0; height < 4; height++) require(clear(stateMap, feet.above(height)), "Port body obstruction: " + feet);
                }
            }
        }
        for (var element : metadata.getAsJsonArray("route_waypoints")) {
            var point = element.getAsJsonArray();
            checkPath(stateMap, size, coordinate(metadata, "entrance"), new BlockPos(point.get(0).getAsInt(), point.get(1).getAsInt(), point.get(2).getAsInt()));
        }
        '''
    source = source.replace('StructureTemplate template = new StructureTemplate();', extra + 'StructureTemplate template = new StructureTemplate();')
    source = source.replace('floor.getFluidState().isEmpty()', '(floor.getFluidState().isEmpty() || (floor.getBlock() instanceof net.minecraft.world.level.block.SlabBlock && floor.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.TOP))')
    (qa / 'VerifyDeliveries.java').write_text(source, encoding='utf-8')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    fixture = qa / 'vanilla-1.20.1-mapped-local-fixture.jar'
    with zipfile.ZipFile(mapped) as original, zipfile.ZipFile(fixture, 'w', compression=zipfile.ZIP_DEFLATED) as output:
        for entry in original.infolist():
            upper = entry.filename.upper()
            if upper.startswith('META-INF/') and (upper.endswith(('.SF', '.RSA', '.DSA', '.EC')) or upper == 'META-INF/MANIFEST.MF'):
                continue
            output.writestr(entry, original.read(entry.filename))
    save('qa/fixture-provenance.json', {'source': str(mapped), 'source_sha256': hashlib.sha256(mapped.read_bytes()).hexdigest(), 'fixture_sha256': hashlib.sha256(fixture.read_bytes()).hexdigest(), 'scope': 'Local QA signature-stripped mapped jar; class bytes unchanged; original untouched.'})
    dependencies = [line for line in Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(fixture), *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    commands = [('compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(qa), *[str(qa / filename) for filename in ['VerifyDeliveries.java', 'VineStability.java', 'BodyRoutes.java']]])]
    for name in ['VineStability', 'VerifyDeliveries', 'BodyRoutes']:
        commands.append((name, [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(qa) + os.pathsep + classpath, name, str(ROOT)]))
    for label, command in commands:
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
        (qa / f'{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            print((result.stdout + result.stderr).encode('ascii', errors='backslashreplace').decode()[-7000:])
            raise SystemExit(result.returncode)
    print((qa / 'native-review.json').read_text(encoding='utf-8'))


if __name__ == '__main__':
    main()
