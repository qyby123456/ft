/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.dorazero;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DoraTimeline {
    public static final int MAX_EFFECTS = 64;
    private final List<Effect> active = new ArrayList<Effect>();

    public List<Effect> active() {
        return this.active;
    }

    public void clear() {
        this.active.clear();
    }

    public void accept(Message message) {
        if (message == null || message.owner() == null || message.ownerId() < 0 || message.spell() == null || message.phase() == null || !List.of("CHARGE", "RELEASE", "PULSE", "BEAM").contains(message.phase()) || !Double.isFinite(message.radius()) || message.radius() < 0.5 || message.radius() > 3.0 || message.duration() <= 0 || message.duration() > 1200 || !DoraTimeline.point(message.start()) || !DoraTimeline.point(message.end())) {
            return;
        }
        if (message.phase().equals("PULSE")) {
            for (Effect effect2 : this.active) {
                if (!DoraTimeline.same(effect2.message, message) || !effect2.message.phase().equals("RELEASE")) continue;
                effect2.pulseAt = message.now();
            }
        }
        if (message.phase().equals("CHARGE")) {
            for (Effect effect2 : this.active) {
                if (!DoraTimeline.same(effect2.message, message) || !effect2.message.phase().equals("CHARGE")) continue;
                effect2.message = message;
                effect2.lastSeen = message.now();
                return;
            }
        }
        if (message.phase().equals("RELEASE")) {
            this.active.removeIf(effect -> DoraTimeline.same(effect.message, message) && (effect.message.phase().equals("CHARGE") || effect.message.phase().equals("RELEASE")));
        }
        if (message.spell().equals("breach_shell") && message.phase().equals("PULSE")) {
            this.active.removeIf(effect -> DoraTimeline.same(effect.message, message) && effect.message.phase().equals("RELEASE"));
        }
        if (this.active.size() >= 64) {
            this.active.remove(0);
        }
        this.active.add(new Effect(message));
    }

    public void expire(long l) {
        this.active.removeIf(effect -> l < effect.born || effect.message.phase().equals("CHARGE") && l - effect.lastSeen > 12L || !effect.message.phase().equals("CHARGE") && l - effect.born >= (long)effect.message.duration());
    }

    private static boolean same(Message message, Message message2) {
        return message.owner().equals(message2.owner()) && message.spell().equals(message2.spell());
    }

    private static boolean point(double[] dArray) {
        if (dArray == null || dArray.length != 3) {
            return false;
        }
        for (double d : dArray) {
            if (Double.isFinite(d) && !(Math.abs(d) > 3.2E7)) continue;
            return false;
        }
        return true;
    }

    public record Message(UUID owner, int ownerId, String spell, String phase, double[] start, double[] end, double radius, int duration, long now) {
    }

    public static final class Effect {
        public Message message;
        public final long born;
        public long lastSeen;
        public long pulseAt = -1L;

        private Effect(Message message) {
            this.message = message;
            this.born = message.now();
            this.lastSeen = message.now();
        }
    }
}

