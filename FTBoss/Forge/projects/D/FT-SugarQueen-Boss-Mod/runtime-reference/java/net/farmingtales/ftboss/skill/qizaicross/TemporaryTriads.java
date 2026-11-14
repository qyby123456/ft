/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.EntityLeaveLevelEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.skill.qizaicross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiCrystalEntity;
import net.farmingtales.ftboss.skill.QizaiCrystals;
import net.farmingtales.ftboss.skill.qizaicross.TriadRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class TemporaryTriads {
    private static final Map<ServerLevel, Map<UUID, Batch>> active = new WeakHashMap<ServerLevel, Map<UUID, Batch>>();
    private static final Map<ServerLevel, Set<EasterBossEntity>> pending = new WeakHashMap<ServerLevel, Set<EasterBossEntity>>();

    private TemporaryTriads() {
    }

    private static boolean current(Batch batch) {
        ServerLevel serverLevel;
        Level level;
        if (batch == null || !QizaiCrystals.validOwner(batch.owner) || !((level = batch.owner.m_9236_()) instanceof ServerLevel) || active.getOrDefault(serverLevel = (ServerLevel)level, Map.of()).get(batch.owner.m_20148_()) != batch || serverLevel.m_8791_(batch.owner.m_20148_()) != batch.owner) {
            return false;
        }
        level = batch.owner.getSkillCue();
        return batch.lease.currentDuringTick(level.m_128451_("qizai"), level.m_128454_("start"), level.m_128451_("duration"), serverLevel.m_46467_());
    }

    private static Batch batch(EasterBossEntity easterBossEntity) {
        Object object;
        if (easterBossEntity == null || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = (Batch)active.getOrDefault(serverLevel, Map.of()).get(easterBossEntity.m_20148_());
        return object != null && ((Batch)object).owner == easterBossEntity ? object : null;
    }

    private static Batch batch(QizaiCrystalEntity qizaiCrystalEntity) {
        if (qizaiCrystalEntity == null) {
            return null;
        }
        Batch batch = TemporaryTriads.batch(qizaiCrystalEntity.getOwner());
        int n = qizaiCrystalEntity.role();
        return batch != null && n >= 0 && n < 3 && batch.crystals[n] == qizaiCrystalEntity ? batch : null;
    }

    public static boolean accept(boolean bl, QizaiCrystalEntity qizaiCrystalEntity) {
        return bl || qizaiCrystalEntity != null && qizaiCrystalEntity.m_6084_() && !qizaiCrystalEntity.m_213877_() && TemporaryTriads.current(TemporaryTriads.batch(qizaiCrystalEntity));
    }

    public static List<QizaiCrystalEntity> members(List<QizaiCrystalEntity> list, EasterBossEntity easterBossEntity) {
        Batch batch = TemporaryTriads.batch(easterBossEntity);
        if (!TemporaryTriads.current(batch)) {
            return list;
        }
        ArrayList<QizaiCrystalEntity> arrayList = new ArrayList<QizaiCrystalEntity>(list);
        for (QizaiCrystalEntity qizaiCrystalEntity : batch.crystals) {
            if (qizaiCrystalEntity == null || !qizaiCrystalEntity.m_6084_() || qizaiCrystalEntity.m_213877_() || arrayList.contains((Object)qizaiCrystalEntity)) continue;
            arrayList.add(qizaiCrystalEntity);
        }
        return List.copyOf(arrayList);
    }

    public static double angle(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        return TemporaryTriads.batch(qizaiCrystalEntity) == null ? d : d + 1.0471975511965976;
    }

    public static double radius(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Batch batch = TemporaryTriads.batch(qizaiCrystalEntity);
        return batch == null ? d : 6.0 * TriadRules.scale(batch.owner.m_20205_(), 0.6);
    }

    public static double height(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Batch batch = TemporaryTriads.batch(qizaiCrystalEntity);
        return batch == null ? d : 2.8 * TriadRules.scale(batch.owner.m_20206_(), 1.8);
    }

    public static double bob(double d, QizaiCrystalEntity qizaiCrystalEntity) {
        Batch batch = TemporaryTriads.batch(qizaiCrystalEntity);
        return batch == null ? d : d * TriadRules.scale(batch.owner.m_20206_(), 1.8);
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        Batch batch = TemporaryTriads.batch(easterBossEntity);
        if (batch != null) {
            batch.close();
        }
    }

    private static void remove(EasterBossEntity easterBossEntity) {
        Object object;
        if (easterBossEntity == null || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = active.get(serverLevel);
        if (object == null) {
            return;
        }
        Batch batch = (Batch)object.remove(easterBossEntity.m_20148_());
        if (batch != null) {
            batch.close();
        }
        if (object.isEmpty()) {
            active.remove(serverLevel);
        }
    }

    private static void pulse(QizaiCrystalEntity qizaiCrystalEntity, boolean bl) {
        Level level = qizaiCrystalEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            level = qizaiCrystalEntity.m_20182_();
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123810_, level.f_82479_, level.f_82480_ + 0.35, level.f_82481_, bl ? 10 : 16, 0.3, 0.45, 0.3, 0.035);
        }
    }

    private static void tick(EasterBossEntity easterBossEntity) {
        Level level = easterBossEntity.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel2 = (ServerLevel)level;
        level = easterBossEntity.getSkillCue();
        long l = serverLevel2.m_46467_();
        long l2 = level.m_128454_("start");
        int n = level.m_128451_("qizai");
        int n2 = level.m_128451_("duration");
        if (!QizaiCrystals.validOwner(easterBossEntity) || !TriadRules.active(n, l2, n2, l)) {
            TemporaryTriads.remove(easterBossEntity);
            return;
        }
        Map map = active.computeIfAbsent(serverLevel2, serverLevel -> new HashMap());
        Batch batch = (Batch)map.get(easterBossEntity.m_20148_());
        if (batch != null && batch.owner != easterBossEntity) {
            batch.close();
            map.remove(easterBossEntity.m_20148_());
            batch = null;
        }
        if (batch == null) {
            if (map.size() >= 64) {
                return;
            }
            batch = new Batch(easterBossEntity, l, l2);
            map.put(easterBossEntity.m_20148_(), batch);
        }
        if (!batch.lease.advance(n, l2, n2, l)) {
            batch.close();
            return;
        }
        for (int i = 0; i < 3; ++i) {
            if (!batch.lease.claim(i)) continue;
            QizaiCrystalEntity qizaiCrystalEntity = new QizaiCrystalEntity(serverLevel2, easterBossEntity, i);
            qizaiCrystalEntity.getPersistentData().m_128379_("ftboss_qizai_cross_temporary", true);
            double[] dArray = TriadRules.position(i, 0.0, easterBossEntity.m_20205_(), easterBossEntity.m_20206_());
            Vec3 vec3 = easterBossEntity.m_20182_().m_82520_(dArray[0], dArray[1], dArray[2]);
            boolean bl = false;
            for (int j = 0; j < 3; ++j) {
                qizaiCrystalEntity.m_146884_(vec3.m_82520_(0.0, (double)j * 0.6, 0.0));
                if (!serverLevel2.m_45784_((Entity)qizaiCrystalEntity)) continue;
                batch.crystals[i] = qizaiCrystalEntity;
                if (serverLevel2.m_7967_((Entity)qizaiCrystalEntity)) {
                    bl = true;
                    TemporaryTriads.pulse(qizaiCrystalEntity, true);
                    break;
                }
                batch.crystals[i] = null;
            }
            if (bl) continue;
            qizaiCrystalEntity.m_146870_();
        }
    }

    @SubscribeEvent
    public static void observed(LivingEvent.LivingTickEvent livingTickEvent) {
        EasterBossEntity easterBossEntity;
        LivingEntity livingEntity = livingTickEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 11 && (livingEntity = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel2 = (ServerLevel)livingEntity;
            pending.computeIfAbsent(serverLevel2, serverLevel -> new LinkedHashSet()).add(easterBossEntity);
        }
    }

    @SubscribeEvent
    public static void ended(TickEvent.LevelTickEvent levelTickEvent) {
        Object object;
        Object object2;
        if (levelTickEvent.phase != TickEvent.Phase.END || !((object2 = levelTickEvent.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object2;
        object2 = pending.remove(serverLevel);
        if (object2 != null) {
            object = object2.iterator();
            while (object.hasNext()) {
                EasterBossEntity easterBossEntity = (EasterBossEntity)object.next();
                if (easterBossEntity.m_9236_() != serverLevel || serverLevel.m_8791_(easterBossEntity.m_20148_()) != easterBossEntity) continue;
                TemporaryTriads.tick(easterBossEntity);
            }
        }
        if ((object = active.get(serverLevel)) == null) {
            return;
        }
        for (Map.Entry entry : List.copyOf(object.entrySet())) {
            Batch batch = (Batch)entry.getValue();
            CompoundTag compoundTag = batch.owner.getSkillCue();
            if (batch.owner.m_9236_() == serverLevel && serverLevel.m_8791_(batch.owner.m_20148_()) == batch.owner && QizaiCrystals.validOwner(batch.owner) && batch.lease.current(compoundTag.m_128451_("qizai"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), serverLevel.m_46467_())) continue;
            batch.close();
            if (batch.owner.m_9236_() == serverLevel && serverLevel.m_8791_(batch.owner.m_20148_()) == batch.owner && QizaiCrystals.validOwner(batch.owner) && TriadRules.active(compoundTag.m_128451_("qizai"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), serverLevel.m_46467_())) continue;
            object.remove(entry.getKey());
        }
        if (object.isEmpty()) {
            active.remove(serverLevel);
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            TemporaryTriads.remove(easterBossEntity);
        }
    }

    @SubscribeEvent
    public static void left(EntityLeaveLevelEvent entityLeaveLevelEvent) {
        Entity entity = entityLeaveLevelEvent.getEntity();
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
            TemporaryTriads.remove(easterBossEntity);
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        pending.remove(unload.getLevel());
        Map<UUID, Batch> map = active.remove(unload.getLevel());
        if (map != null) {
            for (Batch batch : List.copyOf(map.values())) {
                batch.close();
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent serverStoppedEvent) {
        for (Map<UUID, Batch> map : List.copyOf(active.values())) {
            for (Batch batch : List.copyOf(map.values())) {
                batch.close();
            }
        }
        active.clear();
        pending.clear();
    }

    private static final class Batch {
        final EasterBossEntity owner;
        final TriadRules.Lease lease;
        final QizaiCrystalEntity[] crystals = new QizaiCrystalEntity[3];

        Batch(EasterBossEntity easterBossEntity, long l, long l2) {
            this.owner = easterBossEntity;
            this.lease = new TriadRules.Lease(l, l2);
        }

        void close() {
            this.lease.close();
            for (int i = 0; i < this.crystals.length; ++i) {
                QizaiCrystalEntity qizaiCrystalEntity = this.crystals[i];
                this.crystals[i] = null;
                if (qizaiCrystalEntity == null || qizaiCrystalEntity.m_213877_()) continue;
                TemporaryTriads.pulse(qizaiCrystalEntity, false);
                qizaiCrystalEntity.m_146870_();
            }
        }
    }
}

