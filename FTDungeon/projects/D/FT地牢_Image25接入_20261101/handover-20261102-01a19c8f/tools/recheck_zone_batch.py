import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
import time
import urllib.request
from datetime import datetime, timedelta, timezone
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw
import intake_zones as intake


ROOT = intake.ROOT
HOME = intake.HOME
JAVA = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')
NODE = Path('C:/Program Files/nodejs/node.exe')
BASE = HOME.parent / 'deliveries/zone-A05'
EXPECTED = {
    'zone-A05': '24d4a8e622bc191797b3d62af7478c074192eb16a6bd13454d6faa534cd96d40',
    'zone-A01': 'd4bd1ffca427cb8d5693d32204fb1dd5957993339507edd8e2b96718affa28c9',
    'zone-C02': '3efd164b2a7ee8e8cbf328dd6a066d65877d77dd8435db633cf88c16da8c98c8',
    'zone-C05': 'e26f62079f60205e30da369d39e73217664e235844f51eb347faa343d0bde95a',
    'zone-M01': '8138c9601ce31b828ccd1fea88670cf81cde03455bde73d76d6b0e6c7c9837fd',
    'zone-H03': '63420cfb4a6a4d91c354a820e62c2df773752d98d541b4282c565b797ba84631',
    'zone-H02': 'bb3d0dc820024d33c57c56c9e8fdad19ea10939fa291aef6621456fa99d91138',
    'zone-R04': 'da8e9f21e39d9bd24991261eff4f04d56dde825a545b8c7e857459c3f9ef185f',
    'zone-M03': 'f318cf2ca68ee38e98cdb7d35018bbc860da705de46d32030c0a8f85638b4aac',
    'zone-A04': '6bb3b157f5a457515cc39cbb9f8532526823f8e28dc0c1a80c45e509ee89ff7e',
    'zone-N02': '163a640721931400ff07c1680fd34cf99e6b5bd0da86e046442ef33c5e3a90dc',
    'zone-P03': 'c95e063e7461496ab421f6c5e8d0642f9db3819330105db02fbcdd614927a161',
    'zone-N01': 'e605ff944681a414ab25c113388e51863a37f987417456bd0244d74315737f63',
    'zone-N04': '14c978b39244c763c15356388bd1176b1b2452ef37f3a5bed6963890be7cc8d5',
    'zone-A02': '0c9c677cc9e2fd9c15e2dc3341680a490e9920ff90a769375413a68c901eca33',
    'zone-R03': '5035cce02b72189a0a75310657011031da7d3b58dd9a20ec97390ca0a79df326',
    'zone-C03': 'de59629577403c50075f606284cddfb40c0d1612d5cbbee95657822a0f9c2ad2',
    'zone-R02': '50a5eaeaba0a791056625fd164bad013a737022d9fff5e5395573305bae00793',
    'zone-N03': '3c2488468131a5b283fbc80377ae4e62c4837f0d9aed58d175602dbe04f8ccb0',
    'zone-P04': '90b84af4d54f49711893225c8ba5314e0f66c0c7dbd8d3dd17b68e66b1059dc7',
}
ROTATIONS = ['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90']


def stamp():
    return datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S-%f')


def write(path, text):
    path = intake.resolved(path)
    intake.require(path.is_relative_to(intake.resolved(ROOT)), 'Output outside receiver root')
    intake.io_path(path).parent.mkdir(parents=True, exist_ok=True)
    with intake.io_path(path).open('x', encoding='utf-8') as stream:
        stream.write(text)


def copy_file(source, target):
    return intake.freeze(source, target)


def identity(entry):
    return {key: entry[key] for key in ('id', 'candidate_sha256', 'bundle_sha256')}


def context(room_id):
    latest = intake.load(ROOT / 'latest.json')
    registry = intake.load(latest['registry']['path'])
    entry = next(item for item in registry['zones'] if item['id'] == room_id)
    intake.require(entry['candidate_sha256'] == EXPECTED[room_id], 'Candidate does not match requested final hash')
    review = ROOT / 'reviews' / room_id / entry['candidate_sha256'] / entry['bundle_sha256']
    alias = ROOT / 'work' / (room_id.lower() + '-' + entry['bundle_sha256'][:12])
    intake.io_path(review).mkdir(parents=True, exist_ok=True)
    if not alias.exists():
        alias.parent.mkdir(parents=True, exist_ok=True)
        command = ['$target=' + "'" + str(review).replace("'", "''") + "'", '$link=' + "'" + str(alias).replace("'", "''") + "'",
                   "New-Item -ItemType Junction -Path $link -Target $target | Out-Null"]
        subprocess.run(['powershell.exe', '-NoProfile', '-Command', '\n'.join(command)], check=True, creationflags=subprocess.CREATE_NO_WINDOW)
    intake.require(intake.resolved(alias) == intake.resolved(review), 'Unexpected receiver junction')
    return entry, alias


def prepare(room_id):
    entry, review = context(room_id)
    fixture = review / 'fixture'
    source = Path(entry['delivery_directory'])
    originals = []
    for name in intake.REQUIRED:
        information = entry['files'][name]
        intake.verify_file(information)
        intake.require(intake.digest(source / name) == information['sha256'], 'Producer final delivery changed')
        originals.append(copy_file(information['path'], fixture / name))
    for filename in ['capture-preview.mjs', 'preview-server.mjs', 'viewer.mjs', 'viewer.html']:
        originals.append(copy_file(BASE / 'tools' / filename, fixture / 'tools' / filename))
    originals.append(copy_file(BASE / 'native-test/src/main/resources/data/ftzonea05qa/structures/empty.nbt', fixture / 'empty.nbt'))
    static = intake.static_check(Path(entry['snapshot']), entry)
    intake.save(review / 'receiver-static.json', {**identity(entry), **static})
    intake.save(review / 'preparation.json', {**identity(entry), 'slot': entry['slot'], 'category': entry['category'],
                                            'copied_inputs': originals, 'producer_inputs_modified': False,
                                            'receiver_fixture': 'independent common registry, collision, inventory, liquid and exact-state tests'})
    print('PREPARED', room_id, review, flush=True)


def specification(entry, fixture):
    sys.path.insert(0, str(intake.INSTANCE / 'output/dungeon-expansion-20261101/tools'))
    from nbtio import Structure, plain
    metadata = intake.load(fixture / 'room.json')
    structure = Structure.read(fixture / 'room.nbt')
    room_id = entry['id']
    lane = set()
    if room_id == 'zone-A05':
        lane = {(across, 2, depth) for across in range(9, 12) for depth in range(20)}
    elif room_id == 'zone-A01':
        lane = {(across, 2, depth) for across in range(7, 10) for depth in range(15)}
    elif room_id == 'zone-C02':
        lane = {(across, 2, depth) for across in range(8, 11) for depth in range(11)} | {(across, 2, depth) for across in range(8, 19) for depth in range(8, 11)}
    elif room_id == 'zone-C05':
        lane = {(across, 2, depth) for across in range(10, 15) for depth in range(15)} | {(across, 2, depth) for across in range(10, 25) for depth in range(10, 15)}
    elif room_id == 'zone-M01':
        lane = {(across, 2, depth) for across in range(27) for depth in [8, 9, 11, 12]} | {(across, 2, depth) for across in range(11, 16) for depth in range(13, 23)}
    elif room_id == 'zone-H02':
        lane = {(across, 1, depth) for across in range(13, 16) for depth in range(9)}
        lane |= {(across, 1, depth) for across in [6, 7, 8, 20, 21, 22] for depth in range(6, 28)}
        lane |= {(across, 1, depth) for across in range(6, 23) for depth in [6, 7, 8, 25, 26, 27]}
    elif room_id == 'zone-R04':
        lane = {(across, 2, depth) for across in range(11, 14) for depth in range(25)}
    elif room_id == 'zone-M03':
        lane = {(across, 1, depth) for across in range(9, 14) for depth in range(25)}
    elif room_id == 'zone-A04':
        lane = {(across, 1, depth) for across in range(8, 11) for depth in range(15)}
    elif room_id == 'zone-A02':
        lane = {(across, 2, depth) for across in range(9, 12) for depth in range(21)}
    elif room_id == 'zone-R03':
        lane = {(across, 2, depth) for across in range(10, 15) for depth in range(20)}
    elif room_id == 'zone-C03':
        lane = {tuple(point) for point in metadata['route']['cells']}
    elif room_id == 'zone-R02':
        lane = {tuple(point) for point in metadata['required_walkway']}
    elif room_id == 'zone-N04':
        lane = {(across, 2, depth) for across in range(15, 20) for depth in range(35)} | {(across, 2, depth) for across in range(35) for depth in range(15, 20)}
    portals = metadata.get('walking_ports', metadata.get('ports', metadata.get('interfaces', metadata.get('connections', metadata.get('openings', metadata.get('portals', []))))))
    ports = [item.get('feet', item.get('center', item.get('position'))) for item in portals]
    entrance = metadata.get('entrance', ports[0] if ports else None)
    interior = metadata.get('interior', metadata.get('safePoint', metadata.get('center_safe')))
    intake.require(entrance is not None and interior is not None, 'Missing receiver entrance/safe-point mapping')
    if metadata.get('exit') is not None:
        ports.append(metadata['exit'])
    ports += metadata.get('safe_points', [])
    if room_id == 'zone-N01':
        lane = {(across, 2, depth) for across in range(11, 14) for depth in range(25) if depth < 11 or depth > 13} | {(across, 2, depth) for across in range(25) for depth in range(11, 14) if across < 11 or across > 13}
    if room_id == 'zone-N02':
        lane = {(across, 2, depth) for across in range(14, 19) for depth in range(31)} | {(across, 2, depth) for across in range(16, 33) for depth in range(21, 26)}
    roof_exceptions = []
    if room_id == 'zone-A02':
        holes = {(across, depth) for across in range(21) for depth in range(21)
                 if (across, depth) != (17, 3) and ((across == 20 and 1 <= depth <= 6)
                    or (depth == 0 and 15 <= across <= 19 and across != 17)
                    or (13 <= across <= 19 and 1 <= depth <= 6 and not (across == 13 and depth >= 5) and (across, depth) != (19, 6)))}
        actual_holes = {(across, depth) for across in range(21) for depth in range(21) if structure.blocks[(across, 10, depth)].name == 'minecraft:air'}
        intake.require(actual_holes == holes and len(holes) == metadata['collapse']['opening_cells'] == 48, 'Roof does not match northeast collapse plan')
        roof_exceptions = [[across, depth] for across in range(21) for depth in range(21)
                           if not any(structure.blocks.get((across, height, depth)) is not None and structure.blocks[(across, height, depth)].name != 'minecraft:air' for height in range(5, 12))]
        intake.require(all(tuple(point) in holes for point in roof_exceptions), 'Unexpected uncovered column outside collapse')
    if room_id == 'zone-N03':
        roof_exceptions = [[across, depth] for across in range(35) for depth in range(35)
                           if all(structure.blocks[(across, height, depth)].name == 'minecraft:air' for height in range(18))]
        intake.require(len(roof_exceptions) == 112, 'Stair-hub exterior setback changed')
    return {'id': room_id, 'sha256': entry['candidate_sha256'], 'size': list(structure.size), 'records': len(structure.blocks),
            'entrance': entrance, 'interior': interior, 'feet_y': entrance[1], 'roof_exceptions': roof_exceptions,
            'specialized_route': room_id in {'zone-H03', 'zone-N03', 'zone-P03', 'zone-P04'},
            'rail_networks': metadata.get('rail_networks', []),
            'ports': ports, 'lane': [list(point) for point in sorted(lane)],
            'containers': sum(plain(value).get('id') in {'minecraft:barrel', 'minecraft:chest', 'minecraft:furnace'} for value in structure.block_entities.values()),
            'water_cells': sum(state.name in {'minecraft:water', 'minecraft:lava'} or state.get('waterlogged') == 'true' for state in structure.blocks.values()),
            'rails': sum(state.name == 'minecraft:rail' for state in structure.blocks.values())}


def native(room_id):
    entry, review = context(room_id)
    run = review / 'native' / stamp()
    room = run / room_id
    fixture = review / 'fixture'
    for name in ['room.nbt', 'room.json']:
        copy_file(fixture / name, room / name)
    spec = specification(entry, fixture)
    intake.save(room / 'spec.json', spec, exclusive=True)
    room_index = list(EXPECTED).index(room_id)
    methods = []
    for rotation_index, rotation in enumerate(ROTATIONS):
        methods.append(f'    @GameTest(template = "empty", batch = "receiver", timeoutTicks = 1200)\n'
                       f'    public static void receiver{rotation.replace("_", "")} (GameTestHelper helper) throws Exception {{ run(helper, {room_index}, {rotation_index}); }}')
    template_path = HOME / 'tools/zone_receiver/ReceiverTests.java'
    template = intake.io_path(template_path).read_text(encoding='utf-8')
    template = re.sub(r'private static final String\[\] IDS = \{[^;]+;', 'private static final String[] IDS = ' + '{' + ', '.join(json.dumps(room) for room in EXPECTED) + '};', template)
    write(run / 'forge/src/main/java/net/farmingtales/zonereceiver/ReceiverTests.java', template.replace('    GENERATED_TEST_METHODS', '\n\n'.join(methods)))
    copy_file(fixture / 'empty.nbt', run / 'forge/src/main/resources/data/ftzonereceiver/structures/empty.nbt')
    copy_file(BASE / 'native-test/settings.gradle', run / 'forge/settings.gradle')
    write(run / 'forge/gradle.properties', 'org.gradle.daemon=false\norg.gradle.workers.max=1\norg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8\n')
    write(run / 'forge/src/main/resources/META-INF/mods.toml', 'modLoader="javafml"\nloaderVersion="[47,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftzonereceiver"\nversion="1.0"\ndisplayName="Isolated zone receiver tests"\n')
    write(run / 'forge/src/main/resources/pack.mcmeta', '{"pack":{"pack_format":15,"description":"receiver tests"}}')
    write(run / 'forge/build.gradle', '''plugins { id 'net.minecraftforge.gradle' version '6.0.54' }
group = 'net.farmingtales.zonereceiver'
version = '1.0'
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
minecraft {
    mappings channel: 'official', version: '1.20.1'
    runs {
        gameTestServer {
            jvmArgs '-XX:ActiveProcessorCount=2', '-Xms128m', '-Xmx1024m'
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
''')
    write(run / 'server/server.properties', 'server-port=0\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\n')
    launcher = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/lib/gradle-launcher-8.8.jar'))
    command = [str(JAVA / 'bin/java.exe'), '-Dfile.encoding=UTF-8', '-cp', str(launcher), 'org.gradle.launcher.GradleMain',
               'runGameTestServer', '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false']
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(JAVA)
    environment['PATH'] = str(JAVA / 'bin') + os.pathsep + environment.get('PATH', '')
    intake.save(run / 'invocation.json', {**identity(entry), 'command': command, 'cwd': str(run / 'forge'),
                                        'fresh_world': str(run / 'server/world'), 'fixture_template': intake.record(template_path),
                                        'specification': intake.record(room / 'spec.json'), 'production_saves_accessed': False})
    print('START_NATIVE', room_id, run, flush=True)
    with (run / 'console.log').open('wb') as stream:
        process = subprocess.run(command, cwd=run / 'forge', env=environment, stdout=stream, stderr=subprocess.STDOUT,
                                 creationflags=subprocess.CREATE_NO_WINDOW, timeout=900)
    intake.require(process.returncode == 0, 'Native command failed: ' + str(run / 'console.log'))
    xml = ElementTree.parse(run / 'native-tests.xml').getroot()
    intake.require(len(list(xml.iter('testcase'))) == 4 and not any(list(xml.iter(name)) for name in ['failure', 'error', 'skipped']), 'Four native GameTests did not pass')
    captures = validate_captures(spec, room)
    reports = []
    for rotation in ROTATIONS:
        path = room / ('forge-' + rotation.lower() + '.json')
        report = intake.load(path)
        intake.require(report['status'] == 'PASS' and report['source_nbt_sha256'] == entry['candidate_sha256'] and report['stabilization_ticks'] == 400, 'Invalid native report')
        reports.append(intake.record(path))
    result = {**identity(entry), 'status': 'PASS', 'exit_code': 0, 'rotations': [0, 90, 180, 270], 'stable_ticks': 400,
              'isolated_world': True, 'world_directory': str(run / 'server/world'), 'logs': [intake.record(run / 'console.log'), intake.record(run / 'server/logs/latest.log')],
              'test_results': [intake.record(run / 'native-tests.xml')], 'native_reports': reports, 'native_captures': captures,
              'records_checked': spec['records'] * 4, 'native_captures_match_frozen_nbt': True, 'invocation': intake.record(run / 'invocation.json'),
              'random_tick_speed': 0, 'fluid_ticks_forced': True, 'limitations': 'No client/multiplayer, hall integration, occupancy refusal or idempotence test; random plant growth disabled'}
    intake.save(run / 'result.json', result, exclusive=True)
    intake.save(review / 'native/latest.json', {'result': intake.record(run / 'result.json')})
    print('NATIVE_PASS', room_id, flush=True)


def validate_captures(spec, directory):
    from nbtio import Structure, plain
    original = Structure.read(directory / 'room.nbt')
    captures = []
    width, height, length = original.size
    directions = ['north', 'east', 'south', 'west']
    for turns, rotation in enumerate(ROTATIONS):
        capture_path = directory / ('native-placed-' + rotation.lower() + '.nbt')
        capture = Structure.read(capture_path)
        intake.require(not capture.entities and len(capture.blocks) == len(original.blocks), 'Capture entity/count mismatch')
        for position, state in original.blocks.items():
            across, vertical, depth = position
            target = [(across, vertical, depth), (length - 1 - depth, vertical, across),
                      (width - 1 - across, vertical, length - 1 - depth), (depth, vertical, width - 1 - across)][turns]
            properties = dict(state.properties)
            if properties.get('facing') in directions:
                properties['facing'] = directions[(directions.index(properties['facing']) + turns) % 4]
            if properties.get('axis') in ['x', 'z'] and turns % 2:
                properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
            if 'rotation' in properties:
                properties['rotation'] = str((int(properties['rotation']) + turns * 4) % 16)
            for step in range(turns):
                previous = properties.copy()
                for index, direction in enumerate(directions):
                    if direction in previous:
                        properties[directions[(index + 1) % 4]] = previous[direction]
                if 'shape' in previous:
                    shape = previous['shape']
                    rotated = {'north_south': 'east_west', 'east_west': 'north_south', 'ascending_north': 'ascending_east', 'ascending_east': 'ascending_south',
                               'ascending_south': 'ascending_west', 'ascending_west': 'ascending_north', 'north_east': 'south_east', 'south_east': 'south_west', 'south_west': 'north_west', 'north_west': 'north_east'}
                    properties['shape'] = rotated.get(shape, shape)
            actual = capture.blocks.get(target)
            intake.require(actual is not None and actual.name == state.name and dict(actual.properties) == properties, 'Native capture state mismatch: ' + str((rotation, position, state, actual)))
            entity = original.block_entities.get(position)
            actual_entity = capture.block_entities.get(target)
            if entity is not None:
                content = plain(actual_entity) if actual_entity is not None else {}
                intake.require(content.get('id') == plain(entity).get('id') and not content.get('Items') and not content.get('LootTable') and not content.get('Lock'), 'Capture container mismatch')
            else:
                intake.require(actual_entity is None, 'Unexpected capture block entity')
        captures.append({**intake.record(capture_path), 'rotation': rotation})
    return captures


def render(room_id):
    entry, review = context(room_id)
    work = review / 'renders' / stamp()
    fixture = review / 'fixture'
    for name in ['room.nbt', 'room.json']:
        copy_file(fixture / name, work / name)
    for filename in ['preview-server.mjs', 'viewer.html']:
        copy_file(fixture / 'tools' / filename, work / 'tools' / filename)
    capture = intake.io_path(fixture / 'tools/capture-preview.mjs').read_text(encoding='utf-8')
    capture = capture.replace("const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')", 'const root = process.cwd()')
    write(work / 'tools/capture-preview.mjs', capture)
    metadata = intake.load(work / 'room.json')
    width, height, length = metadata['size']
    thickness = {'zone-H02': 2, 'zone-C05': 3, 'zone-N03': 3, 'zone-C03': 2, 'zone-M03': 2, 'zone-N01': 2,
                 'zone-N04': 2, 'zone-P03': 2, 'zone-P04': 2, 'zone-R02': 2}.get(room_id, 1)
    ceiling = {'zone-H02': 13, 'zone-R03': 13, 'zone-C05': 11}.get(room_id, height - 2)
    viewer = (fixture / 'tools/viewer.mjs').read_text(encoding='utf-8')
    viewer = viewer.replace('[10.5, 3.8, 10.5]', f'[{width / 2}, {height * .35}, {length / 2}]')
    viewer = viewer.replace('across === 0', f'across < {thickness}').replace('depth === 0', f'depth < {thickness}')
    viewer = viewer.replace('across === 20', f'across >= {width - thickness}').replace('depth === 20', f'depth >= {length - thickness}')
    if room_id in {'zone-P03', 'zone-P04'}:
        viewer = viewer.replace('cutaway && height >= 2 &&', 'cutaway && height >= 3 &&')
    if room_id == 'zone-N03':
        viewer = viewer.replace('across < 3 || depth < 3', 'across < 3 || depth < 3 || across + depth < 10')
        viewer = viewer.replace('across >= 32 || depth >= 32', 'across >= 32 || depth >= 32 || 68 - across - depth < 10')
        viewer = viewer.replace('across < 3 || depth >= 32', 'across < 3 || depth >= 32 || across + 34 - depth < 10')
        viewer = viewer.replace('across >= 32 || depth < 3', 'across >= 32 || depth < 3 || 34 - across + depth < 10')
    viewer = viewer.replace('if (cutaway && height >= 9) continue', f"if (cutaway && height >= {ceiling} && !name.endsWith('_log') && !name.endsWith('_wood') && !name.includes('chain')) continue")
    viewer = viewer.replace('21 × 11 × 21', f'{width} × {height} × {length}').replace('A05 ·', room_id + ' ·')
    if room_id == 'zone-N03':
        additions = '''
  northeye: { position: [17.5, 2.62, 1.5], target: [17.5, 7.5, 17.5] },
  southeye: { position: [17.5, 2.62, 33.5], target: [17.5, 7.5, 17.5] },
  westeye: { position: [1.5, 14.62, 17.5], target: [17.5, 7.5, 17.5] },
  easteye: { position: [33.5, 14.62, 17.5], target: [17.5, 7.5, 17.5] },
'''
        viewer = viewer.replace('  hero:', additions + '  hero:')
        viewer = viewer.replace("direction === 'hero' || direction === 'reverse' ? 78 : 38", "direction.endsWith('eye') ? 85 : direction === 'hero' || direction === 'reverse' ? 78 : 38")
        capture = capture.replace("['hero', 'reverse']", "['hero', 'reverse', 'northeye', 'southeye', 'westeye', 'easteye']")
        intake.io_path(work / 'tools/capture-preview.mjs').write_text(capture, encoding='utf-8')
    viewer = viewer.replace('[-22, 29, -27]', f'[-{width * 1.2}, {height + 22}, -{length * 1.3}]')
    viewer = viewer.replace('[43, 29, 48]', f'[{width * 2.3}, {height + 22}, {length * 2.3}]')
    viewer = viewer.replace('[-28, 29, 43]', f'[-{width * 1.3}, {height + 22}, {length * 2.3}]')
    viewer = viewer.replace('[48, 29, -22]', f'[{width * 2.3}, {height + 22}, -{length * 1.3}]')
    write(work / 'tools/viewer.mjs', viewer)
    (work / 'qa').mkdir()
    port = 5761 + list(EXPECTED).index(room_id)
    with (work / 'server.log').open('wb') as stream:
        server = subprocess.Popen([str(NODE), str(work / 'tools/preview-server.mjs'), str(port)], cwd=work, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
        try:
            for attempt in range(120):
                try:
                    with urllib.request.urlopen(f'http://127.0.0.1:{port}/viewer.html', timeout=1) as response:
                        if response.status == 200:
                            break
                except OSError:
                    intake.require(server.poll() is None, 'Receiver preview server exited')
                    time.sleep(.25)
            with (work / 'capture.log').open('wb') as output:
                process = subprocess.run([str(NODE), str(work / 'tools/capture-preview.mjs'), f'http://127.0.0.1:{port}/'], cwd=work,
                                         stdout=output, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW, timeout=600)
            intake.require(process.returncode == 0, 'Receiver render failed: ' + str(work / 'capture.log'))
        finally:
            server.terminate()
            server.wait(timeout=20)
    images = []
    for kind in ['interior', 'exterior']:
        sheet = Image.new('RGB', (2000, 1320), '#101a26')
        for index, direction in enumerate(['north', 'south', 'west', 'east']):
            path = work / 'preview' / (direction + '-' + kind + '.png')
            images.append({**intake.record(path), 'direction': direction, 'kind': kind})
            with Image.open(path) as image:
                image.thumbnail((1000, 625))
                left, top = index % 2 * 1000, index // 2 * 660 + 30
                sheet.paste(image, (left, top))
                ImageDraw.Draw(sheet).text((left + 10, top - 24), room_id + ' / ' + direction + ' / ' + kind, fill='white')
        sheet.save(work / (kind + '-four-views.png'))
    manifest = {**identity(entry), 'status': 'RENDERED_NOT_VISUALLY_APPROVED', 'images': images, 'input_nbt': intake.record(work / 'room.nbt'),
                'renderer_inputs': [intake.record(path) for path in sorted((work / 'tools').rglob('*')) if path.is_file()],
                'render_report': intake.record(work / 'qa/render.json'), 'render_log': intake.record(work / 'capture.log'),
                'cutaway_ceiling_y': ceiling, 'cutaway_wall_thickness': thickness,
                'limitations': 'Fresh software renders from frozen NBT; cutaway display only, no client screenshot'}
    intake.save(work / 'manifest.json', manifest, exclusive=True)
    intake.save(review / 'renders/latest.json', {'manifest': intake.record(work / 'manifest.json')})
    print('RENDER_PASS', room_id, work, flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['prepare', 'native', 'render'])
    parser.add_argument('--rooms', nargs='+', default=list(EXPECTED))
    parser.add_argument('--keep-going', action='store_true')
    arguments = parser.parse_args()
    failures = []
    for room_id in arguments.rooms:
        try:
            globals()[arguments.action](room_id)
        except Exception as error:
            if not arguments.keep_going:
                raise
            failures.append({'id': room_id, 'action': arguments.action, 'error': str(error)})
            print('RECEIVER_FAILED', room_id, str(error), flush=True)
    if failures:
        intake.save(ROOT / 'batch-failures' / (stamp() + '.json'), failures, exclusive=True)
        raise SystemExit(1)


if __name__ == '__main__':
    main()
