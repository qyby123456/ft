/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorBossSpellProfile;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.minecraft.world.entity.LivingEntity;

public final class YouziCloneSpellProfile {
    public static final float POWER_SCALE = 0.35f;
    public static final int INITIAL_DELAY = 40;
    public static final int CAST_INTERVAL = 200;
    public static final int RANGE = 16;
    public static final int ABSORPTION_TICKS = 80;
    public static final int RESISTANCE_TICKS = 60;

    private YouziCloneSpellProfile() {
    }

    public static boolean isClone(LivingEntity caster) {
        EasterBossEntity boss;
        return caster instanceof EasterBossEntity && (boss = (EasterBossEntity)caster).isMini() && boss.getVariant() == 0;
    }

    public static int cooldown(LivingEntity caster, int ticks) {
        return Math.max(100, ticks) * (YouziCloneSpellProfile.isClone(caster) ? 2 : 1);
    }

    public static float power(ManorSignatureSpell spell, LivingEntity caster) {
        return BossSpellTuning.scaledPower(spell, caster, ManorBossSpellProfile.power(spell.kind)) * 0.35f;
    }
}

