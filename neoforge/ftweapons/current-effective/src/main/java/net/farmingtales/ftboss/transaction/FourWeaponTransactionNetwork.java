package net.farmingtales.ftboss.transaction;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.CapabilityPacket;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.CommandPacket;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.ContainerOffer;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.Message;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.Payload;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.ReplyPacket;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionPackets.SnapshotPacket;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Reply;
import net.farmingtales.ftboss.transaction.TransactionWire.Command;
import net.farmingtales.ftboss.transaction.TransactionWire.TargetOpen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;

@EventBusSubscriber(modid = "ftboss", bus = Bus.MOD)
public final class FourWeaponTransactionNetwork {
    private static FourWeaponTransactions server;
    private static Consumer<Reply> client;
    private static boolean registered;
    private static boolean capability;
    private static Consumer<WeaponUiSnapshot> snapshots;

    private FourWeaponTransactionNetwork() {
    }

    @SubscribeEvent
    public static synchronized void register(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar(FourWeaponTransactionPackets.PROTOCOL).optional().executesOn(HandlerThread.MAIN)
                .playBidirectional(FourWeaponTransactionPackets.TYPE, FourWeaponTransactionPackets.STREAM_CODEC, FourWeaponTransactionNetwork::handle);
            registered = true;
            FourWeaponTransactionRuntime.register();
        }
    }

    public static void server(FourWeaponTransactions value) {
        server = value;
    }

    public static void client(Consumer<Reply> value) {
        client = value;
    }

    public static boolean ready() {
        return registered && capability && client != null;
    }

    public static boolean negotiated(ServerPlayer player) {
        return registered && player.connection.hasChannel(FourWeaponTransactionPackets.TYPE);
    }

    public static void offerContainer(ServerPlayer var0, ItemStack var1, BlockPos var2) {
        ExistingTransactionBoundary var3 = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        if (negotiated(var0)
            && var0.isAlive()
            && !var0.isSpectator()
            && !var0.isSleeping()
            && !var0.isUsingItem()
            && var0.containerMenu == var0.inventoryMenu
            && var0.inventoryMenu.getCarried().isEmpty()
            && var0.level().hasChunkAt(var2)
            && var3.statueIdentity(var0, var2) != null
            && !(var0.distanceToSqr((double)var2.getX() + 0.5, (double)(var2.getY() + 1), (double)var2.getZ() + 0.5) > 64.0)
            && var1.getCount() == 1
            && (var1 == var0.getMainHandItem() || var1 == var0.getOffhandItem())
            && FourWeaponFoundationAdapter.read(var1).valid()) {
            ForgeTransactionHost.Policy var4 = var3.policy(var0, var1, var2);
            UUID var5 = FoodWeaponLifecycle.weaponId(var1);
            if (var5 != null && var4 != null && var4.allowed().contains(Operation.OPEN_CONTAINER)) {
                TargetOpen var6 = new TargetOpen(
                    UUID.randomUUID(),
                    var1 == var0.getMainHandItem() ? 0 : 1,
                    var0.level().dimension().location().toString(),
                    var2.asLong(),
                    var5,
                    FourWeaponFoundationAdapter.revision(var1),
                    ForgeTransactionHost.fingerprint(var1, var0.registryAccess())
                );
                sendToPlayer(var0, new ContainerOffer(var6));
            }
        }
    }

    public static void snapshots(Consumer<WeaponUiSnapshot> value) {
        snapshots = value;
    }

    public static void clearClient() {
        capability = false;
        client = null;
        snapshots = null;
    }

    public static void capability(ServerPlayer player) {
        if (player.connection.hasChannel(FourWeaponTransactionPackets.TYPE)) {
            sendToPlayer(player, new CapabilityPacket(2));
        }
    }

    public static boolean send(Command command) {
        if (!ready()) {
            return false;
        }
        try {
            PacketDistributor.sendToServer(new Payload(new CommandPacket(TransactionWire.encode(command))));
            return true;
        } catch (RuntimeException | IOException failure) {
            return false;
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop();
        }
        server = null;
    }

    private static void sendToPlayer(ServerPlayer player, Message message) {
        PacketDistributor.sendToPlayer(player, new Payload(message));
    }

    private static void handle(Payload payload, IPayloadContext context) {
        Message message = payload.message();
        if (context.flow() != message.flow()) {
            context.disconnect(Component.literal("Invalid four-weapon transaction packet direction"));
            return;
        }
        switch (message) {
            case CommandPacket command -> handleCommand(command, context);
            case CapabilityPacket offer -> {
                capability = offer.version() == 2;
                if (capability) {
                    FourWeaponClientApi.initialize();
                }
            }
            case ContainerOffer offer -> {
                if (capability) {
                    FourWeaponClientApi.offeredContainer(offer.target());
                }
            }
            case ReplyPacket reply -> {
                if (client != null) {
                    try {
                        client.accept(TransactionWire.decodeReply(reply.payload()));
                    } catch (IOException failure) {
                        client.accept(null);
                    }
                }
            }
            case SnapshotPacket snapshot -> {
                if (capability && snapshots != null) {
                    snapshots.accept(new WeaponUiSnapshot(snapshot.snapshot().request(), snapshot.snapshot().data()));
                }
            }
        }
    }

    private static void handleCommand(CommandPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && server != null) {
            try {
                if (!negotiated(player)) {
                    return;
                }
                Command command = TransactionWire.decode(packet.payload());
                Reply reply = command instanceof TargetOpen target
                    ? FourWeaponTransactionRuntime.openTarget(player, target)
                    : TransactionWire.dispatch(server, player.getUUID(), command);
                if (reply != null) {
                    player.getInventory().setChanged();
                    player.inventoryMenu.broadcastChanges();
                    sendToPlayer(player, new ReplyPacket(TransactionWire.encodeReply(reply)));
                    WeaponUiSnapshot snapshot = WeaponUiSnapshot.read(player, reply);
                    if (snapshot != null) {
                        sendToPlayer(player, new SnapshotPacket(snapshot));
                    }
                }
            } catch (IllegalArgumentException | IOException failure) {
                server.invalidate(player.getUUID());
            }
        }
    }
}
