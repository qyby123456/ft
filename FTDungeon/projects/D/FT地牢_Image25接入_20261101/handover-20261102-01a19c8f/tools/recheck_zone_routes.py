import argparse
import os
import re
import subprocess
from pathlib import Path
from xml.etree import ElementTree

import intake_zones as intake
import recheck_zone_batch as batch


CONFIGURATION = {
    'zone-H03': ('native-test', 'ftzoneh03qa', 'qa/forge-native.json', 240),
    'zone-N03': ('forge', 'ftzonen03qa', 'qa/forge-native.json', 120),
    'zone-P03': ('native-qa', 'ftp03qa', 'qa/native.json', 200),
    'zone-P04': ('', 'ftzonep04qa', 'qa/native.json', 240),
}


def run(room_id):
    entry, review = batch.context(room_id)
    subdirectory, namespace, report_path, original_ticks = CONFIGURATION[room_id]
    source = intake.DELIVERIES / room_id / subdirectory
    run_root = review / 'routes' / batch.stamp()
    inputs = []
    for name in ['room.nbt', 'room.json']:
        inputs.append(batch.copy_file(review / 'fixture' / name, run_root / name))
    inputs.append(batch.copy_file(batch.BASE / 'native-test/settings.gradle', run_root / 'forge/settings.gradle'))
    java_sources = sorted((source / 'src/main/java').rglob('*.java'))
    intake.require(java_sources, 'Missing category-specific native fixture')
    for original in java_sources:
        text = intake.io_path(original).read_text(encoding='utf-8')
        inputs.append(batch.copy_file(original, run_root / 'original-fixtures' / original.relative_to(source)))
        text = text.replace('runAfterDelay(' + str(original_ticks) + ',', 'runAfterDelay(400,')
        text = text.replace('"stabilization_ticks", ' + str(original_ticks), '"stabilization_ticks", 400')
        text = text.replace('"ticks_observed", ' + str(original_ticks), '"ticks_observed", 400')
        text = text.replace('ticks=' + str(original_ticks), 'ticks=400').replace('after ' + str(original_ticks) + ' ticks', 'after 400 ticks')
        text = re.sub(r'timeoutTicks = \d+', 'timeoutTicks = 2400', text)
        text = text.replace('GlobalTestReporter.', 'net.minecraft.gametest.framework.GlobalTestReporter.')
        if room_id == 'zone-H03':
            text = text.replace('root.getFileName().toString().equals("zone-H03") && Files.exists(root.resolve("native-test/build.gradle"))',
                                'root.toString().contains("routes") && Files.isRegularFile(root.resolve("forge/build.gradle"))')
        if '@Mod(' in text:
            constructor = 'public ' + original.stem + '() {}'
            replacement = 'public ' + original.stem + '() { try { net.minecraft.gametest.framework.GlobalTestReporter.replaceWith(new net.minecraft.gametest.framework.JUnitLikeTestReporter(Path.of(System.getProperty("' + namespace + '.root")).resolve("route-tests.xml").toFile())); } catch (Exception exception) { throw new IllegalStateException(exception); } }'
            replacement = replacement.replace('Path.of(', 'java.nio.file.Path.of(')
            intake.require(constructor in text, 'Unexpected fixture mod entry point')
            text = text.replace(constructor, replacement)
        intake.require('runAfterDelay(' + str(original_ticks) + ',' not in text, 'Category fixture stabilization not strengthened')
        batch.write(run_root / 'forge' / original.relative_to(source), text)
    resources = source / 'src/main/resources'
    for original in sorted(resources.rglob('*')):
        if original.is_file():
            inputs.append(batch.copy_file(original, run_root / 'forge' / original.relative_to(source)))
    if room_id == 'zone-P03':
        inputs.append(batch.copy_file(intake.DELIVERIES / room_id / 'qa/route.json', run_root / 'qa/route.json'))
    intake.io_path(run_root / 'qa').mkdir(parents=True, exist_ok=True)
    batch.write(run_root / 'forge/gradle.properties', 'org.gradle.daemon=false\norg.gradle.workers.max=1\norg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8\n')
    batch.write(run_root / 'forge/build.gradle', '''plugins { id 'net.minecraftforge.gradle' version '6.0.54' }
group = 'net.farmingtales.receiver'
version = '1.0'
java.toolchain.languageVersion = JavaLanguageVersion.of(17)
minecraft {
 mappings channel: 'official', version: '1.20.1'
 runs { gameTestServer {
  workingDirectory project.file('../server')
  property 'forge.enabledGameTestNamespaces', '%s'
  property '%s.root', projectDir.parentFile.absolutePath
  jvmArgs '-Xmx1408m', '-XX:ActiveProcessorCount=2'
  mods { %s { source sourceSets.main } }
 } }
}
repositories { mavenCentral() }
dependencies { minecraft 'net.minecraftforge:forge:1.20.1-47.4.10' }
tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }
''' % (namespace, namespace, namespace))
    batch.write(run_root / 'server/server.properties', 'server-port=0\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\n')
    launcher = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/lib/gradle-launcher-8.8.jar'))
    command = [str(batch.JAVA / 'bin/java.exe'), '-Dfile.encoding=UTF-8', '-cp', str(launcher), 'org.gradle.launcher.GradleMain',
               'runGameTestServer', '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false']
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(batch.JAVA)
    environment['PATH'] = str(batch.JAVA / 'bin') + os.pathsep + environment.get('PATH', '')
    intake.save(run_root / 'invocation.json', {**batch.identity(entry), 'inputs': inputs, 'command': command,
                                             'cwd': str(run_root / 'forge'), 'stable_ticks': 400, 'isolated_world': str(run_root / 'server/world'),
                                             'method': 'Frozen producer category fixture independently rerun by receiver with strengthened wait and JUnit reporter',
                                             'producer_sources_modified': False, 'production_files_accessed': False}, exclusive=True)
    print('START_ROUTE_RETEST', room_id, run_root, flush=True)
    with (run_root / 'console.log').open('wb') as stream:
        process = subprocess.run(command, cwd=run_root / 'forge', env=environment, stdout=stream, stderr=subprocess.STDOUT,
                                 creationflags=subprocess.CREATE_NO_WINDOW, timeout=900)
    intake.require(process.returncode == 0, 'Category route retest failed: ' + str(run_root / 'console.log'))
    xml = ElementTree.parse(run_root / 'route-tests.xml').getroot()
    intake.require(len(list(xml.iter('testcase'))) >= 1 and not any(list(xml.iter(name)) for name in ['failure', 'error', 'skipped']), 'Category JUnit did not pass')
    report = intake.load(run_root / report_path)
    intake.require(report['status'] == 'PASS' and len(report['rotations']) == 4, 'Category route report incomplete')
    intake.require(report.get('stabilization_ticks', report.get('ticks_observed')) == 400, 'Category route tick duration insufficient')
    intake.require(intake.digest(run_root / 'room.nbt') == entry['candidate_sha256'], 'Category route input changed')
    result = {**batch.identity(entry), 'status': 'PASS', 'stable_ticks': 400, 'rotations': [0, 90, 180, 270],
              'route_report': intake.record(run_root / report_path), 'test_results': intake.record(run_root / 'route-tests.xml'),
              'invocation': intake.record(run_root / 'invocation.json'), 'log': intake.record(run_root / 'console.log'),
              'limits': 'Native scripted geometric/player probes, not a real client keyboard or multiplayer playthrough'}
    intake.save(run_root / 'result.json', result, exclusive=True)
    intake.save(review / 'routes/latest.json', {'result': intake.record(run_root / 'result.json')})
    print('ROUTE_PASS', room_id, flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--rooms', nargs='+', default=list(CONFIGURATION))
    arguments = parser.parse_args()
    failures = []
    for room_id in arguments.rooms:
        try:
            run(room_id)
        except Exception as error:
            failures.append({'id': room_id, 'error': str(error)})
            print('ROUTE_FAILED', room_id, str(error), flush=True)
    if failures:
        intake.save(batch.ROOT / 'batch-failures' / (batch.stamp() + '.json'), failures, exclusive=True)
        raise SystemExit(1)


if __name__ == '__main__':
    main()
