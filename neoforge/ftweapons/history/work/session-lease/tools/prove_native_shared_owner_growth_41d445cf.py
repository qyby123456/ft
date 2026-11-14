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
candidate=work/'reports/native-repair-ranged-use-candidate-41d445cf.json';frozen(load(candidate))
delta=work/'reports/native-shared-repair-owner-growth-source-delta-41d445cf.json';d=load(delta);frozen(d);assert d['failures']==0
path=work/'reports/native-repair-runtime-material-ranged-crossbow-owner-capacity-qualified-41d445cf.json';data=load(path);frozen(data)
attempt=Path(data['attempt']);terminal=load(attempt/'terminal308.json');probe=data['probe']
assert data['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and probe['failures']==0
assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted']
assert probe['allSixActualOriginalCrossbowFactoriesBeyondFourSharedOwners']
assert probe['foreignPublicSharedNodeMutationNoticeRetiredAllSixWithoutItemMutation']
assert not probe['originalPhysicalMutationActuallyPerformedByNoticeControl'] and not probe['fullSignedImpactPerformedByCapacityFixture']
ids=['ftboss:'+tier+'_crossbow' for tier in ['wooden','stone','iron','golden','diamond','netherite']]
assert [row['id'] for row in probe['cases']]==ids
for count,row in enumerate(probe['cases'],1):
    for key in ['genuineNonNullLeaseAndGenerationBeforeAttack','actualUseStarted','originalEarlyReleasePreservedInputImageAmmoAndAuthority','chargedProjectilesPresent','actualOriginalFactoryCarrierPaidAndValid','genuineGenerationLiveAfterOriginalSequentialFire','actualOriginalPostFactorySaveReturned']:assert row[key],(row['id'],key)
    for key in ['generationRetiredAfterUse','generationRetiredAfterCharge','generationRetiredAfterFire']:assert not row[key],(row['id'],key)
    assert row['actualOriginalUseResult']==row['actualOriginalFireUseResult']=='CONSUME'
    assert row['ammoBefore']==16 and row['ammoAfterCharge']==15 and row['actualDamageAfterOriginalFire']==1
    assert row['actualSharedEmptyOwnerSlotCapacity']==(4 if count<=4 else 8)
    assert row['actualSharedEmptyCurrentOwnerCount']>=count
assert len(probe['cases'])==6
for path_to_hash in [Path(__file__),attempt/'runtime308.log',attempt/'live-process308.json',attempt/'server/actual-storage-view308.json']:digest(path_to_hash)
compile_failure=work/'build/repair-runtime-6a45dd11/compile-terminal320.json';bad=load(compile_failure);assert bad['exitCode']==1 and not bad['serverStarted'];digest(compile_failure.parent/'compile308.log')
output=work/'reports/native-shared-repair-owner-growth-focused-evidence-41d445cf.json'
report={'failures':0,'actualCases':6,'ids':ids,'acceptedRuntime':str(path),'genuinePaidOriginalChargeAndFactoryFire':True,'actualSharedEmptyCapacitySequence':[row['actualSharedEmptyOwnerSlotCapacity'] for row in probe['cases']],'actualSharedEmptyOwnerCountSequence':[row['actualSharedEmptyCurrentOwnerCount'] for row in probe['cases']],'allPriorGenuineOwnersPreservedDuringGrowth':True,'foreignPublicSharedNodeMutationNoticeRetiresAllSixWithoutItemMutation':True,'noticeControlIsNotActualPhysicalComponentMutation':True,'privateAuthorityFieldsWritten':False,'genuineLeaseOrRetiredStateManufacturedOrReset':False,'actualFullSignedImpactAcceptedByThisReceipt':False,'postCombatRestartAcceptedByThisReceipt':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'actualFactories':6,'ownerCapacity':report['actualSharedEmptyCapacitySequence'],'ownerCounts':report['actualSharedEmptyOwnerCountSequence'],'allOwnerNoticeFanout':True,'hashes':len(hashes),'failures':0}))
