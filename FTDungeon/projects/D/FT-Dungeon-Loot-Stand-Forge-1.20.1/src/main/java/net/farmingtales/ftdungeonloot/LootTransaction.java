package net.farmingtales.ftdungeonloot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

final class LootTransaction {
    private final List<ItemStack> before;
    private final List<ItemStack> after;
    private final LootTrades.Status failure;

    private LootTransaction(List<ItemStack> before, List<ItemStack> after, LootTrades.Status failure) {
        this.before = before;
        this.after = after;
        this.failure = failure;
    }

    static LootTransaction plan(Inventory inventory, ItemStack currency, int price, ItemStack reward) {
        List<ItemStack> before = snapshot(inventory);
        List<ItemStack> after = before.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (currency.isEmpty() || price < 1 || reward.isEmpty()) return new LootTransaction(before, before, LootTrades.Status.UNAVAILABLE);
        int remaining = price;
        for (ItemStack stack : after) {
            if (ItemStack.isSameItemSameTags(stack, currency)) {
                int debit = Math.min(remaining, stack.getCount());
                stack.shrink(debit);
                remaining -= debit;
            }
        }
        if (remaining > 0) return new LootTransaction(before, before, LootTrades.Status.INSUFFICIENT_PAYMENT);
        ItemStack insertion = reward.copy();
        for (int pass = 0; pass < 2; pass++) {
            for (int slot = 0; slot < 36; slot++) {
                ItemStack target = after.get(slot);
                int limit = Math.min(inventory.getMaxStackSize(), insertion.getMaxStackSize());
                if (pass == 0 && !target.isEmpty() && ItemStack.isSameItemSameTags(target, insertion)) {
                    int amount = Math.min(insertion.getCount(), Math.max(0, limit - target.getCount()));
                    target.grow(amount);
                    insertion.shrink(amount);
                } else if (pass == 1 && target.isEmpty()) {
                    int amount = Math.min(insertion.getCount(), limit);
                    after.set(slot, insertion.copyWithCount(amount));
                    insertion.shrink(amount);
                }
                if (insertion.isEmpty()) return new LootTransaction(before, after, null);
            }
        }
        return new LootTransaction(before, before, LootTrades.Status.INVENTORY_FULL);
    }

    LootTrades.Status failure() { return failure; }

    boolean commit(Inventory inventory) {
        List<ItemStack> current = snapshot(inventory);
        if (failure != null) return false;
        for (int slot = 0; slot < before.size(); slot++) {
            if (!ItemStack.matches(before.get(slot), current.get(slot))) return false;
        }
        for (int slot = 0; slot < after.size(); slot++) inventory.setItem(slot < 36 ? slot : 40, after.get(slot).copy());
        inventory.setChanged();
        return true;
    }

    private static List<ItemStack> snapshot(Inventory inventory) {
        List<ItemStack> result = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++) result.add(inventory.getItem(slot).copy());
        result.add(inventory.getItem(40).copy());
        return result;
    }
}
