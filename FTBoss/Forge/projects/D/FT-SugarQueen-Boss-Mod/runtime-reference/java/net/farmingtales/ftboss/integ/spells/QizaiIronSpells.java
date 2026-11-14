/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ICastData
 *  io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.integ.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import java.util.Arrays;
import java.util.List;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.BossSignatureSpell;
import net.farmingtales.ftboss.integ.spells.BossSpellEffects;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.integ.spells.ManorBossSpellProfile;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.integ.spells.ManorSpellEffects;
import net.farmingtales.ftboss.skill.QizaiConstellation;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class QizaiIronSpells {
    private QizaiIronSpells() {
    }

    public static boolean matches(ManorSpellRules.Kind kind) {
        return kind == ManorSpellRules.Kind.STAR_CROSS || kind == ManorSpellRules.Kind.ROSE_STARFALL;
    }

    public static int skill(ManorSpellRules.Kind kind) {
        return kind == ManorSpellRules.Kind.STAR_CROSS ? 5 : 6;
    }

    public static Aim aim(ServerLevel level, LivingEntity caster, ManorSpellRules.Kind kind) {
        return new Aim(QizaiConstellation.marks(level, caster, QizaiIronSpells.skill(kind), BossSpellEffects.aim(caster)));
    }

    public static boolean castBoss(ServerLevel level, EasterBossEntity boss, int kind, List<Vec3> marks) {
        ManorSignatureSpell spell = ManorIronSpells.spell(kind == 5 ? ManorSpellRules.Kind.STAR_CROSS : ManorSpellRules.Kind.ROSE_STARFALL);
        if (!spell.isEnabled()) {
            return false;
        }
        MagicData data = new MagicData(true);
        data.setSyncedData(new SyncedSpellData((LivingEntity)boss));
        data.initiateCast((AbstractSpell)spell, 1, 0, CastSource.MOB, "mainhand");
        data.setAdditionalCastData((ICastData)new Aim(marks));
        spell.onCast((Level)level, 1, (LivingEntity)boss, CastSource.MOB, data);
        return true;
    }

    public static void cast(ServerLevel level, LivingEntity caster, ManorSignatureSpell spell, MagicData data) {
        Aim aim;
        ICastData iCastData;
        if (data != null && (iCastData = data.getAdditionalCastData()) instanceof Aim) {
            Aim locked;
            aim = locked = (Aim)iCastData;
        } else if (data != null && (iCastData = data.getAdditionalCastData()) instanceof BossSignatureSpell.Aim) {
            BossSignatureSpell.Aim point = (BossSignatureSpell.Aim)iCastData;
            aim = new Aim(QizaiConstellation.marks(level, caster, QizaiIronSpells.skill(spell.kind), point.point()));
        } else {
            aim = QizaiIronSpells.aim(level, caster, spell.kind);
        }
        Aim aim2 = aim;
        float power = BossSpellTuning.playerPower(caster, BossSpellTuning.manorBoss(caster) ? ManorBossSpellProfile.power(spell.kind) : spell.getSpellPower(1, (Entity)caster));
        if (!Float.isFinite(power) || power <= 0.0f) {
            return;
        }
        QizaiConstellation.cast(level, caster, QizaiIronSpells.skill(spell.kind), aim2.marks(), livingEntity2 -> {
            if (!BossSpellEffects.canTarget(caster, livingEntity2)) {
                return;
            }
            if (caster instanceof EasterBossEntity && !(livingEntity2 instanceof LivingEntity)) {
                return;
            }
            livingEntity2.f_19802_ = 0;
            ManorSpellEffects.hurt(caster, livingEntity2, spell, power);
        });
    }

    public static boolean castSupport(ServerLevel level, EasterBossEntity boss, String spellId) {
        ManorSpellRules.Kind kind = Arrays.stream(ManorSpellRules.Kind.values()).filter(candidate -> candidate.id.equals(spellId) && candidate.support() && candidate.variant != 11).findFirst().orElse(null);
        if (kind == null) {
            return false;
        }
        ManorSignatureSpell spell = ManorIronSpells.spell(kind);
        if (spell == null || !spell.isEnabled()) {
            return false;
        }
        MagicData data = new MagicData(true);
        data.setSyncedData(new SyncedSpellData((LivingEntity)boss));
        data.initiateCast((AbstractSpell)spell, 1, 0, CastSource.MOB, "offhand");
        data.setAdditionalCastData((ICastData)new BossSignatureSpell.Aim(boss.m_20182_()));
        spell.onCast((Level)level, 1, (LivingEntity)boss, CastSource.MOB, data);
        return true;
    }

    public record Aim(List<Vec3> marks) implements ICastData
    {
        public Aim {
            marks = List.copyOf(marks);
        }

        public void reset() {
        }
    }
}

