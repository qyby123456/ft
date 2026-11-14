/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;

public final class DoraSniperDamageRules {
    public static final List<Hit> TABLE = List.of(new Hit("dora_sniper_void", 80.0f), new Hit("dora_sniper_physical", 80.0f), new Hit("dora_sniper_magic", 80.0f));

    private DoraSniperDamageRules() {
    }

    public record Hit(String type, float amount) {
    }
}

