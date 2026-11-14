package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class NativeSocketHelperTickMixin {
    @Inject(method = "tick()V", at = @At("HEAD"), require = 1, remap = false)
    private void ftboss$nativeSocketHelperTick(CallbackInfo callback) {
        FoodSocketPowers.helperTick((LivingEntity)(Object)this);
    }
}
