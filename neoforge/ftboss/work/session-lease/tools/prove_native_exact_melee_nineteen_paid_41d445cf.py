import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1];hashes={}
def digest(path):
    value=hashlib.sha256(path.read_bytes()).hexdigest();hashes[str(path)]=value;return value
def load(path):digest(path);return json.loads(path.read_text(encoding='utf-8'))
def frozen(data):
    for name,wanted in data.get('inputHashes',{}).items():assert digest(Path(name))==wanted,Path(name).name
audit=load(work/'reports/native-remaining-variant-gap-audit-3320e085.json');frozen(audit);expected=audit['groups']['otherExactOriginalClassesStillNeedActualMatrix'];assert len(expected)==19
runtime_path=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json';result=load(runtime_path);frozen(result);attempt=Path(result['attempt']);terminal=load(attempt/'terminal308.json')
assert result['exitCode']==terminal['exitCode']==result['probe']['failures']==terminal['probe']['failures']==0
assert terminal['stopSent'] and terminal['completeSave']
probe=result['probe'];assert probe['nineteenExactOriginalMeleeVariantsPaidRepairAndSameProcessReload'] and probe['scratchPlayersRemoved'] and not probe['authorityWrittenByReflection']
assert [row['id'] for row in probe['cases']]==[row['id'] for row in expected]
for original,row in zip(expected,probe['cases']):
    assert row['itemClass']==original['class'] and row['originalMaximumDamage']==original['maxDamage']
    for key in ['originalExactRegisteredClassAndDurabilityPreserved','originalRegisteredProfileStatueAndFoodRulesPreserved','actualPaidClaimAndThreeSupplyConsumptions','actualOriginalSaveCommitted','actualOriginalLoadRestoredNonNullGenuineSuccessor','sameEpochGenerationZeroToOne','oldPhysicalGenerationRetired','consumedReplayDeniedAndFirstSuccessorUnchanged','actualSuccessorOriginalSaveCommitted','nonNullLease','repairGenerationPresent']:assert row[key],(row['id'],key)
    assert row['damageAfterRepair']==0 and len(row['repairTransactions'])==5
    for index,(transaction,payment) in enumerate(zip(row['repairTransactions'],[0,3,3,2,0])):
        assert transaction['expectedPayment']==payment and transaction['remainingSupply']==0
        if 'accepted' in transaction:assert transaction['accepted'] and transaction['code']=='COMMITTED'
        else:assert transaction['signedSequence']==index+1 and transaction['signedStatus']==('LEASE_ISSUED' if index==4 else 'COMMITTED')
    if not row['originalFoundationApplicable']:assert row['originalPreClaimTaskIdentityUsedForSignedTerminalCheck']
for name in ['runtime308.log','live-process308.json','server/actual-storage-view308.json','compile-command308.json','compile308.log','restart-world-seed.json']:digest(attempt/name)
world=attempt/'server/world'
for path in [world/'level.dat',world/'data/ftboss_native_weapon_wear.dat',*sorted((world/'ftboss-authority').rglob('*.bin')),*sorted((world/'playerdata').glob('*.dat'))]:digest(path)
excluded=[]
for suffix,count in [('paid-repair',1),('paid-repair-qualified',14),('paid-repair-profile-qualified',14)]:
    path=work/('reports/native-repair-runtime-exact-melee-variants-'+suffix+'-41d445cf.json');data=load(path);frozen(data);end=load(Path(data['attempt'])/'terminal308.json')
    assert data['exitCode']==0 and data['probe']['failures']==1 and len(data['probe']['cases'])==count and end['completeSave'] and end['stopSent'];excluded.append(str(path))
root_proof=load(work/'reports/native-shared-repair-owner-growth-source-delta-41d445cf.json');frozen(root_proof);assert root_proof['failures']==root_proof['newProductClasses']==0 and root_proof['classCount']==1381 and root_proof['guardCount']==208
for path in [Path(__file__),root/'WRITE_OWNER.claim']:digest(path)
output=work/'reports/native-exact-melee-nineteen-paid-repair-focused-evidence-41d445cf.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'failures':0,'actualRegisteredItems':[row['id'] for row in expected],'acceptedRuntime':str(runtime_path),'genuineOriginalPayment':[3,3,2],'originalProfileStatueAndFoodRulesPreserved':True,'originalClassIdentityAndDurabilityPreserved':True,'actualGenuineNonNullLeaseSaveLoadZeroToOneAndConsumedReplayDenied':True,'excludedAttempts':excluded,'productChanged':False,'fullSignedCombatAcceptedByThisReceipt':False,'authenticatedMultiplayerAccepted':False,'installable':False,'deployed':False,'inputHashes':hashes},stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'actualPaidCases':19,'hashes':len(hashes),'failures':0}))
