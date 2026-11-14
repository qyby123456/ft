package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeItemHandlerLifetime232;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStack.class, remap = false)
public abstract class NativeHandlerChildCountMixin244 {
    @Unique
    private void ftboss$nativeChildCountChanging() {
        try { NativeItemHandlerLifetime232.childCountChanging((ItemStack)(Object)this); }
        catch (Throwable observationFailure) { }
    }

    @Inject(method = "setCount(I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;count:I", opcode = Opcodes.PUTFIELD),
        require = 1, allow = 1, remap = false)
    private void ftboss$nativeChildCountWrite(CallbackInfo callback) {
        this.ftboss$nativeChildCountChanging();
    }
}
