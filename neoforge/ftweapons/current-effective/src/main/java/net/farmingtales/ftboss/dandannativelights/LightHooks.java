package net.farmingtales.ftboss.dandannativelights;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class LightHooks {
    private static final Map<UUID, Long> STRIP_NOTICES = new HashMap<>();

    private LightHooks() {
    }

    private static boolean dandan(Entity var0) {
        if (var0 instanceof EasterBossEntity var1 && var1.getVariant() == 1 && !var1.isMini() && var1.isAlive()) {
            return true;
        }

        return false;
    }

    private static void send(ServerLevel var0, Entity var1, Vec3 var2, Vec3 var3, double var4, int var6, int var7, int var8) {
        float var9 = (float)Math.atan2(var3.x, var3.z);
        LightNetwork.send(var0, new LightPacket(var0.dimension().location(), var2, var9, (float)var4, var6, var7, var8, var1.getId(), var1.getUUID()));
    }

    public static void start(EasterBossEntity var0, Skill var1, Vec3 var2, Vec3 var3, int var4) {
        if (dandan(var0) && var0.level() instanceof ServerLevel var5) {
            if (var1 == Skill.DANDAN_REAP || var1 == Skill.DANDAN_STAR) {
                send(var5, var0, var2, var3, var1.radius, var1 == Skill.DANDAN_REAP ? 0 : 2, Math.max(1, var1.windup - var4), 0);
            }
        }
    }

    public static void pulse(EasterBossEntity var0, Skill var1, Vec3 var2, Vec3 var3, int var4) {
        if (dandan(var0) && var0.level() instanceof ServerLevel var5) {
            if (var1 == Skill.DANDAN_REAP) {
                send(var5, var0, var2, var3, ManorFiveCombatRules.waveRadius(var1, var4), 1, 8, var4);
            }

            if (var1 == Skill.DANDAN_STAR) {
                send(var5, var0, var2, var3, var1.radius, 3, 13, var4);
            }
        }
    }

    public static void move(ServerLevel var0, Entity var1, Vec3 var2) {
        Vec3 var3 = var1.position();
        ManorSafeTeleport.move(var0, var1, var2);
        Vec3 var4 = var1.position();
        if ((dandan(var1) || var1 instanceof ServerPlayer) && var3.distanceToSqr(var4) > 0.04) {
            send(var0, var1, var3, var1.getLookAngle(), 0.75, 4, 9, 0);
            send(var0, var1, var4, var1.getLookAngle(), 0.75, 5, 9, 0);
        }
    }

    public static boolean teleport(EasterBossEntity var0, double var1, double var3, double var5, boolean var7) {
        Vec3 var8 = var0.position();
        boolean var9 = var0.randomTeleport(var1, var3, var5, var7);
        if (var9 && dandan(var0) && var0.level() instanceof ServerLevel var10) {
            send(var10, var0, var8, var0.getLookAngle(), 0.75, 4, 9, 0);
            send(var10, var0, var0.position(), var0.getLookAngle(), 0.75, 5, 9, 0);
        }

        return var9;
    }

    public static void rush(EasterBossEntity var0, LivingEntity var1, int var2) {
        if (dandan(var0) && var1 != null && var0.level() instanceof ServerLevel var3) {
            send(var3, var0, var1.position(), var0.getLookAngle(), 1.1, 6, 5, Math.max(0, 5 - var2));
        }
    }

    public static void vortex(EasterBossEntity var0, double var1, double var3, double var5, int var7) {
        if (dandan(var0) && var0.level() instanceof ServerLevel var8) {
            send(var8, var0, new Vec3(var1, var3, var5), new Vec3(0.0, 0.0, 1.0), 21.0, 7, var7, 0);
        }
    }

    public static void seal(ServerPlayer var0) {
        if (var0 != null) {
            send(var0.serverLevel(), var0, var0.position(), var0.getLookAngle(), 0.75, 8, 13, 0);
        }
    }

    public static boolean removeAmbient(LivingEntity var0, Holder<MobEffect> var1) {
        boolean var2 = var0.removeEffect(var1);
        if (var2 && var0 instanceof ServerPlayer var3) {
            long var4 = var3.serverLevel().getGameTime();
            Long var6 = STRIP_NOTICES.get(var3.getUUID());
            if (var6 == null || var4 < var6 || var4 - var6 >= 14L) {
                if (STRIP_NOTICES.size() >= 128) {
                    STRIP_NOTICES.clear();
                }

                STRIP_NOTICES.put(var3.getUUID(), var4);
                send(var3.serverLevel(), var3, var3.position(), var3.getLookAngle(), 0.65, 9, 14, 0);
            }
        }

        return var2;
    }

    public static void ramp(EasterBossEntity var0) {
        if (dandan(var0) && var0.level() instanceof ServerLevel var1) {
            send(var1, var0, var0.position(), var0.getLookAngle(), 0.7, 10, 10, 0);
        }
    }

    public static void melee(EasterBossEntity var0, LivingEntity var1) {
        if (dandan(var0) && var1 != null && var0.level() instanceof ServerLevel var2) {
            send(var2, var0, var1.position(), var0.getLookAngle(), 1.0, 11, 6, 0);
        }
    }

    public static void cancel(EasterBossEntity var0, int var1) {
        if (dandan(var0) && var0.level() instanceof ServerLevel var2) {
            send(var2, var0, var0.position(), var0.getLookAngle(), 1.0, 12, 1, var1);
        }
    }
}
