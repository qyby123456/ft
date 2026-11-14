/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 */
package net.farmingtales.ftboss.doramotion;

import net.minecraft.nbt.CompoundTag;

public final class DoraMotionSnapshot {
    private DoraMotionSnapshot() {
    }

    public static CompoundTag update(CompoundTag compoundTag, long l, int n, int n2, double d, boolean bl, double d2) {
        boolean bl2;
        long l2;
        boolean bl3;
        n = Math.max(0, n);
        n2 = Math.max(0, n2);
        boolean bl4 = n > 0 != compoundTag.m_128451_("shots") > 0 || n2 > 0 != compoundTag.m_128451_("reload") > 0 || bl != compoundTag.m_128471_("sniper") ? true : (bl3 = false);
        if (!bl3 && n == 0 && n2 == 0 && !bl) {
            return compoundTag;
        }
        long l3 = l2 = compoundTag.m_128441_("captureAt") ? l - compoundTag.m_128454_("captureAt") : Long.MAX_VALUE;
        if (!bl3 && l2 >= 0L && l2 < (long)(n2 > 0 ? 2 : 4)) {
            return compoundTag;
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        compoundTag2.m_128405_("shots", n);
        compoundTag2.m_128405_("reload", n2);
        compoundTag2.m_128379_("sniper", bl);
        if (n2 > 0) {
            double d3;
            double d4 = Double.isFinite(d) ? Math.max(0.0, Math.min(1.0, d)) : 0.0;
            double d5 = Math.max(0.0, Math.min(1.0, 1.0 - ((double)n2 - d4) / 60.0));
            long l4 = compoundTag.m_128441_("reloadAt") ? l - compoundTag.m_128454_("reloadAt") : 0L;
            double d6 = d3 = compoundTag.m_128451_("reload") > 0 && l4 > 0L ? (d5 - compoundTag.m_128459_("reloadProgress")) / (double)l4 : d2;
            if (!Double.isFinite(d3) || d3 <= 0.0) {
                d3 = Double.isFinite(d2) && d2 > 0.0 ? d2 : 0.016666666666666666;
            }
            compoundTag2.m_128347_("reloadProgress", d5);
            compoundTag2.m_128347_("reloadRate", Math.min(1.0, d3));
            compoundTag2.m_128356_("reloadAt", l);
        } else if (compoundTag.m_128451_("reload") > 0) {
            compoundTag2.m_128347_("reloadProgress", 1.0);
            compoundTag2.m_128473_("reloadAt");
            compoundTag2.m_128473_("reloadRate");
        }
        boolean bl5 = bl2 = n > 0 || n2 > 0 || bl;
        if (bl2 || bl3) {
            compoundTag2.m_128356_("sampleAt", l);
            compoundTag2.m_128356_("captureAt", l);
        }
        return compoundTag2.equals((Object)compoundTag) ? compoundTag : compoundTag2;
    }

    public static double reloadProgress(CompoundTag compoundTag, long l, float f) {
        double d = Math.max(0.0f, Math.min(2.0f, (float)(l - compoundTag.m_128454_("reloadAt")) + f));
        return Math.max(0.0, Math.min(1.0, compoundTag.m_128459_("reloadProgress") + d * compoundTag.m_128459_("reloadRate")));
    }

    public static boolean primary(String string) {
        return !"offhand".equals(string);
    }
}

