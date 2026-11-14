/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class WeaponFxRules {
    public static final int MAX_ACTIVE = 64;
    public static final int MAX_PARTICLES_PER_TICK = 192;
    public static final int MAX_PARTICLE_LIFETIME = 14;
    public static final double VIEW_DISTANCE = 48.0;

    private WeaponFxRules() {
    }

    public static boolean isDoraGunItem(String identifier) {
        return "ftboss:dora_pulse_gun".equals(identifier) || "ftboss:dora_nova_launcher".equals(identifier);
    }

    public static Theme forItem(String identifier) {
        if (identifier == null) {
            return null;
        }
        return switch (identifier) {
            case "ftboss:candy_cane" -> Theme.FROST;
            case "ftboss:turkey_legs", "ftboss:black_cleaver" -> Theme.BLOOD;
            case "ftboss:blooming_thorns", "ftboss:apple_stem" -> Theme.NATURE;
            case "ftboss:wine_bottle_mace", "ftboss:barrel_crossbow" -> Theme.WINE;
            case "ftboss:cheese_wheel_mace" -> Theme.CHEESE;
            case "ftboss:defect_claw", "ftboss:dora_pulse_gun", "ftboss:dora_lance", "ftboss:dora_nova_launcher", "ftboss:wenxuanlan_flywheel", "tridents_n_stuff:stormseeker" -> Theme.ARC;
            case "ftboss:compost_crown", "ftboss:puffer_bat" -> Theme.COMPOST;
            case "ftboss:tide_trident", "tridents_n_stuff:echo_of_the_deep" -> Theme.TIDE;
            case "ftboss:shadow_sword", "tridents_n_stuff:the_desecrator" -> Theme.SHADOW;
            case "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> Theme.ROSE;
            case "ftboss:dog_beating_stick", "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula", "tridents_n_stuff:stellaris" -> Theme.GOLD;
            case "ftboss:maque_blessing_feather" -> Theme.BLESSING;
            case "ftboss:dandan_scythe", "ftboss:statue_youzi" -> Theme.CANDLE;
            case "ftboss:gourmet_blank_blade" -> Theme.GOURMET;
            default -> null;
        };
    }

    public static boolean validPosition(double value) {
        return Double.isFinite(value) && Math.abs(value) <= 3.0E7;
    }

    public static boolean validSize(float radius, float yaw, double lengthSquared) {
        return Float.isFinite(radius) && radius >= 0.1f && radius <= 16.0f && Float.isFinite(yaw) && Double.isFinite(lengthSquared) && lengthSquared >= 0.0 && lengthSquared <= 9216.0;
    }

    public static int budget(int setting) {
        return 192 / (1 << Math.max(0, Math.min(2, setting)));
    }

    public static int samples(int requested, int setting, double distanceSquared) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0.0 || distanceSquared > 2304.0) {
            return 0;
        }
        int divisor = (1 << Math.max(0, Math.min(2, setting))) * (distanceSquared > 576.0 ? 2 : 1);
        return Math.max(3, Math.min(32, requested) / divisor);
    }

    public static double ease(double progress) {
        double bounded = Math.max(0.0, Math.min(1.0, progress));
        return 1.0 - (1.0 - bounded) * (1.0 - bounded);
    }

    public static double sweepAngle(float yaw, double progress, double sample) {
        return Math.toRadians(yaw) + 1.5707963267948966 - 1.2 + 1.6 * Math.max(0.0, Math.min(1.0, progress)) + 0.8 * Math.max(0.0, Math.min(1.0, sample));
    }

    public static enum Theme {
        FROST(9035775, 0xF1FCFF),
        BLOOD(12988246, 16756868),
        NATURE(6736258, 15007659),
        WINE(6411952, 16764280),
        CHEESE(15712868, 16773562),
        ARC(4577485, 0xD7FFFF),
        COMPOST(10139736, 14348426),
        TIDE(4959463, 12648435),
        SHADOW(10050010, 14665215),
        ROSE(15366303, 16770028),
        GOLD(15907151, 16773820),
        BLESSING(9693367, 16187354),
        CANDLE(13937663, 16773299),
        GOURMET(6563482, 15185490);

        public final int color;
        public final int highlight;

        private Theme(int color, int highlight) {
            this.color = color;
            this.highlight = highlight;
        }
    }

    public static enum Kind {
        HIT(5),
        BEAM(6),
        SWEEP(8),
        BLAST(12),
        RING(10),
        MIST(10),
        TRACER(3),
        DORA_CHARGE(6),
        DORA_PULSE(8),
        DORA_NOVA(12),
        DORA_TRACER(4),
        DORA_IMPACT(10),
        PUFFER_CLOUD(24),
        PUFFER_BURST(12),
        PUFFER_SHOCKWAVE(12),
        GOURMET_HIT(7);

        public final int ticks;

        private Kind(int ticks) {
            this.ticks = ticks;
        }

        public boolean doraGun() {
            return this == DORA_CHARGE || this == DORA_PULSE || this == DORA_NOVA || this == DORA_TRACER || this == DORA_IMPACT;
        }

        public boolean pufferBat() {
            return this == PUFFER_CLOUD || this == PUFFER_BURST || this == PUFFER_SHOCKWAVE;
        }
    }
}

