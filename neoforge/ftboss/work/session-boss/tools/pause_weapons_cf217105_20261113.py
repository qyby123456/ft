import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
BOSS = ROOT / 'work/session-boss'
LEASE = ROOT / 'work/session-lease'
OUT = BOSS / 'archives/weapons-paused-cf217105-boss-priority-v2-20261113'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

helper = LEASE / 'tools/reactivate_goal_after_boss_stage12.py'
namespace = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('assert not OUT.exists()')], str(helper), 'exec'), namespace)
patch_file = namespace['replace_file']
assert ROOT == Path('C:/ft工程文件C/FTBoss-NeoForge-1.21.1-移植-20261022')
assert not OUT.exists()
assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == load(ROOT / 'WRITE_OWNER.json')['activeWriterThreadId']
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-lease|session-boss|continue_native_signed_acceptance|run_native_material_proc|run_native_original_nineteen')} } | ConvertTo-Json -Compress"
raw = subprocess.check_output(['powershell', '-NoProfile', '-Command', query], encoding='utf-8-sig').strip()
processes = json.loads(raw) if raw else []
processes = processes if isinstance(processes, list) else [processes]
assert not any(row['owned'] and row['pid'] != os.getpid() for row in processes)
pipeline = LEASE / 'reports/native-signed-acceptance-pipeline-v2-cf217105'
assert load(pipeline / 'terminal.json')['failures'] == 1
runtime_report = LEASE / 'reports/native-repair-runtime-material-proc-whole-nineteen-cf217105.json'
runtime = load(runtime_report)
assert runtime['exitCode'] == 0 and runtime['probe']['failures'] == 1
attempt = Path(runtime['attempt'])
terminal = load(attempt / 'terminal308.json')
assert terminal['exitCode'] == 0 and terminal['completeSave'] and terminal['stopSent']
prior_path = BOSS / 'archives/weapons-paused-5f283607-boss-priority-20261112/archive-receipt.json'
prior = load(prior_path)
for row in prior['formalInstancesUnchanged'].values():
    assert sha(row['path']) == row['sha256']
assert sha(prior['snapshotJar']) == prior['snapshotJarSha256']
candidate_path = LEASE / 'reports/native-repair-material-proc-integration-candidate-cf217105.json'
candidate = load(candidate_path)
for name, expected in candidate['inputHashes'].items():
    assert sha(name) == expected, name
OUT.mkdir(parents=True)
save(OUT / 'process-audit.json', processes)
if not (pipeline / 'cancel-request.json').exists():
    save(pipeline / 'cancel-request.json', {'reason': 'Current direct user pauses weapon system; Boss priority', 'noFurtherStageStarts': True})
save(OUT / 'pause-request.json', {'createdAt': datetime.datetime.now().astimezone().isoformat(), 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'persistedSessionGoal': 'paused', 'nativeAlreadyTerminal': True, 'nativeExitCode': 0, 'observerFailures': 1, 'failure': runtime['probe'].get('failure'), 'completeSave': True, 'failedWorldIsNotRestartSeed': True, 'noNewWeaponRuns': True})
snapshot = OUT / 'ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-cf217105.jar'
classes = Path(candidate['attempt']) / 'complete-classes'
changed = []
written = set()
with zipfile.ZipFile(prior['snapshotJar']) as before, zipfile.ZipFile(snapshot, 'x', compression=zipfile.ZIP_DEFLATED) as after:
    for entry in before.infolist():
        payload = before.read(entry.filename)
        if entry.filename.startswith('frozen-classes/'):
            current = classes / entry.filename.removeprefix('frozen-classes/')
            if current.is_file():
                assert sha(current) == candidate['inputHashes'][str(current)]
                replacement = current.read_bytes()
                if replacement != payload:
                    changed.append(entry.filename)
                payload = replacement
        elif entry.filename == 'META-INF/MANIFEST.MF':
            payload = b'Manifest-Version: 1.0\r\nImplementation-Title: FT Weapons Paused Recovery\r\nImplementation-Version: cf217105\r\nFTBoss-Installable: false\r\n\r\n'
        elif entry.filename == 'README.txt':
            payload = b'Paused weapon recovery container cf217105. Not a runnable addon. Native whole19 failed original assertion; preserve failure. Do not install or resume automatically. Boss migration continues separately.\n'
        after.writestr(entry, payload)
        written.add(entry.filename)
    for current in sorted(classes.rglob('*.class')):
        name = 'frozen-classes/' + current.relative_to(classes).as_posix()
        if name not in written and current.name.startswith(('OwnerMaterialProcAuthority', 'MaterialProcExpectedPlan')):
            assert sha(current) == candidate['inputHashes'][str(current)]
            after.write(current, name)
            changed.append(name)
with zipfile.ZipFile(snapshot) as bundle:
    assert bundle.testzip() is None
    assert not any(name.startswith(('net/farmingtales/ftboss/boss/', 'net/farmingtales/ftboss/butcherskills/')) for name in bundle.namelist())
selected = {Path(__file__), candidate_path, runtime_report, prior_path, ROOT / 'WRITE_OWNER.claim'}
references = []
for relative in ['material-proc-native-implementation-checkpoint-20261112', 'native-signed-acceptance-coordinator-checkpoint-cf217105', 'native-signed-recovery-audit-20261113-0833']:
    receipt_path = LEASE / 'reports' / relative / 'archive-receipt.json'
    receipt = load(receipt_path)
    expected = receipt.get('archiveSha256', receipt.get('sha256'))
    assert expected and sha(receipt['archive']) == expected
    references.append({'receipt': str(receipt_path), 'receiptSha256': sha(receipt_path), 'archive': receipt['archive'], 'archiveSha256': expected})
    selected.add(receipt_path)
external = {}
for name, expected in candidate['inputHashes'].items():
    path = Path(name)
    if path.resolve().is_relative_to(ROOT):
        selected.add(path)
    else:
        external[name] = expected
for folder in [pipeline, attempt, LEASE / 'proposal-material-proc-native-integration']:
    selected.update(path for path in folder.rglob('*') if path.is_file() and '__pycache__' not in path.parts and path.name != 'session.lock')
for name in ['AGENTS.md', 'WRITE_OWNER.json', 'work/session-lease/CURRENT_INCREMENTAL_BATCH.json', 'work/session-boss/CURRENT_BOSS_BATCH.json']:
    path = ROOT / name
    before = path.read_text(encoding='utf-8')
    save(OUT / (name.replace('/', '_') + '.before.json'), {'path': str(path), 'text': before, 'sha256': sha(path)})
    if name == 'AGENTS.md':
        heading = '## Current direct user: weapons paused; Boss priority, 2026-11-13\n\n- The direct user pauses all weapon development and validation, including lease/payment/signed Session. The persisted Session goal is paused. No automatic reactivation or queued weapon runs. cf217105 whole19 is terminal with an original assertion failure; preserve it and never use its world as a restart seed. Coordinator v2 terminated before dependent stages.\n- Continue Boss migration with the same sole C writer. Separate weapon recovery JAR and source/evidence archive are in work/session-boss/archives/weapons-paused-cf217105-boss-priority-v2-20261113. This recovery JAR is NOT an installable NeoForge addon; runnable weapon addon work remains paused.\n- Reuse stage12/stage13 evidence. Verify each Boss segment, deploy only accepted increments under the user release instruction with backup and rollback hashes, then archive. Unaccepted stage13 remains NOT-INSTALLABLE. Forge and historical D stay read-only. Preserve Boss base rewards independently of paused weapon growth.\n\n'
        after = before.replace('# NeoForge continuation on C\n\n', '# NeoForge continuation on C\n\n' + heading, 1)
    else:
        value = json.loads(before)
        value.update({'weaponFunctionalDevelopmentPaused': True, 'weaponFunctionalDevelopment': 'paused_by_current_direct_user', 'weaponValidationPaused': True, 'weaponCandidateValidationResumed': False, 'currentSignedSessionGoalStatus': 'paused_by_current_direct_user', 'noNewWeaponRuns': True, 'currentGoalStatus': 'paused', 'activeWorkstream': 'boss-only-after-weapon-pause-cf217105', 'activeRuntimePid': None, 'activeRuntimePids': [], 'activeSupervisor': None, 'activeDebuggerPid': None, 'latestWeaponPausedArchive': str(OUT / 'archive-receipt.json'), 'weaponSnapshotJar': str(snapshot), 'weaponSnapshotJarSha256': sha(snapshot), 'weaponSnapshotInstallable': False, 'weaponStandaloneJarAccepted': False, 'fullSignedSessionAccepted': False, 'formalInstancePolicy': 'verified_boss_increments_only_with_backup', 'currentFormalInstancePolicy': 'verified_boss_increments_only_with_backup', 'weaponPauseRequest': str(OUT / 'pause-request.json')})
        if 'session-lease' in name:
            value['status'] = 'paused_user_directive_cf217105_terminal_failure_archived'
        if 'session-boss' in name:
            value['status'] = 'boss_priority_stage13_natural_scheduling_acceptance'
            value['nextBossScope'] = 'Natural native entity scheduling for stage13 summon FX; no weapon prerequisite runs'
        after = json.dumps(value, ensure_ascii=False, indent=2) + '\n'
    patch_file(path, before, after)
    selected.add(path)
restore = '# Weapon system paused; Boss priority\n\nThe persisted Session goal is paused, not completed. cf217105 native whole19 exited normally and saved, but failed the original reusable actual-hurt authority assertion. Its world is failure evidence only and must never be a restart seed. No dependent JVM stages started.\n\nThe separate weapon JAR contains frozen recovery classes and resources, without executable NeoForge registration. Keep it outside mods. A usable independently registered weapon addon is deliberately deferred until weapon development resumes; do not claim it is completed. Boss reward/base material ownership remains in the independent Boss core.\n\nRestore into a separate C workspace only after a direct user resume instruction: verify every archive reference SHA256, extract the implementation checkpoint, serial tools checkpoint, recovery audit and this delta; verify ARCHIVE_MANIFEST.json members. Historical external inputs are read-only and listed. Preserve the sole writer claim. Both formal instance hashes are unchanged at this pause. Continue Boss acceptance; deploy only verified increments with backup and rollback receipt.\n'
namespace_create = {'__file__': str(BOSS / 'tools/prepare_boss_blood_tide_stage10.py')}
helper_text = Path(namespace_create['__file__']).read_text(encoding='utf-8')
exec(compile(helper_text[:helper_text.index('probe=(WORK/')], namespace_create['__file__'], 'exec'), namespace_create)
namespace_create['create'](OUT / 'RESTORE.md', restore)
save(OUT / 'archive-references.json', references)
save(OUT / 'external-read-only-inputs.json', external)
selected.update(path for path in OUT.iterdir() if path.is_file())
archive = OUT / 'weapons-source-evidence-and-recovery.zip'
manifest = {}
with zipfile.ZipFile(archive, 'x', compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(), path
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {'sha256': sha(path), 'bytes': path.stat().st_size}
        bundle.write(path, name, compress_type=zipfile.ZIP_STORED if path.suffix in ['.jar', '.zip'] else zipfile.ZIP_DEFLATED)
    encoded = json.dumps(manifest, ensure_ascii=False, indent=2).encode('utf-8')
    bundle.writestr('ARCHIVE_MANIFEST.json', encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json') == encoded
    for name, row in manifest.items():
        payload = bundle.read(name)
        assert len(payload) == row['bytes'] and hashlib.sha256(payload).hexdigest() == row['sha256']
for row in prior['formalInstancesUnchanged'].values():
    assert sha(row['path']) == row['sha256']
receipt = {'failures': 0, 'archive': str(archive), 'archiveSha256': sha(archive), 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'snapshotJar': str(snapshot), 'snapshotJarSha256': sha(snapshot), 'snapshotChangedClasses': changed, 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'weaponStandaloneJarAccepted': False, 'weaponSnapshotInstallable': False, 'fullSignedSessionAccepted': False, 'nativeTerminal': terminal, 'ownedProcessesRemaining': 0, 'formalInstancesUnchanged': prior['formalInstancesUnchanged'], 'deployed': False, 'references': references}
save(OUT / 'archive-receipt.json', receipt)
print(json.dumps({'archive': str(archive), 'files': len(manifest), 'sha256': receipt['archiveSha256'], 'snapshot': str(snapshot), 'weaponPaused': True}), flush=True)
