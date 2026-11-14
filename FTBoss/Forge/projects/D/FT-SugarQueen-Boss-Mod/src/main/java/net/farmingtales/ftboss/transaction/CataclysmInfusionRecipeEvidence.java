package net.farmingtales.ftboss.transaction;

import java.lang.reflect.Method;
import java.util.List;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/** 灾变沙漠之怒融合配方证据；2026-10-21 固定输入角色并完整绑定配方定义、候选物品和输出 NBT。 */
public final class CataclysmInfusionRecipeEvidence {
    private static final String TYPE = "com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe";
    private static final String RECIPE = "cataclysm:weapon_infusion/wrath_of_the_desert";

    private CataclysmInfusionRecipeEvidence() {}

    public static CompoundTag read(Recipe<?> recipe, RegistryAccess access) {
        if (recipe == null || !recipe.getClass().getName().equals(TYPE) || !recipe.getId().toString().equals(RECIPE)) return null;
        try {
            if (!recipe.getSerializer().getClass().getName().equals(TYPE + "$Serializer")) return null;
            ItemStack result = recipe.getResultItem(access);
            if (result == null || result.isEmpty() || result.getCount() != 1
                    || !FourWeaponFoundationAdapter.registryId(result).equals("cataclysm:wrath_of_the_desert")) return null;
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", recipe.getId().toString());
            entry.putString("Type", TYPE);
            entry.putString("Serializer", recipe.getSerializer().getClass().getName());
            entry.put("Result", result.save(new CompoundTag()));
            entry.putString("ResultMode", "STATIC_RESULT_BASE_TAG_OVERRIDE_1_20_1");
            entry.putString("IngredientMode", "CATACLYSM_WEAPON_FUSION_ORDERED_ROLES");
            entry.putInt("SlotCount", 2);
            ListTag ingredients = new ListTag();
            List<String> getters = List.of("getbaseIngredient", "getAdditionIngredient");
            List<String> roles = List.of("base", "addition");
            for (int slotIndex = 0; slotIndex < getters.size(); slotIndex++) {
                Method getter = recipe.getClass().getDeclaredMethod(getters.get(slotIndex));
                if (getter.getReturnType() != Ingredient.class) return null;
                Ingredient ingredient = (Ingredient) getter.invoke(recipe);
                if (ingredient == null || ingredient.getClass() != Ingredient.class || ingredient.isEmpty()) return null;
                ItemStack[] alternatives = ingredient.getItems();
                if (alternatives == null || alternatives.length == 0 || alternatives.length > FoundationCanonicalDigest.MAX_NODES) return null;
                CompoundTag slot = new CompoundTag();
                slot.putInt("Index", slotIndex);
                slot.putString("Role", roles.get(slotIndex));
                slot.putString("State", "NON_EMPTY");
                slot.putString("Type", ingredient.getClass().getName());
                slot.put("Definition", FoundationCanonicalDigest.object(ingredient.toJson()));
                slot.putInt("AlternativeCount", alternatives.length);
                ListTag choices = new ListTag();
                for (int alternativeIndex = 0; alternativeIndex < alternatives.length; alternativeIndex++) {
                    ItemStack choice = alternatives[alternativeIndex];
                    if (choice == null || choice.isEmpty()) return null;
                    CompoundTag payload = choice.save(new CompoundTag());
                    FourWeaponFoundationAdapter.canonical(payload);
                    CompoundTag alternative = new CompoundTag();
                    alternative.putInt("Index", alternativeIndex);
                    alternative.put("Item", payload);
                    choices.add(alternative);
                }
                slot.put("Alternatives", choices);
                ingredients.add(slot);
            }
            entry.put("Ingredients", ingredients);
            FourWeaponFoundationAdapter.canonical(entry);
            return entry;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError rejected) {
            return null;
        }
    }
}
