package net.farmingtales.ftboss.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressExecution;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIngressToken;
import net.farmingtales.ftboss.item.NativeIngressExecution.Frame;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    targets = {"net.gqstavo.armamentarium.item.EnderMaidenShortbowItem", "com.github.L_Ender.cataclysm.items.Meat_Shredder"},
    remap = false
)
public abstract class NativeUseTickIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<Frame>> ftboss$v9$useTickFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
        method = {"onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeBegin(Level var1, LivingEntity var2, ItemStack var3, int var4, CallbackInfo var5) {
        NativeIngressToken var6 = FoodWeaponSourceLedger.nativeIngressBegin(
            var3,
            var2,
            var2.getUsedItemHand(),
            "onUseTick",
            "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"
        );
        ftboss$v9$useTickFrames.get().push(NativeIngressExecution.open(var6));
    }

    @Inject(
        method = {"onUseTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeReturn(Level var1, LivingEntity var2, ItemStack var3, int var4, CallbackInfo var5) {
        Frame var6 = ftboss$v9$useTickFrames.get().peek();
        if (var6 != null) {
            NativeIngressExecution.finish(var6, FoodWeaponSourceLedger.nativeIngressReturned(var6.token()));
            ftboss$v9$useTickFrames.get().remove(var6);
            if (ftboss$v9$useTickFrames.get().isEmpty()) {
                ftboss$v9$useTickFrames.remove();
            }
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$useTick() {
        Frame var0 = ftboss$v9$useTickFrames.get().poll();

        try {
            NativeIngressExecution.abort(var0);
        } finally {
            if (ftboss$v9$useTickFrames.get().isEmpty()) {
                ftboss$v9$useTickFrames.remove();
            }
        }
    }
}
