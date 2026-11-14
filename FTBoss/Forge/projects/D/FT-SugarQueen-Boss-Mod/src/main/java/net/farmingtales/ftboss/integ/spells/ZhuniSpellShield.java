package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

/** 竹鸟每15秒刷新一面跟随玄蓝朝向的铁魔法原生法术盾；退场和重新入场时清理。 */
public final class ZhuniSpellShield {
    private static final int INTERVAL_TICKS = 20 * 15;
    private Entity shield;
    private long nextCast;

    public void reset(long now) {
        clear();
        nextCast = now + INTERVAL_TICKS;
    }

    private boolean ready(long now) {
        if (nextCast == 0) {
            nextCast = now + INTERVAL_TICKS;
            return false;
        }
        if (now < nextCast) return false;
        nextCast = now + INTERVAL_TICKS;
        return true;
    }

    public void tick(EasterBossEntity boss, ServerLevel level) {
        if (!ModList.get().isLoaded("irons_spellbooks")) return;
        if (boss.isInvisible() || boss.isXuanlanSkyHidden()) {
            clear();
            return;
        }
        if (ready(level.getGameTime())) {
            clear();
            var spell = SpellRegistry.SHIELD_SPELL.get();
            if (spell.isEnabled()) {
                int rank = Math.max(spell.getMinLevel(), Math.min(3, spell.getMaxLevel()));
                float health = 10.0F + spell.getSpellPower(rank, boss);
                if (Float.isFinite(health) && health > 0) {
                    shield = new ShieldEntity(level, health);
                    move(boss);
                    ManorFightMembers.inherit(boss, shield);
                    if (!level.addFreshEntity(shield)) clear();
                }
            }
        }
        if (shield != null && !shield.isRemoved()) move(boss);
    }

    private void move(EasterBossEntity boss) {
        Vec3 facing = boss.getLookAngle().multiply(1, 0, 1);
        if (facing.lengthSqr() < 1.0E-6) facing = new Vec3(0, 0, 1);
        shield.setPos(boss.getEyePosition().add(facing.normalize().scale(2.5)));
        ((ShieldEntity) shield).setRotation(0, boss.getYRot());
    }

    public void clear() {
        if (shield != null) shield.discard();
        shield = null;
    }
}
