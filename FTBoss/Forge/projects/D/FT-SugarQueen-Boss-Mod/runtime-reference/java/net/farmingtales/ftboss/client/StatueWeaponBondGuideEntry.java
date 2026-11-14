/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.client.StatueWeaponBondGuideScreen;
import net.farmingtales.ftboss.util.BossPreviewLayout;
import net.farmingtales.ftboss.util.WeaponBondGuideLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class StatueWeaponBondGuideEntry {
    private StatueWeaponBondGuideEntry() {
    }

    public static Button create(Screen screen) {
        Minecraft minecraft = Minecraft.m_91087_();
        AbstractContainerMenu abstractContainerMenu = minecraft.f_91074_ == null ? null : minecraft.f_91074_.f_36096_;
        int n = (screen.f_96543_ - BossPreviewLayout.modelWidth(screen.f_96543_)) / 2;
        int n2 = WeaponBondGuideLayout.entryY(screen.f_96544_);
        boolean bl = WeaponBondGuideLayout.compactEntry(screen.f_96544_);
        int n3 = n + (bl ? 8 : 148);
        int n4 = 132;
        if (bl) {
            for (GuiEventListener guiEventListener : screen.m_6702_()) {
                TranslatableContents translatableContents;
                Button button;
                ComponentContents componentContents;
                if (!(guiEventListener instanceof Button) || !((componentContents = (button = (Button)guiEventListener).m_6035_().m_214077_()) instanceof TranslatableContents) || !(translatableContents = (TranslatableContents)componentContents).m_237508_().equals("screen.ftboss.build_inspector.entry")) continue;
                button.m_257544_(Tooltip.m_257550_((Component)button.m_6035_()));
                button.m_93666_((Component)Component.m_237115_((String)"guide.ftboss.bonds.current_short"));
                button.m_93674_(64);
                n3 = n + 78;
                n4 = 64;
                break;
            }
        }
        Button button = StatueWeaponBondGuideScreen.entry(() -> minecraft.f_91074_ != null && abstractContainerMenu != null && minecraft.f_91074_.f_36096_ == abstractContainerMenu && abstractContainerMenu.m_142621_().m_41619_(), n3, n2, n4);
        if (bl && n4 == 64) {
            button.m_257544_(Tooltip.m_257550_((Component)button.m_6035_()));
            button.m_93666_((Component)Component.m_237115_((String)"guide.ftboss.bonds.entry_short"));
        }
        return button;
    }
}

