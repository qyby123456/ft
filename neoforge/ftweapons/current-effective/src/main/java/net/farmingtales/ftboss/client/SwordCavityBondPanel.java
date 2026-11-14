package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.api.SwordCavityExtensions.BondDefinition;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
    private List<SwordCavityBondPanel.Entry> displayedEntries;

    private static List<SwordCavityBondPanel.Entry> entries(ItemStack var0) {
        ArrayList var1 = new ArrayList();
        if (FourWeaponFoundationAdapter.applies(var0)) {
            View var9 = FourWeaponFoundationAdapter.read(var0);
            boolean var12 = var9.valid() && var9.status().ready() && !FourWeaponUseGuard.blocksUse(var0);
            List<ItemStack> var13 = FourWeaponCavityPresentation.unlockedPaid(var9);

            for (BondDefinition var6 : SwordCavityExtensions.activeBonds(var0, var13, var12)) {
                var1.add(new SwordCavityBondPanel.Entry(var6.name(), var6.description()));
            }

            for (net.farmingtales.ftboss.api.SwordMaterialExtensions.BondDefinition var15 : SwordMaterialExtensions.activeBonds(var0, var13, var12)) {
                var1.add(new SwordCavityBondPanel.Entry(var15.name(), var15.description()));
            }

            return List.copyOf(var1);
        } else {
            for (Link var3 : SwordCavityBuild.links(var0)) {
                String var4 = var3.name().toLowerCase(Locale.ROOT);
                var1.add(
                    new SwordCavityBondPanel.Entry(
                        Component.translatable("link.ftboss.sword." + var4), Component.translatable("link.ftboss.sword.detail." + var4)
                    )
                );
            }

            for (BondDefinition var10 : SwordCavityExtensions.activeBonds(var0)) {
                var1.add(new SwordCavityBondPanel.Entry(var10.name(), var10.description()));
            }

            for (net.farmingtales.ftboss.api.SwordMaterialExtensions.BondDefinition var11 : SwordMaterialExtensions.activeBonds(var0)) {
                var1.add(new SwordCavityBondPanel.Entry(var11.name(), var11.description()));
            }

            return var1;
        }
    }

    public void render(GuiGraphics var1, Font var2, ItemStack var3, int var4, int var5, int var6) {
        var1.fill(var4, var5, var4 + 118, var5 + var6, -10455686);
        var1.fill(var4 + 1, var5 + 1, var4 + 118 - 1, var5 + var6 - 1, -15261399);
        MutableComponent var7 = Component.translatable("screen.ftboss.cavity.bond_panel");
        var1.drawString(var2, var7, var4 + 8, var5 + 8, -2234145, false);
        if (this.displayedWeapon == null || this.displayedWeapon.getCount() != var3.getCount() || !ItemStack.isSameItemSameComponents(this.displayedWeapon, var3)) {
            this.displayedEntries = entries(var3);
            this.displayedWeapon = var3.copy();
        }

        List<SwordCavityBondPanel.Entry> var8 = this.displayedEntries;
        var1.drawString(var2, Integer.toString(var8.size()), var4 + 118 - 8 - 8, var5 + 8, -6112848, false);
        int var9 = 0;

        for (SwordCavityBondPanel.Entry var11 : var8) {
            var9 += var2.lineHeight + 4 + var2.split(var11.description(), 99).size() * var2.lineHeight + 12;
        }

        int var16 = var6 - 27 - 8;
        this.scrollMaximum = Math.max(0, var9 - var16);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollMaximum));
        var1.enableScissor(var4 + 8, var5 + 27, var4 + 118 - 8, var5 + var6 - 8);
        int var17 = var5 + 27 - this.scrollOffset;
        if (var8.isEmpty()) {
            var1.drawString(var2, Component.translatable("screen.ftboss.cavity.bond_none"), var4 + 8, var17, -6112848, false);
        }

        for (SwordCavityBondPanel.Entry var13 : var8) {
            var1.drawString(var2, var2.plainSubstrByWidth(var13.name().getString(), 99), var4 + 8, var17, -930694, false);
            var17 += var2.lineHeight + 4;

            for (FormattedCharSequence var15 : var2.split(var13.description(), 99)) {
                var1.drawString(var2, var15, var4 + 8, var17, -6112848, false);
                var17 += var2.lineHeight;
            }

            var17 += 12;
        }

        var1.disableScissor();
        if (this.scrollMaximum > 0) {
            int var19 = Math.max(12, var16 * var16 / var9);
            int var20 = var5 + 27 + (var16 - var19) * this.scrollOffset / this.scrollMaximum;
            var1.fill(var4 + 118 - 4, var20, var4 + 118 - 2, var20 + var19, -10455686);
        }
    }

    public boolean scroll(double var1, double var3, double var5, int var7, int var8, int var9) {
        if (!(var1 < (double)var7) && !(var1 >= (double)(var7 + 118)) && !(var3 < (double)var8) && !(var3 >= (double)(var8 + var9))) {
            this.scrollOffset = Math.max(0, Math.min(this.scrollMaximum, this.scrollOffset - (int)Math.round(var5 * 18.0)));
            return true;
        } else {
            return false;
        }
    }

    private static record Entry(Component name, Component description) {
    }
}
