package net.farmingtales.ftboss.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class NativeFoodWeaponRepairComponents {
    private NativeFoodWeaponRepairComponents() {
    }

    public static boolean onlyRepairChanged(ItemStack before, ItemStack after) {
        if (before.getCount() != after.getCount()) {
            return false;
        }
        ItemStack normalizedBefore = before.copy();
        ItemStack normalizedAfter = after.copy();
        removeRepairFields(normalizedBefore);
        removeRepairFields(normalizedAfter);
        return ItemStack.isSameItemSameComponents(normalizedBefore, normalizedAfter);
    }

    public static void removeRepairFields(ItemStack stack) {
        stack.remove(DataComponents.DAMAGE);
        stack.remove(DataComponents.REPAIR_COST);
    }
}
