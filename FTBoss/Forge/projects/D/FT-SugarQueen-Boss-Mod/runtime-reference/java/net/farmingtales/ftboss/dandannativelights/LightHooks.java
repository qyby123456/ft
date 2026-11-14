/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.dandannativelights;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.dandannativelights.LightNetwork;
import net.farmingtales.ftboss.dandannativelights.LightPacket;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LightHooks {
    private static final Map<UUID, Long> STRIP_NOTICES = new HashMap<UUID, Long>();

    private LightHooks() {
    }

    private static boolean dandan(Entity entity) {
        EasterBossEntity easterBossEntity;
        return entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 1 && !easterBossEntity.isMini() && easterBossEntity.m_6084_();
    }

    private static void send(ServerLevel serverLevel, Entity entity, Vec3 vec3, Vec3 vec32, double d, int n, int n2, int n3) {
        float f = (float)Math.atan2(vec32.f_82479_, vec32.f_82481_);
        LightNetwork.send(serverLevel, new LightPacket(serverLevel.m_46472_().m_135782_(), vec3, f, (float)d, n, n2, n3, entity.m_19879_(), entity.m_20148_()));
    }

    public static void start(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        Level level;
        if (!LightHooks.dandan((Entity)easterBossEntity) || !((level = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (skill == ManorFiveCombatRules.Skill.DANDAN_REAP || skill == ManorFiveCombatRules.Skill.DANDAN_STAR) {
            LightHooks.send(serverLevel, (Entity)easterBossEntity, vec3, vec32, skill.radius, skill == ManorFiveCombatRules.Skill.DANDAN_REAP ? 0 : 2, Math.max(1, skill.windup - n), 0);
        }
    }

    public static void pulse(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        Level level;
        if (!LightHooks.dandan((Entity)easterBossEntity) || !((level = easterBossEntity.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (skill == ManorFiveCombatRules.Skill.DANDAN_REAP) {
            LightHooks.send(serverLevel, (Entity)easterBossEntity, vec3, vec32, ManorFiveCombatRules.waveRadius(skill, n), 1, 8, n);
        }
        if (skill == ManorFiveCombatRules.Skill.DANDAN_STAR) {
            LightHooks.send(serverLevel, (Entity)easterBossEntity, vec3, vec32, skill.radius, 3, 13, n);
        }
    }

    public static void move(ServerLevel serverLevel, Entity entity, Vec3 vec3) {
        Vec3 vec32 = entity.m_20182_();
        ManorSafeTeleport.move(serverLevel, entity, vec3);
        Vec3 vec33 = entity.m_20182_();
        if ((LightHooks.dandan(entity) || entity instanceof ServerPlayer) && vec32.m_82557_(vec33) > 0.04) {
            LightHooks.send(serverLevel, entity, vec32, entity.m_20154_(), 0.75, 4, 9, 0);
            LightHooks.send(serverLevel, entity, vec33, entity.m_20154_(), 0.75, 5, 9, 0);
        }
    }

    public static boolean teleport(EasterBossEntity easterBossEntity, double d, double d2, double d3, boolean bl) {
        Level level;
        Vec3 vec3 = easterBossEntity.m_20182_();
        boolean bl2 = easterBossEntity.m_20984_(d, d2, d3, bl);
        if (bl2 && LightHooks.dandan((Entity)easterBossEntity) && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            LightHooks.send(serverLevel, (Entity)easterBossEntity, vec3, easterBossEntity.m_20154_(), 0.75, 4, 9, 0);
            LightHooks.send(serverLevel, (Entity)easterBossEntity, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 0.75, 5, 9, 0);
        }
        return bl2;
    }

    public static void rush(EasterBossEntity easterBossEntity, LivingEntity livingEntity, int n) {
        Level level;
        if (LightHooks.dandan((Entity)easterBossEntity) && livingEntity != null && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            LightHooks.send(serverLevel, (Entity)easterBossEntity, livingEntity.m_20182_(), easterBossEntity.m_20154_(), 1.1, 6, 5, Math.max(0, 5 - n));
        }
    }

    public static void vortex(EasterBossEntity easterBossEntity, double d, double d2, double d3, int n) {
        Level level;
        if (LightHooks.dandan((Entity)easterBossEntity) && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            LightHooks.send(serverLevel, (Entity)easterBossEntity, new Vec3(d, d2, d3), new Vec3(0.0, 0.0, 1.0), 21.0, 7, n, 0);
        }
    }

    public static void seal(ServerPlayer serverPlayer) {
        if (serverPlayer != null) {
            LightHooks.send(serverPlayer.m_284548_(), (Entity)serverPlayer, serverPlayer.m_20182_(), serverPlayer.m_20154_(), 0.75, 8, 13, 0);
        }
    }

    public static boolean removeAmbient(LivingEntity livingEntity, MobEffect mobEffect) {
        boolean bl = livingEntity.m_21195_(mobEffect);
        if (bl && livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            long l = serverPlayer.m_284548_().m_46467_();
            Long l2 = STRIP_NOTICES.get(serverPlayer.m_20148_());
            if (l2 == null || l < l2 || l - l2 >= 14L) {
                if (STRIP_NOTICES.size() >= 128) {
                    STRIP_NOTICES.clear();
                }
                STRIP_NOTICES.put(serverPlayer.m_20148_(), l);
                LightHooks.send(serverPlayer.m_284548_(), (Entity)serverPlayer, serverPlayer.m_20182_(), serverPlayer.m_20154_(), 0.65, 9, 14, 0);
            }
        }
        return bl;
    }

    public static void ramp(EasterBossEntity easterBossEntity) {
        Level level;
        if (LightHooks.dandan((Entity)easterBossEntity) && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            LightHooks.send(serverLevel, (Entity)easterBossEntity, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 0.7, 10, 10, 0);
        }
    }

    public static void melee(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        Level level;
        if (LightHooks.dandan((Entity)easterBossEntity) && livingEntity != null && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            LightHooks.send(serverLevel, (Entity)easterBossEntity, livingEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, 11, 6, 0);
        }
    }

    public static void cancel(EasterBossEntity easterBossEntity, int n) {
        Level level;
        if (LightHooks.dandan((Entity)easterBossEntity) && (level = easterBossEntity.m_9236_()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            LightHooks.send(serverLevel, (Entity)easterBossEntity, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, 12, 1, n);
        }
    }
}

