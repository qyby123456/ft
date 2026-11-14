package net.farmingtales.ftboss.util;

import java.util.List;

public final class DoraSniperDamageRules {
    public record Hit(String type, float amount) {}

    public static final List<Hit> TABLE = List.of(
            new Hit("dora_sniper_void", 240.0F),
            new Hit("dora_sniper_physical", 240.0F),
            new Hit("dora_sniper_magic", 240.0F)
    );

    private DoraSniperDamageRules() {}
}
