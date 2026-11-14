package net.farmingtales.ftboss.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({CompoundTag.class})
public abstract class OwnerSnapshotLeaseCopyMixin {
    @WrapMethod(
        method = {"copy()Lnet/minecraft/nbt/CompoundTag;"}
    )
    private CompoundTag ftboss$ownerSnapshotCopy(Operation<CompoundTag> var1) {
        CompoundTag var2 = (CompoundTag)var1.call(new Object[0]);

        try {
            OwnerFullRepairGuard.snapshotCopied(this, var2);
        } catch (Throwable var4) {
        }

        return var2;
    }
}
