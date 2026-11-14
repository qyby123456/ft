/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeTridentFieldCarrier;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ThrownTrident.class}, remap=false)
public interface NativeTridentFieldCarrierMixin
extends NativeTridentFieldCarrier {
    @Override
    @Accessor(value="f_37555_", remap=false)
    public ItemStack ftboss$originalTridentStack();
}

