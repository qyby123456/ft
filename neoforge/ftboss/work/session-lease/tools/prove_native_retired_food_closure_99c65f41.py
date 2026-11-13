import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1]
hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):
    digest(path);return json.loads(path.read_text(encoding='utf-8-sig'))
def verify(receipt):
    for name,wanted in receipt['inputHashes'].items():assert digest(Path(name))==wanted,name
candidate_path=work/'reports/native-repair-ranged-use-candidate-99c65f41.json';candidate=load(candidate_path);verify(candidate)
assert candidate['classCount']==1381 and candidate['effectiveSourceCount']==716 and candidate['guardCount']==208 and not candidate['newClasses']
source_path=work/'reports/native-retired-food-source-bytecode-delta-99c65f41.json';source=load(source_path);verify(source)
assert source['failures']==0 and source['unchangedEffectiveSources']==709 and source['networkUiAndCatalogueExactIntendedDelta']
assert source['retiredEffectsRemovedCommonAttributeDispatcherRetained'] and source['targetedBytecodeAssertions']==13166
asm=load(Path(candidate['attempt'])/'asm.json');assert asm['failures']==0 and asm['prototypeClassesChecked']==268
baseline=load(work/'reports/native-repair-runtime-bow-original-use-529ba4c9.json')
old_assets=Path(baseline['attempt'])/'ftboss-unpackaged-proposal/assets'
original_assets={path.relative_to(old_assets).as_posix():path for path in old_assets.rglob('*') if path.is_file()}
runtimes=[]
for mode in ['trident','bow']:
    for suffix in ['', '-qualified']:
        prepared=load(work/('reports/native-retired-food-'+mode+suffix+'-preparation-99c65f41.json'));verify(prepared)
        assert not prepared['authorityFieldsWritten']
    runtime_path=work/('reports/native-repair-runtime-retired-food-'+mode+'-qualified-99c65f41.json')
    runtime=load(runtime_path);verify(runtime);attempt=Path(runtime['attempt']);terminal=load(attempt/'terminal308.json');digest(attempt/'runtime308.log')
    assert runtime['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
    probe=runtime['probe'];assert probe['failures']==terminal['probe']['failures']==0
    assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted']
    requests=probe['retiredFoodRequests'];assert requests['failures']==0 and requests['rejectedRequests']==176
    for key in ['legacyViewsEmpty','armorRequestsRejected','noFoodWeaponOrExperienceMutation','scratchPlayerNotAuthenticated']:assert requests[key],key
    closed=probe['retiredFoodCatalogProtocolAndEffects']
    for key in ['cataloguesEmpty','storedFoodEffectsInert','storedDataAndVanillaSharpnessThreePreserved',
        'retiredPacketSendAndHandleReturnWithoutNetworkOrContext','actualLanguageResourcesRetiredAndMaqueKeysRetained']:assert closed[key],key
    cases=probe['cases'];assert len(cases)==6
    item='minecraft:trident' if mode=='trident' else 'minecraft:bow'
    case=next(case for case in cases if case['id']==item)
    for key in ['genuineNonNullLeaseAndGenerationBeforeAttack','actualUseStarted','earlyImageSame','earlyLeaseSame','earlyGenerationSame',
        'earlyGenerationLive','earlyHandSame','earlyCurrentInputLive','earlyPaidStateLiveWithObservedSemanticTransition',
        'actualOriginalReleaseReturned','sameLeaseAndGenerationAfterAttack']:assert case[key],key
    for key in ['generationRetiredAfterUse','generationRetiredAfterRelease','generationRetiredAfterAttack']:assert not case[key],key
    assert case['actualOriginalUseResult']=='CONSUME' and case['wearBefore']==0 and case['wearAfter']==1 and case['healthAfter']<case['healthBefore']
    flights=case['actualProjectileFlights'];assert len(flights)==1
    for key in ['snapshotPresent','snapshotValid','repairEpochPresent','signaturePresent','paidCarrierPresent']:assert flights[0][key],key
    assert flights[0]['purpose']=='NATIVE_PROJECTILE' and flights[0]['nativeEntry']==mode.upper()
    assert case['publications'] and len(case['damageEvents'])>=2
    for event in case['damageEvents']:
        for key in ['resolvedSnapshotPresent','resolvedSnapshotValid','repairEpochPresent','serverSignaturePresent']:assert event[key],key
    for publication in case['publications']:
        for key in ['publicationPresent','preparedCommitted','originalSequencePresent','preparationPresent','sessionPresent','prepared_sealed','prepared_bound','session_coverageInstalled','traceComplete']:assert publication[key],key
        for key in ['prepared_unsupported','prepared_retired','session_traceRevoked']:assert not publication[key],key
        assert publication['session_next']==4 and publication.get('firstObservationFailure') is None and publication.get('firstRevocationSite434') is None
    if mode=='trident':
        assert case['actualTridentWholeInstallationCertified'] and flights[0]['copiedPickupHasNoPrivateAuthority']
        for key in ['closedRealSignedImpactCustodyDenied','foreignRealSignedCustodyHelperDenied']:assert case[key],key
        pickup=case['actualPhysicalPickup']
        for key in ['actualProjectileRemovedByPickup','pickedPresent','sameOriginalObject','sameOriginalWholeImage','pickedNonNullLease','pickedNonNullGeneration','sameGenuineLease','sameGenuineGeneration','originalPostPickupSaveReturned']:assert pickup[key],key
        assert pickup['pickedCount']==pickup['pickedWear']==1 and not pickup['originalGenerationRetired']
    else:
        assert case['originalMainHandStillSameBowAfterShot'] and case['originalPostCombatSaveReturned'] and case['originalArrowCountAfterShot']==1
    assets=attempt/'ftboss-unpackaged-proposal/assets'
    current_assets={path.relative_to(assets).as_posix():path for path in assets.rglob('*') if path.is_file()}
    assert original_assets.keys()==current_assets.keys()
    changed=[]
    for relative,path in current_assets.items():
        if digest(path)!=digest(original_assets[relative]):changed.append(relative)
    assert sorted(changed)==['ftboss/lang/en_us.json','ftboss/lang/zh_cn.json']
    for relative,frozen in candidate['additionalProductionResources'].items():assert digest(attempt/'ftboss-unpackaged-proposal'/relative)==digest(Path(frozen))
    observer=next(Path(name) for name in runtime['inputHashes'] if name.endswith('PositiveWeapon436.java'))
    text=observer.read_text(encoding='utf-8');assert 'getModFileById("ftboss").getFile().findResource(' in text
    assert 'enchantments.set(sharpness, 3)' in text and 'stored.get(DataComponents.ENCHANTMENTS).getLevel(sharpness) != 3' in text
    runtimes.append({'mode':mode,'runtime':str(runtime_path),'attempt':str(attempt),'observerFailures':0,'javaExitCode':0,
        'normalStopAndCompleteSave':True,'retiredRequestsRejected':176,'onlyTwoLanguageAssetsChanged':True})
for path in [Path(__file__),work/'tools/prepare_native_retired_food_runtime.py',work/'tools/prepare_native_retired_food_qualified_runtime.py']:digest(path)
output=work/'reports/native-retired-food-closure-focused-evidence-99c65f41.json'
result={'failures':0,'candidate':str(candidate_path),'sourceDelta':str(source_path),'classes':1381,'sources':716,'guards':208,
 'targetedAsmClasses':268,'targetedBytecodeAssertions':13166,'retiredWeaponFoodEffectImplementationsRemoved':True,
 'retiredWeaponAndArmorCatalogueDataRemoved':True,'retiredActionSevenRejectedBeforeSendOrContextAccess':True,
 'deadClientFoodEnchantmentTooltipHelpersRemoved':True,'languageKeysRemovedPerLanguage':85,'unrelatedMaqueBlessingKeysRetained':True,
 'allOtherLoadedAssetFilesByteIdentical':True,'storedLegacyItemDataNotDeletedOrRewritten':True,
 'vanillaSharpnessThreeComponentPreservedAndGlobalRulesUnchanged':True,'independentEventUnchanged':True,
 'originalPaidBowAndTridentFullSignedSessionAndOriginalSaveRegressionAccepted':True,'tridentSameOriginalLiveLeasePhysicalPickupRegressionAccepted':True,
 'strictRuntimeEvidence':runtimes,'oldFixtureResourceLookupFailuresExcluded':['repair-runtime-626097f8','repair-runtime-1951308b'],
 'compatibilityTypeAndNoOpAbiShellsRemain':True,'allHistoricalBlessingSymbolsRemoved':False,
 'newCandidateRestartOrFullSixFamilyCombatMatrixClaimed':False,'authenticatedMultiplayerAccepted':False,
 'fullLifecycleAccepted':False,'installable':False,'deployed':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(result,stream,ensure_ascii=False,indent=2)
print(json.dumps({'evidence':output.name,'inputHashes':len(hashes),'runtimes':len(runtimes),'activeRetiredFoodClosureAccepted':True}))
