/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.block.BlankStatueContent;
import net.farmingtales.ftboss.client.BossPreviewScreen;
import net.farmingtales.ftboss.client.SwordCavityContainerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class BlankStatueClient {
    private BlankStatueClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent fMLClientSetupEvent) {
        fMLClientSetupEvent.enqueueWork(() -> MenuScreens.m_96206_((MenuType)((MenuType)BlankStatueContent.MENU.get()), BossPreviewScreen::new));
        fMLClientSetupEvent.enqueueWork(() -> MenuScreens.m_96206_((MenuType)((MenuType)BlankStatueContent.SWORD_CAVITY_MENU.get()), SwordCavityContainerScreen::new));
    }
}

