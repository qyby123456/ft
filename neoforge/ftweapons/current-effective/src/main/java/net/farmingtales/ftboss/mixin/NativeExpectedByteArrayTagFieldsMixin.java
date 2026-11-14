package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Bytes;
import net.minecraft.nbt.ByteArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {ByteArrayTag.class},
    remap = false
)
public abstract class NativeExpectedByteArrayTagFieldsMixin implements Bytes {
    @Shadow(
        remap = false,
        aliases = {"data"}
    )
    private byte[] data;

    @Override
    public byte[] ftboss$rawBytes() {
        return this.data;
    }
}
