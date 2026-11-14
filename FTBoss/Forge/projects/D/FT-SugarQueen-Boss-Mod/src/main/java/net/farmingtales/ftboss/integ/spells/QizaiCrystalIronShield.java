package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.QizaiCrystalBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalIronShield implements QizaiCrystalBridge.Shields {
    private static final class CrystalShield extends ShieldEntity {
        CrystalShield(ServerLevel level, float health) { super(level, health); }
        @Override public boolean shouldBeSaved() { return false; }
    }

    @Override public Entity create(ServerLevel level, EasterBossEntity owner, Vec3 position, float yaw) {
        var spell = SpellRegistry.SHIELD_SPELL.get();
        if (!spell.isEnabled()) return null;
        int rank = Math.max(spell.getMinLevel(), Math.min(3, spell.getMaxLevel()));
        float health = (10 + spell.getSpellPower(rank, owner)) * net.farmingtales.ftboss.util.QizaiCrystalRules.SHIELD_MULTIPLIER;
        if (!Float.isFinite(health) || health <= 0) return null;
        var shield = new CrystalShield(level, health);
        shield.setPos(position);
        shield.setRotation(0, yaw);
        ManorFightMembers.inherit(owner, shield);
        return level.addFreshEntity(shield) ? shield : null;
    }

    @Override public void move(Entity entity, Vec3 position, float yaw) {
        if (entity instanceof ShieldEntity shield) {
            shield.setPos(position);
            shield.setRotation(0, yaw);
        }
    }
}
