package ftboss.clientfxprobe;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientSummonPacketObserver {
    @Inject(method="handleParticleEvent",at=@At("RETURN"),require=1)
    private void observeOriginalReturn(ClientboundLevelParticlesPacket packet,CallbackInfo callback) {
        BossClientSummonFxProbe.packetReturned(packet);
    }
}
