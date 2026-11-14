package net.farmingtales.ftboss.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.api.SwordMaterialDefinition.Category;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 剑材料与元数据羁绊扩展；2026-10-13 支持食物和有限环境件，价格/复杂度只接受显式信息，不依赖经济模组或自动施法。 */
public final class SwordMaterialExtensions {
    private static final Map<ResourceLocation, SwordMaterialDefinition> MATERIALS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, BondDefinition> BONDS = new ConcurrentHashMap<>();
    private static final ResourceLocation IRON_SWORD = new ResourceLocation("minecraft", "iron_sword");
    private static final ResourceLocation FED_GRIP = new ResourceLocation("ftboss", "fed_grip");

    public record Requirement(Set<Category> categories, Set<String> namespaces, int count, int minimumNutrition,
                              double minimumSaturationGain, OptionalDouble minimumPrice, OptionalInt minimumComplexity,
                              Map<ResourceLocation, Double> attributes, SwordFunctionRules.Values functions) {
        public Requirement {
            categories = Set.copyOf(categories);
            namespaces = Set.copyOf(namespaces);
            attributes = Map.copyOf(attributes);
            Objects.requireNonNull(minimumPrice);
            Objects.requireNonNull(minimumComplexity);
            Objects.requireNonNull(functions);
            if (count < 1 || count > SwordCavityRules.CELL_COUNT || minimumNutrition < 0 || minimumNutrition > 100
                    || !Double.isFinite(minimumSaturationGain) || minimumSaturationGain < 0 || minimumSaturationGain > 2000
                    || minimumPrice.isPresent() && (!Double.isFinite(minimumPrice.getAsDouble()) || minimumPrice.getAsDouble() < 0)
                    || minimumComplexity.isPresent() && (minimumComplexity.getAsInt() < 0 || minimumComplexity.getAsInt() > 64)
                    || namespaces.stream().anyMatch(namespace -> !namespace.matches("[a-z0-9_.-]+"))
                    || attributes.values().stream().anyMatch(value -> !Double.isFinite(value) || value < 0 || value > 1000000)) {
                throw new IllegalArgumentException("Invalid material bond requirement");
            }
        }

        public boolean matches(SwordMaterialDefinition material) {
            if (!material.categories().containsAll(categories) || !namespaces.isEmpty() && !namespaces.contains(material.namespace())
                    || material.nutrition() < minimumNutrition || material.saturationGain() < minimumSaturationGain
                    || minimumPrice.isPresent() && (material.price().isEmpty() || material.price().getAsDouble() < minimumPrice.getAsDouble())
                    || minimumComplexity.isPresent() && (material.processingComplexity().isEmpty()
                    || material.processingComplexity().getAsInt() < minimumComplexity.getAsInt())) return false;
            for (Map.Entry<ResourceLocation, Double> entry : attributes.entrySet()) {
                if (!material.attributes().containsKey(entry.getKey()) || material.attributes().get(entry.getKey()) < entry.getValue()) return false;
            }
            for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
                if (material.functions().value(function) < functions.value(function)) return false;
            }
            return true;
        }
    }

    public record BondDefinition(ResourceLocation id, ResourceLocation weapon, List<Requirement> requirements,
                                 Build bonuses, Set<ResourceLocation> effectIds, Component name, Component description) {
        public BondDefinition {
            Objects.requireNonNull(id);
            requirements = List.copyOf(requirements);
            effectIds = Set.copyOf(effectIds);
            Objects.requireNonNull(bonuses);
            name = Objects.requireNonNull(name).copy();
            description = Objects.requireNonNull(description).copy();
            if (requirements.isEmpty() || requirements.size() > SwordCavityRules.CELL_COUNT
                    || !Double.isFinite(bonuses.durability()) || !Double.isFinite(bonuses.wear()) || !Double.isFinite(bonuses.damage())
                    || !Double.isFinite(bonuses.speed()) || !Double.isFinite(bonuses.reach())) throw new IllegalArgumentException("Invalid material bond");
        }

        @Override
        public Component name() { return name.copy(); }

        @Override
        public Component description() { return description.copy(); }
    }

    private SwordMaterialExtensions() {}

    public static boolean register(SwordMaterialDefinition definition) {
        return MATERIALS.putIfAbsent(definition.itemId(), definition) == null;
    }

    public static void unregister(ResourceLocation item) { MATERIALS.remove(item); }

    public static boolean registered(ResourceLocation item) { return MATERIALS.containsKey(item); }

    public static boolean registerBond(BondDefinition definition) { return BONDS.putIfAbsent(definition.id(), definition) == null; }

    public static void unregisterBond(ResourceLocation id) { BONDS.remove(id); }

    public static List<BondDefinition> participatingBonds(ItemStack ingredient) {
        Optional<SwordMaterialDefinition> material = resolve(ingredient);
        if (material.isEmpty()) return List.of();
        List<BondDefinition> candidates = new ArrayList<>(BONDS.values());
        if (!BONDS.containsKey(FED_GRIP)) candidates.add(fedGrip());
        List<BondDefinition> result = new ArrayList<>();
        for (BondDefinition bond : candidates) {
            for (Requirement requirement : bond.requirements()) {
                if (requirement.matches(material.get())) {
                    result.add(bond);
                    break;
                }
            }
        }
        result.sort(Comparator.comparing(BondDefinition::id, Comparator.comparing(ResourceLocation::toString)));
        return List.copyOf(result);
    }

    public static Optional<SwordMaterialDefinition> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.isDamageableItem()) return Optional.empty();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return Optional.empty();
        SwordMaterialDefinition custom = MATERIALS.get(id);
        if (custom != null) return Optional.of(custom);
        var food = stack.getItem().getFoodProperties();
        if (food != null) {
            int nutrition = food.getNutrition();
            double saturation = food.getSaturationModifier();
            double gain = nutrition * saturation * 2;
            if (nutrition < 0 || nutrition > 100 || !Double.isFinite(saturation) || saturation < 0 || saturation > 10) return Optional.empty();
            SwordFunctionRules.Values functions = new SwordFunctionRules.Values(0.25, 0.05, Math.min(0.45, 0.08 + nutrition * 0.02),
                    Math.min(0.30, 0.04 + nutrition * 0.015 + gain * 0.015), 0.03);
            return Optional.of(new SwordMaterialDefinition(id, Material.WOOD, functions, Set.of(Category.FOOD), nutrition, saturation,
                    OptionalDouble.empty(), OptionalInt.empty(), Map.of(), stack.getHoverName()));
        }
        Material base = SwordCompatibilityRules.material(id.toString());
        if (base == Material.ORIGINAL) base = WeaponMaterialCatalog.materialFor(stack);
        if (base == Material.ORIGINAL) return Optional.empty();
        SwordFunctionRules.Values functions = SwordCompatibilityRules.itemFunctions(id.toString());
        if (functions == null) functions = SwordCompatibilityRules.materialFunctions(base);
        Set<Category> categories = WeaponMaterialCatalog.boss(base) ? Set.of(Category.BOSS)
                : WeaponMaterialCatalogRules.group(base) == WeaponMaterialCatalogRules.Group.INGOT ? Set.of(Category.METAL)
                : base == Material.WOOD ? Set.of(Category.WOOD, Category.ENVIRONMENT)
                : base == Material.STONE || base == Material.OBSIDIAN ? Set.of(Category.STONE, Category.ENVIRONMENT)
                : base == Material.LEATHER || base == Material.BONE || base == Material.COAL || base == Material.BLAZE ? Set.of(Category.ENVIRONMENT)
                : Set.of(Category.GEM);
        if (id.toString().equals("minecraft:string")) categories = Set.of(Category.ENVIRONMENT);
        categories = switch (base) {
            case WOOD_FENCE, BAMBOO_FENCE, BAMBOO -> Set.of(Category.WOOD, Category.ENVIRONMENT);
            case IRON_BARS, CHAIN, LIGHTNING_ROD -> Set.of(Category.METAL, Category.ENVIRONMENT);
            case STONE_WALL, BRICK_WALL, BLACKSTONE_WALL, DEEPSLATE_WALL, NETHER_FENCE -> Set.of(Category.STONE, Category.ENVIRONMENT);
            case END_ROD, SLIME, HONEYCOMB, GLOWSTONE -> Set.of(Category.ENVIRONMENT);
            case PRISMARINE, ECHO_SHARD -> Set.of(Category.GEM, Category.ENVIRONMENT);
            default -> categories;
        };
        if (WeaponMaterialCatalog.boss(base) && id.getPath().endsWith("_ingot")) categories = Set.of(Category.METAL, Category.BOSS);
        String affinity = SwordCompatibilityRules.affinity(id.toString());
        Map<ResourceLocation, Double> attributes = affinity.isEmpty() ? Map.of() : Map.of(new ResourceLocation("ftboss", affinity), 1.0);
        return Optional.of(new SwordMaterialDefinition(id, base, functions, categories, 0, 0, OptionalDouble.empty(), OptionalInt.empty(),
                attributes, stack.getHoverName()));
    }

    public static Material baseMaterial(ItemStack stack) { return resolve(stack).map(SwordMaterialDefinition::baseMaterial).orElse(Material.ORIGINAL); }

    public static List<SwordMaterialDefinition> installed(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon)) {
            return net.farmingtales.ftboss.item.EquipmentWeaponBuild.bondDefinitions(weapon);
        }
        if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon)) {
            return net.farmingtales.ftboss.item.FoundationAccessoryBonds.installed(weapon);
        }
        if (!SwordCavityBuild.applies(weapon)) return List.of();
        List<SwordMaterialDefinition> materials = new ArrayList<>();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) if (SwordCavityBuild.cellUnlocked(weapon, slot.ordinal())) {
            resolve(SwordCavityBuild.storedItem(weapon, slot)).ifPresent(materials::add);
        }
        return List.copyOf(materials);
    }

    public static List<BondDefinition> activeBonds(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon)) {
            return net.farmingtales.ftboss.item.FoundationAccessoryBonds.materialBonds(weapon);
        }
        if (!SwordCavityBuild.applies(weapon) || SwordCavityBuild.missingRequired(weapon)) return List.of();
        List<SwordMaterialDefinition> materials = installed(weapon);
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(weapon.getItem());
        List<BondDefinition> active = new ArrayList<>();
        List<BondDefinition> candidates = new ArrayList<>(BONDS.values());
        if (!BONDS.containsKey(FED_GRIP)) candidates.add(fedGrip());
        for (BondDefinition bond : candidates) {
            if ((bond.weapon() == null || bond.weapon().equals(id)) && bond.requirements().stream()
                    .allMatch(requirement -> materials.stream().filter(requirement::matches).count() >= requirement.count())) active.add(bond);
        }
        active.sort(Comparator.comparing(bond -> bond.id().toString()));
        return List.copyOf(active);
    }

    public static boolean hasBond(ItemStack weapon, ResourceLocation id) {
        return activeBonds(weapon).stream().anyMatch(bond -> bond.id().equals(id));
    }

    public static Set<ResourceLocation> activeEffectIds(ItemStack weapon) {
        Set<ResourceLocation> effects = new TreeSet<>(Comparator.comparing(ResourceLocation::toString));
        for (BondDefinition bond : activeBonds(weapon)) effects.addAll(bond.effectIds());
        return Set.copyOf(effects);
    }

    private static BondDefinition fedGrip() {
        Requirement metal = new Requirement(Set.of(Category.METAL), Set.of(), 2, 0, 0, OptionalDouble.empty(), OptionalInt.empty(), Map.of(), SwordFunctionRules.ZERO);
        Requirement food = new Requirement(Set.of(Category.FOOD), Set.of(), 1, 4, 1, OptionalDouble.empty(), OptionalInt.empty(), Map.of(), SwordFunctionRules.ZERO);
        return new BondDefinition(FED_GRIP, IRON_SWORD, List.of(metal, food), new Build(0, -0.03, 0, 0, 0), Set.of(),
                Component.translatable("bond.ftboss.sword.fed_grip"), Component.translatable("bond.ftboss.sword.fed_grip.detail"));
    }
}
