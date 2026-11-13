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
    progress = load(ROOT / 'integration/v6-final/combined-progress.json')
    assert progress['jarSha256'] == EXPECTED
    results = []
    for quarter_turns in range(4):
        case = progress['cases'][str(quarter_turns)]
        assert case['status'] == 'EXECUTED' and case['exitCode'] == 0
        world = Path(progress['root']) / f'q{quarter_turns}' / 'world'
        global_path = world / 'INDEPENDENT_GLOBAL_REPORT.json'
        native_path = world / 'INDEPENDENT_NATIVE_REPORT.json'
        global_report = load(global_path)
        native_report = load(native_path)
        assert global_report['attempted'] == global_report['passed'] == 148
        assert global_report['localChecksAttempted'] == global_report['localChecksPassed'] == 562
        assert global_report['bridgesPassed'] == len(global_report['bridges']) == 8
        assert not global_report['localFailures']
        assert all(bridge['outbound'] and bridge['returning'] for bridge in global_report['bridges'])
        assert all(target['outbound'] and target['returning'] for target in global_report['targets'])
        assert native_report['status'] == 'NATIVE_ENTRANCE_WALK_RAY_USE_RETURN_PASS'
        assert native_report['attempted'] == native_report['passed'] == 15
        reference = case['INDEPENDENT_NATIVE_REPORT.json']
        assert digest(native_path) == reference['sha256']
        results.append(dict(quarterTurns=quarter_turns, targetsReturned=148,
                            localRoutesPassed=562, bridgesReturned=8,
                            nativeUsesReturned=15,
                            globalReport=dict(path=str(global_path), sha256=digest(global_path)),
                            nativeUseReport=reference,
                            coordinateScope=global_report['coordinateScope'],
                            method=global_report['method']))
    result = dict(status='FOUR_ORIENTATION_REPORT_COUNTS_REVIEW_PASS_OTHER_GATES_PENDING',
                  jarSha256=EXPECTED, cases=results,
                  totals=dict(originalTargetReturns=592, localRoutes=2248,
                              corridorReturns=32, nativeReachUseReturns=60),
                  limitations=['World copies replayed in a source-compiled native Forge fixture; ordinary installed artifact run has separate evidence',
                               'Rotated block-center convention is explicit; final independent coordinate review remains required',
                               'Finite boundary proof, final fault gates and actual commit/restart are separate evidence'],
                  completeGeneratorReady=False, productionPromotion=False)
    Path(__file__).with_name('final-routes-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(result['totals']))


if __name__ == '__main__':
    main()
