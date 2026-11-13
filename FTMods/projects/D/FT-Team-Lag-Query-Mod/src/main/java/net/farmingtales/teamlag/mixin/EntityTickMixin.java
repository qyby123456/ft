package net.farmingtales.teamlag.mixin;

import net.farmingtales.teamlag.MachineTiming;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class EntityTickMixin {
    @Inject(method = {"tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", "m_8647_(Lnet/minecraft/world/entity/Entity;)V"}, at = @At("HEAD"), remap = false)
    private void ftteamlagEntityBefore(Entity entity, CallbackInfo callback) { MachineTiming.begin(entity); }
    @Inject(method = {"tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", "m_8647_(Lnet/minecraft/world/entity/Entity;)V"}, at = @At("RETURN"), remap = false)
    private void ftteamlagEntityAfter(Entity entity, CallbackInfo callback) { MachineTiming.end(entity); }
    @Inject(method = {"tickPassenger(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", "m_8662_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V"}, at = @At("HEAD"), remap = false)
    private void ftteamlagPassengerBefore(Entity vehicle, Entity passenger, CallbackInfo callback) { MachineTiming.begin(passenger); }
    @Inject(method = {"tickPassenger(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", "m_8662_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V"}, at = @At("RETURN"), remap = false)
    private void ftteamlagPassengerAfter(Entity vehicle, Entity passenger, CallbackInfo callback) { MachineTiming.end(passenger); }
}
