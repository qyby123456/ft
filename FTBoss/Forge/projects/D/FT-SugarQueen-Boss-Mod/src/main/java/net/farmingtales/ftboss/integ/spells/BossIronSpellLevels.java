package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.farmingtales.ftboss.entity.EasterBossEntity;

/** Boss 原生铁魔法等级；允许史诗档施法，保留法术配置上限及非 Boss 的原有等级。 */
public final class BossIronSpellLevels {
    private BossIronSpellLevels() {}

    public static int castLevel(Object caster, AbstractSpell spell, int requestedLevel) {
        if (!(caster instanceof RegularBossCastHost) && !(caster instanceof EasterBossEntity)) return requestedLevel;
        return advancedLevel(spell, requestedLevel);
    }

    public static int advancedLevel(AbstractSpell spell, int requestedLevel) {
        if (spell == null || !spell.isEnabled() || !spell.getSpellId().startsWith("irons_spellbooks:")) return requestedLevel;
        int minimum = spell.getMinLevel();
        int maximum = spell.getMaxLevel();
        int bounded = Math.max(minimum, Math.min(requestedLevel, maximum));
        for (int candidate = minimum; candidate <= maximum; candidate++) {
            if (spell.getRarity(candidate).ordinal() >= SpellRarity.EPIC.ordinal()) return Math.max(bounded, candidate);
        }
        return bounded;
    }
}
