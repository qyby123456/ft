import importlib.util
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / '_work/qingyi-balance-20261027'
SPEC = importlib.util.spec_from_file_location('support', ROOT / '_work/material-bow-combat-20261022/build.py')
support = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(support)
support.WORK = WORK
BASE = 'net/farmingtales/ftboss/'
COMPILED = ['util/QingyiCombatRules', 'boss/ai/QingyiDamageHalved', 'boss/ai/QingyiTridentRecovery']
CLASSES = [*COMPILED, 'entity/EasterBossEntity']


def main():
    WORK.mkdir(exist_ok=True)
    (WORK / 'empty').mkdir(exist_ok=True)
    baseline = WORK / 'baseline.jar'
    shutil.copy2(support.TARGET, baseline)
    baseline_hash = support.sha(baseline)
    sources = [ROOT / 'src/main/java' / (BASE + name + '.java') for name in CLASSES]
    sources += [Path(__file__), ROOT / 'tools/runtime_patches/QingyiBalancePatch.java',
                ROOT / 'tools/verification/check_qingyi_recovery.cjs',
                ROOT / 'tools/verification/check_qingyi_nerf.cjs']
    source_hashes = {str(path): support.sha(path) for path in sources}
    dependencies = [Path(value) for value in json.loads(
        (ROOT / '_work/trident-ranged-parity-20261025/dependencies.json').read_text())]
    mods = sorted(support.TARGET.parent.glob('*.jar'))
    asm = [support.LIBRARIES / f'org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar'
           for name in ['asm', 'asm-tree', 'asm-util', 'asm-analysis']]
    support.run(['node', ROOT / 'tools/verification/check_qingyi_recovery.cjs', WORK / 'fixture'], 'behavior-checks')
    named_baseline = WORK / 'baseline-mapped.jar'
    support.run([support.JDK / 'java.exe', '-Xmx2G', '-jar', support.FART, '--input', baseline,
                 '--output', named_baseline, '--map', support.MAPPINGS, '--lib', support.SRG_FORGE,
                 '--threads', '2'], 'map-baseline')
    classes = WORK / 'classes'
    support.compile_java(sources[:len(COMPILED)], classes,
                         [support.FORGE, named_baseline, *dependencies, *mods], 'compile-rules-and-recovery')
    tools = WORK / 'tools'
    support.compile_java([ROOT / 'tools/runtime_patches/QingyiBalancePatch.java',
                          ROOT / 'tools/verification/ZipStreamCheck.java',
                          ROOT / 'tools/verification/FoodWeaponBuildLinkCheck.java'],
                         tools, asm, 'compile-patch-and-checks')
    named = WORK / 'named.jar'
    support.run([support.JDK / 'jar.exe', '--create', '--file', named, '-C', classes, '.'], 'pack-classes')
    reobf = WORK / 'reobf.jar'
    support.run([support.JDK / 'java.exe', '-Xmx1G', '-jar', support.FART, '--input', named, '--output', reobf,
                 '--map', support.MAPPINGS, '--reverse', '--lib', support.FORGE, '--lib', named_baseline,
                 '--threads', '2'], 'reobfuscate')
    overlay = WORK / 'overlay'
    with zipfile.ZipFile(reobf) as compiled:
        for name in COMPILED:
            compiled.extract(BASE + name + '.class', overlay)
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp', ';'.join(map(str, [tools, *asm])),
                 'QingyiBalancePatch', baseline, overlay], 'patch-entity')
    expected = {BASE + name + '.class' for name in CLASSES}
    actual = {path.relative_to(overlay).as_posix() for path in overlay.rglob('*') if path.is_file()}
    assert actual == expected, 'Unexpected overlay files'
    candidate = WORK / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    support.run([support.JDK / 'jar.exe', '--update', '--file', candidate, '-C', overlay, '.'], 'package')
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        previous = set(before.namelist())
        current = set(after.namelist())
        assert previous <= current and len(after.namelist()) == len(current)
        changed = {entry for entry in previous if before.read(entry) != after.read(entry)}
        added = current - previous
        assert changed | added == expected, 'Out-of-scope JAR change'
        assert after.testzip() is None
    support.run(['node', ROOT / 'tools/verification/check_qingyi_nerf.cjs', candidate], 'damage-and-cooldown-checks')
    support.run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'zip-rules')
    support.run([support.JDK / 'java.exe', '-cp', tools, 'ZipStreamCheck', candidate], 'zip-stream')
    mapped = WORK / 'candidate-mapped.jar'
    support.run([support.JDK / 'java.exe', '-Xmx2G', '-jar', support.FART, '--input', candidate,
                 '--output', mapped, '--map', support.MAPPINGS, '--lib', support.SRG_FORGE,
                 '--threads', '2'], 'map-candidate')
    production = WORK / 'production'
    with zipfile.ZipFile(mapped) as archive:
        for name in CLASSES:
            archive.extract(BASE + name + '.class', production)
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp', ';'.join(map(str,
                 [tools, mapped, support.FORGE, *dependencies, *mods, *asm])),
                 'FoodWeaponBuildLinkCheck', production, mapped], 'verify-production-links')
    assert all(support.sha(Path(path)) == digest for path, digest in source_hashes.items()), 'Source changed during build'
    assert support.sha(support.TARGET) == baseline_hash, 'Runtime changed during build; rebuild on latest baseline'
    report = dict(passed=True, baselineSha256=baseline_hash, candidateSha256=support.sha(candidate),
                  changed=sorted(changed), added=sorted(added), preservedEntries=len(previous) - len(changed),
                  sources=source_hashes, outgoingDamageRelativeToPrevious=0.5, outgoingMultiplier=0.25,
                  tridentsPerHeal=5, healAmount=10, armorGrowth=False,
                  docsTouched=False, gameplayTested=False,
                  docsProposal='_work/docs-proposals/qingyi-trident-recovery.md')
    (WORK / 'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('PASS Qingyi balance candidate: ' + support.sha(candidate), flush=True)


if __name__ == '__main__':
    main()
