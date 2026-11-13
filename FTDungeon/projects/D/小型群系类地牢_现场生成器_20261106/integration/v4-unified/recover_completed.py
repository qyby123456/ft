import datetime
import os
from pathlib import Path
import shutil
import sys
import time

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
from installed import OwnedServer
import readback

HOME = Path(__file__).resolve().parent


def main():
    installed = integrate.load(HOME / 'installed-progress.json')
    source = Path(installed['root']) / 'q0'
    prior = integrate.load(source / 'result.json')
    if prior['status'] == 'FAIL':
        raise integrate.GateError('Completed original source case failed')
    snapshot = Path(integrate.load(HOME / 'progress.json')['snapshot'])
    root = HOME / 'completed-recovery' / datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    root.mkdir(parents=True)
    for name in ('libraries', 'mods', 'world'):
        shutil.copytree(source / name, root / name)
    for name in ('server.properties', 'eula.txt'):
        shutil.copyfile(source / name, root / name)
    identity = prior['instanceId']
    result = dict(status='RUNNING', root=str(root), instanceId=identity, exactArtifactSha256=installed['jarSha256'], sourceWorld=str(source / 'world'), productionPromotion=False, completeGeneratorReady=False)
    integrate.save(HOME / 'completed-recovery.json', result)
    before = readback.inspect(snapshot, root / 'world')
    integrate.save(root / 'before.json', before)
    server = None
    try:
        server = OwnedServer(root, Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot', 'completed-native-restart')
        state = server.status(identity)
        result['loadedStatus'] = state
        if 'active=false tickets=0 stage=PAYLOAD_VERIFIED' not in state:
            raise integrate.GateError('Completed job did not load inactive/released')
        server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
        deadline = time.monotonic() + 480
        while time.monotonic() < deadline:
            state = server.status(identity)
            if 'phase=FAILED' in state or 'phase=ROLLBACK_REQUIRED' in state or 'Rejected:' in server.text():
                raise integrate.GateError('Completed native restart quarantined: ' + state)
            if 'active=false tickets=0 stage=PAYLOAD_VERIFIED' in state:
                result['revalidatedStatus'] = state
                break
            time.sleep(2)
        else:
            raise integrate.GateError('Completed native restart did not revalidate')
        server.send('ftbiomedungeon_candidate evidence ' + identity)
        time.sleep(1)
        server.stop()
        result['process'] = server.evidence
        server = None
        after = readback.inspect(snapshot, root / 'world')
        integrate.save(root / 'after.json', after)
        if before['originalNativeFieldDifferences'] != after['originalNativeFieldDifferences'] or after['originalNativeFieldDifferences'] or after['escapedFluidOrFire'] or after['enclosureErrors']:
            raise integrate.GateError('Native payload or fluid/enclosure changed unsafely')
        result.update(status='ACTUAL_INSTALLED_COMPLETED_ORIGINAL_NATIVE_REVALIDATION_PASS', beforeReadback=str(root / 'before.json'), afterReadback=str(root / 'after.json'), originalBEFieldsPreserved=True, noAutomaticResume=True, powerloss=False)
    except Exception as error:
        result.update(status='FAIL', error=repr(error))
    finally:
        if server is not None:
            server.stop()
            result['process'] = server.evidence
        integrate.save(HOME / 'completed-recovery.json', result)
    print(result['status'])


if __name__ == '__main__':
    main()
