import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
HOME = Path.home()
LIBRARIES = HOME / 'AppData/Roaming/.minecraft/libraries'
GRADLE = HOME / '.gradle/caches/forge_gradle'
FORGE = GRADLE / 'minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
JDK = HOME / 'AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin'
BASE = 'net/farmingtales/ftboss/item/'
CLASSES = ['EquipmentWeaponBuild', 'SwordCavityBuild']


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    parser = argparse.ArgumentParser()
    parser.add_argument('--work', type=Path, required=True)
    arguments = parser.parse_args()
    work = arguments.work.resolve()
    work.relative_to(ROOT / '_work')
    baseline = work / 'baseline.jar'
    if not baseline.is_file():
        raise RuntimeError('A pinned baseline and before-source snapshots are required')
    for name in ['empty', 'tools', 'before-classes', 'classes', 'before-srg', 'after-srg', 'overlay']:
        (work / name).mkdir(exist_ok=True)

    def run(command, label):
        if Path(command[0]).name == 'java.exe':
            command = [command[0], '-Dfile.encoding=UTF-8', *command[1:]]
        result = subprocess.run([str(value) for value in command], cwd=ROOT, capture_output=True,
                                text=True, encoding='utf-8', errors='replace')
        (work / (label + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(label + ': ' + result.stdout[-2500:] + result.stderr[-3500:])
        print(label + ' PASS ' + result.stdout[-700:].strip(), flush=True)
        return result.stdout

    def compile_java(sources, destination, classpath, label):
        values = ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', work / 'empty',
                  '-classpath', ';'.join(map(str, classpath)), '-d', destination, *sources]
        argfile = work / (label + '.args')
        argfile.write_text('\n'.join('"' + str(value).replace('\\', '/') + '"' for value in values), encoding='utf-8')
        run([JDK / 'javac.exe', '-J-Dfile.encoding=UTF-8', '@' + str(argfile)], label)

    asm = [LIBRARIES / f'org/ow2/asm/{name}/9.8/{name}-9.8.jar'
           for name in ['asm', 'asm-tree', 'asm-commons', 'asm-util', 'asm-analysis']]
    srg = next((HOME / '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15').rglob('*.jar'))
    tools = [ROOT / 'tools/runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames.java',
             ROOT / 'tools/runtime_patches/zicai-curio-silence-20261018/RemapSilence.java',
             ROOT / 'tools/runtime_patches/EquipmentReadHotPathPatch.java',
             ROOT / 'tools/verification/ZipStreamCheck.java']
    compile_java(tools, work / 'tools', [*asm, srg], 'compile-tools')
    tool_cp = ';'.join(map(str, [work / 'tools', *asm, srg]))
    mapped = work / 'baseline-mapped.jar'
    if not mapped.exists():
        run([JDK / 'java.exe', '-cp', tool_cp, 'RemapRuntimeNames', ROOT / 'build/createMcpToSrg/output.tsrg', baseline, mapped], 'map-baseline')
    dependencies = [FORGE, mapped, *sorted(LIBRARIES.rglob('*.jar')), *sorted((ROOT / 'libs').glob('*.jar'))]
    sources = [ROOT / 'src/main/java' / (BASE + name + '.java') for name in CLASSES]
    hashes = {str(path): digest(path) for path in [Path(__file__), *tools, *sources, ROOT / 'tools/verification/EquipmentReadHotPathCheck.java']}
    compile_java([work / 'before-source' / (name + '.java') for name in CLASSES], work / 'before-classes', dependencies, 'compile-before')
    compile_java(sources, work / 'classes', dependencies, 'compile-after')
    for source, output in [('before-classes', 'before-srg'), ('classes', 'after-srg')]:
        run([JDK / 'java.exe', '-cp', tool_cp, 'RemapSilence', ROOT / 'build/createMcpToSrg/output.tsrg',
             work / source, work / output, FORGE, work / source], 'remap-' + source)
    run([JDK / 'java.exe', '-Xverify:all', '-cp', tool_cp, 'EquipmentReadHotPathPatch', 'patch', baseline,
         work / 'before-srg', work / 'after-srg', work / 'overlay'], 'patch-methods')
    candidate = work / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    run([JDK / 'jar.exe', '--update', '--file', candidate, '-C', work / 'overlay', '.'], 'package')
    run([JDK / 'java.exe', '-Xverify:all', '-cp', tool_cp, 'EquipmentReadHotPathPatch', 'verify', baseline, candidate], 'verify-runtime-preservation')
    compile_java([ROOT / 'tools/verification/EquipmentReadHotPathCheck.java'], work / 'tools', [*asm, candidate], 'compile-checks')
    behavior = run([JDK / 'java.exe', '-Xverify:all', '-cp', tool_cp + ';' + str(candidate), 'EquipmentReadHotPathCheck', baseline, candidate], 'differential-behavior')
    run([JDK / 'java.exe', '-cp', work / 'tools', 'ZipStreamCheck', candidate], 'zip-stream')
    run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'zip-rules')
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        if after.testzip() is not None:
            raise AssertionError('ZIP CRC failure')
        if set(before.namelist()) != set(after.namelist()):
            raise AssertionError('Unexpected added or removed JAR entry')
        changed = sorted(name for name in before.namelist() if before.read(name) != after.read(name))
        if changed != sorted(BASE + name + '.class' for name in CLASSES):
            raise AssertionError('Unexpected JAR delta: ' + str(changed))
    if not all(digest(Path(path)) == expected for path, expected in hashes.items()):
        raise RuntimeError('Inputs changed during the build')
    report = dict(passed=True, baselineSha256=digest(baseline), candidateSha256=digest(candidate),
                  sourceHashes=hashes, changedEntries=changed, addedEntries=[], gameplayTested=False,
                  docsTouched=False, contractsChanged=False, behavior=behavior)
    (work / 'build-report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print('BUILD PASS ' + report['candidateSha256'], flush=True)


if __name__ == '__main__':
    main()
