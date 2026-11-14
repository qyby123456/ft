package net.farmingtales.ftboss.mixin;

import java.util.Set;
import net.farmingtales.ftboss.boss.ai.DandanTeleportSeal;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 兜底直接调用连接层的主动传送，但不拦截服务端移动纠偏。 */
@Mixin(value = ServerGamePacketListenerImpl.class, remap = false)
public abstract class DandanConnectionTeleportMixin {
    @Inject(method = {"teleport(DDDFFLjava/util/Set;)V", "m_9780_(DDDFFLjava/util/Set;)V"},
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$teleport(double targetX, double targetY, double targetZ, float yaw, float pitch,
                                 Set<RelativeMovement> relative, CallbackInfo callback) {
        if (DandanTeleportSeal.blockConnection(((ServerGamePacketListenerImpl) (Object) this).player)) callback.cancel();
    }
}
