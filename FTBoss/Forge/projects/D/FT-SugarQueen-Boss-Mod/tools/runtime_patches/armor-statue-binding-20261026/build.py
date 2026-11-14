import importlib.util
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / '_work/armor-statue-binding-20261026'
spec = importlib.util.spec_from_file_location('support', ROOT / '_work/material-bow-combat-20261022/build.py')
support = importlib.util.module_from_spec(spec)
spec.loader.exec_module(support)
support.WORK = WORK


def main():
    WORK.mkdir(exist_ok=True)
    (WORK / 'empty').mkdir(exist_ok=True)
    expected = '7bf94c8cd1185265127fe45443af4c4f63fb33c93f8c857c8481ddeff36734e8'
    assert support.sha(support.TARGET) == expected, 'Installed baseline changed'
    baseline = WORK / 'baseline.jar'
    shutil.copy2(support.TARGET, baseline)
    sources = [ROOT / 'src/main/java/net/farmingtales/ftboss/item/FoodWeaponEvolution.java', Path(__file__)]
    hashes = {str(path): support.sha(path) for path in sources}
    asm = [support.LIBRARIES / f'org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar'
           for name in ['asm', 'asm-tree', 'asm-util', 'asm-analysis']]
    tools = WORK / 'tools'
    support.compile_java([Path(__file__).with_name('PatchArmorStatueBinding.java'),
                          ROOT / 'tools/verification/ZipStreamCheck.java',
                          ROOT / 'tools/verification/FoodWeaponBuildLinkCheck.java'], tools, asm, 'compile-tools')
    overlay = WORK / 'overlay'
    support.run([support.JDK / 'java.exe', '-cp', ';'.join(map(str, [tools, *asm])),
                 'PatchArmorStatueBinding', baseline, overlay], 'patch')
    candidate = WORK / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    support.run([support.JDK / 'jar.exe', '--update', '--file', candidate, '-C', overlay, '.'], 'package')
    changed = 'net/farmingtales/ftboss/item/FoodWeaponEvolution.class'
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        assert set(before.namelist()) == set(after.namelist()), 'Jar entries changed'
        assert [entry for entry in before.namelist() if before.read(entry) != after.read(entry)] == [changed]
        assert after.testzip() is None
    support.run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'zip-rules')
    support.run([support.JDK / 'java.exe', '-cp', tools, 'ZipStreamCheck', candidate], 'zip-stream')
    mapped = WORK / 'candidate-mapped.jar'
    support.run([support.JDK / 'java.exe', '-jar', support.FART, '--input', candidate, '--output', mapped,
                 '--map', support.MAPPINGS, '--lib', support.SRG_FORGE, '--threads', '2'], 'map-candidate')
    dependencies = [Path(value) for value in json.loads((ROOT / '_work/trident-ranged-parity-20261025/dependencies.json').read_text())]
    production = WORK / 'production'
    with zipfile.ZipFile(mapped) as archive:
        archive.extract(changed, production)
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp', ';'.join(map(str,
                 [mapped, tools, support.FORGE, *dependencies, *support.TARGET.parent.glob('*.jar'), *asm])),
                 'FoodWeaponBuildLinkCheck', production, mapped], 'verify-links')
    assert all(support.sha(Path(path)) == value for path, value in hashes.items()), 'Concurrent source change'
    assert support.sha(support.TARGET) == expected, 'Installed baseline changed'
    report = dict(passed=True, baselineSha256=expected, candidateSha256=support.sha(candidate),
                  changedEntries=[changed], sourceHashes=hashes, docsTouched=False, deployed=False)
    (WORK / 'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('PASS incremental armor binding fix; not deployed', flush=True)


if __name__ == '__main__':
    main()
