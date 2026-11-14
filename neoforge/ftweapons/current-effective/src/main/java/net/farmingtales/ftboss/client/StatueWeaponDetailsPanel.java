package net.farmingtales.ftboss.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity.View;
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

    static Component rarity(View var0) {
        return Component.translatable("screen.ftboss.rarity.value", var0.rarity(), number(var0.multiplier()));
    }

    private static String number(double var0) {
        return BigDecimal.valueOf(var0).stripTrailingZeros().toPlainString();
    }

    static List<Component> rarityTooltip(View var0) {
        ArrayList var1 = new ArrayList();
        var1.add(rarity(var0));
        var1.add(Component.translatable("screen.ftboss.rarity.range", number(var0.minimum()), number(var0.maximum())));
        var1.add(Component.translatable(var0.legacy() ? "screen.ftboss.rarity.legacy_hint" : "screen.ftboss.rarity.saved_hint"));
        return List.copyOf(var1);
    }

    static List<Component> offeringTooltip(ItemStack var0) {
        View var1 = FoodWeaponSkillRarity.preview(var0);
        return List.of(
            Component.translatable("screen.ftboss.rarity.preview", number(var1.minimum()), number(var1.maximum())),
            Component.translatable("screen.ftboss.rarity.preview_hint")
        );
    }

    static StatueWeaponDetailsPanel.Result render(
        GuiGraphics var0, Font var1, int var2, int var3, int var4, int var5, int var6, int var7, ItemStack var8, List<WeaponDetailView> var9, int var10
    ) {
        StatueWeaponUiDraw.heading(var0, var1, var2, var3, "screen.ftboss.statue_hub.details.title", var5);
        if (var8.isEmpty()) {
            var0.fill(var2, var3 + 24, var2 + var4, var3 + 96, -936301001);
            StatueWeaponUiDraw.text(
                var0, var1, Component.translatable("screen.ftboss.statue_hub.details.no_weapon"), var2 + 10, var3 + 36, var4 - 20, -6449784, 12, 4
            );
            return new StatueWeaponDetailsPanel.Result(0, List.of());
        } else {
            List var11 = List.of();
            var0.fill(var2, var3 + 21, var2 + var4, var3 + 70, -936301001);
            StatueWeaponUiDraw.weapon(var0, var2 + 8, var3 + 29, var8, var5);
            StatueWeaponUiDraw.text(var0, var1, var8.getHoverName(), var2 + 34, var3 + 27, var4 - 44, -1515055, 11, 1);
            int var12 = FoodWeaponEvolution.evolutionLevel(var8);
            int var13 = FoodWeaponEvolution.maxEvolutionLevel(var8);
            int var14 = FoodWeaponEvolution.evolutionExperience(var8);
            int var15 = FoodWeaponEvolution.nextEvolutionRequirement(var8);
            MutableComponent var16 = Component.translatable(
                "screen.ftboss.statue_hub.details.summary",
                var12,
                var13,
                var14,
                var12 >= var13 ? var14 : var15,
                FoodWeaponEvolution.socketCount(var8),
                FoodWeaponEvolution.socketCapacity(var8)
            );
            StatueWeaponUiDraw.text(var0, var1, var16, var2 + 34, var3 + 41, var4 - 44, -6449784, 11, 1);
            MutableComponent var17 = Component.translatable(
                "screen.ftboss.skills.loadout",
                FoodWeaponSkillLoadout.carriedSkills(var8).size(),
                FoodWeaponSkillLoadout.skills(var8).size(),
                FoodWeaponSkillWheel.keyName()
            );
            StatueWeaponUiDraw.text(var0, var1, var17, var2 + 34, var3 + 55, var4 - 44, -5858644, 11, 1);
            if (StatueWeaponUiDraw.inside((double)var6, (double)var7, var2, var3 + 21, var4, 49)) {
                ArrayList var18 = new ArrayList<>(
                    List.of(
                        var8.getHoverName(),
                        var16,
                        var17,
                        Component.translatable("screen.ftboss.skills.toggle_hint"),
                        Component.translatable("screen.ftboss.statue_hub.details.grindstone_hint")
                    )
                );
                var18.addAll(rarityTooltip(FoodWeaponSkillRarity.activeView(var8)));
                var11 = List.copyOf(var18);
            }

            int var32 = Math.max(1, (var9.size() + 3 - 1) / 3);
            int var19 = Math.max(0, Math.min(var10, var32 - 1));
            int var20 = var2 + var4 - 96;
            int var21 = var2 + var4 - 18;
            int var22 = var3 + 77;
            var0.fill(var20, var22, var20 + 18, var22 + 13, var19 > 0 ? -936301001 : -400285143);
            var0.fill(var21, var22, var21 + 18, var22 + 13, var19 + 1 < var32 ? -936301001 : -400285143);
            var0.drawCenteredString(var1, "<", var20 + 9, var22 + 2, var19 > 0 ? -1515055 : -6449784);
            var0.drawCenteredString(var1, ">", var21 + 9, var22 + 2, var19 + 1 < var32 ? -1515055 : -6449784);
            var0.drawString(
                var1, Component.translatable("screen.ftboss.statue_hub.details.page", var9.size(), var19 + 1, var32), var2 + 1, var22 + 2, -6449784, false
            );
            if (var9.isEmpty()) {
                StatueWeaponUiDraw.text(
                    var0, var1, Component.translatable("screen.ftboss.statue_hub.details.empty"), var2 + 8, var3 + 109, var4 - 16, -6449784, 12, 4
                );
            }

            List var23 = FoodWeaponSkillLoadout.carried(var8);

            for (int var24 = 0; var24 < 3; var24++) {
                int var25 = var19 * 3 + var24;
                if (var25 >= var9.size()) {
                    break;
                }

                WeaponDetailView var26 = (WeaponDetailView)var9.get(var25);
                View var27 = var26.selectable() ? FoodWeaponSkillRarity.stageView(var8, var26.gourmetSkillIndex()) : null;
                int var28 = var3 + 96 + var24 * 38;
                int var29 = var26.selected() ? -14249 : 0xFF000000 | var5;
                var0.fill(var2, var28, var2 + var4, var28 + 34, var26.selected() ? -800309721 : -936301001);
                var0.fill(var2, var28, var2 + (var26.selected() ? 5 : 3), var28 + 34, var29);
                StatueWeaponUiDraw.centered(var0, var1, var26.category(), var2 + 7, var28 + 7, 58, var29, 10, 2);
                Object var30 = var26.selectable()
                    ? Component.literal(var23.contains(var26.gourmetSkillIndex()) ? "[+] " : "[ ] ").append(var26.title())
                    : var26.title();
                StatueWeaponUiDraw.text(var0, var1, (Component)var30, var2 + 72, var28 + 4, var4 - 80, -1515055, 10, 1);
                if (var27 != null) {
                    StatueWeaponUiDraw.text(var0, var1, rarity(var27), var2 + 72, var28 + 15, var4 - 80, -1917592, 10, 1);
                }

                StatueWeaponUiDraw.text(
                    var0, var1, var26.description(), var2 + 72, var28 + (var27 == null ? 17 : 25), var4 - 80, -6449784, 10, var27 == null ? 2 : 1
                );
                if (StatueWeaponUiDraw.inside((double)var6, (double)var7, var2, var28, var4, 34)) {
                    ArrayList var31 = new ArrayList<>(List.of(var26.title(), var26.description()));
                    if (var27 != null) {
                        var31.addAll(rarityTooltip(var27));
                        var31.add(Component.translatable("screen.ftboss.skills.toggle_hint"));
                        var31.add(
                            Component.translatable(
                                var26.selected()
                                    ? "screen.ftboss.statue_hub.details.gourmet_skill_current_hint"
                                    : "screen.ftboss.statue_hub.details.gourmet_skill_click_hint"
                            )
                        );
                    }

                    var11 = List.copyOf(var31);
                }
            }

            return new StatueWeaponDetailsPanel.Result(var19, var11);
        }
    }

    static record Result(int page, List<Component> tooltip) {
    }
}
