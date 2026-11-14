package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.farmingtales.ftboss.integ.spells.BossIronSpellLevels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** 只提升 FTBoss 原生施法等级；女王、沙耶、酒魔的直接施法入口共用同一规则。 */
@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob", remap = false)
public abstract class BossAdvancedIronSpellMixin {
    @ModifyVariable(method = "initiateCastSpell", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private int ftboss$advancedIronLevel(int requestedLevel, AbstractSpell spell, int originalLevel) {
        return BossIronSpellLevels.castLevel(this, spell, requestedLevel);
    }
}
