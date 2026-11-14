package net.farmingtales.ftboss.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiSweepRules {
    public static final String ID = "star_sweep";
    public static final String VISUAL_BEAM_ID = "star_cross";
    public static final String VISUAL_RING_ID = "rose_starfall";
    public static final String NAME = "绯星指针";
    public static final int WINDUP = 30;
    public static final int SWEEP_TICKS = 100;
    public static final int RAIN_WAVES = 5;
    public static final int RAIN_GAP = 10;
    public static final int RAIN_COUNT = 24;
    public static final int RECOVERY = QizaiRules.RECOVERY;
    public static final int REST_MULTIPLIER = 2;
    public static final int ARMS = 4;
    public static final double RADIUS = 32.0;
    public static final double LOW_HEIGHT = 0.9;
    public static final double HIGH_HEIGHT = 2.1;
    public static final double DEGREES_PER_TICK = -1.325;
    public static final double HIGH_DEGREES_PER_TICK = -0.75;
    public static final double BEAM_HALF_WIDTH = 1.3;
    public static final float BEAM_DAMAGE = QizaiRules.STAR_DAMAGE;
    public static final int BEAM_HIT_COOLDOWN = 20;
    public static final int VOLLEY_INTERVAL = 15;
    public static final int VOLLEY_STARS = 8;
    public static final double VOLLEY_TURN_DEGREES = 4.0;
    public static final int VOLLEY_LIFETIME = 40;
    public static final double VOLLEY_SPEED = 0.55;
    public static final double RAIN_SPEED = 0.7;
    public static final double RAIN_ROTATION_PER_WAVE = 360.0 / RAIN_COUNT / 2;
    public static final float STAR_DAMAGE = QizaiRules.STAR_DAMAGE;

    private QizaiSweepRules() {}

    public static int arms(int round) { return round >= 2 ? ARMS * 2 : ARMS; }

    public static boolean high(int round, int arm) { return round == 1 || round >= 2 && arm >= ARMS; }

    public static double height(int round, int arm) { return high(round, arm) ? HIGH_HEIGHT : LOW_HEIGHT; }

    public static double angle(double initial, double elapsed, int round, int arm) {
        return wrapDegrees(initial + (arm % ARMS) * 90 + (round >= 2 && high(round, arm) ? 45 : 0)
                + elapsed * (high(round, arm) ? HIGH_DEGREES_PER_TICK : DEGREES_PER_TICK));
    }

    public static double wrapDegrees(double degrees) {
        double value = degrees % 360.0;
        if (value > 180.0) value -= 360.0;
        if (value <= -180.0) value += 360.0;
        return Double.isFinite(value) ? value : 0;
    }

    public static boolean crosses(double beamAngle, double playerAngle, double distance) {
        double tolerance = Math.toDegrees(Math.atan2(BEAM_HALF_WIDTH, Math.max(1.0, distance)));
        return Math.abs(wrapDegrees(playerAngle - beamAngle)) <= tolerance;
    }

    public static boolean hitsHeight(AABB box, double baseY, int arm) {
        double beamY = baseY + (arm == 0 ? LOW_HEIGHT : HIGH_HEIGHT);
        return box.minY <= beamY && beamY <= box.maxY;
    }

    public static boolean wouldHit(double feetY, double height, double baseY, int arm) {
        return hitsHeight(new AABB(0, feetY, 0, 1, feetY + height, 1), baseY, arm);
    }

    public static Vec3 turn(Vec3 velocity, Vec3 target) {
        if (velocity.lengthSqr() < 1.0E-8 || target.lengthSqr() < 1.0E-8) return velocity;
        Vec3 current = velocity.normalize();
        Vec3 desired = target.normalize();
        double dot = Math.max(-1, Math.min(1, current.dot(desired)));
        double limit = Math.toRadians(VOLLEY_TURN_DEGREES);
        if (Math.acos(dot) <= limit) return desired.scale(VOLLEY_SPEED);
        Vec3 tangent = desired.subtract(current.scale(dot));
        if (tangent.lengthSqr() < 1.0E-8) {
            tangent = current.cross(Math.abs(current.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0));
        }
        return current.scale(Math.cos(limit)).add(tangent.normalize().scale(Math.sin(limit))).scale(VOLLEY_SPEED);
    }
}
