package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LemonCatNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FTBossMod.MOD_ID, "lemoncat_weapons"), () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        CHANNEL.messageBuilder(LemonCatFxPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LemonCatFxPacket::encode).decoder(LemonCatFxPacket::decode)
                .consumerMainThread(LemonCatFxPacket::handle).add();
        CHANNEL.messageBuilder(LemonSacrificePacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LemonSacrificePacket::encode).decoder(LemonSacrificePacket::decode)
                .consumerMainThread(LemonSacrificePacket::handle).add();
    }

    public static LemonSacrificePacket sacrificePacket(net.minecraft.world.entity.LivingEntity owner,
                                                       java.util.List<? extends net.minecraft.world.entity.LivingEntity> victims) {
        var souls = victims.stream().map(victim -> victim.getBoundingBox().getCenter().subtract(owner.position()))
                .map(point -> new net.farmingtales.ftboss.util.LemonSacrificeGeometry.Point(point.x, point.y, point.z))
                .filter(net.farmingtales.ftboss.util.LemonSacrificeGeometry.Point::valid)
                .limit(net.farmingtales.ftboss.util.LemonSacrificeGeometry.MAX_SOULS).toList();
        return new LemonSacrificePacket(owner.getUUID(), owner.level().dimension().location(), owner.position(),
                owner.level().getGameTime(), souls);
    }

    public static void sacrifice(net.minecraft.world.entity.LivingEntity owner,
                                 java.util.List<? extends net.minecraft.world.entity.LivingEntity> victims) {
        if (!(owner.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        LemonSacrificePacket packet = sacrificePacket(owner, victims);
        if (!packet.valid()) return;
        for (ServerPlayer viewer : level.players()) {
            if (viewer instanceof net.minecraftforge.common.util.FakePlayer || viewer.connection == null
                    || viewer.distanceToSqr(owner) > 96.0 * 96.0 || !CHANNEL.isRemotePresent(viewer.connection.connection)) continue;
            try {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), packet);
            } catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Lemon] Sacrifice visual delivery failed: {}", failure.toString());
            }
        }
    }

    public static void send(ServerPlayer source, int kind) {
        var message = new LemonCatFxPacket(source.level().dimension().location(), source.position(), source.getYRot(), kind);
        for (ServerPlayer viewer : source.serverLevel().players()) {
            if (viewer.distanceToSqr(source) <= 64.0D * 64.0D && CHANNEL.isRemotePresent(viewer.connection.connection)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), message);
            }
        }
    }
}
