import hashlib
import json
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage14-client-resources-20261113'
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
helper=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py'
namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],namespace)
patch=namespace['replace_file']
deployment=load(RELEASE/'deployment-receipt.json');archived=load(RELEASE/'archive-receipt.json')
assert sha(Path(deployment['target']))==deployment['deployedSha256']=='6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c'
assert sha(Path(deployment['backup']))==deployment['backupSha256']=='58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'
assert archived['failures']==0 and archived['files']==18164 and sha(Path(archived['archive']))==archived['archiveSha256']
assert load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
for name in ['WRITE_OWNER.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');data=json.loads(before)
    data.update({'currentGoalStatus':'active','activeGoalObjective':'继续完成boss部分部署移植','currentSignedSessionGoalStatus':'paused_by_current_direct_user','weaponValidationPaused':True,'noNewWeaponRuns':True,'activeRuntimePid':None,'activeRuntimePids':[],'bossActiveRuntimePid':None,'activeSupervisor':None,'ownedJavaPythonProcessesRemaining':False,'ownedJavaPythonRemaining':0,'status':'stage14_resources_verified_deployed_archived_full_boss_active','currentBatch':'stage14 complete:314 resources,21 OBJ ports,136 body views,44 statue views and complete native darkness reset;1443 deployed classes preserved','latestFormalDeployment':'stage14 Boss client resources based on stage12;stage13 excluded','latestBossDeploymentReceipt':str(RELEASE/'deployment-receipt.json'),'latestStageDeploymentReceipt':str(RELEASE/'deployment-receipt.json'),'latestBossArchiveReceipt':str(RELEASE/'archive-receipt.json'),'latestStageArchiveSha256':archived['archiveSha256'],'deployedSha256':deployment['deployedSha256'],'stage14DeploymentPerformed':True,'stage14ArchiveFiles':18164,'stage13DeploymentPerformed':False,'stage13IncludedInCurrentDeployment':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'installable':False,'nextBossAction':'Native original Xuanlan per-owner flywheel cap15 baseline,global-level admission and oldest-first pruning; full Boss goal active;weapons paused','nextBossScope':'Xuanlan limits;authenticated entry/rewards,multiplayer,complete battles remain pending'})
    patch(path,before,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
heading='## Latest deployed stage14 resource increment, 2026-11-13\n\n- Deployed resource-only stage14 SHA6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c is based on stage12. Every existing1443 class and resource byte stays identical. Adds314 original Boss resources including21 OBJ namespace ports. Stage13 classes are excluded and undeployed.\n- Actual exact-JAR native evidence:96 Manor body views,40 non-Manor views,44 statue views and21 baked OBJ models. Focused4-view v8 proves complete reset after238 natural expiry ticks; V5 reset green flag is invalidated by actual image review. Archive18164files SHA09eed5d380cc659c0bb5e57b40bc191fa930749e44aed518e1369d4f8bc16886 fully readback/SHA verified. Exact stage12 rollback preserved. All negative fixtures retained.\n- Full Boss goal remains active:authenticated entry/rewards/multiplayer,complete battles and original behavior remain pending. Weapons paused,recovery JAR noninstallable,same sole C writer. Next actual native Xuanlan cap15 baseline before product change;historical D and Forge read-only.\n\n'
path=ROOT/'AGENTS.md';before=path.read_text(encoding='utf-8');assert '## Latest deployed stage14 resource increment' not in before
patch(path,before,before.replace('# NeoForge continuation on C\n','# NeoForge continuation on C\n\n'+heading,1))
path=WORK/'DEPLOYED_PROGRESS.md';before=path.read_text(encoding='utf-8')
text='\n\n## 2026-11-13 Stage14: Boss client resources\n\nOnly314 original assets added to deployed stage12,including21 original OBJ models ported to neoforge:obj. Every existing1443 class and resource byte is preserved;stage13 excluded. Exact-JAR native tests:96 Manor views,40 non-Manor views,44 statue views,21 model bakes,and full native darkness reset after238 natural expiry ticks. Negative fixtures and invalidated V5 reset flag preserved.\n\nDeployed SHA256 `'+deployment['deployedSha256']+'`;stage12 rollback `'+deployment['backupSha256']+'`. Archive18164files,readback/SHA verified:`'+archived['archiveSha256']+'`. Forge/historical D unchanged;weapons paused. Full authenticated entry/rewards,multiplayer,complete battles and original remaining behavior pending;goal active.\n'
patch(path,before,before+text)
path=WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md';before=path.read_text(encoding='utf-8')
text='\n\n## Stage14 current evidence update\n\nResource-only stage14 is verified/deployed/archived,based on stage12 with1443 unchanged classes and314 resource additions. V4 original partial-negative report preserves96 completed Manor views;V5 supplies40 remaining body and4 survival images. V5 reset green flag is invalidated;V8 accepts4 focused survival images and complete natural reset after238 ticks,zero actual effects,original overlay inactive,92160 bright sky and251507 bright world pixels,with screenshot review. StatueV1 accepts44 real world views and21 actual baked OBJ models/textures. Archive18164files SHA09eed5d380cc659c0bb5e57b40bc191fa930749e44aed518e1369d4f8bc16886. Earlier preparation entries are superseded only within this actual resource/body scope. Stage13 excluded and undeployed. Full authenticated/reward/multiplayer/full-battle goal remains active.\n'
patch(path,before,before+text)
save(RELEASE/'closeout-preparation-note.json',{'priorInlinePreparationFailedBeforeAnyMutation':True,'reason':'Nested escaped newline produced unterminated string before source creation; corrected using raw source literal and verified AST','productOrDeploymentAffected':False})
files={Path(__file__),ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',RELEASE/'deployment-receipt.json',RELEASE/'archive-receipt.json',RELEASE/'feature-verification.json',RELEASE/'terminal-audit.json',RELEASE/'RESTORE.md',RELEASE/'closeout-preparation-note.json'}
manifest={path.relative_to(ROOT).as_posix():{'sha256':sha(path),'bytes':path.stat().st_size} for path in sorted(files)}
archive=WORK/'archives/boss-core-stage14-closeout-20261113.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps(manifest,ensure_ascii=False,indent=2))
    for name in manifest:bundle.write(ROOT/name,name)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None
    for name,row in manifest.items():assert hashlib.sha256(bundle.read(name)).hexdigest()==row['sha256']
save(RELEASE/'closeout-archive-receipt.json',{'archive':str(archive),'archiveSha256':sha(archive),'files':len(manifest),'failures':0,'allFilesReadBackAndHashVerified':True,'fullBossGoalStatus':'active','weaponsPaused':True})
print(json.dumps({'stage14Closed':True,'closeoutArchiveSha256':sha(archive),'fullBossGoalStatus':'active','weaponsPaused':True}),flush=True)
