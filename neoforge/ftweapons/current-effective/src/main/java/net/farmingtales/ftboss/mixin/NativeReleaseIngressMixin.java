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
    targets = {"com.github.L_Ender.cataclysm.items.Soul_Render", "com.github.L_Ender.cataclysm.items.Coral_Spear", "com.github.L_Ender.cataclysm.items.Coral_Bardiche"},
    remap = false
)
public abstract class NativeReleaseIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<Frame>> ftboss$v9$releaseFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
        method = {"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeBegin(ItemStack var1, Level var2, LivingEntity var3, int var4, CallbackInfo var5) {
        NativeIngressToken var6 = FoodWeaponSourceLedger.nativeIngressBegin(
            var1,
            var3,
            var3.getUsedItemHand(),
            "releaseUsing",
            "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"
        );
        ftboss$v9$releaseFrames.get().push(NativeIngressExecution.open(var6));
    }

    @Inject(
        method = {"releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeReturn(ItemStack var1, Level var2, LivingEntity var3, int var4, CallbackInfo var5) {
        Frame var6 = ftboss$v9$releaseFrames.get().peek();
        if (var6 != null) {
            NativeIngressExecution.finish(var6, FoodWeaponSourceLedger.nativeIngressReturned(var6.token()));
            ftboss$v9$releaseFrames.get().remove(var6);
            if (ftboss$v9$releaseFrames.get().isEmpty()) {
                ftboss$v9$releaseFrames.remove();
            }
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$release() {
        Frame var0 = ftboss$v9$releaseFrames.get().poll();

        try {
            NativeIngressExecution.abort(var0);
        } finally {
            if (ftboss$v9$releaseFrames.get().isEmpty()) {
                ftboss$v9$releaseFrames.remove();
            }
        }
    }
}
