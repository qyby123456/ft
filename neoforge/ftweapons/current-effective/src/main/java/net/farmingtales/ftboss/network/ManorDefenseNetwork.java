package net.farmingtales.ftboss.network;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.ManorDefense;
import net.farmingtales.ftboss.client.render.ManorDefenseVisual;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorDefenseRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public final class ManorDefenseNetwork {
    private static long sequence;

    private ManorDefenseNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(Snapshot.TYPE, Snapshot.STREAM_CODEC, Snapshot::handle);
    }

    public static void sync(EasterBossEntity boss, ManorDefenseRules.State state) {
        PacketDistributor.sendToPlayersTrackingEntity(boss, snapshot(boss, state));
    }

    private static ManorDefenseNetwork.Snapshot snapshot(EasterBossEntity boss, ManorDefenseRules.State state) {
        long now = boss.level().getGameTime();
        return new ManorDefenseNetwork.Snapshot(
            boss.level().dimension().location(),
            boss.getUUID(),
            boss.getId(),
            boss.getVariant(),
            state == null ? 0 : state.phase,
            state == null ? now : state.start,
            state == null ? now : state.end,
            now,
            ++sequence,
            ManorDefense.adaptiveKind(boss)
        );
    }

    public static record Snapshot(
        ResourceLocation dimension, UUID uuid, int entityId, int variant, int phase, long start, long end, long sent, long sequence, int adaptiveKind
    ) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "manor_defense"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> STREAM_CODEC = StreamCodec.ofMember(Snapshot::encode, Snapshot::decode);

        @Override
        public Type<Snapshot> type() {
            return TYPE;
        }
        public boolean valid() {
            return this.dimension != null
                && this.uuid != null
                && this.entityId >= 0
                && ManorDefenseRules.profile(this.variant) != null
                && this.phase >= 0
                && this.phase <= 4
                && this.start >= 0L
                && this.sent >= this.start
                && this.end >= this.start
                && this.sequence > 0L
                && this.adaptiveKind >= 0
                && this.adaptiveKind <= 2
                && (this.phase == 4 || this.end - this.start <= 100L);
        }

        public static void encode(ManorDefenseNetwork.Snapshot packet, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(packet.dimension)
                .writeUUID(packet.uuid)
                .writeVarInt(packet.entityId)
                .writeByte(packet.variant)
                .writeByte(packet.phase)
                .writeLong(packet.start)
                .writeLong(packet.end)
                .writeLong(packet.sent)
                .writeLong(packet.sequence)
                .writeByte(packet.adaptiveKind);
        }

        public static ManorDefenseNetwork.Snapshot decode(FriendlyByteBuf buffer) {
            return new ManorDefenseNetwork.Snapshot(
                buffer.readResourceLocation(),
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readUnsignedByte(),
                buffer.readUnsignedByte(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readUnsignedByte()
            );
        }

        public static void handle(Snapshot packet, IPayloadContext context) {
            if (context.flow() != PacketFlow.CLIENTBOUND) {
                throw new IllegalArgumentException("Defense snapshot is clientbound only");
            }
            if (packet.valid()) {
                context.enqueueWork(() -> ClientOnly.accept(packet));
            }
        }
    }

    private static final class ClientOnly {
        private static void accept(Snapshot packet) {
            ManorDefenseVisual.accept(packet);
        }
    }

    @EventBusSubscriber(modid = "ftboss")
    public static final class Tracking {
        private Tracking() {
        }

        @SubscribeEvent
        public static void start(StartTracking event) {
            if (event.getTarget() instanceof EasterBossEntity boss
                && event.getEntity() instanceof ServerPlayer player
                && ManorDefenseRules.profile(boss.getVariant()) != null) {
                PacketDistributor.sendToPlayer(player, snapshot(boss, ManorDefense.state(boss)));
            }
        }
    }
}
