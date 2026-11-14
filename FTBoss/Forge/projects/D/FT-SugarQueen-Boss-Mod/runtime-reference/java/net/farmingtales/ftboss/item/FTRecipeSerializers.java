/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.DistinctShapedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class FTRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.RECIPE_SERIALIZERS, (String)"ftboss");
    public static final RegistryObject<RecipeSerializer<?>> DISTINCT_SHAPED = RECIPE_SERIALIZERS.register("distinct_shaped", DistinctShapedRecipe.Serializer::new);

    private FTRecipeSerializers() {
    }
}

