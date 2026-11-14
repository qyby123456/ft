package net.farmingtales.ftboss.util;

/** 六种材料弩的基础箭伤和装填时间；2026-10-24 与材料弓采用一致的材料定位，保留原版快速装填减时。 */
public final class MaterialCrossbowRules {
    public static final float ARROW_SPEED = 3.15F;
    public record Profile(int damage, int reloadTicks) {
        public double arrowDamageAdjustment() {
            return damage / (double) ARROW_SPEED - 2.0D;
        }
    }

    private MaterialCrossbowRules() {}

    public static Profile profile(String identifier) {
        return switch (identifier) {
            case "wooden_crossbow" -> new Profile(6, 25);
            case "stone_crossbow" -> new Profile(7, 30);
            case "iron_crossbow" -> new Profile(9, 28);
            case "golden_crossbow" -> new Profile(8, 18);
            case "diamond_crossbow" -> new Profile(11, 25);
            case "netherite_crossbow" -> new Profile(13, 23);
            default -> throw new IllegalArgumentException("Unknown material crossbow: " + identifier);
        };
    }
}
