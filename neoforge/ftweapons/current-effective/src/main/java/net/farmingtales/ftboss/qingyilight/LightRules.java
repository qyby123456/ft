package net.farmingtales.ftboss.qingyilight;

public final class LightRules {
    public static final int TIDE = 0;
    public static final int THUNDER = 1;
    public static final int STAR = 2;
    public static final int CONFLUENCE = 3;
    public static final int DASH = 4;
    public static final int BLINK = 5;
    public static final int BIRTH = 6;
    public static final int IMPACT = 7;
    public static final int POTION = 8;
    public static final int CHARGE = 9;
    public static final int ORB = 10;
    public static final int CANCEL = 11;
    public static final int MAX_EVENTS = 96;
    public static final int MAX_TRAILS = 48;
    public static final int MAX_HISTORY = 7;

    private LightRules() {
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double opacity(double var0, int var2) {
        return Double.isFinite(var0) && var0 >= 0.0 && var0 < (double)var2
            ? smooth((var0 + 0.25) / 2.0) * smooth(((double)var2 - var0) / Math.min(6.0, (double)var2 * 0.4))
            : 0.0;
    }

    public static boolean valid(int var0, int var1, int var2, double var3, int var5) {
        return var0 >= 0
            && var0 <= 11
            && var1 >= 0
            && var1 <= 2
            && var2 >= 1
            && var2 <= 640
            && Double.isFinite(var3)
            && var3 >= 0.0
            && var3 <= 32.0
            && var5 >= 0
            && var5 <= 8;
    }

    public static boolean point(double var0, double var2, double var4) {
        return Double.isFinite(var0)
            && Double.isFinite(var2)
            && Double.isFinite(var4)
            && Math.abs(var0) <= 3.0E7
            && Math.abs(var4) <= 3.0E7
            && Math.abs(var2) <= 4096.0;
    }

    public static boolean direction(double var0, double var2, double var4) {
        return Double.isFinite(var0) && Double.isFinite(var2) && Double.isFinite(var4) && var0 * var0 + var2 * var2 + var4 * var4 <= 1.00001;
    }

    public static int budget(int var0) {
        return var0 == 2 ? 1800 : (var0 == 1 ? 3600 : 6400);
    }

    public static double cameraOpacity(double var0, double var2, double var4, double var6, double var8, double var10) {
        double var12 = Math.max(0.0, Math.max(var0, -var6));
        double var14 = Math.max(0.0, Math.max(var2, -var8));
        double var16 = Math.max(0.0, Math.max(var4, -var10));
        return smooth((Math.sqrt(var12 * var12 + var14 * var14 + var16 * var16) - 0.3) / 0.9);
    }

    public static boolean trailSegment(double var0) {
        return Double.isFinite(var0) && var0 > 1.0E-6 && var0 <= 16.0;
    }
}
