import net.farmingtales.ftboss.util.GongdiCombatRules;

public final class GongdiRulesProbe {
    private static int checks;

    public static void main(String[] arguments) {
        GongdiCombatRules.Bleed bleed = new GongdiCombatRules.Bleed();
        require(bleed.stacks(0) == 0 && bleed.pulse(0) == 0.0f, "empty state");
        require(bleed.addHit(0, false) == 1, "first hit");
        require(bleed.pulse(19) == 0.0f, "no immediate damage");
        require(bleed.addHit(19, false) == 2, "refresh adds stack");
        require(bleed.pulse(20) == 3.0f, "refresh does not postpone pulse");
        require(bleed.pulse(20) == 0.0f, "one pulse per tick");
        for (int hit = 0; hit < 12; hit++) require(bleed.addHit(21 + hit, false) <= 5, "stack cap");
        require(bleed.stacks(131) == 5, "last active tick");
        require(bleed.stacks(132) == 0 && bleed.expired(132), "exact expiration");
        require(bleed.pulse(133) == 0.0f, "no post-expiration damage");
        require(bleed.addHit(150, false) == 1, "expired stacks restart");
        require(bleed.pulse(169) == 0.0f && bleed.pulse(170) == 1.5f, "fresh pulse clock");
        require(bleed.addHit(171, true) == 5, "full blood rage applies five stacks");
        GongdiCombatRules.Bleed single = new GongdiCombatRules.Bleed();
        single.addHit(0, false);
        float total = 0.0f;
        for (int tick = 0; tick <= 100; tick++) total += single.pulse(tick);
        require(total == 7.5f, "five pulses over five seconds");
        GongdiCombatRules.Bleed independent = new GongdiCombatRules.Bleed();
        require(independent.addHit(200, false) == 1 && bleed.stacks(200) == 5, "separate victim states");
        require(GongdiCombatRules.executeDamage(60.0f, -1) == 150.0f, "negative stack clamp");
        require(GongdiCombatRules.executeDamage(60.0f, 0) == 150.0f, "base execute damage");
        require(GongdiCombatRules.executeDamage(60.0f, 5) == 300.0f, "full-stack execute");
        require(GongdiCombatRules.executeDamage(60.0f, 50) == 300.0f, "execute stack cap");
        require(GongdiCombatRules.meleeDamage(60.0, 3.0f) == 63.0f, "weapon enchantment included once");
        require(GongdiCombatRules.meleeDamage(-10.0, 0.0f) == 0.0f, "negative melee damage clamp");
        require(GongdiCombatRules.cleaveDamage(-1.0f, true) == 0.0f, "negative cleave damage clamp");
        require(GongdiCombatRules.strikeDamage(-1.0f) == 0.0f, "negative strike damage clamp");
        require(GongdiCombatRules.executeDamage(-1.0f, 5) == 0.0f, "negative execute damage clamp");
        for (float melee : new float[]{1.0f, 60.0f, 63.0f, 210.0f, 400.0f, 1500.0f}) {
            require(close(GongdiCombatRules.cleaveDamage(melee, false), melee * 1.2f), "inner cleave scales");
            require(close(GongdiCombatRules.cleaveDamage(melee, true), melee * 2.0f), "outer cleave scales");
            require(close(GongdiCombatRules.strikeDamage(melee), melee * 1.75f), "strike scales");
            require(GongdiCombatRules.cleaveDamage(melee, false) > melee * 1.1f, "inner exceeds melee plus rage bonus");
            require(GongdiCombatRules.cleaveDamage(melee, true) > GongdiCombatRules.cleaveDamage(melee, false), "inner safer");
            float previousDamage = 0.0f;
            for (int stacks = 0; stacks <= 5; stacks++) {
                float executeDamage = GongdiCombatRules.executeDamage(melee, stacks);
                require(executeDamage > previousDamage, "execute increases with bleed stacks");
                require(executeDamage > melee * 1.1f, "execute exceeds melee plus rage bonus");
                require(executeDamage > GongdiCombatRules.cleaveDamage(melee, true), "ultimate is strongest");
                previousDamage = executeDamage;
            }
        }
        for (int rageStacks = 0; rageStacks <= 5; rageStacks++) {
            float melee = GongdiCombatRules.meleeDamage(60.0 * (1.0 + rageStacks * 0.5), 3.0f);
            require(GongdiCombatRules.strikeDamage(melee) > melee, "strike never falls behind blood rage melee");
            require(GongdiCombatRules.cleaveDamage(melee, true) > melee, "cleave never falls behind blood rage melee");
            require(close(GongdiCombatRules.executeDamage(melee, 5), melee * 5.0f), "full rage ultimate scaling");
        }
        require(close(GongdiCombatRules.outerHeal(500, 1), 60.0f), "one outer hit heals missing health");
        require(close(GongdiCombatRules.outerHeal(500, 10), 180.0f), "three-target heal cap");
        require(GongdiCombatRules.outerHeal(0, 3) == 0.0f, "no overheal");
        require(GongdiCombatRules.outerHeal(500, 0) == 0.0f, "inner hit and miss do not heal");
        require(GongdiCombatRules.inRange(7, 2.5, 0, 7), "range and height boundary");
        require(!GongdiCombatRules.inRange(7.01, 0, 0, 7), "outside radius");
        require(!GongdiCombatRules.inRange(0, 2.51, 0, 7), "different floor excluded");
        require(GongdiCombatRules.inCone(4, 0, 1, 0, GongdiCombatRules.PULL_HALF_ANGLE), "front is hit");
        require(!GongdiCombatRules.inCone(-4, 0, 1, 0, GongdiCombatRules.PULL_HALF_ANGLE), "rear safe");
        require(!GongdiCombatRules.inCone(0, 4, 1, 0, GongdiCombatRules.PULL_HALF_ANGLE), "side safe");
        require(!GongdiCombatRules.inCone(4, 0, 0, 0, GongdiCombatRules.PULL_HALF_ANGLE), "zero direction safe");
        for (int angle = -180; angle <= 180; angle++) {
            double radians = Math.toRadians(angle);
            boolean actual = GongdiCombatRules.inCone(Math.cos(radians), Math.sin(radians), 2, 0,
                    GongdiCombatRules.PULL_HALF_ANGLE);
            require(actual == (Math.abs(angle) <= 50), "cone angle " + angle);
        }
        System.out.println("GONGDI_RULE_CHECKS=" + checks);
    }

    private static boolean close(float actual, float expected) {
        return Math.abs(actual - expected) < 0.001f;
    }

    private static void require(boolean condition, String description) {
        checks++;
        if (!condition) throw new AssertionError(description);
    }
}
