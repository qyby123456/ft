package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeItemComponentFields;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CustomData.class, remap = false)
public abstract class NativeCustomDataFieldMixin implements NativeItemComponentFields.CustomData {
    @Shadow(remap = false)
    @Final
    private CompoundTag tag;

    @Override
    public final CompoundTag ftboss$nativeCustomData() {
        NativeItemComponentFields.requireObserver(CustomData.class, "ftboss$nativeCustomData");
        return this.tag;
    }
}
