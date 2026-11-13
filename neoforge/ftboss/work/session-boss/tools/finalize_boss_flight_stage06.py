import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
BUILD = WORK / 'build/boss-flight-ban-original-v1'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

def digest(path):
    checksum = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            checksum.update(block)
    return checksum.hexdigest()

parser = argparse.ArgumentParser()
parser.add_argument('baseline_runtime', type=Path)
parser.add_argument('candidate_runtime', type=Path)
arguments = parser.parse_args()
owner = '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == owner
assert load(ROOT / 'WRITE_OWNER.json')['activeWriterThreadId'] == owner
baseline_path = arguments.baseline_runtime.resolve()
candidate_path = arguments.candidate_runtime.resolve()
baseline = load(baseline_path)
candidate = load(candidate_path)
for result in [baseline, candidate]:
    terminal = result['terminal']
    assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged']
    assert terminal['supervisionFailure'] is None and result['probe']['failures'] == 0
    assert len(result['probe']['cases']) == 41 and result['probe']['actualNativePlayerTickBothPhasesAccepted']
    assert result['probe']['preEvents'] == 6 and result['probe']['postEvents'] == 6
    assert 'PausedWeaponImplementationAbsent' in result['probe']['cases']
    assert not result['authenticatedMultiplayerAccepted']
    for name, wanted in result['inputHashes'].items():
        assert digest(name) == wanted, name
assert baseline['baselineGapObserved'] and candidate['flightBanFeatureAccepted']
assert candidate['probe']['originalTailFlightProtectionRestored']
context = load(BUILD / 'compile-context.json')
for name, wanted in context['inputHashes'].items():
    assert digest(name) == wanted, name
proof = load(BUILD / 'proof-terminal.json')
assert proof['exitCode'] == 0
proof_text = (BUILD / 'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;methodsBasicVerified=3' in proof_text
assert 'bothNativeTickPhasesPreserveAllOriginalInstructions=true' in proof_text
assert 'originalLowestPriorityPreserved=true' in proof_text
built = load(BUILD / 'candidate-build-receipt.json')
jar = Path(built['jar'])
assert digest(jar) == built['jarSha256'] == candidate['inputHashes'][str(jar)]
assert built['classCount'] == 1433 and built['allExistingClassAndResourceBytesPreserved'] and not built['stage03ChangesIncluded']
expected_added = {'net/farmingtales/ftboss/boss/ai/FullFightFlightBan.class'}
with zipfile.ZipFile(built['baseJar']) as original, zipfile.ZipFile(jar) as changed:
    assert set(changed.namelist()) - set(original.namelist()) == expected_added
    assert not set(original.namelist()) - set(changed.namelist())
    assert all(original.read(name) == changed.read(name) for name in original.namelist())
process_path = WORK / 'reports/stage06-predeployment-process-audit-v2.json'
process_audit = load(process_path)
assert process_audit['ownedJavaPythonRemaining'] == 0 and process_audit['targetInstanceJavaRemaining'] == 0
assert not process_audit['rawCommandLinesRecorded']
previous_path = WORK / 'releases/boss-core-stage05-status-removal-20261112/deployment-receipt.json'
previous = load(previous_path)
target = Path(previous['target']).resolve()
expected = Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar').resolve()
assert target == expected and digest(target) == previous['deployedSha256'] == built['baseJarSha256']
assert not list(target.parent.glob(target.name + '.stage*.tmp'))
forge_target = Path(load(ROOT / 'baseline.json')['forge']['live'])
forge_before = digest(forge_target)
assert forge_before == '7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
release = WORK / 'releases/boss-core-stage06-flight-guard-20261112'
archive_path = WORK / 'archives/boss-core-stage06-flight-guard-20261112.zip'
assert not release.exists() and not archive_path.exists()
release.mkdir(parents=True)
backup = release / 'rollback-stage05-qys-bosses-1.21.1.jar'
shutil.copy2(target, backup)
assert digest(backup) == previous['deployedSha256']
released = release / 'ftboss-boss-core-1.21.1-stage06.jar'
shutil.copyfile(jar, released)
assert digest(released) == built['jarSha256']
verification = {'feature': 'Original final-priority full-fight flight and Elytra guard in native Pre/Post player ticks', 'baselineReceipt': str(baseline_path), 'baselineCases': 41, 'candidateReceipt': str(candidate_path), 'candidateCases': 41, 'relatedBasicVerifiedMethods': 3, 'originalInstructionAndLowestPriorityPreserved': True, 'addedClasses': sorted(expected_added), 'changedExistingEntries': [], 'allExistingClassAndResourceBytesPreserved': True, 'actualNativePlayerDoTickBothPhasesAccepted': True, 'foreignLowPriorityFlightRegrantRejected': True, 'originalMayflyFlagPreserved': True, 'originalCreativeSpectatorRangeAndNoWindowControlsAccepted': True, 'incrementalFeatureAccepted': True, 'fullBossInstallable': False, 'clientRenderingAccepted': False, 'authenticatedMultiplayerAccepted': False, 'completeBattleAccepted': False, 'stage03PositiveKillAccepted': False, 'stage03CandidateIncluded': False, 'weaponFunctionalDevelopmentPaused': True}
save(release / 'feature-verification.json', verification)
restore = chr(10).join([
 '# Stage06 full-fight flight guard: verified incremental deployment', '',
 'Only the original FullFightFlightBan class is restored. Frozen Forge bytecode is bridged to separate concrete NeoForge PlayerTickEvent.Pre/Post handlers because the native abstract event cannot be subscribed. Both handlers retain original LOWEST priority, all original guard/call/jump/return instructions and original ManorFlyBan behavior. The two original event.player field reads become native getEntity calls. Existing Boss classes, resources, timings, ranges, damage and mayfly flag remain unchanged. The mapped Java source is API-link compiled separately; production bytes are derived from the frozen original by FlightBanBoundaryProof.', '',
 'Rollback: stop the relevant NeoForge game/server; check deployment-receipt.json target and deployedSha256. Restore rollback-stage05-qys-bosses-1.21.1.jar to that exact target, read back SHA256 and compare backupSha256. Never write Forge or historical D. This archive is a release checkpoint, not a complete Minecraft distribution.', '',
 'Rebuild: verify all input hashes in compile-context.json and runtime receipts. Use the frozen stage05 Boss JAR, reference/forge-current.jar and the pinned external SDK. Run the archived build_boss_flight_feature.py only after selecting a NEW output directory; never overwrite frozen build or runtime evidence. FlightBanBoundaryProof checks original instruction equivalence for both native phases and BasicVerifier for all three methods. All stage05 ZIP entries must remain byte-identical and only FullFightFlightBan.class may be added.', '',
 'Directed runtime: baseline and candidate each pass 41 checks in separate genuine NeoForge server JVMs, with no weapon implementation. A test LOW listener regrants ability/Elytra flight after normal handlers. Baseline reproduces the missing tail guard; candidate strips flight again at LOWEST. Both actual native ServerPlayer.doTick phases, unchanged mayfly, creative/spectator/range/no-window controls are verified. Scratch ServerPlayer is explicitly NOT authenticated. Both servers save all dimensions and exit normally. Initial baseline listener registration failure is preserved separately and is not counted as successful acceptance.', '',
 'This accepts the narrow incremental flight guard only. Full Boss/client/GL/authenticated multiplayer/battle acceptance remains false. Stage03 material-reward correction is not included and still requires authenticated positive kill integration; the known live omission is not claimed fixed here. Weapon functional work remains paused. The separate weapon JAR is a recovery snapshot and must not be installed.', '',
 'Original Forge, SDK and historical runtime libraries are read-only external inputs recorded in external-dependencies.json. Disposable runtime scratch is archived and retained, respecting the prior deletion policy rejection; no deletion workaround is attempted.'
 ]) + chr(10)
with (release / 'RESTORE.md').open('x', encoding='utf-8') as stream:
    stream.write(restore)
external = {}
for name in context['classpath'].split(';') + [str(ROOT / 'reference/forge-current.jar')]:
    path = Path(name)
    if path.is_file():
        external[str(path)] = {'bytes': path.stat().st_size, 'sha256': digest(path)}
save(release / 'external-dependencies.json', external)
save(release / 'predeployment.json', {'target': str(target), 'priorSha256': previous['deployedSha256'], 'candidateSha256': digest(released), 'rollback': str(backup), 'rollbackSha256': digest(backup), 'incrementalFeatureVerified': True, 'fullBossInstallable': False, 'processAudit': str(process_path)})
assert digest(target) == previous['deployedSha256'] and digest(forge_target) == forge_before
temporary = target.with_name(target.name + '.stage06-' + uuid.uuid4().hex[:8] + '.tmp')
assert temporary.resolve().parent == expected.parent and not temporary.exists()
shutil.copyfile(released, temporary)
assert digest(temporary) == built['jarSha256']
os.replace(temporary, target)
assert digest(target) == built['jarSha256'] and digest(forge_target) == forge_before
inputs = [Path(__file__), baseline_path, candidate_path, BUILD / 'candidate-build-receipt.json', BUILD / 'compile-context.json', BUILD / 'proof-terminal.json', process_path, release / 'feature-verification.json', release / 'external-dependencies.json']
deployment = {'deployedAt': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'target': str(target), 'deployedSha256': digest(target), 'backup': str(backup), 'backupSha256': digest(backup), 'backupHashVerifiedBeforeDeployment': True, 'previousDeployment': str(previous_path), 'feature': verification['feature'], 'incrementalFeatureAccepted': True, 'fullBossInstallable': False, 'fullBossMigrationComplete': False, 'clientRenderingAccepted': False, 'authenticatedMultiplayerAccepted': False, 'completeBattleAccepted': False, 'formalInstanceLaunched': False, 'stage03CandidateIncluded': False, 'stage03MaterialDropCorrectionStillPending': True, 'weaponCandidateValidatedOrDeployed': False, 'weaponFunctionalDevelopmentPaused': True, 'forgeModified': False, 'forgeSha256Unchanged': forge_before, 'historicalDModified': False, 'duplicateDependencyModsChanged': False, 'inputHashes': {str(path): digest(path) for path in inputs}}
save(release / 'deployment-receipt.json', deployment)
files = list(release.rglob('*')) + list(BUILD.rglob('*')) + list((WORK / 'build/boss-flight-ban-original-20261112').rglob('*'))
files += [WORK / 'STAGE06_FULL_FIGHT_FLIGHT_GUARD.md', WORK / 'CURRENT_BOSS_BATCH.json', ROOT / 'AGENTS.md', ROOT / 'WRITE_OWNER.json', ROOT / 'WRITE_OWNER.claim', WORK / 'boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/FullFightFlightBan.java', process_path]
files += [WORK / 'tools' / name for name in ['build_boss_flight_feature.py', 'run_boss_flight_probe.py', 'run_boss_flight_probe_v2.py', Path(__file__).name]]
files += [WORK / 'tests' / name for name in ['FlightBanBoundaryProof.java', 'BossFlightRuntimeProbe.java', 'flight-ban-v2/BossFlightRuntimeProbe.java']]
scratch = []
failed_path = WORK / 'reports/flight-ban-baseline-c7c8bb72.json'
for receipt_path, result in [(failed_path, load(failed_path)), (baseline_path, baseline), (candidate_path, candidate)]:
    runtime = Path(result['attempt'])
    files.append(receipt_path)
    for name in ['runtime.log', 'terminal.json', 'runtime-command.json', 'inputs-before.json', 'compile-command.json', 'compile-terminal.json', 'compile.log', 'live-process.json', 'server/actual-flight-ban-probe.json']:
        files.append(runtime / name)
    for name in ['isolated-home', 'isolated-tmp', 'observer-classes', 'observer-resources', 'server/crash-reports', 'server/logs']:
        folder = runtime / name
        files += list(folder.rglob('*'))
        if name.startswith('isolated-'):
            scratch.append(str(folder))
files += [WORK / 'releases/boss-core-stage05-status-removal-20261112' / name for name in ['deployment-receipt.json', 'archive-receipt.json', 'RESTORE.md']]
manifest = {}
with zipfile.ZipFile(archive_path, 'x', compression=zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(set(files)):
        if not path.is_file():
            continue
        assert path.resolve().is_relative_to(ROOT.resolve()) and not path.is_symlink()
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {'bytes': path.stat().st_size, 'sha256': digest(path)}
        archive.write(path, name)
    archive.writestr('ARCHIVE-MANIFEST.json', json.dumps(manifest, ensure_ascii=False, indent=2))
with zipfile.ZipFile(archive_path) as archive:
    assert archive.testzip() is None
    for name, row in manifest.items():
        content = archive.read(name)
        assert len(content) == row['bytes'] and hashlib.sha256(content).hexdigest() == row['sha256']
assert digest(target) == built['jarSha256'] and digest(backup) == previous['deployedSha256'] and digest(forge_target) == forge_before
save(release / 'archive-receipt.json', {'archive': str(archive_path), 'archiveSha256': digest(archive_path), 'archiveBytes': archive_path.stat().st_size, 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'physicalRestorePerformed': False, 'failures': 0, 'deployedIncrementSha256': digest(target), 'fullBossInstallable': False, 'scratchArchivedAndRetained': scratch, 'scratchDeletionNotAttemptedBecausePriorPolicyRejection': True})
print(json.dumps({'deploymentReceipt': str(release / 'deployment-receipt.json'), 'archive': str(archive_path), 'archiveSha256': digest(archive_path), 'archiveBytes': archive_path.stat().st_size, 'files': len(manifest), 'baselineCases': 41, 'candidateCases': 41, 'deployedSha256': digest(target), 'fullBossInstallable': False}, ensure_ascii=False), flush=True)
