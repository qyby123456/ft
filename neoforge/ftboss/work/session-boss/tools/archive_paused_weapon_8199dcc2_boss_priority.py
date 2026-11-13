import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
BOSS=ROOT/'work/session-boss'
LEASE=ROOT/'work/session-lease'
OUT=BOSS/'archives/weapons-paused-8199dcc2-boss-priority-20261112'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,data):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)

helper=BOSS/'tools/archive_paused_weapon_boss_priority_613d4f6f_v2.py'
namespace={ '__file__':str(helper) }
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('assert not OUT.exists()')],str(helper),'exec'),namespace)
patch_file=namespace['patch_file']
assert not OUT.exists()
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
release=BOSS/'releases/boss-core-stage12-bone-crush-20261112'
deployment=load(release/'deployment-receipt.json')
prior=BOSS/'archives/weapons-paused-613d4f6f-boss-priority-v2-20261112'
prior_receipt=load(prior/'archive-receipt.json')
formal={'neoforge':{'path':deployment['target'],'sha256':deployment['deployedSha256']},'forge':prior_receipt['formalInstancesUnchanged']['forge']}
for row in formal.values():assert sha(row['path'])==row['sha256']
candidate=LEASE/'reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json'
assert sha(candidate)=='23f7a20eae05b48826098112dcee71f67e3c61e51f03ba9c0f897866970f49bc'
frozen=load(candidate)
for name,wanted in frozen['inputHashes'].items():assert sha(name)==wanted,name
attempt=LEASE/'build/repair-runtime-9044dadd'
terminal=load(attempt/'terminal308.json')
assert terminal['exitCode']==0 and terminal['completeSave']
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;owned=($_.CommandLine -match 'session-boss|session-lease')} } | ConvertTo-Json -Compress"
rows=json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',command],encoding='utf-8-sig'))
rows=rows if isinstance(rows,list) else [rows]
assert not any(row['owned'] and row['pid']!=os.getpid() for row in rows)
OUT.mkdir(parents=True)
selected={Path(__file__),candidate,ROOT/'WRITE_OWNER.claim',helper}
assert sha(prior_receipt['archive'])==prior_receipt['archiveSha256']
selected.update(p for p in prior.iterdir() if p.is_file())
external={}
for name,wanted in frozen['inputHashes'].items():
    path=Path(name)
    if path.resolve().is_relative_to(ROOT):selected.add(path)
    else:external[name]=wanted
for folder in ['build/native-repair-8199dcc2','proposal-certified-ranged-timer-frame','tests/certified-ranged-timer-frame-foreign-guard','tests/native-domain-retirement-watch-8199dcc2','reports/goal-reactivation-after-stage12-20261112']:
    selected.update(p for p in (LEASE/folder).rglob('*') if p.is_file() and '__pycache__' not in p.parts)
for folder in [LEASE/'tools',LEASE/'reports']:
    selected.update(p for p in folder.iterdir() if p.is_file() and ('certified_ranged' in p.name or 'certified-ranged' in p.name or 'reactivate_goal_after_boss_stage12' in p.name))
selected.add(LEASE/'tests/CertifiedRangedTimerBoundaryProof.java')
selected.update(p for p in attempt.iterdir() if p.is_file())
for name in ['server/logs/latest.log','server/logs/debug.log']:
    path=attempt/name
    if path.exists():selected.add(path)
selected.update(p for p in (attempt/'server').glob('actual-*.json'))
snapshot=OUT/'ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-8199dcc2.jar'
changed=[]
with zipfile.ZipFile(prior_receipt['snapshotJar']) as before,zipfile.ZipFile(snapshot,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for entry in before.infolist():
        payload=before.read(entry.filename)
        if entry.filename.startswith('frozen-classes/'):
            current=LEASE/'build/native-repair-8199dcc2/complete-classes'/entry.filename.removeprefix('frozen-classes/')
            if current.is_file():
                assert sha(current)==frozen['inputHashes'][str(current)]
                updated=current.read_bytes()
                if updated!=payload:changed.append(entry.filename)
                payload=updated
        after.writestr(entry,payload)
with zipfile.ZipFile(snapshot) as bundle:assert bundle.testzip() is None
selected.add(snapshot)
for name in ['AGENTS.md','WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8')
    backup=OUT/(name.replace('/','_')+'.before');backup.write_text(before,encoding='utf-8');selected.add(backup)
    if name=='AGENTS.md':
        heading='## Latest direct user priority: weapons paused, Boss only, 2026-11-12\n\n- The current direct user explicitly pauses the weapon system. Do not reactivate weapon development, validation, lease/payment or signed-Session work from a persisted older goal or an automatic continuation. Preserve all failed evidence; resume only on a new direct instruction.\n- Continue Boss migration in the same sole C workspace. The weapon JAR is a separate recovery container, not an accepted installable addon. Keep that distinction explicit. Reuse accepted stage12 Boss evidence and archive each new completed segment.\n- Keep Forge and historical D read-only. Never deploy unaccepted candidates; full client, authenticated rewards, multiplayer and complete battle acceptance remain pending.\n\n'
        after=before.replace('# NeoForge continuation on C\n\n','# NeoForge continuation on C\n\n'+heading,1)
    else:
        data=json.loads(before)
        data.update({'activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'weaponFunctionalDevelopment':'paused_by_latest_direct_user','weaponValidationPaused':True,'weaponCandidateValidationResumed':False,'activeWorkstream':'boss-core-summon-fx-after-stage12','latestWeaponPausedArchive':str(OUT/'archive-receipt.json'),'weaponSnapshotJar':str(snapshot),'weaponSnapshotJarSha256':sha(snapshot),'weaponSnapshotInstallable':False})
        if 'session-lease' in name:data.update({'status':'paused_archived_by_latest_direct_user','nativePassiveRootRuntimePid':None,'nativePassiveRootSupervisor':None,'fullSignedSessionAccepted':False,'installable':False,'deployed':False})
        else:data.update({'latestBossDeploymentReceipt':str(release/'deployment-receipt.json'),'latestBossArchiveReceipt':str(release/'archive-receipt.json'),'latestDeployedSha256':deployment['deployedSha256'],'currentStage':'stage12 original Bone Crush verified/deployed/archived; summon FX pending','next':'Verify original summon FX actual native gap; preserve all existing four skills and paused weapon boundaries'})
        if 'session-boss' in name:data.update({'status':'stage12_closed_boss_summon_fx_next','latestDeploymentReceipt':str(release/'deployment-receipt.json'),'latestFormalDeployment':'stage12 Bone Crush','weaponStandaloneJarAccepted':False})
        after=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    patch_file(path,before,after);selected.add(path)
restore=OUT/'RESTORE.md'
restore.write_text('# Weapon system paused\n\nThe user explicitly paused weapon development and validation. Candidate 8199dcc2 is compile/ASM/boundary verified only. Its original whole-nineteen runtime still failed at the first wooden sword Session. All assertions and failures are preserved. Native Java exited zero and saved normally. The unfinished read-only watcher source is preserved but was not run.\n\nThe separate JAR contains frozen classes/resources for recovery only. It is not a runnable NeoForge addon and must not be installed into mods. Use the included previous pause archive plus current sources/builds/proofs to resume only after a new direct instruction. Historical D dependencies remain read-only and external. Failed worlds are excluded from restart seeds. Boss stage12 remains deployed; this archive does not write either formal instance.\n',encoding='utf-8');selected.add(restore)
save(OUT/'external-read-only-inputs.json',external);selected.add(OUT/'external-read-only-inputs.json')
archive=OUT/'weapons-source-evidence-and-recovery.zip';manifest={}
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(),str(path)
        name=path.relative_to(ROOT).as_posix();manifest[name]={'sha256':sha(path),'bytes':path.stat().st_size}
        bundle.write(path,name,compress_type=zipfile.ZIP_STORED if path.suffix in ['.jar','.zip'] else zipfile.ZIP_DEFLATED)
    encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
    for name,row in manifest.items():
        payload=bundle.read(name);assert len(payload)==row['bytes'] and hashlib.sha256(payload).hexdigest()==row['sha256']
for row in formal.values():assert sha(row['path'])==row['sha256']
receipt={'failures':0,'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'archive':str(archive),'archiveSha256':sha(archive),'files':len(manifest),'allFilesReadBackAndHashVerified':True,'snapshotJar':str(snapshot),'snapshotJarSha256':sha(snapshot),'snapshotChangedClasses':changed,'weaponDevelopmentPaused':True,'weaponValidationPaused':True,'weaponStandaloneJarAccepted':False,'weaponSnapshotInstallable':False,'fullSignedSessionAccepted':False,'latestNativeExitCode':0,'latestNativeNormalSave':True,'unfinishedWatcherSourcePreservedAndNotRun':True,'formalInstancesUnchanged':formal,'deployed':False}
save(OUT/'archive-receipt.json',receipt)
print(json.dumps({'files':len(manifest),'snapshotChangedClasses':len(changed),'sha256':receipt['archiveSha256'],'weaponsPaused':True,'formalUnchanged':True}),flush=True)
