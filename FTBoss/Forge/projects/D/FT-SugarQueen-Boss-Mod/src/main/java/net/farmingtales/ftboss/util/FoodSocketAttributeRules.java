package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 食材属性纯规则：固定基础价格、模组系列、人工颜色和加工链；2026-10-12，重复食材不计数。 */
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

    public enum Color { UNKNOWN, RED, GREEN, GOLD, BROWN, WHITE, BLUE, PURPLE }
    public enum Processing { UNKNOWN, RAW, SIMPLE, COMPLEX }
    public enum Power { PRICE_GUARD, SERIES_ECHO, COLOR_REPEL, PROCESS_BURST }

    public record Ingredient(String id, String series, double basePrice, Color color, Processing processing) {}
    public record Resolution(List<Ingredient> ingredients, int priceProgress, float guardHealth,
                             int seriesProgress, String series, int colorProgress, int processProgress,
                             Set<Power> active) {
        public Resolution {
            ingredients = List.copyOf(ingredients);
            active = Set.copyOf(active);
        }

        public boolean has(Power power) { return this.active.contains(power); }
    }

    private FoodSocketAttributeRules() {}

    public static int priceBand(double price) {
        if (!Double.isFinite(price) || price <= 0.0D) return -1;
        if (price <= 10.0D) return 0;
        if (price <= 30.0D) return 1;
        if (price <= 80.0D) return 2;
        return 3;
    }

    public static Resolution resolve(List<Ingredient> installed) {
        Map<String, Ingredient> unique = new LinkedHashMap<>();
        if (installed != null) {
            for (Ingredient food : installed) {
                if (unique.size() >= MAX_SOCKETS) break;
                if (food == null || food.id == null || !food.id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) continue;
                unique.putIfAbsent(food.id, food);
            }
        }
        int[] bands = new int[4];
        Map<String, Integer> seriesCounts = new LinkedHashMap<>();
        EnumSet<Color> colors = EnumSet.noneOf(Color.class);
        EnumSet<Processing> processing = EnumSet.noneOf(Processing.class);
        for (Ingredient food : unique.values()) {
            int band = priceBand(food.basePrice);
            if (band >= 0) bands[band]++;
            if (food.series != null && !food.series.isBlank()) seriesCounts.merge(food.series, 1, Integer::sum);
            if (food.color != null) colors.add(food.color);
            if (food.processing != null) processing.add(food.processing);
        }
        int priceDiversity = 0;
        for (int count : bands) if (count > 0) priceDiversity++;
        boolean fullCourse = priceDiversity >= 3;
        boolean economical = bands[0] >= 3;
        int priceProgress = Math.max(Math.min(3, bands[0]), Math.min(3, priceDiversity));
        int seriesProgress = 0;
        String series = "";
        for (Map.Entry<String, Integer> entry : seriesCounts.entrySet()) {
            if (entry.getValue() > seriesProgress) {
                seriesProgress = entry.getValue();
                series = entry.getKey();
            }
        }
        int colorProgress = 0;
        for (Color color : List.of(Color.RED, Color.GREEN, Color.GOLD)) if (colors.contains(color)) colorProgress++;
        int processProgress = 0;
        for (Processing stage : List.of(Processing.RAW, Processing.SIMPLE, Processing.COMPLEX)) {
            if (processing.contains(stage)) processProgress++;
        }
        EnumSet<Power> active = EnumSet.noneOf(Power.class);
        if (economical || fullCourse) active.add(Power.PRICE_GUARD);
        if (seriesProgress >= 3) active.add(Power.SERIES_ECHO);
        if (colorProgress == 3) active.add(Power.COLOR_REPEL);
        if (processProgress == 3) active.add(Power.PROCESS_BURST);
        List<Ingredient> ingredients = new ArrayList<>(unique.values());
        ingredients.sort(java.util.Comparator.comparing(Ingredient::id));
        return new Resolution(ingredients, priceProgress, fullCourse ? 4.0F : 3.0F, seriesProgress, series,
                colorProgress, processProgress, active);
    }

    public static long deadline(long now, int ticks) {
        if (now < 0 || ticks < 0) throw new IllegalArgumentException("Negative game time or duration");
        return now > Long.MAX_VALUE - ticks ? Long.MAX_VALUE : now + ticks;
    }

    public static long boundedCooldown(long now, long stored, int duration) {
        if (stored < 0 || stored > deadline(now, duration)) return deadline(now, duration);
        return stored;
    }

    public static boolean active(long now, long until, int duration) {
        return now >= 0 && until > now && until <= deadline(now, duration);
    }

    public static float absorb(float incoming, float remaining) {
        if (!Float.isFinite(incoming) || !Float.isFinite(remaining) || incoming <= 0.0F || remaining <= 0.0F) return 0.0F;
        return Math.min(incoming, remaining);
    }

    public static int advance(int previous, long previousHit, long now) {
        if (previous < 0 || previous >= CHAIN_HITS || previousHit < 0 || previousHit > now
                || now - previousHit > CHAIN_WINDOW_TICKS) return 1;
        return previous + 1;
    }
}
