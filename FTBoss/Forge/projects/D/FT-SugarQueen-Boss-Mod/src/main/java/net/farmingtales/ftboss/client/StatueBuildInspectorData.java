package net.farmingtales.ftboss.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.FoodType;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.FoodSocketPowerRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class StatueBuildInspectorData {
    record Entry(Component title, List<Component> lines, boolean active) {
        Entry { lines = List.copyOf(lines); }
    }

    record View(List<Entry> bonds, List<Entry> multipliers, List<Entry> links) {
        View {
            bonds = List.copyOf(bonds);
            multipliers = List.copyOf(multipliers);
            links = List.copyOf(links);
        }
    }

    private StatueBuildInspectorData() {}

    static Component text(String key, Object... arguments) {
        return Component.translatable("screen.ftboss.build_inspector." + key, arguments);
    }

    static String number(double value) {
        return Double.isFinite(value) ? BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() : "—";
    }

    static View read(ItemStack weapon) {
        if (weapon.isEmpty()) return new View(List.of(), List.of(), List.of());
        List<Entry> bonds = new ArrayList<>();
        List<Entry> multipliers = new ArrayList<>();
        List<Entry> links = new ArrayList<>();
        if (FourWeaponFoundationAdapter.applies(weapon)) {
            var view = FourWeaponFoundationAdapter.read(weapon);
            boolean ready = view.valid() && view.status().ready() && !FourWeaponUseGuard.blocksUse(weapon);
            List<ItemStack> paid = FourWeaponCavityPresentation.unlockedPaid(view);
            addBonds(bonds, SwordCavityExtensions.activeBonds(weapon, paid, ready), SwordMaterialExtensions.activeBonds(weapon, paid, ready));
            links.add(new Entry(text("foundation_links"), List.of(text("foundation_links_hint")), false));
        } else if (SwordCavityBuild.applies(weapon)) {
            addBonds(bonds, SwordCavityExtensions.activeBonds(weapon), SwordMaterialExtensions.activeBonds(weapon));
            List<SwordCavityRules.Link> active = SwordCavityBuild.links(weapon);
            for (SwordCavityRules.Link link : SwordCavityRules.Link.values()) {
                String id = link.name().toLowerCase(Locale.ROOT);
                links.add(new Entry(Component.translatable("link.ftboss.sword." + id), List.of(
                        Component.translatable("link.ftboss.sword.detail." + id),
                        bonuses(new Build(link.durabilityBonus, link.wearBonus, link.damageBonus, link.speedBonus, link.reachBonus)),
                        text(active.contains(link) ? "active" : "not_active")), active.contains(link)));
            }
        }
        var stats = FoodWeaponFamilyBuild.stats(weapon);
        Build build = FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.combat(weapon)
                : SwordCavityBuild.applies(weapon) ? SwordCavityBuild.build(weapon) : FoodWeaponFamilyBuild.build(weapon);
        multipliers.add(new Entry(text("material_values"), List.of(
                text("damage", number(stats.damageMultiplier())), text("speed", number(stats.speedMultiplier())),
                text("durability", number(build.durability()), stats.durability()), text("wear", number(stats.wear())),
                stats.kind() == Kind.MELEE ? text("reach", number(build.reach()))
                        : text(stats.kind() == Kind.STAFF ? "mana" : "range", number(stats.kind() == Kind.STAFF ? stats.manaMultiplier() : stats.rangeMultiplier()))), true));
        var rarity = FoodWeaponSkillRarity.activeView(weapon);
        if (FoodWeaponFamilyBuild.isEquipment(weapon)) {
            var bonus = net.farmingtales.ftboss.item.WeaponMaterialBonuses.bonuses(weapon);
            multipliers.add(new Entry(text("accessory_values"), List.of(
                    text("accessory_spell", number(bonus.spellPower() * 100), number(bonus.maxMana())),
                    text("accessory_combat", number(bonus.attackDamage()), number(bonus.armor()), number(bonus.maxHealth())),
                    text("equipment_strength", Math.round(net.farmingtales.ftboss.item.EquipmentWeaponBuild.STRENGTH * 100))), true));
        }
        multipliers.add(new Entry(text("skill_values"), StatueWeaponDetailsPanel.rarityTooltip(rarity), true));
        multipliers.add(new Entry(text("scope"), List.of(text("scope_hint")), true));
        int[] counts = new int[FoodType.values().length];
        for (FoodType type : FoodType.values()) counts[type.ordinal()] = FoodWeaponEvolution.socketTypeCount(weapon, type);
        int fish = counts[FoodType.FISH.ordinal()];
        int wine = counts[FoodType.WINE.ordinal()];
        int meat = counts[FoodType.MEAT.ordinal()];
        int cheese = counts[FoodType.CHEESE.ordinal()];
        links.add(new Entry(Component.translatable("screen.ftboss.socket.flight", fish, wine), List.of(
                Component.translatable("screen.ftboss.socket.flight.description"),
                text(FoodSocketPowerRules.flight(fish, wine) ? "active" : "not_active")), FoodSocketPowerRules.flight(fish, wine)));
        links.add(new Entry(Component.translatable("screen.ftboss.socket.pack", meat, cheese), List.of(
                Component.translatable("screen.ftboss.socket.pack.description"),
                text(FoodSocketPowerRules.pack(meat, cheese) ? "active" : "not_active")), FoodSocketPowerRules.pack(meat, cheese)));
        boolean boosted = false;
        for (FoodType type : FoodType.values()) {
            boosted |= FoodSocketPowers.setBonus(counts, type) > 0;
            if (type == FoodType.FOOD) continue;
            String id = type.name().toLowerCase(Locale.ROOT);
            int count = counts[type.ordinal()];
            if (type == FoodType.SWEET) count += counts[FoodType.FOOD.ordinal()];
            boolean active = count >= 3;
            links.add(new Entry(Component.translatable("food.ftboss.socket." + id), List.of(
                    text("food_progress", count), Component.translatable("tooltip.ftboss.socket.set." + id),
                    text(active ? "active" : "not_active")), active));
        }
        links.add(new Entry(Component.translatable("screen.ftboss.socket.set_boost"), List.of(
                Component.translatable("screen.ftboss.socket.set_boost.description"), text(boosted ? "active" : "not_active")), boosted));
        return new View(bonds, multipliers, links);
    }

    private static void addBonds(List<Entry> target, List<SwordCavityExtensions.BondDefinition> cavity,
                                 List<SwordMaterialExtensions.BondDefinition> material) {
        for (var bond : cavity) {
            List<Component> lines = new ArrayList<>();
            lines.add(bond.description());
            bond.materials().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry ->
                    lines.add(text("requirement", net.farmingtales.ftboss.item.WeaponMaterialBuild.materialName(entry.getKey()), entry.getValue())));
            lines.add(bonuses(bond.bonuses()));
            target.add(new Entry(bond.name(), lines, true));
        }
        for (var bond : material) {
            target.add(new Entry(bond.name(), List.of(bond.description(), bonuses(bond.bonuses())), true));
        }
    }

    private static Component bonuses(Build build) {
        return text("bonuses", number(build.durability() * 100), number(build.wear() * 100),
                number(build.damage() * 100), number(build.speed() * 100), number(build.reach()));
    }
}
