/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.boss.ManorVictoryData;
import net.farmingtales.ftboss.boss.ManorVictoryLoot;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorVictory {
    private static final String CHESTS_KEY = "Chests";
    private static final String SITES_KEY = "Sites";

    private ManorVictory() {
    }

    public static boolean begin(ServerLevel level, UUID identifier, Vec3 center, Set<UUID> participants) {
        CompoundTag player;
        ManorVictoryData data = ManorVictoryData.get(level.m_7654_());
        if (data.known(identifier)) {
            return false;
        }
        List<ItemStack> rewards = ManorVictoryLoot.packedChests(identifier);
        CompoundTag record = new CompoundTag();
        record.m_128362_("Id", identifier);
        record.m_128359_("Dimension", level.m_46472_().m_135782_().toString());
        BlockPos origin = BlockPos.m_274446_((Position)center);
        List<BlockPos> safeSites = ManorVictory.findSites(level, origin, rewards.size());
        if (!safeSites.isEmpty()) {
            origin = safeSites.get(safeSites.size() / 2);
        }
        record.m_128356_("Origin", origin.m_121878_());
        record.m_128356_("Ready", level.m_46467_() + 120L);
        ListTag chests = new ListTag();
        for (ItemStack reward : rewards) {
            chests.add((Object)reward.m_41739_(new CompoundTag()));
        }
        record.m_128365_(CHESTS_KEY, (Tag)chests);
        ListTag audience = new ListTag();
        for (UUID participant : participants) {
            player = new CompoundTag();
            player.m_128362_("Id", participant);
            audience.add((Object)player);
        }
        record.m_128365_("Participants", (Tag)audience);
        data.pending.put(identifier, record);
        data.m_77762_();
        ManorVictory.broadcast(level, record, 0);
        level.m_5594_(null, origin, SoundEvents.f_12496_, SoundSource.BLOCKS, 0.8f, 1.15f);
        for (UUID participant : participants) {
            player = level.m_7654_().m_6846_().m_11259_(participant);
            if (player == null || player.f_8906_ == null || player.m_9236_() != level) continue;
            player.f_8906_.m_9829_((Packet)new ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.f_8906_.m_9829_((Packet)new ClientboundSetTitleTextPacket((Component)Component.m_237115_((String)"message.ftboss.manor_victory.title")));
            player.m_213846_((Component)Component.m_237115_((String)"message.ftboss.manor_victory.soon"));
        }
        FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} queued, {} categorized reward chests", (Object)identifier, (Object)rewards.size());
        return true;
    }

    private static void broadcast(ServerLevel level, CompoundTag record, int age) {
        Vec3 center = Vec3.m_82539_((Vec3i)BlockPos.m_122022_((long)record.m_128454_("Origin")));
        ManorVictoryPacket packet = new ManorVictoryPacket(record.m_128342_("Id"), level.m_46472_().m_135782_(), center, age);
        for (ServerPlayer player : level.m_6907_()) {
            if (player instanceof FakePlayer || player.f_8906_ == null || player.f_8906_.f_9742_.channel() == null || !(player.m_20238_(center) <= 16384.0) || !FTNetwork.CHANNEL.isRemotePresent(player.f_8906_.f_9742_)) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        }
    }

    public static BlockPos findSite(ServerLevel level, BlockPos origin) {
        List<BlockPos> candidates = ManorVictory.candidateSites(level, origin);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static List<BlockPos> findSites(ServerLevel level, BlockPos origin, int count) {
        if (count <= 0) {
            return List.of();
        }
        List<BlockPos> candidates = ManorVictory.candidateSites(level, origin);
        if (candidates.size() < count) {
            return List.of();
        }
        if (count == 1) {
            return List.of(candidates.get(0));
        }
        HashSet<Long> available = new HashSet<Long>();
        for (BlockPos candidate : candidates) {
            available.add(candidate.m_121878_());
        }
        for (BlockPos center : candidates) {
            for (Direction direction : new Direction[]{Direction.EAST, Direction.SOUTH}) {
                ArrayList<BlockPos> row = new ArrayList<BlockPos>();
                int firstOffset = -(count / 2);
                boolean complete = true;
                for (int index = 0; index < count; ++index) {
                    BlockPos position = center.m_5484_(direction, firstOffset + index);
                    if (!available.contains(position.m_121878_())) {
                        complete = false;
                        break;
                    }
                    row.add(position);
                }
                if (!complete) continue;
                return row;
            }
        }
        return List.copyOf(candidates.subList(0, count));
    }

    private static List<BlockPos> candidateSites(ServerLevel level, BlockPos origin) {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        for (int radius = 0; radius <= 8; ++radius) {
            for (int vertical = 0; vertical <= 8; ++vertical) {
                int[] nArray;
                if (vertical == 0) {
                    int[] nArray2 = new int[1];
                    nArray = nArray2;
                    nArray2[0] = 0;
                } else {
                    int[] nArray3 = new int[2];
                    nArray3[0] = vertical;
                    nArray = nArray3;
                    nArray3[1] = -vertical;
                }
                for (int height : nArray) {
                    for (int offsetX = -radius; offsetX <= radius; ++offsetX) {
                        for (int offsetZ = -radius; offsetZ <= radius; ++offsetZ) {
                            BlockPos position;
                            if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius || !ManorVictory.isAvailable(level, position = origin.m_7918_(offsetX, height, offsetZ))) continue;
                            result.add(position);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isAvailable(ServerLevel level, BlockPos position) {
        return !level.m_151570_(position) && level.m_46805_(position) && level.m_6857_().m_61937_(position) && level.m_46859_(position) && level.m_46859_(position.m_7494_()) && level.m_6425_(position).m_76178_() && level.m_8055_(position.m_7495_()).m_60783_((BlockGetter)level, position.m_7495_(), Direction.UP);
    }

    private static List<ItemStack> rewardChests(CompoundTag record) {
        ItemStack legacy;
        ArrayList<ItemStack> rewards = new ArrayList<ItemStack>();
        for (Tag value : record.m_128437_(CHESTS_KEY, 10)) {
            ItemStack reward = ItemStack.m_41712_((CompoundTag)((CompoundTag)value));
            if (reward.m_41619_()) continue;
            rewards.add(reward);
        }
        if (rewards.isEmpty() && record.m_128425_("Chest", 10) && !(legacy = ItemStack.m_41712_((CompoundTag)record.m_128469_("Chest"))).m_41619_()) {
            rewards.add(legacy);
        }
        return rewards;
    }

    private static boolean alreadyDelivered(ServerLevel level, CompoundTag record, UUID identifier, int expected) {
        if (expected == 1 && record.m_128441_("Site")) {
            ManorLootChestBlockEntity chest;
            BlockPos previous = BlockPos.m_122022_((long)record.m_128454_("Site"));
            if (!level.m_46805_(previous)) {
                return false;
            }
            BlockEntity blockEntity = level.m_7702_(previous);
            return blockEntity instanceof ManorLootChestBlockEntity && (chest = (ManorLootChestBlockEntity)blockEntity).belongsTo(identifier);
        }
        long[] encoded = record.m_128467_(SITES_KEY);
        if (encoded.length != expected) {
            return false;
        }
        for (long value : encoded) {
            ManorLootChestBlockEntity chest;
            BlockEntity blockEntity;
            BlockPos previous = BlockPos.m_122022_((long)value);
            if (level.m_46805_(previous) && (blockEntity = level.m_7702_(previous)) instanceof ManorLootChestBlockEntity && (chest = (ManorLootChestBlockEntity)blockEntity).belongsTo(identifier)) continue;
            return false;
        }
        return true;
    }

    private static boolean deliver(ServerLevel level, CompoundTag record) {
        UUID identifier = record.m_128342_("Id");
        List<ItemStack> rewards = ManorVictory.rewardChests(record);
        if (rewards.isEmpty()) {
            return false;
        }
        if (ManorVictory.alreadyDelivered(level, record, identifier, rewards.size())) {
            return true;
        }
        BlockPos origin = BlockPos.m_122022_((long)record.m_128454_("Origin"));
        List<BlockPos> sites = ManorVictory.findSites(level, origin, rewards.size());
        if (!sites.isEmpty()) {
            ArrayList<BlockPos> placed = new ArrayList<BlockPos>();
            for (int index = 0; index < rewards.size(); ++index) {
                BlockPos site = sites.get(index);
                if (!level.m_46597_(site, ((Block)FTBlocks.MANOR_LOOT_CHEST.get()).m_49966_())) {
                    ManorVictory.rollback(level, placed);
                    return false;
                }
                placed.add(site);
                BlockEntity blockEntity = level.m_7702_(site);
                if (!(blockEntity instanceof ManorLootChestBlockEntity)) {
                    ManorVictory.rollback(level, placed);
                    return false;
                }
                ManorLootChestBlockEntity chest = (ManorLootChestBlockEntity)blockEntity;
                ItemStack reward = rewards.get(index);
                chest.m_142466_(reward.m_41784_().m_128469_("BlockEntityTag"));
                chest.setVictory(identifier);
                level.m_5594_(null, site, SoundEvents.f_11736_, SoundSource.BLOCKS, 0.55f, 1.35f + (float)index * 0.1f);
            }
            long[] encoded = new long[sites.size()];
            for (int index = 0; index < sites.size(); ++index) {
                encoded[index] = sites.get(index).m_121878_();
            }
            record.m_128388_(SITES_KEY, encoded);
            if (sites.size() == 1) {
                record.m_128356_("Site", sites.get(0).m_121878_());
            }
            BlockPos anchor = sites.get(sites.size() / 2);
            String message = sites.size() == 1 ? "message.ftboss.manor_victory.chest" : "message.ftboss.manor_victory.chests";
            for (ServerPlayer player : level.m_6907_()) {
                if (!(player.m_20238_(Vec3.m_82512_((Vec3i)anchor)) <= 16384.0)) continue;
                player.m_213846_((Component)Component.m_237110_((String)message, (Object[])new Object[]{anchor.m_123341_(), anchor.m_123342_(), anchor.m_123343_()}));
            }
            FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} placed {} categorized chests near {}", new Object[]{identifier, sites.size(), anchor});
            return true;
        }
        for (Tag value : record.m_128437_("Participants", 10)) {
            ServerPlayer player;
            CompoundTag entry = (CompoundTag)value;
            if (!entry.m_128403_("Id") || (player = level.m_7654_().m_6846_().m_11259_(entry.m_128342_("Id"))) == null || !player.m_6084_() || player.m_5833_()) continue;
            for (ItemStack reward : rewards) {
                if (ManorVictory.givePacked(player, reward)) continue;
                return false;
            }
            player.m_213846_((Component)Component.m_237115_((String)(rewards.size() == 1 ? "message.ftboss.manor_victory.portable" : "message.ftboss.manor_victory.portables")));
            FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} delivered {} packed chests to {}", new Object[]{identifier, rewards.size(), player.m_20148_()});
            return true;
        }
        return false;
    }

    private static void rollback(ServerLevel level, List<BlockPos> placed) {
        for (BlockPos position : placed) {
            BlockEntity blockEntity = level.m_7702_(position);
            if (blockEntity instanceof ManorLootChestBlockEntity) {
                ManorLootChestBlockEntity chest = (ManorLootChestBlockEntity)blockEntity;
                chest.m_6211_();
            }
            level.m_7471_(position, false);
        }
    }

    static boolean givePacked(ServerPlayer player, ItemStack reward) {
        if (player.m_150109_().m_36054_(reward)) {
            return true;
        }
        ItemEntity dropped = player.m_36176_(reward, false);
        if (dropped == null) {
            return false;
        }
        dropped.m_32052_(player.m_20148_());
        dropped.m_266426_(player.m_20148_());
        dropped.m_149678_();
        dropped.m_20331_(true);
        dropped.m_20242_(true);
        dropped.m_20256_(Vec3.f_82478_);
        return true;
    }

    public static void tick(MinecraftServer server) {
        ManorVictoryData data = ManorVictoryData.get(server);
        Iterator<Map.Entry<UUID, CompoundTag>> iterator = data.pending.entrySet().iterator();
        while (iterator.hasNext()) {
            ServerLevel level;
            Map.Entry<UUID, CompoundTag> entry = iterator.next();
            CompoundTag record = entry.getValue();
            ResourceLocation dimension = ResourceLocation.m_135820_((String)record.m_128461_("Dimension"));
            if (dimension == null || (level = server.m_129880_(ResourceKey.m_135785_((ResourceKey)Registries.f_256858_, (ResourceLocation)dimension))) == null) continue;
            long now = level.m_46467_();
            long remaining = record.m_128454_("Ready") - now;
            if (remaining > 0L) {
                if (now % 20L != 0L) continue;
                ManorVictory.broadcast(level, record, (int)Math.max(0L, 120L - remaining));
                continue;
            }
            if (now % 20L != 0L) continue;
            if (ManorVictory.deliver(level, record)) {
                data.delivered.add(entry.getKey());
                iterator.remove();
            }
            data.m_77762_();
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ManorVictory.tick(event.getServer());
        }
    }
}

