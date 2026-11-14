/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class QizaiSweepRules {
    public static final String ID = "star_sweep";
    public static final String VISUAL_BEAM_ID = "star_cross";
    public static final String VISUAL_RING_ID = "rose_starfall";
    public static final String NAME = "\u7eef\u661f\u6307\u9488";
    public static final int WINDUP = 30;
    public static final int SWEEP_TICKS = 100;
    public static final int RAIN_WAVES = 5;
    public static final int RAIN_GAP = 10;
    public static final int RAIN_COUNT = 24;
    public static final int RECOVERY = 24;
    public static final int REST_MULTIPLIER = 2;
    public static final int ARMS = 4;
    public static final double RADIUS = 32.0;
    public static final double LOW_HEIGHT = 0.9;
    public static final double HIGH_HEIGHT = 2.1;
    public static final double DEGREES_PER_TICK = -1.325;
    public static final double HIGH_DEGREES_PER_TICK = -0.75;
    public static final double BEAM_HALF_WIDTH = 1.3;
    public static final float BEAM_DAMAGE = 48.0f;
    public static final int BEAM_HIT_COOLDOWN = 20;
    public static final int VOLLEY_INTERVAL = 15;
    public static final int VOLLEY_STARS = 8;
    public static final double VOLLEY_TURN_DEGREES = 4.0;
    public static final int VOLLEY_LIFETIME = 40;
    public static final double VOLLEY_SPEED = 0.55;
    public static final double RAIN_SPEED = 0.7;
    public static final double RAIN_ROTATION_PER_WAVE = 7.5;
    public static final float STAR_DAMAGE = 48.0f;

    private QizaiSweepRules() {
    }

    public static int arms(int round) {
        return round >= 2 ? 8 : 4;
    }

    public static boolean high(int round, int arm) {
        return round == 1 || round >= 2 && arm >= 4;
    }

    public static double height(int round, int arm) {
        return QizaiSweepRules.high(round, arm) ? 2.1 : 0.9;
    }

    public static double angle(double initial, double elapsed, int round, int arm) {
        return QizaiSweepRules.wrapDegrees(initial + (double)(arm % 4 * 90) + (double)(round >= 2 && QizaiSweepRules.high(round, arm) ? 45 : 0) + elapsed * (QizaiSweepRules.high(round, arm) ? -0.75 : -1.325));
    }

    public static double wrapDegrees(double degrees) {
        double value = degrees % 360.0;
        if (value > 180.0) {
            value -= 360.0;
        }
        if (value <= -180.0) {
            value += 360.0;
        }
        return Double.isFinite(value) ? value : 0.0;
    }

    public static boolean crosses(double beamAngle, double playerAngle, double distance) {
        double tolerance = Math.toDegrees(Math.atan2(1.3, Math.max(1.0, distance)));
        return Math.abs(QizaiSweepRules.wrapDegrees(playerAngle - beamAngle)) <= tolerance;
    }

    public static boolean hitsHeight(AABB box, double baseY, int arm) {
        double beamY = baseY + (arm == 0 ? 0.9 : 2.1);
        return box.f_82289_ <= beamY && beamY <= box.f_82292_;
    }

    public static boolean wouldHit(double feetY, double height, double baseY, int arm) {
        return QizaiSweepRules.hitsHeight(new AABB(0.0, feetY, 0.0, 1.0, feetY + height, 1.0), baseY, arm);
    }

    public static Vec3 turn(Vec3 velocity, Vec3 target) {
        if (velocity.m_82556_() < 1.0E-8 || target.m_82556_() < 1.0E-8) {
            return velocity;
        }
        Vec3 current = velocity.m_82541_();
        Vec3 desired = target.m_82541_();
        double dot = Math.max(-1.0, Math.min(1.0, current.m_82526_(desired)));
        double limit = Math.toRadians(4.0);
        if (Math.acos(dot) <= limit) {
            return desired.m_82490_(0.55);
        }
        Vec3 tangent = desired.m_82546_(current.m_82490_(dot));
        if (tangent.m_82556_() < 1.0E-8) {
            tangent = current.m_82537_(Math.abs(current.f_82480_) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0));
        }
        return current.m_82490_(Math.cos(limit)).m_82549_(tangent.m_82541_().m_82490_(Math.sin(limit))).m_82490_(0.55);
    }
}

