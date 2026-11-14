package net.farmingtales.ftboss.transaction;

import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.client.OrdinaryStatueHubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.farmingtales.ftboss.transaction.TransactionWire.TargetOpen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
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
        FourWeaponTransactionNetwork.client(var0 -> {
            Snapshot var1 = transactions.authority();
            Request var2 = transactions.pending();
            BlockPos var3 = var2 != null ? BlockPos.of(var2.binding().statue()) : (var1 != null ? BlockPos.of(var1.binding().statue()) : null);
            if (transactions.receive(var0)) {
                if (!transactions.menuInputActive()) {
                    snapshot = null;
                }

                observe();
                if (var3 != null) {
                    StatueHubScreen.acknowledgeAction(var3);
                    OrdinaryStatueHubScreen.acknowledgeAction(var3);
                }
            }
        });
        FourWeaponTransactionNetwork.snapshots(
            var0 -> {
                observe();
                if (transactions.confirmSnapshot(
                    var0.request(),
                    var0.session(),
                    var0.data().getLong("Sequence"),
                    var0.data().getString("Fingerprint"),
                    var0.data().getString("Rules"),
                    var0.data().getInt("GridRevision"),
                    var0.data().getInt("Level"),
                    var0.data().getIntArray("Layout"),
                    var0.data().getInt("Container")
                )) {
                    snapshot = var0;
                    observe();
                    if (receiver != null) {
                        receiver.accept(var0);
                    }

                    if (openContainerPending && canWrite(Operation.OPEN_CONTAINER)) {
                        openContainerPending = false;
                        submit(Operation.OPEN_CONTAINER, -1, 0);
                    }
                } else if (transactions.authority() == null) {
                    snapshot = null;
                }
            }
        );
    }

    public static boolean capable() {
        return transactions != null && FourWeaponTransactionNetwork.ready();
    }

    public static void listen(Consumer<WeaponUiSnapshot> var0) {
        receiver = var0;
    }

    public static WeaponUiSnapshot snapshot() {
        return snapshot;
    }

    public static Snapshot authority() {
        return transactions == null ? null : transactions.authority();
    }

    public static boolean open(InteractionHand var0, BlockPos var1) {
        openContainerPending = false;
        LocalPlayer var2 = Minecraft.getInstance().player;
        if (capable() && var2 != null) {
            ItemStack var3 = var2.getItemInHand(var0);
            UUID var4 = FoodWeaponLifecycle.weaponId(var3);
            if (var4 != null && FourWeaponFoundationAdapter.read(var3).valid()) {
                snapshot = null;
                bootstrapStatue = null;
                return transactions.openTarget(
                    var0 == InteractionHand.MAIN_HAND ? 0 : 1,
                    var2.level().dimension().location().toString(),
                    var1.asLong(),
                    var4,
                    FourWeaponFoundationAdapter.revision(var3),
                    ForgeTransactionHost.fingerprint(var3, var2.registryAccess())
                );
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean openContainer(ItemStack var0, BlockPos var1) {
        LocalPlayer var2 = Minecraft.getInstance().player;
        if (var2 != null && (var0 == var2.getMainHandItem() || var0 == var2.getOffhandItem())) {
            boolean var3 = open(var0 == var2.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, var1);
            openContainerPending = var3;
            return var3;
        } else {
            return false;
        }
    }

    public static void offeredContainer(TargetOpen var0) {
        LocalPlayer var1 = Minecraft.getInstance().player;
        if (capable() && var1 != null && var1.level().dimension().location().toString().equals(var0.dimension())) {
            ItemStack var2 = var1.getItemInHand(var0.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (var0.weapon().equals(FoodWeaponLifecycle.weaponId(var2))
                && var0.revision() == FourWeaponFoundationAdapter.revision(var2)
                && var0.fingerprint().equals(ForgeTransactionHost.fingerprint(var2, var1.registryAccess()))) {
                openContainer(var2, BlockPos.of(var0.statue()));
            }
        }
    }

    private static void observe() {
        if (transactions != null && transactions.authority() != null) {
            LocalPlayer var0 = Minecraft.getInstance().player;
            transactions.observeContainer(var0 == null ? -1 : var0.containerMenu.containerId);
            if (transactions.authority() != null) {
                Binding var1 = transactions.authority().binding();
                if (var0 != null && var0.getUUID().equals(var1.owner()) && var0.level().dimension().location().toString().equals(var1.dimension())) {
                    ItemStack var2 = var0.getItemInHand(var1.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                    if (var1.weapon().equals(FoodWeaponLifecycle.weaponId(var2))
                        && var1.item().equals(FourWeaponFoundationAdapter.registryId(var2))
                        && (var1.hand() != 0 || var0.getInventory().selected == var1.inventorySlot())) {
                        transactions.observe(var1, ForgeTransactionHost.fingerprint(var2, var0.registryAccess()));
                    } else {
                        transactions.observe(null, "");
                    }
                } else {
                    transactions.observe(null, "");
                }
            }
        }
    }

    public static boolean canWrite(Operation var0) {
        observe();
        Snapshot var1 = authority();
        return capable()
            && snapshot != null
            && var1 != null
            && snapshot.session().equals(var1.session())
            && snapshot.data().getLong("Sequence") == var1.version().sequence()
            && snapshot.data().getString("Fingerprint").equals(var1.version().fingerprint())
            && transactions.canWrite(var0);
    }

    public static boolean submit(Operation var0, int var1, int var2) {
        if (var1 >= -999 && var1 <= 62 && var2 >= 0 && var2 <= 40) {
            if ((var0 == Operation.BUILD_CELL || var0 == Operation.EVOLVE) && var2 >= 36) {
                return false;
            } else {
                LocalPlayer var3 = Minecraft.getInstance().player;
                if (FourWeaponTransactions.menuOperation(var0)
                    || var0 == Operation.REMOVE_CELL && var3 != null && var3.containerMenu instanceof SwordCavityMenu) {
                    observe();
                    return capable()
                        && var3 != null
                        && var3.containerMenu instanceof SwordCavityMenu
                        && transactions.enqueueMenu(var3.containerMenu.containerId, var0, var1, var2);
                } else if (!canWrite(var0)) {
                    return false;
                } else {
                    String var4 = var0 != Operation.BUILD_CELL && var0 != Operation.EVOLVE
                        ? ""
                        : ForgeTransactionHost.fingerprint(var3.getInventory().getItem(var2), var3.registryAccess());

                    try {
                        return transactions.submit(UUID.randomUUID(), var0, var1, var2, var4);
                    } catch (IllegalArgumentException var6) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    public static boolean menuClick(int var0, int var1, int var2, ClickType var3) {
        LocalPlayer var4 = Minecraft.getInstance().player;
        if (var4 != null && var4.containerMenu instanceof SwordCavityMenu var5 && var5.containerId == var0) {
            Operation var7 = switch (var3) {
                case PICKUP -> Operation.MENU_PICKUP;
                case QUICK_MOVE -> Operation.MENU_QUICK_MOVE;
                case SWAP -> Operation.MENU_SWAP;
                case QUICK_CRAFT -> Operation.MENU_QUICK_CRAFT;
                case PICKUP_ALL -> Operation.MENU_PICKUP_ALL;
                default -> null;
            };
            return var7 != null && submit(var7, var1, var2);
        } else {
            return false;
        }
    }

    public static void bootstrapHub(BlockPos var0) {
        observe();
        Snapshot var1 = authority();
        if (var1 == null || var1.binding().statue() != var0.asLong()) {
            openContainerPending = false;
            bootstrapStatue = var0.immutable();
            bootstrapTicks = 0;
        }
    }

    private static ItemStack foundationTarget() {
        LocalPlayer var0 = Minecraft.getInstance().player;
        if (var0 == null) {
            return ItemStack.EMPTY;
        } else {
            ItemStack var1 = var0.getMainHandItem();
            if (hubCandidate(var1)) {
                return var1;
            } else {
                ItemStack var2 = var0.getOffhandItem();
                return hubCandidate(var2) ? var2 : ItemStack.EMPTY;
            }
        }
    }

    private static boolean hubCandidate(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) || SwordCavityBuild.applies(var0) || FoodWeaponEvolution.isEvolutionCandidate(var0);
    }

    public static boolean sendLegacyAction(BlockPos var0, ItemStack var1, int var2, int var3) {
        LocalPlayer var4 = Minecraft.getInstance().player;
        Operation var5 = FoundationEntryPoints.legacyOperation(var2);
        Snapshot var6 = authority();
        return var4 != null
                && var5 != null
                && var6 != null
                && FourWeaponFoundationAdapter.applies(var1)
                && var6.binding().statue() == var0.asLong()
                && var4.getInventory().getItem(var6.binding().inventorySlot()) == var1
                && var6.binding().weapon().equals(FoodWeaponLifecycle.weaponId(var1))
            ? submit(var5, -1, var3)
            : false;
    }

    public static boolean interceptLegacyAction(BlockPos var0, int var1, int var2) {
        LocalPlayer var3 = Minecraft.getInstance().player;
        ItemStack var4 = foundationTarget();
        if (var3 != null && !FoundationEntryPoints.legacyGlobalAction(var1) && FourWeaponFoundationAdapter.applies(var4)) {
            if (!sendLegacyAction(var0, var4, var1, var2)) {
                rejectHubAction(var0);
            }

            return true;
        } else {
            return false;
        }
    }

    public static void rejectHubAction(BlockPos var0) {
        rejectedHubAction = var0.immutable();
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        {
            if (rejectedHubAction != null) {
                BlockPos var1 = rejectedHubAction;
                rejectedHubAction = null;
                StatueHubScreen.acknowledgeAction(var1);
                    OrdinaryStatueHubScreen.acknowledgeAction(var1);
            }

            LocalPlayer var4 = Minecraft.getInstance().player;
            if (transactions != null) {
                transactions.observeContainer(var4 == null ? -1 : var4.containerMenu.containerId);
                observe();
                transactions.menuTick(var4 == null ? -1 : var4.containerMenu.containerId);
            }

            if (openContainerPending && canWrite(Operation.OPEN_CONTAINER)) {
                openContainerPending = false;
                submit(Operation.OPEN_CONTAINER, -1, 0);
            }

            if (bootstrapStatue != null) {
                if (var4 != null && isHubScreen(Minecraft.getInstance().screen) && ++bootstrapTicks < 200) {
                    ItemStack var2 = foundationTarget();
                    if (capable() && FoodWeaponLifecycle.weaponId(var2) != null && FourWeaponFoundationAdapter.read(var2).valid()) {
                        BlockPos var3 = bootstrapStatue;
                        bootstrapStatue = null;
                        open(var2 == var4.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, var3);
                    }
                } else {
                    bootstrapStatue = null;
                }
            }
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
    public static void logout(LoggingOut var0) {
        close();
        transactions = null;
        FourWeaponTransactionNetwork.clearClient();
    }
    private static boolean isHubScreen(Screen screen) {
        return screen instanceof StatueHubScreen || screen instanceof OrdinaryStatueHubScreen;
    }

}
