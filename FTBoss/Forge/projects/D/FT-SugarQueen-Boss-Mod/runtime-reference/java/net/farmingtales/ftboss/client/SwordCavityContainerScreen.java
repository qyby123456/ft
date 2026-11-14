/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.DefaultedRegistry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.client.CavityPresentationCache;
import net.farmingtales.ftboss.client.FourWeaponCavityPresentation;
import net.farmingtales.ftboss.client.StatueWeaponRepairNavigation;
import net.farmingtales.ftboss.client.SwordCavityBondPanel;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.menu.WeaponCavityMenuRules;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.WeaponUiSnapshot;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityPaletteRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.WeaponCavityShapeRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class SwordCavityContainerScreen
extends AbstractContainerScreen<SwordCavityMenu> {
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
    private final String shapeFamily;
    private final List<ItemStack> environmentItems;
    private List<SwordNativeMaterialRules.Piece> nativeParts = List.of();
    private FourWeaponFoundationAdapter.View foundation;
    private Slot dragSource;
    private int dragButton;
    private boolean cavityPress;
    private int cavityButton;

    public SwordCavityContainerScreen(SwordCavityMenu swordCavityMenu, Inventory inventory, Component component) {
        super((AbstractContainerMenu)swordCavityMenu, inventory, component);
        this.foundationMenu = FourWeaponFoundationAdapter.applies(swordCavityMenu.weapon());
        this.shapeFamily = WeaponCavityMenuRules.shapeFamily(swordCavityMenu.weapon());
        this.environmentItems = WeaponCavityShapeRules.environment(this.shapeFamily, BuiltInRegistries.f_257033_.m_7981_((Object)swordCavityMenu.weapon().m_41720_()).toString()).stream().map(ResourceLocation::new).filter(arg_0 -> ((DefaultedRegistry)BuiltInRegistries.f_257033_).m_7804_(arg_0)).map(resourceLocation -> new ItemStack((ItemLike)BuiltInRegistries.f_257033_.m_7745_(resourceLocation))).toList();
        this.f_97726_ = 320;
        this.f_97727_ = 208;
        this.f_97728_ = 10;
        this.f_97729_ = 7;
        this.f_97730_ = 17;
        this.f_97731_ = 114;
    }

    protected void m_7856_() {
        super.m_7856_();
        this.m_142416_((GuiEventListener)Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.cavity.back_short"), button -> {
            if (this.f_96541_ != null && this.f_96541_.f_91072_ != null) {
                this.f_96541_.f_91072_.m_105208_(((SwordCavityMenu)this.f_97732_).f_38840_, 27);
            }
        }).m_252987_(this.f_97735_ + 157, this.f_97736_ + 5, 29, 17).m_257505_(Tooltip.m_257550_((Component)Component.m_237115_((String)"screen.ftboss.cavity.back_hint"))).m_253136_());
        this.m_142416_((GuiEventListener)StatueWeaponRepairNavigation.cavityEntry((SwordCavityMenu)this.f_97732_, this.f_97735_ + 196 + 6, this.f_97736_ + this.f_97727_ - 22, 118));
    }

    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        this.m_280273_(guiGraphics);
        CavityPresentationCache.Frame frame = CavityPresentationCache.frame((Object)this, ((SwordCavityMenu)this.f_97732_).weapon(), this.f_96541_ == null ? null : this.f_96541_.f_91073_);
        this.foundation = frame.foundation();
        this.nativeParts = frame.nativeParts();
        super.m_88315_(guiGraphics, n, n2, f);
        if (this.f_97734_ != null && this.f_97734_.f_40219_ < 27) {
            this.renderMaterialTooltip(guiGraphics, n, n2, this.f_97734_.f_40219_);
        } else {
            this.m_280072_(guiGraphics, n, n2);
        }
        for (n3 = 0; n3 < this.environmentItems.size(); ++n3) {
            int n4 = this.f_97735_ + 151 + n3 * 18;
            if (n < n4 || n >= n4 + 16 || n2 < this.f_97736_ + 23 || n2 >= this.f_97736_ + 39) continue;
            guiGraphics.m_280666_(this.f_96547_, List.of(this.environmentItems.get(n3).m_41786_(), Component.m_264568_((String)"screen.ftboss.cavity.environment_hint", (String)"\u73af\u5883\u642d\u914d\u53c2\u8003 \u00b7 \u672a\u5b89\u88c5\uff0c\u4e0d\u8ba1\u5c5e\u6027"), Component.m_264568_((String)"screen.ftboss.cavity.environment_help", (String)"\u4ec5\u793a\u610f\u73af\u5883\u642d\u914d\uff1b\u53ef\u88c5\u6750\u6599\u4ee5\u5f53\u524d\u6b66\u5668\u7684\u89c4\u5219\u4e3a\u51c6")), n, n2);
        }
        if ((this.foundation != null ? this.foundation.profile() != null : SwordCavityExtensions.functionProfile(((SwordCavityMenu)this.f_97732_).weapon()) != null) && n >= this.f_97735_ + 17 && n < this.f_97735_ + 17 + 115 && n2 >= this.f_97736_ + 101 && n2 < this.f_97736_ + 112) {
            n3 = (n - this.f_97735_ - 17) / 23;
            if (this.foundation != null) {
                ArrayList<Object> arrayList = new ArrayList<Object>();
                arrayList.add(FourWeaponCavityPresentation.axisName(this.foundation.profile(), n3, false));
                arrayList.add(this.foundation.valid() ? Component.m_237110_((String)"screen.ftboss.cavity.function_score", (Object[])new Object[]{SwordCavityContainerScreen.format(frame.status().total().at(n3)), SwordCavityContainerScreen.format(this.foundation.profile().minimum().at(n3))}) : Component.m_264568_((String)"screen.ftboss.cavity.structure_short", (String)"Structure unavailable"));
                guiGraphics.m_280666_(this.f_96547_, arrayList, n, n2);
                return;
            }
            SwordFunctionRules.Function function = SwordFunctionRules.Function.values()[n3];
            SwordFunctionRules.Status status = frame.swordStatus();
            guiGraphics.m_280666_(this.f_96547_, List.of(Component.m_237115_((String)("function.ftboss.sword." + function.name().toLowerCase(Locale.ROOT))), Component.m_237110_((String)"screen.ftboss.cavity.function_score", (Object[])new Object[]{SwordCavityContainerScreen.format(status.total().value(function)), SwordCavityContainerScreen.format(status.minimum().value(function))})), n, n2);
        }
    }

    protected void m_7286_(GuiGraphics guiGraphics, float f, int n, int n2) {
        guiGraphics.m_280509_(this.f_97735_, this.f_97736_, this.f_97735_ + 196, this.f_97736_ + this.f_97727_, -10455686);
        guiGraphics.m_280509_(this.f_97735_ + 1, this.f_97736_ + 1, this.f_97735_ + 196 - 1, this.f_97736_ + this.f_97727_ - 1, -15129812);
        this.renderWeaponShape(guiGraphics);
        SwordFunctionRules.Profile profile = SwordCavityExtensions.functionProfile(((SwordCavityMenu)this.f_97732_).weapon());
        for (Slot slot : ((SwordCavityMenu)this.f_97732_).f_38839_) {
            int n3;
            int n4 = this.f_97735_ + slot.f_40220_;
            int n5 = this.f_97736_ + slot.f_40221_;
            if (slot.f_40219_ < 27 && !this.cellUnlocked(slot.f_40219_) && !slot.m_6657_()) {
                if (this.foundation != null) {
                    if (!FourWeaponCavityPresentation.reference(this.foundation, slot.f_40219_)) continue;
                } else if (!((SwordCavityMenu)this.f_97732_).reference(slot.f_40219_)) continue;
            }
            guiGraphics.m_280509_(n4 - 1, n5 - 1, n4 + 17, n5 + 17, -10455686);
            int n6 = slot.f_40219_ < 27 ? (this.foundation != null ? FourWeaponCavityPresentation.cellColor(this.foundation, slot.f_40219_) : SwordCavityPaletteRules.cellColor(profile, slot.f_40219_)) : -13944511;
            guiGraphics.m_280509_(n4, n5, n4 + 16, n5 + 16, n6);
            if (slot.f_40219_ < 27 && !this.cellUnlocked(slot.f_40219_) && slot.m_6657_()) {
                guiGraphics.m_280509_(n4, n5, n4 + 16, n5 + 16, -15656673);
            }
            if (slot.f_40219_ < 27 && !this.cellUnlocked(slot.f_40219_) && !slot.m_6657_()) {
                guiGraphics.m_280509_(n4, n5, n4 + 16, n5 + 16, -15656673);
                guiGraphics.m_280509_(n4 + 5, n5 + 7, n4 + 11, n5 + 12, -6112848);
                guiGraphics.m_280509_(n4 + 6, n5 + 4, n4 + 10, n5 + 8, -6112848);
                guiGraphics.m_280509_(n4 + 7, n5 + 5, n4 + 9, n5 + 7, -15656673);
            }
            if (slot.f_40219_ < 27 && (this.foundation != null ? FourWeaponCavityPresentation.reference(this.foundation, slot.f_40219_) : ((SwordCavityMenu)this.f_97732_).reference(slot.f_40219_))) {
                ItemStack itemStack = this.foundation != null ? FourWeaponCavityPresentation.displayItem(this.foundation, slot.f_40219_) : SwordCavityBuild.displayItem(((SwordCavityMenu)this.f_97732_).weapon(), SwordCavityRules.Slot.byIndex(slot.f_40219_), this.nativeParts);
                guiGraphics.m_280480_(itemStack, n4, n5);
                guiGraphics.m_280509_(n4 + 12, n5 + 12, n4 + 16, n5 + 16, -6112848);
            }
            if (slot.f_40219_ >= 27 || (n3 = this.cellStateColor(slot.f_40219_)) == 0) continue;
            guiGraphics.m_280509_(n4 + 1, n5 + 1, n4 + 4, n5 + 4, n3);
        }
        this.bonds.render(guiGraphics, this.f_96547_, ((SwordCavityMenu)this.f_97732_).weapon(), this.f_97735_ + 196 + 6, this.f_97736_, this.f_97727_ - 26);
    }

    protected void m_280003_(GuiGraphics guiGraphics, int n, int n2) {
        int n3;
        int n4;
        CavityPresentationCache.Frame frame = CavityPresentationCache.frame((Object)this, ((SwordCavityMenu)this.f_97732_).weapon(), this.f_96541_ == null ? null : this.f_96541_.f_91073_);
        Component component = this.foundation == null ? this.f_96539_ : Component.m_264568_((String)"screen.ftboss.cavity.structure", (String)"Composition");
        guiGraphics.m_280056_(this.f_96547_, this.f_96547_.m_92834_(component.getString(), 96), this.f_97728_, this.f_97729_, -2234145, false);
        guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_264568_((String)"screen.ftboss.cavity.environment_short", (String)"\u73af\u5883\u53c2\u8003"), 110, 7, -6112848, false);
        ItemStack itemStack = ((SwordCavityMenu)this.f_97732_).weapon();
        guiGraphics.m_280480_(itemStack, 17, 23);
        guiGraphics.m_280056_(this.f_96547_, this.f_96547_.m_92834_(itemStack.m_41786_().getString(), 108), 37, 27, -2234145, false);
        for (n4 = 0; n4 < this.environmentItems.size(); ++n4) {
            n3 = 151 + n4 * 18;
            guiGraphics.m_280509_(n3 - 1, 22, n3 + 17, 40, -13083828);
            guiGraphics.m_280509_(n3, 23, n3 + 16, 39, -15129812);
            guiGraphics.m_280480_(this.environmentItems.get(n4), n3, 23);
        }
        if (this.foundation != null) {
            if (this.foundation.profile() != null) {
                for (n4 = 0; n4 < 5; ++n4) {
                    n3 = frame.status() != null && frame.status().total().at(n4) + 1.0E-9 >= this.foundation.profile().minimum().at(n4) ? 1 : 0;
                    Component component2 = FourWeaponCavityPresentation.axisName(this.foundation.profile(), n4, true);
                    guiGraphics.m_280056_(this.f_96547_, this.f_96547_.m_92834_(component2.getString(), 17) + (n3 != 0 ? "+" : "-"), 17 + n4 * 23, 103, n3 != 0 ? FourWeaponCavityPresentation.axisColor(n4) : -28288, false);
                }
                if (this.foundation.valid()) {
                    guiGraphics.m_280056_(this.f_96547_, frame.status().openCells() + "/" + FourWeaponFoundationRules.capacity(this.foundation.profile(), 4), 151, 103, -6112848, false);
                }
            } else {
                guiGraphics.m_280614_(this.f_96547_, (Component)Component.m_264568_((String)"screen.ftboss.cavity.structure_short", (String)"Structure unavailable"), 17, 103, -28288, false);
            }
        } else if (SwordCavityExtensions.functionProfile(itemStack) != null) {
            SwordFunctionRules.Status status = frame.swordStatus();
            for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
                MutableComponent mutableComponent = Component.m_237115_((String)("function.ftboss.sword.short." + function.name().toLowerCase(Locale.ROOT)));
                String string = status.sufficient(function) ? "+" : "-";
                guiGraphics.m_280056_(this.f_96547_, mutableComponent.getString() + string, 17 + function.ordinal() * 23, 103, status.sufficient(function) ? SwordCavityPaletteRules.functionColor(function) : -28288, false);
            }
            int n5 = SwordFunctionRules.maximumCells(SwordCavityExtensions.functionProfile(itemStack));
            guiGraphics.m_280056_(this.f_96547_, status.unlockedCells() + "/" + n5, 151, 103, -6112848, false);
        }
        guiGraphics.m_280614_(this.f_96547_, this.f_169604_, this.f_97730_, this.f_97731_, -6112848, false);
    }

    private void renderMaterialTooltip(GuiGraphics guiGraphics, int n, int n2, int n3) {
        if (this.foundation != null) {
            this.renderFoundationMaterialTooltip(guiGraphics, n, n2, n3);
            return;
        }
        ItemStack itemStack = ((Slot)((SwordCavityMenu)this.f_97732_).f_38839_.get(n3)).m_7993_();
        boolean bl = ((SwordCavityMenu)this.f_97732_).reference(n3);
        if (bl) {
            itemStack = SwordCavityBuild.displayItem(((SwordCavityMenu)this.f_97732_).weapon(), SwordCavityRules.Slot.byIndex(n3), this.nativeParts);
        }
        ArrayList<Object> arrayList = new ArrayList<Object>();
        if (!itemStack.m_41619_()) {
            arrayList.add(itemStack.m_41786_());
            Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve(itemStack);
            ArrayList arrayList2 = new ArrayList();
            if (SwordCavityExtensions.functionProfile(((SwordCavityMenu)this.f_97732_).weapon()) != null) {
                optional.ifPresent(swordMaterialDefinition -> {
                    for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
                        if (!(swordMaterialDefinition.functions().value(function) >= 0.6)) continue;
                        arrayList2.add(Component.m_237115_((String)("function.ftboss.sword." + function.name().toLowerCase(Locale.ROOT))).getString());
                    }
                });
            }
            if (!arrayList2.isEmpty()) {
                arrayList.add(Component.m_237113_((String)String.join((CharSequence)" \u00b7 ", arrayList2)));
            }
            if (bl) {
                arrayList.add(Component.m_237115_((String)"screen.ftboss.cavity.reference_short"));
            } else {
                FoodWeaponBuildRules.Material material = SwordCavityBuild.materialFor(itemStack);
                arrayList.addAll(SwordCavityBuild.materialEffects(material));
            }
        }
        if (!SwordCavityBuild.cellUnlocked(((SwordCavityMenu)this.f_97732_).weapon(), n3)) {
            arrayList.add(Component.m_237115_((String)"screen.ftboss.cavity.locked_short"));
        }
        if (!SwordCavityBuild.containerEditable(((SwordCavityMenu)this.f_97732_).weapon())) {
            arrayList.add(Component.m_237115_((String)"screen.ftboss.cavity.repair_short"));
        }
        if (!arrayList.isEmpty()) {
            guiGraphics.m_280666_(this.f_96547_, arrayList, n, n2);
        }
    }

    private static String format(double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }

    private void renderWeaponShape(GuiGraphics guiGraphics) {
        int n = -8821688;
        int n2 = -8875106;
        int n3 = -7047496;
        if (this.shapeFamily.equals("bow")) {
            this.shapeLine(guiGraphics, 61, 88, 97, 52, n, 4);
            this.shapeLine(guiGraphics, 97, 52, 133, 88, n, 4);
            this.shapeLine(guiGraphics, 61, 88, 133, 88, -5850972, 1);
        } else if (this.shapeFamily.equals("staff")) {
            this.shapeLine(guiGraphics, 25, 70, 143, 70, n, 4);
            this.shapeLine(guiGraphics, 143, 48, 166, 70, n3, 4);
            this.shapeLine(guiGraphics, 166, 70, 143, 93, n3, 4);
            this.shapeLine(guiGraphics, 143, 93, 126, 70, n3, 4);
            this.shapeLine(guiGraphics, 126, 70, 143, 48, n3, 4);
        } else if (this.shapeFamily.equals("crossbow")) {
            this.shapeLine(guiGraphics, 25, 70, 169, 70, n, 4);
            this.shapeLine(guiGraphics, 79, 48, 115, 70, n2, 4);
            this.shapeLine(guiGraphics, 115, 70, 79, 93, n2, 4);
            this.shapeLine(guiGraphics, 79, 48, 79, 93, -5850972, 1);
        } else if (this.shapeFamily.equals("trident")) {
            this.shapeLine(guiGraphics, 25, 70, 175, 70, n, 4);
            this.shapeLine(guiGraphics, 134, 48, 134, 93, n2, 4);
            this.shapeLine(guiGraphics, 134, 48, 175, 48, n2, 4);
            this.shapeLine(guiGraphics, 134, 93, 175, 93, n2, 4);
        } else if (this.shapeFamily.equals("axe")) {
            this.shapeLine(guiGraphics, 25, 70, 152, 70, n, 4);
            guiGraphics.m_280509_(this.f_97735_ + 128, this.f_97736_ + 46, this.f_97735_ + 160, this.f_97736_ + 94, n2);
            guiGraphics.m_280509_(this.f_97735_ + 160, this.f_97736_ + 52, this.f_97735_ + 166, this.f_97736_ + 88, n2);
        } else {
            this.shapeLine(guiGraphics, 25, 70, 62, 70, n, 4);
            this.shapeLine(guiGraphics, 61, 47, 61, 93, n, 4);
            guiGraphics.m_280509_(this.f_97735_ + 66, this.f_97736_ + 62, this.f_97735_ + 167, this.f_97736_ + 79, n2);
            guiGraphics.m_280509_(this.f_97735_ + 167, this.f_97736_ + 65, this.f_97735_ + 173, this.f_97736_ + 76, n2);
            guiGraphics.m_280509_(this.f_97735_ + 173, this.f_97736_ + 68, this.f_97735_ + 179, this.f_97736_ + 73, n2);
        }
    }

    private void shapeLine(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7 = Math.max(Math.abs(n3 - n), Math.abs(n4 - n2));
        for (int i = 0; i <= n7; ++i) {
            int n8 = this.f_97735_ + n + (n3 - n) * i / Math.max(1, n7);
            int n9 = this.f_97736_ + n2 + (n4 - n2) * i / Math.max(1, n7);
            guiGraphics.m_280509_(n8 - n6, n9 - n6, n8 + n6 + 1, n9 + n6 + 1, n5);
        }
    }

    private boolean cellUnlocked(int n) {
        return this.foundation != null ? FourWeaponCavityPresentation.unlocked(this.foundation, n) : SwordCavityBuild.cellUnlocked(((SwordCavityMenu)this.f_97732_).weapon(), n);
    }

    private int cellStateColor(int n) {
        if (this.foundation != null) {
            if (!FourWeaponCavityPresentation.unlocked(this.foundation, n)) {
                return -1917592;
            }
            FourWeaponFoundationAdapter.Entry entry = this.foundation.entries().get(n);
            if (entry == null) {
                return 0;
            }
            if (entry.paid()) {
                return -9250406;
            }
            return FourWeaponCavityPresentation.displayItem(this.foundation, n).m_41619_() ? 0 : -8408103;
        }
        if (!SwordCavityBuild.applies(((SwordCavityMenu)this.f_97732_).weapon())) {
            return 0;
        }
        List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(((SwordCavityMenu)this.f_97732_).weapon(), this.nativeParts);
        if (n >= list.size()) {
            return 0;
        }
        if (!SwordCavityBuild.cellUnlocked(((SwordCavityMenu)this.f_97732_).weapon(), n)) {
            return -1917592;
        }
        SwordCavityRules.State state = list.get(n).state();
        if (state == SwordCavityRules.State.INSTALLED) {
            return -9250406;
        }
        if (state == SwordCavityRules.State.NATIVE) {
            return -8408103;
        }
        if (state == SwordCavityRules.State.MISSING) {
            return -28288;
        }
        return 0;
    }

    private void renderFoundationMaterialTooltip(GuiGraphics guiGraphics, int n, int n2, int n3) {
        FourWeaponFoundationAdapter.Entry entry;
        List<String> list;
        ArrayList<Object> arrayList = new ArrayList<Object>();
        ItemStack itemStack = FourWeaponCavityPresentation.displayItem(this.foundation, n3);
        if (!itemStack.m_41619_()) {
            arrayList.add(itemStack.m_41786_());
        }
        if (!(list = FourWeaponCavityPresentation.materialRoles(this.foundation, n3).stream().map(Component::getString).toList()).isEmpty()) {
            arrayList.add(Component.m_237113_((String)String.join((CharSequence)" \u00b7 ", list)));
        }
        if ((entry = this.foundation.entries().get(n3)) != null && this.foundation.valid()) {
            if (!entry.paid()) {
                arrayList.add(Component.m_237115_((String)"screen.ftboss.cavity.reference_short"));
            } else {
                arrayList.addAll(SwordCavityBuild.materialEffects(entry.value().material()));
            }
        }
        if (!this.cellUnlocked(n3)) {
            arrayList.add(Component.m_237115_((String)"screen.ftboss.cavity.locked_short"));
        }
        if (!this.foundation.valid()) {
            arrayList.add(Component.m_264568_((String)"screen.ftboss.cavity.structure_short", (String)"Structure unavailable"));
        }
        if (!arrayList.isEmpty()) {
            guiGraphics.m_280666_(this.f_96547_, arrayList, n, n2);
        }
    }

    public boolean m_6050_(double d, double d2, double d3) {
        return this.bonds.scroll(d, d2, d3, this.f_97735_ + 196 + 6, this.f_97736_, this.f_97727_ - 26) || super.m_6050_(d, d2, d3);
    }

    public boolean m_6375_(double d, double d2, int n) {
        this.dragSource = null;
        if (this.usesFoundation() && (n == 0 || n == 1) && !SwordCavityContainerScreen.m_96638_()) {
            Slot slot = this.pointerSlot(d, d2);
            if (slot != null && slot.f_40219_ < 27) {
                this.cavityPress = true;
                this.cavityButton = n;
                super.m_6375_(d, d2, n);
                if (this.foundationReady()) {
                    if (((SwordCavityMenu)this.f_97732_).m_142621_().m_41619_() && ((SwordCavityMenu)this.f_97732_).reference(slot.f_40219_) && FourWeaponClientApi.canWrite(FourWeaponTransactions.Operation.REMOVE_CELL)) {
                        FourWeaponClientApi.submit(FourWeaponTransactions.Operation.REMOVE_CELL, slot.f_40219_, 0);
                    } else {
                        FourWeaponClientApi.menuClick(((SwordCavityMenu)this.f_97732_).f_38840_, slot.f_40219_, n, ClickType.PICKUP);
                    }
                }
                return true;
            }
            if (slot != null && slot.m_6657_() && ((SwordCavityMenu)this.f_97732_).m_142621_().m_41619_() && this.f_96541_ != null && this.f_96541_.f_91074_ != null && slot.m_8010_((Player)this.f_96541_.f_91074_) && this.foundationReady()) {
                this.dragSource = slot;
                this.dragButton = n;
            }
        }
        if ((n == 0 || n == 1) && ((SwordCavityMenu)this.f_97732_).m_142621_().m_41619_()) {
            for (int i = 0; i < 27; ++i) {
                Slot slot = (Slot)((SwordCavityMenu)this.f_97732_).f_38839_.get(i);
                if (!this.m_6774_(slot.f_40220_, slot.f_40221_, 16, 16, d, d2) || !((SwordCavityMenu)this.f_97732_).reference(i)) continue;
                if (this.usesFoundation()) {
                    if (this.foundationReady()) {
                        FourWeaponClientApi.submit(FourWeaponTransactions.Operation.REMOVE_CELL, i, 0);
                    }
                } else if (this.f_96541_ != null && this.f_96541_.f_91072_ != null) {
                    this.f_96541_.f_91072_.m_105208_(((SwordCavityMenu)this.f_97732_).f_38840_, i);
                }
                return true;
            }
        }
        return super.m_6375_(d, d2, n);
    }

    protected void m_6597_(Slot slot, int n, int n2, ClickType clickType) {
        if (this.usesFoundation()) {
            int n3;
            if (this.cavityPress) {
                return;
            }
            int n4 = n3 = slot == null ? n : slot.f_40219_;
            if (this.foundationReady()) {
                FourWeaponClientApi.menuClick(((SwordCavityMenu)this.f_97732_).f_38840_, n3, n2, clickType);
            }
            return;
        }
        super.m_6597_(slot, n, n2, clickType);
    }

    private boolean usesFoundation() {
        return this.foundationMenu || FourWeaponFoundationAdapter.applies(((SwordCavityMenu)this.f_97732_).weapon());
    }

    private boolean foundationReady() {
        WeaponUiSnapshot weaponUiSnapshot = FourWeaponClientApi.snapshot();
        FourWeaponTransactions.Snapshot snapshot = FourWeaponClientApi.authority();
        return FourWeaponClientApi.capable() && weaponUiSnapshot != null && snapshot != null && weaponUiSnapshot.session().equals(snapshot.session()) && weaponUiSnapshot.data().m_128451_("Container") == ((SwordCavityMenu)this.f_97732_).f_38840_ && snapshot.binding().weapon().equals(FoodWeaponLifecycle.weaponId(((SwordCavityMenu)this.f_97732_).weapon()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean m_6348_(double d, double d2, int n) {
        if (this.cavityPress && this.cavityButton == n) {
            try {
                super.m_6348_(d, d2, n);
            }
            finally {
                this.cavityPress = false;
            }
            return true;
        }
        Slot slot = this.dragSource;
        if (slot != null && this.dragButton == n) {
            this.dragSource = null;
            Slot slot2 = this.pointerSlot(d, d2);
            if (slot2 != null && slot2.f_40219_ < 27 && this.foundationReady()) {
                super.m_6348_((double)(this.f_97735_ + slot.f_40220_ + 8), (double)(this.f_97736_ + slot.f_40221_ + 8), n);
                this.m_6375_(d, d2, n);
                return this.m_6348_(d, d2, n);
            }
        }
        return super.m_6348_(d, d2, n);
    }

    private Slot pointerSlot(double d, double d2) {
        for (Slot slot : ((SwordCavityMenu)this.f_97732_).f_38839_) {
            if (!slot.m_6659_() || !this.m_6774_(slot.f_40220_, slot.f_40221_, 16, 16, d, d2)) continue;
            return slot;
        }
        return null;
    }
}

