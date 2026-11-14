package net.farmingtales.ftboss.item;

import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.minecraft.world.item.ItemStack;

/** 武器悬浮描述的单次只读快照；2026-10-25 合并重复构筑校验，不跨描述调用或线程复用。 */
public final class WeaponTooltipReadScope {
    private static final ThreadLocal<IdentityHashMap<ItemStack, FourWeaponFoundationAdapter.View>> VIEWS = new ThreadLocal<>();
    private static final ThreadLocal<IdentityHashMap<ItemStack, List<Cell>>> CELLS = new ThreadLocal<>();
    private static final ThreadLocal<IdentityHashMap<ItemStack, Map<String, Object>>> VALUES = new ThreadLocal<>();

    private WeaponTooltipReadScope() {}

    public static <Result> Result tooltip(Supplier<Result> factory) {
        if (VIEWS.get() != null) return factory.get();
        VIEWS.set(new IdentityHashMap<>());
        CELLS.set(new IdentityHashMap<>());
        VALUES.set(new IdentityHashMap<>());
        try {
            return factory.get();
        } finally {
            VIEWS.remove();
            CELLS.remove();
            VALUES.remove();
        }
    }

    public static FourWeaponFoundationAdapter.View cached(ItemStack stack) {
        var views = VIEWS.get();
        return views == null ? null : views.get(stack);
    }

    public static FourWeaponFoundationAdapter.View remember(ItemStack stack, FourWeaponFoundationAdapter.View view) {
        var views = VIEWS.get();
        if (views != null) views.put(stack, view);
        return view;
    }

    @SuppressWarnings("unchecked")
    public static <Result> Result cachedValue(ItemStack stack, String key) {
        var values = VALUES.get();
        var snapshot = values == null ? null : values.get(stack);
        return snapshot == null ? null : (Result) snapshot.get(key);
    }

    public static <Result> Result rememberValue(ItemStack stack, String key, Result value) {
        var values = VALUES.get();
        if (values != null) {
            var snapshot = values.get(stack);
            if (snapshot == null) {
                snapshot = new HashMap<>();
                values.put(stack, snapshot);
            }
            snapshot.put(key, value);
        }
        return value;
    }

    public static List<Cell> cachedCells(ItemStack stack) {
        var cells = CELLS.get();
        return cells == null ? null : cells.get(stack);
    }

    public static List<Cell> rememberCells(ItemStack stack, List<Cell> cells) {
        var cached = CELLS.get();
        if (cached != null) cached.put(stack, cells);
        return cells;
    }
}
