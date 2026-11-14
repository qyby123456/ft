/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.common.ForgeHooks
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.eventbus.api.Event
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveShield;
import net.farmingtales.ftboss.boss.ai.ManorDefense;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDamageRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorFinalDamage {
    private ManorFinalDamage() {
    }

    public static float legacyGate(LivingEntity livingEntity, DamageSource damageSource, float f) {
        EasterBossEntity easterBossEntity;
        if (livingEntity instanceof EasterBossEntity && !(easterBossEntity = (EasterBossEntity)livingEntity).isMini() && (ManorFight.isManaged((LivingEntity)easterBossEntity) || easterBossEntity.getVariant() == 9)) {
            return f;
        }
        return BossDamageGate.gate(livingEntity, damageSource, f);
    }

    public static float[] settle(EasterBossEntity easterBossEntity, DamageSource damageSource, float f) {
        float f2;
        float f3;
        Object object;
        float f4 = ForgeHooks.onLivingHurt((LivingEntity)easterBossEntity, (DamageSource)damageSource, (float)f);
        if (!Float.isFinite(f4) || f4 <= 0.0f) {
            return null;
        }
        float f5 = easterBossEntity.ftboss$reduceManorDamage(damageSource, f4);
        if (!Float.isFinite(f5 = ManorSpellRules.cap(easterBossEntity, damageSource, f5)) || f5 <= 0.0f) {
            return null;
        }
        EasterBossEntity easterBossEntity2 = easterBossEntity.ftboss$manorSparrow();
        boolean bl = !easterBossEntity.isMini() && easterBossEntity.getVariant() != 9 && easterBossEntity2 != null && !easterBossEntity2.ftboss$blocksManorDamage();
        boolean bl2 = false;
        if (!easterBossEntity.isMini() && easterBossEntity.getVariant() == 0 && (object = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            for (EasterBossEntity easterBossEntity3 : serverLevel.m_45976_(EasterBossEntity.class, easterBossEntity.m_20191_().m_82400_(64.0))) {
                if (easterBossEntity3 == easterBossEntity || !easterBossEntity3.isMini() || easterBossEntity3.getVariant() != 0 || !easterBossEntity3.m_6084_() || easterBossEntity3.m_21224_() || easterBossEntity3.ftboss$blocksManorDamage()) continue;
                bl2 = true;
                break;
            }
        }
        float f6 = ManorDamageRules.remainingDamage(f5, bl, bl2);
        if (easterBossEntity.getVariant() == 5 && !easterBossEntity.isMini()) {
            f6 = (float)((double)f6 * 0.7);
        }
        f6 = ManorAdaptiveShield.reduce((LivingEntity)easterBossEntity, damageSource, f6);
        float f7 = Math.max(0.0f, (f6 = ManorDefense.reduce(easterBossEntity, damageSource, f6)) - (f3 = Math.min(f2 = Math.max(0.0f, easterBossEntity.m_6103_()), f6)));
        LivingDamageEvent livingDamageEvent = new LivingDamageEvent((LivingEntity)easterBossEntity, damageSource, f7);
        if (MinecraftForge.EVENT_BUS.post((Event)livingDamageEvent)) {
            return null;
        }
        float f8 = livingDamageEvent.getAmount();
        if (ManorDamageRules.ignored(f8) || f7 > 0.0f && f8 <= 0.0f) {
            return null;
        }
        easterBossEntity.ftboss$shareManorDamage(f5, damageSource);
        if (f3 > 0.0f) {
            easterBossEntity.m_7911_(f2 - f3);
        }
        return new float[]{Math.max(0.0f, f8), f3};
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onFinalDamage(LivingDamageEvent livingDamageEvent) {
    }
}

