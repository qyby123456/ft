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
OUT=BOSS/'archives/weapons-paused-5f283607-boss-priority-20261112'
ATTEMPT=LEASE/'build/repair-runtime-bc621ceb'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

helper=BOSS/'tools/archive_paused_weapon_boss_priority_613d4f6f_v2.py'
namespace={'__file__':str(helper)}
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('assert not OUT.exists()')],str(helper),'exec'),namespace)
patch_file=namespace['patch_file']
assert not OUT.exists()
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
prior_path=BOSS/'archives/weapons-paused-8199dcc2-boss-priority-20261112/archive-receipt.json'
prior=load(prior_path)
for row in prior['formalInstancesUnchanged'].values():assert sha(row['path'])==row['sha256']
assert sha(prior['archive'])==prior['archiveSha256']
assert sha(prior['snapshotJar'])==prior['snapshotJarSha256']
candidate=LEASE/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
assert sha(candidate)=='482fd005e8287ef41e48eced0ca5bd740df8b78580526165a2b550d47f666b10'
frozen=load(candidate)
for name,wanted in frozen['inputHashes'].items():assert sha(name)==wanted,name
checkpoint=LEASE/'archives/aliased-entity-expected-image-5f283607-LIVE-PROGRESS-20261112.zip'
assert sha(checkpoint)=='b5c2a95703ad33168c8a6da79ebadafe83568753ed599b043f42df202cf83af2'
OUT.mkdir(parents=True)
selected={Path(__file__),helper,candidate,prior_path,Path(prior['archive']),Path(prior['snapshotJar']),checkpoint,ROOT/'WRITE_OWNER.claim'}
live=load(ATTEMPT/'live-process308.json')
assert live['pid']==21176 and live['ownedDebuggerPid']==68388
query="Get-CimInstance Win32_Process -Filter 'ProcessId = 21176' | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;owned=($_.CommandLine -like '*repair-runtime-bc621ceb*')} } | ConvertTo-Json -Compress"
raw=subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
process= json.loads(raw) if raw else None
stop={'userRequestedPause':True,'requestedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'pid':21176,'automaticWeaponContinuationForbidden':True,'fullNineteenAccepted':False,'goalStatus':'paused','normalStopQueued':False,'targetAuthorityModified':False}
if process is not None:
    assert process['pid']==21176 and process['owned']
    jar=LEASE/'build/repair-runtime-0bd91833/normal-stop-helper.jar'
    assert jar.is_file()
    command=['C:/Program Files/Zulu/zulu-21/bin/java.exe','--add-modules','jdk.attach','-cp',str(jar),'NormalStopAttach','21176',str(jar)]
    with (OUT/'normal-stop-request.log').open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT,timeout=45)
    stop.update({'attachExitCode':result.returncode,'normalStopQueued':result.returncode==0,'shutdownHelperSha256':sha(jar),'shutdownHelperHistoricalLiteralIsNotAnAcceptanceClaim':True,'inFlightServerCallbackMayNeedToReturnBeforeNormalStop':True})
    assert result.returncode==0
else:stop['nativeAlreadyTerminal']=True
save(OUT/'pause-request.json',stop)
selected.update(path for path in OUT.iterdir() if path.is_file())
for path in ATTEMPT.iterdir():
    if path.is_file():
        target=OUT/('runtime-prefix-'+path.name)
        with path.open('rb') as source:payload=source.read()
        with target.open('xb') as destination:destination.write(payload)
        selected.add(target)
for folder in ['proposal-aliased-entity-expected-image','tests/native-aliased-domain-value-diff-watch-5f283607','reports/final-acceptance-audit-5f283607-20261112','reports/aliased-entity-expected-image-live-checkpoint-20261112']:
    selected.update(path for path in (LEASE/folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
for name in ['ALIASED_ENTITY_EXPECTED_IMAGE_5F283607.md','tools/archive_aliased_entity_expected_image_20261112.py','tools/audit_current_signed_session_acceptance.py']:
    selected.add(LEASE/name)
external={}
for name,wanted in frozen['inputHashes'].items():
    path=Path(name)
    if path.resolve().is_relative_to(ROOT):selected.add(path)
    else:external[name]=wanted
snapshot=OUT/'ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-5f283607.jar'
changed=[]
with zipfile.ZipFile(prior['snapshotJar']) as before,zipfile.ZipFile(snapshot,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for entry in before.infolist():
        payload=before.read(entry.filename)
        if entry.filename.startswith('frozen-classes/'):
            current=LEASE/'build/native-repair-5f283607/complete-classes'/entry.filename.removeprefix('frozen-classes/')
            if current.is_file():
                assert sha(current)==frozen['inputHashes'][str(current)]
                updated=current.read_bytes()
                if updated!=payload:changed.append(entry.filename)
                payload=updated
        elif entry.filename=='META-INF/MANIFEST.MF':payload=b'Manifest-Version: 1.0\r\nImplementation-Title: FT Weapons Paused Snapshot\r\nImplementation-Version: 5f283607\r\nFTBoss-Installable: false\r\n\r\n'
        elif entry.filename=='README.txt':payload=b'Weapon system frozen by explicit user pause. Candidate 5f283607. Recovery container with frozen classes/resources, not a runnable NeoForge addon. No complete Session, restart, multiplayer or installable acceptance is claimed. No automatic weapon development or validation. Boss migration proceeds separately. Historical external inputs are listed in the accompanying archive. In-flight runtime evidence is a partial prefix only.\n'
        after.writestr(entry,payload)
with zipfile.ZipFile(snapshot) as bundle:
    assert bundle.testzip() is None
    assert not any(name.startswith(('net/farmingtales/ftboss/boss/','net/farmingtales/ftboss/butcherskills/')) for name in bundle.namelist())
assert len(changed)==7,changed
selected.add(snapshot)
for name in ['AGENTS.md','WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8')
    backup=OUT/(name.replace('/','_')+'.before')
    with backup.open('x',encoding='utf-8') as stream:stream.write(before)
    selected.add(backup)
    if name=='AGENTS.md':
        heading='## Current direct user pause overrides persisted Session goal, 2026-11-12\n\n- Pause weapon development and validation immediately. The previous persisted signed-Session prerequisite goal is explicitly paused by the current direct user instruction; do not resume it automatically. Preserve candidate5f283607 and failed/partial runtime evidence. The separate weapon JAR is a recovery snapshot, not an accepted runnable addon.\n- Continue only Boss migration in this same sole C workspace. Request normal shutdown of the in-flight isolated weapon observer; preserve its partial prefix and eventual terminal outcome without converting it into acceptance or a restart seed. No new weapon runs.\n- Keep both formal instances and historical D read-only while finishing the remaining Boss candidate acceptance. Never deploy an unaccepted candidate. Archive each completed Boss segment and preserve rewards/base item ownership independently of paused weapons.\n\n'
        after=before.replace('# NeoForge continuation on C\n\n','# NeoForge continuation on C\n\n'+heading,1)
    else:
        data=json.loads(before)
        data.update({'weaponFunctionalDevelopmentPaused':True,'weaponFunctionalDevelopment':'paused_by_current_direct_user','weaponValidationPaused':True,'weaponCandidateValidationResumed':False,'currentSignedSessionGoalStatus':'paused_by_current_direct_user','activeWorkstream':'boss-only-after-explicit-weapon-pause-5f283607','latestWeaponPausedArchive':str(OUT/'archive-receipt.json'),'weaponSnapshotJar':str(snapshot),'weaponSnapshotJarSha256':sha(snapshot),'weaponSnapshotInstallable':False,'fullSignedSessionAccepted':False,'weaponPauseRequest':str(OUT/'pause-request.json'),'noNewWeaponRuns':True})
        if 'session-lease' in name:data['status']='paused_current_direct_user_runtime_normal_stop_requested'
        if 'session-boss' in name:data['nextBossScope']='Natural entity scheduling and native summon FX ingress; no weapon prerequisite continuation'
        after=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    patch_file(path,before,after);selected.add(path)
restore=OUT/'RESTORE.md'
with restore.open('x',encoding='utf-8') as stream:stream.write('# Weapons paused; Boss priority\n\nThe current direct user pauses the weapon system. The previous Session goal is paused, not completed. Candidate5f283607, its complete frozen graph, current source delta, known diagnostic failure and all earlier pause archives are retained. The separate JAR is a recovery container only; do not place it in mods or describe it as an accepted addon.\n\nRuntime files are bounded live prefixes. A normal stop was requested; an in-flight server callback may need to finish before shutdown. This partial run and its eventual world are not restart seeds or complete acceptance. Do not launch replacement weapon validation. Preserve terminal evidence in a separate supplement when the existing process exits. Historical D remains external read-only and hashes are recorded. Both formal instances retain stage12/Forge hashes. Continue Boss work only.\n')
selected.add(restore)
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
for row in prior['formalInstancesUnchanged'].values():assert sha(row['path'])==row['sha256']
receipt={'failures':0,'archive':str(archive),'archiveSha256':sha(archive),'files':len(manifest),'allFilesReadBackAndHashVerified':True,'snapshotJar':str(snapshot),'snapshotJarSha256':sha(snapshot),'snapshotChangedClasses':changed,'weaponDevelopmentPaused':True,'weaponValidationPaused':True,'weaponStandaloneJarAccepted':False,'weaponSnapshotInstallable':False,'fullSignedSessionAccepted':False,'runtimePrefixOnly':True,'normalStopRequested':stop['normalStopQueued'],'formalInstancesUnchanged':prior['formalInstancesUnchanged'],'deployed':False,'externalReadOnlyInputs':len(external)}
save(OUT/'archive-receipt.json',receipt)
print(json.dumps({'files':len(manifest),'snapshotJar':str(snapshot),'archiveSha256':receipt['archiveSha256'],'normalStopRequested':receipt['normalStopRequested'],'weaponPaused':True,'formalUnchanged':True}),flush=True)
