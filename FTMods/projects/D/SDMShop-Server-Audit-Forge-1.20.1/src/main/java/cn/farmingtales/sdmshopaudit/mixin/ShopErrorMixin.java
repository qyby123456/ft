package cn.farmingtales.sdmshopaudit.mixin;

import cn.farmingtales.sdmshopaudit.AuditService;
import cn.farmingtales.sdmshopaudit.SecurityService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.sixik.sdmshoprework.SDMShopRework", remap = false)
public abstract class ShopErrorMixin {
    @Inject(method = "printStackTrace", at = @At("HEAD"), remap = false)
    private static void sdmAudit$error(String message, Throwable error, CallbackInfo callback) {
        SecurityService.failure();
        AuditService.failure(error);
    }
}
