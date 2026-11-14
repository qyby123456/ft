package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.farmingtales.ftboss.event.NativeWeaponDamagePhases;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NativeWeaponDamagePhases.class, remap = false)
public abstract class NativePaidHurtDispatchMixin {
    @Redirect(
        method = "beforeMitigation(Lnet/minecraft/world/entity/LivingEntity;Lnet/neoforged/neoforge/common/damagesource/DamageContainer;)Z",
        at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"),
        require = 1,
        remap = false
    )
    private static Event ftboss$paidHurtDispatch(IEventBus bus, Event event) {
        if (!(event instanceof BeforeMitigation)) {
            throw new IllegalStateException("Unexpected native before-mitigation dispatch event");
        }
        FoodWeaponFinalDamage.dispatchOriginalHurtEvent(bus, event);
        return event;
    }

    @Inject(
        method = "beforeHealth(Lnet/neoforged/neoforge/common/damagesource/DamageContainer;Lnet/minecraft/world/entity/LivingEntity;)F",
        at = @At("RETURN"),
        cancellable = true,
        require = 1,
        remap = false
    )
    private static void ftboss$finalDamageGuard(DamageContainer container, LivingEntity entity, CallbackInfoReturnable<Float> callback) {
        float amount = FoodWeaponFinalDamage.finish(entity, container.getSource(), callback.getReturnValueF());
        container.setNewDamage(amount);
        callback.setReturnValue(amount);
    }
}
