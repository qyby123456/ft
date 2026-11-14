package net.farmingtales.ftboss.util;

/** 材料弓实战档案；2026-10-22 将满弓箭伤与拉满时间统一供服务端、模型和文本使用。 */
public final class MaterialBowRules {
    public static final int VANILLA_DRAW_TICKS = 20;
    public static final float ARROW_SPEED = 3.0F;

    public record Profile(int damage, int drawTicks) {
        public int vanillaChargeTicks(int elapsed) {
            return (int) Math.min(VANILLA_DRAW_TICKS, (long) Math.max(0, elapsed) * VANILLA_DRAW_TICKS / drawTicks);
        }

        public float pull(int elapsed) {
            return Math.min(1.0F, Math.max(0, elapsed) / (float) drawTicks);
        }

        public double arrowDamageAdjustment() {
            return damage / (double) ARROW_SPEED - 2.0D;
        }
    }

    private MaterialBowRules() {}

    public static Profile profile(String identifier) {
        return switch (identifier) {
            case "wooden_bow" -> new Profile(4, 20);
            case "stone_bow" -> new Profile(5, 24);
            case "iron_bow" -> new Profile(7, 22);
            case "golden_bow" -> new Profile(6, 14);
            case "diamond_bow" -> new Profile(9, 20);
            case "netherite_bow" -> new Profile(11, 18);
            default -> throw new IllegalArgumentException("Unknown material bow: " + identifier);
        };
    }
}
