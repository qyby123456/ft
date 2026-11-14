import importlib.util
import json
import shutil
import subprocess
import zipfile
from pathlib import Path

spec = importlib.util.spec_from_file_location('binding_build', Path(__file__).with_name('build.py'))
build = importlib.util.module_from_spec(spec)
spec.loader.exec_module(build)
ROOT, WORK, support = build.ROOT, build.WORK, build.support


def main():
    previous = ROOT / '_work/armor-native-bonds-20261026'
    client = previous / 'client-final'
    candidate = WORK / 'qys-bosses.jar'
    dependencies = [Path(value) for value in json.loads((ROOT / '_work/trident-ranged-parity-20261025/dependencies.json').read_text())]
    classes = WORK / 'probe-classes'
    support.compile_java([ROOT / 'tools/verification/ArmorStatueBindingGameProbe.java'], classes,
                         [WORK / 'candidate-mapped.jar', support.FORGE, *dependencies], 'compile-probe')
    named = WORK / 'probe-named.jar'
    remapped = WORK / 'probe-srg.jar'
    support.run([support.JDK / 'jar.exe', '--create', '--file', named, '-C', classes, '.'], 'pack-probe')
    support.run([support.JDK / 'java.exe', '-jar', support.FART, '--input', named, '--output', remapped,
                 '--map', support.MAPPINGS, '--reverse', '--lib', support.FORGE,
                 '--lib', WORK / 'candidate-mapped.jar', '--threads', '2'], 'remap-probe')
    overlay = WORK / 'probe-overlay'
    with zipfile.ZipFile(remapped) as archive:
        for entry in archive.namelist():
            if entry.endswith('.class'):
                archive.extract(entry, overlay)
    target = client / 'mods/qys-bosses.jar'
    assert client.resolve().is_relative_to((ROOT / '_work').resolve()), 'Probe escaped work directory'
    shutil.copy2(candidate, target)
    support.run([support.JDK / 'jar.exe', '--update', '--file', target, '-C', overlay, '.'], 'overlay-probe')
    arguments = (previous / 'launch-final.args').read_bytes().decode('gbk')
    arguments = '\n'.join(line for line in arguments.splitlines() if '-Dftboss.' not in line)
    arguments = '"-Dftboss.armorBindingProbe.root=' + WORK.as_posix() + '"\n' + arguments + '\n'
    launch = WORK / 'launch.args'
    launch.write_bytes(arguments.encode('gbk'))
    java = Path.home() / 'AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe'
    with (WORK / 'client-console.log').open('wb') as log:
        process = subprocess.Popen([str(java), '@' + str(launch)], cwd=client, stdout=log,
                                   stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    print('Isolated binding probe started PID ' + str(process.pid), flush=True)
    code = process.wait(timeout=900)
    print('Probe process exit ' + str(code), flush=True)
    report = json.loads((WORK / 'game-result.json').read_text(encoding='utf-8'))
    assert code == 0 and report['passed'], report
    assert report['checkedArmor'] == 1135 and report['freshUnboundArmor'] == 1125, report
    assert support.sha(candidate) == json.loads((WORK / 'build-report.json').read_text(encoding='utf-8'))['candidateSha256']
    print(json.dumps(report, ensure_ascii=True), flush=True)


if __name__ == '__main__':
    main()
