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
source_path=work/'reports/native-trident-pickup-source-delta-529ba4c9.json'
source=load(source_path);verify(source)
assert source['failures']==0 and source['exactInverseOutsidePickupAndWholeParentProof'] and source['allCompiledTimerAssignmentPinsMatch']
assert source['unchangedSources']==708 and not source['runtimeAcceptedByThisReceipt']
prior_path=work/'reports/native-trident-paid-signed-combat-focused-evidence-29296443.json'
prior=load(prior_path);verify(prior)
assert prior['failures']==0 and prior['fullSignedSessionAccepted'] and not prior['pickupAndPersistenceAccepted']
candidate_path=work/'reports/native-repair-ranged-use-candidate-529ba4c9.json'
candidate=load(candidate_path);verify(candidate)
assert candidate['bindingSourcesPreserveProposalOverrides'] and candidate['classCount']==1381 and candidate['effectiveSourceCount']==716 and candidate['guardCount']==208
asm=load(Path(candidate['attempt'])/'asm.json')
assert asm['failures']==0 and asm['prototypeClassesChecked']==240
preparations=['native-trident-foundation-gate-preparation-529ba4c9.json','native-trident-pickup-refusal-preparation-529ba4c9.json',
 'native-trident-post-pickup-restart-preparation-529ba4c9.json','native-trident-foreign-controls-preparation-529ba4c9.json']
for name in preparations:verify(load(work/'reports'/name))
runtimes=[]
for mode in ['foundation-gate','pickup-refusal','post-pickup-restart','external-remove','foreign-release']:
    path=work/('reports/native-repair-runtime-trident-'+mode+'-529ba4c9.json')
    runtime=load(path);verify(runtime)
    attempt=Path(runtime['attempt']);terminal=load(attempt/'terminal308.json');digest(attempt/'runtime308.log')
    assert runtime['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
    probe=runtime['probe'];assert probe['failures']==terminal['probe']['failures']==0
    assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted']
    observer=next(Path(name) for name in runtime['inputHashes'] if name.endswith('PositiveWeapon436.java'))
    observer_text=observer.read_text(encoding='utf-8')
    assert '.set(null,' not in observer_text and 'setBoolean(' not in observer_text and 'setLong(' not in observer_text
    if mode=='post-pickup-restart':
        assert probe['paidTridentPostPickupContinuationAcrossNewProcess'] and len(probe['cases'])==1
        case=probe['cases'][0];assert case['id']=='minecraft:trident'
        for key in ['actualOriginalGenerationFourSaveCommitted','genuineNonNullLeaseAndGeneration','sameEpochGenerationThreeToFour',
         'originalWholeItemAndWearCoherent','consumedReplayDeniedWithoutChangingFirstSuccessor']:assert case[key],key
        assert 'actual.getDamageValue() != 1' in observer_text
        assert 'LeaseGeneration' in observer_text and 'storage.save(restored)' in observer_text
    else:
        cases=probe['cases'];assert len(cases)==6;case=cases[-1];assert case['id']=='minecraft:trident'
        assert case['wearBefore']==0 and case['wearAfter']==1 and case['healthAfter']<case['healthBefore']
        if mode in ['foundation-gate','pickup-refusal']:
            for key in ['actualTridentWholeInstallationCertified','sameGenuineAuthorityAfterGateControls','actualUseStarted',
             'earlyImageSame','earlyLeaseSame','earlyGenerationSame','earlyGenerationLive','earlyHandSame',
             'earlyCurrentInputLive','earlyPreviousInputRetired','earlyPaidStateLiveWithObservedSemanticTransition',
             'actualOriginalReleaseReturned','originalMainHandEmptyAfterThrow','sameLeaseAndGenerationAfterAttack',
             'closedRealSignedImpactCustodyDenied','foreignRealSignedCustodyHelperDenied']:assert case[key],key
            assert case['actualOriginalUseResult']=='CONSUME'
            for key in ['generationRetiredAfterUse','generationRetiredAfterRelease','generationRetiredAfterAttack']:assert not case[key],key
            assert len(case['actualProjectileFlights'])==1
            flight=case['actualProjectileFlights'][0]
            for key in ['snapshotPresent','snapshotValid','repairEpochPresent','signaturePresent','paidCarrierPresent','copiedPickupHasNoPrivateAuthority']:assert flight[key],key
            assert flight['purpose']=='NATIVE_PROJECTILE' and flight['nativeEntry']=='TRIDENT'
            assert len(case['damageEvents'])>=2 and case['publications']
            for event in case['damageEvents']:
                for key in ['resolvedSnapshotPresent','resolvedSnapshotValid','repairEpochPresent','serverSignaturePresent']:assert event[key],key
            for publication in case['publications']:
                for key in ['publicationPresent','preparedCommitted','originalSequencePresent','preparationPresent','sessionPresent','prepared_sealed','prepared_bound','session_coverageInstalled','traceComplete']:assert publication[key],key
                for key in ['prepared_unsupported','prepared_retired','session_traceRevoked']:assert not publication[key],key
                assert publication['session_next']==4 and publication.get('firstObservationFailure') is None and publication.get('firstRevocationSite434') is None
            pickup=case['actualPhysicalPickup']
            for key in ['actualProjectileRemovedByPickup','pickedPresent','sameOriginalObject','sameOriginalWholeImage','pickedNonNullLease','pickedNonNullGeneration',
             'sameGenuineLease','sameGenuineGeneration','originalPostPickupSaveReturned']:assert pickup[key],key
            assert pickup['pickedCount']==pickup['pickedWear']==1 and not pickup['originalGenerationRetired']
            if mode=='pickup-refusal':
                refusal=case['actualPickupRefusalControls']
                for key in ['foreignHelpersDelegatedExactlyOnceWithoutPrivatePublication','foreignInventoryOriginalExceptionIdentityPreserved',
                 'foreignTakenOriginalExceptionIdentityPreserved','fullInventoryOriginalPlayerTouchRefusedWithoutMutation','otherPlayerOriginalTouchDeniedWithoutAuthorityTransfer']:assert refusal[key],key
                assert 'fullTick < 32' in observer_text and 'fullItems[slot] = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 64)' in observer_text
                assert 'physical.playerTouch(otherPicker)' in observer_text
        else:
            for key in ['foreignCallbacksPreserveExactInputImageAndAuthority','foreignOriginalFactoryExceptionIdentityPreserved',
             'foreignNativeEntryOriginalThrowReturnedOnce','foreignNativeEntryNoSignedCarrier','foreignNativeEntryAuthorityRetired','foreignNativeEntryScopesClosed']:assert case[key],key
            assert case['foreignOriginalFactoryCallCount']==1 and not case['publications']
            if mode=='external-remove':assert case['externalAbsentComponentRemovalImageUnchanged'] and case['externalAbsentComponentRemovalRetiresAuthority']
    runtimes.append({'mode':mode,'runtime':str(path),'attempt':str(attempt),'javaExitCode':0,'observerFailures':0,'normalStopAndCompleteSave':True})
for path in [Path(__file__),work/'tools/build_native_trident_pickup_proof.py',work/'tools/prepare_native_trident_certified_followups_runtime.py']:digest(path)
output=work/'reports/native-trident-paid-signed-pickup-restart-focused-evidence-529ba4c9.json'
result={'failures':0,'candidate':str(candidate_path),'sourceDelta':str(source_path),'priorScopedCombat':str(prior_path),
 'classes':1381,'sources':716,'guards':208,'targetedAsmChecks':240,'fullSignedSessionAccepted':True,
 'actualWholePickupAndPlayerTakeParentProofAccepted':True,'actualPaidOriginalPhysicalPickupAccepted':True,
 'sameOriginalWholeImageGenuineLiveLeaseGenerationPreserved':True,'postPickupOriginalSaveNewJvmGenerationThreeToFourAndConsumedReplayDenied':True,
 'fullInventoryOriginalRefusalAndSubsequentSuccessfulPickupAccepted':True,'otherPlayerOriginalTouchAuthorityTransferDenied':True,
 'foreignPickupHelpersOriginalResultsOnceAndExceptionIdentityPreserved':True,'foreignNativeEntrySignedAuthorityDenied':True,
 'copiedPickupHasNoPrivateAuthority':True,'strictRuntimeEvidence':runtimes,'acceptedScope':'Exact unenchanted vanilla trident, original paid throw/real impact/full signed Session, physical owner pickup and original save/new JVM continuation',
 'runtimeOriginalPickupExceptionInsideCertifiedParentExercised':False,'enchantmentsVariantsAndLoyaltyAccepted':False,
 'crossFamilyEvidenceReusedForThisCandidate':False,'authenticatedMultiplayerAccepted':False,'fullLifecycleAccepted':False,
 'installable':False,'deployed':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(result,stream,ensure_ascii=False,indent=2)
print(json.dumps({'evidence':output.name,'inputHashes':len(hashes),'runtimes':len(runtimes),'paidOriginalPickupAndRestartAccepted':True}))
