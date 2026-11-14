import hashlib
import json
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/shadow-sigil-actual-loaded-audit-v4-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
report_path=Path(sys.argv[1]).resolve();native=load(report_path);probe=native['probe'];assert native['actualNaturallyEmittedSigilRenderingAccepted'] and probe['actualNaturallyEmittedSigilRenderingAccepted'] and probe['originalGuardedSurrenderCleanupAccepted'] and probe['failures']==0 and native['productChanged']
assert all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured']) and native['terminal']['exitCode']==0
for path,digest in native['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
for key in ['directBossOrHelperHealthWritesByObserver','victoryUnlockOrChallengeIdentityWrittenByObserver','privateGameplayOrRewardCallbacksInvoked','manualEntityTicksInvoked','worldTimeOverridden','directEntityCreationByObserver','fakePlayerUsed','authenticated','authenticatedFullBattleAccepted','actualMultiplayerGameplayAccepted','fullBossCombatOrSurvivabilityAcceptanceClaimed','nativeWorldRestartAccepted','actualNormalVictoryEarnedUnlock','actualEligibleFinaleAttributeBaselineAccepted']:assert not probe[key],key
assert probe['actualFirstVariant']==7 and probe['ingress']['egg']=='ftboss:easter_egg_zheergen';assert probe['phase']==4 and probe['visibleSigilObservedFrames']>=probe['visibleSigilReturnedFrames']>=3 and probe['visualQueueInspectedReadOnly'] and probe['actualRegisteredEggAcquisitions']<=8
assert probe['visiblePillarObservedFrames']>=probe['visiblePillarReturnedFrames']>=0
assert probe['actualVisibleShadowVisualFrames'] and all(row['actualAfterOriginalRenderReturned'] and row['visibleOriginalSigils']>0 and row['glError']==0 for row in probe['actualVisibleShadowVisualFrames'])
source=WORK/'tests/ShadowSigilActualNativeV2.java';text=source.read_text(encoding='utf-8');assert 'ManorFight.canSurrender' in text and 'ManorFight.surrender' in text
for forbidden in ['.addFreshEntity(','.create(level)','.putUUID(','.putBoolean(','.putString(','.getDeclaredMethod(','.invoke(','.setGameTime(','.recordConqueror(','.award(','.ManorFight.start(','.onCurrentDefeated(','.finishActivation(','.prepare(','ShadowSigilVisual.spawn(']:assert forbidden not in text,forbidden
assert text.count('.setHealth(')==text.count('player.setHealth(')==1
composition_path=WORK/'build/boss-shadow-sigil-buffer-v2-20261114/composition-proof.json';composition=load(composition_path);assert composition['changedExistingMethods']==1 and composition['negativeControls']==3 and composition['originalGeometryAlphaLifetimeCullingRenderTypesAndFlushesRetained']
build_path=WORK/'build/boss-shadow-sigil-buffer-v2-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='95e29f9f6986d58d4f55238776088d82e7782b0e0ece5c2d2f72e25f98ba4215'
base=Path(load(WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json')['jar']);assert sha(base)==built['baseJarSha256'];changed='net/farmingtales/ftboss/client/render/ShadowSigilVisual.class'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(candidate) as after:
    assert before.namelist()==after.namelist();differences=[name for name in before.namelist() if before.read(name)!=after.read(name)];assert differences==[changed];resource_count=sum(not name.endswith('.class') for name in before.namelist())
directory=Path(native['attempt'])/'captured-defined-classes';required=['net/farmingtales/ftboss/'+name for name in ['boss/NonManorFinale','boss/BossChallengePreference','boss/ManorDifficulty','boss/ManorFight','boss/ManorFightData','boss/ManorVictoryData','boss/BossHandler','boss/ai/BossScaling','client/render/ShadowSigilVisual','client/render/ShadowSigilVisual$Sigil']];captured={}
with zipfile.ZipFile(candidate) as jar:
    for name in required:
        path=directory/(name.replace('/','_')+'.class');assert path.is_file(),name
        if not name.endswith('/BossScaling'):assert path.read_bytes()==jar.read(name+'.class'),name;captured[name]=sha(path)
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1];actual=directory/'net_farmingtales_ftboss_entity_EasterBossEntity.class';simulation=WORK/'build/boss-youzi-original-summon-v4-20261114/simulated-after.class';boss_source=WORK/'tests/YouziOriginalSummonLoadedAuditV2.java'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(boss_source),str(WORK/'tests/BossScalingActualLoadedAuditV1.java')])
loaded=run('actual-original-boss-graphs',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'YouziOriginalSummonLoadedAuditV2',str(simulation),str(actual)])
scaling_source=WORK/'tests/BossScalingActualLoadedAuditV1.java';scaling_actual=directory/'net_farmingtales_ftboss_boss_ai_BossScaling.class';scaling_proof=run('actual-scaling-existing-installer',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'BossScalingActualLoadedAuditV1',str(candidate),str(scaling_actual)]);assert 'allActualGraphsEqualExistingInstallerSimulation=true' in scaling_proof and 'negativeControls=2' in scaling_proof
inputs=[Path(__file__),report_path,source,build_path,composition_path,candidate,base,boss_source,actual,simulation,scaling_source,scaling_actual,*[directory/(name.replace('/','_')+'.class') for name in required]]
receipt={'nativeReport':str(report_path),'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':True,'nativeChecks':probe['checks'],'actualNaturallyEmittedSigilRenderingAccepted':True,'visibleSigilReturnedFrames':probe['visibleSigilReturnedFrames'],'visiblePillarReturnedFrames':probe['visiblePillarReturnedFrames'],'actualNativePillarRenderObserved':probe['visiblePillarReturnedFrames']>0,'actualVisibleShadowVisualFrames':probe['actualVisibleShadowVisualFrames'],'originalGuardedSurrenderCleanupAccepted':True,'changedEntries':differences,'allOtherEntriesUnchanged':True,'unchangedResourceEntries':resource_count,'requiredActualDefinedClassesExactlyCandidate':captured,'actualScalingAllGraphsExactlyExistingInstallerSimulation':True,'actualScalingExistingAdapterProof':scaling_proof,'actualScalingClassSha256':sha(scaling_actual),'all342ActualBossGraphsEqualPriorProvenComposition':True,'actualBossProof':loaded,'originalSigilGeometryAlphaLifetimeAndTexturesPreserved':True,'observerHasNoPrivateGameplayOrVictoryUnlockIdentityWrites':True,'fixture':probe['playerFixtureProtection'],'actualNormalVictoryEarnedUnlock':False,'actualEligibleFinaleAttributeBaselineAccepted':False,'normalFightDeathCancellationRemovalGapRemains':True,'authenticatedFullBattleAccepted':False,'actualMultiplayerGameplayAccepted':False,'fullCombatOrSurvivabilityAcceptanceClaimed':False,'nativeWorldRestartAccepted':False,'curiosReloadAcceptanceClaimed':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'inputHashes':{str(path):sha(path) for path in inputs}}
save('receipt.json',receipt);print(json.dumps({key:receipt[key] for key in ['nativeChecks','visibleSigilReturnedFrames','visiblePillarReturnedFrames','actualNativePillarRenderObserved','actualBossProof','unchangedResourceEntries','deployed']}),flush=True)
