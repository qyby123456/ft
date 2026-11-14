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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

public final class SwordNativeMaterials {
    public static final String ROOT = "FTBossSwordNativeMaterials";
    private static final int VERSION = 1;

    private SwordNativeMaterials() {
    }

    private static String itemId(ItemStack var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        return var1 == null ? "" : var1.toString();
    }

    private static boolean netherTheme(String var0) {
        return !var0.startsWith("minecraft:") && var0.matches(".*(?:nether|infernal|incinerator|ignit|blaze|flame|firebrand|demon).*");
    }

    public static List<Piece> budget(ItemStack var0) {
        List var1 = saved(var0);
        return var1.isEmpty() ? defaults(var0) : var1;
    }

    private static Map<String, Integer> budget(List<Piece> var0) {
        LinkedHashMap<String, Integer> var1 = new LinkedHashMap<>();

        for (Piece var3 : var0) {
            if (var3.origin() == Origin.RECIPE && var3.count() > 0) {
                var1.merge(var3.itemId(), var3.count(), Integer::sum);
            }
        }

        return var1;
    }

    private static List<Piece> ingredients(Recipe<?> var0) {
        ArrayList<Piece> var1 = new ArrayList<>();

        for (Ingredient var3 : var0.getIngredients()) {
            ArrayList<ItemStack> var4 = new ArrayList<>(List.of(var3.getItems()));
            var4.removeIf(ItemStack::isEmpty);
            var4.sort(Comparator.comparing(SwordNativeMaterials::itemId));
            if (!var4.isEmpty()) {
                String var5 = itemId((ItemStack)var4.get(0));
                if (!var5.endsWith("_smithing_template")) {
                    if (var5.equals("minecraft:netherite_ingot")) {
                        var1.add(new Piece("minecraft:netherite_scrap", 4, Origin.RECIPE));
                        var1.add(new Piece("minecraft:gold_nugget", 36, Origin.RECIPE));
                    } else {
                        List<Piece> var6 = SwordNativeMaterialRules.vanilla(var5);
                        if (var6.isEmpty()) {
                            var1.add(new Piece(var5, 1, Origin.RECIPE));
                        } else {
                            for (Piece var8 : var6) {
                                if (var8.count() > 0) {
                                    var1.add(new Piece(var8.itemId(), var8.count(), Origin.RECIPE));
                                }
                            }
                        }
                    }
                }
            }
        }

        return var1;
    }

    public static void capture(ItemStack var0, Level var1) {
        if (SwordCavityBuild.applies(var0) && FoodWeaponData.read(var0, "FTBossSwordNativeMaterials") == null) {
            List var2 = budget(var0, var1);
            if (var2.size() >= 4 && var2.size() <= 27) {
                CompoundTag var3 = new CompoundTag();
                var3.putInt("Version", 1);
                var3.putInt("Size", var2.size());
                var3.putString("Weapon", itemId(var0));
                CompoundTag var4 = new CompoundTag();

                for (int var5 = 0; var5 < var2.size(); var5++) {
                    Piece var6 = (Piece)var2.get(var5);
                    CompoundTag var7 = new CompoundTag();
                    var7.putString("Id", var6.itemId());
                    var7.putInt("Count", var6.count());
                    var7.putString("Origin", var6.origin().name());
                    var4.put(Integer.toString(var5), var7);
                }

                var3.put("Parts", var4);
                FoodWeaponData.write(var0, "FTBossSwordNativeMaterials", var3);
            }
        }
    }

    public static ItemStack display(ItemStack var0, int var1, List<Piece> var2) {
        if (!SwordNativeMaterialRules.hasPiece(var2, var1)) {
            return ItemStack.EMPTY;
        } else {
            Piece var3 = (Piece)var2.get(var1);
            ResourceLocation var4 = ResourceLocation.tryParse(var3.itemId());
            return var4 != null && BuiltInRegistries.ITEM.containsKey(var4) ? new ItemStack((ItemLike)BuiltInRegistries.ITEM.getOptional(var4).orElse(null), 1) : ItemStack.EMPTY;
        }
    }

    public static List<Piece> defaults(ItemStack var0) {
        List var10000 = SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), List.of());
        if (!var10000.isEmpty()) {
            return var10000;
        } else if (!SwordCavityBuild.applies(var0)) {
            return List.of();
        } else {
            String var1 = itemId(var0);
            if (var1.equals("minecraft:iron_sword")) {
                return SwordNativeMaterialRules.ironPrototype();
            } else {
                List var2 = SwordNativeMaterialRules.vanilla(var1);
                if (!var2.isEmpty()) {
                    return var2;
                } else {
                    Item var11 = var0.getItem();
                    if (!(var11 instanceof TieredItem)) {
                        return List.of();
                    } else {
                        Tier var3 = ((TieredItem)var11).getTier();
                        String var4 = var3 == Tiers.WOOD
                            ? "wooden"
                            : (
                                var3 == Tiers.STONE
                                    ? "stone"
                                    : (
                                        var3 == Tiers.IRON
                                            ? "iron"
                                            : (
                                                var3 == Tiers.GOLD
                                                    ? "golden"
                                                    : (var3 == Tiers.DIAMOND ? "diamond" : (var3 == Tiers.NETHERITE ? "netherite" : ""))
                                            )
                                    )
                            );
                        ArrayList<Piece> var5 = new ArrayList<>();
                        if (!var4.isEmpty()) {
                            var5.addAll(SwordNativeMaterialRules.vanilla("minecraft:" + var4 + "_sword"));
                        } else {
                            for (ItemStack var9 : var3.getRepairIngredient().getItems()) {
                                if (!var9.isEmpty()) {
                                    var5.add(new Piece(itemId(var9), 2, Origin.RECIPE));
                                    break;
                                }
                            }

                            if (var5.isEmpty()) {
                                var5.add(new Piece(var1, 0, Origin.SHARED));
                            }

                            var5.add(new Piece("minecraft:stick", 1, Origin.RECIPE));
                        }

                        List<Piece> var10 = SwordNativeMaterialRules.recipe(var5.stream().filter(var0x -> var0x.count() > 0).toList(), netherTheme(var1));
                        return var10.stream()
                            .map(var0x -> (Piece)(var0x.origin() == Origin.SHARED ? var0x : new Piece(var0x.itemId(), var0x.count(), Origin.THEME)))
                            .toList();
                    }
                }
            }
        }
    }

    public static List<Piece> parts(ItemStack var0) {
        if (!SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), List.of()).isEmpty()) {
            return SwordCompatibilityRules.nativeLayout(itemId(var0), budget(var0));
        } else {
            List var1 = budget(var0);
            return itemId(var0).equals("minecraft:iron_sword") ? SwordNativeMaterialRules.ironPrototypeLayout(var1) : SwordNativeMaterialRules.layout(var1);
        }
    }

    public static List<Piece> parts(ItemStack var0, Level var1) {
        if (!SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), List.of()).isEmpty()) {
            return SwordCompatibilityRules.nativeLayout(itemId(var0), budget(var0, var1));
        } else {
            List var2 = budget(var0, var1);
            return itemId(var0).equals("minecraft:iron_sword") ? SwordNativeMaterialRules.ironPrototypeLayout(var2) : SwordNativeMaterialRules.layout(var2);
        }
    }

    public static List<Piece> budget(ItemStack var0, Level var1) {
        if (!SwordCavityBuild.applies(var0)) {
            return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), List.of());
        } else {
            List var2 = saved(var0);
            if (var2.isEmpty() && FoodWeaponData.read(var0, "FTBossSwordNativeMaterials") == null) {
                if (var1 == null) {
                    return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), defaults(var0));
                } else {
                    String var3 = itemId(var0);
                    ResourceLocation var4 = SwordCavityExtensions.nativeRecipe(var0);
                    ArrayList<RecipeHolder<?>> var5 = new ArrayList<>(var1.getRecipeManager().getRecipes());
                    var5.sort(Comparator.<RecipeHolder<?>>comparingInt(var1x -> var1x.id().equals(var4) ? 0 : 1).thenComparing(var0x -> var0x.id().toString()));

                    for (RecipeHolder<?> recipeHolder : var5) {
                        Recipe<?> var7 = recipeHolder.value();
                        if (var7 instanceof CraftingRecipe || var7 instanceof SmithingRecipe) {
                            List var10000;
                            try {
                                ItemStack var8 = var7.getResultItem(var1.registryAccess());
                                if (var8.isEmpty() || var8.getItem() != var0.getItem() || var8.getCount() != 1) {
                                    continue;
                                }

                                List<Piece> var9 = ingredients(var7);
                                if (var9.isEmpty()) {
                                    continue;
                                }

                                List<Piece> var10 = SwordNativeMaterialRules.recipe(var9, netherTheme(var3));
                                if (var10.isEmpty()) {
                                    continue;
                                }

                                List<Piece> var11 = SwordNativeMaterialRules.vanilla(var3);
                                if (var3.equals("minecraft:iron_sword") && SwordNativeMaterialRules.ironRecipe(var10)) {
                                    return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), SwordNativeMaterialRules.ironPrototype());
                                }

                                var10000 = !var11.isEmpty() && budget(var11).equals(budget(var10)) ? var11 : var10;
                            } catch (RuntimeException var12) {
                                continue;
                            }

                            return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), var10000);
                        }
                    }

                    return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), defaults(var0));
                }
            } else {
                return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), var2.isEmpty() ? defaults(var0) : var2);
            }
        }
    }

    private static List<Piece> saved(ItemStack var0) {
        CompoundTag var1 = FoodWeaponData.read(var0, "FTBossSwordNativeMaterials");
        if (var1 == null || var1.getInt("Version") != 1) {
            return List.of();
        } else if (!var1.getString("Weapon").equals(itemId(var0))) {
            return List.of();
        } else {
            int var2 = var1.getInt("Size");
            if (var2 >= 4 && var2 <= 27) {
                ArrayList<Piece> var3 = new ArrayList<>();

                try {
                    for (int var4 = 0; var4 < var2; var4++) {
                        CompoundTag var5 = var1.getCompound("Parts").getCompound(Integer.toString(var4));
                        Piece var6 = new Piece(var5.getString("Id"), var5.getInt("Count"), Origin.valueOf(var5.getString("Origin")));
                        ResourceLocation var7 = ResourceLocation.tryParse(var6.itemId());
                        if (var7 == null
                            || !BuiltInRegistries.ITEM.containsKey(var7)
                            || (var6.origin() == Origin.SHARED ? var6.count() != 0 : var6.count() <= 0)) {
                            return List.of();
                        }

                        var3.add(var6);
                    }
                } catch (IllegalArgumentException var8) {
                    return List.of();
                }

                return SwordCompatibilityRules.upgradeReferenceBudget(itemId(var0), List.copyOf(var3));
            } else {
                return List.of();
            }
        }
    }
}
