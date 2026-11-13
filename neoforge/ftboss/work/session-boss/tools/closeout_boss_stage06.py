import datetime
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
RELEASE = WORK / 'releases/boss-core-stage06-flight-guard-20261112'

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

process_path = WORK / 'reports/stage06-postdeployment-process-audit.json'
processes = load(process_path)
assert processes['ownedJavaPythonRemaining'] == 0 and processes['targetInstanceJavaRemaining'] == 0
assert not processes['rawCommandLinesRecorded'] and not processes['stoppedUnrelatedProcesses']
deployment = load(RELEASE / 'deployment-receipt.json')
archive = load(RELEASE / 'archive-receipt.json')
assert digest(deployment['target']) == deployment['deployedSha256'] == 'e4dd3c8d4fff8a66b5eefeac6aa96eb24304cf716e9cd8c5b7424c4ae65c034f'
assert digest(deployment['backup']) == deployment['backupSha256'] == '4f89f69fbc9c90f1b7cd8a29f463568448442c60085538944de199b730fa0016'
assert digest(archive['archive']) == archive['archiveSha256'] and archive['allFilesReadBackAndHashVerified']
forge = Path(load(ROOT / 'baseline.json')['forge']['live'])
assert digest(forge) == deployment['forgeSha256Unchanged'] == '7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
weapons_base = WORK / 'archives/weapons-paused-a08a7373'
checks = {
 str(weapons_base / 'weapons-source-evidence.zip'): '5fb38194a79abeed7ccb97a7e6127cd765dd7ad7df8bae85730e7df45dc0115f',
 str(weapons_base / 'manifest.jsonl'): '68af54f6b36e89cdbd58d6b1c018364d76762f41c92c4f8743a60d914e5fd148',
 str(weapons_base / 'external-dependencies.json'): '7ebe85612be45441ecae5bc99c183bd22f102af1e89c92305fc5fad8ce369f97',
 str(weapons_base / 'RESTORE.md'): '63dd1e2385ac7f16fda8f3ab9761d83fc62aed73598c6bff51013dcffc9ffa0b'
}
for name, wanted in checks.items():
    assert digest(name) == wanted, name
delta_folder = WORK / 'archives/weapons-paused-dd1ad841-20261112'
delta = load(delta_folder / 'archive-receipt.json')
for name, wanted in [(delta['archive'], delta['archiveSha256']), (delta['snapshotJar'], delta['snapshotJarSha256']), (delta_folder / 'manifest.jsonl', delta['manifestSha256']), (delta_folder / 'RESTORE.md', delta['restoreInstructionsSha256'])]:
    assert digest(name) == wanted, str(name)
    checks[str(name)] = wanted
with zipfile.ZipFile(delta['snapshotJar']) as snapshot:
    assert snapshot.testzip() is None
    assert 'META-INF/neoforge.mods.toml' not in snapshot.namelist()
    assert 'PAUSED_STATE.json' in snapshot.namelist()
with zipfile.ZipFile(deployment['target']) as boss:
    assert 'net/farmingtales/ftboss/item/FoodWeaponFinalDamage.class' not in boss.namelist()
    assert 'net/farmingtales/ftboss/boss/ai/FullFightFlightBan.class' in boss.namelist()
mods = Path(deployment['target']).parent
assert not list(mods.glob(Path(deployment['target']).name + '.stage*.tmp'))
assert not list(mods.glob('ftboss-weapons*.jar'))
state = load(WORK / 'CURRENT_BOSS_BATCH.json')
owner = load(ROOT / 'WRITE_OWNER.json')
assert state['latestDeployedSha256'] == deployment['deployedSha256']
assert state['stage06DeploymentPerformed'] and not state['stage03DeploymentPerformed']
assert owner['currentGoalStatus'] == 'paused' and owner['weaponFunctionalDevelopment'] == 'paused'
assert owner['bossActiveRuntimePid'] is None and owner['bossActiveSupervisor'] is None
assert not owner['weaponCandidateValidationResumed']
audit = {'at': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'formalNeoForgeSha256': digest(deployment['target']), 'formalForgeSha256Unchanged': digest(forge), 'stage05RollbackHashVerified': True, 'stage06MainArchiveHashVerified': True, 'stage06MainArchivePreviouslyAllEntriesReadBackVerified': True, 'weaponCheckpointHashChecks': checks, 'weaponSourceArchivesPreviouslyAllBlobsReadBackVerified': True, 'weaponSnapshotIsNonInstallableRecoveryJar': True, 'weaponImplementationAbsentInDeployedBossJar': True, 'weaponFunctionalDevelopmentPaused': True, 'weaponGoalStatus': 'paused', 'ownedJavaPythonRemaining': 0, 'targetInstanceJavaRemaining': 0, 'processAudit': str(process_path), 'unrelatedUserGameAndServicesPreserved': True, 'deploymentTemporaryFiles': [], 'halfWrittenDeploymentDetected': False, 'scratchArchivedAndRetainedWithoutDeletionBypass': True, 'fullBossAcceptance': False, 'stage03MaterialRewardCorrectionDeployed': False, 'latestBossIncrementAcceptedAndDeployed': 'stage06', 'remainingBossCheckpoint': str(WORK / 'REMAINING_BOSS_CHECKPOINT_20261112.md'), 'soleWriterUnchanged': True}
audit_path = WORK / 'reports/stage06-terminal-audit-20261112.json'
save(audit_path, audit)
files = [Path(__file__), audit_path, process_path, WORK / 'reports/stage06-predeployment-process-audit.json', WORK / 'reports/stage06-predeployment-process-audit-v2.json', WORK / 'CURRENT_BOSS_BATCH.json', WORK / 'DEPLOYED_PROGRESS.md', WORK / 'STAGE06_FULL_FIGHT_FLIGHT_GUARD.md', WORK / 'REMAINING_BOSS_CHECKPOINT_20261112.md', WORK / 'reports/boss-remaining-behavior-audit-20261112.json', ROOT / 'WRITE_OWNER.json', ROOT / 'WRITE_OWNER.claim', ROOT / 'AGENTS.md', ROOT / 'work/session-lease/CURRENT_INCREMENTAL_BATCH.json', ROOT / 'work/session-lease/RESUMED_GOAL_20261112.md']
files += [RELEASE / name for name in ['archive-receipt.json', 'deployment-receipt.json', 'feature-verification.json', 'RESTORE.md', 'external-dependencies.json']]
files += [delta_folder / name for name in ['archive-receipt.json', 'manifest.jsonl', 'RESTORE.md']]
files += list((WORK / 'build/boss-remaining-behavior-audit-20261112').rglob('*'))
archive_path = WORK / 'archives/boss-core-stage06-closeout-20261112.zip'
manifest = {}
with zipfile.ZipFile(archive_path, 'x', compression=zipfile.ZIP_DEFLATED) as packed:
    for path in sorted(set(files)):
        if not path.is_file():
            continue
        assert path.resolve().is_relative_to(ROOT.resolve()) and not path.is_symlink()
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {'bytes': path.stat().st_size, 'sha256': digest(path)}
        packed.write(path, name)
    packed.writestr('ARCHIVE-MANIFEST.json', json.dumps(manifest, ensure_ascii=False, indent=2))
with zipfile.ZipFile(archive_path) as packed:
    assert packed.testzip() is None
    for name, row in manifest.items():
        payload = packed.read(name)
        assert len(payload) == row['bytes'] and hashlib.sha256(payload).hexdigest() == row['sha256']
receipt = {'archive': str(archive_path), 'archiveSha256': digest(archive_path), 'archiveBytes': archive_path.stat().st_size, 'files': len(manifest), 'allFilesReadBackAndHashVerified': True, 'terminalAudit': str(audit_path), 'terminalAuditSha256': digest(audit_path), 'mainArchive': archive['archive'], 'mainArchiveSha256': archive['archiveSha256'], 'deployedSha256': digest(deployment['target']), 'weaponsPaused': True, 'fullBossAccepted': False, 'failures': 0}
save(RELEASE / 'closeout-archive-receipt.json', receipt)
print(json.dumps({'closeoutArchive': str(archive_path), 'archiveSha256': receipt['archiveSha256'], 'files': len(manifest), 'ownedJavaPythonRemaining': 0, 'formalNeoForgeSha256': receipt['deployedSha256'], 'weaponGoalPaused': True, 'fullBossAccepted': False}, ensure_ascii=False), flush=True)
