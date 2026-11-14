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
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class XuanlanNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "xuanlan_weapons"), () -> "1", "1"::equals, "1"::equals);

    private XuanlanNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(XuanlanFxPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(XuanlanFxPacket::encode).decoder(XuanlanFxPacket::decode).consumerMainThread(XuanlanFxPacket::handle).add();
    }
}

