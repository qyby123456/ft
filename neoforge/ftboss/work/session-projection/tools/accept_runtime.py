import hashlib
import json
from pathlib import Path


WORK = Path(__file__).resolve().parents[1]


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def inspect(receipt, active):
    report = load(receipt)
    attempt = Path(report['attempt'])
    terminal = load(attempt / 'terminal308.json')
    errors = []
    if terminal['exitCode'] != 0 or not terminal['stopSent'] or not terminal['completeSave']:
        errors.append('Native terminal or complete save failed')
    if report['probe']['failures'] != 0:
        errors.append('Fixture reported a failure')
    cases = report['probe']['cases']
    summaries = []
    expected = {0: 3, 5: 1, 6: 1, 7: 1, 8: 1} if active else {0: 3}
    actual = {}
    for row in cases:
        sample = row['sample']
        actual[sample] = actual.get(sample, 0) + 1
        traces = [publication.get('traceComplete') for publication in row.get('publications', [])]
        clean_read = row.get('playerDomainRetiredBeforeRead') == row.get('playerDomainRetiredAfterRead')
        positive = sample in (0, 3, 4)
        if not clean_read:
            errors.append(f'Observation retired domain: {sample}/{row.get("hit")}')
        if positive and (row.get('attackReturned') is not True or not traces or any(value is not True for value in traces)
                         or row.get('playerDomainRetiredAfterRead') is not False
                         or row.get('inputExpectedMatchesWornItem') is not True):
            errors.append(f'Positive native continuity failed: {sample}/{row.get("hit")}')
        if active and sample >= 5 and row.get('activeFaultRejected') is not True:
            errors.append(f'Active scope fault not rejected: {sample}')
        if positive:
            events = row.get('damageEvents', [])
            if {event.get('phase') for event in events} != {'beforeMitigation', 'beforeHealth'}:
                errors.append(f'Missing native read phases: {sample}/{row.get("hit")}')
            for event in events:
                if (event.get('root_retired') is not False or event.get('cutClosed') is not False
                        or event.get('acceptedImagePresent') is not True or event.get('activeRoots') != 1
                        or event.get('bindingSourceIdentity') is not True or event.get('bindingTargetIdentity') is not True):
                    errors.append(f'Native read projection was not live: {sample}/{row.get("hit")}')
        summaries.append({'sample': sample, 'hit': row.get('hit'), 'traceComplete': traces,
                          'observationClean': clean_read, 'domainRetired': row.get('playerDomainRetiredAfterRead'),
                          'faultInjected': row.get('activeFaultInjected'), 'faultRejected': row.get('activeFaultRejected'),
                          'originalExceptionIdentity': row.get('originalInjectedExceptionIdentity')})
    if actual != expected:
        errors.append(f'Unexpected cases: {actual}, expected {expected}')
    hashes = {str(receipt): hashlib.sha256(receipt.read_bytes()).hexdigest(),
              str(attempt / 'terminal308.json'): hashlib.sha256((attempt / 'terminal308.json').read_bytes()).hexdigest()}
    if active:
        debugger = load(attempt / 'debugger-terminal.json')
        if debugger['exitCode'] != 0:
            errors.append('Debugger did not terminate normally')
        log = (attempt / 'debugger.log').read_text(encoding='utf-8')
        for mode in ['wrong_key', 'wrong_receiver', 'alias_round_trip', 'exception']:
            if f'Injected {mode} with coverageInstalled=true, traceRevokedBefore=false' not in log:
                errors.append(f'Missing covered live native injection: {mode}')
        hashes[str(attempt / 'debugger.log')] = hashlib.sha256((attempt / 'debugger.log').read_bytes()).hexdigest()
    return {'attempt': str(attempt), 'failures': len(errors), 'errors': errors, 'cases': summaries, 'inputHashes': hashes}


results = {name: inspect(WORK / f'reports/{name}.json', name.startswith('active')) for name in ['positive-saved', 'active-saved']}
output = {'failures': sum(result['failures'] for result in results.values()), 'results': results,
          'activeCoveredNegativesAccepted': all(result['failures'] == 0 for result in results.values()),
          'fullSignedSessionAccepted': False, 'authenticatedMultiplayerAccepted': False,
          'installable': False, 'deployed': False}
(WORK / 'reports/accepted-runtime.json').write_text(json.dumps(output, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(output, ensure_ascii=False, indent=2))
assert output['failures'] == 0
