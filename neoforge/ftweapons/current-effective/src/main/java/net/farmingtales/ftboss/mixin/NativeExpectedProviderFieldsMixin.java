package net.farmingtales.ftboss.mixin;

import java.util.Map;
import net.farmingtales.ftboss.item.NativeCapabilityRegistrationFields;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.NativeObservationFrame;
import net.farmingtales.ftboss.item.NativeReadCutObserver;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.farmingtales.ftboss.item.NativeItemCapabilityObservation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ItemCapability.class, remap = false)
public abstract class NativeExpectedProviderFieldsMixin implements NativeCapabilityRegistrationFields {
    @Shadow
    @Final
    private Map<Item, ?> providers;
    @Unique
    private long ftboss$nativeCapabilityRegistrationVersion;

    @Override
    public final Map<Item, ?> ftboss$rawNativeProviders() {
        NativeItemCapabilityObservation.requireFields("ftboss$rawNativeProviders");
        return this.providers;
    }

    @Override
    public final long ftboss$nativeCapabilityVersion() {
        NativeItemCapabilityObservation.requireFields("ftboss$nativeCapabilityVersion");
        return this.ftboss$nativeCapabilityRegistrationVersion;
    }

    @Override
    public final void ftboss$nativeCapabilityRegistration() {
        NativeItemCapabilityObservation.requireRegistration();
        if (this.ftboss$nativeCapabilityRegistrationVersion != Long.MAX_VALUE) this.ftboss$nativeCapabilityRegistrationVersion++;
    }

    @Unique
    private static void ftboss$nativeProviderRepairBefore(Object stack) {
        try {
            OwnerFullRepairGuard.changed(stack, 20);
        } catch (Throwable observationFailure) {
        }
    }

    @Unique
    private static void ftboss$nativeProviderRepairAfter(Object stack, Object result) {
        try {
            OwnerFullRepairGuard.alias(stack, result, true); net.farmingtales.ftboss.item.NativeItemHandlerLifetime232.returned(stack, result);
        } catch (Throwable observationFailure) {
        }
    }

    @Unique
    private static void ftboss$nativeProviderReadFailure(NativeObservationFrame frame, int phase, Throwable failure) {
        if (frame != null) {
            try {
                frame.observationFailed(phase, failure);
            } catch (Throwable recordingFailure) {
            }
        }
    }

    @Unique
    private static void ftboss$nativeProviderReadObserved(Object stack, NativeObservationFrame frame, int phase) {
        try {
            NativeReadCutObserver.weapon(stack);
        } catch (Throwable failure) {
            ftboss$nativeProviderReadFailure(frame, phase, failure);
        }
    }

    @Unique
    private static NativeObservationFrame ftboss$nativeProviderReadBefore(Object stack) {
        NativeObservationFrame frame = null;
        try {
            frame = NativeObservationFrame.open(stack, "net/neoforged/neoforge/capabilities/ItemCapability.nativeProviderQuery190");
        } catch (Throwable failure) {
            ftboss$nativeProviderReadFailure(frame, 0, failure);
        }
        ftboss$nativeProviderReadObserved(stack, frame, 0);
        return frame;
    }

    @Unique
    private static void ftboss$nativeProviderReadFailed(Object stack, NativeObservationFrame frame, Throwable originalFailure) {
        if (frame != null) {
            try {
                frame.originalFailed(originalFailure);
            } catch (Throwable failure) {
                ftboss$nativeProviderReadFailure(frame, 2, failure);
            }
        }
        ftboss$nativeProviderReadObserved(stack, frame, 2);
        try {
            NativeReadCutObserver.failed(stack, frame);
        } catch (Throwable failure) {
            ftboss$nativeProviderReadFailure(frame, 3, failure);
        }
    }

    @Unique
    private static void ftboss$nativeProviderOwnerBefore(Object stack, Object capability) {
        try {
            net.farmingtales.ftboss.item.NativeCapabilityOwnerLinks194.bind(stack, capability);
        } catch (Throwable observationFailure) {
        }
    }

    @Redirect(method = "getCapability(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;",
        at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;getCapability(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
        require = 1, allow = 1, remap = false)
    private Object ftboss$nativeProviderQuery(ICapabilityProvider<Object, Object, Object> provider, Object stack, Object context) {
        NativeObservationFrame readCut = ftboss$nativeProviderReadBefore(stack);
        ftboss$nativeProviderRepairBefore(stack);
        ftboss$nativeProviderOwnerBefore(stack, this);
        Object observation = NativeExpectedFieldObservation.nativeProviderQueryBefore(this, provider, (ItemStack)stack, context);
        try {
            Object result = provider.getCapability(stack, context);
            ftboss$nativeProviderRepairAfter(stack, result);
            NativeExpectedFieldObservation.nativeProviderQueryReturned(observation, result);
            ftboss$nativeProviderReadObserved(stack, readCut, 1);
            return result;
        } catch (Throwable failure) {
            NativeExpectedFieldObservation.nativeProviderQueryFailed(observation, failure);
            ftboss$nativeProviderReadFailed(stack, readCut, failure);
            throw failure;
        }
    }
}
