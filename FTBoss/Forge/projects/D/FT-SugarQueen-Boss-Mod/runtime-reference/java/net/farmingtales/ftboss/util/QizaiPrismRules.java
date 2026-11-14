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

public final class QizaiPrismRules {
    public static final String ID = "prism_lock";
    public static final String NAME = "\u7eef\u661f\u9501\u88c1";
    public static final int BEAMS = 6;
    public static final int TRACK_TICKS = 24;
    public static final int LOCK_TICKS = 12;
    public static final int FIRE_GAP = 2;
    public static final int BEAM_FADE = 8;
    public static final int RECOVERY = 20;
    public static final int REST_MULTIPLIER = 2;
    public static final int ANCHOR_ATTEMPTS = 12;
    public static final double RANGE = 40.0;
    public static final double MIN_ANCHOR_RADIUS = 7.0;
    public static final double MAX_ANCHOR_RADIUS = 14.0;
    public static final double MIN_ANCHOR_HEIGHT = 3.0;
    public static final double MAX_ANCHOR_HEIGHT = 9.0;
    public static final double BEAM_RADIUS = 0.55;
    public static final float DAMAGE = 72.0f;

    private QizaiPrismRules() {
    }

    public static int fireTick(int index, int trackTicks, int lockTicks, int fireGap) {
        return trackTicks + lockTicks + Math.max(0, index) * Math.max(1, fireGap);
    }

    public static int lastFireTick(int trackTicks, int lockTicks, int fireGap) {
        return QizaiPrismRules.fireTick(5, trackTicks, lockTicks, fireGap);
    }

    public static int duration(int trackTicks, int lockTicks, int fireGap, int recovery) {
        return QizaiPrismRules.lastFireTick(trackTicks, lockTicks, fireGap) + 8 + Math.max(0, recovery);
    }

    public static boolean validAnchor(Vec3 anchor, Vec3 target) {
        if (anchor == null || target == null || !Double.isFinite(anchor.f_82479_ + anchor.f_82480_ + anchor.f_82481_ + target.f_82479_ + target.f_82480_ + target.f_82481_)) {
            return false;
        }
        Vec3 offset = anchor.m_82546_(target);
        double horizontal = offset.m_165924_();
        return horizontal >= 6.999 && horizontal <= 14.001 && offset.f_82480_ >= 2.999 && offset.f_82480_ <= 9.001;
    }

    public static boolean intersects(AABB body, Vec3 origin, Vec3 end) {
        if (body == null || origin == null || end == null || !Double.isFinite(origin.f_82479_ + origin.f_82480_ + origin.f_82481_ + end.f_82479_ + end.f_82480_ + end.f_82481_)) {
            return false;
        }
        AABB expanded = body.m_82400_(0.55);
        return expanded.m_82390_(origin) || expanded.m_82371_(origin, end).isPresent();
    }
}

