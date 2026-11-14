package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class NativeExpectedTagAssignmentMixin {
    @Inject(
        method = {"m_41751_(Lnet/minecraft/nbt/CompoundTag;)V"},
        at = {@At(
            value = "FIELD",
            target = "Lnet/minecraft/world/item/ItemStack;f_41590_:Lnet/minecraft/nbt/CompoundTag;",
            opcode = 181,
            shift = Shift.AFTER,
            remap = false
        )},
        require = 1,
        allow = 1,
        remap = false
    )
    private void ftboss$originalTagAssigned(CompoundTag var1, CallbackInfo var2) {
        NativeExpectedFieldObservation.originalTagAssigned((ItemStack)(Object)this, var1);
    }
}
