package net.farmingtales.ftdungeontrading.api;

import com.google.gson.JsonElement;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public final class TradeCost {
    private final Ingredient ingredient;
    private final JsonElement ingredientJson;
    private final int count;
    private final CompoundTag expectedNbt;
    private final boolean allowAnyNbt;

    public TradeCost(Ingredient ingredient, int count, @Nullable CompoundTag expectedNbt, boolean allowAnyNbt) {
        if (ingredient.isEmpty() || count < 1 || count > 4096) {
            throw new IllegalArgumentException("A cost requires a nonempty ingredient and count 1..4096");
        }
        if (allowAnyNbt && expectedNbt != null) {
            throw new IllegalArgumentException("Exact NBT and allow_any_nbt are mutually exclusive");
        }
        this.ingredientJson = ingredient.toJson().deepCopy();
        this.ingredient = Ingredient.fromJson(this.ingredientJson);
        this.count = count;
        this.expectedNbt = expectedNbt == null ? null : expectedNbt.copy();
        this.allowAnyNbt = allowAnyNbt;
    }

    public static TradeCost item(ItemStack example, int count) {
        return new TradeCost(Ingredient.of(example.getItem()), count, example.getTag(), false);
    }

    public int count() { return count; }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && ingredient.test(stack)
                && (allowAnyNbt || Objects.equals(expectedNbt, stack.getTag()));
    }

    public ItemStack displayStack() {
        ItemStack[] examples = ingredient.getItems();
        if (examples.length == 0) return ItemStack.EMPTY;
        ItemStack display = examples[0].copy();
        display.setCount(Math.min(count, display.getMaxStackSize()));
        if (expectedNbt != null) display.setTag(expectedNbt.copy());
        return display;
    }

    public Component description() {
        Component name = ingredientJson.isJsonObject() && ingredientJson.getAsJsonObject().has("tag")
                ? Component.literal("#" + ingredientJson.getAsJsonObject().get("tag").getAsString())
                : displayStack().getDisplayName();
        return Component.translatable("message.ftdungeontrading.entry", count, name);
    }

    public CompoundTag signature() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ingredient", ingredientJson.toString());
        tag.putInt("count", count);
        tag.putBoolean("allow_any_nbt", allowAnyNbt);
        if (expectedNbt != null) tag.put("nbt", expectedNbt.copy());
        return tag;
    }
}
