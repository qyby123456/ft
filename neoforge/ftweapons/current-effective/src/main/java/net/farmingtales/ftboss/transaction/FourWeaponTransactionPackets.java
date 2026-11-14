package net.farmingtales.ftboss.transaction;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import net.farmingtales.ftboss.transaction.TransactionWire.Command;
import net.farmingtales.ftboss.transaction.TransactionWire.TargetOpen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class FourWeaponTransactionPackets {
    public static final String PROTOCOL = "2";
    public static final CustomPacketPayload.Type<Payload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "four_weapon_transactions"));
    public static final StreamCodec<FriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.of(FourWeaponTransactionPackets::encode, FourWeaponTransactionPackets::decode);

    private FourWeaponTransactionPackets() {
    }

    public sealed interface Message permits CommandPacket, ReplyPacket, CapabilityPacket, SnapshotPacket, ContainerOffer {
        int discriminator();

        PacketFlow flow();
    }

    public record Payload(Message message) implements CustomPacketPayload {
        public Payload {
            Objects.requireNonNull(message);
        }

        @Override
        public CustomPacketPayload.Type<Payload> type() {
            return TYPE;
        }
    }

    public static void encode(FriendlyByteBuf buffer, Payload payload) {
        Message message = payload.message();
        buffer.writeVarInt(message.discriminator());
        switch (message) {
            case CommandPacket command -> CommandPacket.encode(command, buffer);
            case ReplyPacket reply -> ReplyPacket.encode(reply, buffer);
            case CapabilityPacket capability -> CapabilityPacket.encode(capability, buffer);
            case SnapshotPacket snapshot -> SnapshotPacket.encode(snapshot, buffer);
            case ContainerOffer offer -> ContainerOffer.encode(offer, buffer);
        }
    }

    public static Payload decode(FriendlyByteBuf buffer) {
        return new Payload(switch (buffer.readVarInt()) {
            case 0 -> CommandPacket.decode(buffer);
            case 1 -> ReplyPacket.decode(buffer);
            case 2 -> CapabilityPacket.decode(buffer);
            case 3 -> SnapshotPacket.decode(buffer);
            case 4 -> ContainerOffer.decode(buffer);
            default -> throw new IllegalArgumentException("transaction discriminator");
        });
    }

    public static record CapabilityPacket(int version) implements Message {
        @Override
        public int discriminator() {
            return 2;
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.CLIENTBOUND;
        }

        public static void encode(CapabilityPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.version());
        }

        public static CapabilityPacket decode(FriendlyByteBuf buffer) {
            CapabilityPacket packet = new CapabilityPacket(buffer.readVarInt());
            if (buffer.isReadable()) {
                throw new IllegalArgumentException("capability trailing bytes");
            }
            return packet;
        }
    }

    public static final class CommandPacket implements Message {
        private final byte[] payload;

        public CommandPacket(byte[] payload) {
            this.payload = Objects.requireNonNull(payload).clone();
        }

        public byte[] payload() {
            return this.payload.clone();
        }

        @Override
        public int discriminator() {
            return 0;
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.SERVERBOUND;
        }

        public static void encode(CommandPacket packet, FriendlyByteBuf buffer) {
            buffer.writeByteArray(packet.payload);
        }

        public static CommandPacket decode(FriendlyByteBuf buffer) {
            CommandPacket packet = new CommandPacket(buffer.readByteArray(8192));
            if (buffer.isReadable()) {
                throw new IllegalArgumentException("command trailing bytes");
            }
            return packet;
        }
    }

    public static record ContainerOffer(TargetOpen target) implements Message {
        @Override
        public int discriminator() {
            return 4;
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.CLIENTBOUND;
        }

        public static void encode(ContainerOffer packet, FriendlyByteBuf buffer) {
            try {
                buffer.writeByteArray(TransactionWire.encode(packet.target()));
            } catch (IOException failure) {
                throw new IllegalArgumentException(failure);
            }
        }

        public static ContainerOffer decode(FriendlyByteBuf buffer) {
            try {
                Command command = TransactionWire.decode(buffer.readByteArray(8192));
                if (buffer.isReadable()) {
                    throw new IllegalArgumentException("offer trailing bytes");
                }
                if (command instanceof TargetOpen target) {
                    return new ContainerOffer(target);
                }
                throw new IllegalArgumentException("offer type");
            } catch (IOException failure) {
                throw new IllegalArgumentException(failure);
            }
        }
    }

    public static final class ReplyPacket implements Message {
        private final byte[] payload;

        public ReplyPacket(byte[] payload) {
            this.payload = Objects.requireNonNull(payload).clone();
        }

        public byte[] payload() {
            return this.payload.clone();
        }

        @Override
        public int discriminator() {
            return 1;
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.CLIENTBOUND;
        }

        public static void encode(ReplyPacket packet, FriendlyByteBuf buffer) {
            buffer.writeByteArray(packet.payload);
        }

        public static ReplyPacket decode(FriendlyByteBuf buffer) {
            ReplyPacket packet = new ReplyPacket(buffer.readByteArray(8192));
            if (buffer.isReadable()) {
                throw new IllegalArgumentException("reply trailing bytes");
            }
            return packet;
        }
    }

    public static record SnapshotPacket(TransactionSnapshotData snapshot) implements Message {
        public static final int LIMIT = 65536;

        @Override
        public int discriminator() {
            return 3;
        }

        @Override
        public PacketFlow flow() {
            return PacketFlow.CLIENTBOUND;
        }

        public static void encode(SnapshotPacket packet, FriendlyByteBuf buffer) {
            FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.buffer());
            try {
                CompoundTag data = packet.snapshot().data();
                validateNbtStrings(data);
                encoded.writeNbt(data);
                if (encoded.readableBytes() > 65536) {
                    throw new IllegalArgumentException("snapshot too large");
                }
                byte[] bytes = new byte[encoded.readableBytes()];
                encoded.readBytes(bytes);
                buffer.writeUUID(packet.snapshot().request());
                buffer.writeByteArray(bytes);
            } finally {
                encoded.release();
            }
        }

        private static void validateNbtStrings(Tag value) {
            if (value instanceof CompoundTag compound) {
                for (String key : compound.getAllKeys()) {
                    validateUtf(key);
                    validateNbtStrings(Objects.requireNonNull(compound.get(key)));
                }
            } else if (value instanceof ListTag list) {
                for (Tag element : list) {
                    validateNbtStrings(element);
                }
            } else if (value instanceof StringTag text) {
                validateUtf(text.getAsString());
            }
        }

        private static void validateUtf(String value) {
            int size = 0;
            for (int index = 0; index < value.length(); index++) {
                char character = value.charAt(index);
                size += character >= 1 && character <= 127 ? 1 : (character > 2047 ? 3 : 2);
                if (size > 65535) {
                    throw new IllegalArgumentException("NBT string too large");
                }
            }
        }

        public static SnapshotPacket decode(FriendlyByteBuf buffer) {
            UUID request = buffer.readUUID();
            byte[] bytes = buffer.readByteArray(65536);
            if (buffer.isReadable()) {
                throw new IllegalArgumentException("snapshot trailing bytes");
            }
            FriendlyByteBuf encoded = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            try {
                CompoundTag data = encoded.readNbt();
                if (data == null || encoded.isReadable()) {
                    throw new IllegalArgumentException("snapshot framing");
                }
                return new SnapshotPacket(new TransactionSnapshotData(request, data));
            } finally {
                encoded.release();
            }
        }
    }
}
