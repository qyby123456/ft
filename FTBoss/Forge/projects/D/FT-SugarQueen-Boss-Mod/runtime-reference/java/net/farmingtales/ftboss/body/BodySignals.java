/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.body;

import net.farmingtales.ftboss.body.BodyCueAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public final class BodySignals {
    private BodySignals() {
    }

    public static void mark(EasterBossEntity easterBossEntity, String string, int n) {
        if (easterBossEntity.getVariant() != 6 || easterBossEntity.m_9236_().f_46443_) {
            return;
        }
        BodyCueAccess bodyCueAccess = easterBossEntity;
        CompoundTag compoundTag = bodyCueAccess.ftboss$bodyRead().m_6426_();
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128359_("id", string);
        compoundTag2.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag2.m_128405_("windup", Math.max(0, Math.min(200, n)));
        compoundTag.m_128365_("xuanlanBody", (Tag)compoundTag2);
        bodyCueAccess.ftboss$bodyWrite(compoundTag);
    }

    public static void flywheel(EasterBossEntity easterBossEntity) {
        BodySignals.mark(easterBossEntity, "flywheel", easterBossEntity.getChallengeDifficulty().acceleratedTicks(8) * 6);
    }

    public static void beam(EasterBossEntity easterBossEntity) {
        BodySignals.mark(easterBossEntity, "beam", 0);
    }

    public static void combo(EasterBossEntity easterBossEntity) {
        BodySignals.mark(easterBossEntity, "combo", 12);
    }

    public static void vent(EasterBossEntity easterBossEntity) {
        BodySignals.mark(easterBossEntity, "vent", easterBossEntity.getChallengeDifficulty().acceleratedTicks(10));
    }

    public static void five(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, Vec3 vec3, int n) {
        if (skill == ManorFiveCombatRules.Skill.XUANLAN_RAIL) {
            BodySignals.mark(easterBossEntity, "rail", skill.windup - n);
        } else if (skill == ManorFiveCombatRules.Skill.XUANLAN_RING) {
            BodySignals.mark(easterBossEntity, "ring", skill.windup - n);
        } else {
            return;
        }
        if (easterBossEntity.getVariant() != 6) {
            return;
        }
        BodyCueAccess bodyCueAccess = easterBossEntity;
        CompoundTag compoundTag = bodyCueAccess.ftboss$bodyRead().m_6426_();
        CompoundTag compoundTag2 = compoundTag.m_128469_("xuanlanBody");
        compoundTag2.m_128347_("aimYaw", Math.toDegrees(-Math.atan2(vec3.f_82479_, vec3.f_82481_)));
        compoundTag2.m_128347_("aimPitch", Math.toDegrees(-Math.atan2(vec3.f_82480_, Math.hypot(vec3.f_82479_, vec3.f_82481_))));
        compoundTag2.m_128379_("lockedAim", true);
        bodyCueAccess.ftboss$bodyWrite(compoundTag);
    }

    public static void iron(EasterBossEntity easterBossEntity, ManorSpellRules.Kind kind, int n) {
        if (easterBossEntity.getVariant() != 6) {
            return;
        }
        CompoundTag compoundTag = ((BodyCueAccess)easterBossEntity).ftboss$bodyRead();
        if (!compoundTag.m_128471_("parallelSpell") || compoundTag.m_128454_("start") != easterBossEntity.m_9236_().m_46467_()) {
            return;
        }
        if (kind.id.equals("orbital_gears") || kind.id.equals("pressure_lance") || kind.id.equals("magnetic_storm")) {
            BodySignals.mark(easterBossEntity, kind.id, n);
        }
    }

    public static void sky(EasterBossEntity easterBossEntity, int n) {
        if (n == 20) {
            BodySignals.mark(easterBossEntity, "sky_rise", 20);
        } else if (n == 23) {
            BodySignals.mark(easterBossEntity, "sky_return", 20);
        } else if (n == 24 && easterBossEntity.getVariant() == 6) {
            BodyCueAccess bodyCueAccess = easterBossEntity;
            CompoundTag compoundTag = bodyCueAccess.ftboss$bodyRead().m_6426_();
            String string = compoundTag.m_128469_("xuanlanBody").m_128461_("id");
            long l = easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128469_("xuanlanBody").m_128454_("start");
            if (string.equals("sky_return") && l >= 19L) {
                return;
            }
            if (string.startsWith("sky_")) {
                compoundTag.m_128473_("xuanlanBody");
                bodyCueAccess.ftboss$bodyWrite(compoundTag);
            }
        }
    }
}

