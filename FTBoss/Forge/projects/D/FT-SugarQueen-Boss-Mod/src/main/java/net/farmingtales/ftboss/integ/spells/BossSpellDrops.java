package net.farmingtales.ftboss.integ.spells;

import java.util.function.Consumer;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.farmingtales.ftboss.util.BossSpellRules.Kind;
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

public final class BossSpellDrops {
    private static final String REWARDED = "ftboss:signature_scroll_rewarded";

    private BossSpellDrops() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void queenDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof SugarQueenEntity queen && !queen.isCloneQueen()) {
            reward(queen, event.getSource(), stack -> net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.spawn(queen.level(), drop(queen, stack), queen));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss instanceof SugarQueenEntity) return;
        reward(boss, event.getSource(), stack -> net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter.append(event.getDrops(), drop(boss, stack), boss));
    }

    private static ItemEntity drop(LivingEntity boss, ItemStack stack) {
        ItemEntity item = new ItemEntity(boss.level(), boss.getX(), boss.getY() + 0.5, boss.getZ(), stack);
        item.setDefaultPickUpDelay();
        return item;
    }

    private static void reward(LivingEntity boss, DamageSource source, Consumer<ItemStack> output) {
        if (net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(boss)) return;
        Kind kind = BossSpellRules.forEntity(EntityType.getKey(boss.getType()).toString());
        if (kind == null || !(boss.level() instanceof ServerLevel level)
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
                || boss.getPersistentData().getBoolean(REWARDED)) return;
        if (!(source.getEntity() instanceof ServerPlayer) && !(boss.getKillCredit() instanceof ServerPlayer)) return;
        boss.getPersistentData().putBoolean(REWARDED, true);
        for (Kind reward : BossSpellRules.forBoss("ftboss:" + kind.boss)) {
            if (BossIronSpells.spell(reward).isEnabled()) output.accept(BossIronSpells.scroll(reward, BossIronSpells.spell(reward).getMaxLevel()));
        }
    }
}
