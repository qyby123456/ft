package net.farmingtales.ftboss.boss.ai;

public final class ZhuniPairRules {
    public static final float HEALTH = 2000.0F;
    public static final double BIRD_ARMOR = 55.0;
    public static final int FLYWHEELS = 15;
    public static final int BUILD_TICKS = 10;
    public static final int TRAVEL_TIMEOUT = 80;
    public static final int RETURN_TICKS = 20;

    private ZhuniPairRules() {}

    public static int summons(boolean partnerDefeated, int normal) {
        return partnerDefeated ? normal * 2 : normal;
    }

    public static float progress(float xuanlan, float zhuni, boolean xuanlanDefeated, boolean zhuniDefeated) {
        float first = xuanlanDefeated ? 0 : Math.max(0, Math.min(HEALTH, xuanlan));
        float second = zhuniDefeated ? 0 : Math.max(0, Math.min(HEALTH, zhuni));
        return (first + second) / (HEALTH * 2);
    }
}
