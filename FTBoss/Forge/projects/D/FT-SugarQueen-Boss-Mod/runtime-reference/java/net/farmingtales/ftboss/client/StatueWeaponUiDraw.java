/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

final class StatueWeaponUiDraw {
    static final int TEXT = -1515055;
    static final int MUTED = -6449784;
    static final int CARD = -936301001;
    static final int INNER = -400285143;

    private StatueWeaponUiDraw() {
    }

    static boolean inside(double d, double d2, int n, int n2, int n3, int n4) {
        return d >= (double)n && d < (double)(n + n3) && d2 >= (double)n2 && d2 < (double)(n2 + n4);
    }

    static int text(GuiGraphics guiGraphics, Font font, Component component, int n, int n2, int n3, int n4, int n5, int n6) {
        List list = font.m_92923_((FormattedText)component, Math.max(1, n3));
        int n7 = Math.min(n6, list.size());
        for (int i = 0; i < n7; ++i) {
            guiGraphics.m_280649_(font, (FormattedCharSequence)list.get(i), n, n2 + i * n5, n4, false);
        }
        return n7;
    }

    static void centered(GuiGraphics guiGraphics, Font font, Component component, int n, int n2, int n3, int n4, int n5, int n6) {
        List list = font.m_92923_((FormattedText)component, Math.max(1, n3));
        for (int i = 0; i < Math.min(n6, list.size()); ++i) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(i);
            guiGraphics.m_280649_(font, formattedCharSequence, n + (n3 - font.m_92724_(formattedCharSequence)) / 2, n2 + i * n5, n4, false);
        }
    }

    static void heading(GuiGraphics guiGraphics, Font font, int n, int n2, String string, int n3) {
        guiGraphics.m_280614_(font, (Component)Component.m_237115_((String)string), n + 1, n2 + 2, 0xFF000000 | n3, false);
        guiGraphics.m_280509_(n, n2 + 15, n + 46, n2 + 17, 0xFF000000 | n3);
    }

    static void weapon(GuiGraphics guiGraphics, int n, int n2, ItemStack itemStack, int n3) {
        guiGraphics.m_280509_(n, n2, n + 18, n2 + 18, -16185333);
        guiGraphics.m_280509_(n + 1, n2 + 1, n + 17, n2 + 17, 0xFF000000 | n3);
        guiGraphics.m_280509_(n + 2, n2 + 2, n + 16, n2 + 16, -400285143);
        if (!itemStack.m_41619_()) {
            guiGraphics.m_280480_(itemStack, n + 1, n2 + 1);
        }
    }
}

