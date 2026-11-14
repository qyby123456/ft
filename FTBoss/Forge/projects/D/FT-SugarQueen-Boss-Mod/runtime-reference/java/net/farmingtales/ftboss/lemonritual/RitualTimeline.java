/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.lemonritual;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public final class RitualTimeline {
    private final List<Cue> active = new ArrayList<Cue>();
    private final LinkedHashMap<String, Long> seen = new LinkedHashMap();
    private String dimension;

    public boolean accept(Cue cue, String string, long l) {
        this.update(string, l);
        if (!RitualTimeline.valid(cue) || !cue.dimension().equals(string) || cue.started() > l + 2L || l - cue.started() >= (long)cue.life() || this.seen.containsKey(cue.key())) {
            return false;
        }
        if (!cue.absorb()) {
            for (Cue cue3 : this.active) {
                if (!cue3.absorb() || !RitualTimeline.matched(cue3, cue)) continue;
                return false;
            }
        } else {
            this.active.removeIf(cue2 -> !cue2.absorb() && RitualTimeline.matched(cue, cue2));
        }
        if (this.active.size() >= 24) {
            this.active.remove(0);
        }
        this.active.add(cue);
        this.seen.put(cue.key(), cue.started());
        while (this.seen.size() > 128) {
            this.seen.remove(this.seen.keySet().iterator().next());
        }
        return true;
    }

    public void update(String string, long l) {
        if (!string.equals(this.dimension)) {
            this.clear();
            this.dimension = string;
        }
        this.active.removeIf(cue -> l - cue.started() >= (long)cue.life() || cue.started() > l + 2L);
        this.seen.entrySet().removeIf(entry -> l - (Long)entry.getValue() > 100L);
    }

    private static boolean matched(Cue cue, Cue cue2) {
        if (!cue.owner().equals(cue2.owner()) || cue2.started() > cue.started() || cue.started() - cue2.started() > 32L) {
            return false;
        }
        for (double[] dArray : cue.sources()) {
            double d;
            double d2 = dArray[0] - cue2.anchor()[0];
            if (!(d2 * d2 + (d = dArray[2] - cue2.anchor()[2]) * d < 0.25)) continue;
            return true;
        }
        return false;
    }

    public static boolean valid(Cue cue) {
        if (cue == null || cue.owner() == null || cue.dimension() == null || cue.started() < 0L || !RitualTimeline.pointValid(cue.anchor()) || !Double.isFinite(cue.width()) || !Double.isFinite(cue.height()) || cue.width() <= 0.0 || cue.width() > 64.0 || cue.height() <= 0.0 || cue.height() > 64.0 || cue.sources() == null || cue.sources().size() > 12) {
            return false;
        }
        if (cue.absorb() ? cue.sources().isEmpty() : cue.subject() == null) {
            return false;
        }
        return cue.sources().stream().allMatch(RitualTimeline::pointValid);
    }

    public static boolean pointValid(double[] dArray) {
        if (dArray == null || dArray.length != 3) {
            return false;
        }
        for (double d : dArray) {
            if (Double.isFinite(d) && !(Math.abs(d) > 3.0E7)) continue;
            return false;
        }
        return true;
    }

    public List<Cue> active() {
        return List.copyOf(this.active);
    }

    public void remove(Cue cue) {
        this.active.remove(cue);
    }

    public void clear() {
        this.active.clear();
        this.seen.clear();
        this.dimension = null;
    }

    public record Cue(boolean absorb, UUID owner, UUID subject, String dimension, double[] anchor, List<double[]> sources, long started, double width, double height) {
        public int life() {
            return this.absorb ? 48 : 32;
        }

        public String key() {
            return String.valueOf(this.owner) + ":" + String.valueOf(this.subject) + ":" + this.absorb + ":" + this.started;
        }
    }
}

