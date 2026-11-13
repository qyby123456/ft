package net.farmingtales.equipmenttooltip;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeTotals(double addition, double multiplier) {
    public static AttributeTotals from(Collection<AttributeModifier> modifiers) {
        Map<UUID, AttributeModifier> unique = new LinkedHashMap<>();
        for (AttributeModifier modifier : modifiers) {
            unique.put(modifier.getId(), modifier);
        }
        double addition = 0.0;
        double baseMultiplier = 0.0;
        double totalMultiplier = 1.0;
        for (AttributeModifier modifier : unique.values()) {
            switch (modifier.getOperation()) {
                case ADDITION -> addition += modifier.getAmount();
                case MULTIPLY_BASE -> baseMultiplier += modifier.getAmount();
                case MULTIPLY_TOTAL -> totalMultiplier *= 1.0 + modifier.getAmount();
            }
        }
        return new AttributeTotals(addition, (1.0 + baseMultiplier) * totalMultiplier);
    }

    public double apply(double base) {
        return (base + addition) * multiplier;
    }

    public double flatContribution() {
        return addition * multiplier;
    }

    public boolean isFinite() {
        return Double.isFinite(addition) && Double.isFinite(multiplier) && Double.isFinite(flatContribution());
    }
}
