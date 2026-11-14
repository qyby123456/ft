package net.farmingtales.ftboss.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressExecution;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIngressToken;
import net.farmingtales.ftboss.item.NativeIngressExecution.Frame;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
    targets = {"com.github.L_Ender.cataclysm.items.Tidal_Claws", "com.github.L_Ender.cataclysm.items.Ancient_Spear"},
    remap = false
)
public abstract class NativeLeftClickIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<Frame>> ftboss$v9$leftClickFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
        method = {"onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeBegin(ItemStack var1, LivingEntity var2, CallbackInfoReturnable<Boolean> var3) {
        NativeIngressToken var4 = FoodWeaponSourceLedger.nativeIngressBegin(
            var1,
            var2,
            NativeIngressExecution.exactHeldHand(var2, var1),
            "onLeftClick",
            "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"
        );
        ftboss$v9$leftClickFrames.get().push(NativeIngressExecution.open(var4));
    }

    @Inject(
        method = {"onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeReturn(ItemStack var1, LivingEntity var2, CallbackInfoReturnable<Boolean> var3) {
        Frame var4 = ftboss$v9$leftClickFrames.get().peek();
        if (var4 != null) {
            NativeIngressExecution.finish(var4, FoodWeaponSourceLedger.nativeIngressReturned(var4.token()));
            ftboss$v9$leftClickFrames.get().remove(var4);
            if (ftboss$v9$leftClickFrames.get().isEmpty()) {
                ftboss$v9$leftClickFrames.remove();
            }
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$leftClick() {
        Frame var0 = ftboss$v9$leftClickFrames.get().poll();

        try {
            NativeIngressExecution.abort(var0);
        } finally {
            if (ftboss$v9$leftClickFrames.get().isEmpty()) {
                ftboss$v9$leftClickFrames.remove();
            }
        }
    }
}
