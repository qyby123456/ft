import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    with path.open('rb') as source:
        return hashlib.file_digest(source, 'sha256').hexdigest()


def main():
    seals = []
    for relative in ('core/next-planner-y', 'runtime/next-unified', 'integration/v4-unified',
                     'catalog/s8-07-doorway-review-v5', 'core/envelope-usability-review',
                     'core/v5-template-usability-review', 'catalog/full-width-certificate-v6',
                     'integration/v5-diagnostics', 'runtime/next-commit',
                     'core/commit-safety-readonly-review', 'core/commit-iofix-readonly-review',
                     'runtime/next-commit-iofix', 'catalog/continuous-native-review',
                     'core/final-runtime-faults', 'catalog/boundary-fixture-review-v6',
                     'runtime/final-delivery-doc-review'):
        base = ROOT / relative
        ready = load(base / 'ready.json')
        failures = []
        if 'files' in ready:
            files = ready['files']
        else:
            inventory = base / 'SHA256SUMS.json'
            if digest(inventory) != ready['inventorySha256']:
                raise ValueError('Inventory seal mismatch: ' + relative)
            files = load(inventory)['files']
        entries = files.items() if isinstance(files, dict) else ((item['path'], item['sha256']) for item in files)
        for name, expected in entries:
            path = (base / name).resolve()
            actual = digest(path) if path.is_relative_to(ROOT) and path.is_file() else None
            if actual != expected:
                failures.append(dict(path=name, expected=expected, actual=actual))
        seals.append(dict(component=relative, filesChecked=len(files),
                          readySha256=digest(base / 'ready.json'), failures=failures))
    evidence = load(ROOT / 'integration/v4-unified/final-evidence.json')
    reports = []

    def verify(record):
        if isinstance(record, list):
            for value in record:
                verify(value)
        elif isinstance(record, dict):
            if 'report' in record and 'reportSha256' in record:
                path = Path(record['report'])
                actual = digest(path) if path.is_file() else None
                reports.append(dict(path=str(path), matches=actual == record['reportSha256']))
            for value in record.values():
                verify(value)

    verify(evidence)
    installed = {}
    for orientation, case in evidence['actualInstalledCases'].items():
        installed[orientation] = {key: case[key] for key in
                                 ('savedCounts', 'independentLocal', 'independentGlobal',
                                  'phase', 'stage', 'restartValidations', 'savedObservedTicks')}
        for key in ('interactions', 'enclosureErrors', 'escapedFluidOrFire'):
            installed[orientation][key + 'Count'] = len(case[key])
    issues = load(ROOT / 'integration/v4-unified/ISSUES.json')
    blockers = []
    for issue in issues['blockers']:
        summary = {key: value for key, value in issue.items() if key != 'failures'}
        summary['recordedFailureCount'] = len(issue.get('failures', []))
        summary['firstFailure'] = issue.get('failures', [])[:1]
        blockers.append(summary)
    current = load(ROOT / 'runtime/next-commit-iofix/ready.json')
    result = dict(status='HASH_AUDIT_ONLY_NOT_GENERATOR_ACCEPTANCE', completeGeneratorReady=False,
                  productionPromotion=False, baselineJarSha256=evidence['jarSha256'],
                  currentCandidateJarSha256=current['artifactSha256'],
                  currentCandidateIndependentWorldAcceptance='PENDING_INTEGRATION_V6_FINAL', seals=seals,
                  reports=reports, allHashesMatch=all(not item['failures'] for item in seals)
                  and all(item['matches'] for item in reports), baselineInstalled=installed,
                  baselineIsolatedFullMatrix=evidence['isolatedFullMatrix'], baselineBlockers=blockers)
    output = Path(__file__).with_name('evidence-audit.json')
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(allHashesMatch=result['allHashesMatch'],
                          sealCounts={item['component']: item['filesChecked'] for item in seals},
                          linkedReports=len(reports), blockers=len(blockers))))
    if not result['allHashesMatch']:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
