import collections
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
HOME = ROOT.parents[1]
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def static():
    group = json.loads((ROOT / 'group.json').read_text(encoding='utf-8'))
    reports = []
    for item in group['rooms']:
        folder = ROOT / item['id']
        metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
        structure = Structure.read(folder / 'room.nbt')
        assert hashlib.sha256((folder / 'room.nbt').read_bytes()).hexdigest() == metadata['sha256']
        assert tuple(metadata['size']) == structure.size and structure.data_version == 3465
        assert len(structure.blocks) == structure.size[0] * structure.size[1] * structure.size[2]
        assert not structure.entities
        def clear(point):
            return structure.get(point).name == 'minecraft:air'
        def walkable(point):
            across, vertical, forward = point
            floor = structure.get((across, vertical - 1, forward)).name
            return clear(point) and clear((across, vertical + 1, forward)) and floor not in {'minecraft:air', 'minecraft:water', 'minecraft:lava'}
        visited = {tuple(metadata['entrance'])}
        queue = collections.deque(visited)
        while queue:
            across, vertical, forward = queue.popleft()
            for deltaX, deltaZ in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                next_point = (across + deltaX, vertical, forward + deltaZ)
                if not (0 <= next_point[0] < structure.size[0] and 0 <= next_point[2] < structure.size[2]):
                    continue
                if next_point not in visited and walkable(next_point):
                    visited.add(next_point)
                    queue.append(next_point)
        assert tuple(metadata['interior']) in visited, item['id'] + ' safe point'
        for point in metadata['route']:
            assert tuple(point) in visited and clear((point[0], point[1] + 2, point[2])), str((item['id'], 'route', point))
        for port in metadata['ports']:
            assert tuple(port['feet']) in visited
        fluids = []
        for point, block in structure.blocks.items():
            assert block.name.startswith('minecraft:') and block.name not in {'minecraft:spawner', 'minecraft:jigsaw', 'minecraft:command_block', 'minecraft:tnt', 'minecraft:structure_block'}
            if block.name == 'minecraft:water':
                fluids.append(point)
                across, vertical, forward = point
                for neighbor in [(across, vertical - 1, forward), (across + 1, vertical, forward), (across - 1, vertical, forward), (across, vertical, forward + 1), (across, vertical, forward - 1)]:
                    assert structure.get(neighbor).name not in {'minecraft:air', 'minecraft:cave_vines', 'minecraft:cave_vines_plant'}, str((item['id'], 'fluid leak', point, neighbor))
            if block.name in {'minecraft:cave_vines', 'minecraft:cave_vines_plant'}:
                assert structure.get((point[0], point[1] + 1, point[2])).name != 'minecraft:air'
        for entity in structure.block_entities.values():
            content = plain(entity)
            assert not content.get('Items') and not content.get('LootTable') and not content.get('Lock')
        roof_exceptions = {tuple(point) for point in metadata['roof_exceptions']}
        containers = sum(plain(entity).get('id') in {'minecraft:chest', 'minecraft:barrel', 'minecraft:trapped_chest'} for entity in structure.block_entities.values())
        for across in range(structure.size[0]):
            for forward in range(structure.size[2]):
                assert (across, forward) in roof_exceptions or structure.get((across, structure.size[1] - 1, forward)).name != 'minecraft:air'
        report = {'id': item['id'], 'status': 'PASS', 'sha256': metadata['sha256'], 'size': structure.size, 'records': len(structure.blocks), 'palette': dict(collections.Counter(block.name for block in structure.blocks.values())), 'reachable_feet': len(visited), 'required_route_feet': len(metadata['route']), 'water_cells': len(fluids), 'containers': containers, 'noncontainer_block_entities': len(structure.block_entities) - containers, 'roof_exceptions': len(roof_exceptions), 'scope': 'Static NBT bounds, full explicit volume, walkability, 3-high route, sealed water, vine support, empty inventory, complete roof except declared courtyard skylight. Not native engine validation.'}
        save(folder / 'qa/static.json', report)
        save(folder / 'qa.json', {'status': 'CANDIDATE_STATIC_PASS_NATIVE_PENDING', 'sha256': metadata['sha256'], 'static': report, 'native': {'status': 'PENDING'}, 'render': {'status': 'PENDING'}, 'integrated': False})
        spec = {'id': item['id'], 'sha256': metadata['sha256'], 'size': metadata['size'], 'records': len(structure.blocks), 'feet_y': 2, 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'ports': [port['feet'] for port in metadata['ports']], 'lane': metadata['route'], 'specialized_route': False, 'water_cells': len(fluids), 'containers': containers, 'rails': 0, 'rail_networks': [], 'roof_exceptions': metadata['roof_exceptions']}
        save(folder / 'spec.json', spec)
        reports.append(report)
        print('STATIC_PASS', item['id'], flush=True)
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'rooms': reports})


def native():
    group = json.loads((ROOT / 'group.json').read_text(encoding='utf-8'))
    ids = [item['id'] for item in group['rooms']]
    forge = ROOT / 'native-test'
    source = (HOME / 'handover-20261102-01a19c8f/tools/zone_receiver/ReceiverTests.java').read_text(encoding='utf-8')
    source = re.sub(r'private static final String\[\] IDS = \{[^;]+;', 'private static final String[] IDS = {' + ', '.join(json.dumps(room) for room in ids) + '};', source)
    methods = []
    for room_index in range(len(ids)):
        for rotation_index in range(4):
            methods.append(f'    @GameTest(template = "empty", batch = "forest", timeoutTicks = 1200)\n    public static void forest{room_index}rotation{rotation_index}(GameTestHelper helper) throws Exception {{ run(helper, {room_index}, {rotation_index}); }}')
    source = source.replace('    GENERATED_TEST_METHODS', '\n\n'.join(methods))
    source = source.replace('" " + rotation, exception);', '" " + rotation + ": " + exception, exception);')
    target = forge / 'src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(source, encoding='utf-8')
    resources = forge / 'src/main/resources'
    (resources / 'data/ftzonereceiver/structures').mkdir(parents=True, exist_ok=True)
    shutil.copyfile(HOME / 'deliveries/zone-C01/native/src/main/resources/data/ftzonec01qa/structures/empty.nbt', resources / 'data/ftzonereceiver/structures/empty.nbt')
    (resources / 'META-INF').mkdir(exist_ok=True)
    (resources / 'META-INF/mods.toml').write_text('modLoader="javafml"\nloaderVersion="[47,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftzonereceiver"\nversion="1.0"\ndisplayName="Forest isolated QA"\n', encoding='utf-8')
    (resources / 'pack.mcmeta').write_text('{"pack":{"pack_format":15,"description":"Forest QA"}}', encoding='utf-8')
    shutil.copyfile(HOME / 'deliveries/zone-A05/native-test/settings.gradle', forge / 'settings.gradle')
    (forge / 'gradle.properties').write_text('org.gradle.daemon=false\norg.gradle.workers.max=1\norg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8\n', encoding='utf-8')
    (forge / 'build.gradle').write_text('''plugins { id 'net.minecraftforge.gradle' version '6.0.54' }
group = 'net.farmingtales.forestqa'
version = '1.0'
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
minecraft {
    mappings channel: 'official', version: '1.20.1'
    runs {
        gameTestServer {
            jvmArgs '-XX:ActiveProcessorCount=2', '-Xms128m', '-Xmx1536m'
            workingDirectory project.file('../server')
            property 'forge.enabledGameTestNamespaces', 'ftzonereceiver'
            property 'ftreceiver.root', projectDir.parentFile.absolutePath
            mods { ftzonereceiver { source sourceSets.main } }
        }
    }
}
repositories { mavenCentral() }
dependencies { minecraft 'net.minecraftforge:forge:1.20.1-47.4.10' }
tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
''', encoding='utf-8')
    (ROOT / 'server').mkdir(exist_ok=True)
    (ROOT / 'server/server.properties').write_text('server-port=0\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\nlevel-name=world-' + str(int(time.time())) + '\n', encoding='utf-8')
    java = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot'
    launcher = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/lib/gradle-launcher-8.8.jar'))
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(java)
    environment['PATH'] = str(java / 'bin') + os.pathsep + environment['PATH']
    command = [str(java / 'bin/java.exe'), '-Dfile.encoding=UTF-8', '-cp', str(launcher), 'org.gradle.launcher.GradleMain', 'runGameTestServer', '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false']
    save(ROOT / 'qa/native-invocation.json', {'command': command, 'cwd': str(forge), 'production_saves_accessed': False, 'expected_tests': len(ids) * 4, 'stability_ticks': 400})
    with (ROOT / 'qa/native-console.log').open('wb') as stream:
        result = subprocess.run(command, cwd=forge, env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW, timeout=900)
    print('NATIVE_EXIT', result.returncode, flush=True)
    raise SystemExit(result.returncode)


if __name__ == '__main__':
    {'static': static, 'native': native}[sys.argv[1]]()
