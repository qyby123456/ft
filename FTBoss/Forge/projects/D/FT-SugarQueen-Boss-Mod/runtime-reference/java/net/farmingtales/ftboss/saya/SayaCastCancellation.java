/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.saya;

import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.saya.SayaCastLifecycle;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class SayaCastCancellation {
    private SayaCastCancellation() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tick(LivingEvent.LivingTickEvent livingTickEvent) {
        TeaTreeBossEntity teaTreeBossEntity;
        Object object;
        block5: {
            block4: {
                object = livingTickEvent.getEntity();
                if (!(object instanceof TeaTreeBossEntity)) break block4;
                teaTreeBossEntity = (TeaTreeBossEntity)object;
                if (!teaTreeBossEntity.m_9236_().f_46443_) break block5;
            }
            return;
        }
        object = teaTreeBossEntity.signatureCastAction();
        if (SayaCastLifecycle.cancelWindup(((BossActionState)object).name(), ((BossActionState)object).elapsed(0.0f), teaTreeBossEntity.getPersistentData().m_128425_("ftboss:signature_cast", 10))) {
            ((BossActionState)object).start("", 0);
        }
    }
}

