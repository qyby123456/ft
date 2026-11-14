/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.FoodWeaponSkillWheel;
import net.farmingtales.ftboss.client.StatueWeaponUiDraw;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

final class StatueWeaponDetailsPanel {
    static final int DETAILS_PER_PAGE = 3;
    private static final int CARD_Y = 96;
    private static final int CARD_SPACING = 38;
    private static final int CARD_HEIGHT = 34;

    private StatueWeaponDetailsPanel() {
    }

    static Component rarity(FoodWeaponSkillRarity.View view) {
        return Component.m_237110_((String)"screen.ftboss.rarity.value", (Object[])new Object[]{view.rarity(), StatueWeaponDetailsPanel.number(view.multiplier())});
    }

    private static String number(double d) {
        return BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
    }

    static List<Component> rarityTooltip(FoodWeaponSkillRarity.View view) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(StatueWeaponDetailsPanel.rarity(view));
        arrayList.add(Component.m_237110_((String)"screen.ftboss.rarity.range", (Object[])new Object[]{StatueWeaponDetailsPanel.number(view.minimum()), StatueWeaponDetailsPanel.number(view.maximum())}));
        arrayList.add(Component.m_237115_((String)(view.legacy() ? "screen.ftboss.rarity.legacy_hint" : "screen.ftboss.rarity.saved_hint")));
        return List.copyOf(arrayList);
    }

    static List<Component> offeringTooltip(ItemStack itemStack) {
        FoodWeaponSkillRarity.View view = FoodWeaponSkillRarity.preview(itemStack);
        return List.of(Component.m_237110_((String)"screen.ftboss.rarity.preview", (Object[])new Object[]{StatueWeaponDetailsPanel.number(view.minimum()), StatueWeaponDetailsPanel.number(view.maximum())}), Component.m_237115_((String)"screen.ftboss.rarity.preview_hint"));
    }

    static Result render(GuiGraphics guiGraphics, Font font, int n, int n2, int n3, int n4, int n5, int n6, ItemStack itemStack, List<FoodWeaponEvolution.WeaponDetailView> list, int n7) {
        int n8;
        StatueWeaponUiDraw.heading(guiGraphics, font, n, n2, "screen.ftboss.statue_hub.details.title", n4);
        if (itemStack.m_41619_()) {
            guiGraphics.m_280509_(n, n2 + 24, n + n3, n2 + 96, -936301001);
            StatueWeaponUiDraw.text(guiGraphics, font, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.no_weapon"), n + 10, n2 + 36, n3 - 20, -6449784, 12, 4);
            return new Result(0, List.of());
        }
        List<Component> list2 = List.of();
        guiGraphics.m_280509_(n, n2 + 21, n + n3, n2 + 70, -936301001);
        StatueWeaponUiDraw.weapon(guiGraphics, n + 8, n2 + 29, itemStack, n4);
        StatueWeaponUiDraw.text(guiGraphics, font, itemStack.m_41786_(), n + 34, n2 + 27, n3 - 44, -1515055, 11, 1);
        int n9 = FoodWeaponEvolution.evolutionLevel(itemStack);
        int n10 = FoodWeaponEvolution.maxEvolutionLevel(itemStack);
        int n11 = FoodWeaponEvolution.evolutionExperience(itemStack);
        int n12 = FoodWeaponEvolution.nextEvolutionRequirement(itemStack);
        MutableComponent mutableComponent = Component.m_237110_((String)"screen.ftboss.statue_hub.details.summary", (Object[])new Object[]{n9, n10, n11, n9 >= n10 ? n11 : n12, FoodWeaponEvolution.socketCount(itemStack), FoodWeaponEvolution.socketCapacity(itemStack)});
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent, n + 34, n2 + 41, n3 - 44, -6449784, 11, 1);
        MutableComponent mutableComponent2 = Component.m_237110_((String)"screen.ftboss.skills.loadout", (Object[])new Object[]{FoodWeaponSkillLoadout.carriedSkills(itemStack).size(), FoodWeaponSkillLoadout.skills(itemStack).size(), FoodWeaponSkillWheel.keyName()});
        StatueWeaponUiDraw.text(guiGraphics, font, (Component)mutableComponent2, n + 34, n2 + 55, n3 - 44, -5858644, 11, 1);
        if (StatueWeaponUiDraw.inside(n5, n6, n, n2 + 21, n3, 49)) {
            ArrayList<MutableComponent> arrayList = new ArrayList<MutableComponent>(List.of(itemStack.m_41786_(), mutableComponent, mutableComponent2, Component.m_237115_((String)"screen.ftboss.skills.toggle_hint"), Component.m_237115_((String)"screen.ftboss.statue_hub.details.grindstone_hint")));
            arrayList.addAll(StatueWeaponDetailsPanel.rarityTooltip(FoodWeaponSkillRarity.activeView(itemStack)));
            list2 = List.copyOf(arrayList);
        }
        int n13 = Math.max(1, (list.size() + 3 - 1) / 3);
        int n14 = Math.max(0, Math.min(n7, n13 - 1));
        int n15 = n + n3 - 96;
        int n16 = n + n3 - 18;
        int n17 = n2 + 77;
        guiGraphics.m_280509_(n15, n17, n15 + 18, n17 + 13, n14 > 0 ? -936301001 : -400285143);
        guiGraphics.m_280509_(n16, n17, n16 + 18, n17 + 13, n14 + 1 < n13 ? -936301001 : -400285143);
        guiGraphics.m_280137_(font, "<", n15 + 9, n17 + 2, n14 > 0 ? -1515055 : -6449784);
        guiGraphics.m_280137_(font, ">", n16 + 9, n17 + 2, n14 + 1 < n13 ? -1515055 : -6449784);
        guiGraphics.m_280614_(font, (Component)Component.m_237110_((String)"screen.ftboss.statue_hub.details.page", (Object[])new Object[]{list.size(), n14 + 1, n13}), n + 1, n17 + 2, -6449784, false);
        if (list.isEmpty()) {
            StatueWeaponUiDraw.text(guiGraphics, font, (Component)Component.m_237115_((String)"screen.ftboss.statue_hub.details.empty"), n + 8, n2 + 109, n3 - 16, -6449784, 12, 4);
        }
        List<Integer> list3 = FoodWeaponSkillLoadout.carried(itemStack);
        for (int i = 0; i < 3 && (n8 = n14 * 3 + i) < list.size(); ++i) {
            FoodWeaponEvolution.WeaponDetailView weaponDetailView = list.get(n8);
            FoodWeaponSkillRarity.View view = weaponDetailView.selectable() ? FoodWeaponSkillRarity.stageView(itemStack, weaponDetailView.gourmetSkillIndex()) : null;
            int n18 = n2 + 96 + i * 38;
            int n19 = weaponDetailView.selected() ? -14249 : 0xFF000000 | n4;
            guiGraphics.m_280509_(n, n18, n + n3, n18 + 34, weaponDetailView.selected() ? -800309721 : -936301001);
            guiGraphics.m_280509_(n, n18, n + (weaponDetailView.selected() ? 5 : 3), n18 + 34, n19);
            StatueWeaponUiDraw.centered(guiGraphics, font, weaponDetailView.category(), n + 7, n18 + 7, 58, n19, 10, 2);
            Component component = weaponDetailView.selectable() ? Component.m_237113_((String)(list3.contains(weaponDetailView.gourmetSkillIndex()) ? "[+] " : "[ ] ")).m_7220_(weaponDetailView.title()) : weaponDetailView.title();
            StatueWeaponUiDraw.text(guiGraphics, font, component, n + 72, n18 + 4, n3 - 80, -1515055, 10, 1);
            if (view != null) {
                StatueWeaponUiDraw.text(guiGraphics, font, StatueWeaponDetailsPanel.rarity(view), n + 72, n18 + 15, n3 - 80, -1917592, 10, 1);
            }
            StatueWeaponUiDraw.text(guiGraphics, font, weaponDetailView.description(), n + 72, n18 + (view == null ? 17 : 25), n3 - 80, -6449784, 10, view == null ? 2 : 1);
            if (!StatueWeaponUiDraw.inside(n5, n6, n, n18, n3, 34)) continue;
            ArrayList<Component> arrayList = new ArrayList<Component>(List.of(weaponDetailView.title(), weaponDetailView.description()));
            if (view != null) {
                arrayList.addAll(StatueWeaponDetailsPanel.rarityTooltip(view));
                arrayList.add((Component)Component.m_237115_((String)"screen.ftboss.skills.toggle_hint"));
                arrayList.add((Component)Component.m_237115_((String)(weaponDetailView.selected() ? "screen.ftboss.statue_hub.details.gourmet_skill_current_hint" : "screen.ftboss.statue_hub.details.gourmet_skill_click_hint")));
            }
            list2 = List.copyOf(arrayList);
        }
        return new Result(n14, list2);
    }

    record Result(int page, List<Component> tooltip) {
    }
}

