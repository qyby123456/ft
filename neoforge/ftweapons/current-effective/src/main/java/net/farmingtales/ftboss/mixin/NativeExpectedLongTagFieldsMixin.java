package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.LongValue;
import net.minecraft.nbt.LongTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {LongTag.class},
    remap = false
)
public interface NativeExpectedLongTagFieldsMixin extends LongValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    long ftboss$rawLong();
}
