package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.TextValue;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
    value = {StringTag.class},
    remap = false
)
public interface NativeExpectedStringTagFieldsMixin extends TextValue {
    @Accessor(
        value = "data",
        remap = false
    )
    @Override
    String ftboss$rawText();
}
