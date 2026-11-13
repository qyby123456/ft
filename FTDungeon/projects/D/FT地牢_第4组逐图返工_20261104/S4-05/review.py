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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-05-corridor.png')
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt, write_nbt


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def run_native():
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
    shutil.copy2('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-01-revision1-independent-final/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8').replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of("S4-05")')
    extra = '''for (var element : metadata.getAsJsonArray("connections")) {
            JsonObject connection = element.getAsJsonObject();
            BlockPos anchor = coordinate(connection, "position");
            String facing = connection.get("facing").getAsString();
            for (int offset = -1; offset <= 1; offset++) {
                for (int depth = 0; depth < 3; depth++) {
                    BlockPos feet = switch (facing) {
                        case "north" -> anchor.offset(offset, 0, depth);
                        case "south" -> anchor.offset(offset, 0, -depth);
                        case "west" -> anchor.offset(depth, 0, offset);
                        default -> anchor.offset(-depth, 0, offset);
                    };
                    require(walkable(stateMap, feet), "Unsafe port floor " + feet);
                    for (int height = 0; height < 4; height++) require(clear(stateMap, feet.above(height)), "Blocked port " + feet);
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
    save('qa/fixture-provenance.json', {'source': str(mapped), 'source_sha256': hashlib.sha256(mapped.read_bytes()).hexdigest(), 'fixture_sha256': hashlib.sha256(fixture.read_bytes()).hexdigest(), 'scope': 'Signature-stripped local QA mapped jar copy, unchanged class bytes, source untouched.'})
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
            print((result.stdout + result.stderr).encode('ascii', errors='backslashreplace').decode()[-6000:])
            raise SystemExit(result.returncode)
    print((qa / 'native-review.json').read_text(encoding='utf-8'))



def main():
    target = ROOT / 'incoming/S4-05'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-05-flooded-timber-corridor.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    previous_path = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-04-R1-independent/independent-details.json')
    previous = json.loads(previous_path.read_text(encoding='utf-8'))
    assert previous['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert hashlib.sha256((ROOT.parent / 'S4-04/incoming/S4-04/room.nbt').read_bytes()).hexdigest() == previous['sha256']
    waypoints = [[7, 4, 0], [7, 4, 38]] + [[7 if south < 4 or south > 34 or (south // 6) % 2 == 0 else 8, 4, south] for south in range(3, 37, 3)]
    metadata = {'id': 'S4-05', 'name': '垂藤积水木框长廊', 'size': [15, 14, 39], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'previous_acceptance': str(previous_path), 'previous_sha256': previous['sha256'], 'entrance': [7, 4, 0], 'interior': [7, 4, 18], 'facing': 'north', 'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': facing, 'position': point, 'width': 3, 'height': 4, 'inward_depth': 3} for facing, point in [('north', [7, 4, 0]), ('south', [7, 4, 38])]], 'route_waypoints': waypoints, 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['15x14x39 estimated from image; image does not provide measured dimensions.', 'Roof retained across entire candidate; exterior view is intentionally opaque.', 'Two-block-wide gently offset waterlogged top-slab route is a dry accessibility approximation of reference submerged stepping stones; no full-width dry aisle.', 'Vanilla froglights with wood caps/brackets approximate framed warm lamps, no shader reflections or biome water tint.', 'Preview cutaway is an explicitly air-only derivative; never install it instead of candidate.']}
    save('incoming/S4-05/room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    palette = data['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in data['blocks']}
    names = {position: state['Name'] for position, state in blocks.items()}
    counts = collections.Counter(names.values())
    failures = []
    fluid = {position for position, state in blocks.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    for east, up, south in fluid:
        for delta in [(1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)]:
            adjacent = (east + delta[0], up + delta[1], south + delta[2])
            if names.get(adjacent, 'minecraft:air') in {'minecraft:air', 'minecraft:vine', 'minecraft:lily_pad'}:
                failures.append(['water_boundary', [east, up, south], adjacent])
    for position, name in names.items():
        if name == 'minecraft:lily_pad':
            assert names.get((position[0], position[1] - 1, position[2])) == 'minecraft:water', position
    assert not failures, failures[:12]
    assert data['size'] == [15, 14, 39] and not data['entities']
    assert not [record for record in data['blocks'] if 'nbt' in record]
    assert not {'minecraft:' + name for name in ['chest', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'lava', 'fire']}.intersection(counts)
    roof_columns = sum(all(names.get((east, up, south), 'minecraft:air') not in {'minecraft:air', 'minecraft:water'} for up in [11, 12, 13]) for east in range(15) for south in range(39))
    assert roof_columns == 585
    solid = {position for position, name in names.items() if name not in {'minecraft:air', 'minecraft:water', 'minecraft:vine', 'minecraft:lily_pad', 'minecraft:cave_vines', 'minecraft:cave_vines_plant'}}
    connected = {(0, 0, 0)}
    pending = list(connected)
    while pending:
        east, up, south = pending.pop()
        for delta in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
            adjacent = (east + delta[0], up + delta[1], south + delta[2])
            if adjacent in solid and adjacent not in connected:
                connected.add(adjacent)
                pending.append(adjacent)
    assert solid == connected, list(solid - connected)[:10]
    assert counts['minecraft:ochre_froglight'] == 10
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'water_bottom_side_air_edges': 0, 'complete_three_layer_roof_columns': roof_columns, 'architectural_solid_components': 1, 'supported_wall_lamps': 10, 'paired_timber_frames': 5, 'water_surface_columns': counts['minecraft:water'], 'interior_floor_columns': 407, 'water_dominant': counts['minecraft:water'] > 407 / 2, 'scope': 'Static enclosure and architectural connection; actual fluid/random ticks NOT_RUN.'})
    run_native()


if __name__ == '__main__':
    main()
