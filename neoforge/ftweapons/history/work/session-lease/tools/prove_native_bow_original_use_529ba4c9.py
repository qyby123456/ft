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
pickup_path=work/'reports/native-trident-paid-signed-pickup-restart-focused-evidence-529ba4c9.json'
pickup=load(pickup_path);verify(pickup);assert pickup['failures']==0 and pickup['actualPaidOriginalPhysicalPickupAccepted']
candidate_path=work/'reports/native-repair-ranged-use-candidate-529ba4c9.json'
candidate=load(candidate_path);verify(candidate)
assert candidate['classCount']==1381 and candidate['effectiveSourceCount']==716 and candidate['guardCount']==208
for name in ['native-bow-original-use-preparation-529ba4c9.json','native-bow-original-use-controls-preparation-529ba4c9.json','native-bow-original-use-restart-preparation-529ba4c9.json']:
    prepared=load(work/'reports'/name);verify(prepared);assert not prepared['productChanges'] and not prepared['authorityFieldsWritten']
runtimes=[]
for mode in ['positive','controls','restart']:
    suffix='' if mode=='positive' else '-'+mode
    path=work/('reports/native-repair-runtime-bow-original-use'+suffix+'-529ba4c9.json')
    runtime=load(path);verify(runtime);attempt=Path(runtime['attempt']);terminal=load(attempt/'terminal308.json');digest(attempt/'runtime308.log')
    assert runtime['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
    probe=runtime['probe'];assert probe['failures']==terminal['probe']['failures']==0
    assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted']
    observer=next(Path(name) for name in runtime['inputHashes'] if name.endswith('PositiveWeapon436.java'))
    source=observer.read_text(encoding='utf-8');assert 'startUsingItem(' not in source
    if mode=='restart':
        assert probe['paidBowPostCombatContinuationAcrossNewProcess'] and len(probe['cases'])==1
        case=probe['cases'][0];assert case['id']=='minecraft:bow'
        for key in ['actualOriginalGenerationFourSaveCommitted','genuineNonNullLeaseAndGeneration','sameEpochGenerationThreeToFour',
         'originalWholeItemAndWearCoherent','consumedReplayDeniedWithoutChangingFirstSuccessor']:assert case[key],key
        assert 'actual.getDamageValue() != 1' in source and 'storage.save(restored)' in source
    else:
        assert len(probe['cases'])==6
        case=next(row for row in probe['cases'] if row['id']=='minecraft:bow')
        assert case['actualItemClass']=='net.minecraft.world.item.BowItem' and case['actualOriginalUseResult']=='CONSUME'
        for key in ['genuineNonNullLeaseAndGenerationBeforeAttack','actualUseStarted','earlyImageSame','earlyLeaseSame','earlyGenerationSame',
         'earlyGenerationLive','earlyHandSame','earlyCurrentInputLive','earlyPaidStateLiveWithObservedSemanticTransition',
         'actualOriginalReleaseReturned','sameLeaseAndGenerationAfterAttack','originalMainHandStillSameBowAfterShot','originalPostCombatSaveReturned']:assert case[key],key
        for key in ['generationRetiredAfterUse','generationRetiredAfterRelease','generationRetiredAfterAttack']:assert not case[key],key
        assert case['wearBefore']==0 and case['wearAfter']==1 and case['originalArrowCountAfterShot']==1
        assert case['healthAfter']<case['healthBefore']
        assert len(case['actualProjectileFlights'])==1
        flight=case['actualProjectileFlights'][0]
        for key in ['snapshotPresent','snapshotValid','repairEpochPresent','signaturePresent','paidCarrierPresent']:assert flight[key],key
        assert flight['purpose']=='NATIVE_PROJECTILE' and flight['nativeEntry']=='BOW'
        assert len(case['damageEvents'])>=2 and case['publications']
        for event in case['damageEvents']:
            for key in ['resolvedSnapshotPresent','resolvedSnapshotValid','repairEpochPresent','serverSignaturePresent']:assert event[key],key
        for publication in case['publications']:
            for key in ['publicationPresent','preparedCommitted','originalSequencePresent','preparationPresent','sessionPresent','prepared_sealed','prepared_bound','session_coverageInstalled','traceComplete']:assert publication[key],key
            for key in ['prepared_unsupported','prepared_retired','session_traceRevoked']:assert not publication[key],key
            assert publication['session_next']==4 and publication.get('firstObservationFailure') is None and publication.get('firstRevocationSite434') is None
        assert source.count('var useResult = actual.use(')==1 and source.count('var secondUse = actual.use(')==1
        assert 'actual.getUseDuration(player) - 1)' in source and 'actual.getUseDuration(player) - 20)' in source
        if mode=='controls':
            controls=case['actualUseRefusalControls']
            assert controls['foreignUseCallbacksDoNotMintOrModifyAuthority'] and controls['originalNoAmmoUseRefusedWithSameWholeImageAndLiveAuthority']
            assert controls['originalNoAmmoUseResult']=='FAIL'
    runtimes.append({'mode':mode,'runtime':str(path),'attempt':str(attempt),'observerFailures':0,'javaExitCode':0,'normalStopAndCompleteSave':True})
digest(Path(__file__))
output=work/'reports/native-bow-original-use-signed-restart-focused-evidence-529ba4c9.json'
result={'failures':0,'candidate':str(candidate_path),'productionGraphUnchangedSinceScopedTridentPickup':True,
 'classes':1381,'sources':716,'guards':208,'actualOriginalItemStackUseEarlyReleaseSecondUseFullReleaseArrowImpactAccepted':True,
 'fullSignedSessionAccepted':True,'wearZeroToOneAmmoTwoToOneSameHeldOriginalAndLiveGenuineAuthority':True,
 'originalNoAmmoUseRefusedWithoutWholeImageAuthorityMutationOrScopeLeak':True,'foreignUseHelpersDoNotMintAuthority':True,
 'originalPostCombatSaveNewJvmGenerationThreeToFourAndConsumedReplayDenied':True,'strictRuntimeEvidence':runtimes,
 'acceptedScope':'Exact unenchanted vanilla bow with ordinary arrows and genuine paid original save/reload',
 'bowVariantsEnchantmentsAndSpecialAmmunitionAccepted':False,'authenticatedMultiplayerAccepted':False,
 'fullLifecycleAccepted':False,'installable':False,'deployed':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(result,stream,ensure_ascii=False,indent=2)
print(json.dumps({'evidence':output.name,'inputHashes':len(hashes),'runtimes':len(runtimes),'actualOriginalBowUseAccepted':True}))
