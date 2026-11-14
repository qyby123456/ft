package net.farmingtales.ftboss.item;

import java.util.TreeSet;
import java.util.UUID;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

final class OwnerStatueTaskPlan {
    private OwnerStatueTaskPlan() {
    }

    static CompoundTag legacyQuest(ItemStack var0) {
        if (var0.getCount() != 1
            || FoodWeaponLifecycle.weaponId(var0) == null
            || !FoodWeaponLifecycle.managed(var0)
            || !FoodWeaponRepair.view(var0, 0).active()) {
            throw new IllegalArgumentException("No valid original legacy task");
        } else if (FourWeaponFoundationAdapter.applies(var0) && !FourWeaponFoundationAdapter.read(var0).valid()) {
            throw new IllegalArgumentException("Invalid original Foundation task");
        } else {
            return FoodWeaponData.read(var0, "FTBossWeaponLifecycle").getCompound("RepairQuest").copy();
        }
    }

    static String rules(ItemStack var0) {
        CompoundTag var1 = new CompoundTag();
        var1.putInt("TaskVersion", 1);
        var1.putInt("Level", FoodWeaponEvolution.evolutionLevel(var0));
        var1.putString("Profile", FoodWeaponLifecycle.repairProfile(var0));
        int[] var2 = new int[3];

        for (int var3 = 0; var3 < 3; var3++) {
            var2[var3] = FoodWeaponLifecycleRules.repairRequired(var1.getInt("Level"), var3);
        }

        var1.putIntArray("Required", var2);
        if (FourWeaponFoundationAdapter.applies(var0)) {
            FourWeaponFoundationAdapter.View var4 = FourWeaponFoundationAdapter.read(var0);
            if (!var4.valid()) {
                throw new IllegalArgumentException("Invalid Foundation task rules");
            }

            var1.putString("Foundation", foundationRules(var4));
        }

        return FoundationCanonicalDigest.fingerprint("repair-task-rules/v1", var1);
    }

    static CompoundTag after(CompoundTag var0, Operation var1, int var2, int var3, int var4, HolderLookup.Provider registries) {
        CompoundTag var5 = var0.copy();
        if (var5.contains("ForgeCaps")) {
            throw new IllegalArgumentException("Task plan capability image unsupported");
        } else {
            ItemStack var6 = NativeRepairStackImage.decode(registries, var5.copy());
            CompoundTag var7 = legacyQuest(var6);
            if (!FoodWeaponLifecycle.repairProfile(var6).equals(FoodWeaponEvolution.profileId(var4))) {
                throw new IllegalArgumentException("Task plan wrong actual statue");
            } else {
                CompoundTag var8 = NativeRepairStackImage.data(var5);
                CompoundTag var9 = var8.getCompound("FTBossWeaponLifecycle");
                if (var1 == Operation.SUBMIT_REPAIR) {
                    int[] var10 = var7.getIntArray("Progress");
                    if (var2 < 0 || var2 >= 3 || var3 <= 0 || var3 > FoodWeaponLifecycleRules.repairRequired(var7.getInt("Level"), var2) - var10[var2]) {
                        throw new IllegalArgumentException("Task plan original payment quantity");
                    }

                    var10[var2] += var3;
                    var9.getCompound("RepairQuest").putIntArray("Progress", var10);
                } else {
                    if (var1 != Operation.CLAIM_REPAIR) {
                        throw new IllegalArgumentException("Only existing migration-task submit and claim are implemented");
                    }

                    if (var3 != 0 || !FoodWeaponLifecycleRules.ready(var7.getIntArray("Progress"), var7.getInt("Level"))) {
                        throw new IllegalArgumentException("Task plan not ready");
                    }

                    var9.remove("RepairQuest");
                    if (FourWeaponFoundationAdapter.applies(var6)) {
                        foundationReset(var5, registries);
                    }

                    if (FoodWeaponEvolution.evolutionLevel(NativeRepairStackImage.decode(registries, var5.copy())) > 0) {
                        var9.putBoolean("WasEvolved", true);
                    }

                    if (var9.getString("RepairProfile").isEmpty()) {
                        var9.putString("RepairProfile", NativeFoodWeaponEvolutionRead.repairProfileId(var6));
                    }

                    NativeRepairStackImage.putDamage(var5, 0);
                    var9.remove("Broken");
                    var9.remove("PendingMaterials");
                    var9.remove("PendingSockets");
                    var9.putInt("WearStep", 0);
                    if (var8.contains("FTBossWeaponMaterials", 10)) {
                        CompoundTag var11 = var8.getCompound("FTBossWeaponMaterials");
                        if (FourWeaponFoundationAdapter.applies(var6)) {
                            var11.remove("WearCarry");
                        } else {
                            var11.putDouble("WearCarry", 0.0);
                        }
                    }
                }

                int var12 = Math.max(0, var9.getInt("RepairRevision"));
                var9.putInt("RepairRevision", var12 == Integer.MAX_VALUE ? 0 : var12 + 1);
                NativeRepairStackImage.normalize(registries, var5);
                FoundationCanonicalDigest.canonical(var5);
                return var5;
            }
        }
    }

    static CompoundTag start(CompoundTag var0, int var1, String var2, int var3, int var4, HolderLookup.Provider registries) {
        if (!var0.contains("ForgeCaps") && var1 >= 0 && var1 <= 4 && var3 > 0 && !var2.isEmpty() && var2.equals(FoodWeaponEvolution.profileId(var4))) {
            CompoundTag var5 = var0.copy();
            CompoundTag var6 = NativeRepairStackImage.data(var5);
            CompoundTag var7 = var6.getCompound("FTBossWeaponLifecycle");
            if (var7.hasUUID("Weapon")
                && var7.contains("WearStep", 3)
                && !var7.contains("RepairQuest")
                && var2.equals(var7.getString("RepairProfile"))
                && NativeRepairStackImage.damage(var5) > 0) {
                if (var1 > 0) {
                    var7.putBoolean("WasEvolved", true);
                }

                if (NativeRepairStackImage.damage(var5) >= var3) {
                    var7.putBoolean("Broken", true);
                }

                CompoundTag var8 = new CompoundTag();
                var8.putInt("Version", 1);
                var8.putInt("Level", var1);
                var8.putString("Profile", var2);
                var8.putIntArray("Progress", new int[3]);
                var7.put("RepairQuest", var8);
                int var9 = Math.max(0, var7.getInt("RepairRevision"));
                var7.putInt("RepairRevision", var9 == Integer.MAX_VALUE ? 0 : var9 + 1);
                NativeRepairStackImage.normalize(registries, var5);
                FoundationCanonicalDigest.canonical(var5);
                return var5;
            } else {
                throw new IllegalArgumentException("No accepted original wear START input");
            }
        } else {
            throw new IllegalArgumentException("Unsupported validated wear START rules");
        }
    }


    static CompoundTag nativeRepair(CompoundTag before, Operation operation, UUID task, int index, int payment, HolderLookup.Provider registries) {
        if (before.contains("ForgeCaps") || task == null) throw new IllegalArgumentException("Unsupported native repair image");
        CompoundTag after = before.copy();
        CompoundTag data = NativeRepairStackImage.data(after);
        CompoundTag lifecycle = data.getCompound("FTBossWeaponLifecycle");
        ItemStack planned = NativeRepairStackImage.decode(registries, before.copy());
        if (!NativeFoodWeaponWear.eligible(planned) || !lifecycle.hasUUID("Weapon")) throw new IllegalArgumentException("Missing original native wear");
        int level = FoodWeaponEvolution.evolutionLevel(planned);
        if (operation == Operation.START_REPAIR) {
            if (lifecycle.contains("RepairQuest") || NativeRepairStackImage.damage(before) <= 0) throw new IllegalArgumentException("Native repair START state");
            CompoundTag quest = new CompoundTag();
            quest.putInt("Version", 1);
            quest.putUUID("NativeTask", task);
            quest.putInt("Level", level);
            quest.putString("Profile", FoodWeaponLifecycle.repairProfile(planned));
            quest.putIntArray("Progress", new int[3]);
            lifecycle.put("RepairQuest", quest);
        } else {
            CompoundTag quest = lifecycle.getCompound("RepairQuest");
            int[] progress = quest.getIntArray("Progress");
            if (!task.equals(quest.getUUID("NativeTask")) || quest.getInt("Level") != level || progress.length != 3
                || !quest.getString("Profile").equals(FoodWeaponLifecycle.repairProfile(planned))) throw new IllegalArgumentException("Native repair task ancestry");
            for (int taskIndex = 0; taskIndex < 3; taskIndex++) {
                if (progress[taskIndex] < 0 || progress[taskIndex] > FoodWeaponLifecycleRules.repairRequired(level, taskIndex)) throw new IllegalArgumentException("Native repair payment bounds");
            }
            if (operation == Operation.SUBMIT_REPAIR) {
                if (index < 0 || index >= 3 || payment <= 0 || payment > FoodWeaponLifecycleRules.repairRequired(level, index) - progress[index]) throw new IllegalArgumentException("Native repair payment quantity");
                progress[index] += payment;
                quest.putIntArray("Progress", progress);
            } else if (operation == Operation.CLAIM_REPAIR && payment == 0 && FoodWeaponLifecycleRules.ready(progress, level)) {
                lifecycle.remove("RepairQuest");
                lifecycle.remove("Broken");
                NativeRepairStackImage.putDamage(after, 0);
            } else throw new IllegalArgumentException("Native repair operation");
        }
        int revision = Math.max(0, lifecycle.getInt("RepairRevision"));
        lifecycle.putInt("RepairRevision", revision == Integer.MAX_VALUE ? 0 : revision + 1);
        NativeRepairStackImage.normalize(registries, after);
        return after;
    }

    private static String foundationRules(FourWeaponFoundationAdapter.View var0) {
        CompoundTag var1 = new CompoundTag();
        var1.put("Profile", FoundationCanonicalDigest.object(var0.profile()));
        ListTag var2 = new ListTag();

        for (Material var6 : Material.values()) {
            CompoundTag var7 = new CompoundTag();
            var7.putString("Name", var6.name());
            var7.putString("Id", var6.id());
            var7.put("Durability", FoundationCanonicalDigest.object(var6.durability));
            var7.put("Wear", FoundationCanonicalDigest.object(var6.wear));
            var7.put("Damage", FoundationCanonicalDigest.object(var6.damage));
            var7.put("Speed", FoundationCanonicalDigest.object(var6.speed));
            var7.put("Reach", FoundationCanonicalDigest.object(var6.reach));
            var2.add(var7);
        }

        var1.put("Materials", var2);
        var1.putInt("Schema", 2);
        return FoundationCanonicalDigest.fingerprint("profile-rules/foundation-build-v1", var1);
    }

    private static void foundationReset(CompoundTag var0, HolderLookup.Provider registries) {
        FourWeaponFoundationAdapter.View var1 = FourWeaponFoundationAdapter.read(NativeRepairStackImage.decode(registries, var0.copy()));
        if (var1.valid() && var1.revision() != Integer.MAX_VALUE) {
            CompoundTag var2 = NativeRepairStackImage.data(var0);
            CompoundTag var3 = var1.rootCopy();
            boolean var4 = var3 == null;
            if (var4) {
                var3 = new CompoundTag();
                var3.put("LegacyBackup", var0.copy());
            }

            var3.putInt("Schema", 2);
            var3.putInt("DigestVersion", 3);
            var3.putInt("ProfileRevision", 1);
            var3.putString("Profile", var1.profile().id());
            var3.putString("Rules", foundationRules(var1));
            var3.putBoolean("Initialized", true);
            var3.putInt("Revision", Math.max(0, var1.revision()) + 1);
            var3.putInt("ReconciledLevel", var1.level());
            ListTag var5 = new ListTag();

            for (int var7 : new TreeSet<>(var1.entries().keySet())) {
                FourWeaponFoundationAdapter.Entry var8 = var1.entries().get(var7);
                CompoundTag var9 = new CompoundTag();
                var9.putInt("Slot", var7);
                var9.putString("Source", var8.value().provenance());
                var9.putString("Id", var8.value().itemId());
                var9.putString("Material", var8.value().material().id());
                if (var8.paid()) {
                    var9.put("Item", var8.payload());
                }

                var5.add(var9);
            }

            var3.put("Cells", var5);
            ListTag var10 = new ListTag();

            for (int var13 : new TreeSet<>(var1.retired())) {
                var10.add(IntTag.valueOf(var13));
            }

            var3.put("RetiredNative", var10);
            var3.putDouble("WearCarry", 0.0);
            var2.put("FTBossFourWeaponFoundation", var3);
            if (var2.contains("FTBossWeaponMaterials", 10)) {
                var2.getCompound("FTBossWeaponMaterials").remove("WearCarry");
            }

            CompoundTag var12 = var2.contains("FTBossFoodEvolution", 10) ? var2.getCompound("FTBossFoodEvolution") : new CompoundTag();
            var12.putInt("Level", var1.level());
            var12.putInt("Experience", var1.experience());
            var12.putInt("ProgressionVersion", 2);
            if (var4 && var12.contains("StageHistory", 9)) {
                ListTag var14 = (ListTag)var12.get("StageHistory");
                if (!var14.isEmpty() && var14.getElementType() != 10) {
                    throw new IllegalArgumentException("Invalid Foundation legacy history");
                }

                while (var14.size() > var1.level()) {
                    var14.remove(var14.size() - 1);
                }

                if (var12.contains("SelectedStage", 3)) {
                    var12.putInt("SelectedStage", Math.max(0, Math.min(var1.level() - 1, var12.getInt("SelectedStage"))));
                }
            }

            var2.put("FTBossFoodEvolution", var12);
            NativeRepairStackImage.normalize(registries, var0);
            FoundationCanonicalDigest.canonical(var0);
        } else {
            throw new IllegalArgumentException("Foundation reset plan rejected");
        }
    }
}
