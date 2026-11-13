import argparse
import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'candidates/boss-core-stage13-client-native-fresh-world-20261113'
REPORTS = WORK / 'reports/stage13-client-freshworld-20261113'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

parser = argparse.ArgumentParser()
parser.add_argument('report', type=Path)
arguments = parser.parse_args()
report_path = arguments.report.resolve()
result = load(report_path)
assert 'client-summon-fx-v14-' in report_path.name and report_path.is_relative_to(WORK / 'reports')
assert not OUT.exists()
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == load(ROOT / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease|run_boss_client_summon_fx')} } | ConvertTo-Json -Compress"
raw = subprocess.check_output(['powershell', '-NoProfile', '-Command', query], encoding='utf-8-sig').strip()
processes = json.loads(raw) if raw else []
if isinstance(processes, dict):
    processes = [processes]
assert not any(row['owned'] and row['pid'] != os.getpid() for row in processes)
prior_receipt = WORK / 'candidates/boss-core-stage13-client-fx-20261113/archive-receipt.json'
prior = load(prior_receipt)
assert sha(prior['archive']) == prior['archiveSha256'] == 'ab877ac0ea4cd894d1b5b704a90b62753da570e46699cac9a101756b63190ea5'
formal = prior['formalInstancesUnchanged']
for row in formal.values():
    assert sha(row['path']) == row['sha256']
    assert not any(path.is_file() and ('qys-bosses' in path.name or 'ftboss' in path.name) and path.suffix in ['.tmp', '.part', '.pending', '.new'] for path in Path(row['path']).parent.iterdir())
terminal = result['terminal']
probe = result['probe']
assert terminal['exitCode'] == 0 and terminal['integratedWorldCompleteSave'] and terminal['inputHashesUnchanged'] and terminal['clientProcessReaped']
assert probe['failures'] == 0 and probe['exactOriginalParticlePacketDeliveryAccepted'] and probe['actualFrontRenderAccepted']
assert probe['expectedOriginalDustPackets'] == probe['actualDustPacketsReceived'] == 396
assert probe['crimsonPackets'] == 288 and probe['bonePackets'] == 108
assert probe['renderMetricExcludesBossHud'] and probe['bothOriginalDustColorsMustAddWorldPixels'] and probe['renderedActiveFrames'] > 0
assert probe['nativePublicFreshWorldCreationRequested'] and probe['noPreviousOrFailedWorldSeed'] and probe['actualLocalPlayerJoinedThroughVanillaIntegratedLogin']
assert not probe['authenticatedEntryAccepted'] and not probe['authenticatedMultiplayerAccepted'] and not probe['fullBossMigrationComplete']
assert not result['installable'] and not result['deployed']
visual_path = REPORTS / 'front-visual-review-v14.json'
visual = load(visual_path)
assert visual['frontVisualReviewAccepted'] and visual['actualReportSha256'] == sha(report_path)
for capture in probe['captures']:
    assert sha(capture['image']) == visual['imageHashes'][capture['image']]
    assert capture['bottomBossHudExcluded']
assert probe['captures'][0]['brightWorldPixels'] > 5000
invalidated = load(REPORTS / 'v13-visual-review-invalidates-front-render.json')
assert invalidated['frontRenderAcceptedAfterVisualReview'] is False and invalidated['packetDeliveryRemainsAccepted']
OUT.mkdir(parents=True)
summary = {'createdAt': datetime.datetime.now().astimezone().isoformat(), 'objective': 'Continue complete Boss migration and deployment; this is one verified client segment, not goal completion', 'actualReport': str(report_path), 'actualReportSha256': sha(report_path), 'strictClientChecks': len(probe['checks']), 'exactOriginalPackets': 396, 'crimsonPackets': 288, 'bonePackets': 108, 'actualFrontRenderAcceptedAfterVisualReview': True, 'worldOnlyRoiExcludesBossHud': True, 'freshNativeFlatWorld': True, 'noOldOrFailedWorldSeed': True, 'terminal': terminal, 'original315ChecksAnd66TicksReusedWithoutRerun': True, 'priorClientPreparationAndInvalidVisualEvidencePreserved': True, 'authenticatedEntryAccepted': False, 'authenticatedMultiplayerAccepted': False, 'fourDirectionGlAccepted': False, 'fullBossMigrationComplete': False, 'goalStatus': 'active', 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'weaponRecoveryJarInstallable': False, 'installable': False, 'deployed': False, 'formalInstancesUnchanged': formal, 'priorArchive': {'path': prior['archive'], 'sha256': prior['archiveSha256']}, 'ownedRuntimeOrSupervisorRemaining': 0}
save(OUT / 'acceptance.json', summary)
save(OUT / 'process-audit-after.json', processes)
selected = {Path(__file__), report_path, prior_receipt, ROOT / 'WRITE_OWNER.claim'}
external = {}
mod_copies = {}
for prefix in ['client-summon-fx-v13-', 'client-summon-fx-v14-']:
    attempts = list((WORK / 'build').glob(prefix + '*'))
    assert len(attempts) == 1
    attempt = attempts[0]
    assert load(attempt / 'terminal.json')['clientProcessReaped']
    for name, expected in load(attempt / 'inputs-before.json').items():
        assert sha(name) == expected, name
        path = Path(name)
        if path.resolve().is_relative_to(ROOT):
            selected.add(path)
        else:
            external[name] = expected
    for path in attempt.rglob('*'):
        if not path.is_file() or path.name == 'session.lock':
            continue
        relative = path.relative_to(attempt)
        if relative.parts[:2] == ('game', 'mods') and 'ftboss-boss-core-' not in path.name:
            mod_copies[str(path.relative_to(ROOT))] = {'sha256': sha(path), 'bytes': path.stat().st_size, 'restoreFrom': 'Exact matching server/mods JAR from frozen 315-check archive'}
            continue
        selected.add(path)
    selected.add(WORK / 'reports' / (attempt.name + '.json'))
reused = load(WORK / 'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json')
with zipfile.ZipFile(reused['archive']) as bundle:
    manifest = json.loads(bundle.read('ARCHIVE_MANIFEST.json'))
    by_name = {Path(name).name: row['sha256'] for name, row in manifest.items() if '/server/mods/' in name}
    for name, row in mod_copies.items():
        assert by_name.get(Path(name).name) == row['sha256'], name
save(OUT / 'copied-mods-restore-manifest.json', mod_copies)
save(OUT / 'external-read-only-inputs.json', external)
save(OUT / 'prior-frozen-evidence.json', reused)
helper = ROOT / 'work/session-lease/tools/reactivate_goal_after_boss_stage12.py'
namespace = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('assert not OUT.exists()')], str(helper), 'exec'), namespace)
patch_file = namespace['replace_file']
creator_path = WORK / 'tools/prepare_boss_blood_tide_stage10.py'
creation = {'__file__': str(creator_path)}
text = creator_path.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')], str(creator_path), 'exec'), creation)
creation['create'](OUT / 'RESTORE.md', '# Native fresh-world Boss client FX checkpoint\n\nThe accepted scope is 396 exact original packets and visible original crimson/bone dust in one actual fixed front GL view, with a visible sky baseline and the Boss HUD excluded from the measured world ROI. The native integrated world saved completely and the client exited normally. V13 packet delivery passed but its red-HUD visual metric was invalidated by actual framebuffer review; preserve that report and the invalidation record. Do not reuse either attempt as an authentication claim.\n\nVerify this ZIP SHA256 and every ARCHIVE_MANIFEST member before restoring into an isolated C workspace. All direct inputs, compiled observers, screenshots, native world files, exact commands and logs are retained. Deduplicated dependency mod copies must be restored by filename and exact SHA256 from the frozen 315-check archive using copied-mods-restore-manifest.json and prior-frozen-evidence.json. External formal assets/libraries and historical D inputs are read-only and listed by hash. The older 10091-file checkpoint contains all previous launch failures and original asset restoration provenance; require its recorded SHA256.\n\nThe candidate remains NOT-INSTALLABLE and is not deployed. Genuine authenticated Boss entry/rewards, dedicated multiplayer, remaining GL directions, remaining Boss resources/behavior parity and full battles are still required. Keep the complete Boss goal active, same sole writer, paused weapon work and noninstallable recovery JAR. Never restart weapon lease/payment/Session from old metadata. Both formal instances remain unchanged at this checkpoint.\n')
for name in ['WRITE_OWNER.json', 'work/session-boss/CURRENT_BOSS_BATCH.json']:
    path = ROOT / name
    before = path.read_text(encoding='utf-8')
    save(OUT / (name.replace('/', '_') + '.before.json'), {'text': before, 'sha256': sha(path)})
    value = json.loads(before)
    value.update({'activeRuntimePid': None, 'activeRuntimePids': [], 'activeSupervisor': None, 'bossActiveRuntimePid': None, 'bossActiveSupervisor': None, 'activeDebuggerPid': None, 'currentGoalStatus': 'active', 'activeGoalObjective': '继续完成boss部分部署移植', 'activeWorkstream': 'boss-migration-and-deployment-weapons-paused', 'currentSignedSessionGoalStatus': 'paused_by_current_direct_user', 'latestBossClientFreshWorldAcceptance': str(OUT / 'acceptance.json'), 'latestBossClientFreshWorldArchiveReceipt': str(OUT / 'archive-receipt.json'), 'runtimeStatusAuthority': str(OUT / 'acceptance.json'), 'lastBossRuntimeTerminal': True, 'ownedBossRuntimeStillRunning': False, 'weaponValidationPaused': True, 'noNewWeaponRuns': True, 'weaponSnapshotInstallable': False, 'stage13DeploymentPerformed': False, 'installable': False, 'nextBossScope': 'Continue remaining original Boss resources and directed behavior parity, remaining client GL views, authentic Boss entry/rewards/multiplayer and full battles before deployment. Full goal remains active.'})
    patch_file(path, before, json.dumps(value, ensure_ascii=False, indent=2) + '\n')
for name, heading, addition in [
    ('AGENTS.md', '# NeoForge continuation on C\n\n', '## Latest verified native client FX checkpoint, 2026-11-13\n\n- V14 uses the stock public createFreshLevel flow with a new flat world. It received all 396 original dust packets and passed a real front framebuffer review with both dust colors in a world-only ROI; the baseline must show visible sky and exclude the Boss HUD. V13 had a misleading green broad-red-pixel flag and was explicitly invalidated after visual review; preserve it, never cite it as visible particle acceptance.\n- Native complete save/exit0 and all frozen input hashes pass. Reuse the earlier 315 checks and 66 natural ticks. New authority: work/session-boss/candidates/boss-core-stage13-client-native-fresh-world-20261113/acceptance.json. All owned child processes are terminal.\n- Full Boss migration/deployment goal stays active. Authenticated entry/rewards, dedicated multiplayer, remaining GL views/resources/behavior parity and full battles remain pending. Weapon work stays paused. Stage13 remains NOT-INSTALLABLE and undeployed. Same sole C writer.\n\n'),
    ('work/session-boss/REMAINING_BOSS_CHECKPOINT_20261113.md', '# Boss priority checkpoint, 2026-11-13\n\n', '## Latest actual client result: native fresh flat world\n\n396 original dust packets arrived through the real client handler; both original colors were visibly rendered in the actual front framebuffer with a bright world baseline and the bottom Boss HUD excluded from the measured region. The world saved completely and client exited zero. V13 broad-red visual acceptance was invalidated after screenshot review; its actual packet evidence remains distinct. The prior BackupConfirmScreen preparation blocker is resolved for this new native world flow, with no old/failed world seed and no private UI callback. See candidates/boss-core-stage13-client-native-fresh-world-20261113/acceptance.json.\n\nFull Boss migration/deployment remains the active goal. Genuine authenticated entry/rewards, actual dedicated multiplayer, remaining GL views, original Boss resources/behavior parity and complete battles remain pending. Stage13 is not installable or deployed. Weapon work remains paused. Historical checkpoint details below are preserved as history, not current client status.\n\n')]:
    path = ROOT / name
    before = path.read_text(encoding='utf-8')
    assert before.count(heading) == 1
    patch_file(path, before, before.replace(heading, heading + addition, 1))
for folder in [OUT, REPORTS, WORK / 'tests/client-summon-fx-v4', WORK / 'tests/client-summon-fx-v5', WORK / 'build/boss-summon-fx-client-assets-v2']:
    selected.update(path for path in folder.rglob('*') if path.is_file())
selected.update([ROOT / 'AGENTS.md', ROOT / 'WRITE_OWNER.json', WORK / 'CURRENT_BOSS_BATCH.json', WORK / 'REMAINING_BOSS_CHECKPOINT_20261113.md', WORK / 'tools/run_boss_client_summon_fx_v13.py', WORK / 'tools/run_boss_client_summon_fx_v14.py', WORK / 'tools/build_boss_client_assets_stage13_v2.py'])
archive = WORK / 'archives/boss-core-stage13-client-native-fresh-world-NOT-INSTALLABLE-20261113.zip'
manifest = {}
with zipfile.ZipFile(archive, 'x', compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(), path
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {'sha256': sha(path), 'bytes': path.stat().st_size}
        bundle.write(path, name, compress_type=zipfile.ZIP_STORED if path.suffix in ['.jar', '.zip', '.png'] else zipfile.ZIP_DEFLATED)
    encoded = json.dumps(manifest, ensure_ascii=False, indent=2).encode('utf-8')
    bundle.writestr('ARCHIVE_MANIFEST.json', encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json') == encoded
    for name, row in manifest.items():
        payload = bundle.read(name)
        assert len(payload) == row['bytes'] and hashlib.sha256(payload).hexdigest() == row['sha256']
for row in formal.values():
    assert sha(row['path']) == row['sha256']
save(OUT / 'archive-receipt.json', {'failures': 0, 'archive': str(archive), 'archiveSha256': sha(archive), 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'actualFrontVisualReviewAccepted': True, 'exactOriginalPackets': 396, 'goalStatus': 'active', 'fullBossMigrationComplete': False, 'installable': False, 'deployed': False, 'ownedRuntimeOrSupervisorRemaining': 0, 'formalInstancesUnchanged': formal})
print(json.dumps({'archive': str(archive), 'sha256': sha(archive), 'files': len(manifest), 'exactPackets': 396, 'frontVisualAccepted': True, 'fullGoalComplete': False, 'installable': False, 'deployed': False}), flush=True)
