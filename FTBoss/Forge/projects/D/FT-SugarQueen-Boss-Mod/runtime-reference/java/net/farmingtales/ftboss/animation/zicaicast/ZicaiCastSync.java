/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.animation.zicaicast;

import net.farmingtales.ftboss.animation.zicaicast.ZicaiCastAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.nbt.CompoundTag;

public final class ZicaiCastSync {
    private ZicaiCastSync() {
    }

    public static void start(EasterBossEntity easterBossEntity, ManorSpellRules.Kind kind, int n) {
        int n2;
        if (easterBossEntity.getVariant() != 8 || !(easterBossEntity instanceof ZicaiCastAccess)) {
            return;
        }
        ZicaiCastAccess zicaiCastAccess = easterBossEntity;
        switch (kind.id) {
            case "luminous_rebuke": {
                int n3 = 0;
                break;
            }
            case "halo_sanctuary": {
                int n3 = 1;
                break;
            }
            case "seraph_descent": {
                int n3 = 2;
                break;
            }
            default: {
                int n3 = n2 = -1;
            }
        }
        if (n2 < 0 || n < 1 || n > 200) {
            return;
        }
        String string = new CompoundTag();
        string.m_128405_("track", n2);
        string.m_128405_("duration", n);
        string.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        string.m_128405_("stage", 0);
        zicaiCastAccess.zicaiCastState((CompoundTag)string);
    }

    public static void release(EasterBossEntity easterBossEntity) {
        ZicaiCastSync.stop(easterBossEntity, 1);
    }

    public static void finish(EasterBossEntity easterBossEntity, boolean bl) {
        ZicaiCastSync.stop(easterBossEntity, 2);
    }

    private static void stop(EasterBossEntity easterBossEntity, int n) {
        if (easterBossEntity.getVariant() != 8 || !(easterBossEntity instanceof ZicaiCastAccess)) {
            return;
        }
        ZicaiCastAccess zicaiCastAccess = easterBossEntity;
        CompoundTag compoundTag = zicaiCastAccess.zicaiCastState();
        if (compoundTag.m_128456_() || compoundTag.m_128451_("stage") != 0) {
            return;
        }
        compoundTag.m_128356_("stop", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("stage", n);
        zicaiCastAccess.zicaiCastState(compoundTag);
    }
}

