import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-lease'
BOSS = ROOT / 'work/session-boss'
OUTPUT = BOSS / 'archives/weapons-paused-73d460d2-20261112'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    return hashlib.file_digest(Path(path).open('rb'), 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

assert not OUTPUT.exists()
candidate_path = WORK / 'reports/native-repair-session-timer-expectation-candidate-73d460d2.json'
candidate = load(candidate_path)
parent_path = WORK / 'reports/native-repair-ranged-use-candidate-dd1ad841.json'
for receipt in (candidate, load(parent_path)):
    for name, wanted in receipt['inputHashes'].items():
        assert digest(name) == wanted, name
preflight = load(WORK / 'reports/goal-reactivation-session-diagnostic-20261112.json')
formal = preflight['formalInstancesBefore']
if isinstance(formal, list):
    raise AssertionError('Inspect formal evidence schema before writing')
for row in formal.values():
    assert digest(row['path']) == row['sha256'], row['path']
process_command = 'Get-CimInstance Win32_Process | Where-Object { $_.Name -match '+chr(39)+'^(java|javaw|python|pythonw).exe$'+chr(39)+' } | ForEach-Object { [PSCustomObject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match '+chr(39)+'session-lease|session-boss'+chr(39)+')} } | ConvertTo-Json -Compress'
processes = json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',process_command],text=True))
assert not any(row['owned'] and row['pid'] != os.getpid() for row in processes)
dependencies = []
for folder in ('weapons-paused-a08a7373','weapons-paused-dd1ad841-20261112'):
    path = BOSS / 'archives' / folder / 'archive-receipt.json'
    receipt = load(path)
    assert receipt['failures'] == 0
    archive = Path(receipt['archive']) if 'archive' in receipt else path.parent / 'weapons-source-evidence.zip'
    assert digest(archive) == receipt['archiveSha256']
    assert digest(path.parent / 'manifest.jsonl') == receipt['manifestSha256']
    assert digest(path.parent / 'RESTORE.md') == receipt['restoreInstructionsSha256']
    with zipfile.ZipFile(archive) as bundle:
        assert bundle.testzip() is None
    dependencies.append({'receipt':str(path),'receiptSha256':digest(path),'archive':str(archive),'archiveSha256':digest(archive)})
selected = {Path(__file__), ROOT / 'AGENTS.md', ROOT / 'WRITE_OWNER.claim', candidate_path, parent_path}
runtime_rows = []
for name in ('native-repair-runtime-damage-authorization-v2-dd1ad841.json','native-repair-runtime-expectation-cursor-dd1ad841.json','native-repair-runtime-full-nineteen-signed-73d460d2.json'):
    path = WORK / 'reports' / name
    receipt = load(path)
    attempt = Path(receipt['attempt'])
    terminal = load(attempt / 'terminal308.json')
    assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
    selected.add(path)
    selected.update(path for path in attempt.iterdir() if path.is_file())
    for relative in ('server/actual-storage-view308.json','server/logs/latest.log','server/logs/debug.log'):
        path = attempt / relative
        if path.is_file(): selected.add(path)
    runtime_rows.append({'attempt':str(attempt),'javaExitCode':0,'normalSave':True,'observerFailures':receipt['probe']['failures'],'accepted':False,'restartSeedAllowed':False})
for folder in ('proposal-session-timer-expectation','build/native-repair-73d460d2','build/native-repair-62834134','build/expectation-cursor-diagnostic-compile','build/damage-authorization-guard-dd1ad841'):
    selected.update(path for path in (WORK / folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
for folder in ('tools','reports','tests'):
    for path in (WORK / folder).rglob('*'):
        if path.is_file() and '__pycache__' not in path.parts and any(key in str(path) for key in ('20261112','damage-authorization','damage_authorization','expectation-cursor','expectation_cursor','session-timer-expectation','session_timer_expectation','nineteen-signed-73','nineteen_signed_73','canonical_cursor','SessionTimerExpectation')):
            selected.add(path)
OUTPUT.mkdir(parents=True)
snapshot = OUTPUT / 'ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-73d460d2.jar'
previous = load(BOSS / 'archives/weapons-paused-dd1ad841-20261112/archive-receipt.json')
names = ['FoodWeaponOriginalSequence$Session','FoodWeaponSourceLedger','NativeCapabilityInstallationProof170','OwnerFieldInstallationProof']
changes = {'frozen-classes/net/farmingtales/ftboss/item/'+name+'.class':(WORK / 'build/native-repair-73d460d2/complete-classes/net/farmingtales/ftboss/item' / (name+'.class')).read_bytes() for name in names}
with zipfile.ZipFile(previous['snapshotJar']) as source, zipfile.ZipFile(snapshot,'x',compression=zipfile.ZIP_DEFLATED) as target:
    assert 'META-INF/neoforge.mods.toml' not in source.namelist()
    assert set(changes).issubset(source.namelist())
    for entry in source.infolist():
        if entry.filename != 'PAUSED_STATE.json': target.writestr(entry,changes.get(entry.filename,source.read(entry.filename)))
    target.writestr('PAUSED_STATE.json',json.dumps({'candidate':'73d460d2','weaponDevelopmentPaused':True,'completeSignedSessionAccepted':False,'recoverySnapshotOnly':True,'installable':False,'resumeOnlyOnExplicitUserInstruction':True}))
with zipfile.ZipFile(snapshot) as bundle:
    assert bundle.testzip() is None
    for name,payload in changes.items(): assert bundle.read(name) == payload
selected.add(snapshot)
save(OUTPUT / 'external-dependencies.json',{'archives':dependencies,'parentCandidate':str(parent_path),'parentSha256':digest(parent_path),'frozenInputHashes':candidate['inputHashes'],'historicalInputsReadOnly':True,'failedWorldsCannotBeRestartSeeds':True})
restore = OUTPUT / 'RESTORE.md'
restore.write_text('# Paused weapon checkpoint 73d460d2'+chr(10)*2+'Restore the a08a7373 base, dd1ad841 delta and this delta in order into a NEW empty workspace. Verify receipts, manifests, blob hashes, sizes and destination containment. Historical D and both formal instances must never be overwritten by restoration. External immutable inputs remain at their original locations; verify external-dependencies.json before reuse.'+chr(10)*2+'The TIMER whole-image cursor bridge compiles and passes ASM. 569 unrelated method bodies remain identical; four raw classes changed including two fingerprint pins. The complete nineteen-item test still fails on the first wooden sword: Nested or unbound Map write cannot claim a domain. All three isolated diagnostic/native processes exited zero and saved normally, but all observer acceptances failed. Do not use their worlds as accepted restart seeds. Preserve the earlier compile failure and superseded candidate evidence. The separate weapon JAR is a recovery snapshot without a mod entrypoint, NOT a runnable accepted addon. Do not install it. Weapon work is paused at the explicit user request; Boss has priority. No originals were deleted or moved.'+chr(10),encoding='utf-8')
selected.update((restore,OUTPUT / 'external-dependencies.json'))
now = datetime.datetime.now(datetime.timezone.utc).isoformat()
for path in (ROOT / 'WRITE_OWNER.json',WORK / 'CURRENT_INCREMENTAL_BATCH.json',BOSS / 'CURRENT_BOSS_BATCH.json'):
    data = load(path)
    data.update({'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidationResumed':False,'activeRuntimePid':None,'activeSupervisor':None,'weaponFunctionalDevelopment':'paused_by_user','updatedAt':now,'currentScopeInstruction':'Pause weapon functional work and validation; archive latest failures; prioritize Boss migration; deploy only completed verified Boss increments then archive each segment','latestWeaponPauseCheckpoint':str(OUTPUT / 'archive-receipt.json')})
    if path.name == 'WRITE_OWNER.json': data.update({'currentGoalStatus':'paused','nativePassiveRootRuntimePid':None,'nativePassiveRootSupervisor':None,'activeWorkstream':'boss-next-original-skill','nextNativeContinuationWork':'Weapon work paused; restore this archive only after explicit request','nativeContinuationStatus':'73d460d2 full nineteen observer failed; native JVM exited zero and saved; archived, not installable'})
    elif path.parent == WORK: data.update({'status':'paused_by_user_terminal_failed_archived','nineteenSignedAndRestartAccepted':False,'nextResumeTarget':'Boss priority; weapon resume requires explicit instruction'})
    else: data.update({'activeWorkstream':'boss-next-original-skill','weaponLatestSnapshotJar':str(snapshot),'weaponLatestSnapshotInstallable':False})
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
    selected.add(path)
archive = OUTPUT / 'weapons-source-evidence-delta.zip'
manifest = OUTPUT / 'manifest.jsonl'
stored = set()
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle, manifest.open('x',encoding='utf-8') as stream:
    for path in sorted(selected):
        assert path.resolve().is_relative_to(ROOT) and not path.is_symlink()
        checksum=digest(path); member='blobs/'+checksum
        if checksum not in stored: bundle.write(path,member); stored.add(checksum)
        stream.write(json.dumps({'workspaceRelativePath':path.relative_to(ROOT).as_posix(),'archiveMember':member,'sha256':checksum,'bytes':path.stat().st_size},ensure_ascii=False)+chr(10))
with zipfile.ZipFile(archive) as bundle:
    for member in bundle.namelist(): assert hashlib.sha256(bundle.read(member)).hexdigest() == member.removeprefix('blobs/')
receipt={'failures':0,'createdAt':now,'archive':str(archive),'archiveSha256':digest(archive),'manifestSha256':digest(manifest),'restoreInstructionsSha256':digest(restore),'snapshotJar':str(snapshot),'snapshotJarSha256':digest(snapshot),'files':len(selected),'uniqueBlobs':len(stored),'allBlobsReadBackVerified':True,'externalDependencies':dependencies,'runtimeRows':runtime_rows,'processAudit':processes,'formalInstancesUnchanged':formal,'weaponDevelopmentPaused':True,'fullSignedSessionAccepted':False,'weaponStandaloneRunnableJarAccepted':False,'installable':False,'deployed':False,'scratchRetained':True}
save(OUTPUT / 'archive-receipt.json',receipt)
print(json.dumps({'failures':0,'files':len(selected),'archiveSha256':receipt['archiveSha256'],'snapshotSha256':receipt['snapshotJarSha256'],'receipt':str(OUTPUT / 'archive-receipt.json')}),flush=True)
