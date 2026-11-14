/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 */
package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;

@Deprecated
public final class PlayerBuffGuard {
    public static final boolean ENABLED = false;
    public static final int SCAN_PERIOD_TICKS = 10;
    public static final int MAX_BUFF_TICKS = 24000;
    public static final int INVULNERABLE_RESISTANCE_AMPLIFIER = 4;

    private PlayerBuffGuard() {
    }

    public static boolean isInvulnerability(MobEffectInstance instance) {
        return false;
    }

    public static boolean isBuff(MobEffectInstance instance) {
        return false;
    }

    public static boolean isOverlong(MobEffectInstance instance) {
        return false;
    }

    public static boolean shouldPurge(MobEffectInstance instance) {
        return false;
    }

    public static void onApplicable(MobEffectEvent.Applicable event) {
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    }

    public static int sweep(ServerPlayer player) {
        return 0;
    }
}

