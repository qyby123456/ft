package cn.farmingtales.qysevent;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WeatherForecasterBlock extends Block {
    public static final MapCodec<WeatherForecasterBlock> CODEC = simpleCodec(WeatherForecasterBlock::new);
    public WeatherForecasterBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (!held.is(Items.EMERALD)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.qys_event.forecaster.need_emerald"), true);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!player.getAbilities().instabuild) held.shrink(1);
        WeatherManager.Forecast forecast = WeatherManager.forecastTomorrow(serverLevel);
        player.sendSystemMessage(Component.translatable("message.qys_event.forecaster.title"));
        player.sendSystemMessage(Component.literal(WeatherManager.forecastLine(forecast)));
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.15F);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + .5D, pos.getY() + 1.05D, pos.getZ() + .5D,
                12, .35D, .20D, .35D, .02D);
        return ItemInteractionResult.CONSUME;
    }
}

