import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):
    digest(path);return json.loads(path.read_text(encoding='utf-8'))
def frozen(data):
    for name,wanted in data['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
candidate=work/'reports/native-repair-ranged-use-candidate-3320e085.json';frozen(load(candidate))
paid=work/'reports/native-material-ranged-paid-repair-focused-evidence-e43eb814.json';frozen(load(paid))
path=work/'reports/native-repair-runtime-material-ranged-crossbow-controls-qualified-3320e085.json';data=load(path);frozen(data)
attempt=Path(data['attempt']);terminal=load(attempt/'terminal308.json');probe=data['probe']
assert data['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and probe['failures']==0
assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted']
ids=['ftboss:'+tier+'_crossbow' for tier in ['wooden','stone','iron','golden','diamond','netherite']]
assert [row['id'] for row in probe['cases']]==ids
for row in probe['cases']:
    for key in ['genuineNonNullLeaseAndGenerationBeforeAttack','actualUseStarted','originalEarlyReleasePreservedInputImageAmmoAndAuthority','chargedProjectilesPresent','outsideOriginalCallbacksPreservedCurrentInputImageAndAuthority','foreignNativeUseOriginalReturnedOnce','foreignNativeUseAuthorityRetired','foreignNativeUseNoSignedCarrier','privateScopesClosedWithoutWrites','actualOriginalTargetHitAndRemovedBeforeNextCase']:assert row[key],(row['id'],key)
    for key in ['generationRetiredAfterUse','generationRetiredAfterCharge']:assert not row[key],(row['id'],key)
    assert row['ammoBefore']==16 and row['ammoAfterCharge']==15 and row['wearBefore']==0 and row['wearAfter']==1 and row['healthAfter']<row['healthBefore']
    assert row['actualOriginalUseResult']==row['actualOriginalFireUseResult']=='CONSUME'
    assert row['damageEvents'] and all(not event['resolvedSnapshotValid'] for event in row['damageEvents'])
    assert len(row['actualProjectileFlights'])==1
    for key in ['snapshotPresent','snapshotValid','repairEpochPresent','signaturePresent','paidCarrierPresent']:assert not row['actualProjectileFlights'][0][key],(row['id'],key)
excluded=work/'reports/native-repair-runtime-material-ranged-crossbow-control-signed-3320e085.json';bad=load(excluded);frozen(bad);end=load(Path(bad['attempt'])/'terminal308.json')
assert bad['exitCode']==0 and end['stopSent'] and end['completeSave'] and bad['probe']['failures']==1
last=bad['probe']['cases'][-1];assert len(bad['probe']['cases'])==5 and not last['damageEvents']
assert last['foreignNativeUseNoSignedCarrier'] and last['foreignNativeUseAuthorityRetired'] and last['privateScopesClosedWithoutWrites']
for path_to_hash in [Path(__file__),attempt/'runtime308.log',attempt/'live-process308.json',attempt/'server/actual-storage-view308.json']:digest(path_to_hash)
output=work/'reports/native-material-crossbow-foreign-controls-focused-evidence-3320e085.json'
report={'failures':0,'actualCases':6,'ids':ids,'acceptedRuntime':str(path),'genuinePaidAuthorityAtOriginalCharge':True,'outsideCallbacksCannotMintOrModifyAuthority':True,'foreignPublicHelperDelegatesOriginalOnceButRetiresAuthorityAndPublishesNoSignedCarrier':True,'privateScopesClosed':True,'actualOriginalArrowImpactsOnCaseTargets':True,'excludedFixtureFailure':str(excluded),'excludedReason':'Old target accumulation caused fifth no damage event despite no signed authority; targets now removed and actual target damage additionally required.','positiveSignedCombatAcceptedByThisReceipt':False,'postCombatRestartAcceptedByThisReceipt':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'actualControls':6,'hashes':len(hashes),'failures':0}))
