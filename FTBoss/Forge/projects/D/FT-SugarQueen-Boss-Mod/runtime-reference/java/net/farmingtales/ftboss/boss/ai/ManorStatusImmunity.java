/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Remove
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import net.farmingtales.ftboss.effect.HealBanEffect;
import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ManorStatusImmunity {
    private ManorStatusImmunity() {
    }

    public static boolean blocks(LivingEntity livingEntity, MobEffect mobEffect) {
        return livingEntity instanceof EasterBossEntity && (mobEffect instanceof HealBanEffect || mobEffect instanceof ShadowErosionEffect);
    }

    public static boolean allowed(boolean bl, LivingEntity livingEntity, MobEffectInstance mobEffectInstance) {
        return bl && (mobEffectInstance == null || !ManorStatusImmunity.blocks(livingEntity, mobEffectInstance.m_19544_()));
    }

    public static boolean protectRemoval(boolean bl, MobEffectEvent.Remove remove) {
        return bl && !ManorStatusImmunity.blocks(remove.getEntity(), remove.getEffect());
    }

    public static void purge(LivingEntity livingEntity) {
        if (!(livingEntity instanceof EasterBossEntity) || livingEntity.m_9236_().f_46443_) {
            return;
        }
        for (MobEffectInstance mobEffectInstance : new ArrayList(livingEntity.m_21220_())) {
            if (!ManorStatusImmunity.blocks(livingEntity, mobEffectInstance.m_19544_())) continue;
            livingEntity.m_21195_(mobEffectInstance.m_19544_());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onApplicable(MobEffectEvent.Applicable applicable) {
        MobEffectInstance mobEffectInstance = applicable.getEffectInstance();
        if (mobEffectInstance != null && ManorStatusImmunity.blocks(applicable.getEntity(), mobEffectInstance.m_19544_())) {
            applicable.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent livingTickEvent) {
        ManorStatusImmunity.purge(livingTickEvent.getEntity());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        Entity entity = entityJoinLevelEvent.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            ManorStatusImmunity.purge(livingEntity);
        }
    }
}

