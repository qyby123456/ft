package net.farmingtales.ftboss.transaction;

import java.util.UUID;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Reply;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WeaponUiSnapshot extends TransactionSnapshotData {
    public WeaponUiSnapshot(UUID request, CompoundTag data) {
        super(request, data);
    }

    public static WeaponUiSnapshot read(ServerPlayer var0, Reply var1) {
        Snapshot var2 = var1.snapshot();
        if (var2 == null) {
            return null;
        } else {
            ItemStack var3 = var0.getInventory().getItem(var2.binding().inventorySlot());
            if (var2.binding().weapon().equals(FoodWeaponLifecycle.weaponId(var3))
                && var2.version().fingerprint().equals(ForgeTransactionHost.fingerprint(var3, var0.registryAccess()))) {
                FourWeaponFoundationAdapter.View var4 = FourWeaponFoundationAdapter.read(var3);
                if (!var4.valid()) {
                    return null;
                } else {
                    CompoundTag var5 = new CompoundTag();
                    var5.putUUID("Session", var2.session());
                    var5.putUUID("Weapon", var2.binding().weapon());
                    var5.putLong("Sequence", var2.version().sequence());
                    var5.putInt("GridRevision", var4.revision());
                    var5.putInt("Level", var4.level());
                    var5.putString("Family", var4.profile().family());
                    var5.putString("Fingerprint", var2.version().fingerprint());
                    var5.putString("Rules", var2.version().rulesFingerprint());
                    var5.putBoolean("Usable", var4.status().ready());
                    var5.putIntArray("Layout", SwordCavityExtensions.layout(var3));
                    ListTag var6 = new ListTag();

                    for (int var7 = 0; var7 < 5; var7++) {
                        CompoundTag var8 = new CompoundTag();
                        var8.putString("Id", var4.profile().axes().get(var7));
                        var8.putDouble("Total", var4.status().total().at(var7));
                        var8.putDouble("Position", var4.status().position().at(var7));
                        var8.putDouble("Material", var4.status().material().at(var7));
                        var8.putDouble("Minimum", var4.profile().minimum().at(var7));
                        var6.add(var8);
                    }

                    var5.put("Axes", var6);
                    ListTag var17 = new ListTag();

                    for (int var18 = 0; var18 < 27; var18++) {
                        CompoundTag var9 = new CompoundTag();
                        var9.putInt("Slot", var18);
                        var9.putBoolean("Unlocked", FourWeaponFoundationAdapter.cellUnlocked(var3, var18));
                        FourWeaponFoundationAdapter.Entry var10 = var4.entries().get(var18);
                        var9.putBoolean("Paid", var10 != null && var10.paid());
                        var9.putBoolean("Native", var10 != null && !var10.paid());
                        if (var10 != null) {
                            var9.putString("ItemId", var10.value().itemId());
                            var9.putString("Material", var10.value().material().id());
                            if (var10.paid()) {
                                var9.put("Item", var10.payload());
                            }
                        }

                        var17.add(var9);
                    }

                    var5.put("Cells", var17);
                    ListTag var19 = new ListTag();

                    for (int var20 = 0; var20 < 36; var20++) {
                        ItemStack var22 = var0.getInventory().getItem(var20);
                        if (!var22.isEmpty() && var22 != var3) {
                            CompoundTag var11 = new CompoundTag();
                            var11.putInt("Slot", var20);
                            var11.putString("ItemId", FourWeaponFoundationAdapter.registryId(var22));
                            var11.putString("Fingerprint", ForgeTransactionHost.fingerprint(var22, var0.registryAccess()));
                            var11.putInt("Count", var22.getCount());
                            var19.add(var11);
                        }
                    }

                    var5.put("Supplies", var19);
                    ListTag var21 = new ListTag();

                    for (WeaponDetailView var25 : FoodWeaponSkillLoadout.skills(var3)) {
                        CompoundTag var12 = new CompoundTag();
                        var12.putInt("Index", var25.gourmetSkillIndex());
                        var12.putBoolean("Selected", var25.selected());
                        var12.putString("Title", Serializer.toJson(var25.title(), var0.registryAccess()));
                        var12.putString("Description", Serializer.toJson(var25.description(), var0.registryAccess()));
                        var21.add(var12);
                    }

                    var5.put("Skills", var21);
                    int var24 = Integer.parseInt(var2.binding().statueIdentity().substring(var2.binding().statueIdentity().lastIndexOf(58) + 1));
                    FoodWeaponRepair.View var26 = FoodWeaponRepair.view(var3, var24);
                    CompoundTag var27 = new CompoundTag();
                    var27.putBoolean("Eligible", var26.eligible());
                    var27.putBoolean("Active", var26.active());
                    var27.putBoolean("Ready", var26.ready());
                    var27.putBoolean("CorrectStatue", var26.correctStatue());
                    ListTag var13 = new ListTag();

                    for (FoodWeaponRepair.TaskView var15 : var26.tasks()) {
                        CompoundTag var16 = new CompoundTag();
                        var16.putInt("Index", var15.index());
                        var16.putInt("Progress", var15.progress());
                        var16.putInt("Required", var15.required());
                        var16.putString("Name", Serializer.toJson(var15.name(), var0.registryAccess()));
                        var13.add(var16);
                    }

                    var27.put("Tasks", var13);
                    var5.put("Repair", var27);
                    var5.putInt("Container", var0.containerMenu.containerId);
                    var5.put("Cursor", var0.containerMenu.getCarried().saveOptional(var0.registryAccess()));
                    return new WeaponUiSnapshot(var1.request(), var5);
                }
            } else {
                return null;
            }
        }
    }
}
