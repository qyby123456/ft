package net.farmingtales.ftboss.transaction;

import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class TransactionLifecycle {
    private final FourWeaponTransactions transactions;

    public TransactionLifecycle(FourWeaponTransactions var1) {
        this.transactions = var1;
    }

    @SubscribeEvent
    public void logout(PlayerLoggedOutEvent var1) {
        this.transactions.invalidate(var1.getEntity().getUUID());
        if (var1.getEntity() instanceof ServerPlayer var2) {
            OwnerStatueTaskService.unloaded(var2);
        }
    }

    @SubscribeEvent
    public void dimension(PlayerChangedDimensionEvent var1) {
        this.transactions.invalidate(var1.getEntity().getUUID());
    }

    @SubscribeEvent
    public void clonePlayer(Clone var1) {
        this.transactions.invalidate(var1.getEntity().getUUID());
    }

    @SubscribeEvent
    public void death(LivingDeathEvent var1) {
        if (var1.getEntity() instanceof ServerPlayer var2) {
            this.transactions.invalidate(var2.getUUID());
        }
    }

    @SubscribeEvent
    public void toss(ItemTossEvent var1) {
        this.transactions.invalidate(var1.getPlayer().getUUID());
    }

    @SubscribeEvent
    public void equipment(LivingEquipmentChangeEvent var1) {
        if (var1.getEntity() instanceof ServerPlayer var2) {
            this.transactions.validate(var2.getUUID());
        }
    }

    private void block(BlockEvent var1) {
        if (var1.getLevel() instanceof ServerLevel var2) {
            this.transactions.invalidateStatue(var2.dimension().location().toString(), var1.getPos().asLong(), var1.getPos().below().asLong());
        }
    }

    @SubscribeEvent
    public void blockBroken(BlockEvent.BreakEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void blockPlaced(BlockEvent.EntityPlaceEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void neighborNotified(BlockEvent.NeighborNotifyEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void fluidPlaced(BlockEvent.FluidPlaceBlockEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void farmlandTrampled(BlockEvent.FarmlandTrampleEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void portalSpawned(BlockEvent.PortalSpawnEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void blockToolModified(BlockEvent.BlockToolModificationEvent event) {
        this.block(event);
    }

    @SubscribeEvent
    public void tick(Post var1) {
        if (var1.getEntity() instanceof ServerPlayer var2) {
            if (var2.isRemoved() || !var2.isAlive() || var2.isSpectator() || var2.isSleeping() || var2.isUsingItem()) {
                this.transactions.invalidate(var2.getUUID());
            }
        }
    }

    @SubscribeEvent
    public void stop(ServerStoppedEvent var1) {
        OwnerStatueTaskService.stop(var1.getServer());
        FourWeaponTransactionNetwork.stop();
        FourWeaponTransactionRuntime.stop();
        NeoForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void login(PlayerLoggedInEvent var1) {
        if (var1.getEntity() instanceof ServerPlayer var2) {
            OwnerStatueTaskService.loaded(var2);
            FourWeaponTransactionNetwork.capability(var2);
        }
    }
}
