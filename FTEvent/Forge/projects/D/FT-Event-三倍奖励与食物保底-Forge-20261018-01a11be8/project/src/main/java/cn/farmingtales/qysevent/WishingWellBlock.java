package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.worldevent.DailyWorldEventManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A small event-themed wish sink; it only costs an emerald and never force-starts an event. */
public final class WishingWellBlock extends Block {
    private static final TagKey<Item> STONE_REWARDS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(QysEventMod.MOD_ID, "wishing_well_stones"));
    private static final TagKey<Item> WOOD_REWARDS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(QysEventMod.MOD_ID, "wishing_well_woods"));

    public WishingWellBlock(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        // Do not show setup/use instructions; only the item reports an actual placement failure.
        if (!held.is(Items.EMERALD)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (serverPlayer.getCooldowns().isOnCooldown(Items.EMERALD)) return InteractionResult.CONSUME;
        if (!player.getAbilities().instabuild) held.shrink(1);
        serverPlayer.getCooldowns().addCooldown(Items.EMERALD, 40);

        float roll = level.random.nextFloat();
        if (roll < .45F) {
            ItemStack reward = switch (level.random.nextInt(7)) {
                case 0 -> taggedBlockReward(serverLevel, STONE_REWARDS, Items.COBBLESTONE, 4, 12);
                case 1 -> taggedBlockReward(serverLevel, WOOD_REWARDS, Items.OAK_PLANKS, 3, 8);
                case 2 -> new ItemStack(Items.GOLD_NUGGET, 2 + level.random.nextInt(5));
                case 3 -> new ItemStack(Items.AMETHYST_SHARD, 1 + level.random.nextInt(3));
                case 4 -> new ItemStack(Items.IRON_NUGGET, 3 + level.random.nextInt(6));
                case 5 -> new ItemStack(Items.EMERALD);
                default -> new ItemStack(Items.DIAMOND);
            };
            Component rewardName = reward.getHoverName().copy();
            int rewardCount = reward.getCount();
            if (!serverPlayer.getInventory().add(reward)) serverPlayer.drop(reward, false);
            serverPlayer.sendSystemMessage(Component.translatable("message.qys_event.well.reward", rewardName, rewardCount));
        } else if (roll < .70F) {
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 0, false, true, true));
            serverPlayer.sendSystemMessage(Component.translatable("message.qys_event.well.luck"));
        } else if (roll < .90F) {
            WeatherManager.Forecast forecast = WeatherManager.forecastTomorrow(serverLevel);
            DailyWorldEventManager.Forecast eventForecast = DailyWorldEventManager.forecast(serverLevel.getServer(), 1).get(0);
            Component weatherState = forecast.specialEvent() == WeatherType.NONE
                    ? Component.translatable("message.qys_event.state.not_triggered")
                    : Component.translatable("message.qys_event.state.triggered", forecast.specialEvent().chineseName());
            Component eventState = eventForecast.triggered()
                    ? Component.translatable("message.qys_event.state.triggered", eventForecast.event().title())
                    : Component.translatable("message.qys_event.state.not_triggered");
            serverPlayer.sendSystemMessage(Component.translatable("message.qys_event.well.forecast",
                    weatherState, eventState));
        } else {
            serverPlayer.sendSystemMessage(Component.translatable("message.qys_event.well.refused"));
        }
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + .5D, pos.getY() + 1.0D, pos.getZ() + .5D,
                10, .35D, .35D, .35D, .02D);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, .8F, 1.15F);
        return InteractionResult.CONSUME;
    }

    private static ItemStack taggedBlockReward(ServerLevel level, TagKey<Item> tag, Item fallback,
                                               int minimum, int maximum) {
        Item selected = level.registryAccess().registryOrThrow(Registries.ITEM).getTag(tag)
                .flatMap(values -> values.getRandomElement(level.random))
                .map(holder -> holder.value())
                .filter(item -> item instanceof BlockItem)
                .orElse(fallback);
        int requested = minimum + level.random.nextInt(maximum - minimum + 1);
        return new ItemStack(selected, Math.min(requested, selected.getDefaultInstance().getMaxStackSize()));
    }
}
