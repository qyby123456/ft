/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill.xuanlannative;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class NativeSignals {
    private NativeSignals() {
    }

    public static CompoundTag five(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        if (easterBossEntity.getVariant() != 6 || skill != ManorFiveCombatRules.Skill.XUANLAN_RAIL && skill != ManorFiveCombatRules.Skill.XUANLAN_RING) {
            return null;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("xuanlanNative", skill.id());
        compoundTag.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", skill.duration() - n);
        compoundTag.m_128405_("windup", skill.windup - n);
        compoundTag.m_128405_("pulses", skill.pulses);
        compoundTag.m_128405_("interval", skill.interval);
        compoundTag.m_128347_("x", vec3.f_82479_);
        compoundTag.m_128347_("y", vec3.f_82480_);
        compoundTag.m_128347_("z", vec3.f_82481_);
        compoundTag.m_128347_("radius", skill.radius);
        compoundTag.m_128347_("directionX", vec32.f_82479_);
        compoundTag.m_128347_("directionZ", vec32.f_82481_);
        compoundTag.m_128379_("follow", false);
        return compoundTag;
    }

    public static void hit(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        Level level;
        if (easterBossEntity.getVariant() == 6 && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            level = livingEntity.m_20191_().m_82399_();
            XuanlanFxPacket.send(serverLevel, 25, easterBossEntity.m_19879_(), (Vec3)level, (Vec3)level);
        }
    }

    public static void vent(EasterBossEntity easterBossEntity) {
        Level level;
        if (easterBossEntity.getVariant() == 6 && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            level = easterBossEntity.m_20182_().m_82520_(0.0, 1.0, 0.0);
            XuanlanFxPacket.send(serverLevel, 26, easterBossEntity.m_19879_(), (Vec3)level, level.m_82549_(easterBossEntity.m_20154_().m_82490_(10.75)));
        }
    }
}

