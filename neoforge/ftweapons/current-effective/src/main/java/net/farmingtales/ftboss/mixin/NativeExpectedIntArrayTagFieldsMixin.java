package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Ints;
import net.minecraft.nbt.IntArrayTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {IntArrayTag.class},
    remap = false
)
public abstract class NativeExpectedIntArrayTagFieldsMixin implements Ints {
    @Shadow(
        remap = false,
        aliases = {"data"}
    )
    private int[] data;

    @Override
    public int[] ftboss$rawInts() {
        return this.data;
    }
}
