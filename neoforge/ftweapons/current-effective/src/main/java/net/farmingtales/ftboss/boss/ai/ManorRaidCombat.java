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
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorRaidCombat {
    private static final int OWNER_DEPTH_LIMIT = 8;
    private static final int CLEANUP_INTERVAL_TICKS = 100;
    private static final String HEALTH_PREPARED = "ftboss:manor_raid_health_prepared";

    private ManorRaidCombat() {
    }

    public static UUID mainFight(LivingEntity entity) {
        if (entity instanceof EasterBossEntity boss && entity.level() instanceof ServerLevel && !boss.isMini() && boss.getVariant() != 9) {
            UUID fight = ManorFight.fightId(boss);
            return fight != null && ManorFight.hasActive(fight) ? fight : null;
        }

        return null;
    }

    public static double scaleMaximumHealth(double normalScaledHealth, LivingEntity boss) {
        return ManorRaidRules.maximumHealth(normalScaledHealth, mainFight(boss) != null && ((EasterBossEntity)boss).isImpossibleChallenge());
    }

    public static void beforeHealthWrite(LivingEntity entity, float requestedHealth, DamageSource source) {
        UUID fight = mainFight(entity);
        if (fight != null && entity.getUUID().equals(ManorFight.currentBoss(fight))) {
            double damage = ManorRaidRules.acceptedDamage(entity.getHealth(), requestedHealth);
            if (!(damage <= 0.0) && source != null) {
                EasterBossEntity boss = (EasterBossEntity)entity;
                ManorContributionData data = ManorContributionData.get(((ServerLevel)entity.level()).getServer());
                data.observe(fight, boss.getUUID()).recordKind(ManorAdaptiveShield.damageKind(source), damage);
                data.setDirty();
                ServerPlayer player = playerOwner(source.getEntity());
                if (player == null) {
                    player = playerOwner(source.getDirectEntity());
                }

                if (BossTargets.valid((Player)player) && player.level() == entity.level()) {
                    data.observe(fight, boss.getUUID()).record(boss.getUUID(), player.getUUID(), damage);
                    data.setDirty();
                    ManorFight.participate(boss, player);
                }
            }
        }
    }

    private static ServerPlayer playerOwner(Entity entity) {
        Set<UUID> visited = new HashSet<>();

        for (int depth = 0; entity != null && depth < 8 && visited.add(entity.getUUID()); depth++) {
            if (entity instanceof ServerPlayer) {
                return (ServerPlayer)entity;
            }

            Entity owner = ManorFightMembers.owner(entity);
            if (owner == null) {
                try {
                    if (entity.getClass().getMethod("getSummoner").invoke(entity) instanceof Entity parent) {
                        owner = parent;
                    }
                } catch (ReflectiveOperationException var6) {
                }
            }

            entity = owner;
        }

        return null;
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity boss && mainFight(boss) != null) {
            BossScaling.tick(boss);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        UUID fight = mainFight(entity);
        if (fight != null && entity.isAlive() && entity.getUUID().equals(ManorFight.currentBoss(fight))) {
            EasterBossEntity boss = (EasterBossEntity)entity;
            ServerLevel level = (ServerLevel)boss.level();
            if (!boss.getPersistentData().getBoolean("ftboss:manor_raid_health_prepared")) {
                BossScaling.tick(boss);
                boss.getPersistentData().putBoolean("ftboss:manor_raid_health_prepared", true);
            }

            ManorContributionData data = ManorContributionData.get(level.getServer());
            ManorRaidLedger ledger = data.observe(fight, boss.getUUID());
            if (!ledger.bonusConsumed() && !boss.isNoAi() && !boss.isInvisible() && !HiddenBossIntro.isActing(boss.getUUID())) {
                long players = level.players().stream().filter(BossTargets::valid).filter(playerx -> playerx.distanceToSqr(boss) <= 4096.0).count();
                UUID winner = ledger.consumeBonus(players > 1L, playerId -> {
                    ServerPlayer playerx = level.getServer().getPlayerList().getPlayer(playerId);
                    return BossTargets.valid((Player)playerx) && playerx.level() == level && playerx.distanceToSqr(boss) <= 4096.0;
                });
                data.setDirty();
                if (winner != null) {
                    ServerPlayer player = level.getServer().getPlayerList().getPlayer(winner);
                    BossAggro.record(boss, player, 100.0, level.getGameTime());
                    FTBossMod.LOGGER
                        .info(
                            "[FTBoss][庄园输出仇恨] 上一位最高输出={}，有效扣血={}，下一位={}，初始仇恨+{}",
                            player.getName().getString(),
                            ledger.previous().get(winner),
                            boss.getName().getString(),
                            Math.min(100.0, (double)boss.getHealth())
                        );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 100 == 0) {
            ManorFight.hasActive(event.getServer().overworld());
            ManorContributionData.get(event.getServer()).prune(ManorFight::hasActive);
        }
    }
}
