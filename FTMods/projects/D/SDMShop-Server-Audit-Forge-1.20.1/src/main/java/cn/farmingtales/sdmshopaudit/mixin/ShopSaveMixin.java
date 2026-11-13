package cn.farmingtales.sdmshopaudit.mixin;

import cn.farmingtales.sdmshopaudit.AuditService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.sixik.sdmshoprework.common.shop.ShopBase", remap = false)
public abstract class ShopSaveMixin {
    @Inject(method = "saveShopToFile", at = @At("RETURN"), remap = false)
    private void sdmAudit$save(CallbackInfo callback) {
        AuditService.saved(this);
    }
}
