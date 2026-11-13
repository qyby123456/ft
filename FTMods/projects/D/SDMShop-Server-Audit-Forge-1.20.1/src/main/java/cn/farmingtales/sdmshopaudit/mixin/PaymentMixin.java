package cn.farmingtales.sdmshopaudit.mixin;

import cn.farmingtales.sdmshopaudit.AuditService;
import cn.farmingtales.sdmshopaudit.SecurityService;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
        "net.sixik.sdmshoprework.common.shop.sellerType.MoneySellerType",
        "net.sixik.sdmshoprework.common.shop.sellerType.ItemSellerType"
}, remap = false)
public abstract class PaymentMixin {
    @Inject(method = "buy", at = @At("HEAD"), cancellable = true, remap = false)
    private void sdmAudit$validatePayment(Player player, @Coerce Object entry, long amount, CallbackInfoReturnable<Boolean> callback) {
        if (!SecurityService.payment(this, player, entry, amount)) {
            AuditService.payment(this, player, entry, amount, false);
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "buy", at = @At("RETURN"), remap = false)
    private void sdmAudit$payment(Player player, @Coerce Object entry, long amount, CallbackInfoReturnable<Boolean> callback) {
        SecurityService.paymentResult(player, entry, callback.getReturnValueZ());
        AuditService.payment(this, player, entry, amount, callback.getReturnValueZ());
    }
}
