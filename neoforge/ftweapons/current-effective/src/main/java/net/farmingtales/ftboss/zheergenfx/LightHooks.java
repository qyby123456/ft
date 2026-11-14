package net.farmingtales.ftboss.zheergenfx;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.balance.ManorShadowBlink;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class LightHooks {
    private static final Map<EasterBossEntity, Boolean> VOID_NOTIFIED = new WeakHashMap<>();
    private static boolean failureReported;

    private LightHooks() {
    }

    private static boolean enabled(EasterBossEntity var0) {
        return var0 != null && var0.getVariant() == 7 && !var0.isMini() && !var0.isGiant() && var0.isAlive() && var0.level() instanceof ServerLevel;
    }

    private static void emit(EasterBossEntity var0, int var1, Vec3 var2, Vec3 var3, double var4, int var6, int var7) {
        if (enabled(var0)) {
            ServerLevel var8 = (ServerLevel)var0.level();

            try {
                LightNetwork.send(
                    var8,
                    new LightPacket(
                        var1,
                        var0.getId(),
                        var0.getUUID(),
                        var8.dimension().location(),
                        var8.getGameTime(),
                        var2,
                        var3,
                        var4,
                        Math.max(1, Math.min(140, var6)),
                        Math.max(0, Math.min(5, var7))
                    )
                );
            } catch (RuntimeException var10) {
                if (!failureReported) {
                    failureReported = true;
                    FTBossMod.LOGGER.warn("Zheergen supplemental light delivery failed; gameplay continues", (Throwable)var10);
                }
            }
        }
    }

    public static void start(EasterBossEntity var0, Skill var1, Vec3 var2, Vec3 var3, int var4) {
        if (var1 == Skill.SHADOW_CROSS || var1 == Skill.SHADOW_CAGE) {
            emit(var0, var1 == Skill.SHADOW_CROSS ? 0 : 2, var2, var3, var1.radius, var1.windup - var4, 0);
        }
    }

    public static void pulse(EasterBossEntity var0, Skill var1, Vec3 var2, Vec3 var3, int var4) {
        if (var1 == Skill.SHADOW_CROSS || var1 == Skill.SHADOW_CAGE) {
            emit(var0, var1 == Skill.SHADOW_CROSS ? 1 : 3, var2, var3, var1.radius, var1 == Skill.SHADOW_CROSS ? 20 : 22, var4);
        }
    }

    public static boolean teleport(EasterBossEntity var0, double var1, double var3, double var5, boolean var7) {
        Vec3 var8 = var0.position();
        boolean var9 = var0.randomTeleport(var1, var3, var5, var7);
        if (var9) {
            emit(var0, 6, var8, var0.getLookAngle(), 1.0, 10, 0);
            emit(var0, 7, var0.position(), var0.getLookAngle(), 1.0, 10, 0);
        }

        return ManorShadowBlink.landed(var9, var0);
    }

    public static void strike(EasterBossEntity var0, boolean var1) {
        if (enabled(var0)) {
            CompoundTag var2 = var0.getSkillCue();
            emit(var0, var1 ? 5 : 4, var0.position(), var0.getLookAngle(), 1.0, var1 ? 18 : var2.getInt("duration"), 0);
        }
    }

    public static void phantom(EasterBossEntity var0, double var1, double var3, double var5, int var7) {
        emit(var0, 8, new Vec3(var1, var3, var5), var0.getLookAngle(), 1.0, var7 + 20, 0);
    }

    public static void echo(EasterBossEntity var0, double var1, double var3, double var5) {
        emit(var0, 9, new Vec3(var1, var3, var5), var0.getLookAngle(), 1.0, 22, 0);
    }

    public static void melee(EasterBossEntity var0, LivingEntity var1) {
        if (var1 != null) {
            emit(var0, 12, var1.position(), var0.getLookAngle(), 1.0, 7, 0);
        }
    }

    public static void voidMode(EasterBossEntity var0) {
        if (enabled(var0)) {
            if (!var0.shadowVoidMode()) {
                VOID_NOTIFIED.remove(var0);
            } else {
                if (VOID_NOTIFIED.put(var0, Boolean.TRUE) == null) {
                    emit(var0, 11, var0.position(), var0.getLookAngle(), 1.0, 18, 0);
                }
            }
        }
    }

    public static void cancel(EasterBossEntity var0) {
        emit(var0, 13, var0.position(), var0.getLookAngle(), 1.0, 1, 0);
    }
}
