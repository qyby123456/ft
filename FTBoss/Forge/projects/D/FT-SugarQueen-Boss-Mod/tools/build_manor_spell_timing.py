import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parent.parent
WORK = Path(sys.argv[1]).resolve()
WORK.relative_to(ROOT / '_work')
WORK.mkdir(parents=True, exist_ok=True)
MINECRAFT = Path(os.environ['APPDATA']) / '.minecraft'
JDK = MINECRAFT / 'runtime/java-runtime-gamma-snapshot/bin'
GRADLE = Path.home() / '.gradle/caches/forge_gradle'
FORGE_ROOT = GRADLE / 'minecraft_user_repo/net/minecraftforge/forge'
FORGE = FORGE_ROOT / '1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar'
SRG = FORGE_ROOT / '1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar'
MAPPING = GRADLE / 'minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg'
RENAMER = GRADLE / 'maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar'
LIVE = MINECRAFT / 'versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar'
BASE = 'net/farmingtales/ftboss/'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command, label):
    command = list(map(str, command))
    if Path(command[0]).name == 'java.exe':
        arguments = WORK / (label + '.args')
        arguments.write_text('\n'.join('"' + value.replace('\\', '/') + '"' for value in command[1:]), encoding='gbk')
        command = [command[0], '@' + str(arguments)]
    result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8', errors='replace')
    (WORK / (label + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
    if result.returncode:
        raise RuntimeError(label + ': ' + result.stdout[-1800:] + result.stderr[-5500:])
    print(label + ' PASS', flush=True)


def compile_java(sources, output, classpath, label):
    output.mkdir(exist_ok=True)
    arguments = WORK / (label + '.args')
    options = ['--release', '17', '-encoding', 'UTF-8', '-proc:none', '-implicit:none', '-sourcepath', WORK / 'empty',
               '-classpath', os.pathsep.join(map(str, classpath)), '-d', output, *sources]
    arguments.write_text('\n'.join('"' + str(value).replace('\\', '/') + '"' for value in options), encoding='gbk')
    run([JDK / 'javac.exe', '@' + str(arguments)], label)


def main():
    (WORK / 'empty').mkdir(exist_ok=True)
    baseline = WORK / 'baseline.jar'
    if not baseline.exists():
        shutil.copy2(LIVE, baseline)
    baseline_hash = sha(baseline)
    if sha(LIVE) != baseline_hash:
        raise RuntimeError('Live baseline changed; use a fresh task directory')
    units = ['util/ManorSpellRules', 'util/ManorSpellCastRules', 'integ/spells/QizaiPlayerSpells',
             'integ/spells/ManorSignatureSpell', 'integ/spells/QizaiInfernoEffects']
    sources = [ROOT / ('src/main/java/' + BASE + unit + '.java') for unit in units]
    assets = ['assets/ftboss/lang/zh_cn.json', 'assets/ftboss/lang/en_us.json',
              'assets/ftboss/textures/gui/spell_icons/qizai_star_inferno.png']
    checks = [ROOT / ('tools/verification/' + name + '.java') for name in
              ['ManorSpellTimingCheck', 'ManorSpellArtifactCheck', 'ZipStreamCheck', 'FoodWeaponBuildLinkCheck', 'WeaponUiJarClosureCheck']]
    patcher = ROOT / 'tools/runtime_patches/ManorSpellTimingPatch.java'
    tracked = [*sources, *checks, patcher, Path(__file__).resolve(),
               *[ROOT / 'src/main/resources' / asset for asset in assets if not asset.endswith('.json')]]
    hashes = {str(path): sha(path) for path in tracked}
    language_entries = {}
    for asset in assets:
        if asset.endswith('.json'):
            language = json.loads((ROOT / 'src/main/resources' / asset).read_text(encoding='utf-8'))
            language_entries[asset] = {key: value for key, value in language.items()
                                      if key.startswith('spell.ftboss.qizai_star_inferno')}
    dependencies = sorted((MINECRAFT / 'libraries').rglob('*.jar')) + sorted((ROOT / 'libs').glob('*.jar'))
    mapped = WORK / 'baseline-mapped.jar'
    if not mapped.exists():
        run([JDK / 'java.exe', '-Xmx2G', '-jar', RENAMER, '--input', baseline, '--output', mapped,
             '--map', MAPPING, '--lib', SRG, '--threads', '2'], 'map-baseline')
    compile_java(sources, WORK / 'classes', [FORGE, mapped, *dependencies], 'compile-spells')
    named = WORK / 'named.jar'
    compiled = WORK / 'compiled-srg.jar'
    run([JDK / 'jar.exe', '--create', '--file', named, '-C', WORK / 'classes', '.'], 'pack-named')
    run([JDK / 'java.exe', '-Xmx2G', '-jar', RENAMER, '--input', named, '--output', compiled,
         '--map', MAPPING, '--reverse', '--lib', FORGE, '--lib', mapped, '--threads', '2'], 'remap-spells')
    asm = [MINECRAFT / f'libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar'
           for name in ['asm', 'asm-tree', 'asm-util', 'asm-commons', 'asm-analysis']]
    compile_java([patcher, *checks], WORK / 'tools', [WORK / 'classes', FORGE, mapped, *dependencies, *asm], 'compile-checks')
    tool_cp = os.pathsep.join(map(str, [WORK / 'tools', *asm]))
    overlay = WORK / 'overlay'
    overlay.mkdir(exist_ok=True)
    run([JDK / 'java.exe', '-Xverify:all', '-cp', tool_cp, 'ManorSpellTimingPatch', baseline, compiled, overlay], 'transplant-lifecycle')
    additions = []
    with zipfile.ZipFile(compiled) as jar:
        for entry in jar.namelist():
            if entry in [BASE + 'util/ManorSpellRules$Kind.class', BASE + 'integ/spells/QizaiPlayerSpells$Kind.class'] \
                    or entry.startswith(BASE + 'util/ManorSpellCastRules') \
                    or entry.startswith(BASE + 'integ/spells/QizaiInfernoEffects'):
                destination = overlay / entry
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes(jar.read(entry))
                additions.append(entry)
    with zipfile.ZipFile(baseline) as old:
        for asset in assets:
            destination = overlay / asset
            destination.parent.mkdir(parents=True, exist_ok=True)
            if asset.endswith('.json'):
                original = json.loads(old.read(asset))
                current = json.loads((ROOT / 'src/main/resources' / asset).read_text(encoding='utf-8'))
                original.update({key: value for key, value in current.items() if key.startswith('spell.ftboss.qizai_star_inferno')})
                destination.write_text(json.dumps(original, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
            else:
                shutil.copy2(ROOT / 'src/main/resources' / asset, destination)
    candidate = WORK / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    run([JDK / 'jar.exe', '--update', '--file', candidate, '-C', overlay, '.'], 'pack-candidate')
    allowed = {path.relative_to(overlay).as_posix() for path in overlay.rglob('*') if path.is_file()}
    with zipfile.ZipFile(baseline) as old, zipfile.ZipFile(candidate) as new:
        changed = {name for name in set(old.namelist()) | set(new.namelist())
                   if name not in old.namelist() or name not in new.namelist() or old.read(name) != new.read(name)}
        if changed - allowed - {name for name in changed if name.endswith('/')}:
            raise RuntimeError('Unexpected modified entries: ' + str(changed - allowed))
    runtime_cp = os.pathsep.join(map(str, [WORK / 'tools', candidate, SRG, *dependencies, *asm]))
    run([JDK / 'java.exe', '-Xverify:all', '-cp', runtime_cp, 'ManorSpellTimingCheck', WORK / 'spell-timing.csv'], 'verify-damage-order')
    run([JDK / 'java.exe', '-Xverify:all', '-cp', tool_cp, 'ManorSpellArtifactCheck', baseline, candidate], 'verify-preservation')
    changed_classes = [name[:-6] for name in allowed if name.endswith('.class')]
    run([JDK / 'java.exe', '-Xverify:all', '-cp', runtime_cp, 'FoodWeaponBuildLinkCheck', overlay, candidate], 'verify-links')
    run([JDK / 'java.exe', '-cp', tool_cp, 'WeaponUiJarClosureCheck', candidate, *changed_classes], 'verify-closure')
    run([JDK / 'java.exe', '-cp', WORK / 'tools', 'ZipStreamCheck', candidate], 'verify-zip-stream')
    run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'verify-zip-rules')
    for asset, entries in language_entries.items():
        current = json.loads((ROOT / 'src/main/resources' / asset).read_text(encoding='utf-8'))
        if any(current.get(key) != value for key, value in entries.items()):
            raise RuntimeError('Owned spell language entries changed: ' + asset)
    changed_sources = [path for path, expected in hashes.items() if sha(Path(path)) != expected]
    if sha(LIVE) != baseline_hash or changed_sources:
        raise RuntimeError('Baseline or source changed during verification: ' + str(changed_sources))
    report = dict(passed=True, baseline=str(baseline), baselineSha256=baseline_hash, candidate=str(candidate),
                  candidateSha256=sha(candidate), sources=hashes, languageEntries=language_entries, changedEntries=sorted(changed),
                  gameplayTested=False, neoForgeTouched=False, docsTouched=False)
    (WORK / 'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('PASS: candidate ' + str(candidate), flush=True)


if __name__ == '__main__':
    main()
