import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
EXPECTED = 'bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f'
GATES = {'ORIGINAL_HASH_METADATA', 'INSTALLED_EXACT_ARTIFACT', 'SAVED_NATIVE_READBACK_RESTART',
         'ALL_ORIGINAL_TARGETS_FOUR_ROTATIONS_RETURN', 'ACTUAL_GLOBAL_ENTRANCE_RETURN',
         'FULL_DECLARED_WIDTH_CLEARANCE', 'NATIVE_STEP_DOOR_CONTAINER_INTERACTIONS',
         'EXACT_SOURCE_FLUID_TICKS_RESTART', 'RUNTIME_PROTECTION_CONFLICT_RECOVERY',
         'NO_UNRESOLVED_BLOCKERS'}


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    folder = ROOT / 'integration/v6-final'
    decision_path = folder / 'acceptance-decision.json'
    decision = load(decision_path)
    assert decision['jarSha256'] == EXPECTED and not decision['unresolvedBlockers']
    assert set(decision['gates']) == GATES
    for entry in decision['gates'].values():
        assert entry['status'] == 'INDEPENDENT_PASS'
        assert digest(Path(entry['report'])) == entry['sha256']
    admission = load(folder / 'commit-io-progress.json')
    assert admission['status'] == 'PASS_INSTALLED_COMMIT_ADMISSION_IO_CLEANUP'
    assert admission['jarSha256'] == EXPECTED and admission['journalUnchanged']
    assert 'active=false tickets=0' in admission['delayedStatus']
    actual_path = folder / 'actual-commit-progress.json'
    actual = load(actual_path)
    assert actual['status'] == 'PASS_ACTUAL_EXPLICIT_ISOLATED_COMMITTED_AND_SAME_PATH_RESTART'
    assert actual['jarSha256'] == EXPECTED and actual['savedPhase'] == 'COMMITTED'
    assert actual['bindingStable'] and actual['receiptStable']
    assert all('phase=COMMITTED' in actual[name] and 'active=false tickets=0' in actual[name]
               for name in ['committedStatus', 'restartStatus'])
    assert len(actual['processes']) == 2 and all(process['exitCode'] == 0 for process in actual['processes'])
    world = Path(actual['world'])
    receipt_path = world / 'candidate-acceptance' / (actual['instanceId'] + '.json')
    receipt = load(receipt_path)
    assert set(receipt['gates']) == set(receipt['reports']) == GATES
    assert not receipt['unresolvedBlockers']
    assert receipt['binding']['jarSha256'] == EXPECTED
    assert receipt['binding']['instanceId'] == actual['instanceId']
    assert all(value == 'INDEPENDENT_PASS' for value in receipt['gates'].values())
    for entry in receipt['reports'].values():
        path = (world / entry['path']).resolve()
        assert path.is_relative_to(world.resolve())
        assert digest(path) == entry['sha256']
    result = dict(status='ACTUAL_INSTALLED_COMMIT_AND_SAME_PATH_RESTART_REVIEW_PASS',
                  jarSha256=EXPECTED, instanceId=actual['instanceId'],
                  worldCanonicalPath=str(world.resolve()),
                  acceptanceDecision=dict(path=str(decision_path), sha256=digest(decision_path)),
                  admissionFault=dict(path=str(folder / 'commit-io-progress.json'),
                                      sha256=digest(folder / 'commit-io-progress.json')),
                  actualCommit=dict(path=str(actual_path), sha256=digest(actual_path)),
                  receipt=dict(path=str(receipt_path), sha256=digest(receipt_path)),
                  savedPhase='COMMITTED', separateJvmRestart=True, actualJobCOMMITTED=True,
                  scope='Explicit isolated experimental job completion; candidate acceptance and production promotion remain separate',
                  productionPromotion=False)
    Path(__file__).with_name('commit-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(status=result['status'], savedPhase='COMMITTED', gates=len(GATES))))


if __name__ == '__main__':
    main()
