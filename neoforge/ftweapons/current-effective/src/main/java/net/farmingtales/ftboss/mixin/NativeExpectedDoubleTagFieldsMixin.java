package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.DoubleValue;
import net.minecraft.nbt.DoubleTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {DoubleTag.class},
    remap = false
)
public interface NativeExpectedDoubleTagFieldsMixin extends DoubleValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    double ftboss$rawDouble();
}
