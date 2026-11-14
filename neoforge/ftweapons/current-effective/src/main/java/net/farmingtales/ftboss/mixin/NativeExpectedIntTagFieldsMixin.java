package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.IntValue;
import net.minecraft.nbt.IntTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {IntTag.class},
    remap = false
)
public interface NativeExpectedIntTagFieldsMixin extends IntValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    int ftboss$rawInt();
}
