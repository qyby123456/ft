/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.NativeSpecialCaller;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets={"com.github.L_Ender.cataclysm.items.Soul_Render", "com.github.L_Ender.cataclysm.event.ServerEventHandler"}, remap=false)
public abstract class NativeSoulRushMixin {
    @Redirect(method={"m_5551_"}, at=@At(value="INVOKE", target="Lcom/github/L_Ender/cataclysm/capabilities/RenderRushCapability$IRenderRushCapability;setRush(Z)V"), require=0, remap=false)
    private void ftboss$rememberRush(@Coerce Object object, boolean bl) {
        NativeSpecialCaller.rush(object, bl);
    }

    @Redirect(method={"onLivingUpdateEvent"}, at=@At(value="INVOKE", target="Lcom/github/L_Ender/cataclysm/capabilities/RenderRushCapability$IRenderRushCapability;tick(Lnet/minecraft/world/entity/LivingEntity;)V"), require=0, remap=false)
    private void ftboss$exactRushTick(@Coerce Object object, LivingEntity livingEntity) {
        NativeSpecialCaller.rushTick(object, livingEntity);
    }
}

