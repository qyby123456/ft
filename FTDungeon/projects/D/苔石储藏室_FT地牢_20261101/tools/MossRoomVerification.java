package net.farmingtales.mossroomverification;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("mossroomverification")
public final class MossRoomVerification {
    public MossRoomVerification() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::verify);
    }

    private void verify(GatherDataEvent event) {
        try {
            VerifyMossRoom.main(new String[]{System.getProperty("ftmr.project")});
        } catch (Exception exception) {
            throw new IllegalStateException("FT moss room S2 verification failed", exception);
        }
    }
}
