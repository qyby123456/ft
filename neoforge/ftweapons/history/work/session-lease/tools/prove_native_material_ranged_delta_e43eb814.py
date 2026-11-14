import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
helper=work/'tools/prove_native_ranged_use_head_delta.py'
frozen=work/'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest()==json.loads(frozen.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0],str(helper),'exec'))
before_path=work/'reports/native-repair-ranged-use-candidate-99c65f41.json'
after_path=work/'reports/native-repair-ranged-use-candidate-e43eb814.json'
before=load(before_path);after=load(after_path)
for receipt in [before,after]:
    assert receipt['classCount']==1381 and receipt['effectiveSourceCount']==716 and receipt['guardCount']==208
    assert receipt['failures']==0 and not receipt['newClasses']
    for name,wanted in receipt['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
previous=source_map(before);current=source_map(after)
changed={key for key in previous if previous[key][1]!=current[key][1]}
prefix='net.farmingtales.ftboss.item.'
assert changed=={prefix+name for name in ['OwnerRepairGenerationAuthority','FoodWeaponSourceLedger','NativeCapabilityInstallationProof170']}
key=prefix+'OwnerRepairGenerationAuthority';text=current[key][1]
start=text.index('    static boolean nativeMaterialRangedPolicy(');end=text.index('    private static boolean nativePolicyCurrent(',start)
bounded=text[start:end];text=text[:start]+text[end:]
start=text.index('    private static boolean nativePolicyCurrent(');end=text.index('    private static boolean knownStackPolicy(',start)
old=previous[key][1];old_start=old.index('    private static boolean nativePolicyCurrent(');old_end=old.index('    private static boolean knownStackPolicy(',old_start)
text=text[:start]+old[old_start:old_end]+text[end:]
text=text.replace(' || nativeMaterialRangedPolicy(stack)','');assert text==old
key=prefix+'FoodWeaponSourceLedger';text=current[key][1]
for family in ['Bow','Crossbow']:
    addition='\n            || var0.getClass() == net.farmingtales.ftboss.item.weapons.Material'+family+'Item.class && OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(var1)'
    assert text.count(addition)==1;text=text.replace(addition,'')
addition='\n            || OwnerRepairGenerationAuthority.nativeMaterialRangedPolicy(stack)';assert text.count(addition)==1;text=text.replace(addition,'')
assert text==previous[key][1]
key=prefix+'NativeCapabilityInstallationProof170'
normalize=lambda text: re.sub(r'"[0-9a-f]{64}"','"HASH"',text)
assert normalize(previous[key][1])==normalize(current[key][1])
pins=re.findall(r'Map.entry\("([^"\n]+)", "([0-9a-f]{64})"\)',current[key][1])
assert pins
for name,wanted in pins:
    path=Path(after['attempt'])/'complete-classes/net/farmingtales/ftboss/item'/(name+'.class')
    if path.is_file():assert digest(path)==wanted,name
resources={}
for field in ['requiredProductionResources','additionalProductionResources']:
    assert before[field].keys()==after[field].keys()
    for name in before[field]:
        assert digest(Path(before[field][name]))==digest(Path(after[field][name])),name
        resources[name]=digest(Path(after[field][name]))
original=work/'build/repair-runtime-d3c3c4e8/ftboss-unpackaged-proposal/net/farmingtales/ftboss/item/weapons'
for name in ['MaterialBowItem','MaterialCrossbowItem']:
    assert digest(original/(name+'.class')) in bounded
assert 'type.getMethod("use"' in bounded and 'type.getMethod("releaseUsing"' in bounded
assert 'BuiltInRegistries.ITEM.getOptional(identity).orElse(null) != item' in bounded
assert 'if (expected.tier != expectedTier) return false;' in bounded
for name in ['wooden','stone','iron','golden','diamond','netherite']:assert 'case "'+name+'"' in bounded
output=work/'reports/native-material-ranged-source-bytecode-delta-e43eb814.json'
for path in [Path(__file__),helper,frozen]:digest(path)
report={'failures':0,'changedEffectiveSources':sorted(changed),'behaviorSourceChanges':2,'hashBindingSourceChanges':1,'unchangedEffectiveSources':713,'classCount':1381,'guardCount':208,'newProductClasses':0,'resourcesByteIdentical':resources,'policyScope':'Only exact final original material Bow/Crossbow classes, canonical12 registry objects, immutable kind/tier and pinned original classfiles; inherited original use/release verified each admission. Existing native provider capture/liveness unchanged.','otherAuthorityAndLedgerWordingUnchanged':True,'vanillaGlobalEnchantmentsChanged':False,'originalGameplayValuesChanged':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'report':output.name,'changedSources':3,'unchangedSources':713,'hashes':len(hashes),'failures':0}))
