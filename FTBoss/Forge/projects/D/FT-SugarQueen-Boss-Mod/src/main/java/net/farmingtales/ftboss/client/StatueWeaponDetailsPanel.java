package net.farmingtales.ftboss.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity.View;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** 食武详情独立面板；2026-10-24 重绘档案层级与技能选中态，只读服务端存档。 */
final class StatueWeaponDetailsPanel {
    static final int DETAILS_PER_PAGE = 3;
    private static final int CARD_Y = 96;
    private static final int CARD_SPACING = 38;
    private static final int CARD_HEIGHT = 34;

    record Result(int page, List<Component> tooltip) {}

    private StatueWeaponDetailsPanel() {}

    static Component rarity(View view) {
        return Component.translatable("screen.ftboss.rarity.value", view.rarity(), number(view.multiplier()));
    }

    private static String number(double value) { return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString(); }

    static List<Component> rarityTooltip(View view) {
        List<Component> result = new ArrayList<>();
        result.add(rarity(view));
        result.add(Component.translatable("screen.ftboss.rarity.range", number(view.minimum()), number(view.maximum())));
        result.add(Component.translatable(view.legacy() ? "screen.ftboss.rarity.legacy_hint" : "screen.ftboss.rarity.saved_hint"));
        return List.copyOf(result);
    }

    static List<Component> offeringTooltip(ItemStack offering) {
        View view = FoodWeaponSkillRarity.preview(offering);
        return List.of(Component.translatable("screen.ftboss.rarity.preview", number(view.minimum()), number(view.maximum())),
                Component.translatable("screen.ftboss.rarity.preview_hint"));
    }

    static Result render(GuiGraphics graphics, Font font, int x, int y, int width, int accent,
                         int mouseX, int mouseY, ItemStack weapon, List<WeaponDetailView> details, int currentPage) {
        StatueUiTheme theme = StatueUiTheme.fromAccent(accent);
        StatueWeaponUiDraw.heading(graphics, font, x, y, "screen.ftboss.statue_hub.details.title", theme);
        if (weapon.isEmpty()) {
            StatueThemeRenderer.card(graphics, x, y + 24, width, 72, theme, false);
            StatueWeaponUiDraw.text(graphics, font, Component.translatable("screen.ftboss.statue_hub.details.no_weapon"),
                    x + 10, y + 36, width - 20, theme.muted, 12, 4);
            return new Result(0, List.of());
        }
        List<Component> hover = List.of();
        var frame = StatueWeaponDetailsCache.frame(Minecraft.getInstance().screen, weapon);
        StatueThemeRenderer.card(graphics, x, y + 21, width, 49, theme, false);
        StatueWeaponUiDraw.weapon(graphics, x + 8, y + 29, weapon, accent);
        StatueWeaponUiDraw.text(graphics, font, weapon.getHoverName(), x + 34, y + 27, width - 44, theme.text, 11, 1);
        StatueWeaponUiDraw.text(graphics, font, frame.summary(), x + 34, y + 41, width - 44, theme.muted, 11, 1);
        StatueWeaponUiDraw.text(graphics, font, frame.familySummary(), x + 34, y + 55, width - 44, theme.muted, 11, 1);
        if (StatueWeaponUiDraw.inside(mouseX, mouseY, x, y + 21, width, 49)) {
            hover = frame.headerTooltip();
        }
        int pages = Math.max(1, (details.size() + DETAILS_PER_PAGE - 1) / DETAILS_PER_PAGE);
        int page = Math.max(0, Math.min(currentPage, pages - 1));
        int leftArrowX = x + width - 96;
        int rightArrowX = x + width - 18;
        int pageY = y + 77;
        StatueWeaponUiDraw.control(graphics, leftArrowX, pageY, 18, 13, theme, false,
                page > 0, StatueWeaponUiDraw.inside(mouseX, mouseY, leftArrowX, pageY, 18, 13));
        StatueWeaponUiDraw.control(graphics, rightArrowX, pageY, 18, 13, theme, false,
                page + 1 < pages, StatueWeaponUiDraw.inside(mouseX, mouseY, rightArrowX, pageY, 18, 13));
        graphics.drawCenteredString(font, "<", leftArrowX + 9, pageY + 2, page > 0 ? theme.text : theme.muted);
        graphics.drawCenteredString(font, ">", rightArrowX + 9, pageY + 2, page + 1 < pages ? theme.text : theme.muted);
        graphics.drawString(font, Component.translatable("screen.ftboss.statue_hub.details.page", details.size(), page + 1, pages),
                x + 1, pageY + 2, theme.muted, false);
        if (details.isEmpty()) {
            StatueWeaponUiDraw.text(graphics, font, Component.translatable("screen.ftboss.statue_hub.details.empty"),
                    x + 8, y + 109, width - 16, theme.muted, 12, 4);
        }
        for (int row = 0; row < DETAILS_PER_PAGE; row++) {
            int index = page * DETAILS_PER_PAGE + row;
            if (index >= details.size()) break;
            WeaponDetailView detail = details.get(index);
            var card = frame.cards().get(index);
            int cardY = y + CARD_Y + row * CARD_SPACING;
            int color = detail.selected() ? theme.accentBright : theme.accent;
            StatueThemeRenderer.card(graphics, x, cardY, width, CARD_HEIGHT, theme, detail.selected());
            graphics.fill(x + 1, cardY + 3, x + 3, cardY + CARD_HEIGHT - 3, color);
            graphics.fill(x + 66, cardY + 5, x + 67, cardY + CARD_HEIGHT - 5, theme.border);
            StatueWeaponUiDraw.centered(graphics, font, detail.category(), x + 7, cardY + 7, 58, color, 10, 2);
            StatueWeaponUiDraw.text(graphics, font, card.title(), x + 72, cardY + 3, width - 80, theme.text, 9, 1);
            if (card.power() != null) StatueWeaponUiDraw.text(graphics, font, card.power(), x + 72, cardY + 12, width - 80, theme.accentBright, 9, 1);
            StatueWeaponUiDraw.text(graphics, font, detail.description(), x + 72, cardY + (card.power() == null ? 13 : 21),
                    width - 80, theme.muted, 9, card.power() == null ? 2 : 1);
            if (StatueWeaponUiDraw.inside(mouseX, mouseY, x, cardY, width, CARD_HEIGHT)) {
                hover = card.tooltip();
            }
        }
        return new Result(page, hover);
    }
}
