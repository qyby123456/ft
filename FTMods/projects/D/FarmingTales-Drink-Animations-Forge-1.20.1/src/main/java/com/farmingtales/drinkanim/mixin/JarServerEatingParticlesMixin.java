package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.JarEatingParticles;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class JarServerEatingParticlesMixin {
    // Check the original mouth position before the client adds Gaussian spread.
    // The packet has already been dispatched onto the render thread here.
    @Inject(method="handleParticleEvent",at=@At(value="INVOKE",
        target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
        shift=At.Shift.AFTER),cancellable=true)
    private void farmingtales$serverJarEatingParticles(ClientboundLevelParticlesPacket packet,CallbackInfo ci) {
        if(JarEatingParticles.hide(packet.getParticle(),packet.getX(),packet.getY(),packet.getZ()))ci.cancel();
    }
}
