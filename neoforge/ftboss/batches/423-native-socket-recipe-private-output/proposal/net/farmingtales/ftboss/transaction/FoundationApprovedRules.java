package net.farmingtales.ftboss.transaction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FourWeaponUseGuard;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public final class FoundationApprovedRules implements ExistingTransactionBoundary.ApprovedRules {
    @Override
    public String fingerprint(ServerPlayer var1, ItemStack var2) {
        if (var1 != null && var2 != null) {
            try {
                if (!var1.level().isClientSide && FourWeaponFoundationAdapter.applies(var2)) {
                    String var3 = FourWeaponFoundationAdapter.rulesFingerprint(var2);
                    if (var3 != null && !var3.isBlank()) {
                        String var4 = this.nativeRecipeFingerprint(var1, var2);
                        if (var4 != null && !var4.isBlank()) {
                            PrivateRead423 var5 = new PrivateRead423();
                            var5.putInt("DigestVersion", 3);
                            var5.putString("Rules", var3);
                            var5.putString("NativeRecipe", var4);
                            return FourWeaponFoundationAdapter.fingerprintTag(var5.finish());
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (LinkageError | RuntimeException var6) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean allows(ItemStack var1, Operation var2) {
        FourWeaponFoundationAdapter.View var3 = FourWeaponFoundationAdapter.read(var1);
        if (!var3.valid()) {
            return false;
        } else {
            return switch (var2) {
                case REPLACE_PART -> false;
                case EVOLVE -> !FourWeaponUseGuard.blocksUse(var1) && FourWeaponFoundationAdapter.canEvolve(var1);
                case SELECT_SKILL -> !FourWeaponUseGuard.blocksUse(var1) && var3.level() > 0;
                case START_REPAIR, SUBMIT_REPAIR, CLAIM_REPAIR -> true;
                case BUILD_CELL, REMOVE_CELL, OPEN_CONTAINER, MENU_PICKUP, MENU_QUICK_MOVE, MENU_SWAP, MENU_QUICK_CRAFT, MENU_PICKUP_ALL -> true;
            };
        }
    }

    @Override
    public boolean progressionPreservesContainer(ItemStack var1) {
        FourWeaponFoundationAdapter.View var2 = FourWeaponFoundationAdapter.read(var1);
        return var2.valid() && var2.level() >= 0 && var2.level() <= 4;
    }

    public static CompoundTag recipeEvidence(RecipeHolder<?> holder, RegistryAccess var1) {
        Recipe<?> var0 = holder == null ? null : holder.value();
        CompoundTag var10000 = CataclysmInfusionRecipeEvidence.read(holder, var1);
        if (var10000 != null) {
            return var10000;
        } else if (var0 == null) {
            return null;
        } else {
            try {
                Class var2 = var0.getClass();
                boolean var3 = var2 == ShapedRecipe.class;
                boolean var4 = var2 == ShapelessRecipe.class;
                boolean var5 = var2 == SmithingTransformRecipe.class;
                boolean var6 = var2 == SmithingTrimRecipe.class;
                if (!var3 && !var4 && !var5 && !var6) {
                    return null;
                } else {
                    PrivateRead423 var7 = new PrivateRead423();
                    var7.putString("Id", holder.id().toString());
                    var7.putString("Encoding", NativeRecipeEvidenceData.ENCODING);
                    var7.putString("Type", var2.getName());
                    var7.putString("Serializer", var0.getSerializer().getClass().getName());
                    if (var6) {
                        var7.putString("ResultMode", "DYNAMIC_REGISTRY_DEPENDENT_TRIM_1_21_1");
                    } else {
                        ItemStack var8 = var0.getResultItem(var1);
                        if (var8 == null || var8.isEmpty()) {
                            return null;
                        }

                        var7.put("Result", NativeRecipeEvidenceData.stack(var8, var1));
                        var7.putString("ResultMode", var5 ? "BASE_COMPONENT_PATCH_THEN_EXPLICIT_RESULT_COMPONENT_PATCH_1_21_1" : "STATIC_COPY");
                    }

                    int var9 = 0;
                    int var10 = 0;
                    List<Ingredient> var23;
                    if (!var5 && !var6) {
                        var23 = var0.getIngredients();
                        if (var23 == null || var23.isEmpty() || var23.size() > 65536) {
                            return null;
                        }

                        if (var3) {
                            var9 = ((ShapedRecipe)var0).getWidth();
                            var10 = ((ShapedRecipe)var0).getHeight();
                            if (var9 <= 0 || var10 <= 0 || (long)var9 * (long)var10 != (long)var23.size() || (long)var9 * (long)var10 > 65536L) {
                                return null;
                            }
                        }

                        var7.putString("IngredientMode", var3 ? "SHAPED_RUNTIME_GRID_ROW_MAJOR" : "SHAPELESS_UNORDERED_MULTISET");
                    } else {
                        String[] var11 = var5 ? new String[]{"f_265949_", "f_265888_", "f_265907_"} : new String[]{"f_265958_", "f_266040_", "f_266053_"};
                        var23 = List.of(
                            smithingIngredient(var0, "template", var11[0]),
                            smithingIngredient(var0, "base", var11[1]),
                            smithingIngredient(var0, "addition", var11[2])
                        );
                        var7.putString("IngredientMode", "SMITHING_FIXED_TEMPLATE_BASE_ADDITION");
                    }

                    var7.putInt("Width", var9);
                    var7.putInt("Height", var10);
                    var7.putInt("SlotCount", var23.size());
                    ArrayList var24 = new ArrayList();
                    int var12 = 0;

                    while (var12 < var23.size()) {
                        Ingredient var13 = (Ingredient)var23.get(var12);
                        if (var13 != null && var13.getClass() == Ingredient.class && !var13.isCustom()) {
                            boolean var14 = var13.isEmpty();
                            if (var14 && !var3) {
                                return null;
                            }

                            PrivateRead423 var15 = new PrivateRead423();
                            var15.putString("State", var14 ? "EMPTY" : "NON_EMPTY");
                            var15.putString("Type", var13.getClass().getName());
                            var15.put("Definition", FoundationCanonicalDigest.object(NativeRecipeEvidenceData.definition(var13, var1)));
                            ItemStack[] var16 = var13.getItems();
                            if (var16 != null && var16.length <= 65536 && (!var14 || var16.length == 0) && (var14 || var16.length != 0)) {
                                var15.putInt("AlternativeCount", var16.length);
                                ListTag var17 = new ListTag();

                                for (int var18 = 0; var18 < var16.length; var18++) {
                                    ItemStack var19 = var16[var18];
                                    if (var19 == null || var19.isEmpty()) {
                                        return null;
                                    }

                                    CompoundTag var20 = NativeRecipeEvidenceData.stack(var19, var1);
                                    FourWeaponFoundationAdapter.canonical(var20);
                                    PrivateRead423 var21 = new PrivateRead423();
                                    var21.putInt("Index", var18);
                                    var21.put("Item", var20);
                                    var17.add(var21.finish());
                                }

                                var15.put("Alternatives", var17);
                                if (var5 || var6) {
                                    var15.putString("Role", new String[]{"template", "base", "addition"}[var12]);
                                }

                                var24.add(var15);
                                var12++;
                                continue;
                            }

                            return null;
                        }

                        return null;
                    }

                    if (var4) {
                        var24.sort(Comparator.comparing(FoundationApprovedRules::draftCanonical423));
                    }

                    ListTag var25 = new ListTag();

                    for (int var26 = 0; var26 < var24.size(); var26++) {
                        PrivateRead423 var27 = (PrivateRead423)var24.get(var26);
                        var27.putInt("Index", var26);
                        var25.add(var27.finish());
                    }

                    var7.put("Ingredients", var25);
                    CompoundTag evidence423 = var7.finish();
                    FourWeaponFoundationAdapter.canonical(evidence423);
                    return evidence423;
                }
            } catch (LinkageError | RuntimeException var22) {
                return null;
            }
        }
    }

    private static Ingredient smithingIngredient(Object var0, String var1, String var2) {
        try {
            Field var3;
            try {
                var3 = var0.getClass().getDeclaredField(var2);
            } catch (NoSuchFieldException var5) {
                var3 = var0.getClass().getDeclaredField(var1);
            }

            if (var3.getType() != Ingredient.class) {
                throw new IllegalArgumentException("smithing field type");
            } else {
                var3.setAccessible(true);
                return (Ingredient)var3.get(var0);
            }
        } catch (ReflectiveOperationException var6) {
            throw new IllegalArgumentException("smithing role access", var6);
        }
    }

    private String nativeRecipeFingerprint(ServerPlayer var1, ItemStack var2) {
        Profile var3 = FourWeaponFoundationAdapter.profile(var2);
        if (var3 == null) {
            return null;
        } else if (var3.recipeEvidence().startsWith("NONE:")) {
            return var3.recipeEvidence();
        } else {
            HashMap<String, Integer> var4 = new HashMap<>(var3.recipeBudget());
            String[] var5 = var3.recipeEvidence().split(";")[0].split("\\+");
            PrivateRead423 var6 = new PrivateRead423();
            ListTag var7 = new ListTag();

            for (int var8 = 0; var8 < var5.length; var8++) {
                ResourceLocation var9 = ResourceLocation.tryParse(var5[var8]);
                if (var9 == null) {
                    return null;
                }

                Optional<RecipeHolder<?>> var10 = var1.level().getRecipeManager().byKey(var9);
                if (var10.isEmpty() || !var10.get().id().equals(var9)) {
                    return null;
                }

                CompoundTag var11 = recipeEvidence(var10.get(), var1.level().registryAccess());
                if (var11 == null || !var11.contains("Result", 10)) {
                    return null;
                }

                String var12 = var8 == var5.length - 1 ? var3.id() : var5[var8];
                if (!var11.getCompound("Result").getString("id").equals(var12)) {
                    return null;
                }

                ListTag var13 = var11.getList("Ingredients", 10);

                for (int var14 = 0; var14 < var13.size(); var14++) {
                    CompoundTag var15 = var13.getCompound(var14);
                    if (!var15.getString("State").equals("EMPTY")) {
                        ListTag var16 = var15.getList("Alternatives", 10);
                        TreeSet var17 = new TreeSet();

                        for (int var18 = 0; var18 < var16.size(); var18++) {
                            var17.add(var16.getCompound(var18).getCompound("Item").getString("id"));
                        }

                        if (!var15.getString("Role").equals("template")
                            && (!var15.getString("Role").equals("base") || var8 <= 0 || var17.size() != 1 || !var17.contains(var5[var8 - 1]))) {
                            String var19 = new TreeSet<>(var4.keySet())
                                .stream()
                                .filter(var2x -> (Integer)var4.get(var2x) > 0 && var17.contains(var2x))
                                .findFirst()
                                .orElse(null);
                            if (var19 == null) {
                                return null;
                            }

                            var4.put(var19, (Integer)var4.get(var19) - 1);
                        }
                    }
                }

                var7.add(var11);
            }

            if (var4.values().stream().anyMatch(var0 -> var0 != 0)) {
                return null;
            } else {
                var6.put("Recipes", var7);
                return FourWeaponFoundationAdapter.fingerprintTag(var6.finish());
            }
        }
    }

    private static String draftCanonical423(PrivateRead423 draft) {
        return FourWeaponFoundationAdapter.canonical(draft.finish());
    }

    private static final class PrivateRead423 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.socketRecipeCompoundForRead423(fields);
        }
    }
}
