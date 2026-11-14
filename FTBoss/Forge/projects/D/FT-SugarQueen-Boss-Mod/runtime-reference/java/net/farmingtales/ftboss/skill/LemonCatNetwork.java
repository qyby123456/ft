/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
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
package net.farmingtales.ftboss.skill;

import java.util.List;
import java.util.function.Predicate;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.LemonCatFxPacket;
import net.farmingtales.ftboss.skill.LemonSacrificePacket;
import net.farmingtales.ftboss.util.LemonSacrificeGeometry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class LemonCatNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "lemoncat_weapons"), () -> "2", (Predicate)NetworkRegistry.acceptMissingOr((String)"2"), (Predicate)NetworkRegistry.acceptMissingOr((String)"2"));

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(LemonCatFxPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(LemonCatFxPacket::encode).decoder(LemonCatFxPacket::decode).consumerMainThread(LemonCatFxPacket::handle).add();
        CHANNEL.messageBuilder(LemonSacrificePacket.class, 1, NetworkDirection.PLAY_TO_CLIENT).encoder(LemonSacrificePacket::encode).decoder(LemonSacrificePacket::decode).consumerMainThread(LemonSacrificePacket::handle).add();
    }

    public static LemonSacrificePacket sacrificePacket(LivingEntity owner, List<? extends LivingEntity> victims) {
        List<LemonSacrificeGeometry.Point> souls = victims.stream().map(victim -> victim.m_20191_().m_82399_().m_82546_(owner.m_20182_())).map(point -> new LemonSacrificeGeometry.Point(point.f_82479_, point.f_82480_, point.f_82481_)).filter(LemonSacrificeGeometry.Point::valid).limit(12L).toList();
        return new LemonSacrificePacket(owner.m_20148_(), owner.m_9236_().m_46472_().m_135782_(), owner.m_20182_(), owner.m_9236_().m_46467_(), souls);
    }

    public static void sacrifice(LivingEntity owner, List<? extends LivingEntity> victims) {
        Level level = owner.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        LemonSacrificePacket packet = LemonCatNetwork.sacrificePacket(owner, victims);
        if (!packet.valid()) {
            return;
        }
        for (ServerPlayer viewer : level2.m_6907_()) {
            if (viewer instanceof FakePlayer || viewer.f_8906_ == null || viewer.m_20280_((Entity)owner) > 9216.0 || !CHANNEL.isRemotePresent(viewer.f_8906_.f_9742_)) continue;
            try {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)packet);
            }
            catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] Sacrifice visual delivery failed: {}", (Object)failure.toString());
            }
        }
    }

    public static void send(ServerPlayer source, int kind) {
        LemonCatFxPacket message = new LemonCatFxPacket(source.m_9236_().m_46472_().m_135782_(), source.m_20182_(), source.m_146908_(), kind);
        for (ServerPlayer viewer : source.m_284548_().m_6907_()) {
            if (!(viewer.m_20280_((Entity)source) <= 4096.0) || !CHANNEL.isRemotePresent(viewer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)message);
        }
    }
}

