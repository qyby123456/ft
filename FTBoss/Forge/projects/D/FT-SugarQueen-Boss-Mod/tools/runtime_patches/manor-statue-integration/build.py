import importlib.util
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / '_work/manor-statue-integration-20261026'
spec = importlib.util.spec_from_file_location('support', ROOT / '_work/material-bow-combat-20261022/build.py')
support = importlib.util.module_from_spec(spec)
spec.loader.exec_module(support)
support.WORK = WORK
ENTRY = support.BASE + 'util/ManorWeaponCavityRules.class'


def main():
    WORK.mkdir(exist_ok=True)
    (WORK / 'empty').mkdir(exist_ok=True)
    baseline = WORK / 'baseline.jar'
    if not baseline.exists():
        shutil.copy2(support.TARGET, baseline)
    if support.sha(baseline) != support.sha(support.TARGET):
        raise RuntimeError('Installed runtime changed; rebase before packaging')
    sources = [ROOT / ('src/main/java/' + support.BASE + 'util/ManorWeaponCavityRules.java'),
               ROOT / 'tools/verification/ManorWeaponCavityRulesCheck.java',
               ROOT / 'tools/verification/ZipStreamCheck.java', Path(__file__).resolve()]
    hashes = {str(source): support.sha(source) for source in sources}
    support.compile_java(sources[:1], WORK / 'classes', [baseline], 'compile-rules')
    support.compile_java(sources[1:3], WORK / 'checks', [baseline], 'compile-checks')
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp',
                 ';'.join(map(str, [WORK / 'checks', WORK / 'classes', baseline])), 'ManorWeaponCavityRulesCheck'], 'verify-rules')
    overlay = WORK / 'overlay' / ENTRY
    overlay.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(WORK / 'classes' / ENTRY, overlay)
    candidate = WORK / 'qys-bosses.jar'
    shutil.copy2(baseline, candidate)
    support.run([support.JDK / 'jar.exe', '--update', '--file', candidate, '-C', WORK / 'overlay', '.'], 'package-runtime')
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        assert set(before.namelist()) == set(after.namelist())
        changed = [name for name in before.namelist() if before.read(name) != after.read(name)]
        assert changed == [ENTRY], changed
        assert after.testzip() is None
        assert not any('Probe' in name for name in after.namelist() if name not in before.namelist())
    support.run(['node', ROOT / 'tools/verification/check_jar_zip_rules.cjs', candidate], 'verify-zip-rules')
    support.run([support.JDK / 'java.exe', '-cp', WORK / 'checks', 'ZipStreamCheck', candidate], 'verify-zip-stream')
    support.run([support.JDK / 'java.exe', '-Xverify:all', '-cp',
                 ';'.join(map(str, [WORK / 'checks', candidate])), 'ManorWeaponCavityRulesCheck'], 'verify-packaged-rules')
    for source in sources:
        if support.sha(source) != hashes[str(source)]:
            raise RuntimeError('Source changed while building: ' + str(source))
    report = {'passed': True, 'baselineSha256': support.sha(baseline), 'candidate': str(candidate),
              'candidateSha256': support.sha(candidate), 'changed': changed, 'sources': hashes,
              'gameplayTested': False, 'docsTouched': False}
    (WORK / 'build-report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print('MANOR STATUE INTEGRATION BUILD PASS ' + support.sha(candidate), flush=True)


if __name__ == '__main__':
    main()
