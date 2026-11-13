package net.farmingtales.ftdungeontrading.client;

import net.farmingtales.ftdungeontrading.FTDungeonTrading;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTDungeonTrading.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TradingClient {
    private TradingClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FTDungeonTrading.TABLE_ENTITY.get(), TradingTableRenderer::new);
    }
}
