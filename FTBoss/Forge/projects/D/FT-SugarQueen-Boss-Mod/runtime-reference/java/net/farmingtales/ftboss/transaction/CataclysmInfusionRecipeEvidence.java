/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 */
package net.farmingtales.ftboss.transaction;

import java.lang.reflect.Method;
import java.util.List;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public final class CataclysmInfusionRecipeEvidence {
    private static final String TYPE = "com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe";
    private static final String RECIPE = "cataclysm:weapon_infusion/wrath_of_the_desert";

    private CataclysmInfusionRecipeEvidence() {
    }

    public static CompoundTag read(Recipe<?> recipe, RegistryAccess registryAccess) {
        if (recipe == null || !recipe.getClass().getName().equals(TYPE) || !recipe.m_6423_().toString().equals(RECIPE)) {
            return null;
        }
        try {
            if (!recipe.m_7707_().getClass().getName().equals("com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe$Serializer")) {
                return null;
            }
            ItemStack itemStack = recipe.m_8043_(registryAccess);
            if (itemStack == null || itemStack.m_41619_() || itemStack.m_41613_() != 1 || !FourWeaponFoundationAdapter.registryId(itemStack).equals("cataclysm:wrath_of_the_desert")) {
                return null;
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.m_128359_("Id", recipe.m_6423_().toString());
            compoundTag.m_128359_("Type", TYPE);
            compoundTag.m_128359_("Serializer", recipe.m_7707_().getClass().getName());
            compoundTag.m_128365_("Result", (Tag)itemStack.m_41739_(new CompoundTag()));
            compoundTag.m_128359_("ResultMode", "STATIC_RESULT_BASE_TAG_OVERRIDE_1_20_1");
            compoundTag.m_128359_("IngredientMode", "CATACLYSM_WEAPON_FUSION_ORDERED_ROLES");
            compoundTag.m_128405_("SlotCount", 2);
            ListTag listTag = new ListTag();
            List<String> list = List.of("getbaseIngredient", "getAdditionIngredient");
            List<String> list2 = List.of("base", "addition");
            for (int i = 0; i < list.size(); ++i) {
                Method method = recipe.getClass().getDeclaredMethod(list.get(i), new Class[0]);
                if (method.getReturnType() != Ingredient.class) {
                    return null;
                }
                Ingredient ingredient = (Ingredient)method.invoke(recipe, new Object[0]);
                if (ingredient == null || ingredient.getClass() != Ingredient.class || ingredient.m_43947_()) {
                    return null;
                }
                ItemStack[] itemStackArray = ingredient.m_43908_();
                if (itemStackArray == null || itemStackArray.length == 0 || itemStackArray.length > 65536) {
                    return null;
                }
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.m_128405_("Index", i);
                compoundTag2.m_128359_("Role", list2.get(i));
                compoundTag2.m_128359_("State", "NON_EMPTY");
                compoundTag2.m_128359_("Type", ingredient.getClass().getName());
                compoundTag2.m_128365_("Definition", (Tag)FoundationCanonicalDigest.object(ingredient.m_43942_()));
                compoundTag2.m_128405_("AlternativeCount", itemStackArray.length);
                ListTag listTag2 = new ListTag();
                for (int j = 0; j < itemStackArray.length; ++j) {
                    ItemStack itemStack2 = itemStackArray[j];
                    if (itemStack2 == null || itemStack2.m_41619_()) {
                        return null;
                    }
                    CompoundTag compoundTag3 = itemStack2.m_41739_(new CompoundTag());
                    FourWeaponFoundationAdapter.canonical((Tag)compoundTag3);
                    CompoundTag compoundTag4 = new CompoundTag();
                    compoundTag4.m_128405_("Index", j);
                    compoundTag4.m_128365_("Item", (Tag)compoundTag3);
                    listTag2.add((Object)compoundTag4);
                }
                compoundTag2.m_128365_("Alternatives", (Tag)listTag2);
                listTag.add((Object)compoundTag2);
            }
            compoundTag.m_128365_("Ingredients", (Tag)listTag);
            FourWeaponFoundationAdapter.canonical((Tag)compoundTag);
            return compoundTag;
        }
        catch (LinkageError | ReflectiveOperationException | RuntimeException throwable) {
            return null;
        }
    }
}

