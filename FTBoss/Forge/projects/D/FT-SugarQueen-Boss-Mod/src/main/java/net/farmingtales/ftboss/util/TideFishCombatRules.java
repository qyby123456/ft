package net.farmingtales.ftboss.util;

public final class TideFishCombatRules {
    public static final int PHASE_TICKS = 40;
    public static final int MINION_CAP = 6;

    // 2026-09-17 第二轮（用户口径：前摇再短一半、出招更快、一次连放两招）：
    //   前摇 -50%（7/9/9/12/11/16/13 -> 4/5/5/6/6/8/7 tick），动画时长按同一差值整体前移
    //   （19/29、22/31、25/44、25/25、32/48、31/41、30/41），出招间隔 25/17 -> 15/10，首次出手 30 -> 18。
    // windup 一律等于该技能第一段伤害帧，改前摇时必须同时改下面的 at()/Active/age 方法
    // 以及 tools/model3d/tide_fish_rework.cjs 的关键帧（动画与伤害帧必须对齐）。
    public enum Skill {
        SWEEP("sweep", 19, 29, 4, 24, 32),
        VOLLEY("volley", 22, 31, 5, 16, 20),
        DASH("dash", 25, 44, 5, 32, 40),
        SUMMON("summon", 25, 25, 6, 0, 0),
        WAVE("wave", 31, 47, 6, 28, 36),
        MAELSTROM("maelstrom", 31, 41, 8, 36, 44),
        GEYSER("geyser", 30, 41, 7, 32, 40);

        public final String id;
        public final int firstDuration;
        public final int secondDuration;
        public final int windup;
        public final float firstDamage;
        public final float secondDamage;

        Skill(String id, int firstDuration, int secondDuration, int windup, float firstDamage, float secondDamage) {
            this.id = id;
            this.firstDuration = firstDuration;
            this.secondDuration = secondDuration;
            this.windup = windup;
            this.firstDamage = firstDamage;
            this.secondDamage = secondDamage;
        }

        public int duration(boolean enraged) { return enraged ? this.secondDuration : this.firstDuration; }
        public float damage(boolean enraged) { return enraged ? this.secondDamage : this.firstDamage; }
    }

    private static final Skill[] ROTATION = Skill.values();

    private TideFishCombatRules() {}

    public static Skill next(int rotation) { return ROTATION[Math.floorMod(rotation, ROTATION.length)]; }
    public static int volleyBolts(boolean enraged) { return enraged ? 7 : 5; }
    public static int summonCount(boolean enraged, int owned) { return Math.max(0, Math.min(enraged ? 5 : 3, MINION_CAP - owned)); }
    public static int geyserCount(boolean enraged) { return enraged ? 5 : 3; }
    public static int maelstromCount(boolean enraged) { return enraged ? 3 : 1; }
    public static int cooldown(boolean enraged) { return enraged ? 10 : 15; }
    public static int openingCooldown() { return 18; }

    public static boolean volleyAt(int elapsed, boolean enraged) {
        return elapsed == 5 || (enraged && (elapsed == 12 || elapsed == 19));
    }

    public static boolean sweepAt(int elapsed, boolean enraged) { return elapsed == 4 || (enraged && elapsed == 15); }

    public static boolean dashActive(int elapsed, boolean enraged) {
        return elapsed >= 5 && elapsed <= 17 || enraged && elapsed >= 31 && elapsed <= 43;
    }

    public static int waveAge(int elapsed, boolean enraged) {
        if (enraged && elapsed >= 25 && elapsed <= 42) return elapsed - 25;
        return elapsed >= 6 && elapsed <= 23 ? elapsed - 6 : -1;
    }

    public static int geyserAt(int elapsed, boolean enraged) {
        int offset = elapsed - 7;
        return offset >= 0 && offset % 5 == 0 && offset / 5 < geyserCount(enraged) ? offset / 5 : -1;
    }

    public static int maelstromAt(int elapsed, boolean enraged) {
        int offset = elapsed - 8;
        return offset >= 0 && offset % 7 == 0 && offset / 7 < maelstromCount(enraged) ? offset / 7 : -1;
    }
}
