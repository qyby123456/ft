package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraft.nbt.ListTag"},
    remap = true
)
public abstract class OwnerFullRepairListTagMixin {
    @Inject(
        method = {"updateTypeAfterRemove()V", "add(ILnet/minecraft/nbt/Tag;)V", "clear()V", "add(ILjava/lang/Object;)V"},
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
        method = {"remove(I)Lnet/minecraft/nbt/Tag;", "set(ILnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "setTag(ILnet/minecraft/nbt/Tag;)Z", "addTag(ILnet/minecraft/nbt/Tag;)Z", "updateType(Lnet/minecraft/nbt/Tag;)Z", "remove(I)Ljava/lang/Object;", "set(ILjava/lang/Object;)Ljava/lang/Object;"},
        at = {@At("HEAD")},
        require = 7
    )
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        } catch (Throwable var3) {
        }
    }

    @Inject(
        method = {"getCompound(I)Lnet/minecraft/nbt/CompoundTag;", "getList(I)Lnet/minecraft/nbt/ListTag;", "getIntArray(I)[I", "getLongArray(I)[J", "get(I)Lnet/minecraft/nbt/Tag;", "get(I)Ljava/lang/Object;"},
        at = {@At("RETURN")},
        require = 6
    )
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFullRepairGuard.alias(this, var1.getReturnValue(), true);
        } catch (Throwable var3) {
        }
    }
}
