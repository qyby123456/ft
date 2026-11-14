package net.farmingtales.ftboss.util;

public final class SayaFleshVisionRules {
    public static final int TEXTURE_WIDTH = 1024;
    public static final int TEXTURE_HEIGHT = 576;
    public static final int PERIOD_TICKS = 36;
    public static final int FRAME_COUNT = 12;
    public static final int FRAME_TICKS = PERIOD_TICKS / FRAME_COUNT;
    public static final int FRAME_WIDTH = 512;
    public static final int FRAME_HEIGHT = 288;
    public static final int ATLAS_COLUMNS = 4;
    public static final int ATLAS_WIDTH = FRAME_WIDTH * ATLAS_COLUMNS;
    public static final int ATLAS_HEIGHT = FRAME_HEIGHT * (FRAME_COUNT / ATLAS_COLUMNS);
    public static final int MAX_ARMOR_PIECES = 4;

    private SayaFleshVisionRules() {}

    public static int clampPieces(int pieces) {
        return Math.max(0, Math.min(MAX_ARMOR_PIECES, pieces));
    }

    public static float intensity(int pieces) {
        return clampPieces(pieces) / (float) MAX_ARMOR_PIECES;
    }

    public static int animationFrame(double ticks) {
        if (!Double.isFinite(ticks)) return 0;
        double wrapped = (ticks % PERIOD_TICKS + PERIOD_TICKS) % PERIOD_TICKS;
        return Math.min(FRAME_COUNT - 1, (int) (wrapped / FRAME_TICKS));
    }

    public static int bloodTint(int pieces) {
        int alpha = Math.round(12.0F * intensity(pieces));
        return alpha << 24 | 0x681626;
    }

    public static double heartbeat(double ticks) {
        if (!Double.isFinite(ticks)) return 0;
        double phase = ((ticks % PERIOD_TICKS + PERIOD_TICKS) % PERIOD_TICKS) / PERIOD_TICKS;
        double primary = (phase - 0.23) / 0.075;
        double secondary = (phase - 0.43) / 0.09;
        return Math.min(1, Math.exp(-primary * primary) + 0.42 * Math.exp(-secondary * secondary));
    }

    public static float fleshAlpha(double ticks, int pieces) {
        float intensity = intensity(pieces);
        if (intensity <= 0) return 0;
        return (float) (0.14 + 0.79 * intensity + 0.05 * intensity * heartbeat(ticks));
    }

    public static float veinAlpha(double ticks, int pieces) {
        float intensity = intensity(pieces);
        if (intensity <= 0) return 0;
        return (float) (0.06 + 0.52 * intensity + 0.18 * intensity * heartbeat(ticks));
    }

    /** Compatibility overloads describe the complete four-piece set. */
    public static float fleshAlpha(double ticks) { return fleshAlpha(ticks, MAX_ARMOR_PIECES); }
    public static float veinAlpha(double ticks) { return veinAlpha(ticks, MAX_ARMOR_PIECES); }
}
