import datetime
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUT=WORK/'reports/material-proc-native-implementation-checkpoint-20261112'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
assert not OUT.exists()
assert load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
candidates=[WORK/'reports/native-repair-material-proc-integration-candidate-ee613dfe.json',WORK/'reports/native-repair-material-proc-integration-candidate-cf217105.json']
for candidate in candidates:
    for name,wanted in load(candidate)['inputHashes'].items():assert sha(name)==wanted,name
focused=WORK/'reports/native-repair-runtime-material-proc-integration-ee613dfe.json';result=load(focused);terminal=load(Path(result['attempt'])/'terminal308.json')
assert result['exitCode']==0 and result['probe']['failures']==0 and result['privateReadGuardProbe']['failures']==0
assert terminal['stopSent'] and terminal['completeSave'] and len(result['probe']['cases'])==1
case=result['probe']['cases'][0];assert case['id']=='ftboss:defect_claw' and case['sameLeaseAndGenerationAfterAttack'] and case['actualOriginalPostCombatSaveAuthenticatedCommitted']
assert all(row['traceComplete'] and row['originalExpectedReturnedStepCount']==9 for row in case['publications'])
formal={
    'Forge':{'path':'C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar','sha256':'7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'},
    'NeoForge':{'path':'C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar','sha256':'58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'}}
for row in formal.values():assert sha(row['path'])==row['sha256']
OUT.mkdir()
live=WORK/'build/repair-runtime-b3299174';live_process=load(live/'live-process308.json')
log=(live/'runtime308.log').read_bytes();(OUT/'whole-nineteen-live-log-snapshot.txt').write_bytes(log)
progress={'createdAt':datetime.datetime.now().astimezone().isoformat(),'goalStatus':'active','currentCandidate':'cf217105',
    'focusedPredecessorAccepted':'ee613dfe/defect_claw','focusedNativeResult':str(focused),
    'wholeNineteenLiveAttempt':str(live),'liveProcess':live_process,'wholeNineteenAccepted':False,'newJvmContinuationAccepted':False,
    'partialWorldMustNotSeed':True,'formalInstancesUnchanged':formal,'installable':False,'deployed':False,
    'boundary':load(WORK/'build/material-proc-original-boundary-cf217105/boundary-terminal.json'),
    'oracle':load(WORK/'reports/material-proc-expected-plan-proof-cf217105-20261112.json')['oracle'],
    'remainingAudit':'work/session-lease/reports/final-acceptance-audit-5f283607-20261112/requirements-and-evidence.json',
    'priorCheckpoint':'work/session-lease/reports/material-proc-session-gap-checkpoint-20261112/archive-receipt.json'}
save(OUT/'progress.json',progress)
selected={Path(__file__),focused,*candidates,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_INCREMENTAL_BATCH.json',WORK/'MATERIAL_PROC_NATIVE_INTEGRATION_20261112.md'}
for candidate in candidates:
    bound=load(candidate)
    for name in bound['inputHashes']:
        path=Path(name)
        if path.resolve().is_relative_to(ROOT):selected.add(path)
    build=Path(bound['attempt'])
    selected.update(path for path in build.rglob('*') if path.is_file())
for folder in ['proposal-material-proc-native-integration','tests/material-proc-nineteen-restart-cf217105','reports/final-acceptance-audit-5f283607-20261112','build/material-proc-original-boundary-cf217105','build/material-proc-expected-plan-proof-cf217105-20261112']:
    selected.update(path for path in (WORK/folder).rglob('*') if path.is_file())
for path in (WORK/'tools').iterdir():
    if path.is_file() and 'material_proc' in path.name:selected.add(path)
for path in (WORK/'reports').iterdir():
    if path.is_file() and ('material-proc' in path.name):selected.add(path)
selected.update([WORK/'tests/MaterialProcOriginalBoundaryProof.java',WORK/'tests/MaterialProcExpectedPlanProof.java',WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java',WORK/'tests/defect-claw-session-gap-5f283607/PositiveWeapon436.java',WORK/'reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json'])
attempt=Path(result['attempt']);selected.update(path for path in attempt.iterdir() if path.is_file())
selected.update(path for path in (attempt/'server').glob('actual-*.json'))
for name in ['logs/latest.log','logs/debug.log']:
    path=attempt/'server'/name
    if path.is_file():selected.add(path)
for name in ['runtime-command308.json','inputs-before308.json','restart-world-seed.json','live-process308.json']:
    path=live/name
    if path.is_file():selected.add(path)
restore=OUT/'RESTORE.md'
with restore.open('x',encoding='utf-8') as stream:stream.write('# Native material implementation checkpoint\n\nRestore in the sole C workspace with the frozen previous archives referenced by progress.json. Both full candidate class graphs and final frozen build sources are included. ee613dfe is focused native evidence only. cf217105 whole-nineteen was live at this checkpoint; its log snapshot is not acceptance. Never use partial/failed worlds as restart seeds or launch another heavy native process. Inspect its actual terminal outcome, then only after complete accepted original nineteen use the prepared new-JVM runner. Historical D resources and the genuine paid849680bb world remain external frozen inputs. Formal instances remain read-only and neither candidate is installable/deployed. The explicit full goal remains active.\n')
selected.update(path for path in OUT.iterdir() if path.is_file())
archive=WORK/'archives/material-proc-native-implementation-FOCUSED-PASS-LIVE19-20261112.zip';manifest={}
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(),path
        name=path.relative_to(ROOT).as_posix();manifest[name]={'sha256':sha(path),'bytes':path.stat().st_size};bundle.write(path,name)
    encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
    for name,row in manifest.items():assert hashlib.sha256(bundle.read(name)).hexdigest()==row['sha256'],name
receipt={'archive':str(archive),'sha256':sha(archive),'fileCount':len(manifest),'failures':0,'allMembersReadBackAndHashVerified':True,
    'progress':str(OUT/'progress.json'),'wholeNineteenAccepted':False,'goalStatus':'active','installable':False,'deployed':False}
save(OUT/'archive-receipt.json',receipt)
print(json.dumps(receipt,ensure_ascii=False),flush=True)
