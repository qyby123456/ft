package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialBowItem;
import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;

/** 食武原生动作准入；2026-10-20 恢复完整且未损坏的原版弓弩入口，保留来源与耐久校验。 */
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
        if (stack.getItem().getClass() == CrossbowItem.class || stack.getItem().getClass() == BowItem.class
                || stack.getItem() instanceof MaterialCrossbowItem || stack.getItem() instanceof MaterialBowItem
                || stack.getItem() instanceof TridentItem) return blocksUse(stack);
        return true;
    }

    public static boolean blocksItemUse(ItemStack stack) {
        if (!FourWeaponFoundationAdapter.applies(stack)) return false;
        var profile = FourWeaponFoundationAdapter.profile(stack);
        return blocksUse(stack) || profile == null || !profile.family().equals("axe")
                && !(profile.family().equals("trident") && stack.getItem() instanceof TridentItem);
    }
}
