package net.farmingtales.ftboss.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.boss.BossChallengePreference;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.StatueDailyView;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

/** 石像界面与服务端动作校验；2026-10-12 增加材料替换、限流和全动作回执。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    public static final int ACTION_REPLACE_MATERIAL = 10;
    public static final int ACTION_BIND_WEAPON = 11;
    public static final int ACTION_TOGGLE_CARRIED_SKILL = 12;
    public static final int ACTION_START_REPAIR = 13;
    public static final int ACTION_SUBMIT_REPAIR = 14;
    public static final int ACTION_CLAIM_REPAIR = 15;
    private static final int MAX_DAILY_TASKS = 12;
    private static final int MIN_ACTION_INTERVAL_TICKS = 4;
    private static final Map<ServerPlayer, Integer> LAST_ACTION_TICK = new WeakHashMap<>();
    private static final String PROTOCOL = "10";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FTBossMod.MOD_ID, "statue_hub"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private StatueMenuNetwork() {}

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(StatueMenuNetwork::register);
    }

    private static synchronized void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(Open.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(Open::encode).decoder(Open::decode).consumerMainThread(Open::handle).add();
        CHANNEL.messageBuilder(Action.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Action::encode).decoder(Action::decode).consumerMainThread(Action::handle).add();
        CHANNEL.messageBuilder(ColorState.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ColorState::encode).decoder(ColorState::decode)
                .consumerMainThread(ColorState::handle).add();
        CHANNEL.messageBuilder(ActionResult.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ActionResult::encode).decoder(ActionResult::decode)
                .consumerMainThread(ActionResult::handle).add();
        CHANNEL.messageBuilder(SkillSelection.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SkillSelection::encode).decoder(SkillSelection::decode)
                .consumerMainThread(SkillSelection::handle).add();
        CHANNEL.messageBuilder(RepairAction.class, 5, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RepairAction::encode).decoder(RepairAction::decode)
                .consumerMainThread(RepairAction::handle).add();
        CHANNEL.messageBuilder(CavityAction.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CavityAction::encode).decoder(CavityAction::decode)
                .consumerMainThread(CavityAction::handle).add();
        CHANNEL.messageBuilder(CavityLayoutAction.class, 7, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CavityLayoutAction::encode).decoder(CavityLayoutAction::decode)
                .consumerMainThread(CavityLayoutAction::handle).add();
        CHANNEL.messageBuilder(CavityOpenAction.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CavityOpenAction::encode).decoder(CavityOpenAction::decode)
                .consumerMainThread(CavityOpenAction::handle).add();
    }

    public static void open(ServerPlayer player, BlockPos clickedPos) {
        if (player == null || clickedPos == null) return;
        BlockPos pos = lowerPos(player.level().getBlockState(clickedPos), clickedPos);
        BlockState state = player.level().getBlockState(pos);
        if (!(state.getBlock() instanceof CandyStatueBlock statue)) return;
        net.farmingtales.ftboss.item.EquipmentWeaponBuild.ensureIdentity(net.farmingtales.ftboss.item.FoodWeaponFamilyBuild.heldBuildItem(player));
        FoodWeaponLifecycle.ensureTracked(FoodWeaponEvolution.heldEvolutionWeapon(player));
        boolean negotiated = net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork.negotiated(player);
        net.farmingtales.ftboss.transaction.FoundationEntryPoints.seedFromStatue(player, player.getMainHandItem(), pos, negotiated);
        net.farmingtales.ftboss.transaction.FoundationEntryPoints.seedFromStatue(player, player.getOffhandItem(), pos, negotiated);
        net.farmingtales.ftboss.item.CataclysmCavityAccess.seedFromStatue(player, player.getMainHandItem(), pos);
        net.farmingtales.ftboss.item.CataclysmCavityAccess.seedFromStatue(player, player.getOffhandItem(), pos);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        List<StatueDailyView> daily = FoodWeaponEvolution.statueDailyViews(player, statue.ownTint());
        boolean finaleUnlocked = ManorDifficulty.unlocked(player);
        boolean finaleSelected = BossChallengePreference.selected(player) == ManorDifficulty.IMPOSSIBLE;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Open(pos, statue.ownTint(),
                CandyStatueBlock.isWhite(state), CandyStatueBlock.isGold(state), finaleUnlocked, finaleSelected,
                Component.translatable(state.getBlock().getDescriptionId()), daily));
    }

    private static BlockPos lowerPos(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof CandyStatueBlock
                && state.getValue(CandyStatueBlock.HALF) == DoubleBlockHalf.UPPER) return pos.below();
        return pos;
    }

    private static BlockPos validatedStatuePos(ServerPlayer player, BlockPos requestedPos) {
        if (!player.isAlive() || player.isSpectator() || requestedPos == null
                || !player.level().hasChunkAt(requestedPos)) return null;
        BlockPos pos = lowerPos(player.level().getBlockState(requestedPos), requestedPos);
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5) > 64.0) return null;
        return player.level().getBlockState(pos).getBlock() instanceof CandyStatueBlock ? pos : null;
    }

    private static void toggleColor(ServerPlayer player, BlockPos requestedPos) {
        BlockPos pos = validatedStatuePos(player, requestedPos);
        if (pos == null) return;
        BlockState lower = player.level().getBlockState(pos);
        if (!(lower.getBlock() instanceof CandyStatueBlock statue)) return;
        boolean wasWhite = CandyStatueBlock.isWhite(lower);
        boolean wasGold = CandyStatueBlock.isGold(lower);
        boolean white;
        boolean gold;
        if (wasGold) {
            white = false;
            gold = false;
        } else if (wasWhite && ManorDifficulty.unlocked(player)) {
            white = false;
            gold = true;
        } else if (wasWhite) {
            white = false;
            gold = false;
        } else {
            white = true;
            gold = false;
        }
        setAppearance(player, pos, lower, statue, white, gold);
        player.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 0.9F, gold ? 1.6F : white ? 0.9F : 1.3F);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ColorState(pos, white, gold));
    }

    private static void setAppearance(ServerPlayer player, BlockPos pos, BlockState lower,
                                      CandyStatueBlock statue, boolean white, boolean gold) {
        BlockPos upperPos = pos.above();
        BlockState upper = player.level().getBlockState(upperPos);
        player.level().setBlock(pos, colorState(lower, statue, white, gold), 3);
        if (upper.getBlock() == lower.getBlock()) {
            player.level().setBlock(upperPos, colorState(upper, statue, white, gold), 3);
        }
    }

    private static BlockState colorState(BlockState state, CandyStatueBlock statue, boolean white, boolean gold) {
        return state.setValue(CandyStatueBlock.WHITE, white)
                .setValue(CandyStatueBlock.GOLD, gold)
                .setValue(CandyStatueBlock.TINT, white ? 0 : statue.ownTint());
    }

    private static void unlockGoldAppearance(ServerPlayer player, BlockPos requestedPos) {
        BlockPos pos = validatedStatuePos(player, requestedPos);
        if (pos == null || !ManorDifficulty.unlocked(player)) return;
        BlockState lower = player.level().getBlockState(pos);
        if (lower.getBlock() instanceof CandyStatueBlock statue) {
            setAppearance(player, pos, lower, statue, false, true);
        }
    }

    public record Open(BlockPos pos, int tint, boolean white, boolean gold,
                       boolean finaleUnlocked, boolean finaleSelected,
                       Component statueName, List<StatueDailyView> dailyTasks) {
        public Open {
            dailyTasks = List.copyOf(dailyTasks);
        }

        private static void encode(Open packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeVarInt(packet.tint);
            buffer.writeBoolean(packet.white);
            buffer.writeBoolean(packet.gold);
            buffer.writeBoolean(packet.finaleUnlocked);
            buffer.writeBoolean(packet.finaleSelected);
            buffer.writeComponent(packet.statueName);
            buffer.writeVarInt(packet.dailyTasks.size());
            for (StatueDailyView task : packet.dailyTasks) {
                buffer.writeComponent(task.request());
                buffer.writeUtf(task.requestItemId());
                buffer.writeVarInt(task.progress());
                buffer.writeVarInt(task.required());
                buffer.writeComponent(task.reward());
                buffer.writeByte(task.state());
            }
        }

        private static Open decode(FriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos();
            int tint = buffer.readVarInt();
            boolean white = buffer.readBoolean();
            boolean gold = buffer.readBoolean();
            boolean finaleUnlocked = buffer.readBoolean();
            boolean finaleSelected = buffer.readBoolean();
            Component statueName = buffer.readComponent();
            int size = buffer.readVarInt();
            if (size < 0 || size > MAX_DAILY_TASKS) throw new IllegalArgumentException("Invalid daily task count");
            List<StatueDailyView> tasks = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                tasks.add(new StatueDailyView(buffer.readComponent(), buffer.readUtf(),
                        buffer.readVarInt(), buffer.readVarInt(), buffer.readComponent(), buffer.readUnsignedByte()));
            }
            return new Open(pos, tint, white, gold, finaleUnlocked, finaleSelected, statueName, tasks);
        }

        private static void handle(Open packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientOnly.open(packet)));
            context.get().setPacketHandled(true);
        }
    }

    private static int statueTint(ServerPlayer player, BlockPos requestedPos) {
        BlockPos pos = validatedStatuePos(player, requestedPos);
        if (pos == null) return 0;
        BlockState state = player.level().getBlockState(pos);
        return state.getBlock() instanceof CandyStatueBlock statue ? statue.ownTint() : 0;
    }

    public record Action(BlockPos pos, int action, int value) {
        public Action(BlockPos pos, int action) { this(pos, action, 0); }

        public static void send(BlockPos pos, int action) {
            send(pos, action, 0);
        }

        public static void send(BlockPos pos, int action, int value) {
            CHANNEL.sendToServer(new Action(pos, action, value));
        }

        private static void encode(Action packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeVarInt(packet.action);
            buffer.writeVarInt(packet.value);
        }

        private static Action decode(FriendlyByteBuf buffer) {
            return new Action(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt());
        }

        private static void handle(Action packet, Supplier<NetworkEvent.Context> context) {
            ServerPlayer player = context.get().getSender();
            if (player != null) context.get().enqueueWork(() -> executeAction(player, packet));
            context.get().setPacketHandled(true);
        }
    }

    private static boolean validActionValue(Action packet) {
        return switch (packet.action) {
            case ACTION_TOGGLE_COLOR, ACTION_REFRESH_EVOLUTION, ACTION_CLAIM_EVOLUTION, ACTION_BIND_WEAPON -> packet.value == 0;
            case ACTION_INSERT_SOCKET, ACTION_SUBMIT_DAILY, ACTION_APPLY_FOOD_BLESSING ->
                    packet.value >= 0 && (packet.value < 36 || packet.value == 40);
            case ACTION_REMOVE_SOCKET -> packet.value >= 0 && packet.value < 30;
            case ACTION_EVOLVE_WEAPON -> packet.value >= 0 && packet.value < 36;
            case ACTION_SET_CHALLENGE_DIFFICULTY -> packet.value == 0 || packet.value == 1;
            case ACTION_SELECT_GOURMET_SKILL, ACTION_TOGGLE_CARRIED_SKILL -> packet.value >= 0 && packet.value < 8;
            case ACTION_REPLACE_MATERIAL -> packet.value >= 0 && packet.value < 108;
            default -> false;
        };
    }

    private static void executeAction(ServerPlayer player, Action packet) {
        if (player.containerMenu instanceof SwordCavityMenu) return;
        try {
            BlockPos pos = validatedStatuePos(player, packet.pos);
            if (pos == null || !validActionValue(packet)) return;
            if (packet.action != ACTION_REFRESH_EVOLUTION) {
                Integer previous = LAST_ACTION_TICK.get(player);
                if (previous != null && player.tickCount - previous < MIN_ACTION_INTERVAL_TICKS) return;
                LAST_ACTION_TICK.put(player, player.tickCount);
            }
            int tint = statueTint(player, pos);
            switch (packet.action) {
                case ACTION_TOGGLE_COLOR -> toggleColor(player, pos);
                case ACTION_INSERT_SOCKET -> FoodWeaponEvolution.insertSocketFromUi(player, tint, packet.value);
                case ACTION_REMOVE_SOCKET -> FoodWeaponEvolution.removeSocketFromUi(player, tint, packet.value);
                case ACTION_SUBMIT_DAILY -> FoodWeaponEvolution.submitDailyFromUi(player, tint, packet.value);
                case ACTION_EVOLVE_WEAPON -> FoodWeaponEvolution.evolveWeaponFromUi(player, tint, packet.value);
                case ACTION_CLAIM_EVOLUTION -> FoodWeaponEvolution.claimEvolutionFromUi(player, tint);
                case ACTION_APPLY_FOOD_BLESSING -> FoodWeaponEvolution.applyFoodBlessingFromUi(player, tint, packet.value);
                case ACTION_SELECT_GOURMET_SKILL -> FoodWeaponSkillLoadout.select(player, packet.value, false);
                case ACTION_BIND_WEAPON -> {
                    if (FoodWeaponSkillLoadout.bindFromUi(player, tint)) {
                        net.farmingtales.ftboss.item.SwordNativeMaterials.capture(
                                FoodWeaponEvolution.heldEvolutionWeapon(player), player.serverLevel());
                    }
                }
                case ACTION_TOGGLE_CARRIED_SKILL -> FoodWeaponSkillLoadout.toggleFromUi(player, packet.value);
                case ACTION_REPLACE_MATERIAL -> WeaponMaterialBuild.replaceFromUi(player, packet.value);
                case ACTION_SET_CHALLENGE_DIFFICULTY -> {
                    boolean selected = BossChallengePreference.select(player, packet.value == 1
                            ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL);
                    if (selected && packet.value == 1) unlockGoldAppearance(player, packet.pos);
                }
                default -> {}
            }
        } finally {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
            if (validatedStatuePos(player, packet.pos) != null) open(player, packet.pos);
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ActionResult(packet.pos));
        }
    }

    public record RepairAction(BlockPos pos, int action, int value, UUID weapon, int revision) {
        public static void send(BlockPos pos, ItemStack weapon, int action, int value) {
            if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(weapon)) {
                if (!net.farmingtales.ftboss.transaction.FourWeaponClientApi.sendLegacyAction(pos, weapon, action, value)) {
                    net.farmingtales.ftboss.transaction.FourWeaponClientApi.rejectHubAction(pos);
                }
                return;
            }
            UUID id = FoodWeaponLifecycle.weaponId(weapon);
            if (id != null) CHANNEL.sendToServer(new RepairAction(pos, action, value, id, FoodWeaponRepair.revision(weapon)));
        }

        private static void encode(RepairAction packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeVarInt(packet.action);
            buffer.writeVarInt(packet.value);
            buffer.writeUUID(packet.weapon);
            buffer.writeVarInt(packet.revision);
        }

        private static RepairAction decode(FriendlyByteBuf buffer) {
            return new RepairAction(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUUID(), buffer.readVarInt());
        }

        private static void handle(RepairAction packet, Supplier<NetworkEvent.Context> context) {
            ServerPlayer player = context.get().getSender();
            if (player != null) context.get().enqueueWork(() -> executeRepair(player, packet));
            context.get().setPacketHandled(true);
        }
    }

    private static void executeRepair(ServerPlayer player, RepairAction packet) {
        if (net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(FoodWeaponEvolution.heldEvolutionWeapon(player))) return;
        if (player.containerMenu instanceof SwordCavityMenu) return;
        net.farmingtales.ftboss.item.StatueRepairMutation mutation = null;
        boolean committed = false;
        BlockPos admittedPos = null;
        BlockState admittedStatue = null;
        int admittedTint = -1;
        try {
            try {
                BlockPos pos = validatedStatuePos(player, packet.pos);
                boolean validValue = switch (packet.action) {
                    case ACTION_START_REPAIR, ACTION_CLAIM_REPAIR -> packet.value == 0;
                    case ACTION_SUBMIT_REPAIR -> packet.value >= 0 && packet.value < 3;
                    default -> false;
                };
                if (pos == null || !validValue || player.isSleeping() || player.isUsingItem()) return;
                Integer previous = LAST_ACTION_TICK.get(player);
                if (previous != null && player.tickCount - previous < MIN_ACTION_INTERVAL_TICKS) return;
                LAST_ACTION_TICK.put(player, player.tickCount);
                ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
                if (!packet.weapon.equals(FoodWeaponLifecycle.weaponId(weapon)) || packet.revision < 0
                        || packet.revision != FoodWeaponRepair.revision(weapon)) {
                    player.displayClientMessage(Component.translatable("message.ftboss.repair.selection_changed"), true);
                    return;
                }
                int tint = statueTint(player, pos);
                admittedPos = pos;
                admittedStatue = player.level().getBlockState(pos);
                admittedTint = tint;
                mutation = legacyRepairMutation(player, weapon, tint, packet);
                committed = mutation.commit();
            } finally {
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
                if (validatedStatuePos(player, packet.pos) != null) open(player, packet.pos);
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ActionResult(packet.pos));
            }
            if (mutation != null) {
                if (committed) {
                    if (!admittedPos.equals(validatedStatuePos(player, packet.pos))
                            || !admittedStatue.equals(player.level().getBlockState(admittedPos))
                            || admittedTint != statueTint(player, admittedPos)) throw new IllegalStateException("legacy repair statue changed");
                    mutation.terminalLegacy();
                } else {
                    mutation.rollback();
                }
            }
        } catch (RuntimeException | Error failure) {
            if (mutation != null) {
                try { mutation.rollback(); }
                catch (RuntimeException | Error cleanup) {
                    if (cleanup != failure) failure.addSuppressed(cleanup);
                }
            }
            throw failure;
        }
    }

    private static net.farmingtales.ftboss.item.StatueRepairMutation legacyRepairMutation(ServerPlayer player, ItemStack weapon,
                                                                                       int tint, RepairAction packet) {
        CompoundTag originalTag = weapon.getTag() == null ? null : weapon.getTag().copy();
        int slot = player.getMainHandItem() == weapon ? player.getInventory().selected : 40;
        var operation = switch (packet.action) {
            case ACTION_START_REPAIR -> net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.START_REPAIR;
            case ACTION_SUBMIT_REPAIR -> net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            case ACTION_CLAIM_REPAIR -> net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation.CLAIM_REPAIR;
            default -> throw new IllegalArgumentException("invalid legacy repair action");
        };
        return net.farmingtales.ftboss.item.StatueRepairMutation.wrapLegacy(new net.farmingtales.ftboss.item.StatueRepairMutation.LegacyCompensation() {
            @Override
            public boolean commit() {
                return switch (packet.action) {
                    case ACTION_START_REPAIR -> FoodWeaponRepair.start(player, weapon, tint);
                    case ACTION_SUBMIT_REPAIR -> FoodWeaponRepair.submit(player, weapon, tint, packet.value);
                    case ACTION_CLAIM_REPAIR -> FoodWeaponRepair.claim(player, weapon, tint);
                    default -> false;
                };
            }
            @Override
            public void rollback(boolean inventoryChanged) {
                boolean tagChanged = !java.util.Objects.equals(originalTag, weapon.getTag());
                if (!tagChanged && !inventoryChanged) return;
                if (tagChanged) weapon.setTag(originalTag == null ? null : originalTag.copy());
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
                if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
            }
        }, player, weapon, operation, packet.value, slot, validatedStatuePos(player, packet.pos));
    }

    public record CavityOpenAction(BlockPos pos, UUID weapon, int revision) {
        public static boolean send(BlockPos pos, ItemStack weapon) {
            UUID id = FoodWeaponLifecycle.weaponId(weapon);
            if (id == null || !SwordCavityBuild.applies(weapon)) return false;
            CHANNEL.sendToServer(new CavityOpenAction(pos, id, SwordCavityBuild.revision(weapon)));
            return true;
        }

        private static void encode(CavityOpenAction packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeUUID(packet.weapon);
            buffer.writeVarInt(packet.revision);
        }

        private static CavityOpenAction decode(FriendlyByteBuf buffer) {
            return new CavityOpenAction(buffer.readBlockPos(), buffer.readUUID(), buffer.readVarInt());
        }

        private static void handle(CavityOpenAction packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player == null || player.containerMenu != player.inventoryMenu || player.isSleeping() || player.isUsingItem()) return;
                BlockPos pos = validatedStatuePos(player, packet.pos);
                ItemStack weapon = net.farmingtales.ftboss.item.FoodWeaponFamilyBuild.heldBuildItem(player);
                if (pos == null || !SwordCavityBuild.applies(weapon) || weapon.getCount() != 1 || packet.revision < 0
                        || !packet.weapon.equals(FoodWeaponLifecycle.weaponId(weapon)) || packet.revision != SwordCavityBuild.revision(weapon)) return;
                int weaponSlot = player.getMainHandItem() == weapon ? player.getInventory().selected : 40;
                SwordNativeMaterials.capture(weapon, player.serverLevel());
                player.inventoryMenu.broadcastChanges();
                int[] positions = SwordCavityExtensions.layout(weapon);
                NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, owner) ->
                        new SwordCavityMenu(containerId, inventory, pos, weaponSlot, packet.weapon, positions),
                        Component.translatable("screen.ftboss.cavity.title")), buffer -> {
                            buffer.writeBlockPos(pos);
                            buffer.writeVarInt(weaponSlot);
                            buffer.writeUUID(packet.weapon);
                            for (int position : positions) buffer.writeVarInt(position);
                        });
            });
            context.get().setPacketHandled(true);
        }
    }

    public record CavityAction(BlockPos pos, int part, int inventorySlot, UUID weapon, int revision, ResourceLocation offering) {
        public static boolean send(BlockPos pos, Slot slot, int inventorySlot, ItemStack weapon, ItemStack offering) {
            UUID id = FoodWeaponLifecycle.weaponId(weapon);
            ResourceLocation item = ForgeRegistries.ITEMS.getKey(offering.getItem());
            if (id == null || item == null || inventorySlot < 0 || inventorySlot >= 36 || offering.isEmpty()
                    || !SwordCavityBuild.canReplace(weapon, slot, WeaponMaterialBuild.materialFor(offering))) return false;
            CHANNEL.sendToServer(new CavityAction(pos, slot.ordinal(), inventorySlot,
                    id, SwordCavityBuild.revision(weapon), item));
            return true;
        }

        private static void encode(CavityAction packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeVarInt(packet.part);
            buffer.writeVarInt(packet.inventorySlot);
            buffer.writeUUID(packet.weapon);
            buffer.writeVarInt(packet.revision);
            buffer.writeResourceLocation(packet.offering);
        }

        private static CavityAction decode(FriendlyByteBuf buffer) {
            return new CavityAction(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readUUID(),
                    buffer.readVarInt(), buffer.readResourceLocation());
        }

        private static void handle(CavityAction packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player == null || player.containerMenu instanceof SwordCavityMenu) return;
                try {
                    if (validatedStatuePos(player, packet.pos) == null || Slot.byIndex(packet.part) == null
                            || packet.inventorySlot < 0 || packet.inventorySlot >= 36 || packet.revision < 0) return;
                    Integer previous = LAST_ACTION_TICK.get(player);
                    if (previous != null && player.tickCount - previous < MIN_ACTION_INTERVAL_TICKS) return;
                    LAST_ACTION_TICK.put(player, player.tickCount);
                    ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
                    ItemStack offering = player.getInventory().getItem(packet.inventorySlot);
                    if (!packet.weapon.equals(FoodWeaponLifecycle.weaponId(weapon))
                            || packet.revision != SwordCavityBuild.revision(weapon)
                            || !packet.offering.equals(ForgeRegistries.ITEMS.getKey(offering.getItem()))) return;
                    SwordCavityBuild.replace(player, Slot.byIndex(packet.part), packet.inventorySlot, packet.revision);
                } finally {
                    player.getInventory().setChanged();
                    player.inventoryMenu.broadcastChanges();
                    if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
                    if (validatedStatuePos(player, packet.pos) != null) open(player, packet.pos);
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ActionResult(packet.pos));
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    public record CavityLayoutAction(BlockPos pos, int action, int from, int to, UUID weapon, int revision) {
        public static final int ACTION_MOVE = 0;
        public static final int ACTION_REMOVE = 1;

        public static boolean send(BlockPos pos, int action, Slot from, Slot to, ItemStack weapon) {
            UUID id = FoodWeaponLifecycle.weaponId(weapon);
            if (id == null || from == null || to == null || action != ACTION_MOVE && action != ACTION_REMOVE) return false;
            if (action == ACTION_MOVE ? !SwordCavityBuild.canMove(weapon, from, to) : !SwordCavityBuild.canRemove(weapon, from)) return false;
            CHANNEL.sendToServer(new CavityLayoutAction(pos, action, from.ordinal(), to.ordinal(), id, SwordCavityBuild.revision(weapon)));
            return true;
        }

        private static void encode(CavityLayoutAction packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeVarInt(packet.action);
            buffer.writeVarInt(packet.from);
            buffer.writeVarInt(packet.to);
            buffer.writeUUID(packet.weapon);
            buffer.writeVarInt(packet.revision);
        }

        private static CavityLayoutAction decode(FriendlyByteBuf buffer) {
            return new CavityLayoutAction(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readUUID(), buffer.readVarInt());
        }

        private static void handle(CavityLayoutAction packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player == null || player.containerMenu instanceof SwordCavityMenu) return;
                try {
                    Slot from = Slot.byIndex(packet.from);
                    Slot to = Slot.byIndex(packet.to);
                    if (validatedStatuePos(player, packet.pos) == null || from == null || to == null || packet.revision < 0
                            || packet.action != ACTION_MOVE && packet.action != ACTION_REMOVE) return;
                    Integer previous = LAST_ACTION_TICK.get(player);
                    if (previous != null && player.tickCount - previous < MIN_ACTION_INTERVAL_TICKS) return;
                    LAST_ACTION_TICK.put(player, player.tickCount);
                    ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
                    if (!packet.weapon.equals(FoodWeaponLifecycle.weaponId(weapon)) || packet.revision != SwordCavityBuild.revision(weapon)) return;
                    if (packet.action == ACTION_MOVE) SwordCavityBuild.move(player, from, to, packet.revision);
                    else SwordCavityBuild.remove(player, from, packet.revision);
                } finally {
                    player.getInventory().setChanged();
                    player.inventoryMenu.broadcastChanges();
                    if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
                    if (validatedStatuePos(player, packet.pos) != null) open(player, packet.pos);
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ActionResult(packet.pos));
                }
            });
            context.get().setPacketHandled(true);
        }
    }

    public record ActionResult(BlockPos pos) {
        private static void encode(ActionResult packet, FriendlyByteBuf buffer) { buffer.writeBlockPos(packet.pos); }
        private static ActionResult decode(FriendlyByteBuf buffer) { return new ActionResult(buffer.readBlockPos()); }
        private static void handle(ActionResult packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientOnly.acknowledge(packet)));
            context.get().setPacketHandled(true);
        }
    }

    public record SkillSelection(ResourceLocation item, int hotbarSlot, int index, int level, String profile) {
        public static void send(ItemStack weapon, int hotbarSlot, int index) {
            ResourceLocation item = ForgeRegistries.ITEMS.getKey(weapon.getItem());
            if (item != null) CHANNEL.sendToServer(new SkillSelection(item, hotbarSlot, index,
                    FoodWeaponEvolution.evolutionLevel(weapon), FoodWeaponEvolution.firstProfileId(weapon)));
        }

        private static void encode(SkillSelection packet, FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(packet.item);
            buffer.writeVarInt(packet.hotbarSlot);
            buffer.writeVarInt(packet.index);
            buffer.writeVarInt(packet.level);
            buffer.writeUtf(packet.profile, 32);
        }

        private static SkillSelection decode(FriendlyByteBuf buffer) {
            return new SkillSelection(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readUtf(32));
        }

        private static void handle(SkillSelection packet, Supplier<NetworkEvent.Context> context) {
            ServerPlayer player = context.get().getSender();
            if (player != null) context.get().enqueueWork(() -> selectSkill(player, packet));
            context.get().setPacketHandled(true);
        }
    }

    private static void selectSkill(ServerPlayer player, SkillSelection packet) {
        ItemStack weapon = player.getMainHandItem();
        Integer previous = LAST_ACTION_TICK.get(player);
        boolean valid = player.isAlive() && !player.isSpectator() && !player.isSleeping() && !player.isUsingItem()
                && packet.hotbarSlot >= 0 && packet.hotbarSlot < 9 && player.getInventory().selected == packet.hotbarSlot
                && packet.index >= 0 && packet.index < 8 && packet.level == FoodWeaponEvolution.evolutionLevel(weapon)
                && packet.item.equals(ForgeRegistries.ITEMS.getKey(weapon.getItem()))
                && packet.profile.equals(FoodWeaponEvolution.firstProfileId(weapon))
                && (previous == null || player.tickCount - previous >= MIN_ACTION_INTERVAL_TICKS);
        if (valid) {
            LAST_ACTION_TICK.put(player, player.tickCount);
            valid = FoodWeaponSkillLoadout.select(player, packet.index, true);
        }
        if (!valid) player.displayClientMessage(Component.translatable("message.ftboss.skills.selection_changed"), true);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
    }

    public record ColorState(BlockPos pos, boolean white, boolean gold) {
        private static void encode(ColorState packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
            buffer.writeBoolean(packet.white);
            buffer.writeBoolean(packet.gold);
        }

        private static ColorState decode(FriendlyByteBuf buffer) {
            return new ColorState(buffer.readBlockPos(), buffer.readBoolean(), buffer.readBoolean());
        }

        private static void handle(ColorState packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientOnly.updateColor(packet)));
            context.get().setPacketHandled(true);
        }
    }

    private static final class ClientOnly {
        private static void acknowledge(ActionResult packet) {
            net.farmingtales.ftboss.client.StatueHubScreen.acknowledgeAction(packet.pos);
        }

        private static void open(Open packet) {
            net.farmingtales.ftboss.client.StatueHubScreen.open(packet);
            net.farmingtales.ftboss.client.StatueWeaponRepairNavigation.bootstrapHub(packet.pos);
        }

        private static void updateColor(ColorState packet) {
            net.farmingtales.ftboss.client.StatueHubScreen.updateAppearance(packet.pos, packet.white, packet.gold);
        }
    }

    /** Runs after the existing evolution handler, including when that handler consumed the click. */
    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
        private ForgeEvents() {}

        @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
        public static void rightClickStatue(PlayerInteractEvent.RightClickBlock event) {
            if (event.getHand() != InteractionHand.MAIN_HAND
                    || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof CandyStatueBlock)) return;
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
                open(player, event.getPos());
            }
        }
    }
}
