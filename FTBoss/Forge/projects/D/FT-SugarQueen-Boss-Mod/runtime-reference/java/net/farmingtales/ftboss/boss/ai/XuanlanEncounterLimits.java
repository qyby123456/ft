/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ImpossibleSparrowSupport;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class XuanlanEncounterLimits {
    private static final int VARIANT = 6;
    private static final int LIMIT = 15;

    private XuanlanEncounterLimits() {
    }

    public static boolean blocksSupport(Entity entity) {
        EasterBossEntity easterBossEntity;
        return entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 6;
    }

    public static EasterBossEntity supportTarget(EasterBossEntity easterBossEntity) {
        return XuanlanEncounterLimits.blocksSupport((Entity)easterBossEntity) ? null : easterBossEntity;
    }

    public static boolean stopSupport(EasterBossEntity easterBossEntity) {
        Object object;
        if (easterBossEntity.getVariant() != 9 || !((object = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = ManorFightMembers.fightId((Entity)easterBossEntity);
        UUID uUID = ManorFightMembers.ownerId((Entity)easterBossEntity);
        if (object != null && ManorFight.currentVariant((UUID)object) == 6 || uUID != null && XuanlanEncounterLimits.blocksSupport(serverLevel.m_8791_(uUID))) {
            ImpossibleSparrowSupport.failedSpawn(easterBossEntity);
            easterBossEntity.m_146870_();
            return true;
        }
        return false;
    }

    private static List<FlywheelProjectileEntity> wheels(ServerLevel serverLevel, EasterBossEntity easterBossEntity, Entity entity) {
        ArrayList<FlywheelProjectileEntity> arrayList = new ArrayList<FlywheelProjectileEntity>();
        for (Entity entity2 : serverLevel.m_8583_()) {
            Entity entity3;
            FlywheelProjectileEntity flywheelProjectileEntity;
            if (entity2 == entity || !(entity2 instanceof FlywheelProjectileEntity) || (flywheelProjectileEntity = (FlywheelProjectileEntity)entity2).m_213877_() || (entity3 = flywheelProjectileEntity.m_19749_()) == null || !entity3.m_20148_().equals(easterBossEntity.m_20148_())) continue;
            arrayList.add(flywheelProjectileEntity);
        }
        return arrayList;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        EasterBossEntity easterBossEntity;
        FlywheelProjectileEntity flywheelProjectileEntity;
        Entity entity;
        EasterBossEntity easterBossEntity2;
        Level level = entityJoinLevelEvent.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        level = entityJoinLevelEvent.getEntity();
        if (level instanceof EasterBossEntity && XuanlanEncounterLimits.stopSupport(easterBossEntity2 = (EasterBossEntity)level)) {
            entityJoinLevelEvent.setCanceled(true);
        } else if (level instanceof FlywheelProjectileEntity && (entity = (flywheelProjectileEntity = (FlywheelProjectileEntity)level).m_19749_()) instanceof EasterBossEntity && XuanlanEncounterLimits.blocksSupport((Entity)(easterBossEntity = (EasterBossEntity)entity)) && XuanlanEncounterLimits.wheels(serverLevel, easterBossEntity, (Entity)flywheelProjectileEntity).size() >= 15) {
            entityJoinLevelEvent.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onTick(LivingEvent.LivingTickEvent livingTickEvent) {
        EasterBossEntity easterBossEntity;
        Object object = livingTickEvent.getEntity();
        if (!(object instanceof EasterBossEntity) || !((object = (easterBossEntity = (EasterBossEntity)object).m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        if (XuanlanEncounterLimits.stopSupport(easterBossEntity) || !XuanlanEncounterLimits.blocksSupport((Entity)easterBossEntity) || serverLevel.m_46467_() % 20L != 0L) {
            return;
        }
        object = XuanlanEncounterLimits.wheels(serverLevel, easterBossEntity, null);
        if (object.size() <= 15) {
            return;
        }
        object.sort(Comparator.comparingInt(Entity::m_19879_));
        for (int i = 15; i < object.size(); ++i) {
            ((FlywheelProjectileEntity)((Object)object.get(i))).m_146870_();
        }
    }
}

