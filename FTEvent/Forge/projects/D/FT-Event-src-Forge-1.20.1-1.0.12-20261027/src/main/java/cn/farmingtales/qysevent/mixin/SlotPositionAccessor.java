package cn.farmingtales.qysevent.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Slot.class, remap = false)
public interface SlotPositionAccessor {
    @Mutable
    @Accessor(value = "f_40220_", remap = false)
    void qysEvent$setX(int position);

    @Mutable
    @Accessor(value = "f_40221_", remap = false)
    void qysEvent$setY(int position);
}
