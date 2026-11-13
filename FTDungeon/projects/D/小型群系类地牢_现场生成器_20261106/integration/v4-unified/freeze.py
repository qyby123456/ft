import datetime
from pathlib import Path
import shutil
import sys

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate

HOME = Path(__file__).resolve().parent


def main():
    inputs = {'runtime': integrate.ROOT / 'runtime/next-unified', 'core': integrate.ROOT / 'core/next-planner-y', 'catalog': integrate.ROOT / 'catalog/acceptance-candidate-v4'}
    missing = [str(path / 'ready.json') for path in inputs.values() if not (path / 'ready.json').is_file()]
    if missing:
        integrate.save(HOME / 'progress.json', dict(status='WAITING_FOR_FROZEN_INPUTS_NO_WORLD_TEST_STARTED', missing=missing, inheritedWorldAcceptance=False, completeGeneratorReady=False))
        print('WAITING_FOR_FROZEN_INPUTS')
        return
    stamp = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    root = HOME / 'staging' / stamp
    lock = dict(createdAt=integrate.stamp(), productionPromotion=False, inheritedWorldAcceptance=False, parentAuthorization='ISOLATED_ACCEPTED_FALSE_CANDIDATES_ONLY', components={})
    for name, source in inputs.items():
        ready_hash = integrate.digest(source / 'ready.json')
        ready = integrate.load(source / 'ready.json')
        runtime_frozen = name == 'runtime' and ready.get('component') == 'runtime-next-unified' and ready.get('status') == 'REAL_UNIFIED_CANDIDATE_COMMAND_AND_INSTALLED_NINE_PAYLOAD_RESTART_PASS_ACCEPTANCE_PENDING'
        if ready.get('ready') is False or not (ready.get('ready') is True or runtime_frozen):
            raise integrate.GateError('Input not declared ready: ' + name)
        files = integrate.inventory(ready, allow_empty=name == 'catalog')
        if name == 'catalog':
            if integrate.digest(source / 'SHA256SUMS.json') != ready['inventorySha256']:
                raise integrate.GateError('Catalog inventory hash mismatch')
            files |= integrate.inventory(integrate.load(source / 'SHA256SUMS.json'))
            for extra in ('SHA256SUMS.json', 'release-gate-candidates.json', 'rotated-route-inputs.json', 'path-check-matrix.json'):
                files[extra] = integrate.digest(source / extra)
        for relative, expected in files.items():
            path = integrate.contained(source, relative)
            if integrate.digest(path) != expected:
                raise integrate.GateError('Input hash mismatch: ' + name + '/' + relative)
            target = integrate.contained(root / name, relative)
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, target)
            if integrate.digest(target) != expected:
                raise integrate.GateError('Snapshot copy mismatch')
        shutil.copyfile(source / 'ready.json', root / name / 'ready.json')
        if integrate.digest(source / 'ready.json') != ready_hash or integrate.digest(root / name / 'ready.json') != ready_hash:
            raise integrate.GateError('Ready changed while copying')
        for relative, expected in files.items():
            if integrate.digest(integrate.contained(source, relative)) != expected:
                raise integrate.GateError('Input changed while copying')
        lock['components'][name] = dict(source=str(source), readySha256=integrate.digest(source / 'ready.json'), files=files)
    gates = integrate.load(root / 'catalog/release-gate-candidates.json')
    if len(gates['templates']) != 9 or any(item['accepted'] for item in gates['templates']):
        raise integrate.GateError('Expected nine unaccepted candidate gates without promotion')
    integrate.save(root / 'snapshot-lock.json', lock)
    integrate.save(HOME / 'progress.json', dict(status='INPUTS_FROZEN_WORLD_ACCEPTANCE_NOT_RUN', snapshot=str(root), snapshotLockSha256=integrate.digest(root / 'snapshot-lock.json'), inheritedWorldAcceptance=False, completeGeneratorReady=False))
    print(root)


if __name__ == '__main__':
    main()
