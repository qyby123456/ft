package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** ftboss 攻击命中举盾玩家时破盾，并使当前盾牌冷却 100 tick（5 秒）。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class BossShieldCooldown {
    private static final int SHIELD_COOLDOWN_TICKS = 100;

    private BossShieldCooldown() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F || !player.isBlocking()) return;
        ItemStack shield = player.getUseItem();
        if (!shield.canPerformAction(ToolActions.SHIELD_BLOCK)) return;
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) attacker = event.getSource().getDirectEntity();
        if (attacker == null) attacker = ManorMercy.damageOwner();
        if (!BossKnockbackEvents.isBossSource(attacker)) return;
        player.getCooldowns().addCooldown(shield.getItem(), SHIELD_COOLDOWN_TICKS);
        player.stopUsingItem();
        player.level().broadcastEntityEvent(player, (byte) 30);
    }
}
