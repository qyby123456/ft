/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressExecution;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemStack.class}, remap=false)
public abstract class NativeWearTokenMixin {
    @Unique
    private static final ThreadLocal<Deque<NativeIngressExecution.WearFrame>> ftboss$v9$wearFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method={"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", "m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"}, at={@At(value="HEAD")}, require=1, remap=false)
    private void ftboss$v9$nativeWearBegin(int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer, CallbackInfo callbackInfo) {
        ftboss$v9$wearFrames.get().push(new NativeIngressExecution.WearFrame(FoodWeaponSourceLedger.nativeIngressWearBegin((ItemStack)this, n, livingEntity)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", "m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$nativeWearReturn(int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer, CallbackInfo callbackInfo) {
        NativeIngressExecution.WearFrame wearFrame = ftboss$v9$wearFrames.get().poll();
        try {
            if (wearFrame != null) {
                FoodWeaponSourceLedger.nativeIngressWearReturned(wearFrame.token(), (ItemStack)this, n, livingEntity);
            }
        }
        finally {
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

