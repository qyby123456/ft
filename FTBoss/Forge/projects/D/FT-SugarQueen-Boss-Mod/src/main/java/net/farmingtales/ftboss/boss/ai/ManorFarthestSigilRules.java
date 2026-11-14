package net.farmingtales.ftboss.boss.ai;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** 多人庄园最远玩家三连法阵的选目标、时序和命中规则。 */
public final class ManorFarthestSigilRules {
    public static final int WINDUP_TICKS = 20;
    public static final int GAP_TICKS = 20;
    public static final int COOLDOWN_TICKS = 140;
    public static final int INITIAL_DELAY_TICKS = 60;
    public static final int RELEASE_TICKS = 18;
    public static final double ARENA_RADIUS = 96;
    public static final double SIGIL_RADIUS = 8;
    public static final double VIEW_RANGE = 192;
    public static final int FOUNTAIN = 0;
    public static final int BEAM = 1;
    public static final int PILLAR = 2;
    private static final int[][] ORDERS = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}};

    private ManorFarthestSigilRules() {}

    public static int style(int order, int strike) {
        if (order < 0 || order >= ORDERS.length || strike < 0 || strike >= 3) throw new IllegalArgumentException("Invalid sigil order");
        return ORDERS[order][strike];
    }

    public static boolean insideArena(double offsetX, double offsetY, double offsetZ) {
        return finite(offsetX, offsetY, offsetZ)
                && offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ <= ARENA_RADIUS * ARENA_RADIUS;
    }

    public static UUID farthest(List<Candidate> candidates) {
        return candidates.stream().filter(candidate -> candidate.eligible() && candidate.id() != null
                        && Double.isFinite(candidate.distanceSquared()) && candidate.distanceSquared() >= 0)
                .sorted(Comparator.comparingDouble(Candidate::distanceSquared).reversed()
                        .thenComparing(candidate -> candidate.id().toString()))
                .map(Candidate::id).findFirst().orElse(null);
    }

    public static boolean hits(int style, double offsetX, double offsetY, double offsetZ, double yaw) {
        if (!finite(offsetX, offsetY, offsetZ) || !Double.isFinite(yaw) || offsetY < -1) return false;
        if (style == FOUNTAIN) return offsetY <= 8 && offsetX * offsetX + offsetZ * offsetZ <= 9;
        if (style == PILLAR) return offsetY <= 20 && offsetX * offsetX + offsetZ * offsetZ <= 6.25;
        if (style != BEAM || offsetY > 4) return false;
        double along = offsetX * Math.cos(yaw) + offsetZ * Math.sin(yaw);
        double across = -offsetX * Math.sin(yaw) + offsetZ * Math.cos(yaw);
        return Math.abs(along) <= SIGIL_RADIUS && Math.abs(across) <= 1.5;
    }

    public static boolean finite(double offsetX, double offsetY, double offsetZ) {
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ);
    }

    public record Candidate(UUID id, double distanceSquared, boolean eligible) {}
}
