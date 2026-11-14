/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.HashSet;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public final class WeaponDurabilityBuild {
    private WeaponDurabilityBuild() {
    }

    public static int maxDurability(ItemStack itemStack, int n) {
        return n <= 0 ? n : WeaponDurabilityRules.maxDurability(n, WeaponDurabilityBuild.installedAlloys(itemStack));
    }

    public static FoodWeaponBuildRules.Build combat(ItemStack itemStack, FoodWeaponBuildRules.Build build) {
        return new FoodWeaponBuildRules.Build(WeaponDurabilityRules.multiplier(WeaponDurabilityBuild.installedAlloys(itemStack)), build.wear(), build.damage(), build.speed(), build.reach());
    }

    public static int installedAlloys(ItemStack itemStack) {
        if (itemStack.m_41619_() || !itemStack.m_41782_() || WeaponAttachmentPolicy.excluded(itemStack)) {
            return 0;
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossFourWeaponFoundation");
        if (compoundTag != null) {
            if (!FourWeaponFoundationAdapter.applies(itemStack)) {
                return 0;
            }
            return FourWeaponFoundationAdapter.durabilityAlloys(itemStack);
        }
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossSwordStructure");
        if (compoundTag2 != null) {
            if (compoundTag2.m_128451_("Version") != 2 || !SwordCavityBuild.applies(itemStack)) {
                return 0;
            }
            CompoundTag compoundTag3 = compoundTag2.m_128469_("Cells");
            int n = 0;
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                CompoundTag compoundTag4 = compoundTag3.m_128469_(slot.name());
                if (!compoundTag4.m_128461_("State").equals("installed") || !WeaponDurabilityBuild.storedAlloy(compoundTag4) || !SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal())) continue;
                ++n;
            }
            return n;
        }
        CompoundTag compoundTag5 = itemStack.m_41737_("FTBossWeaponMaterials");
        if (compoundTag5 == null) {
            return 0;
        }
        int n = 0;
        for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
            if (!compoundTag5.m_128461_(part.name()).equals("diamond_alloy")) continue;
            ++n;
        }
        return n;
    }

    public static int foundationAlloys(ItemStack itemStack, FourWeaponFoundationRules.Profile profile) {
        CompoundTag compoundTag;
        int n;
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossFourWeaponFoundation");
        if (compoundTag2 == null || compoundTag2.m_128451_("Schema") != 2 || compoundTag2.m_128451_("DigestVersion") != 3 || compoundTag2.m_128451_("ProfileRevision") != 1 || !compoundTag2.m_128471_("Initialized") || !compoundTag2.m_128461_("Profile").equals(profile.id())) {
            return 0;
        }
        ListTag listTag = compoundTag2.m_128437_("Cells", 10);
        if (listTag.size() > 27) {
            return 0;
        }
        boolean bl = false;
        for (n = 0; n < listTag.size(); ++n) {
            compoundTag = listTag.m_128728_(n);
            if (!compoundTag.m_128461_("Source").equals("player-paid") || !WeaponDurabilityBuild.storedAlloy(compoundTag)) continue;
            bl = true;
        }
        if (!bl || !FoundationProfileFingerprints.matches(compoundTag2.m_128461_("Rules"), profile)) {
            return 0;
        }
        n = compoundTag2.m_128451_("ReconciledLevel");
        compoundTag = itemStack.m_41737_("FTBossFoodEvolution");
        if (n < 0 || n > 4 || n != (compoundTag == null ? 0 : compoundTag.m_128451_("Level"))) {
            return 0;
        }
        HashSet<Integer> hashSet = new HashSet<Integer>();
        int n2 = 0;
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag3 = listTag.m_128728_(i);
            int n3 = compoundTag3.m_128451_("Slot");
            if (!compoundTag3.m_128425_("Slot", 3) || n3 < 0 || n3 >= 27 || !hashSet.add(n3)) {
                return 0;
            }
            if (!compoundTag3.m_128461_("Source").equals("player-paid") || !WeaponDurabilityBuild.storedAlloy(compoundTag3) || !compoundTag3.m_128461_("Id").equals("kubejs:ne_ingot") || !FourWeaponFoundationRules.unlocked(profile, n3, n)) continue;
            ++n2;
        }
        return n2;
    }

    private static boolean storedAlloy(CompoundTag compoundTag) {
        if (!compoundTag.m_128461_("Material").equals("diamond_alloy") || !compoundTag.m_128425_("Item", 10)) {
            return false;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("Item");
        return compoundTag2.m_128461_("id").equals("kubejs:ne_ingot") && compoundTag2.m_128451_("Count") == 1;
    }
}

