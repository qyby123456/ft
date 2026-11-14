/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkHooks
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.boss.BossChallengePreference;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.client.StatueWeaponRepairNavigation;
import net.farmingtales.ftboss.item.CataclysmCavityAccess;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.StatueRepairMutation;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.util.SwordCavityRules;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
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
    private static final SimpleChannel CHANNEL;
    private static boolean registered;
    private static final int MIN_ACTION_INTERVAL_TICKS = 4;
    private static final Map<ServerPlayer, Integer> LAST_ACTION_TICK;
    public static final int ACTION_SUBMIT_REPAIR = 14;
    public static final int ACTION_CLAIM_REPAIR = 15;
    public static final int ACTION_BIND_WEAPON = 11;
    public static final int ACTION_TOGGLE_CARRIED_SKILL = 12;
    public static final int ACTION_REPLACE_MATERIAL = 10;
    public static final int ACTION_START_REPAIR = 13;
    private static final int MAX_DAILY_TASKS = 12;
    private static final String PROTOCOL = "10";

    private StatueMenuNetwork() {
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(StatueMenuNetwork::register);
    }

    private static BlockPos lowerPos(BlockState state, BlockPos pos) {
        if (state.m_60734_() instanceof CandyStatueBlock && state.m_61143_((Property)CandyStatueBlock.f_52858_) == DoubleBlockHalf.UPPER) {
            return pos.m_7495_();
        }
        return pos;
    }

    private static void toggleColor(ServerPlayer player, BlockPos requestedPos) {
        boolean gold;
        boolean white;
        BlockPos pos = StatueMenuNetwork.validatedStatuePos(player, requestedPos);
        if (pos == null) {
            return;
        }
        BlockState lower = player.m_9236_().m_8055_(pos);
        Block block = lower.m_60734_();
        if (!(block instanceof CandyStatueBlock)) {
            return;
        }
        CandyStatueBlock statue = (CandyStatueBlock)block;
        boolean wasWhite = CandyStatueBlock.isWhite(lower);
        boolean wasGold = CandyStatueBlock.isGold(lower);
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
        StatueMenuNetwork.setAppearance(player, pos, lower, statue, white, gold);
        player.m_9236_().m_5594_(null, pos, SoundEvents.f_144243_, SoundSource.BLOCKS, 0.9f, gold ? 1.6f : (white ? 0.9f : 1.3f));
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new ColorState(pos, white, gold));
    }

    private static void setAppearance(ServerPlayer player, BlockPos pos, BlockState lower, CandyStatueBlock statue, boolean white, boolean gold) {
        BlockPos upperPos = pos.m_7494_();
        BlockState upper = player.m_9236_().m_8055_(upperPos);
        player.m_9236_().m_7731_(pos, StatueMenuNetwork.colorState(lower, statue, white, gold), 3);
        if (upper.m_60734_() == lower.m_60734_()) {
            player.m_9236_().m_7731_(upperPos, StatueMenuNetwork.colorState(upper, statue, white, gold), 3);
        }
    }

    private static BlockState colorState(BlockState state, CandyStatueBlock statue, boolean white, boolean gold) {
        return (BlockState)((BlockState)((BlockState)state.m_61124_((Property)CandyStatueBlock.WHITE, (Comparable)Boolean.valueOf(white))).m_61124_((Property)CandyStatueBlock.GOLD, (Comparable)Boolean.valueOf(gold))).m_61124_((Property)CandyStatueBlock.TINT, (Comparable)Integer.valueOf(white ? 0 : statue.ownTint()));
    }

    private static void unlockGoldAppearance(ServerPlayer player, BlockPos requestedPos) {
        BlockPos pos = StatueMenuNetwork.validatedStatuePos(player, requestedPos);
        if (pos == null || !ManorDifficulty.unlocked(player)) {
            return;
        }
        BlockState lower = player.m_9236_().m_8055_(pos);
        Block block = lower.m_60734_();
        if (block instanceof CandyStatueBlock) {
            CandyStatueBlock statue = (CandyStatueBlock)block;
            StatueMenuNetwork.setAppearance(player, pos, lower, statue, false, true);
        }
    }

    private static int statueTint(ServerPlayer player, BlockPos requestedPos) {
        int n;
        BlockPos pos = StatueMenuNetwork.validatedStatuePos(player, requestedPos);
        if (pos == null) {
            return 0;
        }
        BlockState state = player.m_9236_().m_8055_(pos);
        Block block = state.m_60734_();
        if (block instanceof CandyStatueBlock) {
            CandyStatueBlock statue = (CandyStatueBlock)block;
            n = statue.ownTint();
        } else {
            n = 0;
        }
        return n;
    }

    public static void open(ServerPlayer serverPlayer, BlockPos blockPos) {
        if (serverPlayer == null || blockPos == null) {
            return;
        }
        BlockPos blockPos2 = StatueMenuNetwork.lowerPos(serverPlayer.m_9236_().m_8055_(blockPos), blockPos);
        BlockState blockState = serverPlayer.m_9236_().m_8055_(blockPos2);
        Object object = blockState.m_60734_();
        if (!(object instanceof CandyStatueBlock)) {
            return;
        }
        CandyStatueBlock candyStatueBlock = (CandyStatueBlock)((Object)object);
        FoodWeaponLifecycle.ensureTracked(FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer));
        FoundationEntryPoints.seedFromStatue(serverPlayer, serverPlayer.m_21205_(), blockPos2, FourWeaponTransactionNetwork.negotiated(serverPlayer));
        FoundationEntryPoints.seedFromStatue(serverPlayer, serverPlayer.m_21206_(), blockPos2, FourWeaponTransactionNetwork.negotiated(serverPlayer));
        CataclysmCavityAccess.seedFromStatue(serverPlayer, serverPlayer.m_21205_(), blockPos2);
        CataclysmCavityAccess.seedFromStatue(serverPlayer, serverPlayer.m_21206_(), blockPos2);
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36095_.m_38946_();
        if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
            serverPlayer.f_36096_.m_38946_();
        }
        object = FoodWeaponEvolution.statueDailyViews(serverPlayer, candyStatueBlock.ownTint());
        boolean bl = ManorDifficulty.unlocked(serverPlayer);
        boolean bl2 = BossChallengePreference.selected((Player)serverPlayer) == ManorDifficulty.IMPOSSIBLE;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new Open(blockPos2, candyStatueBlock.ownTint(), CandyStatueBlock.isWhite(blockState), CandyStatueBlock.isGold(blockState), bl, bl2, (Component)Component.m_237115_((String)blockState.m_60734_().m_7705_()), (List<FoodWeaponEvolution.StatueDailyView>)object));
    }

    private static BlockPos validatedStatuePos(ServerPlayer serverPlayer, BlockPos blockPos) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_5833_() || blockPos == null || !serverPlayer.m_9236_().m_46805_(blockPos)) {
            return null;
        }
        BlockPos blockPos2 = StatueMenuNetwork.lowerPos(serverPlayer.m_9236_().m_8055_(blockPos), blockPos);
        if (serverPlayer.m_20275_((double)blockPos2.m_123341_() + 0.5, (double)blockPos2.m_123342_() + 1.0, (double)blockPos2.m_123343_() + 0.5) > 64.0) {
            return null;
        }
        return serverPlayer.m_9236_().m_8055_(blockPos2).m_60734_() instanceof CandyStatueBlock ? blockPos2 : null;
    }

    private static boolean validActionValue(Action action) {
        return switch (action.action) {
            case 0, 1, 6, 11 -> {
                if (action.value == 0) {
                    yield true;
                }
                yield false;
            }
            case 2, 4, 7 -> {
                if (action.value >= 0 && (action.value < 36 || action.value == 40)) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (action.value >= 0 && action.value < 30) {
                    yield true;
                }
                yield false;
            }
            case 5 -> {
                if (action.value >= 0 && action.value < 36) {
                    yield true;
                }
                yield false;
            }
            case 8 -> {
                if (action.value == 0 || action.value == 1) {
                    yield true;
                }
                yield false;
            }
            case 9, 12 -> {
                if (action.value >= 0 && action.value < 8) {
                    yield true;
                }
                yield false;
            }
            case 10 -> {
                if (action.value >= 0 && action.value < 108) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private static void selectSkill(ServerPlayer serverPlayer, SkillSelection skillSelection) {
        boolean bl;
        ItemStack itemStack = serverPlayer.m_21205_();
        Integer n = LAST_ACTION_TICK.get(serverPlayer);
        boolean bl2 = bl = serverPlayer.m_6084_() && !serverPlayer.m_5833_() && !serverPlayer.m_5803_() && !serverPlayer.m_6117_() && skillSelection.hotbarSlot >= 0 && skillSelection.hotbarSlot < 9 && serverPlayer.m_150109_().f_35977_ == skillSelection.hotbarSlot && skillSelection.index >= 0 && skillSelection.index < 8 && skillSelection.level == FoodWeaponEvolution.evolutionLevel(itemStack) && skillSelection.item.equals((Object)ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_())) && skillSelection.profile.equals(FoodWeaponEvolution.firstProfileId(itemStack)) && (n == null || serverPlayer.f_19797_ - n >= 4);
        if (bl) {
            LAST_ACTION_TICK.put(serverPlayer, serverPlayer.f_19797_);
            bl = FoodWeaponSkillLoadout.select(serverPlayer, skillSelection.index, true);
        }
        if (!bl) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.skills.selection_changed"), true);
        }
        serverPlayer.m_150109_().m_6596_();
        serverPlayer.f_36095_.m_38946_();
        if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
            serverPlayer.f_36096_.m_38946_();
        }
    }

    private static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(Open.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(Open::encode).decoder(Open::decode).consumerMainThread(Open::handle).add();
        CHANNEL.messageBuilder(Action.class, 1, NetworkDirection.PLAY_TO_SERVER).encoder(Action::encode).decoder(Action::decode).consumerMainThread(Action::handle).add();
        CHANNEL.messageBuilder(ColorState.class, 2, NetworkDirection.PLAY_TO_CLIENT).encoder(ColorState::encode).decoder(ColorState::decode).consumerMainThread(ColorState::handle).add();
        CHANNEL.messageBuilder(ActionResult.class, 3, NetworkDirection.PLAY_TO_CLIENT).encoder(ActionResult::encode).decoder(ActionResult::decode).consumerMainThread(ActionResult::handle).add();
        CHANNEL.messageBuilder(SkillSelection.class, 4, NetworkDirection.PLAY_TO_SERVER).encoder(SkillSelection::encode).decoder(SkillSelection::decode).consumerMainThread(SkillSelection::handle).add();
        CHANNEL.messageBuilder(RepairAction.class, 5, NetworkDirection.PLAY_TO_SERVER).encoder(RepairAction::encode).decoder(RepairAction::decode).consumerMainThread(RepairAction::handle).add();
        CHANNEL.messageBuilder(CavityAction.class, 6, NetworkDirection.PLAY_TO_SERVER).encoder(CavityAction::encode).decoder(CavityAction::decode).consumerMainThread(CavityAction::handle).add();
        CHANNEL.messageBuilder(CavityLayoutAction.class, 7, NetworkDirection.PLAY_TO_SERVER).encoder(CavityLayoutAction::encode).decoder(CavityLayoutAction::decode).consumerMainThread(CavityLayoutAction::handle).add();
        CHANNEL.messageBuilder(CavityOpenAction.class, 8, NetworkDirection.PLAY_TO_SERVER).encoder(CavityOpenAction::encode).decoder(CavityOpenAction::decode).consumerMainThread(CavityOpenAction::handle).add();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static void executeAction(ServerPlayer serverPlayer, Action action) {
        if (serverPlayer.f_36096_ instanceof SwordCavityMenu) {
            return;
        }
        try {
            BlockPos blockPos = StatueMenuNetwork.validatedStatuePos(serverPlayer, action.pos);
            if (blockPos == null) return;
            if (!StatueMenuNetwork.validActionValue(action)) {
                return;
            }
            if (action.action != 1) {
                Integer n = LAST_ACTION_TICK.get(serverPlayer);
                if (n != null && serverPlayer.f_19797_ - n < 4) {
                    return;
                }
                LAST_ACTION_TICK.put(serverPlayer, serverPlayer.f_19797_);
            }
            int n = StatueMenuNetwork.statueTint(serverPlayer, blockPos);
            switch (action.action) {
                case 0: {
                    StatueMenuNetwork.toggleColor(serverPlayer, blockPos);
                    return;
                }
                case 2: {
                    FoodWeaponEvolution.insertSocketFromUi(serverPlayer, n, action.value);
                    return;
                }
                case 3: {
                    FoodWeaponEvolution.removeSocketFromUi(serverPlayer, n, action.value);
                    return;
                }
                case 4: {
                    FoodWeaponEvolution.submitDailyFromUi(serverPlayer, n, action.value);
                    return;
                }
                case 5: {
                    FoodWeaponEvolution.evolveWeaponFromUi(serverPlayer, n, action.value);
                    return;
                }
                case 6: {
                    FoodWeaponEvolution.claimEvolutionFromUi(serverPlayer, n);
                    return;
                }
                case 7: {
                    FoodWeaponEvolution.applyFoodBlessingFromUi(serverPlayer, n, action.value);
                    return;
                }
                case 9: {
                    FoodWeaponSkillLoadout.select(serverPlayer, action.value, false);
                    return;
                }
                case 11: {
                    if (!FoodWeaponSkillLoadout.bindFromUi(serverPlayer, n)) return;
                    SwordNativeMaterials.capture(FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer), (Level)serverPlayer.m_284548_());
                    return;
                }
                case 12: {
                    FoodWeaponSkillLoadout.toggleFromUi(serverPlayer, action.value);
                    return;
                }
                case 10: {
                    WeaponMaterialBuild.replaceFromUi(serverPlayer, action.value);
                    return;
                }
                case 8: {
                    boolean bl = BossChallengePreference.select(serverPlayer, action.value == 1 ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL);
                    if (!bl) return;
                    if (action.value != 1) return;
                    StatueMenuNetwork.unlockGoldAppearance(serverPlayer, action.pos);
                    return;
                }
            }
            return;
        }
        finally {
            serverPlayer.m_150109_().m_6596_();
            serverPlayer.f_36095_.m_38946_();
            if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                serverPlayer.f_36096_.m_38946_();
            }
            if (StatueMenuNetwork.validatedStatuePos(serverPlayer, action.pos) != null) {
                StatueMenuNetwork.open(serverPlayer, action.pos);
            }
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new ActionResult(action.pos));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void executeRepair(ServerPlayer serverPlayer, RepairAction repairAction) {
        if (FourWeaponFoundationAdapter.applies(FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer))) {
            return;
        }
        if (serverPlayer.f_36096_ instanceof SwordCavityMenu) {
            return;
        }
        StatueRepairMutation statueRepairMutation = null;
        boolean bl = false;
        BlockPos blockPos = null;
        BlockState blockState = null;
        int n = -1;
        try {
            try {
                boolean bl2;
                BlockPos blockPos2 = StatueMenuNetwork.validatedStatuePos(serverPlayer, repairAction.pos);
                switch (repairAction.action) {
                    case 13: 
                    case 15: {
                        boolean bl3;
                        if (repairAction.value == 0) {
                            bl3 = true;
                            break;
                        }
                        bl3 = false;
                        break;
                    }
                    case 14: {
                        boolean bl3;
                        if (repairAction.value >= 0 && repairAction.value < 3) {
                            bl3 = true;
                            break;
                        }
                        bl3 = false;
                        break;
                    }
                    default: {
                        boolean bl3 = bl2 = false;
                    }
                }
                if (blockPos2 == null || !bl2 || serverPlayer.m_5803_() || serverPlayer.m_6117_()) {
                    return;
                }
                Integer n2 = LAST_ACTION_TICK.get(serverPlayer);
                if (n2 != null && serverPlayer.f_19797_ - n2 < 4) {
                    return;
                }
                LAST_ACTION_TICK.put(serverPlayer, serverPlayer.f_19797_);
                ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
                if (!repairAction.weapon.equals(FoodWeaponLifecycle.weaponId(itemStack)) || repairAction.revision < 0 || repairAction.revision != FoodWeaponRepair.revision(itemStack)) {
                    serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.repair.selection_changed"), true);
                    return;
                }
                int n3 = StatueMenuNetwork.statueTint(serverPlayer, blockPos2);
                blockPos = blockPos2;
                blockState = serverPlayer.m_9236_().m_8055_(blockPos2);
                n = n3;
                statueRepairMutation = StatueMenuNetwork.legacyRepairMutation(serverPlayer, itemStack, n3, repairAction);
                bl = statueRepairMutation.commit();
            }
            finally {
                serverPlayer.m_150109_().m_6596_();
                serverPlayer.f_36095_.m_38946_();
                if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                    serverPlayer.f_36096_.m_38946_();
                }
                if (StatueMenuNetwork.validatedStatuePos(serverPlayer, repairAction.pos) != null) {
                    StatueMenuNetwork.open(serverPlayer, repairAction.pos);
                }
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new ActionResult(repairAction.pos));
            }
            if (statueRepairMutation != null) {
                if (bl) {
                    if (!blockPos.equals((Object)StatueMenuNetwork.validatedStatuePos(serverPlayer, repairAction.pos)) || !blockState.equals(serverPlayer.m_9236_().m_8055_(blockPos)) || n != StatueMenuNetwork.statueTint(serverPlayer, blockPos)) {
                        throw new IllegalStateException("legacy repair statue changed");
                    }
                    statueRepairMutation.terminalLegacy();
                } else {
                    statueRepairMutation.rollback();
                }
            }
        }
        catch (Error | RuntimeException throwable) {
            block29: {
                if (statueRepairMutation != null) {
                    try {
                        statueRepairMutation.rollback();
                    }
                    catch (Error | RuntimeException throwable2) {
                        if (throwable2 == throwable) break block29;
                        throwable.addSuppressed(throwable2);
                    }
                }
            }
            throw throwable;
        }
    }

    static {
        LAST_ACTION_TICK = new WeakHashMap<ServerPlayer, Integer>();
        CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "statue_hub"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    }

    private static StatueRepairMutation legacyRepairMutation(final ServerPlayer serverPlayer, final ItemStack itemStack, final int n, final RepairAction repairAction) {
        final CompoundTag compoundTag = itemStack.m_41783_() == null ? null : itemStack.m_41783_().m_6426_();
        int n2 = serverPlayer.m_21205_() == itemStack ? serverPlayer.m_150109_().f_35977_ : 40;
        FourWeaponTransactions.Operation operation = switch (repairAction.action) {
            case 13 -> FourWeaponTransactions.Operation.START_REPAIR;
            case 14 -> FourWeaponTransactions.Operation.SUBMIT_REPAIR;
            case 15 -> FourWeaponTransactions.Operation.CLAIM_REPAIR;
            default -> throw new IllegalArgumentException("invalid legacy repair action");
        };
        return StatueRepairMutation.wrapLegacy(new StatueRepairMutation.LegacyCompensation(){

            @Override
            public boolean commit() {
                return switch (repairAction.action) {
                    case 13 -> FoodWeaponRepair.start(serverPlayer, itemStack, n);
                    case 14 -> FoodWeaponRepair.submit(serverPlayer, itemStack, n, repairAction.value);
                    case 15 -> FoodWeaponRepair.claim(serverPlayer, itemStack, n);
                    default -> false;
                };
            }

            @Override
            public void rollback(boolean bl) {
                boolean bl2;
                boolean bl3 = bl2 = !Objects.equals(compoundTag, itemStack.m_41783_());
                if (!bl2 && !bl) {
                    return;
                }
                if (bl2) {
                    itemStack.m_41751_(compoundTag == null ? null : compoundTag.m_6426_());
                }
                serverPlayer.m_150109_().m_6596_();
                serverPlayer.f_36095_.m_38946_();
                if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                    serverPlayer.f_36096_.m_38946_();
                }
            }
        }, serverPlayer, itemStack, operation, repairAction.value, n2, StatueMenuNetwork.validatedStatuePos(serverPlayer, repairAction.pos));
    }

    public record Open(BlockPos pos, int tint, boolean white, boolean gold, boolean finaleUnlocked, boolean finaleSelected, Component statueName, List<FoodWeaponEvolution.StatueDailyView> dailyTasks) {
        public Open {
            dailyTasks = List.copyOf(dailyTasks);
        }

        private static void encode(Open packet, FriendlyByteBuf buffer) {
            buffer.m_130064_(packet.pos);
            buffer.m_130130_(packet.tint);
            buffer.writeBoolean(packet.white);
            buffer.writeBoolean(packet.gold);
            buffer.writeBoolean(packet.finaleUnlocked);
            buffer.writeBoolean(packet.finaleSelected);
            buffer.m_130083_(packet.statueName);
            buffer.m_130130_(packet.dailyTasks.size());
            for (FoodWeaponEvolution.StatueDailyView task : packet.dailyTasks) {
                buffer.m_130083_(task.request());
                buffer.m_130070_(task.requestItemId());
                buffer.m_130130_(task.progress());
                buffer.m_130130_(task.required());
                buffer.m_130083_(task.reward());
                buffer.writeByte(task.state());
            }
        }

        private static void handle(Open packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientOnly.open(packet)));
            context.get().setPacketHandled(true);
        }

        private static Open decode(FriendlyByteBuf friendlyByteBuf) {
            BlockPos blockPos = friendlyByteBuf.m_130135_();
            int n = friendlyByteBuf.m_130242_();
            boolean bl = friendlyByteBuf.readBoolean();
            boolean bl2 = friendlyByteBuf.readBoolean();
            boolean bl3 = friendlyByteBuf.readBoolean();
            boolean bl4 = friendlyByteBuf.readBoolean();
            Component component = friendlyByteBuf.m_130238_();
            int n2 = friendlyByteBuf.m_130242_();
            if (n2 < 0 || n2 > 12) {
                throw new IllegalArgumentException("Invalid daily task count");
            }
            ArrayList<FoodWeaponEvolution.StatueDailyView> arrayList = new ArrayList<FoodWeaponEvolution.StatueDailyView>(n2);
            for (int i = 0; i < n2; ++i) {
                arrayList.add(new FoodWeaponEvolution.StatueDailyView(friendlyByteBuf.m_130238_(), friendlyByteBuf.m_130277_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130238_(), friendlyByteBuf.readUnsignedByte()));
            }
            return new Open(blockPos, n, bl, bl2, bl3, bl4, component, arrayList);
        }
    }

    public record Action(BlockPos pos, int action, int value) {
        public Action(BlockPos pos, int action) {
            this(pos, action, 0);
        }

        public static void send(BlockPos pos, int action) {
            Action.send(pos, action, 0);
        }

        public static void send(BlockPos pos, int action, int value) {
            CHANNEL.sendToServer((Object)new Action(pos, action, value));
        }

        private static void encode(Action packet, FriendlyByteBuf buffer) {
            buffer.m_130064_(packet.pos);
            buffer.m_130130_(packet.action);
            buffer.m_130130_(packet.value);
        }

        private static Action decode(FriendlyByteBuf buffer) {
            return new Action(buffer.m_130135_(), buffer.m_130242_(), buffer.m_130242_());
        }

        private static void handle(Action action, Supplier<NetworkEvent.Context> supplier) {
            ServerPlayer serverPlayer = supplier.get().getSender();
            if (serverPlayer != null) {
                supplier.get().enqueueWork(() -> StatueMenuNetwork.executeAction(serverPlayer, action));
            }
            supplier.get().setPacketHandled(true);
        }
    }

    public record ColorState(BlockPos pos, boolean white, boolean gold) {
        private static void encode(ColorState packet, FriendlyByteBuf buffer) {
            buffer.m_130064_(packet.pos);
            buffer.writeBoolean(packet.white);
            buffer.writeBoolean(packet.gold);
        }

        private static ColorState decode(FriendlyByteBuf buffer) {
            return new ColorState(buffer.m_130135_(), buffer.readBoolean(), buffer.readBoolean());
        }

        private static void handle(ColorState packet, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientOnly.updateColor(packet)));
            context.get().setPacketHandled(true);
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
        public static void rightClickStatue(PlayerInteractEvent.RightClickBlock event) {
            Player player;
            if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getLevel().m_8055_(event.getPos()).m_60734_() instanceof CandyStatueBlock)) {
                return;
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!event.getLevel().f_46443_ && (player = event.getEntity()) instanceof ServerPlayer) {
                ServerPlayer player2 = (ServerPlayer)player;
                StatueMenuNetwork.open(player2, event.getPos());
            }
        }
    }

    private static final class ClientOnly {
        private ClientOnly() {
        }

        private static void open(Open open) {
            StatueHubScreen.open(open);
            StatueWeaponRepairNavigation.bootstrapHub(open.pos);
        }

        private static void updateColor(ColorState packet) {
            StatueHubScreen.updateAppearance(packet.pos, packet.white, packet.gold);
        }

        private static void acknowledge(ActionResult actionResult) {
            StatueHubScreen.acknowledgeAction(actionResult.pos);
        }
    }

    public record ActionResult(BlockPos pos) {
        private static void encode(ActionResult actionResult, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130064_(actionResult.pos);
        }

        private static ActionResult decode(FriendlyByteBuf friendlyByteBuf) {
            return new ActionResult(friendlyByteBuf.m_130135_());
        }

        private static void handle(ActionResult actionResult, Supplier<NetworkEvent.Context> supplier) {
            supplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientOnly.acknowledge(actionResult)));
            supplier.get().setPacketHandled(true);
        }
    }

    public record SkillSelection(ResourceLocation item, int hotbarSlot, int index, int level, String profile) {
        public static void send(ItemStack itemStack, int n, int n2) {
            ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
            if (resourceLocation != null) {
                CHANNEL.sendToServer((Object)new SkillSelection(resourceLocation, n, n2, FoodWeaponEvolution.evolutionLevel(itemStack), FoodWeaponEvolution.firstProfileId(itemStack)));
            }
        }

        private static void encode(SkillSelection skillSelection, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130085_(skillSelection.item);
            friendlyByteBuf.m_130130_(skillSelection.hotbarSlot);
            friendlyByteBuf.m_130130_(skillSelection.index);
            friendlyByteBuf.m_130130_(skillSelection.level);
            friendlyByteBuf.m_130072_(skillSelection.profile, 32);
        }

        private static SkillSelection decode(FriendlyByteBuf friendlyByteBuf) {
            return new SkillSelection(friendlyByteBuf.m_130281_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130136_(32));
        }

        private static void handle(SkillSelection skillSelection, Supplier<NetworkEvent.Context> supplier) {
            ServerPlayer serverPlayer = supplier.get().getSender();
            if (serverPlayer != null) {
                supplier.get().enqueueWork(() -> StatueMenuNetwork.selectSkill(serverPlayer, skillSelection));
            }
            supplier.get().setPacketHandled(true);
        }
    }

    public record RepairAction(BlockPos pos, int action, int value, UUID weapon, int revision) {
        public static void send(BlockPos blockPos, ItemStack itemStack, int n, int n2) {
            if (FourWeaponFoundationAdapter.applies(itemStack)) {
                if (!FourWeaponClientApi.sendLegacyAction(blockPos, itemStack, n, n2)) {
                    FourWeaponClientApi.rejectHubAction(blockPos);
                }
                return;
            }
            UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
            if (uUID != null) {
                CHANNEL.sendToServer((Object)new RepairAction(blockPos, n, n2, uUID, FoodWeaponRepair.revision(itemStack)));
            }
        }

        private static void encode(RepairAction repairAction, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130064_(repairAction.pos);
            friendlyByteBuf.m_130130_(repairAction.action);
            friendlyByteBuf.m_130130_(repairAction.value);
            friendlyByteBuf.m_130077_(repairAction.weapon);
            friendlyByteBuf.m_130130_(repairAction.revision);
        }

        private static RepairAction decode(FriendlyByteBuf friendlyByteBuf) {
            return new RepairAction(friendlyByteBuf.m_130135_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_());
        }

        private static void handle(RepairAction repairAction, Supplier<NetworkEvent.Context> supplier) {
            ServerPlayer serverPlayer = supplier.get().getSender();
            if (serverPlayer != null) {
                supplier.get().enqueueWork(() -> StatueMenuNetwork.executeRepair(serverPlayer, repairAction));
            }
            supplier.get().setPacketHandled(true);
        }
    }

    public record CavityAction(BlockPos pos, int part, int inventorySlot, UUID weapon, int revision, ResourceLocation offering) {
        public static boolean send(BlockPos blockPos, SwordCavityRules.Slot slot, int n, ItemStack itemStack, ItemStack itemStack2) {
            UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
            ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_());
            if (uUID == null || resourceLocation == null || n < 0 || n >= 36 || itemStack2.m_41619_() || !SwordCavityBuild.canReplace(itemStack, slot, WeaponMaterialBuild.materialFor(itemStack2))) {
                return false;
            }
            CHANNEL.sendToServer((Object)new CavityAction(blockPos, slot.ordinal(), n, uUID, SwordCavityBuild.revision(itemStack), resourceLocation));
            return true;
        }

        private static void encode(CavityAction cavityAction, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130064_(cavityAction.pos);
            friendlyByteBuf.m_130130_(cavityAction.part);
            friendlyByteBuf.m_130130_(cavityAction.inventorySlot);
            friendlyByteBuf.m_130077_(cavityAction.weapon);
            friendlyByteBuf.m_130130_(cavityAction.revision);
            friendlyByteBuf.m_130085_(cavityAction.offering);
        }

        private static CavityAction decode(FriendlyByteBuf friendlyByteBuf) {
            return new CavityAction(friendlyByteBuf.m_130135_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130281_());
        }

        private static void handle(CavityAction cavityAction, Supplier<NetworkEvent.Context> supplier) {
            supplier.get().enqueueWork(() -> {
                ServerPlayer serverPlayer = ((NetworkEvent.Context)supplier.get()).getSender();
                if (serverPlayer == null || serverPlayer.f_36096_ instanceof SwordCavityMenu) {
                    return;
                }
                try {
                    if (StatueMenuNetwork.validatedStatuePos(serverPlayer, cavityAction.pos) == null || SwordCavityRules.Slot.byIndex(cavityAction.part) == null || cavityAction.inventorySlot < 0 || cavityAction.inventorySlot >= 36 || cavityAction.revision < 0) {
                        return;
                    }
                    Integer n = LAST_ACTION_TICK.get(serverPlayer);
                    if (n != null && serverPlayer.f_19797_ - n < 4) {
                        return;
                    }
                    LAST_ACTION_TICK.put(serverPlayer, serverPlayer.f_19797_);
                    ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
                    ItemStack itemStack2 = serverPlayer.m_150109_().m_8020_(cavityAction.inventorySlot);
                    if (!cavityAction.weapon.equals(FoodWeaponLifecycle.weaponId(itemStack)) || cavityAction.revision != SwordCavityBuild.revision(itemStack) || !cavityAction.offering.equals((Object)ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_()))) {
                        return;
                    }
                    SwordCavityBuild.replace(serverPlayer, SwordCavityRules.Slot.byIndex(cavityAction.part), cavityAction.inventorySlot, cavityAction.revision);
                }
                finally {
                    serverPlayer.m_150109_().m_6596_();
                    serverPlayer.f_36095_.m_38946_();
                    if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                        serverPlayer.f_36096_.m_38946_();
                    }
                    if (StatueMenuNetwork.validatedStatuePos(serverPlayer, cavityAction.pos) != null) {
                        StatueMenuNetwork.open(serverPlayer, cavityAction.pos);
                    }
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new ActionResult(cavityAction.pos));
                }
            });
            supplier.get().setPacketHandled(true);
        }
    }

    public record CavityLayoutAction(BlockPos pos, int action, int from, int to, UUID weapon, int revision) {
        public static final int ACTION_MOVE = 0;
        public static final int ACTION_REMOVE = 1;

        public static boolean send(BlockPos blockPos, int n, SwordCavityRules.Slot slot, SwordCavityRules.Slot slot2, ItemStack itemStack) {
            UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
            if (uUID == null || slot == null || slot2 == null || n != 0 && n != 1) {
                return false;
            }
            if (n == 0 ? !SwordCavityBuild.canMove(itemStack, slot, slot2) : !SwordCavityBuild.canRemove(itemStack, slot)) {
                return false;
            }
            CHANNEL.sendToServer((Object)new CavityLayoutAction(blockPos, n, slot.ordinal(), slot2.ordinal(), uUID, SwordCavityBuild.revision(itemStack)));
            return true;
        }

        private static void encode(CavityLayoutAction cavityLayoutAction, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130064_(cavityLayoutAction.pos);
            friendlyByteBuf.m_130130_(cavityLayoutAction.action);
            friendlyByteBuf.m_130130_(cavityLayoutAction.from);
            friendlyByteBuf.m_130130_(cavityLayoutAction.to);
            friendlyByteBuf.m_130077_(cavityLayoutAction.weapon);
            friendlyByteBuf.m_130130_(cavityLayoutAction.revision);
        }

        private static CavityLayoutAction decode(FriendlyByteBuf friendlyByteBuf) {
            return new CavityLayoutAction(friendlyByteBuf.m_130135_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_());
        }

        private static void handle(CavityLayoutAction cavityLayoutAction, Supplier<NetworkEvent.Context> supplier) {
            supplier.get().enqueueWork(() -> {
                ServerPlayer serverPlayer = ((NetworkEvent.Context)supplier.get()).getSender();
                if (serverPlayer == null || serverPlayer.f_36096_ instanceof SwordCavityMenu) {
                    return;
                }
                try {
                    SwordCavityRules.Slot slot = SwordCavityRules.Slot.byIndex(cavityLayoutAction.from);
                    SwordCavityRules.Slot slot2 = SwordCavityRules.Slot.byIndex(cavityLayoutAction.to);
                    if (StatueMenuNetwork.validatedStatuePos(serverPlayer, cavityLayoutAction.pos) == null || slot == null || slot2 == null || cavityLayoutAction.revision < 0 || cavityLayoutAction.action != 0 && cavityLayoutAction.action != 1) {
                        return;
                    }
                    Integer n = LAST_ACTION_TICK.get(serverPlayer);
                    if (n != null && serverPlayer.f_19797_ - n < 4) {
                        return;
                    }
                    LAST_ACTION_TICK.put(serverPlayer, serverPlayer.f_19797_);
                    ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
                    if (!cavityLayoutAction.weapon.equals(FoodWeaponLifecycle.weaponId(itemStack)) || cavityLayoutAction.revision != SwordCavityBuild.revision(itemStack)) {
                        return;
                    }
                    if (cavityLayoutAction.action == 0) {
                        SwordCavityBuild.move(serverPlayer, slot, slot2, cavityLayoutAction.revision);
                    } else {
                        SwordCavityBuild.remove(serverPlayer, slot, cavityLayoutAction.revision);
                    }
                }
                finally {
                    serverPlayer.m_150109_().m_6596_();
                    serverPlayer.f_36095_.m_38946_();
                    if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                        serverPlayer.f_36096_.m_38946_();
                    }
                    if (StatueMenuNetwork.validatedStatuePos(serverPlayer, cavityLayoutAction.pos) != null) {
                        StatueMenuNetwork.open(serverPlayer, cavityLayoutAction.pos);
                    }
                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new ActionResult(cavityLayoutAction.pos));
                }
            });
            supplier.get().setPacketHandled(true);
        }
    }

    public record CavityOpenAction(BlockPos pos, UUID weapon, int revision) {
        public static boolean send(BlockPos blockPos, ItemStack itemStack) {
            UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
            if (uUID == null || !SwordCavityBuild.applies(itemStack)) {
                return false;
            }
            CHANNEL.sendToServer((Object)new CavityOpenAction(blockPos, uUID, SwordCavityBuild.revision(itemStack)));
            return true;
        }

        private static void encode(CavityOpenAction cavityOpenAction, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130064_(cavityOpenAction.pos);
            friendlyByteBuf.m_130077_(cavityOpenAction.weapon);
            friendlyByteBuf.m_130130_(cavityOpenAction.revision);
        }

        private static CavityOpenAction decode(FriendlyByteBuf friendlyByteBuf) {
            return new CavityOpenAction(friendlyByteBuf.m_130135_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130242_());
        }

        private static void handle(CavityOpenAction cavityOpenAction, Supplier<NetworkEvent.Context> supplier) {
            supplier.get().enqueueWork(() -> {
                ServerPlayer serverPlayer = ((NetworkEvent.Context)supplier.get()).getSender();
                if (serverPlayer == null || serverPlayer.f_36096_ != serverPlayer.f_36095_ || serverPlayer.m_5803_() || serverPlayer.m_6117_()) {
                    return;
                }
                BlockPos blockPos = StatueMenuNetwork.validatedStatuePos(serverPlayer, cavityOpenAction.pos);
                ItemStack itemStack = FoodWeaponEvolution.heldEvolutionWeapon(serverPlayer);
                if (blockPos == null || !SwordCavityBuild.applies(itemStack) || itemStack.m_41613_() != 1 || cavityOpenAction.revision < 0 || !cavityOpenAction.weapon.equals(FoodWeaponLifecycle.weaponId(itemStack)) || cavityOpenAction.revision != SwordCavityBuild.revision(itemStack)) {
                    return;
                }
                int n = serverPlayer.m_21205_() == itemStack ? serverPlayer.m_150109_().f_35977_ : 40;
                SwordNativeMaterials.capture(itemStack, (Level)serverPlayer.m_284548_());
                serverPlayer.f_36095_.m_38946_();
                int[] nArray = SwordCavityExtensions.layout(itemStack);
                NetworkHooks.openScreen((ServerPlayer)serverPlayer, (MenuProvider)new SimpleMenuProvider((n2, inventory, player) -> new SwordCavityMenu(n2, inventory, blockPos, n, cavityOpenAction.weapon, nArray), (Component)Component.m_237115_((String)"screen.ftboss.cavity.title")), friendlyByteBuf -> {
                    friendlyByteBuf.m_130064_(blockPos);
                    friendlyByteBuf.m_130130_(n);
                    friendlyByteBuf.m_130077_(cavityOpenAction.weapon);
                    for (int n2 : nArray) {
                        friendlyByteBuf.m_130130_(n2);
                    }
                });
            });
            supplier.get().setPacketHandled(true);
        }
    }
}

