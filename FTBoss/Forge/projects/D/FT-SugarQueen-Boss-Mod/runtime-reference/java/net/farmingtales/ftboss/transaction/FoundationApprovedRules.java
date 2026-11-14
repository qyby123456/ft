/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.item.crafting.ShapelessRecipe
 *  net.minecraft.world.item.crafting.SmithingTransformRecipe
 *  net.minecraft.world.item.crafting.SmithingTrimRecipe
 */
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
import net.farmingtales.ftboss.transaction.CataclysmInfusionRecipeEvidence;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public final class FoundationApprovedRules
implements ExistingTransactionBoundary.ApprovedRules {
    @Override
    public String fingerprint(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (serverPlayer == null || itemStack == null) {
            return null;
        }
        try {
            if (serverPlayer.m_9236_().f_46443_ || !FourWeaponFoundationAdapter.applies(itemStack)) {
                return null;
            }
            String string = FourWeaponFoundationAdapter.rulesFingerprint(itemStack);
            if (string == null || string.isBlank()) {
                return null;
            }
            String string2 = this.nativeRecipeFingerprint(serverPlayer, itemStack);
            if (string2 == null || string2.isBlank()) {
                return null;
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128405_("DigestVersion", 3);
            compoundTag.m_128359_("Rules", string);
            compoundTag.m_128359_("NativeRecipe", string2);
            return FourWeaponFoundationAdapter.fingerprintTag((Tag)compoundTag);
        }
        catch (LinkageError | RuntimeException throwable) {
            return null;
        }
    }

    @Override
    public boolean allows(ItemStack itemStack, FourWeaponTransactions.Operation operation) {
        FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
        if (!view.valid()) {
            return false;
        }
        return switch (operation) {
            default -> throw new IncompatibleClassChangeError();
            case FourWeaponTransactions.Operation.REPLACE_PART -> false;
            case FourWeaponTransactions.Operation.EVOLVE -> {
                if (!FourWeaponUseGuard.blocksUse(itemStack) && FourWeaponFoundationAdapter.canEvolve(itemStack)) {
                    yield true;
                }
                yield false;
            }
            case FourWeaponTransactions.Operation.SELECT_SKILL -> {
                if (!FourWeaponUseGuard.blocksUse(itemStack) && view.level() > 0) {
                    yield true;
                }
                yield false;
            }
            case FourWeaponTransactions.Operation.START_REPAIR, FourWeaponTransactions.Operation.SUBMIT_REPAIR, FourWeaponTransactions.Operation.CLAIM_REPAIR -> true;
            case FourWeaponTransactions.Operation.BUILD_CELL, FourWeaponTransactions.Operation.REMOVE_CELL, FourWeaponTransactions.Operation.OPEN_CONTAINER, FourWeaponTransactions.Operation.MENU_PICKUP, FourWeaponTransactions.Operation.MENU_QUICK_MOVE, FourWeaponTransactions.Operation.MENU_SWAP, FourWeaponTransactions.Operation.MENU_QUICK_CRAFT, FourWeaponTransactions.Operation.MENU_PICKUP_ALL -> true;
        };
    }

    @Override
    public boolean progressionPreservesContainer(ItemStack itemStack) {
        FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.read(itemStack);
        return view.valid() && view.level() >= 0 && view.level() <= 4;
    }

    public static CompoundTag recipeEvidence(Recipe<?> recipe, RegistryAccess registryAccess) {
        CompoundTag compoundTag = CataclysmInfusionRecipeEvidence.read(recipe, registryAccess);
        if (compoundTag != null) {
            return compoundTag;
        }
        if (recipe == null) {
            return null;
        }
        try {
            Object object;
            Object object2;
            boolean bl;
            Class<?> clazz = recipe.getClass();
            boolean bl2 = clazz == ShapedRecipe.class;
            boolean bl3 = clazz == ShapelessRecipe.class;
            boolean bl4 = clazz == SmithingTransformRecipe.class;
            boolean bl5 = bl = clazz == SmithingTrimRecipe.class;
            if (!(bl2 || bl3 || bl4 || bl)) {
                return null;
            }
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.m_128359_("Id", recipe.m_6423_().toString());
            compoundTag2.m_128359_("Type", clazz.getName());
            compoundTag2.m_128359_("Serializer", recipe.m_7707_().getClass().getName());
            if (bl) {
                compoundTag2.m_128359_("ResultMode", "DYNAMIC_REGISTRY_DEPENDENT_TRIM_1_20_1");
            } else {
                object2 = recipe.m_8043_(registryAccess);
                if (object2 == null || object2.m_41619_()) {
                    return null;
                }
                compoundTag2.m_128365_("Result", (Tag)object2.m_41739_(new CompoundTag()));
                compoundTag2.m_128359_("ResultMode", bl4 ? "STATIC_TRANSFORM_RESULT_BASE_TAG_OVERRIDE_1_20_1" : "STATIC_COPY");
            }
            int n = 0;
            int n2 = 0;
            if (bl4 || bl) {
                String[] stringArray;
                if (bl4) {
                    String[] stringArray2 = new String[3];
                    stringArray2[0] = "f_265949_";
                    stringArray2[1] = "f_265888_";
                    stringArray = stringArray2;
                    stringArray2[2] = "f_265907_";
                } else {
                    String[] stringArray3 = new String[3];
                    stringArray3[0] = "f_265958_";
                    stringArray3[1] = "f_266040_";
                    stringArray = stringArray3;
                    stringArray3[2] = "f_266053_";
                }
                object = stringArray;
                object2 = List.of(FoundationApprovedRules.smithingIngredient(recipe, "template", object[0]), FoundationApprovedRules.smithingIngredient(recipe, "base", object[1]), FoundationApprovedRules.smithingIngredient(recipe, "addition", object[2]));
                compoundTag2.m_128359_("IngredientMode", "SMITHING_FIXED_TEMPLATE_BASE_ADDITION");
            } else {
                object2 = recipe.m_7527_();
                if (object2 == null || object2.isEmpty() || object2.size() > 65536) {
                    return null;
                }
                if (bl2) {
                    n = ((ShapedRecipe)recipe).m_44220_();
                    n2 = ((ShapedRecipe)recipe).m_44221_();
                    if (n <= 0 || n2 <= 0 || (long)n * (long)n2 != (long)object2.size() || (long)n * (long)n2 > 65536L) {
                        return null;
                    }
                }
                compoundTag2.m_128359_("IngredientMode", bl2 ? "SHAPED_RUNTIME_GRID_ROW_MAJOR" : "SHAPELESS_UNORDERED_MULTISET");
            }
            compoundTag2.m_128405_("Width", n);
            compoundTag2.m_128405_("Height", n2);
            compoundTag2.m_128405_("SlotCount", object2.size());
            object = new ArrayList();
            for (int i = 0; i < object2.size(); ++i) {
                Ingredient ingredient = (Ingredient)object2.get(i);
                if (ingredient == null || ingredient.getClass() != Ingredient.class) {
                    return null;
                }
                boolean bl6 = ingredient.m_43947_();
                if (bl6 && !bl2) {
                    return null;
                }
                CompoundTag compoundTag3 = new CompoundTag();
                compoundTag3.m_128359_("State", bl6 ? "EMPTY" : "NON_EMPTY");
                compoundTag3.m_128359_("Type", ingredient.getClass().getName());
                compoundTag3.m_128365_("Definition", (Tag)FoundationCanonicalDigest.object(ingredient.m_43942_()));
                ItemStack[] itemStackArray = ingredient.m_43908_();
                if (itemStackArray == null || itemStackArray.length > 65536 || bl6 && itemStackArray.length != 0 || !bl6 && itemStackArray.length == 0) {
                    return null;
                }
                compoundTag3.m_128405_("AlternativeCount", itemStackArray.length);
                ListTag listTag = new ListTag();
                for (int j = 0; j < itemStackArray.length; ++j) {
                    ItemStack itemStack = itemStackArray[j];
                    if (itemStack == null || itemStack.m_41619_()) {
                        return null;
                    }
                    CompoundTag compoundTag4 = itemStack.m_41739_(new CompoundTag());
                    FourWeaponFoundationAdapter.canonical((Tag)compoundTag4);
                    CompoundTag compoundTag5 = new CompoundTag();
                    compoundTag5.m_128405_("Index", j);
                    compoundTag5.m_128365_("Item", (Tag)compoundTag4);
                    listTag.add((Object)compoundTag5);
                }
                compoundTag3.m_128365_("Alternatives", (Tag)listTag);
                if (bl4 || bl) {
                    compoundTag3.m_128359_("Role", (new String[]{"template", "base", "addition"})[i]);
                }
                object.add(compoundTag3);
            }
            if (bl3) {
                object.sort(Comparator.comparing(FourWeaponFoundationAdapter::canonical));
            }
            ListTag listTag = new ListTag();
            for (int i = 0; i < object.size(); ++i) {
                CompoundTag compoundTag6 = (CompoundTag)object.get(i);
                compoundTag6.m_128405_("Index", i);
                listTag.add((Object)compoundTag6);
            }
            compoundTag2.m_128365_("Ingredients", (Tag)listTag);
            FourWeaponFoundationAdapter.canonical((Tag)compoundTag2);
            return compoundTag2;
        }
        catch (LinkageError | RuntimeException throwable) {
            return null;
        }
    }

    private static Ingredient smithingIngredient(Object object, String string, String string2) {
        try {
            Field field;
            try {
                field = object.getClass().getDeclaredField(string2);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                field = object.getClass().getDeclaredField(string);
            }
            if (field.getType() != Ingredient.class) {
                throw new IllegalArgumentException("smithing field type");
            }
            field.setAccessible(true);
            return (Ingredient)field.get(object);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalArgumentException("smithing role access", reflectiveOperationException);
        }
    }

    private String nativeRecipeFingerprint(ServerPlayer serverPlayer, ItemStack itemStack) {
        FourWeaponFoundationRules.Profile profile = FourWeaponFoundationAdapter.profile(itemStack);
        if (profile == null) {
            return null;
        }
        if (profile.recipeEvidence().startsWith("NONE:")) {
            return profile.recipeEvidence();
        }
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>(profile.recipeBudget());
        String[] stringArray = profile.recipeEvidence().split(";")[0].split("\\+");
        CompoundTag compoundTag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (int i = 0; i < stringArray.length; ++i) {
            String string2;
            ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)stringArray[i]);
            if (resourceLocation == null) {
                return null;
            }
            Optional optional = serverPlayer.m_9236_().m_7465_().m_44043_(resourceLocation);
            if (optional.isEmpty()) {
                return null;
            }
            CompoundTag compoundTag2 = FoundationApprovedRules.recipeEvidence((Recipe)optional.get(), serverPlayer.m_9236_().m_9598_());
            if (compoundTag2 == null || !compoundTag2.m_128425_("Result", 10)) {
                return null;
            }
            String string3 = string2 = i == stringArray.length - 1 ? profile.id() : stringArray[i];
            if (!compoundTag2.m_128469_("Result").m_128461_("id").equals(string2)) {
                return null;
            }
            ListTag listTag2 = compoundTag2.m_128437_("Ingredients", 10);
            for (int j = 0; j < listTag2.size(); ++j) {
                CompoundTag compoundTag3 = listTag2.m_128728_(j);
                if (compoundTag3.m_128461_("State").equals("EMPTY")) continue;
                ListTag listTag3 = compoundTag3.m_128437_("Alternatives", 10);
                TreeSet<String> treeSet = new TreeSet<String>();
                for (int k = 0; k < listTag3.size(); ++k) {
                    treeSet.add(listTag3.m_128728_(k).m_128469_("Item").m_128461_("id"));
                }
                if (compoundTag3.m_128461_("Role").equals("template") || compoundTag3.m_128461_("Role").equals("base") && i > 0 && treeSet.size() == 1 && treeSet.contains(stringArray[i - 1])) continue;
                String string4 = new TreeSet(hashMap.keySet()).stream().filter(string -> (Integer)hashMap.get(string) > 0 && treeSet.contains(string)).findFirst().orElse(null);
                if (string4 == null) {
                    return null;
                }
                hashMap.put(string4, (Integer)hashMap.get(string4) - 1);
            }
            listTag.add((Object)compoundTag2);
        }
        if (hashMap.values().stream().anyMatch(n -> n != 0)) {
            return null;
        }
        compoundTag.m_128365_("Recipes", (Tag)listTag);
        return FourWeaponFoundationAdapter.fingerprintTag((Tag)compoundTag);
    }
}

