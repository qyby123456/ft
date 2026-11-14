/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemStack.class}, remap=false)
public abstract class NativeExpectedTagAssignmentMixin {
    @Inject(method={"m_41751_(Lnet/minecraft/nbt/CompoundTag;)V"}, at={@At(value="FIELD", target="Lnet/minecraft/world/item/ItemStack;f_41590_:Lnet/minecraft/nbt/CompoundTag;", opcode=181, shift=At.Shift.AFTER, remap=false)}, require=1, allow=1, remap=false)
    private void ftboss$originalTagAssigned(CompoundTag compoundTag, CallbackInfo callbackInfo) {
        NativeExpectedFieldObservation.originalTagAssigned((ItemStack)this, compoundTag);
    }
}

