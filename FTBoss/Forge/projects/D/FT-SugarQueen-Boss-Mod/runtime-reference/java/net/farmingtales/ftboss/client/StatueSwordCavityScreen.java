/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.RecipeManager
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.client.StatueSwordCavityPanel;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public final class StatueSwordCavityScreen
extends Screen {
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
    private SwordCavityRules.Slot selected;
    private SwordCavityRules.Slot destination;
    private int inventorySlot = -1;
    private ItemStack weaponSnapshot = ItemStack.f_41583_;
    private ItemStack ingredientSnapshot = ItemStack.f_41583_;
    private List<SwordNativeMaterialRules.Piece> nativeParts = List.of();
    private Object recipeSnapshot;
    private boolean actionPending;
    private float viewScale = 1.0f;
    private double viewLeft;
    private double viewTop;

    public StatueSwordCavityScreen(StatueHubScreen statueHubScreen, BlockPos blockPos, Supplier<ItemStack> supplier) {
        super((Component)Component.m_237115_((String)"screen.ftboss.cavity.title"));
        this.parent = statueHubScreen;
        this.statuePos = blockPos.m_7949_();
        this.weaponSupplier = supplier;
    }

    StatueHubScreen parent() {
        return this.parent;
    }

    public boolean matches(BlockPos blockPos) {
        return this.statuePos.equals((Object)blockPos);
    }

    public void acknowledgeAction() {
        this.actionPending = false;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.f_41583_;
        this.destination = null;
        this.refreshSelection();
    }

    protected void m_7856_() {
        this.viewScale = Math.max(0.01f, Math.min(((float)this.f_96543_ - 12.0f) / 408.0f, ((float)this.f_96544_ - 12.0f) / 426.0f));
        this.viewLeft = (double)((float)this.f_96543_ - 408.0f * this.viewScale) / 2.0;
        this.viewTop = (double)((float)this.f_96544_ - 426.0f * this.viewScale) / 2.0;
        this.refreshSelection();
    }

    private ItemStack weapon() {
        ItemStack itemStack = this.weaponSupplier.get();
        return SwordCavityBuild.applies(itemStack) ? itemStack : ItemStack.f_41583_;
    }

    private ItemStack inventoryItem(int n) {
        return this.f_96541_ == null || this.f_96541_.f_91074_ == null || n < 0 || n >= 36 ? ItemStack.f_41583_ : this.f_96541_.f_91074_.m_150109_().m_8020_(n);
    }

    private void refreshSelection() {
        RecipeManager recipeManager;
        ItemStack itemStack = this.weapon();
        RecipeManager recipeManager2 = recipeManager = this.f_96541_ == null || this.f_96541_.f_91073_ == null ? null : this.f_96541_.f_91073_.m_7465_();
        if (!ItemStack.m_41728_((ItemStack)itemStack, (ItemStack)this.weaponSnapshot) || recipeManager != this.recipeSnapshot) {
            this.nativeParts = SwordNativeMaterials.parts(itemStack, this.f_96541_ == null ? null : this.f_96541_.f_91073_);
            this.recipeSnapshot = recipeManager;
        }
        if (!ItemStack.m_41728_((ItemStack)itemStack, (ItemStack)this.weaponSnapshot)) {
            this.selected = null;
            this.destination = null;
            this.inventorySlot = -1;
            this.ingredientSnapshot = ItemStack.f_41583_;
            this.weaponSnapshot = itemStack.m_41777_();
        }
        if (this.inventorySlot >= 0 && !ItemStack.m_41728_((ItemStack)this.inventoryItem(this.inventorySlot), (ItemStack)this.ingredientSnapshot)) {
            this.inventorySlot = -1;
            this.ingredientSnapshot = ItemStack.f_41583_;
        }
    }

    private boolean available(ItemStack itemStack) {
        return !this.actionPending && !itemStack.m_41619_() && this.f_96541_ != null && this.f_96541_.f_91074_ != null && this.f_96541_.f_91074_.m_6084_() && !this.f_96541_.f_91074_.m_5833_();
    }

    private boolean canInsert(ItemStack itemStack) {
        return this.available(itemStack) && FoodWeaponLifecycle.weaponId(itemStack) != null && this.selected != null && this.inventorySlot >= 0 && !ItemStack.m_41728_((ItemStack)itemStack, (ItemStack)this.inventoryItem(this.inventorySlot)) && WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot)) != FoodWeaponBuildRules.Material.ORIGINAL && SwordCavityBuild.canReplace(itemStack, this.selected, WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot)));
    }

    private boolean canMove(ItemStack itemStack) {
        return this.available(itemStack) && FoodWeaponLifecycle.weaponId(itemStack) != null && this.selected != null && this.destination != null && this.destination != this.selected && SwordCavityBuild.canMove(itemStack, this.selected, this.destination);
    }

    private boolean canRemove(ItemStack itemStack) {
        return this.available(itemStack) && FoodWeaponLifecycle.weaponId(itemStack) != null && this.selected != null && SwordCavityBuild.canRemove(itemStack, this.selected);
    }

    private void requestStarted() {
        this.actionPending = true;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.f_41583_;
        this.destination = null;
    }

    private void insert() {
        this.refreshSelection();
        ItemStack itemStack = this.weapon();
        if (this.canInsert(itemStack) && StatueMenuNetwork.CavityAction.send(this.statuePos, this.selected, this.inventorySlot, itemStack, this.inventoryItem(this.inventorySlot))) {
            this.requestStarted();
        }
    }

    private void move() {
        this.refreshSelection();
        ItemStack itemStack = this.weapon();
        if (this.canMove(itemStack) && StatueMenuNetwork.CavityLayoutAction.send(this.statuePos, 0, this.selected, this.destination, itemStack)) {
            this.requestStarted();
        }
    }

    private void remove() {
        this.refreshSelection();
        ItemStack itemStack = this.weapon();
        if (this.canRemove(itemStack) && StatueMenuNetwork.CavityLayoutAction.send(this.statuePos, 1, this.selected, this.selected, itemStack)) {
            this.requestStarted();
        }
    }

    private void bind() {
        ItemStack itemStack = this.weapon();
        if (!this.available(itemStack) || !FoodWeaponSkillLoadout.canBind(itemStack)) {
            return;
        }
        StatueMenuNetwork.Action.send(this.statuePos, 11);
        this.requestStarted();
    }

    public void m_86600_() {
        if (this.f_96541_ == null || this.f_96541_.f_91074_ == null || this.f_96541_.f_91073_ == null) {
            return;
        }
        if (this.f_96541_.f_91074_.m_20275_((double)this.statuePos.m_123341_() + 0.5, (double)this.statuePos.m_123342_() + 1.0, (double)this.statuePos.m_123343_() + 0.5) > 64.0 || !(this.f_96541_.f_91073_.m_8055_(this.statuePos).m_60734_() instanceof CandyStatueBlock)) {
            this.f_96541_.m_91152_(null);
            return;
        }
        this.refreshSelection();
    }

    public boolean m_7043_() {
        return false;
    }

    public void m_7379_() {
        if (!this.actionPending && this.f_96541_ != null) {
            this.f_96541_.m_91152_((Screen)this.parent);
        }
    }

    private double localX(double d) {
        return (d - this.viewLeft) / (double)this.viewScale;
    }

    private double localY(double d) {
        return (d - this.viewTop) / (double)this.viewScale;
    }

    private int inventoryAt(double d, double d2) {
        int n = 32;
        if (StatueSwordCavityPanel.inside(d, d2, 60, 230, 9 * n, 3 * n)) {
            return 9 + (int)((d2 - 230.0) / (double)n) * 9 + (int)((d - 60.0) / (double)n);
        }
        if (StatueSwordCavityPanel.inside(d, d2, 60, 334, 9 * n, n)) {
            return (int)((d - 60.0) / (double)n);
        }
        return -1;
    }

    public boolean m_6375_(double d, double d2, int n) {
        this.refreshSelection();
        if (n != 0 || this.actionPending) {
            return true;
        }
        double d3 = this.localX(d);
        double d4 = this.localY(d2);
        ItemStack itemStack = this.weapon();
        if (this.insideButton(d3, d4, 12, 60)) {
            this.m_7379_();
            return true;
        }
        if (this.insideButton(d3, d4, 78, 66)) {
            this.bind();
            return true;
        }
        if (this.insideButton(d3, d4, 150, 84)) {
            this.insert();
            return true;
        }
        if (this.insideButton(d3, d4, 240, 74)) {
            this.move();
            return true;
        }
        if (this.insideButton(d3, d4, 320, 76)) {
            this.remove();
            return true;
        }
        if (!this.available(itemStack)) {
            return true;
        }
        SwordCavityRules.Slot slot = this.panel.hit(d3, d4, 60, 68);
        if (slot != null) {
            if (this.selected != null && this.selected != slot && this.inventorySlot < 0 && this.destination != slot && SwordCavityBuild.cells(itemStack).get(this.selected.ordinal()).installed()) {
                this.destination = slot;
            } else {
                this.selected = slot;
                this.destination = null;
            }
            return true;
        }
        int n2 = this.inventoryAt(d3, d4);
        if (n2 >= 0) {
            ItemStack itemStack2 = this.inventoryItem(n2);
            this.inventorySlot = !itemStack2.m_41619_() && WeaponMaterialBuild.materialFor(itemStack2) != FoodWeaponBuildRules.Material.ORIGINAL && !ItemStack.m_41728_((ItemStack)itemStack, (ItemStack)itemStack2) ? n2 : -1;
            this.ingredientSnapshot = this.inventorySlot < 0 ? ItemStack.f_41583_ : itemStack2.m_41777_();
            this.destination = null;
            return true;
        }
        this.selected = null;
        this.destination = null;
        this.inventorySlot = -1;
        this.ingredientSnapshot = ItemStack.f_41583_;
        return true;
    }

    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        ArrayList<MutableComponent> arrayList;
        List<Object> list;
        MutableComponent mutableComponent;
        this.refreshSelection();
        this.m_280273_(guiGraphics);
        double d = this.localX(n);
        double d2 = this.localY(n2);
        ItemStack itemStack = this.weapon();
        List<Object> list2 = List.of();
        guiGraphics.m_280168_().m_85836_();
        guiGraphics.m_280168_().m_85837_(this.viewLeft, this.viewTop, 0.0);
        guiGraphics.m_280168_().m_85841_(this.viewScale, this.viewScale, 1.0f);
        guiGraphics.m_280509_(0, 0, 408, 426, -233039580);
        guiGraphics.m_280509_(2, 2, 406, 424, -232118734);
        guiGraphics.m_280653_(this.f_96547_, this.f_96539_, 204, 12, -1515055);
        if (!itemStack.m_41619_()) {
            StatueSwordCavityPanel.item(guiGraphics, this.f_96547_, itemStack, 12, 30, false, false);
            this.clipped(guiGraphics, itemStack.m_41786_(), 50, 35, 264, -1515055);
            mutableComponent = Component.m_237115_((String)(FoodWeaponSkillLoadout.canBind(itemStack) ? "screen.ftboss.cavity.unbound" : (SwordCavityBuild.missingRequired(itemStack) ? "screen.ftboss.cavity.missing" : (FoodWeaponLifecycle.isBroken(itemStack) ? "screen.ftboss.cavity.depleted" : "screen.ftboss.cavity.ready"))));
            this.clipped(guiGraphics, (Component)mutableComponent, 50, 49, 264, -5594189);
            list2 = this.panel.render(guiGraphics, this.f_96547_, itemStack, this.selected, this.destination, 60, 68, d, d2, this.nativeParts);
            if (StatueSwordCavityPanel.inside(d, d2, 12, 30, 302, 32)) {
                list2 = List.of(itemStack.m_41786_(), mutableComponent);
            }
            list = SwordCavityBuild.links(itemStack);
            guiGraphics.m_280509_(326, 35, 396, 57, -12438714);
            this.clipped(guiGraphics, (Component)Component.m_237110_((String)"screen.ftboss.cavity.links", (Object[])new Object[]{list.size()}), 331, 42, 60, -1917592);
            if (StatueSwordCavityPanel.inside(d, d2, 326, 35, 70, 22)) {
                arrayList = new ArrayList<MutableComponent>();
                arrayList.add(Component.m_237110_((String)"screen.ftboss.cavity.links", (Object[])new Object[]{list.size()}));
                for (SwordCavityRules.Link link : list) {
                    arrayList.add(Component.m_237115_((String)("link.ftboss.sword.detail." + link.name().toLowerCase(Locale.ROOT))));
                }
                if (list.isEmpty()) {
                    arrayList.add(Component.m_237115_((String)"screen.ftboss.cavity.no_links"));
                }
                list2 = List.copyOf(arrayList);
            }
        } else {
            guiGraphics.m_280653_(this.f_96547_, (Component)Component.m_237115_((String)"screen.ftboss.cavity.no_sword"), 204, 110, -5594189);
        }
        mutableComponent = Component.m_237115_((String)"screen.ftboss.cavity.native_legend");
        this.clipped(guiGraphics, (Component)mutableComponent, 60, 170, 288, -5594189);
        if (StatueSwordCavityPanel.inside(d, d2, 60, 168, 288, 13)) {
            list2 = List.of(mutableComponent);
        }
        list = this.selectedEffects(itemStack);
        arrayList = this.f_96547_.m_92923_((FormattedText)this.joinEffects(list), 288);
        for (n3 = 0; n3 < Math.min(2, arrayList.size()); ++n3) {
            guiGraphics.m_280649_(this.f_96547_, (FormattedCharSequence)arrayList.get(n3), 60, 184 + n3 * 12, -1515055, false);
        }
        if (StatueSwordCavityPanel.inside(d, d2, 60, 182, 288, 26)) {
            list2 = list;
        }
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_237115_((String)"screen.ftboss.cavity.inventory"), 60, 216, -5594189, false);
        for (n3 = 0; n3 < 36; ++n3) {
            int n4 = n3 < 9 ? n3 : (n3 - 9) % 9;
            int n5 = n3 < 9 ? 0 : (n3 - 9) / 9;
            int n6 = n3 < 9 ? 334 : 230 + n5 * 32;
            int n7 = 60 + n4 * 32;
            ItemStack itemStack2 = this.inventoryItem(n3);
            boolean bl = WeaponMaterialBuild.materialFor(itemStack2) == FoodWeaponBuildRules.Material.ORIGINAL || ItemStack.m_41728_((ItemStack)itemStack, (ItemStack)itemStack2);
            StatueSwordCavityPanel.item(guiGraphics, this.f_96547_, itemStack2, n7, n6, n3 == this.inventorySlot, bl);
        }
        n3 = this.inventoryAt(d, d2);
        if (n3 >= 0 && !this.inventoryItem(n3).m_41619_()) {
            ItemStack itemStack3 = this.inventoryItem(n3);
            FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(itemStack3);
            ArrayList<Object> arrayList2 = new ArrayList<Object>();
            arrayList2.add(itemStack3.m_41786_());
            if (material != FoodWeaponBuildRules.Material.ORIGINAL && !ItemStack.m_41728_((ItemStack)itemStack, (ItemStack)itemStack3)) {
                arrayList2.addAll(SwordCavityBuild.materialEffects(material));
            } else {
                arrayList2.add(Component.m_237115_((String)"screen.ftboss.cavity.not_material"));
            }
            list2 = List.copyOf(arrayList2);
        }
        this.drawButton(guiGraphics, 12, 60, "back", !this.actionPending);
        this.drawButton(guiGraphics, 78, 66, "bind", this.available(itemStack) && FoodWeaponSkillLoadout.canBind(itemStack));
        this.drawButton(guiGraphics, 150, 84, "insert", this.canInsert(itemStack));
        this.drawButton(guiGraphics, 240, 74, "move", this.canMove(itemStack));
        this.drawButton(guiGraphics, 320, 76, "remove", this.canRemove(itemStack));
        if (this.insideButton(d, d2, 12, 60)) {
            list2 = List.of(Component.m_237115_((String)"screen.ftboss.cavity.back_hint"));
        }
        if (this.insideButton(d, d2, 78, 66)) {
            list2 = List.of(Component.m_237115_((String)"screen.ftboss.cavity.bind_hint"));
        }
        if (this.insideButton(d, d2, 150, 84)) {
            String string;
            ArrayList<MutableComponent> arrayList3 = new ArrayList<MutableComponent>(List.of(Component.m_237115_((String)"screen.ftboss.cavity.insert_hint")));
            FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot));
            if (!itemStack.m_41619_() && this.selected != null && material != FoodWeaponBuildRules.Material.ORIGINAL && !(string = SwordCavityBuild.replacementFailure(itemStack, this.selected, material)).isEmpty()) {
                arrayList3.add(Component.m_237115_((String)string));
            }
            list2 = List.copyOf(arrayList3);
        }
        if (this.insideButton(d, d2, 240, 74)) {
            list2 = List.of(Component.m_237115_((String)"screen.ftboss.cavity.move_hint"));
        }
        if (this.insideButton(d, d2, 320, 76)) {
            list2 = List.of(Component.m_237115_((String)"screen.ftboss.cavity.remove_hint"));
        }
        MutableComponent mutableComponent2 = Component.m_237115_((String)(this.actionPending ? "screen.ftboss.cavity.pending" : (!itemStack.m_41619_() && !FoodWeaponSkillLoadout.canBind(itemStack) && FoodWeaponLifecycle.weaponId(itemStack) == null ? "screen.ftboss.cavity.tracking" : "screen.ftboss.cavity.select_hint")));
        this.clipped(guiGraphics, (Component)mutableComponent2, 12, 412, 384, -5594189);
        if (StatueSwordCavityPanel.inside(d, d2, 12, 410, 384, 14)) {
            list2 = List.of(mutableComponent2);
        }
        guiGraphics.m_280168_().m_85849_();
        if (!list2.isEmpty()) {
            guiGraphics.m_280666_(this.f_96547_, list2, n, n2);
        }
    }

    private List<Component> selectedEffects(ItemStack itemStack) {
        if (this.inventorySlot >= 0) {
            FoodWeaponBuildRules.Material material = WeaponMaterialBuild.materialFor(this.inventoryItem(this.inventorySlot));
            return SwordCavityBuild.materialEffects(material);
        }
        if (!itemStack.m_41619_() && this.selected != null) {
            SwordCavityRules.Cell cell = SwordCavityBuild.cells(itemStack, this.nativeParts).get(this.selected.ordinal());
            if (cell.installed()) {
                return SwordCavityBuild.materialEffects(cell.material());
            }
            if (cell.present()) {
                return List.of(Component.m_237115_((String)"screen.ftboss.sword.native_no_bonus"));
            }
            return StatueSwordCavityPanel.tooltip(itemStack, this.selected, this.nativeParts);
        }
        return List.of(Component.m_237115_((String)"screen.ftboss.cavity.select_hint"));
    }

    private Component joinEffects(List<Component> list) {
        MutableComponent mutableComponent = Component.m_237119_();
        for (int i = 0; i < list.size(); ++i) {
            if (i > 0) {
                mutableComponent.m_7220_((Component)Component.m_237113_((String)" \u00b7 "));
            }
            mutableComponent.m_7220_(list.get(i));
        }
        return mutableComponent;
    }

    private void clipped(GuiGraphics guiGraphics, Component component, int n, int n2, int n3, int n4) {
        guiGraphics.m_280056_(this.f_96547_, this.f_96547_.m_92834_(component.getString(), n3), n, n2, n4, false);
    }

    private void drawButton(GuiGraphics guiGraphics, int n, int n2, String string, boolean bl) {
        guiGraphics.m_280509_(n, 382, n + n2, 404, bl ? -10071699 : -13225667);
        guiGraphics.m_280653_(this.f_96547_, (Component)Component.m_237115_((String)("screen.ftboss.cavity." + string)), n + n2 / 2, 389, bl ? -1515055 : -8949377);
    }

    private boolean insideButton(double d, double d2, int n, int n2) {
        return StatueSwordCavityPanel.inside(d, d2, n, 382, n2, 22);
    }
}

