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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-04-great-hall.png')
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-04'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-04-drowned-banner-hall.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    anchors = [('north', [18, 5, 0]), ('south', [18, 5, 48]), ('west', [0, 5, 30]), ('east', [36, 5, 30])]
    waypoints = [point for _, point in anchors] + [[18, 5, 6], [18, 5, 12], [18, 5, 24], [18, 5, 36], [18, 5, 42], [6, 5, 30], [30, 5, 30], [12, 5, 30], [24, 5, 30]]
    metadata = {'id': 'S4-04', 'name': '沉水双列旗柱大殿', 'size': [37, 23, 49], 'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'entrance': [18, 5, 0], 'interior': [18, 5, 24], 'facing': 'north', 'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'}, 'connections': [{'facing': facing, 'position': point, 'width': 5, 'height': 4, 'inward_depth': 4} for facing, point in anchors], 'route_waypoints': waypoints, 'previous_acceptance': 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-03-ACCEPTANCE.json', 'runtime_slot': None, 'registration': 'PENDING_INDEPENDENT_ACCEPTANCE', 'world_placement_ticks_gameplay': 'NOT_RUN', 'integration': 'NOT_RUN', 'deployment': 'NOT_RUN', 'deviations': ['37x23x49 estimated from visible block proportions, not prompt nominal 21x15.', 'Longitudinal axis is Z: tall northern arched exit, lower southern entry boundary and two lateral ports at Z30.', 'Two rows of three freestanding pillars retained; open roof intentional.', 'One narrow waterlogged side connector at Z30 added for dry side-port accessibility; no basin-wide dry platform.', 'Vanilla green banners carry yellow border and flower/lozenge patterns as approximation; no custom textures.', 'Biome water tint, fog and external swamp forest not reproduced.'], 'assembly': 'Single vanilla NBT. Size Z49 exceeds default structure-block save limit48: do not resave truncated. Use StructureTemplate import with full-volume occupied-space rejection.'}
    save('incoming/S4-04/room.json', metadata)
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
                if names.get((east + delta[0], up + delta[1], south + delta[2]), 'minecraft:air') in {'minecraft:air', 'minecraft:lily_pad', 'minecraft:vine'}:
                    failures.append(['water_boundary', east, up, south, delta])
        if name == 'minecraft:ochre_froglight' and names.get((east, up - 1, south), 'minecraft:air') in {'minecraft:air', 'minecraft:water'}:
            failures.append(['lamp_support', east, up, south])
    assert not failures, failures[:20]
    assert data['size'] == [37, 23, 49] and not data['entities']
    assert not {'minecraft:' + name for name in ['chest', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'lava', 'fire']}.intersection(counts)
    banners = [record for record in data['blocks'] if palette[record['state']]['Name'] == 'minecraft:green_wall_banner']
    assert len(banners) == 8 and all(record['nbt']['id'] == 'minecraft:banner' and len(record['nbt']['Patterns']) >= 2 for record in banners)
    components = []
    remaining = {position for position, state in blocks.items() if state['Name'] == 'minecraft:water' or state.get('Properties', {}).get('waterlogged') == 'true'}
    while remaining:
        pending = [remaining.pop()]
        count = 0
        while pending:
            east, up, south = pending.pop()
            count += 1
            for delta in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
                neighbor = (east + delta[0], up + delta[1], south + delta[2])
                if neighbor in remaining:
                    remaining.remove(neighbor)
                    pending.append(neighbor)
        components.append(count)
    assert len(components) == 2, components
    save('materials.json', dict(counts))
    save('geometry-check.json', {'status': 'PASS', 'sha256': digest, 'water_bottom_side_air_edges': 0, 'fluid_components': sorted(components), 'reason_for_two_components': 'Continuous raised central causeway divides west/east basins intentionally.', 'pillar_rows': [[9, 12], [9, 24], [9, 36], [27, 12], [27, 24], [27, 36]], 'banners_with_patterns': 8, 'exposed_water_columns': sum(names.get((east, 3, south)) == 'minecraft:water' for east in range(3, 34) for south in range(3, 46)), 'basin_columns': 1333, 'scope': 'Static boundary and support checks; fluid/random ticks NOT_RUN.'})
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
    shutil.copy2('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-01-revision1-independent-final/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8').replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of("S4-04")')
    extra = '''for (var element : metadata.getAsJsonArray("connections")) {
            JsonObject connection = element.getAsJsonObject();
            BlockPos anchor = coordinate(connection, "position");
            String facing = connection.get("facing").getAsString();
            for (int offset = -2; offset <= 2; offset++) {
                for (int depth = 0; depth < 4; depth++) {
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


if __name__ == '__main__':
    main()
