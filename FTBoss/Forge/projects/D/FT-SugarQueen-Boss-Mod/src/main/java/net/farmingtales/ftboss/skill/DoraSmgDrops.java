package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.util.DoraSmgRules;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class DoraSmgDrops {
    private DoraSmgDrops() {}

    @SubscribeEvent
    public static void drop(LivingDropsEvent event) {
        /* ★ v180 按用户要求「庄园 boss 单个击杀不要掉落掉落物, 得全部击杀才获取」:
           朵拉的冲锋枪属于"单只庄园 Boss 掉落", 一并关闭;
           通关宝箱 ManorVictoryLoot 里本来就含 ftboss:dora_nova_launcher。 */
        if (!net.farmingtales.ftboss.entity.EasterBossEntity.PER_BOSS_DROPS) return;
        if (!(event.getEntity() instanceof EasterBossEntity boss) || boss.level().isClientSide
                || !boss.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
                || !DoraSmgRules.shouldDrop(boss.getVariant(), boss.isMini(), boss.isGiant(), boss.isNoAi(),
                ManorFight.isFinaleMember(boss), event.isRecentlyHit())) return;
        event.getDrops().removeIf(drop -> "bren:machine_gun".equals(String.valueOf(ForgeRegistries.ITEMS.getKey(drop.getItem().getItem()))));
        if (event.getDrops().stream().anyMatch(drop -> drop.getItem().is(FTBossItems.DORA_NOVA_LAUNCHER.get()))) return;
        ItemEntity reward = new ItemEntity(boss.level(), boss.getX(), boss.getY() + 0.3, boss.getZ(),
                new ItemStack(FTBossItems.DORA_NOVA_LAUNCHER.get()));
        reward.setDefaultPickUpDelay();
        event.getDrops().add(reward);
    }
}
