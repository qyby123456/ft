package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.ByteValue;
import net.minecraft.nbt.ByteTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {ByteTag.class},
    remap = false
)
public interface NativeExpectedByteTagFieldsMixin extends ByteValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    byte ftboss$rawByte();
}
