import hashlib
import json
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
mode=sys.argv[1]
assert mode in ['restart','controls']
positive_path=work/'reports/native-repair-runtime-bow-original-use-529ba4c9.json'
positive=json.loads(positive_path.read_text(encoding='utf-8'));assert positive['exitCode']==positive['probe']['failures']==0
terminal_path=Path(positive['attempt'])/'terminal308.json'
terminal=json.loads(terminal_path.read_text(encoding='utf-8'));assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
case=next(case for case in positive['probe']['cases'] if case['id']=='minecraft:bow')
assert case['actualUseStarted'] and case['originalPostCombatSaveReturned'] and case['originalMainHandStillSameBowAfterShot']
for name,wanted in positive['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,name
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=work/'tests/bow-original-use-529ba4c9/PositiveWeapon436.java'
old_launcher=work/'tools/run_native_bow_original_use_529ba4c9.py'
launcher=old_launcher.read_text(encoding='utf-8')
extra=[]
if mode=='restart':
    origin=work/'reports/native-trident-post-pickup-restart-preparation-529ba4c9.json'
    frozen=json.loads(origin.read_text(encoding='utf-8'));restart_source=Path(frozen['newObserver'])
    assert hashlib.sha256(restart_source.read_bytes()).hexdigest()==frozen['inputHashes'][str(restart_source)]
    source=restart_source.read_text(encoding='utf-8').replace('new int[]{4}','new int[]{1}').replace('paidTridentPostPickupContinuationAcrossNewProcess','paidBowPostCombatContinuationAcrossNewProcess')
    assert 'new int[]{1}' in source and 'actual.getDamageValue() != 1' in source
    anchor="seed_receipt = WORK / 'reports/native-repair-runtime-six-family-paid-restart.json'"
    assert launcher.count(anchor)==1
    launcher=launcher.replace(anchor,"seed_receipt = WORK / 'reports/native-repair-runtime-bow-original-use-529ba4c9.json'")
    anchor="assert seed_result['probe']['sixNativeDamageableFamilyRepresentativesGenuinePaidContinuationAcrossNewProcess']\nassert len(seed_result['probe']['cases']) == 6\nassert all(case['actualOriginalGenerationTwoSaveCommitted'] for case in seed_result['probe']['cases'])"
    assert launcher.count(anchor)==1
    launcher=launcher.replace(anchor,"assert seed_result['probe']['failures'] == 0\nassert next(case for case in seed_result['probe']['cases'] if case['id'] == 'minecraft:bow')['originalPostCombatSaveReturned']")
    extra=[origin,restart_source]
else:
    source=old_source.read_text(encoding='utf-8')
    anchor='                player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 2));'
    assert source.count(anchor)==1
    block='''                Map<String, Object> useControls = new LinkedHashMap<>(); row.put("actualUseRefusalControls", useControls);
                CompoundTag beforeRefusal = net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual);
                Object inputBeforeHelpers = ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput();
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseEntered(actual, server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeRangedOriginalUseReturned(actual);
                Object rangedLocal = readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_RANGED_USE");
                boolean foreignUnchanged = ((ThreadLocal<?>)rangedLocal).get() == null
                    && inputBeforeHelpers == ((net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields)(Object)actual).ftboss$ownerNativeWeaponInput()
                    && beforeRefusal.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired"));
                useControls.put("foreignUseCallbacksDoNotMintOrModifyAuthority", foreignUnchanged);
                if (!foreignUnchanged) throw new AssertionError("Foreign original use helpers changed private authority");
                var noAmmoResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
                boolean noAmmoRefused = !noAmmoResult.getResult().consumesAction() && !player.isUsingItem()
                    && beforeRefusal.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired")) && ((ThreadLocal<?>)rangedLocal).get() == null;
                useControls.put("originalNoAmmoUseResult", noAmmoResult.getResult().toString());
                useControls.put("originalNoAmmoUseRefusedWithSameWholeImageAndLiveAuthority", noAmmoRefused);
                if (!noAmmoRefused) throw new AssertionError("Original no-ammo bow use altered paid state or leaked private use scope");
'''
    source=source.replace(anchor,block+anchor)
new_source=work/('tests/bow-original-use-'+mode+'-529ba4c9/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=launcher.replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace("output = WORK / 'reports/native-repair-runtime-bow-original-use-529ba4c9.json'","output = WORK / 'reports/native-repair-runtime-bow-original-use-"+mode+"-529ba4c9.json'")
preparation=work/('reports/native-bow-original-use-'+mode+'-preparation-529ba4c9.json')
anchor="save(attempt / 'inputs-before308.json', inputs)"
head,tail=launcher.rsplit(anchor,1)
launcher=head+"inputs[str(WORK / "+repr(preparation.relative_to(work).as_posix())+")] = digest(WORK / "+repr(preparation.relative_to(work).as_posix())+")\n"+anchor+tail
new_launcher=work/('tools/run_native_bow_original_use_'+mode+'_529ba4c9.py')
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,positive_path,terminal_path,old_source,old_launcher,new_source,new_launcher,*extra]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'mode':mode,'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanges':False,'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'mode':mode}))
