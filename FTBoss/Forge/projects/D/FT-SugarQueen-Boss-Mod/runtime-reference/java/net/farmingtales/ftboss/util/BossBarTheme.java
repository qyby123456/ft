/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.HashMap;
import java.util.Map;

public record BossBarTheme(String id, String displayKey, int frameColor, int fillTop, int fillBottom, int accent, Emblem emblem, float[] phaseTicks, String[] phaseNames, boolean rainbowFill) {
    public static final int[] MANOR_VARIANT_COLORS = new int[]{-1519046, -858456, -2065604, -7573878, -3484086, -12605152, -11898912, -9459524, -8762264, -5207462, -4199822, -2077574};
    public static final int MANOR_FALLBACK_COLOR = -6645382;
    private static final BossBarTheme[] ALL = new BossBarTheme[]{new BossBarTheme("sugar_queen", "ftboss.bar.sugar_queen", -8399371, -1639425, -9459224, -1, Emblem.SNOWFLAKE, new float[0], new String[0], false), new BossBarTheme("meat_mountain", "ftboss.bar.meat_mountain", -7463397, -47808, -10612720, -7470, Emblem.CLEAVER, new float[0], new String[0], false), new BossBarTheme("tea_tree", "ftboss.bar.tea_tree", -11632054, -6629011, -13280726, -1443644, Emblem.TEA_LEAF, new float[]{0.5f}, new String[]{"ftboss.bar.phase.2"}, false), new BossBarTheme("wine_barrel", "ftboss.bar.wine_barrel", -8762794, -2054035, -9621172, -5940, Emblem.BARREL_HOOP, new float[]{0.5f}, new String[]{"ftboss.bar.phase.2"}, false), new BossBarTheme("rat_boss", "ftboss.bar.rat_boss", -10852260, -6518154, -12569302, -865941, Emblem.PAW, new float[]{0.5f}, new String[]{"ftboss.bar.phase.2"}, false), new BossBarTheme("defect_core", "ftboss.bar.defect_core", -14719368, -8390683, -15254440, -4947201, Emblem.CIRCUIT, new float[]{0.7f, 0.35f}, new String[]{"ftboss.bar.phase.2", "ftboss.bar.phase.3"}, false), new BossBarTheme("compost_boss", "ftboss.bar.compost_boss", -12603025, -38027, -8758529, -6422593, Emblem.GOO, new float[]{0.7f, 0.35f}, new String[]{"ftboss.bar.phase.2", "ftboss.bar.phase.3"}, true), new BossBarTheme("apple_boss", "ftboss.bar.apple_boss", -7706030, -8054, -4617388, -12608166, Emblem.APPLE, new float[]{0.5f}, new String[]{"ftboss.bar.phase.2"}, false), new BossBarTheme("benger", "ftboss.bar.benger", -10783642, -5712230, -12691640, -1186107, Emblem.BONE, new float[]{0.5f, 0.4f}, new String[]{"ftboss.bar.phase.2", "ftboss.bar.phase.rage"}, false), new BossBarTheme("tide_fish_boss", "ftboss.bar.tide_fish_boss", -14787462, -11026712, -15644833, -6621953, Emblem.TIDE, new float[]{0.5f}, new String[]{"ftboss.bar.phase.2"}, false), new BossBarTheme("easter_boss", "ftboss.bar.easter_boss", -9545305, -2513426, -9552490, -12176, Emblem.MANOR_CREST, new float[0], new String[0], false)};
    private static final Map<String, BossBarTheme> BY_ID = new HashMap<String, BossBarTheme>();

    public static BossBarTheme[] all() {
        return (BossBarTheme[])ALL.clone();
    }

    public static BossBarTheme byId(String id) {
        return id != null && !id.isEmpty() ? BY_ID.get(id) : null;
    }

    public static BossBarTheme byEntityPath(String path) {
        return BossBarTheme.byId(path);
    }

    public static boolean hasTheme(String path) {
        return BossBarTheme.byEntityPath(path) != null;
    }

    public int phaseIndex(float ratio) {
        float value = BossBarTheme.clamp01(ratio);
        int phase = 0;
        for (float tick : this.phaseTicks) {
            if (!(value < tick)) continue;
            ++phase;
        }
        return Math.min(phase, this.phaseTicks.length);
    }

    public String phaseNameKey(int phase) {
        return phase > 0 && phase <= this.phaseNames.length ? this.phaseNames[phase - 1] : null;
    }

    public int phaseCount() {
        return this.phaseTicks.length;
    }

    public float phaseTick(int index) {
        return index >= 0 && index < this.phaseTicks.length ? this.phaseTicks[index] : -1.0f;
    }

    public boolean hasPhaseTicks() {
        return this.phaseTicks.length > 0;
    }

    public static int manorVariantColor(int variant) {
        return variant >= 0 && variant < MANOR_VARIANT_COLORS.length ? MANOR_VARIANT_COLORS[variant] : -6645382;
    }

    public static float clamp01(float value) {
        return Float.isNaN(value) ? 0.0f : Math.max(0.0f, Math.min(1.0f, value));
    }

    static {
        for (BossBarTheme theme : ALL) {
            BY_ID.put(theme.id, theme);
        }
    }

    public static enum Emblem {
        SNOWFLAKE("...#...", ".#.#.#.", "..###..", "#######", "..###..", ".#.#.#.", "...#..."),
        CLEAVER(".#####.", "#######", "#######", ".#####.", "#......", "##.....", "......."),
        TEA_LEAF("...#...", "..###..", ".#####.", "#######", ".#####.", "..###..", "...#..."),
        BARREL_HOOP("#.#.#.#", "#.#.#.#", "#######", "#.#.#.#", "#.#.#.#", "#######", "#.#.#.#"),
        PAW(".#.#.#.", "#.#.#.#", ".#.#.#.", ".......", ".#####.", "#######", ".#####."),
        CIRCUIT("#######", "#.....#", "#.###.#", "#.###.#", "#.###.#", "#.....#", "#######"),
        GOO("..###..", ".#####.", "#######", "#######", "#######", ".#####.", "..#.#.."),
        APPLE("....##.", "...##..", ".#####.", "#######", "#######", "#######", ".#####."),
        BONE("##...##", "##...##", "#######", "#######", "#######", "##...##", "##...##"),
        TIDE(".......", ".##.##.", "#..#..#", ".......", ".##.##.", "#..#..#", "......."),
        MANOR_CREST("#######", "#######", "#######", ".#####.", ".#####.", "..###..", "...#...");

        public static final int GRID = 7;
        private final String[] rows;

        private Emblem(String ... rows) {
            this.rows = rows;
        }

        public String[] rows() {
            return this.rows;
        }
    }
}

