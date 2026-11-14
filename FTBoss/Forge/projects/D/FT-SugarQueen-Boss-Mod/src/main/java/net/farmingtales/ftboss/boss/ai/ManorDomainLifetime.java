package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDomainLifetimeRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class ManorDomainLifetime {
    private static final String DATA = "ftboss:domain_lifetime";

    private ManorDomainLifetime() {}

    public static boolean eligible(boolean original, EasterBossEntity boss, UUID fight) {
        if (!original || fight == null || !(boss.level() instanceof ServerLevel level)) return false;
        CompoundTag persistent = boss.getPersistentData();
        CompoundTag state = persistent.getCompound(DATA);
        long now = level.getGameTime();
        if (!state.hasUUID("Fight") || !fight.equals(state.getUUID("Fight")) || state.getInt("Variant") != boss.getVariant()) {
            state = new CompoundTag();
            state.putUUID("Fight", fight);
            state.putInt("Variant", boss.getVariant());
            state.putLong("Started", now);
            persistent.put(DATA, state);
        }
        if (state.getBoolean("Expired")) return false;
        if (!ManorDomainLifetimeRules.expired(state.getLong("Started"), now)) return true;
        state.putBoolean("Expired", true);
        boss.setHealth(ManorDomainLifetimeRules.recoveredHealth(boss.getHealth(), boss.getMaxHealth()));
        return false;
    }
}
