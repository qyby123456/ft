/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 */
package net.farmingtales.ftboss.entity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyilight.LightHooks;
import net.farmingtales.ftboss.util.BossKnockback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class QingyiTridentEntity
extends ThrownTrident {
    private int weaponIndex = 0;
    public static final int ORB_PULSE_TICKS = 60;
    public static final int STRIKE_CD = 70;
    public static final int BUFF_CD = 60;
    private static final Map<UUID, Long> LAST_STRIKE = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, Long> LAST_BUFF = new ConcurrentHashMap<UUID, Long>();
    public static final double ORB_RANGE = 24.0;
    private int pulseTicks = 0;

    public QingyiTridentEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public QingyiTridentEntity(Level level, LivingEntity livingEntity, ItemStack itemStack, int n) {
        super(level, livingEntity, itemStack);
        this.weaponIndex = n;
        this.m_6034_(livingEntity.m_20185_(), livingEntity.m_20188_() - 0.1, livingEntity.m_20189_());
    }

    public int getWeaponIndex() {
        return this.weaponIndex;
    }

    public void m_8119_() {
        MobEffect[] mobEffectArray;
        EasterBossEntity easterBossEntity;
        Entity entity;
        if (!this.m_9236_().f_46443_ && (this.f_19797_ >= 600 || !((entity = this.m_19749_()) instanceof EasterBossEntity) || !(easterBossEntity = (EasterBossEntity)entity).m_6084_() || easterBossEntity.m_213877_() || easterBossEntity.m_21525_() || !easterBossEntity.allowsParallelMelee() || ManorFight.isFinaleMember((LivingEntity)easterBossEntity) || this.m_20280_((Entity)easterBossEntity) > 4096.0)) {
            this.m_146870_();
            return;
        }
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            return;
        }
        if (++this.pulseTicks < 60) {
            return;
        }
        this.pulseTicks = 0;
        entity = this.m_9236_();
        if (!(entity instanceof ServerLevel)) {
            return;
        }
        easterBossEntity = (ServerLevel)entity;
        Object object = this.m_19749_();
        if (!(object instanceof LivingEntity) || !(entity = (LivingEntity)object).m_6084_()) {
            return;
        }
        if (entity instanceof EasterBossEntity && ((object = (EasterBossEntity)entity).m_21525_() || ((EasterBossEntity)object).isMini() || ManorFight.isFinaleMember((LivingEntity)object))) {
            return;
        }
        long l = easterBossEntity.m_46467_();
        UUID uUID = entity.m_20148_();
        if (QingyiTridentEntity.ready(LAST_STRIKE, uUID, l, 70)) {
            mobEffectArray = BossAggro.highest((LivingEntity)entity);
            double d = 576.0;
            if (mobEffectArray != null && mobEffectArray.m_20280_((Entity)this) > d) {
                mobEffectArray = null;
            }
            if (mobEffectArray == null) {
                mobEffectArray = BossTargets.nearest((ServerLevel)easterBossEntity, (Entity)this, 24.0);
            }
            if (mobEffectArray != null) {
                try {
                    LightningBolt lightningBolt = (LightningBolt)EntityType.f_20465_.m_20615_((Level)easterBossEntity);
                    if (lightningBolt != null) {
                        lightningBolt.m_6027_(mobEffectArray.m_20185_(), mobEffectArray.m_20186_(), mobEffectArray.m_20189_());
                        lightningBolt.m_20874_(false);
                        LightHooks.orbSpawn((ServerLevel)easterBossEntity, (Entity)lightningBolt, this);
                    }
                    float f = 36.0f;
                    Entity entity2 = this.m_19749_();
                    if (entity2 instanceof EasterBossEntity) {
                        EasterBossEntity easterBossEntity2 = (EasterBossEntity)entity2;
                        f = easterBossEntity2.manorSkillDamage((LivingEntity)mobEffectArray, f + easterBossEntity2.qingyiDamageBonus(), 1.5);
                    }
                    mobEffectArray.f_19802_ = 0;
                    mobEffectArray.m_6469_(this.m_269291_().m_269104_((Entity)this, this.m_19749_()), f);
                    LAST_STRIKE.put(uUID, l);
                    FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u4e09\u53c9\u621f(\u5145\u80fd\u7403\u5f0f) \u843d\u96f7: \u76ee\u6807={}", (Object)mobEffectArray.m_7755_().getString());
                }
                catch (Throwable throwable) {
                    FTBossMod.LOGGER.warn("[FTBoss][Easter] \u4e09\u53c9\u621f\u53ec\u5524\u95ea\u7535\u5931\u8d25: {}", (Object)throwable.toString());
                }
            }
        }
        if (entity.m_6084_() && QingyiTridentEntity.ready(LAST_BUFF, uUID, l, 60)) {
            mobEffectArray = new MobEffect[]{MobEffects.f_19596_, MobEffects.f_19600_, MobEffects.f_19606_, MobEffects.f_19605_, MobEffects.f_19617_, MobEffects.f_19607_};
            MobEffect mobEffect = mobEffectArray[this.f_19796_.m_188503_(mobEffectArray.length)];
            LightHooks.orbBuff((LivingEntity)entity, new MobEffectInstance(mobEffect, 200, 1, true, false, false), this);
            LAST_BUFF.put(uUID, l);
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u9752\u8863\u4e09\u53c9\u621f(\u5145\u80fd\u7403\u5f0f)\u7ed9\u672c\u4f53\u4e0a\u589e\u76ca: {}", (Object)mobEffect.m_19482_().getString());
        }
    }

    public static boolean spawnOwned(ServerLevel serverLevel, EasterBossEntity easterBossEntity, QingyiTridentEntity qingyiTridentEntity2) {
        List list = serverLevel.m_6443_(QingyiTridentEntity.class, easterBossEntity.m_20191_().m_82400_(128.0), qingyiTridentEntity -> qingyiTridentEntity.m_19749_() == easterBossEntity);
        list.sort(Comparator.comparingInt(qingyiTridentEntity -> qingyiTridentEntity.f_19797_).reversed());
        int n = list.size() - 48 + 1;
        for (int i = 0; i < n; ++i) {
            ((QingyiTridentEntity)((Object)list.get(i))).m_146870_();
        }
        return serverLevel.m_7967_((Entity)qingyiTridentEntity2);
    }

    public boolean m_142391_() {
        return false;
    }

    private static boolean ready(Map map, UUID uUID, long l, int n) {
        Long l2 = (Long)map.get(uUID);
        if (l2 != null && l - l2 < (long)n) {
            return false;
        }
        if (map.size() > 32) {
            map.entrySet().removeIf(entry -> l - (Long)entry.getValue() > 12000L);
        }
        return true;
    }

    protected void m_5790_(EntityHitResult entityHitResult) {
        LivingEntity livingEntity;
        super.m_5790_(entityHitResult);
        if (this.m_9236_().f_46443_) {
            return;
        }
        Entity entity = entityHitResult.m_82443_();
        if (!(entity instanceof LivingEntity) || !(livingEntity = (LivingEntity)entity).m_6084_() || ManorLivingTargets.creative(livingEntity) || livingEntity.m_5833_()) {
            return;
        }
        BossKnockback.push((Entity)livingEntity, 0.0, 1.05, 0.0);
        livingEntity.f_19864_ = true;
        Entity entity2 = this.m_19749_();
        if (!(entity2 instanceof EasterBossEntity)) {
            return;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)entity2;
        try {
            easterBossEntity.onTridentHit(livingEntity, this.weaponIndex);
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] \u4e09\u53c9\u621f\u547d\u4e2d\u56de\u8c03\u5f02\u5e38: {}", (Object)throwable.toString());
        }
    }

    protected boolean m_5603_(Entity entity) {
        if (!ManorLivingTargets.canHit((Entity)this, entity)) {
            return false;
        }
        return super.m_5603_(entity);
    }
}

