package net.farmingtales.teamlag.mixin;

import net.farmingtales.teamlag.MachineTiming;
import net.farmingtales.teamlag.ReflectionAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity", remap = false)
public abstract class BlockTickMixin {
    @Unique private BlockEntity ftteamlag$entity;
    @Unique private BlockEntity ftteamlag$entity() {
        if (ftteamlag$entity == null) ftteamlag$entity = ReflectionAccess.blockEntity(this);
        return ftteamlag$entity;
    }
    @Inject(method = {"tick()V", "m_142224_()V"}, at = @At("HEAD"), remap = false)
    private void ftteamlagBefore(CallbackInfo callback) { MachineTiming.begin(ftteamlag$entity()); }
    @Inject(method = {"tick()V", "m_142224_()V"}, at = @At("RETURN"), remap = false)
    private void ftteamlagAfter(CallbackInfo callback) { MachineTiming.end(ftteamlag$entity()); }
}
