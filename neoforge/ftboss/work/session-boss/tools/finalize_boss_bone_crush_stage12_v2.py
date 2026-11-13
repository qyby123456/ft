import argparse
import datetime
import difflib
import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
BUILD=WORK/'build/boss-bone-crush-v1'
PATCH='C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def patch_file(path,before,after):
    lines=list(difflib.unified_diff(before.splitlines(),after.splitlines(),n=3))[2:]
    patch='*** Begin Patch\n*** Update File: '+path.relative_to(ROOT).as_posix()+'\n'+''.join('@@\n' if line.startswith('@@') else line+'\n' for line in lines)+'*** End Patch\n'
    result=subprocess.run([PATCH,'--codex-run-as-apply-patch',patch],cwd=ROOT,capture_output=True,text=True)
    assert result.returncode==0,result.stderr
    assert path.read_text(encoding='utf-8')==after

def audit_processes():
    command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease');target=($_.CommandLine -like '*FarmingTales_NeoForge 机械动力*')} } | ConvertTo-Json -Compress"
    rows=json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',command],encoding='utf-8-sig'))
    rows=rows if isinstance(rows,list) else [rows]
    rows=[row for row in rows if row['pid']!=os.getpid()]
    assert not any(row['owned'] or row['target'] for row in rows)
    return {'ownedJavaPythonRemaining':0,'targetInstanceJavaRemaining':0,'unrelatedProcessesPreserved':rows,'rawCommandLinesRecorded':False}

def archive(path,files):
    manifest={}
    with zipfile.ZipFile(path,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
        for item in sorted(set(files)):
            assert item.is_file() and item.resolve().is_relative_to(ROOT) and not item.is_symlink(),str(item)
            name=item.relative_to(ROOT).as_posix();manifest[name]={'bytes':item.stat().st_size,'sha256':digest(item)}
            bundle.write(item,name)
        encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
    with zipfile.ZipFile(path) as bundle:
        assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
        for name,row in manifest.items():
            data=bundle.read(name);assert len(data)==row['bytes'] and hashlib.sha256(data).hexdigest()==row['sha256']
    return {'archive':str(path),'archiveSha256':digest(path),'files':len(manifest),'allFilesReadBackAndHashVerified':True,'failures':0}

parser=argparse.ArgumentParser()
parser.add_argument('candidate_report',type=Path)
args=parser.parse_args()
baseline_path=WORK/'reports/bone-crush-v2-baseline-14881531.json'
candidate_path=args.candidate_report.resolve()
baseline=load(baseline_path);candidate=load(candidate_path)
for result in (baseline,candidate):
    terminal=result['terminal']
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and result['probe']['completedScenarios']==7
    assert not result['authenticatedMultiplayerAccepted'] and not result['fullBossAcceptance'] and not result['probe']['authorityWrittenByReflection']
    for name,wanted in result['inputHashes'].items():assert digest(name)==wanted,name
assert baseline['baselineGapObserved'] and not baseline['probe']['nativeBloodDamageEvents']
assert candidate['boneCrushFeatureAccepted'] and candidate['probe']['boneCrushFeatureAccepted']
rows=candidate['probe']['rows'];positive=rows[0]
assert len(positive['castStarts'])==len(positive['castEnds'])==2
assert positive['castStarts'][0]-positive['firstAiTick']==418 and positive['castStarts'][1]-positive['castStarts'][0]==452
assert all(end-start==34 for start,end in zip(positive['castStarts'],positive['castEnds']))
for key,delay in [('brandStarts',80),('tideStarts',200),('sweepStarts',314)]:
    assert len(positive[key])==2 and positive[key][0]-positive['firstAiTick']==delay and positive[key][1]-positive[key][0]==452
hits=candidate['probe']['nativeBloodDamageEvents'];incoming=candidate['probe']['nativeBloodIncomingEvents']
assert len(hits)==len(incoming)==5
assert sorted(row['targetIndex'] for row in hits if row['gameTime']<positive['castStarts'][1])==[1,2,5]
assert sorted(row['targetIndex'] for row in hits if row['gameTime']>=positive['castStarts'][1])==[1,2]
assert all(row['scenario']==0 and row['actualDamage']>0 and row['actualSourceIsButcher'] for row in hits)
assert all(abs(row['originalIncomingAmount']-28.0*(row['liveAttackAttribute']/5.5)*2.0)<0.0001 for row in incoming)
assert incoming[4]['liveAttackAttribute']>incoming[0]['liveAttackAttribute']
for row in hits:
    start=positive['castStarts'][0] if row['gameTime']<positive['castStarts'][1] else positive['castStarts'][1]
    assert row['gameTime']-start==20
knocks=[row for row in candidate['probe']['nativeBoneCrushKnockbackEvents'] if row['scenario']==0 and abs(row['strength']-0.45)<0.00001]
assert len(knocks)==5 and all(abs(row['ratioX']-row['expectedRatioX'])<0.00001 and abs(row['ratioZ']-row['expectedRatioZ'])<0.00001 for row in knocks)
sounds=[row for row in candidate['probe']['nativeBoneCrushSoundEvents'] if row['scenario']==0]
assert len(sounds)==2 and all(row['sound']=='minecraft:entity.player.attack.strong' and abs(row['pitch']-0.65)<0.00001 and row['volume']==1.0 for row in sounds)
for index in (1,2,3,4,6):assert not rows[index]['castStarts']
assert len(rows[5]['castStarts'])==len(rows[5]['castEnds'])==1 and rows[5]['castEnds'][0]-rows[5]['castStarts'][0]==10
built=load(BUILD/'candidate-build-receipt.json')
for name,wanted in built['inputHashes'].items():assert digest(name)==wanted,name
assert 'failures=0;methodsBasicVerified=8;unrelatedMethodsPreserved=7;' in built['proof'] and 'originalSchedulerInstructionsPreserved=true' in built['proof']
jar=Path(built['jar']);assert digest(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as changed:
    assert base.namelist()==changed.namelist() and changed.testzip() is None
    assert sorted(name for name in base.namelist() if base.read(name)!=changed.read(name))==built['changedEntries']==['net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.class']
    assert 'net/farmingtales/ftboss/item/FoodWeaponFinalDamage.class' not in changed.namelist()
old_source=WORK/'proposal-stage11-cleaver-sweep/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
new_source=WORK/'proposal-stage12-bone-crush/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
assert new_source.read_text(encoding='utf-8').replace(',ButcherSkillRules.Skill.BONE_CRUSH','')==old_source.read_text(encoding='utf-8')
previous_path=WORK/'releases/boss-core-stage11-cleaver-sweep-20261112/deployment-receipt.json'
previous=load(previous_path);previous_archive=load(previous_path.parent/'archive-receipt.json')
assert digest(previous_archive['archive'])==previous_archive['archiveSha256']
paused_path=WORK/'archives/weapons-paused-613d4f6f-boss-priority-v2-20261112/archive-receipt.json'
paused=load(paused_path)
assert paused['failures']==0 and paused['weaponDevelopmentPaused'] and not paused['weaponStandaloneJarAccepted']
assert digest(paused['archive'])==paused['archiveSha256'] and digest(paused['snapshotJar'])==paused['snapshotJarSha256']
target=Path(previous['target']).resolve();assert digest(target)==previous['deployedSha256']==built['baseJarSha256']
assert target==Path(paused['formalInstancesUnchanged']['neoforge']['path']).resolve()
forge=Path(paused['formalInstancesUnchanged']['forge']['path']);forge_before=digest(forge)
assert forge_before==paused['formalInstancesUnchanged']['forge']['sha256']
audit=audit_processes();assert not list(target.parent.glob(target.name+'.stage*.tmp'))
release=WORK/'releases/boss-core-stage12-bone-crush-20261112';release.mkdir()
save(release/'predeployment-process-audit.json',audit)
backup=release/'rollback-stage11-qys-bosses-1.21.1.jar';shutil.copyfile(target,backup);assert digest(backup)==built['baseJarSha256']
released=release/'ftboss-boss-core-1.21.1-stage12.jar';shutil.copyfile(jar,released);assert digest(released)==built['jarSha256']
feature={'feature':'stage12 original Bone Crush server skill only','baselineReceipt':str(baseline_path),'candidateReceipt':str(candidate_path),'baselineCases':len(baseline['probe']['cases']),'candidateCases':len(candidate['probe']['cases']),'scenariosPerRuntime':7,'nativeServerTickCallbacks':candidate['probe']['nativeServerTickCallbacks'],'nativeDamageEvents':hits,'nativeIncomingEvents':incoming,'originalKnockbackEvents':knocks,'originalSoundEvents':sounds,'completedCasts':2,'canceledCasts':1,'windup':20,'recovery':14,'perSkillCooldown':200,'globalCooldown':80,'castRange':5.0,'radius':3.0,'circularBehindAndSideHitsVerified':True,'verticalHalfRange':2.5,'baseDamage':28.0,'soundPitch':0.65,'successfulHitKnockback':0.45,'firstBoneCrushDelay':418,'directedFourSkillInterval':452,'existingThreeSkillOrderTimingAndAccountingAccepted':True,'fixtureV1FailurePreserved':True,'productJarUnchangedForFixtureCorrection':True,'relatedMethodsBasicVerified':8,'existingMethodsPreserved':7,'onlyChangedClassInitializerChoiceArray':True,'allOtherClassAndResourceBytesPreserved':True,'stage11EvidenceReusedWithoutRerun':True,'weaponDevelopmentPaused':True,'fullFourSkillRotationAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'clientRenderingAccepted':False,'completeBattleAccepted':False,'fullBossInstallable':False,'fullBossMigrationComplete':False}
save(release/'feature-verification.json',feature)
save(release/'external-dependencies.json',{'stage11Deployment':str(previous_path),'stage11DeploymentSha256':digest(previous_path),'stage11Archive':previous_archive,'weaponPauseReceipt':str(paused_path),'weaponPauseReceiptSha256':digest(paused_path),'weaponArchiveSha256':paused['archiveSha256'],'runtimeInputHashes':{'baseline':baseline['inputHashes'],'candidate':candidate['inputHashes']},'historicalDReadOnly':True,'fullAcceptancePending':True})
(release/'RESTORE.md').write_text('# Stage12 Bone Crush restore\n\nVerify every ARCHIVE_MANIFEST member hash and archive receipt before restoration into a new empty directory. External compiler/runtime libraries remain at their frozen paths in external-dependencies.json. Prior stage11 acceptance is reused without rerunning it.\n\nDeployed change adds only the original fourth Bone Crush choice. Original windup20, recovery14, cooldown200, global80, castRange5, radius3, damage28 with original scaling, native melee source, pitch0.65 and successful-hit knockback0.45 remain unchanged. Shared methods and all other classes/resources are identical. Tests use registered NPCs and actual server callbacks; there are no authenticated players.\n\nRollback requires the correct NeoForge instance stopped, explicit authorization, and exact deployment-receipt and rollback-stage11 hash checks. Forge and historical D are read-only. Weapons remain paused; the independent recovery JAR is not installable. Stage03 authenticated reward drops, summon FX, real client motion/GL, authenticated multiplayer and complete battles remain pending. Do not claim complete Boss migration.\n',encoding='utf-8')
intent={'target':str(target),'oldSha256':digest(target),'newSha256':digest(released),'backup':str(backup),'backupSha256':digest(backup),'userAuthorizedVerifiedBossIncrement':True,'formalInstanceLaunched':False};save(release/'deployment-intent.json',intent)
temporary=target.with_name(target.name+'.stage12.tmp');assert not temporary.exists()
shutil.copyfile(released,temporary);assert digest(temporary)==built['jarSha256'] and digest(target)==built['baseJarSha256']
os.replace(temporary,target);assert digest(target)==built['jarSha256'] and digest(forge)==forge_before
save(release/'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':digest(target),'backup':str(backup),'backupSha256':digest(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'formalInstanceLaunched':False,'boneCrushEnabled':True,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'stage03MaterialDropCorrectionStillPending':True,'forgeSha256Unchanged':forge_before,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'authenticatedRewardsAccepted':False,'completeBattleAccepted':False,'fullFourSkillRotationAccepted':False})
files={Path(__file__),ROOT/'AGENTS.md',new_source,old_source,baseline_path,candidate_path,paused_path,previous_path,previous_path.parent/'feature-verification.json',previous_path.parent/'archive-receipt.json',previous_path.parent/'closeout-archive-receipt.json',WORK/'tests/BoneCrushBoundaryProof.java',WORK/'tests/bone-crush-v2/BossBoneCrushRuntimeProbeV2.java',WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java'}
for folder in [BUILD,WORK/'build/boss-bone-crush-audit-20261112',release]:files.update(path for path in folder.rglob('*') if path.is_file())
files.update(path for path in (WORK/'tools').glob('*bone_crush*') if path.is_file())
files.add(WORK/'tests/bone-crush-v1/BossBoneCrushRuntimeProbe.java')
files.add(WORK/'reports/bone-crush-candidate-fb36f8ba.json')
files.add(WORK/'reports/bone-crush-baseline-f3fca1ed.json')
failed_attempt=WORK/'build/bone-crush-candidate-fb36f8ba'
files.update(path for path in failed_attempt.iterdir() if path.is_file())
files.add(failed_attempt/'server/actual-bone-crush-probe.json')
for result in (baseline,candidate):
    attempt=Path(result['attempt']);files.update(path for path in attempt.iterdir() if path.is_file())
    for relative in ['observer-classes','observer-resources','server/world','server/logs']:
        files.update(path for path in (attempt/relative).rglob('*') if path.is_file())
    files.add(attempt/'server/actual-bone-crush-probe-v2.json')
archived=archive(WORK/'archives/boss-core-stage12-bone-crush-20261112.zip',files)
archived.update({'deployedIncrementSha256':digest(target),'backupSha256':digest(backup)});save(release/'archive-receipt.json',archived)
for name in ['WRITE_OWNER.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');data=json.loads(before)
    data.update({'activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'activeWorkstream':'boss-core-stage12-bone-crush-closed','weaponFunctionalDevelopment':'paused_by_latest_direct_user','weaponValidationPaused':True,'latestBossDeploymentReceipt':str(release/'deployment-receipt.json'),'latestBossArchiveReceipt':str(release/'archive-receipt.json'),'fullBossMigrationComplete':False,'nextBossScope':'Compare original summon FX/native ingress or remaining combat gap; no weapon resumption'})
    if 'CURRENT_BOSS' in name:data.update({'status':'stage12_verified_deployed_archived','stage12BaselineReceipt':str(baseline_path),'stage12CandidateReceipt':str(candidate_path),'stage12BuildReceipt':str(BUILD/'candidate-build-receipt.json'),'deployedSha256':digest(target),'next':'Original summon FX and remaining combat/client/authenticated reward acceptance; weapons paused'})
    patch_file(path,before,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
progress=WORK/'DEPLOYED_PROGRESS.md';before=progress.read_text(encoding='utf-8')
after=before+'\n\n## 2026-11-12 Stage12: original Bone Crush\n\nRestored only the fourth original skill choice. Baseline and candidate each completed seven directed native scenarios with '+str(feature['nativeServerTickCallbacks'])+' server callbacks and normal zero-exit complete saves. Candidate passes '+str(feature['candidateCases'])+' checks, two complete casts, one target-loss cancellation and five actual native melee hits. Original 20+14 timing, cooldown200, global80, castRange5, circular radius3, vertical2.5, damage28 with original attribute/phase scaling, sound0.65 and knockback0.45 are verified. Earlier three skills retain their exact first delays and the directed four-skill interval is452. Eight methods pass ASM; seven existing method bodies and all other class/resource bytes remain unchanged.\n\nDeployed SHA256 `'+digest(target)+'`; exact stage11 rollback verified. Archive SHA256 `'+archived['archiveSha256']+'`, '+str(archived['files'])+' files read back and hash verified. Weapon development/validation remains paused; latest separate recovery JAR and complete compressed evidence are under weapons-paused-613d4f6f-boss-priority-v2-20261112. It is not an installable weapon addon. Forge and historical D unchanged. Full rotation/fight parity, summon FX, client/GL and authenticated multiplayer/rewards remain pending.\n'
patch_file(progress,before,after)
remaining=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md';before=remaining.read_text(encoding='utf-8')
after='# Boss remaining checkpoint after stage12\n\nBone Crush is now restored as the original fourth skill choice and is verified/deployed/archived. Only the controller class initializer changes. Stage11 evidence is frozen and reused. Two directed cycles, circular rear/side hits, radius/vertical exclusions and target-loss cancellation pass; this does not establish full combat or client/authenticated acceptance.\n\nNext compare original summon FX and genuine native ingress before changing that single gap. Full rotation parity, phase chase/hit balance, client motion/rendering/GL, authenticated multiplayer and complete battles remain pending. Stage03 basic material/reward correction remains archived and undeployed pending an authenticated positive kill. Preserve original Boss rewards and base item definitions independently of paused weapon growth.\n\nWeapon functional development and validation are paused by the latest direct user. The latest compressed checkpoint and separate noninstallable recovery JAR are in archives/weapons-paused-613d4f6f-boss-priority-v2-20261112. Do not resume signed-Session work without a new explicit request. Same sole C writer; Forge and historical D read-only. Verify, back up, deploy, then archive each accepted Boss segment.\n'
patch_file(remaining,before,after)
final_processes=audit_processes();save(release/'terminal-audit.json',{'baselineTerminal':baseline['terminal'],'candidateTerminal':candidate['terminal'],'processes':final_processes,'targetTemporaryAbsent':not temporary.exists(),'failedPreparationRetained':True,'fullAcceptance':False})
closed=archive(WORK/'archives/boss-core-stage12-closeout-20261112.zip',[ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',progress,remaining,release/'deployment-receipt.json',release/'archive-receipt.json',release/'feature-verification.json',release/'terminal-audit.json',release/'RESTORE.md',paused_path,Path(__file__)])
save(release/'closeout-archive-receipt.json',closed)
assert digest(target)==built['jarSha256'] and digest(backup)==built['baseJarSha256'] and digest(forge)==forge_before and not temporary.exists()
print(json.dumps({'stage12Deployed':True,'deployedSha256':digest(target),'candidateCases':feature['candidateCases'],'archiveFiles':archived['files'],'archiveSha256':archived['archiveSha256'],'closeoutArchiveSha256':closed['archiveSha256'],'weaponsPaused':True,'ownedJavaPythonRemaining':0,'fullBossMigrationComplete':False}),flush=True)
