package net.farmingtales.ftboss.client.render;

public final class GongdiAnimationPriority {
    private static final float ACTIVE_WEIGHT = 0.001f;

    private GongdiAnimationPriority() {
    }

    public static boolean ordinaryAttack(boolean casting, float actionWeight) {
        return !casting && actionWeight > ACTIVE_WEIGHT;
    }

    public static boolean nativeCue(boolean active, boolean released, boolean casting, float actionWeight) {
        return active && !(released && ordinaryAttack(casting, actionWeight));
    }

    public static Object spellPose(Object pose, boolean casting, float actionWeight) {
        return ordinaryAttack(casting, actionWeight) ? null : pose;
    }
}
