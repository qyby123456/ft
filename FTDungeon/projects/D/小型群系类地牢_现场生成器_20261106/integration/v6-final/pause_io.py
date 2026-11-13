import datetime
from pathlib import Path
import shutil
import sys
import time

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent));sys.path.insert(0,str(HOME.parent/'v5-diagnostics'))
import integrate
from faults import Server
import importlib.util
_readback_spec=importlib.util.spec_from_file_location('final_ftd5_pause_readback',HOME/'readback.py')
readback=importlib.util.module_from_spec(_readback_spec)
_readback_spec.loader.exec_module(readback)


def main():
    frozen=integrate.load(HOME/'progress.json');installed=integrate.load(HOME/'installed-progress.json')
    if frozen['jarSha256']!=installed['jarSha256'] or 'next-commit-iofix' not in integrate.load(Path(frozen['snapshot'])/'snapshot-lock.json')['components']['runtime']['source']:
        raise integrate.GateError('The installed final iofix JAR is required for native pause I/O validation')
    source=Path(installed['root'])/'q0';completed=integrate.load(source/'result.json')
    if completed['status']=='FAIL':raise integrate.GateError('Completed disposable source required')
    identity=completed['instanceId'];root=HOME/'io-run'/datetime.datetime.now().strftime('%Y%m%d_%H%M%S');root.mkdir(parents=True)
    shutil.copytree(source/'libraries',root/'libraries');shutil.copytree(source/'world',root/'world');shutil.copytree(source/'mods',root/'mods')
    for name in ('eula.txt','server.properties'):shutil.copyfile(source/name,root/name)
    if len(list((root/'mods').glob('*.jar')))!=1 or integrate.digest(next((root/'mods').glob('*.jar')))!=frozen['jarSha256']:
        raise integrate.GateError('Copied exact final JAR mismatch')
    world=root/'world';journal=world/'ft_biome_dungeon_candidate_v5/minecraft/overworld'/(identity+'.nbt');blocked=journal.with_name(journal.name+'.pending')
    evidence=dict(status='RUNNING',jarSha256=frozen['jarSha256'],world=str(world),mechanism='Real filesystem .nbt.pending directory makes FileChannel.open fail with IOException; build/pause/status queued in one console flush before level tick',nativeMethod='UnifiedEngine.pause on the installed release JAR',accepted=False,productionPromotion=False)
    integrate.save(HOME/'pause-io-progress.json',evidence)
    baseline=readback.inspect(Path(frozen['snapshot']),world);integrate.save(root/'baseline-readback.json',baseline)
    server=None
    try:
        server=Server(root,'pause-io');before=integrate.digest(journal);blocked.mkdir();offset=len(server.text())
        server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE\nftbiomedungeon_candidate pause '+identity+'\nftbiomedungeon_candidate status '+identity+'\nftbiomedungeon_candidate list')
        text=server.wait(lambda text:'DISABLED: Pause journal persistence failed' in text[offset:],45)
        status=server.status(identity)
        if 'active=false tickets=0' not in status:raise integrate.GateError('Pause command failure did not release active job and native tickets')
        offset=len(server.text());server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE')
        server.wait(lambda text:'Runtime disabled' in text[offset:] or 'runtime disabled' in text[offset:],30)
        blocked.rmdir();time.sleep(5);later=server.status(identity)
        if 'active=false tickets=0' not in later or 'phase=COMMITTED' in later:raise integrate.GateError('Failed pause later resumed or committed')
        if integrate.digest(journal)!=before:raise integrate.GateError('Failed pause changed the formal journal')
        server.stop();evidence['process']=server.evidence;server=None
        observed=readback.inspect(Path(frozen['snapshot']),world);integrate.save(root/'saved-readback.json',observed)
        if observed['originalStateDifferences']!=baseline['originalStateDifferences'] or any(observed[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors')):
            raise integrate.GateError('Pause failure changed source/native cells or enclosure')
        evidence.update(status='PASS_NATIVE_INSTALLED_PAUSE_IO_EXCEPTION_CLEANUP',journalUnchanged=True,initialStatus=status,delayedStatus=later,savedReadback=str(root/'saved-readback.json'),sourceCells=observed['counts']['sourceCells'],nativePayloads=observed['counts']['nativeBlockEntities'])
    except Exception as error:evidence.update(status='FAIL',error=repr(error))
    finally:
        if blocked.is_dir():blocked.rmdir()
        if server is not None:server.stop();evidence['process']=server.evidence
        integrate.save(HOME/'pause-io-progress.json',evidence);integrate.save(root/'result.json',evidence)
    print(evidence['status'])


if __name__=='__main__':main()
