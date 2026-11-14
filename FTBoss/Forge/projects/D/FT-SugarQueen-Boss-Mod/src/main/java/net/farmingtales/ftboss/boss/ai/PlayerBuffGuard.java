package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;

/**
 * Compatibility holder for the former global player-buff guard.
 *
 * <p>The global FTBoss buff interception was retired on 2026-09-29.  The
 * public helpers remain as no-op compatibility methods for old integrations;
 * no event-bus subscriber is registered and no player effect is changed.
 * Manor-specific challenge rules are kept in their own classes.
 */
@Deprecated
public final class PlayerBuffGuard {
    /** Global buff interception retired per user request. */
    public static final boolean ENABLED = false;
    public static final int SCAN_PERIOD_TICKS = 10;
    public static final int MAX_BUFF_TICKS = 20 * 60 * 20;
    public static final int INVULNERABLE_RESISTANCE_AMPLIFIER = 4;

    private PlayerBuffGuard() {}

    public static boolean isInvulnerability(MobEffectInstance instance) { return false; }
    public static boolean isBuff(MobEffectInstance instance) { return false; }
    public static boolean isOverlong(MobEffectInstance instance) { return false; }
    public static boolean shouldPurge(MobEffectInstance instance) { return false; }
    public static void onApplicable(MobEffectEvent.Applicable event) {}
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {}
    public static int sweep(ServerPlayer player) { return 0; }
}
