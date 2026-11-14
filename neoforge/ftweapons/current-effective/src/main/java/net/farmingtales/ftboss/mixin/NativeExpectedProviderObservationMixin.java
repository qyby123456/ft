package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeCapabilityRegistrationFields;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegisterCapabilitiesEvent.class, remap = false)
public abstract class NativeExpectedProviderObservationMixin {
    @Unique
    private static void ftboss$nativeProviderRevocationBefore(Object capability) {
        try {
            OwnerFullRepairGuard.changed(capability, 20);
        } catch (Throwable failure) {
        }
        try {
            net.farmingtales.ftboss.item.NativeCapabilityOwnerLinks194.changing(capability);
        } catch (Throwable failure) {
        }
    }

    @Inject(method = "registerItem(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V", at = @At("HEAD"), require = 1, remap = false)
    private void ftboss$originalProviderBefore(ItemCapability<?, ?> capability, ICapabilityProvider<?, ?, ?> provider, ItemLike[] items, CallbackInfo callback) {
        try {
            net.farmingtales.ftboss.item.NativeItemCapabilityObservation.nativeRepairProviders(capability, provider, items, true);
        } catch (Throwable observationFailure) {
        }
        ftboss$nativeProviderRevocationBefore(capability);
        if ((Object)capability instanceof NativeCapabilityRegistrationFields fields) fields.ftboss$nativeCapabilityRegistration();
        NativeExpectedFieldObservation.providerFields(capability);
    }

    @Inject(method = "registerItem(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V", at = @At("RETURN"), require = 1, remap = false)
    private void ftboss$originalProviderAfter(ItemCapability<?, ?> capability, ICapabilityProvider<?, ?, ?> provider, ItemLike[] items, CallbackInfo callback) {
        try {
            net.farmingtales.ftboss.item.NativeItemCapabilityObservation.nativeRepairProviders(capability, provider, items, false);
        } catch (Throwable observationFailure) {
        }
        NativeExpectedFieldObservation.providerFields(capability);
    }
}
