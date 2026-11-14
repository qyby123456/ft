package net.farmingtales.ftboss.util;

public final class MaterialCrossbowRules {
    public static final float ARROW_SPEED = 3.15F;

    private MaterialCrossbowRules() {
    }

    public static MaterialCrossbowRules.Profile profile(String var0) {
        return switch (var0) {
            case "wooden_crossbow" -> new MaterialCrossbowRules.Profile(6, 25);
            case "stone_crossbow" -> new MaterialCrossbowRules.Profile(7, 30);
            case "iron_crossbow" -> new MaterialCrossbowRules.Profile(9, 28);
            case "golden_crossbow" -> new MaterialCrossbowRules.Profile(8, 18);
            case "diamond_crossbow" -> new MaterialCrossbowRules.Profile(11, 25);
            case "netherite_crossbow" -> new MaterialCrossbowRules.Profile(13, 23);
            default -> throw new IllegalArgumentException("Unknown material crossbow: " + var0);
        };
    }

    public static record Profile(int damage, int reloadTicks) {
        public double arrowDamageAdjustment() {
            return (double)this.damage / 3.15F - 2.0;
        }
    }
}
