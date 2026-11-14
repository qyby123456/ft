package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.ShortValue;
import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {ShortTag.class},
    remap = false
)
public interface NativeExpectedShortTagFieldsMixin extends ShortValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    short ftboss$rawShort();
}
