import datetime
import gzip
import hashlib
import importlib.util
import json
from pathlib import Path
import re
import shutil
import sys
import time

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
PROJECT=HOME.parent.parent
INTEGRATION=PROJECT/'integration'
sys.path.insert(0,str(INTEGRATION));sys.path.insert(0,str(INTEGRATION/'v5-diagnostics'))
import integrate
import verify_saved_world as base
from faults import Server
import importlib.util
specification=importlib.util.spec_from_file_location('locked_v6_readback',HOME/'inputs/reference-integration--v6-final--readback.py')
readback=importlib.util.module_from_spec(specification);specification.loader.exec_module(readback)


def record(reader,world,identity):
    path=world/'ft_biome_dungeon_candidate_v5/minecraft/overworld'/(identity+'.nbt');frame=path.read_bytes()
    if frame[:4]!=b'FTD5' or len(frame)-72!=int.from_bytes(frame[4:8],'big') or hashlib.sha256(frame[72:]).hexdigest().encode()!=frame[8:72]:
        raise integrate.GateError('Final FTD5 journal frame invalid')
    return base.decode(reader,gzip.decompress(frame[72:])),path


def wait_idle(server,identity,seconds=1000):
    deadline=time.monotonic()+seconds
    while time.monotonic()<deadline:
        status=server.status(identity)
        if 'active=false tickets=0' in status:return status
        time.sleep(1)
    raise integrate.GateError('Owned live job remains active; no restart or duplicate launch')


def native(reader,world):
    spec=importlib.util.spec_from_file_location('native_final_fault',INTEGRATION/'v2/read_native_be.py');module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    return module.NativeWorld(reader,world)


def compare_sources(reader,snapshot,before,after,plan):
    original=native(reader,before);current=native(reader,after);changes=[];checked=0
    for room in plan['rooms']:
        structure=reader.Structure.read(snapshot/'catalog/nine-room-package-v5/data/biomedungeon/structures'/(room['templateId']+'.nbt'))
        for local in structure.blocks:
            position=base.block_position(local,room)
            if original.state(position)!=current.state(position) or original.entity(position)!=current.entity(position):changes.append(position)
            checked+=1
    return dict(sourceCellsCompared=checked,changedSourceOrNativeCells=changes)


def main():
    lock=integrate.load(HOME/'input-lock.json');snapshot=HOME/'inputs/snapshot';jar=Path(lock['jar']);source=HOME/'inputs/baseline';source_id=lock['sourceInstanceId']
    if integrate.digest(jar)!=lock['jarSha256']:raise integrate.GateError('Exact final jar mismatch')
    for entry in lock['helperFiles']:
        if integrate.digest(Path(entry['path']))!=entry['sha256']:raise integrate.GateError('Read-only helper drift')
    reader=base.load_reader(INTEGRATION/'staging/20261106_132015_129205');root=HOME/'run'/datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    result=dict(status='RUNNING',root=str(root),snapshot=str(snapshot),jarSha256=lock['jarSha256'],powerloss=False,inheritedFaultPass=False,cases={},accepted=False,productionPromotion=False,independentExecutor=lock['independentExecutor'],maxOwnedServers=1)
    progress=HOME/'progress.json'
    for case in (sys.argv[1:] or ['truncated_journal','checksum_journal','foreign_block_preflight','unknown_inventory_forcekill','durable_checkpoint_forcekill']):
        run=root/case;run.mkdir(parents=True);print('CASE_START '+case,flush=True);shutil.copytree(source/'libraries',run/'libraries');(run/'mods').mkdir();shutil.copyfile(jar,run/'mods'/jar.name)
        for name in ('eula.txt','server.properties'):shutil.copyfile(source/name,run/name)
        evidence=dict(status='RUNNING',processes=[]);result['cases'][case]=evidence;integrate.save(progress,result);server=None;world=run/'world'
        try:
            copied=case in ('truncated_journal','checksum_journal','unknown_inventory_forcekill')
            if copied:
                shutil.copytree(source/'world',world);identity=source_id;saved,journal=record(reader,world,identity)
                integrate.save(run/'copied-world-lock.json',dict(files=[dict(path=path.relative_to(world).as_posix(),sha256=integrate.digest(path)) for path in sorted(world.rglob('*')) if path.is_file()]))
            else:
                world.mkdir();(world/'ALLOW_CANDIDATE_EVIDENCE_WORLD_V5').write_text('PARENT_AUTHORIZED_EIGHT_ORIGINALS_ONE_DERIVED_V5_EVIDENCE_ONLY\n')
            if 'journal' in case:
                original=journal.read_bytes();shutil.copyfile(journal,run/'journal-before.nbt');journal.write_bytes(original[:32] if case=='truncated_journal' else original[:-1]+bytes([original[-1]^1]));evidence.update(originalJournalSha256=hashlib.sha256(original).hexdigest(),faultJournalSha256=integrate.digest(journal))
                server=Server(run,'corruption');offset=len(server.text());server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE');text=server.wait(lambda text:'Rejected:' in text[offset:]);rejected=text[offset:]
                expected='Truncated journal' if case=='truncated_journal' else 'checksum mismatch'
                if expected.lower() not in rejected.lower():raise integrate.GateError('Wrong corruption refusal '+rejected[-600:])
                server.stop();evidence['processes'].append(server.evidence);server=None
                compared=compare_sources(reader,snapshot,source/'world',world,json.loads(saved['plan']))
                if compared['changedSourceOrNativeCells']:raise integrate.GateError('Corruption refusal changed native/source cells')
                evidence.update(status='PASS_CORRUPTION_ACTUAL_COMMAND_REFUSAL_ZERO_SOURCE_WRITES',rejection=rejected,**compared)
            elif case=='unknown_inventory_forcekill':
                plan=json.loads(saved['plan']);position=None
                for room in plan['rooms']:
                    structure=reader.Structure.read(snapshot/'catalog/nine-room-package-v5/data/biomedungeon/structures'/(room['templateId']+'.nbt'))
                    for local,state in structure.blocks.items():
                        if state.name=='minecraft:chest':position=base.block_position(local,room);break
                    if position is not None:break
                if position is None:raise integrate.GateError('Actual native chest missing')
                server=Server(run,'unknown');coordinate=' '.join(map(str,position));offset=len(server.text());server.send('forceload add '+str(position[0])+' '+str(position[2]));time.sleep(2);server.send('item replace block '+coordinate+' container.0 with minecraft:emerald 7');server.wait(lambda text:'Replaced a slot' in text[offset:]);offset=len(server.text());server.send('save-all flush');server.wait(lambda text:'Saved the game' in text[offset:]);server.kill_verified();evidence['processes'].append(server.evidence);server=None
                server=Server(run,'resume-unknown');loaded=server.status(identity);server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE');status=wait_idle(server,identity)
                if 'ROLLBACK_REQUIRED' not in status and 'phase=FAILED' not in status:raise integrate.GateError('Unknown inventory not quarantined')
                server.stop();evidence['processes'].append(server.evidence);server=None;entity=native(reader,world).entity(position)
                if not any(item.get('id')=='minecraft:emerald' and item.get('Count')==7 for item in entity.get('Items',[])):raise integrate.GateError('Unknown inventory overwritten')
                evidence.update(status='PASS_FORCED_JAVA_UNKNOWN_INVENTORY_PRESERVED',loadedStatus=loaded,finalStatus=status,position=position,nativeEntity=entity,syntheticUnknownInventory=True)
            else:
                server=Server(run,'prepare');offset=len(server.text());server.send('ftbiomedungeon_candidate plan 1000 200 1000 s8 9 0');text=server.wait(lambda text:re.search(r'DRY RUN (ftde-[a-f0-9]+)',text[offset:])is not None);identity=re.search(r'DRY RUN (ftde-[a-f0-9]+)',text[offset:]).group(1);saved,journal=record(reader,world,identity)
                if case=='foreign_block_preflight':
                    position=(1000,200,1000);server.send('forceload add 1000 1000');time.sleep(2);offset=len(server.text());server.send('setblock 1000 200 1000 minecraft:diamond_block');server.wait(lambda text:'Changed the block' in text[offset:]);server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE');status=wait_idle(server,identity)
                    if 'ROLLBACK_REQUIRED' not in status and 'phase=FAILED' not in status:raise integrate.GateError('Foreign block not quarantined')
                    server.stop();evidence['processes'].append(server.evidence);server=None;observed=base.World(reader,world);bounds=json.loads(saved['bounds']);lower=bounds['min'];upper=bounds['maxExclusive'];changed=[];checked=0
                    for coordinate_y in range(lower['y'],upper['y']):
                        for coordinate_z in range(lower['z'],upper['z']):
                            for coordinate_x in range(lower['x'],upper['x']):
                                current=(coordinate_x,coordinate_y,coordinate_z);state=observed.state(current)[0];expected='minecraft:diamond_block' if current==position else 'minecraft:air'
                                if state!=expected:changed.append(dict(position=current,state=state))
                                checked+=1
                    if changed:raise integrate.GateError('Foreign preflight made world writes '+str(changed[:5]))
                    evidence.update(status='PASS_FOREIGN_BLOCK_PREFLIGHT_ZERO_WRITES',envelopeCellsCompared=checked,finalStatus=status)
                else:
                    server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE');deadline=time.monotonic()+240
                    while time.monotonic()<deadline:
                        status=server.status(identity);cursor=int(re.search(r'cursor=(\d+)',status).group(1))
                        durable=case=='durable_checkpoint_forcekill'
                        if 'phase=BUILDING' in status and cursor>4096 and (not durable or 'V5_DURABLE_CHECKPOINT' in server.text()):evidence['armedStatus']=status;break
                        if 'ROLLBACK_REQUIRED' in status or 'phase=FAILED' in status:raise integrate.GateError(status)
                        time.sleep(0.2)
                    else:raise integrate.GateError('No live partial build to terminate')
                    server.kill_verified();evidence['processes'].append(server.evidence);server=None;saved,journal=record(reader,world,identity);shutil.copyfile(journal,run/'killed-journal.nbt');shutil.copytree(world/'region',run/'after-kill-regions');evidence['actualJournalCursorAfterKill']=saved['cursor'];evidence['completedThrough']=saved.get('completedThrough')
                    server=Server(run,'resume');loaded=server.status(identity)
                    evidence['loadedStatus']=loaded
                    if saved['cursor']!=saved.get('completedThrough'):raise integrate.GateError('Persisted BUILDING cursor not durable completedThrough')
                    if 'active=false tickets=0' not in loaded:raise integrate.GateError('Automatic resume/ticket retention after abrupt Java termination')
                    server.send('ftbiomedungeon_candidate build '+identity+' CONFIRM_ISOLATED_AIRSPACE');status=wait_idle(server,identity,1100)
                    if 'PAYLOAD_VERIFIED' not in status:raise integrate.GateError('Durable partial build did not resume successfully '+status)
                    server.stop();evidence['processes'].append(server.evidence);server=None;observed=readback.inspect(snapshot,world);integrate.save(run/'readback.json',observed)
                    if any(observed[key] for key in ('originalNativeFieldDifferences','escapedFluidOrFire','enclosureErrors')):raise integrate.GateError('Resumed saved native readback mismatch')
                    evidence.update(status='PASS_ABRUPT_JAVA_PARTIAL_BUILD_EXPLICIT_RESUME_SAVED_NATIVE_READBACK',loadedStatus=loaded,finalStatus=status,readback=str(run/'readback.json'),reportedOriginalStateDifferences=len(observed['originalStateDifferences']),powerloss=False)
        except Exception as error:evidence.update(status='FAIL',error=repr(error))
        finally:
            if server is not None:server.stop();evidence['processes'].append(server.evidence)
            integrate.save(run/'result.json',evidence);integrate.save(progress,result);print('CASE_RESULT '+case+' '+evidence['status']+' '+evidence.get('error',''),flush=True)
    result['status']='PASS_ALL_EXACT_FINAL_FAULT_CASES' if all(entry['status'].startswith('PASS') for entry in result['cases'].values()) else 'FAULT_FAILURES_OR_INCOMPLETE';integrate.save(progress,result);integrate.save(root/'result.json',result);print(result['status'])


if __name__=='__main__':main()

