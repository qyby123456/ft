/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.client.FourWeaponCavityPresentation;
import net.farmingtales.ftboss.client.StatueWeaponDetailsPanel;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.FoodSocketPowerRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class StatueBuildInspectorData {
    private StatueBuildInspectorData() {
    }

    static Component text(String string, Object ... objectArray) {
        return Component.m_237110_((String)("screen.ftboss.build_inspector." + string), (Object[])objectArray);
    }

    static String number(double d) {
        return Double.isFinite(d) ? BigDecimal.valueOf(d).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() : "\u2014";
    }

    static View read(ItemStack itemStack) {
        Object object;
        Object object2;
        if (itemStack.m_41619_()) {
            return new View(List.of(), List.of(), List.of());
        }
        ArrayList<Entry> arrayList = new ArrayList<Entry>();
        ArrayList<Entry> arrayList2 = new ArrayList<Entry>();
        ArrayList<Entry> arrayList3 = new ArrayList<Entry>();
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            object2 = FourWeaponFoundationAdapter.read(itemStack);
            boolean bl = ((FourWeaponFoundationAdapter.View)object2).valid() && ((FourWeaponFoundationAdapter.View)object2).status().ready() && !FourWeaponUseGuard.blocksUse(itemStack);
            object = FourWeaponCavityPresentation.unlockedPaid((FourWeaponFoundationAdapter.View)object2);
            StatueBuildInspectorData.addBonds(arrayList, SwordCavityExtensions.activeBonds(itemStack, object, bl), SwordMaterialExtensions.activeBonds(itemStack, object, bl));
            arrayList3.add(new Entry(StatueBuildInspectorData.text("foundation_links", new Object[0]), List.of(StatueBuildInspectorData.text("foundation_links_hint", new Object[0])), false));
        } else if (SwordCavityBuild.applies(itemStack)) {
            StatueBuildInspectorData.addBonds(arrayList, SwordCavityExtensions.activeBonds(itemStack), SwordMaterialExtensions.activeBonds(itemStack));
            object2 = SwordCavityBuild.links(itemStack);
            for (SwordCavityRules.Link foodTypeArray : SwordCavityRules.Link.values()) {
                String n2 = foodTypeArray.name().toLowerCase(Locale.ROOT);
                arrayList3.add(new Entry((Component)Component.m_237115_((String)("link.ftboss.sword." + n2)), List.of(Component.m_237115_((String)("link.ftboss.sword.detail." + n2)), StatueBuildInspectorData.bonuses(new FoodWeaponBuildRules.Build(foodTypeArray.durabilityBonus, foodTypeArray.wearBonus, foodTypeArray.damageBonus, foodTypeArray.speedBonus, foodTypeArray.reachBonus)), StatueBuildInspectorData.text(object2.contains((Object)foodTypeArray) ? "active" : "not_active", new Object[0])), object2.contains((Object)foodTypeArray)));
            }
        }
        object2 = FoodWeaponFamilyBuild.stats(itemStack);
        FoodWeaponBuildRules.Build build = FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.combat(itemStack) : (SwordCavityBuild.applies(itemStack) ? SwordCavityBuild.build(itemStack) : FoodWeaponFamilyBuild.build(itemStack));
        arrayList2.add(new Entry(StatueBuildInspectorData.text("material_values", new Object[0]), List.of(StatueBuildInspectorData.text("damage", StatueBuildInspectorData.number(((FoodWeaponFamilyBuild.Stats)object2).damageMultiplier())), StatueBuildInspectorData.text("speed", StatueBuildInspectorData.number(((FoodWeaponFamilyBuild.Stats)object2).speedMultiplier())), StatueBuildInspectorData.text("durability", StatueBuildInspectorData.number(build.durability()), ((FoodWeaponFamilyBuild.Stats)object2).durability()), StatueBuildInspectorData.text("wear", StatueBuildInspectorData.number(((FoodWeaponFamilyBuild.Stats)object2).wear())), ((FoodWeaponFamilyBuild.Stats)object2).kind() == FoodWeaponFamilyRules.Kind.MELEE ? StatueBuildInspectorData.text("reach", StatueBuildInspectorData.number(build.reach())) : StatueBuildInspectorData.text(((FoodWeaponFamilyBuild.Stats)object2).kind() == FoodWeaponFamilyRules.Kind.STAFF ? "mana" : "range", StatueBuildInspectorData.number(((FoodWeaponFamilyBuild.Stats)object2).kind() == FoodWeaponFamilyRules.Kind.STAFF ? ((FoodWeaponFamilyBuild.Stats)object2).manaMultiplier() : ((FoodWeaponFamilyBuild.Stats)object2).rangeMultiplier()))), true));
        object = FoodWeaponSkillRarity.activeView(itemStack);
        arrayList2.add(new Entry(StatueBuildInspectorData.text("skill_values", new Object[0]), StatueWeaponDetailsPanel.rarityTooltip((FoodWeaponSkillRarity.View)object), true));
        arrayList2.add(new Entry(StatueBuildInspectorData.text("scope", new Object[0]), List.of(StatueBuildInspectorData.text("scope_hint", new Object[0])), true));
        int[] nArray = new int[FoodWeaponEvolution.FoodType.values().length];
        for (FoodWeaponEvolution.FoodType n4 : FoodWeaponEvolution.FoodType.values()) {
            nArray[n4.ordinal()] = FoodWeaponEvolution.socketTypeCount(itemStack, n4);
        }
        int n = nArray[FoodWeaponEvolution.FoodType.FISH.ordinal()];
        int n2 = nArray[FoodWeaponEvolution.FoodType.WINE.ordinal()];
        int n3 = nArray[FoodWeaponEvolution.FoodType.MEAT.ordinal()];
        int n4 = nArray[FoodWeaponEvolution.FoodType.CHEESE.ordinal()];
        arrayList3.add(new Entry((Component)Component.m_237110_((String)"screen.ftboss.socket.flight", (Object[])new Object[]{n, n2}), List.of(Component.m_237115_((String)"screen.ftboss.socket.flight.description"), StatueBuildInspectorData.text(FoodSocketPowerRules.flight(n, n2) ? "active" : "not_active", new Object[0])), FoodSocketPowerRules.flight(n, n2)));
        arrayList3.add(new Entry((Component)Component.m_237110_((String)"screen.ftboss.socket.pack", (Object[])new Object[]{n3, n4}), List.of(Component.m_237115_((String)"screen.ftboss.socket.pack.description"), StatueBuildInspectorData.text(FoodSocketPowerRules.pack(n3, n4) ? "active" : "not_active", new Object[0])), FoodSocketPowerRules.pack(n3, n4)));
        boolean bl = false;
        for (FoodWeaponEvolution.FoodType foodType : FoodWeaponEvolution.FoodType.values()) {
            bl |= FoodSocketPowers.setBonus(nArray, foodType) > 0;
            if (foodType == FoodWeaponEvolution.FoodType.FOOD) continue;
            String string = foodType.name().toLowerCase(Locale.ROOT);
            int n5 = nArray[foodType.ordinal()];
            if (foodType == FoodWeaponEvolution.FoodType.SWEET) {
                n5 += nArray[FoodWeaponEvolution.FoodType.FOOD.ordinal()];
            }
            boolean bl2 = n5 >= 3;
            arrayList3.add(new Entry((Component)Component.m_237115_((String)("food.ftboss.socket." + string)), List.of(StatueBuildInspectorData.text("food_progress", n5), Component.m_237115_((String)("tooltip.ftboss.socket.set." + string)), StatueBuildInspectorData.text(bl2 ? "active" : "not_active", new Object[0])), bl2));
        }
        arrayList3.add(new Entry((Component)Component.m_237115_((String)"screen.ftboss.socket.set_boost"), List.of(Component.m_237115_((String)"screen.ftboss.socket.set_boost.description"), StatueBuildInspectorData.text(bl ? "active" : "not_active", new Object[0])), bl));
        return new View(arrayList, arrayList2, arrayList3);
    }

    private static void addBonds(List<Entry> list, List<SwordCavityExtensions.BondDefinition> list2, List<SwordMaterialExtensions.BondDefinition> list3) {
        for (SwordCavityExtensions.BondDefinition record : list2) {
            ArrayList<Component> arrayList = new ArrayList<Component>();
            arrayList.add(record.description());
            record.materials().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> arrayList.add(StatueBuildInspectorData.text("requirement", WeaponMaterialBuild.materialName((FoodWeaponBuildRules.Material)((Object)((Object)((Object)entry.getKey())))), entry.getValue())));
            arrayList.add(StatueBuildInspectorData.bonuses(record.bonuses()));
            list.add(new Entry(record.name(), arrayList, true));
        }
        for (SwordMaterialExtensions.BondDefinition bondDefinition : list3) {
            list.add(new Entry(bondDefinition.name(), List.of(bondDefinition.description(), StatueBuildInspectorData.bonuses(bondDefinition.bonuses())), true));
        }
    }

    private static Component bonuses(FoodWeaponBuildRules.Build build) {
        return StatueBuildInspectorData.text("bonuses", StatueBuildInspectorData.number(build.durability() * 100.0), StatueBuildInspectorData.number(build.wear() * 100.0), StatueBuildInspectorData.number(build.damage() * 100.0), StatueBuildInspectorData.number(build.speed() * 100.0), StatueBuildInspectorData.number(build.reach()));
    }

    record View(List<Entry> bonds, List<Entry> multipliers, List<Entry> links) {
        View {
            list = List.copyOf(list);
            list2 = List.copyOf(list2);
            list3 = List.copyOf(list3);
        }
    }

    record Entry(Component title, List<Component> lines, boolean active) {
        Entry {
            list = List.copyOf(list);
        }
    }
}

