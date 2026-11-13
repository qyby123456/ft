import gzip
from pathlib import Path
import sys
import time

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent));sys.path.insert(0,str(HOME.parent/'v5-diagnostics'))
import integrate
import verify_saved_world as base
from faults import Server
import importlib.util
_readback_spec=importlib.util.spec_from_file_location('final_ftd5_restart_readback',HOME/'readback.py')
readback=importlib.util.module_from_spec(_readback_spec)
_readback_spec.loader.exec_module(readback)


def main():
    frozen=integrate.load(HOME/'progress.json');installed=integrate.load(HOME/'installed-progress.json')
    if frozen['jarSha256']!=installed['jarSha256']:raise integrate.GateError('Restart exact artifact mismatch')
    result=dict(status='RUNNING',jarSha256=frozen['jarSha256'],cases={},accepted=False,productionPromotion=False)
    reader=base.load_reader(HOME.parent/'staging/20261106_132015_129205')
    for turn in range(4):
        run=Path(installed['root'])/('q'+str(turn));entry=installed['cases'][str(turn)];identity=entry['instanceId'];server=None;evidence=dict(status='RUNNING');result['cases'][str(turn)]=evidence
        try:
            before=readback.inspect(Path(frozen['snapshot']),run/'world');integrate.save(run/'independent-before-restart.json',before)
            if any(before[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors')):raise integrate.GateError('Pre-restart native saved mismatch')
            server=Server(run,'independent-restart');status=server.status(identity)
            if 'active=false tickets=0' not in status or 'PAYLOAD_VERIFIED' not in status:raise integrate.GateError('Restart automatically continued or lost payload pending phase')
            server.send('ftbiomedungeon_candidate evidence '+identity);time.sleep(1);offset=len(server.text());server.send('save-all flush');server.wait(lambda text:'Saved the game' in text[offset:]);server.stop();evidence['process']=server.evidence;server=None
            after=readback.inspect(Path(frozen['snapshot']),run/'world');integrate.save(run/'independent-after-restart.json',after)
            if after['originalStateDifferences']!=before['originalStateDifferences'] or any(after[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors')):raise integrate.GateError('Post-restart changed raw source differences/native payload/fluid/enclosure')
            evidence.update(status='PASS_SEPARATE_JVM_INSTALLED_RESTART_SOURCE_NATIVE_FLUID_READBACK',jobStatus=status,before=str(run/'independent-before-restart.json'),after=str(run/'independent-after-restart.json'),sourceCells=after['counts']['sourceCells'],nativePayloads=after['counts']['nativeBlockEntities'],fluidStates=len(after['fluidFireStates']))
        except Exception as error:evidence.update(status='FAIL',error=repr(error))
        finally:
            if server is not None:server.stop();evidence['process']=server.evidence
            integrate.save(HOME/'restart-progress.json',result)
    result['status']='PASS_ALL_FOUR_INSTALLED_SEPARATE_JVM_NATIVE_RESTARTS' if all(entry['status'].startswith('PASS') for entry in result['cases'].values()) else 'FAILED';integrate.save(HOME/'restart-progress.json',result);print(result['status'])


if __name__=='__main__':main()
