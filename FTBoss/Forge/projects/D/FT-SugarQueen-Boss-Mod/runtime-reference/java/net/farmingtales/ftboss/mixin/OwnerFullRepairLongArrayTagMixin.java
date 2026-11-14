/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.nbt.LongArrayTag"}, remap=true)
public abstract class OwnerFullRepairLongArrayTagMixin {
    @Inject(method={"add(ILnet/minecraft/nbt/LongTag;)V", "clear()V", "add(ILnet/minecraft/nbt/Tag;)V", "add(ILjava/lang/Object;)V"}, at={@At(value="HEAD")}, require=4)
    private void ftboss$fullRepairVoidMutation(CallbackInfo callbackInfo) {
        try {
            OwnerFullRepairGuard.changed(this, 20);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"set(ILnet/minecraft/nbt/LongTag;)Lnet/minecraft/nbt/LongTag;", "setTag(ILnet/minecraft/nbt/Tag;)Z", "addTag(ILnet/minecraft/nbt/Tag;)Z", "remove(I)Lnet/minecraft/nbt/LongTag;", "remove(I)Lnet/minecraft/nbt/Tag;", "set(ILnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", "remove(I)Ljava/lang/Object;", "set(ILjava/lang/Object;)Ljava/lang/Object;"}, at={@At(value="HEAD")}, require=8)
    private void ftboss$fullRepairReturnedMutation(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.changed(this, 21);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"getAsLongArray()[J"}, at={@At(value="RETURN")}, require=1)
    private void ftboss$fullRepairAlias(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFullRepairGuard.alias(this, callbackInfoReturnable.getReturnValue(), true);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

