package net.farmingtales.ftboss.mixin;

import java.util.Map;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Compound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {CompoundTag.class},
    remap = false
)
public abstract class NativeExpectedCompoundTagFieldsMixin implements Compound {
    @Shadow(
        remap = false,
        aliases = {"tags"}
    )
    @Final
    private Map<String, Tag> tags;

    @Override
    public Map<String, Tag> ftboss$rawCompoundFields() {
        return this.tags;
    }
}
