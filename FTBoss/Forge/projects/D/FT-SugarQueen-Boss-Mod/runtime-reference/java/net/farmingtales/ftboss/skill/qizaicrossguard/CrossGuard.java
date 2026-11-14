/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.entity.living.LivingKnockBackEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.skill.qizaicrossguard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaicross.TriadRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class CrossGuard {
    private static final String MARKER = "ftboss_qizai_cross_extra_guard";
    private static final String ARMED = "ftboss_qizai_cross_extra_guard_armed";
    private static final Map<ServerLevel, Map<UUID, Group>> ACTIVE = new WeakHashMap<ServerLevel, Map<UUID, Group>>();

    private CrossGuard() {
    }

    public static boolean holding(EasterBossEntity easterBossEntity) {
        ServerLevel serverLevel;
        Level level;
        if (!QizaiCrystals.validOwner(easterBossEntity) || !((level = easterBossEntity.m_9236_()) instanceof ServerLevel) || (serverLevel = (ServerLevel)level).m_8791_(easterBossEntity.m_20148_()) != easterBossEntity) {
            return false;
        }
        level = easterBossEntity.getSkillCue();
        Group group = (Group)ACTIVE.getOrDefault(serverLevel, Map.of()).get(easterBossEntity.m_20148_());
        return TriadRules.active(level.m_128451_("qizai"), level.m_128454_("start"), level.m_128451_("duration"), serverLevel.m_46467_()) || group != null && group.owner == easterBossEntity && group.lease.currentDuringTick(level.m_128451_("qizai"), level.m_128454_("start"), level.m_128451_("duration"), serverLevel.m_46467_());
    }

    private static Group group(EasterBossEntity easterBossEntity) {
        Object object;
        if (easterBossEntity == null || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = (Group)ACTIVE.getOrDefault(serverLevel, Map.of()).get(easterBossEntity.m_20148_());
        return object != null && ((Group)object).owner == easterBossEntity ? object : null;
    }

    private static boolean current(Group group) {
        ServerLevel serverLevel;
        Level level;
        if (group == null || !QizaiCrystals.validOwner(group.owner) || !((level = group.owner.m_9236_()) instanceof ServerLevel) || (serverLevel = (ServerLevel)level).m_8791_(group.owner.m_20148_()) != group.owner || CrossGuard.group(group.owner) != group) {
            return false;
        }
        level = group.owner.getSkillCue();
        return group.lease.currentDuringTick(level.m_128451_("qizai"), level.m_128454_("start"), level.m_128451_("duration"), serverLevel.m_46467_());
    }

    private static Group group(QizaiCrystalEntity qizaiCrystalEntity) {
        if (qizaiCrystalEntity == null || !qizaiCrystalEntity.m_6084_() || qizaiCrystalEntity.m_213877_()) {
            return null;
        }
        Group group = CrossGuard.group(qizaiCrystalEntity.getOwner());
        if (!CrossGuard.current(group) || group.crystal != qizaiCrystalEntity || qizaiCrystalEntity.m_9236_() != group.owner.m_9236_()) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)group.owner.m_9236_();
        return serverLevel.m_8791_(qizaiCrystalEntity.m_20148_()) == qizaiCrystalEntity ? group : null;
    }

    public static void stabilize(EasterBossEntity easterBossEntity) {
        if (CrossGuard.holding(easterBossEntity)) {
            easterBossEntity.m_21573_().m_26573_();
            Vec3 vec3 = easterBossEntity.m_20184_();
            easterBossEntity.m_20256_(new Vec3(0.0, Math.min(0.0, vec3.f_82480_), 0.0));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void livingTick(LivingEvent.LivingTickEvent livingTickEvent) {
        EasterBossEntity easterBossEntity;
        LivingEntity livingEntity = livingTickEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 11 && easterBossEntity.m_9236_() instanceof ServerLevel) {
            CrossGuard.tick(easterBossEntity);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
    public static void knockback(LivingKnockBackEvent livingKnockBackEvent) {
        EasterBossEntity easterBossEntity;
        LivingEntity livingEntity = livingKnockBackEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity && CrossGuard.holding(easterBossEntity = (EasterBossEntity)livingEntity)) {
            livingKnockBackEvent.setCanceled(true);
            CrossGuard.stabilize(easterBossEntity);
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            CrossGuard.cancel(easterBossEntity);
        }
    }

    @SubscribeEvent
    public static void leave(EntityLeaveLevelEvent entityLeaveLevelEvent) {
        Entity entity = entityLeaveLevelEvent.getEntity();
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
            CrossGuard.cancel(easterBossEntity);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        CrossGuard.unloadLevel(unload);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent serverStoppedEvent) {
        CrossGuard.stop();
    }

    public static Vec3 movement(EasterBossEntity easterBossEntity, MoverType moverType, Vec3 vec3) {
        if (!CrossGuard.holding(easterBossEntity) || vec3 == null) {
            return vec3;
        }
        CrossGuard.stabilize(easterBossEntity);
        return new Vec3(0.0, Math.min(0.0, vec3.f_82480_), 0.0);
    }

    public static void cue(EasterBossEntity easterBossEntity) {
        CrossGuard.stabilize(easterBossEntity);
        CrossGuard.tick(easterBossEntity);
    }

    public static void tick(EasterBossEntity easterBossEntity) {
        if (!CrossGuard.holding(easterBossEntity)) {
            CrossGuard.cancel(easterBossEntity);
            return;
        }
        ServerLevel serverLevel2 = (ServerLevel)easterBossEntity.m_9236_();
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        Map map = ACTIVE.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
        Group group = (Group)map.get(easterBossEntity.m_20148_());
        if (group != null && group.owner != easterBossEntity) {
            group.close();
            map.remove(easterBossEntity.m_20148_());
            group = null;
        }
        if (group == null) {
            if (map.size() >= 64) {
                return;
            }
            group = new Group(easterBossEntity, serverLevel2.m_46467_(), compoundTag.m_128454_("start"));
            map.put(easterBossEntity.m_20148_(), group);
        }
        if (!TriadRules.active(compoundTag.m_128451_("qizai"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), serverLevel2.m_46467_())) {
            return;
        }
        if (!group.lease.advance(compoundTag.m_128451_("qizai"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), serverLevel2.m_46467_())) {
            group.close();
            return;
        }
        if (!group.lease.claim(2)) {
            return;
        }
        QizaiCrystalEntity qizaiCrystalEntity = new QizaiCrystalEntity(serverLevel2, easterBossEntity, 2);
        qizaiCrystalEntity.getPersistentData().m_128379_(MARKER, true);
        double d = 0.0;
        double d2 = 6.0 * TriadRules.scale(easterBossEntity.m_20205_(), 0.6);
        double d3 = (2.8 + Math.sin(2.0) * 0.4) * TriadRules.scale(easterBossEntity.m_20206_(), 1.8);
        Vec3 vec3 = easterBossEntity.m_20182_().m_82520_(Math.cos(d) * d2, d3, Math.sin(d) * d2);
        for (int i = 0; i < 3; ++i) {
            qizaiCrystalEntity.m_146884_(vec3.m_82520_(0.0, (double)i * 0.6, 0.0));
            if (!serverLevel2.m_45784_((Entity)qizaiCrystalEntity)) continue;
            group.crystal = qizaiCrystalEntity;
            if (serverLevel2.m_7967_((Entity)qizaiCrystalEntity)) {
                return;
            }
            group.crystal = null;
        }
        qizaiCrystalEntity.m_146870_();
    }

    public static boolean accept(boolean bl, QizaiCrystalEntity qizaiCrystalEntity) {
        return bl || CrossGuard.group(qizaiCrystalEntity) != null;
    }

    public static List<QizaiCrystalEntity> members(List<QizaiCrystalEntity> list, EasterBossEntity easterBossEntity) {
        Group group = CrossGuard.group(easterBossEntity);
        if (!CrossGuard.current(group) || CrossGuard.group(group.crystal) == null || list.contains((Object)group.crystal)) {
            return list;
        }
        ArrayList<QizaiCrystalEntity> arrayList = new ArrayList<QizaiCrystalEntity>(list);
        arrayList.add(group.crystal);
        return List.copyOf(arrayList);
    }

    public static int arm(int n, QizaiCrystalEntity qizaiCrystalEntity) {
        if (CrossGuard.group(qizaiCrystalEntity) == null || qizaiCrystalEntity.role() != 2) {
            return n;
        }
        CompoundTag compoundTag = qizaiCrystalEntity.getPersistentData();
        if (!compoundTag.m_128471_(MARKER) || compoundTag.m_128471_(ARMED)) {
            return n;
        }
        compoundTag.m_128379_(ARMED, true);
        return 0;
    }

    public static float absorb(float f, float f2, QizaiCrystalEntity qizaiCrystalEntity) {
        if (qizaiCrystalEntity == null || !Float.isFinite(f) || f < 0.0f || !Float.isFinite(f2) || f2 <= 0.0f) {
            return f;
        }
        Group group = CrossGuard.group(qizaiCrystalEntity.getOwner());
        if (!CrossGuard.current(group) || CrossGuard.group(group.crystal) == null || qizaiCrystalEntity == group.crystal) {
            return f;
        }
        float f3 = Math.max(0.0f, f2 * 0.35f - f);
        return f + group.crystal.absorb(f3);
    }

    public static double angle(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Group group = CrossGuard.group(qizaiCrystalEntity);
        return group == null ? d : (double)qizaiCrystalEntity.f_19797_ * 0.022;
    }

    public static double radius(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Group group = CrossGuard.group(qizaiCrystalEntity);
        return group == null ? d : 6.0 * TriadRules.scale(group.owner.m_20205_(), 0.6);
    }

    public static double height(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Group group = CrossGuard.group(qizaiCrystalEntity);
        return group == null ? d : 2.8 * TriadRules.scale(group.owner.m_20206_(), 1.8);
    }

    public static double bob(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Group group = CrossGuard.group(qizaiCrystalEntity);
        return group == null ? d : d * TriadRules.scale(group.owner.m_20206_(), 1.8);
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        if (easterBossEntity == null) {
            return;
        }
        for (Map<UUID, Group> map : List.copyOf(ACTIVE.values())) {
            Group group = map.get(easterBossEntity.m_20148_());
            if (group == null || group.owner != easterBossEntity) continue;
            map.remove(easterBossEntity.m_20148_());
            group.close();
        }
        ACTIVE.values().removeIf(Map::isEmpty);
    }

    @SubscribeEvent
    public static void ended(TickEvent.LevelTickEvent levelTickEvent) {
        Object object;
        if (levelTickEvent.phase != TickEvent.Phase.END || !((object = levelTickEvent.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ACTIVE.get(serverLevel);
        if (object == null) {
            return;
        }
        for (Group group : List.copyOf(object.values())) {
            CompoundTag compoundTag = group.owner.getSkillCue();
            if (group.owner.m_9236_() == serverLevel && serverLevel.m_8791_(group.owner.m_20148_()) == group.owner && QizaiCrystals.validOwner(group.owner) && group.lease.current(compoundTag.m_128451_("qizai"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), serverLevel.m_46467_())) continue;
            group.close();
            object.remove(group.owner.m_20148_(), group);
        }
        if (object.isEmpty()) {
            ACTIVE.remove(serverLevel);
        }
    }

    private static void unloadLevel(LevelEvent.Unload unload) {
        Map<UUID, Group> map = ACTIVE.remove(unload.getLevel());
        if (map != null) {
            map.values().forEach(Group::close);
        }
    }

    public static void stop() {
        ACTIVE.values().forEach(map -> map.values().forEach(Group::close));
        ACTIVE.clear();
    }

    private static final class Group {
        final EasterBossEntity owner;
        final TriadRules.Lease lease;
        QizaiCrystalEntity crystal;

        Group(EasterBossEntity easterBossEntity, long l, long l2) {
            this.owner = easterBossEntity;
            this.lease = new TriadRules.Lease(l, l2);
        }

        void close() {
            this.lease.close();
            if (this.crystal != null) {
                this.crystal.m_146870_();
                this.crystal = null;
            }
        }
    }
}

