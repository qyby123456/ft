package com.farmingtales.drinkanim;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DrinkConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED, SOUNDS;
    public static final ForgeConfigSpec.DoubleValue STRENGTH, VOLUME;
    static {
        var b = new ForgeConfigSpec.Builder();
        ENABLED = b.comment("启用已适配酒水和饮料的第一人称动画").define("enabled", true);
        SOUNDS = b.comment("播放饮用、检视和液体晃动音效").define("sounds", true);
        STRENGTH = b.comment("行走、起跳、落地和转向时的液面晃动强度").defineInRange("sloshStrength", 1.0, 0.0, 2.0);
        VOLUME = b.comment("附加音效音量，游戏音量仍然生效").defineInRange("soundVolume", 0.80, 0.0, 1.0);
        SPEC = b.build();
    }
}
