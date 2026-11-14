/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.item.BossRelicItem;
import net.farmingtales.ftboss.item.BossRelicItems;
import net.farmingtales.ftboss.item.WeaponBossMaterialItems;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossRelicDrops {
    private static final String REWARDED = "ftboss:boss_relic_rewarded";

    private BossRelicDrops() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void scriptedDeath(LivingDeathEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity) {
            BossRelicDrops.reward(boss, event.getSource(), stack -> {
                ItemEntity drop = new ItemEntity(boss.m_9236_(), boss.m_20185_(), boss.m_20186_() + 0.5, boss.m_20189_(), stack);
                drop.m_32060_();
                NonManorBossLootRouter.spawn(boss.m_9236_(), (Entity)drop, boss);
            });
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void normalDrops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity) {
            return;
        }
        BossRelicDrops.reward(boss, event.getSource(), stack -> {
            ItemEntity drop = new ItemEntity(boss.m_9236_(), boss.m_20185_(), boss.m_20186_() + 0.5, boss.m_20189_(), stack);
            drop.m_32060_();
            NonManorBossLootRouter.append(event.getDrops(), drop, boss);
        });
    }

    private static void reward(LivingEntity livingEntity, DamageSource damageSource, Consumer<ItemStack> consumer) {
        RatBossEntity ratBossEntity;
        if (LemonSummonRewards.suppressed((Entity)livingEntity)) {
            return;
        }
        String string = EntityType.m_20613_((EntityType)livingEntity.m_6095_()).toString();
        Object object = livingEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        if (!serverLevel.m_46469_().m_46207_(GameRules.f_46135_)) {
            FTBossMod.LOGGER.info("[FTBoss] \u9057\u7269\u8df3\u8fc7: {} \u2014\u2014 mobLoot \u6e38\u620f\u89c4\u5219\u5173\u95ed", (Object)string);
            return;
        }
        if (livingEntity.getPersistentData().m_128471_(REWARDED)) {
            FTBossMod.LOGGER.info("[FTBoss] \u9057\u7269\u8df3\u8fc7: {} \u2014\u2014 \u8fd9\u53ea\u5df2\u7ecf\u7ed3\u7b97\u8fc7(REWARDED)", (Object)string);
            return;
        }
        if (!(damageSource.m_7639_() instanceof ServerPlayer) && !(livingEntity.m_21232_() instanceof ServerPlayer)) {
            FTBossMod.LOGGER.info("[FTBoss] \u9057\u7269\u8df3\u8fc7: {} \u2014\u2014 \u51fb\u6740\u8005\u4e0d\u662f\u73a9\u5bb6\uff08source={}, killCredit={}\uff09", new Object[]{string, damageSource.m_7639_(), livingEntity.m_21232_()});
            return;
        }
        object = BossRelicRules.forEntity(string, livingEntity instanceof RatBossEntity && (ratBossEntity = (RatBossEntity)livingEntity).isMinion());
        if (object == null) {
            return;
        }
        livingEntity.getPersistentData().m_128379_(REWARDED, true);
        WeaponBossMaterialItems.reward((BossRelicRules.Kind)((Object)object), consumer);
        float f = livingEntity.m_217043_().m_188501_();
        if (BossRelicRules.drops(f)) {
            ItemStack itemStack = BossRelicItem.roll(BossRelicItems.get((BossRelicRules.Kind)((Object)object)), livingEntity.m_217043_());
            consumer.accept(itemStack);
            FTBossMod.LOGGER.info("[FTBoss] \u9057\u7269\u6389\u843d: {}\uff08{}\uff0c\u63b7\u70b9 {}=\u547d\u4e2d {}%\uff09", new Object[]{itemStack.m_41786_().getString(), string, String.format("%.3f", Float.valueOf(f)), String.format("%.0f", Float.valueOf(100.0f))});
        } else {
            FTBossMod.LOGGER.info("[FTBoss] \u9057\u7269\u672a\u6389: {}\uff08{}\uff0c\u63b7\u70b9 {}\u2265{}\uff0c\u672c\u6b21\u6ca1\u4e2d\uff09", new Object[]{object.item, string, String.format("%.3f", Float.valueOf(f)), Float.valueOf(1.0f)});
        }
        if (object == BossRelicRules.Kind.FROST) {
            consumer.accept(new ItemStack((ItemLike)BossRelicItems.FROST_QUEEN_STAFF.get()));
        }
    }
}

