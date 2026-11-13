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
    progress = load(ROOT / 'integration/v6-final/matrix-progress.json')
    assert progress['jarSha256'] == EXPECTED
    entry = progress['cases']['0']
    assert entry['status'] == 'EXECUTED' and entry['exitCode'] == 0
    references = {}
    for name in ('INDEPENDENT_NATIVE_REPORT.json', 'INDEPENDENT_NATIVE_SHAPES.json'):
        record = entry[name]
        path = Path(record['path'])
        assert digest(path) == record['sha256']
        references[name] = record
    report = load(Path(entry['INDEPENDENT_NATIVE_REPORT.json']['path']))
    assert report['attempted'] == report['passed'] == 2248
    assert report['isolatedScenes'] == 36 and report['originalChecksRequired'] == 2200
    assert report['sideGateChecksRequired'] == 48
    assert not report['failures'] and not report['escapedFluidOrFire']
    assert len(report['bands']) == 748
    assert all(band['forward'] and band['returned'] for band in report['bands'])
    assert report['allSourceLavaForceTicking'] and report['nativeElapsedTicks'] >= 240
    certificate = load(Path(__file__).with_name('continuous-recheck') / 'certificate.json')
    assert certificate['status'] == 'CONDITIONAL_FINITE_COVERAGE_PASS'
    assert len(certificate['results']) == 12
    assert all(not strip['failures'] for strip in certificate['results'])
    result = dict(status='INDEPENDENT_FINAL_MATRIX_REPORT_REVIEW_PASS_REMAINING_GATES_PENDING',
                  jarSha256=EXPECTED, references=references,
                  originalChecks=2200, additionalSideGateChecks=48, totalPassed=2248,
                  scenes=36, fullWidthNativeSampleLanesPassed=748,
                  continuousStaticStripsPassed=12,
                  continuousStaticTiles=sum(len(strip['tiles']) for strip in certificate['results']),
                  staticCertificateSha256=digest(Path(__file__).with_name('continuous-recheck') / 'certificate.json'),
                  completeGeneratorReady=False, productionPromotion=False,
                  remaining=['Installed all-orientation/global/native-use/save-readback evidence',
                             'Final exact-artifact independent faults and protection',
                             'Finite-boundary native motion and context-stability evidence',
                             'Explicit independently evidenced isolated commit and restart'])
    Path(__file__).with_name('final-matrix-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(totalPassed=2248, originalChecks=2200, lanesPassed=748,
                          stripsPassed=12, staticTiles=result['continuousStaticTiles'])))


if __name__ == '__main__':
    main()
