package net.farmingtales.ftboss.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public final class WeaponSystemScope410 {
    private WeaponSystemScope410() {}

    public static boolean supports(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof BowItem
            || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof TridentItem) return true;
        for (Class<?> type = stack.getItem().getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) return true;
        }
        return FourWeaponFoundationAdapter.matchesType(stack, "bow")
            || FourWeaponFoundationAdapter.matchesType(stack, "crossbow")
            || FourWeaponFoundationAdapter.matchesType(stack, "axe")
            || FourWeaponFoundationAdapter.matchesType(stack, "trident")
            || FourWeaponFoundationAdapter.matchesType(stack, "staff");
    }
}
