/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import java.util.function.Consumer;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"net.gqstavo.armamentarium.item.EnderMaidenShortbowItem", "com.github.L_Ender.cataclysm.items.Ancient_Spear", "com.github.L_Ender.cataclysm.items.Coral_Spear", "com.github.L_Ender.cataclysm.items.Coral_Bardiche"}, remap=false)
public abstract class NativeSpecialWearMixin {
    @Redirect(method={"m_5929_", "launchTornado", "m_5551_", "m_7579_"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;m_41622_(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"), require=0, remap=false)
    private void ftboss$observeActualWear(ItemStack itemStack, int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer) {
        NativeActionObservation.nativeWear(itemStack, n, livingEntity, consumer);
    }
}

