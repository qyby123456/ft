package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.BossHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class ManorIntroPlayerBuffs {
    private ManorIntroPlayerBuffs() {}

    public static void begin(ServerPlayer player, Set<UUID> prepared) {
        if (!BossTargets.valid(player) || !prepared.add(player.getUUID())) return;
        for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
            if (effect.getEffect().isBeneficial()) player.removeEffect(effect.getEffect());
        }
        applyFieldBuffs(player);
    }

    public static void applyFieldBuffs(ServerPlayer player) {
        for (MobEffect effect : BossHandler.BEACON_EFFECTS) {
            MobEffectInstance current = player.getEffect(effect);
            if (current == null || current.getAmplifier() < 1 || current.getDuration() < 60) {
                player.addEffect(new MobEffectInstance(effect, 12 * 20, 1, true, false, false));
            }
        }
    }
}
