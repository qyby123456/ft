package net.farmingtales.ftdungeonmobs;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.spells.magic_missile.MagicMissileProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class IronSpellCombat {
    private IronSpellCombat() {}

    public static boolean castMagicMissile(ServerLevel level, LivingEntity caster, LivingEntity target) {
        AbstractSpell spell = SpellRegistry.MAGIC_MISSILE_SPELL.get();
        if (!spell.isEnabled()) { return false; }
        MagicMissileProjectile missile = new MagicMissileProjectile(level, caster);
        missile.setPos(caster.getEyePosition().add(0, -missile.getBbHeight() * 0.5, 0));
        Vec3 direction = target.getBoundingBox().getCenter().subtract(missile.position());
        missile.shoot(direction);
        missile.setDamage(spell.getSpellPower(1, caster) * 0.5F);
        if (!level.addFreshEntity(missile)) { return false; }
        spell.playSound(spell.getCastFinishSound(), caster);
        return true;
    }
}
