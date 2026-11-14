package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.network.StatueMenuNetwork.Action;
import net.farmingtales.ftboss.network.NativeStatueCavityNetwork.CavityAction;
import net.farmingtales.ftboss.network.NativeStatueCavityNetwork.CavityLayoutAction;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public final class StatueSwordCavityScreen extends Screen {
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

    public StatueSwordCavityScreen(StatueHubScreen var1, BlockPos var2, Supplier<ItemStack> var3) {
        super(Component.translatable("screen.ftboss.cavity.title"));
        this.parent = var1;
        this.statuePos = var2.immutable();
        this.weaponSupplier = var3;
    }

    StatueHubScreen parent() {
        return this.parent;
    }

    public boolean matches(BlockPos var1) {
        return this.statuePos.equals(var1);
    }

    public void acknowledgeAction() {
        this.actionPending = false;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.EMPTY;
        this.destination = null;
        this.refreshSelection();
    }

    @Override
    protected void init() {
        this.viewScale = Math.max(0.01F, Math.min(((float)this.width - 12.0F) / 408.0F, ((float)this.height - 12.0F) / 426.0F));
        this.viewLeft = (double)((float)this.width - 408.0F * this.viewScale) / 2.0;
        this.viewTop = (double)((float)this.height - 426.0F * this.viewScale) / 2.0;
        this.refreshSelection();
    }

    private ItemStack weapon() {
        ItemStack var1 = this.weaponSupplier.get();
        return SwordCavityBuild.applies(var1) ? var1 : ItemStack.EMPTY;
    }

    private ItemStack inventoryItem(int var1) {
        return this.minecraft != null && this.minecraft.player != null && var1 >= 0 && var1 < 36
            ? this.minecraft.player.getInventory().getItem(var1)
            : ItemStack.EMPTY;
    }

    private void refreshSelection() {
        ItemStack var1 = this.weapon();
        RecipeManager var2 = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getRecipeManager() : null;
        if (!ItemStack.matches(var1, this.weaponSnapshot) || var2 != this.recipeSnapshot) {
            this.nativeParts = SwordNativeMaterials.parts(var1, this.minecraft == null ? null : this.minecraft.level);
            this.recipeSnapshot = var2;
        }

        if (!ItemStack.matches(var1, this.weaponSnapshot)) {
            this.selected = null;
            this.destination = null;
            this.inventorySlot = -1;
            this.ingredientSnapshot = ItemStack.EMPTY;
            this.weaponSnapshot = var1.copy();
        }

        if (this.inventorySlot >= 0 && !ItemStack.matches(this.inventoryItem(this.inventorySlot), this.ingredientSnapshot)) {
            this.inventorySlot = -1;
            this.ingredientSnapshot = ItemStack.EMPTY;
        }
    }

    private boolean available(ItemStack var1) {
        return !this.actionPending
            && !var1.isEmpty()
            && this.minecraft != null
            && this.minecraft.player != null
            && this.minecraft.player.isAlive()
            && !this.minecraft.player.isSpectator();
    }

    private boolean canInsert(ItemStack var1) {
        return this.available(var1)
            && FoodWeaponLifecycle.weaponId(var1) != null
            && this.selected != null
            && this.inventorySlot >= 0
            && !ItemStack.matches(var1, this.inventoryItem(this.inventorySlot))
            && WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot)) != Material.ORIGINAL
            && SwordCavityBuild.canReplace(var1, this.selected, WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot)));
    }

    private boolean canMove(ItemStack var1) {
        return this.available(var1)
            && FoodWeaponLifecycle.weaponId(var1) != null
            && this.selected != null
            && this.destination != null
            && this.destination != this.selected
            && SwordCavityBuild.canMove(var1, this.selected, this.destination);
    }

    private boolean canRemove(ItemStack var1) {
        return this.available(var1) && FoodWeaponLifecycle.weaponId(var1) != null && this.selected != null && SwordCavityBuild.canRemove(var1, this.selected);
    }

    private void requestStarted() {
        this.actionPending = true;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.EMPTY;
        this.destination = null;
    }

    private void insert() {
        this.refreshSelection();
        ItemStack var1 = this.weapon();
        if (this.canInsert(var1) && CavityAction.send(this.statuePos, this.selected, this.inventorySlot, var1, this.inventoryItem(this.inventorySlot))) {
            this.requestStarted();
        }
    }

    private void move() {
        this.refreshSelection();
        ItemStack var1 = this.weapon();
        if (this.canMove(var1) && CavityLayoutAction.send(this.statuePos, 0, this.selected, this.destination, var1)) {
            this.requestStarted();
        }
    }

    private void remove() {
        this.refreshSelection();
        ItemStack var1 = this.weapon();
        if (this.canRemove(var1) && CavityLayoutAction.send(this.statuePos, 1, this.selected, this.selected, var1)) {
            this.requestStarted();
        }
    }

    private void bind() {
        ItemStack var1 = this.weapon();
        if (this.available(var1) && FoodWeaponSkillLoadout.canBind(var1)) {
            Action.send(this.statuePos, 11);
            this.requestStarted();
        }
    }

    @Override
    public void tick() {
        if (this.minecraft != null && this.minecraft.player != null && this.minecraft.level != null) {
            if (!(
                    this.minecraft
                            .player
                            .distanceToSqr((double)this.statuePos.getX() + 0.5, (double)this.statuePos.getY() + 1.0, (double)this.statuePos.getZ() + 0.5)
                        > 64.0
                )
                && this.minecraft.level.getBlockState(this.statuePos).getBlock() instanceof CandyStatueBlock) {
                this.refreshSelection();
            } else {
                this.minecraft.setScreen(null);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!this.actionPending && this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private double localX(double var1) {
        return (var1 - this.viewLeft) / (double)this.viewScale;
    }

    private double localY(double var1) {
        return (var1 - this.viewTop) / (double)this.viewScale;
    }

    private int inventoryAt(double var1, double var3) {
        byte var5 = 32;
        if (StatueSwordCavityPanel.inside(var1, var3, 60, 230, 9 * var5, 3 * var5)) {
            return 9 + (int)((var3 - 230.0) / (double)var5) * 9 + (int)((var1 - 60.0) / (double)var5);
        } else {
            return StatueSwordCavityPanel.inside(var1, var3, 60, 334, 9 * var5, var5) ? (int)((var1 - 60.0) / (double)var5) : -1;
        }
    }

    @Override
    public boolean mouseClicked(double var1, double var3, int var5) {
        this.refreshSelection();
        if (var5 == 0 && !this.actionPending) {
            double var6 = this.localX(var1);
            double var8 = this.localY(var3);
            ItemStack var10 = this.weapon();
            if (this.insideButton(var6, var8, 12, 60)) {
                this.onClose();
                return true;
            } else if (this.insideButton(var6, var8, 78, 66)) {
                this.bind();
                return true;
            } else if (this.insideButton(var6, var8, 150, 84)) {
                this.insert();
                return true;
            } else if (this.insideButton(var6, var8, 240, 74)) {
                this.move();
                return true;
            } else if (this.insideButton(var6, var8, 320, 76)) {
                this.remove();
                return true;
            } else if (!this.available(var10)) {
                return true;
            } else {
                Slot var11 = this.panel.hit(var6, var8, 60, 68);
                if (var11 != null) {
                    if (this.selected != null
                        && this.selected != var11
                        && this.inventorySlot < 0
                        && this.destination != var11
                        && ((Cell)SwordCavityBuild.cells(var10).get(this.selected.ordinal())).installed()) {
                        this.destination = var11;
                    } else {
                        this.selected = var11;
                        this.destination = null;
                    }

                    return true;
                } else {
                    int var12 = this.inventoryAt(var6, var8);
                    if (var12 < 0) {
                        this.selected = null;
                        this.destination = null;
                        this.inventorySlot = -1;
                        this.ingredientSnapshot = ItemStack.EMPTY;
                        return true;
                    } else {
                        ItemStack var13 = this.inventoryItem(var12);
                        this.inventorySlot = !var13.isEmpty()
                                && WeaponMaterialBuild.materialFor(var13) != Material.ORIGINAL
                                && !ItemStack.matches(var10, var13)
                            ? var12
                            : -1;
                        this.ingredientSnapshot = this.inventorySlot < 0 ? ItemStack.EMPTY : var13.copy();
                        this.destination = null;
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        this.refreshSelection();
        this.renderBackground(var1, var2, var3, var4);
        double var5 = this.localX((double)var2);
        double var7 = this.localY((double)var3);
        ItemStack var9 = this.weapon();
        List var10 = List.of();
        var1.pose().pushPose();
        var1.pose().translate(this.viewLeft, this.viewTop, 0.0);
        var1.pose().scale(this.viewScale, this.viewScale, 1.0F);
        var1.fill(0, 0, 408, 426, -233039580);
        var1.fill(2, 2, 406, 424, -232118734);
        var1.drawCenteredString(this.font, this.title, 204, 12, -1515055);
        if (!var9.isEmpty()) {
            StatueSwordCavityPanel.item(var1, this.font, var9, 12, 30, false, false);
            this.clipped(var1, var9.getHoverName(), 50, 35, 264, -1515055);
            MutableComponent var11 = Component.translatable(
                FoodWeaponSkillLoadout.canBind(var9)
                    ? "screen.ftboss.cavity.unbound"
                    : (
                        SwordCavityBuild.missingRequired(var9)
                            ? "screen.ftboss.cavity.missing"
                            : (FoodWeaponLifecycle.isBroken(var9) ? "screen.ftboss.cavity.depleted" : "screen.ftboss.cavity.ready")
                    )
            );
            this.clipped(var1, var11, 50, 49, 264, -5594189);
            var10 = this.panel.render(var1, this.font, var9, this.selected, this.destination, 60, 68, var5, var7, this.nativeParts);
            if (StatueSwordCavityPanel.inside(var5, var7, 12, 30, 302, 32)) {
                var10 = List.of(var9.getHoverName(), var11);
            }

            List<Link> var12 = SwordCavityBuild.links(var9);
            var1.fill(326, 35, 396, 57, -12438714);
            this.clipped(var1, Component.translatable("screen.ftboss.cavity.links", var12.size()), 331, 42, 60, -1917592);
            if (StatueSwordCavityPanel.inside(var5, var7, 326, 35, 70, 22)) {
                ArrayList var13 = new ArrayList();
                var13.add(Component.translatable("screen.ftboss.cavity.links", var12.size()));

                for (Link var15 : var12) {
                    var13.add(Component.translatable("link.ftboss.sword.detail." + var15.name().toLowerCase(Locale.ROOT)));
                }

                if (var12.isEmpty()) {
                    var13.add(Component.translatable("screen.ftboss.cavity.no_links"));
                }

                var10 = List.copyOf(var13);
            }
        } else {
            var1.drawCenteredString(this.font, Component.translatable("screen.ftboss.cavity.no_sword"), 204, 110, -5594189);
        }

        MutableComponent var21 = Component.translatable("screen.ftboss.cavity.native_legend");
        this.clipped(var1, var21, 60, 170, 288, -5594189);
        if (StatueSwordCavityPanel.inside(var5, var7, 60, 168, 288, 13)) {
            var10 = List.of(var21);
        }

        List var22 = this.selectedEffects(var9);
        List var23 = this.font.split(this.joinEffects(var22), 288);

        for (int var24 = 0; var24 < Math.min(2, var23.size()); var24++) {
            var1.drawString(this.font, (FormattedCharSequence)var23.get(var24), 60, 184 + var24 * 12, -1515055, false);
        }

        if (StatueSwordCavityPanel.inside(var5, var7, 60, 182, 288, 26)) {
            var10 = var22;
        }

        var1.drawString(this.font, Component.translatable("screen.ftboss.cavity.inventory"), 60, 216, -5594189, false);

        for (int var25 = 0; var25 < 36; var25++) {
            int var27 = var25 < 9 ? var25 : (var25 - 9) % 9;
            int var16 = var25 < 9 ? 0 : (var25 - 9) / 9;
            int var17 = var25 < 9 ? 334 : 230 + var16 * 32;
            int var18 = 60 + var27 * 32;
            ItemStack var19 = this.inventoryItem(var25);
            boolean var20 = WeaponMaterialBuild.materialFor(var19) == Material.ORIGINAL || ItemStack.matches(var9, var19);
            StatueSwordCavityPanel.item(var1, this.font, var19, var18, var17, var25 == this.inventorySlot, var20);
        }

        int var26 = this.inventoryAt(var5, var7);
        if (var26 >= 0 && !this.inventoryItem(var26).isEmpty()) {
            ItemStack var28 = this.inventoryItem(var26);
            Material var31 = WeaponMaterialBuild.materialFor(var28);
            ArrayList var33 = new ArrayList();
            var33.add(var28.getHoverName());
            if (var31 != Material.ORIGINAL && !ItemStack.matches(var9, var28)) {
                var33.addAll(SwordCavityBuild.materialEffects(var31));
            } else {
                var33.add(Component.translatable("screen.ftboss.cavity.not_material"));
            }

            var10 = List.copyOf(var33);
        }

        this.drawButton(var1, 12, 60, "back", !this.actionPending);
        this.drawButton(var1, 78, 66, "bind", this.available(var9) && FoodWeaponSkillLoadout.canBind(var9));
        this.drawButton(var1, 150, 84, "insert", this.canInsert(var9));
        this.drawButton(var1, 240, 74, "move", this.canMove(var9));
        this.drawButton(var1, 320, 76, "remove", this.canRemove(var9));
        if (this.insideButton(var5, var7, 12, 60)) {
            var10 = List.of(Component.translatable("screen.ftboss.cavity.back_hint"));
        }

        if (this.insideButton(var5, var7, 78, 66)) {
            var10 = List.of(Component.translatable("screen.ftboss.cavity.bind_hint"));
        }

        if (this.insideButton(var5, var7, 150, 84)) {
            ArrayList var29 = new ArrayList<>(List.of(Component.translatable("screen.ftboss.cavity.insert_hint")));
            Material var32 = WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot));
            if (!var9.isEmpty() && this.selected != null && var32 != Material.ORIGINAL) {
                String var34 = SwordCavityBuild.replacementFailure(var9, this.selected, var32);
                if (!var34.isEmpty()) {
                    var29.add(Component.translatable(var34));
                }
            }

            var10 = List.copyOf(var29);
        }

        if (this.insideButton(var5, var7, 240, 74)) {
            var10 = List.of(Component.translatable("screen.ftboss.cavity.move_hint"));
        }

        if (this.insideButton(var5, var7, 320, 76)) {
            var10 = List.of(Component.translatable("screen.ftboss.cavity.remove_hint"));
        }

        MutableComponent var30 = Component.translatable(
            this.actionPending
                ? "screen.ftboss.cavity.pending"
                : (
                    !var9.isEmpty() && !FoodWeaponSkillLoadout.canBind(var9) && FoodWeaponLifecycle.weaponId(var9) == null
                        ? "screen.ftboss.cavity.tracking"
                        : "screen.ftboss.cavity.select_hint"
                )
        );
        this.clipped(var1, var30, 12, 412, 384, -5594189);
        if (StatueSwordCavityPanel.inside(var5, var7, 12, 410, 384, 14)) {
            var10 = List.of(var30);
        }

        var1.pose().popPose();
        if (!var10.isEmpty()) {
            var1.renderComponentTooltip(this.font, var10, var2, var3);
        }
    }

    private List<Component> selectedEffects(ItemStack var1) {
        if (this.inventorySlot >= 0) {
            Material var3 = WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot));
            return SwordCavityBuild.materialEffects(var3);
        } else if (!var1.isEmpty() && this.selected != null) {
            Cell var2 = (Cell)SwordCavityBuild.cells(var1, this.nativeParts).get(this.selected.ordinal());
            if (var2.installed()) {
                return SwordCavityBuild.materialEffects(var2.material());
            } else {
                return var2.present()
                    ? List.of(Component.translatable("screen.ftboss.sword.native_no_bonus"))
                    : StatueSwordCavityPanel.tooltip(var1, this.selected, this.nativeParts);
            }
        } else {
            return List.of(Component.translatable("screen.ftboss.cavity.select_hint"));
        }
    }

    private Component joinEffects(List<Component> var1) {
        MutableComponent var2 = Component.empty();

        for (int var3 = 0; var3 < var1.size(); var3++) {
            if (var3 > 0) {
                var2.append(Component.literal(" · "));
            }

            var2.append((Component)var1.get(var3));
        }

        return var2;
    }

    private void clipped(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6) {
        var1.drawString(this.font, this.font.plainSubstrByWidth(var2.getString(), var5), var3, var4, var6, false);
    }

    private void drawButton(GuiGraphics var1, int var2, int var3, String var4, boolean var5) {
        var1.fill(var2, 382, var2 + var3, 404, var5 ? -10071699 : -13225667);
        var1.drawCenteredString(this.font, Component.translatable("screen.ftboss.cavity." + var4), var2 + var3 / 2, 389, var5 ? -1515055 : -8949377);
    }

    private boolean insideButton(double var1, double var3, int var5, int var6) {
        return StatueSwordCavityPanel.inside(var1, var3, var5, 382, var6, 22);
    }
}
