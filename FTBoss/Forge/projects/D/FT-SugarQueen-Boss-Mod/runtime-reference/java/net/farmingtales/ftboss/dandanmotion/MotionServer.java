/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.dandanmotion;

import java.util.Set;
import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.minecraft.nbt.CompoundTag;

public final class MotionServer {
    private static final String[] KEYS = new String[]{"dandan_motion_clip", "dandan_motion_start", "dandan_motion_ticks", "dandan_motion_reverse", "dandan_motion_schema"};

    private MotionServer() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 1 && !easterBossEntity.isMini() && easterBossEntity.m_6084_() && !easterBossEntity.m_9236_().f_46443_ && easterBossEntity instanceof MotionAccess;
    }

    public static void play(EasterBossEntity easterBossEntity, String string, int n, boolean bl) {
        if (!MotionServer.eligible(easterBossEntity)) {
            return;
        }
        MotionAccess motionAccess = easterBossEntity;
        CompoundTag compoundTag = motionAccess.dandanMotionData().m_6426_();
        compoundTag.m_128359_("dandan_motion_clip", string);
        compoundTag.m_128356_("dandan_motion_start", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("dandan_motion_ticks", Math.max(1, Math.min(200, n)));
        compoundTag.m_128379_("dandan_motion_reverse", bl);
        compoundTag.m_128405_("dandan_motion_schema", 1);
        motionAccess.dandanMotionData(compoundTag);
    }

    public static void start(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, int n) {
        if (skill == ManorFiveCombatRules.Skill.DANDAN_REAP) {
            MotionServer.play(easterBossEntity, "reap_charge", skill.windup - n, false);
        }
        if (skill == ManorFiveCombatRules.Skill.DANDAN_STAR) {
            MotionServer.play(easterBossEntity, "star_charge", skill.windup - n, false);
        }
    }

    public static void pulse(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill) {
        if (skill == ManorFiveCombatRules.Skill.DANDAN_REAP) {
            MotionServer.play(easterBossEntity, "reap_cut", skill.interval, false);
        }
        if (skill == ManorFiveCombatRules.Skill.DANDAN_STAR) {
            MotionServer.play(easterBossEntity, "star_release", skill.interval, false);
        }
    }

    public static void finish(EasterBossEntity easterBossEntity) {
        if (!MotionServer.eligible(easterBossEntity)) {
            return;
        }
        String string = ((MotionAccess)easterBossEntity).dandanMotionData().m_128461_("dandan_motion_clip");
        if (string.equals("reap_cut")) {
            MotionServer.play(easterBossEntity, "settle", 7, false);
        }
        if (string.equals("star_release")) {
            MotionServer.play(easterBossEntity, "star_charge", 7, true);
        }
    }

    public static void carry(EasterBossEntity easterBossEntity, CompoundTag compoundTag) {
        if (!MotionServer.eligible(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag2 = ((MotionAccess)easterBossEntity).dandanMotionData();
        String string = compoundTag2.m_128461_("dandan_motion_clip");
        if (!compoundTag.m_128471_("cast") && Set.of("vortex_open", "swap_fold", "rush_cut").contains(string)) {
            return;
        }
        long l = easterBossEntity.m_9236_().m_46467_() - compoundTag2.m_128454_("dandan_motion_start");
        if (compoundTag2.m_128451_("dandan_motion_schema") != 1 || l < 0L || l >= (long)compoundTag2.m_128451_("dandan_motion_ticks")) {
            return;
        }
        for (String string2 : KEYS) {
            if (!compoundTag2.m_128441_(string2)) continue;
            compoundTag.m_128365_(string2, compoundTag2.m_128423_(string2).m_6426_());
        }
    }

    public static void clear(EasterBossEntity easterBossEntity) {
        if (!MotionServer.eligible(easterBossEntity)) {
            return;
        }
        MotionAccess motionAccess = easterBossEntity;
        CompoundTag compoundTag = motionAccess.dandanMotionData().m_6426_();
        for (String string : KEYS) {
            compoundTag.m_128473_(string);
        }
        motionAccess.dandanMotionData(compoundTag);
    }

    public static void swap(EasterBossEntity easterBossEntity) {
        MotionServer.play(easterBossEntity, "swap_fold", 9, false);
    }

    public static void rush(EasterBossEntity easterBossEntity) {
        MotionServer.play(easterBossEntity, "rush_cut", 5, false);
    }

    public static void vortex(EasterBossEntity easterBossEntity) {
        MotionServer.play(easterBossEntity, "vortex_open", 16, false);
    }
}

