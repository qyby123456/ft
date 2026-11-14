/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraftforge.registries.ForgeRegistries
 */
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
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.api.SwordMaterialDefinition;
import net.farmingtales.ftboss.api.SwordMaterialExtensions;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponAttachmentPolicy;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;

public final class SwordCavityExtensions {
    public static final int API_VERSION = 2;
    private static final Map<ResourceLocation, int[]> LAYOUTS = new ConcurrentHashMap<ResourceLocation, int[]>();
    private static final Map<ResourceLocation, ResourceLocation> RECIPES = new ConcurrentHashMap<ResourceLocation, ResourceLocation>();
    private static final Map<ResourceLocation, FourWeaponFoundationRules.Profile> FOUNDATION_RECIPES = new ConcurrentHashMap<ResourceLocation, FourWeaponFoundationRules.Profile>();
    private static final Map<ResourceLocation, BondDefinition> BONDS = new ConcurrentHashMap<ResourceLocation, BondDefinition>();
    private static final Map<ResourceLocation, SwordFunctionRules.Profile> FUNCTIONS = new ConcurrentHashMap<ResourceLocation, SwordFunctionRules.Profile>();
    private static final Map<ResourceLocation, List<SwordFunctionRules.Function>> FUNCTION_AXES = new ConcurrentHashMap<ResourceLocation, List<SwordFunctionRules.Function>>();
    private static final ResourceLocation IRON_SWORD = new ResourceLocation("minecraft", "iron_sword");

    private SwordCavityExtensions() {
    }

    public static int[] validateLayout(int[] nArray) {
        if (nArray == null || nArray.length != 27) {
            throw new IllegalArgumentException("Layout requires 27 cells");
        }
        boolean[] blArray = new boolean[nArray.length];
        for (int n : nArray) {
            if (n < 0 || n >= blArray.length || blArray[n]) {
                throw new IllegalArgumentException("Layout must be a permutation of 0..26");
            }
            blArray[n] = true;
        }
        return (int[])nArray.clone();
    }

    public static boolean registerLayout(ResourceLocation resourceLocation, int[] nArray) {
        return LAYOUTS.putIfAbsent(Objects.requireNonNull(resourceLocation), SwordCavityExtensions.validateLayout(nArray)) == null;
    }

    public static synchronized boolean registerNativeRecipe(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
        return RECIPES.putIfAbsent(Objects.requireNonNull(resourceLocation), Objects.requireNonNull(resourceLocation2)) == null;
    }

    public static synchronized boolean registerNativeRecipe(ResourceLocation resourceLocation, Recipe<?> recipe, RegistryAccess registryAccess, FourWeaponFoundationRules.Profile profile) {
        if (resourceLocation == null || recipe == null || profile == null || !resourceLocation.toString().equals(profile.id()) || RECIPES.containsKey(resourceLocation) || FOUNDATION_RECIPES.containsKey(resourceLocation)) {
            return false;
        }
        try {
            FourWeaponFoundationRules.Values values;
            Object object2;
            CompoundTag compoundTag = FoundationApprovedRules.recipeEvidence(recipe, registryAccess);
            if (compoundTag == null || !compoundTag.m_128425_("Result", 10) || !compoundTag.m_128469_("Result").m_128461_("id").equals(profile.id())) {
                return false;
            }
            TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
            TreeMap<String, FourWeaponFoundationRules.Values> treeMap2 = new TreeMap<String, FourWeaponFoundationRules.Values>(profile.items());
            TreeMap<Integer, FourWeaponFoundationRules.Cell> treeMap3 = new TreeMap<Integer, FourWeaponFoundationRules.Cell>();
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            for (int i = 0; i < 27; ++i) {
                if (!FourWeaponFoundationRules.unlocked(profile, i, 0)) continue;
                arrayList.add(i);
            }
            ListTag listTag = compoundTag.m_128437_("Ingredients", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                object2 = listTag.m_128728_(i);
                if (object2.m_128461_("State").equals("EMPTY") || object2.m_128461_("Role").equals("template")) continue;
                ListTag object3 = object2.m_128437_("Alternatives", 10);
                if (object3.size() != 1 || treeMap3.size() >= arrayList.size()) {
                    return false;
                }
                values = ItemStack.m_41712_((CompoundTag)object3.m_128728_(0).m_128469_("Item"));
                if (values.m_41619_() || values.m_41613_() != 1 || values.m_41763_()) {
                    return false;
                }
                String string = ForgeRegistries.ITEMS.getKey((Object)values.m_41720_()).toString();
                FoodWeaponBuildRules.Material material = SwordMaterialExtensions.baseMaterial((ItemStack)values);
                if (!profile.items().containsKey(string) && !profile.materials().containsKey((Object)material)) {
                    return false;
                }
                Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve((ItemStack)values);
                if (optional.isPresent() && optional.get().categories().contains((Object)SwordMaterialDefinition.Category.FOOD)) {
                    SwordFunctionRules.Values values2 = optional.get().functions();
                    treeMap2.put(string, new FourWeaponFoundationRules.Values(values2.handle(), values2.guard(), values2.core(), values2.stability(), values2.blade()));
                }
                treeMap.merge(string, 1, Integer::sum);
                treeMap3.put((Integer)arrayList.get(treeMap3.size()), new FourWeaponFoundationRules.Cell(string, material, "native-reference", ""));
            }
            if (treeMap3.isEmpty()) {
                return false;
            }
            FourWeaponFoundationRules.Values values3 = FourWeaponFoundationRules.ZERO;
            for (Map.Entry entry : treeMap3.entrySet()) {
                values = treeMap2.getOrDefault(((FourWeaponFoundationRules.Cell)entry.getValue()).itemId(), profile.materials().get((Object)((FourWeaponFoundationRules.Cell)entry.getValue()).material()));
                if (values == null) {
                    return false;
                }
                values3 = values3.plus(profile.bases().get((Integer)entry.getKey())).plus(values.times(profile.weights().get((Integer)entry.getKey())));
            }
            object2 = values3.scale(0.995);
            FourWeaponFoundationRules.Profile profile2 = new FourWeaponFoundationRules.Profile(profile.id(), profile.family(), profile.axes(), (FourWeaponFoundationRules.Values)object2, profile.weights(), profile.bases(), profile.unlocks(), profile.materials(), treeMap2, treeMap3, recipe.m_6423_().toString(), treeMap, Map.of());
            FOUNDATION_RECIPES.put(resourceLocation, profile2);
            RECIPES.put(resourceLocation, recipe.m_6423_());
            return true;
        }
        catch (LinkageError | RuntimeException throwable) {
            return false;
        }
    }

    public static boolean registerBond(BondDefinition bondDefinition) {
        return BONDS.putIfAbsent(bondDefinition.id(), bondDefinition) == null;
    }

    public static boolean registerFunctions(ResourceLocation resourceLocation, SwordFunctionRules.Profile profile) {
        return FUNCTIONS.putIfAbsent(Objects.requireNonNull(resourceLocation), Objects.requireNonNull(profile)) == null;
    }

    public static void unregisterFunctions(ResourceLocation resourceLocation) {
        FUNCTIONS.remove(resourceLocation);
        FUNCTION_AXES.remove(resourceLocation);
    }

    public static SwordFunctionRules.Profile registeredFunctionProfile(ItemStack itemStack) {
        return FUNCTIONS.get(SwordCavityExtensions.weaponId(itemStack));
    }

    public static boolean registerFunctions(ResourceLocation resourceLocation, SwordFunctionRules.Profile profile, List<SwordFunctionRules.Function> list) {
        if ((list = List.copyOf(list)).size() != 5 || list.stream().distinct().count() != 5L) {
            throw new IllegalArgumentException("Explicit five-axis permutation required");
        }
        if (!SwordCavityExtensions.registerFunctions(resourceLocation, profile)) {
            return false;
        }
        FUNCTION_AXES.put(resourceLocation, list);
        return true;
    }

    public static int[] registeredLayout(ItemStack itemStack) {
        int[] nArray = LAYOUTS.get(SwordCavityExtensions.weaponId(itemStack));
        return nArray == null ? null : (int[])nArray.clone();
    }

    public static synchronized ResourceLocation registeredNativeRecipe(ItemStack itemStack) {
        return RECIPES.get(SwordCavityExtensions.weaponId(itemStack));
    }

    public static synchronized FourWeaponFoundationRules.Profile foundationProfile(ItemStack itemStack, FourWeaponFoundationRules.Profile record) {
        Record record2;
        ResourceLocation resourceLocation = SwordCavityExtensions.registeredNativeRecipe(itemStack);
        if (resourceLocation != null) {
            record2 = FOUNDATION_RECIPES.get(SwordCavityExtensions.weaponId(itemStack));
            if (record2 == null || !((FourWeaponFoundationRules.Profile)record2).recipeEvidence().equals(resourceLocation.toString())) {
                return null;
            }
            record = record2;
        }
        if ((record2 = SwordCavityExtensions.registeredFunctionProfile(itemStack)) == null) {
            return record;
        }
        List<SwordFunctionRules.Function> list = FUNCTION_AXES.get(SwordCavityExtensions.weaponId(itemStack));
        if (list == null) {
            return null;
        }
        ArrayList<FourWeaponFoundationRules.Values> arrayList = new ArrayList<FourWeaponFoundationRules.Values>();
        ArrayList<FourWeaponFoundationRules.Values> arrayList2 = new ArrayList<FourWeaponFoundationRules.Values>();
        for (int i = 0; i < 27; ++i) {
            arrayList.add(SwordCavityExtensions.foundationValues(((SwordFunctionRules.Profile)record2).cellWeights().get(i), list));
            arrayList2.add(SwordCavityExtensions.foundationValues(((SwordFunctionRules.Profile)record2).cellBaseValues().get(i), list));
        }
        EnumMap<FoodWeaponBuildRules.Material, FourWeaponFoundationRules.Values> enumMap = new EnumMap<FoodWeaponBuildRules.Material, FourWeaponFoundationRules.Values>(FoodWeaponBuildRules.Material.class);
        ((SwordFunctionRules.Profile)record2).materials().forEach((material, values) -> enumMap.put((FoodWeaponBuildRules.Material)((Object)material), SwordCavityExtensions.foundationValues(values, list)));
        HashMap<String, FourWeaponFoundationRules.Values> hashMap = new HashMap<String, FourWeaponFoundationRules.Values>();
        ((SwordFunctionRules.Profile)record2).itemValues().forEach((string, values) -> hashMap.put((String)string, SwordCavityExtensions.foundationValues(values, list)));
        return new FourWeaponFoundationRules.Profile(((FourWeaponFoundationRules.Profile)record).id(), ((FourWeaponFoundationRules.Profile)record).family(), ((FourWeaponFoundationRules.Profile)record).axes(), SwordCavityExtensions.foundationValues(((SwordFunctionRules.Profile)record2).minimum(), list), arrayList, arrayList2, ((SwordFunctionRules.Profile)record2).unlockLevels(), enumMap, hashMap, ((FourWeaponFoundationRules.Profile)record).nativeCells(), ((FourWeaponFoundationRules.Profile)record).recipeEvidence(), ((FourWeaponFoundationRules.Profile)record).recipeBudget(), ((FourWeaponFoundationRules.Profile)record).environmentBudget());
    }

    private static FourWeaponFoundationRules.Values foundationValues(SwordFunctionRules.Values values, List<SwordFunctionRules.Function> list) {
        return new FourWeaponFoundationRules.Values(values.value(list.get(0)), values.value(list.get(1)), values.value(list.get(2)), values.value(list.get(3)), values.value(list.get(4)));
    }

    public static CompoundTag foundationExtensionsSnapshot(ItemStack itemStack) {
        TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
        treeMap.put("Layout", SwordCavityExtensions.registeredLayout(itemStack));
        treeMap.put("NativeRecipe", SwordCavityExtensions.registeredNativeRecipe(itemStack));
        treeMap.put("FoundationRecipeProfile", FOUNDATION_RECIPES.get(SwordCavityExtensions.weaponId(itemStack)));
        treeMap.put("Bonds", Map.copyOf(BONDS));
        return FoundationCanonicalDigest.object(treeMap);
    }

    public static String foundationExtensionsFingerprint(ItemStack itemStack) {
        StringBuilder stringBuilder = new StringBuilder();
        int[] nArray = SwordCavityExtensions.registeredLayout(itemStack);
        stringBuilder.append(Arrays.toString(nArray)).append('|').append(SwordCavityExtensions.registeredNativeRecipe(itemStack));
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>(BONDS.values());
        arrayList.sort(Comparator.comparing(bondDefinition -> bondDefinition.id().toString()));
        for (BondDefinition bondDefinition2 : arrayList) {
            stringBuilder.append('|').append(bondDefinition2.id()).append(':').append(bondDefinition2.weapon()).append(':').append(bondDefinition2.bonuses());
            for (FoodWeaponBuildRules.Material material : FoodWeaponBuildRules.Material.values()) {
                if (!bondDefinition2.materials().containsKey((Object)material)) continue;
                stringBuilder.append(':').append((Object)material).append('=').append(bondDefinition2.materials().get((Object)material));
            }
        }
        return stringBuilder.toString();
    }

    public static List<BondDefinition> activeBonds(ItemStack itemStack, List<ItemStack> list, boolean bl) {
        if (!bl) {
            return List.of();
        }
        EnumMap<FoodWeaponBuildRules.Material, Integer> enumMap = new EnumMap<FoodWeaponBuildRules.Material, Integer>(FoodWeaponBuildRules.Material.class);
        for (ItemStack object2 : list) {
            Object object;
            if (object2.m_41619_() || object2.m_41613_() != 1 || (object = SwordMaterialExtensions.baseMaterial(object2)) == FoodWeaponBuildRules.Material.ORIGINAL) continue;
            enumMap.merge((FoodWeaponBuildRules.Material)((Object)object), 1, Integer::sum);
        }
        ResourceLocation resourceLocation = SwordCavityExtensions.weaponId(itemStack);
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>();
        for (BondDefinition bondDefinition2 : BONDS.values()) {
            if (bondDefinition2.weapon() != null && !bondDefinition2.weapon().equals((Object)resourceLocation) || !bondDefinition2.materials().entrySet().stream().allMatch(entry -> enumMap.getOrDefault(entry.getKey(), 0) >= (Integer)entry.getValue())) continue;
            arrayList.add(bondDefinition2);
        }
        arrayList.sort(Comparator.comparing(bondDefinition -> bondDefinition.id().toString()));
        return List.copyOf(arrayList);
    }

    public static FoodWeaponBuildRules.Build applyBuild(ItemStack itemStack, FoodWeaponBuildRules.Build build, List<ItemStack> list, boolean bl) {
        List<BondDefinition> list2 = SwordCavityExtensions.activeBonds(itemStack, list, bl);
        List<SwordMaterialExtensions.BondDefinition> list3 = SwordMaterialExtensions.activeBonds(itemStack, list, bl);
        return SwordCavityExtensions.applyBonds(build, list2, list3);
    }

    public static SwordFunctionRules.Profile functionProfile(ItemStack itemStack) {
        SwordFunctionRules.Profile profile;
        if (!SwordCavityBuild.applies(itemStack)) {
            return null;
        }
        ResourceLocation resourceLocation = SwordCavityExtensions.weaponId(itemStack);
        SwordFunctionRules.Profile profile2 = profile = resourceLocation == null ? null : FUNCTIONS.get(resourceLocation);
        return profile != null ? profile : (resourceLocation == null ? null : SwordCompatibilityRules.profile(resourceLocation.toString(), SwordNativeMaterials.budget(itemStack)));
    }

    public static void unregisterLayout(ResourceLocation resourceLocation) {
        LAYOUTS.remove(resourceLocation);
    }

    public static synchronized void unregisterNativeRecipe(ResourceLocation resourceLocation) {
        RECIPES.remove(resourceLocation);
        FOUNDATION_RECIPES.remove(resourceLocation);
    }

    public static void unregisterBond(ResourceLocation resourceLocation) {
        BONDS.remove(resourceLocation);
    }

    private static ResourceLocation weaponId(ItemStack itemStack) {
        return ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
    }

    public static int[] layout(ItemStack itemStack) {
        int[] nArray;
        int[] nArray2 = nArray = itemStack.m_41619_() ? null : LAYOUTS.get(SwordCavityExtensions.weaponId(itemStack));
        if (nArray != null) {
            return (int[])nArray.clone();
        }
        int[] nArray3 = new int[27];
        Arrays.setAll(nArray3, n -> SwordNativeMaterialRules.visualRow(n) * 9 + SwordNativeMaterialRules.visualColumn(n));
        return nArray3;
    }

    public static ResourceLocation nativeRecipe(ItemStack itemStack) {
        if (itemStack.m_41619_()) {
            return null;
        }
        ResourceLocation resourceLocation = SwordCavityExtensions.weaponId(itemStack);
        if (resourceLocation == null) {
            return null;
        }
        ResourceLocation resourceLocation2 = RECIPES.get(resourceLocation);
        if (resourceLocation2 != null) {
            return resourceLocation2;
        }
        String string = SwordCompatibilityRules.nativeRecipe(resourceLocation.toString());
        return string.isEmpty() ? null : ResourceLocation.m_135820_((String)string);
    }

    public static List<BondDefinition> activeBonds(ItemStack itemStack) {
        if (BONDS.isEmpty() || !SwordCavityBuild.applies(itemStack)) {
            return List.of();
        }
        List<SwordCavityRules.Cell> list = SwordCavityBuild.cells(itemStack);
        if (!SwordCavityRules.usable(list) || SwordCavityBuild.missingRequired(itemStack)) {
            return List.of();
        }
        EnumMap<FoodWeaponBuildRules.Material, Integer> enumMap = new EnumMap<FoodWeaponBuildRules.Material, Integer>(FoodWeaponBuildRules.Material.class);
        for (SwordCavityRules.Slot object : SwordCavityRules.Slot.values()) {
            if (!SwordCavityBuild.cellUnlocked(itemStack, object.ordinal()) || SwordCavityBuild.storedItem(itemStack, object).m_41619_()) continue;
            enumMap.merge(SwordCavityRules.installed(list, object), 1, Integer::sum);
        }
        ResourceLocation resourceLocation = SwordCavityExtensions.weaponId(itemStack);
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>();
        for (BondDefinition bondDefinition2 : BONDS.values()) {
            if (bondDefinition2.weapon() != null && !bondDefinition2.weapon().equals((Object)resourceLocation) || !bondDefinition2.materials().entrySet().stream().allMatch(entry -> enumMap.getOrDefault(entry.getKey(), 0) >= (Integer)entry.getValue())) continue;
            arrayList.add(bondDefinition2);
        }
        arrayList.sort(Comparator.comparing(bondDefinition -> bondDefinition.id().toString()));
        return List.copyOf(arrayList);
    }

    public static boolean hasBond(ItemStack itemStack, ResourceLocation resourceLocation) {
        return SwordCavityExtensions.activeBonds(itemStack).stream().anyMatch(bondDefinition -> bondDefinition.id().equals((Object)resourceLocation));
    }

    public static FoodWeaponBuildRules.Build applyBuild(ItemStack itemStack, FoodWeaponBuildRules.Build build) {
        List<BondDefinition> list = SwordCavityExtensions.activeBonds(itemStack);
        List<SwordMaterialExtensions.BondDefinition> list2 = SwordMaterialExtensions.activeBonds(itemStack);
        return SwordCavityExtensions.applyBonds(build, list, list2);
    }

    private static FoodWeaponBuildRules.Build applyBonds(FoodWeaponBuildRules.Build build, List<BondDefinition> list, List<SwordMaterialExtensions.BondDefinition> list2) {
        if (list.isEmpty() && list2.isEmpty()) {
            return build;
        }
        double d = build.durability();
        double d2 = build.wear();
        double d3 = build.damage();
        double d4 = build.speed();
        double d5 = build.reach();
        for (BondDefinition record : list) {
            d += record.bonuses().durability();
            d2 += record.bonuses().wear();
            d3 += record.bonuses().damage();
            d4 += record.bonuses().speed();
            d5 += record.bonuses().reach();
        }
        for (SwordMaterialExtensions.BondDefinition bondDefinition : list2) {
            d += bondDefinition.bonuses().durability();
            d2 += bondDefinition.bonuses().wear();
            d3 += bondDefinition.bonuses().damage();
            d4 += bondDefinition.bonuses().speed();
            d5 += bondDefinition.bonuses().reach();
        }
        return new FoodWeaponBuildRules.Build(SwordCavityExtensions.bound(d, 0.5, 3.0, build.durability()), SwordCavityExtensions.bound(d2, 0.35, 2.0, build.wear()), SwordCavityExtensions.bound(d3, 0.5, 2.0, build.damage()), SwordCavityExtensions.bound(d4, 0.5, 1.5, build.speed()), SwordCavityExtensions.bound(d5, 0.0, 0.75, build.reach()));
    }

    private static double bound(double d, double d2, double d3, double d4) {
        return Double.isFinite(d) ? Math.max(d2, Math.min(d3, d)) : d4;
    }

    public static boolean supports(ItemStack itemStack) {
        if (itemStack.m_41619_() || WeaponAttachmentPolicy.excluded(itemStack)) {
            return false;
        }
        ResourceLocation resourceLocation = SwordCavityExtensions.weaponId(itemStack);
        return resourceLocation != null && (FUNCTIONS.containsKey(resourceLocation) || SwordCompatibilityRules.supports(resourceLocation.toString()));
    }

    public static List<BondDefinition> participatingBonds(ItemStack itemStack) {
        Optional<SwordMaterialDefinition> optional = SwordMaterialExtensions.resolve(itemStack);
        if (optional.isEmpty()) {
            return List.of();
        }
        ArrayList<BondDefinition> arrayList = new ArrayList<BondDefinition>();
        for (BondDefinition bondDefinition : BONDS.values()) {
            if (!bondDefinition.materials().containsKey((Object)optional.get().baseMaterial())) continue;
            arrayList.add(bondDefinition);
        }
        arrayList.sort(Comparator.comparing(BondDefinition::id, Comparator.comparing(ResourceLocation::toString)));
        return List.copyOf(arrayList);
    }

    public record BondDefinition(ResourceLocation id, ResourceLocation weapon, Map<FoodWeaponBuildRules.Material, Integer> materials, FoodWeaponBuildRules.Build bonuses, Component name, Component description) {
        public BondDefinition {
            Objects.requireNonNull(resourceLocation);
            Objects.requireNonNull(build);
            Objects.requireNonNull(component);
            Objects.requireNonNull(component2);
            map = Map.copyOf(map);
            if (map.isEmpty() || map.containsKey((Object)FoodWeaponBuildRules.Material.ORIGINAL) || map.values().stream().anyMatch(n -> n < 1 || n > 27) || map.values().stream().mapToInt(Integer::intValue).sum() > 27) {
                throw new IllegalArgumentException("Bond requires 1..27 paid material pieces");
            }
            if (!(Double.isFinite(build.durability()) && Double.isFinite(build.wear()) && Double.isFinite(build.damage()) && Double.isFinite(build.speed()) && Double.isFinite(build.reach()))) {
                throw new IllegalArgumentException("Bond bonuses must be finite additive values");
            }
        }
    }
}

