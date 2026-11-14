package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class SparrowAppearanceRules {
    public static final int MIN_APPEARANCES = 4;
    public static final int MAX_APPEARANCES = 10;
    public static final int VALID_MASK = 0xdbf;

    private SparrowAppearanceRules() {}

    public static int bit(int variant) {
        return variant >= 0 && variant <= 11 ? (1 << variant) & VALID_MASK : 0;
    }

    public static int plan(UUID fight) {
        Random random = new Random(fight.getMostSignificantBits() ^ fight.getLeastSignificantBits());
        List<Integer> variants = new ArrayList<>();
        for (int variant = 0; variant <= 11; variant++) {
            if (bit(variant) != 0) variants.add(variant);
        }
        Collections.shuffle(variants, random);
        int appearances = MIN_APPEARANCES + random.nextInt(MAX_APPEARANCES - MIN_APPEARANCES + 1);
        int plan = 0;
        for (int index = 0; index < appearances; index++) plan |= bit(variants.get(index));
        return plan;
    }
}
