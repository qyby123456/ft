/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Added
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.effect.HealBanEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class HealBanAbsorptionGuard {
    private HealBanAbsorptionGuard() {
    }

    private static void clearAbsorption(LivingEntity entity) {
        if (entity.m_21023_(MobEffects.f_19617_)) {
            entity.m_21195_(MobEffects.f_19617_);
        }
        if (entity.m_6103_() > 0.0f) {
            entity.m_7911_(0.0f);
        }
    }

    private static boolean isServerBanned(LivingEntity entity) {
        return !entity.m_9236_().f_46443_ && HealBan.isBanned(entity);
    }

    private static void suppressAbsorption(LivingEntity entity) {
        if (HealBanAbsorptionGuard.isServerBanned(entity)) {
            HealBanAbsorptionGuard.clearAbsorption(entity);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (!entity.m_9236_().f_46443_ && event.getEffectInstance().m_19544_() instanceof HealBanEffect) {
            HealBanAbsorptionGuard.clearAbsorption(entity);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().m_19544_() == MobEffects.f_19617_ && HealBanAbsorptionGuard.isServerBanned(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        HealBanAbsorptionGuard.suppressAbsorption(event.getEntity());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            HealBanAbsorptionGuard.suppressAbsorption((LivingEntity)event.player);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        HealBanAbsorptionGuard.suppressAbsorption(event.getEntity());
    }
}

