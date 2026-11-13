package net.farmingtales.teamlag.mixin;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import net.farmingtales.teamlag.RefinedStorageTiming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.refinedmods.refinedstorage.apiimpl.network.NetworkListener", remap = false)
public abstract class RefinedStorageMixin {
    @Redirect(method = "onLevelTick(Lnet/minecraftforge/event/TickEvent$LevelTickEvent;)V",
            at = @At(value = "INVOKE", target = "Lcom/refinedmods/refinedstorage/api/network/INetwork;update()V"), remap = false)
    private void ftteamlagNetworkUpdate(INetwork network) {
        var sample = RefinedStorageTiming.begin(network.getLevel(), network.getPosition(), network.getClass());
        try {
            network.update();
        } finally {
            RefinedStorageTiming.end(sample);
        }
    }

    @Redirect(method = "onLevelTick(Lnet/minecraftforge/event/TickEvent$LevelTickEvent;)V",
            at = @At(value = "INVOKE", target = "Lcom/refinedmods/refinedstorage/api/network/node/INetworkNode;update()V"), remap = false)
    private void ftteamlagNodeUpdate(INetworkNode node) {
        var sample = RefinedStorageTiming.begin(node.getLevel(), node.getPos(), node.getClass());
        try {
            node.update();
        } finally {
            RefinedStorageTiming.end(sample);
        }
    }
}
