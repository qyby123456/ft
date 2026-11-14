/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.sound;

import net.farmingtales.ftboss.sound.BossMusicPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class BossMusicNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "boss_music"), () -> "1", "1"::equals, "1"::equals);

    private BossMusicNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(BossMusicPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(BossMusicPacket::encode).decoder(BossMusicPacket::decode).consumerMainThread(BossMusicPacket::handle).add();
    }
}

