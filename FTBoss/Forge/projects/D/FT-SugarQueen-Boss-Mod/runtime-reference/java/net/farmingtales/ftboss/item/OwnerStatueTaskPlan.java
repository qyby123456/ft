/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.TreeSet;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

final class OwnerStatueTaskPlan {
    private OwnerStatueTaskPlan() {
    }

    static CompoundTag legacyQuest(ItemStack itemStack) {
        if (itemStack.m_41613_() != 1 || FoodWeaponLifecycle.weaponId(itemStack) == null || !FoodWeaponLifecycle.managed(itemStack) || !FoodWeaponRepair.view(itemStack, 0).active()) {
            throw new IllegalArgumentException("No valid original legacy task");
        }
        if (FourWeaponFoundationAdapter.applies(itemStack) && !FourWeaponFoundationAdapter.read(itemStack).valid()) {
            throw new IllegalArgumentException("Invalid original Foundation task");
        }
        return itemStack.m_41737_("FTBossWeaponLifecycle").m_128469_("RepairQuest").m_6426_();
    }

    static String rules(ItemStack itemStack) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("TaskVersion", 1);
        compoundTag.m_128405_("Level", FoodWeaponEvolution.evolutionLevel(itemStack));
        compoundTag.m_128359_("Profile", FoodWeaponLifecycle.repairProfile(itemStack));
        int[] nArray = new int[3];
        for (int i = 0; i < 3; ++i) {
            nArray[i] = FoodWeaponLifecycleRules.repairRequired(compoundTag.m_128451_("Level"), i);
        }
        compoundTag.m_128385_("Required", nArray);
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
            if (!view.valid()) {
                throw new IllegalArgumentException("Invalid Foundation task rules");
            }
            compoundTag.m_128359_("Foundation", OwnerStatueTaskPlan.foundationRules(view));
        }
        return FoundationCanonicalDigest.fingerprint("repair-task-rules/v1", (Tag)compoundTag);
    }

    static CompoundTag after(CompoundTag compoundTag, FourWeaponTransactions.Operation operation, int n, int n2, int n3) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        if (compoundTag2.m_128441_("ForgeCaps")) {
            throw new IllegalArgumentException("Task plan capability image unsupported");
        }
        ItemStack itemStack = ItemStack.m_41712_((CompoundTag)compoundTag2.m_6426_());
        CompoundTag compoundTag3 = OwnerStatueTaskPlan.legacyQuest(itemStack);
        if (!FoodWeaponLifecycle.repairProfile(itemStack).equals(FoodWeaponEvolution.profileId(n3))) {
            throw new IllegalArgumentException("Task plan wrong actual statue");
        }
        CompoundTag compoundTag4 = compoundTag2.m_128469_("tag");
        CompoundTag compoundTag5 = compoundTag4.m_128469_("FTBossWeaponLifecycle");
        if (operation == FourWeaponTransactions.Operation.SUBMIT_REPAIR) {
            var10_10 = compoundTag3.m_128465_("Progress");
            if (n < 0 || n >= 3 || n2 <= 0 || n2 > FoodWeaponLifecycleRules.repairRequired(compoundTag3.m_128451_("Level"), n) - var10_10[n]) {
                throw new IllegalArgumentException("Task plan original payment quantity");
            }
            int n4 = n;
            var10_10[n4] = var10_10[n4] + n2;
            compoundTag5.m_128469_("RepairQuest").m_128385_("Progress", var10_10);
        } else if (operation == FourWeaponTransactions.Operation.CLAIM_REPAIR) {
            if (n2 != 0 || !FoodWeaponLifecycleRules.ready(compoundTag3.m_128465_("Progress"), compoundTag3.m_128451_("Level"))) {
                throw new IllegalArgumentException("Task plan not ready");
            }
            compoundTag5.m_128473_("RepairQuest");
            if (FourWeaponFoundationAdapter.applies(itemStack)) {
                OwnerStatueTaskPlan.foundationReset(compoundTag2);
            }
            if (FoodWeaponEvolution.evolutionLevel(ItemStack.m_41712_((CompoundTag)compoundTag2.m_6426_())) > 0) {
                compoundTag5.m_128379_("WasEvolved", true);
            }
            if (compoundTag5.m_128461_("RepairProfile").isEmpty()) {
                compoundTag5.m_128359_("RepairProfile", FoodWeaponEvolution.repairProfileId(itemStack));
            }
            compoundTag4.m_128405_("Damage", 0);
            compoundTag5.m_128473_("Broken");
            compoundTag5.m_128473_("PendingMaterials");
            compoundTag5.m_128473_("PendingSockets");
            compoundTag5.m_128405_("WearStep", 0);
            if (compoundTag4.m_128425_("FTBossWeaponMaterials", 10)) {
                var10_10 = compoundTag4.m_128469_("FTBossWeaponMaterials");
                if (FourWeaponFoundationAdapter.applies(itemStack)) {
                    var10_10.m_128473_("WearCarry");
                } else {
                    var10_10.m_128347_("WearCarry", 0.0);
                }
            }
        } else {
            throw new IllegalArgumentException("Only existing migration-task submit and claim are implemented");
        }
        int n5 = Math.max(0, compoundTag5.m_128451_("RepairRevision"));
        compoundTag5.m_128405_("RepairRevision", n5 == Integer.MAX_VALUE ? 0 : n5 + 1);
        FoundationCanonicalDigest.canonical((Tag)compoundTag2);
        return compoundTag2;
    }

    static CompoundTag start(CompoundTag compoundTag, int n, String string, int n2, int n3) {
        if (compoundTag.m_128441_("ForgeCaps") || n < 0 || n > 4 || n2 <= 0 || string.isEmpty() || !string.equals(FoodWeaponEvolution.profileId(n3))) {
            throw new IllegalArgumentException("Unsupported validated wear START rules");
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = compoundTag2.m_128469_("tag");
        CompoundTag compoundTag4 = compoundTag3.m_128469_("FTBossWeaponLifecycle");
        if (!compoundTag4.m_128403_("Weapon") || !compoundTag4.m_128425_("WearStep", 3) || compoundTag4.m_128441_("RepairQuest") || !string.equals(compoundTag4.m_128461_("RepairProfile")) || compoundTag3.m_128451_("Damage") <= 0) {
            throw new IllegalArgumentException("No accepted original wear START input");
        }
        if (n > 0) {
            compoundTag4.m_128379_("WasEvolved", true);
        }
        if (compoundTag3.m_128451_("Damage") >= n2) {
            compoundTag4.m_128379_("Broken", true);
        }
        CompoundTag compoundTag5 = new CompoundTag();
        compoundTag5.m_128405_("Version", 1);
        compoundTag5.m_128405_("Level", n);
        compoundTag5.m_128359_("Profile", string);
        compoundTag5.m_128385_("Progress", new int[3]);
        compoundTag4.m_128365_("RepairQuest", (Tag)compoundTag5);
        int n4 = Math.max(0, compoundTag4.m_128451_("RepairRevision"));
        compoundTag4.m_128405_("RepairRevision", n4 == Integer.MAX_VALUE ? 0 : n4 + 1);
        FoundationCanonicalDigest.canonical((Tag)compoundTag2);
        return compoundTag2;
    }

    private static String foundationRules(FourWeaponFoundationAdapter.View view) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128365_("Profile", (Tag)FoundationCanonicalDigest.object(view.profile()));
        ListTag listTag = new ListTag();
        for (FoodWeaponBuildRules.Material material : FoodWeaponBuildRules.Material.values()) {
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128359_("Name", material.name());
            compoundTag2.m_128359_("Id", material.id());
            compoundTag2.m_128365_("Durability", (Tag)FoundationCanonicalDigest.object(material.durability));
            compoundTag2.m_128365_("Wear", (Tag)FoundationCanonicalDigest.object(material.wear));
            compoundTag2.m_128365_("Damage", (Tag)FoundationCanonicalDigest.object(material.damage));
            compoundTag2.m_128365_("Speed", (Tag)FoundationCanonicalDigest.object(material.speed));
            compoundTag2.m_128365_("Reach", (Tag)FoundationCanonicalDigest.object(material.reach));
            listTag.add((Object)compoundTag2);
        }
        compoundTag.m_128365_("Materials", (Tag)listTag);
        compoundTag.m_128405_("Schema", 2);
        return FoundationCanonicalDigest.fingerprint("profile-rules/foundation-build-v1", (Tag)compoundTag);
    }

    private static void foundationReset(CompoundTag compoundTag) {
        boolean bl;
        FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(ItemStack.m_41712_((CompoundTag)compoundTag.m_6426_()));
        if (!view.valid() || view.revision() == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Foundation reset plan rejected");
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("tag");
        CompoundTag compoundTag3 = view.rootCopy();
        boolean bl2 = bl = compoundTag3 == null;
        if (bl) {
            compoundTag3 = new CompoundTag();
            compoundTag3.m_128365_("LegacyBackup", (Tag)compoundTag.m_6426_());
        }
        compoundTag3.m_128405_("Schema", 2);
        compoundTag3.m_128405_("DigestVersion", 3);
        compoundTag3.m_128405_("ProfileRevision", 1);
        compoundTag3.m_128359_("Profile", view.profile().id());
        compoundTag3.m_128359_("Rules", OwnerStatueTaskPlan.foundationRules(view));
        compoundTag3.m_128379_("Initialized", true);
        compoundTag3.m_128405_("Revision", Math.max(0, view.revision()) + 1);
        compoundTag3.m_128405_("ReconciledLevel", view.level());
        ListTag listTag = new ListTag();
        ListTag listTag2 = new TreeSet<Integer>(view.entries().keySet()).iterator();
        while (listTag2.hasNext()) {
            int n = listTag2.next();
            FourWeaponFoundationAdapter.Entry entry = view.entries().get(n);
            CompoundTag compoundTag4 = new CompoundTag();
            compoundTag4.m_128405_("Slot", n);
            compoundTag4.m_128359_("Source", entry.value().provenance());
            compoundTag4.m_128359_("Id", entry.value().itemId());
            compoundTag4.m_128359_("Material", entry.value().material().id());
            if (entry.paid()) {
                compoundTag4.m_128365_("Item", (Tag)entry.payload());
            }
            listTag.add((Object)compoundTag4);
        }
        compoundTag3.m_128365_("Cells", (Tag)listTag);
        listTag2 = new ListTag();
        CompoundTag compoundTag5 = new TreeSet<Integer>(view.retired()).iterator();
        while (compoundTag5.hasNext()) {
            int n = compoundTag5.next();
            listTag2.add((Object)IntTag.m_128679_((int)n));
        }
        compoundTag3.m_128365_("RetiredNative", (Tag)listTag2);
        compoundTag3.m_128347_("WearCarry", 0.0);
        compoundTag2.m_128365_("FTBossFourWeaponFoundation", (Tag)compoundTag3);
        if (compoundTag2.m_128425_("FTBossWeaponMaterials", 10)) {
            compoundTag2.m_128469_("FTBossWeaponMaterials").m_128473_("WearCarry");
        }
        compoundTag5 = compoundTag2.m_128425_("FTBossFoodEvolution", 10) ? compoundTag2.m_128469_("FTBossFoodEvolution") : new CompoundTag();
        compoundTag5.m_128405_("Level", view.level());
        compoundTag5.m_128405_("Experience", view.experience());
        compoundTag5.m_128405_("ProgressionVersion", 2);
        if (bl && compoundTag5.m_128425_("StageHistory", 9)) {
            ListTag listTag3 = (ListTag)compoundTag5.m_128423_("StageHistory");
            if (!listTag3.isEmpty() && listTag3.m_7264_() != 10) {
                throw new IllegalArgumentException("Invalid Foundation legacy history");
            }
            while (listTag3.size() > view.level()) {
                listTag3.remove(listTag3.size() - 1);
            }
            if (compoundTag5.m_128425_("SelectedStage", 3)) {
                compoundTag5.m_128405_("SelectedStage", Math.max(0, Math.min(view.level() - 1, compoundTag5.m_128451_("SelectedStage"))));
            }
        }
        compoundTag2.m_128365_("FTBossFoodEvolution", (Tag)compoundTag5);
        FoundationCanonicalDigest.canonical((Tag)compoundTag);
    }
}

