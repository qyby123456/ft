import argparse
import datetime
import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
RELEASE = WORK / 'releases/boss-core-stage14-client-resources-20261113'
EXPECTED = '6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c'
PREVIOUS = '58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

def processes():
    query = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease');formal=($_.CommandLine -match 'FarmingTales_NeoForge|FarmingTales_Forge')} } | ConvertTo-Json -Compress"
    raw = subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
    rows = json.loads(raw) if raw else []
    rows = rows if isinstance(rows,list) else [rows]
    assert not any(row['pid'] != os.getpid() and (row['owned'] or row['formal']) for row in rows), rows
    return rows

def frozen_report(path):
    report = load(path)
    assert report['terminal']['exitCode'] == 0 and report['terminal']['clientProcessReaped'] and report['terminal']['integratedWorldCompleteSave'] and report['terminal']['inputHashesUnchanged']
    for name, expected in report['inputHashes'].items():
        assert sha(name) == expected, name
    assert EXPECTED in report['inputHashes'].values()
    return report

def archive(path, files):
    manifest = {str(file.relative_to(ROOT)).replace('\\','/'): {'sha256':sha(file),'bytes':file.stat().st_size} for file in sorted(files)}
    encoded = json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8')
    with zipfile.ZipFile(path,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
        bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
        for name in manifest:
            bundle.write(ROOT / name,name)
    with zipfile.ZipFile(path) as bundle:
        assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json') == encoded
        for name,row in manifest.items():
            payload = bundle.read(name)
            assert len(payload) == row['bytes'] and hashlib.sha256(payload).hexdigest() == row['sha256'], name
    return {'archive':str(path),'archiveSha256':sha(path),'files':len(manifest),'allFilesReadBackAndHashVerified':True,'failures':0}

parser = argparse.ArgumentParser()
parser.add_argument('remaining_report',type=Path)
parser.add_argument('reset_report',type=Path)
arguments = parser.parse_args()
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == load(ROOT / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
before_processes = processes()
assert not RELEASE.exists()
build_path = WORK / 'build/boss-stage14-resources-only-v1/candidate-build-receipt.json'
built = load(build_path)
jar = Path(built['jar'])
assert sha(jar) == built['jarSha256'] == EXPECTED
assert built['stage13ClassesIncluded'] is False and built['allDeployedStage12EntriesByteIdentical'] and built['addedResourceCount'] == 314
for name,expected in built['inputHashes'].items():
    assert sha(name) == expected, name
manor_path = WORK / 'reports/client-boss-matrix-v4-6c8c85da.json'
statue_path = WORK / 'reports/client-statue-matrix-v1-a7378903.json'
manor = frozen_report(manor_path)
statues = frozen_report(statue_path)
remaining_path = arguments.remaining_report.resolve()
assert remaining_path.parent == WORK / 'reports' and remaining_path.name.startswith('client-boss-matrix-v5-')
remaining = frozen_report(remaining_path)
reset_path = arguments.reset_report.resolve()
assert reset_path.parent == WORK / 'reports' and reset_path.name.startswith('client-boss-darkness-v6-')
reset = frozen_report(reset_path)
assert reset['probe']['failures'] == 0 and reset['probe']['actualBossMatrixAccepted'] and reset['probe']['completedViewCount'] == 4
assert reset['probe']['nativeEffectsNaturallyExpiredWithoutRemoval'] and reset['probe']['nativeDarknessResetBrightSkyPixels'] >= 80000
assert reset['probe']['nativeSpectatorDarknessExclusionAndResetAccepted'] and reset['probe']['nativeDarknessResetBrightPixels'] >= 50000
assert manor['probe']['failures'] == 1 and 'Actual body framebuffer missing for case 96' in manor['probe']['failure']
assert [row['caseIndex'] for row in manor['probe']['views']] == list(range(96))
assert all(row['changedWorldPixels'] > 100 and row['actualLivingRenderReturns'] >= 3 and row['textureResourcePresent'] for row in manor['probe']['views'])
assert all(row['actualClientDifficulty'] == row['serverFixtureDifficulty'] for row in manor['probe']['views'])
assert statues['probe']['failures'] == 0 and statues['probe']['actualBossMatrixAccepted'] and statues['probe']['completedViewCount'] == 44
assert all(row['lowerBakedQuads'] > 0 and (row['upperBakedQuads'] > 0 or row['subject'] == 'blank') for row in statues['probe']['views'])
assert remaining['probe']['failures'] == 0 and remaining['probe']['actualBossMatrixAccepted'] and remaining['probe']['completedViewCount'] == 44
assert load(WORK / 'reports/boss-client-resource-completion-20261113/v5-reset-visual-invalidation.json')['originalGreenResetFlagInvalidatedAfterVisualReview']
assert all(row['actualEntityDispatcherRenderReturns'] >= 3 and row['changedWorldPixels'] > 100 for row in remaining['probe']['views'])
review_dir = WORK / 'reports/boss-client-resource-completion-20261113'
assert load(review_dir / 'visual-review-v4/review.json')['all96ActualBodyImagesReviewedInThreeOriginalCenterCropSheets']
assert load(review_dir / 'visual-review-statues-v1/review.json')['all44ActualStatueFramesReviewed']
review = load(review_dir / 'visual-review-v5/review.json')
assert review['remainingActualBodyViewsReviewed'] == 40 and review['survivalDarknessViewsReviewed'] == 4 and review['bodyAndSurvivalVisualAcceptancePassed']
assert review['reportSha256'] == sha(remaining_path)
reset_review = load(review_dir / 'visual-review-v6/review.json')
assert reset_review['nativeResetFrameReviewed'] and reset_review['fullNativeResetVisualAcceptancePassed'] and reset_review['reportSha256'] == sha(reset_path)
static = load(review_dir / 'stage14-static-resource-closure.json')
assert static['sha256'] == EXPECTED and static['modelCount'] == 21
previous_path = WORK / 'releases/boss-core-stage12-bone-crush-20261112/deployment-receipt.json'
previous = load(previous_path)
target = Path(previous['target'])
assert sha(target) == previous['deployedSha256'] == PREVIOUS
forge = Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar')
forge_sha = '7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
assert sha(forge) == forge_sha
with zipfile.ZipFile(Path(built['baseJar'])) as base,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None
    assert all(base.read(name) == current.read(name) for name in base.namelist())
    assert not any(name.startswith('net/farmingtales/ftboss/butcherskills/ButcherSummonFx') for name in current.namelist())
paused_path = WORK / 'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json'
paused = load(paused_path)
assert paused['failures'] == 0 and sha(paused['archive']) == paused['archiveSha256']
RELEASE.mkdir()
released = RELEASE / 'ftboss-boss-core-1.21.1-stage14-resources.jar'
backup = RELEASE / 'rollback-stage12-qys-bosses-1.21.1.jar'
shutil.copyfile(jar,released)
shutil.copyfile(target,backup)
assert sha(released) == EXPECTED and sha(backup) == PREVIOUS
feature = {'scope':'Only 314 original Boss resources added to deployed stage12, including 21 original OBJ model loader namespace ports. All existing 1443 class bytes and all existing resource bytes unchanged. No stage13 classes integrated.','candidateSha256':EXPECTED,'allDeployedStage12EntriesByteIdentical':True,'addedBossResources':314,'portedObjModels':21,'actualManorBodyViews':96,'actualNonManorBodyViews':40,'actualSurvivalDarknessViews':4,'actualStatueViews':44,'nativeSpectatorDarknessResetAccepted':True,'nativeEffectsNaturallyExpiredWithoutRemoval':True,'priorV5IncompleteResetGreenFlagInvalidated':True,'manorPartialNegativeReportExplicitlyPreserved':True,'reports':{str(path):sha(path) for path in [manor_path,statue_path,remaining_path,reset_path]},'resourceIncrementAccepted':True,'stage13IncludedOrDeployed':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'fullBattleAccepted':False,'fullBossMigrationComplete':False,'weaponDevelopmentPaused':True}
save(RELEASE / 'feature-verification.json',feature)
save(RELEASE / 'deployment-intent.json',{'target':str(target),'oldSha256':PREVIOUS,'newSha256':EXPECTED,'backup':str(backup),'backupSha256':sha(backup),'userAuthorizedVerifiedBossIncrement':True,'formalInstanceLaunched':False,'processAudit':before_processes})
temporary = target.with_name(target.name+'.stage14-resources.tmp')
assert not temporary.exists()
shutil.copyfile(released,temporary)
assert sha(temporary) == EXPECTED and sha(target) == PREVIOUS and sha(backup) == PREVIOUS
os.replace(temporary,target)
assert sha(target) == EXPECTED and sha(forge) == forge_sha
save(RELEASE / 'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':sha(target),'backup':str(backup),'backupSha256':sha(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'actualIsolatedNativeClientLoadedExactJar':True,'formalInstanceLaunched':False,'allExistingStage12ClassBytesPreserved':1443,'addedBossResources':314,'stage13IncludedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_sha,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False})
print(json.dumps({'stage14ResourceIncrementDeployed':True,'sha256':sha(target),'rollbackSha256':sha(backup),'fullBossMigrationComplete':False}),flush=True)
selected = {Path(__file__),ROOT / 'AGENTS.md',ROOT / 'WRITE_OWNER.json',ROOT / 'WRITE_OWNER.claim',WORK / 'CURRENT_BOSS_BATCH.json',WORK / 'BOSS_MIGRATION_REQUIREMENTS_20261113.md',WORK / 'DEPLOYED_PROGRESS.md',previous_path,paused_path}
external = {}
mod_copies = {}
reused = load(WORK / 'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json')
assert sha(reused['archive']) == reused['archiveSha256']
with zipfile.ZipFile(reused['archive']) as bundle:
    prior_manifest = json.loads(bundle.read('ARCHIVE_MANIFEST.json'))
    by_name = {Path(name).name:row['sha256'] for name,row in prior_manifest.items() if '/server/mods/' in name}
for prefix in ['client-boss-matrix-v1-','client-boss-matrix-v2-','client-boss-matrix-v3-','client-boss-matrix-v4-','client-boss-matrix-v5-','client-statue-matrix-v1-','client-boss-darkness-v6-']:
    attempts = list((WORK / 'build').glob(prefix+'*'))
    assert len(attempts) == 1, attempts
    attempt = attempts[0]
    assert load(attempt / 'terminal.json')['clientProcessReaped']
    selected.add(WORK / 'reports' / (attempt.name+'.json'))
    for name,wanted in load(attempt / 'inputs-before.json').items():
        assert sha(name) == wanted, name
        path = Path(name)
        if path.is_relative_to(ROOT):
            selected.add(path)
        else:
            external[name] = wanted
    for path in attempt.rglob('*'):
        if not path.is_file() or path.name == 'session.lock':
            continue
        relative = path.relative_to(attempt)
        if relative.parts[:2] == ('game','mods') and 'ftboss-boss-core-' not in path.name:
            wanted = sha(path)
            assert by_name.get(path.name) == wanted, path
            mod_copies[str(path.relative_to(ROOT))] = {'sha256':wanted,'bytes':path.stat().st_size,'restoreFrom':reused['archive']}
            continue
        selected.add(path)
for folder in ['build/boss-client-resource-completion-v1','build/boss-client-resource-completion-v2','build/boss-stage14-resources-only-v1','build/boss-blackwall-visual-audit-20261113','build/boss-xuanlan-boundary-audit-20261113','build/client-statue-compile-preflight-v1','build/client-boss-v5-compile-preflight-v1','tests/client-boss-matrix-v1','tests/client-boss-matrix-v2','tests/client-boss-matrix-v3','tests/client-boss-matrix-v4','tests/client-boss-matrix-v5','tests/client-boss-darkness-v6','tests/client-statue-matrix-v1','reports/boss-client-resource-completion-20261113']:
    selected.update(path for path in (WORK / folder).rglob('*') if path.is_file())
for pattern in ['*client_resource_completion*','*stage14_resources*','run_boss_client_render_matrix*','run_boss_client_statue_matrix*','run_boss_client_darkness*']:
    selected.update(path for path in (WORK / 'tools').glob(pattern) if path.is_file())
save(RELEASE / 'copied-mods-restore-manifest.json',mod_copies)
save(RELEASE / 'external-read-only-inputs.json',external)
save(RELEASE / 'reused-original-315-archive.json',reused)
save(RELEASE / 'terminal-audit.json',{'processes':processes(),'ownedRuntimeOrSupervisorRemaining':0,'formalTemporaryAbsent':not temporary.exists(),'targetSha256':sha(target),'backupSha256':sha(backup),'forgeSha256':sha(forge),'stage13Deployed':False,'fullGoalComplete':False})
creator = ROOT / 'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
namespace = {'__file__':str(creator)}
exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],namespace)
namespace['create'](RELEASE / 'RESTORE.md','# Stage14 Boss client resource increment\n\nThis release is deployed stage12 plus 314 original Boss assets. All 1443 deployed class bytes remain identical; stage13 summon FX is not included. The 21 restored OBJ model loader namespaces use neoforge:obj. Native tests cover 136 idle body views, four survival darkness views and stock spectator reset, plus 44 statue world views and 21 actual baked OBJ model checks. These directed offline fixtures do not establish authenticated rewards, multiplayer, full battles or complete migration.\n\nThe original failed probes and partial v4 negative report are preserved. All selected files are read-back/SHA verified in ARCHIVE_MANIFEST.json. Copied common mod JARs restore from the exact-name/SHA frozen original315 archive; external SDK/client libraries remain at their recorded read-only paths. No failed world is a restart seed.\n\nRollback: stop the correct NeoForge instance, verify deployment-receipt target and both hashes, then restore rollback-stage12-qys-bosses-1.21.1.jar to that exact target. Forge and historical D remain read-only. Weapons remain paused with the separate noninstallable recovery snapshot. Full Boss goal remains active.\n')
selected.update(path for path in RELEASE.rglob('*') if path.is_file())
archived = archive(WORK / 'archives/boss-core-stage14-client-resources-20261113.zip',selected)
save(RELEASE / 'archive-receipt.json',archived)
print(json.dumps({'archive':archived,'deployedSha256':sha(target),'backupSha256':sha(backup),'fullBossMigrationComplete':False}),flush=True)
