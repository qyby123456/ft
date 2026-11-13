package cn.farmingtales.qysevent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WeatherForecasterBlock extends Block {
    public WeatherForecasterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.EMERALD)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.qys_event.forecaster.need_emerald"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        if (!player.getAbilities().instabuild) held.shrink(1);
        WeatherManager.Forecast forecast = WeatherManager.forecastTomorrow(serverLevel);
        player.sendSystemMessage(Component.translatable("message.qys_event.forecaster.title"));
        player.sendSystemMessage(Component.literal(WeatherManager.forecastLine(forecast)));

        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.15F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                12, 0.35D, 0.20D, 0.35D, 0.02D);
        return InteractionResult.CONSUME;
    }
}
