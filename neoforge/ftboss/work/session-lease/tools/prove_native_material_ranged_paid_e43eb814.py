import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1]
hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):
    digest(path);return json.loads(path.read_text(encoding='utf-8'))
def frozen(data):
    for name,wanted in data['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
candidate=work/'reports/native-repair-ranged-use-candidate-e43eb814.json';frozen(load(candidate))
delta=work/'reports/native-material-ranged-source-bytecode-delta-e43eb814.json';frozen(load(delta))
path=work/'reports/native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json';accepted=load(path);frozen(accepted)
attempt=Path(accepted['attempt']);terminal=load(attempt/'terminal308.json');probe=accepted['probe']
assert accepted['exitCode']==terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and probe['failures']==0
assert probe['twelveMaterialRangedPaidRepairAndSameProcessReload']
assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted'] and not probe['fullSignedCombatSessionRunByThisFixture']
ids=['ftboss:'+tier+'_'+family for family in ['bow','crossbow'] for tier in ['wooden','stone','iron','golden','diamond','netherite']]
assert [row['id'] for row in probe['cases']]==ids
for row in probe['cases']:
    for key in ['boundRegisteredFinalMaterialClassAccepted','nonUnitCountMaterialPolicyDenied','vanillaObjectNotMaterialPolicyGranted','nonNullLease','repairGenerationPresent','actualPaidClaimAndThreeSupplyConsumptions','actualOriginalSaveCommitted','actualOriginalLoadRestoredNonNullGenuineSuccessor','sameEpochGenerationZeroToOne','oldPhysicalGenerationRetired','consumedReplayDeniedAndFirstSuccessorUnchanged','actualSuccessorOriginalSaveCommitted']:assert row[key],(row['id'],key)
    assert row['nativeWearEligibleBeforeStart'] and row['repairDamageBeforeStart']==10
    transactions=row['repairTransactions'];assert len(transactions)==5
    assert all(item['accepted'] and item['code']=='COMMITTED' and item['remainingSupply']==0 for item in transactions)
    assert [item['expectedPayment'] for item in transactions]==[0,3,3,2,0]
excluded=[]
for suffix,reason in [('baseline-99c65f41','original START_REPAIR rejection on eligible wooden Bow; product baseline'),('e43eb814','observer illegally constructs new Item after registry frozen; no accepted payment claim')]:
    name='native-repair-runtime-material-ranged-paid-repair-'+suffix+'.json'
    result=load(work/'reports'/name);frozen(result);end=load(Path(result['attempt'])/'terminal308.json')
    assert result['exitCode']==end['exitCode']==0 and end['completeSave'] and end['stopSent'] and result['probe']['failures']==1
    excluded.append({'receipt':str(work/'reports'/name),'reason':reason,'failure':result['probe']['failure'],'normalSavedStop':True})
assert 'START_REPAIR' in excluded[0]['failure'] and 'Registry is already frozen' in excluded[1]['failure']
output=work/'reports/native-material-ranged-paid-repair-focused-evidence-e43eb814.json'
for file in [Path(__file__),attempt/'runtime308.log',attempt/'server/actual-storage-view308.json',attempt/'live-process308.json']:digest(file)
with output.open('x',encoding='utf-8') as stream:json.dump({'failures':0,'acceptedRuntime':str(path),'actualMaterialCases':12,'ids':ids,'genuineOriginalPayment':[3,3,2],'genuineNonNullLeaseGenerationZeroToOne':True,'originalSaveLoadAndConsumedReplayDenied':True,'existingProviderCaptureAndLivenessKept':True,'excludedAttempts':excluded,'unregisteredItemConstructorControlAccepted':False,'fullSignedCombatAcceptedByThisReceipt':False,'authenticatedMultiplayerAccepted':False,'installable':False,'deployed':False,'inputHashes':hashes},stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'paidCases':12,'hashes':len(hashes),'excludedAttempts':2,'failures':0}))
