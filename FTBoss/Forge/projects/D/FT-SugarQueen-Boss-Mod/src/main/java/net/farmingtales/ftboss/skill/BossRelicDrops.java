package net.farmingtales.ftboss.skill;

import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.item.BossRelicItem;
import net.farmingtales.ftboss.item.BossRelicItems;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossRelicDrops {
    private static final String REWARDED = "ftboss:boss_relic_rewarded";

    private BossRelicDrops() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void scriptedDeath(LivingDeathEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity) {
            reward(boss, event.getSource(), stack -> {
                ItemEntity drop = new ItemEntity(boss.level(), boss.getX(), boss.getY() + 0.5, boss.getZ(), stack);
                drop.setDefaultPickUpDelay();
                net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(boss.level(), drop, boss);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void normalDrops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity) return;
        reward(boss, event.getSource(), stack -> {
            ItemEntity drop = new ItemEntity(boss.level(), boss.getX(), boss.getY() + 0.5, boss.getZ(), stack);
            drop.setDefaultPickUpDelay();
            net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.append(event.getDrops(), drop, boss);
        });
    }

    private static void reward(LivingEntity boss, DamageSource source, Consumer<ItemStack> drop) {
        if (net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(boss)) return;
        /* 2026-09-19 用户反馈「机器人的吊坠未掉落」：这条链路以前**一句日志都没有**，
           掉了/没中/被守卫拦下全都看不见，只能靠猜。现在每一次死亡都留痕。 */
        String who = EntityType.getKey(boss.getType()).toString();
        if (!(boss.level() instanceof ServerLevel level)) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            FTBossMod.LOGGER.info("[FTBoss] 遗物跳过: {} —— mobLoot 游戏规则关闭", who);
            return;
        }
        if (boss.getPersistentData().getBoolean(REWARDED)) {
            FTBossMod.LOGGER.info("[FTBoss] 遗物跳过: {} —— 这只已经结算过(REWARDED)", who);
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer) && !(boss.getKillCredit() instanceof ServerPlayer)) {
            FTBossMod.LOGGER.info("[FTBoss] 遗物跳过: {} —— 击杀者不是玩家（source={}, killCredit={}）",
                    who, source.getEntity(), boss.getKillCredit());
            return;
        }
        Kind kind = BossRelicRules.forEntity(who, boss instanceof RatBossEntity rat && rat.isMinion());
        if (kind == null) return;
        boss.getPersistentData().putBoolean(REWARDED, true);
        net.farmingtales.ftboss.item.WeaponBossMaterialItems.reward(kind, drop);
        float roll = boss.getRandom().nextFloat();
        if (BossRelicRules.drops(roll)) {
            ItemStack relic = BossRelicItem.roll(BossRelicItems.get(kind), boss.getRandom());
            drop.accept(relic);
            FTBossMod.LOGGER.info("[FTBoss] 遗物掉落: {}（{}，掷点 {}=命中 {}%）",
                    relic.getHoverName().getString(), who, String.format("%.3f", roll),
                    String.format("%.0f", BossRelicRules.DROP_CHANCE * 100));
        } else {
            FTBossMod.LOGGER.info("[FTBoss] 遗物未掉: {}（{}，掷点 {}≥{}，本次没中）",
                    kind.item, who, String.format("%.3f", roll), BossRelicRules.DROP_CHANCE);
        }
        if (kind == Kind.FROST) drop.accept(new ItemStack(BossRelicItems.FROST_QUEEN_STAFF.get()));
    }
}
