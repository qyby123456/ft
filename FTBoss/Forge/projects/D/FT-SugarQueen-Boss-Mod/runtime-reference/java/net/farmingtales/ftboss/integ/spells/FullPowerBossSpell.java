/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.SpellRarity
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;

public abstract class FullPowerBossSpell
extends AbstractSpell {
    protected FullPowerBossSpell(int mana, int power, int manaStep, int powerStep) {
        this.baseManaCost = mana + 4 * manaStep;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = power + 4 * powerStep;
        this.spellPowerPerLevel = 0;
    }

    public final int getMaxLevel() {
        return 1;
    }

    public final int getMinRarity() {
        return SpellRarity.LEGENDARY.ordinal();
    }
}

