package net.farmingtales.ftboss.item;

import java.util.function.Supplier;
import net.farmingtales.ftboss.boss.SummonGate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;

public final class OverworldBossSpawnEggItem extends ForgeSpawnEggItem {
    public OverworldBossSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int primary, int secondary, Properties properties) {
        super(type, primary, secondary, properties);
    }

    @Override
    protected net.minecraft.core.dispenser.DispenseItemBehavior createDispenseBehavior() {
        var original = super.createDispenseBehavior();
        return (source, stack) -> SummonGate.allow(source.getLevel(), null, "Boss 刷怪蛋")
                ? original.dispense(source, stack) : stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!SummonGate.allow(context.getLevel(), context.getPlayer(), "Boss 刷怪蛋")) return InteractionResult.FAIL;
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!SummonGate.allow(level, player, "Boss 刷怪蛋")) return InteractionResultHolder.fail(player.getItemInHand(hand));
        return super.use(level, player, hand);
    }
}
