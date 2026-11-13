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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-01-large-chamber.png')
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/S4-01'
    target.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'nbt/s4-01-swamp-heart-rebuild-v1.nbt', target / 'room.nbt')
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    anchors = [('north', [24, 5, 0]), ('east', [48, 5, 24]), ('south', [24, 5, 48]), ('west', [0, 5, 24])]
    waypoints = [point for _, point in anchors] + [[24, 8, 17], [31, 8, 24], [24, 8, 31], [17, 8, 24], [5, 5, 24], [43, 5, 24], [24, 5, 5], [24, 5, 43]]
    metadata = {
        'id': 'S4-01', 'name': '沼泽古树环水主室', 'size': [49, 31, 49],
        'entrance': [24, 5, 0], 'interior': [24, 8, 17], 'facing': 'north',
        'connections': [{'facing': facing, 'position': point, 'width': 5, 'height': 4, 'position_means': 'feet center', 'floor_y': 4} for facing, point in anchors],
        'origin': [0, 0, 0], 'axes': {'x': 'east', 'y': 'up', 'z': 'south'},
        'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(),
        'sha256': digest, 'route_waypoints': waypoints,
        'runtime_slot': None, 'registration': 'PENDING_COORDINATOR_ACCEPTANCE',
        'world_placement': 'NOT_RUN', 'gameplay': 'NOT_RUN',
        'deviations': ['49x31x49 is estimated from visible block proportions, not the prompt nominal 19x19.', 'Water depth is 2 blocks; all mandatory routes are dry.', 'South/east ruined walls are intentionally low, north/west are tall; no artificial roof.', 'Vanilla daytime preview cannot reproduce shader mist, fireflies or tinted swamp biome water.'],
        'assembly': 'One NBT; command/structure placement must use size 49x31x49, not limited to a 16-block chunk; no world is modified by this delivery.'
    }
    save(target / 'room.json', metadata)
    data = plain(read_nbt(target / 'room.nbt')[1])
    palette = data['palette']
    blocks = {tuple(record['pos']): palette[record['state']]['Name'] for record in data['blocks']}
    counts = collections.Counter(blocks.values())
    failures = []
    for (east, up, south), name in blocks.items():
        if name == 'minecraft:lily_pad' and blocks.get((east, up - 1, south)) != 'minecraft:water':
            failures.append(['unsupported_lily', east, up, south])
        if name == 'minecraft:water':
            for delta_east, delta_south in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                neighbor = blocks.get((east + delta_east, up, south + delta_south), 'minecraft:air')
                if neighbor in {'minecraft:air', 'minecraft:lily_pad', 'minecraft:vine'}:
                    failures.append(['uncontained_water', east, up, south])
            if blocks.get((east, up - 1, south), 'minecraft:air') == 'minecraft:air':
                failures.append(['uncontained_water_below', east, up, south])
    forbidden = {'minecraft:' + name for name in ['spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void', 'lava', 'fire', 'chest', 'barrel']}
    assert not forbidden.intersection(counts), forbidden.intersection(counts)
    assert not failures, failures[:20]
    assert data['size'] == [49, 31, 49]
    assert not data['entities']
    save(ROOT / 'materials.json', dict(counts))
    save(ROOT / 'geometry-check.json', {'status': 'PASS', 'water_containment': 'PASS', 'lily_support': 'PASS', 'forbidden_blocks': 'PASS', 'size': data['size'], 'reference_sha256': metadata['reference_sha256'], 'sha256': digest, 'old_candidate_used': False, 'route_waypoints': waypoints})
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    source = source.replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of("S4-01")')
    marker = 'StructureTemplate template = new StructureTemplate();'
    extra = '''for (com.google.gson.JsonElement element : metadata.getAsJsonArray("connections")) {
            JsonObject connection = element.getAsJsonObject();
            BlockPos anchor = coordinate(connection, "position");
            String facing = connection.get("facing").getAsString();
            boolean alongX = facing.equals("north") || facing.equals("south");
            for (int offset = -2; offset <= 2; offset++) {
                BlockPos feet = anchor.offset(alongX ? offset : 0, 0, alongX ? 0 : offset);
                require(walkable(stateMap, feet), "Unsafe connection floor: " + feet);
                for (int height = 0; height < 4; height++) {
                    require(clear(stateMap, feet.above(height)), "Connection lacks 5x4 clearance: " + feet);
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
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
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
        ('vine-compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(qa), str(qa / 'VineStability.java')]),
        ('compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(qa), str(qa / 'VerifyDeliveries.java')]),
        ('vine-run', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(qa) + os.pathsep + classpath, 'VineStability', str(ROOT)]),
        ('run', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(qa) + os.pathsep + classpath, 'VerifyDeliveries', str(ROOT)])
    ]:
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
        (qa / f'native-{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            print(result.stdout[-5000:] + result.stderr[-5000:])
            raise SystemExit(result.returncode)
    print((qa / 'native-review.json').read_text(encoding='utf-8'))


if __name__ == '__main__':
    main()
