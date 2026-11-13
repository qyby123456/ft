package ftboss.storage308;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.Objects;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class RecipeDataBefore429 {
    public static final String ENCODING = "NEOFORGE_1_21_1_COMPONENT_RECIPE_V1";

    private RecipeDataBefore429() {
    }

    public static JsonElement definition(Ingredient ingredient, RegistryAccess registries) {
        Objects.requireNonNull(ingredient, "ingredient");
        Objects.requireNonNull(registries, "registries");
        if (ingredient.getClass() != Ingredient.class || ingredient.isCustom()) {
            throw new IllegalArgumentException("Unapproved custom native ingredient implementation");
        }
        return Ingredient.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), ingredient).getOrThrow();
    }

    public static CompoundTag stack(ItemStack stack, RegistryAccess registries) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(registries, "registries");
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Empty native recipe item evidence");
        }
        Tag encoded = stack.saveOptional(registries);
        if (!(encoded instanceof CompoundTag compound)) {
            throw new IllegalArgumentException("Invalid native recipe item evidence type");
        }
        return compound;
    }
}
