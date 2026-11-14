package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 本模组的自定义配方序列化器（2026-09-19 v160 起）。
 *
 * <p>目前只有 {@link #DISTINCT_SHAPED} 一个：给【禁果】用 ——
 * 形状与原版有序配方一致，但要求外圈 8 格是 **8 种不同**的苹果制品。
 * 详见 {@link DistinctShapedRecipe}。
 */
public final class FTRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FTBossMod.MOD_ID);

    /** {@code ftboss:distinct_shaped} —— 声明式有序配方 + "非空格子两两不同"校验。 */
    public static final RegistryObject<RecipeSerializer<?>> DISTINCT_SHAPED =
            RECIPE_SERIALIZERS.register("distinct_shaped", DistinctShapedRecipe.Serializer::new);

    private FTRecipeSerializers() {}
}
