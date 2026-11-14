import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;

import java.util.HashSet;
import java.util.Set;

public final class ManorFiveCombatRulesTest {
    private static int assertions;

    public static void main(String[] arguments) {
        testAdaptiveDamage();
        for (int variant = -1; variant <= 11; variant++) {
            Set<Skill> skills = new HashSet<>();
            int legacy = 0;
            int rotationLength = ManorFiveCombatRules.rotationLength(variant);
            for (int rotation = 0; rotation < rotationLength; rotation++) {
                Skill skill = ManorFiveCombatRules.select(variant, rotation);
                if (skill == null) legacy++;
                else {
                    check(skill.variant == variant, "wrong variant");
                    skills.add(skill);
                }
            }
            if (ManorFiveCombatRules.hasExpandedSkills(variant)) {
                int expected = variant == 5 ? 3 : 2;
                check(skills.size() == expected, "all new skills reachable");
                check(legacy == expected, "legacy skills must remain reachable");
            } else check(skills.isEmpty(), "unrelated boss changed");
            for (int rotation = -8; rotation < 12; rotation++) {
                check(ManorFiveCombatRules.select(variant, rotation)
                        == ManorFiveCombatRules.select(variant, rotation + rotationLength), "rotation continuity");
            }
        }
        check(ManorFiveCombatRules.select(4, 0) == null, "lemon must retain opening summon");
        check(Skill.values().length == 13, "thirteen new skills");
        check(!ManorFiveCombatRules.supports(5), "Qingyi must retain separate attack and adaptive rules");
        check(ManorFiveCombatRules.select(5, 0) == Skill.QINGYI_TIDE, "Qingyi tide slot");
        check(ManorFiveCombatRules.select(5, 2) == Skill.QINGYI_THUNDER, "Qingyi thunder slot");
        check(ManorFiveCombatRules.select(5, 4) == Skill.QINGYI_STAR, "Qingyi star slot");
        for (Skill skill : Skill.values()) {
            int count = 0;
            for (int age = 0; age <= skill.duration() + 100; age++) {
                int pulse = ManorFiveCombatRules.pulseAt(skill, age);
                if (age < skill.windup) check(pulse == -1, "damage before warning");
                if (pulse >= 0) {
                    check(pulse == count++, "pulse order");
                    check(age < skill.duration(), "pulse after completion");
                }
            }
            check(count == skill.pulses, "lost or duplicate pulse");
            check(skill.damage > 0 && skill.knockback > 0 && skill.lift > 0, "invalid combat value");
            check(skill.windup >= 20, "minimum dodge window");
            check(skill.meleeMultiplier() >= 1.5 && skill.meleeMultiplier() <= 2.0, "skill melee multiplier");
            check(!hit(skill, 0, 0.0, 0.0, 4.0), "vertical bounds");
            check(!hit(skill, 0, 100.0, 100.0, 0.0), "range bounds");
        }
        check(hit(Skill.YOUZI_SEAL, 0, 5.7, 0.0, 0.0), "include target width");
        check(!hit(Skill.YOUZI_SEAL, 0, 5.9, 0.0, 0.0), "circle boundary");
        check(hit(Skill.YOUZI_ECHO, 0, 2.0, 1.0, 0.0), "echo circle");
        check(hit(Skill.SHADOW_CROSS, 0, -8.0, 1.0, 0.0), "cross backwards");
        check(hit(Skill.SHADOW_CROSS, 0, 1.0, 8.0, 0.0), "cross lateral");
        check(!hit(Skill.SHADOW_CROSS, 0, 5.0, 5.0, 0.0), "cross diagonal safe spot");
        check(hit(Skill.XUANLAN_RAIL, 0, 12.0, 2.4, 0.0), "expanded beam width");
        check(!hit(Skill.XUANLAN_RAIL, 0, 12.0, 2.6, 0.0), "expanded beam dodge boundary");
        check(ManorFiveCombatRules.beamHalfWidth(Skill.XUANLAN_RAIL) == 2.2, "rail radius increases by one");
        for (int pulse = 0; pulse < 3; pulse++) {
            check(ManorFiveCombatRules.waveRadius(Skill.XUANLAN_RING, pulse) == 4 + pulse * 3,
                    "each Xuanlan ring expands by exactly one");
        }
        check(!hit(Skill.XUANLAN_RAIL, 0, -2.0, 0.0, 0.0), "beam behind caster");
        check(!hit(Skill.XUANLAN_RAIL, 0, 19.0, 0.0, 0.0), "beam end");
        check(hit(Skill.LEMON_FLAMBE, 0, 4.0, 2.0, 0.0), "cone front");
        check(!hit(Skill.LEMON_FLAMBE, 0, -4.0, 0.0, 0.0), "cone behind");
        check(!hit(Skill.LEMON_FLAMBE, 0, 1.0, 4.0, 0.0), "cone side");
        check(hit(Skill.QINGYI_TIDE, 0, 8.0, 2.0, 0.0), "Qingyi tide front");
        check(!hit(Skill.QINGYI_TIDE, 0, -8.0, 0.0, 0.0), "Qingyi tide behind safe");
        check(!hit(Skill.QINGYI_TIDE, 0, 0.0, 8.0, 0.0), "Qingyi tide side safe");
        check(hit(Skill.QINGYI_THUNDER, 0, Skill.QINGYI_THUNDER.radius + 0.2, 0.0, 0.0), "Qingyi thunder edge");
        check(!hit(Skill.QINGYI_THUNDER, 0, Skill.QINGYI_THUNDER.radius + 0.4, 0.0, 0.0), "Qingyi thunder outside");
        check(hit(Skill.QINGYI_STAR, 0, 0.0, -9.0, 0.0), "Qingyi star lateral");
        check(!hit(Skill.QINGYI_STAR, 0, 7.0, 7.0, 0.0), "Qingyi star diagonal safe");
        for (Skill skill : new Skill[]{Skill.DANDAN_REAP, Skill.XUANLAN_RING}) {
            for (int pulse = 0; pulse < 3; pulse++) {
                double radius = ManorFiveCombatRules.waveRadius(skill, pulse);
                check(hit(skill, pulse, radius, 0.0, 0.0), "expanding ring");
                check(!hit(skill, pulse, 0.0, 0.0, 0.0), "ring center safe spot");
                check(!hit(skill, pulse, radius + 2.0, 0.0, 0.0), "ring outside");
            }
        }
        System.out.println("ManorFiveCombatRulesTest: " + assertions + " checks passed");
    }

    private static void testAdaptiveDamage() {
        checkBonus(20.0, 0, 0.10);
        checkBonus(60.0, 5, 0.30);
        checkBonus(100.0, 10, 0.50);
        checkBonus(20.0, 10, 0.30);
        checkBonus(100.0, 0, 0.30);
        checkBonus(200.0, 40, 0.50);
        checkBonus(-20.0, -1, 0.10);
        checkBonus(Double.NaN, 0, 0.10);
        checkBonus(Double.POSITIVE_INFINITY, Integer.MAX_VALUE, 0.50);
        checkBonus(Double.NEGATIVE_INFINITY, Integer.MIN_VALUE, 0.10);
        for (int health = 0; health <= 200; health += 5) {
            for (int buffs = 0; buffs <= 20; buffs++) {
                double bonus = ManorFiveCombatRules.adaptiveDamageBonus(health, buffs);
                check(bonus >= 0.10 && bonus <= 0.50, "adaptive bonus bounds");
                check(bonus <= ManorFiveCombatRules.adaptiveDamageBonus(health + 5, buffs), "health monotonicity");
                check(bonus <= ManorFiveCombatRules.adaptiveDamageBonus(health, buffs + 1), "buff monotonicity");
            }
        }
    }

    private static void checkBonus(double health, int buffs, double expected) {
        check(Math.abs(ManorFiveCombatRules.adaptiveDamageBonus(health, buffs) - expected) < 1.0E-9,
                "adaptive damage example");
    }

    private static boolean hit(Skill skill, int pulse, double forward, double side, double height) {
        return ManorFiveCombatRules.contains(skill, pulse, forward, side, height, 0.3);
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
