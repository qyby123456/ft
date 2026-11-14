package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.BlankStatueContent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** 石像图鉴与剑体容器客户端接线；2026-10-13 注册标准箱子式剑体界面。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BlankStatueClient {
    private BlankStatueClient() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(BlankStatueContent.MENU.get(), BossPreviewScreen::new));
        event.enqueueWork(() -> MenuScreens.register(BlankStatueContent.SWORD_CAVITY_MENU.get(), SwordCavityContainerScreen::new));
    }
}
