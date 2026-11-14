package net.farmingtales.ftboss.client;

import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild.Stats;
import net.farmingtales.ftboss.item.FrostQueenStaffItem;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBuild.CombatStats;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/** 食武子页共用绘制；2026-10-24 增加随石像主题变化的嵌槽、分页控件与进度轨。 */
final class StatueWeaponUiDraw {
    static final int TEXT = 0xFFE8E1D1;
    static final int MUTED = 0xFF9D9588;
    static final int CARD = 0xC8312E37;
    static final int INNER = 0xE8242229;

    private StatueWeaponUiDraw() {}

    static Component familyName(Kind kind) {
        return Component.translatable("screen.ftboss.family." + kind.name().toLowerCase(Locale.ROOT));
    }

    static List<Component> buildLines(ItemStack weapon) {
        Stats stats = FoodWeaponFamilyBuild.stats(weapon);
        return buildLines(stats, stats.kind() == Kind.MELEE ? WeaponMaterialBuild.combatStats(weapon) : null);
    }

    static List<Component> buildLines(Stats stats, CombatStats melee) {
        Component durability = Component.translatable("screen.ftboss.material.durability_wear",
                stats.durability(), Math.round(stats.wear() * 100));
        return switch (stats.kind()) {
            case MELEE -> List.of(Component.translatable("screen.ftboss.material.combat", decimal(melee.damage(), 1), decimal(melee.speed(), 2)),
                    Component.translatable("screen.ftboss.material.durability_wear", melee.durability(), Math.round(melee.wear() * 100)),
                    Component.translatable("screen.ftboss.material.reach", decimal(melee.reach(), 2)),
                    Component.translatable("screen.ftboss.material.weapon_only"));
            case BOW -> List.of(Component.translatable("screen.ftboss.family.bow.damage_speed",
                    decimal(stats.damageMultiplier(), 2), decimal(stats.speedMultiplier(), 2)),
                    durability, Component.translatable("screen.ftboss.family.bow.velocity", decimal(stats.rangeMultiplier(), 2)),
                    Component.translatable("screen.ftboss.family.bow.scope"));
            case STAFF -> List.of(Component.translatable("screen.ftboss.family.staff.power_speed",
                    decimal(stats.damageMultiplier(), 2), decimal(stats.speedMultiplier(), 2)),
                    durability, Component.translatable("screen.ftboss.family.staff.mana", decimal(stats.manaMultiplier(), 2)),
                    Component.translatable("screen.ftboss.family.staff.scope"));
        };
    }

    static List<Component> familyTooltip(ItemStack weapon, Stats stats) {
        boolean frost = weapon.getItem() instanceof FrostQueenStaffItem;
        return switch (stats.kind()) {
            case MELEE -> List.of();
            case BOW -> List.of(Component.translatable("screen.ftboss.family.bow.hint"));
            case STAFF -> frost ? List.of(Component.translatable("screen.ftboss.family.staff.hint"),
                    Component.translatable("screen.ftboss.family.staff.frost_range", decimal(stats.rangeMultiplier(), 2)))
                    : List.of(Component.translatable("screen.ftboss.family.staff.hint"));
        };
    }

    private static String decimal(double value, int precision) {
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    static int text(GuiGraphics graphics, Font font, Component text, int x, int y, int width,
                     int color, int lineHeight, int maxLines) {
        List<FormattedCharSequence> lines = font.split(text, Math.max(1, width));
        int count = Math.min(maxLines, lines.size());
        for (int index = 0; index < count; index++) {
            graphics.drawString(font, lines.get(index), x, y + index * lineHeight, color, false);
        }
        return count;
    }

    static void centered(GuiGraphics graphics, Font font, Component text, int x, int y, int width,
                         int color, int lineHeight, int maxLines) {
        List<FormattedCharSequence> lines = font.split(text, Math.max(1, width));
        for (int index = 0; index < Math.min(maxLines, lines.size()); index++) {
            FormattedCharSequence line = lines.get(index);
            graphics.drawString(font, line, x + (width - font.width(line)) / 2, y + index * lineHeight, color, false);
        }
    }

    static void heading(GuiGraphics graphics, Font font, int x, int y, String key, int accent) {
        heading(graphics, font, x, y, key, StatueUiTheme.fromAccent(accent));
    }

    static void heading(GuiGraphics graphics, Font font, int x, int y, String key, StatueUiTheme theme) {
        graphics.drawString(font, Component.translatable(key), x + 1, y + 2, theme.accentBright, false);
        StatueThemeRenderer.line(graphics, x, y + 15, 46, theme);
    }

    static void weapon(GuiGraphics graphics, int x, int y, ItemStack stack, int accent) {
        slot(graphics, x, y, stack, accent, false, StatueUiTheme.fromAccent(accent));
    }

    static void slot(GuiGraphics graphics, int x, int y, ItemStack stack, int color, boolean selected, StatueUiTheme theme) {
        int edge = selected ? theme.accentBright : theme.border;
        graphics.fill(x, y, x + 18, y + 18, theme.background);
        graphics.fill(x, y, x + 18, y + 1, edge);
        graphics.fill(x, y + 1, x + 1, y + 18, edge);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, theme.panel);
        graphics.fill(x + 2, y + 16, x + 16, y + 17, 0x70000000 | (color & 0xFFFFFF));
        if (!stack.isEmpty()) graphics.renderItem(stack, x + 1, y + 1);
        if (selected) {
            graphics.fill(x, y + 15, x + 1, y + 18, theme.accentBright);
            graphics.fill(x, y + 17, x + 4, y + 18, theme.accentBright);
            graphics.fill(x + 17, y, x + 18, y + 4, theme.accentBright);
            graphics.fill(x + 14, y, x + 18, y + 1, theme.accentBright);
        }
    }

    static void control(GuiGraphics graphics, int x, int y, int width, int height, StatueUiTheme theme,
                        boolean selected, boolean enabled, boolean hovered) {
        StatueThemeRenderer.card(graphics, x, y, width, height, theme, selected);
        if (!enabled) graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x66000000);
        if (enabled && hovered) graphics.fill(x + 2, y + height - 2, x + width - 2, y + height - 1, theme.accentBright);
    }

    static void progress(GuiGraphics graphics, int x, int y, int width, int height, double ratio,
                         StatueUiTheme theme, int color) {
        graphics.fill(x, y, x + width, y + height, theme.background);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, theme.border);
        int filled = (int) Math.round(Math.max(0, width - 2) * Math.max(0.0D, Math.min(1.0D, ratio)));
        if (filled > 0) {
            graphics.fillGradient(x + 1, y + 1, x + 1 + filled, y + height - 1, color, theme.accent);
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + 2, theme.accentBright);
        }
    }
}
