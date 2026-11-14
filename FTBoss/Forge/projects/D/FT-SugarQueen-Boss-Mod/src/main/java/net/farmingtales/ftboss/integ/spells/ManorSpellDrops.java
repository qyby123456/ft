package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorSpellDrops {
    private static final String REWARDED = "ftboss:manor_scroll_pair_rewarded";

    private ManorSpellDrops() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
        /* ★ v180 按用户要求「庄园 boss 单个击杀不要掉落掉落物, 得全部击杀才获取」:
           本类**只处理庄园系**(EasterBossEntity) —— 也就是"单只庄园 Boss 掉一对卷轴"这条路,
           正是用户要关掉的。现在整体短路; 卷轴改由通关宝箱发放
           (ManorVictoryLoot.addScrolls 本来就含满阶卷轴)。
           开关与 {@code EasterBossEntity.PER_BOSS_DROPS} 同义, 设回 true 即恢复。 */
        if (!net.farmingtales.ftboss.entity.EasterBossEntity.PER_BOSS_DROPS) return;
        if (!(event.getEntity() instanceof EasterBossEntity boss) || !(boss.level() instanceof ServerLevel level)
                || boss.isMini() || !ManorSpellCombat.eligible(boss) || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
                || boss.getPersistentData().getBoolean(REWARDED)) return;
        var killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer) && !(boss.getKillCredit() instanceof ServerPlayer)
                && !(killer instanceof OwnableEntity pet && pet.getOwner() instanceof ServerPlayer)) return;
        boss.getPersistentData().putBoolean(REWARDED, true);
        for (var kind : ManorSpellRules.pair(boss.getVariant())) {
            var spell = ManorIronSpells.spell(kind);
            if (!spell.isEnabled()) continue;
            ItemEntity drop = new ItemEntity(level, boss.getX(), boss.getY() + 0.5, boss.getZ(), ManorIronSpells.scroll(kind, spell.getMaxLevel()));
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }
}
