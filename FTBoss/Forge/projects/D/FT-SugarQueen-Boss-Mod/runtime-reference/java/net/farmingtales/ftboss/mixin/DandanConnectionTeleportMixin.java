/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.entity.RelativeMovement
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import java.util.Set;
import net.farmingtales.ftboss.boss.ai.DandanTeleportSeal;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerGamePacketListenerImpl.class}, remap=false)
public abstract class DandanConnectionTeleportMixin {
    @Inject(method={"teleport(DDDFFLjava/util/Set;)V", "m_9780_(DDDFFLjava/util/Set;)V"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$teleport(double d, double d2, double d3, float f, float f2, Set<RelativeMovement> set, CallbackInfo callbackInfo) {
        if (DandanTeleportSeal.blockConnection(((ServerGamePacketListenerImpl)this).f_9743_)) {
            callbackInfo.cancel();
        }
    }
}

