package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;

/** 食武原生动作准入；2026-10-21 按已注册弓弩档案准入，保留完整性、来源与耐久校验。 */
public final class FourWeaponUseGuard {
    private FourWeaponUseGuard() {}

    public static boolean blocksUse(ItemStack stack) {
        if (!FourWeaponFoundationAdapter.applies(stack)) return false;
        if (FourWeaponFoundationAdapter.blocksUse(stack)) return true;
        CompoundTag life = stack.getTagElement("FTBossWeaponLifecycle");
        return life != null && life.getBoolean("Broken") || stack.isDamageableItem()
                && stack.getMaxDamage() > 0 && stack.getDamageValue() >= stack.getMaxDamage();
    }

    public static boolean blocksNativeSourceAction(ItemStack stack) {
        if (stack == null) return true;
        if (!FourWeaponFoundationAdapter.applies(stack)) return false;
        if (FourWeaponFoundationAdapter.matchesType(stack, "staff")) return blocksUse(stack);
        if (stack.getItem().getClass() == CrossbowItem.class || stack.getItem().getClass() == BowItem.class
                || stack.getItem() instanceof MaterialCrossbowItem || stack.getItem() instanceof MaterialBowItem
                || stack.getItem() instanceof TridentItem || nativeBow(stack)) return blocksUse(stack);
        return true;
    }

    public static boolean blocksItemUse(ItemStack stack) {
        if (!FourWeaponFoundationAdapter.applies(stack)) return false;
        var profile = FourWeaponFoundationAdapter.profile(stack);
        return blocksUse(stack) || profile == null || !profile.family().equals("axe")
                && !(profile.family().equals("trident") && stack.getItem() instanceof TridentItem)
                && !nativeBow(stack) && !FourWeaponFoundationAdapter.matchesType(stack, "staff");
    }

    private static boolean nativeBow(ItemStack stack) {
        return FourWeaponFoundationAdapter.matchesType(stack, "bow")
                || FourWeaponFoundationAdapter.matchesType(stack, "crossbow");
    }
}
