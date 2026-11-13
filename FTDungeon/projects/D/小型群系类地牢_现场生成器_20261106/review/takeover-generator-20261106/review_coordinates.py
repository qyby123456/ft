import hashlib
import json
import math
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def rotate(horizontal, quarter_turns):
    source_x, source_z = horizontal
    return ((source_x, source_z), (-source_z, source_x),
            (-source_x, -source_z), (source_z, -source_x))[quarter_turns]


def vector(value):
    return [value.get(axis, value.get(obfuscated)) for axis, obfuscated in
            [('x', 'f_82479_'), ('y', 'f_82480_'), ('z', 'f_82481_')]]


def main():
    installed = load(ROOT / 'integration/v6-final/installed-progress.json')
    manifest_path = ROOT / 'runtime/next-commit-iofix/src/main/resources/data/ftbd_candidate/path-matrix.json'
    checks = load(manifest_path)['checks']
    by_case = {}
    for check in checks:
        by_case.setdefault(check['caseId'], []).append(check)
    endpoint_count = 0
    references = []
    for ordinal in range(4):
        world = Path(installed['root']) / f'q{ordinal}' / 'world'
        routes_path, = (world / 'ft_biome_dungeon_candidate_v5/minecraft/overworld').glob('*-routes.json')
        report = load(routes_path)
        assert report['attempted'] == report['passed'] == len(report['checks']) == 562
        for observed in report['checks']:
            case_id = observed['caseId']
            template_id, rotation = case_id.rsplit('_r', 1)
            quarter_turns = int(rotation)
            index = observed['checkIndexWithinCase']
            declared = by_case[case_id][index]
            assert observed['sourceFrozenCheck'] == declared
            original = by_case[template_id + '_r0'][index]
            origin = observed['actualOrigin']
            offset_x, offset_z = rotate((0.5, 0.5), quarter_turns)
            for source_name, native_name in [('start', 'nativeStart'), ('end', 'nativeEnd')]:
                source = original[source_name]
                rotated_x, rotated_z = rotate((source[0], source[2]), quarter_turns)
                assert declared[source_name] == [rotated_x, source[1], rotated_z]
                expected = [origin['x'] + rotated_x + offset_x,
                            origin['y'] + source[1] - 96,
                            origin['z'] + rotated_z + offset_z]
                assert all(math.isclose(actual, target, abs_tol=1e-9) for actual, target in
                           zip(vector(observed[native_name]), expected))
                endpoint_count += 1
        references.append(dict(path=str(routes_path), sha256=hashlib.sha256(routes_path.read_bytes()).hexdigest()))
    result = dict(status='ROTATED_BLOCK_CENTER_CONVENTION_INDEPENDENT_ARITHMETIC_REVIEW_PASS',
                  endpointCount=endpoint_count, routeChecks=2248, references=references,
                  derivation='Continuous point O+R(q)*(x+0.5,z+0.5); feetY=O.y+sourceY-96. Discrete cell placement uses the corresponding -1 correction, whose cell center equals this point. Fractional source coordinates are preserved, not rounded.',
                  scope='Explicit source block-center coordinate convention, not a claim that raw block-min corner coordinates were physically replayed',
                  companionNativeEvidence='final-routes-review.json',
                  completeGeneratorReady=False, productionPromotion=False)
    Path(__file__).with_name('coordinate-review.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(dict(endpointCount=endpoint_count, routeChecks=2248)))


if __name__ == '__main__':
    main()
