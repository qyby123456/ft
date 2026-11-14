package net.farmingtales.ftboss.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Rules for Qizai's grounded arena-wide random-direction laser barrage. */
public final class QizaiLaserBarrageRules {
    public static final String ID = "laser_barrage";
    public static final String NAME = "万棱星狱";
    public static final int ACTIVE_TICKS = 120;
    public static final int SPAWN_GAP = 4;
    public static final int ROUNDS = 3;
    public static final int MAX_BEAMS_PER_ROUND = 120;
    public static final int TARGET_INTERVAL = 20;
    public static final int TARGET_EVERY_BEAMS = TARGET_INTERVAL / SPAWN_GAP;
    public static final int WARNING_TICKS = 14;
    public static final int BEAM_FADE = 6;
    public static final int RECOVERY = 20;
    public static final int REST_MULTIPLIER = 2;
    public static final int ANCHOR_ATTEMPTS = 12;
    public static final int DIRECTION_ATTEMPTS = 8;
    public static final double RANGE = 40.0;
    public static final double FIELD_RADIUS = 24.0;
    public static final double MIN_ANCHOR_HEIGHT = 3.0;
    public static final double MAX_ANCHOR_HEIGHT = 10.0;
    public static final double BEAM_LENGTH = 36.0;
    public static final double BEAM_RADIUS = 0.45;
    public static final float DAMAGE = 48.0F;

    private QizaiLaserBarrageRules() {}

    public static int roundBeamCount(int round) {
        int base = beamCount(ACTIVE_TICKS, SPAWN_GAP);
        return Math.min(MAX_BEAMS_PER_ROUND, base << Math.max(0, Math.min(ROUNDS - 1, round)));
    }
    public static int roundSpawnTick(int index, int activeTicks, int count) {
        return 1 + (int) ((long) Math.max(0, index) * Math.max(1, activeTicks)
                / Math.max(1, Math.min(MAX_BEAMS_PER_ROUND, count)));
    }
    public static int roundDuration(int activeTicks, int warningTicks, int fadeTicks) {
        return duration(activeTicks, warningTicks, fadeTicks, 0);
    }
    public static int castDuration(int activeTicks, int warningTicks, int fadeTicks, int recoveryTicks) {
        return ROUNDS * roundDuration(activeTicks, warningTicks, fadeTicks) + Math.max(0, recoveryTicks);
    }

    public static int beamCount(int activeTicks, int spawnGap) {
        return Math.max(1, (Math.max(1, activeTicks) + Math.max(1, spawnGap) - 1) / Math.max(1, spawnGap));
    }
    public static int spawnTick(int index, int spawnGap) {
        return 1 + Math.max(0, index) * Math.max(1, spawnGap);
    }
    public static boolean targetsPlayer(int index) {
        return Math.max(0, index) % Math.max(1, TARGET_EVERY_BEAMS) == 0;
    }
    public static int fireTick(int index, int spawnGap, int warningTicks) {
        return spawnTick(index, spawnGap) + Math.max(1, warningTicks);
    }
    public static int duration(int activeTicks, int warningTicks, int fadeTicks, int recoveryTicks) {
        return Math.max(1, activeTicks) + Math.max(1, warningTicks) + Math.max(1, fadeTicks) + Math.max(0, recoveryTicks);
    }
    public static boolean validOrigin(Vec3 origin, Vec3 center) {
        if (!finite(origin) || !finite(center)) return false;
        Vec3 offset = origin.subtract(center);
        return offset.horizontalDistance() <= FIELD_RADIUS + 0.001
                && offset.y >= MIN_ANCHOR_HEIGHT - 0.001 && offset.y <= MAX_ANCHOR_HEIGHT + 0.001;
    }
    public static boolean intersects(AABB body, Vec3 origin, Vec3 end) {
        if (body == null || !finite(origin) || !finite(end)) return false;
        AABB expanded = body.inflate(BEAM_RADIUS);
        return expanded.contains(origin) || expanded.clip(origin, end).isPresent();
    }
    private static boolean finite(Vec3 point) {
        return point != null && Double.isFinite(point.x + point.y + point.z);
    }
}
