import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
positive_path=work/'reports/native-repair-runtime-trident-foundation-gate-529ba4c9.json'
positive=json.loads(positive_path.read_text(encoding='utf-8'))
assert positive['exitCode']==positive['probe']['failures']==0
for name,wanted in positive['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,name
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=work/'tests/trident-foundation-gate-529ba4c9/PositiveWeapon436.java'
source=old_source.read_text(encoding='utf-8')
assert source.count('if (sample != 4) continue;')==1
source=source.replace('if (sample != 4) continue;','if (sample != 1) continue;')
anchor='                var useResult = actual.use(server.overworld(), player, net.minecraft.world.InteractionHand.MAIN_HAND);'
assert source.count(anchor)==1
source=source.replace(anchor,'                player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW, 2));\n'+anchor)
source=source.replace('entity instanceof net.minecraft.world.entity.projectile.ThrownTrident arrow','entity instanceof net.minecraft.world.entity.projectile.Arrow arrow')
start=source.index('                    net.minecraft.world.item.ItemStack copiedPickup =')
end=source.index('                    for (int ticks =',start)
source=source[:start]+source[end:]
start=source.index('                boolean closedImpactDenied =')
end=source.index('                if (publications.isEmpty()',start)
source=source[:start]+source[end:]
start=source.index('                var physical =')
end=source.index('                for (var projectile : arrows) projectile.discard();',start)
source=source[:start]+'''                boolean originalBowHeld = player.getMainHandItem() == actual && actual.getCount() == 1;
                row.put("originalMainHandStillSameBowAfterShot", originalBowHeld);
                row.put("originalArrowCountAfterShot", player.getInventory().getItem(1).getCount());
                if (!originalBowHeld || player.getInventory().getItem(1).getCount() != 1 || actual.getDamageValue() != wearBefore + 1)
                    throw new AssertionError("Original bow shot changed held weapon or consumed wrong ammo/wear");
                storage.save(player);
                row.put("originalPostCombatSaveReturned", true);
'''+source[end:]
source=source.replace('!"TRIDENT".equals(value.get("nativeEntry"))','!"BOW".equals(value.get("nativeEntry"))')
source=source.replace('Trident','Bow').replace('tridentEntry','bowEntry')
source=source.replace('"nativeBowCertified"','"nativeTridentCertified"')
source=source.replace('Actual paid ItemStack.use -> original Bow releaseUsing/constructor/add/remove inventory -> actual ThrownBow.tick impact; pickup and persistence still unaccepted',
 'Actual paid original ItemStack.use -> early original release -> original ItemStack.use -> full original Bow releaseUsing -> actual Arrow.tick impact and full signed Session')
source=source.replace('row.put("actualBowWholeInstallationCertified", tridentCertificate.invoke(null));','row.put("underlyingAbstractArrowCustodyProofCertified", tridentCertificate.invoke(null));')
assert 'startUsingItem(' not in source and 'ThrownBow' not in source
new_source=work/'tests/bow-original-use-529ba4c9/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(),source)
old_launcher=work/'tools/run_native_trident_foundation_gate_529ba4c9.py'
launcher=old_launcher.read_text(encoding='utf-8').replace('tests/trident-foundation-gate-529ba4c9/PositiveWeapon436.java',new_source.relative_to(work).as_posix())
launcher=launcher.replace("output = WORK / 'reports/native-repair-runtime-trident-foundation-gate-529ba4c9.json'","output = WORK / 'reports/native-repair-runtime-bow-original-use-529ba4c9.json'")
anchor="save(attempt / 'inputs-before308.json', inputs)"
head,tail=launcher.rsplit(anchor,1)
launcher=head+"inputs[str(WORK / 'reports/native-bow-original-use-preparation-529ba4c9.json')] = digest(WORK / 'reports/native-bow-original-use-preparation-529ba4c9.json')\n"+anchor+tail
new_launcher=work/'tools/run_native_bow_original_use_529ba4c9.py'
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,positive_path,old_source,old_launcher,new_source,new_launcher]
output=work/'reports/native-bow-original-use-preparation-529ba4c9.json'
with output.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanges':False,'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':output.name,'startsUsingByOriginalItemStackUse':True}))
