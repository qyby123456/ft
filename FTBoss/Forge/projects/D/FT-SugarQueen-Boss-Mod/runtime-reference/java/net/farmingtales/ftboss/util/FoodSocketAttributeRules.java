/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FoodSocketAttributeRules {
    public static final int MAX_SOCKETS = 30;
    public static final int GUARD_TICKS = 80;
    public static final int ECHO_DELAY_TICKS = 12;
    public static final int CHAIN_WINDOW_TICKS = 100;
    public static final int GUARD_COOLDOWN_TICKS = 600;
    public static final int OFFENSE_COOLDOWN_TICKS = 400;
    public static final int REPEL_COOLDOWN_TICKS = 400;
    public static final int CHAIN_HITS = 3;
    public static final int MAX_SPLASH_TARGETS = 4;

    private FoodSocketAttributeRules() {
    }

    public static int priceBand(double d) {
        if (!Double.isFinite(d) || d <= 0.0) {
            return -1;
        }
        if (d <= 10.0) {
            return 0;
        }
        if (d <= 30.0) {
            return 1;
        }
        if (d <= 80.0) {
            return 2;
        }
        return 3;
    }

    public static Resolution resolve(List<Ingredient> list) {
        Object object6;
        Object object22;
        Object object32;
        int n;
        LinkedHashMap<String, Ingredient> linkedHashMap = new LinkedHashMap<String, Ingredient>();
        if (list != null) {
            for (Ingredient object42 : list) {
                if (linkedHashMap.size() >= 30) break;
                if (object42 == null || object42.id == null || !object42.id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) continue;
                linkedHashMap.putIfAbsent(object42.id, object42);
            }
        }
        Object object5 = new int[4];
        LinkedHashMap<String, Integer> linkedHashMap2 = new LinkedHashMap<String, Integer>();
        EnumSet<Color> enumSet = EnumSet.noneOf(Color.class);
        EnumSet<Processing> enumSet2 = EnumSet.noneOf(Processing.class);
        for (Object bl : linkedHashMap.values()) {
            n = FoodSocketAttributeRules.priceBand(((Ingredient)bl).basePrice);
            if (n >= 0) {
                Object object4 = object5;
                int n2 = n;
                object4[n2] = object4[n2] + true;
            }
            if (((Ingredient)bl).series != null && !((Ingredient)bl).series.isBlank()) {
                linkedHashMap2.merge(((Ingredient)bl).series, 1, Integer::sum);
            }
            if (((Ingredient)bl).color != null) {
                enumSet.add(((Ingredient)bl).color);
            }
            if (((Ingredient)bl).processing == null) continue;
            enumSet2.add(((Ingredient)bl).processing);
        }
        int n3 = 0;
        for (Object object32 : object5) {
            if (object32 <= 0) continue;
            ++n3;
        }
        boolean bl = n3 >= 3;
        n = object5[0] >= 3 ? 1 : 0;
        int n4 = Math.max(Math.min(3, (int)object5[0]), Math.min(3, n3));
        object32 = false;
        String string = "";
        for (Map.Entry n6 : linkedHashMap2.entrySet()) {
            if ((Integer)n6.getValue() <= object32) continue;
            object32 = (Integer)n6.getValue();
            string = (String)n6.getKey();
        }
        int n5 = 0;
        for (Object object22 : List.of(Color.RED, Color.GREEN, Color.GOLD)) {
            if (!enumSet.contains(object22)) continue;
            ++n5;
        }
        int n6 = 0;
        for (Object object6 : List.of(Processing.RAW, Processing.SIMPLE, Processing.COMPLEX)) {
            if (!enumSet2.contains(object6)) continue;
            ++n6;
        }
        object22 = EnumSet.noneOf(Power.class);
        if (n != 0 || bl) {
            ((AbstractCollection)object22).add(Power.PRICE_GUARD);
        }
        if (object32 >= 3) {
            ((AbstractCollection)object22).add(Power.SERIES_ECHO);
        }
        if (n5 == 3) {
            ((AbstractCollection)object22).add(Power.COLOR_REPEL);
        }
        if (n6 == 3) {
            ((AbstractCollection)object22).add(Power.PROCESS_BURST);
        }
        object6 = new ArrayList(linkedHashMap.values());
        object6.sort(Comparator.comparing(Ingredient::id));
        return new Resolution((List<Ingredient>)object6, n4, bl ? 4.0f : 3.0f, (int)object32, string, n5, n6, (Set<Power>)object22);
    }

    public static long deadline(long l, int n) {
        if (l < 0L || n < 0) {
            throw new IllegalArgumentException("Negative game time or duration");
        }
        return l > Long.MAX_VALUE - (long)n ? Long.MAX_VALUE : l + (long)n;
    }

    public static long boundedCooldown(long l, long l2, int n) {
        if (l2 < 0L || l2 > FoodSocketAttributeRules.deadline(l, n)) {
            return FoodSocketAttributeRules.deadline(l, n);
        }
        return l2;
    }

    public static boolean active(long l, long l2, int n) {
        return l >= 0L && l2 > l && l2 <= FoodSocketAttributeRules.deadline(l, n);
    }

    public static float absorb(float f, float f2) {
        if (!Float.isFinite(f) || !Float.isFinite(f2) || f <= 0.0f || f2 <= 0.0f) {
            return 0.0f;
        }
        return Math.min(f, f2);
    }

    public static int advance(int n, long l, long l2) {
        if (n < 0 || n >= 3 || l < 0L || l > l2 || l2 - l > 100L) {
            return 1;
        }
        return n + 1;
    }

    public record Ingredient(String id, String series, double basePrice, Color color, Processing processing) {
    }

    public static enum Color {
        UNKNOWN,
        RED,
        GREEN,
        GOLD,
        BROWN,
        WHITE,
        BLUE,
        PURPLE;

    }

    public static enum Processing {
        UNKNOWN,
        RAW,
        SIMPLE,
        COMPLEX;

    }

    public static enum Power {
        PRICE_GUARD,
        SERIES_ECHO,
        COLOR_REPEL,
        PROCESS_BURST;

    }

    public record Resolution(List<Ingredient> ingredients, int priceProgress, float guardHealth, int seriesProgress, String series, int colorProgress, int processProgress, Set<Power> active) {
        public Resolution {
            list = List.copyOf(list);
            set = Set.copyOf(set);
        }

        public boolean has(Power power) {
            return this.active.contains((Object)power);
        }
    }
}

