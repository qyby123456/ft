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
package net.farmingtales.ftboss.combat.xueli;

import java.util.function.Predicate;
import net.farmingtales.ftboss.combat.xueli.CombatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CombatNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "xueli_combat"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));
    private static boolean registered;

    private CombatNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        if (!registered) {
            registered = true;
            CHANNEL.messageBuilder(CombatPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(CombatPacket::encode).decoder(CombatPacket::decode).consumerMainThread(CombatPacket::handle).add();
        }
    }
}

