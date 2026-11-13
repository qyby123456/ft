import argparse
import json
import os
import shutil
import subprocess
import sys
import time
from datetime import datetime, timedelta, timezone
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

from intake_corridors import INSTANCE, REQUIRED, ROOT, digest, load, picture, record, require, resolved, save, static_check


ID = 'corridor-M09'
EXPECTED_HASH = '5a776d5e6a96b0497aaf0411e289cebbc5d124a0b5b76b1e27897a0fd379abec'
EXPECTED_THREAD = '01a19f18-0384-7662-bacf-3ca3a6a38167'
SLOT = 49
RECORDS = 7380
NODE = Path('C:/Program Files/nodejs/node.exe')
JAVA = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')
ROTATIONS = {'NONE': 0, 'CLOCKWISE_90': 90, 'CLOCKWISE_180': 180, 'COUNTERCLOCKWISE_90': 270}


def current():
    registry = load(load(ROOT / 'latest.json')['registry']['path'])
    entry = next(value for value in registry['corridors'] if value['id'] == ID)
    require(entry['slot'] == SLOT and entry['producer_thread'] == EXPECTED_THREAD, 'Corridor reservation changed')
    require(entry['candidate_sha256'] == EXPECTED_HASH and entry['receiver_static']['status'] == 'PASS_INTAKE_ONLY', 'Unexpected candidate or static failure')
    directory = ROOT / 'reviews' / ID / entry['candidate_sha256'] / entry['bundle_sha256']
    alias = ROOT / 'work' / (ID.removeprefix('corridor-').lower() + '-' + entry['bundle_sha256'][:12])
    if alias.exists():
        require(resolved(alias) == resolved(directory), 'Short-path junction points to another review')
        directory = alias
    return entry, directory


def identity(entry):
    return {key: entry[key] for key in ('id', 'candidate_sha256', 'bundle_sha256')}


def copy_checked(source, target):
    original = record(source)
    require(resolved(target).is_relative_to(resolved(ROOT)), 'Copy outside review root')
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists():
        require(digest(target) == original['sha256'], 'Refusing to overwrite receiver evidence')
    else:
        shutil.copy2('\\\\?\\' + str(resolved(source)), '\\\\?\\' + str(resolved(target)))
    require(digest(source) == original['sha256'] == digest(target), 'Source changed during copy')
    return {**original, 'copy': record(target)}


def prepare():
    entry, directory = current()
    fixture = directory / 'fixture'
    require(not (directory / 'preparation.json').exists(), 'Prepared fixture already frozen; do not overwrite receiver work')
    snapshot = Path(entry['snapshot'])
    producer = Path(entry['delivery_directory'])
    copied = []
    for name in REQUIRED:
        require(digest(producer / name) == entry['files'][name]['sha256'], 'Delivery changed before receiver review')
        copied.append(copy_checked(snapshot / name, fixture / name))
    files = ['forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties']
    render_files = ['tools/common.mjs', 'tools/capture.mjs', 'tools/preview-server.mjs', 'tools/viewer/index.html', 'tools/viewer/main.mjs']
    render_source = producer if ID == 'corridor-M09' else producer.parent / 'corridor-M09'
    for name in render_files:
        copied.append(copy_checked(render_source / name, fixture / name))
    if ID == 'corridor-M09':
        files.append('qa/blueprint.json')
    files += [str(path.relative_to(producer)).replace('\\', '/') for path in sorted((producer / 'forge/src').rglob('*')) if path.is_file()]
    for name in files:
        copied.append(copy_checked(producer / name, fixture / name))
    (fixture / 'qa').mkdir(exist_ok=True)
    if ID == 'corridor-M09':
        blueprint = load(fixture / 'qa/blueprint.json')
        require(blueprint['size'] == [15, 12, 41] and blueprint['reference_sha256'] == entry['reference']['sha256'], 'Blueprint identity mismatch')
        feet = {(across, 2, depth) for across in range(6, 9) for depth in range(41)}
        clearance = {(across, elevation, depth) for across in range(6, 9) for elevation in range(2, 5) for depth in range(41)}
        require({tuple(value) for value in blueprint['required_feet']} == feet, 'Blueprint route incomplete')
        require({tuple(value) for value in blueprint['required_clearance']} == clearance, 'Blueprint clearance incomplete')
    save(directory / 'preparation.json', {**identity(entry), 'slot': SLOT, 'copied_files': copied,
        'producer_files_are_read_only': True, 'fixture_changes_required': 'Split all-rotation test into four isolated single-rotation GameTests with native JUnit reporter; never change assertions or structure'})
    print('PREPARED', fixture, flush=True)


def native():
    entry, directory = current()
    fixture = directory / 'fixture'
    require(digest(fixture / 'room.nbt') == EXPECTED_HASH, 'Fixture NBT changed')
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S-%f')
    run = directory / 'native' / stamp
    run.mkdir(parents=True, exist_ok=False)
    names = ['room.nbt', 'room.json'] + (['qa/blueprint.json'] if ID == 'corridor-M09' else [])
    for name in names:
        copy_checked(fixture / name, run / name)
    (run / 'qa').mkdir(exist_ok=True)
    shutil.copytree(fixture / 'forge', run / 'forge')
    run_directory = run / 'server'
    run_directory.mkdir()
    (run_directory / 'server.properties').write_text('server-port=0\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\n', encoding='utf-8')
    require(not (run_directory / 'world').exists(), 'Native world is not fresh')
    launcher = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/lib/gradle-launcher-8.8.jar'))
    command = [str(JAVA / 'bin/java.exe'), '-Dfile.encoding=UTF-8', '-cp', str(launcher), 'org.gradle.launcher.GradleMain',
               'runGameTestServer', '--offline', '--no-daemon', '--console=plain', '--max-workers=1',
               '-Dnet.minecraftforge.gradle.check.certs=false', '-PrunDirectory=' + str(run_directory)]
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(JAVA)
    environment['PATH'] = str(JAVA / 'bin') + os.pathsep + environment.get('PATH', '')
    inputs = [record(path) for path in sorted((run / 'forge').rglob('*')) if path.is_file()]
    save(run / 'invocation.json', {**identity(entry), 'command': command, 'cwd': str(run / 'forge'), 'fresh_world': str(run_directory / 'world'),
         'copied_fixture_files': inputs, 'player_saves_accessed': False, 'parallel_deliveries_modified': False})
    print('START_NATIVE', run, flush=True)
    with (run / 'console.log').open('wb') as stream:
        process = subprocess.run(command, cwd=run / 'forge', env=environment, stdout=stream, stderr=subprocess.STDOUT,
                                 creationflags=subprocess.CREATE_NO_WINDOW, timeout=900)
    console = (run / 'console.log').read_text(encoding='utf-8', errors='replace')
    require(process.returncode == 0 and 'BUILD SUCCESSFUL' in console, 'Native command failed; inspect ' + str(run / 'console.log'))
    reports = []
    sys.path.insert(0, str(INSTANCE / 'output/dungeon-expansion-20261101/tools'))
    from nbtio import Structure

    original = Structure.read(run / 'room.nbt')
    for rotation in ROTATIONS:
        report_path = run / 'qa' / ('forge-' + rotation.lower() + '.json')
        report_value = load(report_path)
        require(report_value['status'] == 'PASS' and report_value['source_nbt_sha256'] == EXPECTED_HASH, 'Native report failed')
        require(report_value['stabilization_ticks'] == 240 and report_value['requested_rotation'] == rotation, 'Wrong native stability/rotation')
        require(len(report_value['rotations']) == 1 and report_value['rotations'][0]['rotation'] == rotation, 'Wrong per-rotation evidence')
        require(report_value['records_checked_after_ticks'] == RECORDS, 'Incomplete native volume verification')
        capture = Structure.read(run / 'qa' / ('native-placed-' + rotation.lower() + '.nbt'))
        require(capture.size == original.size and capture.blocks == original.blocks and not capture.entities and not capture.block_entities, 'Native capture differs from final NBT')
        reports.append(record(report_path))
    xml_path = run / 'qa/native-tests.xml'
    xml = ElementTree.parse(xml_path).getroot()
    cases = list(xml.iter('testcase'))
    require(len(cases) == 4 and not any(list(xml.iter(tag)) for tag in ('failure', 'error', 'skipped')), 'Native XML must have four passing cases')
    require(len({value.get('name') for value in cases}) == 4, 'Native XML repeats a test case')
    require(digest(run / 'room.nbt') == EXPECTED_HASH, 'NBT changed during runtime')
    logs = [record(run / 'console.log')]
    if (run_directory / 'logs/latest.log').is_file():
        logs.append(record(run_directory / 'logs/latest.log'))
    result = {**identity(entry), 'status': 'PASS', 'exit_code': process.returncode, 'rotations': [0, 90, 180, 270],
              'stable_ticks': 240, 'isolated_world': True, 'world_directory': str(run_directory / 'world'), 'logs': logs,
              'test_results': [record(xml_path)], 'native_reports': reports, 'records_checked': 4 * RECORDS,
              'native_captures_match_frozen_nbt': True, 'invocation': record(run / 'invocation.json'),
              'limitations': 'Independent copied-fixture Forge rerun; not hall integration, occupancy refusal, idempotence, multiplayer, FPS or client gameplay'}
    save(run / 'result.json', result)
    save(directory / 'native/latest.json', {'result': record(run / 'result.json')})
    print('NATIVE_RECHECK_PASS', run / 'result.json', flush=True)


def render():
    entry, directory = current()
    fixture = directory / 'fixture'
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S-%f')
    work = directory / 'renders' / stamp
    work.mkdir(parents=True, exist_ok=False)
    copy_checked(fixture / 'room.nbt', work / 'room.nbt')
    shutil.copytree(fixture / 'tools', work / 'tools')
    (work / 'qa').mkdir()
    server_log = (work / 'server.log').open('wb')
    server = subprocess.Popen([str(NODE), str(work / 'tools/preview-server.mjs')], cwd=work,
                              stdout=server_log, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    try:
        for unused in range(120):
            if (work / 'qa/preview-server.json').exists():
                break
            require(server.poll() is None, 'Preview server failed; inspect ' + str(work / 'server.log'))
            time.sleep(0.5)
        require((work / 'qa/preview-server.json').is_file(), 'Preview server startup timed out')
        with (work / 'capture.log').open('wb') as stream:
            result = subprocess.run([str(NODE), str(work / 'tools/capture.mjs')], cwd=work, stdout=stream,
                                    stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW, timeout=300)
        require(result.returncode == 0, 'Receiver rendering failed; inspect ' + str(work / 'capture.log'))
    finally:
        server.terminate()
        try:
            server.wait(timeout=10)
        except subprocess.TimeoutExpired:
            server.kill()
            server.wait(timeout=10)
        server_log.close()
    rendered = load(work / 'qa/render.json')
    require(rendered['status'] == 'PASS' and rendered['source_nbt_sha256'] == EXPECTED_HASH and not rendered['browser_errors'], 'Render error or wrong source')
    images = []
    for kind in ('interior', 'exterior'):
        sheet = Image.new('RGB', (2048, 1404), '#10101a')
        drawing = ImageDraw.Draw(sheet)
        font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 23)
        for index, direction in enumerate(('north', 'south', 'west', 'east')):
            path = work / 'views' / f'{kind}-{direction}.png'
            picture(path, [1536, 1024])
            images.append({**record(path), 'direction': direction, 'kind': kind, 'cutaway': kind == 'interior'})
            with Image.open(path) as image:
                thumbnail = image.convert('RGB').resize((1024, 682), Image.Resampling.LANCZOS)
            offset_x, offset_y = index % 2 * 1024, index // 2 * 702
            sheet.paste(thumbnail, (offset_x, offset_y + 20))
            drawing.text((offset_x + 8, offset_y), f'{ID} / {direction} / {kind}', font=font, fill='#eee5d0')
        sheet.save(work / f'{kind}-four-views.png')
    manifest = {**identity(entry), 'status': 'RENDERED_NOT_VISUALLY_APPROVED', 'images': images,
                'input_nbt': record(work / 'room.nbt'), 'renderer_inputs': [record(path) for path in sorted((work / 'tools').rglob('*')) if path.is_file()],
                'render_report': record(work / 'qa/render.json'), 'render_log': record(work / 'capture.log'),
                'limitations': 'Fresh software renders from frozen NBT; display-only cutaways, no client screenshot or shader simulation'}
    save(work / 'manifest.json', manifest)
    save(directory / 'renders/latest.json', {'manifest': record(work / 'manifest.json'),
         'interior_sheet': record(work / 'interior-four-views.png'), 'exterior_sheet': record(work / 'exterior-four-views.png')})
    print('RENDERED', work, flush=True)


def main():
    global ID, EXPECTED_HASH, EXPECTED_THREAD, SLOT, RECORDS
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=('prepare', 'native', 'render'))
    parser.add_argument('--room', choices=('M09', 'S06'), default='M09')
    arguments = parser.parse_args()
    if arguments.room == 'S06':
        ID = 'corridor-S06'
        EXPECTED_HASH = 'e63afe337dfafa98704f68c643bfac7b19c3f97a78667aa224c7a195f95fc3f6'
        EXPECTED_THREAD = '01a19f19-75c0-7f81-8033-fa4a7344253e'
        SLOT, RECORDS = 66, 2080
    {'prepare': prepare, 'native': native, 'render': render}[arguments.action]()


if __name__ == '__main__':
    main()
