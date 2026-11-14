/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.doramotion;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.farmingtales.ftboss.doramotion.DoraMotionAccess;
import net.farmingtales.ftboss.doramotion.DoraMotionSnapshot;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.DoraSniperCycle;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;

public final class DoraMotionSync {
    private DoraMotionSync() {
    }

    private static DoraMotionAccess access(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 2 && !easterBossEntity.isMini() && !easterBossEntity.m_9236_().f_46443_ && easterBossEntity instanceof DoraMotionAccess ? (DoraMotionAccess)easterBossEntity : null;
    }

    public static void capture(EasterBossEntity easterBossEntity, int n, int n2, double d, boolean bl) {
        long l;
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState();
        CompoundTag compoundTag2 = DoraMotionSnapshot.update(compoundTag, l = easterBossEntity.m_9236_().m_46467_(), n, n2, d, bl, easterBossEntity.getChallengeDifficulty().attackRate() / 60.0);
        if (compoundTag2 != compoundTag) {
            doraMotionAccess.doraMotionState(compoundTag2);
        }
    }

    public static void shot(EasterBossEntity easterBossEntity) {
        DoraMotionSync.stamp(easterBossEntity, "shotAt");
    }

    public static void spear(EasterBossEntity easterBossEntity) {
        DoraMotionSync.stamp(easterBossEntity, "spearAt");
    }

    public static void fire(EasterBossEntity easterBossEntity) {
        DoraMotionSync.stamp(easterBossEntity, "fireAt");
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState().m_6426_();
        compoundTag.m_128405_("recovery", easterBossEntity.isImpossibleChallenge() ? 6 : 12);
        doraMotionAccess.doraMotionState(compoundTag);
    }

    private static void stamp(EasterBossEntity easterBossEntity, String string) {
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState().m_6426_();
        compoundTag.m_128356_(string, easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128356_("sampleAt", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_(string + "Serial", compoundTag.m_128451_(string + "Serial") + 1);
        doraMotionAccess.doraMotionState(compoundTag);
    }

    public static void sniper(EasterBossEntity easterBossEntity) {
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState().m_6426_();
        compoundTag.m_128356_("sniperAt", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128356_("sampleAt", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("charge", easterBossEntity.isImpossibleChallenge() ? (DoraSniperCycle.CHARGE_TICKS + 1) / 2 : DoraSniperCycle.CHARGE_TICKS);
        compoundTag.m_128405_("lock", easterBossEntity.isImpossibleChallenge() ? (DoraSniperCycle.TRACK_TICKS + 1) / 2 : DoraSniperCycle.TRACK_TICKS);
        compoundTag.m_128379_("sniper", true);
        compoundTag.m_128473_("fireAt");
        doraMotionAccess.doraMotionState(compoundTag);
    }

    public static void clear(EasterBossEntity easterBossEntity) {
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess != null && !doraMotionAccess.doraMotionState().m_128456_()) {
            doraMotionAccess.doraMotionState(new CompoundTag());
        }
    }

    public static void cancelSniper(EasterBossEntity easterBossEntity) {
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState().m_6426_();
        if (!compoundTag.m_128441_("sniperAt") && !compoundTag.m_128441_("fireAt")) {
            return;
        }
        compoundTag.m_128379_("sniper", false);
        compoundTag.m_128473_("sniperAt");
        compoundTag.m_128473_("fireAt");
        if (!compoundTag.equals((Object)doraMotionAccess.doraMotionState())) {
            doraMotionAccess.doraMotionState(compoundTag);
        }
    }

    public static void begin(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData) {
        EasterBossEntity easterBossEntity;
        block5: {
            block4: {
                if (kind == null || kind.variant != 2 || !(livingEntity instanceof EasterBossEntity)) break block4;
                easterBossEntity = (EasterBossEntity)livingEntity;
                if (magicData != null && DoraMotionSnapshot.primary(magicData.getCastingEquipmentSlot())) break block5;
            }
            return;
        }
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState().m_6426_();
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag2.m_128359_("id", kind.id);
        compoundTag2.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag2.m_128405_("duration", Math.max(1, Math.min(2400, magicData.getCastDuration())));
        compoundTag2.m_128405_("phase", 0);
        compoundTag.m_128365_("spell", (Tag)compoundTag2);
        doraMotionAccess.doraMotionState(compoundTag);
    }

    public static void release(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData) {
        DoraMotionSync.end(kind, livingEntity, magicData, false);
    }

    public static void complete(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData, boolean bl) {
        if (bl) {
            DoraMotionSync.end(kind, livingEntity, magicData, true);
        }
    }

    private static void end(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData, boolean bl) {
        EasterBossEntity easterBossEntity;
        block6: {
            block5: {
                if (kind == null || kind.variant != 2 || !(livingEntity instanceof EasterBossEntity)) break block5;
                easterBossEntity = (EasterBossEntity)livingEntity;
                if (magicData != null && DoraMotionSnapshot.primary(magicData.getCastingEquipmentSlot())) break block6;
            }
            return;
        }
        DoraMotionAccess doraMotionAccess = DoraMotionSync.access(easterBossEntity);
        if (doraMotionAccess == null) {
            return;
        }
        CompoundTag compoundTag = doraMotionAccess.doraMotionState().m_6426_();
        CompoundTag compoundTag2 = compoundTag.m_128469_("spell");
        if (!kind.id.equals(compoundTag2.m_128461_("id")) || compoundTag2.m_128451_("phase") != 0) {
            return;
        }
        compoundTag2.m_128405_("phase", bl ? 2 : 1);
        compoundTag2.m_128356_("stop", easterBossEntity.m_9236_().m_46467_());
        doraMotionAccess.doraMotionState(compoundTag);
    }
}

