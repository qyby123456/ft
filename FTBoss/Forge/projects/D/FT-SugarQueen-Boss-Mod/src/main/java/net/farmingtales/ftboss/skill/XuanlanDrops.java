package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class XuanlanDrops {
    private XuanlanDrops() {}

    @SubscribeEvent
    public static void drop(LivingDropsEvent event) {
        /* ★ v180 按用户要求「庄园 boss 单个击杀不要掉落掉落物, 得全部击杀才获取」:
           玄蓝的飞轮属于"单只庄园 Boss 掉落", 一并关闭;
           通关宝箱 ManorVictoryLoot 里本来就含 ftboss:wenxuanlan_flywheel(绿/白两形)。 */
        if (!net.farmingtales.ftboss.entity.EasterBossEntity.PER_BOSS_DROPS) return;
        if (!(event.getEntity() instanceof EasterBossEntity boss) || boss.level().isClientSide
                || boss.isNoAi() || net.farmingtales.ftboss.boss.ManorFight.isFinaleMember(boss)
                || !boss.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
                || !XuanlanRules.shouldDrop(boss.getVariant(), boss.isMini(), boss.isGiant(), event.isRecentlyHit())) return;
        if (event.getDrops().stream().anyMatch(drop -> drop.getItem().is(FTBossItems.WENXUANLAN_FLYWHEEL.get()))) return;
        ItemEntity reward = new ItemEntity(boss.level(), boss.getX(), boss.getY() + 0.3, boss.getZ(),
                new ItemStack(FTBossItems.WENXUANLAN_FLYWHEEL.get()));
        reward.setDefaultPickUpDelay();
        event.getDrops().add(reward);
    }
}
