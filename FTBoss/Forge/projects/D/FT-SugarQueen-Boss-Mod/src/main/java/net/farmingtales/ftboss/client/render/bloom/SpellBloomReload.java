package net.farmingtales.ftboss.client.render.bloom;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SpellBloomReload {
    private SpellBloomReload() {}

    @SubscribeEvent
    public static void shaders(RegisterShadersEvent event) {
        SpellBloomRenderer.reset();
    }
}
