package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.block.BlankStatueContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NativeCavityScreenRegistration {
    private NativeCavityScreenRegistration() {
    }

    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(BlankStatueContent.SWORD_CAVITY_MENU.get(), SwordCavityContainerScreen::new);
    }
}
