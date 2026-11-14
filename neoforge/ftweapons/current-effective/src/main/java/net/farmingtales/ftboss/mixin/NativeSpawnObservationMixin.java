package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {ServerLevel.class},
    remap = false
)
public abstract class NativeSpawnObservationMixin {
    @Inject(
        method = {"addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"},
        at = {@At("RETURN")},
        require = 0,
        remap = false
    )
    @Group(
        name = "native$spawn",
        min = 1,
        max = 1
    )
    private void nativeAddedSrg(Entity var1, CallbackInfoReturnable<Boolean> var2) {
        NativeActionObservation.added(var1, (Boolean)var2.getReturnValue());
    }
}
