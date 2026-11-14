package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Longs;
import net.minecraft.nbt.LongArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {LongArrayTag.class},
    remap = false
)
public abstract class NativeExpectedLongArrayTagFieldsMixin implements Longs {
    @Shadow(
        remap = false,
        aliases = {"data"}
    )
    private long[] data;

    @Override
    public long[] ftboss$rawLongs() {
        return this.data;
    }
}
