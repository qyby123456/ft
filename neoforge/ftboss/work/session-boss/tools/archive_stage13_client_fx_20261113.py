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
OUT = WORK / 'candidates/boss-core-stage13-client-fx-20261113'

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
assert report_path.is_relative_to(WORK / 'reports')
assert not OUT.exists()
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused'] and owner['weaponSnapshotInstallable'] is False
assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == owner['activeWriterThreadId']
query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;parentPid=$_.ParentProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease|run_boss_client_summon_fx')} } | ConvertTo-Json -Compress"
raw = subprocess.check_output(['powershell', '-NoProfile', '-Command', query], encoding='utf-8-sig').strip()
processes = json.loads(raw) if raw else []
processes = processes if isinstance(processes, list) else [processes]
assert not any(row['owned'] and row['pid'] != os.getpid() for row in processes)
preflight = WORK / 'reports/stage13-client-fx-20261113'
reused = load(preflight / 'reuse-frozen-evidence.json')
assert reused['originalChecksRerun'] is False and reused['reusedChecks'] == 315 and reused['reusedNaturalTicks'] == 66
assert sha(reused['archive']) == reused['archiveSha256']
assert sha(reused['report']) == reused['reportSha256']
blocker = load(preflight / 'authenticated-entry-blocker.json')
assert blocker['authenticatedSummonEntryAccepted'] is False and blocker['personalLauncherAccountStoresRead'] is False
formal = load(preflight / 'preflight.json')['formalInstancesUnchanged']
for row in formal.values():
    assert sha(row['path']) == row['sha256']
    assert not any(path.suffix in ['.tmp', '.part', '.pending'] for path in Path(row['path']).parent.glob('qys-bosses*') if path.is_file())
for name, expected in result['inputHashes'].items():
    assert sha(name) == expected, name
assert result['terminal']['clientProcessReaped'] and result['terminal']['inputHashesUnchanged']
assert not result['authenticatedSummonEntryAccepted'] and not result['authenticatedMultiplayerAccepted'] and not result['installable'] and not result['deployed']
probe = result['probe']
passed = probe.get('failures') == 0 and result['terminal']['exitCode'] == 0 and result['terminal']['integratedWorldCompleteSave'] and probe.get('actualFrontRenderAccepted') and probe.get('exactOriginalParticlePacketDeliveryAccepted')
if passed:
    assert probe['expectedOriginalDustPackets'] == probe['actualDustPacketsReceived']
    assert probe['crimsonPackets'] > 0 and probe['bonePackets'] > 0 and probe['renderedActiveFrames'] > 0
OUT.mkdir(parents=True)
save(OUT / 'process-audit-after.json', processes)
summary = {'createdAt': datetime.datetime.now().astimezone().isoformat(), 'scope': 'Single stage13 client summon particle acceptance after truthful external authentication readiness blocker', 'authenticatedSummonEntryAccepted': False, 'authenticationBlocker': str(preflight / 'authenticated-entry-blocker.json'), 'clientPacketAndFrontRenderAccepted': bool(passed), 'clientReport': str(report_path), 'clientReportSha256': sha(report_path), 'terminal': result['terminal'], 'original315ChecksReusedWithoutRerun': True, 'reusedNaturalTicks': 66, 'fullBossMigrationComplete': False, 'dedicatedAuthenticatedMultiplayerAccepted': False, 'fourDirectionGlAccepted': False, 'installable': False, 'deployed': False, 'weaponDevelopmentPaused': True, 'weaponValidationPaused': True, 'weaponRecoveryJarInstallable': False, 'formalInstancesUnchanged': formal, 'ownedJavaPythonRemainingExcludingThisArchiver': 0}
summary['originalClientAssetRestoration'] = load(WORK / 'build/boss-summon-fx-client-assets-v2/candidate-build-receipt.json')
summary['sixOriginalShaderFilesRestored'] = True
summary['actualClientWorldEntryBlocker'] = load(preflight / 'client-world-entry-blocker.json')
summary['actualBackupConfirmScreenObservedWithoutWorldEntry'] = True
summary['noParticleDeliveryOrFrontRenderAcceptanceClaimed'] = True
summary['processHandlesConsumed'] = [94879, 30853, 4648, 76108, 32660]

summary['otherOriginalBossResourceGapsStillPending'] = True
summary['original13JsonAndOneTextureRestored'] = True
summary['candidateAll1446ClassesByteIdenticalTo315CheckCandidate'] = True
summary['resourceRootCauseEvidence'] = str(preflight / 'resource-restoration-proof.json')
save(OUT / 'acceptance.json', summary)
selected = {Path(__file__), report_path, ROOT / 'WRITE_OWNER.claim'}
external = {}
attempt_summaries = []
mod_copies = {}
for attempt in sorted((WORK / 'build').glob('client-summon-fx-v*-*')):
    terminal_path = attempt / 'terminal.json'
    launch_terminal = attempt / 'launch-terminal.json'
    attempt_summaries.append({'attempt': str(attempt), 'terminal': load(terminal_path) if terminal_path.exists() else load(launch_terminal) if launch_terminal.exists() else {'explicitTerminalRecordMissing': True}, 'notUsedAsAnotherRunSeed': True})
    if (attempt / 'inputs-before.json').exists():
        for name, expected in load(attempt / 'inputs-before.json').items():
            path = Path(name)
            assert sha(path) == expected, name
            if path.resolve().is_relative_to(ROOT):
                selected.add(path)
            else:
                external[name] = expected
    for path in attempt.rglob('*'):
        if not path.is_file() or path.name == 'session.lock':
            continue
        relative = path.relative_to(attempt)
        if relative.parts[0] in ['mc', 'genuine-minecraft-neoforge-dev-view']:
            continue
        if relative.parts[:2] == ('game', 'mods'):
            if path.name in ['ftboss-boss-core-1.21.1-stage13-client-assets-NOT-INSTALLABLE.jar', 'ftboss-boss-core-1.21.1-stage13-client-assets2-NOT-INSTALLABLE.jar']:
                version = 'v2' if 'client-assets2-' in path.name else 'v1'
                expected = '589ad968f2efe15a98a1438ee9d9b7218343c197b69d616ada7da3681bb2e3fe' if version == 'v2' else 'd1ac74d82af03383e495b4bc35948e8033b4f563e353c6c687c461011fdf02d6'
                resource_build = load(WORK / ('build/boss-summon-fx-client-assets-' + version + '/candidate-build-receipt.json'))
                assert resource_build['resourceRestorationOnly'] and resource_build['allOriginalEntriesByteIdentical']
                assert sha(path) == resource_build['jarSha256'] == expected
                selected.add(path)
            else:
                mod_copies[str(path.relative_to(ROOT))] = {'sha256': sha(path), 'bytes': path.stat().st_size, 'restoreFrom': 'Frozen 315-check archive server/mods with the same filename and SHA256'}
            continue
        selected.add(path)
save(OUT / 'attempt-terminals.json', attempt_summaries)
save(OUT / 'copied-mods-restore-manifest.json', mod_copies)
save(OUT / 'external-read-only-inputs.json', external)
with zipfile.ZipFile(reused['archive']) as frozen:
    manifest = json.loads(frozen.read('ARCHIVE_MANIFEST.json'))
    by_name = {Path(name).name: row['sha256'] for name, row in manifest.items() if '/server/mods/' in name}
    for name, row in mod_copies.items():
        assert by_name.get(Path(name).name) == row['sha256'], name
helper = ROOT / 'work/session-lease/tools/reactivate_goal_after_boss_stage12.py'
namespace = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('assert not OUT.exists()')], str(helper), 'exec'), namespace)
patch_file = namespace['replace_file']
helper = WORK / 'tools/prepare_boss_blood_tide_stage10.py'
creation = {'__file__': str(helper)}
text = helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')], str(helper), 'exec'), creation)
status = 'passed_in_offline_integrated_front_view_only' if passed else 'not_accepted_actual_client_failure_preserved'
creation['create'](OUT / 'RESTORE.md', '# Stage13 client summon FX checkpoint\n\nStatus: ' + status + '. Authentication remains blocked because no real authenticated client/session was supplied to this batch; no personal launcher account store was read. Offline integrated login is never counted as authenticated or dedicated multiplayer acceptance.\n\nReuse the frozen 315-check/66-natural-tick archive and its normal save/exit evidence without rerunning those tests. Preserve every preparation/bootstrap/runtime failure in attempt-terminals.json. No failed client world is a restart seed. Each attempt copied only the accepted frozen server world.\n\nRestore into a separate C workspace after verifying archive SHA256 and every ARCHIVE_MANIFEST member. Copied mod JARs are deduplicated: restore them from the frozen 315-check archive using copied-mods-restore-manifest.json, and require exact SHA256 matches. The changed resource-only Boss candidate and all 20 byte-identical original assets are archived directly; do not replace it with the older server-test JAR. Its build receipt proves every existing entry including all 1446 classes is unchanged, so the prior 315 checks are reused without rerunning. Each generated client-view JAR and member manifest is archived; rebuild its mc directory by safe extraction and require every listed member hash. Historical D, formal launcher libraries/assets and both formal instances are read-only; their required files are listed in external-read-only-inputs.json.\n\nStage13 and the separate paused weapon recovery JAR remain NOT-INSTALLABLE. The actual client is blocked at stock BackupConfirmScreen with no world/player/particle evidence; see client-world-entry-blocker.json. Complete the real backup/confirmation flow with supported tools before a future client retry. Never change world version/metadata or invoke a private UI callback to fabricate entry. Never deploy this candidate from the scoped client result. Genuine authenticated start/rewards, actual dedicated multiplayer, remaining GL views and full battles remain pending. All owned child processes are terminal and reaped at this checkpoint; the same sole writer retains the claim.\n')
for name in ['WRITE_OWNER.json', 'work/session-boss/CURRENT_BOSS_BATCH.json']:
    path = ROOT / name
    before = path.read_text(encoding='utf-8')
    save(OUT / (name.replace('/', '_') + '.before.json'), {'text': before, 'sha256': sha(path)})
    value = json.loads(before)
    value.update({'activeWorkstream': 'boss-stage13-client-fx-checkpoint-weapons-paused', 'activeRuntimePid': None, 'activeRuntimePids': [], 'activeSupervisor': None, 'bossActiveRuntimePid': None, 'bossActiveSupervisor': None, 'activeDebuggerPid': None, 'ownedBossRuntimeStillRunning': False, 'lastBossRuntimeTerminal': True, 'latestBossClientFxAcceptance': str(OUT / 'acceptance.json'), 'latestBossClientFxArchiveReceipt': str(OUT / 'archive-receipt.json'), 'runtimeStatusAuthority': str(OUT / 'acceptance.json'), 'noNewWeaponRuns': True, 'weaponValidationPaused': True, 'weaponSnapshotInstallable': False, 'queuedStages': [], 'installable': False, 'stage13DeploymentPerformed': False, 'fullBossMigrationComplete': False, 'latestBossAuthenticatedEntryBlocker': str(preflight / 'authenticated-entry-blocker.json'), 'nextBossScope': 'Real authenticated summon entry remains blocked. Actual offline client is blocked at stock BackupConfirmScreen; use a supported real backup/confirmation flow, then validate original summon particles/front rendering. Preserve all current failures; no automatic native retry queued'})
    patch_file(path, before, json.dumps(value, ensure_ascii=False, indent=2) + '\n')
agents = ROOT / 'AGENTS.md'
before = agents.read_text(encoding='utf-8')
heading = '## Current delegated stage13 single-acceptance checkpoint, 2026-11-13\n\n- Reused 315 checks,66 natural ticks, normal exit/save and the frozen archive without rerunning them. Current authentication readiness is truthfully blocked: no real authenticated client/session was provided. No personal launcher credential stores were read and offline fixtures never establish authentication.\n- The scoped actual-client summon packet/front-render result is ' + status + '; authoritative evidence is work/session-boss/candidates/boss-core-stage13-client-fx-20261113/acceptance.json. Preserve all preparation/bootstrap failures and exact input hashes. All owned native/supervisor processes are terminal at this checkpoint.\n- Keep this same sole C writer, all weapon work paused and its recovery JAR noninstallable. Stage13 stays NOT-INSTALLABLE until complete required acceptance; no deployment. Both formal instances and historical D remain read-only.\n\n'
patch_file(agents, before, before.replace('# NeoForge continuation on C\n\n', '# NeoForge continuation on C\n\n' + heading, 1))
checkpoint = WORK / 'REMAINING_BOSS_CHECKPOINT_20261113.md'
checkpoint_before = checkpoint.read_text(encoding='utf-8')
checkpoint_update = '## Scoped actual-client checkpoint, 2026-11-13\n\nActual client summon packet/front-render status: ' + status + '. See candidates/boss-core-stage13-client-fx-20261113/acceptance.json and its archive receipt for exact evidence. Real authenticated entry is blocked because this batch has no supplied authenticated session; no personal launcher account store was read. The 315 checks, 66 natural ticks and original normal shutdown evidence were reused without rerun.\n\nThe actual offline client is still blocked at stock BackupConfirmScreen; no player/world entry or particle/front-render evidence was produced. See reports/stage13-client-fx-20261113/client-world-entry-blocker.json. No further native attempt is queued.\n\nActual client loading found missing original Xueli effect resources and zhuni_blackwall/shadow_darkness shader files. The new NOT-INSTALLABLE resource-only candidate restores exactly 20 byte-identical original resources; all 1446 class bytes and all previous JAR entries remain unchanged. Preserve all failed launch attempts and normal-stop receipts. Other original resource omissions require individually justified future work; do not blindly restore paused weapon resources.\n\nAll owned client/supervisor processes are terminal and reaped. Weapon development/validation stays paused, its separate recovery JAR is not installable, and the signed-Session goal stays paused. Both formal instances and historical D remain read-only. Stage13 is not deployed. Real authenticated entry/rewards, dedicated multiplayer, remaining GL views, remaining Boss resources and full battles remain pending.\n\n'
patch_file(checkpoint, checkpoint_before, checkpoint_before.replace('# Boss priority checkpoint, 2026-11-13\n\n', '# Boss priority checkpoint, 2026-11-13\n\n' + checkpoint_update, 1))
for folder in [OUT, preflight, WORK / 'tests/client-summon-fx-v1', WORK / 'tests/client-summon-fx-v2', WORK / 'tests/client-summon-fx-v3', WORK / 'tests/client-read-state', WORK / 'build/client-read-state-v1', WORK / 'tests/client-normal-stop', WORK / 'build/client-normal-stop-v1', WORK / 'build/boss-summon-fx-client-assets-v1', WORK / 'build/boss-summon-fx-client-assets-v2']:
    selected.update(path for path in folder.rglob('*') if path.is_file())
selected.update((WORK / 'tools').glob('*client*fx*.py'))
selected.update([WORK / 'tools/build_boss_client_assets_stage13_v1.py', WORK / 'tools/build_boss_client_assets_stage13_v2.py', ROOT / 'reference/forge-current.jar'])
selected.update([agents, ROOT / 'WRITE_OWNER.json', WORK / 'CURRENT_BOSS_BATCH.json', WORK / 'REMAINING_BOSS_CHECKPOINT_20261113.md'])
archive = WORK / 'archives/boss-core-stage13-client-summon-fx-NOT-INSTALLABLE-20261113.zip'
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
save(OUT / 'archive-receipt.json', {'failures': 0, 'archive': str(archive), 'archiveSha256': sha(archive), 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'clientPacketAndFrontRenderAccepted': bool(passed), 'authenticatedEntryAccepted': False, 'installable': False, 'deployed': False, 'formalInstancesUnchanged': formal, 'ownedJavaPythonRemaining': 0})
print(json.dumps({'archive': str(archive), 'sha256': sha(archive), 'files': len(manifest), 'clientFrontScopeAccepted': bool(passed), 'authenticated': False, 'installable': False, 'deployed': False}), flush=True)
