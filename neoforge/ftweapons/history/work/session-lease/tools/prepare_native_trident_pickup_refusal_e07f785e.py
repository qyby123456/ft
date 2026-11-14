import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
positive_path=work/'reports/native-repair-runtime-trident-foundation-gate-e07f785e.json'
positive=json.loads(positive_path.read_text(encoding='utf-8'))
assert positive['probe']['failures']==0
for name,wanted in positive['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,name
old_source=work/'tests/trident-foundation-gate-e07f785e/PositiveWeapon436.java'
source=old_source.read_text(encoding='utf-8')
block=r"""                Map<String, Object> refusalRow = new LinkedHashMap<>(); row.put("actualPickupRefusalControls", refusalRow);
                Object paidPickup = ((net.farmingtales.ftboss.item.NativePaidWeaponCarrier)physical).ftboss$originalOwnerPaidWeapon();
                var helperCandidate = net.minecraft.world.item.ItemStack.parseOptional(server.registryAccess(), originalBeforePickup);
                int[] pickupCalls = {0};
                boolean helperFalse = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentPickupInventory(physical, player, player.getInventory(), helperCandidate,
                    arguments -> { pickupCalls[0]++; return false; });
                boolean helperTrue = net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentPickupInventory(physical, player, player.getInventory(), helperCandidate,
                    arguments -> { pickupCalls[0]++; return true; });
                RuntimeException inventorySentinel = new IllegalStateException("foreign-pickup-inventory-sentinel");
                boolean inventorySameException = false;
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentPickupInventory(physical, player, player.getInventory(), helperCandidate,
                        arguments -> { pickupCalls[0]++; throw inventorySentinel; });
                } catch (RuntimeException expected) { inventorySameException = expected == inventorySentinel; }
                int[] takenCalls = {0};
                net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentPickupTaken(physical, player, physical, 1,
                    arguments -> { takenCalls[0]++; return null; });
                RuntimeException takenSentinel = new IllegalStateException("foreign-pickup-taken-sentinel");
                boolean takenSameException = false;
                try {
                    net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeTridentPickupTaken(physical, player, physical, 1,
                        arguments -> { takenCalls[0]++; throw takenSentinel; });
                } catch (RuntimeException expected) { takenSameException = expected == takenSentinel; }
                boolean helpersOrdinary = !helperFalse && helperTrue && pickupCalls[0] == 3 && takenCalls[0] == 2
                    && inventorySameException && takenSameException && helperCandidate != actual
                    && originalBeforePickup.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(helperCandidate))
                    && leaseMethod.invoke(null, helperCandidate) == null && generationMethod.invoke(null, helperCandidate) == null
                    && !Boolean.TRUE.equals(read(paidPickup, "pickupPublished")) && read(paidPickup, "pickupInventory") == null;
                refusalRow.put("foreignHelpersDelegatedExactlyOnceWithoutPrivatePublication", helpersOrdinary);
                refusalRow.put("foreignInventoryOriginalExceptionIdentityPreserved", inventorySameException);
                refusalRow.put("foreignTakenOriginalExceptionIdentityPreserved", takenSameException);
                if (!helpersOrdinary) throw new AssertionError("Foreign pickup helper acquired private publication or changed original result/exception");
                net.minecraft.world.item.ItemStack[] fullItems = new net.minecraft.world.item.ItemStack[36];
                CompoundTag[] fullImages = new CompoundTag[36];
                for (int slot = 0; slot < 36; slot++) {
                    if (!player.getInventory().getItem(slot).isEmpty()) throw new AssertionError("Throw did not leave an empty pickup test inventory");
                    fullItems[slot] = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 64);
                    player.getInventory().setItem(slot, fullItems[slot]);
                    fullImages[slot] = net.farmingtales.ftboss.item.NativeFoundationItemData.image(fullItems[slot]);
                }
                for (int fullTick = 0; fullTick < 32; fullTick++) {
                    physical.tick();
                    player.moveTo(physical.getX(), physical.getY(), physical.getZ(), 0.0F, 0.0F);
                    physical.playerTouch(player);
                }
                boolean fullUnchanged = !physical.isRemoved() && originalBeforePickup.equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(actual))
                    && leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired")) && read(paidPickup, "pickupInventory") == null
                    && !Boolean.TRUE.equals(read(paidPickup, "pickupInProgress")) && !Boolean.TRUE.equals(read(paidPickup, "pickupPublished"));
                for (int slot = 0; slot < 36; slot++) fullUnchanged &= player.getInventory().getItem(slot) == fullItems[slot]
                    && fullImages[slot].equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(fullItems[slot]));
                refusalRow.put("fullInventoryOriginalPlayerTouchRefusedWithoutMutation", fullUnchanged);
                if (!fullUnchanged) throw new AssertionError("Full inventory pickup altered private original custody or inventory");
                ServerPlayer otherPicker = new ServerPlayer(server, server.overworld(), new GameProfile(new UUID(621, 94), "ft_other_picker"), ClientInformation.createDefault());
                scratches.add(otherPicker);
                var otherConnection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
                var otherChannel = new io.netty.channel.embedded.EmbeddedChannel(otherConnection); channels.add(otherChannel);
                otherPicker.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, otherConnection, otherPicker,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(otherPicker.getGameProfile(), false));
                ((List<ServerPlayer>)read(server.getPlayerList(), "players")).add(otherPicker);
                ((Map<UUID, ServerPlayer>)read(server.getPlayerList(), "playersByUUID")).put(otherPicker.getUUID(), otherPicker);
                otherPicker.moveTo(physical.getX(), physical.getY(), physical.getZ(), 0.0F, 0.0F);
                physical.playerTouch(otherPicker);
                boolean otherDenied = !physical.isRemoved();
                for (int slot = 0; slot < otherPicker.getInventory().getContainerSize(); slot++) otherDenied &= otherPicker.getInventory().getItem(slot).isEmpty();
                otherDenied &= leaseMethod.invoke(null, actual) == actualLease && generationMethod.invoke(null, actual) == generation
                    && !Boolean.TRUE.equals(read(generation, "retired")) && read(paidPickup, "pickupInventory") == null
                    && !Boolean.TRUE.equals(read(paidPickup, "pickupPublished"));
                refusalRow.put("otherPlayerOriginalTouchDeniedWithoutAuthorityTransfer", otherDenied);
                if (!otherDenied) throw new AssertionError("Other player pickup moved owner private authority");
                for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, net.minecraft.world.item.ItemStack.EMPTY);
"""
anchor='                int physicalTicks = 0;'
assert source.count(anchor)==1
source=source.replace(anchor,block+anchor)
new_source=work/'tests/trident-pickup-refusal-e07f785e/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(),source)
old_launcher=work/'tools/run_native_trident_foundation_gate_e07f785e.py'
launcher=old_launcher.read_text(encoding='utf-8')
launcher=launcher.replace('tests/trident-foundation-gate-e07f785e/PositiveWeapon436.java',new_source.relative_to(work).as_posix())
launcher=launcher.replace("output = WORK / 'reports/native-repair-runtime-trident-foundation-gate-e07f785e.json'", "output = WORK / 'reports/native-repair-runtime-trident-pickup-refusal-e07f785e.json'")
anchor="save(attempt / 'inputs-before308.json', inputs)"
head,tail=launcher.rsplit(anchor,1)
launcher=head+"inputs[str(WORK / 'reports/native-trident-pickup-refusal-preparation-e07f785e.json')] = digest(WORK / 'reports/native-trident-pickup-refusal-preparation-e07f785e.json')\n"+anchor+tail
new_launcher=work/'tools/run_native_trident_pickup_refusal_e07f785e.py'
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,positive_path,old_source,old_launcher,new_source,new_launcher]
output=work/'reports/native-trident-pickup-refusal-preparation-e07f785e.json'
with output.open('x',encoding='utf-8') as stream:
    json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':output.name}))
