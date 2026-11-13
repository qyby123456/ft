import datetime
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'candidates/boss-core-stage13-natural-summon-20261113'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

helper = ROOT / 'work/session-lease/tools/reactivate_goal_after_boss_stage12.py'
namespace = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('assert not OUT.exists()')], str(helper), 'exec'), namespace)
patch_file = namespace['replace_file']
helper = WORK / 'tools/prepare_boss_blood_tide_stage10.py'
namespace_create = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')], str(helper), 'exec'), namespace_create)
create = namespace_create['create']
assert not OUT.exists()
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused']
assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == owner['activeWriterThreadId']
report_path = WORK / 'reports/summon-fx-v8-candidate-b74fa0f4.json'
result = load(report_path)
probe = result['probe']
assert result['terminal'] == {'exitCode': 0, 'stopSent': True, 'completeSave': True, 'inputHashesUnchanged': True, 'supervisionFailure': None}
assert probe['failures'] == 0 and probe['naturalEntitySchedulingAccepted'] and probe['naturalOwnerTicks'] == 66
assert len(probe['cases']) == 315 and probe['completedStages'] == 2
assert not probe['directedRegisteredEntityTicksPerNativeCallback']
assert not result['authenticatedMultiplayerAccepted'] and not result['fullBossAcceptance']
for name, expected in result['inputHashes'].items():
    assert sha(name) == expected, name
build_path = WORK / 'build/boss-summon-fx-v2/candidate-build-receipt.json'
built = load(build_path)
assert sha(built['jar']) == built['jarSha256'] == '60cfcc8fd1ae10e4249b807a8f68c23074d251d091c4e3a804b2be20903d3b6a'
for name, expected in built['inputHashes'].items():
    assert sha(name) == expected, name
pause_path = WORK / 'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json'
pause = load(pause_path)
assert sha(pause['archive']) == pause['archiveSha256']
assert sha(pause['snapshotJar']) == pause['snapshotJarSha256']
formal = pause['formalInstancesUnchanged']
for row in formal.values():
    assert sha(row['path']) == row['sha256']
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell', '-NoProfile', '-Command', query], encoding='utf-8-sig').strip()
OUT.mkdir(parents=True)
summary = {'createdAt': datetime.datetime.now().astimezone().isoformat(), 'failures': 0, 'candidateJar': built['jar'], 'candidateJarSha256': built['jarSha256'], 'report': str(report_path), 'reportSha256': sha(report_path), 'checks': 315, 'naturalOwnerTicks': 66, 'warmupTicks': 60, 'naturalEntitySchedulingAccepted': True, 'observerCallsEntityTick': False, 'actualServerSummonFxAccepted': True, 'fixtureUsesPublicCloneAndStaticBossSetup': True, 'authenticatedStartOrRewardsAccepted': False, 'activeFullBattleAiAccepted': False, 'clientParticlesOrGlAccepted': False, 'fullBossMigrationComplete': False, 'installable': False, 'deployed': False, 'deploymentBlockedByRemainingFeatureAcceptance': ['authenticated original start/clone entry', 'actual client particle delivery/rendering'], 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'formalInstancesUnchanged': formal, 'ownedHeavyJvmsRemaining': 0}
save(OUT / 'acceptance.json', summary)
create(OUT / 'RESTORE.md', '# Boss stage13 natural summon scheduling checkpoint\n\nThe unchanged candidate JAR 60cfcc8f passes 315 strict checks with 66 actual natural owner ticks after 60 forced-chunk warmup ticks. The observer never calls Entity.tick. Original summonWave, four UUID phases, boss/minion points, negative admissions, cap24/eviction, same-tick deduplication and expiry pass. Server exit0, normal stop, complete save and all frozen input hashes are verified.\n\nThis is server scheduling evidence only. The fixture sets public clone status and static Boss level/UUID; it does not prove authenticated start/rewards, unconstrained full combat, client particle delivery/rendering or GL. Candidate remains NOT-INSTALLABLE and is not deployed. Formal NeoForge stays stage12 and Forge is untouched.\n\nVerify archive SHA256 and all ARCHIVE_MANIFEST.json members before restoring into a separate C workspace. Reuse the frozen stage13 implementation archive; do not mutate historical archives or install this unaccepted candidate. Weapon work and the persisted Session goal remain paused. The separate cf217105 weapon JAR is a recovery container outside mods.\n')
checkpoint = WORK / 'REMAINING_BOSS_CHECKPOINT_20261113.md'
create(checkpoint, '# Boss priority checkpoint, 2026-11-13\n\nWeapon development and validation are paused by the direct user instruction. The previous signed-Session goal is paused, not completed. Candidate cf217105 whole19 saved and exited normally but failed an unchanged actual-hurt authority assertion; no dependent JVMs started. Its failure world is not a seed. A new 5807-file verified archive and separate recovery JAR preserve the complete current implementation/evidence. Runnable weapon addon packaging remains deferred.\n\nBoss stage12 remains formally installed. Stage13 now also passes natural native scheduling: 315 checks, 66 actual owner ticks, two pulse cycles, original cap/budget/phases/expiry and negative controls. No observer Entity.tick invocation and no paused weapon implementation loaded. Client FX/authenticated start remain required; stage13 is not installable/deployed. Boss rewards/base material independence remains a separate stage03 candidate awaiting authenticated kill acceptance.\n\nNext continue genuine Boss entry/client/full battle acceptance in the same sole C workspace. Do not reactivate weapon lease/payment/Session work. Deploy only fully verified Boss increments after backup/rollback verification, then archive each segment. Keep Forge and historical D read-only.\n')
for name in ['WRITE_OWNER.json', 'work/session-boss/CURRENT_BOSS_BATCH.json']:
    path = ROOT / name
    before = path.read_text(encoding='utf-8')
    save(OUT / (name.replace('/', '_') + '.before.json'), {'text': before, 'sha256': sha(path)})
    value = json.loads(before)
    value.update({'latestBossNaturalSummonAcceptance': str(OUT / 'acceptance.json'), 'latestBossRuntimeReceipt': str(report_path), 'lastBossRuntimePid': 24188, 'lastBossRuntimeTerminal': True, 'ownedBossRuntimeStillRunning': False, 'bossActiveRuntimePid': None, 'bossActiveSupervisor': None, 'activeRuntimePid': None, 'activeSupervisor': None, 'activeDebuggerPid': None, 'activeAcceptancePipeline': None, 'queuedStages': [], 'noNewWeaponRuns': True, 'weaponValidationPaused': True, 'currentSignedSessionGoalStatus': 'paused_by_current_direct_user', 'latestBossCheckpoint': str(checkpoint), 'latestBossNaturalSummonArchiveReceipt': str(OUT / 'archive-receipt.json'), 'runtimeStatusAuthority': str(OUT / 'acceptance.json'), 'fullBossMigrationComplete': False, 'installable': False, 'stage13DeploymentPerformed': False, 'nextBossScope': 'Genuine Boss start/client/full battle acceptance; weapons paused'})
    patch_file(path, before, json.dumps(value, ensure_ascii=False, indent=2) + '\n')
create(OUT / 'pause-preflight-failure-preserved.md', '# Earlier archive preparation failure\n\nThe first pause packaging attempt stopped before metadata updates or archive creation with KeyError archiveSha256 because older receipts use sha256. Its already written process audit, pause request and recovery JAR remain in archives/weapons-paused-cf217105-boss-priority-20261113. The v2 attempt accepts the explicitly observed receipt schema, verifies each archive hash, and completed 5807 member checks. This was a packaging preflight failure, not a native acceptance pass. No weapon JVM restarted.\n')
selected = {Path(__file__), report_path, build_path, Path(built['jar']), pause_path, checkpoint, ROOT / 'AGENTS.md', ROOT / 'WRITE_OWNER.json', ROOT / 'WRITE_OWNER.claim', WORK / 'CURRENT_BOSS_BATCH.json', WORK / 'tools/prepare_boss_summon_fx_natural_v8.py', WORK / 'tools/run_boss_summon_fx_probe_v8.py'}
for folder in [OUT, Path(result['attempt']), WORK / 'tests/summon-fx-v8', WORK / 'archives/weapons-paused-cf217105-boss-priority-20261113']:
    selected.update(path for path in folder.rglob('*') if path.is_file() and path.name != 'session.lock')
archive = WORK / 'archives/boss-core-stage13-natural-summon-NOT-INSTALLABLE-20261113.zip'
manifest = {}
with zipfile.ZipFile(archive, 'x', compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.resolve().is_relative_to(ROOT) and not path.is_symlink()
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {'sha256': sha(path), 'bytes': path.stat().st_size}
        bundle.write(path, name)
    encoded = json.dumps(manifest, ensure_ascii=False, indent=2).encode('utf-8')
    bundle.writestr('ARCHIVE_MANIFEST.json', encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json') == encoded
    for name, row in manifest.items():
        payload = bundle.read(name)
        assert len(payload) == row['bytes'] and hashlib.sha256(payload).hexdigest() == row['sha256']
for row in formal.values():
    assert sha(row['path']) == row['sha256']
save(OUT / 'archive-receipt.json', {'failures': 0, 'archive': str(archive), 'archiveSha256': sha(archive), 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'acceptance': str(OUT / 'acceptance.json'), 'formalInstancesUnchanged': formal, 'installable': False, 'deployed': False})
print(json.dumps({'archive': str(archive), 'sha256': sha(archive), 'files': len(manifest), 'checks': 315, 'naturalOwnerTicks': 66, 'deployed': False}), flush=True)
