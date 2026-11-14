package net.farmingtales.ftboss.balance;

import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.event.tick.EntityTickEvent.Pre;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorExpansionHooks {
    private static final Map<EasterBossEntity, Map<UUID, Long>> DRAINS = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Integer> BULLETS = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Long> TURRETS = new WeakHashMap<>();
    private static boolean snowFailureLogged;

    private ManorExpansionHooks() {
    }

    public static void nova(EasterBossEntity boss, ServerLevel level, Vec3 origin, int phase) {
        if (ManorExpansionRules.warningPhase(phase) && boss.isAlive() && !boss.isRemoved()) {
            Map<UUID, Long> inside = DRAINS.computeIfAbsent(boss, ignored -> new HashMap<>());
            long now = level.getGameTime();
            List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
            inside.keySet().removeIf(id -> players.stream().noneMatch(playerx -> playerx.getUUID().equals(id) && inNova(playerx, origin)));

            for (ServerPlayer player : players) {
                if (inNova(player, origin)) {
                    long previous = inside.computeIfAbsent(player.getUUID(), ignored -> now);
                    if (ManorExpansionRules.drainDue(now, previous)) {
                        inside.put(player.getUUID(), now);
                        MagicData data = MagicData.getPlayerMagicData(player);
                        data.setMana(ManorExpansionRules.drainedMana(data.getMana()));
                        PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
                        level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 0.5, player.getZ(), 3, 0.25, 0.2, 0.25, 0.01);
                    }
                }
            }
        } else {
            DRAINS.remove(boss);
        }
    }

    private static boolean inNova(ServerPlayer player, Vec3 origin) {
        Vec3 offset = player.position().subtract(origin);
        return offset.x * offset.x + offset.z * offset.z <= 2304.0 && Math.abs(offset.y) <= 32.0;
    }

    public static void bullet(EasterBossEntity boss, ServerLevel level, LivingEntity original) {
        if (boss.getVariant() == 2 && ManorBalanceHooks.mainBoss(boss)) {
            List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
            if (players.size() < 2) {
                BULLETS.remove(boss);
            } else {
                int count = BULLETS.getOrDefault(boss, 0) + 1;
                BULLETS.put(boss, count % 10);
                if (ManorExpansionRules.tenthBullet(count)) {
                    ServerPlayer far = players.stream()
                        .filter(player -> player != original && boss.distanceToSqr(player) >= 144.0 && boss.distanceToSqr(player) <= 4096.0)
                        .filter(player -> clear(level, boss.getEyePosition(), player.getBoundingBox().getCenter(), boss))
                        .max(Comparator.comparingDouble(boss::distanceToSqr))
                        .orElse(null);
                    if (far != null) {
                        Vec3 from = boss.getEyePosition().add(0.0, -0.15, 0.0);
                        Vec3 end = far.getBoundingBox().getCenter();
                        WeaponFx.send(level, Kind.DORA_TRACER, Theme.ARC, from, end, 0.12, boss.getYRot());
                        far.invulnerableTime = 0;
                        far.hurt(boss.damageSources().mobAttack(boss), 6.666667F);
                        far.invulnerableTime = 0;
                        far.hurt(boss.damageSources().indirectMagic(boss, boss), 6.666667F);
                        WeaponFx.send(level, Kind.DORA_IMPACT, Theme.GOLD, end, end, 0.34, boss.getYRot());
                    }
                }
            }
        }
    }

    public static boolean clear(ServerLevel level, Vec3 from, Vec3 to, Entity source) {
        return level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, source)).getType() == Type.MISS;
    }

    @SubscribeEvent
    public static void tick(Pre event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss)
            || !(boss.level() instanceof ServerLevel level)
            || !ManorBalanceHooks.mainBoss(boss)
            || !boss.isAlive()) {
            return;
        }

        if (!boss.isNoAi() && !boss.isInvisible() && !boss.isManorIntroHidden() && !HiddenBossIntro.isActing(boss.getUUID())) {
            if (boss.getVariant() == 8 && !boss.getPersistentData().getBoolean("ftboss.zicaiSnowStarted")) {
                try {
                    if (DailyWorldEventManager.active(level.getServer()) != DailyWorldEvent.HEAVY_SNOW) {
                        DailyWorldEventManager.setActiveForTesting(level.getServer(), DailyWorldEvent.HEAVY_SNOW);
                    }

                    boss.getPersistentData().putBoolean("ftboss.zicaiSnowStarted", true);
                } catch (LinkageError var4) {
                    if (!snowFailureLogged) {
                        FTBossMod.LOGGER.error("[FTBoss] 大雪事件接口不可用，未替换成普通天气", (Throwable)var4);
                    }

                    snowFailureLogged = true;
                }
            }
        }
    }

    public static boolean dropTurret(EasterBossEntity boss, ServerLevel level) {
        long now = level.getGameTime();
        long previous = TURRETS.computeIfAbsent(boss, ignored -> now - 100L);
        if (now >= previous && now - previous >= 100L) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = (double)attempt * Math.PI / 4.0 + (double)boss.getYRot() * Math.PI / 180.0;
                BlockPos floor = null;

                for (int down = 0; down < 48; down++) {
                    BlockPos candidate = BlockPos.containing(
                        boss.getX() + Math.cos(angle) * 3.0, boss.getY() - 1.0 - (double)down, boss.getZ() + Math.sin(angle) * 3.0
                    );
                    if (!level.hasChunkAt(candidate)) {
                        break;
                    }

                    if (level.getBlockState(candidate).isSolidRender(level, candidate)) {
                        floor = candidate;
                        break;
                    }
                }

                if (floor != null) {
                    XuanlanTurretEntity turret = new XuanlanTurretEntity(XuanlanTurrets.TYPE, level);
                    turret.setOwner(boss);
                    turret.moveTo((double)floor.getX() + 0.5, (double)floor.getY() + 1.0, (double)floor.getZ() + 0.5, boss.getYRot(), 0.0F);
                    ManorFightMembers.inherit(boss, turret);
                    if (level.noCollision(turret) && level.addFreshEntity(turret)) {
                        TURRETS.put(boss, now);
                        return true;
                    }
                }
            }

            TURRETS.put(boss, now - 100L + 20L);
            return false;
        } else {
            return false;
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        DRAINS.clear();
        BULLETS.clear();
        TURRETS.clear();
        snowFailureLogged = false;
    }
}
