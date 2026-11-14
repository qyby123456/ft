package net.farmingtales.ftboss.item;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;

/** 满拉弓不再逐 tick 重算材料规则；2026-10-25 使用原生已累计拉弓进度，不改变放箭、伤害或使用事件。 */
public final class BowChargeProgress {
    private static final int FULL_DRAW_TICKS = 20;

    private BowChargeProgress() {}

    public static boolean full(LivingEntityUseItemEvent.Tick event) {
        ItemStack weapon = event.getItem();
        return weapon.getItem() instanceof BowItem && weapon.getUseDuration() - event.getDuration() >= FULL_DRAW_TICKS;
    }
}
