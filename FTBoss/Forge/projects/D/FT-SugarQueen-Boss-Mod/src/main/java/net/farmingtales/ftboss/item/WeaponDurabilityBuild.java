package net.farmingtales.ftboss.item;

import java.util.HashSet;
import java.util.Set;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class WeaponDurabilityBuild {
    private WeaponDurabilityBuild() {}

    public static int maxDurability(ItemStack weapon, int original) {
        if (EquipmentWeaponBuild.applies(weapon)) return net.farmingtales.ftboss.util.FoodWeaponBuildRules.maxDurability(original,
                EquipmentWeaponBuild.scale(weapon, new Build(WeaponDurabilityRules.multiplier(installedAlloys(weapon)), 1, 1, 1, 0)));
        return original <= 0 ? original : WeaponDurabilityRules.maxDurability(original, installedAlloys(weapon));
    }

    public static Build combat(ItemStack weapon, Build build) {
        return new Build(WeaponDurabilityRules.multiplier(installedAlloys(weapon)), build.wear(), build.damage(), build.speed(), build.reach());
    }

    public static int installedAlloys(ItemStack weapon) {
        if (weapon.isEmpty() || !weapon.hasTag() || WeaponAttachmentPolicy.excluded(weapon)) return 0;
        CompoundTag foundation = weapon.getTagElement("FTBossFourWeaponFoundation");
        if (foundation != null) {
            if (!FourWeaponFoundationAdapter.applies(weapon)) return 0;
            return FourWeaponFoundationAdapter.durabilityAlloys(weapon);
        }
        CompoundTag sword = weapon.getTagElement(SwordCavityBuild.ROOT);
        if (sword != null) {
            if (sword.getInt("Version") != SwordCavityRules.VERSION || !SwordCavityBuild.applies(weapon)) return 0;
            CompoundTag cells = sword.getCompound("Cells");
            int count = 0;
            for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
                CompoundTag entry = cells.getCompound(slot.name());
                if (entry.getString("State").equals("installed") && storedAlloy(entry)
                        && SwordCavityBuild.cellUnlocked(weapon, slot.ordinal())) count++;
            }
            return count;
        }
        CompoundTag legacy = weapon.getTagElement(WeaponMaterialBuild.ROOT);
        if (legacy == null) return 0;
        int count = 0;
        for (Part part : Part.values()) if (legacy.getString(part.name()).equals("diamond_alloy")) count++;
        return count;
    }

    public static int foundationAlloys(ItemStack weapon, FourWeaponFoundationRules.Profile profile) {
        CompoundTag root = weapon.getTagElement("FTBossFourWeaponFoundation");
        if (root == null || root.getInt("Schema") != FourWeaponFoundationAdapter.SCHEMA
                || root.getInt("DigestVersion") != FourWeaponFoundationAdapter.DIGEST_VERSION
                || root.getInt("ProfileRevision") != 1 || !root.getBoolean("Initialized")
                || !root.getString("Profile").equals(profile.id())) return 0;
        ListTag cells = root.getList("Cells", Tag.TAG_COMPOUND);
        if (cells.size() > FourWeaponFoundationRules.CELLS) return 0;
        boolean candidate = false;
        for (int index = 0; index < cells.size(); index++) {
            CompoundTag entry = cells.getCompound(index);
            if (entry.getString("Source").equals("player-paid") && storedAlloy(entry)) candidate = true;
        }
        if (!candidate || !FoundationProfileFingerprints.matches(root.getString("Rules"), profile)) return 0;
        int level = root.getInt("ReconciledLevel");
        CompoundTag evolution = weapon.getTagElement("FTBossFoodEvolution");
        if (level < 0 || level > 4 || level != (evolution == null ? 0 : evolution.getInt("Level"))) return 0;
        Set<Integer> occupied = new HashSet<>();
        int count = 0;
        for (int index = 0; index < cells.size(); index++) {
            CompoundTag entry = cells.getCompound(index);
            int slot = entry.getInt("Slot");
            if (!entry.contains("Slot", Tag.TAG_INT) || slot < 0 || slot >= FourWeaponFoundationRules.CELLS || !occupied.add(slot)) return 0;
            if (entry.getString("Source").equals("player-paid") && storedAlloy(entry)
                    && entry.getString("Id").equals(WeaponDurabilityRules.ALLOY_ITEM)
                    && FourWeaponFoundationRules.unlocked(profile, slot, level)) count++;
        }
        return count;
    }

    private static boolean storedAlloy(CompoundTag entry) {
        if (!entry.getString("Material").equals("diamond_alloy") || !entry.contains("Item", Tag.TAG_COMPOUND)) return false;
        CompoundTag item = entry.getCompound("Item");
        return item.getString("id").equals(WeaponDurabilityRules.ALLOY_ITEM) && item.getInt("Count") == 1;
    }
}
