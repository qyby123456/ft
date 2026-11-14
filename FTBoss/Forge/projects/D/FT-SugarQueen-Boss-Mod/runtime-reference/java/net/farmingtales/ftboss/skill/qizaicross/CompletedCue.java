/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.skill.qizaicross;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class CompletedCue {
    private CompletedCue() {
    }

    public static void sweep(ServerLevel serverLevel, EasterBossEntity easterBossEntity, int n, long l, int n2, Vec3 vec3, int n3, int n4, int n5) {
        if (n <= 0 || easterBossEntity.getVariant() != 11 || !easterBossEntity.m_6084_() || easterBossEntity.m_213877_() || serverLevel != easterBossEntity.m_9236_() || n2 <= 0) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        if (n < 3) {
            if (compoundTag.m_128451_("qizai") == 7) {
                compoundTag.m_128405_("activeTicks", Math.max(1, n4));
                easterBossEntity.setQizaiCue(compoundTag);
            }
            return;
        }
        if (compoundTag.m_128451_("qizai") != 0 && compoundTag.m_128451_("completePhase") != 3 && compoundTag.m_128451_("completePhase") != 4) {
            return;
        }
        if (n == 4) {
            l = serverLevel.m_46467_() - (long)Math.max(0, 24 - n5);
            n2 = 24;
        }
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128405_("qizai", 7);
        compoundTag2.m_128356_("start", l);
        compoundTag2.m_128405_("duration", n2);
        compoundTag2.m_128379_("released", true);
        compoundTag2.m_128405_("completePhase", n);
        compoundTag2.m_128356_("windupStart", l);
        compoundTag2.m_128405_("windupDuration", n2);
        compoundTag2.m_128405_("activeTicks", Math.max(1, n4));
        compoundTag2.m_128405_("volley", n == 4 ? 3 : 1);
        compoundTag2.m_128405_("elapsed", (int)Math.max(0L, serverLevel.m_46467_() - l));
        compoundTag2.m_128347_("anchorX", vec3.f_82479_);
        compoundTag2.m_128347_("anchorY", vec3.f_82480_);
        compoundTag2.m_128347_("anchorZ", vec3.f_82481_);
        easterBossEntity.setQizaiCue(compoundTag2);
    }
}

