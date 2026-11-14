import argparse
import hashlib
import json
import shutil
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
HELPER=WORK/'tools/finalize_boss_stage14_resources_v4.py'
namespace={'__file__':str(HELPER),'__name__':'archive_helper_only'}
exec(compile(HELPER.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],str(HELPER),'exec'),namespace)
load=namespace['load']
sha=namespace['sha']
save=namespace['save']
parser=argparse.ArgumentParser()
parser.add_argument('valid_lifecycle_report',type=Path)
parser.add_argument('reload_report',type=Path)
parser.add_argument('screen_diagnostic_report',type=Path)
arguments=parser.parse_args()
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused'] and owner['weaponSnapshotInstallable'] is False
process_state=namespace['processes']()
assert not owner.get('activeRuntimePids')
formal=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar')
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar')
assert sha(formal)=='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0'
assert sha(forge)=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
built_path=WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json'
built=load(built_path)
assert sha(built['jar'])==built['jarSha256']=='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
assert built['allExistingEntriesByteIdentical'] and built['changedExistingEntries']==[]
paths=[WORK/'reports/zicai-curio-client-v1-baseline-2547edc9.json',WORK/'reports/zicai-curio-client-v2-baseline-e0f2324a.json',WORK/'reports/zicai-curio-client-v3-candidate-5e0d3262.json',WORK/'reports/zicai-curio-client-v4-candidate-09812b83.json',WORK/'reports/zicai-curio-client-v5-control-e3d249c2.json',arguments.valid_lifecycle_report.resolve(),arguments.reload_report.resolve(),arguments.screen_diagnostic_report.resolve()]
reports=[]
for index,path in enumerate(paths):
    report=load(path)
    terminal=report['terminal']
    assert terminal['exitCode']==0 and terminal['clientProcessReaped'] and terminal['inputHashesUnchanged'],path
    if index<6 or report['probe']['failures']==0:assert terminal['integratedWorldCompleteSave'],path
    for name,expected in report['inputHashes'].items():assert sha(name)==expected,name
    reports.append(report)
assert reports[0]['probe']['failures']==1
assert reports[1]['probe']['failures']==0 and reports[1]['probe']['baselineOriginalZicaiSilenceGapsDemonstrated']
assert reports[2]['probe']['failures']==0 and reports[2]['probe']['candidateZicaiSilenceBasicContractAccepted']
assert reports[3]['probe']['failures']==1 and 'NativeKeepInventoryRespawnPreservesRestoredCurioExactlyOnce' in reports[3]['probe']['failure']
assert reports[4]['probe']['unchangedStage27InvalidSlotControlCompleted']
assert reports[5]['probe']['failures']==0 and reports[5]['probe']['candidateZicaiSilenceLifecyclePreLogoutAccepted'] and reports[5]['probe']['originalSlotValidatorAccepted']
assert reports[6]['probe']['failures']==1 and reports[6]['probe']['failure']=='java.lang.AssertionError: BoundedNativeReloadClientTicks'
assert reports[7]['probe']['publicScreenStateReadOnly'] and not reports[7]['probe']['uiButtonsOrWarningsAutomaticallyAccepted']
if reports[7]['probe']['failures']:
    assert reports[7]['probe']['failures']==1 and 'PublicWorldOpenFlowAwaitingActualScreen:' in reports[7]['probe']['failure'] and reports[7]['probe']['screenObservations']
reload_accepted=reports[7]['probe']['failures']==0 and reports[7]['probe']['newJvmLogoutLoginPersistenceAccepted']
supplement_path=paths[5].with_name(paths[5].stem+'-lifecycle-supplement.json')
supplement=load(supplement_path)
assert supplement['scopedActualDeathRespawnAndForcedLogoutAccepted'] and supplement['logout']['matchingCurioAndInventoryItemCount']==1
release=WORK/'releases/boss-core-stage28-zicai-curio-silence-v3-20261113'
release.mkdir()
shutil.copyfile(built['jar'],release/Path(built['jar']).name)
assert sha(release/Path(built['jar']).name)==built['jarSha256']
acceptance={'candidateJarSha256':built['jarSha256'],'baseStage27Sha256':built['baseJarSha256'],'addedClasses':built['addedEntries'],'existingClassesByteIdentical':built['existingClassesByteIdentical'],'existingResourcesByteIdentical':True,'reportHashes':{path.relative_to(ROOT).as_posix():sha(path) for path in paths+[supplement_path]},'checksByVersion':{str(index+1):len(report['probe']['checks']) for index,report in enumerate(reports)},'scopedBasicContractAndValidDeathRespawnLogoutAccepted':True,'newJvmPersistenceAccepted':reload_accepted,'failedReloadV7Retained':True,'publicScreenDiagnosticRetained':True,'invalidHeadFixtureBaselineDuplicationKnownAndUnchanged':True,'failedFixturesRetainedAndNeverSeeded':True,'strictGlobalCountExactlyOneForValidLifecycle':True,'remaining':([] if reload_accepted else ['actual new-JVM native world load/login beyond observed public screen gate'])+['actual slot-removal andoccupied-slot inventory fallback','full original eligibility andowner/projectile resolution matrix','actual player equip-action UI integration','authenticated multiplayer,reward andfull AI/battle acceptance'],'fullOriginalZicaiLifecycleMatrixAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'formalInstancesReadOnly':True,'weaponsPaused':True,'largeArtifactsRemoteRecoveryVerified':False,'localOriginalsDeleted':0}
save(release/'scoped-acceptance.json',acceptance)
files={Path(__file__),built_path,Path(built['baseJar']),supplement_path,ROOT/'reference/forge-current.jar',WORK/'tools/restore_boss_zicai_curio_archive_v1.py',WORK/'tools/finalize_boss_stage14_resources_v4.py',WORK/'ZICAI_CURIO_ARCHIVE_RESTORE.md'}
for path,report in zip(paths,reports):
    files.add(path)
    files.update(item for item in Path(report['attempt']).rglob('*') if item.is_file())
for folder in WORK.joinpath('build').iterdir():
    if folder.is_dir() and (folder.name.startswith('boss-zicai-curio-') or folder.name=='curios-death-clone-primary-audit-v1-20261113'):
        files.update(item for item in folder.rglob('*') if item.is_file())
for folder in WORK.joinpath('reports').iterdir():
    if folder.is_dir() and folder.name.startswith('zicai-curio-'):
        files.update(item for item in folder.rglob('*') if item.is_file())
files.update(WORK.joinpath('tools').glob('*zicai*.py'))
files.add(WORK/'tests/ZicaiCurioSilenceComposerV1.java')
for folder in WORK.joinpath('tests').glob('zicai-curio-client-v*'):
    files.update(folder.glob('*.java'))
files.update(path for path in release.rglob('*') if path.is_file())
files.update(path for path in (WORK/'releases/boss-core-stage28-zicai-curio-silence-20261113').rglob('*') if path.is_file())
assert all(path.is_relative_to(ROOT) for path in files)
archive_path=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip'
manifest={};unique={}
for path in sorted(files):
    digest=sha(path);size=path.stat().st_size
    manifest[path.relative_to(ROOT).as_posix()]={'sha256':digest,'bytes':size}
    assert digest not in unique or unique[digest].stat().st_size==size
    unique.setdefault(digest,path)
assert shutil.disk_usage(ROOT).free>sum(path.stat().st_size for path in unique.values())+512*1024*1024,'Retain originals;insufficient safe archive space'
encoded=json.dumps({'format':'ftboss-content-addressed-evidence-v1','scope':'Local scoped stage28 evidence;all original files retained;not an installable product orfull SDK recovery','files':manifest},ensure_ascii=False,indent=2).encode('utf-8')
with zipfile.ZipFile(archive_path,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
    for digest,path in unique.items():bundle.write(path,'blobs/'+digest)
with zipfile.ZipFile(archive_path) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
    for digest,path in unique.items():
        checksum=hashlib.sha256();size=0
        with bundle.open('blobs/'+digest) as stream:
            while chunk:=stream.read(1024*1024):checksum.update(chunk);size+=len(chunk)
        assert checksum.hexdigest()==digest and size==path.stat().st_size
for path in files:assert sha(path)==manifest[path.relative_to(ROOT).as_posix()]['sha256'],path
receipt={'archive':str(archive_path),'archiveSha256':sha(archive_path),'files':len(manifest),'uniqueContentBlobs':len(unique),'allFilesReadBackAndHashVerified':True,'format':'ftboss-content-addressed-evidence-v1','restoreTool':'work/session-boss/tools/restore_boss_zicai_curio_archive_v1.py','failures':0,'installable':False,'deployed':False,'formalInstancesReadOnly':True,'weaponsPaused':True,'fullBossMigrationComplete':False,'largeArtifactsRemoteRecoveryVerified':False,'localOriginalsDeleted':0,'sourceOnlyGithubPersistenceSeparate':True}
save(release/'archive-receipt.json',receipt)
assert sha(formal)=='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0'
assert sha(forge)=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
namespace['processes']()
print(json.dumps({'archive':receipt,'release':str(release)},ensure_ascii=False),flush=True)
