/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.Unpooled
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.transaction;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionWire;
import net.farmingtales.ftboss.transaction.WeaponUiSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class FourWeaponTransactionNetwork {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)Objects.requireNonNull(ResourceLocation.m_135820_((String)"ftboss:four_weapon_transactions")), () -> "2", (Predicate)NetworkRegistry.acceptMissingOr((String)"2"), (Predicate)NetworkRegistry.acceptMissingOr((String)"2"));
    private static FourWeaponTransactions server;
    private static Consumer<FourWeaponTransactions.Reply> client;
    private static boolean registered;
    private static boolean capability;
    private static Consumer<WeaponUiSnapshot> snapshots;

    private FourWeaponTransactionNetwork() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(CommandPacket.class, 0, NetworkDirection.PLAY_TO_SERVER).encoder(CommandPacket::encode).decoder(CommandPacket::decode).consumerMainThread(CommandPacket::handle).add();
        CHANNEL.messageBuilder(ReplyPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT).encoder(ReplyPacket::encode).decoder(ReplyPacket::decode).consumerMainThread(ReplyPacket::handle).add();
        CHANNEL.messageBuilder(CapabilityPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT).encoder(CapabilityPacket::encode).decoder(CapabilityPacket::decode).consumerMainThread(CapabilityPacket::handle).add();
        CHANNEL.messageBuilder(SnapshotPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT).encoder(SnapshotPacket::encode).decoder(SnapshotPacket::decode).consumerMainThread(SnapshotPacket::handle).add();
        CHANNEL.messageBuilder(ContainerOffer.class, 4, NetworkDirection.PLAY_TO_CLIENT).encoder(ContainerOffer::encode).decoder(ContainerOffer::decode).consumerMainThread(ContainerOffer::handle).add();
    }

    public static void server(FourWeaponTransactions fourWeaponTransactions) {
        server = fourWeaponTransactions;
    }

    public static void client(Consumer<FourWeaponTransactions.Reply> consumer) {
        client = consumer;
    }

    public static boolean ready() {
        return registered && capability && client != null;
    }

    public static boolean negotiated(ServerPlayer serverPlayer) {
        return registered && CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_);
    }

    public static void offerContainer(ServerPlayer serverPlayer, ItemStack itemStack, BlockPos blockPos) {
        ExistingTransactionBoundary existingTransactionBoundary = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        if (!FourWeaponTransactionNetwork.negotiated(serverPlayer) || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || serverPlayer.f_36096_ != serverPlayer.f_36095_ || !serverPlayer.f_36095_.m_142621_().m_41619_() || !serverPlayer.m_9236_().m_46805_(blockPos) || existingTransactionBoundary.statueIdentity(serverPlayer, blockPos) == null || serverPlayer.m_20275_((double)blockPos.m_123341_() + 0.5, (double)(blockPos.m_123342_() + 1), (double)blockPos.m_123343_() + 0.5) > 64.0 || itemStack.m_41613_() != 1 || itemStack != serverPlayer.m_21205_() && itemStack != serverPlayer.m_21206_() || !FourWeaponFoundationAdapter.read(itemStack).valid()) {
            return;
        }
        ForgeTransactionHost.Policy policy = existingTransactionBoundary.policy(serverPlayer, itemStack, blockPos);
        UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
        if (uUID == null || policy == null || !policy.allowed().contains((Object)FourWeaponTransactions.Operation.OPEN_CONTAINER)) {
            return;
        }
        TransactionWire.TargetOpen targetOpen = new TransactionWire.TargetOpen(UUID.randomUUID(), itemStack == serverPlayer.m_21205_() ? 0 : 1, serverPlayer.m_9236_().m_46472_().m_135782_().toString(), blockPos.m_121878_(), uUID, FourWeaponFoundationAdapter.revision(itemStack), ForgeTransactionHost.fingerprint(itemStack));
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new ContainerOffer(targetOpen));
    }

    public static void snapshots(Consumer<WeaponUiSnapshot> consumer) {
        snapshots = consumer;
    }

    public static void clearClient() {
        capability = false;
        client = null;
        snapshots = null;
    }

    public static void capability(ServerPlayer serverPlayer) {
        if (CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new CapabilityPacket(2));
        }
    }

    public static boolean send(TransactionWire.Command command) {
        if (!FourWeaponTransactionNetwork.ready()) {
            return false;
        }
        try {
            CHANNEL.sendToServer((Object)new CommandPacket(TransactionWire.encode(command)));
            return true;
        }
        catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop();
        }
        server = null;
    }

    public static final class CommandPacket {
        private final byte[] payload;

        public CommandPacket(byte[] byArray) {
            this.payload = (byte[])Objects.requireNonNull(byArray).clone();
        }

        public static void encode(CommandPacket commandPacket, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130087_(commandPacket.payload);
        }

        public static CommandPacket decode(FriendlyByteBuf friendlyByteBuf) {
            CommandPacket commandPacket = new CommandPacket(friendlyByteBuf.m_130101_(8192));
            if (friendlyByteBuf.isReadable()) {
                throw new IllegalArgumentException("command trailing bytes");
            }
            return commandPacket;
        }

        private static void handle(CommandPacket commandPacket, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null && server != null) {
                try {
                    FourWeaponTransactions.Reply reply;
                    Object object;
                    if (!FourWeaponTransactionNetwork.negotiated(serverPlayer)) {
                        return;
                    }
                    TransactionWire.Command command = TransactionWire.decode(commandPacket.payload);
                    if (command instanceof TransactionWire.TargetOpen) {
                        object = (TransactionWire.TargetOpen)command;
                        v0 = FourWeaponTransactionRuntime.openTarget(serverPlayer, (TransactionWire.TargetOpen)object);
                    } else {
                        v0 = reply = TransactionWire.dispatch(server, serverPlayer.m_20148_(), command);
                    }
                    if (reply != null) {
                        serverPlayer.m_150109_().m_6596_();
                        serverPlayer.f_36095_.m_38946_();
                        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new ReplyPacket(TransactionWire.encodeReply(reply)));
                        object = WeaponUiSnapshot.read(serverPlayer, reply);
                        if (object != null) {
                            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new SnapshotPacket((WeaponUiSnapshot)object));
                        }
                    }
                }
                catch (IOException | IllegalArgumentException exception) {
                    server.invalidate(serverPlayer.m_20148_());
                }
            }
            context.setPacketHandled(true);
        }
    }

    public static final class ReplyPacket {
        private final byte[] payload;

        public ReplyPacket(byte[] byArray) {
            this.payload = (byte[])Objects.requireNonNull(byArray).clone();
        }

        public static void encode(ReplyPacket replyPacket, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130087_(replyPacket.payload);
        }

        public static ReplyPacket decode(FriendlyByteBuf friendlyByteBuf) {
            ReplyPacket replyPacket = new ReplyPacket(friendlyByteBuf.m_130101_(8192));
            if (friendlyByteBuf.isReadable()) {
                throw new IllegalArgumentException("reply trailing bytes");
            }
            return replyPacket;
        }

        private static void handle(ReplyPacket replyPacket, Supplier<NetworkEvent.Context> supplier) {
            if (client != null) {
                try {
                    client.accept(TransactionWire.decodeReply(replyPacket.payload));
                }
                catch (IOException iOException) {
                    client.accept(null);
                }
            }
            supplier.get().setPacketHandled(true);
        }
    }

    public record CapabilityPacket(int version) {
        public static void encode(CapabilityPacket capabilityPacket, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130130_(capabilityPacket.version());
        }

        public static CapabilityPacket decode(FriendlyByteBuf friendlyByteBuf) {
            CapabilityPacket capabilityPacket = new CapabilityPacket(friendlyByteBuf.m_130242_());
            if (friendlyByteBuf.isReadable()) {
                throw new IllegalArgumentException("capability trailing bytes");
            }
            return capabilityPacket;
        }

        private static void handle(CapabilityPacket capabilityPacket, Supplier<NetworkEvent.Context> supplier) {
            boolean bl = capability = capabilityPacket.version() == 2;
            if (capability) {
                FourWeaponClientApi.initialize();
            }
            supplier.get().setPacketHandled(true);
        }
    }

    public record SnapshotPacket(WeaponUiSnapshot snapshot) {
        public static final int LIMIT = 65536;

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static void encode(SnapshotPacket snapshotPacket, FriendlyByteBuf friendlyByteBuf) {
            FriendlyByteBuf friendlyByteBuf2 = new FriendlyByteBuf(Unpooled.buffer());
            try {
                CompoundTag compoundTag = snapshotPacket.snapshot().data();
                SnapshotPacket.validateNbtStrings((Tag)compoundTag);
                friendlyByteBuf2.m_130079_(compoundTag);
                if (friendlyByteBuf2.readableBytes() > 65536) {
                    throw new IllegalArgumentException("snapshot too large");
                }
                byte[] byArray = new byte[friendlyByteBuf2.readableBytes()];
                friendlyByteBuf2.readBytes(byArray);
                friendlyByteBuf.m_130077_(snapshotPacket.snapshot().request());
                friendlyByteBuf.m_130087_(byArray);
            }
            finally {
                friendlyByteBuf2.release();
            }
        }

        private static void validateNbtStrings(Tag tag) {
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                for (String string : compoundTag.m_128431_()) {
                    SnapshotPacket.validateUtf(string);
                    SnapshotPacket.validateNbtStrings(Objects.requireNonNull(compoundTag.m_128423_(string)));
                }
            } else if (tag instanceof ListTag) {
                ListTag listTag = (ListTag)tag;
                for (Tag tag2 : listTag) {
                    SnapshotPacket.validateNbtStrings(tag2);
                }
            } else if (tag instanceof StringTag) {
                StringTag stringTag = (StringTag)tag;
                SnapshotPacket.validateUtf(stringTag.m_7916_());
            }
        }

        private static void validateUtf(String string) {
            int n = 0;
            for (int i = 0; i < string.length(); ++i) {
                char c = string.charAt(i);
                if ((n += c >= '\u0001' && c <= '\u007f' ? 1 : (c > '\u07ff' ? 3 : 2)) <= 65535) continue;
                throw new IllegalArgumentException("NBT string too large");
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static SnapshotPacket decode(FriendlyByteBuf friendlyByteBuf) {
            UUID uUID = friendlyByteBuf.m_130259_();
            byte[] byArray = friendlyByteBuf.m_130101_(65536);
            if (friendlyByteBuf.isReadable()) {
                throw new IllegalArgumentException("snapshot trailing bytes");
            }
            FriendlyByteBuf friendlyByteBuf2 = new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])byArray));
            try {
                CompoundTag compoundTag = friendlyByteBuf2.m_130260_();
                if (compoundTag == null || friendlyByteBuf2.isReadable()) {
                    throw new IllegalArgumentException("snapshot framing");
                }
                SnapshotPacket snapshotPacket = new SnapshotPacket(new WeaponUiSnapshot(uUID, compoundTag));
                return snapshotPacket;
            }
            finally {
                friendlyByteBuf2.release();
            }
        }

        private static void handle(SnapshotPacket snapshotPacket, Supplier<NetworkEvent.Context> supplier) {
            if (capability && snapshots != null) {
                snapshots.accept(snapshotPacket.snapshot());
            }
            supplier.get().setPacketHandled(true);
        }
    }

    public record ContainerOffer(TransactionWire.TargetOpen target) {
        public static void encode(ContainerOffer containerOffer, FriendlyByteBuf friendlyByteBuf) {
            try {
                friendlyByteBuf.m_130087_(TransactionWire.encode(containerOffer.target()));
            }
            catch (IOException iOException) {
                throw new IllegalArgumentException(iOException);
            }
        }

        public static ContainerOffer decode(FriendlyByteBuf friendlyByteBuf) {
            try {
                TransactionWire.Command command = TransactionWire.decode(friendlyByteBuf.m_130101_(8192));
                if (friendlyByteBuf.isReadable()) {
                    throw new IllegalArgumentException("offer trailing bytes");
                }
                if (!(command instanceof TransactionWire.TargetOpen)) {
                    throw new IllegalArgumentException("offer type");
                }
                TransactionWire.TargetOpen targetOpen = (TransactionWire.TargetOpen)command;
                return new ContainerOffer(targetOpen);
            }
            catch (IOException iOException) {
                throw new IllegalArgumentException(iOException);
            }
        }

        private static void handle(ContainerOffer containerOffer, Supplier<NetworkEvent.Context> supplier) {
            if (capability) {
                FourWeaponClientApi.offeredContainer(containerOffer.target());
            }
            supplier.get().setPacketHandled(true);
        }
    }
}

