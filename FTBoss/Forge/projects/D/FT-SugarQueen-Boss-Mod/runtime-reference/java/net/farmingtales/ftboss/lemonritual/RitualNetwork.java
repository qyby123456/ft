/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.lemonritual;

import java.util.function.Predicate;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.lemonritual.RitualSummonPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RitualNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "lemon_ritual_v1"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));

    private RitualNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        CHANNEL.messageBuilder(RitualSummonPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(RitualSummonPacket::encode).decoder(RitualSummonPacket::decode).consumerMainThread(RitualSummonPacket::handle).add();
    }

    public static void summoned(LivingEntity livingEntity, Entity entity) {
        try {
            ServerLevel serverLevel;
            Object object;
            block10: {
                block9: {
                    object = livingEntity.m_9236_();
                    if (!(object instanceof ServerLevel)) break block9;
                    serverLevel = (ServerLevel)object;
                    if (entity != null && entity.m_6084_()) break block10;
                }
                return;
            }
            object = new RitualSummonPacket(livingEntity.m_20148_(), entity.m_20148_(), serverLevel.m_46472_().m_135782_(), entity.m_20182_(), serverLevel.m_46467_(), entity.m_20205_(), entity.m_20206_());
            if (!((RitualSummonPacket)object).valid()) {
                return;
            }
            for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
                if (serverPlayer instanceof FakePlayer || serverPlayer.f_8906_ == null || serverPlayer.m_20280_(entity) > 9216.0 || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) continue;
                try {
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), object);
                }
                catch (RuntimeException runtimeException) {
                    FTBossMod.LOGGER.warn("[LemonRitual] Visual delivery failed: {}", (Object)runtimeException.toString());
                }
            }
        }
        catch (RuntimeException runtimeException) {
            FTBossMod.LOGGER.warn("[LemonRitual] Visual skipped: {}", (Object)runtimeException.toString());
        }
    }
}

