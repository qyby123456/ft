/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.maque.support;

import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.maque.support.MaqueSupportPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class MaqueSupportHooks {
    private MaqueSupportHooks() {
    }

    public static void arrival(Entity entity) {
        MaqueSupportPacket.send(entity, entity, 0);
    }

    public static void departure(Entity entity) {
        MaqueSupportPacket.send(entity, entity, 1);
    }

    public static void intercept(Entity entity, Entity entity2, float f) {
        if (f > 0.0f && Float.isFinite(f)) {
            MaqueSupportPacket.send(entity2, entity, 6);
        }
    }

    public static void heal(LivingEntity livingEntity, float f, Entity entity, int n) {
        float f2 = livingEntity.m_21223_();
        livingEntity.m_5634_(f);
        if (livingEntity.m_21223_() > f2) {
            MaqueSupportPacket.send((Entity)(entity == null ? livingEntity : entity), (Entity)livingEntity, n);
        }
    }

    public static boolean empower(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, Entity entity, int n) {
        boolean bl = livingEntity.m_7292_(mobEffectInstance);
        if (bl && mobEffectInstance.m_19544_().m_19486_()) {
            MaqueSupportPacket.send((Entity)(entity == null ? livingEntity : entity), (Entity)livingEntity, n);
        }
        return bl;
    }

    public static boolean cleanse(LivingEntity livingEntity, MobEffect mobEffect, Entity entity) {
        boolean bl = livingEntity.m_21195_(mobEffect);
        if (bl && !mobEffect.m_19486_()) {
            MaqueSupportPacket.send((Entity)(entity == null ? livingEntity : entity), (Entity)livingEntity, 4);
        }
        return bl;
    }

    public static boolean copy(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, Entity entity) {
        EasterBossEntity easterBossEntity;
        boolean bl = livingEntity.m_7292_(mobEffectInstance);
        if (bl && livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 9 && mobEffectInstance.m_19544_().m_19486_()) {
            MaqueSupportPacket.send(entity, (Entity)livingEntity, 5);
        }
        return bl;
    }

    public static void healLock(ServerPlayer serverPlayer, Entity entity) {
        HealBan.ban(serverPlayer);
        MaqueSupportPacket.send(entity, (Entity)serverPlayer, 11);
    }
}

