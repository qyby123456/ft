package net.farmingtales.ftboss.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QizaiBarrageNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("ftboss", "qizai_barrage"),
            () -> "1", "1"::equals, "1"::equals);

    private QizaiBarrageNetwork() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(State.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(State::encode).decoder(State::decode).consumerMainThread(State::handle).add());
    }

    public static void show(ServerLevel level, UUID owner, Vec3 center, CompoundTag cue) {
        State state = new State(level.dimension().location(), owner, cue);
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= 96 * 96)
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), state);
        }
    }

    public record State(ResourceLocation dimension, UUID owner, CompoundTag cue) {
        public static void encode(State state, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(state.dimension).writeUUID(state.owner).writeNbt(state.cue);
        }

        public static State decode(FriendlyByteBuf buffer) {
            ResourceLocation dimension = buffer.readResourceLocation();
            UUID owner = buffer.readUUID();
            CompoundTag cue = buffer.readNbt();
            return new State(dimension, owner, cue == null ? new CompoundTag() : cue);
        }

        public static void handle(State state, Supplier<NetworkEvent.Context> context) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.farmingtales.ftboss.client.render.QizaiBarrageVisual.accept(state));
            context.get().setPacketHandled(true);
        }
    }
}
