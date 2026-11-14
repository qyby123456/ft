import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
helper=work/'tools/prove_native_ranged_use_head_delta.py'
head=work/'reports/native-ranged-use-head-delta-f4f2deae.json'
assert hashlib.sha256(helper.read_bytes()).hexdigest()==json.loads(head.read_text(encoding='utf-8'))['inputHashes'][str(helper)]
exec(compile(helper.read_text(encoding='utf-8').split('before = load(before_path)')[0],str(helper),'exec'))
before_path=work/'reports/native-repair-ranged-use-candidate-529ba4c9.json'
after_path=work/'reports/native-repair-ranged-use-candidate-99c65f41.json'
before=load(before_path);after=load(after_path)
for receipt in [before,after]:
    assert receipt['classCount']==1381 and receipt['effectiveSourceCount']==716 and receipt['guardCount']==208
    assert receipt['failures']==0 and not receipt['newClasses']
    for name,wanted in receipt['inputHashes'].items():assert digest(Path(name))==wanted,name
previous=source_map(before);current=source_map(after)
assert previous.keys()==current.keys()
changed={key for key in previous if previous[key][1]!=current[key][1]}
expected={'net.farmingtales.ftboss.'+suffix for suffix in ['item.ArmorBlessingCatalog','item.FoodBlessingCombat','item.StatueBlessingCatalog','item.StatueBlessingCombat','network.StatueMenuNetwork']}
expected.update(key for key in previous if key.endswith('.StatueHubScreen') or key.endswith('.OrdinaryStatueHubScreen'))
assert len(expected)==7 and changed==expected
prepared_path=work/'reports/native-retired-food-enchantment-preparation.json';prepared=load(prepared_path)
for name,wanted in prepared['inputHashes'].items():assert digest(Path(name))==wanted,name
assert not prepared['vanillaGlobalEnchantmentsChanged'] and not prepared['storedItemDataDeleted'] and not prepared['originalGameplayValuesChanged']
preparer=work/'tools/prepare_native_retired_food_enchantment.py'
namespace={'__file__':str(preparer)}
exec(compile(preparer.read_text(encoding='utf-8').split('changes=[]')[0],str(preparer),'exec'),namespace)
without_method=namespace['without_method'];block_end=namespace['block_end']
for row in prepared['sourceChanges']:
    assert digest(Path(row['source']))==row['beforeSha256']
    assert digest(Path(row['staged']))==row['stagedSha256']
    key=next(key for key in previous if previous[key][0]==Path(row['source']))
    assert current[key][1]==Path(row['staged']).read_text(encoding='utf-8-sig')
for key in expected:
    old=previous[key][1];actual=current[key][1]
    if key.endswith(('.StatueHubScreen','.OrdinaryStatueHubScreen')):
        assert without_method(old,'blessingFoodTooltip')==actual
    elif key.endswith('.StatueMenuNetwork'):
        reconstructed=actual
        for addition in ['\n            if (var1 == 7) return;','\n            if (var0.action == 7) return;','\n            if (action == 7) return;']:
            assert reconstructed.count(addition)==1;reconstructed=reconstructed.replace(addition,'')
        anchor='''                    } else if (var0.action == 9) {'''
        assert reconstructed.count(anchor)==1
        reconstructed=reconstructed.replace(anchor,'''                    } else if (var0.action == 7) {
                        FoodWeaponEvolution.applyFoodBlessingFromUi(var2, var5, var0.value);
'''+anchor)
        anchor='''                        case 8:
                            boolean var4 = BossChallengePreference.select'''
        assert reconstructed.count(anchor)==1
        reconstructed=reconstructed.replace(anchor,'''                        case 7:
                            FoodWeaponEvolution.ftboss$legacyOrdinary$applyFoodBlessingFromUi(var0, var8, var1.value);
                            return;
'''+anchor)
        assert reconstructed==old
    elif key.endswith('.ArmorBlessingCatalog'):
        reconstructed=old
        for name in ['rule','add']:reconstructed=without_method(reconstructed,name)
        first=reconstructed.index('    static {');last=block_end(reconstructed,reconstructed.index('{',first))
        reconstructed=reconstructed[:first]+reconstructed[last:]
        reconstructed=reconstructed.replace('new LinkedHashMap<>()','Map.of()')
        assert reconstructed==actual
    elif key.endswith('.StatueBlessingCatalog'):
        assert actual[actual.index('    public static record Definition('):]==old[old.index('    public static record Definition('):]
        assert 'static final ' not in actual and 'return List.of();' in actual and 'return null;' in actual and 'return false;' in actual
    elif key.endswith('.StatueBlessingCombat'):
        assert actual[actual.index('    private static record Active('):]==old[old.index('    private static record Active('):]
        assert not any(token in actual[actual.index('public final class'):actual.index('    private static record Active(')] for token in ['.write(','.add(','.heal(','.setAmount(','.knockback(','.putLong(','.getList('])
    elif key.endswith('.FoodBlessingCombat'):
        assert 'EquipmentAttributeSlots.attributes(event);' in actual and 'return ticks;' in actual
        assert not any(token in actual[actual.index('public final class'):] for token in ['.getList(','.getString(','.setAmount(','.heal(','.addModifier('])
resource_rows=[]
for row in prepared['resourceChanges']:
    original=Path(row['original']);staged=Path(row['staged'])
    assert digest(original)==row['originalSha256'] and digest(staged)==row['stagedSha256']
    old=load(original);actual=load(staged);removed=set(row['removedKeys'])
    assert len(removed)==85 and set(old)-set(actual)==removed and not (set(actual)-set(old))
    assert all(old[key]==actual[key] for key in actual)
    for key in ['item.ftboss.maque_blessing_feather','tooltip.ftboss.maque_feather.blessing','message.ftboss.maque_feather.blessing']:assert actual[key]==old[key]
    resource_rows.append({'language':original.stem,'removedKeys':85,'everyOtherKeyValueUnchanged':True,'maqueBlessingKeysPreserved':True})
bytecode_path=work/'reports/native-retired-food-bytecode-99c65f41.json';bytecode=load(bytecode_path)
assert bytecode['failures']==0 and bytecode['checks']==13166
for key in ['clientScreensDoNotAdvertiseRetiredFeature','generalEquipmentAttributeDispatcherRetained','noRetiredEffectOrRegistrationImplementations','retiredActionSevenRejectedBeforeNetworkOrContext']:assert bytecode[key]
checks=work/'build/retired-food-closure-99c65f41'
for name in ['compile-terminal.json','check-terminal.json']:assert load(checks/name)['exitCode']==0
for path in [Path(__file__),preparer,work/'tools/build_native_retired_food_enchantment.py',work/'tools/check_native_retired_food_bytecode_99c65f41.py',work/'tests/retired-food-closure/RetiredFoodClosure.java',checks/'RetiredFoodClosure.class']:digest(path)
output=work/'reports/native-retired-food-source-bytecode-delta-99c65f41.json'
result={'failures':0,'candidate':str(after_path),'classes':1381,'sources':716,'guards':208,'changedBehaviorSources':sorted(changed),'unchangedEffectiveSources':709,
 'networkUiAndCatalogueExactIntendedDelta':True,'retiredEffectsRemovedCommonAttributeDispatcherRetained':True,'targetedBytecodeAssertions':13166,
 'resourceRows':resource_rows,'vanillaGlobalEnchantmentImplementationUnchanged':True,'independentEventAndUnrelatedGameplaySourcesUnchanged':True,
 'runtimeAcceptedByThisReceipt':False,'installable':False,'deployed':False,'inputHashes':hashes}
with output.open('x',encoding='utf-8') as stream:json.dump(result,stream,ensure_ascii=False,indent=2)
print(json.dumps({'evidence':output.name,'inputHashes':len(hashes),'unchangedSources':709,'targetedBytecodeAssertions':13166}))
