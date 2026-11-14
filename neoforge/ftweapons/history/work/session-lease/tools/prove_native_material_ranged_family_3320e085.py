import hashlib
import json
import sys
from pathlib import Path
family=sys.argv[1];assert family in ['bow','crossbow']
work=Path(__file__).resolve().parents[1];hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):
    digest(path);return json.loads(path.read_text(encoding='utf-8'))
def frozen(data):
    for name,wanted in data['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
def runtime(path,expected):
    data=load(path);frozen(data);attempt=Path(data['attempt']);terminal=load(attempt/'terminal308.json')
    assert data['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
    probe=data['probe'];assert probe['failures']==0 and len(probe['cases'])==expected
    assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted']
    for name in ['runtime308.log','live-process308.json','server/actual-storage-view308.json']:digest(attempt/name)
    assert 'Stopping server' in (attempt/'runtime308.log').read_text(encoding='utf-8',errors='replace')
    return data,probe,attempt
candidate=work/'reports/native-repair-ranged-use-candidate-3320e085.json';frozen(load(candidate))
delta=work/'reports/native-material-ranged-absent-component-source-delta-3320e085.json';d=load(delta);frozen(d);assert d['failures']==0
paid=work/'reports/native-material-ranged-paid-repair-focused-evidence-e43eb814.json';p=load(paid);frozen(p);assert p['failures']==0 and p['actualMaterialCases']==12
positive_tag='2be7d6c1' if family=='bow' else '3320e085'
positive_path=work/('reports/native-repair-runtime-material-ranged-'+family+'-signed-'+positive_tag+'.json')
positive,probe,attempt=runtime(positive_path,6)
ids=['ftboss:'+tier+'_'+family for tier in ['wooden','stone','iron','golden','diamond','netherite']]
assert [row['id'] for row in probe['cases']]==ids
expected_material={'ftboss:'+tier+'_'+kind for tier in ['wooden','stone','iron','golden','diamond','netherite'] for kind in ['bow','crossbow']}
assert set(probe['actualRegisteredMaterialPolicyAcceptedIds'])==expected_material
for row in probe['cases']:
    for key in ['genuineNonNullLeaseAndGenerationBeforeAttack','actualUseStarted','sameLeaseAndGenerationAfterAttack']:assert row[key],(row['id'],key)
    for key in ['generationRetiredAfterUse','generationRetiredAfterAttack']:assert not row[key],(row['id'],key)
    assert row['actualOriginalUseResult']=='CONSUME' and row['wearBefore']==0 and row['wearAfter']==1 and row['healthAfter']<row['healthBefore']
    flights=row['actualProjectileFlights'];assert len(flights)==1
    for key in ['snapshotPresent','snapshotValid','repairEpochPresent','signaturePresent','paidCarrierPresent']:assert flights[0][key],(row['id'],key)
    assert flights[0]['purpose']=='NATIVE_PROJECTILE' and flights[0]['nativeEntry']==family.upper()
    assert len(row['damageEvents'])>=2 and row['publications']
    for event in row['damageEvents']:
        for key in ['resolvedSnapshotPresent','resolvedSnapshotValid','repairEpochPresent','serverSignaturePresent']:assert event[key],(row['id'],key)
    for publication in row['publications']:
        for key in ['publicationPresent','preparedCommitted','originalSequencePresent','preparationPresent','sessionPresent','prepared_sealed','prepared_bound','session_coverageInstalled','traceComplete']:assert publication[key],(row['id'],key)
        for key in ['prepared_unsupported','prepared_retired','session_traceRevoked']:assert not publication[key],(row['id'],key)
        assert publication['session_next']==4 and publication.get('firstObservationFailure') is None and publication.get('firstRevocationSite434') is None
    if family=='bow':
        for key in ['earlyImageSame','earlyLeaseSame','earlyGenerationSame','earlyGenerationLive','earlyHandSame','earlyCurrentInputLive','earlyPaidStateLiveWithObservedSemanticTransition','actualOriginalReleaseReturned','originalMainHandStillSameBowAfterShot','originalPostCombatSaveReturned']:assert row[key],(row['id'],key)
        assert not row['generationRetiredAfterRelease'] and row['originalArrowCountAfterShot']==1
    else:
        for key in ['originalEarlyReleasePreservedInputImageAmmoAndAuthority','chargedProjectilesPresent','actualOriginalPostCombatSaveAuthenticatedCommitted']:assert row[key],(row['id'],key)
        for key in ['generationRetiredAfterCharge','generationRetiredAfterFire']:assert not row[key],(row['id'],key)
        assert row['ammoBefore']==2 and row['ammoAfterCharge']==1
restart_path=work/('reports/native-repair-runtime-material-ranged-'+family+'-restart-qualified-3320e085.json')
restart,restarted,restart_attempt=runtime(restart_path,6)
assert [row['id'] for row in restarted['cases']]==ids and restarted['paidMaterialRangedPostCombatContinuationAcrossNewProcess']
for row in restarted['cases']:
    for key in ['originalWholeItemAndWearCoherent','genuineNonNullLeaseAndGeneration','sameEpochGenerationTwoToThree','consumedReplayDeniedWithoutChangingFirstSuccessor','actualOriginalGenerationThreeSaveCommitted']:assert row[key],(row['id'],key)
assert attempt!=restart_attempt
control_name='native-repair-runtime-material-ranged-bow-controls-qualified-3320e085.json' if family=='bow' else 'native-repair-runtime-material-ranged-crossbow-control-signed-3320e085.json'
control_path=work/'reports'/control_name;control,controls,control_attempt=runtime(control_path,6)
assert [row['id'] for row in controls['cases']]==ids
for row in controls['cases']:
    if family=='bow':
        negative=row['actualUseRefusalControls']
        for key in ['foreignUseCallbacksDoNotMintOrModifyAuthority','originalNoAmmoUseRefusedWithSameWholeImageAndLiveAuthority']:assert negative[key],(row['id'],key)
        assert negative['originalNoAmmoUseResult']=='FAIL'
    else:
        for key in ['outsideOriginalCallbacksPreservedCurrentInputImageAndAuthority','foreignNativeUseOriginalReturnedOnce','foreignNativeUseAuthorityRetired','foreignNativeUseNoSignedCarrier','privateScopesClosedWithoutWrites']:assert row[key],(row['id'],key)
assert len({attempt,restart_attempt,control_attempt})==3
for path in [Path(__file__),delta,paid,candidate]:digest(path)
output=work/('reports/native-material-'+family+'-paid-signed-restart-focused-evidence-3320e085.json')
report={'failures':0,'family':family,'actualRegisteredItems':ids,'actualRuntimeCount':3,'positiveRuntime':str(positive_path),'restartRuntime':str(restart_path),'foreignControlRuntime':str(control_path),'actualPaidRepairEvidence':str(paid),'actualOriginalSignedProjectileImpactAndWholeSignedSession':True,'actualOriginalSaveNewJvmGenerationTwoToThreeAndReplayDenied':True,'candidate':str(candidate),'positiveCandidate':positive_tag,'positiveReuseOn3320':'Bow methods and data unchanged;3320 only modifies bounded material Crossbow absence-before-charge semantics' if family=='bow' else 'Actual3320 runtime','scope':'Unenchanted ordinary arrow only; no arbitrary subclasses/special ammo or authenticated clients certified.','authorityFieldsWritten':False,'authenticatedMultiplayerAccepted':False,'full103ItemMatrixAccepted':False,'installable':False,'deployed':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'items':6,'runtimes':3,'hashes':len(hashes),'failures':0}))
