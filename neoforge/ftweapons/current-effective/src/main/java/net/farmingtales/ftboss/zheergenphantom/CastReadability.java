package net.farmingtales.ftboss.zheergenphantom;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.world.entity.LivingEntity;

public final class CastReadability {
    private CastReadability() {
    }

    public static int windup(int variant, boolean normalSize, int original, int minimum) {
        return variant == 7 && normalSize ? Math.max(minimum, original) : original;
    }

    public static int fiveAge(int age, Skill skill) {
        return skill == Skill.SHADOW_CROSS || skill == Skill.SHADOW_CAGE ? skill.windup - Math.max(16, skill.windup - age) : age;
    }

    public static int strikeWindup(int original, EasterBossEntity boss) {
        return windup(boss.getVariant(), !boss.isMini() && !boss.isGiant(), original, 16);
    }

    public static int ironWindup(int original, EasterBossEntity boss) {
        return windup(boss.getVariant(), !boss.isMini() && !boss.isGiant(), original, 20);
    }

    public static int ironCaster(int original, LivingEntity caster) {
        return caster instanceof EasterBossEntity boss ? ironWindup(original, boss) : original;
    }
}

