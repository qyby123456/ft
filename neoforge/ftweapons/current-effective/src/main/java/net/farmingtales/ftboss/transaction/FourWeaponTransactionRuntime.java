package net.farmingtales.ftboss.transaction;

import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Binding;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Code;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Open;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Operation;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Reply;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Request;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.farmingtales.ftboss.transaction.TransactionWire.TargetOpen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class FourWeaponTransactionRuntime {
    private static FourWeaponTransactions authority;
    private static ForgeTransactionHost host;
    private static boolean registered;

    private FourWeaponTransactionRuntime() {
    }

    public static synchronized void register() {
        if (!registered) {
            registered = true;
            NeoForge.EVENT_BUS.register(FourWeaponTransactionRuntime.class);
        }
    }

    @SubscribeEvent
    public static void starting(ServerStartingEvent var0) {
        OwnerStatueTaskService.start(var0.getServer());
        ExistingTransactionBoundary var1 = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        host = new ForgeTransactionHost(var1x -> var0.getServer().getPlayerList().getPlayer(var1x), var1);
        authority = new FourWeaponTransactions(host, 16384);
        FourWeaponTransactionNetwork.server(authority);
        NeoForge.EVENT_BUS.register(new TransactionLifecycle(authority));
    }

    public static Snapshot current(UUID var0) {
        return authority == null ? null : authority.current(var0);
    }

    public static void close(UUID var0, UUID var1) {
        if (authority != null) {
            authority.close(var0, var1);
        }
    }

    public static boolean executing(UUID var0, Operation var1) {
        return authority != null && authority.executing(var0, var1);
    }

    public static void stop() {
        authority = null;
        host = null;
    }

    public static Reply openTarget(ServerPlayer var0, TargetOpen var1) {
        if (!TransactionAdmission.target(var0, var1, FourWeaponTransactionNetwork.negotiated(var0))) {
            return new Reply(var1.request(), Code.DENIED, null);
        } else if (authority != null
            && host != null
            && var1.hand() >= 0
            && var1.hand() <= 1
            && var0.isAlive()
            && !var0.isSpectator()
            && !var0.isSleeping()
            && !var0.isUsingItem()
            && var0.containerMenu == var0.inventoryMenu
            && var0.level().dimension().location().toString().equals(var1.dimension())) {
            ItemStack var2 = var0.getItemInHand(var1.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (!FourWeaponFoundationAdapter.read(var2).valid()) {
                return new Reply(var1.request(), Code.DENIED, null);
            } else {
                BlockPos var3 = BlockPos.of(var1.statue());
                if (var1.weapon().equals(FoodWeaponLifecycle.weaponId(var2))
                    && var1.revision() == FourWeaponFoundationAdapter.revision(var2)
                    && var1.fingerprint().equals(ForgeTransactionHost.fingerprint(var2, var0.registryAccess()))) {
                    ExistingTransactionBoundary var4 = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
                    if (!var0.level().hasChunkAt(var3)) {
                        return new Reply(var1.request(), Code.DENIED, null);
                    } else {
                        String var5 = var4.statueIdentity(var0, var3);
                        if (var5 != null
                            && var0.level().hasChunkAt(var3)
                            && !(var0.distanceToSqr((double)var3.getX() + 0.5, (double)(var3.getY() + 1), (double)var3.getZ() + 0.5) > 64.0)
                            && var4.policy(var0, var2, var3) != null) {
                            Binding var6 = new Binding(
                                var0.getUUID(),
                                FoodWeaponLifecycle.weaponId(var2),
                                FourWeaponFoundationAdapter.registryId(var2),
                                var1.hand(),
                                var1.hand() == 0 ? var0.getInventory().selected : 40,
                                var1.dimension(),
                                var1.statue(),
                                var5
                            );
                            return authority.open(var0.getUUID(), new Open(var1.request(), var6));
                        } else {
                            return new Reply(var1.request(), Code.DENIED, null);
                        }
                    }
                } else {
                    return new Reply(var1.request(), Code.STALE, null);
                }
            }
        } else {
            return new Reply(var1.request(), Code.DENIED, null);
        }
    }

    public static boolean openContainer(ServerPlayer var0, Request var1) {
        Snapshot var2 = current(var0.getUUID());
        if (var2 != null && var2.session().equals(var1.session()) && var0.containerMenu == var0.inventoryMenu && var0.inventoryMenu.getCarried().isEmpty()) {
            ItemStack var3 = var0.getInventory().getItem(var1.binding().inventorySlot());
            if (!FourWeaponFoundationAdapter.read(var3).valid()) {
                return false;
            } else {
                BlockPos var4 = BlockPos.of(var1.binding().statue());
                int[] var5 = SwordCavityExtensions.layout(var3);
                var0.openMenu(new SimpleMenuProvider((var4x, var5x, var6) -> {
                    SwordCavityMenu var7 = new SwordCavityMenu(var4x, var5x, var4, var1.binding().inventorySlot(), var1.binding().weapon(), var5);
                    var7.bindTransaction(var2);
                    return var7;
                }, Component.translatable("screen.ftboss.cavity.title")), var3x -> {
                    var3x.writeBlockPos(var4);
                    var3x.writeVarInt(var1.binding().inventorySlot());
                    var3x.writeUUID(var1.binding().weapon());

                    for (int var7 : var5) {
                        var3x.writeVarInt(var7);
                    }
                });
                return var0.containerMenu instanceof SwordCavityMenu;
            }
        } else {
            return false;
        }
    }
}
