package net.farmingtales.ftboss.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Rules for Qizai's grounded, multi-angle lock-on beam skill. */
public final class QizaiPrismRules {
    public static final String ID = "prism_lock";
    public static final String NAME = "绯星锁裁";
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
    public static final float DAMAGE = 72.0F;
    private QizaiPrismRules() {}
    public static int fireTick(int index, int trackTicks, int lockTicks, int fireGap) {
        return trackTicks + lockTicks + Math.max(0, index) * Math.max(1, fireGap);
    }
    public static int lastFireTick(int trackTicks, int lockTicks, int fireGap) {
        return fireTick(BEAMS - 1, trackTicks, lockTicks, fireGap);
    }
    public static int duration(int trackTicks, int lockTicks, int fireGap, int recovery) {
        return lastFireTick(trackTicks, lockTicks, fireGap) + BEAM_FADE + Math.max(0, recovery);
    }
    public static boolean validAnchor(Vec3 anchor, Vec3 target) {
        if (anchor == null || target == null || !Double.isFinite(anchor.x + anchor.y + anchor.z + target.x + target.y + target.z)) return false;
        Vec3 offset = anchor.subtract(target);
        double horizontal = offset.horizontalDistance();
        return horizontal >= MIN_ANCHOR_RADIUS - 0.001 && horizontal <= MAX_ANCHOR_RADIUS + 0.001
                && offset.y >= MIN_ANCHOR_HEIGHT - 0.001 && offset.y <= MAX_ANCHOR_HEIGHT + 0.001;
    }
    public static boolean intersects(AABB body, Vec3 origin, Vec3 end) {
        if (body == null || origin == null || end == null || !Double.isFinite(origin.x + origin.y + origin.z + end.x + end.y + end.z)) return false;
        AABB expanded = body.inflate(BEAM_RADIUS);
        return expanded.contains(origin) || expanded.clip(origin, end).isPresent();
    }
}
