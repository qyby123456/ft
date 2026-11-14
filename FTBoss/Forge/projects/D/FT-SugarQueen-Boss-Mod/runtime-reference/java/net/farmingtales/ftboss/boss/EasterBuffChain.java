/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Added
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class EasterBuffChain {
    private EasterBuffChain() {
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added e) {
        LivingEntity livingEntity = e.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)livingEntity;
            boss.onBuffGainedHook(e.getEffectInstance());
        }
    }
}

