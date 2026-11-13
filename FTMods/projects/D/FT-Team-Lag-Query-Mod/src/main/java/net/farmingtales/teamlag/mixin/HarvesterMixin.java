package net.farmingtales.teamlag.mixin;

import net.farmingtales.teamlag.MachineTiming;
import net.farmingtales.teamlag.ReflectionAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.farmingtales.scheduler.HarvesterTask", remap = false)
public abstract class HarvesterMixin {
    @Inject(method = "tickSlice", at = @At("HEAD"), remap = false)
    private void ftteamlagBefore(CallbackInfoReturnable<Boolean> callback) {
        var entity = ReflectionAccess.blockEntity(ReflectionAccess.field(this, "entry"));
        MachineTiming.begin(entity);
    }
    @Inject(method = "tickSlice", at = @At("RETURN"), remap = false)
    private void ftteamlagAfter(CallbackInfoReturnable<Boolean> callback) {
        var entity = ReflectionAccess.blockEntity(ReflectionAccess.field(this, "entry"));
        if (entity != null) MachineTiming.end(entity);
    }
}
