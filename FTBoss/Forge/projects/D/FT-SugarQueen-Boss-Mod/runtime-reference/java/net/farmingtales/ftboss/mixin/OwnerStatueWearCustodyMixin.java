/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={NonNullList.class}, remap=true)
public abstract class OwnerStatueWearCustodyMixin {
    @Inject(method={"set(ILjava/lang/Object;)Ljava/lang/Object;", "remove(I)Ljava/lang/Object;"}, at={@At(value="HEAD")}, require=2)
    private void ftboss$statueCustodyReturned(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerStatueWearAdmission.custodyChanged(this);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Inject(method={"add(ILjava/lang/Object;)V", "clear()V"}, at={@At(value="HEAD")}, require=2)
    private void ftboss$statueCustodyVoid(CallbackInfo callbackInfo) {
        try {
            OwnerStatueWearAdmission.custodyChanged(this);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

