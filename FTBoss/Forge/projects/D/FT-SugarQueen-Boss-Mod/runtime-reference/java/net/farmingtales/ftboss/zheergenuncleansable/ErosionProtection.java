/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Remove
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.zheergenuncleansable;

import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ErosionProtection {
    private ErosionProtection() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
    public static void onRemove(MobEffectEvent.Remove remove) {
        if (!remove.getEntity().m_9236_().f_46443_ && remove.getEffect() instanceof ShadowErosionEffect) {
            remove.setCanceled(true);
        }
    }
}

