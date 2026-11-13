package net.farmingtales.archiveverification;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("archiveverification")
public final class ArchiveVerification {
    public ArchiveVerification() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::verify);
    }

    private void verify(GatherDataEvent event) {
        try {
            VerifyArchive.main(new String[]{System.getProperty("ftta.project")});
        } catch (Exception exception) {
            throw new IllegalStateException("Tomb archive verification failed", exception);
        }
    }
}
