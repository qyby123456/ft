package net.farmingtales.ftboss.effect;

import java.util.Set;
import net.farmingtales.ftboss.boss.ai.ManorStatusImmunity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = "ftboss")
public class HealBanEffect extends MobEffect {
    public HealBanEffect() {
        super(MobEffectCategory.HARMFUL, 11541032);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effect) {}

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRemove(MobEffectEvent.Remove event) {
        if (!event.getEntity().level().isClientSide
                && (event.getEffect().value() instanceof HealBanEffect
                || event.getEffect().value() instanceof ShadowErosionEffect)) {
            event.setCanceled(ManorStatusImmunity.protectRemoval(true, event));
        }
    }
}
