package cn.farmingtales.sdmshopaudit.mixin;

import cn.farmingtales.sdmshopaudit.AuditService;
import cn.farmingtales.sdmshopaudit.SecurityService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {
        "net.sixik.sdmshoprework.network.server.create.SendCreateShopTabC2S",
        "net.sixik.sdmshoprework.network.server.create.SendCreateShopEntryC2S",
        "net.sixik.sdmshoprework.network.server.SendChangesShopC2S",
        "net.sixik.sdmshoprework.network.server.SendChangesShopEntriesC2S",
        "net.sixik.sdmshoprework.network.server.SendChangeShopTabC2S",
        "net.sixik.sdmshoprework.network.server.edit.SendEditShopTabC2S",
        "net.sixik.sdmshoprework.network.server.edit.SendEditShopEntryC2S",
        "net.sixik.sdmshoprework.network.server.move.SendMoveShopTabC2S",
        "net.sixik.sdmshoprework.network.server.move.SendMoveShopEntryC2S"
}, remap = false)
public abstract class EditPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private void sdmAudit$begin(@Coerce Object context, CallbackInfo callback) {
        if (!SecurityService.edit(this, context)) {
            callback.cancel();
            return;
        }
        AuditService.beginEdit(this, context);
    }

    @Inject(method = "handle", at = @At("RETURN"), remap = false)
    private void sdmAudit$end(@Coerce Object context, CallbackInfo callback) {
        AuditService.finish(this);
    }
}
