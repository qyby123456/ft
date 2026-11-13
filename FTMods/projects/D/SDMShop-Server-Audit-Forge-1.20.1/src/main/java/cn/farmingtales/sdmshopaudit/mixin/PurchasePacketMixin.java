package cn.farmingtales.sdmshopaudit.mixin;

import cn.farmingtales.sdmshopaudit.AuditService;
import cn.farmingtales.sdmshopaudit.SecurityService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.sixik.sdmshoprework.network.client.SendBuyShopEntryC2S", remap = false)
public abstract class PurchasePacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private void sdmAudit$begin(@Coerce Object context, CallbackInfo callback) {
        AuditService.beginPurchase(this, context);
        if (!SecurityService.purchase(this, context)) {
            AuditService.finish(this);
            callback.cancel();
        }
    }

    @Inject(method = "handle", at = {
            @At(value = "INVOKE", target = "Lnet/sixik/sdmshoprework/api/shop/AbstractShopEntryType;buy(Lnet/minecraft/world/entity/player/Player;ILnet/sixik/sdmshoprework/api/shop/AbstractShopEntry;)V"),
            @At(value = "INVOKE", target = "Lnet/sixik/sdmshoprework/api/shop/AbstractShopEntryType;sell(Lnet/minecraft/world/entity/player/Player;ILnet/sixik/sdmshoprework/api/shop/AbstractShopEntry;)V")
    }, cancellable = true, remap = false)
    private void sdmAudit$dispatch(@Coerce Object context, CallbackInfo callback) {
        if (!SecurityService.beforeDispatch(this)) {
            SecurityService.finish(this);
            AuditService.finish(this);
            callback.cancel();
            return;
        }
        AuditService.dispatch(false);
    }

    @Inject(method = "handle", at = {
            @At(value = "INVOKE", target = "Lnet/sixik/sdmshoprework/api/shop/AbstractShopEntryType;buy(Lnet/minecraft/world/entity/player/Player;ILnet/sixik/sdmshoprework/api/shop/AbstractShopEntry;)V", shift = At.Shift.AFTER),
            @At(value = "INVOKE", target = "Lnet/sixik/sdmshoprework/api/shop/AbstractShopEntryType;sell(Lnet/minecraft/world/entity/player/Player;ILnet/sixik/sdmshoprework/api/shop/AbstractShopEntry;)V", shift = At.Shift.AFTER)
    }, remap = false)
    private void sdmAudit$complete(@Coerce Object context, CallbackInfo callback) {
        SecurityService.completed();
        AuditService.dispatch(true);
    }

    @Inject(method = "handle", at = @At("RETURN"), remap = false)
    private void sdmAudit$end(@Coerce Object context, CallbackInfo callback) {
        SecurityService.finish(this);
        AuditService.finish(this);
    }
}
