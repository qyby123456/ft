package net.farmingtales.ftboss.util;

public final class QizaiArsenalRules {
    public static final int CRYSTALS = 6;
    public static final int MISSILES = 12;
    public static final int CHARGE = 24;
    public static final int LASER_GAP = 2;
    public static final int LASER_FADE = 8;
    public static final int SALVO_DURATION = CHARGE + (CRYSTALS - 1) * LASER_GAP + LASER_FADE;
    public static final int MAX_SALVOS = 4;
    public static final int COOLDOWN = 180;
    public static final double RANGE = 32;
    public static final double LASER_WIDTH = 0.45;
    public static final float LASER_DAMAGE = 48;
    public static final int MISSILE_LIFE = 60;
    public static final double MISSILE_SPEED = 0.58;
    public static final double MISSILE_TURN = Math.toRadians(6);
    public static final double PUSH_TRIGGER = 5;
    public static final double PUSH_RADIUS = 7;
    public static final int PUSH_WINDUP = 16;
    public static final int PUSH_FADE = 12;
    public static final int PUSH_COOLDOWN = 100;
    private QizaiArsenalRules() {}

    public static boolean insidePush(double horizontalSquared, double height) {
        return Double.isFinite(horizontalSquared + height) && horizontalSquared >= 0
                && horizontalSquared <= PUSH_RADIUS * PUSH_RADIUS && Math.abs(height) <= 3;
    }
}
