package net.farmingtales.ftboss.sound;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BossMusicNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FTBossMod.MOD_ID, "boss_music"), () -> "1", "1"::equals, "1"::equals);

    private BossMusicNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(BossMusicPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BossMusicPacket::encode).decoder(BossMusicPacket::decode)
                .consumerMainThread(BossMusicPacket::handle).add();
    }
}
