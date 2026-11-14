package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraft.nbt.LongArrayTag"},
    remap = true
)
public abstract class OwnerFullRepairLongArrayTagMixin {
    @Inject(
        method = {"add(ILnet/minecraft/nbt/LongTag;)V", "clear()V", "add(ILnet/minecraft/nbt/Tag;)V", "add(ILjava/lang/Object;)V"},
        at = {@At("HEAD")},
        require = 4
    )
    private void ftboss$fullRepairVoidMutation(CallbackInfo var1) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"set(ILnet/minecraft/nbt/LongTag;)Lnet/minecraft/nbt/LongTag;", "setTag(ILnet/minecraft/nbt/Tag;)Z", "addTag(ILnet/minecraft/nbt/Tag;)Z", "remove(I)Lnet/minecraft/nbt/LongTag;", "remove(I)Lnet/minecraft/nbt/Tag;", "set(ILnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "remove(I)Ljava/lang/Object;", "set(ILjava/lang/Object;)Ljava/lang/Object;"},
        at = {@At("HEAD")},
        require = 8
    )
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"getAsLongArray()[J"},
        at = {@At("RETURN")},
        require = 1
    )
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.alias(this, var1.getReturnValue(), true);
        } catch (Throwable var3) {
        }
    }
}
