package net.farmingtales.ftboss.boss.ai;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 庄园多人输出继承仇恨与终结四倍血量；2026-10-15：仅主战轮换Boss，保留既有伤害、防御和技能流程。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorRaidCombat {
    private static final int OWNER_DEPTH_LIMIT = 8;
    private static final int CLEANUP_INTERVAL_TICKS = 100;
    private static final String HEALTH_PREPARED = "ftboss:manor_raid_health_prepared";

    private ManorRaidCombat() {}

    public static UUID mainFight(LivingEntity entity) {
        if (!(entity instanceof EasterBossEntity boss) || !(entity.level() instanceof ServerLevel)
                || boss.isMini() || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT) return null;
        UUID fight = ManorFight.fightId(boss);
        return fight != null && ManorFight.hasActive(fight) ? fight : null;
    }

    public static double scaleMaximumHealth(double normalScaledHealth, LivingEntity boss) {
        return ManorRaidRules.maximumHealth(normalScaledHealth, mainFight(boss) != null
                && ((EasterBossEntity) boss).isImpossibleChallenge());
    }

    public static void beforeHealthWrite(LivingEntity entity, float requestedHealth, DamageSource source) {
        UUID fight = mainFight(entity);
        if (fight == null || !entity.getUUID().equals(ManorFight.currentBoss(fight))) return;
        double damage = ManorRaidRules.acceptedDamage(entity.getHealth(), requestedHealth);
        if (damage <= 0.0D || source == null) return;
        EasterBossEntity boss = (EasterBossEntity) entity;
        ManorContributionData data = ManorContributionData.get(((ServerLevel) entity.level()).getServer());
        data.observe(fight, boss.getUUID()).recordKind(ManorAdaptiveShield.damageKind(source), damage);
        data.setDirty();
        ServerPlayer player = playerOwner(source.getEntity());
        if (player == null) player = playerOwner(source.getDirectEntity());
        if (!BossTargets.valid(player) || player.level() != entity.level()) return;
        data.observe(fight, boss.getUUID()).record(boss.getUUID(), player.getUUID(), damage);
        data.setDirty();
        ManorFight.participate(boss, player);
    }

    private static ServerPlayer playerOwner(Entity entity) {
        Set<UUID> visited = new HashSet<>();
        for (int depth = 0; entity != null && depth < OWNER_DEPTH_LIMIT && visited.add(entity.getUUID()); depth++) {
            if (entity instanceof ServerPlayer player) return player;
            Entity owner = ManorFightMembers.owner(entity);
            if (owner == null) {
                try {
                    Object summoner = entity.getClass().getMethod("getSummoner").invoke(entity);
                    if (summoner instanceof Entity parent) owner = parent;
                } catch (ReflectiveOperationException ignored) {
                }
            }
            entity = owner;
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity boss && mainFight(boss) != null) BossScaling.tick(boss);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        UUID fight = mainFight(entity);
        if (fight == null || !entity.isAlive() || !entity.getUUID().equals(ManorFight.currentBoss(fight))) return;
        EasterBossEntity boss = (EasterBossEntity) entity;
        ServerLevel level = (ServerLevel) boss.level();
        if (!boss.getPersistentData().getBoolean(HEALTH_PREPARED)) {
            BossScaling.tick(boss);
            boss.getPersistentData().putBoolean(HEALTH_PREPARED, true);
        }
        ManorContributionData data = ManorContributionData.get(level.getServer());
        ManorRaidLedger ledger = data.observe(fight, boss.getUUID());
        if (ledger.bonusConsumed() || boss.isNoAi() || boss.isInvisible() || HiddenBossIntro.isActing(boss.getUUID())) return;
        long players = level.players().stream().filter(BossTargets::valid)
                .filter(player -> player.distanceToSqr(boss) <= BossAggro.MAX_DIST * BossAggro.MAX_DIST).count();
        UUID winner = ledger.consumeBonus(players > 1, playerId -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            return BossTargets.valid(player) && player.level() == level
                    && player.distanceToSqr(boss) <= BossAggro.MAX_DIST * BossAggro.MAX_DIST;
        });
        data.setDirty();
        if (winner == null) return;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(winner);
        BossAggro.record(boss, player, ManorRaidRules.PREVIOUS_LEADER_THREAT, level.getGameTime());
        FTBossMod.LOGGER.info("[FTBoss][庄园输出仇恨] 上一位最高输出={}，有效扣血={}，下一位={}，初始仇恨+{}",
                player.getName().getString(), ledger.previous().get(winner), boss.getName().getString(),
                Math.min(ManorRaidRules.PREVIOUS_LEADER_THREAT, boss.getHealth()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % CLEANUP_INTERVAL_TICKS != 0) return;
        ManorFight.hasActive(event.getServer().overworld());
        ManorContributionData.get(event.getServer()).prune(ManorFight::hasActive);
    }
}
