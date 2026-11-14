package net.farmingtales.ftboss.client;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

final class StatueWeaponUiDraw {
    static final int TEXT = -1515055;
    static final int MUTED = -6449784;
    static final int CARD = -936301001;
    static final int INNER = -400285143;

    private StatueWeaponUiDraw() {
    }

    static boolean inside(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= (double)var4 && var0 < (double)(var4 + var6) && var2 >= (double)var5 && var2 < (double)(var5 + var7);
    }

    static int text(GuiGraphics var0, Font var1, Component var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        List var9 = var1.split(var2, Math.max(1, var5));
        int var10 = Math.min(var8, var9.size());

        for (int var11 = 0; var11 < var10; var11++) {
            var0.drawString(var1, (FormattedCharSequence)var9.get(var11), var3, var4 + var11 * var7, var6, false);
        }

        return var10;
    }

    static void centered(GuiGraphics var0, Font var1, Component var2, int var3, int var4, int var5, int var6, int var7, int var8) {
        List var9 = var1.split(var2, Math.max(1, var5));

        for (int var10 = 0; var10 < Math.min(var8, var9.size()); var10++) {
            FormattedCharSequence var11 = (FormattedCharSequence)var9.get(var10);
            var0.drawString(var1, var11, var3 + (var5 - var1.width(var11)) / 2, var4 + var10 * var7, var6, false);
        }
    }

    static void heading(GuiGraphics var0, Font var1, int var2, int var3, String var4, int var5) {
        var0.drawString(var1, Component.translatable(var4), var2 + 1, var3 + 2, 0xFF000000 | var5, false);
        var0.fill(var2, var3 + 15, var2 + 46, var3 + 17, 0xFF000000 | var5);
    }

    static void weapon(GuiGraphics var0, int var1, int var2, ItemStack var3, int var4) {
        var0.fill(var1, var2, var1 + 18, var2 + 18, -16185333);
        var0.fill(var1 + 1, var2 + 1, var1 + 17, var2 + 17, 0xFF000000 | var4);
        var0.fill(var1 + 2, var2 + 2, var1 + 16, var2 + 16, -400285143);
        if (!var3.isEmpty()) {
            var0.renderItem(var3, var1 + 1, var2 + 1);
        }
    }
}
