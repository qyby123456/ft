import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
helper=work/'tools/prove_native_ranged_use_head_delta.py';frozen=work/'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest()==json.loads(frozen.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0],str(helper),'exec'))
before_path=work/'reports/native-repair-ranged-use-candidate-3320e085.json';after_path=work/'reports/native-repair-ranged-use-candidate-41d445cf.json'
before=load(before_path);after=load(after_path)
for receipt in [before,after]:
    assert receipt['classCount']==1381 and receipt['effectiveSourceCount']==716 and receipt['guardCount']==208
    assert receipt['failures']==0 and not receipt['newClasses']
    for name,wanted in receipt['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
previous=source_map(before);current=source_map(after);prefix='net.farmingtales.ftboss.item.'
changed={key for key in previous if previous[key][1]!=current[key][1]}
assert changed=={prefix+name for name in ['OwnerFullRepairGuard','NativeCapabilityInstallationProof170']},changed
key=prefix+'OwnerFullRepairGuard';text=current[key][1];old=previous[key][1]
start=text.index('    private static int availableOwnerSlot(');end=text.index('    private static void associate(',start);growth=text[start:end];text=text[:start]+text[end:]
assert 'Thread.holdsLock(OwnerFullRepairGuard.class)' in growth
assert 'record(owner.actual.get(), false) != owner' in growth and 'owner.actual.get() == actual' in growth
assert 'if (previousSize >= 65536) return -1;' in growth and 'Math.min(65536, previousSize * 2)' in growth
start=text.index('                    int ownerSlot = availableOwnerSlot(var9, var0);');end=text.index('                }\n\n                WeakReference var14',start)
old_start=old.index('                    int var10 = -1;');old_end=old.index('                }\n\n                WeakReference var14',old_start)
text=text[:start]+old[old_start:old_end]+text[end:]
addition='                    if (var5 < 0) var5 = availableOwnerSlot(var4, var1.actual.get());\n';assert text.count(addition)==1;text=text.replace(addition,'')
new='private WeakReference<OwnerFullRepairGuard.Record>[] owners = new WeakReference[4];';assert text.count(new)==1;text=text.replace(new,'private final WeakReference<OwnerFullRepairGuard.Record>[] owners = new WeakReference[4];')
assert text==old
normalize=lambda value:re.sub(r'"[0-9a-f]{64}"','"HASH"',value)
for binding_name in ['NativeCapabilityInstallationProof170','OwnerFieldInstallationProof','NativeStackFieldRequirementProof172']:
    key=prefix+binding_name;assert normalize(previous[key][1])==normalize(current[key][1]),binding_name
    for name,wanted in re.findall(r'Map.entry\("([^"\n]+)", "([0-9a-f]{64})"\)',current[key][1]):
        path=Path(after['attempt'])/'complete-classes/net/farmingtales/ftboss/item'/(name+'.class')
        if path.is_file():assert digest(path)==wanted,name
baseline=source_map(load(work/'reports/native-repair-ranged-use-candidate-99c65f41.json'))
combined={key for key in current if baseline[key][1]!=current[key][1]}
assert len(combined)==9 and prefix+'OwnerFullRepairGuard' in combined
for field in ['requiredProductionResources','additionalProductionResources']:
    assert before[field].keys()==after[field].keys()
    for name in before[field]:assert digest(Path(before[field][name]))==digest(Path(after[field][name]))
prior_path=work/'reports/native-material-ranged-absent-component-source-delta-3320e085.json';prior=load(prior_path);assert prior['failures']==0
for name,wanted in prior['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
failed=load(work/'reports/native-repair-runtime-material-ranged-crossbow-signed-3320e085.json');end=load(Path(failed['attempt'])/'terminal308.json')
assert failed['exitCode']==0 and failed['probe']['failures']==1 and end['completeSave'] and end['stopSent']
assert len(failed['probe']['cases'])==5 and all(row['actualOriginalPostCombatSaveAuthenticatedCommitted'] for row in failed['probe']['cases'][:4])
assert failed['probe']['cases'][-1]['generationRetiredAfterFire']
diagnostic_path=work/'reports/native-repair-runtime-material-ranged-crossbow-diamond-diagnostic-17e0dabe.json';diagnostic=load(diagnostic_path);end=load(Path(diagnostic['attempt'])/'terminal308.json')
assert diagnostic['exitCode']==0 and diagnostic['probe']['failures']==0 and end['completeSave'] and end['stopSent']
assert len(diagnostic['probe']['cases'])==1 and diagnostic['probe']['cases'][0]['id']=='ftboss:diamond_crossbow'
assert not diagnostic['probe']['cases'][0]['generationRetiredAfterFire']
for path in [Path(__file__),helper,frozen]:digest(path)
output=work/'reports/native-shared-repair-owner-growth-source-delta-41d445cf.json'
report={'failures':0,'changedSourcesFrom3320':sorted(changed),'changedSourcesFrom99':sorted(combined),'unchangedSourcesFrom3320':714,'unchangedSourcesFrom99':707,'newProductClasses':0,'classCount':1381,'guardCount':208,'ownerArrayInitialCapacity':4,'ownerArrayMaximumCapacity':65536,'growthRequiresOriginalClassMonitor':True,'allMutableSharedNodesStillObserved':True,'existingOwnerRecordsNotEvictedOrReset':True,'oldRefusalAtMaximumKept':True,'temporaryDiagnosticLoggingRemoved':True,'originalSixCrossbowSignedMatrixAcceptedByThisReceipt':False,'actualSharedOwnerCapacityRuntimeStillRequired':True,'installable':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'behaviorSourceChanges':1,'hashBindingSources':1,'unchangedEffectiveSources':707,'hashes':len(hashes),'failures':0}))
