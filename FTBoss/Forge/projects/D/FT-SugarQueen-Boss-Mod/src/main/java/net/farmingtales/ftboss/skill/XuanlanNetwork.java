package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class XuanlanNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FTBossMod.MOD_ID, "xuanlan_weapons"), () -> "1", "1"::equals, "1"::equals);

    private XuanlanNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(XuanlanFxPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(XuanlanFxPacket::encode).decoder(XuanlanFxPacket::decode)
                .consumerMainThread(XuanlanFxPacket::handle).add();
    }
}
