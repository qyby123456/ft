package net.farmingtales.ftboss.qingyilight;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QingyiTridentEntity;
import net.farmingtales.ftboss.util.QingyiConfluenceRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.GAME
)
public final class LightHooks {
    private LightHooks() {
    }

    private static boolean own(EasterBossEntity var0) {
        return var0 != null && var0.getVariant() == 5 && !var0.isMini() && !var0.isNoAi() && var0.isAlive() && var0.level() instanceof ServerLevel;
    }

    private static void signal(EasterBossEntity var0, int var1, int var2, Vec3 var3, Vec3 var4, double var5, int var7, int var8, Entity var9) {
        if (own(var0)) {
            ServerLevel var10 = (ServerLevel)var0.level();
            LightNetwork.send(
                var10,
                new LightPacket(
                    var1,
                    var2,
                    var0.getId(),
                    var0.getUUID(),
                    var10.dimension().location(),
                    var10.getGameTime(),
                    var3,
                    var4.normalize(),
                    var5,
                    var7,
                    var8,
                    var9 == null ? -1 : var9.getId(),
                    var9 == null ? null : var9.getUUID()
                )
            );
        }
    }

    private static int kind(Skill var0) {
        return var0 == Skill.QINGYI_TIDE ? 0 : (var0 == Skill.QINGYI_THUNDER ? 1 : (var0 == Skill.QINGYI_STAR ? 2 : -1));
    }

    public static void start(EasterBossEntity var0, Skill var1, Vec3 var2, Vec3 var3, int var4) {
        int var5 = kind(var1);
        if (var5 >= 0) {
            signal(var0, var5, 0, var2, var3, var1.radius, Math.max(1, var1.windup - var4), 0, null);
        }
    }

    public static void pulse(EasterBossEntity var0, Skill var1, Vec3 var2, Vec3 var3, int var4) {
        int var5 = kind(var1);
        if (var5 >= 0) {
            signal(var0, var5, 1, var2, var3, var1.radius, 12, var4, null);
        }
    }

    public static void confluence(EasterBossEntity var0, Vec3 var1, int var2) {
        signal(var0, 3, 0, var1, var1.subtract(var0.position()), 3.5, Math.max(1, QingyiConfluenceRules.useTicks() - var2), 0, null);
    }

    public static void burst(LivingEntity var0, Vec3 var1) {
        if (var0 instanceof EasterBossEntity var2) {
            signal(var2, 3, 1, var1, new Vec3(0.0, 0.0, 1.0), 3.5, 16, 0, null);
        }
    }

    public static void dash(EasterBossEntity var0, Vec3 var1, int var2) {
        signal(var0, 4, 1, var0.position(), var1, 3.75, var2, 0, null);
    }

    public static boolean teleport(EasterBossEntity var0, double var1, double var3, double var5, boolean var7) {
        Vec3 var8 = var0.position();
        boolean var9 = var0.randomTeleport(var1, var3, var5, var7);
        if (var9) {
            signal(var0, 5, 0, var8, new Vec3(0.0, 0.0, 1.0), 0.65, 10, 0, null);
            signal(var0, 5, 1, var0.position(), new Vec3(0.0, 0.0, 1.0), 0.65, 10, 0, null);
        }

        return var9;
    }

    public static boolean spawnTrident(ServerLevel var0, EasterBossEntity var1, QingyiTridentEntity var2) {
        boolean var3 = QingyiTridentEntity.spawnOwned(var0, var1, var2);
        if (var3) {
            signal(var1, 6, 1, var2.position(), var2.getDeltaMovement(), 0.48, 600, 0, var2);
            signal(var1, 9, 1, var1.position(), new Vec3(0.0, 0.0, 1.0), 0.65, 12, 0, null);
        }

        return var3;
    }

    public static boolean spawnPotion(ServerLevel var0, Entity var1) {
        boolean var2 = var0.addFreshEntity(var1);
        if (var2 && var1 instanceof ThrownPotion var3 && var3.getOwner() instanceof EasterBossEntity var4) {
            signal(var4, 8, 0, var3.position(), var3.getDeltaMovement(), 0.28, 160, 0, var3);
        }

        return var2;
    }

    public static boolean orbSpawn(ServerLevel var0, Entity var1, QingyiTridentEntity var2) {
        boolean var3 = var0.addFreshEntity(var1);
        if (var3 && var1 instanceof LightningBolt && var2.getOwner() instanceof EasterBossEntity var4) {
            signal(var4, 10, 1, var1.position(), new Vec3(0.0, 1.0, 0.0), 0.45, 10, 0, null);
        }

        return var3;
    }

    public static boolean directBolt(ServerLevel var0, Entity var1, EasterBossEntity var2) {
        boolean var3 = var0.addFreshEntity(var1);
        if (var3 && var1 instanceof LightningBolt) {
            signal(var2, 10, 1, var1.position(), new Vec3(0.0, 1.0, 0.0), 0.45, 10, 0, null);
        }

        return var3;
    }

    public static boolean orbBuff(LivingEntity var0, MobEffectInstance var1, QingyiTridentEntity var2) {
        boolean var3 = var0.addEffect(var1);
        if (var3 && var2.getOwner() instanceof EasterBossEntity var4 && var0 == var4) {
            signal(var4, 9, 1, var4.position(), new Vec3(0.0, 0.0, 1.0), 0.8, 18, 0, null);
        }

        return var3;
    }

    public static void melee(EasterBossEntity var0, LivingEntity var1) {
        if (var1 != null) {
            signal(var0, 7, 1, var1.position().add(0.0, 0.9, 0.0), var1.position().subtract(var0.position()), 0.65, 8, 0, null);
        }
    }

    public static void tridentHit(EasterBossEntity var0, LivingEntity var1, int var2) {
        if (var1 != null) {
            signal(var0, 7, 1, var1.position().add(0.0, 0.9, 0.0), new Vec3(0.0, 0.0, 1.0), 0.9, 10, Math.max(0, Math.min(3, var2)), null);
        }
    }

    public static void combo(EasterBossEntity var0, LivingEntity var1) {
        if (var1 != null) {
            signal(var0, 7, 2, var1.position().add(0.0, 0.9, 0.0), new Vec3(0.0, 0.0, 1.0), 1.1, 14, 3, null);
        }
    }

    public static void cancel(EasterBossEntity var0) {
        signal(var0, 11, 0, var0.position(), Vec3.ZERO, 0.0, 1, 0, null);
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void projectileImpact(ProjectileImpactEvent var0) {
        if (!var0.isCanceled() && var0.getRayTraceResult().getType() != Type.MISS) {
            Projectile var1 = var0.getProjectile();
            if (var1 instanceof ThrownPotion && var1.getOwner() instanceof EasterBossEntity var2) {
                signal(var2, 8, 1, var0.getRayTraceResult().getLocation(), new Vec3(0.0, 1.0, 0.0), 1.0, 14, 0, null);
            }
        }
    }
}
