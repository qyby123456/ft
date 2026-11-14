package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {LivingEntity.class},
    remap = false
)
public abstract class NativeHealObservationMixin {
    @Redirect(
        method = {"heal(F)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/event/EventHooks;onLivingHeal(Lnet/minecraft/world/entity/LivingEntity;F)F"
        ),
        require = 0,
        remap = false
    )
    @Group(
        name = "native$heal",
        min = 1,
        max = 1
    )
    private float nativeHealSrg(LivingEntity var1, float var2) {
        return observe(var1, var2);
    }

    private static float observe(LivingEntity var0, float var1) {
        float var2 = EventHooks.onLivingHeal(var0, var1);
        NativeActionObservation.heal(var0, var2);
        return var2;
    }
}
