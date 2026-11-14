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
    targets = {"com.github.L_Ender.cataclysm.items.Ancient_Spear"},
    remap = false
)
public abstract class NativeAncientMeleeIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<Frame>> ftboss$v9$meleeFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
        method = {"hurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeBegin(ItemStack var1, LivingEntity var2, LivingEntity var3, CallbackInfoReturnable<Boolean> var4) {
        NativeIngressToken var5 = FoodWeaponSourceLedger.nativeIngressBegin(
            var1,
            var3,
            NativeIngressExecution.exactHeldHand(var3, var1),
            "hurtEnemy",
            "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"
        );
        ftboss$v9$meleeFrames.get().push(NativeIngressExecution.open(var5));
    }

    @Inject(
        method = {"hurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeReturn(ItemStack var1, LivingEntity var2, LivingEntity var3, CallbackInfoReturnable<Boolean> var4) {
        Frame var5 = ftboss$v9$meleeFrames.get().peek();
        if (var5 != null) {
            NativeIngressExecution.finish(var5, FoodWeaponSourceLedger.nativeIngressReturned(var5.token()));
            ftboss$v9$meleeFrames.get().remove(var5);
            if (ftboss$v9$meleeFrames.get().isEmpty()) {
                ftboss$v9$meleeFrames.remove();
            }
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$melee() {
        Frame var0 = ftboss$v9$meleeFrames.get().poll();

        try {
            NativeIngressExecution.abort(var0);
        } finally {
            if (ftboss$v9$meleeFrames.get().isEmpty()) {
                ftboss$v9$meleeFrames.remove();
            }
        }
    }
}
