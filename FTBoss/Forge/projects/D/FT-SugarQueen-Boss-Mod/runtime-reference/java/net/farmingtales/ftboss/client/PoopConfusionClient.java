/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.Input
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.MovementInputUpdateEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.client.player.Input;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class PoopConfusionClient {
    private PoopConfusionClient() {
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent e) {
        Player player = e.getEntity();
        if (player == null) {
            return;
        }
        MobEffectInstance inst = player.m_21124_((MobEffect)FTEffects.POOP_CONFUSION.get());
        if (inst == null) {
            return;
        }
        Input in = e.getInput();
        float fwd = in.f_108567_;
        float side = in.f_108566_;
        boolean up = in.f_108568_;
        boolean down = in.f_108569_;
        boolean left = in.f_108570_;
        boolean right = in.f_108571_;
        int amp = inst.m_19564_();
        if (amp <= 0) {
            in.f_108566_ = -side;
            in.f_108570_ = right;
            in.f_108571_ = left;
        } else if (amp == 1) {
            in.f_108567_ = -fwd;
            in.f_108566_ = -side;
            in.f_108568_ = down;
            in.f_108569_ = up;
            in.f_108570_ = right;
            in.f_108571_ = left;
        } else {
            in.f_108567_ = -side;
            in.f_108566_ = fwd;
            in.f_108568_ = right;
            in.f_108571_ = down;
            in.f_108569_ = left;
            in.f_108570_ = up;
        }
    }
}

