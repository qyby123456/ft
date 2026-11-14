package net.farmingtales.ftboss.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.api.SwordMaterialDefinition.Category;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.NativeRepairStackImage;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Cell;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SwordCavityExtensions {
    public static final int API_VERSION = 2;
    private static final Map<ResourceLocation, int[]> LAYOUTS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> RECIPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Profile> FOUNDATION_RECIPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, SwordCavityExtensions.BondDefinition> BONDS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, net.farmingtales.ftboss.util.SwordFunctionRules.Profile> FUNCTIONS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, List<Function>> FUNCTION_AXES = new ConcurrentHashMap<>();
    private static final ResourceLocation IRON_SWORD = ResourceLocation.fromNamespaceAndPath("minecraft", "iron_sword");

    private SwordCavityExtensions() {
    }

    public static int[] validateLayout(int[] var0) {
        if (var0 != null && var0.length == 27) {
            boolean[] var1 = new boolean[var0.length];

            for (int var5 : var0) {
                if (var5 < 0 || var5 >= var1.length || var1[var5]) {
                    throw new IllegalArgumentException("Layout must be a permutation of 0..26");
                }

                var1[var5] = true;
            }

            return (int[])var0.clone();
        } else {
            throw new IllegalArgumentException("Layout requires 27 cells");
        }
    }

    public static boolean registerLayout(ResourceLocation var0, int[] var1) {
        return LAYOUTS.putIfAbsent(Objects.requireNonNull(var0), validateLayout(var1)) == null;
    }

    public static synchronized boolean registerNativeRecipe(ResourceLocation var0, ResourceLocation var1) {
        return RECIPES.putIfAbsent(Objects.requireNonNull(var0), Objects.requireNonNull(var1)) == null;
    }

    public static synchronized boolean registerNativeRecipe(ResourceLocation var0, RecipeHolder<?> holder, RegistryAccess var2, Profile var3) {
        Recipe<?> var1 = holder == null ? null : holder.value();
        if (var0 != null
            && var1 != null
            && var3 != null
            && var0.toString().equals(var3.id())
            && !RECIPES.containsKey(var0)
            && !FOUNDATION_RECIPES.containsKey(var0)) {
            try {
                CompoundTag var4 = FoundationApprovedRules.recipeEvidence(holder, var2);
                if (var4 != null && var4.contains("Result", 10) && var4.getCompound("Result").getString("id").equals(var3.id())) {
                    TreeMap<String, Integer> var5 = new TreeMap<>();
                    TreeMap<String, net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values> var6 = new TreeMap<>(var3.items());
                    TreeMap<Integer, Cell> var7 = new TreeMap<>();
                    ArrayList var8 = new ArrayList();

                    for (int var9 = 0; var9 < 27; var9++) {
                        if (FourWeaponFoundationRules.unlocked(var3, var9, 0)) {
                            var8.add(var9);
                        }
                    }

                    ListTag var19 = var4.getList("Ingredients", 10);

                    for (int var10 = 0; var10 < var19.size(); var10++) {
                        CompoundTag var11 = var19.getCompound(var10);
                        if (!var11.getString("State").equals("EMPTY") && !var11.getString("Role").equals("template")) {
                            ListTag var12 = var11.getList("Alternatives", 10);
                            if (var12.size() != 1 || var7.size() >= var8.size()) {
                                return false;
                            }

                            ItemStack var13 = NativeRepairStackImage.decode(var2, var12.getCompound(0).getCompound("Item"));
                            if (var13.isEmpty() || var13.getCount() != 1 || var13.isDamageableItem()) {
                                return false;
                            }

                            String var14 = BuiltInRegistries.ITEM.getKey(var13.getItem()).toString();
                            Material var15 = SwordMaterialExtensions.baseMaterial(var13);
                            if (!var3.items().containsKey(var14) && !var3.materials().containsKey(var15)) {
                                return false;
                            }

                            Optional var16 = SwordMaterialExtensions.resolve(var13);
                            if (var16.isPresent() && ((SwordMaterialDefinition)var16.get()).categories().contains(Category.FOOD)) {
                                Values var17 = ((SwordMaterialDefinition)var16.get()).functions();
                                var6.put(
                                    var14,
                                    new net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values(
                                        var17.handle(), var17.guard(), var17.core(), var17.stability(), var17.blade()
                                    )
                                );
                            }

                            var5.merge(var14, 1, Integer::sum);
                            var7.put((Integer)var8.get(var7.size()), new Cell(var14, var15, "native-reference", ""));
                        }
                    }

                    if (var7.isEmpty()) {
                        return false;
                    } else {
                        net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values var20 = FourWeaponFoundationRules.ZERO;

                        for (Entry<Integer, Cell> var23 : var7.entrySet()) {
                            net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values var25 = var6.getOrDefault(
                                ((Cell)var23.getValue()).itemId(), var3.materials().get(((Cell)var23.getValue()).material())
                            );
                            if (var25 == null) {
                                return false;
                            }

                            var20 = var20.plus(var3.bases().get((Integer)var23.getKey())).plus(var25.times(var3.weights().get((Integer)var23.getKey())));
                        }

                        net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values var22 = var20.scale(0.995);
                        Profile var24 = new Profile(
                            var3.id(),
                            var3.family(),
                            var3.axes(),
                            var22,
                            var3.weights(),
                            var3.bases(),
                            var3.unlocks(),
                            var3.materials(),
                            var6,
                            var7,
                            holder.id().toString(),
                            var5,
                            Map.of()
                        );
                        FOUNDATION_RECIPES.put(var0, var24);
                        RECIPES.put(var0, holder.id());
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (LinkageError | RuntimeException var18) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean registerBond(SwordCavityExtensions.BondDefinition var0) {
        return BONDS.putIfAbsent(var0.id(), var0) == null;
    }

    public static boolean registerFunctions(ResourceLocation var0, net.farmingtales.ftboss.util.SwordFunctionRules.Profile var1) {
        return FUNCTIONS.putIfAbsent(Objects.requireNonNull(var0), Objects.requireNonNull(var1)) == null;
    }

    public static void unregisterFunctions(ResourceLocation var0) {
        FUNCTIONS.remove(var0);
        FUNCTION_AXES.remove(var0);
    }

    public static net.farmingtales.ftboss.util.SwordFunctionRules.Profile registeredFunctionProfile(ItemStack var0) {
        return FUNCTIONS.get(weaponId(var0));
    }

    public static boolean registerFunctions(ResourceLocation var0, net.farmingtales.ftboss.util.SwordFunctionRules.Profile var1, List<Function> var2) {
        var2 = List.copyOf(var2);
        if (var2.size() != 5 || var2.stream().distinct().count() != 5L) {
            throw new IllegalArgumentException("Explicit five-axis permutation required");
        } else if (!registerFunctions(var0, var1)) {
            return false;
        } else {
            FUNCTION_AXES.put(var0, var2);
            return true;
        }
    }

    public static int[] registeredLayout(ItemStack var0) {
        int[] var1 = LAYOUTS.get(weaponId(var0));
        return var1 == null ? null : (int[])var1.clone();
    }

    public static synchronized ResourceLocation registeredNativeRecipe(ItemStack var0) {
        return RECIPES.get(weaponId(var0));
    }

    public static synchronized Profile foundationProfile(ItemStack var0, Profile var1) {
        ResourceLocation var2 = registeredNativeRecipe(var0);
        if (var2 != null) {
            Profile var3 = FOUNDATION_RECIPES.get(weaponId(var0));
            if (var3 == null || !var3.recipeEvidence().equals(var2.toString())) {
                return null;
            }

            var1 = var3;
        }

        net.farmingtales.ftboss.util.SwordFunctionRules.Profile var9 = registeredFunctionProfile(var0);
        if (var9 == null) {
            return var1;
        } else {
            List var4 = FUNCTION_AXES.get(weaponId(var0));
            if (var4 == null) {
                return null;
            } else {
                ArrayList var5 = new ArrayList();
                ArrayList var6 = new ArrayList();

                for (int var7 = 0; var7 < 27; var7++) {
                    var5.add(foundationValues(var9.cellWeights().get(var7), var4));
                    var6.add(foundationValues(var9.cellBaseValues().get(var7), var4));
                }

                EnumMap var10 = new EnumMap<>(Material.class);
                var9.materials().forEach((var2x, var3x) -> var10.put(var2x, foundationValues(var3x, var4)));
                HashMap var8 = new HashMap();
                var9.itemValues().forEach((var2x, var3x) -> var8.put(var2x, foundationValues(var3x, var4)));
                return new Profile(
                    var1.id(),
                    var1.family(),
                    var1.axes(),
                    foundationValues(var9.minimum(), var4),
                    var5,
                    var6,
                    var9.unlockLevels(),
                    var10,
                    var8,
                    var1.nativeCells(),
                    var1.recipeEvidence(),
                    var1.recipeBudget(),
                    var1.environmentBudget()
                );
            }
        }
    }

    private static net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values foundationValues(Values var0, List<Function> var1) {
        return new net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values(
            var0.value((Function)var1.get(0)),
            var0.value((Function)var1.get(1)),
            var0.value((Function)var1.get(2)),
            var0.value((Function)var1.get(3)),
            var0.value((Function)var1.get(4))
        );
    }

    public static CompoundTag foundationExtensionsSnapshot(ItemStack var0) {
        TreeMap var1 = new TreeMap();
        var1.put("Layout", registeredLayout(var0));
        var1.put("NativeRecipe", registeredNativeRecipe(var0));
        var1.put("FoundationRecipeProfile", FOUNDATION_RECIPES.get(weaponId(var0)));
        var1.put("Bonds", Map.copyOf(BONDS));
        return FoundationCanonicalDigest.object(var1);
    }

    public static String foundationExtensionsFingerprint(ItemStack var0) {
        StringBuilder var1 = new StringBuilder();
        int[] var2 = registeredLayout(var0);
        var1.append(Arrays.toString(var2)).append('|').append(registeredNativeRecipe(var0));
        ArrayList<SwordCavityExtensions.BondDefinition> var3 = new ArrayList<>(BONDS.values());
        var3.sort(Comparator.comparing(var0x -> var0x.id().toString()));

        for (SwordCavityExtensions.BondDefinition var5 : var3) {
            var1.append('|').append(var5.id()).append(':').append(var5.weapon()).append(':').append(var5.bonuses());

            for (Material var9 : Material.values()) {
                if (var5.materials().containsKey(var9)) {
                    var1.append(':').append(var9).append('=').append(var5.materials().get(var9));
                }
            }
        }

        return var1.toString();
    }

    public static List<SwordCavityExtensions.BondDefinition> activeBonds(ItemStack var0, List<ItemStack> var1, boolean var2) {
        if (!var2) {
            return List.of();
        } else {
            EnumMap<Material, Integer> var3 = new EnumMap<>(Material.class);

            for (ItemStack var5 : var1) {
                if (!var5.isEmpty() && var5.getCount() == 1) {
                    Material var6 = SwordMaterialExtensions.baseMaterial(var5);
                    if (var6 != Material.ORIGINAL) {
                        var3.merge(var6, 1, Integer::sum);
                    }
                }
            }

            ResourceLocation var8 = weaponId(var0);
            ArrayList<SwordCavityExtensions.BondDefinition> var9 = new ArrayList<>();

            for (SwordCavityExtensions.BondDefinition var7 : BONDS.values()) {
                if ((var7.weapon() == null || var7.weapon().equals(var8))
                    && var7.materials().entrySet().stream().allMatch(var1x -> var3.getOrDefault(var1x.getKey(), 0) >= var1x.getValue())) {
                    var9.add(var7);
                }
            }

            var9.sort(Comparator.comparing(var0x -> var0x.id().toString()));
            return List.copyOf(var9);
        }
    }

    public static Build applyBuild(ItemStack var0, Build var1, List<ItemStack> var2, boolean var3) {
        List var4 = activeBonds(var0, var2, var3);
        List var5 = SwordMaterialExtensions.activeBonds(var0, var2, var3);
        return applyBonds(var1, var4, var5);
    }

    public static net.farmingtales.ftboss.util.SwordFunctionRules.Profile functionProfile(ItemStack var0) {
        if (!SwordCavityBuild.applies(var0)) {
            return null;
        } else {
            ResourceLocation var1 = weaponId(var0);
            net.farmingtales.ftboss.util.SwordFunctionRules.Profile var2 = var1 == null ? null : FUNCTIONS.get(var1);
            return var2 != null ? var2 : (var1 == null ? null : SwordCompatibilityRules.profile(var1.toString(), SwordNativeMaterials.budget(var0)));
        }
    }

    public static void unregisterLayout(ResourceLocation var0) {
        LAYOUTS.remove(var0);
    }

    public static synchronized void unregisterNativeRecipe(ResourceLocation var0) {
        RECIPES.remove(var0);
        FOUNDATION_RECIPES.remove(var0);
    }

    public static void unregisterBond(ResourceLocation var0) {
        BONDS.remove(var0);
    }

    private static ResourceLocation weaponId(ItemStack var0) {
        return BuiltInRegistries.ITEM.getKey(var0.getItem());
    }

    public static int[] layout(ItemStack var0) {
        int[] var1 = var0.isEmpty() ? null : LAYOUTS.get(weaponId(var0));
        if (var1 != null) {
            return (int[])var1.clone();
        } else {
            int[] var2 = new int[27];
            Arrays.setAll(var2, var0x -> SwordNativeMaterialRules.visualRow(var0x) * 9 + SwordNativeMaterialRules.visualColumn(var0x));
            return var2;
        }
    }

    public static ResourceLocation nativeRecipe(ItemStack var0) {
        if (var0.isEmpty()) {
            return null;
        } else {
            ResourceLocation var1 = weaponId(var0);
            if (var1 == null) {
                return null;
            } else {
                ResourceLocation var2 = RECIPES.get(var1);
                if (var2 != null) {
                    return var2;
                } else {
                    String var3 = SwordCompatibilityRules.nativeRecipe(var1.toString());
                    return var3.isEmpty() ? null : ResourceLocation.tryParse(var3);
                }
            }
        }
    }

    public static List<SwordCavityExtensions.BondDefinition> activeBonds(ItemStack var0) {
        if (!BONDS.isEmpty() && SwordCavityBuild.applies(var0)) {
            List var1 = SwordCavityBuild.cells(var0);
            if (SwordCavityRules.usable(var1) && !SwordCavityBuild.missingRequired(var0)) {
                EnumMap<Material, Integer> var2 = new EnumMap<>(Material.class);

                for (Slot var6 : Slot.values()) {
                    if (SwordCavityBuild.cellUnlocked(var0, var6.ordinal()) && !SwordCavityBuild.storedItem(var0, var6).isEmpty()) {
                        var2.merge(SwordCavityRules.installed(var1, var6), 1, Integer::sum);
                    }
                }

                ResourceLocation var7 = weaponId(var0);
                ArrayList<SwordCavityExtensions.BondDefinition> var8 = new ArrayList<>();

                for (SwordCavityExtensions.BondDefinition var10 : BONDS.values()) {
                    if ((var10.weapon() == null || var10.weapon().equals(var7))
                        && var10.materials().entrySet().stream().allMatch(var1x -> var2.getOrDefault(var1x.getKey(), 0) >= var1x.getValue())) {
                        var8.add(var10);
                    }
                }

                var8.sort(Comparator.comparing(var0x -> var0x.id().toString()));
                return List.copyOf(var8);
            } else {
                return List.of();
            }
        } else {
            return List.of();
        }
    }

    public static boolean hasBond(ItemStack var0, ResourceLocation var1) {
        return activeBonds(var0).stream().anyMatch(var1x -> var1x.id().equals(var1));
    }

    public static Build applyBuild(ItemStack var0, Build var1) {
        List var2 = activeBonds(var0);
        List var3 = SwordMaterialExtensions.activeBonds(var0);
        return applyBonds(var1, var2, var3);
    }

    private static Build applyBonds(Build var0, List<SwordCavityExtensions.BondDefinition> var1, List<SwordMaterialExtensions.BondDefinition> var2) {
        if (var1.isEmpty() && var2.isEmpty()) {
            return var0;
        } else {
            double var3 = var0.durability();
            double var5 = var0.wear();
            double var7 = var0.damage();
            double var9 = var0.speed();
            double var11 = var0.reach();

            for (SwordCavityExtensions.BondDefinition var14 : var1) {
                var3 += var14.bonuses().durability();
                var5 += var14.bonuses().wear();
                var7 += var14.bonuses().damage();
                var9 += var14.bonuses().speed();
                var11 += var14.bonuses().reach();
            }

            for (SwordMaterialExtensions.BondDefinition var16 : var2) {
                var3 += var16.bonuses().durability();
                var5 += var16.bonuses().wear();
                var7 += var16.bonuses().damage();
                var9 += var16.bonuses().speed();
                var11 += var16.bonuses().reach();
            }

            return new Build(
                bound(var3, 0.5, 3.0, var0.durability()),
                bound(var5, 0.35, 2.0, var0.wear()),
                bound(var7, 0.5, 2.0, var0.damage()),
                bound(var9, 0.5, 1.5, var0.speed()),
                bound(var11, 0.0, 0.75, var0.reach())
            );
        }
    }

    private static double bound(double var0, double var2, double var4, double var6) {
        return Double.isFinite(var0) ? Math.max(var2, Math.min(var4, var0)) : var6;
    }

    public static boolean supports(ItemStack var0) {
        if (!var0.isEmpty() && !WeaponAttachmentPolicy.excluded(var0)) {
            ResourceLocation var1 = weaponId(var0);
            return var1 != null && (FUNCTIONS.containsKey(var1) || SwordCompatibilityRules.supports(var1.toString()));
        } else {
            return false;
        }
    }

    public static List<SwordCavityExtensions.BondDefinition> participatingBonds(ItemStack var0) {
        Optional var1 = SwordMaterialExtensions.resolve(var0);
        if (var1.isEmpty()) {
            return List.of();
        } else {
            ArrayList var2 = new ArrayList();

            for (SwordCavityExtensions.BondDefinition var4 : BONDS.values()) {
                if (var4.materials().containsKey(((SwordMaterialDefinition)var1.get()).baseMaterial())) {
                    var2.add(var4);
                }
            }

            var2.sort(Comparator.comparing(SwordCavityExtensions.BondDefinition::id, Comparator.comparing(ResourceLocation::toString)));
            return List.copyOf(var2);
        }
    }

    public static record BondDefinition(
        ResourceLocation id, ResourceLocation weapon, Map<Material, Integer> materials, Build bonuses, Component name, Component description
    ) {
        public BondDefinition(
            ResourceLocation id, ResourceLocation weapon, Map<Material, Integer> materials, Build bonuses, Component name, Component description
        ) {
            Objects.requireNonNull(id);
            Objects.requireNonNull(bonuses);
            Objects.requireNonNull(name);
            Objects.requireNonNull(description);
            materials = Map.copyOf(materials);
            if (materials.isEmpty()
                || materials.containsKey(Material.ORIGINAL)
                || materials.values().stream().anyMatch(var0 -> var0 < 1 || var0 > 27)
                || materials.values().stream().mapToInt(Integer::intValue).sum() > 27) {
                throw new IllegalArgumentException("Bond requires 1..27 paid material pieces");
            } else if (Double.isFinite(bonuses.durability())
                && Double.isFinite(bonuses.wear())
                && Double.isFinite(bonuses.damage())
                && Double.isFinite(bonuses.speed())
                && Double.isFinite(bonuses.reach())) {
                this.id = id;
                this.weapon = weapon;
                this.materials = materials;
                this.bonuses = bonuses;
                this.name = name;
                this.description = description;
            } else {
                throw new IllegalArgumentException("Bond bonuses must be finite additive values");
            }
        }
    }
}
