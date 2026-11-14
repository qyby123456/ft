package net.farmingtales.ftboss.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.entity.EquipmentSlot;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressExecution.WearFrame;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {ItemStack.class},
    remap = false
)
public abstract class NativeWearTokenMixin {
    @Unique
    private static final ThreadLocal<Deque<WearFrame>> ftboss$v9$wearFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
        method = {"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeWearBegin(int var1, LivingEntity var2, EquipmentSlot var3, CallbackInfo var4) {
        ftboss$v9$wearFrames.get().push(new WearFrame(FoodWeaponSourceLedger.nativeIngressWearBegin((ItemStack)(Object)this, var1, var2)));
    }

    @Inject(
        method = {"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$nativeWearReturn(int var1, LivingEntity var2, EquipmentSlot var3, CallbackInfo var4) {
        WearFrame var5 = ftboss$v9$wearFrames.get().poll();

        try {
            if (var5 != null) {
                FoodWeaponSourceLedger.nativeIngressWearReturned(var5.token(), (ItemStack)(Object)this, var1, var2);
            }
        } finally {
            if (ftboss$v9$wearFrames.get().isEmpty()) {
                ftboss$v9$wearFrames.remove();
            }
        }
    }

    @Unique
    private static void ftboss$v9$wearUnwind() {
        ftboss$v9$wearFrames.get().poll();
        if (ftboss$v9$wearFrames.get().isEmpty()) {
            ftboss$v9$wearFrames.remove();
        }
    }
}
