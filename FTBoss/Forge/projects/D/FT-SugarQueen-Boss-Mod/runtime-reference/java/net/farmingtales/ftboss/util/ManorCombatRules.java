/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.UUID;

public final class ManorCombatRules {
    private ManorCombatRules() {
    }

    public static boolean allowsDamage(boolean voidDamage, boolean administrativeKill, boolean realPlayer) {
        return !voidDamage && (administrativeKill || realPlayer);
    }

    public static boolean shouldThaw(boolean noAi, boolean invulnerable, boolean acting, boolean finale) {
        return (noAi || invulnerable) && !acting && !finale;
    }

    public static boolean canSurrender(UUID summoner, UUID player, UUID fight, UUID offeredFight) {
        return summoner != null && summoner.equals(player) && fight != null && fight.equals(offeredFight);
    }

    public static boolean relocationReady(long now, long last, int cooldown) {
        return last == Long.MIN_VALUE || now < last || now - last >= (long)cooldown;
    }

    public static boolean defeated(boolean deathConfirmed, int unloadedTicks) {
        return deathConfirmed;
    }
}

