package net.farmingtales.ftboss.network;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public final class NativeStatueCavityNetwork {
    static final Map<ServerPlayer, Integer> LAST_ACTION_TICK = new WeakHashMap<>();

    private NativeStatueCavityNetwork() {
    }

    public record Payload(Object packet) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "statue_hub"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.ofMember(Payload::write, Payload::read);

        public Payload {
            if (!(packet instanceof OrdinaryCavityAction) && !(packet instanceof OrdinaryCavityLayoutAction) && !(packet instanceof StatueMenuNetwork.OrdinaryRepairAction) && !(packet instanceof StatueMenuNetwork.OrdinaryOpen) && !(packet instanceof StatueMenuNetwork.OrdinaryAction) && !(packet instanceof ActionResult) && !(packet instanceof CavityAction) && !(packet instanceof CavityLayoutAction) && !(packet instanceof CavityOpenAction) && !(packet instanceof StatueMenuNetwork.RepairAction) && !(packet instanceof StatueMenuNetwork.SkillSelection)) {
                throw new IllegalArgumentException("Unsupported original statue cavity packet");
            }
        }

        private static void write(Payload payload, RegistryFriendlyByteBuf buffer) {
            if (payload.packet instanceof OrdinaryCavityAction ordinary) {
                buffer.writeVarInt(10);
                OrdinaryCavityAction.encode(ordinary, buffer);
            } else if (payload.packet instanceof OrdinaryCavityLayoutAction ordinary) {
                buffer.writeVarInt(11);
                OrdinaryCavityLayoutAction.encode(ordinary, buffer);
            } else if (payload.packet instanceof StatueMenuNetwork.OrdinaryRepairAction repair) {
                buffer.writeVarInt(9);
                StatueMenuNetwork.OrdinaryRepairAction.encode(repair, buffer);
            } else if (payload.packet instanceof StatueMenuNetwork.OrdinaryOpen open) {
                buffer.writeVarInt(0);
                StatueMenuNetwork.OrdinaryOpen.encode(open, buffer);
            } else if (payload.packet instanceof ActionResult result) {
                buffer.writeVarInt(3);
                ActionResult.encode(result, buffer);
            } else if (payload.packet instanceof CavityAction action) {
                buffer.writeVarInt(6);
                CavityAction.encode(action, buffer);
            } else if (payload.packet instanceof CavityLayoutAction layout) {
                buffer.writeVarInt(7);
                CavityLayoutAction.encode(layout, buffer);
            } else if (payload.packet instanceof CavityOpenAction open) {
                buffer.writeVarInt(8);
                CavityOpenAction.encode(open, buffer);
            } else if (payload.packet instanceof StatueMenuNetwork.RepairAction repair) {
                buffer.writeVarInt(5);
                StatueMenuNetwork.RepairAction.encode(repair, buffer);
            } else if (payload.packet instanceof StatueMenuNetwork.SkillSelection skill) {
                buffer.writeVarInt(4);
                StatueMenuNetwork.SkillSelection.encode(skill, buffer);
            } else if (payload.packet instanceof StatueMenuNetwork.OrdinaryAction action) {
                buffer.writeVarInt(1);
                StatueMenuNetwork.OrdinaryAction.encode(action, buffer);
            } else {
                throw new IllegalArgumentException("Unsupported original statue cavity packet");
            }
        }

        private static Payload read(RegistryFriendlyByteBuf buffer) {
            return new Payload(switch (buffer.readVarInt()) {
                case 0 -> StatueMenuNetwork.OrdinaryOpen.decode(buffer);
                case 1 -> StatueMenuNetwork.OrdinaryAction.decode(buffer);
                case 3 -> ActionResult.decode(buffer);
                case 6 -> CavityAction.decode(buffer);
                case 7 -> CavityLayoutAction.decode(buffer);
                case 8 -> CavityOpenAction.decode(buffer);
                case 5 -> StatueMenuNetwork.RepairAction.decode(buffer);
                case 9 -> StatueMenuNetwork.OrdinaryRepairAction.decode(buffer);
                case 11 -> OrdinaryCavityLayoutAction.decode(buffer);
                case 10 -> OrdinaryCavityAction.decode(buffer);
                case 4 -> StatueMenuNetwork.SkillSelection.decode(buffer);
                default -> throw new IllegalArgumentException("Unsupported original statue cavity discriminator");
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("10").playBidirectional(Payload.TYPE, Payload.STREAM_CODEC, NativeStatueCavityNetwork::handle);
    }

    private static void handle(Payload payload, IPayloadContext context) {
        if (context.flow() == PacketFlow.SERVERBOUND) {
            if (payload.packet instanceof StatueMenuNetwork.OrdinaryAction action) StatueMenuNetwork.OrdinaryAction.handle(action, context);
            else if (payload.packet instanceof CavityAction action) CavityAction.handle(action, context);
            else if (payload.packet instanceof CavityLayoutAction layout) CavityLayoutAction.handle(layout, context);
            else if (payload.packet instanceof CavityOpenAction open) CavityOpenAction.handle(open, context);
            else if (payload.packet instanceof StatueMenuNetwork.RepairAction repair) StatueMenuNetwork.RepairAction.handle(repair, context);
            else if (payload.packet instanceof StatueMenuNetwork.OrdinaryRepairAction repair) StatueMenuNetwork.OrdinaryRepairAction.handle(repair, context);
            else if (payload.packet instanceof OrdinaryCavityLayoutAction ordinary) OrdinaryCavityLayoutAction.handle(ordinary, context);
            else if (payload.packet instanceof OrdinaryCavityAction ordinary) OrdinaryCavityAction.handle(ordinary, context);
            else if (payload.packet instanceof StatueMenuNetwork.SkillSelection skill) StatueMenuNetwork.SkillSelection.handle(skill, context);
            else throw new IllegalArgumentException("Client cannot send original server acknowledgement");
        } else if (context.flow() == PacketFlow.CLIENTBOUND && payload.packet instanceof StatueMenuNetwork.OrdinaryOpen open) {
            context.enqueueWork(() -> ClientOnly.open(open));
        } else if (context.flow() == PacketFlow.CLIENTBOUND && payload.packet instanceof ActionResult result) {
            context.enqueueWork(() -> ClientOnly.acknowledge(result));
        } else {
            throw new IllegalArgumentException("Server cannot send original client cavity command");
        }
    }

    static void send(Object packet) {
        PacketDistributor.sendToServer(new Payload(packet));
    }

    static void send(ServerPlayer player, Object packet) {
        PacketDistributor.sendToPlayer(player, new Payload(packet));
    }

    private static final class ClientOnly {
        private static void open(StatueMenuNetwork.OrdinaryOpen packet) {
            net.farmingtales.ftboss.client.OrdinaryStatueHubScreen.open(packet);
            net.farmingtales.ftboss.client.StatueWeaponRepairNavigation.bootstrapOrdinaryHub(packet.pos());
        }

        private static void acknowledge(ActionResult result) {
            net.farmingtales.ftboss.client.StatueHubScreen.acknowledgeAction(result.pos);
            net.farmingtales.ftboss.client.OrdinaryStatueHubScreen.acknowledgeAction(result.pos);
        }
    }

    private static BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.getBlock() instanceof CandyStatueBlock && state.getValue(CandyStatueBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    static BlockPos validatedStatuePos(ServerPlayer var0, BlockPos var1) {
        if (var0.isAlive() && !var0.isSpectator() && var1 != null && var0.level().hasChunkAt(var1)) {
            BlockPos var2 = lowerPos(var0.level().getBlockState(var1), var1);
            if (var0.distanceToSqr((double)var2.getX() + 0.5, (double)var2.getY() + 1.0, (double)var2.getZ() + 0.5) > 64.0) {
                return null;
            } else {
                return var0.level().getBlockState(var2).getBlock() instanceof CandyStatueBlock ? var2 : null;
            }
        } else {
            return null;
        }
    }

    public static record CavityAction(BlockPos pos, int part, int inventorySlot, UUID weapon, int revision, ResourceLocation offering) {
        public static boolean send(BlockPos var0, Slot var1, int var2, ItemStack var3, ItemStack var4) {
            UUID var5 = FoodWeaponLifecycle.weaponId(var3);
            ResourceLocation var6 = BuiltInRegistries.ITEM.getKey(var4.getItem());
            if (var5 != null
                && var6 != null
                && var2 >= 0
                && var2 < 36
                && !var4.isEmpty()
                && SwordCavityBuild.canReplace(var3, var1, WeaponMaterialBuild.materialFor(var4))) {
                NativeStatueCavityNetwork.send(new NativeStatueCavityNetwork.CavityAction(var0, var1.ordinal(), var2, var5, SwordCavityBuild.revision(var3), var6));
                return true;
            } else {
                return false;
            }
        }

        private static void encode(NativeStatueCavityNetwork.CavityAction var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeVarInt(var0.part);
            var1.writeVarInt(var0.inventorySlot);
            var1.writeUUID(var0.weapon);
            var1.writeVarInt(var0.revision);
            var1.writeResourceLocation(var0.offering);
        }

        private static NativeStatueCavityNetwork.CavityAction decode(FriendlyByteBuf var0) {
            return new NativeStatueCavityNetwork.CavityAction(
                var0.readBlockPos(), var0.readVarInt(), var0.readVarInt(), var0.readUUID(), var0.readVarInt(), var0.readResourceLocation()
            );
        }

        private static void handle(NativeStatueCavityNetwork.CavityAction var0, IPayloadContext var1) {
            var1
                .enqueueWork(
                    () -> {
                        ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
                        if (var2 != null && !(var2.containerMenu instanceof SwordCavityMenu)) {
                            try {
                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) == null
                                    || Slot.byIndex(var0.part) == null
                                    || var0.inventorySlot < 0
                                    || var0.inventorySlot >= 36
                                    || var0.revision < 0) {
                                    return;
                                }

                                Integer var3 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var2);
                                if (var3 != null && var2.tickCount - var3 < 4) {
                                    return;
                                }

                                NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var2, var2.tickCount);
                                ItemStack var4 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var2);
                                ItemStack var5 = var2.getInventory().getItem(var0.inventorySlot);
                                if (!var0.weapon.equals(FoodWeaponLifecycle.weaponId(var4))
                                    || var0.revision != SwordCavityBuild.revision(var4)
                                    || !var0.offering.equals(BuiltInRegistries.ITEM.getKey(var5.getItem()))) {
                                    return;
                                }

                                SwordCavityBuild.replace(var2, Slot.byIndex(var0.part), var0.inventorySlot, var0.revision);
                            } finally {
                                var2.getInventory().setChanged();
                                var2.inventoryMenu.broadcastChanges();
                                if (var2.containerMenu != var2.inventoryMenu) {
                                    var2.containerMenu.broadcastChanges();
                                }

                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) != null) {
                                    StatueMenuNetwork.open(var2, var0.pos);
                                }

                                NativeStatueCavityNetwork.send(var2, new NativeStatueCavityNetwork.ActionResult(var0.pos));
                            }
                        }
                    }
                );
        }
    }

    public static record CavityLayoutAction(BlockPos pos, int action, int from, int to, UUID weapon, int revision) {
        public static final int ACTION_MOVE = 0;
        public static final int ACTION_REMOVE = 1;

        public static boolean send(BlockPos var0, int var1, Slot var2, Slot var3, ItemStack var4) {
            UUID var5 = FoodWeaponLifecycle.weaponId(var4);
            if (var5 != null && var2 != null && var3 != null && (var1 == 0 || var1 == 1)) {
                if (var1 == 0 ? SwordCavityBuild.canMove(var4, var2, var3) : SwordCavityBuild.canRemove(var4, var2)) {
                    NativeStatueCavityNetwork.send(
                            new NativeStatueCavityNetwork.CavityLayoutAction(var0, var1, var2.ordinal(), var3.ordinal(), var5, SwordCavityBuild.revision(var4))
                        );
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        private static void encode(NativeStatueCavityNetwork.CavityLayoutAction var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeVarInt(var0.action);
            var1.writeVarInt(var0.from);
            var1.writeVarInt(var0.to);
            var1.writeUUID(var0.weapon);
            var1.writeVarInt(var0.revision);
        }

        private static NativeStatueCavityNetwork.CavityLayoutAction decode(FriendlyByteBuf var0) {
            return new NativeStatueCavityNetwork.CavityLayoutAction(
                var0.readBlockPos(), var0.readVarInt(), var0.readVarInt(), var0.readVarInt(), var0.readUUID(), var0.readVarInt()
            );
        }

        private static void handle(NativeStatueCavityNetwork.CavityLayoutAction var0, IPayloadContext var1) {
            var1
                .enqueueWork(
                    () -> {
                        ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
                        if (var2 != null && !(var2.containerMenu instanceof SwordCavityMenu)) {
                            try {
                                Slot var3 = Slot.byIndex(var0.from);
                                Slot var4 = Slot.byIndex(var0.to);
                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) == null
                                    || var3 == null
                                    || var4 == null
                                    || var0.revision < 0
                                    || var0.action != 0 && var0.action != 1) {
                                    return;
                                }

                                Integer var5 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var2);
                                if (var5 != null && var2.tickCount - var5 < 4) {
                                    return;
                                }

                                NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var2, var2.tickCount);
                                ItemStack var6 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var2);
                                if (!var0.weapon.equals(FoodWeaponLifecycle.weaponId(var6)) || var0.revision != SwordCavityBuild.revision(var6)) {
                                    return;
                                }

                                if (var0.action == 0) {
                                    SwordCavityBuild.move(var2, var3, var4, var0.revision);
                                } else {
                                    SwordCavityBuild.remove(var2, var3, var0.revision);
                                }
                            } finally {
                                var2.getInventory().setChanged();
                                var2.inventoryMenu.broadcastChanges();
                                if (var2.containerMenu != var2.inventoryMenu) {
                                    var2.containerMenu.broadcastChanges();
                                }

                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) != null) {
                                    StatueMenuNetwork.open(var2, var0.pos);
                                }

                                NativeStatueCavityNetwork.send(var2, new NativeStatueCavityNetwork.ActionResult(var0.pos));
                            }
                        }
                    }
                );
        }
    }

    public static record ActionResult(BlockPos pos) {
        private static void encode(NativeStatueCavityNetwork.ActionResult var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
        }

        private static NativeStatueCavityNetwork.ActionResult decode(FriendlyByteBuf var0) {
            return new NativeStatueCavityNetwork.ActionResult(var0.readBlockPos());
        }

    }

    public static record CavityOpenAction(BlockPos pos, UUID weapon, int revision) {
        public static boolean send(BlockPos var0, ItemStack var1) {
            UUID var2 = FoodWeaponLifecycle.weaponId(var1);
            if (var2 != null && SwordCavityBuild.applies(var1)) {
                NativeStatueCavityNetwork.send(new NativeStatueCavityNetwork.CavityOpenAction(var0, var2, SwordCavityBuild.revision(var1)));
                return true;
            } else {
                return false;
            }
        }

        private static void encode(NativeStatueCavityNetwork.CavityOpenAction var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeUUID(var0.weapon);
            var1.writeVarInt(var0.revision);
        }

        private static NativeStatueCavityNetwork.CavityOpenAction decode(FriendlyByteBuf var0) {
            return new NativeStatueCavityNetwork.CavityOpenAction(var0.readBlockPos(), var0.readUUID(), var0.readVarInt());
        }

        private static void handle(NativeStatueCavityNetwork.CavityOpenAction var0, IPayloadContext var1) {
            var1
                .enqueueWork(
                    () -> {
                        ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
                        if (var2 != null && var2.containerMenu == var2.inventoryMenu && !var2.isSleeping() && !var2.isUsingItem()) {
                            BlockPos var3 = NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos);
                            ItemStack var4 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var2);
                            if (var3 != null
                                && SwordCavityBuild.applies(var4)
                                && var4.getCount() == 1
                                && var0.revision >= 0
                                && var0.weapon.equals(FoodWeaponLifecycle.weaponId(var4))
                                && var0.revision == SwordCavityBuild.revision(var4)) {
                                int var5 = var2.getMainHandItem() == var4 ? var2.getInventory().selected : 40;
                                SwordNativeMaterials.capture(var4, var2.serverLevel());
                                var2.inventoryMenu.broadcastChanges();
                                int[] var6 = SwordCavityExtensions.layout(var4);
                                var2.openMenu(
                                    new SimpleMenuProvider(
                                        (var4x, var5x, var6x) -> new SwordCavityMenu(var4x, var5x, var3, var5, var0.weapon, var6),
                                        Component.translatable("screen.ftboss.cavity.title")
                                    ),
                                    var4x -> {
                                        var4x.writeBlockPos(var3);
                                        var4x.writeVarInt(var5);
                                        var4x.writeUUID(var0.weapon);

                                        for (int var8 : var6) {
                                            var4x.writeVarInt(var8);
                                        }
                                    }
                                );
                            }
                        }
                    }
                );
        }
    }
    public static record OrdinaryCavityAction(BlockPos pos, int part, int inventorySlot, UUID weapon, int revision, ResourceLocation offering) {
        public static boolean send(BlockPos var0, Slot var1, int var2, ItemStack var3, ItemStack var4) {
            UUID var5 = FoodWeaponLifecycle.weaponId(var3);
            ResourceLocation var6 = BuiltInRegistries.ITEM.getKey(var4.getItem());
            if (var5 != null
                && var6 != null
                && var2 >= 0
                && var2 < 36
                && !var4.isEmpty()
                && SwordCavityBuild.canReplace(var3, var1, WeaponMaterialBuild.materialFor(var4))) {
                NativeStatueCavityNetwork.send(new NativeStatueCavityNetwork.OrdinaryCavityAction(var0, var1.ordinal(), var2, var5, SwordCavityBuild.revision(var3), var6));
                return true;
            } else {
                return false;
            }
        }

        private static void encode(NativeStatueCavityNetwork.OrdinaryCavityAction var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeVarInt(var0.part);
            var1.writeVarInt(var0.inventorySlot);
            var1.writeUUID(var0.weapon);
            var1.writeVarInt(var0.revision);
            var1.writeResourceLocation(var0.offering);
        }

        private static NativeStatueCavityNetwork.OrdinaryCavityAction decode(FriendlyByteBuf var0) {
            return new NativeStatueCavityNetwork.OrdinaryCavityAction(
                var0.readBlockPos(), var0.readVarInt(), var0.readVarInt(), var0.readUUID(), var0.readVarInt(), var0.readResourceLocation()
            );
        }

        private static void handle(NativeStatueCavityNetwork.OrdinaryCavityAction var0, IPayloadContext var1) {
            var1
                .enqueueWork(
                    () -> {
                        ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
                        if (var2 != null && !(var2.containerMenu instanceof SwordCavityMenu)) {
                            try {
                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) == null
                                    || Slot.byIndex(var0.part) == null
                                    || var0.inventorySlot < 0
                                    || var0.inventorySlot >= 36
                                    || var0.revision < 0) {
                                    return;
                                }

                                Integer var3 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var2);
                                if (var3 != null && var2.tickCount - var3 < 4) {
                                    return;
                                }

                                NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var2, var2.tickCount);
                                ItemStack var4 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var2);
                                ItemStack var5 = var2.getInventory().getItem(var0.inventorySlot);
                                if (!var0.weapon.equals(FoodWeaponLifecycle.weaponId(var4))
                                    || var0.revision != SwordCavityBuild.revision(var4)
                                    || !var0.offering.equals(BuiltInRegistries.ITEM.getKey(var5.getItem()))) {
                                    return;
                                }

                                SwordCavityBuild.replace(var2, Slot.byIndex(var0.part), var0.inventorySlot, var0.revision);
                            } finally {
                                var2.getInventory().setChanged();
                                var2.inventoryMenu.broadcastChanges();
                                if (var2.containerMenu != var2.inventoryMenu) {
                                    var2.containerMenu.broadcastChanges();
                                }

                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) != null) {
                                    StatueMenuNetwork.openOrdinary(var2, var0.pos);
                                }

                                NativeStatueCavityNetwork.send(var2, new NativeStatueCavityNetwork.ActionResult(var0.pos));
                            }
                        }
                    }
                );
        }
    }

    public static record OrdinaryCavityLayoutAction(BlockPos pos, int action, int from, int to, UUID weapon, int revision) {
        public static final int ACTION_MOVE = 0;
        public static final int ACTION_REMOVE = 1;

        public static boolean send(BlockPos var0, int var1, Slot var2, Slot var3, ItemStack var4) {
            UUID var5 = FoodWeaponLifecycle.weaponId(var4);
            if (var5 != null && var2 != null && var3 != null && (var1 == 0 || var1 == 1)) {
                if (var1 == 0 ? SwordCavityBuild.canMove(var4, var2, var3) : SwordCavityBuild.canRemove(var4, var2)) {
                    NativeStatueCavityNetwork.send(
                            new NativeStatueCavityNetwork.OrdinaryCavityLayoutAction(var0, var1, var2.ordinal(), var3.ordinal(), var5, SwordCavityBuild.revision(var4))
                        );
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        private static void encode(NativeStatueCavityNetwork.OrdinaryCavityLayoutAction var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeVarInt(var0.action);
            var1.writeVarInt(var0.from);
            var1.writeVarInt(var0.to);
            var1.writeUUID(var0.weapon);
            var1.writeVarInt(var0.revision);
        }

        private static NativeStatueCavityNetwork.OrdinaryCavityLayoutAction decode(FriendlyByteBuf var0) {
            return new NativeStatueCavityNetwork.OrdinaryCavityLayoutAction(
                var0.readBlockPos(), var0.readVarInt(), var0.readVarInt(), var0.readVarInt(), var0.readUUID(), var0.readVarInt()
            );
        }

        private static void handle(NativeStatueCavityNetwork.OrdinaryCavityLayoutAction var0, IPayloadContext var1) {
            var1
                .enqueueWork(
                    () -> {
                        ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
                        if (var2 != null && !(var2.containerMenu instanceof SwordCavityMenu)) {
                            try {
                                Slot var3 = Slot.byIndex(var0.from);
                                Slot var4 = Slot.byIndex(var0.to);
                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) == null
                                    || var3 == null
                                    || var4 == null
                                    || var0.revision < 0
                                    || var0.action != 0 && var0.action != 1) {
                                    return;
                                }

                                Integer var5 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var2);
                                if (var5 != null && var2.tickCount - var5 < 4) {
                                    return;
                                }

                                NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var2, var2.tickCount);
                                ItemStack var6 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var2);
                                if (!var0.weapon.equals(FoodWeaponLifecycle.weaponId(var6)) || var0.revision != SwordCavityBuild.revision(var6)) {
                                    return;
                                }

                                if (var0.action == 0) {
                                    SwordCavityBuild.move(var2, var3, var4, var0.revision);
                                } else {
                                    SwordCavityBuild.remove(var2, var3, var0.revision);
                                }
                            } finally {
                                var2.getInventory().setChanged();
                                var2.inventoryMenu.broadcastChanges();
                                if (var2.containerMenu != var2.inventoryMenu) {
                                    var2.containerMenu.broadcastChanges();
                                }

                                if (NativeStatueCavityNetwork.validatedStatuePos(var2, var0.pos) != null) {
                                    StatueMenuNetwork.openOrdinary(var2, var0.pos);
                                }

                                NativeStatueCavityNetwork.send(var2, new NativeStatueCavityNetwork.ActionResult(var0.pos));
                            }
                        }
                    }
                );
        }
    }

}
