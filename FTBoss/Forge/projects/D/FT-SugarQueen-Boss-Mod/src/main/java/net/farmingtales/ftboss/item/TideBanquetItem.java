package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TideBanquetItem extends Item {
    public TideBanquetItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        if (!SummonGate.allow(level, player, "潮宴鱼王")) return InteractionResultHolder.fail(stack);
        TideFishBossEntity boss = FTEntities.TIDE_FISH_BOSS.get().create(server);
        if (boss == null) return InteractionResultHolder.fail(stack);
        Vec3 direction = player.getLookAngle().multiply(1, 0, 1).normalize();
        if (direction.lengthSqr() < 0.01) direction = Vec3.atLowerCornerOf(player.getDirection().getNormal());
        Vec3 center = player.position().add(direction.scale(5));
        boolean placed = net.farmingtales.ftboss.util.BossCollision.place(server, boss,
                new Vec3(center.x, player.getY(), center.z),
                net.farmingtales.ftboss.util.BossCollision.SpawnMode.FLUID, player.getYRot() + 180)
                && server.addFreshEntity(boss);
        if (!placed) {
            player.displayClientMessage(Component.translatable("message.ftboss.tide_no_room"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.getCooldowns().addCooldown(this, 40);
        server.playSound(null, boss.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.HOSTILE, 1.5f, 0.6f);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.tide_banquet"));
    }
}
