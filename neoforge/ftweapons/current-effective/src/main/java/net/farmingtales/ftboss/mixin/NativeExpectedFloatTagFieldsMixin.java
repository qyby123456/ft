package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.FloatValue;
import net.minecraft.nbt.FloatTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {FloatTag.class},
    remap = false
)
public interface NativeExpectedFloatTagFieldsMixin extends FloatValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    float ftboss$rawFloat();
}
