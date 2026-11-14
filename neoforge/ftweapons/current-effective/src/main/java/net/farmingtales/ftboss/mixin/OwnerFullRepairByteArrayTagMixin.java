package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraft.nbt.ByteArrayTag"},
    remap = true
)
public abstract class OwnerFullRepairByteArrayTagMixin {
    @Inject(
        method = {"add(ILnet/minecraft/nbt/ByteTag;)V", "clear()V", "add(ILnet/minecraft/nbt/Tag;)V", "add(ILjava/lang/Object;)V"},
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
        method = {"set(ILnet/minecraft/nbt/ByteTag;)Lnet/minecraft/nbt/ByteTag;", "setTag(ILnet/minecraft/nbt/Tag;)Z", "addTag(ILnet/minecraft/nbt/Tag;)Z", "remove(I)Lnet/minecraft/nbt/ByteTag;", "remove(I)Lnet/minecraft/nbt/Tag;", "set(ILnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "remove(I)Ljava/lang/Object;", "set(ILjava/lang/Object;)Ljava/lang/Object;"},
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
        method = {"getAsByteArray()[B"},
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
