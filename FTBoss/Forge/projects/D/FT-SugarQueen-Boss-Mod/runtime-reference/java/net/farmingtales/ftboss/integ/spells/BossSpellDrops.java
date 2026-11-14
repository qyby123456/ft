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
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.integ.spells;

import java.util.function.Consumer;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.boss.ai.NonManorLootCoverage;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossSpellDrops {
    private static final String REWARDED = "ftboss:signature_scroll_rewarded";

    private BossSpellDrops() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void queenDeath(LivingDeathEvent event) {
        SugarQueenEntity queen;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof SugarQueenEntity && !(queen = (SugarQueenEntity)livingEntity).isCloneQueen()) {
            BossSpellDrops.reward((LivingEntity)queen, event.getSource(), stack -> NonManorBossLootRouter.spawn(queen.m_9236_(), (Entity)BossSpellDrops.drop((LivingEntity)queen, stack), (LivingEntity)queen));
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss instanceof SugarQueenEntity) {
            return;
        }
        BossSpellDrops.reward(boss, event.getSource(), stack -> NonManorBossLootRouter.append(event.getDrops(), BossSpellDrops.drop(boss, stack), boss));
    }

    private static ItemEntity drop(LivingEntity boss, ItemStack stack) {
        ItemEntity item = new ItemEntity(boss.m_9236_(), boss.m_20185_(), boss.m_20186_() + 0.5, boss.m_20189_(), stack);
        item.m_32060_();
        return item;
    }

    private static void reward(LivingEntity boss, DamageSource source, Consumer<ItemStack> output) {
        ServerLevel level;
        Object object;
        if (LemonSummonRewards.suppressed((Entity)boss)) {
            return;
        }
        BossSpellRules.Kind kind = BossSpellRules.forEntity(EntityType.m_20613_((EntityType)boss.m_6095_()).toString());
        if (kind == null || !((object = boss.m_9236_()) instanceof ServerLevel) || !(level = (ServerLevel)object).m_46469_().m_46207_(GameRules.f_46135_) || boss.getPersistentData().m_128471_(REWARDED)) {
            return;
        }
        if (!(source.m_7639_() instanceof ServerPlayer) && !(NonManorLootCoverage.creditedPlayer(boss) instanceof ServerPlayer)) {
            return;
        }
        boss.getPersistentData().m_128379_(REWARDED, true);
        for (BossSpellRules.Kind reward : BossSpellRules.forBoss("ftboss:" + kind.boss)) {
            if (!BossIronSpells.spell(reward).isEnabled()) continue;
            output.accept(BossIronSpells.scroll(reward, BossIronSpells.spell(reward).getMaxLevel()));
        }
    }
}

