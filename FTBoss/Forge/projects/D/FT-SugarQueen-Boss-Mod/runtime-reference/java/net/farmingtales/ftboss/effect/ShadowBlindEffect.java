/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Remove
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.effect;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.zheergenphantom.ErosionFilter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public class ShadowBlindEffect
extends MobEffect {
    public static final int TICKS = 60;
    public static final int AMPLIFIER = 1;

    public ShadowBlindEffect() {
        super(MobEffectCategory.BENEFICIAL, 1313310);
    }

    public boolean m_19486_() {
        return true;
    }

    public List<ItemStack> getCurativeItems() {
        return new ArrayList<ItemStack>();
    }

    @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
    public static void onRemove(MobEffectEvent.Remove remove) {
    }

    public static void applyTo(LivingEntity livingEntity, int n) {
        ErosionFilter.retire(livingEntity, n);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.player.m_9236_().f_46443_ && event.player.m_6084_()) {
            ShadowBlindEffect.synchronize(event.player);
        }
    }

    private static void synchronize(Player player) {
        ErosionFilter.cleanup(player);
    }

    private static void companion(Player player, MobEffect effect, int duration, int amplifier) {
        MobEffectInstance current = player.m_21124_(effect);
        if (current != null && current.m_19564_() >= amplifier && (current.m_267577_() || duration != -1 && current.m_19557_() >= duration)) {
            return;
        }
        MobEffectInstance companion = new MobEffectInstance(effect, duration, amplifier, false, false, false);
        player.m_7292_(companion);
    }

    public boolean m_6584_(int duration, int amplifier) {
        return true;
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        Player p;
        if (entity.m_9236_().f_46443_) {
            return;
        }
        if (entity instanceof Player && (p = (Player)entity).m_20142_()) {
            p.m_6858_(false);
        }
    }
}

