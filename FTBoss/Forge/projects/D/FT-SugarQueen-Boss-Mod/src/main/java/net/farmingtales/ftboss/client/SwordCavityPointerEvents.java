package net.farmingtales.ftboss.client;

import java.lang.ref.WeakReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 武器构成界面的唯一指针分发；2026-10-20 避免鼠标手势模组绕过服务端权威光标事务。 */
@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class SwordCavityPointerEvents {
    private static WeakReference<SwordCavityContainerScreen> active = new WeakReference<>(null);
    private static int pressed;

    private SwordCavityPointerEvents() {}

    private static boolean inside(SwordCavityContainerScreen screen, double horizontal, double vertical) {
        return horizontal >= screen.getGuiLeft() && horizontal < screen.getGuiLeft() + screen.getXSize()
                && vertical >= screen.getGuiTop() && vertical < screen.getGuiTop() + screen.getYSize();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void press(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof SwordCavityContainerScreen screen)) return;
        if (event.getButton() < 0 || event.getButton() > 1 || !inside(screen, event.getMouseX(), event.getMouseY())) return;
        if (active.get() != screen) pressed = 0;
        active = new WeakReference<>(screen);
        pressed |= 1 << event.getButton();
        screen.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void release(ScreenEvent.MouseButtonReleased.Pre event) {
        if (!(event.getScreen() instanceof SwordCavityContainerScreen screen)) return;
        if (active.get() != screen || event.getButton() < 0 || event.getButton() > 1 || (pressed & 1 << event.getButton()) == 0) return;
        pressed &= ~(1 << event.getButton());
        if (pressed == 0) active.clear();
        screen.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drag(ScreenEvent.MouseDragged.Pre event) {
        if (!(event.getScreen() instanceof SwordCavityContainerScreen screen)) return;
        if (active.get() != screen || event.getMouseButton() < 0 || event.getMouseButton() > 1
                || (pressed & 1 << event.getMouseButton()) == 0) return;
        screen.mouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton(), event.getDragX(), event.getDragY());
        event.setCanceled(true);
    }
}
