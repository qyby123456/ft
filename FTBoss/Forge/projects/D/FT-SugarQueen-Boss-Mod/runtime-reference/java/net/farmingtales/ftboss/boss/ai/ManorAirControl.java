/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorCombatContinuity;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ManorAirControl {
    public static final int BAN_COOLDOWN_TICKS = 2400;
    private static final String CHECK_TARGET = "ftboss_air_check_target";
    private static final String CHECK_DUE = "ftboss_air_check_due";
    private static final Map<ServerLevel, Map<UUID, Long>> BAN_NEXT = new WeakHashMap<ServerLevel, Map<UUID, Long>>();

    private ManorAirControl() {
    }

    public static boolean check(EasterBossEntity easterBossEntity, int n) {
        ServerPlayer serverPlayer;
        ServerLevel serverLevel2;
        Level level;
        block10: {
            block9: {
                level = easterBossEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block9;
                serverLevel2 = (ServerLevel)level;
                if (!easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.getVariant() != 9 && !easterBossEntity.m_21525_() && easterBossEntity.m_6084_() && !easterBossEntity.m_213877_() && ManorMultiplayerBalance.isManagedManorBoss((LivingEntity)easterBossEntity)) break block10;
            }
            return false;
        }
        level = easterBossEntity.m_5448_();
        if (!(level instanceof ServerPlayer) || !ManorMultiplayerBalance.validManorTarget((LivingEntity)easterBossEntity, (LivingEntity)(serverPlayer = (ServerPlayer)level))) {
            return false;
        }
        boolean bl = ManorLivingTargets.airborne((LivingEntity)serverPlayer);
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        if (!bl && n < 2400) {
            compoundTag.m_128473_(CHECK_TARGET);
            compoundTag.m_128473_(CHECK_DUE);
            return false;
        }
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
        Vec3 vec3 = ManorFight.anchorOf(uUID);
        if (uUID == null || vec3 == null) {
            return false;
        }
        long l = serverLevel2.m_46467_();
        String string = serverPlayer.m_20148_().toString();
        if (string.equals(compoundTag.m_128461_(CHECK_TARGET)) && l < compoundTag.m_128454_(CHECK_DUE)) {
            return false;
        }
        Map map = BAN_NEXT.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
        map.entrySet().removeIf(entry -> (Long)entry.getValue() <= l);
        long l2 = map.getOrDefault(uUID, 0L);
        compoundTag.m_128359_(CHECK_TARGET, string);
        if (l < l2 || ManorFlyBan.anyActive(serverLevel2)) {
            compoundTag.m_128356_(CHECK_DUE, l < l2 ? l2 : l + 20L);
            return false;
        }
        l2 = l + 2400L;
        map.put(uUID, l2);
        compoundTag.m_128356_(CHECK_DUE, l2);
        ManorFlyBan.trigger(serverLevel2, vec3.f_82479_, vec3.f_82480_, vec3.f_82481_);
        if (ManorFlyBan.active(serverPlayer) && ManorFlyBan.stripFlight(serverPlayer)) {
            ManorFlyBan.notifyPlayer(serverPlayer);
        }
        return true;
    }

    public static void fly(EasterBossEntity easterBossEntity, boolean bl, boolean bl2) {
        double d;
        LivingEntity livingEntity;
        if (!bl || !bl2) {
            return;
        }
        easterBossEntity.m_20242_(true);
        if (!easterBossEntity.m_21573_().m_26571_()) {
            easterBossEntity.m_21573_().m_26573_();
        }
        boolean bl3 = ManorCombatContinuity.present((Entity)easterBossEntity, livingEntity = easterBossEntity.m_5448_()) && (!ManorMultiplayerBalance.isManagedManorBoss((LivingEntity)easterBossEntity) || ManorMultiplayerBalance.validManorTarget((LivingEntity)easterBossEntity, livingEntity));
        double d2 = d = easterBossEntity.allowsParallelMelee() ? 12.25 : 9.0;
        if (!bl3 || easterBossEntity.m_20280_((Entity)livingEntity) <= d) {
            Vec3 vec3 = easterBossEntity.m_20184_();
            easterBossEntity.m_20334_(vec3.f_82479_ * 0.6, vec3.f_82480_ * 0.6, vec3.f_82481_ * 0.6);
            easterBossEntity.m_21566_().m_6849_(easterBossEntity.m_20185_(), easterBossEntity.m_20186_(), easterBossEntity.m_20189_(), 0.0);
            return;
        }
        double d3 = livingEntity.m_20186_() + (double)(livingEntity.m_20206_() - easterBossEntity.m_20206_()) * 0.5;
        easterBossEntity.m_21566_().m_6849_(livingEntity.m_20185_(), d3, livingEntity.m_20189_(), 1.2);
        Level level = easterBossEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (easterBossEntity.f_19797_ % 8 == 0 && !easterBossEntity.m_20096_()) {
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, easterBossEntity.m_20185_(), easterBossEntity.m_20186_() + (double)easterBossEntity.m_20206_() * 0.55, easterBossEntity.m_20189_(), 1, 0.22, 0.12, 0.22, 0.015);
            }
        }
    }
}

