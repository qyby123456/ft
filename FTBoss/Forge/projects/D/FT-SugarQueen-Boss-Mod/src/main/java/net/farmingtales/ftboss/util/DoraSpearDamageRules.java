package net.farmingtales.ftboss.util;

public final class DoraSpearDamageRules {
    public static final double MOVEMENT_SPEED_SCALING = 3.0;

    private DoraSpearDamageRules() {}

    public static float damage(float baseDamage, double movementSpeed) {
        double speed = Double.isFinite(movementSpeed) ? Math.max(0.0, movementSpeed) : 0.0;
        return (float) (baseDamage + speed * MOVEMENT_SPEED_SCALING);
    }
}
