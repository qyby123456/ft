package cn.farmingtales.sdmshopaudit.mixin;

import cn.farmingtales.sdmshopaudit.SecurityService;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.sixik.sdmshoprework.common.shop.type.ShopCommandEntryType", remap = false)
public abstract class CommandEntryMixin {
    @Inject(method = "buy", at = @At("HEAD"), cancellable = true, remap = false)
    private void sdmAudit$command(Player player, int count, @Coerce Object entry, CallbackInfo callback) {
        SecurityService.executeCommand(this, player, count, entry);
        callback.cancel();
    }
}
