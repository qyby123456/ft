package net.farmingtales.ftboss.util;

public final class WeaponFxRules {
    public static final int MAX_ACTIVE = 64;
    public static final int MAX_PARTICLES_PER_TICK = 192;
    public static final int MAX_PARTICLE_LIFETIME = 14;
    public static final double VIEW_DISTANCE = 48;

    public enum Kind {
        HIT(5), BEAM(6), SWEEP(8), BLAST(12), RING(10), MIST(10), TRACER(3),
        DORA_CHARGE(6), DORA_PULSE(8), DORA_NOVA(12), DORA_TRACER(4), DORA_IMPACT(10),
        PUFFER_CLOUD(24), PUFFER_BURST(12), PUFFER_SHOCKWAVE(12),
        GOURMET_HIT(7);

        public final int ticks;

        Kind(int ticks) {
            this.ticks = ticks;
        }

        public boolean doraGun() {
            return this == DORA_CHARGE || this == DORA_PULSE || this == DORA_NOVA
                    || this == DORA_TRACER || this == DORA_IMPACT;
        }

        public boolean pufferBat() {
            return this == PUFFER_CLOUD || this == PUFFER_BURST || this == PUFFER_SHOCKWAVE;
        }
    }

    public enum Theme {
        FROST(0x89DFFF, 0xF1FCFF), BLOOD(0xC62F56, 0xFFB084), NATURE(0x66C982, 0xE4FFAB),
        WINE(0x61D6B0, 0xFFCD78), CHEESE(0xEFC264, 0xFFF1BA), ARC(0x45D8CD, 0xD7FFFF),
        COMPOST(0x9AB858, 0xDAF08A), TIDE(0x4BACE7, 0xC0FFF3), SHADOW(0x9959DA, 0xDFC5FF),
        ROSE(0xEA789F, 0xFFE3EC), GOLD(0xF2B94F, 0xFFF2BC), BLESSING(0x93E8B7, 0xF6FFDA),
        CANDLE(0xD4ABFF, 0xFFF0B3), GOURMET(0x64269A, 0xE7B652);

        public final int color;
        public final int highlight;

        Theme(int color, int highlight) {
            this.color = color;
            this.highlight = highlight;
        }
    }

    private WeaponFxRules() {}

    public static boolean isDoraGunItem(String identifier) {
        return "ftboss:dora_pulse_gun".equals(identifier)
                || "ftboss:dora_nova_launcher".equals(identifier);
    }

    public static Theme forItem(String identifier) {
        if (identifier == null) return null;
        return switch (identifier) {
            case "ftboss:candy_cane" -> Theme.FROST;
            case "ftboss:turkey_legs", "ftboss:black_cleaver" -> Theme.BLOOD;
            case "ftboss:blooming_thorns", "ftboss:apple_stem" -> Theme.NATURE;
            case "ftboss:wine_bottle_mace", "ftboss:barrel_crossbow" -> Theme.WINE;
            case "ftboss:cheese_wheel_mace" -> Theme.CHEESE;
            case "ftboss:defect_claw", "ftboss:dora_pulse_gun", "ftboss:dora_lance",
                    "ftboss:dora_nova_launcher", "ftboss:wenxuanlan_flywheel", "tridents_n_stuff:stormseeker" -> Theme.ARC;
            case "ftboss:compost_crown", "ftboss:puffer_bat" -> Theme.COMPOST;
            case "ftboss:tide_trident", "tridents_n_stuff:echo_of_the_deep" -> Theme.TIDE;
            case "ftboss:shadow_sword", "tridents_n_stuff:the_desecrator" -> Theme.SHADOW;
            case "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> Theme.ROSE;
            case "ftboss:dog_beating_stick", "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula",
                    "tridents_n_stuff:stellaris" -> Theme.GOLD;
            case "ftboss:maque_blessing_feather" -> Theme.BLESSING;
            case "ftboss:dandan_scythe", "ftboss:statue_youzi" -> Theme.CANDLE;
            case "ftboss:gourmet_blank_blade" -> Theme.GOURMET;
            default -> null;
        };
    }

    public static boolean validPosition(double value) {
        return Double.isFinite(value) && Math.abs(value) <= 30_000_000;
    }

    public static boolean validSize(float radius, float yaw, double lengthSquared) {
        return Float.isFinite(radius) && radius >= 0.1F && radius <= 16
                && Float.isFinite(yaw) && Double.isFinite(lengthSquared) && lengthSquared >= 0 && lengthSquared <= 96 * 96;
    }

    public static int budget(int setting) {
        return MAX_PARTICLES_PER_TICK / (1 << Math.max(0, Math.min(2, setting)));
    }

    public static int samples(int requested, int setting, double distanceSquared) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0 || distanceSquared > VIEW_DISTANCE * VIEW_DISTANCE) return 0;
        int divisor = (1 << Math.max(0, Math.min(2, setting))) * (distanceSquared > 24 * 24 ? 2 : 1);
        return Math.max(3, Math.min(32, requested) / divisor);
    }

    public static double ease(double progress) {
        double bounded = Math.max(0, Math.min(1, progress));
        return 1 - (1 - bounded) * (1 - bounded);
    }

    public static double sweepAngle(float yaw, double progress, double sample) {
        return Math.toRadians(yaw) + Math.PI / 2 - 1.2
                + 1.6 * Math.max(0, Math.min(1, progress)) + 0.8 * Math.max(0, Math.min(1, sample));
    }
}
