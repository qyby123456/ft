package net.farmingtales.ftboss.item;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.minecraft.world.item.ItemStack;

public final class WeaponTooltipReadScope {
    private static final ThreadLocal<IdentityHashMap<ItemStack, View>> VIEWS = new ThreadLocal<>();
    private static final ThreadLocal<IdentityHashMap<ItemStack, List<Cell>>> CELLS = new ThreadLocal<>();
    private static final ThreadLocal<IdentityHashMap<ItemStack, Map<String, Object>>> VALUES = new ThreadLocal<>();

    private WeaponTooltipReadScope() {
    }

    public static <Result> Result tooltip(Supplier<Result> var0) {
        if (VIEWS.get() != null) {
            return (Result)var0.get();
        } else {
            VIEWS.set(new IdentityHashMap<>());
            CELLS.set(new IdentityHashMap<>());
            VALUES.set(new IdentityHashMap<>());

            Object var1;
            try {
                var1 = var0.get();
            } finally {
                VIEWS.remove();
                CELLS.remove();
                VALUES.remove();
            }

            return (Result)var1;
        }
    }

    public static View cached(ItemStack var0) {
        IdentityHashMap var1 = VIEWS.get();
        return var1 == null ? null : (View)var1.get(var0);
    }

    public static View remember(ItemStack var0, View var1) {
        IdentityHashMap var2 = VIEWS.get();
        if (var2 != null) {
            var2.put(var0, var1);
        }

        return var1;
    }

    public static <Result> Result cachedValue(ItemStack var0, String var1) {
        IdentityHashMap var2 = VALUES.get();
        Map var3 = var2 == null ? null : (Map)var2.get(var0);
        return (Result)(var3 == null ? null : var3.get(var1));
    }

    public static <Result> Result rememberValue(ItemStack var0, String var1, Result var2) {
        IdentityHashMap<ItemStack, Map<String, Object>> var3 = VALUES.get();
        if (var3 != null) {
            Map<String, Object> var4 = var3.get(var0);
            if (var4 == null) {
                var4 = new HashMap<>();
                var3.put(var0, var4);
            }

            var4.put(var1, var2);
        }

        return (Result)var2;
    }

    public static List<Cell> cachedCells(ItemStack var0) {
        IdentityHashMap var1 = CELLS.get();
        return var1 == null ? null : (List)var1.get(var0);
    }

    public static List<Cell> rememberCells(ItemStack var0, List<Cell> var1) {
        IdentityHashMap var2 = CELLS.get();
        if (var2 != null) {
            var2.put(var0, var1);
        }

        return var1;
    }
}
