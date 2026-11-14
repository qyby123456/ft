package net.farmingtales.ftboss.mixin;

import java.util.Set;
import net.farmingtales.ftboss.boss.ai.DandanTeleportSeal;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {ServerGamePacketListenerImpl.class},
    remap = false
)
public abstract class DandanConnectionTeleportMixin {
    @Inject(
        method = {"teleport(DDDFFLjava/util/Set;)V", "teleport(DDDFFLjava/util/Set;)V"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$teleport(double var1, double var3, double var5, float var7, float var8, Set<RelativeMovement> var9, CallbackInfo var10) {
        if (DandanTeleportSeal.blockConnection(((ServerGamePacketListenerImpl)(Object)this).player)) {
            var10.cancel();
        }
    }
}
