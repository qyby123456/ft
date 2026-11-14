/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.combat.dorabasic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class DoraBasicCue {
    public static final String KEY = "doraBasic";

    private DoraBasicCue() {
    }

    public static CompoundTag stamp(CompoundTag compoundTag, int n, long l, int n2) {
        if (n2 != 2) {
            return compoundTag;
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = compoundTag2.m_128469_(KEY).m_6426_();
        String string = n == 0 ? "gun" : "spear";
        compoundTag3.m_128356_(string + "Tick", l);
        compoundTag3.m_128405_(string + "Sequence", Math.floorMod(compoundTag3.m_128451_(string + "Sequence"), 3) + 1);
        compoundTag2.m_128365_(KEY, (Tag)compoundTag3);
        return compoundTag2;
    }
}

