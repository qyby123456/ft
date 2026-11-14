package net.farmingtales.ftboss.api;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** 剑材料只读元数据；2026-10-13 提供五功能、食物营养、饱和度和可选价格/加工信息，未知值不会被当作零。 */
public record SwordMaterialDefinition(ResourceLocation itemId, Material baseMaterial, SwordFunctionRules.Values functions,
                                      Set<Category> categories, int nutrition, double saturationModifier, OptionalDouble price,
                                      OptionalInt processingComplexity, Map<ResourceLocation, Double> attributes, Component name) {
    public enum Category { METAL, FOOD, BOSS, ENVIRONMENT, WOOD, STONE, GEM }

    public SwordMaterialDefinition {
        Objects.requireNonNull(itemId);
        Objects.requireNonNull(baseMaterial);
        Objects.requireNonNull(functions);
        Objects.requireNonNull(price);
        Objects.requireNonNull(processingComplexity);
        categories = Set.copyOf(categories);
        attributes = Map.copyOf(attributes);
        name = Objects.requireNonNull(name).copy();
        if (baseMaterial == Material.ORIGINAL || categories.isEmpty() || nutrition < 0 || nutrition > 100
                || !Double.isFinite(saturationModifier) || saturationModifier < 0 || saturationModifier > 10
                || price.isPresent() && (!Double.isFinite(price.getAsDouble()) || price.getAsDouble() < 0)
                || processingComplexity.isPresent() && (processingComplexity.getAsInt() < 0 || processingComplexity.getAsInt() > 64)
                || attributes.values().stream().anyMatch(value -> !Double.isFinite(value) || value < 0 || value > 1000000)) {
            throw new IllegalArgumentException("Invalid sword material metadata");
        }
        for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
            if (functions.value(function) > 64) throw new IllegalArgumentException("Material function exceeds limit");
        }
    }

    @Override
    public Component name() { return name.copy(); }

    public String namespace() { return itemId.getNamespace(); }

    public double saturationGain() { return nutrition * saturationModifier * 2.0; }
}
