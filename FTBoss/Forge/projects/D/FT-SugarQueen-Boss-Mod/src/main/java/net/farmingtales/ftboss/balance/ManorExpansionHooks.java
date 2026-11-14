package net.farmingtales.ftboss.balance;

import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.Messages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.farmingtales.ftboss.util.QizaiSkyNovaRules;
import net.farmingtales.ftboss.util.ManorRefinementRules;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules.Kind;
import net.farmingtales.ftboss.util.WeaponFxRules.Theme;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 庄园追加机制：天罚耗蓝、紫菜大雪、朵拉远程补射与玄蓝炮台。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorExpansionHooks {
    private static final Map<EasterBossEntity, Map<UUID, Long>> DRAINS = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Integer> BULLETS = new WeakHashMap<>();
    private static final Map<EasterBossEntity, Long> TURRETS = new WeakHashMap<>();
    private static boolean snowFailureLogged;

    private ManorExpansionHooks() {}

    public static void nova(EasterBossEntity boss, ServerLevel level, Vec3 origin, int phase) {
        if (!ManorExpansionRules.warningPhase(phase) || !boss.isAlive() || boss.isRemoved()) {
            DRAINS.remove(boss);
            return;
        }
        Map<UUID, Long> inside = DRAINS.computeIfAbsent(boss, ignored -> new HashMap<>());
        long now = level.getGameTime();
        List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
        inside.keySet().removeIf(id -> players.stream().noneMatch(player -> player.getUUID().equals(id)
                && inNova(player, origin)));
        for (ServerPlayer player : players) {
            if (!inNova(player, origin)) continue;
            long previous = inside.computeIfAbsent(player.getUUID(), ignored -> now);
            if (!ManorExpansionRules.drainDue(now, previous)) continue;
            inside.put(player.getUUID(), now);
            MagicData data = MagicData.getPlayerMagicData(player);
            data.setMana(ManorExpansionRules.drainedMana(data.getMana()));
            Messages.sendToPlayer(new SyncManaPacket(data), player);
            level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 0.5, player.getZ(),
                    3, 0.25, 0.2, 0.25, 0.01);
        }
    }

    private static boolean inNova(ServerPlayer player, Vec3 origin) {
        Vec3 offset = player.position().subtract(origin);
        return offset.x * offset.x + offset.z * offset.z <= QizaiSkyNovaRules.RADIUS * QizaiSkyNovaRules.RADIUS
                && Math.abs(offset.y) <= QizaiSkyNovaRules.VERTICAL_TOLERANCE;
    }

    public static void bullet(EasterBossEntity boss, ServerLevel level, LivingEntity original) {
        if (boss.getVariant() != 2 || !ManorBalanceHooks.mainBoss(boss)) return;
        List<ServerPlayer> players = ManorBalanceHooks.players(boss, level);
        if (players.size() < 2) { BULLETS.remove(boss); return; }
        int count = BULLETS.getOrDefault(boss, 0) + 1;
        BULLETS.put(boss, count % 10);
        if (!ManorExpansionRules.tenthBullet(count)) return;
        ServerPlayer far = players.stream().filter(player -> player != original && boss.distanceToSqr(player) >= 144
                && boss.distanceToSqr(player) <= 64 * 64)
                .filter(player -> clear(level, boss.getEyePosition(), player.getBoundingBox().getCenter(), boss))
                .max(java.util.Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
        if (far == null) return;
        Vec3 from = boss.getEyePosition().add(0, -0.15, 0);
        Vec3 end = far.getBoundingBox().getCenter();
        WeaponFx.send(level, Kind.DORA_TRACER, Theme.ARC, from, end, 0.12, boss.getYRot());
        far.invulnerableTime = 0;
        far.hurt(boss.damageSources().mobAttack(boss), EasterBossEntity.BURST_DAMAGE_PHYS * ManorRefinementRules.DORA_DAMAGE_MULTIPLIER);
        far.invulnerableTime = 0;
        far.hurt(boss.damageSources().indirectMagic(boss, boss), EasterBossEntity.BURST_DAMAGE_MAGIC * ManorRefinementRules.DORA_DAMAGE_MULTIPLIER);
        WeaponFx.send(level, Kind.DORA_IMPACT, Theme.GOLD, end, end, 0.34, boss.getYRot());
    }

    public static boolean clear(ServerLevel level, Vec3 from, Vec3 to, net.minecraft.world.entity.Entity source) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source))
                .getType() == HitResult.Type.MISS;
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)
                || !ManorBalanceHooks.mainBoss(boss) || !boss.isAlive()) return;
        if (boss.isNoAi() || boss.isInvisible() || boss.isManorIntroHidden()
                || HiddenBossIntro.isActing(boss.getUUID())) return;
        if (boss.getVariant() == 8 && !boss.getPersistentData().getBoolean("ftboss.zicaiSnowStarted")) {
            try {
                if (DailyWorldEventManager.active(level.getServer()) != DailyWorldEvent.HEAVY_SNOW) {
                    DailyWorldEventManager.setActiveForTesting(level.getServer(), DailyWorldEvent.HEAVY_SNOW);
                }
                boss.getPersistentData().putBoolean("ftboss.zicaiSnowStarted", true);
            } catch (LinkageError failure) {
                if (!snowFailureLogged) FTBossMod.LOGGER.error("[FTBoss] 大雪事件接口不可用，未替换成普通天气", failure);
                snowFailureLogged = true;
            }
        }
    }

    public static boolean dropTurret(EasterBossEntity boss, ServerLevel level) {
        long now = level.getGameTime();
        long previous = TURRETS.computeIfAbsent(boss, ignored -> now - ManorExpansionRules.TURRET_COOLDOWN);
        if (now < previous || now - previous < ManorExpansionRules.TURRET_COOLDOWN) return false;
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = (attempt * Math.PI / 4.0) + boss.getYRot() * Math.PI / 180.0;
            BlockPos floor = null;
            for (int down = 0; down < 48; down++) {
                BlockPos candidate = BlockPos.containing(boss.getX() + Math.cos(angle) * 3.0,
                        boss.getY() - 1 - down, boss.getZ() + Math.sin(angle) * 3.0);
                if (!level.hasChunkAt(candidate)) break;
                if (level.getBlockState(candidate).isSolidRender(level, candidate)) {
                    floor = candidate;
                    break;
                }
            }
            if (floor == null) continue;
            XuanlanTurretEntity turret = new XuanlanTurretEntity(XuanlanTurrets.TYPE, level);
            turret.setOwner(boss);
            turret.moveTo(floor.getX() + 0.5, floor.getY() + 1.0, floor.getZ() + 0.5, boss.getYRot(), 0);
            ManorFightMembers.inherit(boss, turret);
            if (!level.noCollision(turret) || !level.addFreshEntity(turret)) continue;
            TURRETS.put(boss, now);
            return true;
        }
        TURRETS.put(boss, now - ManorExpansionRules.TURRET_COOLDOWN + 20);
        return false;
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        DRAINS.clear();
        BULLETS.clear();
        TURRETS.clear();
        snowFailureLogged = false;
    }
}
