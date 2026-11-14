/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressExecution;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"com.github.L_Ender.cataclysm.items.Tidal_Claws", "com.github.L_Ender.cataclysm.items.Ancient_Spear"}, remap=false)
public abstract class NativeLeftClickIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<NativeIngressExecution.Frame>> ftboss$v9$leftClickFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method={"onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"}, at={@At(value="HEAD")}, require=1, remap=false)
    private void ftboss$v9$nativeBegin(ItemStack itemStack, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        FoodWeaponSourceLedger.NativeIngressToken nativeIngressToken = FoodWeaponSourceLedger.nativeIngressBegin(itemStack, livingEntity, NativeIngressExecution.exactHeldHand(livingEntity, itemStack), "onLeftClick", "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z");
        ftboss$v9$leftClickFrames.get().push(NativeIngressExecution.open(nativeIngressToken));
    }

    @Inject(method={"onLeftClick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$nativeReturn(ItemStack itemStack, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NativeIngressExecution.Frame frame = ftboss$v9$leftClickFrames.get().peek();
        if (frame == null) {
            return;
        }
        NativeIngressExecution.finish(frame, FoodWeaponSourceLedger.nativeIngressReturned(frame.token()));
        ftboss$v9$leftClickFrames.get().remove(frame);
        if (ftboss$v9$leftClickFrames.get().isEmpty()) {
            ftboss$v9$leftClickFrames.remove();
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$leftClick() {
        NativeIngressExecution.Frame frame = ftboss$v9$leftClickFrames.get().poll();
        try {
            NativeIngressExecution.abort(frame);
        }
        finally {
            if (ftboss$v9$leftClickFrames.get().isEmpty()) {
                ftboss$v9$leftClickFrames.remove();
            }
        }
    }
}

