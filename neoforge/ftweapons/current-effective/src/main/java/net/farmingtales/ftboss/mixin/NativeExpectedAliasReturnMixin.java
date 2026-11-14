package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {CompoundTag.class},
    remap = false
)
public abstract class NativeExpectedAliasReturnMixin {
    @Inject(
        method = {"get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$originalAliasReturned(String var1, CallbackInfoReturnable<Tag> var2) {
        NativeExpectedFieldObservation.aliasReturned(this, var1, var2.getReturnValue());
    }
}
