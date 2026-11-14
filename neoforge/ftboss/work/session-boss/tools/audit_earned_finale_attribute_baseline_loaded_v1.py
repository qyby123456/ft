import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/earned-finale-attribute-baseline-loaded-audit-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
report_path=Path(sys.argv[1]).resolve();native=load(report_path);probe=native['probe'];assert native['actualEarnedUnlockAndFinaleAttributeBaselineAccepted'] and probe['actualEarnedUnlockAndFinaleAttributeBaselineAccepted'] and probe['actualEligibleFinaleAddValueParityGapObserved'] and probe['failures']==0 and not native['productChanged']
assert all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured']) and native['terminal']['exitCode']==0
for path,digest in native['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
assert not any(probe[key] for key in ['directBossOrHelperHealthWritesByObserver','victoryUnlockOrChallengeIdentityWrittenByObserver','privateGameplayOrRewardCallbacksInvoked','manualEntityTicksInvoked','worldTimeOverridden','directEntityCreationByObserver','fakePlayerUsed','authenticated','authenticatedFullBattleAccepted','actualMultiplayerGameplayAccepted','fullBossCombatOrSurvivabilityAcceptanceClaimed','nativeWorldRestartAccepted','productChanged'])
assert probe['actualPublicChallengeSelectionWithOriginalGate'] and probe['ingress']['originalImpossibleSelectionRejectedBeforeVictory'] and probe['ingress']['owner']==probe['actualPlayerUuid']
victory=probe['actualNormalVictoryEarnedUnlock'];stages=probe['observedPrimaryStages'];assert victory['stageCount']==victory['originalManorCount']==len(stages) and len({row['uuid'] for row in stages})==len(stages)
assert len({row['variant'] for row in stages})==len(stages) and victory['player']==probe['actualPlayerUuid'] and victory['positiveNativeHits']==probe['positiveNativeHits']>0
for row in stages:
    assert row['managed'] and not row['mini'] and row['fightId']==probe['ingress']['fight'] and row['summoner']==probe['actualPlayerUuid'] and row['health']>0
    actions=[action for action in probe['actions'] if action['before']['uuid']==row['uuid']];assert actions and any(action['nativePreEvents']>0 for action in actions) and all(action['actualAttackEvents']==1 for action in actions)
attributes=probe['attributeCases'];assert len(attributes)==4 and [row['case'] for row in attributes]==list(range(4))
for index,row in enumerate(attributes):
    assert row['finale']==(index>=2) and row['preexistingAddValue']==bool(index%2) and row['naturalTicksObserved']>=40
    assert row['matchesOriginalBaseDoubling']==(index!=3) and row['parallelSpells']==(2 if index>=2 else 1) and row['acceleratedTicks99']==(50 if index>=2 else 99)
    assert abs(row['originalBaseDoublingExpectedValue']-(row['base']*(2 if index>=2 else 1)+(5 if index%2 else 0)))<0.001
assert abs(attributes[3]['actualValue']-attributes[3]['originalBaseDoublingExpectedValue']-5)<0.001
source=WORK/'tests/ManorEarnedFinaleAttributeBaselineNativeV1.java';text=source.read_text(encoding='utf-8')
for forbidden in ['.addFreshEntity(','.create(level)','.putUUID(','.putBoolean(','.putString(','.getDeclaredMethod(','.invoke(','.setGameTime(','.recordConqueror(','.award(','.ManorFight.start(','.onCurrentDefeated(','.finishActivation(','.prepare(']:assert forbidden not in text,forbidden
assert text.count('.setHealth(')==text.count('player.setHealth(')==1
assert probe['playerFixtureProtection']['nativePreDamageCap']==0.01 and probe['playerFixtureProtection']['targetPreDamageFixture']==250
build_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='6c0acbcdda6fdecba9db9cc512ebdaceff36f84dd52ddf09b2879563716f54d9'
preflight_path=WORK/'reports/earned-finale-attribute-baseline-original-boundary-preflight-v1-20261114.json';preflight=load(preflight_path);assert preflight['candidateJarSha256']==sha(candidate) and preflight['priorNativeArithmeticChecks']==24
agent=WORK/'tests/LoadedBossClassCaptureV9.java';directory=Path(native['attempt'])/'captured-defined-classes';required=['net/farmingtales/ftboss/'+name for name in ['boss/NonManorFinale','boss/BossChallengePreference','boss/ManorDifficulty','boss/ManorFight','boss/ManorFightData','boss/ManorVictoryData','boss/BossHandler','entity/TeaTreeBossEntity','entity/RatBossEntity']];captured={}
with zipfile.ZipFile(candidate) as jar:
    for name in required:
        path=directory/(name.replace('/','_')+'.class');assert path.is_file() and path.read_bytes()==jar.read(name+'.class'),name;captured[name]=sha(path)
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1];actual=directory/'net_farmingtales_ftboss_entity_EasterBossEntity.class';simulation=WORK/'build/boss-youzi-original-summon-v4-20261114/simulated-after.class';boss_source=WORK/'tests/YouziOriginalSummonLoadedAuditV2.java'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(boss_source)])
loaded=run('actual-original-boss-graphs',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'YouziOriginalSummonLoadedAuditV2',str(simulation),str(actual)])
inputs=[Path(__file__),report_path,build_path,candidate,source,agent,preflight_path,boss_source,actual,simulation,*[directory/(name.replace('/','_')+'.class') for name in required]]
receipt={'nativeReport':str(report_path),'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':False,'nativeChecks':probe['checks'],'actualOriginalNormalManagedStages':len(stages),'actualNormalVictoryEarnedOriginalUnlock':True,'actualOriginalImpossiblePreferenceRejectedBeforeAndAcceptedAfterEarnedVictory':True,'actualEligibleRegularBossAttributeCases':attributes,'actualEligibleFinaleAddValueParityGapObserved':True,'gapScope':'Native normal/victory gated real Rat Boss admission with preexisting ADD_VALUE5; current value exceeds original base-doubling arithmetic by5','originalCurrentArithmeticPreflight':str(preflight_path),'requiredActualDefinedClassesExactlyCandidate':captured,'all342ActualBossGraphsEqualPriorProvenComposition':True,'actualBossProof':loaded,'observerHasNoPrivateGameplayOrVictoryUnlockIdentityWrites':True,'actualFullNormalVictoryFixtureAccepted':True,'fixture':probe['playerFixtureProtection'],'authenticatedFullBattleAccepted':False,'actualMultiplayerGameplayAccepted':False,'fullCombatOrSurvivabilityAcceptanceClaimed':False,'nativeWorldRestartAccepted':False,'curiosReloadAcceptanceClaimed':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'inputHashes':{str(path):sha(path) for path in inputs}}
save('receipt.json',receipt);print(json.dumps({'nativeChecks':probe['checks'],'actualManagedPrimaryStages':len(stages),'actualNormalVictoryEarnedUnlock':True,'actualEligibleFinaleAttributeGapObserved':True,'requiredActualClasses':9,'actualBossProof':loaded,'productChanged':False}),flush=True)
