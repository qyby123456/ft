package net.farmingtales.ftboss.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Build;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 剑体构筑扩展接口；2026-10-13 第二版增加材料功能值、格子权重和进化解锁配置，默认仅启用铁剑试案。 */
public final class SwordCavityExtensions {
    public static final int API_VERSION = 2;
    private static final Map<ResourceLocation, int[]> LAYOUTS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> RECIPES = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, BondDefinition> BONDS = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, SwordFunctionRules.Profile> FUNCTIONS = new ConcurrentHashMap<>();
    private static final ResourceLocation IRON_SWORD = new ResourceLocation("minecraft", "iron_sword");

    public record BondDefinition(ResourceLocation id, ResourceLocation weapon, Map<Material, Integer> materials,
                                 Build bonuses, Component name, Component description) {
        public BondDefinition {
            Objects.requireNonNull(id);
            Objects.requireNonNull(bonuses);
            Objects.requireNonNull(name);
            Objects.requireNonNull(description);
            materials = Map.copyOf(materials);
            if (materials.isEmpty() || materials.containsKey(Material.ORIGINAL)
                    || materials.values().stream().anyMatch(count -> count < 1 || count > SwordCavityRules.CELL_COUNT)
                    || materials.values().stream().mapToInt(Integer::intValue).sum() > SwordCavityRules.CELL_COUNT) {
                throw new IllegalArgumentException("Bond requires 1..27 paid material pieces");
            }
            if (!Double.isFinite(bonuses.durability()) || !Double.isFinite(bonuses.wear()) || !Double.isFinite(bonuses.damage())
                    || !Double.isFinite(bonuses.speed()) || !Double.isFinite(bonuses.reach())) {
                throw new IllegalArgumentException("Bond bonuses must be finite additive values");
            }
        }
    }

    private SwordCavityExtensions() {}

    public static int[] validateLayout(int[] positions) {
        if (positions == null || positions.length != SwordCavityRules.CELL_COUNT) throw new IllegalArgumentException("Layout requires 27 cells");
        boolean[] occupied = new boolean[positions.length];
        for (int position : positions) {
            if (position < 0 || position >= occupied.length || occupied[position]) throw new IllegalArgumentException("Layout must be a permutation of 0..26");
            occupied[position] = true;
        }
        return positions.clone();
    }

    public static boolean registerLayout(ResourceLocation weapon, int[] positions) {
        return LAYOUTS.putIfAbsent(Objects.requireNonNull(weapon), validateLayout(positions)) == null;
    }

    public static boolean registerNativeRecipe(ResourceLocation weapon, ResourceLocation recipe) {
        return RECIPES.putIfAbsent(Objects.requireNonNull(weapon), Objects.requireNonNull(recipe)) == null;
    }

    public static boolean registerBond(BondDefinition bond) { return BONDS.putIfAbsent(bond.id(), bond) == null; }

    public static List<BondDefinition> participatingBonds(ItemStack ingredient) {
        var material = SwordMaterialExtensions.resolve(ingredient);
        if (material.isEmpty()) return List.of();
        List<BondDefinition> result = new ArrayList<>();
        for (BondDefinition bond : BONDS.values()) {
            if (bond.materials().containsKey(material.get().baseMaterial())) result.add(bond);
        }
        result.sort(Comparator.comparing(BondDefinition::id, Comparator.comparing(ResourceLocation::toString)));
        return List.copyOf(result);
    }

    public static boolean registerFunctions(ResourceLocation weapon, SwordFunctionRules.Profile profile) {
        return FUNCTIONS.putIfAbsent(Objects.requireNonNull(weapon), Objects.requireNonNull(profile)) == null;
    }

    public static void unregisterFunctions(ResourceLocation weapon) { FUNCTIONS.remove(weapon); }

    public static boolean supports(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon)) return true;
        if (net.farmingtales.ftboss.item.GourmetBladeCavity.applies(weapon)) return true;
        if (weapon.isEmpty() || net.farmingtales.ftboss.item.WeaponAttachmentPolicy.excluded(weapon)) return false;
        ResourceLocation id = weaponId(weapon);
        return id != null && (FUNCTIONS.containsKey(id) || SwordCompatibilityRules.supports(id.toString()));
    }

    public static SwordFunctionRules.Profile functionProfile(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon)) return net.farmingtales.ftboss.item.EquipmentWeaponBuild.profile(weapon);
        if (!SwordCavityBuild.applies(weapon)) return null;
        ResourceLocation id = weaponId(weapon);
        SwordFunctionRules.Profile custom = id == null ? null : FUNCTIONS.get(id);
        return custom != null ? custom : id == null ? null : SwordCompatibilityRules.profile(id.toString(), SwordNativeMaterials.budget(weapon));
    }

    public static void unregisterLayout(ResourceLocation weapon) { LAYOUTS.remove(weapon); }
    public static void unregisterNativeRecipe(ResourceLocation weapon) { RECIPES.remove(weapon); }
    public static void unregisterBond(ResourceLocation id) { BONDS.remove(id); }

    private static ResourceLocation weaponId(ItemStack weapon) { return ForgeRegistries.ITEMS.getKey(weapon.getItem()); }

    public static int[] layout(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon)) return net.farmingtales.ftboss.item.EquipmentWeaponBuild.layout(weapon);
        int[] custom = weapon.isEmpty() ? null : LAYOUTS.get(weaponId(weapon));
        if (custom != null) return custom.clone();
        int[] positions = new int[SwordCavityRules.CELL_COUNT];
        Arrays.setAll(positions, index -> SwordNativeMaterialRules.visualRow(index) * SwordCavityRules.COLUMNS
                + SwordNativeMaterialRules.visualColumn(index));
        return positions;
    }

    public static ResourceLocation nativeRecipe(ItemStack weapon) {
        if (weapon.isEmpty()) return null;
        ResourceLocation id = weaponId(weapon);
        if (id == null) return null;
        ResourceLocation custom = RECIPES.get(id);
        if (custom != null) return custom;
        String recipe = SwordCompatibilityRules.nativeRecipe(id.toString());
        return recipe.isEmpty() ? null : ResourceLocation.tryParse(recipe);
    }

    public static List<BondDefinition> activeBonds(ItemStack weapon) {
        if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon)) {
            return net.farmingtales.ftboss.item.FoundationAccessoryBonds.cavityBonds(weapon);
        }
        if (BONDS.isEmpty() || !SwordCavityBuild.applies(weapon)) return List.of();
        boolean equipment = net.farmingtales.ftboss.item.EquipmentWeaponBuild.applies(weapon);
        List<SwordCavityRules.Cell> cells = SwordCavityBuild.cells(weapon);
        if (!SwordCavityRules.usable(cells) || SwordCavityBuild.missingRequired(weapon)) return List.of();
        Map<Material, Integer> counts = new EnumMap<>(Material.class);
        if (equipment) for (ItemStack item : net.farmingtales.ftboss.item.EquipmentWeaponBuild.bondItems(weapon)) {
            Material material = SwordMaterialExtensions.baseMaterial(item);
            if (material != Material.ORIGINAL) counts.merge(material, 1, Integer::sum);
        }
        for (SwordCavityRules.Slot slot : SwordCavityRules.Slot.values()) {
            if (!equipment && SwordCavityBuild.cellUnlocked(weapon, slot.ordinal()) && !SwordCavityBuild.storedItem(weapon, slot).isEmpty()) {
                counts.merge(SwordCavityRules.installed(cells, slot), 1, Integer::sum);
            }
        }
        ResourceLocation weaponId = weaponId(weapon);
        List<BondDefinition> active = new ArrayList<>();
        for (BondDefinition bond : BONDS.values()) {
            if ((bond.weapon() == null || bond.weapon().equals(weaponId))
                    && bond.materials().entrySet().stream().allMatch(entry -> counts.getOrDefault(entry.getKey(), 0) >= entry.getValue())) active.add(bond);
        }
        active.sort(Comparator.comparing(bond -> bond.id().toString()));
        return List.copyOf(active);
    }

    public static boolean hasBond(ItemStack weapon, ResourceLocation id) {
        return activeBonds(weapon).stream().anyMatch(bond -> bond.id().equals(id));
    }

    public static Build applyBuild(ItemStack weapon, Build base) {
        List<BondDefinition> active = activeBonds(weapon);
        List<SwordMaterialExtensions.BondDefinition> materialBonds = SwordMaterialExtensions.activeBonds(weapon);
        if (active.isEmpty() && materialBonds.isEmpty()) return base;
        double durability = base.durability();
        double wear = base.wear();
        double damage = base.damage();
        double speed = base.speed();
        double reach = base.reach();
        for (BondDefinition bond : active) {
            durability += bond.bonuses().durability();
            wear += bond.bonuses().wear();
            damage += bond.bonuses().damage();
            speed += bond.bonuses().speed();
            reach += bond.bonuses().reach();
        }
        for (SwordMaterialExtensions.BondDefinition bond : materialBonds) {
            durability += bond.bonuses().durability();
            wear += bond.bonuses().wear();
            damage += bond.bonuses().damage();
            speed += bond.bonuses().speed();
            reach += bond.bonuses().reach();
        }
        return new Build(bound(durability, SwordCavityRules.MIN_DURABILITY, SwordCavityRules.MAX_DURABILITY, base.durability()),
                bound(wear, SwordCavityRules.MIN_WEAR, SwordCavityRules.MAX_WEAR, base.wear()),
                bound(damage, SwordCavityRules.MIN_DAMAGE, SwordCavityRules.MAX_DAMAGE, base.damage()),
                bound(speed, SwordCavityRules.MIN_SPEED, SwordCavityRules.MAX_SPEED, base.speed()),
                bound(reach, 0.0, SwordCavityRules.MAX_REACH, base.reach()));
    }

    private static double bound(double value, double minimum, double maximum, double fallback) {
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }
}
