/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class TideFishCombatRules {
    public static final int PHASE_TICKS = 40;
    public static final int MINION_CAP = 6;
    private static final Skill[] ROTATION = Skill.values();

    private TideFishCombatRules() {
    }

    public static Skill next(int rotation) {
        return ROTATION[Math.floorMod(rotation, ROTATION.length)];
    }

    public static int volleyBolts(boolean enraged) {
        return enraged ? 7 : 5;
    }

    public static int summonCount(boolean enraged, int owned) {
        return Math.max(0, Math.min(enraged ? 5 : 3, 6 - owned));
    }

    public static int geyserCount(boolean enraged) {
        return enraged ? 5 : 3;
    }

    public static int maelstromCount(boolean enraged) {
        return enraged ? 3 : 1;
    }

    public static int cooldown(boolean enraged) {
        return enraged ? 10 : 15;
    }

    public static int openingCooldown() {
        return 18;
    }

    public static boolean volleyAt(int elapsed, boolean enraged) {
        return elapsed == 5 || enraged && (elapsed == 12 || elapsed == 19);
    }

    public static boolean sweepAt(int elapsed, boolean enraged) {
        return elapsed == 4 || enraged && elapsed == 15;
    }

    public static boolean dashActive(int elapsed, boolean enraged) {
        return elapsed >= 5 && elapsed <= 17 || enraged && elapsed >= 31 && elapsed <= 43;
    }

    public static int waveAge(int elapsed, boolean enraged) {
        if (enraged && elapsed >= 25 && elapsed <= 42) {
            return elapsed - 25;
        }
        return elapsed >= 6 && elapsed <= 23 ? elapsed - 6 : -1;
    }

    public static int geyserAt(int elapsed, boolean enraged) {
        int offset = elapsed - 7;
        return offset >= 0 && offset % 5 == 0 && offset / 5 < TideFishCombatRules.geyserCount(enraged) ? offset / 5 : -1;
    }

    public static int maelstromAt(int elapsed, boolean enraged) {
        int offset = elapsed - 8;
        return offset >= 0 && offset % 7 == 0 && offset / 7 < TideFishCombatRules.maelstromCount(enraged) ? offset / 7 : -1;
    }

    public static enum Skill {
        SWEEP("sweep", 19, 29, 4, 24.0f, 32.0f),
        VOLLEY("volley", 22, 31, 5, 16.0f, 20.0f),
        DASH("dash", 25, 44, 5, 32.0f, 40.0f),
        SUMMON("summon", 25, 25, 6, 0.0f, 0.0f),
        WAVE("wave", 31, 47, 6, 28.0f, 36.0f),
        MAELSTROM("maelstrom", 31, 41, 8, 36.0f, 44.0f),
        GEYSER("geyser", 30, 41, 7, 32.0f, 40.0f);

        public final String id;
        public final int firstDuration;
        public final int secondDuration;
        public final int windup;
        public final float firstDamage;
        public final float secondDamage;

        private Skill(String id, int firstDuration, int secondDuration, int windup, float firstDamage, float secondDamage) {
            this.id = id;
            this.firstDuration = firstDuration;
            this.secondDuration = secondDuration;
            this.windup = windup;
            this.firstDamage = firstDamage;
            this.secondDamage = secondDamage;
        }

        public int duration(boolean enraged) {
            return enraged ? this.secondDuration : this.firstDuration;
        }

        public float damage(boolean enraged) {
            return enraged ? this.secondDamage : this.firstDamage;
        }
    }
}

