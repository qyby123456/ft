/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.zheergenphantom;

import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergenphantom.ErosionRules;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ErosionFilter {
    private ErosionFilter() {
    }

    public static float strength(LivingEntity livingEntity, MobEffect mobEffect) {
        MobEffectInstance mobEffectInstance = livingEntity.m_21124_(mobEffect);
        return mobEffectInstance == null ? 0.0f : ErosionRules.strength(mobEffectInstance.m_19564_() + 1);
    }

    public static boolean suppress(LivingEntity livingEntity, MobEffect mobEffect) {
        EasterBossEntity easterBossEntity;
        return livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 7 && (mobEffect == MobEffects.f_19610_ || mobEffect == MobEffects.f_216964_ || mobEffect == FTEffects.SHADOW_BLIND.get());
    }

    public static void retire(LivingEntity livingEntity, int n) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            ErosionFilter.cleanup(player);
        }
    }

    public static void cleanup(Player player) {
        if (player.m_9236_().f_46443_) {
            return;
        }
        MobEffectInstance mobEffectInstance = player.m_21124_((MobEffect)FTEffects.SHADOW_BLIND.get());
        if (mobEffectInstance == null) {
            return;
        }
        player.m_21195_((MobEffect)FTEffects.SHADOW_BLIND.get());
        ErosionFilter.removeCompanion(player, MobEffects.f_19610_, mobEffectInstance, 1);
        ErosionFilter.removeCompanion(player, MobEffects.f_216964_, mobEffectInstance, 0);
    }

    private static void removeCompanion(Player player, MobEffect mobEffect, MobEffectInstance mobEffectInstance, int n) {
        MobEffectInstance mobEffectInstance2 = player.m_21124_(mobEffect);
        if (!(mobEffectInstance2 == null || mobEffectInstance2.m_19572_() || mobEffectInstance2.m_19575_() || mobEffectInstance2.m_19564_() != n || mobEffectInstance2.m_267577_() != mobEffectInstance.m_267577_() || !mobEffectInstance2.m_267577_() && Math.abs(mobEffectInstance2.m_19557_() - mobEffectInstance.m_19557_()) > 2)) {
            player.m_21195_(mobEffect);
        }
    }
}

