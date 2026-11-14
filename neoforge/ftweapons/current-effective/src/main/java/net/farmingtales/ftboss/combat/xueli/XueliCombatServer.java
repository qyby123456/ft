package net.farmingtales.ftboss.combat.xueli;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.boss.ai.XueliMaids;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class XueliCombatServer {
    private static final Map<EasterBossEntity, Long> SHIELDED = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Long> HITS = new WeakHashMap<>();

    private XueliCombatServer() {
    }

    private static ServerLevel level(EasterBossEntity var0) {
        return var0 != null && var0.getVariant() == 10 && var0.level() instanceof ServerLevel var1 ? var1 : null;
    }

    private static void send(EasterBossEntity var0, int var1, Vec3 var2, double var3, int var5, double var6, float var8, List<CombatPacket.Anchor> var9) {
        ServerLevel var10 = level(var0);
        if (var10 != null) {
            CombatPacket var11 = new CombatPacket(
                var10.dimension().location(), var0.getId(), var0.getUUID(), var1, var10.getGameTime(), var2, var3, var5, var6, var8, var9
            );
            if (var11.valid()) {
                for (ServerPlayer player : var10.getServer().getPlayerList().getPlayers()) {
                    if (player.level().dimension() == var10.dimension()
                        && player.distanceToSqr(var2) < 64.0 * 64.0
                        && player.connection.hasChannel(CombatPacket.TYPE)) {
                        PacketDistributor.sendToPlayer(player, var11);
                    }
                }
            }
        }
    }

    private static double shieldRadius(EasterBossEntity var0) {
        return Math.max(1.3, (double)var0.getBbWidth() / Math.sqrt(2.0) + 0.38);
    }

    public static void maids(EasterBossEntity var0, boolean var1) {
        ServerLevel var2 = level(var0);
        if (var2 != null) {
            long var3 = var2.getGameTime();
            if (var1 && var0.isAlive() && !var0.isRemoved()) {
                Long var5 = SHIELDED.get(var0);
                if (var5 == null || var3 - var5 >= 4L) {
                    SHIELDED.put(var0, var3);
                    List var6 = var2.getEntitiesOfClass(Mob.class, var0.getBoundingBox().inflate(48.0))
                        .stream()
                        .filter(
                            var1x -> var1x.isAlive()
                                    && XueliMaids.isGuard(var1x)
                                    && var1x.getPersistentData().hasUUID("ftboss_xueli_guard_owner")
                                    && var0.getUUID().equals(var1x.getPersistentData().getUUID("ftboss_xueli_guard_owner"))
                        )
                        .limit(6L)
                        .map(var0x -> new CombatPacket.Anchor(var0x.getId(), var0x.getUUID(), var0x.position()))
                        .toList();
                    send(var0, 0, var0.position(), shieldRadius(var0), 12, 0.0, -var0.getYRot(), var6);
                }
            } else {
                if (SHIELDED.remove(var0) != null) {
                    send(var0, 1, var0.position(), shieldRadius(var0), 17, 0.0, var0.getYRot(), List.of());
                }
            }
        }
    }

    public static void blocked(EasterBossEntity var0, DamageSource var1, boolean var2) {
        ServerLevel var3 = level(var0);
        if (var3 != null && var2) {
            long var4 = var3.getGameTime();
            Long var6 = HITS.get(var0);
            if (var6 == null || var4 - var6 >= 4L) {
                HITS.put(var0, var4);
                Entity var7 = var1 == null ? null : var1.getEntity();
                float var8 = var7 == null ? var0.getYRot() : (float)Math.toDegrees(Math.atan2(var7.getX() - var0.getX(), var7.getZ() - var0.getZ()));
                send(var0, 2, var0.position(), shieldRadius(var0), 9, 0.0, var8, List.of());
            }
        }
    }

    public static void charge(EasterBossEntity var0, int var1) {
        if (level(var0) != null) {
            CompoundTag var2 = var0.getSkillCue();
            if (!var2.isEmpty() && !var2.getBoolean("released")) {
                Vec3 var3 = new Vec3(var2.getDouble("x"), var2.getDouble("y"), var2.getDouble("z"));
                send(var0, var1, var3, var1 == 7 ? 8.0 : 4.5, var2.getInt("duration"), 0.0, var0.getYRot(), List.of());
            }
        }
    }

    public static void impact(EasterBossEntity var0, int var1) {
        CompoundTag var2 = var0.getSkillCue();
        Vec3 var3 = var1 == 8 ? new Vec3(var2.getDouble("x"), var2.getDouble("y"), var2.getDouble("z")) : var0.position();
        send(var0, var1, var3, var1 == 8 ? 8.0 : 4.5, var1 == 8 ? 15 : 12, 0.0, var0.getYRot(), List.of());
    }

    public static void roll(EasterBossEntity var0, int var1) {
        if (var1 > 0 && level(var0) != null && var0.tickCount % 2 == 0) {
            Vec3 var2 = var0.getDeltaMovement();
            float var3 = var2.x * var2.x + var2.z * var2.z > 1.0E-4 ? (float)Math.toDegrees(Math.atan2(var2.x, var2.z)) : -var0.getYRot();
            send(var0, 5, var0.position(), 3.0, Math.min(var1, 12), 0.0, var3, List.of());
        }
    }

    public static void crush(EasterBossEntity var0, LivingEntity var1) {
        target(var0, var1, 6, 60);
    }

    public static void stun(EasterBossEntity var0, LivingEntity var1, int var2) {
        target(var0, var1, 11, var2);
    }

    private static void target(EasterBossEntity var0, LivingEntity var1, int var2, int var3) {
        if (level(var0) != null && var1 != null && var1.isAlive()) {
            send(
                var0,
                var2,
                var1.position(),
                1.0,
                Math.min(200, Math.max(1, var3)),
                0.0,
                var1.getYRot(),
                List.of(new CombatPacket.Anchor(var1.getId(), var1.getUUID(), var1.position()))
            );
        }
    }

    public static void fuse(EasterBossEntity var0, int var1) {
        if (var1 > 0 && var0.isMini() && var0.tickCount % 2 == 0) {
            send(var0, 9, var0.position(), 0.8, Math.min(var1, 12), Math.max(0.0, Math.min(1.0, 1.0 - (double)var1 / 100.0)), var0.getYRot(), List.of());
        }
    }

    public static void burst(EasterBossEntity var0, boolean var1) {
        if (var0.isMini()) {
            send(var0, 10, var0.position(), var1 ? 3.375 : 4.5, 15, 0.0, var0.getYRot(), List.of());
        }
    }

    public static void stunTell(LivingEntity var0, Component var1, boolean var2, EasterBossEntity var3) {
        if (var3.getVariant() != 10) {
            ManorLivingTargets.tell(var0, var1, var2);
        }
    }
}
