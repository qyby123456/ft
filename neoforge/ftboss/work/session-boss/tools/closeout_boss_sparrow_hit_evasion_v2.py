import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/sparrow-hit-evasion-v2-closeout-20261114.json'
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def proof_file(path):return {'path':path.relative_to(ROOT).as_posix(),'bytes':path.stat().st_size,'sha256':sha(path)}
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment']
assert owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists()
build=WORK/'build/boss-sparrow-evasion-v2-20261114/candidate-build-receipt.json'
candidate=load(build)
assert candidate['jarSha256']=='877309d1ff2332ab6d407cd1b456d39987109d17e8f6a8e98184abe55703c25b'==sha(Path(candidate['jar']))
assert candidate['baseJarSha256']=='78caa46ef163c4f3ee9cc3d1a90d5ee58e1bbf4d791f213fca390986f987692f'==sha(Path(candidate['baseJar']))
assert candidate['originalRulesByteIdentical'] and candidate['allOtherBaseEntriesByteIdentical']
assert candidate['proof']=='failures=0;originalClasses=3;mappedOriginalMethodGraphs=25;asmVerifiedOriginalMethods=25;pureRulesByteIdentical=true;integrationHooks=3;changedLoadedMethods=2;idempotent=true;negativeControls=1;'
accepted_names=['sparrow-hit-evasion-native-v4-ba1eddca','sparrow-hit-evasion-ally-native-v1-ad0360ee']
accepted=[]
for name in accepted_names:
    path=WORK/'reports'/f'{name}.json'
    report=load(path)
    probe=report['probe']
    terminal=report['terminal']
    assert probe['failures']==0 and not probe['authenticated'] and not probe['fakePlayerUsed']
    assert not probe['privateGameplayCallbacksInvoked'] and not probe['manualEntityTicksInvoked']
    assert terminal['exitCode']==0 and all(terminal[key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured','observerRequestedNormalStop'])
    assert not terminal['missingInputs']
    for name,wanted in report['inputHashes'].items():assert sha(Path(name))==wanted,name
    accepted.append((path,report))
basic=accepted[0][1]
ally=accepted[1][1]
assert basic['actualOriginalSparrowHitEvasionAccepted'] and basic['probe']['checks']==971
assert basic['probe']['activeOwnedPathNaturalTicks']==22
assert all(basic['probe'][key] for key in ['noAiGuardAccepted','naturalExpiryAccepted','actualEntityLeaveCleanupAccepted'])
assert len(basic['probe']['cases'])==3 and [case['stateAccepted'] for case in basic['probe']['cases']]==[True,False,True]
assert all(case['beforeHealth']>case['afterHealth'] for case in basic['probe']['cases'])
assert ally['actualOriginalSparrowAllyEvasionAccepted'] and ally['probe']['checks']==772
assert ally['probe']['originalAllyCoverObserved'] and ally['probe']['allyOwnedPathNaturalTicks']==16 and ally['probe']['controlledOriginalKitingTicks']==16
assert ally['probe']['allyUuid']!=ally['probe']['actualRegisteredSparrowUuid']
assert len(ally['probe']['cases'])==1 and ally['probe']['cases'][0]['stateAccepted']
assert all(report['probe']['remainingStateEntriesAfterFixtures']==0 for path,report in accepted)
audit=WORK/'build/boss-sparrow-evasion-v2-20261114/loaded-class-audit-v1/audit.log'
assert audit.read_text(encoding='utf-8').strip()=='failures=0;checks=351;actualDefinedBossMethodsPreserved=339;changedMethods=hitFeedback,sparrowTick;originalDefenseVerifierPreserved=true;stage29DamageContextUnchanged=true;actualInstallationIdempotent=true;negativeControls=1;'
audited=WORK/'build/sparrow-hit-evasion-native-v2-3b5422b7/captured-defined-classes'
audited_classes=sorted(audited.glob('*.class'))
assert len(audited_classes)==6
bindings=[]
for path,report in accepted:
    captured=Path(report['attempt'])/'captured-defined-classes'
    assert {item.name for item in captured.glob('*.class')}=={item.name for item in audited_classes}
    rows=[]
    for item in audited_classes:
        current=captured/item.name
        identical=current.read_bytes()==item.read_bytes()
        if item.name!='net_minecraft_world_entity_LivingEntity.class':assert identical,current
        rows.append({'class':item.name,'sha256':sha(current),'auditedSha256':sha(item),'byteIdentical':identical})
    bindings.append({'report':proof_file(path),'actualEasterBossAndFourBossHelperDefinitionsByteIdentical':True,'classes':rows})
binding_path=WORK/'reports/sparrow-evasion-accepted-loaded-class-binding-20261114.json'
assert not binding_path.exists()
save(binding_path,{'auditedRuntime':str(audited.parent),'audit':proof_file(audit),'checksReusedOnlyWithinByteIdenticalEasterBossDefinition':351,'livingEntityIsOutsideThisAuditAndItsBytesAreNotIdentical':True,'acceptedRuntimes':bindings,'originalProbeMetadataUnmodified':True})
failures=[]
for name in ['sparrow-hit-evasion-native-v1-64287f18','sparrow-hit-evasion-native-v2-3b5422b7','sparrow-hit-evasion-native-v3-6fe5328f']:
    path=WORK/'reports'/f'{name}.json'
    report=load(path)
    assert not report['sparrowEvasionPortAccepted'] and report['terminal']['clientProcessReaped']
    failures.append(proof_file(path))
rejection=WORK/'reports/sparrow-original-path-safety-rejection-20261114.json'
assert load(rejection)['allSevenCandidatePathsFailOriginalSafetyPredicateAtFirstNode']
dedup=WORK/'reports/closed-sparrow-runtime-jar-dedup-v1-20261114/receipt.json'
local=load(dedup)
assert local['jarPathsPreserved']==84 and local['readonlyAliasesCreated']==82 and local['allFrozenRuntimeInputsUnchanged']
formal=[]
for text,wanted in [
    ('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar','6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0'),
    ('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar','7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3')]:
    actual=sha(Path(text));assert actual==wanted
    formal.append({'path':text,'sha256':actual,'readOnlyUnchanged':True})
closeout={
    'stage':30,'scope':'Original Sparrow confirmed-hit evasion algorithm and three original integration hooks; original ally cover under naturally null same-fight/summoner fixture scope.',
    'candidateBuildReceipt':proof_file(build),'candidateSha256':candidate['jarSha256'],
    'mappedOriginalMethodGraphs':25,'asmVerifiedOriginalMethods':25,'pureRulesByteIdentical':True,
    'basicNativeChecks':971,'basicActualPositiveAttacks':3,'basicOwnedNavigationNaturalTicks':22,
    'original60TickExpiryAccepted':True,'noAiGuardAccepted':True,'actualEntityLeaveCleanupAccepted':True,
    'allyNativeChecks':772,'allyOwnedNavigationNaturalTicks':16,'allyOriginalKitingControlledTicks':16,
    'allyFixtureActualFightAndSummonerIdsEqualWithoutFabrication':True,'authenticatedManagedFightNotClaimed':True,
    'nativeReports':[proof_file(path) for path,report in accepted],
    'loadedDefinitionBinding':proof_file(binding_path),'actualDefinedClassAuditChecks':351,
    'unchangedActualBossMethods':339,'changedActualBossMethods':['hitFeedback','sparrowTick'],
    'stage29DamageContextPreserved':True,'originalDefenseVerifierAndNegativeControlPreserved':True,
    'metadataCorrections':[{'report':accepted[1][0].relative_to(ROOT).as_posix(),'frozenField':'probe.noAllyFixture','frozenValue':True,'correctMeaning':'Inherited label is stale: this run admits an actual distinct registered ally UUID and verifies all original ally guards, same-scope equality, range, owned path and kiting branch. Frozen report and observer are preserved. Broad probe.sparrowEvasionPortAccepted=false is an unupdated template label; scoped actualOriginalSparrowAllyEvasionAccepted=true is the asserted outer acceptance.'}],
    'strictFailedTrialsPreserved':failures,'unchangedOriginalSafetyRejectionCounterexample':proof_file(rejection),
    'allRuntimeAndCandidateInputsRehashedUnchanged':True,'formalInstances':formal,
    'localDedupReceipt':proof_file(dedup),'localDedupReclaimedBytesAtMeasurement':local['freeBytesAfter']-local['freeBytesBefore'],
    'allModelAndMusicResourcesLocalOnly':True,'modelOrMusicUploaded':False,
    'weaponDevelopmentAndValidationPaused':True,'nativeLivingDamagePostContractAccepted':False,
    'authenticated':False,'newJvmAuthenticatedReloadAccepted':False,'fullBossMigrationComplete':False,
    'sparrowEvasionPortScopedAccepted':True,'installable':False,'deployed':False,
    'remaining':['Native damage-event compatibility including LivingDamage.Post remains unaccepted.','Authenticated summons, managed fights, all complete battles, rewards, multiplayer and persistence gates remain.','Do not start attended Curios reload until user confirms they are present; no private confirmation bypass.','Keep formal instances and D history read-only; no deployment under current instruction.']}
save(OUT,closeout)
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path)
    state.update({'activeRuntimePids':[],'ownedJavaOrPythonProcessesRemaining':False,'sparrowEvasionPortAccepted':True,'sparrowHitEvasionNativeAccepted':True,'sparrowAllyEvasionNativeAccepted':True,'latestAcceptedSparrowEvasionReceipt':str(OUT),'latestScopedBossCandidate':candidate['jar'],'latestScopedBossCandidateSha256':candidate['jarSha256'],'latestScopedBossCandidateBuildReceipt':str(build),'currentBossBatch':'Stage30 original Sparrow hit evasion scoped accepted; source-only checkpoint pending','modelResourceUploadProhibited':True,'musicUploadProhibited':True,'installable':False,'deployed':False,'fullBossMigrationComplete':False})
    save(path,state)
print(json.dumps({'closeout':str(OUT),'basicChecks':971,'allyChecks':772,'actualDefinedClassChecks':351,'scopedAccepted':True,'fullBossMigrationComplete':False,'modelOrMusicUploaded':False}),flush=True)
