import argparse
import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
HOME = Path.home()
GRADLE = HOME / '.gradle/caches/forge_gradle'
FORGE_ROOT = GRADLE / 'minecraft_user_repo/net/minecraftforge/forge'
FORGE = FORGE_ROOT / '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
MAPPINGS = GRADLE / 'minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg'
FART = GRADLE / 'maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar'
LIBRARIES = HOME / 'AppData/Roaming/.minecraft/libraries'
JDK = Path(os.environ.get('JAVA_HOME', str(HOME / 'AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot'))) / 'bin'
BASE = 'net/farmingtales/ftboss/client/render/bloom/'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--work', type=Path, required=True)
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--before-source', type=Path, required=True)
    arguments = parser.parse_args()
    work = arguments.work.resolve()
    work.mkdir(parents=True, exist_ok=True)
    if (work / 'baseline.jar').exists():
        raise RuntimeError('Use a new work directory for each baseline')
    baseline = work / 'baseline.jar'
    shutil.copy2(arguments.baseline, baseline)
    for directory in ['empty', 'tools', 'classes', 'before-classes', 'overlay']:
        (work / directory).mkdir()

    def run(command, label):
        result = subprocess.run([str(value) for value in command], cwd=ROOT, capture_output=True,
                                text=True, encoding='utf-8', errors='replace')
        (work / (label + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(label + ': ' + result.stdout[-1500:] + result.stderr[-3500:])
        print(label + ' PASS', flush=True)

    def compile_java(sources, destination, classpath, label):
        values = ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', work / 'empty',
                  '-classpath', ';'.join(map(str, classpath)), '-d', destination, *sources]
        argfile = work / (label + '.args')
        argfile.write_text('\n'.join('"' + str(value).replace('\\', '/') + '"' for value in values), encoding='utf-8')
        run([JDK / 'javac.exe', '-J-Dfile.encoding=UTF-8', '@' + str(argfile)], label)

    asm = [LIBRARIES / f'org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar'
           for name in ['asm', 'asm-tree', 'asm-commons', 'asm-util', 'asm-analysis']]
    tool_sources = [ROOT / 'tools/runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames.java',
                    ROOT / 'tools/runtime_patches/UiPerformancePatch.java', ROOT / 'tools/verification/ZipStreamCheck.java',
                    ROOT / 'tools/verification/WeaponUiJarClosureCheck.java']
    compile_java(tool_sources, work / 'tools', asm, 'compile-tools')
    tool_cp = ';'.join(map(str, [work / 'tools', *asm]))
    mapped = work / 'baseline-mapped.jar'
    run([JDK / 'java.exe', '-cp', tool_cp, 'RemapRuntimeNames', ROOT / 'build/createMcpToSrg/output.tsrg', baseline, mapped], 'map-baseline')
    dependencies = [LIBRARIES / 'net/minecraftforge/javafmllanguage/1.20.1-47.4.10/javafmllanguage-1.20.1-47.4.10.jar',
                    *sorted(LIBRARIES.rglob('*.jar')), *sorted((ROOT / 'libs').glob('*.jar'))]
    sources = [ROOT / 'src/main/java' / (BASE + name + '.java') for name in ['ShaderPackProbe', 'SpellBloomRenderer']]
    hashes = {str(source): sha(source) for source in [*sources, *tool_sources, Path(__file__)]}
    for label, source_paths, directory in [('before', [arguments.before_source], 'before-classes'), ('runtime', sources, 'classes')]:
        compile_java(source_paths, work / directory, [FORGE, mapped, *dependencies], 'compile-' + label)
        run([JDK / 'jar.exe', '--create', '--file', work / (label + '-named.jar'), '-C', work / directory, '.'], 'pack-' + label)
        run([JDK / 'java.exe', '-Xmx1G', '-jar', FART, '--input', work / (label + '-named.jar'),
             '--output', work / (label + '-srg.jar'), '--map', MAPPINGS, '--reverse',
             '--lib', FORGE, '--lib', mapped, '--threads', '2'], 'remap-' + label)
    run([JDK / 'java.exe', '-cp', tool_cp, 'UiPerformancePatch', baseline, work / 'runtime-srg.jar', work / 'overlay', work / 'before-srg.jar'], 'patch')
    candidate = work / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    run([JDK / 'jar.exe', '--update', '--file', candidate, '-C', work / 'overlay', '.'], 'package')
    run([JDK / 'java.exe', '-Xverify:all', '-cp', tool_cp, 'UiPerformancePatch', baseline, candidate], 'verify-preserved-methods')
    run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'zip-rules')
    run([JDK / 'java.exe', '-cp', work / 'tools', 'ZipStreamCheck', candidate], 'zip-stream')
    run([JDK / 'java.exe', '-cp', tool_cp, 'WeaponUiJarClosureCheck', candidate, BASE + 'ShaderPackProbe', BASE + 'SpellBloomRenderer'], 'dependency-closure')
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        assert after.testzip() is None
        changed = sorted(name for name in before.namelist() if before.read(name) != after.read(name))
        added = sorted(set(after.namelist()) - set(before.namelist()))
        assert changed == [BASE + 'SpellBloomRenderer.class'], changed
        assert added == [BASE + 'ShaderPackProbe$DefaultProbe.class', BASE + 'ShaderPackProbe.class'], added
    assert all(sha(Path(source)) == digest for source, digest in hashes.items()), 'Sources changed during build'
    report = dict(passed=True, baselineSha256=sha(baseline), candidateSha256=sha(candidate), sourceHashes=hashes,
                  changedEntries=changed, addedEntries=added, gameplayTested=False, docsTouched=False)
    (work / 'build-report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print('BUILD PASS ' + report['candidateSha256'], flush=True)


if __name__ == '__main__':
    main()
