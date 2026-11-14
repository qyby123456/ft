package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponBossPassive;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

final class StatueWeaponDetailsCache {
    private static final Map<Object, Frame> FRAMES = new WeakHashMap<>();

    record Card(Component title, Component power, List<Component> tooltip) {}
    record Frame(ItemStack weapon, Language language, Component key, List<WeaponDetailView> details,
                 Component summary, Component familySummary, List<Component> headerTooltip, List<Card> cards) {}

    private StatueWeaponDetailsCache() {}

    static Frame frame(Object screen, ItemStack weapon) {
        Language language = Language.getInstance();
        Component key = FoodWeaponSkillWheel.keyName();
        Frame cached = FRAMES.get(screen);
        if (cached != null && cached.language() == language && Objects.equals(cached.key(), key)
                && cached.weapon().getCount() == weapon.getCount()
                && ItemStack.isSameItemSameTags(cached.weapon(), weapon)) return cached;
        Frame current = build(weapon, language, key);
        FRAMES.put(screen, current);
        return current;
    }

    private static Frame build(ItemStack weapon, Language language, Component key) {
        if (weapon.isEmpty()) return new Frame(ItemStack.EMPTY, language, key, List.of(),
                Component.empty(), Component.empty(), List.of(), List.of());
        List<WeaponDetailView> details = new ArrayList<>(FoodWeaponEvolution.weaponDetailViews(weapon));
        WeaponDetailView passive = FoodWeaponBossPassive.detail(weapon);
        if (passive != null) details.add(passive);
        int level = FoodWeaponEvolution.evolutionLevel(weapon);
        int maximum = FoodWeaponEvolution.maxEvolutionLevel(weapon);
        int experience = FoodWeaponEvolution.evolutionExperience(weapon);
        int required = FoodWeaponEvolution.nextEvolutionRequirement(weapon);
        Component summary = Component.translatable("screen.ftboss.statue_hub.details.summary", level, maximum, experience,
                level >= maximum ? experience : required, FoodWeaponEvolution.socketCount(weapon), FoodWeaponEvolution.socketCapacity(weapon));
        List<Integer> carried = FoodWeaponSkillLoadout.carried(weapon);
        var available = new LinkedHashSet<Integer>();
        List<Card> cards = new ArrayList<>(details.size());
        for (WeaponDetailView detail : details) {
            if (detail.selectable() && detail.gourmetSkillIndex() < WeaponSkillLoadoutRules.MAX_SKILLS) available.add(detail.gourmetSkillIndex());
            var power = detail.selectable() ? FoodWeaponSkillRarity.stageView(weapon, detail.gourmetSkillIndex()) : null;
            Component title = detail.selectable() ? Component.literal(carried.contains(detail.gourmetSkillIndex()) ? "[+] " : "[ ] ")
                    .append(detail.title()) : detail.title();
            List<Component> tooltip = new ArrayList<>(List.of(detail.title(), detail.description()));
            if (power != null) {
                tooltip.addAll(StatueWeaponDetailsPanel.rarityTooltip(power));
                tooltip.add(Component.translatable("screen.ftboss.skills.toggle_hint"));
                tooltip.add(Component.translatable(detail.selected() ? "screen.ftboss.statue_hub.details.gourmet_skill_current_hint"
                        : "screen.ftboss.statue_hub.details.gourmet_skill_click_hint"));
            }
            cards.add(new Card(title, power == null ? null : StatueWeaponDetailsPanel.rarity(power), List.copyOf(tooltip)));
        }
        long carriedCount = available.stream().filter(carried::contains).count();
        Component loadout = Component.translatable("screen.ftboss.skills.loadout", (int) carriedCount, available.size(), key);
        Component familySummary = loadout;
        List<Component> header = new ArrayList<>(List.of(weapon.getHoverName(), summary, loadout,
                Component.translatable("screen.ftboss.skills.toggle_hint"),
                Component.translatable("screen.ftboss.statue_hub.details.grindstone_hint")));
        header.addAll(StatueWeaponDetailsPanel.rarityTooltip(FoodWeaponSkillRarity.activeView(weapon)));
        if (FoodWeaponFamilyBuild.kind(weapon) != Kind.MELEE) {
            var stats = FoodWeaponFamilyBuild.stats(weapon);
            List<Component> buildLines = StatueWeaponUiDraw.buildLines(stats, null);
            familySummary = StatueWeaponUiDraw.familyName(stats.kind()).copy().append(Component.literal(" · ")).append(buildLines.get(0));
            header.add(StatueWeaponUiDraw.familyName(stats.kind()));
            header.addAll(buildLines);
            header.addAll(StatueWeaponUiDraw.familyTooltip(weapon, stats));
            List<Material> materials = WeaponMaterialBuild.materials(weapon);
            for (Part part : Part.values()) {
                header.add(Component.translatable("screen.ftboss.material.part", FoodWeaponFamilyBuild.partName(weapon, part),
                        WeaponMaterialBuild.materialName(materials.get(part.ordinal()))));
            }
        }
        return new Frame(weapon.copy(), language, key, List.copyOf(details), summary, familySummary, List.copyOf(header), List.copyOf(cards));
    }
}
