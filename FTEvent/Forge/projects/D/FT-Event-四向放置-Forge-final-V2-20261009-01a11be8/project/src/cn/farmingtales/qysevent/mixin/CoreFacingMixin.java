package cn.farmingtales.qysevent.mixin;

import cn.farmingtales.qysevent.CoreFacingPolicy;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockBehaviour.class, remap = false)
public abstract class CoreFacingMixin {
    @Inject(method = "m_6843_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/Rotation;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void qysEvent$rotateCore(BlockState state, Rotation rotation, CallbackInfoReturnable<BlockState> callback) {
        BlockState updated = CoreFacingPolicy.rotateCore(state, rotation);
        if (updated != null) callback.setReturnValue(updated);
    }

    @Inject(method = "m_6943_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/Mirror;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void qysEvent$mirrorCore(BlockState state, Mirror mirror, CallbackInfoReturnable<BlockState> callback) {
        BlockState updated = CoreFacingPolicy.mirrorCore(state, mirror);
        if (updated != null) callback.setReturnValue(updated);
    }
}
