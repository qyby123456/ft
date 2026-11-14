package net.farmingtales.ftboss.boss;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.ReefMinionEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * 极限模式死亡护栏：死亡事件真正结算前，先让归属明确的 Boss 战无奖励退场。
 *
 * <p>庄园参战玩家会额外保留 1 点生命并获得短暂无敌；其余 Boss 仍只中止战斗，
 * 不改变玩家死亡。使用 {@link LivingDeathEvent} 而不是“伤害大于当前生命”的预判，
 * 因此原版不死图腾成功保命时不会误停战斗。
 */
public final class HardcoreBossSafety {
    public static final int MANOR_GRACE_TICKS = 60;
    public static final float MANOR_GRACE_HEALTH = 1.0F;
    private static final int MANOR_GRACE_RESISTANCE_AMPLIFIER = 4;
    private static final double ATTRIBUTION_RADIUS = 64.0;
    private static final Map<UUID, Long> MANOR_GRACE_UNTIL = new ConcurrentHashMap<>();

    private HardcoreBossSafety() {}

    public static boolean beforePlayerDeath(LivingDeathEvent event) {
        if (event == null || event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)
                || player.isSpectator() || !player.server.isHardcore()) return false;

        // 极少数绕过 LivingHurtEvent 的伤害（例如虚空/命令）也不能穿透尚未结束的庄园保护。
        if (hasManorGrace(player)) {
            keepAlive(event, player);
            return true;
        }

        DamageSource source = event.getSource();
        Entity attacker = source == null ? null : source.getEntity();
        if (attacker == null && source != null) attacker = source.getDirectEntity();
        if (attacker == null) attacker = ManorMercy.damageOwner();

        UUID fight = ManorFight.fightId(attacker);
        if (ManorFight.abortForHardcore(player, fight)) {
            grantManorGrace(event, player);
            return true;
        }

        LivingEntity boss = resolveBoss(attacker, player);
        if (boss == null && ManorMercy.damageOwner() != attacker) {
            boss = resolveBoss(ManorMercy.damageOwner(), player);
        }
        if (boss != null) {
            UUID bossFight = ManorFight.fightId(boss);
            if (ManorFight.abortForHardcore(player, bossFight)) {
                grantManorGrace(event, player);
                return true;
            }
            String name = boss.getDisplayName().getString();
            BossMercy.despawnBoss(boss, "极限模式玩家死亡前中止");
            report(player, name);
            return true;
        }

        boolean stopped = false;
        if (SugarQueenBoss.isActive() && player.getUUID().equals(SugarQueenBoss.ownerIdOf())) {
            SugarQueenBoss.cleanup("极限模式召唤者死亡前中止");
            stopped = true;
        }
        if (MeatBoss.isActive() && player.getUUID().equals(MeatBoss.ownerIdOf())) {
            MeatBoss.cleanup("极限模式召唤者死亡前中止");
            stopped = true;
        }
        if (stopped) report(player, "召唤者所属 Boss");
        return stopped;
    }

    /** 庄园保护期间拦截后续伤害；死亡事件另有 keepAlive 兜底。 */
    public static boolean cancelManorGraceDamage(LivingHurtEvent event) {
        if (event == null || event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)
                || !player.server.isHardcore() || !hasManorGrace(player)) return false;
        event.setCanceled(true);
        return true;
    }

    /** 每 tick 清掉过期项，避免玩家在保护期内退出后留下静态记录。 */
    public static void tickGrace(MinecraftServer server) {
        if (server == null || MANOR_GRACE_UNTIL.isEmpty()) return;
        long now = server.getTickCount();
        MANOR_GRACE_UNTIL.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    public static void clearGrace() {
        MANOR_GRACE_UNTIL.clear();
    }

    private static boolean hasManorGrace(ServerPlayer player) {
        Long until = MANOR_GRACE_UNTIL.get(player.getUUID());
        if (until == null) return false;
        if (player.server.getTickCount() < until) return true;
        MANOR_GRACE_UNTIL.remove(player.getUUID(), until);
        return false;
    }

    private static void grantManorGrace(LivingDeathEvent event, ServerPlayer player) {
        long until = player.server.getTickCount() + MANOR_GRACE_TICKS;
        MANOR_GRACE_UNTIL.put(player.getUUID(), until);
        player.setHealth(MANOR_GRACE_HEALTH);
        player.invulnerableTime = Math.max(player.invulnerableTime, MANOR_GRACE_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MANOR_GRACE_TICKS,
                MANOR_GRACE_RESISTANCE_AMPLIFIER, false, true, true));
        event.setCanceled(true);
        player.sendSystemMessage(Component.literal("§c【极限模式】§f庄园致命伤害已拦截：保留 1 点生命并获得 3 秒无敌，Boss 战已中止；无掉落、无经验。"));
        FTBossMod.LOGGER.warn("[FTBoss][Hardcore] {} 遭受庄园致命伤害：战斗已中止，死亡已取消并给予 {}t 无敌",
                player.getGameProfile().getName(), MANOR_GRACE_TICKS);
    }

    private static void keepAlive(LivingDeathEvent event, ServerPlayer player) {
        player.setHealth(Math.max(MANOR_GRACE_HEALTH, player.getHealth()));
        event.setCanceled(true);
        FTBossMod.LOGGER.debug("[FTBoss][Hardcore] {} 的庄园无敌拦截了保护期内的死亡事件",
                player.getGameProfile().getName());
    }

    private static LivingEntity resolveBoss(Entity entity, ServerPlayer player) {
        Entity current = entity;
        for (int depth = 0; current != null && depth < 8; depth++) {
            if (current instanceof LivingEntity living && isBoss(living)) return living;
            Entity parent = owner(current);
            if (parent == null || parent == current) break;
            current = parent;
        }
        if (entity == null || !BossAggro.isBossSide(entity)) return null;
        return nearestBoss(player);
    }

    private static Entity owner(Entity entity) {
        Entity owner = ManorFightMembers.owner(entity);
        if (owner != null && owner != entity) return owner;
        if (entity instanceof ReefMinionEntity reef && reef.ownerId() != null
                && reef.level() instanceof ServerLevel level) {
            owner = level.getEntity(reef.ownerId());
            if (owner != null) return owner;
        }
        UUID marked = MinionBlessing.ownerId(entity);
        if (marked != null && entity.level() instanceof ServerLevel level) {
            owner = level.getEntity(marked);
            if (owner != null) return owner;
        }
        try {
            Object summoner = entity.getClass().getMethod("getSummoner").invoke(entity);
            if (summoner instanceof Entity found && found != entity) return found;
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static LivingEntity nearestBoss(ServerPlayer player) {
        LivingEntity nearest = null;
        double best = ATTRIBUTION_RADIUS * ATTRIBUTION_RADIUS;
        for (LivingEntity candidate : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(ATTRIBUTION_RADIUS))) {
            if (!candidate.isAlive() || !isBoss(candidate)) continue;
            double distance = player.distanceToSqr(candidate);
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static boolean isBoss(LivingEntity entity) {
        if (entity instanceof RatBossEntity rat) return !rat.isMinion();
        if (entity instanceof EasterBossEntity easter) return !easter.isMini();
        return entity instanceof TideFishBossEntity
                || entity instanceof SugarQueenEntity
                || entity instanceof MeatMountainEntity
                || entity instanceof TeaTreeBossEntity
                || entity instanceof WineBarrelBossEntity
                || entity instanceof DefectCoreEntity
                || entity instanceof CompostBossEntity
                || entity instanceof AppleBossEntity
                || entity instanceof BengerEntity;
    }

    private static void report(ServerPlayer player, String boss) {
        player.sendSystemMessage(Component.literal("§c【极限模式】§7检测到致命伤害，已在死亡结算前中止 Boss 战；无掉落、无经验。"));
        FTBossMod.LOGGER.warn("[FTBoss][Hardcore] {} 即将死亡，已先中止 Boss 战：{}",
                player.getGameProfile().getName(), boss);
    }
}
