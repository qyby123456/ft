package net.farmingtales.ftboss.util;

public final class WeaponTypeSkillRules {
    public enum Kind { MELEE, BOW, CROSSBOW, TRIDENT, STAFF }

    public record Profile(int shots, int interval, int duration, double spellPower,
                          double manaRegen, double dashRange) {}

    private WeaponTypeSkillRules() {}

    public static Profile profile(boolean secondary, double rarityPower) {
        double power = Double.isFinite(rarityPower) ? Math.max(0.0, rarityPower) : 1.0;
        return new Profile(secondary ? 5 : 3, 4, secondary ? 160 : 120,
                (secondary ? 0.45 : 0.30) * power, (secondary ? 1.5 : 1.0) * power,
                secondary ? 7.0 : 5.0);
    }
}
