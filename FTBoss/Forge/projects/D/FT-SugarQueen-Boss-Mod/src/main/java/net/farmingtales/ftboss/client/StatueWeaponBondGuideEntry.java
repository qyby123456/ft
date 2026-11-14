package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.util.BossPreviewLayout;
import net.farmingtales.ftboss.util.WeaponBondGuideLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** 羁绊图鉴入口；2026-10-25 显式保留来源页面主题，小屏入口不覆盖原生背包槽。 */
public final class StatueWeaponBondGuideEntry {
    private StatueWeaponBondGuideEntry() {}

    public static Button create(Screen screen) {
        Minecraft client = Minecraft.getInstance();
        var menu = client.player == null ? null : client.player.containerMenu;
        int modelLeft = (screen.width - BossPreviewLayout.modelWidth(screen.width)) / 2;
        int buttonY = WeaponBondGuideLayout.entryY(screen.height);
        boolean compact = WeaponBondGuideLayout.compactEntry(screen.height);
        int buttonX = modelLeft + (compact ? 8 : 148);
        int buttonWidth = 132;
        if (compact) for (var child : screen.children()) {
            if (child instanceof Button existing && existing.getMessage().getContents() instanceof TranslatableContents contents
                    && contents.getKey().equals("screen.ftboss.build_inspector.entry")) {
                existing.setTooltip(Tooltip.create(existing.getMessage()));
                existing.setMessage(Component.translatable("guide.ftboss.bonds.current_short"));
                existing.setWidth(64);
                buttonX = modelLeft + 78;
                buttonWidth = 64;
                break;
            }
        }
        Button result = StatueWeaponBondGuideScreen.entry(() -> client.player != null && menu != null
                && client.player.containerMenu == menu && menu.getCarried().isEmpty(), buttonX, buttonY, buttonWidth,
                () -> StatueUiTheme.fromScreen(screen));
        if (compact && buttonWidth == 64) {
            result.setTooltip(Tooltip.create(result.getMessage()));
            result.setMessage(Component.translatable("guide.ftboss.bonds.entry_short"));
        }
        return result;
    }
}
