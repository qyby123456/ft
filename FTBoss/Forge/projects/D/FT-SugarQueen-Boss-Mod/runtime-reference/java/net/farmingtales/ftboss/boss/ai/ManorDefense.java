/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveShield;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ManorDefenseNetwork;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorDefense {
    private static final Map<EasterBossEntity, ManorDefenseRules.State> STATES = new WeakHashMap<EasterBossEntity, ManorDefenseRules.State>();
    private static final Map<EasterBossEntity, Integer> ADAPTIVE = new WeakHashMap<EasterBossEntity, Integer>();

    private ManorDefense() {
    }

    private static boolean eligible(EasterBossEntity boss) {
        if (boss.m_9236_().f_46443_ || boss.isMini() || !boss.m_6084_() || boss.m_213877_() || boss.isManorIntroHidden() || boss.m_20145_() || boss.isXuanlanSkyHidden() || ManorFight.isFinaleMember((LivingEntity)boss) || HiddenBossIntro.isActing(boss.m_20148_())) {
            return false;
        }
        if (ManorDefense.nativeProtection(boss)) {
            return true;
        }
        LivingEntity target = boss.m_5448_();
        return !boss.m_21525_() && !boss.m_20147_() && target != null && target.m_6084_() && !target.m_213877_() && target.m_9236_() == boss.m_9236_() && boss.m_20280_((Entity)target) <= 4096.0;
    }

    private static boolean nativeProtection(EasterBossEntity boss) {
        return boss.getVariant() == 6 && boss.isXuanlanSkyCasting() || boss.getVariant() == 10 && boss.isManorMaidShielded() || boss.getVariant() == 11 && boss.isQizaiProtectedCasting();
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        boolean heartbeat;
        EasterBossEntity boss;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof EasterBossEntity) || !((livingEntity = (boss = (EasterBossEntity)livingEntity).m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)livingEntity;
        ManorDefenseRules.Profile profile = ManorDefenseRules.profile(boss.getVariant());
        if (profile == null) {
            ManorDefense.clear(boss);
            return;
        }
        int previousKind = ManorDefense.adaptiveKind(boss);
        int kind = ManorAdaptiveShield.activeKind((LivingEntity)boss);
        if (kind == 0) {
            ADAPTIVE.remove(boss);
        } else {
            ADAPTIVE.put(boss, kind);
        }
        boolean bl = heartbeat = level.m_46467_() % 20L == 0L;
        if (!ManorDefense.eligible(boss)) {
            ManorDefenseRules.State old = STATES.remove(boss);
            if (old != null || previousKind != kind || kind != 0 && heartbeat) {
                ManorDefenseNetwork.sync(boss, null);
            }
            return;
        }
        ManorDefenseRules.State state = STATES.get(boss);
        if (state == null || state.profile.variant() != boss.getVariant()) {
            state = new ManorDefenseRules.State(profile, level.m_46467_());
            STATES.put(boss, state);
        }
        int previous = state.phase;
        state.advance(level.m_46467_(), ManorDefense.nativeProtection(boss));
        if (previous != state.phase || previousKind != kind || (state.phase != 0 || kind != 0) && heartbeat) {
            ManorDefenseNetwork.sync(boss, state);
        }
    }

    public static void confirmedHit(EasterBossEntity boss, DamageSource source) {
        if (!ManorDefense.eligible(boss) || source == null || source.m_276093_(DamageTypes.f_286979_)) {
            return;
        }
        ManorDefenseRules.State state = STATES.get(boss);
        if (state == null || ManorDefense.nativeProtection(boss)) {
            return;
        }
        long now = boss.m_9236_().m_46467_();
        state.advance(now, false);
        if (state.confirmedHit(now)) {
            ManorDefenseNetwork.sync(boss, state);
            FTBossMod.LOGGER.debug("[FTBoss][\u9632\u5fa1] {} \u9884\u8b66 {} ticks, \u9632\u5b88 {} ticks, CD {} ticks", new Object[]{state.profile.name(), 12, state.profile.guardTicks(), state.profile.cooldownTicks()});
        }
    }

    public static float reduce(EasterBossEntity boss, DamageSource source, float amount) {
        if (source == null || !ManorDefense.eligible(boss) || ManorDefense.nativeProtection(boss)) {
            return amount;
        }
        ManorDefenseRules.State state = STATES.get(boss);
        if (state == null) {
            return amount;
        }
        state.advance(boss.m_9236_().m_46467_(), false);
        Entity direct = source.m_7640_();
        Entity attacker = source.m_7639_();
        Vec3 origin = attacker != null && attacker != boss ? attacker.m_20182_() : (direct != null ? direct.m_20182_() : source.m_7270_());
        double frontDot = -1.0;
        if (state.profile.frontCosine() <= -1.0) {
            frontDot = 1.0;
        } else if (origin != null) {
            Vec3 toward = origin.m_82546_(boss.m_20182_());
            double length = Math.sqrt(toward.f_82479_ * toward.f_82479_ + toward.f_82481_ * toward.f_82481_);
            double yaw = Math.toRadians(boss.f_20883_);
            if (length > 1.0E-6) {
                frontDot = (-Math.sin(yaw) * toward.f_82479_ + Math.cos(yaw) * toward.f_82481_) / length;
            }
        }
        boolean bypass = source.m_276093_(DamageTypes.f_286979_) || source.m_269533_(DamageTypeTags.f_268738_);
        return ManorDefenseRules.reduce(state.profile, state.phase, amount, source.m_269533_(DamageTypeTags.f_268524_), frontDot, bypass);
    }

    public static float reduceAfterSharing(float amount, EasterBossEntity boss, DamageSource source) {
        return ManorDefense.reduce(boss, source, amount);
    }

    public static ManorDefenseRules.State state(EasterBossEntity boss) {
        return STATES.get(boss);
    }

    public static int adaptiveKind(EasterBossEntity boss) {
        return ADAPTIVE.getOrDefault(boss, 0);
    }

    private static void clear(EasterBossEntity boss) {
        ManorDefenseRules.State old = STATES.remove(boss);
        Integer adaptive = ADAPTIVE.remove(boss);
        if (old != null || adaptive != null) {
            ManorDefenseNetwork.sync(boss, null);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)entity;
            if (!boss.m_9236_().f_46443_) {
                ManorDefense.clear(boss);
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        STATES.clear();
        ADAPTIVE.clear();
    }
}

