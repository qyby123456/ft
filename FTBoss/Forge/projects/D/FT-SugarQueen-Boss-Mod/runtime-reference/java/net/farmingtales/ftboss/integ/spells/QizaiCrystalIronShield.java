/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.registry.SpellRegistry
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.QizaiCrystalBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class QizaiCrystalIronShield
implements QizaiCrystalBridge.Shields {
    @Override
    public Entity create(ServerLevel level, EasterBossEntity owner, Vec3 position, float yaw) {
        AbstractSpell spell = (AbstractSpell)SpellRegistry.SHIELD_SPELL.get();
        if (!spell.isEnabled()) {
            return null;
        }
        int rank = Math.max(spell.getMinLevel(), Math.min(3, spell.getMaxLevel()));
        float health = (10.0f + spell.getSpellPower(rank, (Entity)owner)) * 3.0f;
        if (!Float.isFinite(health) || health <= 0.0f) {
            return null;
        }
        CrystalShield shield = new CrystalShield(level, health);
        shield.m_146884_(position);
        shield.setRotation(0.0f, yaw);
        ManorFightMembers.inherit((Entity)owner, (Entity)shield);
        return level.m_7967_((Entity)shield) ? shield : null;
    }

    @Override
    public void move(Entity entity, Vec3 position, float yaw) {
        if (entity instanceof ShieldEntity) {
            ShieldEntity shield = (ShieldEntity)entity;
            shield.m_146884_(position);
            shield.setRotation(0.0f, yaw);
        }
    }

    private static final class CrystalShield
    extends ShieldEntity {
        CrystalShield(ServerLevel level, float health) {
            super((Level)level, health);
        }

        public boolean m_142391_() {
            return false;
        }
    }
}

