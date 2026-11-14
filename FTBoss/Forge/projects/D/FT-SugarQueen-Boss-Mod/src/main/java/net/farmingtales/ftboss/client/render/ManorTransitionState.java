package net.farmingtales.ftboss.client.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 庄园切换时钟和活动表：暂停冻结、增量校时、跨维清理；2026-10-25独立重做。 */
public final class ManorTransitionState {
    public static final class Show {
        public final UUID id;
        public final String dimension;
        public final double centerX;
        public final double centerY;
        public final double centerZ;
        private int variant;
        private int packetAge;
        private double age;

        private Show(UUID id, String dimension, double centerX, double centerY, double centerZ, int variant, int age) {
            this.id = id;
            this.dimension = dimension;
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.variant = variant;
            this.packetAge = age;
            this.age = age;
        }

        public double age(double partialTick, boolean paused) {
            return age + (paused ? 0 : Math.max(0, Math.min(1, partialTick)));
        }

        public int variant() { return variant; }
    }

    private final Map<UUID, Show> active = new LinkedHashMap<>();

    public void receive(UUID id, String dimension, double centerX, double centerY, double centerZ,
                        int variant, int packetAge, boolean enabled, String currentDimension) {
        if (id == null) return;
        if (!enabled) {
            active.remove(id);
            return;
        }
        if (dimension == null || !dimension.equals(currentDimension) || !ManorTransitionRules.valid(variant, packetAge)
                || !Double.isFinite(centerX) || !Double.isFinite(centerY) || !Double.isFinite(centerZ)
                || Math.abs(centerX) > 30000000 || Math.abs(centerY) > 30000000 || Math.abs(centerZ) > 30000000) return;
        Show previous = active.get(id);
        if (previous != null && previous.dimension.equals(dimension) && previous.centerX == centerX
                && previous.centerY == centerY && previous.centerZ == centerZ) {
            previous.age = ManorTransitionRules.correctedAge(previous.age, previous.packetAge, packetAge);
            previous.packetAge = packetAge;
            previous.variant = variant;
            return;
        }
        if (!active.containsKey(id) && active.size() >= ManorTransitionRules.MAX_ACTIVE) active.remove(active.keySet().iterator().next());
        active.put(id, new Show(id, dimension, centerX, centerY, centerZ, variant, packetAge));
    }

    public void tick(String dimension, boolean paused) {
        if (dimension == null) {
            clear();
            return;
        }
        active.values().removeIf(show -> !show.dimension.equals(dimension));
        if (paused) return;
        for (Show show : active.values()) show.age++;
        active.values().removeIf(show -> show.age >= ManorTransitionRules.DURATION_TICKS);
    }

    public List<Show> shows() { return new ArrayList<>(active.values()); }
    public void clear() { active.clear(); }
}
