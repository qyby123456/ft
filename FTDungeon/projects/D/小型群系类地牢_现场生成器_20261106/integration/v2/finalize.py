from pathlib import Path
import re
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate

HOME = Path(__file__).resolve().parent


def main():
    progress = integrate.load(HOME / 'progress.json')
    frozen = Path(progress['snapshot'])
    run = Path(progress['run'])
    suite = []
    for name in ('payload', 'be-restart-prepare', 'be-restart-resume'):
        log = run / (name + '.log')
        if not log.exists():
            continue
        text = log.read_text(encoding='utf-8', errors='replace')
        passed = re.findall(r'All (\d+) required tests passed', text)
        markers = [line for line in text.splitlines() if line.startswith(('INDEPENDENT_V2_', 'PAYLOAD_V2_', 'ROLE_BE_V2_', 'BE_V2_RESTART_'))]
        suite.append(dict(name=name, requiredTestsPassed=int(passed[-1]) if passed else 0, assertions=markers, log=str(log), sha256=integrate.digest(log)))
    installed = integrate.load(HOME / 'installed-result.json') if (HOME / 'installed-result.json').exists() else dict(status='NOT_RUN')
    native = integrate.load(HOME / 'native-be-readback.json') if (HOME / 'native-be-readback.json').exists() else dict(status='NOT_RUN')
    seal = integrate.load(HOME / 'v1-seal.json')
    changed = [relative for relative, expected in seal['files'].items() if integrate.digest(integrate.contained(integrate.HOME, relative)) != expected]
    if integrate.digest(integrate.HOME / 'ready.json') != seal['readySha256']:
        changed.append('ready.json')
    sources_ok = all(item['requiredTestsPassed'] > 0 for item in suite) and len(suite) == 3 and any('INDEPENDENT_V2_PASS' in line for item in suite for line in item['assertions'])
    result = dict(component='integration-v2', apiVersion=1, status='SCOPED_CAPABILITY_PASS' if sources_ok and native['status'] == 'PASS' and installed['status'] == 'PASS_BASELINE_POOL_ONLY' and not changed else 'PARTIAL_OR_FAILED', completeGeneratorReady=False, productionPromotion=False, artifactSha256=progress['artifactSha256'], snapshot=str(frozen), snapshotLockSha256=integrate.digest(frozen / 'snapshot-lock.json'), suites=suite, independentNativeSavedBE=native, actualInstalledExperimental=installed, v1SealStatus='UNCHANGED' if not changed else 'CHANGED', v1ChangedFiles=changed, limitations=['Five original BE rooms remain unaccepted, test-only; installed JAR contains only s8_05/s8_06', 'Original Items/Patterns in current fixtures are empty; no rich nonempty-original-item/pattern preservation claim', 'Clean interrupted source-process BE recovery, not force kill/powerloss; installed original-five BE recovery NOT_RUN_NOT_PACKAGED', 'Full nine-room biome, lava/vines and protected player worlds not accepted'])
    issues = [dict(id='V2_TEST_ONLY_POOL', severity='BLOCKER_FOR_FULL_BIOME', status='OPEN', detail=result['limitations'][0]), dict(id='NONEMPTY_ORIGINAL_PAYLOAD_NOT_PRESENT', severity='COVERAGE_LIMIT', status='OPEN', detail=result['limitations'][1]), dict(id='INSTALLED_FIVE_ORIGINAL_BE_NOT_RUN', severity='COVERAGE_LIMIT', status='OPEN', detail=result['limitations'][2]), dict(id='V2_OVERLAY_ORDER', severity='FIXTURE_HISTORY', status='RESOLVED', detail='First attempt failed before startup: overlay referenced gameTest before build declared source set. Only integration overlay changed; original log retained under v2/run/20261106_134241/payload.log.'), dict(id='V2_INSTALLED_WORLD_MARKER', severity='FIXTURE_HISTORY', status='RESOLVED' if installed['status'] == 'PASS_BASELINE_POOL_ONLY' else 'OPEN', detail='First normal-server attempt correctly refused v1 world marker; integration fixture now uses explicit V2 marker and separate timestamp directory. No producer source changed.')]
    if not sources_ok or native['status'] != 'PASS' or installed['status'] != 'PASS_BASELINE_POOL_ONLY' or changed:
        issues.append(dict(id='INCOMPLETE_OR_FAILED_EXECUTION', severity='BLOCKER', detail=result))
    integrate.save(HOME / 'ISSUES.json', dict(updatedAt=integrate.stamp(), issues=issues))
    integrate.save(HOME / 'final-evidence.json', result)
    progress.update(status=result['status'], v1SealStatus=result['v1SealStatus'], nativeSavedBE=native['status'], actualInstalled=installed['status'], completeGeneratorReady=False, productionPromotion=False)
    integrate.save(HOME / 'progress.json', progress)
    report = ['# V2 independent capability review', '', 'Conclusion: ' + result['status'] + '. This is NOT acceptance of a complete biome generator.', '', '- Experimental JAR SHA256: ' + result['artifactSha256'], '- Snapshot: ' + str(frozen), '- Snapshot-lock SHA256: ' + result['snapshotLockSha256'], '- V1 seal: ' + result['v1SealStatus'], '', '## Source-based real Forge world checks']
    for item in suite:
        report += ['- ' + item['name'] + ': required passed=' + str(item['requiredTestsPassed']) + '; log=' + item['log'] + '; SHA256=' + item['sha256']] + ['  - ' + marker for marker in item['assertions']]
    report += ['', '## Independent saved native BE readback', '- Status: ' + native['status'], '- BE counts: ' + str(native.get('nativeTypes')), '- Raw fields compared: ' + str(native.get('rawFieldsCompared')), '- Compared directly against frozen original NBT, not merely regenerated runtime operation payloads. Region and journal hashes are in native-be-readback.json.', '', '## Actual installed experimental JAR', '- Status: ' + installed['status'], '- Actual template IDs used: ' + str(installed.get('actualUsedTemplates')), '- Independent saved interior blocks checked: ' + str(installed.get('savedRawInteriorCells')), '- Production pool is only s8_05/s8_06. Candidate BE rooms are not packaged; normal command generation of those five originals and installed-JAR BE restart remain NOT_RUN_NOT_PACKAGED.', '', '## Limits', *['- ' + item for item in result['limitations']], '', '## Reproduction', '- run_v2.py freezes both current readiness inventories into a new staging/run, then runs independent payload test alongside two owner fixtures and distinct-JVM BE prepare/resume.', '- install_experimental.py --snapshot <v2 staging snapshot> uses a new normal server directory and exact experimental JAR, copying only known Forge libraries, never reusing any v1 world.', '- read_native_be.py --snapshot <snapshot> --world <v2 run/be-restart-world/world> --mode complete --output <integration/v2/native-be-readback.json> compares saved native BE data independently.', '- finalize.py rechecks the complete sealed v1 inventory and emits precise readiness, limitations and file hashes.']
    (HOME / 'README.md').write_text('\n'.join(report) + '\n', encoding='utf-8')
    files = {}
    for path in HOME.rglob('*'):
        if not path.is_file() or path.name in ('ready.json',) or '__pycache__' in path.parts:
            continue
        relative = path.relative_to(HOME)
        if any(part in ('libraries', 'gradle-cache', '.gradle', 'assets', 'natives') for part in relative.parts):
            continue
        if path.suffix in ('.log', '.json', '.py', '.java', '.gradle', '.md', '.nbt', '.mca') or (path.suffix == '.jar' and ('mods' in relative.parts or 'libs' in relative.parts)):
            files[str(relative).replace('\\', '/')] = integrate.digest(path)
    integrate.save(HOME / 'ready.json', dict(component='integration-v2', apiVersion=1, ready=True, status='READY_SCOPED_REVIEW' if result['status'] == 'SCOPED_CAPABILITY_PASS' else 'PARTIAL_REVIEW', completeGeneratorReady=False, productionPromotion=False, files=files, artifactSha256=result['artifactSha256'], scope=result['status'], limitations=result['limitations']))
    print({key: result[key] for key in ('status', 'v1SealStatus', 'artifactSha256')})


if __name__ == '__main__':
    main()
