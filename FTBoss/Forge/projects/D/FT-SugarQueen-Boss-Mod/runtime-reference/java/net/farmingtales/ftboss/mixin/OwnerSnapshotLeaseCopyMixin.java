/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.nbt.CompoundTag
 *  org.spongepowered.asm.mixin.Mixin
 */
package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={CompoundTag.class})
public abstract class OwnerSnapshotLeaseCopyMixin {
    @WrapMethod(method={"copy()Lnet/minecraft/nbt/CompoundTag;"})
    private CompoundTag ftboss$ownerSnapshotCopy(Operation<CompoundTag> operation) {
        CompoundTag compoundTag = (CompoundTag)operation.call(new Object[0]);
        try {
            OwnerFullRepairGuard.snapshotCopied(this, compoundTag);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return compoundTag;
    }
}

