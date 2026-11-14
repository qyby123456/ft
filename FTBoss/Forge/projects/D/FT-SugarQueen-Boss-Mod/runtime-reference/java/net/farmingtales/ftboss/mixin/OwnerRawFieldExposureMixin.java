/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.nbt.CompoundTag", "net.minecraft.nbt.ListTag", "net.minecraft.nbt.ByteArrayTag", "net.minecraft.nbt.IntArrayTag", "net.minecraft.nbt.LongArrayTag", "net.minecraft.world.item.ItemStack", "net.minecraft.world.entity.Entity", "net.minecraftforge.common.capabilities.CapabilityProvider"}, remap=false)
public abstract class OwnerRawFieldExposureMixin {
    @Inject(method={"ftboss$rawCompoundFields", "ftboss$rawListFields", "ftboss$rawBytes", "ftboss$rawInts", "ftboss$rawLongs", "ftboss$rawStackTag", "ftboss$rawStackCaps", "ftboss$rawPersistentData", "ftboss$rawCapsLazyData", "ftboss$rawDispatcher", "ftboss$rawCapsParentSupplier"}, at={@At(value="RETURN")}, require=1)
    private void ftboss$ownerRawExposed(CallbackInfoReturnable<?> callbackInfoReturnable) {
        try {
            OwnerFieldExpectedAuthority.rawExposed(this, callbackInfoReturnable.getReturnValue());
        }
        catch (Throwable throwable) {
            try {
                OwnerFieldExpectedAuthority.changed(this);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
        }
    }
}

