package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.menu.WeaponCavityMenuRules;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCavityPaletteRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 武器箱式构成界面；2026-10-25 从父页捕获主题并重绘嵌槽与状态层级，保留即时排队和背包拖放。 */
public final class SwordCavityContainerScreen extends AbstractContainerScreen<SwordCavityMenu> implements StatueThemeSource {
    private static final int WARNING = 0xFFFF9180;
    private static final int LOCKED = 0xFF11191F;
    private static final int FUNCTION_X = 17;
    private static final int FUNCTION_Y = 103;
    private static final int FUNCTION_PITCH = 23;
    private final SwordCavityBondPanel bonds = new SwordCavityBondPanel();
    private final StatueUiTheme theme;
    private final boolean foundationMenu;
    private final String shapeFamily;
    private final List<ItemStack> environmentItems;
    private FourWeaponFoundationAdapter.View foundation;
    private CavityPresentationCache.Frame presentation;
    private Slot dragSource;
    private int dragButton;
    private boolean cavityPress;
    private int cavityButton;

    public SwordCavityContainerScreen(SwordCavityMenu menu, Inventory inventory, Component title) {
        this(menu, inventory, title, StatueUiTheme.fromScreen(Minecraft.getInstance().screen));
    }

    public SwordCavityContainerScreen(SwordCavityMenu menu, Inventory inventory, Component title, StatueUiTheme theme) {
        super(menu, inventory, title);
        this.theme = theme;
        foundationMenu = FourWeaponFoundationAdapter.applies(menu.weapon());
        shapeFamily = WeaponCavityMenuRules.shapeFamily(menu.weapon());
        environmentItems = WeaponCavityShapeRules.environment(shapeFamily, BuiltInRegistries.ITEM.getKey(menu.weapon().getItem()).toString())
                .stream().map(ResourceLocation::new).filter(BuiltInRegistries.ITEM::containsKey)
                .map(identifier -> new ItemStack(BuiltInRegistries.ITEM.get(identifier))).toList();
        imageWidth = SwordCavityMenu.WIDTH + SwordCavityBondPanel.GAP + SwordCavityBondPanel.WIDTH;
        imageHeight = SwordCavityMenu.HEIGHT;
        titleLabelX = 25;
        titleLabelY = 7;
        inventoryLabelX = SwordCavityMenu.GRID_X;
        inventoryLabelY = 114;
    }

    @Override
    public StatueUiTheme statueTheme() { return theme; }

    @Override
    protected void init() {
        super.init();
        Button back = StatueThemeButton.create(Component.translatable("screen.ftboss.cavity.back_short"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SwordCavityMenu.BACK_BUTTON);
        }, leftPos + SwordCavityMenu.BACK_X, topPos + SwordCavityMenu.BACK_Y, SwordCavityMenu.BACK_WIDTH, SwordCavityMenu.BACK_HEIGHT, this::statueTheme);
        back.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.cavity.back_hint")));
        addRenderableWidget(back);
        addRenderableWidget(StatueWeaponRepairNavigation.cavityEntry(menu,
                leftPos + SwordCavityMenu.WIDTH + SwordCavityBondPanel.GAP, topPos + imageHeight - 22, SwordCavityBondPanel.WIDTH));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        presentation = CavityPresentationCache.frame(this, menu.weapon(), minecraft == null ? null : minecraft.level);
        foundation = presentation.foundation();
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hoveredSlot != null && hoveredSlot.index < SwordCavityRules.CELL_COUNT) renderMaterialTooltip(graphics, mouseX, mouseY, hoveredSlot.index);
        else renderTooltip(graphics, mouseX, mouseY);
        for (int index = 0; index < environmentItems.size(); index++) {
            int position = leftPos + 151 + index * 18;
            if (mouseX >= position && mouseX < position + 16 && mouseY >= topPos + 23 && mouseY < topPos + 39) {
                graphics.renderComponentTooltip(font, List.of(environmentItems.get(index).getHoverName(),
                        Component.translatableWithFallback("screen.ftboss.cavity.environment_hint", "环境搭配参考 · 未安装，不计属性"),
                        Component.translatableWithFallback("screen.ftboss.cavity.environment_help", "仅示意环境搭配；可装材料以当前武器的规则为准")), mouseX, mouseY);
            }
        }
        if ((foundation != null ? foundation.profile() != null : presentation.profile() != null) && mouseX >= leftPos + FUNCTION_X
                && mouseX < leftPos + FUNCTION_X + FUNCTION_PITCH * 5 && mouseY >= topPos + 101 && mouseY < topPos + 112) {
            int index = (mouseX - leftPos - FUNCTION_X) / FUNCTION_PITCH;
            if (foundation != null) {
                List<Component> tips = new ArrayList<>();
                tips.add(FourWeaponCavityPresentation.axisName(foundation.profile(), index, false));
                tips.add(foundation.valid() ? Component.translatable("screen.ftboss.cavity.function_score",
                        format(presentation.status().total().at(index)), format(foundation.profile().minimum().at(index)))
                        : Component.translatableWithFallback("screen.ftboss.cavity.structure_short", "Structure unavailable"));
                graphics.renderComponentTooltip(font, tips, mouseX, mouseY);
                return;
            }
            SwordFunctionRules.Function function = SwordFunctionRules.Function.values()[index];
            SwordFunctionRules.Status status = presentation.swordStatus();
            graphics.renderComponentTooltip(font, List.of(functionName(function, false),
                    Component.translatable("screen.ftboss.cavity.function_score", format(status.total().value(function)), format(status.minimum().value(function)))), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        StatueThemeRenderer.frame(graphics, leftPos, topPos, SwordCavityMenu.WIDTH, imageHeight, theme);
        StatueUiDecor.gear(graphics, leftPos + 7, topPos + 5, 14);
        StatueThemeRenderer.line(graphics, leftPos + 9, topPos + 19, 137, theme);
        StatueThemeRenderer.card(graphics, leftPos + 12, topPos + 22, 132, 19, theme, false);
        StatueThemeRenderer.card(graphics, leftPos + 11, topPos + 42, 174, 56, theme, false);
        StatueThemeRenderer.card(graphics, leftPos + 12, topPos + 99, 173, 13, theme, false);
        StatueThemeRenderer.card(graphics, leftPos + 12, topPos + 121, 172, 78, theme, false);
        graphics.drawManaged(() -> renderWeaponShape(graphics));
        graphics.drawManaged(() -> renderCells(graphics, mouseX, mouseY));
        bonds.render(graphics, font, menu.weapon(), leftPos + SwordCavityMenu.WIDTH + SwordCavityBondPanel.GAP, topPos, imageHeight - 26, theme, presentation);
    }

    private void renderCells(GuiGraphics graphics, int mouseX, int mouseY) {
        SwordFunctionRules.Profile profile = presentation.profile();
        for (Slot slot : menu.slots) {
            int posX = leftPos + slot.x;
            int posY = topPos + slot.y;
            if (slot.index < SwordCavityRules.CELL_COUNT && !cellUnlocked(slot.index) && !slot.hasItem()
                    && !reference(slot.index)) continue;
            int color = slot.index < SwordCavityRules.CELL_COUNT ? foundation != null
                    ? FourWeaponCavityPresentation.cellColor(foundation, slot.index) : SwordCavityPaletteRules.cellColor(profile, slot.index) : theme.border;
            boolean disabled = slot.index < SwordCavityRules.CELL_COUNT && !cellUnlocked(slot.index);
            boolean hovered = mouseX >= posX && mouseX < posX + 16 && mouseY >= posY && mouseY < posY + 16;
            graphics.fill(posX - 1, posY - 1, posX + 17, posY + 17, theme.border);
            graphics.fill(posX, posY, posX + 16, posY + 16, theme.background);
            graphics.fill(posX, posY, posX + 16, posY + 1, 0xFF080B0D);
            StatueUiDecor.socket(graphics, posX - 1, posY - 1, 18, disabled ? 3 : hovered ? 1 : slot.hasItem() ? 2 : 0);
            graphics.fill(posX, posY + 15, posX + 16, posY + 16, color);
            if (slot.index < SwordCavityRules.CELL_COUNT && !cellUnlocked(slot.index) && slot.hasItem()) {
                graphics.fill(posX, posY, posX + 16, posY + 16, LOCKED);
            }
            if (slot.index < SwordCavityRules.CELL_COUNT && !cellUnlocked(slot.index) && !slot.hasItem()) {
                graphics.fill(posX, posY, posX + 16, posY + 16, LOCKED);
                graphics.fill(posX + 5, posY + 7, posX + 11, posY + 12, theme.muted);
                graphics.fill(posX + 6, posY + 4, posX + 10, posY + 8, theme.muted);
                graphics.fill(posX + 7, posY + 5, posX + 9, posY + 7, LOCKED);
            }
            if (slot.index < SwordCavityRules.CELL_COUNT && reference(slot.index)) {
                ItemStack icon = presentation.cells().get(slot.index).icon();
                graphics.renderItem(icon, posX, posY);
                graphics.fill(posX + 12, posY + 12, posX + 16, posY + 16, theme.muted);
            }
            if (slot.index < SwordCavityRules.CELL_COUNT) {
                int stateColor = cellStateColor(slot.index);
                if (stateColor != 0) graphics.fill(posX + 1, posY + 1, posX + 4, posY + 4, stateColor);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component heading = net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(menu.weapon())
                ? Component.translatable("screen.ftboss.cavity.armor_structure") : foundation == null ? title
                : Component.translatableWithFallback("screen.ftboss.cavity.structure", "Composition");
        graphics.drawString(font, font.plainSubstrByWidth(heading.getString(), 79), titleLabelX, titleLabelY, theme.accentBright, false);
        graphics.drawString(font, Component.translatableWithFallback("screen.ftboss.cavity.environment_short", "环境参考"), 110, 7, theme.muted, false);
        ItemStack weapon = menu.weapon();
        graphics.renderItem(weapon, 17, 23);
        graphics.drawString(font, font.plainSubstrByWidth(weapon.getHoverName().getString(), 103), 37, 27, theme.text, false);
        for (int index = 0; index < environmentItems.size(); index++) {
            int position = 151 + index * 18;
            graphics.fill(position - 1, 22, position + 17, 40, theme.border);
            graphics.fill(position, 23, position + 16, 39, theme.background);
            graphics.renderItem(environmentItems.get(index), position, 23);
        }
        if (foundation != null) {
            if (foundation.profile() != null) {
                for (int axis = 0; axis < 5; axis++) {
                    boolean sufficient = presentation.status() != null && presentation.status().total().at(axis) + 1.0e-9 >= foundation.profile().minimum().at(axis);
                    Component label = FourWeaponCavityPresentation.axisName(foundation.profile(), axis, true);
                    graphics.drawString(font, font.plainSubstrByWidth(label.getString(), FUNCTION_PITCH - 6) + (sufficient ? "+" : "-"),
                            FUNCTION_X + axis * FUNCTION_PITCH, FUNCTION_Y, sufficient ? FourWeaponCavityPresentation.axisColor(axis) : WARNING, false);
                }
                if (foundation.valid()) graphics.drawString(font, presentation.status().openCells() + "/"
                        + FourWeaponFoundationRules.capacity(foundation.profile(), 4), 151, FUNCTION_Y, theme.muted, false);
            } else graphics.drawString(font, Component.translatableWithFallback("screen.ftboss.cavity.structure_short", "Structure unavailable"),
                    FUNCTION_X, FUNCTION_Y, WARNING, false);
        } else if (net.farmingtales.ftboss.item.GourmetBladeCavity.hasFunctions(presentation.profile(), menu.weapon())) {
            SwordFunctionRules.Status status = presentation.swordStatus();
            for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
                Component label = functionName(function, true);
                String mark = status.sufficient(function) ? "+" : "-";
                graphics.drawString(font, label.getString() + mark, FUNCTION_X + function.ordinal() * FUNCTION_PITCH, FUNCTION_Y,
                        status.sufficient(function) ? SwordCavityPaletteRules.functionColor(function) : WARNING, false);
            }
            int maximum = net.farmingtales.ftboss.item.GourmetBladeCavity.maximumCells(presentation.profile(), menu.weapon());
            graphics.drawString(font, status.unlockedCells() + "/" + maximum, 151, FUNCTION_Y, theme.muted, false);
        }
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, theme.muted, false);
    }

    private void renderMaterialTooltip(GuiGraphics graphics, int mouseX, int mouseY, int index) {
        if (foundation != null) {
            renderFoundationMaterialTooltip(graphics, mouseX, mouseY, index);
            return;
        }
        ItemStack item = menu.slots.get(index).getItem();
        boolean reference = reference(index);
        if (reference) item = presentation.cells().get(index).icon();
        List<Component> tips = new ArrayList<>();
        if (!item.isEmpty()) {
            tips.add(item.getHoverName());
            var definition = SwordMaterialExtensions.resolve(item);
            List<String> roles = new ArrayList<>();
            if (presentation.profile() != null) definition.ifPresent(material -> {
                for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) if (material.functions().value(function) >= 0.6) {
                    roles.add(functionName(function, false).getString());
                }
            });
            if (!roles.isEmpty()) tips.add(Component.literal(String.join(" · ", roles)));
            if (reference) {
                if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(menu.weapon())) {
                    var piece = presentation.nativeParts().get(index);
                    tips.add(Component.translatable(piece.origin() == net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin.RECIPE
                            ? "screen.ftboss.cavity.armor_recipe_reference" : "screen.ftboss.cavity.armor_environment_reference"));
                } else tips.add(net.farmingtales.ftboss.item.GourmetBladeCavity.referenceHint(menu.weapon(), index));
            }
            else {
                var material = SwordCavityBuild.materialFor(item);
                tips.addAll(SwordCavityBuild.materialEffects(material));
            }
        }
        if (!cellUnlocked(index)) tips.add(Component.translatable("screen.ftboss.cavity.locked_short"));
        if (!SwordCavityBuild.containerEditable(menu.weapon())) tips.add(Component.translatable("screen.ftboss.cavity.repair_short"));
        if (!tips.isEmpty()) graphics.renderComponentTooltip(font, tips, mouseX, mouseY);
    }

    private static String format(double value) { return String.format(java.util.Locale.ROOT, "%.2f", value); }

    private Component functionName(SwordFunctionRules.Function function, boolean compact) {
        return Component.translatable(net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(menu.weapon())
                ? net.farmingtales.ftboss.item.EquipmentWeaponBuild.functionKey(function, compact)
                : "function.ftboss.sword." + (compact ? "short." : "") + function.name().toLowerCase(java.util.Locale.ROOT));
    }

    private void renderWeaponShape(GuiGraphics graphics) {
        int wood = theme.border;
        int metal = theme.border;
        int crystal = theme.accent;
        if (List.of("helmet", "chestplate", "leggings", "boots").contains(shapeFamily)) {
            for (int position : WeaponCavityShapeRules.outline(shapeFamily)) {
                int column = position % 9;
                int row = position / 9;
                int centerX = SwordCavityMenu.GRID_X + column * SwordCavityMenu.PITCH + 8;
                int centerY = SwordCavityMenu.GRID_Y + row * SwordCavityMenu.PITCH + 8;
                for (int neighbor : WeaponCavityShapeRules.outline(shapeFamily)) {
                    if (neighbor == position + 1 && column < 8 || neighbor == position + 9) {
                        shapeLine(graphics, centerX, centerY, SwordCavityMenu.GRID_X + neighbor % 9 * SwordCavityMenu.PITCH + 8,
                                SwordCavityMenu.GRID_Y + neighbor / 9 * SwordCavityMenu.PITCH + 8, metal, 2);
                    }
                }
            }
        } else if (shapeFamily.equals("bow")) {
            shapeLine(graphics, 61, 88, 97, 52, wood, 4);
            shapeLine(graphics, 97, 52, 133, 88, wood, 4);
            shapeLine(graphics, 61, 88, 133, 88, 0xFFA6B8A4, 1);
        } else if (shapeFamily.equals("staff")) {
            shapeLine(graphics, 25, 70, 143, 70, wood, 4);
            shapeLine(graphics, 143, 48, 166, 70, crystal, 4);
            shapeLine(graphics, 166, 70, 143, 93, crystal, 4);
            shapeLine(graphics, 143, 93, 126, 70, crystal, 4);
            shapeLine(graphics, 126, 70, 143, 48, crystal, 4);
        } else if (shapeFamily.equals("crossbow")) {
            shapeLine(graphics, 25, 70, 169, 70, wood, 4);
            shapeLine(graphics, 79, 48, 115, 70, metal, 4);
            shapeLine(graphics, 115, 70, 79, 93, metal, 4);
            shapeLine(graphics, 79, 48, 79, 93, 0xFFA6B8A4, 1);
        } else if (shapeFamily.equals("trident")) {
            shapeLine(graphics, 25, 70, 175, 70, wood, 4);
            shapeLine(graphics, 134, 48, 134, 93, metal, 4);
            shapeLine(graphics, 134, 48, 175, 48, metal, 4);
            shapeLine(graphics, 134, 93, 175, 93, metal, 4);
        } else if (shapeFamily.equals("axe")) {
            shapeLine(graphics, 25, 70, 152, 70, wood, 4);
            graphics.fill(leftPos + 128, topPos + 46, leftPos + 160, topPos + 94, metal);
            graphics.fill(leftPos + 160, topPos + 52, leftPos + 166, topPos + 88, metal);
        } else {
            shapeLine(graphics, 25, 70, 62, 70, wood, 4);
            shapeLine(graphics, 61, 47, 61, 93, wood, 4);
            graphics.fill(leftPos + 66, topPos + 62, leftPos + 167, topPos + 79, metal);
            graphics.fill(leftPos + 167, topPos + 65, leftPos + 173, topPos + 76, metal);
            graphics.fill(leftPos + 173, topPos + 68, leftPos + 179, topPos + 73, metal);
        }
    }

    private void shapeLine(GuiGraphics graphics, int startX, int startY, int endX, int endY, int color, int radius) {
        if (startX == endX || startY == endY) {
            graphics.fill(leftPos + Math.min(startX, endX) - radius, topPos + Math.min(startY, endY) - radius,
                    leftPos + Math.max(startX, endX) + radius + 1, topPos + Math.max(startY, endY) + radius + 1, color);
            return;
        }
        int steps = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
        for (int step = 0; step <= steps; step++) {
            int positionX = leftPos + startX + (endX - startX) * step / Math.max(1, steps);
            int positionY = topPos + startY + (endY - startY) * step / Math.max(1, steps);
            graphics.fill(positionX - radius, positionY - radius, positionX + radius + 1, positionY + radius + 1, color);
        }
    }

    private boolean cellUnlocked(int index) {
        return presentation.cells().get(index).unlocked();
    }

    private boolean reference(int index) {
        return presentation.cells().get(index).reference() && (foundation != null || !menu.slots.get(index).hasItem());
    }

    /** 只读状态角标：已装=绿、原生参考=蓝、缺件=红、锁格=金；不改变槽位坐标与点击。 */
    private int cellStateColor(int index) {
        return presentation.cells().get(index).stateColor();
    }

    private void renderFoundationMaterialTooltip(GuiGraphics graphics, int mouseX, int mouseY, int index) {
        List<Component> tips = new ArrayList<>();
        ItemStack item = presentation.cells().get(index).icon();
        if (!item.isEmpty()) tips.add(item.getHoverName());
        List<String> roles = FourWeaponCavityPresentation.materialRoles(foundation, index).stream().map(Component::getString).toList();
        if (!roles.isEmpty()) tips.add(Component.literal(String.join(" · ", roles)));
        var entry = foundation.entries().get(index);
        if (entry != null && foundation.valid()) {
            if (!entry.paid()) tips.add(Component.translatable("screen.ftboss.cavity.reference_short"));
            else tips.addAll(SwordCavityBuild.materialEffects(entry.value().material()));
        }
        if (!cellUnlocked(index)) tips.add(Component.translatable("screen.ftboss.cavity.locked_short"));
        if (!foundation.valid()) tips.add(Component.translatableWithFallback("screen.ftboss.cavity.structure_short", "Structure unavailable"));
        if (!tips.isEmpty()) graphics.renderComponentTooltip(font, tips, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return bonds.scroll(mouseX, mouseY, amount, leftPos + SwordCavityMenu.WIDTH + SwordCavityBondPanel.GAP, topPos, imageHeight - 26)
                || super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragSource = null;
        if (usesFoundation() && (button == 0 || button == 1) && !hasShiftDown()) {
            Slot hovered = pointerSlot(mouseX, mouseY);
            if (hovered != null && hovered.index < SwordCavityRules.CELL_COUNT) {
                cavityPress = true;
                cavityButton = button;
                super.mouseClicked(mouseX, mouseY, button);
                if (foundationReady()) {
                    if (menu.getCarried().isEmpty() && menu.reference(hovered.index)
                            && FourWeaponClientApi.canWrite(FourWeaponTransactions.Operation.REMOVE_CELL)) {
                        FourWeaponClientApi.submit(FourWeaponTransactions.Operation.REMOVE_CELL, hovered.index, 0);
                    } else {
                        FourWeaponClientApi.menuClick(menu.containerId, hovered.index, button, ClickType.PICKUP);
                    }
                }
                return true;
            }
            if (hovered != null && hovered.hasItem() && menu.getCarried().isEmpty()
                    && minecraft != null && minecraft.player != null && hovered.mayPickup(minecraft.player) && foundationReady()) {
                dragSource = hovered;
                dragButton = button;
            }
        }
        if ((button == 0 || button == 1) && menu.getCarried().isEmpty()) {
            for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
                Slot slot = menu.slots.get(index);
                if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && menu.reference(index)) {
                    if (usesFoundation()) {
                        if (foundationReady()) FourWeaponClientApi.submit(FourWeaponTransactions.Operation.REMOVE_CELL, index, 0);
                    } else if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (cavityPress && cavityButton == button) {
            try {
                super.mouseReleased(mouseX, mouseY, button);
            } finally {
                cavityPress = false;
            }
            return true;
        }
        Slot source = dragSource;
        if (source != null && dragButton == button) {
            dragSource = null;
            Slot target = pointerSlot(mouseX, mouseY);
            if (target != null && target.index < SwordCavityRules.CELL_COUNT && foundationReady()) {
                super.mouseReleased(leftPos + source.x + 8, topPos + source.y + 8, button);
                mouseClicked(mouseX, mouseY, button);
                return mouseReleased(mouseX, mouseY, button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private Slot pointerSlot(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.isActive() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) return slot;
        }
        return null;
    }

    @Override
    protected void slotClicked(Slot slot, int slotIndex, int button, ClickType clickType) {
        if (usesFoundation()) {
            if (cavityPress) return;
            int logicalSlot = slot == null ? slotIndex : slot.index;
            if (foundationReady()) FourWeaponClientApi.menuClick(menu.containerId, logicalSlot, button, clickType);
            return;
        }
        super.slotClicked(slot, slotIndex, button, clickType);
    }

    private boolean usesFoundation() {
        return foundationMenu || FourWeaponFoundationAdapter.applies(menu.weapon());
    }

    private boolean foundationReady() {
        var snapshot = FourWeaponClientApi.snapshot();
        var authority = FourWeaponClientApi.authority();
        return FourWeaponClientApi.capable() && snapshot != null && authority != null
                && snapshot.session().equals(authority.session()) && snapshot.data().getInt("Container") == menu.containerId
                && authority.binding().weapon().equals(FoodWeaponLifecycle.weaponId(menu.weapon()));
    }
}
