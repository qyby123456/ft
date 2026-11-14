/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.client.FourWeaponCavityPresentation;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class SwordCavityBondPanel {
    public static final int GAP = 6;
    public static final int WIDTH = 118;
    private static final int EDGE = -10455686;
    private static final int BACKGROUND = -15261399;
    private static final int TEXT = -2234145;
    private static final int MUTED = -6112848;
    private static final int ACCENT = -930694;
    private static final int CONTENT_Y = 27;
    private static final int PADDING = 8;
    private static final int ENTRY_GAP = 12;
    private int scrollOffset;
    private int scrollMaximum;
    private ItemStack displayedWeapon;
    private List<Entry> displayedEntries;

    private static List<Entry> entries(ItemStack itemStack) {
        ArrayList<Entry> arrayList = new ArrayList<Entry>();
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
            boolean bl = view.valid() && view.status().ready() && !FourWeaponUseGuard.blocksUse(itemStack);
            List<ItemStack> list = FourWeaponCavityPresentation.unlockedPaid(view);
            for (SwordCavityExtensions.BondDefinition bondDefinition : SwordCavityExtensions.activeBonds(itemStack, list, bl)) {
                arrayList.add(new Entry(bondDefinition.name(), bondDefinition.description()));
            }
            for (SwordMaterialExtensions.BondDefinition bondDefinition : SwordMaterialExtensions.activeBonds(itemStack, list, bl)) {
                arrayList.add(new Entry(bondDefinition.name(), bondDefinition.description()));
            }
            return List.copyOf(arrayList);
        }
        for (SwordCavityRules.Link object : SwordCavityBuild.links(itemStack)) {
            String string = object.name().toLowerCase(Locale.ROOT);
            arrayList.add(new Entry((Component)Component.m_237115_((String)("link.ftboss.sword." + string)), (Component)Component.m_237115_((String)("link.ftboss.sword.detail." + string))));
        }
        for (SwordCavityExtensions.BondDefinition bondDefinition : SwordCavityExtensions.activeBonds(itemStack)) {
            arrayList.add(new Entry(bondDefinition.name(), bondDefinition.description()));
        }
        for (SwordMaterialExtensions.BondDefinition bondDefinition : SwordMaterialExtensions.activeBonds(itemStack)) {
            arrayList.add(new Entry(bondDefinition.name(), bondDefinition.description()));
        }
        return arrayList;
    }

    public void render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int n, int n2, int n3) {
        guiGraphics.m_280509_(n, n2, n + 118, n2 + n3, -10455686);
        guiGraphics.m_280509_(n + 1, n2 + 1, n + 118 - 1, n2 + n3 - 1, -15261399);
        MutableComponent mutableComponent = Component.m_237115_((String)"screen.ftboss.cavity.bond_panel");
        guiGraphics.m_280614_(font, (Component)mutableComponent, n + 8, n2 + 8, -2234145, false);
        if (this.displayedWeapon == null || this.displayedWeapon.m_41613_() != itemStack.m_41613_() || !ItemStack.m_150942_((ItemStack)this.displayedWeapon, (ItemStack)itemStack)) {
            this.displayedEntries = SwordCavityBondPanel.entries(itemStack);
            this.displayedWeapon = itemStack.m_41777_();
        }
        List<Entry> list = this.displayedEntries;
        guiGraphics.m_280056_(font, Integer.toString(list.size()), n + 118 - 8 - 8, n2 + 8, -6112848, false);
        int n4 = 0;
        for (Entry entry : list) {
            Objects.requireNonNull(font);
            int n5 = font.m_92923_((FormattedText)entry.description(), 99).size();
            Objects.requireNonNull(font);
            n4 += 9 + 4 + n5 * 9 + 12;
        }
        int n6 = n3 - 27 - 8;
        this.scrollMaximum = Math.max(0, n4 - n6);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollMaximum));
        guiGraphics.m_280588_(n + 8, n2 + 27, n + 118 - 8, n2 + n3 - 8);
        int n7 = n2 + 27 - this.scrollOffset;
        if (list.isEmpty()) {
            guiGraphics.m_280614_(font, (Component)Component.m_237115_((String)"screen.ftboss.cavity.bond_none"), n + 8, n7, -6112848, false);
        }
        for (Entry entry : list) {
            guiGraphics.m_280056_(font, font.m_92834_(entry.name().getString(), 99), n + 8, n7, -930694, false);
            Objects.requireNonNull(font);
            n7 += 9 + 4;
            for (FormattedCharSequence formattedCharSequence : font.m_92923_((FormattedText)entry.description(), 99)) {
                guiGraphics.m_280649_(font, formattedCharSequence, n + 8, n7, -6112848, false);
                Objects.requireNonNull(font);
                n7 += 9;
            }
            n7 += 12;
        }
        guiGraphics.m_280618_();
        if (this.scrollMaximum > 0) {
            int n8 = Math.max(12, n6 * n6 / n4);
            int n9 = n2 + 27 + (n6 - n8) * this.scrollOffset / this.scrollMaximum;
            guiGraphics.m_280509_(n + 118 - 4, n9, n + 118 - 2, n9 + n8, -10455686);
        }
    }

    public boolean scroll(double d, double d2, double d3, int n, int n2, int n3) {
        if (d < (double)n || d >= (double)(n + 118) || d2 < (double)n2 || d2 >= (double)(n2 + n3)) {
            return false;
        }
        this.scrollOffset = Math.max(0, Math.min(this.scrollMaximum, this.scrollOffset - (int)Math.round(d3 * 18.0)));
        return true;
    }

    private record Entry(Component name, Component description) {
    }
}

