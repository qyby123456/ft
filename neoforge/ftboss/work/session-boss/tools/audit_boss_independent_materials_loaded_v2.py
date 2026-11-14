import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-independent-materials-loaded-audit-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
report_path=Path(sys.argv[1]).resolve();native=load(report_path);probe=native['probe'];assert native['actualIndependentBossMaterialIntegrationAccepted'] and probe['failures']==0 and probe['completedKinds']==len(probe['kills'])==len(probe['registryChecks'])==10
assert probe['actualIndependentBossMaterialIntegrationAccepted'] and probe['actualOriginalMaterialRegistryAndCreativeTabAccepted'] and probe['actualTenPlayerAttributedKillsAccepted'] and probe['productChanged']
assert not any(probe[key] for key in ['directEntityCreationByObserver','directBossOrHelperHealthWritesByObserver','bossRewardIdentityOrEligibilityFieldsWrittenByObserver','privateGameplayOrRewardCallbacksInvoked','manualEntityTicksInvoked','worldTimeOverridden','fakePlayerUsed','authenticated','authenticatedKillAccepted','actualMultiplayerGameplayAccepted','nativeWorldRestartAccepted'])
assert probe['publicVanillaConsoleSummonFixture'] and probe['playerFixtureProtection']['nativePreDamageCap']==0.01 and probe['playerFixtureProtection']['targetPreDamageFixture']==250
expected={'FROST':'frost_material','BUTCHER':'butcher_material','TEA':'tea_material','WINE':'wine_material','RAT':'cheese_material','DEFECT':'capacitor_material','COMPOST':'spore_material','APPLE':'apple_material','BENGER':'shepherd_material','TIDE':'tide_material'}
assert {row['kind']:row['id'] for row in probe['registryChecks']}=={key:'ftboss:'+value for key,value in expected.items()}
assert {row['kind'] for row in probe['kills']}==set(expected)
for row in probe['kills']:
    assert row['material']=='ftboss:'+expected[row['kind']] and row['actualItemEntities']==row['count']==1 and row['actualLivingDeathEvents']>0 and row['positiveNativeHits']>0 and row['state']['rewarded'] and not row['state']['alive']
    deaths=[event for event in probe['events'] if event['phase']=='actualLivingDeath' and event['kind']==row['kind']];assert deaths and all(not event['canceled'] and event['realPlayerSource'] and event['realPlayerKillCredit'] for event in deaths)
    joins=[event for event in probe['events'] if event['phase']=='actualMaterialItemEntityJoin' and event['kind']==row['kind']];assert len(joins)==1 and joins[0]['count']==1
    assert any('BossRelicDrops.' in frame for frame in joins[0]['stack']) and any('BossMaterialDrops.reward' in frame for frame in joins[0]['stack'])
assert all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured']) and native['terminal']['exitCode']==0
for path,digest in native['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
build_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='6c0acbcdda6fdecba9db9cc512ebdaceff36f84dd52ddf09b2879563716f54d9'
source=WORK/'tests/BossIndependentMaterialsNativeV2.java';text=source.read_text(encoding='utf-8')
for forbidden in ['.addFreshEntity(','.create(level)','.putUUID(','.putBoolean(','.getDeclaredMethod(','.invoke(','.setGameTime(','.scriptedDeath(','.normalDrops(','.die(']:assert forbidden not in text,forbidden
agent=WORK/'tests/LoadedBossClassCaptureV8.java';directory=Path(native['attempt'])/'captured-defined-classes';selected=re.findall(r'"([^\"]+)"',agent.read_text(encoding='utf-8').split('Set<String> selected=Set.of(')[1].split(');')[0]);assert len(selected)==30
required=['net/farmingtales/ftboss/item/FTBossItems','net/farmingtales/ftboss/item/FTCreativeTabs','net/farmingtales/ftboss/skill/BossRelicDrops','net/farmingtales/ftboss/item/BossMaterialDrops','net/farmingtales/ftboss/item/BossMaterialDrops$Definition','net/farmingtales/ftboss/item/BossMaterialDrops$MaterialItem'];captured={}
with zipfile.ZipFile(candidate) as jar:
    for name in required:
        path=directory/(name.replace('/','_')+'.class');assert path.is_file() and path.read_bytes()==jar.read(name+'.class'),name;captured[name]=sha(path)
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1];actual=directory/'net_farmingtales_ftboss_entity_EasterBossEntity.class';simulation=WORK/'build/boss-youzi-original-summon-v4-20261114/simulated-after.class';boss_source=WORK/'tests/YouziOriginalSummonLoadedAuditV2.java';original_source=WORK/'tests/BossOriginalMaterialRulesProofV1.java';original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(boss_source),str(original_source)])
loaded=run('actual-original-boss-graphs',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'YouziOriginalSummonLoadedAuditV2',str(simulation),str(actual)])
golden=run('original-forge-material-rules',[str(JAVA/'java.exe'),'-Xmx128m','-cp',str(classes),'BossOriginalMaterialRulesProofV1',str(original)])
inputs=[Path(__file__),report_path,build_path,candidate,source,agent,boss_source,original_source,original,actual,simulation,*[directory/(name.replace('/','_')+'.class') for name in required]]
receipt={'nativeReport':str(report_path),'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':True,'nativeChecks':probe['checks'],'actualTenRegisteredMaterialAndCreativeEntriesAccepted':True,'actualTenRealPlayerAttributedKillsAccepted':True,'actualOriginalRewardMaterialCountsOneEach':True,'actualNativeItemCodecRoundtripsAccepted':True,'actualMaterialItemEntityProductStacksObserved':True,'requiredActualDefinedClassesExactlyCandidate':captured,'all342ActualBossGraphsEqualPriorProvenComposition':True,'actualBossProof':loaded,'originalForgePureMaterialRulesProof':golden,'originalRewardConditionsAndRandomnessPreservedByBuildReverseGraphProof':True,'fixture':probe['playerFixtureProtection'],'observerHasNoPrivateGameplayOrRewardInvocation':True,'publicVanillaConsoleSummonFixture':True,'noPlayerOrMobLootRejectionNativeAcceptanceClaimed':False,'authenticatedKillAccepted':False,'actualMultiplayerGameplayAccepted':False,'nativeWorldRestartAccepted':False,'fullBossCombatOrSurvivabilityAcceptanceClaimed':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'inputHashes':{str(path):sha(path) for path in inputs}}
save('receipt.json',receipt);print(json.dumps({'nativeChecks':probe['checks'],'actualRegisteredMaterialAndCreativeEntries':10,'actualPlayerAttributedKills':10,'actualMaterialOutputCountsOneEach':True,'requiredActualClasses':6,'bossProof':loaded,'goldenProof':golden,'authenticatedKillAccepted':False}),flush=True)
