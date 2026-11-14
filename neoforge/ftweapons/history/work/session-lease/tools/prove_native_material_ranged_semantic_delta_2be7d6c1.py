import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
helper=work/'tools/prove_native_ranged_use_head_delta.py';frozen=work/'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest()==json.loads(frozen.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0],str(helper),'exec'))
before_path=work/'reports/native-repair-ranged-use-candidate-e43eb814.json';after_path=work/'reports/native-repair-ranged-use-candidate-2be7d6c1.json'
before=load(before_path);after=load(after_path)
for receipt in [before,after]:
    assert receipt['classCount']==1381 and receipt['effectiveSourceCount']==716 and receipt['guardCount']==208
    assert receipt['failures']==0 and not receipt['newClasses']
    for name,wanted in receipt['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
previous=source_map(before);current=source_map(after);prefix='net.farmingtales.ftboss.item.'
changed={key for key in previous if previous[key][1]!=current[key][1]}
assert changed=={prefix+name for name in ['OwnerNativeSemanticAuthority','FoodWeaponUseBridge','NativeCapabilityInstallationProof170']}
key=prefix+'FoodWeaponUseBridge';text=current[key][1]
addition='\n                || var0.getClass() == MaterialCrossbowItem.class && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(var1)';assert text.count(addition)==1;text=text.replace(addition,'')
addition='\n                && !(var0.getClass() == MaterialBowItem.class && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(var1))';assert text.count(addition)==1;text=text.replace(addition,'')
assert text==previous[key][1]
key=prefix+'OwnerNativeSemanticAuthority';text=current[key][1]
addition='\n                && !OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(stack)';assert text.count(addition)==1;text=text.replace(addition,'')
addition='\n                && !(stack.getItem().getClass() == net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class\n                    && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(stack))';assert text.count(addition)==1;text=text.replace(addition,'')
old='if ((stack.getItem() == net.minecraft.world.item.Items.CROSSBOW\n                || stack.getItem().getClass() == net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class\n                    && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(stack))'
assert text.count(old)==1;text=text.replace(old,'if (stack.getItem() == net.minecraft.world.item.Items.CROSSBOW')
assert text==previous[key][1]
key=prefix+'NativeCapabilityInstallationProof170';normalize=lambda value:re.sub(r'"[0-9a-f]{64}"','"HASH"',value)
assert normalize(previous[key][1])==normalize(current[key][1])
for name,wanted in re.findall(r'Map.entry\("([^"\n]+)", "([0-9a-f]{64})"\)',current[key][1]):
    path=Path(after['attempt'])/'complete-classes/net/farmingtales/ftboss/item'/(name+'.class')
    if path.is_file():assert digest(path)==wanted,name
baseline=source_map(load(work/'reports/native-repair-ranged-use-candidate-99c65f41.json'))
combined={key for key in current if baseline[key][1]!=current[key][1]}
assert combined=={prefix+name for name in ['OwnerRepairGenerationAuthority','OwnerNativeSemanticAuthority','FoodWeaponSourceLedger','FoodWeaponUseBridge','NativeCapabilityInstallationProof170']}
for field in ['requiredProductionResources','additionalProductionResources']:
    assert before[field].keys()==after[field].keys()
    for name in before[field]:assert digest(Path(before[field][name]))==digest(Path(after[field][name]))
paid_path=work/'reports/native-material-ranged-paid-repair-focused-evidence-e43eb814.json';paid=load(paid_path)
assert paid['failures']==0 and paid['actualMaterialCases']==12
for name,wanted in paid['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
failure_path=work/'reports/native-repair-runtime-material-ranged-bow-signed-e43eb814.json';failure=load(failure_path);terminal=load(Path(failure['attempt'])/'terminal308.json')
assert failure['exitCode']==0 and terminal['completeSave'] and terminal['stopSent'] and failure['probe']['failures']==1
assert 'Original early Bow release lost paid state' in failure['probe']['failure']
for path in [Path(__file__),helper,frozen]:digest(path)
output=work/'reports/native-material-ranged-semantic-source-delta-2be7d6c1.json'
report={'failures':0,'changedSourcesFromE43':sorted(changed),'changedSourcesFrom99':sorted(combined),'unchangedSourcesFrom99':711,'unchangedSourcesFromE43':713,'newProductClasses':0,'classCount':1381,'guardCount':208,'paymentAndSaveReloadProofReused':str(paid_path),'reuseScope':'Paid repair object policy/provider/payment/save/load unchanged; only bounded material semantic operation and original use/release bridge admission added. New current-candidate combat/restart still requires actual runtimes.','excludedEarlyReleaseFailure':str(failure_path),'originalGameplayValuesChanged':False,'vanillaGlobalEnchantmentRulesChanged':False,'installable':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'combinedBehaviorSourceChanges':4,'unchangedEffectiveSources':711,'hashes':len(hashes),'failures':0}))
