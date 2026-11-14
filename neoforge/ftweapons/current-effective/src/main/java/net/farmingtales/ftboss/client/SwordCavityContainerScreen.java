package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.Entry;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.menu.WeaponCavityMenuRules;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.WeaponUiSnapshot;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.farmingtales.ftboss.util.SwordCavityPaletteRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Status;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SwordCavityContainerScreen extends AbstractContainerScreen<SwordCavityMenu> {
    private static final int BACKGROUND = -15129812;
    private static final int PANEL = -13944511;
    private static final int EDGE = -10455686;
    private static final int TEXT = -2234145;
    private static final int MUTED = -6112848;
    private static final int WARNING = -28288;
    private static final int LOCKED = -15656673;
    private static final int FUNCTION_X = 17;
    private static final int FUNCTION_Y = 103;
    private static final int FUNCTION_PITCH = 23;
    private final SwordCavityBondPanel bonds = new SwordCavityBondPanel();
    private final boolean foundationMenu;
    private final int hubReturnButton;
    private final String shapeFamily;
    private final List<ItemStack> environmentItems;
    private List<Piece> nativeParts = List.of();
    private View foundation;
    private Slot dragSource;
    private int dragButton;
    private boolean cavityPress;
    private int cavityButton;

    public SwordCavityContainerScreen(SwordCavityMenu var1, Inventory var2, Component var3) {
        super(var1, var2, var3);
        this.hubReturnButton = StatueWeaponRepairNavigation.returnButton(var1, net.minecraft.client.Minecraft.getInstance().screen);
        this.foundationMenu = FourWeaponFoundationAdapter.applies(var1.weapon());
        this.shapeFamily = WeaponCavityMenuRules.shapeFamily(var1.weapon());
        this.environmentItems = WeaponCavityShapeRules.environment(this.shapeFamily, BuiltInRegistries.ITEM.getKey(var1.weapon().getItem()).toString())
            .stream()
            .<ResourceLocation>map(ResourceLocation::parse)
            .filter(BuiltInRegistries.ITEM::containsKey)
            .map(var0 -> new ItemStack(BuiltInRegistries.ITEM.get(var0)))
            .toList();
        this.imageWidth = 320;
        this.imageHeight = 208;
        this.titleLabelX = 10;
        this.titleLabelY = 7;
        this.inventoryLabelX = 17;
        this.inventoryLabelY = 114;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("screen.ftboss.cavity.back_short"), var1 -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.hubReturnButton);
            }
        }).bounds(this.leftPos + 157, this.topPos + 5, 29, 17).tooltip(Tooltip.create(Component.translatable("screen.ftboss.cavity.back_hint"))).build());
        this.addRenderableWidget(StatueWeaponRepairNavigation.cavityEntry(this.menu, this.leftPos + 196 + 6, this.topPos + this.imageHeight - 22, 118, this.hubReturnButton));
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        this.renderBackground(var1, var2, var3, var4);
        CavityPresentationCache.Frame var5 = CavityPresentationCache.frame(this, this.menu.weapon(), this.minecraft == null ? null : this.minecraft.level);
        this.foundation = var5.foundation();
        this.nativeParts = var5.nativeParts();
        super.render(var1, var2, var3, var4);
        if (this.hoveredSlot != null && this.hoveredSlot.index < 27) {
            this.renderMaterialTooltip(var1, var2, var3, this.hoveredSlot.index);
        } else {
            this.renderTooltip(var1, var2, var3);
        }

        for (int var6 = 0; var6 < this.environmentItems.size(); var6++) {
            int var7 = this.leftPos + 151 + var6 * 18;
            if (var2 >= var7 && var2 < var7 + 16 && var3 >= this.topPos + 23 && var3 < this.topPos + 39) {
                var1.renderComponentTooltip(
                    this.font,
                    List.of(
                        this.environmentItems.get(var6).getHoverName(),
                        Component.translatableWithFallback("screen.ftboss.cavity.environment_hint", "环境搭配参考 · 未安装，不计属性"),
                        Component.translatableWithFallback("screen.ftboss.cavity.environment_help", "仅示意环境搭配；可装材料以当前武器的规则为准")
                    ),
                    var2,
                    var3
                );
            }
        }

        if ((this.foundation != null ? this.foundation.profile() != null : SwordCavityExtensions.functionProfile(this.menu.weapon()) != null)
            && var2 >= this.leftPos + 17
            && var2 < this.leftPos + 17 + 115
            && var3 >= this.topPos + 101
            && var3 < this.topPos + 112) {
            int var9 = (var2 - this.leftPos - 17) / 23;
            if (this.foundation != null) {
                ArrayList var11 = new ArrayList();
                var11.add(FourWeaponCavityPresentation.axisName(this.foundation.profile(), var9, false));
                var11.add(
                    this.foundation.valid()
                        ? Component.translatable(
                            "screen.ftboss.cavity.function_score", format(var5.status().total().at(var9)), format(this.foundation.profile().minimum().at(var9))
                        )
                        : Component.translatableWithFallback("screen.ftboss.cavity.structure_short", "Structure unavailable")
                );
                var1.renderComponentTooltip(this.font, var11, var2, var3);
                return;
            }

            Function var10 = Function.values()[var9];
            Status var8 = var5.swordStatus();
            var1.renderComponentTooltip(
                this.font,
                List.of(
                    Component.translatable("function.ftboss.sword." + var10.name().toLowerCase(Locale.ROOT)),
                    Component.translatable("screen.ftboss.cavity.function_score", format(var8.total().value(var10)), format(var8.minimum().value(var10)))
                ),
                var2,
                var3
            );
        }
    }

    @Override
    protected void renderBg(GuiGraphics var1, float var2, int var3, int var4) {
        var1.fill(this.leftPos, this.topPos, this.leftPos + 196, this.topPos + this.imageHeight, -10455686);
        var1.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + 196 - 1, this.topPos + this.imageHeight - 1, -15129812);
        this.renderWeaponShape(var1);
        Profile var5 = SwordCavityExtensions.functionProfile(this.menu.weapon());

        for (Slot var7 : this.menu.slots) {
            int var8 = this.leftPos + var7.x;
            int var9 = this.topPos + var7.y;
            if (var7.index >= 27
                || this.cellUnlocked(var7.index)
                || var7.hasItem()
                || (this.foundation != null ? FourWeaponCavityPresentation.reference(this.foundation, var7.index) : this.menu.reference(var7.index))) {
                var1.fill(var8 - 1, var9 - 1, var8 + 17, var9 + 17, -10455686);
                int var10 = var7.index < 27
                    ? (
                        this.foundation != null
                            ? FourWeaponCavityPresentation.cellColor(this.foundation, var7.index)
                            : SwordCavityPaletteRules.cellColor(var5, var7.index)
                    )
                    : -13944511;
                var1.fill(var8, var9, var8 + 16, var9 + 16, var10);
                if (var7.index < 27 && !this.cellUnlocked(var7.index) && var7.hasItem()) {
                    var1.fill(var8, var9, var8 + 16, var9 + 16, -15656673);
                }

                if (var7.index < 27 && !this.cellUnlocked(var7.index) && !var7.hasItem()) {
                    var1.fill(var8, var9, var8 + 16, var9 + 16, -15656673);
                    var1.fill(var8 + 5, var9 + 7, var8 + 11, var9 + 12, -6112848);
                    var1.fill(var8 + 6, var9 + 4, var8 + 10, var9 + 8, -6112848);
                    var1.fill(var8 + 7, var9 + 5, var8 + 9, var9 + 7, -15656673);
                }

                if (var7.index < 27
                    && (this.foundation != null ? FourWeaponCavityPresentation.reference(this.foundation, var7.index) : this.menu.reference(var7.index))) {
                    ItemStack var11 = this.foundation != null
                        ? FourWeaponCavityPresentation.displayItem(this.foundation, var7.index)
                        : SwordCavityBuild.displayItem(
                            this.menu.weapon(), net.farmingtales.ftboss.util.SwordCavityRules.Slot.byIndex(var7.index), this.nativeParts
                        );
                    var1.renderItem(var11, var8, var9);
                    var1.fill(var8 + 12, var9 + 12, var8 + 16, var9 + 16, -6112848);
                }

                if (var7.index < 27) {
                    int var12 = this.cellStateColor(var7.index);
                    if (var12 != 0) {
                        var1.fill(var8 + 1, var9 + 1, var8 + 4, var9 + 4, var12);
                    }
                }
            }
        }

        this.bonds.render(var1, this.font, this.menu.weapon(), this.leftPos + 196 + 6, this.topPos, this.imageHeight - 26);
    }

    @Override
    protected void renderLabels(GuiGraphics var1, int var2, int var3) {
        CavityPresentationCache.Frame var4 = CavityPresentationCache.frame(this, this.menu.weapon(), this.minecraft == null ? null : this.minecraft.level);
        Component var5 = this.foundation == null ? this.title : Component.translatableWithFallback("screen.ftboss.cavity.structure", "Composition");
        var1.drawString(this.font, this.font.plainSubstrByWidth(var5.getString(), 96), this.titleLabelX, this.titleLabelY, -2234145, false);
        var1.drawString(this.font, Component.translatableWithFallback("screen.ftboss.cavity.environment_short", "环境参考"), 110, 7, -6112848, false);
        ItemStack var6 = this.menu.weapon();
        var1.renderItem(var6, 17, 23);
        var1.drawString(this.font, this.font.plainSubstrByWidth(var6.getHoverName().getString(), 108), 37, 27, -2234145, false);

        for (int var7 = 0; var7 < this.environmentItems.size(); var7++) {
            int var8 = 151 + var7 * 18;
            var1.fill(var8 - 1, 22, var8 + 17, 40, -13083828);
            var1.fill(var8, 23, var8 + 16, 39, -15129812);
            var1.renderItem(this.environmentItems.get(var7), var8, 23);
        }

        if (this.foundation != null) {
            if (this.foundation.profile() != null) {
                for (int var14 = 0; var14 < 5; var14++) {
                    boolean var16 = var4.status() != null && var4.status().total().at(var14) + 1.0E-9 >= this.foundation.profile().minimum().at(var14);
                    Component var9 = FourWeaponCavityPresentation.axisName(this.foundation.profile(), var14, true);
                    var1.drawString(
                        this.font,
                        this.font.plainSubstrByWidth(var9.getString(), 17) + (var16 ? "+" : "-"),
                        17 + var14 * 23,
                        103,
                        var16 ? FourWeaponCavityPresentation.axisColor(var14) : -28288,
                        false
                    );
                }

                if (this.foundation.valid()) {
                    var1.drawString(
                        this.font,
                        var4.status().openCells() + "/" + FourWeaponFoundationRules.capacity(this.foundation.profile(), 4),
                        151,
                        103,
                        -6112848,
                        false
                    );
                }
            } else {
                var1.drawString(
                    this.font, Component.translatableWithFallback("screen.ftboss.cavity.structure_short", "Structure unavailable"), 17, 103, -28288, false
                );
            }
        } else if (SwordCavityExtensions.functionProfile(var6) != null) {
            Status var15 = var4.swordStatus();

            for (Function var11 : Function.values()) {
                MutableComponent var12 = Component.translatable("function.ftboss.sword.short." + var11.name().toLowerCase(Locale.ROOT));
                String var13 = var15.sufficient(var11) ? "+" : "-";
                var1.drawString(
                    this.font,
                    var12.getString() + var13,
                    17 + var11.ordinal() * 23,
                    103,
                    var15.sufficient(var11) ? SwordCavityPaletteRules.functionColor(var11) : -28288,
                    false
                );
            }

            int var18 = SwordFunctionRules.maximumCells(SwordCavityExtensions.functionProfile(var6));
            var1.drawString(this.font, var15.unlockedCells() + "/" + var18, 151, 103, -6112848, false);
        }

        var1.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -6112848, false);
    }

    private void renderMaterialTooltip(GuiGraphics var1, int var2, int var3, int var4) {
        if (this.foundation != null) {
            this.renderFoundationMaterialTooltip(var1, var2, var3, var4);
        } else {
            ItemStack var5 = ((Slot)this.menu.slots.get(var4)).getItem();
            boolean var6 = this.menu.reference(var4);
            if (var6) {
                var5 = SwordCavityBuild.displayItem(this.menu.weapon(), net.farmingtales.ftboss.util.SwordCavityRules.Slot.byIndex(var4), this.nativeParts);
            }

            ArrayList var7 = new ArrayList();
            if (!var5.isEmpty()) {
                var7.add(var5.getHoverName());
                var var8 = SwordMaterialExtensions.resolve(var5);
                ArrayList var9 = new ArrayList();
                if (SwordCavityExtensions.functionProfile(this.menu.weapon()) != null) {
                    var8.ifPresent(var1x -> {
                        for (Function var5x : Function.values()) {
                            if (var1x.functions().value(var5x) >= 0.6) {
                                var9.add(Component.translatable("function.ftboss.sword." + var5x.name().toLowerCase(Locale.ROOT)).getString());
                            }
                        }
                    });
                }

                if (!var9.isEmpty()) {
                    var7.add(Component.literal(String.join(" · ", var9)));
                }

                if (var6) {
                    var7.add(Component.translatable("screen.ftboss.cavity.reference_short"));
                } else {
                    Material var10 = SwordCavityBuild.materialFor(var5);
                    var7.addAll(SwordCavityBuild.materialEffects(var10));
                }
            }

            if (!SwordCavityBuild.cellUnlocked(this.menu.weapon(), var4)) {
                var7.add(Component.translatable("screen.ftboss.cavity.locked_short"));
            }

            if (!SwordCavityBuild.containerEditable(this.menu.weapon())) {
                var7.add(Component.translatable("screen.ftboss.cavity.repair_short"));
            }

            if (!var7.isEmpty()) {
                var1.renderComponentTooltip(this.font, var7, var2, var3);
            }
        }
    }

    private static String format(double var0) {
        return String.format(Locale.ROOT, "%.2f", var0);
    }

    private void renderWeaponShape(GuiGraphics var1) {
        int var2 = -8821688;
        int var3 = -8875106;
        int var4 = -7047496;
        if (this.shapeFamily.equals("bow")) {
            this.shapeLine(var1, 61, 88, 97, 52, var2, 4);
            this.shapeLine(var1, 97, 52, 133, 88, var2, 4);
            this.shapeLine(var1, 61, 88, 133, 88, -5850972, 1);
        } else if (this.shapeFamily.equals("staff")) {
            this.shapeLine(var1, 25, 70, 143, 70, var2, 4);
            this.shapeLine(var1, 143, 48, 166, 70, var4, 4);
            this.shapeLine(var1, 166, 70, 143, 93, var4, 4);
            this.shapeLine(var1, 143, 93, 126, 70, var4, 4);
            this.shapeLine(var1, 126, 70, 143, 48, var4, 4);
        } else if (this.shapeFamily.equals("crossbow")) {
            this.shapeLine(var1, 25, 70, 169, 70, var2, 4);
            this.shapeLine(var1, 79, 48, 115, 70, var3, 4);
            this.shapeLine(var1, 115, 70, 79, 93, var3, 4);
            this.shapeLine(var1, 79, 48, 79, 93, -5850972, 1);
        } else if (this.shapeFamily.equals("trident")) {
            this.shapeLine(var1, 25, 70, 175, 70, var2, 4);
            this.shapeLine(var1, 134, 48, 134, 93, var3, 4);
            this.shapeLine(var1, 134, 48, 175, 48, var3, 4);
            this.shapeLine(var1, 134, 93, 175, 93, var3, 4);
        } else if (this.shapeFamily.equals("axe")) {
            this.shapeLine(var1, 25, 70, 152, 70, var2, 4);
            var1.fill(this.leftPos + 128, this.topPos + 46, this.leftPos + 160, this.topPos + 94, var3);
            var1.fill(this.leftPos + 160, this.topPos + 52, this.leftPos + 166, this.topPos + 88, var3);
        } else {
            this.shapeLine(var1, 25, 70, 62, 70, var2, 4);
            this.shapeLine(var1, 61, 47, 61, 93, var2, 4);
            var1.fill(this.leftPos + 66, this.topPos + 62, this.leftPos + 167, this.topPos + 79, var3);
            var1.fill(this.leftPos + 167, this.topPos + 65, this.leftPos + 173, this.topPos + 76, var3);
            var1.fill(this.leftPos + 173, this.topPos + 68, this.leftPos + 179, this.topPos + 73, var3);
        }
    }

    private void shapeLine(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        int var8 = Math.max(Math.abs(var4 - var2), Math.abs(var5 - var3));

        for (int var9 = 0; var9 <= var8; var9++) {
            int var10 = this.leftPos + var2 + (var4 - var2) * var9 / Math.max(1, var8);
            int var11 = this.topPos + var3 + (var5 - var3) * var9 / Math.max(1, var8);
            var1.fill(var10 - var7, var11 - var7, var10 + var7 + 1, var11 + var7 + 1, var6);
        }
    }

    private boolean cellUnlocked(int var1) {
        return this.foundation != null ? FourWeaponCavityPresentation.unlocked(this.foundation, var1) : SwordCavityBuild.cellUnlocked(this.menu.weapon(), var1);
    }

    private int cellStateColor(int var1) {
        if (this.foundation != null) {
            if (!FourWeaponCavityPresentation.unlocked(this.foundation, var1)) {
                return -1917592;
            } else {
                Entry var4 = (Entry)this.foundation.entries().get(var1);
                if (var4 == null) {
                    return 0;
                } else if (var4.paid()) {
                    return -9250406;
                } else {
                    return FourWeaponCavityPresentation.displayItem(this.foundation, var1).isEmpty() ? 0 : -8408103;
                }
            }
        } else if (!SwordCavityBuild.applies(this.menu.weapon())) {
            return 0;
        } else {
            List var2 = SwordCavityBuild.cells(this.menu.weapon(), this.nativeParts);
            if (var1 >= var2.size()) {
                return 0;
            } else if (!SwordCavityBuild.cellUnlocked(this.menu.weapon(), var1)) {
                return -1917592;
            } else {
                State var3 = ((Cell)var2.get(var1)).state();
                if (var3 == State.INSTALLED) {
                    return -9250406;
                } else if (var3 == State.NATIVE) {
                    return -8408103;
                } else {
                    return var3 == State.MISSING ? -28288 : 0;
                }
            }
        }
    }

    private void renderFoundationMaterialTooltip(GuiGraphics var1, int var2, int var3, int var4) {
        ArrayList var5 = new ArrayList();
        ItemStack var6 = FourWeaponCavityPresentation.displayItem(this.foundation, var4);
        if (!var6.isEmpty()) {
            var5.add(var6.getHoverName());
        }

        List var7 = FourWeaponCavityPresentation.materialRoles(this.foundation, var4).stream().map(Component::getString).toList();
        if (!var7.isEmpty()) {
            var5.add(Component.literal(String.join(" · ", var7)));
        }

        Entry var8 = (Entry)this.foundation.entries().get(var4);
        if (var8 != null && this.foundation.valid()) {
            if (!var8.paid()) {
                var5.add(Component.translatable("screen.ftboss.cavity.reference_short"));
            } else {
                var5.addAll(SwordCavityBuild.materialEffects(var8.value().material()));
            }
        }

        if (!this.cellUnlocked(var4)) {
            var5.add(Component.translatable("screen.ftboss.cavity.locked_short"));
        }

        if (!this.foundation.valid()) {
            var5.add(Component.translatableWithFallback("screen.ftboss.cavity.structure_short", "Structure unavailable"));
        }

        if (!var5.isEmpty()) {
            var1.renderComponentTooltip(this.font, var5, var2, var3);
        }
    }

    public boolean mouseScrolled(double var1, double var3, double horizontalScroll, double var5) {
        return this.bonds.scroll(var1, var3, var5, this.leftPos + 196 + 6, this.topPos, this.imageHeight - 26) || super.mouseScrolled(var1, var3, horizontalScroll, var5);
    }

    @Override
    public boolean mouseClicked(double var1, double var3, int var5) {
        this.dragSource = null;
        if (this.usesFoundation() && (var5 == 0 || var5 == 1) && !hasShiftDown()) {
            Slot var6 = this.pointerSlot(var1, var3);
            if (var6 != null && var6.index < 27) {
                this.cavityPress = true;
                this.cavityButton = var5;
                super.mouseClicked(var1, var3, var5);
                if (this.foundationReady()) {
                    if (this.menu.getCarried().isEmpty() && this.menu.reference(var6.index) && FourWeaponClientApi.canWrite(Operation.REMOVE_CELL)) {
                        FourWeaponClientApi.submit(Operation.REMOVE_CELL, var6.index, 0);
                    } else {
                        FourWeaponClientApi.menuClick(this.menu.containerId, var6.index, var5, ClickType.PICKUP);
                    }
                }

                return true;
            }

            if (var6 != null
                && var6.hasItem()
                && this.menu.getCarried().isEmpty()
                && this.minecraft != null
                && this.minecraft.player != null
                && var6.mayPickup(this.minecraft.player)
                && this.foundationReady()) {
                this.dragSource = var6;
                this.dragButton = var5;
            }
        }

        if ((var5 == 0 || var5 == 1) && this.menu.getCarried().isEmpty()) {
            for (int var8 = 0; var8 < 27; var8++) {
                Slot var7 = (Slot)this.menu.slots.get(var8);
                if (this.isHovering(var7.x, var7.y, 16, 16, var1, var3) && this.menu.reference(var8)) {
                    if (this.usesFoundation()) {
                        if (this.foundationReady()) {
                            FourWeaponClientApi.submit(Operation.REMOVE_CELL, var8, 0);
                        }
                    } else if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var8);
                    }

                    return true;
                }
            }
        }

        return super.mouseClicked(var1, var3, var5);
    }

    @Override
    protected void slotClicked(Slot var1, int var2, int var3, ClickType var4) {
        if (this.usesFoundation()) {
            if (!this.cavityPress) {
                int var5 = var1 == null ? var2 : var1.index;
                if (this.foundationReady()) {
                    FourWeaponClientApi.menuClick(this.menu.containerId, var5, var3, var4);
                }
            }
        } else {
            super.slotClicked(var1, var2, var3, var4);
        }
    }

    private boolean usesFoundation() {
        return this.foundationMenu || FourWeaponFoundationAdapter.applies(this.menu.weapon());
    }

    private boolean foundationReady() {
        WeaponUiSnapshot var1 = FourWeaponClientApi.snapshot();
        Snapshot var2 = FourWeaponClientApi.authority();
        return FourWeaponClientApi.capable()
            && var1 != null
            && var2 != null
            && var1.session().equals(var2.session())
            && var1.data().getInt("Container") == this.menu.containerId
            && var2.binding().weapon().equals(FoodWeaponLifecycle.weaponId(this.menu.weapon()));
    }

    @Override
    public boolean mouseReleased(double var1, double var3, int var5) {
        if (this.cavityPress && this.cavityButton == var5) {
            try {
                super.mouseReleased(var1, var3, var5);
            } finally {
                this.cavityPress = false;
            }

            return true;
        } else {
            Slot var6 = this.dragSource;
            if (var6 != null && this.dragButton == var5) {
                this.dragSource = null;
                Slot var7 = this.pointerSlot(var1, var3);
                if (var7 != null && var7.index < 27 && this.foundationReady()) {
                    super.mouseReleased((double)(this.leftPos + var6.x + 8), (double)(this.topPos + var6.y + 8), var5);
                    this.mouseClicked(var1, var3, var5);
                    return this.mouseReleased(var1, var3, var5);
                }
            }

            return super.mouseReleased(var1, var3, var5);
        }
    }

    private Slot pointerSlot(double var1, double var3) {
        for (Slot var6 : this.menu.slots) {
            if (var6.isActive() && this.isHovering(var6.x, var6.y, 16, 16, var1, var3)) {
                return var6;
            }
        }

        return null;
    }
}
