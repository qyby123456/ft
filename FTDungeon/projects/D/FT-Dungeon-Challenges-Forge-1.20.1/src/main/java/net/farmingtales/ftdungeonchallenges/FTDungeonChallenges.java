package net.farmingtales.ftdungeonchallenges;

import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTDungeonChallenges.ID)
public final class FTDungeonChallenges {
    public static final String ID = "ftdungeonchallenges";

    public FTDungeonChallenges() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ArenaChallengeConfig::onConfig);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArenaChallengeConfig.SPEC, ArenaChallengeConfig.FILE_NAME);
        MinecraftForge.EVENT_BUS.addListener(ArenaChallengeApi::onTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, ArenaChallengeApi::onDeath);
        MinecraftForge.EVENT_BUS.addListener(ArenaChallengeApi::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(ArenaChallengeApi::onStopping);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onRightClick);
        MinecraftForge.EVENT_BUS.addListener(this::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(this::onCommands);
    }

    public static ResourceLocation resource(String path) { return new ResourceLocation(ID, path); }

    private void onCommands(RegisterCommandsEvent event) { ArenaChallengeCommands.register(event.getDispatcher()); }

    private void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(FTDungeonMobs.ARENA_TORCH_ITEM.get())) {
            event.getToolTip().add(Component.translatable("challenge.ftdungeonmobs.tooltip").withStyle(ChatFormatting.GOLD));
        }
    }

    private void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)
                || !level.getBlockState(event.getPos()).is(FTDungeonMobs.ARENA_TORCH.get())
                || player.isSpectator() || !level.mayInteract(player, event.getPos())) { return; }
        boolean starting = player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty();
        if (!starting && !ArenaChallengeApi.isLocked(level, event.getPos())) { return; }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        try {
            ArenaChallengeApi.Snapshot snapshot = starting ? ArenaChallengeApi.start(player, event.getPos())
                    : ArenaChallengeApi.find(level, event.getPos()).orElseThrow();
            player.displayClientMessage(ArenaChallengeApi.describe(snapshot), true);
        } catch (ArenaChallengeApi.ChallengeException exception) {
            player.displayClientMessage(exception.component().copy().withStyle(ChatFormatting.RED), true);
        }
    }
}
