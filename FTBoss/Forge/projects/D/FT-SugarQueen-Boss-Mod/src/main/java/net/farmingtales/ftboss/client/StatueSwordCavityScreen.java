package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** 独立大剑腔界面；2026-10-25 增加来源石像主题、分层嵌槽与主题控件，保留虚拟坐标和服务端确认。 */
public final class StatueSwordCavityScreen extends Screen implements StatueThemeSource {
    private static final int VIEW_WIDTH = 408;
    private static final int VIEW_HEIGHT = 426;
    private static final int GRID_X = 60;
    private static final int CAVITY_Y = 68;
    private static final int BACKPACK_Y = 230;
    private static final int HOTBAR_Y = 334;
    private static final int BUTTON_Y = 382;
    private static final int ACTION_MOVE = 0;
    private static final int ACTION_REMOVE = 1;
    private final StatueHubScreen parent;
    private final BlockPos statuePos;
    private final Supplier<ItemStack> weaponSupplier;
    private final StatueSwordCavityPanel panel = new StatueSwordCavityPanel();
    private final StatueUiTheme theme;
    private final List<Button> actions = new ArrayList<>();
    private Slot selected;
    private Slot destination;
    private int inventorySlot = -1;
    private ItemStack weaponSnapshot = ItemStack.EMPTY;
    private ItemStack ingredientSnapshot = ItemStack.EMPTY;
    private List<Piece> nativeParts = List.of();
    private Object recipeSnapshot;
    private boolean actionPending;
    private float viewScale = 1.0F;
    private double viewLeft;
    private double viewTop;

    public StatueSwordCavityScreen(StatueHubScreen parent, BlockPos statuePos, Supplier<ItemStack> weaponSupplier) {
        super(Component.translatable("screen.ftboss.cavity.title"));
        this.parent = parent;
        this.statuePos = statuePos.immutable();
        this.theme = StatueUiTheme.forStatue(this.statuePos);
        this.weaponSupplier = weaponSupplier;
    }

    @Override
    public StatueUiTheme statueTheme() { return this.theme; }

    StatueHubScreen parent() { return this.parent; }

    public boolean matches(BlockPos pos) { return this.statuePos.equals(pos); }

    public void acknowledgeAction() {
        this.actionPending = false;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.EMPTY;
        this.destination = null;
        refreshSelection();
    }

    @Override
    protected void init() {
        this.viewScale = Math.max(0.01F, Math.min((this.width - 12.0F) / VIEW_WIDTH, (this.height - 12.0F) / VIEW_HEIGHT));
        this.viewLeft = (this.width - VIEW_WIDTH * this.viewScale) / 2.0;
        this.viewTop = (this.height - VIEW_HEIGHT * this.viewScale) / 2.0;
        this.actions.clear();
        action("back", 12, 60, this::onClose);
        action("bind", 78, 66, this::bind);
        action("insert", 150, 84, this::insert);
        action("move", 240, 74, this::move);
        action("remove", 320, 76, this::remove);
        refreshSelection();
        updateActions(weapon());
    }

    private void action(String key, int left, int width, Runnable callback) {
        this.actions.add(addRenderableWidget(StatueThemeButton.create(Component.translatable("screen.ftboss.cavity." + key),
                button -> callback.run(), left, BUTTON_Y, width, 22, this::statueTheme)));
    }

    private void updateActions(ItemStack current) {
        this.actions.get(0).active = !this.actionPending;
        this.actions.get(1).active = available(current) && FoodWeaponSkillLoadout.canBind(current);
        this.actions.get(2).active = canInsert(current);
        this.actions.get(3).active = canMove(current);
        this.actions.get(4).active = canRemove(current);
    }

    private ItemStack weapon() {
        ItemStack current = this.weaponSupplier.get();
        return SwordCavityBuild.applies(current) ? current : ItemStack.EMPTY;
    }

    private ItemStack inventoryItem(int slot) {
        return this.minecraft == null || this.minecraft.player == null || slot < 0 || slot >= 36
                ? ItemStack.EMPTY : this.minecraft.player.getInventory().getItem(slot);
    }

    private void refreshSelection() {
        ItemStack current = weapon();
        Object recipes = this.minecraft == null || this.minecraft.level == null ? null : this.minecraft.level.getRecipeManager();
        if (!ItemStack.matches(current, this.weaponSnapshot) || recipes != this.recipeSnapshot) {
            this.nativeParts = SwordNativeMaterials.parts(current, this.minecraft == null ? null : this.minecraft.level);
            this.recipeSnapshot = recipes;
        }
        if (!ItemStack.matches(current, this.weaponSnapshot)) {
            this.selected = null;
            this.destination = null;
            this.inventorySlot = -1;
            this.ingredientSnapshot = ItemStack.EMPTY;
            this.weaponSnapshot = current.copy();
        }
        if (this.inventorySlot >= 0 && !ItemStack.matches(inventoryItem(this.inventorySlot), this.ingredientSnapshot)) {
            this.inventorySlot = -1;
            this.ingredientSnapshot = ItemStack.EMPTY;
        }
    }

    private boolean available(ItemStack current) {
        return !this.actionPending && !current.isEmpty() && this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.isAlive() && !this.minecraft.player.isSpectator();
    }

    private boolean canInsert(ItemStack current) {
        return available(current) && FoodWeaponLifecycle.weaponId(current) != null && this.selected != null
                && this.inventorySlot >= 0 && !ItemStack.matches(current, inventoryItem(this.inventorySlot))
                && WeaponMaterialBuild.materialFor(inventoryItem(this.inventorySlot)) != Material.ORIGINAL
                && SwordCavityBuild.canReplace(current, this.selected, WeaponMaterialBuild.materialFor(inventoryItem(this.inventorySlot)));
    }

    private boolean canMove(ItemStack current) {
        return available(current) && FoodWeaponLifecycle.weaponId(current) != null && this.selected != null
                && this.destination != null && this.destination != this.selected
                && SwordCavityBuild.canMove(current, this.selected, this.destination);
    }

    private boolean canRemove(ItemStack current) {
        return available(current) && FoodWeaponLifecycle.weaponId(current) != null && this.selected != null
                && SwordCavityBuild.canRemove(current, this.selected);
    }

    private void requestStarted() {
        this.actionPending = true;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.EMPTY;
        this.destination = null;
    }

    private void insert() {
        refreshSelection();
        ItemStack current = weapon();
        if (canInsert(current) && StatueMenuNetwork.CavityAction.send(this.statuePos, this.selected,
                this.inventorySlot, current, inventoryItem(this.inventorySlot))) requestStarted();
    }

    private void move() {
        refreshSelection();
        ItemStack current = weapon();
        if (canMove(current) && StatueMenuNetwork.CavityLayoutAction.send(this.statuePos, ACTION_MOVE,
                this.selected, this.destination, current)) requestStarted();
    }

    private void remove() {
        refreshSelection();
        ItemStack current = weapon();
        if (canRemove(current) && StatueMenuNetwork.CavityLayoutAction.send(this.statuePos, ACTION_REMOVE,
                this.selected, this.selected, current)) requestStarted();
    }

    private void bind() {
        ItemStack current = weapon();
        if (!available(current) || !FoodWeaponSkillLoadout.canBind(current)) return;
        StatueMenuNetwork.Action.send(this.statuePos, StatueMenuNetwork.ACTION_BIND_WEAPON);
        requestStarted();
    }

    @Override
    public void tick() {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.level == null) return;
        if (this.minecraft.player.distanceToSqr(this.statuePos.getX() + 0.5, this.statuePos.getY() + 1.0,
                this.statuePos.getZ() + 0.5) > 64.0 || !(this.minecraft.level.getBlockState(this.statuePos).getBlock()
                instanceof net.farmingtales.ftboss.block.CandyStatueBlock)) {
            this.minecraft.setScreen(null);
            return;
        }
        refreshSelection();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (!this.actionPending && this.minecraft != null) this.minecraft.setScreen(this.parent);
    }

    private double localX(double mouseX) { return (mouseX - this.viewLeft) / this.viewScale; }
    private double localY(double mouseY) { return (mouseY - this.viewTop) / this.viewScale; }

    private int inventoryAt(double mouseX, double mouseY) {
        int pitch = StatueSwordCavityPanel.PITCH;
        if (StatueSwordCavityPanel.inside(mouseX, mouseY, GRID_X, BACKPACK_Y, 9 * pitch, 3 * pitch)) {
            return 9 + (int) ((mouseY - BACKPACK_Y) / pitch) * 9 + (int) ((mouseX - GRID_X) / pitch);
        }
        if (StatueSwordCavityPanel.inside(mouseX, mouseY, GRID_X, HOTBAR_Y, 9 * pitch, pitch)) {
            return (int) ((mouseX - GRID_X) / pitch);
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        refreshSelection();
        if (button != 0 || this.actionPending) return true;
        double localX = localX(mouseX);
        double localY = localY(mouseY);
        ItemStack current = weapon();
        updateActions(current);
        if (super.mouseClicked(localX, localY, button)) return true;
        for (Button action : this.actions) {
            if (StatueSwordCavityPanel.inside(localX, localY, action.getX(), action.getY(), action.getWidth(), action.getHeight())) return true;
        }
        if (!available(current)) return true;
        Slot clicked = this.panel.hit(localX, localY, GRID_X, CAVITY_Y);
        if (clicked != null) {
            if (this.selected != null && this.selected != clicked && this.inventorySlot < 0
                    && this.destination != clicked && SwordCavityBuild.cells(current).get(this.selected.ordinal()).installed()) this.destination = clicked;
            else { this.selected = clicked; this.destination = null; }
            return true;
        }
        int clickedInventory = inventoryAt(localX, localY);
        if (clickedInventory >= 0) {
            ItemStack ingredient = inventoryItem(clickedInventory);
            this.inventorySlot = !ingredient.isEmpty() && WeaponMaterialBuild.materialFor(ingredient) != Material.ORIGINAL
                    && !ItemStack.matches(current, ingredient) ? clickedInventory : -1;
            this.ingredientSnapshot = this.inventorySlot < 0 ? ItemStack.EMPTY : ingredient.copy();
            this.destination = null;
            return true;
        }
        this.selected = null;
        this.destination = null;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.EMPTY;
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshSelection();
        this.renderBackground(graphics);
        double localX = localX(mouseX);
        double localY = localY(mouseY);
        ItemStack current = weapon();
        List<Component> hover = List.of();
        graphics.pose().pushPose();
        graphics.pose().translate(this.viewLeft, this.viewTop, 0);
        graphics.pose().scale(this.viewScale, this.viewScale, 1.0F);
        StatueThemeRenderer.frame(graphics, 0, 0, VIEW_WIDTH, VIEW_HEIGHT, this.theme);
        StatueUiDecor.gear(graphics, 12, 7, 18);
        graphics.drawCenteredString(this.font, this.title, VIEW_WIDTH / 2, 12, this.theme.accentBright);
        StatueThemeRenderer.line(graphics, 38, 25, VIEW_WIDTH - 50, this.theme);
        StatueThemeRenderer.card(graphics, 9, 29, VIEW_WIDTH - 18, 35, this.theme, false);
        StatueThemeRenderer.card(graphics, GRID_X - 6, CAVITY_Y - 3, 300, 112, this.theme, false);
        StatueThemeRenderer.card(graphics, GRID_X - 6, 181, 300, 27, this.theme, false);
        StatueThemeRenderer.card(graphics, GRID_X - 6, 227, 300, 140, this.theme, false);
        if (!current.isEmpty()) {
            StatueSwordCavityPanel.item(graphics, this.font, current, 12, 30, false, false, this.theme);
            clipped(graphics, current.getHoverName(), 50, 35, 264, this.theme.text);
            Component status = Component.translatable(FoodWeaponSkillLoadout.canBind(current) ? "screen.ftboss.cavity.unbound"
                    : SwordCavityBuild.missingRequired(current) ? "screen.ftboss.cavity.missing"
                    : FoodWeaponLifecycle.isBroken(current) ? "screen.ftboss.cavity.depleted" : "screen.ftboss.cavity.ready");
            clipped(graphics, status, 50, 49, 264, this.theme.muted);
            hover = this.panel.render(graphics, this.font, current, this.selected, this.destination,
                    GRID_X, CAVITY_Y, localX, localY, this.nativeParts, this.theme);
            if (StatueSwordCavityPanel.inside(localX, localY, 12, 30, 302, 32)) hover = List.of(current.getHoverName(), status);
            var links = SwordCavityBuild.links(current);
            StatueThemeRenderer.card(graphics, 326, 35, 70, 22, this.theme, true);
            clipped(graphics, Component.translatable("screen.ftboss.cavity.links", links.size()), 331, 42, 60, this.theme.accentBright);
            if (StatueSwordCavityPanel.inside(localX, localY, 326, 35, 70, 22)) {
                List<Component> linkLines = new ArrayList<>();
                linkLines.add(Component.translatable("screen.ftboss.cavity.links", links.size()));
                for (var link : links) linkLines.add(Component.translatable("link.ftboss.sword.detail." + link.name().toLowerCase(Locale.ROOT)));
                if (links.isEmpty()) linkLines.add(Component.translatable("screen.ftboss.cavity.no_links"));
                hover = List.copyOf(linkLines);
            }
        } else {
            graphics.drawCenteredString(this.font, Component.translatable("screen.ftboss.cavity.no_sword"),
                    VIEW_WIDTH / 2, 110, this.theme.muted);
        }
        Component legend = Component.translatable("screen.ftboss.cavity.native_legend");
        clipped(graphics, legend, GRID_X, 170, 288, this.theme.muted);
        if (StatueSwordCavityPanel.inside(localX, localY, GRID_X, 168, 288, 13)) hover = List.of(legend);
        List<Component> effects = selectedEffects(current);
        List<net.minecraft.util.FormattedCharSequence> effectLines = this.font.split(joinEffects(effects), 288);
        for (int index = 0; index < Math.min(2, effectLines.size()); index++) graphics.drawString(this.font,
                effectLines.get(index), GRID_X, 184 + index * 12, this.theme.text, false);
        if (StatueSwordCavityPanel.inside(localX, localY, GRID_X, 182, 288, 26)) hover = effects;
        graphics.drawString(this.font, Component.translatable("screen.ftboss.cavity.inventory"),
                GRID_X, 216, this.theme.muted, false);
        for (int slot = 0; slot < 36; slot++) {
            int column = slot < 9 ? slot : (slot - 9) % 9;
            int row = slot < 9 ? 0 : (slot - 9) / 9;
            int top = slot < 9 ? HOTBAR_Y : BACKPACK_Y + row * StatueSwordCavityPanel.PITCH;
            int left = GRID_X + column * StatueSwordCavityPanel.PITCH;
            ItemStack ingredient = inventoryItem(slot);
            boolean invalid = WeaponMaterialBuild.materialFor(ingredient) == Material.ORIGINAL || ItemStack.matches(current, ingredient);
            StatueSwordCavityPanel.item(graphics, this.font, ingredient, left, top, slot == this.inventorySlot, invalid, this.theme);
        }
        int hoveredInventory = inventoryAt(localX, localY);
        if (hoveredInventory >= 0 && !inventoryItem(hoveredInventory).isEmpty()) {
            ItemStack ingredient = inventoryItem(hoveredInventory);
            Material material = WeaponMaterialBuild.materialFor(ingredient);
            List<Component> inventoryHover = new ArrayList<>();
            inventoryHover.add(ingredient.getHoverName());
            if (material != Material.ORIGINAL && !ItemStack.matches(current, ingredient)) {
                inventoryHover.addAll(SwordCavityBuild.materialEffects(material));
            } else inventoryHover.add(Component.translatable("screen.ftboss.cavity.not_material"));
            hover = List.copyOf(inventoryHover);
        }
        updateActions(current);
        for (Button action : this.actions) action.render(graphics, (int) localX, (int) localY, partialTick);
        if (insideButton(localX, localY, 12, 60)) hover = List.of(Component.translatable("screen.ftboss.cavity.back_hint"));
        if (insideButton(localX, localY, 78, 66)) hover = List.of(Component.translatable("screen.ftboss.cavity.bind_hint"));
        if (insideButton(localX, localY, 150, 84)) {
            List<Component> insertHover = new ArrayList<>(List.of(Component.translatable("screen.ftboss.cavity.insert_hint")));
            Material material = WeaponMaterialBuild.materialFor(inventoryItem(this.inventorySlot));
            if (!current.isEmpty() && this.selected != null && material != Material.ORIGINAL) {
                String failure = SwordCavityBuild.replacementFailure(current, this.selected, material);
                if (!failure.isEmpty()) insertHover.add(Component.translatable(failure));
            }
            hover = List.copyOf(insertHover);
        }
        if (insideButton(localX, localY, 240, 74)) hover = List.of(Component.translatable("screen.ftboss.cavity.move_hint"));
        if (insideButton(localX, localY, 320, 76)) hover = List.of(Component.translatable("screen.ftboss.cavity.remove_hint"));
        Component footer = Component.translatable(this.actionPending ? "screen.ftboss.cavity.pending"
                : !current.isEmpty() && !FoodWeaponSkillLoadout.canBind(current) && FoodWeaponLifecycle.weaponId(current) == null
                        ? "screen.ftboss.cavity.tracking" : "screen.ftboss.cavity.select_hint");
        StatueThemeRenderer.line(graphics, 12, 408, VIEW_WIDTH - 24, this.theme);
        clipped(graphics, footer, 12, 412, VIEW_WIDTH - 24, this.theme.muted);
        if (StatueSwordCavityPanel.inside(localX, localY, 12, 410, VIEW_WIDTH - 24, 14)) hover = List.of(footer);
        graphics.pose().popPose();
        if (!hover.isEmpty()) graphics.renderComponentTooltip(this.font, hover, mouseX, mouseY);
    }

    private List<Component> selectedEffects(ItemStack current) {
        if (this.inventorySlot >= 0) {
            Material material = WeaponMaterialBuild.materialFor(inventoryItem(this.inventorySlot));
            return SwordCavityBuild.materialEffects(material);
        }
        if (!current.isEmpty() && this.selected != null) {
            var cell = SwordCavityBuild.cells(current, this.nativeParts).get(this.selected.ordinal());
            if (cell.installed()) return SwordCavityBuild.materialEffects(cell.material());
            if (cell.present()) return List.of(Component.translatable("screen.ftboss.sword.native_no_bonus"));
            return StatueSwordCavityPanel.tooltip(current, this.selected, this.nativeParts);
        }
        return List.of(Component.translatable("screen.ftboss.cavity.select_hint"));
    }

    private Component joinEffects(List<Component> effects) {
        var text = Component.empty();
        for (int index = 0; index < effects.size(); index++) {
            if (index > 0) text.append(Component.literal(" · "));
            text.append(effects.get(index));
        }
        return text;
    }

    private void clipped(GuiGraphics graphics, Component text, int left, int top, int width, int color) {
        graphics.drawString(this.font, this.font.plainSubstrByWidth(text.getString(), width), left, top, color, false);
    }

    private boolean insideButton(double mouseX, double mouseY, int left, int width) {
        return StatueSwordCavityPanel.inside(mouseX, mouseY, left, BUTTON_Y, width, 22);
    }
}
