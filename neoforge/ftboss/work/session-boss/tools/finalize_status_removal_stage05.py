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
BUILD = WORK / 'build/boss-status-removal-v1'
parser = argparse.ArgumentParser()
parser.add_argument('baseline_runtime', type=Path)
parser.add_argument('candidate_runtime', type=Path)
arguments = parser.parse_args()

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

assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
baseline_path = arguments.baseline_runtime.resolve()
candidate_path = arguments.candidate_runtime.resolve()
baseline = load(baseline_path)
candidate = load(candidate_path)
for result in [baseline, candidate]:
    terminal = result['terminal']
    assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged']
    assert terminal['supervisionFailure'] is None and result['probe']['failures'] == 0
    for name, wanted in result['inputHashes'].items():
        assert digest(name) == wanted, name
assert baseline['baselineGapObserved'] and len(baseline['probe']['cases']) == 47
assert candidate['statusRemovalFeatureAccepted'] and len(candidate['probe']['cases']) == 49
assert candidate['probe']['actualNaturalPostTicks'] >= 20 and candidate['probe']['bossCanceledCallbacks'] >= 4
proof = load(BUILD / 'proof-terminal.json')
assert proof['proofExitCode'] == 0 and 'noOriginalDamageDurationAiMethodsChanged=true' in proof['proofOutput']
context = load(BUILD / 'compile-context.json')
assert context['exitCode'] == 0 and digest(context['source']) == context['sourceSha256']
assert digest(WORK / 'tests/StatusRemovalBoundaryProof.java') == load(BUILD / 'proof-compile-terminal.json')['sourceSha256']
built = load(BUILD / 'candidate-build-receipt.json')
jar = Path(built['jar'])
assert digest(jar) == built['jarSha256'] == candidate['inputHashes'][str(jar)]
assert built['classCount'] == 1432 and built['allOtherClassAndResourceBytesPreserved'] and not built['stage03ChangesIncluded']
expected_changed = {'net/farmingtales/ftboss/effect/HealBanEffect.class', 'net/farmingtales/ftboss/effect/ShadowErosionEffect.class'}
with zipfile.ZipFile(built['baseJar']) as original, zipfile.ZipFile(jar) as changed:
    assert set(original.namelist()) == set(changed.namelist())
    assert {name for name in original.namelist() if original.read(name) != changed.read(name)} == expected_changed
process_path = WORK / 'reports/stage05-predeployment-process-audit.json'
process_audit = load(process_path)
assert process_audit['ownedJavaPythonRemaining'] == 0 and not process_audit['rawCommandLinesRecorded']
assert process_audit['targetInstanceJavaRemaining'] == 0
previous_path = WORK / 'releases/boss-core-stage04-status-immunity-20261112/deployment-receipt.json'
previous = load(previous_path)
target = Path(previous['target']).resolve()
expected = Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar').resolve()
assert target == expected and digest(target) == previous['deployedSha256'] == built['baseJarSha256']
assert not list(target.parent.glob(target.name + '.stage*.tmp'))
forge_target = Path(load(ROOT / 'baseline.json')['forge']['live'])
forge_before = digest(forge_target)
release = WORK / 'releases/boss-core-stage05-status-removal-20261112'
assert not release.exists()
release.mkdir(parents=True)
backup = release / 'rollback-stage04-qys-bosses-1.21.1.jar'
shutil.copy2(target, backup)
assert digest(backup) == previous['deployedSha256']
released = release / 'ftboss-boss-core-1.21.1-stage05.jar'
shutil.copyfile(jar, released)
assert digest(released) == built['jarSha256']
verification = {'feature': 'Original HealBan and ShadowErosion removal/cure protection, Manor purge exception and natural expiration', 'baselineReceipt': str(baseline_path), 'baselineCases': 47, 'candidateReceipt': str(candidate_path), 'candidateCases': 49, 'relatedBasicVerifiedMethods': 15, 'allFourOriginalNativeMethodsPreserved': True, 'classDelta': sorted(expected_changed), 'allOtherClassAndResourceBytesPreserved': True, 'originalForgeEmptyCureOverridesVerified': 2, 'fullBossInstallable': False, 'clientRenderingAccepted': False, 'authenticatedMultiplayerAccepted': False, 'completeBattleAccepted': False, 'stage03PositiveKillAccepted': False, 'stage03CandidateIncluded': False, 'weaponFunctionalDevelopmentPaused': True}
save(release / 'feature-verification.json', verification)
restore = chr(10).join(['# Stage05 status removal protection: incremental deployment only', '', 'This restores the original non-curable HealBan/ShadowErosion removal rules. Forge empty getCurativeItems becomes NeoForge empty fillEffectCures. HealBanEffect.onRemove retains original LOWEST receiveCanceled behavior and ManorStatusImmunity.protectRemoval exception. Existing constructors, damage/duration/AI and all other class/resource bytes remain stage04. Stage03 material-reward correction is NOT included and the known deployed reward omission remains.', '', 'Rollback: stop the relevant NeoForge game/server; verify exact deployment target and current SHA in deployment-receipt.json. Copy rollback-stage04-qys-bosses-1.21.1.jar to that exact target. Read back SHA and compare backupSha256. Do not write Forge or historical D.', '', 'Rebuild: use frozen stage04 Boss JAR plus SDK referenced by build/boss-status-removal-v1/compile-context.json. Compile only effect/HealBanEffect.java. StatusRemovalBoundaryProof adds only the empty fillEffectCures override to original ShadowErosionEffect. All four existing native methods retain their original instructions; 15 related methods pass BasicVerifier. Package only these two existing effect classes; class count stays 1432. build_status_removal_feature.py recreates this procedure in a new, unused output directory after adjusting its output name, never over frozen evidence.', '', 'Runtime: genuine NeoForge baseline 47 checks demonstrates native direct/remove-all/milk/totem and extended cure-list gaps. Candidate 49 checks verifies protection, vanilla controls, real natural expiry/healing restoration, and actual Manor join/tick purge even when HIGHEST event listeners have canceled removal. Both runs advance 20 natural post ticks, save all dimensions and exit normally. This is directed server evidence, not client/GL/authenticated multiplayer/full battle acceptance.', '', 'Archive contains source, tools, targeted ASM/native original bytecode, compile/runtime logs, hash manifests and receipts. Original Forge snapshot and historical SDK/prior runtime libraries are read-only external prerequisites; this is not a full game distribution. The weapon system remains paused with its separate recovery snapshot JAR, which is not an accepted runnable addon.', '', 'Prior automatic-policy rejection of temporary-directory deletion is respected. Isolated scratch files are archived and retained; no deletion retry or bypass is attempted. Exited processes and retained directories are distinct.']) + chr(10)
with (release / 'RESTORE.md').open('x', encoding='utf-8') as stream:
    stream.write(restore)
save(release / 'predeployment.json', {'target': str(target), 'priorSha256': previous['deployedSha256'], 'candidateSha256': digest(released), 'rollback': str(backup), 'rollbackSha256': digest(backup), 'incrementalFeatureVerified': True, 'fullBossInstallable': False})
assert digest(target) == previous['deployedSha256']
temporary = target.with_name(target.name + '.stage05-' + uuid.uuid4().hex[:8] + '.tmp')
assert temporary.resolve().parent == expected.parent and not temporary.exists()
shutil.copyfile(released, temporary)
assert digest(temporary) == built['jarSha256']
os.replace(temporary, target)
assert digest(target) == built['jarSha256'] and digest(forge_target) == forge_before
inputs = [Path(__file__), baseline_path, candidate_path, BUILD / 'candidate-build-receipt.json', BUILD / 'compile-context.json', BUILD / 'proof-terminal.json', WORK / 'reports/pre-stage05-audit-20261112.json', process_path, release / 'feature-verification.json']
deployment = {'deployedAt': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'target': str(target), 'deployedSha256': digest(target), 'backup': str(backup), 'backupSha256': digest(backup), 'backupHashVerifiedBeforeDeployment': True, 'previousDeployment': str(previous_path), 'feature': verification['feature'], 'incrementalFeatureAccepted': True, 'fullBossInstallable': False, 'fullBossMigrationComplete': False, 'clientRenderingAccepted': False, 'authenticatedMultiplayerAccepted': False, 'completeBattleAccepted': False, 'formalInstanceLaunched': False, 'stage03CandidateIncluded': False, 'stage03MaterialDropCorrectionStillPending': True, 'weaponCandidateValidatedOrDeployed': False, 'weaponFunctionalDevelopmentPaused': True, 'forgeModified': False, 'historicalDModified': False, 'duplicateDependencyModsChanged': False, 'inputHashes': {str(path): digest(path) for path in inputs}}
save(release / 'deployment-receipt.json', deployment)
files = list(release.rglob('*')) + list(BUILD.rglob('*')) + [WORK / 'STAGE05_STATUS_REMOVAL_PROTECTION.md', WORK / 'reports/pre-stage05-audit-20261112.json', process_path, WORK / 'CURRENT_BOSS_BATCH.json', ROOT / 'AGENTS.md', ROOT / 'WRITE_OWNER.json', ROOT / 'WRITE_OWNER.claim', WORK / 'boss-core/src/main/java/net/farmingtales/ftboss/effect/HealBanEffect.java']
files += [WORK / 'tools' / name for name in ['build_status_removal_feature.py', 'run_status_removal_probe.py', Path(__file__).name]]
files += [WORK / 'tests' / name for name in ['StatusRemovalBoundaryProof.java', 'StatusRemovalRuntimeProbe.java']]
scratch = []
for receipt_path, result in [(baseline_path, baseline), (candidate_path, candidate)]:
    runtime = Path(result['attempt'])
    files.append(receipt_path)
    for name in ['runtime.log', 'terminal.json', 'runtime-command.json', 'inputs-before.json', 'compile-command.json', 'compile-terminal.json', 'compile.log', 'live-process.json', 'server/actual-status-removal-probe.json']:
        files.append(runtime / name)
    for name in ['isolated-home', 'isolated-tmp', 'observer-classes', 'observer-resources']:
        folder = runtime / name
        files += list(folder.rglob('*'))
        if name.startswith('isolated-'):
            scratch.append(str(folder))
for stage in ['boss-core-stage01-20261112', 'boss-core-stage02-absorption-20261112', 'boss-core-stage04-status-immunity-20261112']:
    files += [WORK / 'releases' / stage / name for name in ['deployment-receipt.json', 'archive-receipt.json', 'RESTORE.md']]
archive_path = WORK / 'archives/boss-core-stage05-status-removal-20261112.zip'
assert not archive_path.exists()
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
        assert hashlib.sha256(archive.read(name)).hexdigest() == row['sha256']
assert digest(target) == built['jarSha256'] and digest(backup) == previous['deployedSha256']
save(release / 'archive-receipt.json', {'archive': str(archive_path), 'archiveSha256': digest(archive_path), 'archiveBytes': archive_path.stat().st_size, 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'physicalRestorePerformed': False, 'failures': 0, 'deployedIncrementSha256': digest(target), 'fullBossInstallable': False, 'scratchArchivedAndRetained': scratch, 'scratchDeletionNotAttemptedBecausePriorPolicyRejection': True})
print(json.dumps({'deploymentReceipt': str(release / 'deployment-receipt.json'), 'archive': str(archive_path), 'archiveBytes': archive_path.stat().st_size, 'files': len(manifest), 'baselineCases': 47, 'candidateCases': 49, 'deployedSha256': digest(target), 'fullBossInstallable': False}), flush=True)
