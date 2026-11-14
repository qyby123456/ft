package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraft.nbt.CompoundTag", "net.minecraft.nbt.ListTag", "net.minecraft.nbt.ByteArrayTag", "net.minecraft.nbt.IntArrayTag", "net.minecraft.nbt.LongArrayTag", "net.minecraft.world.item.ItemStack", "net.minecraft.world.entity.Entity", "net.minecraft.core.component.PatchedDataComponentMap", "net.neoforged.neoforge.capabilities.ItemCapability"},
    remap = false
)
public abstract class OwnerRawFieldExposureMixin {
    @Inject(
        method = {"ftboss$rawCompoundFields", "ftboss$rawListFields", "ftboss$rawBytes", "ftboss$rawInts", "ftboss$rawLongs", "ftboss$nativeComponents", "ftboss$nativePrototype", "ftboss$nativePatch", "ftboss$rawNativeProviders", "ftboss$rawPersistentData"},
        at = {@At("RETURN")},
        require = 1
    )
    private void ftboss$ownerRawExposed(CallbackInfoReturnable<?> var1) {
        try {
            OwnerFieldExpectedAuthority.rawExposed(this, var1.getReturnValue());
        } catch (Throwable var5) {
            try {
                OwnerFieldExpectedAuthority.changed(this);
            } catch (Throwable var4) {
            }
        }
    }
}
