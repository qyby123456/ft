/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Remove
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.effect;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.boss.ai.ManorStatusImmunity;
import net.farmingtales.ftboss.effect.ShadowErosionEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public class HealBanEffect
extends MobEffect {
    public HealBanEffect() {
        super(MobEffectCategory.HARMFUL, 11541032);
    }

    public boolean m_19486_() {
        return false;
    }

    public List<ItemStack> getCurativeItems() {
        return new ArrayList<ItemStack>();
    }

    @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
    public static void onRemove(MobEffectEvent.Remove remove) {
        if (!remove.getEntity().m_9236_().f_46443_ && (remove.getEffect() instanceof HealBanEffect || remove.getEffect() instanceof ShadowErosionEffect)) {
            remove.setCanceled(ManorStatusImmunity.protectRemoval(true, remove));
        }
    }
}

