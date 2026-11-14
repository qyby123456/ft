import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
helper=work/'tools/prove_native_ranged_use_head_delta.py';frozen=work/'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest()==json.loads(frozen.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0],str(helper),'exec'))
before_path=work/'reports/native-repair-ranged-use-candidate-2be7d6c1.json';after_path=work/'reports/native-repair-ranged-use-candidate-3320e085.json'
before=load(before_path);after=load(after_path)
for receipt in [before,after]:
    assert receipt['classCount']==1381 and receipt['effectiveSourceCount']==716 and receipt['guardCount']==208
    assert receipt['failures']==0 and not receipt['newClasses']
    for name,wanted in receipt['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
previous=source_map(before);current=source_map(after);prefix='net.farmingtales.ftboss.item.'
changed={key for key in previous if previous[key][1]!=current[key][1]}
assert changed=={prefix+name for name in ['OwnerNativeSemanticAuthority','NativeItemComponentObservation','NativeCapabilityInstallationProof170','OwnerFieldInstallationProof','NativeStackFieldRequirementProof172']}
key=prefix+'OwnerNativeSemanticAuthority';text=current[key][1]
new="""        if ((charged == null ? !(stack.getItem().getClass() == net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class
                && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(stack)) : !charged.isEmpty())
            || !stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,"""
old='        if (charged == null || !charged.isEmpty() || !stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS,'
assert text.count(new)==1;text=text.replace(new,old);assert text==previous[key][1]
key=prefix+'NativeItemComponentObservation';text=current[key][1]
new="""                boolean originalMaterialAbsence = beforeValue == null && !firing && !this.values.containsKey(type)
                    && this.prototype.get(DataComponents.CHARGED_PROJECTILES) == null
                    && this.item.getClass() == net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem.class
                    && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(this.stack);
                if ((!originalMaterialAbsence && (!(beforeValue instanceof net.minecraft.world.item.component.ChargedProjectiles charged)
                        || charged.isEmpty() == firing))"""
old='                if (!(beforeValue instanceof net.minecraft.world.item.component.ChargedProjectiles charged) || charged.isEmpty() == firing'
assert text.count(new)==1;text=text.replace(new,old);assert text==previous[key][1]
normalize=lambda value:re.sub(r'"[0-9a-f]{64}"','"HASH"',value)
for binding_name in ['NativeCapabilityInstallationProof170','OwnerFieldInstallationProof','NativeStackFieldRequirementProof172']:
    key=prefix+binding_name
    assert normalize(previous[key][1])==normalize(current[key][1]),binding_name
    for name,wanted in re.findall(r'Map.entry\("([^"\n]+)", "([0-9a-f]{64})"\)',current[key][1]):
        path=Path(after['attempt'])/'complete-classes/net/farmingtales/ftboss/item'/(name+'.class')
        if path.is_file():assert digest(path)==wanted,name
baseline=source_map(load(work/'reports/native-repair-ranged-use-candidate-99c65f41.json'))
combined={key for key in current if baseline[key][1]!=current[key][1]}
assert combined=={prefix+name for name in ['OwnerRepairGenerationAuthority','OwnerNativeSemanticAuthority','FoodWeaponSourceLedger','FoodWeaponUseBridge','NativeItemComponentObservation','NativeCapabilityInstallationProof170','OwnerFieldInstallationProof','NativeStackFieldRequirementProof172']}
for field in ['requiredProductionResources','additionalProductionResources']:
    assert before[field].keys()==after[field].keys()
    for name in before[field]:assert digest(Path(before[field][name]))==digest(Path(after[field][name]))
prior_path=work/'reports/native-material-ranged-semantic-source-delta-2be7d6c1.json';prior=load(prior_path)
assert prior['failures']==0
for name,wanted in prior['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
failure_path=work/'reports/native-repair-runtime-material-ranged-crossbow-signed-2be7d6c1.json';failure=load(failure_path);terminal=load(Path(failure['attempt'])/'terminal308.json')
assert failure['exitCode']==0 and terminal['completeSave'] and terminal['stopSent'] and failure['probe']['failures']==1
assert 'Original early release modified or retired unchanged genuine authority' in failure['probe']['failure']
for path in [Path(__file__),helper,frozen]:digest(path)
output=work/'reports/native-material-ranged-absent-component-source-delta-3320e085.json'
report={'failures':0,'changedSourcesFrom2be':sorted(changed),'changedSourcesFrom99':sorted(combined),'unchangedSourcesFrom99':708,'unchangedSourcesFrom2be':711,'newProductClasses':0,'classCount':1381,'guardCount':208,'reuseScope':'Only canonical material Crossbow absence-before-charge handling changes; preserve original absent default, no forced component injection. All other methods byte-text identical. Paid repair/save/load and material Bow behavioral paths unchanged; runtime acceptance still requires actual evidence.','excludedMaterialCrossbowEarlyReleaseFailure':str(failure_path),'originalGameplayValuesChanged':False,'vanillaGlobalEnchantmentRulesChanged':False,'installable':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'combinedBehaviorSourceChanges':5,'hashBindingSourceChanges':3,'unchangedEffectiveSources':708,'hashes':len(hashes),'failures':0}))
