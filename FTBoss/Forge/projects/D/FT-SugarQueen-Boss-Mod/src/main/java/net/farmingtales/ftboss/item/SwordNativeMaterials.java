package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.util.SwordCompatibilityRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/** 原生剑体配方快照；2026-10-13 铁剑试案保留两铁锭与木棍，仅增三类环境参考，旧铁粒账本只读等价展开，不提供退款。 */
public final class SwordNativeMaterials {
    public static final String ROOT = "FTBossSwordNativeMaterials";
    private static final int VERSION = 1;

    private SwordNativeMaterials() {}

    private static String itemId(ItemStack stack) {
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    public static List<Piece> defaults(ItemStack weapon) {
        if (!SwordCavityBuild.applies(weapon)) return List.of();
        String identifier = itemId(weapon);
        List<Piece> compatible = SwordCompatibilityRules.nativeBudget(identifier);
        if (!compatible.isEmpty()) return compatible;
        List<Piece> vanilla = SwordNativeMaterialRules.vanilla(identifier);
        if (!vanilla.isEmpty()) return vanilla;
        if (!(weapon.getItem() instanceof TieredItem tiered)) return List.of();
        var tier = tiered.getTier();
        String fallback = tier == Tiers.WOOD ? "wooden" : tier == Tiers.STONE ? "stone" : tier == Tiers.IRON ? "iron"
                : tier == Tiers.GOLD ? "golden" : tier == Tiers.DIAMOND ? "diamond" : tier == Tiers.NETHERITE ? "netherite" : "";
        List<Piece> materials = new ArrayList<>();
        if (!fallback.isEmpty()) {
            materials.addAll(SwordNativeMaterialRules.vanilla("minecraft:" + fallback + "_sword"));
        } else {
            for (ItemStack ingredient : tier.getRepairIngredient().getItems()) {
                if (!ingredient.isEmpty()) {
                    materials.add(new Piece(itemId(ingredient), 2, Origin.RECIPE));
                    break;
                }
            }
            if (materials.isEmpty()) materials.add(new Piece(identifier, 0, Origin.SHARED));
            materials.add(new Piece("minecraft:stick", 1, Origin.RECIPE));
        }
        List<Piece> expanded = SwordNativeMaterialRules.recipe(materials.stream().filter(piece -> piece.count() > 0).toList(),
                netherTheme(identifier));
        return expanded.stream().map(piece -> piece.origin() == Origin.SHARED ? piece
                : new Piece(piece.itemId(), piece.count(), Origin.THEME)).toList();
    }

    private static boolean netherTheme(String identifier) {
        return !identifier.startsWith("minecraft:")
                && identifier.matches(".*(?:nether|infernal|incinerator|ignit|blaze|flame|firebrand|demon).*");
    }

    private static List<Piece> saved(ItemStack weapon) {
        CompoundTag data = weapon.getTagElement(ROOT);
        if (data == null || data.getInt("Version") != VERSION) return List.of();
        if (!data.getString("Weapon").equals(itemId(weapon))) return List.of();
        int size = data.getInt("Size");
        if (size < 4 || size > 27) return List.of();
        List<Piece> result = new ArrayList<>();
        try {
            for (int index = 0; index < size; index++) {
                CompoundTag entry = data.getCompound("Parts").getCompound(Integer.toString(index));
                Piece piece = new Piece(entry.getString("Id"), entry.getInt("Count"), Origin.valueOf(entry.getString("Origin")));
                ResourceLocation id = ResourceLocation.tryParse(piece.itemId());
                if (id == null || !ForgeRegistries.ITEMS.containsKey(id)
                        || (piece.origin() == Origin.SHARED ? piece.count() != 0 : piece.count() <= 0)) return List.of();
                result.add(piece);
            }
        } catch (IllegalArgumentException exception) {
            return List.of();
        }
        return SwordCompatibilityRules.upgradeReferenceBudget(itemId(weapon), List.copyOf(result));
    }

    public static List<Piece> budget(ItemStack weapon) {
        List<Piece> stored = saved(weapon);
        return stored.isEmpty() ? defaults(weapon) : stored;
    }

    public static List<Piece> parts(ItemStack weapon) {
        if (EquipmentWeaponBuild.applies(weapon)) return EquipmentWeaponBuild.parts(weapon);
        List<Piece> source = budget(weapon);
        return SwordCompatibilityRules.nativeLayout(itemId(weapon), source);
    }

    public static List<Piece> parts(ItemStack weapon, Level level) {
        if (EquipmentWeaponBuild.applies(weapon)) return EquipmentWeaponBuild.parts(weapon, level);
        List<Piece> source = budget(weapon, level);
        return SwordCompatibilityRules.nativeLayout(itemId(weapon), source);
    }

    private static Map<String, Integer> budget(List<Piece> pieces) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Piece piece : pieces) if (piece.origin() == Origin.RECIPE && piece.count() > 0) {
            result.merge(piece.itemId(), piece.count(), Integer::sum);
        }
        return result;
    }

    private static List<Piece> ingredients(Recipe<?> recipe) {
        List<Piece> result = new ArrayList<>();
        for (var ingredient : recipeIngredients(recipe)) {
            List<ItemStack> alternatives = new ArrayList<>(List.of(ingredient.getItems()));
            alternatives.removeIf(ItemStack::isEmpty);
            alternatives.sort(Comparator.comparing(SwordNativeMaterials::itemId));
            if (alternatives.isEmpty()) continue;
            String identifier = itemId(alternatives.get(0));
            if (identifier.endsWith("_smithing_template")) continue;
            result.addAll(SwordCompatibilityRules.expandIngredient(identifier));
        }
        return result;
    }

    private static boolean weaponFusion(Recipe<?> recipe) {
        return recipe.getClass().getName().equals("com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe");
    }

    private static List<Ingredient> recipeIngredients(Recipe<?> recipe) {
        if (!weaponFusion(recipe)) return recipe.getIngredients();
        try {
            Object base = recipe.getClass().getMethod("getbaseIngredient").invoke(recipe);
            Object addition = recipe.getClass().getMethod("getAdditionIngredient").invoke(recipe);
            if (base instanceof Ingredient baseIngredient && addition instanceof Ingredient additionIngredient) {
                return List.of(baseIngredient, additionIngredient);
            }
        } catch (ReflectiveOperationException exception) {
            return List.of();
        }
        return List.of();
    }

    public static List<Piece> budget(ItemStack weapon, Level level) {
        if (!SwordCavityBuild.applies(weapon)) return List.of();
        List<Piece> stored = saved(weapon);
        if (!stored.isEmpty() || weapon.getTagElement(ROOT) != null) return stored.isEmpty() ? defaults(weapon) : stored;
        if (level == null) return defaults(weapon);
        String identifier = itemId(weapon);
        ResourceLocation preferred = SwordCavityExtensions.nativeRecipe(weapon);
        List<Recipe<?>> recipes = new ArrayList<>(level.getRecipeManager().getRecipes());
        recipes.sort(Comparator.comparingInt((Recipe<?> recipe) -> recipe.getId().equals(preferred) ? 0 : 1)
                .thenComparing(recipe -> recipe.getId().toString()));
        for (Recipe<?> recipe : recipes) {
            if (!(recipe instanceof CraftingRecipe) && !(recipe instanceof SmithingRecipe) && !weaponFusion(recipe)) continue;
            try {
                ItemStack output = recipe.getResultItem(level.registryAccess());
                if (output.isEmpty() || output.getItem() != weapon.getItem() || output.getCount() != 1) continue;
                List<Piece> inputs = ingredients(recipe);
                if (inputs.isEmpty()) continue;
                List<Piece> directCompatible = SwordCompatibilityRules.canonicalBudget(identifier, inputs);
                if (!directCompatible.isEmpty()) return directCompatible;
                List<Piece> expanded = SwordNativeMaterialRules.recipe(inputs, netherTheme(identifier));
                if (expanded.isEmpty()) continue;
                List<Piece> compatible = SwordCompatibilityRules.canonicalBudget(identifier, expanded);
                if (!compatible.isEmpty()) return compatible;
                List<Piece> vanilla = SwordNativeMaterialRules.vanilla(identifier);
                if (identifier.equals("minecraft:iron_sword") && SwordNativeMaterialRules.ironRecipe(expanded)) {
                    return SwordNativeMaterialRules.ironPrototype();
                }
                return !vanilla.isEmpty() && budget(vanilla).equals(budget(expanded)) ? vanilla : expanded;
            } catch (RuntimeException exception) {
                continue;
            }
        }
        return defaults(weapon);
    }

    public static void capture(ItemStack weapon, Level level) {
        if (EquipmentWeaponBuild.applies(weapon)) {
            EquipmentWeaponBuild.capture(weapon, level);
            return;
        }
        if (!SwordCavityBuild.applies(weapon) || weapon.getTagElement(ROOT) != null) return;
        List<Piece> parts = budget(weapon, level);
        if (parts.size() < 4 || parts.size() > 27) return;
        CompoundTag data = new CompoundTag();
        data.putInt("Version", VERSION);
        data.putInt("Size", parts.size());
        data.putString("Weapon", itemId(weapon));
        CompoundTag entries = new CompoundTag();
        for (int index = 0; index < parts.size(); index++) {
            Piece piece = parts.get(index);
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", piece.itemId());
            entry.putInt("Count", piece.count());
            entry.putString("Origin", piece.origin().name());
            entries.put(Integer.toString(index), entry);
        }
        data.put("Parts", entries);
        weapon.getOrCreateTag().put(ROOT, data);
    }

    public static ItemStack display(ItemStack weapon, int index, List<Piece> parts) {
        if (DiamondAlloyArmorCore.applies(weapon) && index == DiamondAlloyArmorCore.index(weapon, parts)) return DiamondAlloyArmorCore.display(weapon);
        if (!SwordNativeMaterialRules.hasPiece(parts, index)) return ItemStack.EMPTY;
        Piece piece = parts.get(index);
        ResourceLocation id = ResourceLocation.tryParse(piece.itemId());
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) return ItemStack.EMPTY;
        return new ItemStack(ForgeRegistries.ITEMS.getValue(id), 1);
    }
}
