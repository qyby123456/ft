import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

from review import ROOT, TOOLS, save

def run_native():
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
    shutil.copy2('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-01-revision1-independent-final/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    source = (TOOLS / 'VerifyDeliveries.java').read_text(encoding='utf-8').replace('List.of("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")', 'List.of("S4-09")')
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
    source = source.replace('int reachable = checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "interior"));', 'int reachable = 0;')
    source = source.replace('checkPath(stateMap, size, coordinate(metadata, "entrance"), new BlockPos(point.get(0).getAsInt(), point.get(1).getAsInt(), point.get(2).getAsInt()));', '')
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


