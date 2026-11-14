/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.zheergenfield;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergenfield.ErosionPulseRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ErosionAura {
    private static final Map<EasterBossEntity, PulseState> CLOCKS = new WeakHashMap<EasterBossEntity, PulseState>();

    private ErosionAura() {
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent livingTickEvent) {
        LivingEntity livingEntity = livingTickEvent.getEntity();
        if (!(livingEntity instanceof EasterBossEntity)) {
            return;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
        Object object = easterBossEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        livingEntity = (ServerLevel)object;
        object = ManorFight.fightId((Entity)easterBossEntity);
        if (easterBossEntity.getVariant() != 7 || easterBossEntity.isMini() || !easterBossEntity.m_6084_() || easterBossEntity.m_213877_() || easterBossEntity.m_21525_() || object == null || !ManorFight.hasActive((UUID)object) || !ManorFight.isManaged((LivingEntity)easterBossEntity) || ManorFight.currentVariant((UUID)object) != 7 || !easterBossEntity.m_20148_().equals(ManorFight.currentBoss((UUID)object))) {
            CLOCKS.remove(easterBossEntity);
            return;
        }
        Vec3 vec3 = ManorFight.anchorOf((UUID)object);
        if (vec3 == null) {
            CLOCKS.remove(easterBossEntity);
            return;
        }
        long l = livingEntity.m_46467_();
        PulseState pulseState = CLOCKS.get(easterBossEntity);
        if (pulseState == null || !pulseState.fight.equals(object) || l < pulseState.lastSeen) {
            CLOCKS.put(easterBossEntity, new PulseState((UUID)object, l));
            return;
        }
        pulseState.lastSeen = l;
        if (!ErosionPulseRules.due(l, pulseState.lastPulse)) {
            return;
        }
        pulseState.lastPulse = l;
        MobEffect mobEffect = (MobEffect)FTEffects.SHADOW_EROSION.get();
        for (ServerPlayer serverPlayer : livingEntity.m_6907_()) {
            if (serverPlayer instanceof FakePlayer || serverPlayer.m_9236_() != livingEntity || !serverPlayer.m_6084_() || serverPlayer.m_213877_() || serverPlayer.m_5833_() || serverPlayer.m_7500_() || !ErosionPulseRules.inside(serverPlayer.m_20182_().m_82557_(vec3))) continue;
            MobEffectInstance mobEffectInstance = serverPlayer.m_21124_(mobEffect);
            int n = ErosionPulseRules.nextAmplifier(mobEffectInstance == null ? -1 : mobEffectInstance.m_19564_());
            serverPlayer.m_7292_(new MobEffectInstance(mobEffect, 200, n, false, true, true));
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        LevelAccessor levelAccessor = unload.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)levelAccessor;
            CLOCKS.keySet().removeIf(easterBossEntity -> easterBossEntity.m_9236_() == serverLevel);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent serverStoppedEvent) {
        CLOCKS.clear();
    }

    private static final class PulseState {
        private final UUID fight;
        private long lastPulse;
        private long lastSeen;

        private PulseState(UUID uUID, long l) {
            this.fight = uUID;
            this.lastPulse = l;
            this.lastSeen = l;
        }
    }
}

