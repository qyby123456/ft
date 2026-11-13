import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
EXPECTED = 'bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f'


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify_seal(folder):
    ready = load(folder / 'ready.json')
    records = ready['files']
    if isinstance(records, int):
        inventory = folder / ready['inventory']
        assert digest(inventory) == ready['inventorySha256']
        records = load(inventory)
    if isinstance(records, list):
        entries = [(entry['path'], entry['sha256']) for entry in records]
    else:
        entries = [(name, value['sha256'] if isinstance(value, dict) else value)
                   for name, value in records.items()]
    for name, expected in entries:
        path = (folder / name).resolve()
        assert path.is_relative_to(folder.resolve())
        assert digest(path) == expected, str(path)
    return dict(path=str(folder / 'ready.json'), sha256=digest(folder / 'ready.json'),
                verifiedFiles=len(entries), status=ready['status'])


def main():
    boundary = ROOT / 'catalog/final-native-boundary'
    commands = ROOT / 'core/final-command-gates'
    boundary_seal = verify_seal(boundary)
    command_seal = verify_seal(commands)
    verdict = load(boundary / 'verdict.json')
    assert verdict['runtimeJarSha256'] == EXPECTED
    assert not verdict['concreteRemainingDeclaredGeometryBlockers']
    report = load(boundary / 'native-boundary-report.json')
    assert report['attempted'] == report['passed'] == len(report['checks']) == 352
    assert report['directed'] == 704
    assert report['singletonAtoms'] == 192 and report['openIntervalAtoms'] == 160
    assert all(entry['forward']['pass'] and entry['returned']['pass'] for entry in report['checks'])
    assert len(report['sceneChecks']) == 24
    assert all(list(entry.values()).count('PASS') == 1 for entry in report['sceneChecks'])
    assert report['contextChecks'] == 23784 and report['sourceShapeChecks'] == 370364
    negative = load(commands / 'receipt-negative-progress.json')
    assert len(negative['cases']) == 6
    assert all(case['exactReasonVerified'] and case['expectedReason'] in '\n'.join(case['response'])
               for case in negative['cases'].values())
    restart = load(commands / 'restart-progress.json')
    assert restart['status'] == 'PASS_ALL_FOUR_INSTALLED_SEPARATE_JVM_NATIVE_RESTARTS'
    assert len(restart['cases']) == 4
    assert all(case['canonicalWorldIdentityVerified'] for case in restart['cases'].values())
    assert load(commands / 'pause-io-progress.json')['status'] == 'PASS_NATIVE_INSTALLED_PAUSE_IO_EXCEPTION_CLEANUP'
    protection = load(ROOT / 'integration/v6-final/protection-progress.json')
    assert protection['jarSha256'] == EXPECTED and len(protection['cases']) == 6
    event_reports = []
    for case in protection['cases'].values():
        assert case['status'] == 'EXECUTED' and case['exitCode'] == 0
        path = Path(case['log']).parent / 'world/INDEPENDENT_NATIVE_REPORT.json'
        event = load(path)
        assert event['status'] == 'PASS_NATIVE_FORGE_EVENT_QUARANTINE_RELEASE'
        assert event['blockAndNativePayloadUnchanged']
        event_reports.append(dict(event=event['event'], path=str(path), sha256=digest(path)))
    assert {entry['event'] for entry in event_reports} == {'break', 'place', 'bucket', 'piston', 'fluid', 'explosion'}
    sidegate = load(ROOT / 'integration/v6-final/sidegate-progress.json')
    assert sidegate['jarSha256'] == EXPECTED and len(sidegate['cases']) == 4
    gate_reports = []
    for case in sidegate['cases'].values():
        assert case['status'] == 'EXECUTED' and case['exitCode'] == 0
        reference = case['INDEPENDENT_SIDEGATE_REPORT.json']
        path = Path(reference['path'])
        assert digest(path) == reference['sha256']
        gate = load(path)
        assert gate['status'] == 'PASS_DERIVED_SIDE_BRANCH_GEOMETRY_AND_MAIN_ARCH_INDEPENDENCE'
        assert all(gate[name] for name in ['openForward', 'openReturn', 'closedSideRouteBlocked',
                                           'closedSideDoorsDoNotSealMainArch', 'reopenedForwardReturn',
                                           'allLowerUpperStatesRestored'])
        gate_reports.append(reference)
    result = dict(status='WIDTH_COMMAND_RESTART_AND_PROTECTION_REPORT_REVIEW_PASS_COMMIT_PENDING',
                  jarSha256=EXPECTED, boundarySeal=boundary_seal, commandSeal=command_seal,
                  boundaryNativeChecks=352, boundaryDirections=704,
                  finiteCoverageTiles=4620, fullSceneChecks=24,
                  nativeContextChecks=23784, sourceShapeChecks=370364,
                  exactReceiptRejections=6, sameCanonicalPathRestarts=4,
                  protectionEventReports=event_reports,
                  fourOrientationSidegateReports=gate_reports,
                  scope='Full declared-width finite geometry plus native representative step/context evidence; no universal floating point branch theorem. Native event bus fixtures complement ordinary installed actual piston/fault cases.',
                  completeGeneratorReady=False, actualJobCOMMITTED=False,
                  productionPromotion=False)
    Path(__file__).with_name('final-gates-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(boundaryFiles=boundary_seal['verifiedFiles'], commandFiles=command_seal['verifiedFiles'],
                          nativeBoundaryPass=352, receiptRejects=6, restarts=4, protectionEvents=6)))


if __name__ == '__main__':
    main()
