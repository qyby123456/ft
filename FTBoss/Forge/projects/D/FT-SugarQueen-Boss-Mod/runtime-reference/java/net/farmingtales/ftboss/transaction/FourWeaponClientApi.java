/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.transaction;

import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FoundationEntryPoints;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionClient;
import net.farmingtales.ftboss.transaction.TransactionWire;
import net.farmingtales.ftboss.transaction.WeaponUiSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class FourWeaponClientApi {
    private static TransactionClient transactions;
    private static WeaponUiSnapshot snapshot;
    private static Consumer<WeaponUiSnapshot> receiver;
    private static boolean openContainerPending;
    private static BlockPos bootstrapStatue;
    private static int bootstrapTicks;
    private static BlockPos rejectedHubAction;

    private FourWeaponClientApi() {
    }

    public static void initialize() {
        transactions = new TransactionClient(FourWeaponTransactionNetwork::send);
        FourWeaponTransactionNetwork.client(reply -> {
            BlockPos blockPos;
            FourWeaponTransactions.Snapshot snapshot = transactions.authority();
            FourWeaponTransactions.Request request = transactions.pending();
            BlockPos blockPos2 = request != null ? BlockPos.m_122022_((long)request.binding().statue()) : (blockPos = snapshot != null ? BlockPos.m_122022_((long)snapshot.binding().statue()) : null);
            if (transactions.receive((FourWeaponTransactions.Reply)reply)) {
                if (!transactions.menuInputActive()) {
                    snapshot = null;
                }
                FourWeaponClientApi.observe();
                if (blockPos != null) {
                    StatueHubScreen.acknowledgeAction(blockPos);
                }
            }
        });
        FourWeaponTransactionNetwork.snapshots(weaponUiSnapshot -> {
            FourWeaponClientApi.observe();
            if (transactions.confirmSnapshot(weaponUiSnapshot.request(), weaponUiSnapshot.session(), weaponUiSnapshot.data().m_128454_("Sequence"), weaponUiSnapshot.data().m_128461_("Fingerprint"), weaponUiSnapshot.data().m_128461_("Rules"), weaponUiSnapshot.data().m_128451_("GridRevision"), weaponUiSnapshot.data().m_128451_("Level"), weaponUiSnapshot.data().m_128465_("Layout"), weaponUiSnapshot.data().m_128451_("Container"))) {
                snapshot = weaponUiSnapshot;
                FourWeaponClientApi.observe();
                if (receiver != null) {
                    receiver.accept((WeaponUiSnapshot)weaponUiSnapshot);
                }
                if (openContainerPending && FourWeaponClientApi.canWrite(FourWeaponTransactions.Operation.OPEN_CONTAINER)) {
                    openContainerPending = false;
                    FourWeaponClientApi.submit(FourWeaponTransactions.Operation.OPEN_CONTAINER, -1, 0);
                }
            } else if (transactions.authority() == null) {
                snapshot = null;
            }
        });
    }

    public static boolean capable() {
        return transactions != null && FourWeaponTransactionNetwork.ready();
    }

    public static void listen(Consumer<WeaponUiSnapshot> consumer) {
        receiver = consumer;
    }

    public static WeaponUiSnapshot snapshot() {
        return snapshot;
    }

    public static FourWeaponTransactions.Snapshot authority() {
        return transactions == null ? null : transactions.authority();
    }

    public static boolean open(InteractionHand interactionHand, BlockPos blockPos) {
        openContainerPending = false;
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        if (!FourWeaponClientApi.capable() || localPlayer == null) {
            return false;
        }
        ItemStack itemStack = localPlayer.m_21120_(interactionHand);
        UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
        if (uUID == null || !FourWeaponFoundationAdapter.read(itemStack).valid()) {
            return false;
        }
        snapshot = null;
        bootstrapStatue = null;
        return transactions.openTarget(interactionHand == InteractionHand.MAIN_HAND ? 0 : 1, localPlayer.m_9236_().m_46472_().m_135782_().toString(), blockPos.m_121878_(), uUID, FourWeaponFoundationAdapter.revision(itemStack), ForgeTransactionHost.fingerprint(itemStack));
    }

    public static boolean openContainer(ItemStack itemStack, BlockPos blockPos) {
        boolean bl;
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        if (localPlayer == null || itemStack != localPlayer.m_21205_() && itemStack != localPlayer.m_21206_()) {
            return false;
        }
        openContainerPending = bl = FourWeaponClientApi.open(itemStack == localPlayer.m_21205_() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockPos);
        return bl;
    }

    public static void offeredContainer(TransactionWire.TargetOpen targetOpen) {
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        if (!FourWeaponClientApi.capable() || localPlayer == null || !localPlayer.m_9236_().m_46472_().m_135782_().toString().equals(targetOpen.dimension())) {
            return;
        }
        ItemStack itemStack = localPlayer.m_21120_(targetOpen.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (targetOpen.weapon().equals(FoodWeaponLifecycle.weaponId(itemStack)) && targetOpen.revision() == FourWeaponFoundationAdapter.revision(itemStack) && targetOpen.fingerprint().equals(ForgeTransactionHost.fingerprint(itemStack))) {
            FourWeaponClientApi.openContainer(itemStack, BlockPos.m_122022_((long)targetOpen.statue()));
        }
    }

    private static void observe() {
        if (transactions == null || transactions.authority() == null) {
            return;
        }
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        transactions.observeContainer(localPlayer == null ? -1 : localPlayer.f_36096_.f_38840_);
        if (transactions.authority() == null) {
            return;
        }
        FourWeaponTransactions.Binding binding = transactions.authority().binding();
        if (localPlayer == null || !localPlayer.m_20148_().equals(binding.owner()) || !localPlayer.m_9236_().m_46472_().m_135782_().toString().equals(binding.dimension())) {
            transactions.observe(null, "");
            return;
        }
        ItemStack itemStack = localPlayer.m_21120_(binding.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!binding.weapon().equals(FoodWeaponLifecycle.weaponId(itemStack)) || !binding.item().equals(FourWeaponFoundationAdapter.registryId(itemStack)) || binding.hand() == 0 && localPlayer.m_150109_().f_35977_ != binding.inventorySlot()) {
            transactions.observe(null, "");
        } else {
            transactions.observe(binding, ForgeTransactionHost.fingerprint(itemStack));
        }
    }

    public static boolean canWrite(FourWeaponTransactions.Operation operation) {
        FourWeaponClientApi.observe();
        FourWeaponTransactions.Snapshot snapshot = FourWeaponClientApi.authority();
        return FourWeaponClientApi.capable() && FourWeaponClientApi.snapshot != null && snapshot != null && FourWeaponClientApi.snapshot.session().equals(snapshot.session()) && FourWeaponClientApi.snapshot.data().m_128454_("Sequence") == snapshot.version().sequence() && FourWeaponClientApi.snapshot.data().m_128461_("Fingerprint").equals(snapshot.version().fingerprint()) && transactions.canWrite(operation);
    }

    public static boolean submit(FourWeaponTransactions.Operation operation, int n, int n2) {
        if (n < -999 || n > 62 || n2 < 0 || n2 > 40) {
            return false;
        }
        if ((operation == FourWeaponTransactions.Operation.BUILD_CELL || operation == FourWeaponTransactions.Operation.EVOLVE) && n2 >= 36) {
            return false;
        }
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        if (FourWeaponTransactions.menuOperation(operation) || operation == FourWeaponTransactions.Operation.REMOVE_CELL && localPlayer != null && localPlayer.f_36096_ instanceof SwordCavityMenu) {
            FourWeaponClientApi.observe();
            return FourWeaponClientApi.capable() && localPlayer != null && localPlayer.f_36096_ instanceof SwordCavityMenu && transactions.enqueueMenu(localPlayer.f_36096_.f_38840_, operation, n, n2);
        }
        if (!FourWeaponClientApi.canWrite(operation)) {
            return false;
        }
        String string = operation == FourWeaponTransactions.Operation.BUILD_CELL || operation == FourWeaponTransactions.Operation.EVOLVE ? ForgeTransactionHost.fingerprint(localPlayer.m_150109_().m_8020_(n2)) : "";
        try {
            return transactions.submit(UUID.randomUUID(), operation, n, n2, string);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    public static boolean menuClick(int n, int n2, int n3, ClickType clickType) {
        Object object;
        block10: {
            block9: {
                LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
                if (localPlayer == null || !((object = localPlayer.f_36096_) instanceof SwordCavityMenu)) break block9;
                SwordCavityMenu swordCavityMenu = (SwordCavityMenu)((Object)object);
                if (swordCavityMenu.f_38840_ == n) break block10;
            }
            return false;
        }
        object = switch (clickType) {
            case ClickType.PICKUP -> FourWeaponTransactions.Operation.MENU_PICKUP;
            case ClickType.QUICK_MOVE -> FourWeaponTransactions.Operation.MENU_QUICK_MOVE;
            case ClickType.SWAP -> FourWeaponTransactions.Operation.MENU_SWAP;
            case ClickType.QUICK_CRAFT -> FourWeaponTransactions.Operation.MENU_QUICK_CRAFT;
            case ClickType.PICKUP_ALL -> FourWeaponTransactions.Operation.MENU_PICKUP_ALL;
            default -> null;
        };
        return object != null && FourWeaponClientApi.submit((FourWeaponTransactions.Operation)((Object)object), n2, n3);
    }

    public static void bootstrapHub(BlockPos blockPos) {
        FourWeaponClientApi.observe();
        FourWeaponTransactions.Snapshot snapshot = FourWeaponClientApi.authority();
        if (snapshot != null && snapshot.binding().statue() == blockPos.m_121878_()) {
            return;
        }
        openContainerPending = false;
        bootstrapStatue = blockPos.m_7949_();
        bootstrapTicks = 0;
    }

    private static ItemStack foundationTarget() {
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        if (localPlayer == null) {
            return ItemStack.f_41583_;
        }
        ItemStack itemStack = localPlayer.m_21205_();
        if (FourWeaponClientApi.hubCandidate(itemStack)) {
            return itemStack;
        }
        ItemStack itemStack2 = localPlayer.m_21206_();
        return FourWeaponClientApi.hubCandidate(itemStack2) ? itemStack2 : ItemStack.f_41583_;
    }

    private static boolean hubCandidate(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack) || FoodWeaponEvolution.isEvolutionCandidate(itemStack);
    }

    public static boolean sendLegacyAction(BlockPos blockPos, ItemStack itemStack, int n, int n2) {
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        FourWeaponTransactions.Operation operation = FoundationEntryPoints.legacyOperation(n);
        FourWeaponTransactions.Snapshot snapshot = FourWeaponClientApi.authority();
        if (localPlayer == null || operation == null || snapshot == null || !FourWeaponFoundationAdapter.applies(itemStack) || snapshot.binding().statue() != blockPos.m_121878_() || localPlayer.m_150109_().m_8020_(snapshot.binding().inventorySlot()) != itemStack || !snapshot.binding().weapon().equals(FoodWeaponLifecycle.weaponId(itemStack))) {
            return false;
        }
        return FourWeaponClientApi.submit(operation, -1, n2);
    }

    public static boolean interceptLegacyAction(BlockPos blockPos, int n, int n2) {
        LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
        ItemStack itemStack = FourWeaponClientApi.foundationTarget();
        if (localPlayer == null || FoundationEntryPoints.legacyGlobalAction(n) || !FourWeaponFoundationAdapter.applies(itemStack)) {
            return false;
        }
        if (!FourWeaponClientApi.sendLegacyAction(blockPos, itemStack, n, n2)) {
            FourWeaponClientApi.rejectHubAction(blockPos);
        }
        return true;
    }

    public static void rejectHubAction(BlockPos blockPos) {
        rejectedHubAction = blockPos.m_7949_();
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        BlockPos blockPos;
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        if (rejectedHubAction != null) {
            blockPos = rejectedHubAction;
            rejectedHubAction = null;
            StatueHubScreen.acknowledgeAction(blockPos);
        }
        blockPos = Minecraft.m_91087_().f_91074_;
        if (transactions != null) {
            transactions.observeContainer(blockPos == null ? -1 : blockPos.f_36096_.f_38840_);
            FourWeaponClientApi.observe();
            transactions.menuTick(blockPos == null ? -1 : blockPos.f_36096_.f_38840_);
        }
        if (openContainerPending && FourWeaponClientApi.canWrite(FourWeaponTransactions.Operation.OPEN_CONTAINER)) {
            openContainerPending = false;
            FourWeaponClientApi.submit(FourWeaponTransactions.Operation.OPEN_CONTAINER, -1, 0);
        }
        if (bootstrapStatue == null) {
            return;
        }
        if (blockPos == null || !(Minecraft.m_91087_().f_91080_ instanceof StatueHubScreen) || ++bootstrapTicks >= 200) {
            bootstrapStatue = null;
            return;
        }
        ItemStack itemStack = FourWeaponClientApi.foundationTarget();
        if (FourWeaponClientApi.capable() && FoodWeaponLifecycle.weaponId(itemStack) != null && FourWeaponFoundationAdapter.read(itemStack).valid()) {
            BlockPos blockPos2 = bootstrapStatue;
            bootstrapStatue = null;
            FourWeaponClientApi.open(itemStack == blockPos.m_21205_() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, blockPos2);
        }
    }

    public static void close() {
        if (transactions != null) {
            transactions.close();
        }
        snapshot = null;
        receiver = null;
        openContainerPending = false;
        bootstrapStatue = null;
        rejectedHubAction = null;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        FourWeaponClientApi.close();
        transactions = null;
        FourWeaponTransactionNetwork.clearClient();
    }
}

