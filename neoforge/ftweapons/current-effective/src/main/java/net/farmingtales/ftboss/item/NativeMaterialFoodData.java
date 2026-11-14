package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.minecraft.world.food.FoodProperties;

public final class NativeMaterialFoodData {
    private NativeMaterialFoodData() {
    }

    public static double saturationModifier(FoodProperties food) {
        Objects.requireNonNull(food, "food");
        if (food.nutrition() == 0) {
            return food.saturation() == 0.0F ? 0.0 : Double.NaN;
        }
        return (double)food.saturation() / ((double)food.nutrition() * 2.0);
    }
}
