package net.farmingtales.ftboss.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Inventory.class, PlayerDataStorage.class})
public abstract class OwnerStatueWearInventoryMixin {
    @Inject(
        method = {"load(Lnet/minecraft/nbt/ListTag;)V", "save(Lnet/minecraft/world/entity/player/Player;)V"},
        at = {@At("HEAD")},
        require = 1
    )
    private void ftboss$statueWear$loadAnchor(CallbackInfo callback) {
    }
}
