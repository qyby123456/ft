/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.entity.IMagicEntity
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.List;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.integ.spells.BossIronSpells;
import net.farmingtales.ftboss.integ.spells.BossSpellCombat;
import net.farmingtales.ftboss.util.BossSpellRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class BossSpellPacing {
    public static final int SIGNATURE_OPENING_TICKS = 60;
    public static final int SIGNATURE_INTERVAL_TICKS = 60;
    public static final int LEGACY_INTERVAL_TICKS = 240;
    public static final int LEGACY_RETRY_TICKS = 60;
    public static final float LEGACY_CHANCE = 0.25f;
    private static final String LEGACY_READY = "ftboss:legacy_spell_ready";

    private BossSpellPacing() {
    }

    public static int signatureCooldown(int spellCooldown) {
        return Math.max(160, (int)Math.ceil((double)spellCooldown * 0.75));
    }

    public static boolean tryLegacyCast(LivingEntity caster, AbstractSpell spell) {
        IMagicEntity magic;
        ServerLevel level;
        Mob boss;
        block7: {
            block6: {
                Level level2;
                if (!(caster instanceof Mob) || !((level2 = (boss = (Mob)caster).m_9236_()) instanceof ServerLevel)) break block6;
                level = (ServerLevel)level2;
                if (spell != null && spell.getSpellId().startsWith("irons_spellbooks:")) break block7;
            }
            return true;
        }
        List<BossSpellRules.Kind> signatures = BossSpellCombat.spellPool(boss);
        if (signatures.stream().noneMatch(kind -> BossIronSpells.spell(kind).isEnabled())) {
            return true;
        }
        CompoundTag data = boss.getPersistentData();
        if (boss instanceof IMagicEntity && (magic = (IMagicEntity)boss).isCasting()) {
            return false;
        }
        long now = level.m_46467_();
        if (now < data.m_128454_(LEGACY_READY)) {
            return false;
        }
        boolean selected = boss.m_217043_().m_188501_() < 0.25f;
        data.m_128356_(LEGACY_READY, now + (long)NonManorFinaleDifficulty.acceleratedTicks((LivingEntity)boss, selected ? 240 : 60));
        return selected;
    }
}

