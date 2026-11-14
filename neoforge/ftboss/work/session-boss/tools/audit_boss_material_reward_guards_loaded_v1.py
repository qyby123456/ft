import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-material-reward-guards-loaded-audit-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
report_path=Path(sys.argv[1]).resolve();native=load(report_path);probe=native['probe'];assert native['actualBossMaterialRewardGuardsAccepted'] and probe['actualBossMaterialRewardGuardsAccepted'] and probe['failures']==0 and probe['completedCases']==len(probe['cases'])==10 and not native['productChanged']
expected_modes=['NO_PLAYER','MOB_LOOT_OFF','PLAYER_CREDIT_FALLBACK','CANCELED_DEATH','CANCELED_DROPS']
assert {(row['mode'],row['kind']) for row in probe['cases']}=={(mode,kind) for mode in expected_modes for kind in ['FROST','WINE']}
for row in probe['cases']:
    index=row['case'];mode=row['mode'];kind=row['kind'];assert index//2==expected_modes.index(mode) and index%2==['FROST','WINE'].index(kind)
    events=[event for event in probe['events'] if event['case']==index];deaths=[event for event in events if event['phase']=='actualLivingDeath'];drops=[event for event in events if event['phase']=='actualLivingDrops'];joins=[event for event in events if event['phase']=='actualMaterialJoin'];actions=[event for event in probe['actions'] if event['case']==index]
    expected=1 if mode=='PLAYER_CREDIT_FALLBACK' or mode=='CANCELED_DROPS' and kind=='FROST' else 0
    assert row['expectedMaterials']==row['actualMaterials']==len(joins)==expected and row['state']['rewarded']==bool(expected) and row['naturalTicksAfterTerminal']>=40 and len(deaths)==row['deathEvents']>0
    assert any(event['nativePreEvents']>0 for event in actions) and all(event['count']==1 for event in joins)
    if mode=='NO_PLAYER':
        assert row['playerActions']==0 and row['sourceFreeVanillaCommands']>0 and not row['state']['playerKillCredit'];assert all(event['noEntitySource'] and not event['playerSource'] and not event['playerKillCredit'] and not event['canceled'] for event in deaths)
    elif mode=='MOB_LOOT_OFF':
        assert row['playerActions']>0 and row['sourceFreeVanillaCommands']==0 and row['state']['playerKillCredit'];assert all(event['playerSource'] and event['playerKillCredit'] and not event['mobLoot'] and not event['canceled'] for event in deaths)
    elif mode=='PLAYER_CREDIT_FALLBACK':
        assert row['playerActions']==1 and row['sourceFreeVanillaCommands']>0 and row['state']['playerKillCredit'];assert all(event['noEntitySource'] and not event['playerSource'] and event['playerKillCredit'] and event['mobLoot'] and not event['canceled'] for event in deaths);assert actions[0]['actualPlayerAttackEvents']==1 and actions[0]['nativePreEvents']>0 and actions[0]['after']['alive'] and actions[0]['after']['playerKillCredit']
    elif mode=='CANCELED_DEATH':
        assert row['deathCanceled'] and all(event['canceled'] and event['playerSource'] for event in deaths) and any(event['phase']=='actualDeathCancellation' for event in events)
    else:
        assert row['dropsCanceled'] and len(drops)==row['dropsEvents']>0 and all(event['canceled'] and event['playerSource'] for event in drops) and all(not event['canceled'] for event in deaths)
        if kind=='FROST':assert any('BossRelicDrops.scriptedDeath' in frame for frame in joins[0]['productStack']) and any('BossMaterialDrops.reward' in frame for frame in joins[0]['productStack'])
assert not any(probe[key] for key in ['directEntityCreationByObserver','directBossOrHelperHealthWritesByObserver','bossRewardIdentityOrEligibilityFieldsWrittenByObserver','privateGameplayOrRewardCallbacksInvoked','manualEntityTicksInvoked','worldTimeOverridden','fakePlayerUsed','authenticated','authenticatedKillAccepted','actualMultiplayerGameplayAccepted','nativeWorldRestartAccepted','productChanged'])
assert probe['publicVanillaConsoleSummonFixture'] and probe['publicVanillaDamageCommandFixture'] and probe['playerFixtureProtection']['nativePreDamageCap']==0.01 and probe['playerFixtureProtection']['targetPreDamageFixture']==250
assert all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured']) and native['terminal']['exitCode']==0
for path,digest in native['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
build_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='6c0acbcdda6fdecba9db9cc512ebdaceff36f84dd52ddf09b2879563716f54d9'
source=WORK/'tests/BossMaterialRewardGuardsNativeV1.java';text=source.read_text(encoding='utf-8')
for forbidden in ['.addFreshEntity(','.create(level)','.putUUID(','.putBoolean(','.getDeclaredMethod(','.invoke(','.setGameTime(','.scriptedDeath(','.normalDrops(','.die(','.BossMaterialDrops.reward(']:assert forbidden not in text,forbidden
assert text.count('.setHealth(')==text.count('player.setHealth(')==1 and text.count('.setCanceled(true)')==2
prior_path=WORK/'reports/boss-independent-materials-loaded-audit-v5-20261114/receipt.json';prior=load(prior_path);assert prior['candidateJarSha256']==sha(candidate) and prior['actualTenRealPlayerAttributedKillsAccepted']
composition_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/composition-proof.json';composition=load(composition_path);assert composition['originalRewardBranchesRandomnessAndDuplicateGuardPreserved'] and not composition['pausedWeaponServiceModified']
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
inputs.extend([prior_path,composition_path])
receipt={'nativeReport':str(report_path),'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':False,'nativeCases':10,'nativeChecks':probe['checks'],'actualNoPlayerSourceAndNoCreditRejectionAccepted':True,'actualMobLootFalsePlayerKillRejectionAccepted':True,'actualNativePlayerCreditFallbackAccepted':True,'actualDeathCancellationRejectionAccepted':True,'actualDropCancellationOriginalScriptedVersusOrdinaryOrderingAccepted':True,'actualOriginalMaterialCountsAndFortyNaturalTickNoDuplicateAccepted':True,'requiredActualDefinedClassesExactlyCandidate':captured,'all342ActualBossGraphsEqualPriorProvenComposition':True,'actualBossProof':loaded,'originalForgePureMaterialRulesProof':golden,'originalRewardBranchesRandomnessAndDuplicateGuardPreserved':True,'previousTenPositiveKindsReceipt':str(prior_path),'fixture':probe['playerFixtureProtection'],'observerHasNoPrivateGameplayOrRewardInvocation':True,'publicVanillaDamageCommandsSupplyRealNoEntitySource':True,'noNaturalEnvironmentalPhysicsKillAcceptanceClaimed':True,'authenticatedKillAccepted':False,'actualMultiplayerGameplayAccepted':False,'nativeWorldRestartAccepted':False,'fullBossCombatOrSurvivabilityAcceptanceClaimed':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'inputHashes':{str(path):sha(path) for path in inputs}}
save('receipt.json',receipt);print(json.dumps({'nativeCases':10,'nativeChecks':probe['checks'],'noPlayerAndMobLootRejections':True,'nativePlayerCreditFallback':True,'originalCanceledDeathAndDropOrdering':True,'requiredActualClasses':6,'bossProof':loaded,'goldenProof':golden,'productChanged':False}),flush=True)
