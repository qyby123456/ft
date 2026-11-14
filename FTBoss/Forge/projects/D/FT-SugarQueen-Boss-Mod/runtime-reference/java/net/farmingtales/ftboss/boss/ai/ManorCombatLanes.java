/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import java.util.function.Predicate;
import net.farmingtales.ftboss.boss.ai.BossThreatTable;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class ManorCombatLanes {
    private ManorCombatLanes() {
    }

    public static boolean independent(EasterBossEntity easterBossEntity) {
        if (easterBossEntity == null || easterBossEntity.isMini() || easterBossEntity.isGiant()) {
            return false;
        }
        int n = easterBossEntity.getVariant();
        return n == 0 || n == 1 || n == 4 || n == 5 || n == 7 || n == 10;
    }

    public static boolean spellReady(EasterBossEntity easterBossEntity) {
        return easterBossEntity.allowsParallelMelee() || ManorCombatLanes.independent(easterBossEntity);
    }

    public static boolean blocksMelee(boolean bl, EasterBossEntity easterBossEntity, boolean bl2) {
        return bl && (!ManorCombatLanes.independent(easterBossEntity) || bl2);
    }

    public static boolean rangedBlocksMelee(boolean bl, EasterBossEntity easterBossEntity) {
        if (bl || !ManorCombatLanes.independent(easterBossEntity)) {
            return ManorLivingTargets.preferRanged(bl, easterBossEntity);
        }
        return false;
    }

    public static LivingEntity selectThreat(BossThreatTable bossThreatTable, long l, UUID uUID, Predicate<UUID> predicate, LivingEntity livingEntity) {
        Object object;
        LivingEntity livingEntity2;
        Object object2;
        Object object3;
        UUID uUID2 = bossThreatTable.highest(l, uUID, predicate);
        ServerLevel serverLevel = (ServerLevel)livingEntity.m_9236_();
        if (uUID2 != null && (object3 = serverLevel.m_8791_(uUID2)) instanceof LivingEntity) {
            object2 = (LivingEntity)object3;
            v0 = object2;
        } else {
            v0 = livingEntity2 = null;
        }
        if (!ManorMultiplayerBalance.isManagedManorBoss(livingEntity) || !(livingEntity instanceof EasterBossEntity) || ((EasterBossEntity)(object2 = (EasterBossEntity)livingEntity)).isGiant() || ((EasterBossEntity)object2).isMini() || livingEntity2 instanceof ServerPlayer) {
            return livingEntity2;
        }
        object3 = null;
        double d = 0.0;
        LivingEntity livingEntity3 = ((Mob)livingEntity).m_5448_();
        if (livingEntity3 instanceof ServerPlayer && predicate.test((object = (ServerPlayer)livingEntity3).m_20148_()) && (d = bossThreatTable.value(object.m_20148_(), l)) > 0.0) {
            object3 = object;
        }
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            double d2;
            if (!predicate.test(serverPlayer.m_20148_()) || !((d2 = bossThreatTable.value(serverPlayer.m_20148_(), l)) > d) && (!(d2 > 0.0) || d2 != d || object3 == null || livingEntity3 == object3 || serverPlayer.m_20148_().toString().compareTo(object3.m_20148_().toString()) >= 0)) continue;
            object3 = serverPlayer;
            d = d2;
        }
        return object3 != null ? object3 : livingEntity2;
    }

    public static UUID selectThreatId(BossThreatTable bossThreatTable, long l, UUID uUID, Predicate<UUID> predicate, LivingEntity livingEntity) {
        LivingEntity livingEntity2 = ManorCombatLanes.selectThreat(bossThreatTable, l, uUID, predicate, livingEntity);
        return livingEntity2 == null ? null : livingEntity2.m_20148_();
    }
}

