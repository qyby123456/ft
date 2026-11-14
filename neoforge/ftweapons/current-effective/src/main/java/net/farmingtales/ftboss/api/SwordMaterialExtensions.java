package net.farmingtales.ftboss.api;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.api.SwordMaterialDefinition.Category;
import net.farmingtales.ftboss.item.FoodSocketAttributeCatalog;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialCatalog;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules.Ingredient;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules.Group;
import net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FoundationCatalogueCopies;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SwordMaterialExtensions {
    private static final Map<ResourceLocation, SwordMaterialDefinition> MATERIALS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, SwordMaterialExtensions.BondDefinition> BONDS = new ConcurrentHashMap<>();
    private static final ResourceLocation IRON_SWORD = ResourceLocation.fromNamespaceAndPath("minecraft", "iron_sword");
    private static final ResourceLocation FED_GRIP = ResourceLocation.fromNamespaceAndPath("ftboss", "fed_grip");

    private SwordMaterialExtensions() {
    }

    public static boolean register(SwordMaterialDefinition var0) {
        return MATERIALS.putIfAbsent(var0.itemId(), var0) == null;
    }

    public static void unregister(ResourceLocation var0) {
        MATERIALS.remove(var0);
    }

    public static boolean registered(ResourceLocation var0) {
        return MATERIALS.containsKey(var0);
    }

    public static boolean registerBond(SwordMaterialExtensions.BondDefinition var0) {
        return BONDS.putIfAbsent(var0.id(), var0) == null;
    }

    public static void unregisterBond(ResourceLocation var0) {
        BONDS.remove(var0);
    }

    public static Optional<SwordMaterialDefinition> resolve(ItemStack var0) {
        if (var0 != null && !var0.isEmpty() && !var0.isDamageableItem()) {
            ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            if (var1 == null) {
                return Optional.empty();
            } else {
                SwordMaterialDefinition var2 = MATERIALS.get(var1);
                if (var2 != null) {
                    return Optional.of(var2);
                } else {
                    FoodProperties var3 = var0.getFoodProperties(null);
                    if (var3 != null) {
                        int var17 = var3.nutrition();
                        double var18 = net.farmingtales.ftboss.item.NativeMaterialFoodData.saturationModifier(var3);
                        double var19 = (double)var3.saturation();
                        if (var17 >= 0 && var17 <= 100 && Double.isFinite(var18) && !(var18 < 0.0) && !(var18 > 10.0)) {
                            Ingredient var9 = FoodSocketAttributeCatalog.descriptor(var1.toString(), var1.getNamespace());
                            OptionalDouble var10 = Double.isFinite(var9.basePrice()) && var9.basePrice() > 0.0
                                ? OptionalDouble.of(var9.basePrice())
                                : OptionalDouble.empty();

                            OptionalInt var11 = switch (var9.processing()) {
                                case RAW -> OptionalInt.of(0);
                                case SIMPLE -> OptionalInt.of(1);
                                case COMPLEX -> OptionalInt.of(2);
                                default -> OptionalInt.empty();
                            };
                            double var12 = var10.isPresent() ? (double)(FoodSocketAttributeRules.priceBand(var10.getAsDouble()) + 1) * 0.01 : 0.0;
                            double var14 = var11.isPresent() ? (double)var11.getAsInt() * 0.01 : 0.0;
                            Values var16 = new Values(
                                0.25,
                                0.05 + var12,
                                Math.min(0.45, 0.08 + (double)var17 * 0.02),
                                Math.min(0.3, 0.04 + (double)var17 * 0.015 + var19 * 0.015 + var14),
                                0.03
                            );
                            return Optional.of(
                                new SwordMaterialDefinition(
                                    var1, Material.WOOD, var16, Set.of(Category.FOOD), var17, var18, var10, var11, Map.of(), var0.getHoverName()
                                )
                            );
                        } else {
                            return Optional.empty();
                        }
                    } else {
                        Material var4 = SwordCompatibilityRules.material(var1.toString());
                        if (var4 == Material.ORIGINAL) {
                            var4 = WeaponMaterialCatalog.materialFor(var0);
                        }

                        if (var4 == Material.ORIGINAL) {
                            return Optional.empty();
                        } else {
                            Values var5 = SwordCompatibilityRules.itemFunctions(var1.toString());
                            if (var5 == null) {
                                var5 = SwordCompatibilityRules.materialFunctions(var4);
                            }

                            Set var6 = WeaponMaterialCatalog.boss(var4)
                                ? Set.of(Category.BOSS)
                                : (
                                    WeaponMaterialCatalogRules.group(var4) == Group.INGOT
                                        ? Set.of(Category.METAL)
                                        : (
                                            var4 == Material.WOOD
                                                ? Set.of(Category.WOOD, Category.ENVIRONMENT)
                                                : (
                                                    var4 != Material.STONE && var4 != Material.OBSIDIAN
                                                        ? (
                                                            var4 != Material.LEATHER
                                                                    && var4 != Material.BONE
                                                                    && var4 != Material.COAL
                                                                    && var4 != Material.BLAZE
                                                                ? Set.of(Category.GEM)
                                                                : Set.of(Category.ENVIRONMENT)
                                                        )
                                                        : Set.of(Category.STONE, Category.ENVIRONMENT)
                                                )
                                        )
                                );
                            if (var1.toString().equals("minecraft:string")) {
                                var6 = Set.of(Category.ENVIRONMENT);
                            }

                            if (WeaponMaterialCatalog.boss(var4) && var1.getPath().endsWith("_ingot")) {
                                var6 = Set.of(Category.METAL, Category.BOSS);
                            }

                            String var7 = SwordCompatibilityRules.affinity(var1.toString());
                            Map var8 = var7.isEmpty() ? Map.of() : Map.of(ResourceLocation.fromNamespaceAndPath("ftboss", var7), 1.0);
                            return Optional.of(
                                new SwordMaterialDefinition(
                                    var1, var4, var5, var6, 0, 0.0, OptionalDouble.empty(), OptionalInt.empty(), var8, var0.getHoverName()
                                )
                            );
                        }
                    }
                }
            }
        } else {
            return Optional.empty();
        }
    }

    public static Material baseMaterial(ItemStack var0) {
        return resolve(var0).map(SwordMaterialDefinition::baseMaterial).orElse(Material.ORIGINAL);
    }

    public static List<SwordMaterialDefinition> installed(ItemStack var0) {
        if (!SwordCavityBuild.applies(var0)) {
            return List.of();
        } else {
            ArrayList var1 = new ArrayList();

            for (Slot var5 : Slot.values()) {
                if (SwordCavityBuild.cellUnlocked(var0, var5.ordinal())) {
                    resolve(SwordCavityBuild.storedItem(var0, var5)).ifPresent(var1::add);
                }
            }

            return List.copyOf(var1);
        }
    }

    public static List<SwordMaterialExtensions.BondDefinition> activeBonds(ItemStack var0) {
        if (SwordCavityBuild.applies(var0) && !SwordCavityBuild.missingRequired(var0)) {
            List var1 = installed(var0);
            return matchingBonds(var0, var1);
        } else {
            return List.of();
        }
    }

    public static List<SwordMaterialExtensions.BondDefinition> activeBonds(ItemStack var0, List<ItemStack> var1, boolean var2) {
        if (!var2) {
            return List.of();
        } else {
            ArrayList var3 = new ArrayList();

            for (ItemStack var5 : var1) {
                if (!var5.isEmpty() && var5.getCount() == 1) {
                    resolve(var5).ifPresent(var3::add);
                }
            }

            return matchingBonds(var0, var3);
        }
    }

    private static CompoundTag foodCatalogueSnapshot() {
        return FoundationCatalogueCopies.copyCatalogue(SwordMaterialExtensions.FoodCatalogueRules.SNAPSHOT);
    }

    private static CompoundTag readFoodCatalogueSnapshot() {
        CompoundTag var0 = new CompoundTag();
        ArrayList<String> var1 = new ArrayList<>();

        for (int var2 = 0; var2 < 8; var2++) {
            var1.add("prices_" + var2 + ".json");
        }

        var1.add("traits.json");

        for (String var3 : var1) {
            try (InputStream var4 = FoodSocketAttributeCatalog.class.getResourceAsStream("/assets/ftboss/food_socket_attributes/" + var3)) {
                if (var4 == null) {
                    throw new IllegalArgumentException("Missing food catalogue " + var3);
                }

                byte[] var5 = var4.readNBytes(1048577);
                if (var5.length > 1048576) {
                    throw new IllegalArgumentException("Food catalogue limit");
                }

                String var6 = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(var5))
                    .toString();
                var0.put(var3, FoundationCanonicalDigest.object(JsonParser.parseString(var6)));
            } catch (IOException var9) {
                throw new IllegalArgumentException("Invalid food catalogue", var9);
            }
        }

        return var0;
    }

    public static CompoundTag foundationExtensionsSnapshot() {
        return foundationRuleSnapshot().copy();
    }

    public static String foundationExtensionsFingerprint() {
        ArrayList<SwordMaterialExtensions.BondDefinition> var0 = new ArrayList<>(BONDS.values());
        var0.sort(Comparator.comparing(var0x -> var0x.id().toString()));
        StringBuilder var1 = new StringBuilder();

        for (SwordMaterialExtensions.BondDefinition var3 : var0) {
            var1.append('|').append(var3.id()).append(':').append(var3.weapon()).append(':').append(var3.bonuses()).append(':').append(var3.requirements());
        }

        ArrayList<ResourceLocation> var5 = new ArrayList<>(MATERIALS.keySet());
        var5.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation var4 : var5) {
            var1.append('|').append(var4).append('=').append(MATERIALS.get(var4));
        }

        return var1.toString();
    }

    private static List<SwordMaterialExtensions.BondDefinition> matchingBonds(ItemStack var0, List<SwordMaterialDefinition> var1) {
        ResourceLocation var2 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        ArrayList<SwordMaterialExtensions.BondDefinition> var3 = new ArrayList<>();
        ArrayList<SwordMaterialExtensions.BondDefinition> var4 = new ArrayList<>(BONDS.values());
        if (!BONDS.containsKey(FED_GRIP)) {
            var4.add(fedGrip());
        }

        for (SwordMaterialExtensions.BondDefinition var6 : var4) {
            if ((var6.weapon() == null || var6.weapon().equals(var2))
                && var6.requirements().stream().allMatch(var1x -> var1.stream().filter(var1x::matches).count() >= (long)var1x.count())) {
                var3.add(var6);
            }
        }

        var3.sort(Comparator.comparing(var0x -> var0x.id().toString()));
        return List.copyOf(var3);
    }

    public static boolean hasBond(ItemStack var0, ResourceLocation var1) {
        return activeBonds(var0).stream().anyMatch(var1x -> var1x.id().equals(var1));
    }

    public static Set<ResourceLocation> activeEffectIds(ItemStack var0) {
        TreeSet var1 = new TreeSet<>(Comparator.comparing(ResourceLocation::toString));

        for (SwordMaterialExtensions.BondDefinition var3 : activeBonds(var0)) {
            var1.addAll(var3.effectIds());
        }

        return Set.copyOf(var1);
    }

    private static SwordMaterialExtensions.BondDefinition fedGrip() {
        SwordMaterialExtensions.Requirement var0 = new SwordMaterialExtensions.Requirement(
            Set.of(Category.METAL), Set.of(), 2, 0, 0.0, OptionalDouble.empty(), OptionalInt.empty(), Map.of(), SwordFunctionRules.ZERO
        );
        SwordMaterialExtensions.Requirement var1 = new SwordMaterialExtensions.Requirement(
            Set.of(Category.FOOD), Set.of(), 1, 4, 1.0, OptionalDouble.empty(), OptionalInt.empty(), Map.of(), SwordFunctionRules.ZERO
        );
        return new SwordMaterialExtensions.BondDefinition(
            FED_GRIP,
            IRON_SWORD,
            List.of(var0, var1),
            new Build(0.0, -0.03, 0.0, 0.0, 0.0),
            Set.of(),
            Component.translatable("bond.ftboss.sword.fed_grip"),
            Component.translatable("bond.ftboss.sword.fed_grip.detail")
        );
    }

    public static FoundationAuthorRuleSnapshots.MaterialSnapshot foundationRuleSnapshot() {
        TreeMap var0 = new TreeMap();
        var0.put("Bonds", Map.copyOf(BONDS));
        var0.put("Materials", Map.copyOf(MATERIALS));
        var0.put("FoodProviderRevision", 1);
        return FoundationAuthorRuleSnapshots.material(var0, SwordMaterialExtensions::foodCatalogueSnapshot);
    }

    public static List<SwordMaterialExtensions.BondDefinition> participatingBonds(ItemStack var0) {
        Optional var1 = resolve(var0);
        if (var1.isEmpty()) {
            return List.of();
        } else {
            ArrayList<SwordMaterialExtensions.BondDefinition> var2 = new ArrayList<>(BONDS.values());
            if (!BONDS.containsKey(FED_GRIP)) {
                var2.add(fedGrip());
            }

            ArrayList<SwordMaterialExtensions.BondDefinition> var3 = new ArrayList<>();

            for (SwordMaterialExtensions.BondDefinition var5 : var2) {
                for (SwordMaterialExtensions.Requirement var7 : var5.requirements()) {
                    if (var7.matches((SwordMaterialDefinition)var1.get())) {
                        var3.add(var5);
                        break;
                    }
                }
            }

            var3.sort(Comparator.comparing(SwordMaterialExtensions.BondDefinition::id, Comparator.comparing(ResourceLocation::toString)));
            return List.copyOf(var3);
        }
    }

    public static record BondDefinition(
        ResourceLocation id,
        ResourceLocation weapon,
        List<SwordMaterialExtensions.Requirement> requirements,
        Build bonuses,
        Set<ResourceLocation> effectIds,
        Component name,
        Component description
    ) {
        public BondDefinition(
            ResourceLocation id,
            ResourceLocation weapon,
            List<SwordMaterialExtensions.Requirement> requirements,
            Build bonuses,
            Set<ResourceLocation> effectIds,
            Component name,
            Component description
        ) {
            Objects.requireNonNull(id);
            requirements = List.copyOf(requirements);
            effectIds = Set.copyOf(effectIds);
            Objects.requireNonNull(bonuses);
            MutableComponent var10 = Objects.requireNonNull(name).copy();
            MutableComponent var11 = Objects.requireNonNull(description).copy();
            if (!requirements.isEmpty()
                && requirements.size() <= 27
                && Double.isFinite(bonuses.durability())
                && Double.isFinite(bonuses.wear())
                && Double.isFinite(bonuses.damage())
                && Double.isFinite(bonuses.speed())
                && Double.isFinite(bonuses.reach())) {
                this.id = id;
                this.weapon = weapon;
                this.requirements = requirements;
                this.bonuses = bonuses;
                this.effectIds = effectIds;
                this.name = var10;
                this.description = var11;
            } else {
                throw new IllegalArgumentException("Invalid material bond");
            }
        }

        public Component name() {
            return this.name.copy();
        }

        public Component description() {
            return this.description.copy();
        }
    }

    private static final class FoodCatalogueRules {
        private static final CompoundTag SNAPSHOT = SwordMaterialExtensions.readFoodCatalogueSnapshot();
    }

    public static record Requirement(
        Set<Category> categories,
        Set<String> namespaces,
        int count,
        int minimumNutrition,
        double minimumSaturationGain,
        OptionalDouble minimumPrice,
        OptionalInt minimumComplexity,
        Map<ResourceLocation, Double> attributes,
        Values functions
    ) {
        public Requirement(
            Set<Category> categories,
            Set<String> namespaces,
            int count,
            int minimumNutrition,
            double minimumSaturationGain,
            OptionalDouble minimumPrice,
            OptionalInt minimumComplexity,
            Map<ResourceLocation, Double> attributes,
            Values functions
        ) {
            categories = Set.copyOf(categories);
            namespaces = Set.copyOf(namespaces);
            attributes = Map.copyOf(attributes);
            Objects.requireNonNull(minimumPrice);
            Objects.requireNonNull(minimumComplexity);
            Objects.requireNonNull(functions);
            if (count >= 1
                && count <= 27
                && minimumNutrition >= 0
                && minimumNutrition <= 100
                && Double.isFinite(minimumSaturationGain)
                && !(minimumSaturationGain < 0.0)
                && !(minimumSaturationGain > 2000.0)
                && (!minimumPrice.isPresent() || Double.isFinite(minimumPrice.getAsDouble()) && !(minimumPrice.getAsDouble() < 0.0))
                && (!minimumComplexity.isPresent() || minimumComplexity.getAsInt() >= 0 && minimumComplexity.getAsInt() <= 64)
                && !namespaces.stream().anyMatch(var0 -> !var0.matches("[a-z0-9_.-]+"))
                && !attributes.values().stream().anyMatch(var0 -> !Double.isFinite(var0) || var0 < 0.0 || var0 > 1000000.0)) {
                this.categories = categories;
                this.namespaces = namespaces;
                this.count = count;
                this.minimumNutrition = minimumNutrition;
                this.minimumSaturationGain = minimumSaturationGain;
                this.minimumPrice = minimumPrice;
                this.minimumComplexity = minimumComplexity;
                this.attributes = attributes;
                this.functions = functions;
            } else {
                throw new IllegalArgumentException("Invalid material bond requirement");
            }
        }

        public boolean matches(SwordMaterialDefinition var1) {
            if (var1.categories().containsAll(this.categories)
                && (this.namespaces.isEmpty() || this.namespaces.contains(var1.namespace()))
                && var1.nutrition() >= this.minimumNutrition
                && !(var1.saturationGain() < this.minimumSaturationGain)
                && (!this.minimumPrice.isPresent() || !var1.price().isEmpty() && !(var1.price().getAsDouble() < this.minimumPrice.getAsDouble()))
                && (
                    !this.minimumComplexity.isPresent()
                        || !var1.processingComplexity().isEmpty() && var1.processingComplexity().getAsInt() >= this.minimumComplexity.getAsInt()
                )) {
                for (Entry var3 : this.attributes.entrySet()) {
                    if (!var1.attributes().containsKey(var3.getKey()) || var1.attributes().get(var3.getKey()) < (Double)var3.getValue()) {
                        return false;
                    }
                }

                for (Function var5 : Function.values()) {
                    if (var1.functions().value(var5) < this.functions.value(var5)) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }
    }
}
