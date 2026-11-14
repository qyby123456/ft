package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;

public final class FourWeaponUseGuard {
    private FourWeaponUseGuard() {
    }

    public static boolean blocksUse(ItemStack var0) {
        if (!FourWeaponFoundationAdapter.applies(var0)) {
            return false;
        } else if (FourWeaponFoundationAdapter.blocksUse(var0)) {
            return true;
        } else {
            CompoundTag var1 = FoodWeaponData.read(var0, "FTBossWeaponLifecycle");
            return var1 != null && var1.getBoolean("Broken")
                || var0.isDamageableItem() && var0.getMaxDamage() > 0 && var0.getDamageValue() >= var0.getMaxDamage();
        }
    }

    public static boolean blocksNativeSourceAction(ItemStack var0) {
        if (FoodWeaponSourceLedger.nativeFrostGateAllowed(var0)) return blocksUse(var0);
        if (var0 == null) {
            return true;
        } else if (!FourWeaponFoundationAdapter.applies(var0)) {
            return false;
        } else {
            return var0.getItem().getClass() != CrossbowItem.class
                    && var0.getItem().getClass() != BowItem.class
                    && !(var0.getItem() instanceof MaterialCrossbowItem)
                    && !(var0.getItem() instanceof MaterialBowItem)
                    && !(var0.getItem() instanceof TridentItem)
                    && !nativeBow(var0)
                ? true
                : blocksUse(var0);
        }
    }

    public static boolean blocksItemUse(ItemStack var0) {
        if (FoodWeaponSourceLedger.nativeFrostGateAllowed(var0)) return blocksUse(var0);
        if (!FourWeaponFoundationAdapter.applies(var0)) {
            return false;
        } else {
            Profile var1 = FourWeaponFoundationAdapter.profile(var0);
            return blocksUse(var0)
                || var1 == null
                || !var1.family().equals("axe") && (!var1.family().equals("trident") || !(var0.getItem() instanceof TridentItem)) && !nativeBow(var0);
        }
    }

    private static boolean nativeBow(ItemStack var0) {
        return FourWeaponFoundationAdapter.matchesType(var0, "bow") || FourWeaponFoundationAdapter.matchesType(var0, "crossbow");
    }
}

