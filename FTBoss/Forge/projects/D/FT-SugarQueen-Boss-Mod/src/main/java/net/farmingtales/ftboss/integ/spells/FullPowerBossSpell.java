package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;

public abstract class FullPowerBossSpell extends AbstractSpell {
    protected FullPowerBossSpell(int mana, int power, int manaStep, int powerStep) {
        baseManaCost = mana + 4 * manaStep;
        manaCostPerLevel = 0;
        baseSpellPower = power + 4 * powerStep;
        spellPowerPerLevel = 0;
    }

    @Override
    public final int getMaxLevel() { return 1; }

    @Override
    public final int getMinRarity() { return SpellRarity.LEGENDARY.ordinal(); }

    @Override
    public int getManaCost(int level) {
        int cost = super.getManaCost(level);
        return this instanceof ManorSignatureSpell || this instanceof QizaiSpell || this instanceof QizaiBarrageSpell
                ? net.farmingtales.ftboss.util.ManorRewardBalanceRules.mana(getSpellResource().getPath(), cost) : cost;
    }
}
