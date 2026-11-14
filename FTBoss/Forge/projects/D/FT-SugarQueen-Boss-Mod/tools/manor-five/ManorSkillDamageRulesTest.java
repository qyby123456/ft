import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.util.ManorSkillDamageRules;

public final class ManorSkillDamageRulesTest {
    private static int assertions;

    public static void main(String[] arguments) {
        checkDamage(80.0, 10.0, 28.0f, 1.5, 135.0f);
        checkDamage(80.0, 10.0, 28.0f, 2.0, 180.0f);
        checkDamage(40.0, 0.0, 504.0f, 2.0, 504.0f);
        checkDamage(100.0, 10.0, 56.0f, 0.5, 165.0f);
        checkDamage(-20.0, -10.0, -30.0f, -2.0, 0.0f);
        checkDamage(Double.NaN, Double.POSITIVE_INFINITY, 28.0f, Double.NaN, 28.0f);
        checkDamage(20.0, 10.0, Float.NaN, Double.NEGATIVE_INFINITY, 45.0f);
        checkDamage(Double.MAX_VALUE, Double.MAX_VALUE, 0.0f, 2.0, Float.MAX_VALUE);
        check(ManorSkillDamageRules.STANDARD_MULTIPLIER == 1.5, "standard multiplier");
        check(ManorSkillDamageRules.HEAVY_MULTIPLIER == 2.0, "heavy multiplier");
        for (Skill skill : Skill.values()) {
            for (int attack = 20; attack <= 800; attack += 20) {
                for (int enchantment = 0; enchantment <= 30; enchantment += 5) {
                    float damage = ManorSkillDamageRules.damage(attack, enchantment, skill.damage, skill.meleeMultiplier());
                    check(damage >= skill.damage, "existing high damage must not decrease");
                    check(damage >= (attack + enchantment) * skill.meleeMultiplier(), "live melee floor");
                    check(damage > attack + enchantment, "skill must exceed melee benchmark");
                    check(Float.isFinite(damage), "finite output");
                    check(damage <= ManorSkillDamageRules.damage(attack + 20, enchantment, skill.damage,
                            skill.meleeMultiplier()), "attack buffs and stacks must scale skills");
                    check(damage <= ManorSkillDamageRules.damage(attack, enchantment + 5, skill.damage,
                            skill.meleeMultiplier()), "enchantments must scale skills");
                }
            }
        }
        float shadowTotal = ManorSkillDamageRules.damage(200.0, 10.0, 112.0f, 2.0);
        check(shadowTotal * 0.75f + shadowTotal * 0.25f == shadowTotal, "shadow split total");
        float beamTotal = ManorSkillDamageRules.damage(400.0, 10.0, 504.0f, 2.0);
        check(beamTotal / 4.0f * 4.0f == beamTotal, "four-school split total");
        System.out.println("ManorSkillDamageRulesTest: " + assertions + " checks passed");
    }

    private static void checkDamage(double attack, double enchantment, float base, double multiplier, float expected) {
        check(ManorSkillDamageRules.damage(attack, enchantment, base, multiplier) == expected, "damage example");
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
