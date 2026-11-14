package net.farmingtales.ftboss.util;

public final class MaqueRicochetRules {
    public static final int FEATHERS = 5;
    public static final int HITS = 3;
    public static final int LIFETIME = 160;
    public static final int MAX_FLIGHTS = 320;
    public static final double AREA_RADIUS = 8;
    public static final double COLLISION_RADIUS = 0.28;
    public static final double SPEED = 1.38;

    private MaqueRicochetRules() {}

    public static boolean finished(int hits, long age, boolean ownerValid) {
        return !ownerValid || hits >= HITS || age < 0 || age >= LIFETIME;
    }

    public static int choose(int size, int previousIndex, double random) {
        if (size <= 0) return -1;
        boolean exclude = size > 1 && previousIndex >= 0 && previousIndex < size;
        int choices = size - (exclude ? 1 : 0);
        double roll = Double.isFinite(random) ? Math.max(0, Math.min(Math.nextDown(1.0), random)) : 0;
        int selected = Math.min(choices - 1, (int) (roll * choices));
        return exclude && selected >= previousIndex ? selected + 1 : selected;
    }

    public static double[] steer(double[] velocity, double[] displacement) {
        double distance = Math.sqrt(displacement[0] * displacement[0] + displacement[1] * displacement[1]
                + displacement[2] * displacement[2]);
        if (!(distance > 1e-8) || !Double.isFinite(distance)) return velocity.clone();
        double[] next = new double[3];
        double length = 0;
        for (int axis = 0; axis < 3; axis++) {
            next[axis] = velocity[axis] * 0.55 + displacement[axis] / distance * 0.62;
            length += next[axis] * next[axis];
        }
        double scale = length > SPEED * SPEED ? SPEED / Math.sqrt(length) : 1;
        for (int axis = 0; axis < 3; axis++) next[axis] *= scale;
        return next;
    }
}
