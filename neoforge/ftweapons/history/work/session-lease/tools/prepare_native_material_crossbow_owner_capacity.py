import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
product=json.loads((work/'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'));tag=Path(product['attempt']).name.removeprefix('native-repair-')
parent=work/('reports/native-material-ranged-crossbow-signed-preparation-'+tag+'.json');frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='            for (int sample : new int[]{6, 7, 8, 9, 10, 11}) {';assert source.count(anchor)==1
source=source.replace(anchor,"""            List<net.minecraft.world.item.ItemStack> liveOriginals = new ArrayList<>();
            var nodeMethod = guard.getDeclaredMethod("node", Object.class, boolean.class); nodeMethod.setAccessible(true);
            var recordMethod = guard.getDeclaredMethod("record", Object.class, boolean.class); recordMethod.setAccessible(true);
            for (int sample : new int[]{6, 7, 8, 9, 10, 11}) {""")
start=source.index('                for (var arrow : arrows) {')
end=source.index('                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).remove(player);',start)
source=source[:start]+"""                if (arrows.size() != 1 || Boolean.TRUE.equals(read(generation, "retired"))
                    || leaseMethod.invoke(null, actual) != actualLease || generationMethod.invoke(null, actual) != generation)
                    throw new AssertionError("Original sequential charge/fire lost genuine authority for " + id);
                CompoundTag signed = net.farmingtales.ftboss.item.NativeVanillaImpact.snapshot(arrows.get(0));
                if (signed.isEmpty() || !net.farmingtales.ftboss.item.FoodWeaponSourceLedger.validReadOnly(signed, player)
                    || !signed.hasUUID("OwnerRepairAuthorityEpoch") || signed.getString("ServerSignature").length() != 64)
                    throw new AssertionError("Original sequential Crossbow factory lacks paid signed carrier for " + id);
                liveOriginals.add(actual);
                Object sharedNode = nodeMethod.invoke(null, net.minecraft.world.item.component.ChargedProjectiles.EMPTY, false);
                if (sharedNode == null) throw new AssertionError("Actual shared EMPTY revocation node absent");
                Object[] owners = (Object[])read(sharedNode, "owners");
                int liveOwners = 0;
                for (Object reference : owners) {
                    Object record = reference == null ? null : ((java.lang.ref.Reference<?>)reference).get();
                    if (record == null) continue;
                    Object ownerActual = ((java.lang.ref.Reference<?>)read(record, "actual")).get();
                    if (ownerActual != null && recordMethod.invoke(null, ownerActual, false) == record) liveOwners++;
                }
                row.put("actualSharedEmptyOwnerSlotCapacity", owners.length);
                row.put("actualSharedEmptyCurrentOwnerCount", liveOwners);
                row.put("actualOriginalFactoryCarrierPaidAndValid", true);
                row.put("genuineGenerationLiveAfterOriginalSequentialFire", !Boolean.TRUE.equals(read(generation, "retired")));
                row.put("actualDamageAfterOriginalFire", actual.getDamageValue());
                if (liveOwners < liveOriginals.size() || (liveOriginals.size() > 4 && owners.length <= 4))
                    throw new AssertionError("Shared original EMPTY did not retain every actual owner beyond four slots");
                for (var original : liveOriginals)
                    if (leaseMethod.invoke(null, original) == null || Boolean.TRUE.equals(read(generationMethod.invoke(null, original), "retired")))
                        throw new AssertionError("Earlier genuine owner was lost during capacity growth");
                storage.save(player);
                row.put("actualOriginalPostFactorySaveReturned", true);
                for (var arrow : arrows) arrow.discard();
                target.discard();
"""+source[end:]
anchor='            report.put("failures", 0);';assert source.count(anchor)==1
source=source.replace(anchor,"""            List<CompoundTag> imagesBeforeNotice = new ArrayList<>();
            for (var original : liveOriginals) imagesBeforeNotice.add(net.farmingtales.ftboss.item.NativeFoundationItemData.image(original));
            net.farmingtales.ftboss.item.OwnerFullRepairGuard.changed(net.minecraft.world.item.component.ChargedProjectiles.EMPTY, 21);
            for (int index = 0; index < liveOriginals.size(); index++) {
                var original = liveOriginals.get(index);
                if (!Boolean.TRUE.equals(read(generationMethod.invoke(null, original), "retired"))
                    || !imagesBeforeNotice.get(index).equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(original)))
                    throw new AssertionError("Foreign public shared-node mutation notice did not retire every owner without changing items");
            }
            report.put("allSixActualOriginalCrossbowFactoriesBeyondFourSharedOwners", true);
            report.put("foreignPublicSharedNodeMutationNoticeRetiredAllSixWithoutItemMutation", true);
            report.put("originalPhysicalMutationActuallyPerformedByNoticeControl", false);
            report.put("fullSignedImpactPerformedByCapacityFixture", false);
            report.put("failures", 0);""")
source=source.replace('Actual original ItemStack.use charge (including unchanged early release), original releaseUsing, then original ItemStack.use fire and full signed damage Session','Actual6 original charge/fire factories with shared EMPTY owner growth and foreign public mutation notice fanout; no actual projectile impact/full signed damage Session executed by this fixture')
new_source=work/('tests/material-ranged-crossbow-owner-capacity-'+tag+'/PositiveWeapon436.java');namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-crossbow-signed-'+tag+'.json','native-repair-runtime-material-ranged-crossbow-owner-capacity-'+tag+'.json')
preparation=work/('reports/native-material-ranged-crossbow-owner-capacity-preparation-'+tag+'.json');launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_material_ranged_crossbow_owner_capacity_'+tag+'.py');namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'actualCases':6,'actualImpactPerformed':False,'foreignPublicMutationNoticeOnly':True,'privateAuthorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualFactories':6,'allOwnerFanoutRefusalControl':True,'signedImpactClaim':False}))
