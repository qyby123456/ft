package cn.farmingtales.sdmshopaudit;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AuditConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue SCAN_INTERVAL;
    public static final ForgeConfigSpec.IntValue INLINE_CHANGES;
    public static final ForgeConfigSpec.BooleanValue SNAPSHOTS;
    public static final ForgeConfigSpec.ConfigValue<String> TIME_ZONE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SCAN_INTERVAL = builder.defineInRange("scanIntervalTicks", 200, 20, 72000);
        INLINE_CHANGES = builder.defineInRange("maxInlineChanges", 200, 1, 2000);
        SNAPSHOTS = builder.define("saveSnapshots", true);
        TIME_ZONE = builder.define("timeZone", "Asia/Shanghai");
        SPEC = builder.build();
    }

    private AuditConfig() {
    }
}
