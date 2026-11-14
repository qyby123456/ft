/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={CompoundTag.class}, remap=false)
public abstract class NativeExpectedAliasReturnMixin {
    @Inject(method={"m_128423_(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$originalAliasReturned(String string, CallbackInfoReturnable<Tag> callbackInfoReturnable) {
        NativeExpectedFieldObservation.aliasReturned(this, string, callbackInfoReturnable.getReturnValue());
    }
}

