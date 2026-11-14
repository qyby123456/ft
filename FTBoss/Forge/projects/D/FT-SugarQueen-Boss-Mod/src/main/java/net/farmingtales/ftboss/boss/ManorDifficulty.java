package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public enum ManorDifficulty {
    NORMAL("normal", "普通"),
    IMPOSSIBLE("impossible", "终结");

    public static final String WARNING = "该难度几乎无法通过而且不会获取成就";
    public static final double LEGACY_SPEED = 0.33;
    public static final float LEGACY_DAMAGE_MULTIPLIER = 2.0F;
    public static final float NORMAL_DAMAGE_MULTIPLIER = 5.0F / 6.0F;
    public static final float IMPOSSIBLE_ATTRIBUTE_MULTIPLIER = 10.0F;
    public static final float IMPOSSIBLE_MOVEMENT_MULTIPLIER = 2.0F;
    public static final float IMPOSSIBLE_ATTACK_MULTIPLIER = IMPOSSIBLE_ATTRIBUTE_MULTIPLIER;
    public static final float IMPOSSIBLE_SPARROW_HEALTH = 500.0F;
    public static final float IMPOSSIBLE_KITING_SPEED = 1.2F;
    public static final float XUANLAN_MOVEMENT_MULTIPLIER = 1.2F;
    /** ★ v188 祁崽自己的移速倍率（用户要求「移速加快」） */
    public static final float QIZAI_MOVEMENT_MULTIPLIER = 1.3F;
    private final String id;
    private final String title;

    ManorDifficulty(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() { return this.id; }
    public String title() { return this.title; }

    public String label() { return this.title + "难度"; }

    public String badge() { return this == IMPOSSIBLE ? " §4[" + this.title + "]" : ""; }

    public int acceleratedTicks(int ticks) {
        return this == IMPOSSIBLE && ticks > 0 ? ticks / 10 + (ticks % 10 == 0 ? 0 : 1) : ticks;
    }

    public double attackRate() { return this == IMPOSSIBLE ? IMPOSSIBLE_ATTRIBUTE_MULTIPLIER : 1.0; }

    public float movementMultiplier(int variant) {
        /* ★ v188 用户要求「祁崽移速加快」: variant 11 走自己的倍率（1.3 = +30%） */
        float speed = variant == 6 ? XUANLAN_MOVEMENT_MULTIPLIER
                : variant == EasterBossEntity.QIZAI_VARIANT ? QIZAI_MOVEMENT_MULTIPLIER : 1.0F;
        if (this == IMPOSSIBLE) speed *= IMPOSSIBLE_MOVEMENT_MULTIPLIER;
        return speed;
    }

    public static ManorDifficulty fromId(String id) {
        return IMPOSSIBLE.id.equals(id) ? IMPOSSIBLE : NORMAL;
    }

    public static boolean unlocked(ServerPlayer player) {
        if (player == null) return false;
        var victories = ManorVictoryData.get(player.server);
        if (victories.hasConquered(player.getUUID())) return true;
        var advancement = player.server.getAdvancements().getAdvancement(
                new ResourceLocation(FTBossMod.MOD_ID, BossAdvancements.MANOR_VICTORY));
        if (advancement == null || !player.getAdvancements().getOrStartProgress(advancement).isDone()) return false;
        victories.recordConqueror(player.getUUID());
        return true;
    }
}
