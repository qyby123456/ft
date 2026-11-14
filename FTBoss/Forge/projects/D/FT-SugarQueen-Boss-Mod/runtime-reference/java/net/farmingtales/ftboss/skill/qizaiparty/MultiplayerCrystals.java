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
 *  net.minecraftforge.event.level.LevelEvent$Unload
 */
package net.farmingtales.ftboss.skill.qizaiparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaiparty.ExtraCrystalRoster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.LevelEvent;

public final class MultiplayerCrystals {
    private static final Map<ServerLevel, Map<UUID, Group>> active = new WeakHashMap<ServerLevel, Map<UUID, Group>>();

    private MultiplayerCrystals() {
    }

    private static Group group(EasterBossEntity easterBossEntity) {
        Object object;
        if (easterBossEntity == null || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = (Group)active.getOrDefault(serverLevel, Map.of()).get(easterBossEntity.m_20148_());
        return object != null && ((Group)object).owner == easterBossEntity ? object : null;
    }

    private static int index(QizaiCrystalEntity qizaiCrystalEntity) {
        Group group = qizaiCrystalEntity == null ? null : MultiplayerCrystals.group(qizaiCrystalEntity.getOwner());
        return group == null ? -1 : group.roster.indexOf(qizaiCrystalEntity);
    }

    public static int participants(EasterBossEntity easterBossEntity) {
        Vec3 vec3;
        Object object = easterBossEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return 1;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ManorFightMembers.fightId((Entity)easterBossEntity);
        Vec3 vec32 = vec3 = object != null && ManorFight.hasActive((UUID)object) ? ManorFight.anchorOf((UUID)object) : null;
        if (vec3 == null) {
            return BossScaling.playersInFight((LivingEntity)easterBossEntity);
        }
        int n = 0;
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!serverPlayer.m_6084_() || serverPlayer.m_213877_() || serverPlayer.m_7500_() || serverPlayer.m_5833_() || !(serverPlayer.m_20275_(vec3.f_82479_, vec3.f_82480_, vec3.f_82481_) <= 9216.0)) continue;
            ++n;
        }
        return Math.max(1, n);
    }

    public static void tick(EasterBossEntity easterBossEntity) {
        boolean bl;
        Level level = easterBossEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel2 = (ServerLevel)level;
        if (!QizaiCrystals.validOwner(easterBossEntity) || serverLevel2.m_8791_(easterBossEntity.m_20148_()) != easterBossEntity) {
            MultiplayerCrystals.cancel(easterBossEntity);
            return;
        }
        int n3 = ExtraCrystalRoster.count(MultiplayerCrystals.participants(easterBossEntity));
        Group group = MultiplayerCrystals.group(easterBossEntity);
        if (n3 == 0) {
            MultiplayerCrystals.cancel(easterBossEntity);
            return;
        }
        boolean bl2 = bl = QizaiCrystals.combatTarget(easterBossEntity) != null;
        if (group == null) {
            if (!bl) {
                return;
            }
            Map map = active.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
            if (map.size() >= 64) {
                return;
            }
            Group group2 = (Group)map.remove(easterBossEntity.m_20148_());
            if (group2 != null) {
                group2.close();
            }
            group = new Group(easterBossEntity);
            map.put(easterBossEntity.m_20148_(), group);
        }
        group.roster.update(serverLevel2.m_46467_(), n3, bl, () -> easterBossEntity.m_217043_().m_188503_(3), qizaiCrystalEntity -> qizaiCrystalEntity.m_6084_() && !qizaiCrystalEntity.m_213877_() && serverLevel2.m_8791_(qizaiCrystalEntity.m_20148_()) == qizaiCrystalEntity, (n, n2) -> MultiplayerCrystals.summon(serverLevel2, easterBossEntity, n, n2), qizaiCrystalEntity -> qizaiCrystalEntity.m_146870_());
    }

    private static QizaiCrystalEntity summon(ServerLevel serverLevel, EasterBossEntity easterBossEntity, int n, int n2) {
        QizaiCrystalEntity qizaiCrystalEntity = new QizaiCrystalEntity(serverLevel, easterBossEntity, n2);
        double d = ExtraCrystalRoster.angle(n, easterBossEntity.f_19797_);
        double d2 = ExtraCrystalRoster.radius(n);
        Vec3 vec3 = easterBossEntity.m_20182_().m_82520_(Math.cos(d) * d2, ExtraCrystalRoster.height(n), Math.sin(d) * d2);
        for (int i = 0; i < 3; ++i) {
            qizaiCrystalEntity.m_146884_(vec3.m_82520_(0.0, (double)i * 0.6, 0.0));
            if (!serverLevel.m_45784_((Entity)qizaiCrystalEntity) || !serverLevel.m_7967_((Entity)qizaiCrystalEntity)) continue;
            return qizaiCrystalEntity;
        }
        qizaiCrystalEntity.m_146870_();
        return null;
    }

    public static boolean accept(boolean bl, QizaiCrystalEntity qizaiCrystalEntity) {
        ServerLevel serverLevel;
        Level level;
        if (bl) {
            return true;
        }
        if (qizaiCrystalEntity == null || !qizaiCrystalEntity.m_6084_() || qizaiCrystalEntity.m_213877_()) {
            return false;
        }
        EasterBossEntity easterBossEntity = qizaiCrystalEntity.getOwner();
        return QizaiCrystals.validOwner(easterBossEntity) && (level = easterBossEntity.m_9236_()) instanceof ServerLevel && (serverLevel = (ServerLevel)level).m_8791_(easterBossEntity.m_20148_()) == easterBossEntity && serverLevel.m_8791_(qizaiCrystalEntity.m_20148_()) == qizaiCrystalEntity && MultiplayerCrystals.index(qizaiCrystalEntity) >= 0;
    }

    public static List<QizaiCrystalEntity> members(List<QizaiCrystalEntity> list, EasterBossEntity easterBossEntity) {
        Group group = MultiplayerCrystals.group(easterBossEntity);
        if (group == null) {
            return list;
        }
        ArrayList<QizaiCrystalEntity> arrayList = new ArrayList<QizaiCrystalEntity>(list);
        for (QizaiCrystalEntity qizaiCrystalEntity : group.roster.members()) {
            if (!MultiplayerCrystals.accept(false, qizaiCrystalEntity) || arrayList.contains((Object)qizaiCrystalEntity)) continue;
            arrayList.add(qizaiCrystalEntity);
        }
        return List.copyOf(arrayList);
    }

    public static double angle(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        int n = MultiplayerCrystals.index(qizaiCrystalEntity);
        return n < 0 ? d : ExtraCrystalRoster.angle(n, qizaiCrystalEntity.getOwner().f_19797_);
    }

    public static double radius(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        int n = MultiplayerCrystals.index(qizaiCrystalEntity);
        return n < 0 ? d : ExtraCrystalRoster.radius(n);
    }

    public static double height(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        int n = MultiplayerCrystals.index(qizaiCrystalEntity);
        return n < 0 ? d : ExtraCrystalRoster.height(n);
    }

    public static double bob(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        return MultiplayerCrystals.index(qizaiCrystalEntity) < 0 ? d : 0.3;
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        Object object;
        if (easterBossEntity == null || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = active.get(serverLevel);
        if (object == null) {
            return;
        }
        Group group = (Group)object.remove(easterBossEntity.m_20148_());
        if (group != null) {
            group.close();
        }
        if (object.isEmpty()) {
            active.remove(serverLevel);
        }
    }

    public static void unload(LevelEvent.Unload unload) {
        Map<UUID, Group> map = active.remove(unload.getLevel());
        if (map != null) {
            List.copyOf(map.values()).forEach(Group::close);
        }
    }

    public static void stop() {
        List<Map<UUID, Group>> list = List.copyOf(active.values());
        active.clear();
        for (Map<UUID, Group> map : list) {
            List.copyOf(map.values()).forEach(Group::close);
        }
    }

    private static final class Group {
        final EasterBossEntity owner;
        final ExtraCrystalRoster<QizaiCrystalEntity> roster = new ExtraCrystalRoster();

        Group(EasterBossEntity easterBossEntity) {
            this.owner = easterBossEntity;
        }

        void close() {
            this.roster.close(qizaiCrystalEntity -> qizaiCrystalEntity.m_146870_());
        }
    }
}

