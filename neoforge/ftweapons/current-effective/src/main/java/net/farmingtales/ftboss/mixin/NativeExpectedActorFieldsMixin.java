package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Actor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
    value = {Entity.class},
    remap = false
)
public abstract class NativeExpectedActorFieldsMixin implements Actor {
    @Shadow(
        remap = false
    )
    private CompoundTag persistentData;

    @Override
    public CompoundTag ftboss$rawPersistentData() {
        return this.persistentData;
    }
}
