/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.food.FoodProperties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.api;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.item.FoodSocketAttributeCatalog;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FoundationCatalogueCopies;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SwordMaterialExtensions {
    private static final Map<ResourceLocation, SwordMaterialDefinition> MATERIALS = new ConcurrentHashMap<ResourceLocation, SwordMaterialDefinition>();
    private static final Map<ResourceLocation, BondDefinition> BONDS = new ConcurrentHashMap<ResourceLocation, BondDefinition>();
    private static final ResourceLocation IRON_SWORD = new ResourceLocation("minecraft", "iron_sword");
    private static final ResourceLocation FED_GRIP = new ResourceLocation("ftboss", "fed_grip");

    private SwordMaterialExtensions() {
    }

    public static boolean register(SwordMaterialDefinition swordMaterialDefinition) {
        return MATERIALS.putIfAbsent(swordMaterialDefinition.itemId(), swordMaterialDefinition) == null;
    }

    public static void unregister(ResourceLocation resourceLocation) {
        MATERIALS.remove(resourceLocation);
    }

    public static boolean registered(ResourceLocation resourceLocation) {
        return MATERIALS.containsKey(resourceLocation);
    }

    public static boolean registerBond(BondDefinition bondDefinition) {
        return BONDS.putIfAbsent(bondDefinition.id(), bondDefinition) == null;
    }

    public static void unregisterBond(ResourceLocation resourceLocation) {
        BONDS.remove(resourceLocation);
    }

    public static Optional<SwordMaterialDefinition> resolve(ItemStack itemStack) {
        String string;
        Set<SwordMaterialDefinition.Category> set;
        if (itemStack == null || itemStack.m_41619_() || itemStack.m_41763_()) {
            return Optional.empty();
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null) {
            return Optional.empty();
        }
        SwordMaterialDefinition swordMaterialDefinition = MATERIALS.get(resourceLocation);
        if (swordMaterialDefinition != null) {
            return Optional.of(swordMaterialDefinition);
        }
        FoodProperties foodProperties = itemStack.m_41720_().m_41473_();
        if (foodProperties != null) {
            int n = foodProperties.m_38744_();
            double d = foodProperties.m_38745_();
            double d2 = (double)n * d * 2.0;
            if (n < 0 || n > 100 || !Double.isFinite(d) || d < 0.0 || d > 10.0) {
                return Optional.empty();
            }
            FoodSocketAttributeRules.Ingredient ingredient = FoodSocketAttributeCatalog.descriptor(resourceLocation.toString(), resourceLocation.m_135827_());
            OptionalDouble optionalDouble = Double.isFinite(ingredient.basePrice()) && ingredient.basePrice() > 0.0 ? OptionalDouble.of(ingredient.basePrice()) : OptionalDouble.empty();
            OptionalInt optionalInt = switch (ingredient.processing()) {
                case FoodSocketAttributeRules.Processing.RAW -> OptionalInt.of(0);
                case FoodSocketAttributeRules.Processing.SIMPLE -> OptionalInt.of(1);
                case FoodSocketAttributeRules.Processing.COMPLEX -> OptionalInt.of(2);
                default -> OptionalInt.empty();
            };
            double d3 = optionalDouble.isPresent() ? (double)(FoodSocketAttributeRules.priceBand(optionalDouble.getAsDouble()) + 1) * 0.01 : 0.0;
            double d4 = optionalInt.isPresent() ? (double)optionalInt.getAsInt() * 0.01 : 0.0;
            SwordFunctionRules.Values values = new SwordFunctionRules.Values(0.25, 0.05 + d3, Math.min(0.45, 0.08 + (double)n * 0.02), Math.min(0.3, 0.04 + (double)n * 0.015 + d2 * 0.015 + d4), 0.03);
            return Optional.of(new SwordMaterialDefinition(resourceLocation, FoodWeaponBuildRules.Material.WOOD, values, Set.of(SwordMaterialDefinition.Category.FOOD), n, d, optionalDouble, optionalInt, Map.of(), itemStack.m_41786_()));
        }
        FoodWeaponBuildRules.Material material = SwordCompatibilityRules.material(resourceLocation.toString());
        if (material == FoodWeaponBuildRules.Material.ORIGINAL) {
            material = WeaponMaterialCatalog.materialFor(itemStack);
        }
        if (material == FoodWeaponBuildRules.Material.ORIGINAL) {
            return Optional.empty();
        }
        SwordFunctionRules.Values values = SwordCompatibilityRules.itemFunctions(resourceLocation.toString());
        if (values == null) {
            values = SwordCompatibilityRules.materialFunctions(material);
        }
        Set<SwordMaterialDefinition.Category> set2 = WeaponMaterialCatalog.boss(material) ? Set.of(SwordMaterialDefinition.Category.BOSS) : (WeaponMaterialCatalogRules.group(material) == WeaponMaterialCatalogRules.Group.INGOT ? Set.of(SwordMaterialDefinition.Category.METAL) : (material == FoodWeaponBuildRules.Material.WOOD ? Set.of(SwordMaterialDefinition.Category.WOOD, SwordMaterialDefinition.Category.ENVIRONMENT) : (material == FoodWeaponBuildRules.Material.STONE || material == FoodWeaponBuildRules.Material.OBSIDIAN ? Set.of(SwordMaterialDefinition.Category.STONE, SwordMaterialDefinition.Category.ENVIRONMENT) : (set = material == FoodWeaponBuildRules.Material.LEATHER || material == FoodWeaponBuildRules.Material.BONE || material == FoodWeaponBuildRules.Material.COAL || material == FoodWeaponBuildRules.Material.BLAZE ? Set.of(SwordMaterialDefinition.Category.ENVIRONMENT) : Set.of(SwordMaterialDefinition.Category.GEM)))));
        if (resourceLocation.toString().equals("minecraft:string")) {
            set = Set.of(SwordMaterialDefinition.Category.ENVIRONMENT);
        }
        if (WeaponMaterialCatalog.boss(material) && resourceLocation.m_135815_().endsWith("_ingot")) {
            set = Set.of(SwordMaterialDefinition.Category.METAL, SwordMaterialDefinition.Category.BOSS);
        }
        Map<ResourceLocation, Double> map = (string = SwordCompatibilityRules.affinity(resourceLocation.toString())).isEmpty() ? Map.of() : Map.of(new ResourceLocation("ftboss", string), 1.0);
        return Optional.of(new SwordMaterialDefinition(resourceLocation, material, values, set, 0, 0.0, OptionalDouble.empty(), OptionalInt.empty(), map, itemStack.m_41786_()));
    }

    public static FoodWeaponBuildRules.Material baseMaterial(ItemStack itemStack) {
        return SwordMaterialExtensions.resolve(itemStack).map(SwordMaterialDefinition::baseMaterial).orElse(FoodWeaponBuildRules.Material.ORIGINAL);
    }

    public static List<SwordMaterialDefinition> installed(ItemStack itemStack) {
        if (!SwordCavityBuild.applies(itemStack)) {
            return List.of();
        }
        ArrayList arrayList = new ArrayList();
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (!SwordCavityBuild.cellUnlocked(itemStack, slot.ordinal())) continue;
            SwordMaterialExtensions.resolve(SwordCavityBuild.storedItem(itemStack, slot)).ifPresent(arrayList::add);
        }
        return List.copyOf(arrayList);
    }

    public static List<BondDefinition> activeBonds(ItemStack itemStack) {
        if (!SwordCavityBuild.applies(itemStack) || SwordCavityBuild.missingRequired(itemStack)) {
            return List.of();
        }
        List<SwordMaterialDefinition> list = SwordMaterialExtensions.installed(itemStack);
        return SwordMaterialExtensions.matchingBonds(itemStack, list);
    }

    public static List<BondDefinition> activeBonds(ItemStack itemStack, List<ItemStack> list, boolean bl) {
        if (!bl) {
            return List.of();
        }
        ArrayList<SwordMaterialDefinition> arrayList = new ArrayList<SwordMaterialDefinition>();
        for (ItemStack itemStack2 : list) {
            if (itemStack2.m_41619_() || itemStack2.m_41613_() != 1) continue;
            SwordMaterialExtensions.resolve(itemStack2).ifPresent(arrayList::add);
        }
        return SwordMaterialExtensions.matchingBonds(itemStack, arrayList);
    }

    private static CompoundTag foodCatalogueSnapshot() {
        return FoundationCatalogueCopies.copyCatalogue(FoodCatalogueRules.SNAPSHOT);
    }

    private static CompoundTag readFoodCatalogueSnapshot() {
        CompoundTag compoundTag = new CompoundTag();
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (int i = 0; i < 8; ++i) {
            arrayList.add("prices_" + i + ".json");
        }
        arrayList.add("traits.json");
        for (String string : arrayList) {
            try {
                InputStream inputStream = FoodSocketAttributeCatalog.class.getResourceAsStream("/assets/ftboss/food_socket_attributes/" + string);
                try {
                    if (inputStream == null) {
                        throw new IllegalArgumentException("Missing food catalogue " + string);
                    }
                    byte[] byArray = inputStream.readNBytes(0x100001);
                    if (byArray.length > 0x100000) {
                        throw new IllegalArgumentException("Food catalogue limit");
                    }
                    String string2 = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(byArray)).toString();
                    compoundTag.m_128365_(string, (Tag)FoundationCanonicalDigest.object(JsonParser.parseString((String)string2)));
                }
                finally {
                    if (inputStream == null) continue;
                    inputStream.close();
                }
            }
            catch (IOException iOException) {
                throw new IllegalArgumentException("Invalid food catalogue", iOException);
            }
        }
        return compoundTag;
    }

    public static CompoundTag foundationExtensionsSnapshot() {
        return SwordMaterialExtensions.foundationRuleSnapshot().copy();
    }

    public static String foundationExtensionsFingerprint() {
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>(BONDS.values());
        arrayList.sort(Comparator.comparing(bondDefinition -> bondDefinition.id().toString()));
        StringBuilder stringBuilder = new StringBuilder();
        for (BondDefinition object2 : arrayList) {
            stringBuilder.append('|').append(object2.id()).append(':').append(object2.weapon()).append(':').append(object2.bonuses()).append(':').append(object2.requirements());
        }
        ArrayList<ResourceLocation> arrayList2 = new ArrayList<ResourceLocation>(MATERIALS.keySet());
        arrayList2.sort(Comparator.comparing(ResourceLocation::toString));
        Iterator iterator = arrayList2.iterator();
        while (iterator.hasNext()) {
            ResourceLocation resourceLocation = (ResourceLocation)iterator.next();
            stringBuilder.append('|').append(resourceLocation).append('=').append(MATERIALS.get(resourceLocation));
        }
        return stringBuilder.toString();
    }

    private static List<BondDefinition> matchingBonds(ItemStack itemStack, List<SwordMaterialDefinition> list) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>();
        ArrayList<BondDefinition> arrayList2 = new ArrayList<BondDefinition>(BONDS.values());
        if (!BONDS.containsKey(FED_GRIP)) {
            arrayList2.add(SwordMaterialExtensions.fedGrip());
        }
        for (BondDefinition bondDefinition2 : arrayList2) {
            if (bondDefinition2.weapon() != null && !bondDefinition2.weapon().equals((Object)resourceLocation) || !bondDefinition2.requirements().stream().allMatch(requirement -> list.stream().filter(requirement::matches).count() >= (long)requirement.count())) continue;
            arrayList.add(bondDefinition2);
        }
        arrayList.sort(Comparator.comparing(bondDefinition -> bondDefinition.id().toString()));
        return List.copyOf(arrayList);
    }

    public static boolean hasBond(ItemStack itemStack, ResourceLocation resourceLocation) {
        return SwordMaterialExtensions.activeBonds(itemStack).stream().anyMatch(bondDefinition -> bondDefinition.id().equals((Object)resourceLocation));
    }

    public static Set<ResourceLocation> activeEffectIds(ItemStack itemStack) {
        TreeSet<ResourceLocation> treeSet = new TreeSet<ResourceLocation>(Comparator.comparing(ResourceLocation::toString));
        for (BondDefinition bondDefinition : SwordMaterialExtensions.activeBonds(itemStack)) {
            treeSet.addAll(bondDefinition.effectIds());
        }
        return Set.copyOf(treeSet);
    }

    private static BondDefinition fedGrip() {
        Requirement requirement = new Requirement(Set.of(SwordMaterialDefinition.Category.METAL), Set.of(), 2, 0, 0.0, OptionalDouble.empty(), OptionalInt.empty(), Map.of(), SwordFunctionRules.ZERO);
        Requirement requirement2 = new Requirement(Set.of(SwordMaterialDefinition.Category.FOOD), Set.of(), 1, 4, 1.0, OptionalDouble.empty(), OptionalInt.empty(), Map.of(), SwordFunctionRules.ZERO);
        return new BondDefinition(FED_GRIP, IRON_SWORD, List.of(requirement, requirement2), new FoodWeaponBuildRules.Build(0.0, -0.03, 0.0, 0.0, 0.0), Set.of(), (Component)Component.m_237115_((String)"bond.ftboss.sword.fed_grip"), (Component)Component.m_237115_((String)"bond.ftboss.sword.fed_grip.detail"));
    }

    public static FoundationAuthorRuleSnapshots.MaterialSnapshot foundationRuleSnapshot() {
        TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
        treeMap.put("Bonds", Map.copyOf(BONDS));
        treeMap.put("Materials", Map.copyOf(MATERIALS));
        treeMap.put("FoodProviderRevision", 1);
        return FoundationAuthorRuleSnapshots.material(treeMap, SwordMaterialExtensions::foodCatalogueSnapshot);
    }

    public static List<BondDefinition> participatingBonds(ItemStack itemStack) {
        Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve(itemStack);
        if (optional.isEmpty()) {
            return List.of();
        }
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>(BONDS.values());
        if (!BONDS.containsKey(FED_GRIP)) {
            arrayList.add(SwordMaterialExtensions.fedGrip());
        }
        ArrayList<BondDefinition> arrayList2 = new ArrayList<BondDefinition>();
        block0: for (BondDefinition bondDefinition : arrayList) {
            for (Requirement requirement : bondDefinition.requirements()) {
                if (!requirement.matches(optional.get())) continue;
                arrayList2.add(bondDefinition);
                continue block0;
            }
        }
        arrayList2.sort(Comparator.comparing(BondDefinition::id, Comparator.comparing(ResourceLocation::toString)));
        return List.copyOf(arrayList2);
    }

    public record BondDefinition(ResourceLocation id, ResourceLocation weapon, List<Requirement> requirements, FoodWeaponBuildRules.Build bonuses, Set<ResourceLocation> effectIds, Component name, Component description) {
        private final Component name;
        private final Component description;

        public BondDefinition {
            Objects.requireNonNull(resourceLocation);
            list = List.copyOf(list);
            set = Set.copyOf(set);
            Objects.requireNonNull(build);
            component = Objects.requireNonNull(component).m_6881_();
            component2 = Objects.requireNonNull(component2).m_6881_();
            if (!(!list.isEmpty() && list.size() <= 27 && Double.isFinite(build.durability()) && Double.isFinite(build.wear()) && Double.isFinite(build.damage()) && Double.isFinite(build.speed()) && Double.isFinite(build.reach()))) {
                throw new IllegalArgumentException("Invalid material bond");
            }
        }

        public Component name() {
            return this.name.m_6881_();
        }

        public Component description() {
            return this.description.m_6881_();
        }
    }

    private static final class FoodCatalogueRules {
        private static final CompoundTag SNAPSHOT = SwordMaterialExtensions.readFoodCatalogueSnapshot();

        private FoodCatalogueRules() {
        }
    }

    public record Requirement(Set<SwordMaterialDefinition.Category> categories, Set<String> namespaces, int count, int minimumNutrition, double minimumSaturationGain, OptionalDouble minimumPrice, OptionalInt minimumComplexity, Map<ResourceLocation, Double> attributes, SwordFunctionRules.Values functions) {
        public Requirement {
            set = Set.copyOf(set);
            set2 = Set.copyOf(set2);
            map = Map.copyOf(map);
            Objects.requireNonNull(optionalDouble);
            Objects.requireNonNull(optionalInt);
            Objects.requireNonNull(values);
            if (n < 1 || n > 27 || n2 < 0 || n2 > 100 || !Double.isFinite(d2) || d2 < 0.0 || d2 > 2000.0 || optionalDouble.isPresent() && (!Double.isFinite(optionalDouble.getAsDouble()) || optionalDouble.getAsDouble() < 0.0) || optionalInt.isPresent() && (optionalInt.getAsInt() < 0 || optionalInt.getAsInt() > 64) || set2.stream().anyMatch(string -> !string.matches("[a-z0-9_.-]+")) || map.values().stream().anyMatch(d -> !Double.isFinite(d) || d < 0.0 || d > 1000000.0)) {
                throw new IllegalArgumentException("Invalid material bond requirement");
            }
        }

        public boolean matches(SwordMaterialDefinition swordMaterialDefinition) {
            if (!swordMaterialDefinition.categories().containsAll(this.categories) || !this.namespaces.isEmpty() && !this.namespaces.contains(swordMaterialDefinition.namespace()) || swordMaterialDefinition.nutrition() < this.minimumNutrition || swordMaterialDefinition.saturationGain() < this.minimumSaturationGain || this.minimumPrice.isPresent() && (swordMaterialDefinition.price().isEmpty() || swordMaterialDefinition.price().getAsDouble() < this.minimumPrice.getAsDouble()) || this.minimumComplexity.isPresent() && (swordMaterialDefinition.processingComplexity().isEmpty() || swordMaterialDefinition.processingComplexity().getAsInt() < this.minimumComplexity.getAsInt())) {
                return false;
            }
            for (Map.Entry<ResourceLocation, Double> entry : this.attributes.entrySet()) {
                if (swordMaterialDefinition.attributes().containsKey(entry.getKey()) && !(swordMaterialDefinition.attributes().get(entry.getKey()) < entry.getValue())) continue;
                return false;
            }
            for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
                if (!(swordMaterialDefinition.functions().value(function) < this.functions.value(function))) continue;
                return false;
            }
            return true;
        }
    }
}

