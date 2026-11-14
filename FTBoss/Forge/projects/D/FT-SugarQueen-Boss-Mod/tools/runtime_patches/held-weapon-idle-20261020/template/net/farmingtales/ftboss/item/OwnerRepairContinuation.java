package net.farmingtales.ftboss.item;

import java.util.Map;
import net.minecraft.world.item.ItemStack;

final class OwnerRepairContinuation {
    private static final Map<ItemStack, Object> BINDINGS = null;

    static boolean noBinding(ItemStack actual) {
        synchronized (OwnerFullRepairGuard.class) {
            return BINDINGS.get(actual) == null;
        }
    }
}
