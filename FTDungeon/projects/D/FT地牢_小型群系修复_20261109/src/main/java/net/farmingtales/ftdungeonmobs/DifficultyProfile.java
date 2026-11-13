package net.farmingtales.ftdungeonmobs;

import java.util.List;

public record DifficultyProfile(double healthMultiplier, double attackMultiplier, double armorBonus,
                                double magicArmorBonus, double spellPowerMultiplier) {
    public static final DifficultyProfile IDENTITY = new DifficultyProfile(1, 1, 0, 0, 1);
    public static final List<String> STAT_NAMES = List.of("health", "attack", "armor", "magic_armor", "spell_power");

    public DifficultyProfile {
        if (!Double.isFinite(healthMultiplier) || healthMultiplier <= 0
                || !Double.isFinite(attackMultiplier) || attackMultiplier < 0
                || !Double.isFinite(armorBonus) || !Double.isFinite(magicArmorBonus)
                || !Double.isFinite(spellPowerMultiplier) || spellPowerMultiplier < 0) {
            throw new IllegalArgumentException("Health must be positive; attack and spell multipliers must be nonnegative; all values must be finite.");
        }
    }

    public DifficultyProfile combine(DifficultyProfile other) {
        return new DifficultyProfile(healthMultiplier * other.healthMultiplier, attackMultiplier * other.attackMultiplier,
                armorBonus + other.armorBonus, magicArmorBonus + other.magicArmorBonus,
                spellPowerMultiplier * other.spellPowerMultiplier);
    }

    public DifficultyProfile with(String stat, double value) {
        return switch (stat) {
            case "health" -> new DifficultyProfile(value, attackMultiplier, armorBonus, magicArmorBonus, spellPowerMultiplier);
            case "attack" -> new DifficultyProfile(healthMultiplier, value, armorBonus, magicArmorBonus, spellPowerMultiplier);
            case "armor" -> new DifficultyProfile(healthMultiplier, attackMultiplier, value, magicArmorBonus, spellPowerMultiplier);
            case "magic_armor" -> new DifficultyProfile(healthMultiplier, attackMultiplier, armorBonus, value, spellPowerMultiplier);
            case "spell_power" -> new DifficultyProfile(healthMultiplier, attackMultiplier, armorBonus, magicArmorBonus, value);
            default -> throw new IllegalArgumentException("Unknown stat: " + stat);
        };
    }
}
