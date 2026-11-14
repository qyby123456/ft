/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package net.farmingtales.ftboss.api;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record SwordMaterialDefinition(ResourceLocation itemId, FoodWeaponBuildRules.Material baseMaterial, SwordFunctionRules.Values functions, Set<Category> categories, int nutrition, double saturationModifier, OptionalDouble price, OptionalInt processingComplexity, Map<ResourceLocation, Double> attributes, Component name) {
    private final Component name;

    public SwordMaterialDefinition {
        Objects.requireNonNull(resourceLocation);
        Objects.requireNonNull(material);
        Objects.requireNonNull(values);
        Objects.requireNonNull(optionalDouble);
        Objects.requireNonNull(optionalInt);
        set = Set.copyOf(set);
        map = Map.copyOf(map);
        component = Objects.requireNonNull(component).m_6881_();
        if (material == FoodWeaponBuildRules.Material.ORIGINAL || set.isEmpty() || n < 0 || n > 100 || !Double.isFinite(d2) || d2 < 0.0 || d2 > 10.0 || optionalDouble.isPresent() && (!Double.isFinite(optionalDouble.getAsDouble()) || optionalDouble.getAsDouble() < 0.0) || optionalInt.isPresent() && (optionalInt.getAsInt() < 0 || optionalInt.getAsInt() > 64) || map.values().stream().anyMatch(d -> !Double.isFinite(d) || d < 0.0 || d > 1000000.0)) {
            throw new IllegalArgumentException("Invalid sword material metadata");
        }
        for (SwordFunctionRules.Function function : SwordFunctionRules.Function.values()) {
            if (!(values.value(function) > 64.0)) continue;
            throw new IllegalArgumentException("Material function exceeds limit");
        }
    }

    public Component name() {
        return this.name.m_6881_();
    }

    public String namespace() {
        return this.itemId.m_135827_();
    }

    public double saturationGain() {
        return (double)this.nutrition * this.saturationModifier * 2.0;
    }

    public static enum Category {
        METAL,
        FOOD,
        BOSS,
        ENVIRONMENT,
        WOOD,
        STONE,
        GEM;

    }
}

