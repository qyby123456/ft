package net.farmingtales.frostgardenverification;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("frostgardenverification")
public final class FrostGardenVerification {
    public FrostGardenVerification() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::verify);
    }

    private void verify(GatherDataEvent event) {
        try {
            VerifyGarden.main(new String[]{System.getProperty("ftfg.project")});
        } catch (Exception exception) {
            throw new IllegalStateException("Frost garden verification failed", exception);
        }
    }
}
