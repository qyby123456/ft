/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.capabilities.CapabilityProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CapabilityProvider.class}, remap=false)
public abstract class NativeExpectedProviderObservationMixin {
    @Inject(method={"invalidateCaps()V", "reviveCaps()V"}, at={@At(value="HEAD")}, require=2, remap=false)
    private void ftboss$originalProviderBefore(CallbackInfo callbackInfo) {
        NativeExpectedFieldObservation.providerFields(this);
    }

    @Inject(method={"invalidateCaps()V", "reviveCaps()V"}, at={@At(value="RETURN")}, require=2, remap=false)
    private void ftboss$originalProviderAfter(CallbackInfo callbackInfo) {
        NativeExpectedFieldObservation.providerFields(this);
    }
}

