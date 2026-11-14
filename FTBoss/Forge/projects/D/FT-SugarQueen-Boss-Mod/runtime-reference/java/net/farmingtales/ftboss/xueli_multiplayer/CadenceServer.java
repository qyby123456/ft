/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.xueli_multiplayer;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_multiplayer.CadenceRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class CadenceServer {
    private static final Map<EasterBossEntity, Snapshot> COUNTS = new WeakHashMap<EasterBossEntity, Snapshot>();

    private CadenceServer() {
    }

    public static int players(EasterBossEntity easterBossEntity) {
        ServerPlayer serverPlayer;
        double d;
        Level level;
        if (easterBossEntity == null || !CadenceRules.enabled(easterBossEntity.getVariant(), easterBossEntity.isMini(), easterBossEntity.isGiant(), easterBossEntity.isManorIntroHidden(), easterBossEntity.m_21525_(), easterBossEntity.m_6084_(), easterBossEntity.m_9236_().f_46443_, ManorFight.isFinaleMember((LivingEntity)easterBossEntity)) || !((level = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return 1;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        long l = serverLevel.m_46467_();
        Snapshot snapshot = COUNTS.get(easterBossEntity);
        if (snapshot != null && snapshot.level() == serverLevel && snapshot.tick() == l) {
            return snapshot.players();
        }
        UUID uUID = ManorFightMembers.fightId((Entity)easterBossEntity);
        Vec3 vec3 = uUID != null && ManorFight.hasActive(uUID) ? ManorFight.anchorOf(uUID) : null;
        double d2 = d = vec3 == null ? 2304.0 : 9216.0;
        if (vec3 == null) {
            vec3 = easterBossEntity.m_20182_();
        }
        int n = 0;
        Iterator iterator = serverLevel.m_6907_().iterator();
        while (iterator.hasNext() && (!CadenceRules.participant((serverPlayer = (ServerPlayer)iterator.next()).m_6084_(), serverPlayer.m_7500_(), serverPlayer.m_5833_(), serverPlayer.m_9236_() == serverLevel, serverPlayer.m_20275_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_), d) || ++n < 6)) {
        }
        int n2 = Math.max(1, n);
        COUNTS.put(easterBossEntity, new Snapshot(serverLevel, l, n2));
        return n2;
    }

    public static int melee(int n, EasterBossEntity easterBossEntity) {
        return CadenceRules.interval(n, CadenceServer.players(easterBossEntity), 10);
    }

    public static int skill(int n, EasterBossEntity easterBossEntity) {
        return CadenceRules.interval(n, CadenceServer.players(easterBossEntity), 40);
    }

    public static long cooldown(long l, EasterBossEntity easterBossEntity, long l2) {
        return CadenceRules.deadline(l, l2, 0, CadenceServer.players(easterBossEntity), 40);
    }

    public static long ready(long l, EasterBossEntity easterBossEntity, long l2, int n) {
        return CadenceRules.deadline(l, l2, n, CadenceServer.players(easterBossEntity), 20);
    }

    private record Snapshot(ServerLevel level, long tick, int players) {
    }
}

