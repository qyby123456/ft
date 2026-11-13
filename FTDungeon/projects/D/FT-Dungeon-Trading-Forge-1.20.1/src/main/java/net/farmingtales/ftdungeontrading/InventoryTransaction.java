package net.farmingtales.ftdungeontrading;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.farmingtales.ftdungeontrading.api.TradeCost;
import net.farmingtales.ftdungeontrading.api.TradeOffer;
import net.farmingtales.ftdungeontrading.api.TradeResult.Status;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

final class InventoryTransaction {
    private static final int MAIN_SLOTS = 36;
    private static final int OFFHAND_SLOT = 40;
    private final List<ItemStack> before;
    private final List<ItemStack> after;
    private final Status failure;

    private InventoryTransaction(List<ItemStack> before, List<ItemStack> after, Status failure) {
        this.before = before;
        this.after = after;
        this.failure = failure;
    }

    static InventoryTransaction plan(Inventory inventory, TradeOffer offer) {
        List<ItemStack> before = snapshot(inventory);
        List<ItemStack> after = before.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<TradeCost> costs = offer.costs();
        int source = after.size() + costs.size();
        int sink = source + 1;
        int[][] residual = new int[sink + 1][sink + 1];
        int required = 0;
        for (int slot = 0; slot < after.size(); slot++) {
            residual[source][slot] = after.get(slot).getCount();
            for (int cost = 0; cost < costs.size(); cost++) {
                if (costs.get(cost).matches(after.get(slot))) residual[slot][after.size() + cost] = after.get(slot).getCount();
            }
        }
        for (int cost = 0; cost < costs.size(); cost++) {
            residual[after.size() + cost][sink] = costs.get(cost).count();
            required += costs.get(cost).count();
        }
        if (maximumFlow(residual, source, sink) != required) {
            return new InventoryTransaction(before, before, Status.INSUFFICIENT_PAYMENT);
        }
        for (int slot = 0; slot < after.size(); slot++) after.get(slot).shrink(residual[slot][source]);
        for (ItemStack reward : offer.rewards()) {
            if (!insert(after, reward, inventory.getMaxStackSize())) {
                return new InventoryTransaction(before, before, Status.INVENTORY_FULL);
            }
        }
        return new InventoryTransaction(before, List.copyOf(after), null);
    }

    Status failure() { return failure; }

    boolean commit(Inventory inventory) {
        if (failure != null || !same(before, snapshot(inventory))) return false;
        for (int index = 0; index < after.size(); index++) inventory.setItem(index < MAIN_SLOTS ? index : OFFHAND_SLOT, after.get(index).copy());
        inventory.setChanged();
        return true;
    }

    private static List<ItemStack> snapshot(Inventory inventory) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int index = 0; index < MAIN_SLOTS; index++) stacks.add(inventory.getItem(index).copy());
        stacks.add(inventory.getItem(OFFHAND_SLOT).copy());
        return stacks;
    }

    private static boolean same(List<ItemStack> first, List<ItemStack> second) {
        for (int index = 0; index < first.size(); index++) if (!ItemStack.matches(first.get(index), second.get(index))) return false;
        return true;
    }

    private static boolean insert(List<ItemStack> slots, ItemStack reward, int inventoryLimit) {
        ItemStack remaining = reward.copy();
        for (int pass = 0; pass < 2; pass++) {
            for (int index = 0; index < MAIN_SLOTS; index++) {
                ItemStack target = slots.get(index);
                if (pass == 0 && !target.isEmpty() && ItemStack.isSameItemSameTags(target, remaining)) {
                    int transfer = Math.min(remaining.getCount(), Math.max(0, Math.min(inventoryLimit, target.getMaxStackSize()) - target.getCount()));
                    target.grow(transfer);
                    remaining.shrink(transfer);
                } else if (pass == 1 && target.isEmpty()) {
                    int transfer = Math.min(remaining.getCount(), Math.min(inventoryLimit, remaining.getMaxStackSize()));
                    slots.set(index, remaining.copyWithCount(transfer));
                    remaining.shrink(transfer);
                }
                if (remaining.isEmpty()) return true;
            }
        }
        return false;
    }

    private static int maximumFlow(int[][] residual, int source, int sink) {
        int flow = 0;
        while (true) {
            int[] parent = new int[residual.length];
            Arrays.fill(parent, -1);
            parent[source] = source;
            var queue = new ArrayDeque<Integer>();
            queue.add(source);
            while (!queue.isEmpty() && parent[sink] == -1) {
                int node = queue.remove();
                for (int next = 0; next < residual.length; next++) {
                    if (residual[node][next] > 0 && parent[next] == -1) {
                        parent[next] = node;
                        queue.add(next);
                    }
                }
            }
            if (parent[sink] == -1) return flow;
            int amount = Integer.MAX_VALUE;
            for (int node = sink; node != source; node = parent[node]) amount = Math.min(amount, residual[parent[node]][node]);
            for (int node = sink; node != source; node = parent[node]) {
                residual[parent[node]][node] -= amount;
                residual[node][parent[node]] += amount;
            }
            flow += amount;
        }
    }
}
