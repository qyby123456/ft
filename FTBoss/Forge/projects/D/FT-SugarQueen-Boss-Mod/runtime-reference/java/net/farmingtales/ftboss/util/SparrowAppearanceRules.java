/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

public final class SparrowAppearanceRules {
    public static final int MIN_APPEARANCES = 4;
    public static final int MAX_APPEARANCES = 10;
    public static final int VALID_MASK = 3583;

    private SparrowAppearanceRules() {
    }

    public static int bit(int variant) {
        return variant >= 0 && variant <= 11 ? 1 << variant & 0xDFF : 0;
    }

    public static int plan(UUID fight) {
        Random random = new Random(fight.getMostSignificantBits() ^ fight.getLeastSignificantBits());
        ArrayList<Integer> variants = new ArrayList<Integer>();
        for (int variant = 0; variant <= 11; ++variant) {
            if (SparrowAppearanceRules.bit(variant) == 0) continue;
            variants.add(variant);
        }
        Collections.shuffle(variants, random);
        int appearances = 4 + random.nextInt(7);
        int plan = 0;
        for (int index = 0; index < appearances; ++index) {
            plan |= SparrowAppearanceRules.bit((Integer)variants.get(index));
        }
        return plan;
    }
}

