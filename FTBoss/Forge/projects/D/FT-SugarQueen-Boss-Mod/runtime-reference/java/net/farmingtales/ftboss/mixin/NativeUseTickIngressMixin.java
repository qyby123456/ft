/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeIngressExecution;
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
@Mixin(targets={"net.gqstavo.armamentarium.item.EnderMaidenShortbowItem", "com.github.L_Ender.cataclysm.items.Meat_Shredder"}, remap=false)
public abstract class NativeUseTickIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<NativeIngressExecution.Frame>> ftboss$v9$useTickFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method={"m_5929_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"}, at={@At(value="HEAD")}, require=1, remap=false)
    private void ftboss$v9$nativeBegin(Level level, LivingEntity livingEntity, ItemStack itemStack, int n, CallbackInfo callbackInfo) {
        FoodWeaponSourceLedger.NativeIngressToken nativeIngressToken = FoodWeaponSourceLedger.nativeIngressBegin(itemStack, livingEntity, livingEntity.m_7655_(), "m_5929_", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V");
        ftboss$v9$useTickFrames.get().push(NativeIngressExecution.open(nativeIngressToken));
    }

    @Inject(method={"m_5929_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$nativeReturn(Level level, LivingEntity livingEntity, ItemStack itemStack, int n, CallbackInfo callbackInfo) {
        NativeIngressExecution.Frame frame = ftboss$v9$useTickFrames.get().peek();
        if (frame == null) {
            return;
        }
        NativeIngressExecution.finish(frame, FoodWeaponSourceLedger.nativeIngressReturned(frame.token()));
        ftboss$v9$useTickFrames.get().remove(frame);
        if (ftboss$v9$useTickFrames.get().isEmpty()) {
            ftboss$v9$useTickFrames.remove();
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$useTick() {
        NativeIngressExecution.Frame frame = ftboss$v9$useTickFrames.get().poll();
        try {
            NativeIngressExecution.abort(frame);
        }
        finally {
            if (ftboss$v9$useTickFrames.get().isEmpty()) {
                ftboss$v9$useTickFrames.remove();
            }
        }
    }
}

