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

public final class StatueBuildInspectorScreen extends Screen {
    private static final int PANEL_WIDTH = 490;
    private static final int PANEL_HEIGHT = 310;
    private static final int TEXT = -1515055;
    private static final int MUTED = -5594442;
    private static final int ACCENT = -1655951;
    private static final int ACTIVE = -7480404;
    private static final int LINE_HEIGHT = 12;
    private final Screen parent;
    private final BlockPos statuePos;
    private final Supplier<ItemStack> weaponSupplier;
    private final Runnable adjust;
    private final BooleanSupplier available;
    private ItemStack displayedWeapon;
    private StatueBuildInspectorData.View view;
    private List<StatueBuildInspectorScreen.Line> lines = List.of();
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
    private final List<StatueBuildInspectorScreen.Line> linesBuffer = new ArrayList<>();

    private StatueBuildInspectorScreen(Screen var1, BlockPos var2, Supplier<ItemStack> var3, Runnable var4, BooleanSupplier var5) {
        super(StatueBuildInspectorData.text("title"));
        this.parent = var1;
        this.statuePos = var2.immutable();
        this.weaponSupplier = var3;
        this.adjust = var4;
        this.available = var5;
    }

    public static Button entry(Screen var0, BlockPos var1, Supplier<ItemStack> var2, Runnable var3, BooleanSupplier var4, int var5, int var6, int var7) {
        return Button.builder(StatueBuildInspectorData.text("entry"), var5x -> {
            if (var4.getAsBoolean()) {
                Minecraft.getInstance().setScreen(new StatueBuildInspectorScreen(var0, var1, var2, var3, var4));
            }
        }).bounds(var5, var6, var7, 25).build();
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(490, this.width - 16);
        this.panelHeight = Math.min(310, this.height - 16);
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.tabs.clear();
        int var1 = (this.panelWidth - 24) / 3;

        for (int var2 = 0; var2 < 3; var2++) {
            int var3 = var2;
            this.tabs.add(this.addRenderableWidget(Button.builder(StatueBuildInspectorData.text("tab." + var2), var2x -> {
                this.tab = var3;
                this.scroll = 0;
                this.rebuildLines();
            }).bounds(this.left + 12 + var2 * var1, this.top + 54, var1 - 4, 20).build()));
        }

        this.filterButton = this.addRenderableWidget(Button.builder(this.filterLabel(), var1x -> {
            this.activeOnly = !this.activeOnly;
            this.scroll = 0;
            this.rebuildLines();
        }).bounds(this.left + this.panelWidth - 112, this.top + 79, 100, 18).build());
        this.addRenderableWidget(
            Button.builder(
                    (Component)(this.parent == null ? Component.translatable("gui.back") : StatueBuildInspectorData.text("back")), var1x -> this.onClose()
                )
                .bounds(this.left + 12, this.top + this.panelHeight - 28, 96, 20)
                .build()
        );
        if (this.adjust == null) {
            this.adjustButton = null;
        } else {
            this.adjustButton = this.addRenderableWidget(Button.builder(StatueBuildInspectorData.text("adjust"), var1x -> {
                if (this.available.getAsBoolean() && !this.weaponSupplier.get().isEmpty()) {
                    this.minecraft.setScreen(this.parent);
                    this.adjust.run();
                }
            }).bounds(this.left + this.panelWidth - 132, this.top + this.panelHeight - 28, 120, 20).build());
        }

        this.refresh();
        this.rebuildLines();
    }

    private Component filterLabel() {
        return StatueBuildInspectorData.text(this.activeOnly ? "filter.active" : "filter.all");
    }

    private void refresh() {
        ItemStack var1 = this.weaponSupplier.get();
        if (this.displayedWeapon == null || this.displayedWeapon.getCount() != var1.getCount() || !ItemStack.isSameItemSameComponents(this.displayedWeapon, var1)) {
            this.displayedWeapon = var1.copy();
            this.view = StatueBuildInspectorData.read(var1);
            this.scroll = 0;
            this.rebuildLines();
        }

        if (this.adjustButton != null) {
            this.adjustButton.active = !var1.isEmpty() && this.available.getAsBoolean();
        }
    }

    private void addLines(Component var1, int var2) {
        for (FormattedCharSequence var4 : this.font.split(var1, this.panelWidth - 42)) {
            this.linesBuffer.add(new StatueBuildInspectorScreen.Line(var4, var2));
        }
    }

    private void rebuildLines() {
        if (this.font != null && this.view != null) {
            this.linesBuffer.clear();
            if (this.displayedWeapon.isEmpty()) {
                this.addLines(StatueBuildInspectorData.text("no_weapon"), -5594442);
            } else {
                this.addLines(StatueBuildInspectorData.text(this.tab == 0 ? "bonds_hint" : (this.tab == 1 ? "multipliers_hint" : "links_hint")), -5594442);

                List<StatueBuildInspectorData.Entry> var1 = switch (this.tab) {
                    case 0 -> this.view.bonds();
                    case 1 -> this.view.multipliers();
                    default -> this.view.links();
                };
                int var2 = 0;

                for (StatueBuildInspectorData.Entry var4 : var1) {
                    if (this.tab != 2 || !this.activeOnly || var4.active()) {
                        this.addLines(Component.empty(), -1515055);
                        this.addLines(var4.title(), var4.active() ? -7480404 : -1655951);

                        for (Component var6 : var4.lines()) {
                            this.addLines(var6, -1515055);
                        }

                        var2++;
                    }
                }

                if (var2 == 0) {
                    this.addLines(StatueBuildInspectorData.text(this.tab == 0 ? "no_bonds" : "no_links"), -5594442);
                }
            }

            this.lines = List.copyOf(this.linesBuffer);
            this.scroll = clampScroll(this.scroll, this.lines.size() * 12, this.viewportHeight());

            for (int var7 = 0; var7 < this.tabs.size(); var7++) {
                this.tabs.get(var7).active = var7 != this.tab;
            }

            if (this.filterButton != null) {
                this.filterButton.visible = this.tab == 2;
                this.filterButton.setMessage(this.filterLabel());
            }
        }
    }

    static int clampScroll(int var0, int var1, int var2) {
        return Math.max(0, Math.min(var0, Math.max(0, var1 - var2)));
    }

    private int viewportHeight() {
        return Math.max(1, this.panelHeight - 140);
    }

    @Override
    public void tick() {
        Block var1 = this.minecraft.level == null ? null : this.minecraft.level.getBlockState(this.statuePos).getBlock();
        if (this.minecraft.player != null
            && this.minecraft.level != null
            && this.minecraft.player.isAlive()
            && !(
                this.minecraft
                        .player
                        .distanceToSqr((double)this.statuePos.getX() + 0.5, (double)this.statuePos.getY() + 1.0, (double)this.statuePos.getZ() + 0.5)
                    > 64.0
            )
            && (var1 instanceof CandyStatueBlock || var1 instanceof BlankStatueBlock)) {
            this.refresh();
        } else {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        this.renderBackground(var1, var2, var3, var4);
        var1.fill(this.left - 1, this.top - 1, this.left + this.panelWidth + 1, this.top + this.panelHeight + 1, -1655951);
        var1.fill(this.left, this.top, this.left + this.panelWidth, this.top + this.panelHeight, -266856677);
        var1.drawString(this.font, this.title, this.left + 12, this.top + 10, -1655951, false);
        if (this.displayedWeapon != null && !this.displayedWeapon.isEmpty()) {
            var1.renderItem(this.displayedWeapon, this.left + 12, this.top + 29);
            var1.drawString(
                this.font,
                this.font.plainSubstrByWidth(this.displayedWeapon.getHoverName().getString(), this.panelWidth - 48),
                this.left + 34,
                this.top + 33,
                -1515055,
                false
            );
        }

        var1.drawString(this.font, StatueBuildInspectorData.text("readonly"), this.left + 12, this.top + 83, -5594442, false);
        int var5 = this.top + 103;
        var1.fill(this.left + 11, var5 - 2, this.left + this.panelWidth - 11, var5 + this.viewportHeight() + 2, -14409175);
        var1.enableScissor(this.left + 12, var5, this.left + this.panelWidth - 12, var5 + this.viewportHeight());

        for (int var6 = 0; var6 < this.lines.size(); var6++) {
            int var7 = var5 + var6 * 12 - this.scroll;
            if (var7 + 12 > var5 && var7 < var5 + this.viewportHeight()) {
                StatueBuildInspectorScreen.Line var8 = this.lines.get(var6);
                var1.drawString(this.font, var8.text(), this.left + 18, var7, var8.color(), false);
            }
        }

        var1.disableScissor();
        int var9 = Math.max(0, this.lines.size() * 12 - this.viewportHeight());
        if (var9 > 0) {
            int var10 = Math.max(14, this.viewportHeight() * this.viewportHeight() / (this.lines.size() * 12));
            int var11 = var5 + this.scroll * (this.viewportHeight() - var10) / var9;
            var1.fill(this.left + this.panelWidth - 16, var11, this.left + this.panelWidth - 13, var11 + var10, -1655951);
        }

        super.render(var1, var2, var3, var4);
    }

    public boolean mouseScrolled(double var1, double var3, double horizontalScroll, double var5) {
        if (var1 >= (double)(this.left + 12)
            && var1 < (double)(this.left + this.panelWidth - 12)
            && var3 >= (double)(this.top + 103)
            && var3 < (double)(this.top + 103 + this.viewportHeight())) {
            this.scroll = clampScroll(this.scroll - (int)Math.round(var5 * 24.0), this.lines.size() * 12, this.viewportHeight());
            return true;
        } else {
            return super.mouseScrolled(var1, var3, horizontalScroll, var5);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static Button previewEntry(BlockPos var0, Supplier<ItemStack> var1, BooleanSupplier var2, int var3, int var4, int var5) {
        return Button.builder(StatueBuildInspectorData.text("entry"), var3x -> {
            Minecraft var4x = Minecraft.getInstance();
            if (var4x.player != null && var2.getAsBoolean()) {
                var4x.player.closeContainer();
                var4x.setScreen(new StatueBuildInspectorScreen(null, var0, var1, null, () -> var4x.player != null && var4x.player.isAlive()));
            }
        }).bounds(var3, var4, var5, 16).build();
    }

    private static record Line(FormattedCharSequence text, int color) {
    }
}
