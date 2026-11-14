package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.block.BlankStatueBlock;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** 构成只读检查器；2026-10-25 按来源石像分层展示构成、倍率与联动，保留调整入口和独立滚动。 */
public final class StatueBuildInspectorScreen extends Screen implements StatueThemeSource {
    private static final int PANEL_WIDTH = 490;
    private static final int PANEL_HEIGHT = 310;
    private static final int ACTIVE = 0xFF8DDBAC;
    private static final int LINE_HEIGHT = 12;
    private final Screen parent;
    private final StatueUiTheme theme;
    private final BlockPos statuePos;
    private final Supplier<ItemStack> weaponSupplier;
    private final Runnable adjust;
    private final BooleanSupplier available;
    private ItemStack displayedWeapon;
    private StatueBuildInspectorData.View view;
    private List<Line> lines = List.of();
    private final List<Button> tabs = new ArrayList<>();
    private Button adjustButton;
    private Button filterButton;
    private int tab;
    private boolean activeOnly;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int scroll;

    private record Line(FormattedCharSequence text, int color, boolean heading) {}

    private StatueBuildInspectorScreen(Screen parent, BlockPos statuePos, Supplier<ItemStack> weaponSupplier,
                                       Runnable adjust, BooleanSupplier available) {
        this(parent, statuePos, weaponSupplier, adjust, available,
                parent instanceof StatueThemeSource ? StatueUiTheme.fromScreen(parent) : StatueUiTheme.forStatue(statuePos));
    }

    private StatueBuildInspectorScreen(Screen parent, BlockPos statuePos, Supplier<ItemStack> weaponSupplier,
                                       Runnable adjust, BooleanSupplier available, StatueUiTheme theme) {
        super(StatueBuildInspectorData.text("title"));
        this.parent = parent;
        this.theme = theme;
        this.statuePos = statuePos.immutable();
        this.weaponSupplier = weaponSupplier;
        this.adjust = adjust;
        this.available = available;
    }

    @Override
    public StatueUiTheme statueTheme() { return theme; }

    public static Button entry(Screen parent, BlockPos pos, Supplier<ItemStack> weaponSupplier, Runnable adjust,
                               BooleanSupplier available, int x, int y, int width) {
        return StatueThemeButton.create(StatueBuildInspectorData.text("entry"), button -> {
            if (available.getAsBoolean()) Minecraft.getInstance().setScreen(new StatueBuildInspectorScreen(parent, pos, weaponSupplier, adjust, available));
        }, x, y, width, 25, () -> parent instanceof StatueThemeSource ? StatueUiTheme.fromScreen(parent) : StatueUiTheme.forStatue(pos));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(PANEL_WIDTH, width - 16);
        panelHeight = Math.min(PANEL_HEIGHT, height - 16);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        tabs.clear();
        int tabWidth = (panelWidth - 24) / 3;
        for (int index = 0; index < 3; index++) {
            int selected = index;
            tabs.add(addRenderableWidget(StatueThemeButton.create(StatueBuildInspectorData.text("tab." + index), button -> {
                tab = selected;
                scroll = 0;
                rebuildLines();
            }, left + 12 + index * tabWidth, top + 54, tabWidth - 4, 20, this::statueTheme)));
        }
        filterButton = addRenderableWidget(StatueThemeButton.create(filterLabel(), button -> {
            activeOnly = !activeOnly;
            scroll = 0;
            rebuildLines();
        }, left + panelWidth - 112, top + 79, 100, 18, this::statueTheme));
        addRenderableWidget(StatueThemeButton.create(parent == null ? Component.translatable("gui.back")
                : StatueBuildInspectorData.text("back"), button -> onClose(),
                left + 12, top + panelHeight - 28, 96, 20, this::statueTheme));
        if (adjust == null) {
            adjustButton = null;
        } else {
            adjustButton = addRenderableWidget(StatueThemeButton.create(StatueBuildInspectorData.text("adjust"), button -> {
                if (!available.getAsBoolean() || weaponSupplier.get().isEmpty()) return;
                minecraft.setScreen(parent);
                adjust.run();
            }, left + panelWidth - 132, top + panelHeight - 28, 120, 20, this::statueTheme));
        }
        refresh();
        rebuildLines();
    }

    private Component filterLabel() { return StatueBuildInspectorData.text(activeOnly ? "filter.active" : "filter.all"); }

    private void refresh() {
        ItemStack weapon = weaponSupplier.get();
        if (displayedWeapon == null || displayedWeapon.getCount() != weapon.getCount() || !ItemStack.isSameItemSameTags(displayedWeapon, weapon)) {
            displayedWeapon = weapon.copy();
            view = StatueBuildInspectorData.read(weapon);
            scroll = 0;
            rebuildLines();
        }
        if (adjustButton != null) adjustButton.active = !weapon.isEmpty() && available.getAsBoolean();
    }

    private void addLines(Component text, int color) {
        addLines(text, color, false);
    }

    private void addLines(Component text, int color, boolean heading) {
        for (FormattedCharSequence line : font.split(text, panelWidth - 48)) linesBuffer.add(new Line(line, color, heading));
    }

    private final List<Line> linesBuffer = new ArrayList<>();

    private void rebuildLines() {
        if (font == null || view == null) return;
        linesBuffer.clear();
        if (displayedWeapon.isEmpty()) {
            addLines(StatueBuildInspectorData.text("no_weapon"), theme.muted);
        } else {
            addLines(StatueBuildInspectorData.text(tab == 0 ? "bonds_hint" : tab == 1 ? "multipliers_hint" : "links_hint"), theme.muted);
            List<StatueBuildInspectorData.Entry> entries = switch (tab) {
                case 0 -> view.bonds();
                case 1 -> view.multipliers();
                default -> view.links();
            };
            int count = 0;
            for (var entry : entries) {
                if (tab == 2 && activeOnly && !entry.active()) continue;
                addLines(Component.empty(), theme.text);
                addLines(entry.title(), entry.active() ? ACTIVE : theme.accentBright, true);
                for (Component line : entry.lines()) addLines(line, theme.text);
                count++;
            }
            if (count == 0) addLines(StatueBuildInspectorData.text(tab == 0 ? "no_bonds" : "no_links"), theme.muted);
        }
        lines = List.copyOf(linesBuffer);
        scroll = clampScroll(scroll, lines.size() * LINE_HEIGHT, viewportHeight());
        for (int index = 0; index < tabs.size(); index++) {
            tabs.get(index).active = index != tab;
            tabs.get(index).setMessage(StatueBuildInspectorData.text("tab." + index));
        }
        if (filterButton != null) {
            filterButton.visible = tab == 2;
            filterButton.setMessage(filterLabel());
        }
    }

    static int clampScroll(int requested, int contentHeight, int viewportHeight) {
        return Math.max(0, Math.min(requested, Math.max(0, contentHeight - viewportHeight)));
    }

    private int viewportHeight() { return Math.max(1, panelHeight - 140); }

    @Override
    public void tick() {
        Block statueBlock = minecraft.level == null ? null : minecraft.level.getBlockState(statuePos).getBlock();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.isAlive()
                || minecraft.player.distanceToSqr(statuePos.getX() + 0.5, statuePos.getY() + 1.0, statuePos.getZ() + 0.5) > 64
                || !(statueBlock instanceof CandyStatueBlock) && !(statueBlock instanceof BlankStatueBlock)) {
            minecraft.setScreen(null);
            return;
        }
        refresh();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        StatueThemeRenderer.frame(graphics, left, top, panelWidth, panelHeight, theme);
        StatueThemeRenderer.crest(graphics, left + 12, top + 8, 18, theme);
        graphics.drawString(font, title, left + 38, top + 12, theme.accentBright, false);
        StatueThemeRenderer.line(graphics, left + 38, top + 24, panelWidth - 50, theme);
        StatueThemeRenderer.card(graphics, left + 10, top + 28, panelWidth - 20, 22, theme, false);
        if (displayedWeapon != null && !displayedWeapon.isEmpty()) {
            graphics.renderItem(displayedWeapon, left + 12, top + 29);
            graphics.drawString(font, font.plainSubstrByWidth(displayedWeapon.getHoverName().getString(), panelWidth - 48), left + 34, top + 33, theme.text, false);
        }
        graphics.drawString(font, StatueBuildInspectorData.text("readonly"), left + 12, top + 83, theme.muted, false);
        int listTop = top + 103;
        StatueThemeRenderer.card(graphics, left + 11, listTop - 2, panelWidth - 22, viewportHeight() + 4, theme, false);
        graphics.enableScissor(left + 12, listTop, left + panelWidth - 12, listTop + viewportHeight());
        for (int index = 0; index < lines.size(); index++) {
            int lineY = listTop + index * LINE_HEIGHT - scroll;
            if (lineY + LINE_HEIGHT > listTop && lineY < listTop + viewportHeight()) {
                Line line = lines.get(index);
                if (line.heading()) {
                    graphics.fill(left + 15, lineY - 1, left + panelWidth - 20, lineY + LINE_HEIGHT - 1, theme.panel);
                    graphics.fill(left + 15, lineY - 1, left + 17, lineY + LINE_HEIGHT - 1, line.color());
                }
                graphics.drawString(font, line.text(), left + 21, lineY, line.color(), false);
            }
        }
        graphics.disableScissor();
        int maximum = Math.max(0, lines.size() * LINE_HEIGHT - viewportHeight());
        if (maximum > 0) {
            int thumbHeight = Math.max(14, viewportHeight() * viewportHeight() / (lines.size() * LINE_HEIGHT));
            int thumbTop = listTop + scroll * (viewportHeight() - thumbHeight) / maximum;
            graphics.fill(left + panelWidth - 16, thumbTop, left + panelWidth - 13, thumbTop + thumbHeight, theme.accent);
        }
        StatueThemeRenderer.line(graphics, left + 12, top + panelHeight - 33, panelWidth - 24, theme);
        super.render(graphics, mouseX, mouseY, partialTick);
        Button selected = tabs.get(tab);
        graphics.fill(selected.getX() + 3, selected.getY() + selected.getHeight() - 3,
                selected.getX() + selected.getWidth() - 3, selected.getY() + selected.getHeight() - 1, theme.accentBright);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= left + 12 && mouseX < left + panelWidth - 12 && mouseY >= top + 103
                && mouseY < top + 103 + viewportHeight()) {
            scroll = clampScroll(scroll - (int) Math.round(amount * 24), lines.size() * LINE_HEIGHT, viewportHeight());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }

    @Override
    public boolean isPauseScreen() { return false; }

    public static Button previewEntry(BlockPos pos, Supplier<ItemStack> weaponSupplier, BooleanSupplier available,
                                      int x, int y, int width) {
        return StatueThemeButton.create(StatueBuildInspectorData.text("entry"), button -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !available.getAsBoolean()) return;
            StatueUiTheme source = minecraft.screen instanceof StatueThemeSource ? StatueUiTheme.fromScreen(minecraft.screen) : StatueUiTheme.forStatue(pos);
            minecraft.player.closeContainer();
            minecraft.setScreen(new StatueBuildInspectorScreen(null, pos, weaponSupplier, null,
                    () -> minecraft.player != null && minecraft.player.isAlive(), source));
        }, x, y, width, 16, () -> Minecraft.getInstance().screen instanceof StatueThemeSource
                ? StatueUiTheme.fromScreen(Minecraft.getInstance().screen) : StatueUiTheme.forStatue(pos));
    }
}
