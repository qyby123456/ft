/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class QizaiRules {
    public static final int FAN = 1;
    public static final int CIRCLE = 2;
    public static final int PRAYER = 3;
    public static final int STEP = 4;
    public static final double STEP_TRIGGER_RANGE = 5.0;
    public static final float STEP_CHANCE = 0.4f;
    public static final int STEP_RETRY_TICKS = 20;
    public static final int STEP_COOLDOWN = 80;
    public static final double STEP_MIN_DISTANCE = 4.0;
    public static final double STEP_MAX_DISTANCE = 8.0;
    public static final int STAR_CROSS = 5;
    public static final int ROSE_STARFALL = 6;
    public static final int RELEASES = 3;
    public static final int REPEAT_INTERVAL = 12;
    public static final int SPELL_DAMAGE_MULTIPLIER = 2;
    public static final double CROSS_LENGTH = 12.0;
    public static final double CROSS_WIDTH = 1.4;
    public static final double STARFALL_RADIUS = 3.2;
    public static final double STARFALL_OFFSET = 7.6;
    public static final int STARFALL_INTERVAL = 8;
    public static final float CROSS_DAMAGE = 104.0f;
    public static final float STARFALL_DAMAGE = 64.0f;
    public static final int RECOVERY = 24;
    public static final int REST = 32;
    public static final int COOLDOWN_MULTIPLIER = 3;
    public static final int STAR_LIFETIME = 60;
    public static final double RANGE = 32.0;
    public static final double STAR_SPEED = 0.8;
    public static final double CIRCLE_RADIUS = 5.6;
    public static final double PRAYER_RADIUS = 6.4;
    public static final float STAR_DAMAGE = 48.0f;
    public static final float CIRCLE_DAMAGE = 80.0f;
    public static final float PRAYER_DAMAGE = 128.0f;
    public static final int SWEEP = 7;
    public static final int SKY_NOVA = 8;
    public static final int PRISM_LOCK = 9;
    public static final int LASER_BARRAGE = 10;
    private static final int[] PATTERN = new int[]{1, 2, 5, 1, 2, 1, 6, 3, 7, 8, 9, 10};

    public static int cooldown(int ticks) {
        return Math.max(1, (ticks + 3 - 1) / 3);
    }

    private QizaiRules() {
    }

    public static int next(int sequence) {
        return PATTERN[Math.floorMod(sequence, PATTERN.length)];
    }

    public static int repeatTicks() {
        return 24;
    }

    public static boolean constellation(int kind) {
        return kind == 5 || kind == 6;
    }

    public static boolean sweep(int kind) {
        return kind == 7;
    }

    public static boolean skyNova(int kind) {
        return kind == 8;
    }

    public static boolean prismLock(int kind) {
        return kind == 9;
    }

    public static boolean laserBarrage(int kind) {
        return kind == 10;
    }

    public static boolean spell(int kind) {
        return kind >= 1 && kind <= 3 || QizaiRules.constellation(kind) || QizaiRules.sweep(kind) || QizaiRules.skyNova(kind) || QizaiRules.prismLock(kind) || QizaiRules.laserBarrage(kind);
    }

    public static int windup(int kind) {
        return QizaiRules.laserBarrage(kind) ? 14 : (QizaiRules.prismLock(kind) ? 36 : (QizaiRules.skyNova(kind) ? 120 : (QizaiRules.sweep(kind) ? 30 : (kind == 5 ? 18 : (kind == 6 ? 20 : (kind == 3 ? 30 : (kind == 2 ? 16 : 12)))))));
    }

    public static int recovery(int kind) {
        return QizaiRules.laserBarrage(kind) ? 20 : (QizaiRules.prismLock(kind) ? 20 : (QizaiRules.skyNova(kind) ? 20 : (QizaiRules.sweep(kind) ? 24 : (kind == 6 ? 40 : 24))));
    }

    public static double radius(int kind) {
        return QizaiRules.laserBarrage(kind) ? 40.0 : (QizaiRules.prismLock(kind) ? 40.0 : (QizaiRules.skyNova(kind) ? 48.0 : (QizaiRules.sweep(kind) ? 32.0 : (kind == 5 ? 12.0 : (kind == 6 ? 3.2 : (kind == 3 ? 6.4 : 5.6))))));
    }

    public static String id(int kind) {
        return QizaiRules.laserBarrage(kind) ? "laser_barrage" : (QizaiRules.prismLock(kind) ? "prism_lock" : (QizaiRules.skyNova(kind) ? "sky_nova" : (QizaiRules.sweep(kind) ? "star_sweep" : (kind == 5 ? "star_cross" : "rose_starfall"))));
    }

    public static boolean cross(double offsetX, double height, double offsetZ) {
        return Double.isFinite(offsetX + height + offsetZ) && Math.abs(height) <= 2.5 && (Math.abs(offsetX) <= 12.0 && Math.abs(offsetZ) <= 1.4 || Math.abs(offsetZ) <= 12.0 && Math.abs(offsetX) <= 1.4);
    }

    public static boolean inside(double horizontalSquared, double height, int kind) {
        double radius = QizaiRules.radius(kind);
        return Double.isFinite(horizontalSquared) && Double.isFinite(height) && horizontalSquared >= 0.0 && horizontalSquared <= radius * radius && Math.abs(height) <= 2.5;
    }
}

