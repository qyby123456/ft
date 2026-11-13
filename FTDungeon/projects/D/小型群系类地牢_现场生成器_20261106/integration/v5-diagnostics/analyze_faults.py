import gzip
import hashlib
import importlib.util
import json
from pathlib import Path
import shutil
import sys

sys.dont_write_bytecode=True
HOME=Path(__file__).resolve().parent
sys.path.insert(0,str(HOME.parent))
import integrate
import verify_saved_world as base


def main():
    initial=integrate.load(HOME/'fault-initial-results.json')
    retry=integrate.load(HOME/'fault-progress.json')
    cases=initial['cases']|retry['cases']
    root=Path(initial['root'])/'midbuild_forcekill'
    frame=(root/'killed-journal.nbt').read_bytes()
    reader=base.load_reader(HOME.parent/'staging/20261106_132015_129205')
    saved=base.decode(reader,gzip.decompress(frame[72:]))
    identity=json.loads(saved['plan'])['instanceId']
    after_path=root/'world/ft_biome_dungeon_candidate_v4/minecraft/overworld'/(identity+'.nbt')
    after=base.decode(reader,gzip.decompress(after_path.read_bytes()[72:]))
    before_root=root/'preserved-before-resume'
    if not (before_root/'region').exists():
        shutil.copytree(root/'after-kill-regions',before_root/'region')
    spec=importlib.util.spec_from_file_location('sealed_native_v5_fault',HOME.parent/'v2/read_native_be.py')
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    before_world=module.NativeWorld(reader,before_root);after_world=module.NativeWorld(reader,root/'world')
    bounds=json.loads(saved['bounds']);lower=bounds['min'];upper=bounds['maxExclusive']
    checked=0;missing_chunks=[];changes=[];before_hash=hashlib.sha256();after_hash=hashlib.sha256()
    for chunk_x in range(lower['x']//16,(upper['x']-1)//16+1):
        for chunk_z in range(lower['z']//16,(upper['z']-1)//16+1):
            try:
                before_world.chunk(chunk_x,chunk_z)
            except (FileNotFoundError,integrate.GateError):
                missing_chunks.append([chunk_x,chunk_z]);continue
            for coordinate_y in range(lower['y'],upper['y']):
                for coordinate_z in range(max(lower['z'],chunk_z*16),min(upper['z'],chunk_z*16+16)):
                    for coordinate_x in range(max(lower['x'],chunk_x*16),min(upper['x'],chunk_x*16+16)):
                        position=(coordinate_x,coordinate_y,coordinate_z)
                        original=(before_world.state(position),before_world.entity(position));current=(after_world.state(position),after_world.entity(position))
                        before_hash.update(json.dumps(original,sort_keys=True).encode());after_hash.update(json.dumps(current,sort_keys=True).encode())
                        if original!=current:
                            changes.append(dict(position=position,before=original,after=current))
                        checked+=1
    checkpoint=root/'world/V5_FAULT_ANALYSIS.json'
    stable=not changes and checked>0 and saved['cursor']==after['cursor'] and after['phase']=='ROLLBACK_REQUIRED' and 'tickets=0' in cases['midbuild_forcekill']['finalStatus']
    diagnostic=dict(status='PASS_FORCEKILL_FAIL_CLOSED_SAVED_CHUNKS_UNCHANGED' if stable else 'FAIL_OR_UNPROVEN_FORCEKILL_QUARANTINE',comparedSavedEnvelopeCells=checked,absentBeforeResumeChunks=missing_chunks,changedCells=changes,beforeSemanticSha256=before_hash.hexdigest(),afterSemanticSha256=after_hash.hexdigest(),savedJournalCursor=saved['cursor'],afterJournalCursor=after['cursor'],afterPhase=after['phase'],error=after.get('error'),scope='Exact V4 ordinary installed JAR; abrupt Java termination left completed journal cells without persisted block writes. Explicit resume preflight quarantined without rewriting any compared saved envelope cell/native entity. Absent chunks are listed, not asserted unchanged. This is fail-closed safety evidence, not successful resume completion or power-loss durability.')
    integrate.save(checkpoint,diagnostic)
    cases['midbuild_forcekill'].update(status=diagnostic['status'],independentSavedWorldAnalysis=diagnostic)
    result=dict(status='SCOPED_EXACT_V4_FAULT_PROTECTION_PASS' if all(entry['status'].startswith('PASS') for entry in cases.values()) else 'FAULTS_CLOSED_WITH_FAILURES',jarSha256=initial['jarSha256'],cases=cases,inheritedV3FaultPass=False,powerloss=False,completeGeneratorReady=False,sourceRuns=[initial['root'],retry['root']])
    integrate.save(HOME/'fault-final-evidence.json',result)
    print(result['status'],'midbuildCells=',checked,'missingChunks=',len(missing_chunks),'changes=',len(changes))


if __name__=='__main__':
    main()
