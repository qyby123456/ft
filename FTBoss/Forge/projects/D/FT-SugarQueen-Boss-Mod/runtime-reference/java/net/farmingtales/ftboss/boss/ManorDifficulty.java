/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 */
package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.boss.ManorVictoryData;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public enum ManorDifficulty {
    NORMAL("normal", "\u666e\u901a"),
    IMPOSSIBLE("impossible", "\u7ec8\u7ed3");

    public static final String WARNING = "\u8be5\u96be\u5ea6\u51e0\u4e4e\u65e0\u6cd5\u901a\u8fc7\u800c\u4e14\u4e0d\u4f1a\u83b7\u53d6\u6210\u5c31";
    public static final double LEGACY_SPEED = 0.33;
    public static final float LEGACY_DAMAGE_MULTIPLIER = 2.0f;
    public static final float NORMAL_DAMAGE_MULTIPLIER = 0.8333333f;
    public static final float IMPOSSIBLE_MOVEMENT_MULTIPLIER = 2.0f;
    public static final float IMPOSSIBLE_ATTACK_MULTIPLIER = 2.0f;
    public static final float IMPOSSIBLE_SPARROW_HEALTH = 500.0f;
    public static final float IMPOSSIBLE_KITING_SPEED = 1.2f;
    public static final float XUANLAN_MOVEMENT_MULTIPLIER = 1.2f;
    public static final float QIZAI_MOVEMENT_MULTIPLIER = 1.3f;
    private final String id;
    private final String title;

    private ManorDifficulty(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return this.id;
    }

    public String title() {
        return this.title;
    }

    public String label() {
        return this.title + "\u96be\u5ea6";
    }

    public String badge() {
        return this == IMPOSSIBLE ? " \u00a74[" + this.title + "]" : "";
    }

    public int acceleratedTicks(int ticks) {
        return this == IMPOSSIBLE && ticks > 0 ? ticks / 2 + ticks % 2 : ticks;
    }

    public double attackRate() {
        return this == IMPOSSIBLE ? 2.0 : 1.0;
    }

    public float movementMultiplier(int variant) {
        float speed;
        float f = variant == 6 ? 1.2f : (speed = variant == 11 ? 1.3f : 1.0f);
        if (this == IMPOSSIBLE) {
            speed *= 2.0f * (variant == 0 ? 1.0f : 1.2f);
        }
        return speed;
    }

    public static ManorDifficulty fromId(String id) {
        return ManorDifficulty.IMPOSSIBLE.id.equals(id) ? IMPOSSIBLE : NORMAL;
    }

    public static boolean unlocked(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        ManorVictoryData victories = ManorVictoryData.get(player.f_8924_);
        if (victories.hasConquered(player.m_20148_())) {
            return true;
        }
        Advancement advancement = player.f_8924_.m_129889_().m_136041_(new ResourceLocation("ftboss", "memories_end"));
        if (advancement == null || !player.m_8960_().m_135996_(advancement).m_8193_()) {
            return false;
        }
        victories.recordConqueror(player.m_20148_());
        return true;
    }
}

