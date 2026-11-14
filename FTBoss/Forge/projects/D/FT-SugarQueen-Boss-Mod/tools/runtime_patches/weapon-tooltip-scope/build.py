import hashlib
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / '_work/weapon-hover-cache-20261025'
MINECRAFT = Path.home() / 'AppData/Roaming/.minecraft'
INSTANCE = MINECRAFT / 'versions/FarmingTales_ForgeⅡ v1.5.2'
JDK = MINECRAFT / 'runtime/java-runtime-gamma-snapshot/bin'
os.environ['JAVA_TOOL_OPTIONS'] = (os.environ.get('JAVA_TOOL_OPTIONS', '') + ' -Dfile.encoding=UTF-8').strip()


def sha(file):
    return hashlib.sha256(Path(file).read_bytes()).hexdigest()


def run(executable, *arguments):
    result = subprocess.run([str(executable), *map(str, arguments)], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output = result.stdout.decode('utf-8', errors='replace')
    print(output, end='', flush=True)
    with (WORK / 'verification.log').open('a', encoding='utf-8') as log:
        log.write(output)
    if result.returncode:
        raise RuntimeError('Command failed: ' + str(executable))


def arguments(name, values):
    file = WORK / name
    file.write_text('\n'.join('"' + str(value).replace('\\', '/') + '"' for value in values), encoding='utf-8')
    return '@' + str(file)


def classpath_jar(file, paths):
    line = 'Class-Path: ' + ' '.join(path.resolve().as_uri() + ('/' if path.is_dir() else '') for path in paths)
    manifest = 'Manifest-Version: 1.0\n' + line[:70] + '\n'
    manifest += ''.join(' ' + line[offset:offset + 69] + '\n' for offset in range(70, len(line), 69)) + '\n'
    with zipfile.ZipFile(file, 'w') as archive:
        archive.writestr('META-INF/MANIFEST.MF', manifest)


def main():
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    WORK.mkdir(parents=True, exist_ok=True)
    (WORK / 'verification.log').write_text('', encoding='utf-8')
    baseline = WORK / 'baseline.jar'
    target = INSTANCE / 'mods/qys-bosses.jar'
    if sha(target) != sha(baseline):
        raise RuntimeError('Baseline is not current; explicitly rebase before building')
    mapped = ROOT / 'build/fg_cache/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
    forge = Path.home() / '.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar'
    libraries = [file for file in (MINECRAFT / 'libraries').rglob('*.jar') if not any(marker in file.name for marker in ('natives', 'sources', 'javadoc'))]
    asm = [MINECRAFT / f'libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar' for name in ('asm', 'asm-tree', 'asm-analysis', 'asm-util', 'asm-commons')]
    extras = WORK / 'MixinExtras-0.5.5.jar'
    extras_mod = next(file for file in (INSTANCE / 'mods').glob('*.jar') if 'create_central_kitchen-' in file.name)
    with zipfile.ZipFile(extras_mod) as archive:
        with zipfile.ZipFile(io.BytesIO(archive.read('META-INF/jarjar/mixinextras-forge-0.5.5.jar'))) as platform:
            extras.write_bytes(platform.read('META-INF/jars/MixinExtras-0.5.5.jar'))
    source_root = ROOT / 'src/main/java/net/farmingtales/ftboss'
    sources = [source_root / 'item/WeaponTooltipReadScope.java', source_root / 'mixin/client/WeaponTooltipReadScopeMixin.java']
    tools = [ROOT / 'tools/runtime_patches/weapon-tooltip-scope/PatchTooltipReadScope.java',
             ROOT / 'tools/runtime_patches/zicai-curio-silence-20261018/RemapSilence.java',
             *[ROOT / 'tools/verification' / (name + '.java') for name in ('WeaponTooltipScopeJarCheck', 'FoodWeaponBuildLinkCheck', 'ZipStreamCheck')]]
    fixtures = sorted((ROOT / 'tools/verification/weapon-tooltip-scope-fixture').rglob('*.java'))
    test = ROOT / 'tools/verification/WeaponTooltipScopeCheck.java'
    tracked = [*sources, source_root / 'item/SwordCavityBuild.java', *tools, *fixtures, test, Path(__file__), ROOT / 'src/main/resources/ftboss.mixins.json',
               ROOT / 'tools/runtime_patches/weapon-tooltip-scope/deploy.ps1']
    hashes = {str(file): sha(file) for file in tracked}
    classes, payload, checks = [WORK / name for name in ('compiled', 'payload', 'checks')]
    fixture_classes, fixture_payload = [WORK / name for name in ('fixture-classes', 'fixture-payload')]
    for directory in (classes, payload, checks, fixture_classes, fixture_payload):
        directory.mkdir(parents=True, exist_ok=True)
    compile_cp = os.pathsep.join(map(str, [mapped, baseline, extras, *libraries]))
    run(JDK / 'javac.exe', arguments('compile.args', ['--release', '17', '-proc:none', '-encoding', 'UTF-8', '-implicit:none',
        '-sourcepath', WORK / 'empty-sourcepath', '-cp', compile_cp, '-d', classes, *sources]))
    srg = next((Path.home() / '.gradle/caches/modules-2/files-2.1/net.minecraftforge/srgutils/0.4.15').rglob('srgutils-0.4.15.jar'))
    tools_cp = os.pathsep.join(map(str, [checks, srg, *asm]))
    run(JDK / 'javac.exe', arguments('tools.args', ['--release', '17', '-proc:none', '-encoding', 'UTF-8', '-cp', tools_cp, '-d', checks, *tools]))
    run(JDK / 'java.exe', '-cp', tools_cp, 'RemapSilence', ROOT / 'build/createMcpToSrg/output.tsrg', classes, payload, mapped, classes)
    run(JDK / 'java.exe', '-cp', tools_cp, 'PatchTooltipReadScope', baseline, payload)
    run(JDK / 'javac.exe', arguments('fixtures.args', ['--release', '17', '-encoding', 'UTF-8', '-d', fixture_classes, *fixtures]))
    fixture_jar = WORK / 'fixture.jar'
    run(JDK / 'jar.exe', '--create', '--file', fixture_jar, '-C', fixture_classes, '.')
    run(JDK / 'java.exe', '-cp', tools_cp, 'PatchTooltipReadScope', fixture_jar, fixture_payload)
    fixture_cp = os.pathsep.join(map(str, [fixture_payload, fixture_classes, classes, checks, extras, *libraries]))
    run(JDK / 'javac.exe', arguments('scope-check.args', ['--release', '17', '-proc:none', '-encoding', 'UTF-8', '-cp', fixture_cp, '-d', checks, test]))
    fixture_runtime = WORK / 'fixture-classpath.jar'
    classpath_jar(fixture_runtime, [fixture_payload, fixture_classes, classes, checks, extras, *libraries])
    run(JDK / 'java.exe', '-Xverify:all', '-cp', fixture_runtime, 'WeaponTooltipScopeCheck')
    with zipfile.ZipFile(baseline) as archive:
        mixins = json.loads(archive.read('ftboss.mixins.json'))
    updated = json.loads(json.dumps(mixins))
    updated.setdefault('client', []).append('client.WeaponTooltipReadScopeMixin')
    declared = json.loads((ROOT / 'src/main/resources/ftboss.mixins.json').read_text(encoding='utf-8'))
    if declared.get('client') != updated.get('client'):
        raise AssertionError('Client mixin source config differs from the current baseline plus one client mixin')
    (payload / 'ftboss.mixins.json').write_text(json.dumps(updated, indent=2) + '\n', encoding='utf-8')
    entries = sorted(str(file.relative_to(payload)).replace('\\', '/') for file in payload.rglob('*') if file.is_file())
    expected = {'ftboss.mixins.json', 'net/farmingtales/ftboss/item/FourWeaponFoundationAdapter.class',
                'net/farmingtales/ftboss/item/SwordCavityBuild.class',
                'net/farmingtales/ftboss/item/WeaponTooltipReadScope.class', 'net/farmingtales/ftboss/mixin/client/WeaponTooltipReadScopeMixin.class'}
    if set(entries) != expected:
        raise AssertionError('Unexpected payload: ' + str(entries))
    candidate = WORK / 'qys-bosses.jar'
    candidate.write_bytes(baseline.read_bytes())
    run(JDK / 'jar.exe', '--update', '--file', candidate, *[value for entry in entries for value in ('-C', payload, entry)])
    run(JDK / 'java.exe', '-Xverify:all', '-cp', tools_cp, 'WeaponTooltipScopeJarCheck', baseline, candidate, forge)
    mods = [file for file in (INSTANCE / 'mods').glob('*.jar') if file.name != 'qys-bosses.jar']
    runtime = WORK / 'runtime-classpath.jar'
    classpath_jar(runtime, [candidate, checks, extras, forge, *libraries, *mods])
    run(JDK / 'java.exe', '-Xverify:all', '-cp', runtime, 'FoodWeaponBuildLinkCheck', payload, candidate)
    run(JDK / 'java.exe', '-cp', checks, 'ZipStreamCheck', candidate)
    run('node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate)
    if sha(target) != sha(baseline) or any(sha(file) != expected for file, expected in hashes.items()):
        raise RuntimeError('Source or installed baseline changed during validation')
    report = {'passed': True, 'baselineSha256': sha(baseline), 'candidateSha256': sha(candidate), 'candidate': str(candidate),
              'sources': hashes, 'entries': entries, 'gameplayTested': False,
              'docsImpact': '_work/docs-proposals/weapon-tooltip-read-scope.md'}
    (WORK / 'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('READY tooltip read scope: ' + report['candidateSha256'], flush=True)


if __name__ == '__main__':
    main()
