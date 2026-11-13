import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
EXPECTED = 'bab12080dae0e8fb2290f73b6988f40af2094d932a3303cdabc12aaac250263f'


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    folder = ROOT / 'core/final-runtime-faults'
    ready_path = folder / 'ready.json'
    ready = load(ready_path)
    assert ready['status'] == 'PASS_ALL_SIX_INDEPENDENT_INSTALLED_FAULT_CASES'
    assert ready['jarSha256'] == EXPECTED and not ready['failedCases']
    assert len(ready['passedCases']) == 6
    checked = []
    for record in ready['files']:
        path = (folder / record['path']).resolve()
        assert path.is_relative_to(folder.resolve())
        assert digest(path) == record['sha256'], str(path)
        checked.append(record['path'])
    piston = load(folder / 'protection-result.json')
    assert piston['challengeChanged'] and piston['receiptEmpty']
    assert not piston['changedSourceOrNativeCells']
    assert piston['sourceCellsCompared'] == 127239
    result = dict(status='SIX_EXACT_INSTALLED_FAULTS_FROZEN_EVIDENCE_REVIEW_PASS',
                  jarSha256=EXPECTED, verifiedFiles=len(checked),
                  ready=dict(path=str(ready_path), sha256=digest(ready_path)),
                  passedCases=ready['passedCases'],
                  nativePistonSourceCellsUnchanged=127239,
                  limitations=['Strong process termination was tested; power loss was not',
                               'Native piston was triggered by an operator fixture in protected padding',
                               'Fault worlds use a copied producer baseline, but faults, commands and restarts were independently executed',
                               '76 saved source-state differences were retained: 68 native wall up-property changes and 8 native vine growth cells',
                               'Actual legal commit/restart remains a separate independent gate'],
                  completeGeneratorReady=False, productionPromotion=False)
    Path(__file__).with_name('final-faults-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(verifiedFiles=len(checked), passedCases=len(ready['passedCases']))))


if __name__ == '__main__':
    main()
