package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;

public final class YouziCloneSpellProfile {
    public static final float POWER_SCALE = 0.35F;
    public static final int INITIAL_DELAY = 40;
    public static final int CAST_INTERVAL = 200;
    public static final int RANGE = 16;
    public static final int ABSORPTION_TICKS = 80;
    public static final int RESISTANCE_TICKS = 60;

    private YouziCloneSpellProfile() {}

    public static boolean isClone(LivingEntity caster) {
        return caster instanceof EasterBossEntity boss && boss.isMini() && boss.getVariant() == 0;
    }

    public static int cooldown(LivingEntity caster, int ticks) {
        return Math.max(100, ticks) * (isClone(caster) ? 2 : 1);
    }

    public static float power(ManorSignatureSpell spell, LivingEntity caster) {
        return BossSpellTuning.scaledPower(spell, caster, ManorBossSpellProfile.power(spell.kind)) * POWER_SCALE;
    }
}
