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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-02-medium-room.png')
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-02'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-02-drowned-study.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    anchors = [('north', [19, 4, 0]), ('south', [13, 4, 26])]
    waypoints = [point for _, point in anchors] + [[19, 4, 4], [13, 4, 22], [7, 4, 9], [19, 4, 14], [12, 6, 14], [16, 6, 14], [4, 4, 8], [22, 4, 8], [7, 4, 18], [13, 4, 19], [4, 4, 10], [25, 4, 8]]
    metadata = {
        'id': 'S4-02', 'name': '浅水遗迹书房与发光石台', 'size': [29, 18, 27],
        'entrance': [19, 4, 0], 'interior': [12, 6, 14], 'facing': 'north',
        'connections': [{'facing': facing, 'position': point, 'width': 5, 'height': 4, 'position_means': 'feet center', 'floor_y': 3} for facing, point in anchors],
        'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'},
        'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(),
        'sha256': digest, 'route_waypoints': waypoints,
        'runtime_slot': None, 'registration': 'PENDING_COORDINATOR_ACCEPTANCE',
        'world_placement': 'NOT_RUN', 'gameplay': 'NOT_RUN',
        'revision': 1,
        'deviations': ['29x18x27 estimated from image block rhythm, not prompt nominal 15x15.', 'Open-air ruins with low south/east walls and tall north/west walls.', 'Single-block-wide waterlogged top-slab footpaths replace wide dry causeways; free water remains underneath. Two-block basin except beneath footpaths.', 'North grille is moved to doorway sides, leaving a supported 5x4 port, not a sealed gate.', 'Vanilla water/lighting cannot reproduce illustrated fog or green biome tint.'],
        'assembly': 'Single NBT. Origin northwest bottom; no world placement performed.',
        'sequential_authorization': 'S4-01 revision1 ACCEPTED_FOR_SEQUENTIAL_PRODUCTION; only S4-02 authorized.',
        'previous_acceptance': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-01-revision1-independent-final/ACCEPTANCE.json'

    }
    save(target / 'room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    palette = data['palette']
    blocks = {tuple(record['pos']): palette[record['state']]['Name'] for record in data['blocks']}
    counts = collections.Counter(blocks.values())
    fluid = {tuple(record['pos']) for record in data['blocks'] if palette[record['state']]['Name'] == 'minecraft:water' or palette[record['state']].get('Properties', {}).get('waterlogged') == 'true'}
    remaining = set(fluid)
    components = []
    while remaining:
        pending = [remaining.pop()]
        visited = set(pending)
        while pending:
            east, up, south = pending.pop()
            for delta in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
                neighbor = (east + delta[0], up + delta[1], south + delta[2])
                if neighbor in remaining:
                    remaining.remove(neighbor)
                    visited.add(neighbor)
                    pending.append(neighbor)
        components.append(len(visited))
    surface = sum(blocks.get((east, 3, south)) == 'minecraft:water' for east in range(4, 25) for south in range(4, 23))
    assert len(components) == 1, components
    assert counts['minecraft:verdant_froglight'] == 5
    assert surface > 399 / 2, surface
    save(ROOT / 'water-composition.json', {'status': 'PASS', 'sha256': digest, 'interior_floor_columns': 399, 'exposed_water_columns': surface, 'exposed_water_fraction': surface / 399, 'fluid_components_including_waterlogged_slabs': components, 'footpath_width': 1, 'green_inlay_tiles': 5, 'scope': 'Static six-face fluid-cell adjacency; waterlogged slab cells included. Not fluid tick simulation.'})
    failures = []
    for (east, up, south), name in blocks.items():
        if name in {'minecraft:chain', 'minecraft:ochre_froglight'} and blocks.get((east, up + 1, south)) not in {'minecraft:chain', 'minecraft:dark_oak_log'}:
            failures.append(['unsupported_hanging_light', east, up, south])
        if name == 'minecraft:dark_oak_fence' and blocks.get((east, up - 1, south), 'minecraft:air') in {'minecraft:air', 'minecraft:water'}:
            failures.append(['unsupported_table_leg', east, up, south])
        if name == 'minecraft:lily_pad' and blocks.get((east, up - 1, south)) != 'minecraft:water':
            failures.append(['unsupported_lily', east, up, south])
        if name == 'minecraft:water':
            for delta_east, delta_south in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                neighbor = blocks.get((east + delta_east, up, south + delta_south), 'minecraft:air')
                if neighbor in {'minecraft:air', 'minecraft:lily_pad', 'minecraft:vine'}:
                    failures.append(['uncontained_water', east, up, south])
            if blocks.get((east, up - 1, south), 'minecraft:air') == 'minecraft:air':
                failures.append(['uncontained_water_below', east, up, south])
    forbidden = {'minecraft:' + name for name in ['spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void', 'lava', 'fire', 'barrel']}
    assert not forbidden.intersection(counts), forbidden.intersection(counts)
    assert not failures, failures[:20]
    assert data['size'] == [29, 18, 27]
    assert not data['entities']
    chests = [record for record in data['blocks'] if palette[record['state']]['Name'] == 'minecraft:chest']
    assert len(chests) == 2
    assert all(not record.get('nbt', {}).get('Items') and not record.get('nbt', {}).get('LootTable') for record in chests)
    save(ROOT / 'materials.json', dict(counts))
    save(ROOT / 'geometry-check.json', {'status': 'PASS', 'water_containment': 'PASS', 'lily_support': 'PASS', 'forbidden_blocks': 'PASS', 'table_legs_and_hanging_lights_support': 'PASS', 'empty_decorative_chests': 2, 'size': data['size'], 'reference_sha256': metadata['reference_sha256'], 'sha256': digest, 'old_candidate_used': False, 'route_waypoints': waypoints})
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    source = source.replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of("S4-02")')
    marker = 'StructureTemplate template = new StructureTemplate();'
    extra = '''for (com.google.gson.JsonElement element : metadata.getAsJsonArray("connections")) {
            JsonObject connection = element.getAsJsonObject();
            BlockPos anchor = coordinate(connection, "position");
            String facing = connection.get("facing").getAsString();
            boolean alongX = facing.equals("north") || facing.equals("south");
            for (int offset = -2; offset <= 2; offset++) {
                for (int depth = 0; depth < 4; depth++) {
                BlockPos feet = anchor.offset(alongX ? offset : 0, 0, (facing.equals("north") ? depth : -depth));
                require(walkable(stateMap, feet), "Unsafe connection floor: " + feet);
                for (int height = 0; height < 4; height++) {
                    require(clear(stateMap, feet.above(height)), "Connection lacks 5x4 clearance: " + feet);
                }
            }
        }
        }
        for (com.google.gson.JsonElement element : metadata.getAsJsonArray("route_waypoints")) {
            com.google.gson.JsonArray waypoint = element.getAsJsonArray();
            BlockPos target = new BlockPos(waypoint.get(0).getAsInt(), waypoint.get(1).getAsInt(), waypoint.get(2).getAsInt());
            checkPath(stateMap, size, coordinate(metadata, "entrance"), target);
        }
        '''
    source = source.replace(marker, extra + marker)
    source = source.replace('floor.getFluidState().isEmpty()', '(floor.getFluidState().isEmpty() || (floor.getBlock() instanceof net.minecraft.world.level.block.SlabBlock && floor.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.TOP))')
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
    shutil.copy2('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-01-revision1-independent-final/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    (qa / 'VerifyDeliveries.java').write_text(source, encoding='utf-8')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    mapped = 'D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar'
    fixture = qa / 'vanilla-1.20.1-mapped-local-fixture.jar'
    with zipfile.ZipFile(mapped) as original, zipfile.ZipFile(fixture, 'w', compression=zipfile.ZIP_DEFLATED) as output:
        for entry in original.infolist():
            upper = entry.filename.upper()
            if upper.startswith('META-INF/') and (upper.endswith(('.SF', '.RSA', '.DSA', '.EC')) or upper == 'META-INF/MANIFEST.MF'):
                continue
            output.writestr(entry, original.read(entry.filename))
    save(qa / 'fixture-provenance.json', {'source': mapped, 'source_sha256': hashlib.sha256(Path(mapped).read_bytes()).hexdigest(), 'fixture_sha256': hashlib.sha256(fixture.read_bytes()).hexdigest(), 'scope': 'Local QA copy: removed stale archive signatures/manifest from mapped jar; class bytes unchanged; original untouched.'})
    mapped = str(fixture)
    classpath = os.pathsep.join([mapped, *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    for label, command in [
        ('body-compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(qa), str(qa / 'BodyRoutes.java')]),
        ('vine-compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(qa), str(qa / 'VineStability.java')]),
        ('compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(qa), str(qa / 'VerifyDeliveries.java')]),
        ('vine-run', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(qa) + os.pathsep + classpath, 'VineStability', str(ROOT)]),
        ('run', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(qa) + os.pathsep + classpath, 'VerifyDeliveries', str(ROOT)]),
        ('body-run', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(qa) + os.pathsep + classpath, 'BodyRoutes', str(ROOT)])
    ]:
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
        (qa / f'native-{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            print(result.stdout[-5000:] + result.stderr[-5000:])
            raise SystemExit(result.returncode)
    print((qa / 'native-review.json').read_text(encoding='utf-8'))


if __name__ == '__main__':
    main()
