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
package net.farmingtales.ftboss.lemoncasting;

import java.util.function.Predicate;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.lemoncasting.CastingClock;
import net.farmingtales.ftboss.lemoncasting.CastingPacket;
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
public final class CastingNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "lemon_cast_v1"), () -> "1", (Predicate)NetworkRegistry.acceptMissingOr((String)"1"), (Predicate)NetworkRegistry.acceptMissingOr((String)"1"));

    private CastingNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        CHANNEL.messageBuilder(CastingPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(CastingPacket::encode).decoder(CastingPacket::decode).consumerMainThread(CastingPacket::handle).add();
    }

    public static void send(LivingEntity livingEntity, CastingClock.Cue cue) {
        Object object = livingEntity.m_9236_();
        if (!(object instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        object = new CastingPacket(livingEntity.m_20148_(), serverLevel.m_46472_().m_135782_(), cue);
        if (!((CastingPacket)object).valid()) {
            return;
        }
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (serverPlayer instanceof FakePlayer || serverPlayer.f_8906_ == null || serverPlayer.m_20280_((Entity)livingEntity) > 9216.0 || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) continue;
            try {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), object);
            }
            catch (RuntimeException runtimeException) {
                FTBossMod.LOGGER.warn("[LemonCasting] Cue delivery skipped: {}", (Object)runtimeException.toString());
            }
        }
    }
}

