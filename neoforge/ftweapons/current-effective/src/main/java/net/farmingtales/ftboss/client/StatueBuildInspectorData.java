package net.farmingtales.ftboss.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.api.SwordCavityExtensions.BondDefinition;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild.Stats;
import net.farmingtales.ftboss.util.FoodSocketPowerRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.SwordCavityRules.Link;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class StatueBuildInspectorData {
    private StatueBuildInspectorData() {
    }

    static Component text(String var0, Object... var1) {
        return Component.translatable("screen.ftboss.build_inspector." + var0, var1);
    }

    static String number(double var0) {
        return Double.isFinite(var0) ? BigDecimal.valueOf(var0).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() : "—";
    }

    static StatueBuildInspectorData.View read(ItemStack var0) {
        if (var0.isEmpty()) {
            return new StatueBuildInspectorData.View(List.of(), List.of(), List.of());
        } else {
            ArrayList var1 = new ArrayList();
            ArrayList var2 = new ArrayList();
            ArrayList var3 = new ArrayList();
            if (FourWeaponFoundationAdapter.applies(var0)) {
                net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View var4 = FourWeaponFoundationAdapter.read(var0);
                boolean var5 = var4.valid() && var4.status().ready() && !FourWeaponUseGuard.blocksUse(var0);
                List var6 = FourWeaponCavityPresentation.unlockedPaid(var4);
                addBonds(var1, SwordCavityExtensions.activeBonds(var0, var6, var5), SwordMaterialExtensions.activeBonds(var0, var6, var5));
                var3.add(new StatueBuildInspectorData.Entry(text("foundation_links"), List.of(text("foundation_links_hint")), false));
            } else if (SwordCavityBuild.applies(var0)) {
                addBonds(var1, SwordCavityExtensions.activeBonds(var0), SwordMaterialExtensions.activeBonds(var0));
                List var20 = SwordCavityBuild.links(var0);

                for (Link var8 : Link.values()) {
                    String var9 = var8.name().toLowerCase(Locale.ROOT);
                    var3.add(
                        new StatueBuildInspectorData.Entry(
                            Component.translatable("link.ftboss.sword." + var9),
                            List.of(
                                Component.translatable("link.ftboss.sword.detail." + var9),
                                bonuses(new Build(var8.durabilityBonus, var8.wearBonus, var8.damageBonus, var8.speedBonus, var8.reachBonus)),
                                text(var20.contains(var8) ? "active" : "not_active")
                            ),
                            var20.contains(var8)
                        )
                    );
                }
            }

            Stats var21 = FoodWeaponFamilyBuild.stats(var0);
            Build var23 = FourWeaponFoundationAdapter.applies(var0)
                ? FourWeaponFoundationAdapter.combat(var0)
                : (SwordCavityBuild.applies(var0) ? SwordCavityBuild.build(var0) : FoodWeaponFamilyBuild.build(var0));
            var2.add(
                new StatueBuildInspectorData.Entry(
                    text("material_values"),
                    List.of(
                        text("damage", number(var21.damageMultiplier())),
                        text("speed", number(var21.speedMultiplier())),
                        text("durability", number(var23.durability()), var21.durability()),
                        text("wear", number(var21.wear())),
                        var21.kind() == Kind.MELEE
                            ? text("reach", number(var23.reach()))
                            : text(
                                var21.kind() == Kind.STAFF ? "mana" : "range",
                                number(var21.kind() == Kind.STAFF ? var21.manaMultiplier() : var21.rangeMultiplier())
                            )
                    ),
                    true
                )
            );
            net.farmingtales.ftboss.item.FoodWeaponSkillRarity.View var25 = FoodWeaponSkillRarity.activeView(var0);
            var2.add(new StatueBuildInspectorData.Entry(text("skill_values"), StatueWeaponDetailsPanel.rarityTooltip(var25), true));
            var2.add(new StatueBuildInspectorData.Entry(text("scope"), List.of(text("scope_hint")), true));
            int[] var26 = new int[FoodType.values().length];

            for (FoodType var11 : FoodType.values()) {
                var26[var11.ordinal()] = FoodWeaponEvolution.socketTypeCount(var0, var11);
            }

            int var28 = var26[FoodType.FISH.ordinal()];
            int var30 = var26[FoodType.WINE.ordinal()];
            int var31 = var26[FoodType.MEAT.ordinal()];
            int var32 = var26[FoodType.CHEESE.ordinal()];
            var3.add(
                new StatueBuildInspectorData.Entry(
                    Component.translatable("screen.ftboss.socket.flight", var28, var30),
                    List.of(
                        Component.translatable("screen.ftboss.socket.flight.description"),
                        text(FoodSocketPowerRules.flight(var28, var30) ? "active" : "not_active")
                    ),
                    FoodSocketPowerRules.flight(var28, var30)
                )
            );
            var3.add(
                new StatueBuildInspectorData.Entry(
                    Component.translatable("screen.ftboss.socket.pack", var31, var32),
                    List.of(
                        Component.translatable("screen.ftboss.socket.pack.description"),
                        text(FoodSocketPowerRules.pack(var31, var32) ? "active" : "not_active")
                    ),
                    FoodSocketPowerRules.pack(var31, var32)
                )
            );
            boolean var12 = false;

            for (FoodType var16 : FoodType.values()) {
                var12 |= FoodSocketPowers.setBonus(var26, var16) > 0;
                if (var16 != FoodType.FOOD) {
                    String var17 = var16.name().toLowerCase(Locale.ROOT);
                    int var18 = var26[var16.ordinal()];
                    if (var16 == FoodType.SWEET) {
                        var18 += var26[FoodType.FOOD.ordinal()];
                    }

                    boolean var19 = var18 >= 3;
                    var3.add(
                        new StatueBuildInspectorData.Entry(
                            Component.translatable("food.ftboss.socket." + var17),
                            List.of(
                                text("food_progress", var18),
                                Component.translatable("tooltip.ftboss.socket.set." + var17),
                                text(var19 ? "active" : "not_active")
                            ),
                            var19
                        )
                    );
                }
            }

            var3.add(
                new StatueBuildInspectorData.Entry(
                    Component.translatable("screen.ftboss.socket.set_boost"),
                    List.of(Component.translatable("screen.ftboss.socket.set_boost.description"), text(var12 ? "active" : "not_active")),
                    var12
                )
            );
            return new StatueBuildInspectorData.View(var1, var2, var3);
        }
    }

    private static void addBonds(
        List<StatueBuildInspectorData.Entry> var0, List<BondDefinition> var1, List<net.farmingtales.ftboss.api.SwordMaterialExtensions.BondDefinition> var2
    ) {
        for (BondDefinition var4 : var1) {
            ArrayList var5 = new ArrayList();
            var5.add(var4.description());
            var4.materials()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(var1x -> var5.add(text("requirement", WeaponMaterialBuild.materialName((Material)var1x.getKey()), var1x.getValue())));
            var5.add(bonuses(var4.bonuses()));
            var0.add(new StatueBuildInspectorData.Entry(var4.name(), var5, true));
        }

        for (net.farmingtales.ftboss.api.SwordMaterialExtensions.BondDefinition var7 : var2) {
            var0.add(new StatueBuildInspectorData.Entry(var7.name(), List.of(var7.description(), bonuses(var7.bonuses())), true));
        }
    }

    private static Component bonuses(Build var0) {
        return text(
            "bonuses",
            number(var0.durability() * 100.0),
            number(var0.wear() * 100.0),
            number(var0.damage() * 100.0),
            number(var0.speed() * 100.0),
            number(var0.reach())
        );
    }

    static record Entry(Component title, List<Component> lines, boolean active) {
        Entry(Component title, List<Component> lines, boolean active) {
            lines = List.copyOf(lines);
            this.title = title;
            this.lines = lines;
            this.active = active;
        }
    }

    static record View(List<StatueBuildInspectorData.Entry> bonds, List<StatueBuildInspectorData.Entry> multipliers, List<StatueBuildInspectorData.Entry> links) {
        View(List<StatueBuildInspectorData.Entry> bonds, List<StatueBuildInspectorData.Entry> multipliers, List<StatueBuildInspectorData.Entry> links) {
            bonds = List.copyOf(bonds);
            multipliers = List.copyOf(multipliers);
            links = List.copyOf(links);
            this.bonds = bonds;
            this.multipliers = multipliers;
            this.links = links;
        }
    }
}
