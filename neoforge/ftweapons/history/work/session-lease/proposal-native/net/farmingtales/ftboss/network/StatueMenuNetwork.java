package net.farmingtales.ftboss.network;

import java.util.ArrayList;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.farmingtales.ftboss.item.CataclysmCavityAccess;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.minecraft.core.registries.BuiltInRegistries;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.item.StatueRepairMutation.LegacyCompensation;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.boss.BossChallengePreference;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.StatueDailyView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class StatueMenuNetwork {
    public static final int ACTION_TOGGLE_COLOR = 0;
    public static final int ACTION_REFRESH_EVOLUTION = 1;
    public static final int ACTION_INSERT_SOCKET = 2;
    public static final int ACTION_REMOVE_SOCKET = 3;
    public static final int ACTION_SUBMIT_DAILY = 4;
    public static final int ACTION_EVOLVE_WEAPON = 5;
    public static final int ACTION_CLAIM_EVOLUTION = 6;
    public static final int ACTION_APPLY_FOOD_BLESSING = 7;
    public static final int ACTION_SET_CHALLENGE_DIFFICULTY = 8;
    public static final int ACTION_SELECT_GOURMET_SKILL = 9;
    private static final int MAX_DAILY_TASKS = 8;
    public static final Type<StatueMenuNetwork.Open> TYPE = StatueMenuNetwork.Open.TYPE;

    private StatueMenuNetwork() {
    }

    public static void open(ServerPlayer var0, BlockPos var1) {
        if (var0 != null && var1 != null) {
            BlockPos var2 = lowerPos(var0.level().getBlockState(var1), var1);
            BlockState var3 = var0.level().getBlockState(var2);
            if (var3.getBlock() instanceof CandyStatueBlock var4) {
                boolean var11 = CandyStatueBlock.isGoldenAppearance(var3);
                boolean var6 = CandyStatueBlock.isGoldenUnlocked(var3);
                List var7 = FoodWeaponEvolution.statueDailyViews(var0, var4.ownTint(), var6);
                boolean var8 = ManorDifficulty.unlocked(var0);
                boolean var9 = var6 || var8;
                boolean var10 = BossChallengePreference.selected(var0) == ManorDifficulty.IMPOSSIBLE;
                PacketDistributor.sendToPlayer(
                    var0,
                    new StatueMenuNetwork.Open(
                        var2,
                        var4.ownTint(),
                        CandyStatueBlock.isWhite(var3),
                        var11,
                        var9,
                        var8,
                        var10,
                        Component.translatable(var3.getBlock().getDescriptionId()),
                        var7
                    )
                );
            }
        }
    }

    private static BlockPos lowerPos(BlockState var0, BlockPos var1) {
        return var0.getBlock() instanceof CandyStatueBlock && var0.getValue(CandyStatueBlock.HALF) == DoubleBlockHalf.UPPER ? var1.below() : var1;
    }

    private static BlockPos validatedStatuePos(ServerPlayer var0, BlockPos var1) {
        if (var0 != null && var1 != null) {
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

    private static int statueTint(ServerPlayer var0, BlockPos var1) {
        BlockPos var2 = validatedStatuePos(var0, var1);
        if (var2 == null) {
            return 0;
        } else {
            BlockState var3 = var0.level().getBlockState(var2);
            return var3.getBlock() instanceof CandyStatueBlock var4 ? var4.ownTint() : 0;
        }
    }

    private static boolean golden(ServerPlayer var0, BlockPos var1) {
        BlockPos var2 = validatedStatuePos(var0, var1);
        return var2 != null && CandyStatueBlock.isGoldenUnlocked(var0.level().getBlockState(var2));
    }

    private static void toggleColor(ServerPlayer var0, BlockPos var1) {
        BlockPos var2 = validatedStatuePos(var0, var1);
        if (var2 != null) {
            BlockState var3 = var0.level().getBlockState(var2);
            if (var3.getBlock() instanceof CandyStatueBlock var4) {
                boolean var11 = CandyStatueBlock.isWhite(var3);
                boolean var6 = CandyStatueBlock.isGoldenAppearance(var3);
                boolean var7 = CandyStatueBlock.isGoldenUnlocked(var3);
                boolean var8 = var7 || ManorDifficulty.unlocked(var0);
                boolean var9;
                boolean var10;
                if (var6) {
                    var9 = false;
                    var10 = false;
                } else if (var11 && var8) {
                    var9 = false;
                    var10 = true;
                } else if (var11) {
                    var9 = false;
                    var10 = false;
                } else {
                    var9 = true;
                    var10 = false;
                }

                setAppearance(var0, var2, var3, var4, var9, var10, var7);
                var0.level().playSound(null, var2, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, var10 ? 1.6F : (var9 ? 0.9F : 1.3F));
                PacketDistributor.sendToPlayer(var0, new StatueMenuNetwork.ColorState(var2, var9, var10));
            }
        }
    }

    private static void setAppearance(ServerPlayer var0, BlockPos var1, BlockState var2, CandyStatueBlock var3, boolean var4, boolean var5, boolean var6) {
        BlockState var7 = var2.setValue(CandyStatueBlock.WHITE, Boolean.valueOf(var4))
            .setValue(CandyStatueBlock.GOLDEN, Boolean.valueOf(var5))
            .setValue(CandyStatueBlock.GOLDEN_UNLOCKED, Boolean.valueOf(var6))
            .setValue(CandyStatueBlock.TINT, Integer.valueOf(appearanceTint(var3, var4, var5, var6)));
        var0.level().setBlock(var1, var7, 3);
        BlockPos var8 = var1.above();
        BlockState var9 = var0.level().getBlockState(var8);
        if (var9.getBlock() == var2.getBlock()) {
            var0.level()
                .setBlock(
                    var8,
                    var9.setValue(CandyStatueBlock.WHITE, Boolean.valueOf(var4))
                        .setValue(CandyStatueBlock.GOLDEN, Boolean.valueOf(var5))
                        .setValue(CandyStatueBlock.GOLDEN_UNLOCKED, Boolean.valueOf(var6))
                        .setValue(CandyStatueBlock.TINT, Integer.valueOf(appearanceTint(var3, var4, var5, var6))),
                    3
                );
        }
    }

    private static int appearanceTint(CandyStatueBlock var0, boolean var1, boolean var2, boolean var3) {
        return !var1 && (!var2 || var3) ? var0.ownTint() : 0;
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST,
        receiveCanceled = true
    )
    public static void rightClickStatue(RightClickBlock var0) {
        if (var0.getHand() == InteractionHand.MAIN_HAND && var0.getLevel().getBlockState(var0.getPos()).getBlock() instanceof CandyStatueBlock) {
            var0.setCanceled(true);
            var0.setCancellationResult(InteractionResult.SUCCESS);
            if (!var0.getLevel().isClientSide && var0.getEntity() instanceof ServerPlayer var1) {
                open(var1, var0.getPos());
            }
        }
    }

    public static record Action(BlockPos pos, int action, int value) implements CustomPacketPayload {
        public static final Type<StatueMenuNetwork.Action> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "statue_hub_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatueMenuNetwork.Action> STREAM_CODEC = StreamCodec.ofMember(
            StatueMenuNetwork.Action::write, StatueMenuNetwork.Action::read
        );

        public Action(BlockPos var1, int var2) {
            this(var1, var2, 0);
        }

        public static void send(BlockPos var0, int var1) {
            send(var0, var1, 0);
        }

        public static void send(BlockPos var0, int var1, int var2) {
            if (var1 == 7) return;
            PacketDistributor.sendToServer(new StatueMenuNetwork.Action(var0, var1, var2));
        }

        private static void write(StatueMenuNetwork.Action var0, RegistryFriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos).writeVarInt(var0.action).writeVarInt(var0.value);
        }

        private static StatueMenuNetwork.Action read(RegistryFriendlyByteBuf var0) {
            return new StatueMenuNetwork.Action(var0.readBlockPos(), var0.readVarInt(), var0.readVarInt());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(StatueMenuNetwork.Action var0, IPayloadContext var1) {
            if (var0.action == 7) return;
            if (var1.player() instanceof ServerPlayer var2) {
                if (var0.action == 0) {
                    StatueMenuNetwork.toggleColor(var2, var0.pos);
                } else if (StatueMenuNetwork.validatedStatuePos(var2, var0.pos) != null) {
                    int var5 = StatueMenuNetwork.statueTint(var2, var0.pos);
                    boolean var4 = StatueMenuNetwork.golden(var2, var0.pos);
                    if (var0.action == 2) {
                        FoodWeaponEvolution.insertSocketFromUi(var2, var5, var0.value);
                    } else if (var0.action == 3) {
                        FoodWeaponEvolution.removeSocketFromUi(var2, var5, var0.value);
                    } else if (var0.action == 4) {
                        FoodWeaponEvolution.submitDailyFromUi(var2, var5, var0.value, var4);
                    } else if (var0.action == 5) {
                        FoodWeaponEvolution.evolveWeaponFromUi(var2, var5, var4);
                    } else if (var0.action == 6) {
                        FoodWeaponEvolution.claimEvolutionFromUi(var2, var5, var4);
                    } else if (var0.action == 9) {
                        FoodWeaponEvolution.selectGourmetSkillFromUi(var2, var0.value);
                    } else if (var0.action == 8) {
                        BossChallengePreference.select(var2, var0.value == 1 ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL);
                    }

                    StatueMenuNetwork.open(var2, var0.pos);
                }
            }
        }
    }

    public static record ColorState(BlockPos pos, boolean white, boolean gold) implements CustomPacketPayload {
        public static final Type<StatueMenuNetwork.ColorState> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "statue_hub_color"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatueMenuNetwork.ColorState> STREAM_CODEC = StreamCodec.ofMember(
            StatueMenuNetwork.ColorState::write, StatueMenuNetwork.ColorState::read
        );

        private static void write(StatueMenuNetwork.ColorState var0, RegistryFriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos).writeBoolean(var0.white).writeBoolean(var0.gold);
        }

        private static StatueMenuNetwork.ColorState read(RegistryFriendlyByteBuf var0) {
            return new StatueMenuNetwork.ColorState(var0.readBlockPos(), var0.readBoolean(), var0.readBoolean());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(StatueMenuNetwork.ColorState var0, IPayloadContext var1) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                StatueHubScreen.updateAppearance(var0.pos, var0.white, var0.gold);
                net.farmingtales.ftboss.client.OrdinaryStatueHubScreen.updateAppearance(var0.pos, var0.white, var0.gold);
            }
        }
    }

    public static record Open(
        BlockPos pos,
        int tint,
        boolean white,
        boolean gold,
        boolean goldAvailable,
        boolean finaleUnlocked,
        boolean finaleSelected,
        Component statueName,
        List<StatueDailyView> dailyTasks
    ) implements CustomPacketPayload {
        public static final Type<StatueMenuNetwork.Open> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "statue_hub_open"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatueMenuNetwork.Open> STREAM_CODEC = StreamCodec.ofMember(
            StatueMenuNetwork.Open::write, StatueMenuNetwork.Open::read
        );

        public Open(
            BlockPos pos,
            int tint,
            boolean white,
            boolean gold,
            boolean goldAvailable,
            boolean finaleUnlocked,
            boolean finaleSelected,
            Component statueName,
            List<StatueDailyView> dailyTasks
        ) {
            dailyTasks = List.copyOf(dailyTasks);
            this.pos = pos;
            this.tint = tint;
            this.white = white;
            this.gold = gold;
            this.goldAvailable = goldAvailable;
            this.finaleUnlocked = finaleUnlocked;
            this.finaleSelected = finaleSelected;
            this.statueName = statueName;
            this.dailyTasks = dailyTasks;
        }

        private static void write(StatueMenuNetwork.Open var0, RegistryFriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeVarInt(var0.tint);
            var1.writeBoolean(var0.white);
            var1.writeBoolean(var0.gold);
            var1.writeBoolean(var0.goldAvailable);
            var1.writeBoolean(var0.finaleUnlocked);
            var1.writeBoolean(var0.finaleSelected);
            ComponentSerialization.STREAM_CODEC.encode(var1, var0.statueName);
            var1.writeVarInt(var0.dailyTasks.size());

            for (StatueDailyView var3 : var0.dailyTasks) {
                ComponentSerialization.STREAM_CODEC.encode(var1, var3.request());
                var1.writeUtf(var3.requestItemId());
                var1.writeVarInt(var3.progress());
                var1.writeVarInt(var3.required());
                ComponentSerialization.STREAM_CODEC.encode(var1, var3.reward());
                var1.writeByte(var3.state());
            }
        }

        private static StatueMenuNetwork.Open read(RegistryFriendlyByteBuf var0) {
            BlockPos var1 = var0.readBlockPos();
            int var2 = var0.readVarInt();
            boolean var3 = var0.readBoolean();
            boolean var4 = var0.readBoolean();
            boolean var5 = var0.readBoolean();
            boolean var6 = var0.readBoolean();
            boolean var7 = var0.readBoolean();
            Component var8 = ComponentSerialization.STREAM_CODEC.decode(var0);
            int var9 = Math.max(0, var0.readVarInt());
            int var10 = Math.min(8, var9);
            ArrayList var11 = new ArrayList(var10);

            for (int var12 = 0; var12 < var9; var12++) {
                StatueDailyView var13 = new StatueDailyView(
                    ComponentSerialization.STREAM_CODEC.decode(var0),
                    var0.readUtf(),
                    var0.readVarInt(),
                    var0.readVarInt(),
                    ComponentSerialization.STREAM_CODEC.decode(var0),
                    var0.readUnsignedByte()
                );
                if (var12 < var10) {
                    var11.add(var13);
                }
            }

            return new StatueMenuNetwork.Open(var1, var2, var3, var4, var5, var6, var7, var8, var11);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(StatueMenuNetwork.Open var0, IPayloadContext var1) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                StatueHubScreen.open(var0);
            }
        }
    }

    private static void executeRepair(ServerPlayer var0, StatueMenuNetwork.RepairAction var1) {
        if (!FourWeaponFoundationAdapter.applies(FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0))) {
            if (!(var0.containerMenu instanceof SwordCavityMenu)) {
                StatueRepairMutation var2 = null;
                boolean var3 = false;
                BlockPos var4 = null;
                BlockState var5 = null;
                int var6 = -1;

                try {
                    try {
                        BlockPos var7 = NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos);

                        boolean var8 = switch (var1.action) {
                            case 13, 15 -> var1.value == 0;
                            case 14 -> {
                                if (var1.value >= 0 && var1.value < 3) {
                                    yield true;
                                }

                                yield false;
                            }
                            default -> false;
                        };
                        if (var7 == null || !var8 || var0.isSleeping() || var0.isUsingItem()) {
                            return;
                        }

                        Integer var9 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var0);
                        if (var9 != null && var0.tickCount - var9 < 4) {
                            return;
                        }

                        NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var0, var0.tickCount);
                        ItemStack var10 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
                        if (!var1.weapon.equals(FoodWeaponLifecycle.weaponId(var10)) || var1.revision < 0 || var1.revision != FoodWeaponRepair.revision(var10)) {
                            var0.displayClientMessage(Component.translatable("message.ftboss.repair.selection_changed"), true);
                            return;
                        }

                        int var11 = legacyStatueTint(var0, var7);
                        var4 = var7;
                        var5 = var0.level().getBlockState(var7);
                        var6 = var11;
                        var2 = legacyRepairMutation(var0, var10, var11, var1);
                        var3 = var2.commit();
                    } finally {
                        var0.getInventory().setChanged();
                        var0.inventoryMenu.broadcastChanges();
                        if (var0.containerMenu != var0.inventoryMenu) {
                            var0.containerMenu.broadcastChanges();
                        }

                        if (NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos) != null) {
                            open(var0, var1.pos);
                        }

                        NativeStatueCavityNetwork.send(var0, new NativeStatueCavityNetwork.ActionResult(var1.pos));
                    }

                    if (var2 != null) {
                        if (var3) {
                            if (!var4.equals(NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos))
                                || !var5.equals(var0.level().getBlockState((BlockPos)var4))
                                || var6 != legacyStatueTint(var0, (BlockPos)var4)) {
                                throw new IllegalStateException("legacy repair statue changed");
                            }

                            var2.terminalLegacy();
                        } else {
                            var2.rollback();
                        }
                    }
                } catch (Error | RuntimeException var18) {
                    if (var2 != null) {
                        try {
                            var2.rollback();
                        } catch (Error | RuntimeException var16) {
                            if (var16 != var18) {
                                var18.addSuppressed(var16);
                            }
                        }
                    }

                    throw var18;
                }
            }
        }
    }

    private static StatueRepairMutation legacyRepairMutation(
        final ServerPlayer var0, final ItemStack var1, final int var2, final StatueMenuNetwork.RepairAction var3
    ) {
        final NativeLegacyRepairData var4 = NativeLegacyRepairData.capture(var1, var0.registryAccess());
        int var5 = var0.getMainHandItem() == var1 ? var0.getInventory().selected : 40;

        Operation var6 = switch (var3.action) {
            case 13 -> Operation.START_REPAIR;
            case 14 -> Operation.SUBMIT_REPAIR;
            case 15 -> Operation.CLAIM_REPAIR;
            default -> throw new IllegalArgumentException("invalid legacy repair action");
        };
        return StatueRepairMutation.wrapLegacy(new LegacyCompensation() {
            public boolean commit() {
                return switch (var3.action) {
                    case 13 -> FoodWeaponRepair.start(var0, var1, var2);
                    case 14 -> FoodWeaponRepair.submit(var0, var1, var2, var3.value);
                    case 15 -> FoodWeaponRepair.claim(var0, var1, var2);
                    default -> false;
                };
            }

            public void rollback(boolean var1x) {
                boolean var2x = var4.changed(var1);
                if (var2x || var1x) {
                    if (var2x) {
                        var4.restore(var1);
                    }

                    var0.getInventory().setChanged();
                    var0.inventoryMenu.broadcastChanges();
                    if (var0.containerMenu != var0.inventoryMenu) {
                        var0.containerMenu.broadcastChanges();
                    }
                }
            }
        }, var0, var1, var6, var3.value, var5, NativeStatueCavityNetwork.validatedStatuePos(var0, var3.pos));
    }

    public static record RepairAction(BlockPos pos, int action, int value, UUID weapon, int revision) {
        public static void send(BlockPos var0, ItemStack var1, int var2, int var3) {
            if (FourWeaponFoundationAdapter.applies(var1)) {
                if (!FourWeaponClientApi.sendLegacyAction(var0, var1, var2, var3)) {
                    FourWeaponClientApi.rejectHubAction(var0);
                }
            } else {
                UUID var4 = FoodWeaponLifecycle.weaponId(var1);
                if (var4 != null) {
                    NativeStatueCavityNetwork.send(new StatueMenuNetwork.RepairAction(var0, var2, var3, var4, FoodWeaponRepair.revision(var1)));
                }
            }
        }

        static void encode(StatueMenuNetwork.RepairAction var0, FriendlyByteBuf var1) {
            var1.writeBlockPos(var0.pos);
            var1.writeVarInt(var0.action);
            var1.writeVarInt(var0.value);
            var1.writeUUID(var0.weapon);
            var1.writeVarInt(var0.revision);
        }

        static StatueMenuNetwork.RepairAction decode(FriendlyByteBuf var0) {
            return new StatueMenuNetwork.RepairAction(var0.readBlockPos(), var0.readVarInt(), var0.readVarInt(), var0.readUUID(), var0.readVarInt());
        }

        static void handle(StatueMenuNetwork.RepairAction var0, IPayloadContext var1) {
            ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
            if (var2 != null) {
                var1.enqueueWork(() -> StatueMenuNetwork.executeRepair(var2, var0));
            }

        }
    }

    private static int legacyStatueTint(ServerPlayer var0, BlockPos var1) {
        BlockPos var2 = NativeStatueCavityNetwork.validatedStatuePos(var0, var1);
        if (var2 == null) {
            return 0;
        } else {
            BlockState var3 = var0.level().getBlockState(var2);
            return var3.getBlock() instanceof CandyStatueBlock var4 ? var4.ownTint() : 0;
        }
    }

    private static void selectSkill(ServerPlayer var0, StatueMenuNetwork.SkillSelection var1) {
        ItemStack var2 = var0.getMainHandItem();
        Integer var3 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var0);
        boolean var4 = var0.isAlive()
            && !var0.isSpectator()
            && !var0.isSleeping()
            && !var0.isUsingItem()
            && var1.hotbarSlot >= 0
            && var1.hotbarSlot < 9
            && var0.getInventory().selected == var1.hotbarSlot
            && var1.index >= 0
            && var1.index < 8
            && var1.level == FoodWeaponEvolution.evolutionLevel(var2)
            && var1.item.equals(BuiltInRegistries.ITEM.getKey(var2.getItem()))
            && var1.profile.equals(FoodWeaponEvolution.firstProfileId(var2))
            && (var3 == null || var0.tickCount - var3 >= 4);
        if (var4) {
            NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var0, var0.tickCount);
            var4 = FoodWeaponSkillLoadout.select(var0, var1.index, true);
        }

        if (!var4) {
            var0.displayClientMessage(Component.translatable("message.ftboss.skills.selection_changed"), true);
        }

        var0.getInventory().setChanged();
        var0.inventoryMenu.broadcastChanges();
        if (var0.containerMenu != var0.inventoryMenu) {
            var0.containerMenu.broadcastChanges();
        }
    }

    public static record SkillSelection(ResourceLocation item, int hotbarSlot, int index, int level, String profile) {
        public static void send(ItemStack var0, int var1, int var2) {
            ResourceLocation var3 = BuiltInRegistries.ITEM.getKey(var0.getItem());
            if (var3 != null) {
                NativeStatueCavityNetwork
                    .send(
                        new StatueMenuNetwork.SkillSelection(
                            var3, var1, var2, FoodWeaponEvolution.evolutionLevel(var0), FoodWeaponEvolution.firstProfileId(var0)
                        )
                    );
            }
        }

        static void encode(StatueMenuNetwork.SkillSelection var0, FriendlyByteBuf var1) {
            var1.writeResourceLocation(var0.item);
            var1.writeVarInt(var0.hotbarSlot);
            var1.writeVarInt(var0.index);
            var1.writeVarInt(var0.level);
            var1.writeUtf(var0.profile, 32);
        }

        static StatueMenuNetwork.SkillSelection decode(FriendlyByteBuf var0) {
            return new StatueMenuNetwork.SkillSelection(var0.readResourceLocation(), var0.readVarInt(), var0.readVarInt(), var0.readVarInt(), var0.readUtf(32));
        }

        static void handle(StatueMenuNetwork.SkillSelection var0, IPayloadContext var1) {
            ServerPlayer var2 = var1.player() instanceof ServerPlayer player ? player : null;
            if (var2 != null) {
                var1.enqueueWork(() -> StatueMenuNetwork.selectSkill(var2, var0));
            }

        }
    }

    private static boolean validActionValue(StatueMenuNetwork.OrdinaryAction var0) {
        return switch (var0.action) {
            case 0, 1, 6, 11 -> var0.value == 0;
            case 2, 4, 7 -> var0.value >= 0 && (var0.value < 36 || var0.value == 40);
            case 3 -> var0.value >= 0 && var0.value < 30;
            case 5 -> var0.value >= 0 && var0.value < 36;
            case 8 -> var0.value == 0 || var0.value == 1;
            case 9, 12 -> var0.value >= 0 && var0.value < 8;
            case 10 -> var0.value >= 0 && var0.value < 108;
            default -> false;
        };
    }

    private static void executeAction(ServerPlayer var0, StatueMenuNetwork.OrdinaryAction var1) {
        if (!(var0.containerMenu instanceof SwordCavityMenu)) {
            try {
                BlockPos var2 = NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos);
                if (var2 != null && validActionValue(var1)) {
                    if (var1.action != 1) {
                        Integer var3 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var0);
                        if (var3 != null && var0.tickCount - var3 < 4) {
                            return;
                        }

                        NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var0, var0.tickCount);
                    }

                    int var8 = statueTint(var0, var2);
                    switch (var1.action) {
                        case 0:
                            toggleColor(var0, var2);
                            return;
                        case 1:
                        default:
                            return;
                        case 2:
                            FoodWeaponEvolution.ftboss$legacyOrdinary$insertSocketFromUi(var0, var8, var1.value);
                            return;
                        case 3:
                            FoodWeaponEvolution.ftboss$legacyOrdinary$removeSocketFromUi(var0, var8, var1.value);
                            return;
                        case 4:
                            FoodWeaponEvolution.ftboss$legacyOrdinary$submitDailyFromUi(var0, var8, var1.value);
                            return;
                        case 5:
                            FoodWeaponEvolution.evolveWeaponFromUi(var0, var8, var1.value);
                            return;
                        case 6:
                            FoodWeaponEvolution.ftboss$legacyOrdinary$claimEvolutionFromUi(var0, var8);
                            return;
                        case 8:
                            boolean var4 = BossChallengePreference.select(var0, var1.value == 1 ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL);
                            if (var4 && var1.value == 1) {
                                unlockGoldAppearance(var0, var1.pos);
                            }

                            return;
                        case 9:
                            FoodWeaponSkillLoadout.select(var0, var1.value, false);
                            return;
                        case 10:
                            WeaponMaterialBuild.replaceFromUi(var0, var1.value);
                            return;
                        case 11:
                            if (FoodWeaponSkillLoadout.bindFromUi(var0, var8)) {
                                SwordNativeMaterials.capture(FoodWeaponEvolution.ftboss$legacyOrdinary$heldEvolutionWeapon(var0), var0.serverLevel());
                            }

                            return;
                        case 12:
                            FoodWeaponSkillLoadout.toggleFromUi(var0, var1.value);
                    }
                }
            } finally {
                var0.getInventory().setChanged();
                var0.inventoryMenu.broadcastChanges();
                if (var0.containerMenu != var0.inventoryMenu) {
                    var0.containerMenu.broadcastChanges();
                }

                if (NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos) != null) {
                    openOrdinary(var0, var1.pos);
                }

                NativeStatueCavityNetwork.send(var0, new NativeStatueCavityNetwork.ActionResult(var1.pos));
            }
        }
    }

    private static void unlockGoldAppearance(ServerPlayer var0, BlockPos var1) {
        BlockPos pos = NativeStatueCavityNetwork.validatedStatuePos(var0, var1);
        if (pos != null && ManorDifficulty.unlocked(var0)) {
            BlockState lower = var0.level().getBlockState(pos);
            if (lower.getBlock() instanceof CandyStatueBlock statue) {
                setAppearance(var0, pos, lower, statue, false, true, CandyStatueBlock.isGoldenUnlocked(lower));
            }
        }
    }

    public static record OrdinaryAction(BlockPos pos, int action, int value) {
        public OrdinaryAction(BlockPos pos, int action) {
            this(pos, action, 0);
        }

        public static void send(BlockPos pos, int action) {
            send(pos, action, 0);
        }

        public static void send(BlockPos pos, int action, int value) {
            if (action == 7) return;
            NativeStatueCavityNetwork.send(new OrdinaryAction(pos, action, value));
        }

        static void encode(OrdinaryAction packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeVarInt(packet.action);
            buffer.writeVarInt(packet.value);
        }

        static OrdinaryAction decode(FriendlyByteBuf buffer) {
            return new OrdinaryAction(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt());
        }

        static void handle(OrdinaryAction packet, IPayloadContext context) {
            ServerPlayer player = context.player() instanceof ServerPlayer sender ? sender : null;
            if (player != null) {
                context.enqueueWork(() -> StatueMenuNetwork.executeAction(player, packet));
            }
        }
    }


    public static void openOrdinary(ServerPlayer var0, BlockPos var1) {
            if (var0 != null && var1 != null) {
                BlockPos var2 = lowerPos(var0.level().getBlockState(var1), var1);
                BlockState var3 = var0.level().getBlockState(var2);
                if (var3.getBlock() instanceof CandyStatueBlock var4) {
                    FoodWeaponLifecycle.ensureTracked(FoodWeaponEvolution.ftboss$legacyOrdinary$heldEvolutionWeapon(var0));
                    FoundationEntryPoints.seedFromStatue(var0, var0.getMainHandItem(), var2, FourWeaponTransactionNetwork.negotiated(var0));
                    FoundationEntryPoints.seedFromStatue(var0, var0.getOffhandItem(), var2, FourWeaponTransactionNetwork.negotiated(var0));
                    CataclysmCavityAccess.seedFromStatue(var0, var0.getMainHandItem(), var2);
                    CataclysmCavityAccess.seedFromStatue(var0, var0.getOffhandItem(), var2);
                    var0.getInventory().setChanged();
                    var0.inventoryMenu.broadcastChanges();
                    if (var0.containerMenu != var0.inventoryMenu) {
                        var0.containerMenu.broadcastChanges();
                    }
    
                    List var8 = FoodWeaponEvolution.ftboss$legacyOrdinary$statueDailyViews(var0, var4.ownTint());
                    boolean var6 = ManorDifficulty.unlocked(var0);
                    boolean var7 = BossChallengePreference.selected(var0) == ManorDifficulty.IMPOSSIBLE;
                    NativeStatueCavityNetwork.send(
                        var0,
                        new StatueMenuNetwork.OrdinaryOpen(
                            var2,
                            var4.ownTint(),
                            CandyStatueBlock.isWhite(var3),
                            CandyStatueBlock.isGoldenAppearance(var3),
                            var6,
                            var7,
                            Component.translatable(var3.getBlock().getDescriptionId()),
                            var8
                        )
                    );
                }
            }
        }

    public static record OrdinaryOpen(
            BlockPos pos,
            int tint,
            boolean white,
            boolean gold,
            boolean finaleUnlocked,
            boolean finaleSelected,
            Component statueName,
            List<StatueDailyView> dailyTasks
        ) {
            public OrdinaryOpen(
                BlockPos pos,
                int tint,
                boolean white,
                boolean gold,
                boolean finaleUnlocked,
                boolean finaleSelected,
                Component statueName,
                List<StatueDailyView> dailyTasks
            ) {
                dailyTasks = List.copyOf(dailyTasks);
                this.pos = pos;
                this.tint = tint;
                this.white = white;
                this.gold = gold;
                this.finaleUnlocked = finaleUnlocked;
                this.finaleSelected = finaleSelected;
                this.statueName = statueName;
                this.dailyTasks = dailyTasks;
            }
    
            static void encode(StatueMenuNetwork.OrdinaryOpen packet, RegistryFriendlyByteBuf buffer) {
                buffer.writeBlockPos(packet.pos);
                buffer.writeVarInt(packet.tint);
                buffer.writeBoolean(packet.white);
                buffer.writeBoolean(packet.gold);
                buffer.writeBoolean(packet.finaleUnlocked);
                buffer.writeBoolean(packet.finaleSelected);
                ComponentSerialization.STREAM_CODEC.encode(buffer, packet.statueName);
                buffer.writeVarInt(packet.dailyTasks.size());
    
                for (StatueDailyView task : packet.dailyTasks) {
                    ComponentSerialization.STREAM_CODEC.encode(buffer, task.request());
                    buffer.writeUtf(task.requestItemId());
                    buffer.writeVarInt(task.progress());
                    buffer.writeVarInt(task.required());
                    ComponentSerialization.STREAM_CODEC.encode(buffer, task.reward());
                    buffer.writeByte(task.state());
                }
            }
    
            
    
            static StatueMenuNetwork.OrdinaryOpen decode(RegistryFriendlyByteBuf var0) {
                BlockPos var1 = var0.readBlockPos();
                int var2 = var0.readVarInt();
                boolean var3 = var0.readBoolean();
                boolean var4 = var0.readBoolean();
                boolean var5 = var0.readBoolean();
                boolean var6 = var0.readBoolean();
                Component var7 = ComponentSerialization.STREAM_CODEC.decode(var0);
                int var8 = var0.readVarInt();
                if (var8 >= 0 && var8 <= 12) {
                    ArrayList var9 = new ArrayList(var8);
    
                    for (int var10 = 0; var10 < var8; var10++) {
                        var9.add(
                            new StatueDailyView(
                                ComponentSerialization.STREAM_CODEC.decode(var0), var0.readUtf(), var0.readVarInt(), var0.readVarInt(), ComponentSerialization.STREAM_CODEC.decode(var0), var0.readUnsignedByte()
                            )
                        );
                    }
    
                    return new StatueMenuNetwork.OrdinaryOpen(var1, var2, var3, var4, var5, var6, var7, var9);
                } else {
                    throw new IllegalArgumentException("Invalid daily task count");
                }
            }
        }
    private static void executeOrdinaryRepair(ServerPlayer var0, StatueMenuNetwork.RepairAction var1) {
        if (!FourWeaponFoundationAdapter.applies(FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0))) {
            if (!(var0.containerMenu instanceof SwordCavityMenu)) {
                StatueRepairMutation var2 = null;
                boolean var3 = false;
                BlockPos var4 = null;
                BlockState var5 = null;
                int var6 = -1;

                try {
                    try {
                        BlockPos var7 = NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos);

                        boolean var8 = switch (var1.action) {
                            case 13, 15 -> var1.value == 0;
                            case 14 -> {
                                if (var1.value >= 0 && var1.value < 3) {
                                    yield true;
                                }

                                yield false;
                            }
                            default -> false;
                        };
                        if (var7 == null || !var8 || var0.isSleeping() || var0.isUsingItem()) {
                            return;
                        }

                        Integer var9 = NativeStatueCavityNetwork.LAST_ACTION_TICK.get(var0);
                        if (var9 != null && var0.tickCount - var9 < 4) {
                            return;
                        }

                        NativeStatueCavityNetwork.LAST_ACTION_TICK.put(var0, var0.tickCount);
                        ItemStack var10 = FoodWeaponEvolution.heldEvolutionWeaponForProgression(var0);
                        if (!var1.weapon.equals(FoodWeaponLifecycle.weaponId(var10)) || var1.revision < 0 || var1.revision != FoodWeaponRepair.revision(var10)) {
                            var0.displayClientMessage(Component.translatable("message.ftboss.repair.selection_changed"), true);
                            return;
                        }

                        int var11 = legacyStatueTint(var0, var7);
                        var4 = var7;
                        var5 = var0.level().getBlockState(var7);
                        var6 = var11;
                        var2 = legacyRepairMutation(var0, var10, var11, var1);
                        var3 = var2.commit();
                    } finally {
                        var0.getInventory().setChanged();
                        var0.inventoryMenu.broadcastChanges();
                        if (var0.containerMenu != var0.inventoryMenu) {
                            var0.containerMenu.broadcastChanges();
                        }

                        if (NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos) != null) {
                            openOrdinary(var0, var1.pos);
                        }

                        NativeStatueCavityNetwork.send(var0, new NativeStatueCavityNetwork.ActionResult(var1.pos));
                    }

                    if (var2 != null) {
                        if (var3) {
                            if (!var4.equals(NativeStatueCavityNetwork.validatedStatuePos(var0, var1.pos))
                                || !var5.equals(var0.level().getBlockState((BlockPos)var4))
                                || var6 != legacyStatueTint(var0, (BlockPos)var4)) {
                                throw new IllegalStateException("legacy repair statue changed");
                            }

                            var2.terminalLegacy();
                        } else {
                            var2.rollback();
                        }
                    }
                } catch (Error | RuntimeException var18) {
                    if (var2 != null) {
                        try {
                            var2.rollback();
                        } catch (Error | RuntimeException var16) {
                            if (var16 != var18) {
                                var18.addSuppressed(var16);
                            }
                        }
                    }

                    throw var18;
                }
            }
        }
    }

    public static record OrdinaryRepairAction(RepairAction action) {
        public OrdinaryRepairAction {
            java.util.Objects.requireNonNull(action);
        }

        public static void send(BlockPos var0, ItemStack var1, int var2, int var3) {
            if (FourWeaponFoundationAdapter.applies(var1)) {
                if (!FourWeaponClientApi.sendLegacyAction(var0, var1, var2, var3)) {
                    FourWeaponClientApi.rejectHubAction(var0);
                }
            } else {
                UUID var4 = FoodWeaponLifecycle.weaponId(var1);
                if (var4 != null) {
                    NativeStatueCavityNetwork.send(new OrdinaryRepairAction(new StatueMenuNetwork.RepairAction(var0, var2, var3, var4, FoodWeaponRepair.revision(var1))));
                }
            }
        }

        static void encode(OrdinaryRepairAction packet, FriendlyByteBuf buffer) {
            RepairAction.encode(packet.action, buffer);
        }

        static OrdinaryRepairAction decode(FriendlyByteBuf buffer) {
            return new OrdinaryRepairAction(RepairAction.decode(buffer));
        }

        static void handle(OrdinaryRepairAction packet, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                context.enqueueWork(() -> executeOrdinaryRepair(player, packet.action));
            }
        }
    }

}
