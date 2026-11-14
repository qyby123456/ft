package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/** 剑体右侧羁绊查看栏；2026-10-25 使用父页主题和独立效果卡，固定宽度、裁切滚动，不放大玩家物品栏。 */
public final class SwordCavityBondPanel {
    public static final int GAP = 6;
    public static final int WIDTH = 118;
    private static final int CONTENT_Y = 30;
    private static final int PADDING = 8;
    private static final int ENTRY_GAP = 12;
    private int scrollOffset;
    private int scrollMaximum;
    private CavityPresentationCache.Frame displayedFrame;
    private Font displayedFont;
    private Language displayedLanguage;
    private List<Layout> displayedEntries = List.of();
    private int contentHeight;

    private record Entry(Component name, Component description) {}
    private record Layout(String name, List<FormattedCharSequence> lines, int offset, int height) {}

    private static List<Entry> entries(ItemStack weapon, CavityPresentationCache.Frame presentation) {
        List<Entry> entries = new ArrayList<>();
        if (FourWeaponFoundationAdapter.applies(weapon)) {
            var view = presentation.foundation();
            boolean ready = view.valid() && view.status().ready() && !FourWeaponUseGuard.blocksUse(weapon);
            List<ItemStack> paid = FourWeaponCavityPresentation.unlockedPaid(view);
            if (ready) for (SwordCavityRules.Link link : net.farmingtales.ftboss.item.FoundationAccessoryBonds.links(weapon)) {
                String identifier = link.name().toLowerCase(java.util.Locale.ROOT);
                entries.add(new Entry(Component.translatable("link.ftboss.sword." + identifier),
                        Component.translatable("link.ftboss.sword.detail." + identifier)));
            }
            for (SwordCavityExtensions.BondDefinition bond : SwordCavityExtensions.activeBonds(weapon, paid, ready)) entries.add(new Entry(bond.name(), bond.description()));
            for (SwordMaterialExtensions.BondDefinition bond : SwordMaterialExtensions.activeBonds(weapon, paid, ready)) entries.add(new Entry(bond.name(), bond.description()));
            return List.copyOf(entries);
        }
        for (SwordCavityRules.Link link : SwordCavityBuild.links(weapon)) {
            String identifier = link.name().toLowerCase(java.util.Locale.ROOT);
            entries.add(new Entry(Component.translatable("link.ftboss.sword." + identifier),
                    Component.translatable("link.ftboss.sword.detail." + identifier)));
        }
        for (SwordCavityExtensions.BondDefinition bond : SwordCavityExtensions.activeBonds(weapon)) entries.add(new Entry(bond.name(), bond.description()));
        for (SwordMaterialExtensions.BondDefinition bond : SwordMaterialExtensions.activeBonds(weapon)) entries.add(new Entry(bond.name(), bond.description()));
        return entries;
    }

    public void render(GuiGraphics graphics, Font font, ItemStack weapon, int posX, int posY, int height) {
        render(graphics, font, weapon, posX, posY, height, StatueUiTheme.forTint(0));
    }

    public void render(GuiGraphics graphics, Font font, ItemStack weapon, int posX, int posY, int height, StatueUiTheme theme) {
        render(graphics, font, weapon, posX, posY, height, theme, CavityPresentationCache.frame(this, weapon, null));
    }

    public void render(GuiGraphics graphics, Font font, ItemStack weapon, int posX, int posY, int height, StatueUiTheme theme,
                       CavityPresentationCache.Frame presentation) {
        StatueThemeRenderer.frame(graphics, posX, posY, WIDTH, height, theme);
        StatueUiDecor.star(graphics, posX + 6, posY + 7, 14);
        Component title = Component.translatable("screen.ftboss.cavity.bond_panel");
        graphics.drawString(font, font.plainSubstrByWidth(title.getString(), WIDTH - 52), posX + 25, posY + 10, theme.accentBright, false);
        if (displayedFrame != presentation || displayedFont != font || displayedLanguage != Language.getInstance()) {
            List<Layout> layout = new ArrayList<>();
            contentHeight = 0;
            for (Entry entry : entries(weapon, presentation)) {
                List<FormattedCharSequence> lines = List.copyOf(font.split(entry.description(), WIDTH - PADDING * 2 - 3));
                int entryHeight = font.lineHeight + 4 + lines.size() * font.lineHeight;
                layout.add(new Layout(font.plainSubstrByWidth(entry.name().getString(), WIDTH - PADDING * 2 - 3), lines, contentHeight, entryHeight));
                contentHeight += entryHeight + ENTRY_GAP;
            }
            displayedEntries = List.copyOf(layout);
            displayedFrame = presentation;
            displayedFont = font;
            displayedLanguage = Language.getInstance();
        }
        List<Layout> entries = displayedEntries;
        graphics.drawString(font, Integer.toString(entries.size()), posX + WIDTH - PADDING - 8, posY + 10, theme.muted, false);
        StatueThemeRenderer.line(graphics, posX + PADDING, posY + 24, WIDTH - PADDING * 2, theme);
        int viewport = height - CONTENT_Y - PADDING;
        scrollMaximum = Math.max(0, contentHeight - viewport);
        scrollOffset = Math.max(0, Math.min(scrollOffset, scrollMaximum));
        graphics.enableScissor(posX + 4, posY + CONTENT_Y, posX + WIDTH - 5, posY + height - PADDING);
        int cursor = posY + CONTENT_Y - scrollOffset;
        if (entries.isEmpty()) graphics.drawString(font, Component.translatable("screen.ftboss.cavity.bond_none"), posX + PADDING, cursor, theme.muted, false);
        for (Layout entry : entries) {
            int entryY = cursor + entry.offset();
            if (entryY + entry.height() + 4 <= posY + CONTENT_Y || entryY - 2 >= posY + height - PADDING) continue;
            StatueThemeRenderer.card(graphics, posX + 4, entryY - 2, WIDTH - 10, entry.height() + 6, theme, false);
            graphics.fill(posX + 5, entryY, posX + 6, entryY + font.lineHeight, theme.accent);
            graphics.drawString(font, entry.name(), posX + PADDING, entryY, theme.accentBright, false);
            int lineY = entryY + font.lineHeight + 4;
            for (FormattedCharSequence line : entry.lines()) {
                graphics.drawString(font, line, posX + PADDING, lineY, theme.text, false);
                lineY += font.lineHeight;
            }
        }
        graphics.disableScissor();
        if (scrollMaximum > 0) {
            int thumbHeight = Math.max(12, viewport * viewport / contentHeight);
            int thumbY = posY + CONTENT_Y + (viewport - thumbHeight) * scrollOffset / scrollMaximum;
            graphics.fill(posX + WIDTH - 4, posY + CONTENT_Y, posX + WIDTH - 2, posY + height - PADDING, theme.background);
            graphics.fill(posX + WIDTH - 4, thumbY, posX + WIDTH - 2, thumbY + thumbHeight, theme.accent);
        }
    }

    public boolean scroll(double mouseX, double mouseY, double amount, int posX, int posY, int height) {
        if (mouseX < posX || mouseX >= posX + WIDTH || mouseY < posY || mouseY >= posY + height) return false;
        scrollOffset = Math.max(0, Math.min(scrollMaximum, scrollOffset - (int) Math.round(amount * 18)));
        return true;
    }
}
