/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.boss.ai.ManorAttackFrames;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.bmt.kaleidoscope_world_liquor.event.DamageEvents"}, remap=false)
public abstract class TequilaDamageEventsMixin {
    @Inject(method={"onLivingHurt"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private static void ftboss$bypassManorDamageCap(LivingHurtEvent event, CallbackInfo callback) {
        if (!event.getEntity().m_9236_().f_46443_ && !event.isCanceled() && ManorAttackFrames.isManorAttack(event.getSource())) {
            callback.cancel();
        }
    }
}

