package net.farmingtales.ftdungeonmobs;

import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import software.bernie.geckolib.core.animation.RawAnimation;

public enum SkeletonVariant {
    WARRIOR("skeleton_warrior", 0.7F, 1.95F, 40, 6, 0.25, 2.5, 25, 14,
            Map.of("idle", 2.0, "walk", 0.58333, "attack", 1.25, "animation2", 0.5)),
    ARCHER("skeleton_archer", 0.7F, 2.05F, 30, 4, 0.25, 16, 31, 22,
            Map.of("idle", 2.0, "walk", 0.58333, "attack", 1.54167, "animation2", 0.5)),
    MAGE("skeleton_mage", 0.7F, 2.1F, 35, 6, 0.23, 14, 30, 18,
            Map.of("idle", 2.0, "walk", 1.5, "attack", 1.5, "animation2", 0.5)),
    KING("skeleton_king", 1.0F, 3.25F, 200, 10, 0.23, 3, 30, 16,
            Map.of("idle", 3.0, "walk", 1.5, "attack", 1.5, "dash", 1.5, "summon", 1.5));

    private final String id;
    private final float width;
    private final float height;
    private final double health;
    private final double damage;
    private final double speed;
    private final double attackRange;
    private final int attackTicks;
    private final int impactTick;
    private final Map<String, Double> durations;
    private final Map<String, RawAnimation> animations;

    SkeletonVariant(String id, float width, float height, double health, double damage, double speed,
                    double attackRange, int attackTicks, int impactTick, Map<String, Double> durations) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.attackRange = attackRange;
        this.attackTicks = attackTicks;
        this.impactTick = impactTick;
        this.durations = durations;
        this.animations = new java.util.HashMap<>();
        durations.forEach((name, duration) -> animations.put(name,
                name.equals("idle") || name.equals("walk")
                        ? RawAnimation.begin().thenLoop(animationId(name))
                        : RawAnimation.begin().thenPlay(animationId(name))));
    }

    public String id() { return id; }
    public float width() { return width; }
    public float height() { return height; }
    public double health() { return health; }
    public double damage() { return damage; }
    public double baseAttackDamage() {
        return defaultWeapon() instanceof SwordItem sword ? Math.max(0, damage - sword.getDamage()) : damage;
    }
    public Item defaultWeapon() {
        return switch (this) {
            case WARRIOR -> Items.IRON_SWORD;
            case KING -> Items.DIAMOND_SWORD;
            case ARCHER -> Items.BOW;
            case MAGE -> Items.AIR;
        };
    }
    public double speed() { return speed; }
    public double attackRange() { return attackRange; }
    public int attackTicks() { return attackTicks; }
    public int impactTick() { return impactTick; }
    public String animationId(String name) { return "animation." + id + "." + name; }
    public boolean hasAnimation(String name) { return animations.containsKey(name); }
    public RawAnimation animation(String name) { return animations.get(name); }
    public int animationTicks(String name) { return (int) Math.ceil(durations.get(name) * 20); }
}
