/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.entity;

import java.util.Iterator;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.farmingtales.ftboss.util.XuanlanFlywheelCount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class XuanlanEliteFlywheels {
    public static final String MARK = "ftboss_elite_flywheel";
    public static final String ECHO_FORM = "flywheel_echo";
    public static final int MAX_ELITE = 20;
    public static final double ORBIT_OFFSET = 1.9;
    public static final double TARGET_RANGE = 24.0;
    public static final float ECHO_SCALE = 0.9f;
    public static final float ECHO_ALPHA_MIN = 0.85f;
    public static final int KEEP_VOLLEY_SHOTS = 5;
    private static final double RANGE_SQR = 576.0;

    private XuanlanEliteFlywheels() {
    }

    public static boolean isElite(Entity entity) {
        if (!(entity instanceof FlywheelProjectileEntity)) {
            return false;
        }
        FlywheelProjectileEntity flywheelProjectileEntity = (FlywheelProjectileEntity)entity;
        ItemStack itemStack = flywheelProjectileEntity.m_7846_();
        if (itemStack.m_41619_()) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41783_();
        return compoundTag != null && compoundTag.m_128471_(MARK);
    }

    public static void mark(FlywheelProjectileEntity flywheelProjectileEntity) {
        flywheelProjectileEntity.m_7846_().m_41784_().m_128379_(MARK, true);
    }

    public static double widen(Entity entity, double d) {
        return XuanlanEliteFlywheels.isElite(entity) ? d + 1.9 : d;
    }

    public static LivingEntity pick(Entity entity) {
        Entity entity2;
        Object object;
        if (!XuanlanEliteFlywheels.isElite(entity)) {
            return ManorLivingTargets.flywheelTarget(entity);
        }
        if (entity instanceof Projectile) {
            object = (Projectile)entity;
            v0 = object.m_19749_();
        } else {
            v0 = entity2 = null;
        }
        if (entity2 == null) {
            return null;
        }
        object = ManorLivingTargets.nearby(entity2.m_9236_(), entity);
        LivingEntity livingEntity = null;
        double d = -1.0;
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            double d2;
            LivingEntity livingEntity2 = (LivingEntity)iterator.next();
            if (livingEntity2 == null || !livingEntity2.m_6084_() || livingEntity2 == entity2 || (d2 = livingEntity2.m_20280_(entity)) > 576.0 || !(d2 > d)) continue;
            d = d2;
            livingEntity = livingEntity2;
        }
        return livingEntity;
    }

    public static int count(ServerLevel serverLevel, EasterBossEntity easterBossEntity) {
        return serverLevel.m_6443_(FlywheelProjectileEntity.class, easterBossEntity.m_20191_().m_82400_(48.0), flywheelProjectileEntity -> flywheelProjectileEntity.m_19749_() == easterBossEntity && XuanlanEliteFlywheels.isElite((Entity)flywheelProjectileEntity)).size();
    }

    public static void summon(EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        if (XuanlanEliteFlywheels.count(serverLevel, easterBossEntity) >= XuanlanFlywheelCount.limit(easterBossEntity, serverLevel)) {
            return;
        }
        try {
            FlywheelProjectileEntity flywheelProjectileEntity = new FlywheelProjectileEntity((Level)serverLevel, (LivingEntity)easterBossEntity);
            XuanlanEliteFlywheels.mark(flywheelProjectileEntity);
            double d = Math.toRadians((double)easterBossEntity.m_146908_() + (easterBossEntity.m_217043_().m_188500_() - 0.5) * 50.0);
            double d2 = -Math.sin(d);
            double d3 = Math.cos(d);
            flywheelProjectileEntity.m_6034_(easterBossEntity.m_20185_() + d2 * 0.8, easterBossEntity.m_20188_() - 0.15, easterBossEntity.m_20189_() + d3 * 0.8);
            flywheelProjectileEntity.m_6686_(d2 * 0.55, 0.05, d3 * 0.55, 1.1f, 0.0f);
            ManorFightMembers.inherit((Entity)easterBossEntity, (Entity)flywheelProjectileEntity);
            serverLevel.m_7967_((Entity)flywheelProjectileEntity);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u6587\u7384\u84dd \u6295\u51fa\u7cbe\u82f1\u98de\u8f6e ({}/{}\uff0c\u666e\u901a\u578b\u5df2\u6ee1 25)", (Object)XuanlanEliteFlywheels.count(serverLevel, easterBossEntity), (Object)XuanlanFlywheelCount.limit(easterBossEntity, serverLevel));
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u6295\u63b7\u7cbe\u82f1\u98de\u8f6e\u5931\u8d25: {}", (Object)throwable.toString());
        }
    }

    public static int remainingVolley(EasterBossEntity easterBossEntity, ServerLevel serverLevel) {
        return XuanlanEliteFlywheels.count(serverLevel, easterBossEntity) >= XuanlanFlywheelCount.limit(easterBossEntity, serverLevel) ? 0 : 5;
    }

    public static float wheelScale(Entity entity, float f) {
        return XuanlanEliteFlywheels.isElite(entity) ? 0.001f : f;
    }

    public static float echoScale(Entity entity, float f) {
        return XuanlanEliteFlywheels.isElite(entity) ? 0.9f : f;
    }

    public static float echoAlpha(Entity entity, float f) {
        return XuanlanEliteFlywheels.isElite(entity) ? Math.max(0.85f, f) : f;
    }
}

