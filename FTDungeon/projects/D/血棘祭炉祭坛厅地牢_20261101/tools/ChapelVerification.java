package net.farmingtales.chapelverification;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("chapelverification")
public final class ChapelVerification {
    public ChapelVerification() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::verify);
    }

    private void verify(GatherDataEvent event) {
        try {
            VerifyChapel.main(new String[]{System.getProperty("ftbc.project")});
        } catch (Exception exception) {
            throw new IllegalStateException("Bloodthorn chapel verification failed", exception);
        }
    }
}

