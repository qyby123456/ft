/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.server.ServerStartingEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.network.NetworkHooks
 */
package net.farmingtales.ftboss.transaction;

import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.ExistingTransactionBoundary;
import net.farmingtales.ftboss.transaction.ForgeTransactionHost;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.TransactionAdmission;
import net.farmingtales.ftboss.transaction.TransactionLifecycle;
import net.farmingtales.ftboss.transaction.TransactionWire;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;

public final class FourWeaponTransactionRuntime {
    private static FourWeaponTransactions authority;
    private static ForgeTransactionHost host;
    private static boolean registered;

    private FourWeaponTransactionRuntime() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        FourWeaponTransactionNetwork.register();
        MinecraftForge.EVENT_BUS.register(FourWeaponTransactionRuntime.class);
    }

    @SubscribeEvent
    public static void starting(ServerStartingEvent serverStartingEvent) {
        OwnerStatueTaskService.start(serverStartingEvent.getServer());
        ExistingTransactionBoundary existingTransactionBoundary = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        host = new ForgeTransactionHost(uUID -> serverStartingEvent.getServer().m_6846_().m_11259_(uUID), existingTransactionBoundary);
        authority = new FourWeaponTransactions(host, 16384);
        FourWeaponTransactionNetwork.server(authority);
        MinecraftForge.EVENT_BUS.register((Object)new TransactionLifecycle(authority));
    }

    public static FourWeaponTransactions.Snapshot current(UUID uUID) {
        return authority == null ? null : authority.current(uUID);
    }

    public static void close(UUID uUID, UUID uUID2) {
        if (authority != null) {
            authority.close(uUID, uUID2);
        }
    }

    public static boolean executing(UUID uUID, FourWeaponTransactions.Operation operation) {
        return authority != null && authority.executing(uUID, operation);
    }

    public static void stop() {
        authority = null;
        host = null;
    }

    public static FourWeaponTransactions.Reply openTarget(ServerPlayer serverPlayer, TransactionWire.TargetOpen targetOpen) {
        if (!TransactionAdmission.target(serverPlayer, targetOpen, FourWeaponTransactionNetwork.negotiated(serverPlayer))) {
            return new FourWeaponTransactions.Reply(targetOpen.request(), FourWeaponTransactions.Code.DENIED, null);
        }
        if (authority == null || host == null || targetOpen.hand() < 0 || targetOpen.hand() > 1 || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_() || serverPlayer.f_36096_ != serverPlayer.f_36095_ || !serverPlayer.m_9236_().m_46472_().m_135782_().toString().equals(targetOpen.dimension())) {
            return new FourWeaponTransactions.Reply(targetOpen.request(), FourWeaponTransactions.Code.DENIED, null);
        }
        ItemStack itemStack = serverPlayer.m_21120_(targetOpen.hand() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (!FourWeaponFoundationAdapter.read(itemStack).valid()) {
            return new FourWeaponTransactions.Reply(targetOpen.request(), FourWeaponTransactions.Code.DENIED, null);
        }
        BlockPos blockPos = BlockPos.m_122022_((long)targetOpen.statue());
        if (!targetOpen.weapon().equals(FoodWeaponLifecycle.weaponId(itemStack)) || targetOpen.revision() != FourWeaponFoundationAdapter.revision(itemStack) || !targetOpen.fingerprint().equals(ForgeTransactionHost.fingerprint(itemStack))) {
            return new FourWeaponTransactions.Reply(targetOpen.request(), FourWeaponTransactions.Code.STALE, null);
        }
        ExistingTransactionBoundary existingTransactionBoundary = new ExistingTransactionBoundary(Set.of(), new FoundationApprovedRules());
        if (!serverPlayer.m_9236_().m_46805_(blockPos)) {
            return new FourWeaponTransactions.Reply(targetOpen.request(), FourWeaponTransactions.Code.DENIED, null);
        }
        String string = existingTransactionBoundary.statueIdentity(serverPlayer, blockPos);
        if (string == null || !serverPlayer.m_9236_().m_46805_(blockPos) || serverPlayer.m_20275_((double)blockPos.m_123341_() + 0.5, (double)(blockPos.m_123342_() + 1), (double)blockPos.m_123343_() + 0.5) > 64.0 || existingTransactionBoundary.policy(serverPlayer, itemStack, blockPos) == null) {
            return new FourWeaponTransactions.Reply(targetOpen.request(), FourWeaponTransactions.Code.DENIED, null);
        }
        FourWeaponTransactions.Binding binding = new FourWeaponTransactions.Binding(serverPlayer.m_20148_(), FoodWeaponLifecycle.weaponId(itemStack), FourWeaponFoundationAdapter.registryId(itemStack), targetOpen.hand(), targetOpen.hand() == 0 ? serverPlayer.m_150109_().f_35977_ : 40, targetOpen.dimension(), targetOpen.statue(), string);
        return authority.open(serverPlayer.m_20148_(), new FourWeaponTransactions.Open(targetOpen.request(), binding));
    }

    public static boolean openContainer(ServerPlayer serverPlayer, FourWeaponTransactions.Request request) {
        FourWeaponTransactions.Snapshot snapshot = FourWeaponTransactionRuntime.current(serverPlayer.m_20148_());
        if (snapshot == null || !snapshot.session().equals(request.session()) || serverPlayer.f_36096_ != serverPlayer.f_36095_ || !serverPlayer.f_36095_.m_142621_().m_41619_()) {
            return false;
        }
        ItemStack itemStack = serverPlayer.m_150109_().m_8020_(request.binding().inventorySlot());
        if (!FourWeaponFoundationAdapter.read(itemStack).valid()) {
            return false;
        }
        BlockPos blockPos = BlockPos.m_122022_((long)request.binding().statue());
        int[] nArray = SwordCavityExtensions.layout(itemStack);
        NetworkHooks.openScreen((ServerPlayer)serverPlayer, (MenuProvider)new SimpleMenuProvider((n, inventory, player) -> {
            SwordCavityMenu swordCavityMenu = new SwordCavityMenu(n, inventory, blockPos, request.binding().inventorySlot(), request.binding().weapon(), nArray);
            swordCavityMenu.bindTransaction(snapshot);
            return swordCavityMenu;
        }, (Component)Component.m_237115_((String)"screen.ftboss.cavity.title")), friendlyByteBuf -> {
            friendlyByteBuf.m_130064_(blockPos);
            friendlyByteBuf.m_130130_(request.binding().inventorySlot());
            friendlyByteBuf.m_130077_(request.binding().weapon());
            for (int n : nArray) {
                friendlyByteBuf.m_130130_(n);
            }
        });
        return serverPlayer.f_36096_ instanceof SwordCavityMenu;
    }
}

