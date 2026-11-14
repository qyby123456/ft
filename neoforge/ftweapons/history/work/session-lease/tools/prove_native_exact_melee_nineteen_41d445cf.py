import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1];hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):digest(path);return json.loads(path.read_text(encoding='utf-8'))
def frozen(data):
    for name,wanted in data.get('inputHashes',{}).items():assert digest(Path(name))==wanted,Path(name).name
audit_path=work/'reports/native-remaining-variant-gap-audit-3320e085.json';audit=load(audit_path);frozen(audit)
expected=audit['groups']['otherExactOriginalClassesStillNeedActualMatrix'];ids=[row['id'] for row in expected];assert len(ids)==19
root_delta=load(work/'reports/native-shared-repair-owner-growth-source-delta-41d445cf.json');frozen(root_delta)
assert root_delta['failures']==root_delta['newProductClasses']==0 and root_delta['classCount']==1381 and root_delta['guardCount']==208
candidate=work/'reports/native-repair-ranged-use-candidate-41d445cf.json';frozen(load(candidate))
def runtime(mode,failures=0):
    path=work/('reports/native-repair-runtime-exact-melee-variants-'+mode+'-41d445cf.json');data=load(path);frozen(data);attempt=Path(data['attempt']);terminal=load(attempt/'terminal308.json')
    assert data['exitCode']==terminal['exitCode']==0 and data['probe']['failures']==terminal['probe']['failures']==failures
    assert terminal['stopSent'] and terminal['completeSave']
    for name in ['live-process308.json','runtime308.log','server/actual-storage-view308.json','compile-command308.json','compile308.log','restart-world-seed.json']:digest(attempt/name)
    world=attempt/'server/world'
    for path in [world/'level.dat',world/'data/ftboss_native_weapon_wear.dat',*sorted((world/'ftboss-authority').rglob('*.bin')),*sorted((world/'playerdata').glob('*.dat'))]:digest(path)
    assert not [path for directory in ['ftboss-authority','playerdata'] for path in (world/directory).rglob('*') if path.is_file() and path.suffix in ['.tmp','.pending']]
    if failures==0:assert [row['id'] for row in data['probe']['cases']]==ids
    return path,data['probe'],attempt
paid_path,paid,paid_attempt=runtime('paid-repair-pair-qualified')
assert paid['nineteenExactOriginalMeleeVariantsPaidRepairAndSameProcessReload']
for expected_row,row in zip(expected,paid['cases']):
    assert row['itemClass']==expected_row['class'] and row['originalMaximumDamage']==expected_row['maxDamage']
    assert row['originalRegisteredProfileStatueAndFoodRulesPreserved']
    for key in ['originalExactRegisteredClassAndDurabilityPreserved','actualPaidClaimAndThreeSupplyConsumptions','actualOriginalSaveCommitted','actualOriginalLoadRestoredNonNullGenuineSuccessor','sameEpochGenerationZeroToOne','oldPhysicalGenerationRetired','consumedReplayDeniedAndFirstSuccessorUnchanged','actualSuccessorOriginalSaveCommitted','nonNullLease','repairGenerationPresent']:assert row[key],(row['id'],key)
    assert row['damageAfterRepair']==0
    transactions=row['repairTransactions'];assert len(transactions)==5
    for index,(transaction,payment) in enumerate(zip(transactions,[0,3,3,2,0])):
        assert transaction['expectedPayment']==payment and transaction['remainingSupply']==0
        if 'accepted' in transaction:assert transaction['accepted'] and transaction['code']=='COMMITTED'
        else:assert transaction['signedSequence']==index+1 and transaction['signedStatus']==('LEASE_ISSUED' if index==4 else 'COMMITTED')
    if not row['originalFoundationApplicable']:assert row['originalPreClaimTaskIdentityUsedForSignedTerminalCheck']
positive_path,positive,positive_attempt=runtime('signed-qualified')
assert positive['nineteenExactOriginalMeleeVariantsGenerationTwoSignedSessions']
for expected_row,row in zip(expected,positive['cases']):
    for key in ['genuineNonNullLeaseAndGenerationBeforeAttack','repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority','foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation','foreignDirectEntryPreservesOriginalCallbackExceptionIdentity','originalPlayerAttackReturned','originalMeleeSnapshotValid','originalMeleeSnapshotRepairEpochPresent','sameLeaseAndGenerationAfterAttack','sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope','actualOriginalPostCombatSaveCalled','actualOriginalPostCombatSaveAuthenticatedCommitted']:assert row[key],(row['id'],key)
    assert not row['generationRetiredAfterAttack'] and row['healthAfter']<row['healthBefore']
    assert row['wearBefore']==0 and row['wearAfter']==(1 if expected_row['family']=='sword' else 2)
    assert row['capturedNativeMeleePurpose']=='NATIVE_MELEE_ACTUAL_HURT' and row['publications'] and row['damageEvents']
    for publication in row['publications']:
        for key in ['publicationPresent','preparedCommitted','originalSequencePresent','preparationPresent','sessionPresent','prepared_sealed','prepared_bound','session_coverageInstalled','traceComplete']:assert publication[key],(row['id'],key)
        for key in ['prepared_unsupported','prepared_retired','session_traceRevoked']:assert not publication[key],(row['id'],key)
        assert publication['session_next']==4 and publication['firstObservationFailure'] is None
    assert all(event['resolvedSnapshotValid'] and event['repairEpochPresent'] and event['serverSignaturePresent'] for event in row['damageEvents'])
restart_path,restart,restart_attempt=runtime('restart')
assert restart['nineteenExactMeleeVariantsPostCombatContinuationAcrossNewProcess']
for row in restart['cases']:
    for key in ['originalWholeItemAndWearCoherent','genuineNonNullLeaseAndGeneration','sameEpochGenerationTwoToThree','consumedReplayDeniedWithoutChangingFirstSuccessor','actualOriginalGenerationThreeSaveCommitted']:assert row[key],(row['id'],key)
controls_path,controls,controls_attempt=runtime('controls')
for expected_row,row in zip(expected,controls['cases']):
    for key in ['genuineGenerationTwoBeforeControl','originalForeignCallbackExactlyOnce','originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation','foreignOriginalMutationRetiresGenuineAuthority','noPrivatePermissionOrAuthorityReissued']:assert row[key],(row['id'],key)
    assert row['copyControl']==(expected_row['family']=='sword')
assert len({paid_attempt,positive_attempt,restart_attempt,controls_attempt})==4
excluded_path,excluded,excluded_attempt=runtime('paid-repair',1)
assert 'NullPointerException' in excluded['failure'] and len(excluded['cases'])==1
excluded_profile_path,excluded_profile,excluded_profile_attempt=runtime('paid-repair-qualified',1)
assert len(excluded_profile['cases'])==14 and excluded_profile['cases'][-1]['id']=='ftboss:defect_claw' and all(row['actualSuccessorOriginalSaveCommitted'] for row in excluded_profile['cases'][:13])
excluded_pair_path,excluded_pair,excluded_pair_attempt=runtime('paid-repair-profile-qualified',1)
assert len(excluded_pair['cases'])==14 and 'Scratch statue pair invalid' in excluded_pair['failure']
assert digest(root/'work/session-chain/reports/actual-current.json')=='1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
for path in [Path(__file__),root/'WRITE_OWNER.claim']:digest(path)
output=work/'reports/native-exact-melee-nineteen-paid-signed-restart-controls-focused-evidence-41d445cf.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'failures':0,'actualRegisteredItems':ids,'swordCount':13,'axeCount':6,'candidate':str(candidate),'actualAcceptedRuntimes':[str(path) for path in [paid_path,positive_path,restart_path,controls_path]],'excludedObserverFailuresPreserved':[str(excluded_path),str(excluded_profile_path),str(excluded_pair_path)],'genuineOriginalPaidRepairAndSupplies':True,'actualPlayerAttackWholeSignedSessionLiveGenuineAuthorityAndOriginalWear':True,'actualOriginalSaveNewJvmTwoToThreeAndReplayDenied':True,'foreignOriginalCopyOrPostCallbacksRetireAuthorityWithoutRegrantAndOriginalExceptionIdentityPreserved':True,'originalClassesAndDurabilityUnchanged':True,'scope':'Exact original registered SwordItem and AxeItem only, ordinary unenchanted melee. No arbitrary subclasses, special enchantments, transformed-provider internal mutations, authenticated clients or full lifecycle certified.','authorityWrittenByReflection':False,'full103ItemMatrixAccepted':False,'authenticatedMultiplayerAccepted':False,'installable':False,'deployed':False,'inputHashes':hashes},stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'actualItems':19,'actualAcceptedRuntimes':4,'excludedFailuresPreserved':3,'hashes':len(hashes),'failures':0}))
