package com.farmingtales.drinkanim;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(DrinkAnimations.ID)
public final class DrinkAnimations {
    public static final String ID = "farmingtales_drink_anim";
    public DrinkAnimations() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DrinkConfig.SPEC);
    }
}
