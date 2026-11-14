/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.ShapedRecipe
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item;

import com.google.gson.JsonObject;
import java.util.HashSet;
import net.farmingtales.ftboss.item.FTRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class DistinctShapedRecipe
extends ShapedRecipe {
    public static final int REQUIRED_ITEMS = 8;

    public DistinctShapedRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id, group, category, width, height, ingredients, result);
    }

    public boolean m_5818_(CraftingContainer container, Level level) {
        if (!super.m_5818_(container, level)) {
            return false;
        }
        return DistinctShapedRecipe.satisfied((Container)container);
    }

    public static boolean satisfied(Container container) {
        int filled = 0;
        HashSet<Item> seen = new HashSet<Item>();
        for (int slot = 0; slot < container.m_6643_(); ++slot) {
            ItemStack stack = container.m_8020_(slot);
            if (stack.m_41619_()) continue;
            ++filled;
            if (seen.add(stack.m_41720_())) continue;
            return false;
        }
        return filled == 8;
    }

    public RecipeSerializer<?> m_7707_() {
        return (RecipeSerializer)FTRecipeSerializers.DISTINCT_SHAPED.get();
    }

    public static class Serializer
    implements RecipeSerializer<DistinctShapedRecipe> {
        public DistinctShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe parsed = (ShapedRecipe)RecipeSerializer.f_44076_.m_6729_(id, json);
            return new DistinctShapedRecipe(parsed.m_6423_(), parsed.m_6076_(), parsed.m_245232_(), parsed.m_44220_(), parsed.m_44221_(), (NonNullList<Ingredient>)parsed.m_7527_(), parsed.m_8043_((RegistryAccess)RegistryAccess.f_243945_));
        }

        public void toNetwork(FriendlyByteBuf buffer, DistinctShapedRecipe recipe) {
            RecipeSerializer.f_44076_.m_6178_(buffer, (Recipe)recipe);
        }

        public DistinctShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapedRecipe parsed = (ShapedRecipe)RecipeSerializer.f_44076_.m_8005_(id, buffer);
            return new DistinctShapedRecipe(parsed.m_6423_(), parsed.m_6076_(), parsed.m_245232_(), parsed.m_44220_(), parsed.m_44221_(), (NonNullList<Ingredient>)parsed.m_7527_(), parsed.m_8043_((RegistryAccess)RegistryAccess.f_243945_));
        }
    }
}

