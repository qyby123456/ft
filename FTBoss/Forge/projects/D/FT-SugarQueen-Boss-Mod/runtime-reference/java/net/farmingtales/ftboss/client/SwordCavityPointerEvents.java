/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ScreenEvent$MouseButtonPressed$Pre
 *  net.minecraftforge.client.event.ScreenEvent$MouseButtonReleased$Pre
 *  net.minecraftforge.client.event.ScreenEvent$MouseDragged$Pre
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client;

import java.lang.ref.WeakReference;
import net.farmingtales.ftboss.client.SwordCavityContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class SwordCavityPointerEvents {
    private static WeakReference<SwordCavityContainerScreen> active = new WeakReference<Object>(null);
    private static int pressed;

    private SwordCavityPointerEvents() {
    }

    private static boolean inside(SwordCavityContainerScreen swordCavityContainerScreen, double d, double d2) {
        return d >= (double)swordCavityContainerScreen.getGuiLeft() && d < (double)(swordCavityContainerScreen.getGuiLeft() + swordCavityContainerScreen.getXSize()) && d2 >= (double)swordCavityContainerScreen.getGuiTop() && d2 < (double)(swordCavityContainerScreen.getGuiTop() + swordCavityContainerScreen.getYSize());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void press(ScreenEvent.MouseButtonPressed.Pre pre) {
        Screen screen = pre.getScreen();
        if (!(screen instanceof SwordCavityContainerScreen)) {
            return;
        }
        SwordCavityContainerScreen swordCavityContainerScreen = (SwordCavityContainerScreen)screen;
        if (pre.getButton() < 0 || pre.getButton() > 1 || !SwordCavityPointerEvents.inside(swordCavityContainerScreen, pre.getMouseX(), pre.getMouseY())) {
            return;
        }
        if (active.get() != swordCavityContainerScreen) {
            pressed = 0;
        }
        active = new WeakReference<SwordCavityContainerScreen>(swordCavityContainerScreen);
        pressed |= 1 << pre.getButton();
        swordCavityContainerScreen.m_6375_(pre.getMouseX(), pre.getMouseY(), pre.getButton());
        pre.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void release(ScreenEvent.MouseButtonReleased.Pre pre) {
        Screen screen = pre.getScreen();
        if (!(screen instanceof SwordCavityContainerScreen)) {
            return;
        }
        SwordCavityContainerScreen swordCavityContainerScreen = (SwordCavityContainerScreen)screen;
        if (active.get() != swordCavityContainerScreen || pre.getButton() < 0 || pre.getButton() > 1 || (pressed & 1 << pre.getButton()) == 0) {
            return;
        }
        if ((pressed &= ~(1 << pre.getButton())) == 0) {
            active.clear();
        }
        swordCavityContainerScreen.m_6348_(pre.getMouseX(), pre.getMouseY(), pre.getButton());
        pre.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void drag(ScreenEvent.MouseDragged.Pre pre) {
        Screen screen = pre.getScreen();
        if (!(screen instanceof SwordCavityContainerScreen)) {
            return;
        }
        SwordCavityContainerScreen swordCavityContainerScreen = (SwordCavityContainerScreen)screen;
        if (active.get() != swordCavityContainerScreen || pre.getMouseButton() < 0 || pre.getMouseButton() > 1 || (pressed & 1 << pre.getMouseButton()) == 0) {
            return;
        }
        swordCavityContainerScreen.m_7979_(pre.getMouseX(), pre.getMouseY(), pre.getMouseButton(), pre.getDragX(), pre.getDragY());
        pre.setCanceled(true);
    }
}

