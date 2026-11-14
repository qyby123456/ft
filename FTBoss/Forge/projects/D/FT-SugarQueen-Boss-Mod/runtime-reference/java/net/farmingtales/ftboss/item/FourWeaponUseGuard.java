/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TridentItem
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;

public final class FourWeaponUseGuard {
    private FourWeaponUseGuard() {
    }

    public static boolean blocksUse(ItemStack itemStack) {
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        if (FourWeaponFoundationAdapter.blocksUse(itemStack)) {
            return true;
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossWeaponLifecycle");
        return compoundTag != null && compoundTag.m_128471_("Broken") || itemStack.m_41763_() && itemStack.m_41776_() > 0 && itemStack.m_41773_() >= itemStack.m_41776_();
    }

    public static boolean blocksNativeSourceAction(ItemStack itemStack) {
        if (itemStack == null) {
            return true;
        }
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        if (itemStack.m_41720_().getClass() == CrossbowItem.class || itemStack.m_41720_().getClass() == BowItem.class || itemStack.m_41720_() instanceof MaterialCrossbowItem || itemStack.m_41720_() instanceof MaterialBowItem || itemStack.m_41720_() instanceof TridentItem || FourWeaponUseGuard.nativeBow(itemStack)) {
            return FourWeaponUseGuard.blocksUse(itemStack);
        }
        return true;
    }

    public static boolean blocksItemUse(ItemStack itemStack) {
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        FourWeaponFoundationRules.Profile profile = FourWeaponFoundationAdapter.profile(itemStack);
        return FourWeaponUseGuard.blocksUse(itemStack) || profile == null || !profile.family().equals("axe") && (!profile.family().equals("trident") || !(itemStack.m_41720_() instanceof TridentItem)) && !FourWeaponUseGuard.nativeBow(itemStack);
    }

    private static boolean nativeBow(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.matchesType(itemStack, "bow") || FourWeaponFoundationAdapter.matchesType(itemStack, "crossbow");
    }
}

