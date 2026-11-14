package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** 剑腔自由网格；2026-10-25 显式接收主题并绘制凹槽、悬停和选中态，命中映射与图标保持一致。 */
public final class StatueSwordCavityPanel {
    public static final int PITCH = 32;
    public static final int TEXT = 0xFFE8E1D1;
    public static final int MUTED = 0xFFAAA3B3;
    public static final int CARD = 0xFF302D35;

    public Slot hit(double mouseX, double mouseY, int originX, int originY) {
        for (Slot slot : Slot.values()) {
            if (inside(mouseX, mouseY, originX + SwordNativeMaterialRules.visualColumn(slot.ordinal()) * PITCH,
                    originY + SwordNativeMaterialRules.visualRow(slot.ordinal()) * PITCH, PITCH, PITCH)) return slot;
        }
        return null;
    }

    public List<Component> render(GuiGraphics graphics, Font font, ItemStack weapon, Slot selected, Slot destination,
                                  int originX, int originY, double mouseX, double mouseY) {
        return render(graphics, font, weapon, selected, destination, originX, originY, mouseX, mouseY, SwordNativeMaterials.parts(weapon));
    }

    public List<Component> render(GuiGraphics graphics, Font font, ItemStack weapon, Slot selected, Slot destination,
                                  int originX, int originY, double mouseX, double mouseY, List<Piece> nativeParts) {
        return render(graphics, font, weapon, selected, destination, originX, originY, mouseX, mouseY, nativeParts, StatueUiTheme.forTint(0));
    }

    public List<Component> render(GuiGraphics graphics, Font font, ItemStack weapon, Slot selected, Slot destination,
                                  int originX, int originY, double mouseX, double mouseY, List<Piece> nativeParts, StatueUiTheme theme) {
        List<Cell> cells = SwordCavityBuild.cells(weapon, nativeParts);
        Slot hovered = hit(mouseX, mouseY, originX, originY);
        for (Slot slot : Slot.values()) {
            Cell cell = cells.get(slot.ordinal());
            int left = originX + SwordNativeMaterialRules.visualColumn(slot.ordinal()) * PITCH;
            int top = originY + SwordNativeMaterialRules.visualRow(slot.ordinal()) * PITCH;
            ItemStack display = SwordCavityBuild.displayItem(weapon, slot, nativeParts);
            if (!display.isEmpty()) {
                display = display.copy();
                display.setCount(1);
            }
            item(graphics, font, display, left, top, slot == selected, false, theme);
            if (slot == hovered && slot != selected) {
                StatueUiDecor.socket(graphics, left + 1, top + 1, PITCH - 2, 1);
                graphics.fill(left + 3, top + 3, left + PITCH - 3, top + PITCH - 3, 0x18FFFFFF);
            }
            if (slot == destination) {
                graphics.fill(left + 2, top + 2, left + PITCH - 2, top + 3, 0xFF82E2A9);
                graphics.fill(left + 2, top + PITCH - 3, left + PITCH - 2, top + PITCH - 2, 0xFF82E2A9);
            }
            String badge = cell.state() == State.NATIVE ? "native" : cell.state() == State.INSTALLED ? "installed"
                    : cell.state() == State.MISSING ? "missing" : "";
            if (cell.state() == State.NATIVE && SwordNativeMaterialRules.hasPiece(nativeParts, slot.ordinal())) {
                Origin origin = nativeParts.get(slot.ordinal()).origin();
                if (origin != Origin.RECIPE) badge = origin == Origin.SHARED ? "shared" : "theme";
            }
            if (!badge.isEmpty()) {
                graphics.fill(left + PITCH - 13, top + 2, left + PITCH - 2, top + 12, theme.background);
                graphics.drawCenteredString(font, Component.translatable("screen.ftboss.cavity.badge." + badge),
                        left + PITCH - 8, top + 3, cell.state() == State.MISSING ? 0xFFFF9180 : theme.text);
            }
        }
        return hovered == null ? List.of() : tooltip(weapon, hovered, nativeParts);
    }

    public static List<Component> tooltip(ItemStack weapon, Slot slot) {
        return tooltip(weapon, slot, SwordNativeMaterials.parts(weapon));
    }

    public static List<Component> tooltip(ItemStack weapon, Slot slot, List<Piece> nativeParts) {
        Cell cell = SwordCavityBuild.cells(weapon, nativeParts).get(slot.ordinal());
        List<Component> lines = new ArrayList<>();
        if (cell.present()) {
            ItemStack display = SwordCavityBuild.displayItem(weapon, slot, nativeParts);
            lines.add(display.isEmpty() ? WeaponMaterialBuild.materialName(cell.material()) : display.getHoverName());
            lines.add(Component.translatable(cell.state() == State.NATIVE ? "screen.ftboss.cavity.native" : "screen.ftboss.cavity.installed"));
            if (cell.state() == State.NATIVE && SwordNativeMaterialRules.hasPiece(nativeParts, slot.ordinal())) {
                Piece piece = nativeParts.get(slot.ordinal());
                lines.add(Component.translatable("screen.ftboss.cavity.native." + piece.origin().name().toLowerCase(java.util.Locale.ROOT)));
            }
            if (cell.state() == State.INSTALLED) lines.addAll(SwordCavityBuild.materialEffects(cell.material()));
        } else {
            lines.add(Component.translatable(cell.state() == State.EMPTY ? "screen.ftboss.cavity.empty"
                    : slot.required ? "screen.ftboss.cavity.missing" : "screen.ftboss.cavity.lost"));
        }
        return List.copyOf(lines);
    }

    public static void item(GuiGraphics graphics, Font font, ItemStack stack, int left, int top, boolean selected, boolean dimmed) {
        item(graphics, font, stack, left, top, selected, dimmed, StatueUiTheme.forTint(0));
    }

    public static void item(GuiGraphics graphics, Font font, ItemStack stack, int left, int top, boolean selected, boolean dimmed,
                            StatueUiTheme theme) {
        StatueThemeRenderer.card(graphics, left + 1, top + 1, PITCH - 2, PITCH - 2, theme, selected);
        StatueUiDecor.socket(graphics, left + 1, top + 1, PITCH - 2, dimmed ? 3 : selected ? 2 : 0);
        graphics.fill(left + 3, top + 3, left + PITCH - 3, top + PITCH - 3, theme.background);
        graphics.fill(left + 3, top + 3, left + PITCH - 3, top + 4, theme.border);
        if (selected) graphics.fill(left + 3, top + PITCH - 5, left + PITCH - 3, top + PITCH - 3, theme.accentBright);
        if (!stack.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(left + 4, top + 4, 0);
            graphics.pose().scale(1.5F, 1.5F, 1.0F);
            graphics.renderItem(stack, 0, 0);
            graphics.pose().popPose();
            if (stack.getCount() > 1) graphics.drawString(font, Integer.toString(stack.getCount()),
                    left + PITCH - 4 - font.width(Integer.toString(stack.getCount())), top + PITCH - 11, theme.text, true);
            if (dimmed) graphics.fill(left + 3, top + 3, left + PITCH - 3, top + PITCH - 3, 0x80000000);
        }
    }

    public static boolean inside(double mouseX, double mouseY, int left, int top, int width, int height) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }
}
