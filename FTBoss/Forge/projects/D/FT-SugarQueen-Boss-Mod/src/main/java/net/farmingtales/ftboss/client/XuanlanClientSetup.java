package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.PlayerMoonChakramRenderer;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class XuanlanClientSetup {
    private XuanlanClientSetup() {}

    @SubscribeEvent
    public static void models(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(FTBossItems.WENXUANLAN_FLYWHEEL.get(),
                new ResourceLocation(FTBossMod.MOD_ID, "xuanlan_white"),
                (stack, level, entity, seed) -> XuanlanMoonWeaponItem.isWhite(stack) ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FTEntities.PLAYER_MOON_CHAKRAM.get(), PlayerMoonChakramRenderer::new);
    }
}
