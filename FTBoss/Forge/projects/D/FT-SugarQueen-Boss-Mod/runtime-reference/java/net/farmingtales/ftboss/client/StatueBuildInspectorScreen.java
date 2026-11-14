/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.Block
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.block.BlankStatueBlock;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.client.StatueBuildInspectorData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class StatueBuildInspectorScreen
extends Screen {
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
    private List<Line> lines = List.of();
    private final List<Button> tabs = new ArrayList<Button>();
    private Button adjustButton;
    private Button filterButton;
    private int tab;
    private boolean activeOnly;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int scroll;
    private final List<Line> linesBuffer = new ArrayList<Line>();

    private StatueBuildInspectorScreen(Screen screen, BlockPos blockPos, Supplier<ItemStack> supplier, Runnable runnable, BooleanSupplier booleanSupplier) {
        super(StatueBuildInspectorData.text("title", new Object[0]));
        this.parent = screen;
        this.statuePos = blockPos.m_7949_();
        this.weaponSupplier = supplier;
        this.adjust = runnable;
        this.available = booleanSupplier;
    }

    public static Button entry(Screen screen, BlockPos blockPos, Supplier<ItemStack> supplier, Runnable runnable, BooleanSupplier booleanSupplier, int n, int n2, int n3) {
        return Button.m_253074_((Component)StatueBuildInspectorData.text("entry", new Object[0]), button -> {
            if (booleanSupplier.getAsBoolean()) {
                Minecraft.m_91087_().m_91152_((Screen)new StatueBuildInspectorScreen(screen, blockPos, supplier, runnable, booleanSupplier));
            }
        }).m_252987_(n, n2, n3, 25).m_253136_();
    }

    protected void m_7856_() {
        this.panelWidth = Math.min(490, this.f_96543_ - 16);
        this.panelHeight = Math.min(310, this.f_96544_ - 16);
        this.left = (this.f_96543_ - this.panelWidth) / 2;
        this.top = (this.f_96544_ - this.panelHeight) / 2;
        this.tabs.clear();
        int n = (this.panelWidth - 24) / 3;
        for (int i = 0; i < 3; ++i) {
            int n2 = i;
            this.tabs.add((Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)StatueBuildInspectorData.text("tab." + i, new Object[0]), button -> {
                this.tab = n2;
                this.scroll = 0;
                this.rebuildLines();
            }).m_252987_(this.left + 12 + i * n, this.top + 54, n - 4, 20).m_253136_()));
        }
        this.filterButton = (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)this.filterLabel(), button -> {
            this.activeOnly = !this.activeOnly;
            this.scroll = 0;
            this.rebuildLines();
        }).m_252987_(this.left + this.panelWidth - 112, this.top + 79, 100, 18).m_253136_());
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)(this.parent == null ? Component.m_237115_((String)"gui.back") : StatueBuildInspectorData.text("back", new Object[0])), button -> this.m_7379_()).m_252987_(this.left + 12, this.top + this.panelHeight - 28, 96, 20).m_253136_());
        this.adjustButton = this.adjust == null ? null : (Button)this.m_142416_((GuiEventListener)Button.m_253074_((Component)StatueBuildInspectorData.text("adjust", new Object[0]), button -> {
            if (!this.available.getAsBoolean() || this.weaponSupplier.get().m_41619_()) {
                return;
            }
            this.f_96541_.m_91152_(this.parent);
            this.adjust.run();
        }).m_252987_(this.left + this.panelWidth - 132, this.top + this.panelHeight - 28, 120, 20).m_253136_());
        this.refresh();
        this.rebuildLines();
    }

    private Component filterLabel() {
        return StatueBuildInspectorData.text(this.activeOnly ? "filter.active" : "filter.all", new Object[0]);
    }

    private void refresh() {
        ItemStack itemStack = this.weaponSupplier.get();
        if (this.displayedWeapon == null || this.displayedWeapon.m_41613_() != itemStack.m_41613_() || !ItemStack.m_150942_((ItemStack)this.displayedWeapon, (ItemStack)itemStack)) {
            this.displayedWeapon = itemStack.m_41777_();
            this.view = StatueBuildInspectorData.read(itemStack);
            this.scroll = 0;
            this.rebuildLines();
        }
        if (this.adjustButton != null) {
            this.adjustButton.f_93623_ = !itemStack.m_41619_() && this.available.getAsBoolean();
        }
    }

    private void addLines(Component component, int n) {
        for (FormattedCharSequence formattedCharSequence : this.f_96547_.m_92923_((FormattedText)component, this.panelWidth - 42)) {
            this.linesBuffer.add(new Line(formattedCharSequence, n));
        }
    }

    private void rebuildLines() {
        if (this.f_96547_ == null || this.view == null) {
            return;
        }
        this.linesBuffer.clear();
        if (this.displayedWeapon.m_41619_()) {
            this.addLines(StatueBuildInspectorData.text("no_weapon", new Object[0]), -5594442);
        } else {
            this.addLines(StatueBuildInspectorData.text(this.tab == 0 ? "bonds_hint" : (this.tab == 1 ? "multipliers_hint" : "links_hint"), new Object[0]), -5594442);
            List<StatueBuildInspectorData.Entry> list = switch (this.tab) {
                case 0 -> this.view.bonds();
                case 1 -> this.view.multipliers();
                default -> this.view.links();
            };
            int n = 0;
            for (StatueBuildInspectorData.Entry entry : list) {
                if (this.tab == 2 && this.activeOnly && !entry.active()) continue;
                this.addLines((Component)Component.m_237119_(), -1515055);
                this.addLines(entry.title(), entry.active() ? -7480404 : -1655951);
                for (Component component : entry.lines()) {
                    this.addLines(component, -1515055);
                }
                ++n;
            }
            if (n == 0) {
                this.addLines(StatueBuildInspectorData.text(this.tab == 0 ? "no_bonds" : "no_links", new Object[0]), -5594442);
            }
        }
        this.lines = List.copyOf(this.linesBuffer);
        this.scroll = StatueBuildInspectorScreen.clampScroll(this.scroll, this.lines.size() * 12, this.viewportHeight());
        for (int i = 0; i < this.tabs.size(); ++i) {
            this.tabs.get((int)i).f_93623_ = i != this.tab;
        }
        if (this.filterButton != null) {
            this.filterButton.f_93624_ = this.tab == 2;
            this.filterButton.m_93666_(this.filterLabel());
        }
    }

    static int clampScroll(int n, int n2, int n3) {
        return Math.max(0, Math.min(n, Math.max(0, n2 - n3)));
    }

    private int viewportHeight() {
        return Math.max(1, this.panelHeight - 140);
    }

    public void m_86600_() {
        Block block;
        Block block2 = block = this.f_96541_.f_91073_ == null ? null : this.f_96541_.f_91073_.m_8055_(this.statuePos).m_60734_();
        if (this.f_96541_.f_91074_ == null || this.f_96541_.f_91073_ == null || !this.f_96541_.f_91074_.m_6084_() || this.f_96541_.f_91074_.m_20275_((double)this.statuePos.m_123341_() + 0.5, (double)this.statuePos.m_123342_() + 1.0, (double)this.statuePos.m_123343_() + 0.5) > 64.0 || !(block instanceof CandyStatueBlock) && !(block instanceof BlankStatueBlock)) {
            this.f_96541_.m_91152_(null);
            return;
        }
        this.refresh();
    }

    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        int n4;
        this.m_280273_(guiGraphics);
        guiGraphics.m_280509_(this.left - 1, this.top - 1, this.left + this.panelWidth + 1, this.top + this.panelHeight + 1, -1655951);
        guiGraphics.m_280509_(this.left, this.top, this.left + this.panelWidth, this.top + this.panelHeight, -266856677);
        guiGraphics.m_280614_(this.f_96547_, this.f_96539_, this.left + 12, this.top + 10, -1655951, false);
        if (this.displayedWeapon != null && !this.displayedWeapon.m_41619_()) {
            guiGraphics.m_280480_(this.displayedWeapon, this.left + 12, this.top + 29);
            guiGraphics.m_280056_(this.f_96547_, this.f_96547_.m_92834_(this.displayedWeapon.m_41786_().getString(), this.panelWidth - 48), this.left + 34, this.top + 33, -1515055, false);
        }
        guiGraphics.m_280614_(this.f_96547_, StatueBuildInspectorData.text("readonly", new Object[0]), this.left + 12, this.top + 83, -5594442, false);
        int n5 = this.top + 103;
        guiGraphics.m_280509_(this.left + 11, n5 - 2, this.left + this.panelWidth - 11, n5 + this.viewportHeight() + 2, -14409175);
        guiGraphics.m_280588_(this.left + 12, n5, this.left + this.panelWidth - 12, n5 + this.viewportHeight());
        for (n4 = 0; n4 < this.lines.size(); ++n4) {
            n3 = n5 + n4 * 12 - this.scroll;
            if (n3 + 12 <= n5 || n3 >= n5 + this.viewportHeight()) continue;
            Line line = this.lines.get(n4);
            guiGraphics.m_280649_(this.f_96547_, line.text(), this.left + 18, n3, line.color(), false);
        }
        guiGraphics.m_280618_();
        n4 = Math.max(0, this.lines.size() * 12 - this.viewportHeight());
        if (n4 > 0) {
            n3 = Math.max(14, this.viewportHeight() * this.viewportHeight() / (this.lines.size() * 12));
            int n6 = n5 + this.scroll * (this.viewportHeight() - n3) / n4;
            guiGraphics.m_280509_(this.left + this.panelWidth - 16, n6, this.left + this.panelWidth - 13, n6 + n3, -1655951);
        }
        super.m_88315_(guiGraphics, n, n2, f);
    }

    public boolean m_6050_(double d, double d2, double d3) {
        if (d >= (double)(this.left + 12) && d < (double)(this.left + this.panelWidth - 12) && d2 >= (double)(this.top + 103) && d2 < (double)(this.top + 103 + this.viewportHeight())) {
            this.scroll = StatueBuildInspectorScreen.clampScroll(this.scroll - (int)Math.round(d3 * 24.0), this.lines.size() * 12, this.viewportHeight());
            return true;
        }
        return super.m_6050_(d, d2, d3);
    }

    public void m_7379_() {
        this.f_96541_.m_91152_(this.parent);
    }

    public boolean m_7043_() {
        return false;
    }

    public static Button previewEntry(BlockPos blockPos, Supplier<ItemStack> supplier, BooleanSupplier booleanSupplier, int n, int n2, int n3) {
        return Button.m_253074_((Component)StatueBuildInspectorData.text("entry", new Object[0]), button -> {
            Minecraft minecraft = Minecraft.m_91087_();
            if (minecraft.f_91074_ == null || !booleanSupplier.getAsBoolean()) {
                return;
            }
            minecraft.f_91074_.m_6915_();
            minecraft.m_91152_((Screen)new StatueBuildInspectorScreen(null, blockPos, supplier, null, () -> minecraft.f_91074_ != null && minecraft.f_91074_.m_6084_()));
        }).m_252987_(n, n2, n3, 16).m_253136_();
    }

    private record Line(FormattedCharSequence text, int color) {
    }
}

