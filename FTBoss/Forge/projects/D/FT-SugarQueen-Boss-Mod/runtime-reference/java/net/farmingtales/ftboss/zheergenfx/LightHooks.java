/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.zheergenfx;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.balance.ManorShadowBlink;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.zheergenfx.LightNetwork;
import net.farmingtales.ftboss.zheergenfx.LightPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class LightHooks {
    private static final Map<EasterBossEntity, Boolean> VOID_NOTIFIED = new WeakHashMap<EasterBossEntity, Boolean>();
    private static boolean failureReported;

    private LightHooks() {
    }

    private static boolean enabled(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 7 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_() && easterBossEntity.m_9236_() instanceof ServerLevel;
    }

    private static void emit(EasterBossEntity easterBossEntity, int n, Vec3 vec3, Vec3 vec32, double d, int n2, int n3) {
        block3: {
            if (!LightHooks.enabled(easterBossEntity)) {
                return;
            }
            ServerLevel serverLevel = (ServerLevel)easterBossEntity.m_9236_();
            try {
                LightNetwork.send(serverLevel, new LightPacket(n, easterBossEntity.m_19879_(), easterBossEntity.m_20148_(), serverLevel.m_46472_().m_135782_(), serverLevel.m_46467_(), vec3, vec32, d, Math.max(1, Math.min(140, n2)), Math.max(0, Math.min(5, n3))));
            }
            catch (RuntimeException runtimeException) {
                if (failureReported) break block3;
                failureReported = true;
                FTBossMod.LOGGER.warn("Zheergen supplemental light delivery failed; gameplay continues", (Throwable)runtimeException);
            }
        }
    }

    public static void start(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        if (skill == ManorFiveCombatRules.Skill.SHADOW_CROSS || skill == ManorFiveCombatRules.Skill.SHADOW_CAGE) {
            LightHooks.emit(easterBossEntity, skill == ManorFiveCombatRules.Skill.SHADOW_CROSS ? 0 : 2, vec3, vec32, skill.radius, skill.windup - n, 0);
        }
    }

    public static void pulse(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, Vec3 vec32, int n) {
        if (skill == ManorFiveCombatRules.Skill.SHADOW_CROSS || skill == ManorFiveCombatRules.Skill.SHADOW_CAGE) {
            LightHooks.emit(easterBossEntity, skill == ManorFiveCombatRules.Skill.SHADOW_CROSS ? 1 : 3, vec3, vec32, skill.radius, skill == ManorFiveCombatRules.Skill.SHADOW_CROSS ? 20 : 22, n);
        }
    }

    public static boolean teleport(EasterBossEntity easterBossEntity, double d, double d2, double d3, boolean bl) {
        Vec3 vec3 = easterBossEntity.m_20182_();
        boolean bl2 = easterBossEntity.m_20984_(d, d2, d3, bl);
        if (bl2) {
            LightHooks.emit(easterBossEntity, 6, vec3, easterBossEntity.m_20154_(), 1.0, 10, 0);
            LightHooks.emit(easterBossEntity, 7, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, 10, 0);
        }
        return ManorShadowBlink.landed(bl2, easterBossEntity);
    }

    public static void strike(EasterBossEntity easterBossEntity, boolean bl) {
        if (!LightHooks.enabled(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        LightHooks.emit(easterBossEntity, bl ? 5 : 4, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, bl ? 18 : compoundTag.m_128451_("duration"), 0);
    }

    public static void phantom(EasterBossEntity easterBossEntity, double d, double d2, double d3, int n) {
        LightHooks.emit(easterBossEntity, 8, new Vec3(d, d2, d3), easterBossEntity.m_20154_(), 1.0, n + 20, 0);
    }

    public static void echo(EasterBossEntity easterBossEntity, double d, double d2, double d3) {
        LightHooks.emit(easterBossEntity, 9, new Vec3(d, d2, d3), easterBossEntity.m_20154_(), 1.0, 22, 0);
    }

    public static void melee(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        if (livingEntity != null) {
            LightHooks.emit(easterBossEntity, 12, livingEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, 7, 0);
        }
    }

    public static void voidMode(EasterBossEntity easterBossEntity) {
        if (!LightHooks.enabled(easterBossEntity)) {
            return;
        }
        if (!easterBossEntity.shadowVoidMode()) {
            VOID_NOTIFIED.remove(easterBossEntity);
            return;
        }
        if (VOID_NOTIFIED.put(easterBossEntity, Boolean.TRUE) == null) {
            LightHooks.emit(easterBossEntity, 11, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, 18, 0);
        }
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        LightHooks.emit(easterBossEntity, 13, easterBossEntity.m_20182_(), easterBossEntity.m_20154_(), 1.0, 1, 0);
    }
}

