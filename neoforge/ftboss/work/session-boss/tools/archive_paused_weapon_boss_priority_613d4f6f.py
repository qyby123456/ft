import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
BOSS=ROOT/'work/session-boss'
OUT=BOSS/'archives/weapons-paused-613d4f6f-boss-priority-20261112'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

def patch_file(path,before,after):
    import difflib
    lines=list(difflib.unified_diff(before.splitlines(),after.splitlines(),n=3))[2:]
    patch='*** Begin Patch\n*** Update File: '+path.relative_to(ROOT).as_posix()+'\n'+''.join(('@@\n' if line.startswith('@@') else line+'\n') for line in lines)+'*** End Patch\n'
    command=['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch]
    result=subprocess.run(command,cwd=ROOT,capture_output=True,text=True)
    assert result.returncode==0,result.stderr
    assert path.read_text(encoding='utf-8')==after

assert not OUT.exists()
claim=load(ROOT/'WRITE_OWNER.claim')
assert claim['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease')} } | ConvertTo-Json -Compress"
processes=json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',command],encoding='utf-8-sig'))
processes=processes if isinstance(processes,list) else [processes]
assert not any(row['owned'] and row['pid']!=os.getpid() for row in processes)
formal={
 'forge':{'path':r'C:\Users\qyby\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\mods\qys-bosses.jar','sha256':'7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'},
 'neoforge':{'path':r'C:\Users\qyby\AppData\Roaming\.minecraft\versions\FarmingTales_NeoForge 机械动力\mods\qys-bosses-1.21.1.jar','sha256':'5378b5657405a14d46fa5b7ad8bad78fd89764c83a9c89d901fbf51789b5e75e'}}
for row in formal.values():assert digest(row['path'])==row['sha256']
terminal=load(WORK/'build/repair-runtime-98fb0193/terminal308.json')
debugger=load(WORK/'build/repair-runtime-98fb0193/diagnostic-terminal.json')
assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
assert debugger['exitCode']==1 and debugger['readOnly'] and not debugger['methodInvocations'] and not debugger['authorityWrites']
candidate=WORK/'reports/native-repair-private-fingerprint-candidate-613d4f6f.json'
assert digest(candidate)=='59051d1e1377b954278e76e9deadc54d433e4e8f9f03f16ea113dcde037be4c8'
OUT.mkdir(parents=True)
selected={Path(__file__),candidate,ROOT/'WRITE_OWNER.claim'}
for folder in ('weapons-paused-a08a7373','weapons-paused-dd1ad841-20261112','weapons-paused-73d460d2-20261112'):
    base=BOSS/'archives'/folder
    receipt=load(base/'archive-receipt.json')
    archive=Path(receipt.get('archive',base/'weapons-source-evidence.zip'))
    assert digest(archive)==receipt['archiveSha256']
    with zipfile.ZipFile(archive) as bundle:assert bundle.testzip() is None
    selected.update(path for path in base.iterdir() if path.is_file())
for stem in ('private-read-construction','session-revoke-diagnostic','field-gate-diagnostic','field-retirement-diagnostic','field-retirement-v2-diagnostic'):
    archive=WORK/'archives'/(stem+'-613d4f6f-20261112.zip')
    with zipfile.ZipFile(archive) as bundle:assert bundle.testzip() is None
    selected.add(archive)
selected.update(path for path in (WORK/'reports').iterdir() if path.is_file() and any(word in path.name for word in ('private-read-construction','field-retirement','private-fingerprint','private-timer-image','session-revoke','field-gate')))
for folder in ('tests/native-field-retirement-diagnostic-v3-613d4f6f','build/native-field-retirement-diagnostic-613d4f6f','proposal-private-fingerprint-composition','proposal-private-timer-expectation-v2'):
    selected.update(path for path in (WORK/folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
for name in ('prepare_native_field_retirement_diagnostic_v3.py','run_native_field_retirement_v3_613d4f6f.py','archive_gated_field_retirement_diagnostic.py'):
    selected.add(WORK/'tools'/name)
attempt=WORK/'build/repair-runtime-98fb0193'
selected.update(path for path in attempt.iterdir() if path.is_file())
for name in ('server/logs/latest.log','server/logs/debug.log','server/actual-storage-view308.json','server/.mixin.out/class/net/minecraft/nbt/CompoundTag.class'):
    path=attempt/name
    if path.is_file():selected.add(path)
previous=BOSS/'archives/weapons-paused-73d460d2-20261112/ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-73d460d2.jar'
snapshot=OUT/'ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-613d4f6f.jar'
changed=[]
with zipfile.ZipFile(previous) as before,zipfile.ZipFile(snapshot,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for entry in before.infolist():
        payload=before.read(entry)
        if entry.filename.startswith('frozen-classes/'):
            current=WORK/'build/native-repair-613d4f6f/complete-classes'/entry.filename.removeprefix('frozen-classes/')
            assert current.is_file(),str(current)
            current_bytes=current.read_bytes()
            if current_bytes!=payload:changed.append(entry.filename)
            payload=current_bytes
        after.writestr(entry,payload)
with zipfile.ZipFile(snapshot) as bundle:assert bundle.testzip() is None
assert changed
selected.add(snapshot)
for name in ('AGENTS.md','WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json','work/session-boss/CURRENT_BOSS_BATCH.json'):
    path=ROOT/name
    before=path.read_text(encoding='utf-8')
    backup=OUT/(name.replace('/','_')+'.before')
    backup.write_text(before,encoding='utf-8');selected.add(backup)
    if name=='AGENTS.md':
        heading='## Latest direct user instruction: pause weapons, Boss priority, 2026-11-12\n\n- The direct user pauses weapon development and validation. Archive all newer weapon evidence and keep its separate recovery JAR noninstallable. Earlier signed-Session reactivation does not authorize further weapon work under this instruction.\n- Keep the same sole C writer. Prioritize Boss migration in single-function increments; verify, back up, deploy each accepted NeoForge increment and archive it. Forge and historical D remain read-only. Never deploy an unaccepted weapon candidate or claim full Boss/client/authenticated acceptance.\n\n'
        after=before.replace('# NeoForge continuation on C\n\n','# NeoForge continuation on C\n\n'+heading,1)
    else:
        data=json.loads(before)
        data.update({'activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'weaponFunctionalDevelopment':'paused_by_latest_direct_user','weaponValidationPaused':True,'activeWorkstream':'boss-core-stage12-bone-crush','currentScopeInstruction':'Pause and compress weapons; prioritize Boss; verify/deploy/archive each accepted Boss increment only','latestWeaponPauseCheckpoint':str(OUT/'archive-receipt.json'),'weaponStandaloneJarAccepted':False,'fullSignedSessionAccepted':False})
        if name=='WRITE_OWNER.json':data.update({'formalInstancePolicy':'verified_boss_increment_neoforge_only_forge_read_only'})
        elif 'session-lease' in name:data.update({'status':'paused_archived_by_user','nativePassiveRootRuntimePid':None,'nativePassiveRootSupervisor':None,'installable':False,'deployed':False})
        else:data.update({'status':'stage12_bone_crush_preflight','next':'Compare original Bone Crush against native stage11 baseline; restore only that confirmed skill gap','currentFormalInstancePolicy':'verified_boss_increment_neoforge_only_forge_read_only','weaponSnapshotJar':str(snapshot),'weaponSnapshotJarSha256':digest(snapshot)})
        after=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    patch_file(path,before,after);selected.add(path)
restore=OUT/'RESTORE.md'
restore.write_text('# Weapon system paused\n\nLatest candidate 613d4f6f is NOT accepted or installable. The separate JAR is a recovery container with frozen-classes and frozen-resources, not a loader mod. Do not place it in mods.\n\nRestore the bundled ancestor archives in order a08a7373, dd1ad841, 73d460d2, then private-read-construction and diagnostic supplements. Historical D compiler dependencies remain external read-only inputs recorded by those receipts. Failed worlds are retained at original paths and MUST NOT be used as restart seeds.\n\nLatest native process exited zero and saved normally. The read-only JDI helper exited one due to JDWP Error 35. Full nineteen failed and no acceptance was granted. Actual transformed putLong wrapper mismatch was observed but is deliberately unfixed because the user paused weapon work. Resume only on explicit user request.\n',encoding='utf-8')
selected.add(restore)
manifest={}
archive=OUT/'weapons-source-evidence-and-recovery.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(),str(path)
        name=path.relative_to(ROOT).as_posix();manifest[name]={'sha256':digest(path),'bytes':path.stat().st_size}
        bundle.write(path,name,compress_type=zipfile.ZIP_STORED if path.suffix in ('.zip','.jar') else zipfile.ZIP_DEFLATED)
    encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
    for name,row in manifest.items():
        payload=bundle.read(name);assert len(payload)==row['bytes'] and hashlib.sha256(payload).hexdigest()==row['sha256']
for row in formal.values():assert digest(row['path'])==row['sha256']
receipt={'failures':0,'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'archive':str(archive),'archiveSha256':digest(archive),'files':len(manifest),'allFilesReadBackAndHashVerified':True,'snapshotJar':str(snapshot),'snapshotJarSha256':digest(snapshot),'snapshotChangedClasses':changed,'restoreInstructionsSha256':digest(restore),'weaponDevelopmentPaused':True,'weaponRuntimeAccepted':False,'weaponStandaloneJarAccepted':False,'deployed':False,'latestJavaExitCode':0,'latestJavaNormalSave':True,'latestDebuggerExitCode':1,'debuggerErrorPreserved':'JDWP Error 35 while reading arguments','failedWorldsExcludedFromRestartSeeds':True,'ownedJavaPythonRemaining':0,'formalInstancesUnchanged':formal}
save(OUT/'archive-receipt.json',receipt)
print(json.dumps({'archive':str(archive),'sha256':receipt['archiveSha256'],'files':len(manifest),'snapshotChangedClasses':len(changed),'weaponsPaused':True,'formalUnchanged':True}),flush=True)
