/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.item.ItemTossEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.level.BlockEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.transaction;

import net.farmingtales.ftboss.item.OwnerStatueTaskService;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponTransactionRuntime;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class TransactionLifecycle {
    private final FourWeaponTransactions transactions;

    public TransactionLifecycle(FourWeaponTransactions fourWeaponTransactions) {
        this.transactions = fourWeaponTransactions;
    }

    @SubscribeEvent
    public void logout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        this.transactions.invalidate(playerLoggedOutEvent.getEntity().m_20148_());
        Player player = playerLoggedOutEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            OwnerStatueTaskService.unloaded(serverPlayer);
        }
    }

    @SubscribeEvent
    public void dimension(PlayerEvent.PlayerChangedDimensionEvent playerChangedDimensionEvent) {
        this.transactions.invalidate(playerChangedDimensionEvent.getEntity().m_20148_());
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone clone) {
        this.transactions.invalidate(clone.getEntity().m_20148_());
    }

    @SubscribeEvent
    public void death(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            this.transactions.invalidate(serverPlayer.m_20148_());
        }
    }

    @SubscribeEvent
    public void toss(ItemTossEvent itemTossEvent) {
        this.transactions.invalidate(itemTossEvent.getPlayer().m_20148_());
    }

    @SubscribeEvent
    public void equipment(LivingEquipmentChangeEvent livingEquipmentChangeEvent) {
        LivingEntity livingEntity = livingEquipmentChangeEvent.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            this.transactions.validate(serverPlayer.m_20148_());
        }
    }

    @SubscribeEvent
    public void block(BlockEvent blockEvent) {
        LevelAccessor levelAccessor = blockEvent.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)levelAccessor;
            this.transactions.invalidateStatue(serverLevel.m_46472_().m_135782_().toString(), blockEvent.getPos().m_121878_(), blockEvent.getPos().m_7495_().m_121878_());
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent playerTickEvent) {
        Player player;
        if (playerTickEvent.phase != TickEvent.Phase.END || !((player = playerTickEvent.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (serverPlayer.m_213877_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_()) {
            this.transactions.invalidate(serverPlayer.m_20148_());
        }
    }

    @SubscribeEvent
    public void stop(ServerStoppedEvent serverStoppedEvent) {
        OwnerStatueTaskService.stop(serverStoppedEvent.getServer());
        FourWeaponTransactionNetwork.stop();
        FourWeaponTransactionRuntime.stop();
        MinecraftForge.EVENT_BUS.unregister((Object)this);
    }

    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        Player player = playerLoggedInEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            OwnerStatueTaskService.loaded(serverPlayer);
            FourWeaponTransactionNetwork.capability(serverPlayer);
        }
    }
}

