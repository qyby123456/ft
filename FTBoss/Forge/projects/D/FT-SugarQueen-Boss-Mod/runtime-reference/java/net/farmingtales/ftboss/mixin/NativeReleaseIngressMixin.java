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
@Mixin(targets={"com.github.L_Ender.cataclysm.items.Soul_Render", "com.github.L_Ender.cataclysm.items.Coral_Spear", "com.github.L_Ender.cataclysm.items.Coral_Bardiche"}, remap=false)
public abstract class NativeReleaseIngressMixin {
    @Unique
    private static final ThreadLocal<Deque<NativeIngressExecution.Frame>> ftboss$v9$releaseFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method={"m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at={@At(value="HEAD")}, require=1, remap=false)
    private void ftboss$v9$nativeBegin(ItemStack itemStack, Level level, LivingEntity livingEntity, int n, CallbackInfo callbackInfo) {
        FoodWeaponSourceLedger.NativeIngressToken nativeIngressToken = FoodWeaponSourceLedger.nativeIngressBegin(itemStack, livingEntity, livingEntity.m_7655_(), "m_5551_", "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V");
        ftboss$v9$releaseFrames.get().push(NativeIngressExecution.open(nativeIngressToken));
    }

    @Inject(method={"m_5551_(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$nativeReturn(ItemStack itemStack, Level level, LivingEntity livingEntity, int n, CallbackInfo callbackInfo) {
        NativeIngressExecution.Frame frame = ftboss$v9$releaseFrames.get().peek();
        if (frame == null) {
            return;
        }
        NativeIngressExecution.finish(frame, FoodWeaponSourceLedger.nativeIngressReturned(frame.token()));
        ftboss$v9$releaseFrames.get().remove(frame);
        if (ftboss$v9$releaseFrames.get().isEmpty()) {
            ftboss$v9$releaseFrames.remove();
        }
    }

    @Unique
    private static void ftboss$v9$nativeUnwind$release() {
        NativeIngressExecution.Frame frame = ftboss$v9$releaseFrames.get().poll();
        try {
            NativeIngressExecution.abort(frame);
        }
        finally {
            if (ftboss$v9$releaseFrames.get().isEmpty()) {
                ftboss$v9$releaseFrames.remove();
            }
        }
    }
}

