package net.farmingtales.ftboss.item;

import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class DistinctShapedRecipe extends ShapedRecipe {

    /** 外圈必须塞满的格子数（禁果 = 8 格外圈 + 1 格金桶）。 */
    public static final int REQUIRED_ITEMS = 8;

    public DistinctShapedRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                int width, int height, NonNullList<Ingredient> ingredients,
                                ItemStack result) {
        super(id, group, category, width, height, ingredients, result);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) return false;
        return satisfied(container);
    }

    /**
     * 「非空格子正好 {@value #REQUIRED_ITEMS} 个，且两两不同」—— 本配方唯一的自定义规则。
     *
     * <p>抽成**不依赖 Minecraft 的静态方法**是为了能被校验脚本直接跑
     * （见 {@code tools/verification/DistinctRecipeCheck.java}）：
     * 把规则留在这里、把入口留在 {@link #matches}，改错任何一边都会被测出来。
     *
     * @param container 任意"有格子的东西"（合成台 3×3、玩家 2×2 都行）
     */
    public static boolean satisfied(net.minecraft.world.Container container) {
        int filled = 0;
        Set<Item> seen = new HashSet<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;
            filled++;
            /* 同一个物品出现第二次 -> 不是"不同种"，直接不匹配。 */
            if (!seen.add(stack.getItem())) return false;
        }
        /* 外圈 8 格必须全塞满：正好 8 个非空（中间那格是金桶，已由 super 校验过）。 */
        return filled == REQUIRED_ITEMS;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FTRecipeSerializers.DISTINCT_SHAPED.get();
    }

    /** 序列化器：解析与写包的格式**与 {@code minecraft:crafting_shaped} 完全一致**，只是不允许多格同物。 */
    public static class Serializer implements RecipeSerializer<DistinctShapedRecipe> {

        @Override
        public DistinctShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe parsed = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            return new DistinctShapedRecipe(parsed.getId(), parsed.getGroup(), parsed.category(),
                    parsed.getWidth(), parsed.getHeight(), parsed.getIngredients(), parsed.getResultItem(RegistryAccess.EMPTY));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DistinctShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }

        @Override
        public DistinctShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapedRecipe parsed = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer);
            return new DistinctShapedRecipe(parsed.getId(), parsed.getGroup(), parsed.category(),
                    parsed.getWidth(), parsed.getHeight(), parsed.getIngredients(), parsed.getResultItem(RegistryAccess.EMPTY));
        }
    }
}
