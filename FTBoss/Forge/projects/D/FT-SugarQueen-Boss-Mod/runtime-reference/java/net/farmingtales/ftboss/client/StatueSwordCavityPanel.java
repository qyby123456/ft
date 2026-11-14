/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class StatueSwordCavityPanel {
    private static final int DIAGRAM_X = 14;
    private static final int DIAGRAM_Y = 39;
    private static final int DETAIL_X = 150;
    private static final int WARNING = -26000;
    private SwordCavityRules.Slot selected;
    public static final int MUTED = -5594189;
    public static final int PITCH = 32;
    public static final int CARD = -13619915;
    public static final int TEXT = -1515055;

    public SwordCavityRules.Slot hit(double d, double d2, int n, int n2) {
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (!StatueSwordCavityPanel.inside(d, d2, n + SwordNativeMaterialRules.visualColumn(slot.ordinal()) * 32, n2 + SwordNativeMaterialRules.visualRow(slot.ordinal()) * 32, 32, 32)) continue;
            return slot;
        }
        return null;
    }

    public List<Component> render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, SwordCavityRules.Slot slot, SwordCavityRules.Slot slot2, int n, int n2, double d, double d2) {
        return this.render(guiGraphics, font, itemStack, slot, slot2, n, n2, d, d2, SwordNativeMaterials.parts(itemStack));
    }

    public List<Component> render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, SwordCavityRules.Slot slot, SwordCavityRules.Slot slot2, int n, int n2, double d, double d2, List<SwordNativeMaterialRules.Piece> list) {
        List<SwordCavityRules.Cell> list2 = SwordCavityBuild.cells(itemStack, list);
        SwordCavityRules.Slot slot3 = this.hit(d, d2, n, n2);
        for (SwordCavityRules.Slot slot4 : SwordCavityRules.Slot.values()) {
            SwordNativeMaterialRules.Origin origin;
            String string;
            SwordCavityRules.Cell cell = list2.get(slot4.ordinal());
            int n3 = n + SwordNativeMaterialRules.visualColumn(slot4.ordinal()) * 32;
            int n4 = n2 + SwordNativeMaterialRules.visualRow(slot4.ordinal()) * 32;
            ItemStack itemStack2 = SwordCavityBuild.displayItem(itemStack, slot4, list);
            if (!itemStack2.m_41619_()) {
                itemStack2 = itemStack2.m_41777_();
                itemStack2.m_41764_(1);
            }
            StatueSwordCavityPanel.item(guiGraphics, font, itemStack2, n3, n4, slot4 == slot, false);
            if (slot4 == slot2) {
                guiGraphics.m_280509_(n3 + 2, n4 + 2, n3 + 32 - 2, n4 + 3, -8199511);
                guiGraphics.m_280509_(n3 + 2, n4 + 32 - 3, n3 + 32 - 2, n4 + 32 - 2, -8199511);
            }
            String string2 = cell.state() == SwordCavityRules.State.NATIVE ? "native" : (cell.state() == SwordCavityRules.State.INSTALLED ? "installed" : (string = cell.state() == SwordCavityRules.State.MISSING ? "missing" : ""));
            if (cell.state() == SwordCavityRules.State.NATIVE && SwordNativeMaterialRules.hasPiece(list, slot4.ordinal()) && (origin = list.get(slot4.ordinal()).origin()) != SwordNativeMaterialRules.Origin.RECIPE) {
                String string3 = string = origin == SwordNativeMaterialRules.Origin.SHARED ? "shared" : "theme";
            }
            if (string.isEmpty()) continue;
            guiGraphics.m_280509_(n3 + 32 - 13, n4 + 2, n3 + 32 - 2, n4 + 12, -300477409);
            guiGraphics.m_280653_(font, (Component)Component.m_237115_((String)("screen.ftboss.cavity.badge." + string)), n3 + 32 - 8, n4 + 3, cell.state() == SwordCavityRules.State.MISSING ? -31370 : -1515055);
        }
        return slot3 == null ? List.of() : StatueSwordCavityPanel.tooltip(itemStack, slot3, list);
    }

    public static List<Component> tooltip(ItemStack itemStack, SwordCavityRules.Slot slot) {
        return StatueSwordCavityPanel.tooltip(itemStack, slot, SwordNativeMaterials.parts(itemStack));
    }

    public static List<Component> tooltip(ItemStack itemStack, SwordCavityRules.Slot slot, List<SwordNativeMaterialRules.Piece> list) {
        SwordCavityRules.Cell cell = SwordCavityBuild.cells(itemStack, list).get(slot.ordinal());
        ArrayList<Object> arrayList = new ArrayList<Object>();
        if (cell.present()) {
            ItemStack itemStack2 = SwordCavityBuild.displayItem(itemStack, slot, list);
            arrayList.add(itemStack2.m_41619_() ? WeaponMaterialBuild.materialName(cell.material()) : itemStack2.m_41786_());
            arrayList.add(Component.m_237115_((String)(cell.state() == SwordCavityRules.State.NATIVE ? "screen.ftboss.cavity.native" : "screen.ftboss.cavity.installed")));
            if (cell.state() == SwordCavityRules.State.NATIVE && SwordNativeMaterialRules.hasPiece(list, slot.ordinal())) {
                SwordNativeMaterialRules.Piece piece = list.get(slot.ordinal());
                arrayList.add(Component.m_237115_((String)("screen.ftboss.cavity.native." + piece.origin().name().toLowerCase(Locale.ROOT))));
            }
            if (cell.state() == SwordCavityRules.State.INSTALLED) {
                arrayList.addAll(SwordCavityBuild.materialEffects(cell.material()));
            }
        } else {
            arrayList.add(Component.m_237115_((String)(cell.state() == SwordCavityRules.State.EMPTY ? "screen.ftboss.cavity.empty" : (slot.required ? "screen.ftboss.cavity.missing" : "screen.ftboss.cavity.lost"))));
        }
        return List.copyOf(arrayList);
    }

    public static void item(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int n, int n2, boolean bl, boolean bl2) {
        guiGraphics.m_280509_(n + 1, n2 + 1, n + 32 - 1, n2 + 32 - 1, bl ? -10374 : -9147261);
        guiGraphics.m_280509_(n + 2, n2 + 2, n + 32 - 2, n2 + 32 - 2, -13619915);
        if (!itemStack.m_41619_()) {
            guiGraphics.m_280168_().m_85836_();
            guiGraphics.m_280168_().m_252880_((float)(n + 4), (float)(n2 + 4), 0.0f);
            guiGraphics.m_280168_().m_85841_(1.5f, 1.5f, 1.0f);
            guiGraphics.m_280480_(itemStack, 0, 0);
            guiGraphics.m_280168_().m_85849_();
            if (itemStack.m_41613_() > 1) {
                guiGraphics.m_280056_(font, Integer.toString(itemStack.m_41613_()), n + 32 - 4 - font.m_92895_(Integer.toString(itemStack.m_41613_())), n2 + 32 - 11, -1515055, true);
            }
            if (bl2) {
                guiGraphics.m_280509_(n + 2, n2 + 2, n + 32 - 2, n2 + 32 - 2, -2145971169);
            }
        }
    }

    public static boolean inside(double d, double d2, int n, int n2, int n3, int n4) {
        return d >= (double)n && d < (double)(n + n3) && d2 >= (double)n2 && d2 < (double)(n2 + n4);
    }
}

