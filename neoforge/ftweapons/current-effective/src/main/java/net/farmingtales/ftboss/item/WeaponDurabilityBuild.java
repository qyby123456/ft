package net.farmingtales.ftboss.item;

import java.util.HashSet;
import net.farmingtales.ftboss.util.WeaponDurabilityRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.foundations.FoundationProfileFingerprints;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public final class WeaponDurabilityBuild {
    private WeaponDurabilityBuild() {
    }

    public static int maxDurability(ItemStack var0, int var1) {
        return var1 <= 0 ? var1 : WeaponDurabilityRules.maxDurability(var1, installedAlloys(var0));
    }

    public static Build combat(ItemStack var0, Build var1) {
        return new Build(WeaponDurabilityRules.multiplier(installedAlloys(var0)), var1.wear(), var1.damage(), var1.speed(), var1.reach());
    }

    public static int installedAlloys(ItemStack var0) {
        if (!var0.isEmpty() && !WeaponAttachmentPolicy.excluded(var0)) {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossFourWeaponFoundation");
            if (var1 != null) {
                return !FourWeaponFoundationAdapter.applies(var0) ? 0 : FourWeaponFoundationAdapter.durabilityAlloys(var0);
            } else {
                CompoundTag var2 = FoodWeaponData.read(var0, "FTBossSwordStructure");
                if (var2 != null) {
                    if (var2.getInt("Version") == 2 && SwordCavityBuild.applies(var0)) {
                        CompoundTag var10 = var2.getCompound("Cells");
                        int var11 = 0;

                        for (Slot var15 : Slot.values()) {
                            CompoundTag var9 = var10.getCompound(var15.name());
                            if (var9.getString("State").equals("installed") && storedAlloy(var9) && SwordCavityBuild.cellUnlocked(var0, var15.ordinal())) {
                                var11++;
                            }
                        }

                        return var11;
                    } else {
                        return 0;
                    }
                } else {
                    CompoundTag var3 = FoodWeaponData.read(var0, "FTBossWeaponMaterials");
                    if (var3 == null) {
                        return 0;
                    } else {
                        int var4 = 0;

                        for (Part var8 : Part.values()) {
                            if (var3.getString(var8.name()).equals("diamond_alloy")) {
                                var4++;
                            }
                        }

                        return var4;
                    }
                }
            }
        } else {
            return 0;
        }
    }

    public static int foundationAlloys(ItemStack var0, Profile var1) {
        CompoundTag var2 = FoodWeaponData.read(var0, "FTBossFourWeaponFoundation");
        if (var2 != null
            && var2.getInt("Schema") == 2
            && var2.getInt("DigestVersion") == 3
            && var2.getInt("ProfileRevision") == 1
            && var2.getBoolean("Initialized")
            && var2.getString("Profile").equals(var1.id())) {
            ListTag var3 = var2.getList("Cells", 10);
            if (var3.size() > 27) {
                return 0;
            } else {
                boolean var4 = false;

                for (int var5 = 0; var5 < var3.size(); var5++) {
                    CompoundTag var6 = var3.getCompound(var5);
                    if (var6.getString("Source").equals("player-paid") && storedAlloy(var6)) {
                        var4 = true;
                    }
                }

                if (var4 && FoundationProfileFingerprints.matches(var2.getString("Rules"), var1)) {
                    int var12 = var2.getInt("ReconciledLevel");
                    CompoundTag var13 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
                    if (var12 >= 0 && var12 <= 4 && var12 == (var13 == null ? 0 : var13.getInt("Level"))) {
                        HashSet var7 = new HashSet();
                        int var8 = 0;

                        for (int var9 = 0; var9 < var3.size(); var9++) {
                            CompoundTag var10 = var3.getCompound(var9);
                            int var11 = var10.getInt("Slot");
                            if (!var10.contains("Slot", 3) || var11 < 0 || var11 >= 27 || !var7.add(var11)) {
                                return 0;
                            }

                            if (var10.getString("Source").equals("player-paid")
                                && storedAlloy(var10)
                                && var10.getString("Id").equals("kubejs:ne_ingot")
                                && FourWeaponFoundationRules.unlocked(var1, var11, var12)) {
                                var8++;
                            }
                        }

                        return var8;
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    private static boolean storedAlloy(CompoundTag var0) {
        if (var0.getString("Material").equals("diamond_alloy") && var0.contains("Item", 10)) {
            CompoundTag var1 = var0.getCompound("Item");
            return var1.getString("id").equals("kubejs:ne_ingot") && var1.getInt("Count") == 1;
        } else {
            return false;
        }
    }
}
