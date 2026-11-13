import datetime
import importlib.util
import json
from pathlib import Path
import shutil
import sys
import time

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent));sys.path.insert(0,str(HOME.parent/'v5-diagnostics'))
import integrate
from faults import Server
spec=importlib.util.spec_from_file_location('final_installed_commands',HOME/'installed.py');commands=importlib.util.module_from_spec(spec);spec.loader.exec_module(commands)
spec=importlib.util.spec_from_file_location('final_commit_ftd5_readback',HOME/'readback.py');readback=importlib.util.module_from_spec(spec);spec.loader.exec_module(readback)


def receipt(world,identity,binding,decision):
    reports={}
    for gate,entry in decision['gates'].items():
        if entry['status']!='INDEPENDENT_PASS':raise integrate.GateError('Unpassed gate '+gate)
        source=Path(entry['report']);relative='candidate-acceptance/reports/'+gate+'.json';target=world/relative
        if integrate.digest(source)!=entry['sha256']:raise integrate.GateError('Reviewed evidence report changed '+gate)
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,target);reports[gate]=dict(path=relative,sha256=entry['sha256'])
    value=dict(schema=1,binding=binding,gates={gate:'INDEPENDENT_PASS' for gate in reports},reports=reports,unresolvedBlockers=[],independentExecutor='integration-v6-final-with-verified-independent-core-and-catalog-evidence',purpose='ISOLATED_EXPERIMENTAL_JOB_COMMIT_ONLY')
    target=world/'candidate-acceptance'/(identity+'.json');integrate.save(target,value);return integrate.digest(target)


def main():
    mode=sys.argv[1] if len(sys.argv)>1 else 'probe'
    decision=integrate.load(HOME/'acceptance-decision.json');frozen=integrate.load(HOME/'progress.json');installed=integrate.load(HOME/'installed-progress.json')
    if decision['status']!='ALL_INDEPENDENT_GATES_PASS_ISOLATED_COMMIT_ELIGIBLE' or decision['unresolvedBlockers'] or decision['jarSha256']!=frozen['jarSha256'] or installed['jarSha256']!=frozen['jarSha256']:
        raise integrate.GateError('All independent final artifact gates must pass before a legitimate receipt exists')
    source=Path(installed['root'])/'q0';identity=installed['cases']['0']['instanceId']
    if mode=='probe':
        run=HOME/'commit-io-run'/datetime.datetime.now().strftime('%Y%m%d_%H%M%S');run.mkdir(parents=True)
        for name in ('world','libraries','mods'):shutil.copytree(source/name,run/name)
        for name in ('server.properties','eula.txt'):shutil.copyfile(source/name,run/name)
    else:
        probe=integrate.load(HOME/'commit-io-progress.json')
        if probe['status']!='PASS_INSTALLED_COMMIT_ADMISSION_IO_CLEANUP' or probe['jarSha256']!=frozen['jarSha256']:raise integrate.GateError('Final installed admission I/O cleanup must pass first')
        run=source
    world=run/'world';journal=world/'ft_biome_dungeon_candidate_v5/minecraft/overworld'/(identity+'.nbt');blocked=journal.with_name(journal.name+'.pending');server=None
    result=dict(status='RUNNING',mode=mode,world=str(world),jarSha256=frozen['jarSha256'],instanceId=identity,decisionSha256=integrate.digest(HOME/'acceptance-decision.json'),accepted=False,productionPromotion=False,processes=[])
    progress=HOME/('commit-io-progress.json' if mode=='probe' else 'actual-commit-progress.json');integrate.save(progress,result)
    try:
        baseline=readback.inspect(Path(frozen['snapshot']),world);integrate.save(run/(mode+'-baseline-readback.json'),baseline)
        server=Server(run,'commit-io' if mode=='probe' else 'actual-commit');binding=commands.context(server,identity)
        if binding['jarSha256']!=frozen['jarSha256']:raise integrate.GateError('Live commit artifact binding mismatch')
        receipt_sha=receipt(world,identity,binding,decision);result.update(binding=binding,receiptSha256=receipt_sha)
        journal_before=integrate.digest(journal)
        if mode=='probe':blocked.mkdir()
        offset=len(server.text());server.send('ftbiomedungeon_candidate commit '+identity+' '+receipt_sha+' CONFIRM_INDEPENDENT_EVIDENCE_COMMIT')
        if mode=='probe':
            server.wait(lambda text:'Rejected:' in text[offset:],45);server.send('ftbiomedungeon_candidate list');text=server.wait(lambda text:'DISABLED: Commit journal persistence failed' in text[offset:],45)
            status=server.status(identity)
            if 'active=false tickets=0' not in status or 'phase=COMMITTED' in status:raise integrate.GateError('Failed commit remained active or committed')
            blocked.rmdir();time.sleep(6);later=server.status(identity)
            if 'active=false tickets=0' not in later or 'phase=COMMITTED' in later or integrate.digest(journal)!=journal_before:raise integrate.GateError('Failed commit autonomously continued or altered durable journal')
            offset=len(server.text());server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE');server.wait(lambda text:'runtime disabled' in text[offset:].lower(),30)
            result.update(commandResponse=text[offset:],initialStatus=status,delayedStatus=later,journalUnchanged=True,mechanism='Valid independently backed receipt followed by real .nbt.pending directory FileChannel IOException at final commit admission persist; unchanged installed release JAR',privateIntentFlagsEvidence='Installed source catch/finally verification; installed behavioral evidence is idle tickets0, disabled rebuild and no delayed continuation')
        else:
            server.wait(lambda text:'Independent evidence hash admitted by explicit operator command' in text[offset:] or 'Rejected:' in text[offset:],60)
            if 'Rejected:' in server.text()[offset:]:raise integrate.GateError('Actual legitimate commit command rejected '+server.text()[offset:][-800:])
            deadline=time.monotonic()+1200
            while time.monotonic()<deadline:
                status=server.status(identity)
                if 'phase=COMMITTED' in status and 'active=false tickets=0' in status:break
                if 'ROLLBACK_REQUIRED' in status or 'phase=FAILED' in status:raise integrate.GateError('Actual commit quarantined '+status)
                time.sleep(2)
            else:raise integrate.GateError('Actual commit did not finish; process left under owned handle')
            result['committedStatus']=status
        server.stop();result['processes'].append(server.evidence);server=None
        observed=readback.inspect(Path(frozen['snapshot']),world);integrate.save(run/(mode+'-after-readback.json'),observed)
        if observed['originalStateDifferences']!=baseline['originalStateDifferences'] or any(observed[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors')):raise integrate.GateError('Commit operation changed original/native/containment state')
        if mode=='probe':result['status']='PASS_INSTALLED_COMMIT_ADMISSION_IO_CLEANUP'
        else:
            server=Server(run,'actual-committed-restart');status=server.status(identity);rebound=commands.context(server,identity)
            if 'phase=COMMITTED' not in status or 'active=false tickets=0' not in status or rebound!=binding:raise integrate.GateError('Same canonical world failed COMMITTED restart/binding verification')
            if integrate.digest(world/'candidate-acceptance'/(identity+'.json'))!=receipt_sha:raise integrate.GateError('Committed receipt changed')
            server.send('ftbiomedungeon_candidate evidence '+identity);time.sleep(1);server.stop();result['processes'].append(server.evidence);server=None
            restarted=readback.inspect(Path(frozen['snapshot']),world);integrate.save(run/'committed-restart-readback.json',restarted)
            if restarted['phase']!='COMMITTED' or restarted['originalStateDifferences']!=observed['originalStateDifferences'] or any(restarted[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors')):raise integrate.GateError('COMMITTED separate JVM saved readback failed')
            result.update(status='PASS_ACTUAL_EXPLICIT_ISOLATED_COMMITTED_AND_SAME_PATH_RESTART',restartStatus=status,bindingStable=True,receiptStable=True,savedPhase=restarted['phase'])
    except Exception as error:result.update(status='FAIL',error=repr(error))
    finally:
        if blocked.is_dir():blocked.rmdir()
        if server is not None:server.stop();result['processes'].append(server.evidence)
        integrate.save(progress,result);integrate.save(run/(mode+'-commit-result.json'),result)
    print(result['status'])
    if result['status']=='FAIL':raise SystemExit(1)


if __name__=='__main__':main()
