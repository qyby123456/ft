/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public final class ZhuniBlackwallRules {
    public static final int INTERVAL_TICKS = 200;
    public static final int DURATION_TICKS = 100;
    public static final int ITEM_COUNT = 3;

    private ZhuniBlackwallRules() {
    }

    public static <T> List<T> choose(List<T> list, IntUnaryOperator intUnaryOperator) {
        ArrayList<T> arrayList = new ArrayList<T>(list);
        ArrayList arrayList2 = new ArrayList();
        while (!arrayList.isEmpty() && arrayList2.size() < 3) {
            arrayList2.add(arrayList.remove(intUnaryOperator.applyAsInt(arrayList.size())));
        }
        return arrayList2;
    }

    public static boolean due(long l, long l2) {
        return l2 > 0L && l >= l2;
    }

    public static float strength(float f) {
        if (f < 0.0f || f >= 100.0f) {
            return 0.0f;
        }
        float f2 = Math.min(1.0f, (100.0f - f) / 16.0f);
        return (0.58f + 0.42f * (float)Math.exp(-f / 9.0f)) * f2;
    }
}

