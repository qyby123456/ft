package net.farmingtales.teamlag.mixin;

import dev.latvian.mods.kubejs.block.entity.BlockEntityCallback;
import dev.latvian.mods.kubejs.block.entity.BlockEntityJS;
import net.farmingtales.teamlag.MachineTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.farmingtales.machine.NativeMachineScheduler", remap = false)
public abstract class NativeSchedulerMixin {
    @Redirect(method = "beforeCollection", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/block/entity/BlockEntityCallback;accept(Ldev/latvian/mods/kubejs/block/entity/BlockEntityJS;)V"), remap = false)
    private static void ftteamlagBeforeCollection(BlockEntityCallback callback, BlockEntityJS entity) {
        MachineTiming.begin(entity);
        try { callback.accept(entity); } finally { MachineTiming.end(entity); }
    }
    @Redirect(method = {"tickLevel", "drain"}, at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/block/entity/BlockEntityCallback;accept(Ldev/latvian/mods/kubejs/block/entity/BlockEntityJS;)V"), remap = false)
    private static void ftteamlagTick(BlockEntityCallback callback, BlockEntityJS entity) {
        MachineTiming.begin(entity);
        try { callback.accept(entity); } finally { MachineTiming.end(entity); }
    }
}
