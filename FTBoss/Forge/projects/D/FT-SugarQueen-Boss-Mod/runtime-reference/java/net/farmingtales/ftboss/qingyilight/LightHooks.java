/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ThrownPotion
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.ProjectileImpactEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.qingyilight;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QingyiTridentEntity;
import net.farmingtales.ftboss.qingyilight.LightNetwork;
import net.farmingtales.ftboss.qingyilight.LightPacket;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.QingyiConfluenceRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class LightHooks {
    private LightHooks() {
    }

    private static boolean own(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 5 && !easterBossEntity.isMini() && !easterBossEntity.m_21525_() && easterBossEntity.m_6084_() && easterBossEntity.m_9236_() instanceof ServerLevel;
    }

    private static void signal(EasterBossEntity easterBossEntity, int n, int n2, Vec3 vec3, Vec3 vec32, double d, int n3, int n4, Entity entity) {
        if (!LightHooks.own(easterBossEntity)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)easterBossEntity.m_9236_();
        LightNetwork.send(serverLevel, new LightPacket(n, n2, easterBossEntity.m_19879_(), easterBossEntity.m_20148_(), serverLevel.m_46472_().m_135782_(), serverLevel.m_46467_(), vec3, vec32.m_82541_(), d, n3, n4, entity == null ? -1 : entity.m_19879_(), entity == null ? null : entity.m_20148_()));
    }

    private static int kind(ManorFiveCombatRules.Skill skill) {
        return skill == ManorFiveCombatRules.Skill.QINGYI_TIDE ? 0 : (skill == ManorFiveCombatRules.Skill.QINGYI_THUNDER ? 1 : (skill == ManorFiveCombatRules.Skill.QINGYI_STAR ? 2 : -1));
    }

    public static void start(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        int n2 = LightHooks.kind(skill);
        if (n2 >= 0) {
            LightHooks.signal(easterBossEntity, n2, 0, vec3, vec32, skill.radius, Math.max(1, skill.windup - n), 0, null);
        }
    }

    public static void pulse(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        int n2 = LightHooks.kind(skill);
        if (n2 >= 0) {
            LightHooks.signal(easterBossEntity, n2, 1, vec3, vec32, skill.radius, 12, n, null);
        }
    }

    public static void confluence(EasterBossEntity easterBossEntity, Vec3 vec3, int n) {
        LightHooks.signal(easterBossEntity, 3, 0, vec3, vec3.m_82546_(easterBossEntity.m_20182_()), 3.5, Math.max(1, QingyiConfluenceRules.useTicks() - n), 0, null);
    }

    public static void burst(LivingEntity livingEntity, Vec3 vec3) {
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            LightHooks.signal(easterBossEntity, 3, 1, vec3, new Vec3(0.0, 0.0, 1.0), 3.5, 16, 0, null);
        }
    }

    public static void dash(EasterBossEntity easterBossEntity, Vec3 vec3, int n) {
        LightHooks.signal(easterBossEntity, 4, 1, easterBossEntity.m_20182_(), vec3, 3.75, n, 0, null);
    }

    public static boolean teleport(EasterBossEntity easterBossEntity, double d, double d2, double d3, boolean bl) {
        Vec3 vec3 = easterBossEntity.m_20182_();
        boolean bl2 = easterBossEntity.m_20984_(d, d2, d3, bl);
        if (bl2) {
            LightHooks.signal(easterBossEntity, 5, 0, vec3, new Vec3(0.0, 0.0, 1.0), 0.65, 10, 0, null);
            LightHooks.signal(easterBossEntity, 5, 1, easterBossEntity.m_20182_(), new Vec3(0.0, 0.0, 1.0), 0.65, 10, 0, null);
        }
        return bl2;
    }

    public static boolean spawnTrident(ServerLevel serverLevel, EasterBossEntity easterBossEntity, QingyiTridentEntity qingyiTridentEntity) {
        boolean bl = QingyiTridentEntity.spawnOwned(serverLevel, easterBossEntity, qingyiTridentEntity);
        if (bl) {
            LightHooks.signal(easterBossEntity, 6, 1, qingyiTridentEntity.m_20182_(), qingyiTridentEntity.m_20184_(), 0.48, 600, 0, (Entity)qingyiTridentEntity);
            LightHooks.signal(easterBossEntity, 9, 1, easterBossEntity.m_20182_(), new Vec3(0.0, 0.0, 1.0), 0.65, 12, 0, null);
        }
        return bl;
    }

    public static boolean spawnPotion(ServerLevel serverLevel, Entity entity) {
        ThrownPotion thrownPotion;
        Entity entity2;
        boolean bl = serverLevel.m_7967_(entity);
        if (bl && entity instanceof ThrownPotion && (entity2 = (thrownPotion = (ThrownPotion)entity).m_19749_()) instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity2;
            LightHooks.signal(easterBossEntity, 8, 0, thrownPotion.m_20182_(), thrownPotion.m_20184_(), 0.28, 160, 0, (Entity)thrownPotion);
        }
        return bl;
    }

    public static boolean orbSpawn(ServerLevel serverLevel, Entity entity, QingyiTridentEntity qingyiTridentEntity) {
        Entity entity2;
        boolean bl = serverLevel.m_7967_(entity);
        if (bl && entity instanceof LightningBolt && (entity2 = qingyiTridentEntity.m_19749_()) instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity2;
            LightHooks.signal(easterBossEntity, 10, 1, entity.m_20182_(), new Vec3(0.0, 1.0, 0.0), 0.45, 10, 0, null);
        }
        return bl;
    }

    public static boolean directBolt(ServerLevel serverLevel, Entity entity, EasterBossEntity easterBossEntity) {
        boolean bl = serverLevel.m_7967_(entity);
        if (bl && entity instanceof LightningBolt) {
            LightHooks.signal(easterBossEntity, 10, 1, entity.m_20182_(), new Vec3(0.0, 1.0, 0.0), 0.45, 10, 0, null);
        }
        return bl;
    }

    public static boolean orbBuff(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, QingyiTridentEntity qingyiTridentEntity) {
        EasterBossEntity easterBossEntity;
        Entity entity;
        boolean bl = livingEntity.m_7292_(mobEffectInstance);
        if (bl && (entity = qingyiTridentEntity.m_19749_()) instanceof EasterBossEntity && livingEntity == (easterBossEntity = (EasterBossEntity)entity)) {
            LightHooks.signal(easterBossEntity, 9, 1, easterBossEntity.m_20182_(), new Vec3(0.0, 0.0, 1.0), 0.8, 18, 0, null);
        }
        return bl;
    }

    public static void melee(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (livingEntity != null) {
            LightHooks.signal(easterBossEntity, 7, 1, livingEntity.m_20182_().m_82520_(0.0, 0.9, 0.0), livingEntity.m_20182_().m_82546_(easterBossEntity.m_20182_()), 0.65, 8, 0, null);
        }
    }

    public static void tridentHit(EasterBossEntity easterBossEntity, LivingEntity livingEntity, int n) {
        if (livingEntity != null) {
            LightHooks.signal(easterBossEntity, 7, 1, livingEntity.m_20182_().m_82520_(0.0, 0.9, 0.0), new Vec3(0.0, 0.0, 1.0), 0.9, 10, Math.max(0, Math.min(3, n)), null);
        }
    }

    public static void combo(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (livingEntity != null) {
            LightHooks.signal(easterBossEntity, 7, 2, livingEntity.m_20182_().m_82520_(0.0, 0.9, 0.0), new Vec3(0.0, 0.0, 1.0), 1.1, 14, 3, null);
        }
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        LightHooks.signal(easterBossEntity, 11, 0, easterBossEntity.m_20182_(), Vec3.f_82478_, 0.0, 1, 0, null);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void projectileImpact(ProjectileImpactEvent projectileImpactEvent) {
        Entity entity;
        if (projectileImpactEvent.isCanceled() || projectileImpactEvent.getRayTraceResult().m_6662_() == HitResult.Type.MISS) {
            return;
        }
        Projectile projectile = projectileImpactEvent.getProjectile();
        if (projectile instanceof ThrownPotion && (entity = projectile.m_19749_()) instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
            LightHooks.signal(easterBossEntity, 8, 1, projectileImpactEvent.getRayTraceResult().m_82450_(), new Vec3(0.0, 1.0, 0.0), 1.0, 14, 0, null);
        }
    }
}

