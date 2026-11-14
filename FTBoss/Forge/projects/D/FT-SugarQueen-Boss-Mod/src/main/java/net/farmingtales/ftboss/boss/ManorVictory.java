package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
import net.farmingtales.ftboss.util.ManorVictoryGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorVictory {
    private static final String CHESTS_KEY = "Chests";
    private static final String SITES_KEY = "Sites";

    private ManorVictory() {}

    public static boolean begin(ServerLevel level, UUID identifier, Vec3 center, Set<UUID> participants) {
        ManorVictoryData data = ManorVictoryData.get(level.getServer());
        if (data.known(identifier)) return false;
        List<ItemStack> rewards = ManorVictoryLoot.packedChests(identifier);
        CompoundTag record = new CompoundTag();
        record.putUUID("Id", identifier);
        record.putString("Dimension", level.dimension().location().toString());
        BlockPos origin = BlockPos.containing(center);
        List<BlockPos> safeSites = findSites(level, origin, rewards.size());
        if (!safeSites.isEmpty()) origin = safeSites.get(safeSites.size() / 2);
        record.putLong("Origin", origin.asLong());
        record.putLong("Ready", level.getGameTime() + ManorVictoryGeometry.DURATION);
        ListTag chests = new ListTag();
        for (ItemStack reward : rewards) chests.add(reward.save(new CompoundTag()));
        record.put(CHESTS_KEY, chests);
        ListTag audience = new ListTag();
        for (UUID participant : participants) {
            CompoundTag player = new CompoundTag();
            player.putUUID("Id", participant);
            audience.add(player);
        }
        record.put("Participants", audience);
        data.pending.put(identifier, record);
        data.setDirty();
        broadcast(level, record, 0);
        level.playSound(null, origin, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.8F, 1.15F);
        for (UUID participant : participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player == null || player.connection == null || player.level() != level) continue;
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.ftboss.manor_victory.title")));
            player.sendSystemMessage(Component.translatable("message.ftboss.manor_victory.soon"));
        }
        FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} queued, {} categorized reward chests", identifier, rewards.size());
        return true;
    }

    private static void broadcast(ServerLevel level, CompoundTag record, int age) {
        Vec3 center = Vec3.atBottomCenterOf(BlockPos.of(record.getLong("Origin")));
        ManorVictoryPacket packet = new ManorVictoryPacket(record.getUUID("Id"), level.dimension().location(), center, age);
        for (ServerPlayer player : level.players()) {
            if (player instanceof net.minecraftforge.common.util.FakePlayer || player.connection == null
                    || player.connection.connection.channel() == null) continue;
            if (player.distanceToSqr(center) <= 128 * 128 && FTNetwork.CHANNEL.isRemotePresent(player.connection.connection))
                FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    public static BlockPos findSite(ServerLevel level, BlockPos origin) {
        List<BlockPos> candidates = candidateSites(level, origin);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static List<BlockPos> findSites(ServerLevel level, BlockPos origin, int count) {
        if (count <= 0) return List.of();
        List<BlockPos> candidates = candidateSites(level, origin);
        if (candidates.size() < count) return List.of();
        if (count == 1) return List.of(candidates.get(0));

        Set<Long> available = new HashSet<>();
        for (BlockPos candidate : candidates) available.add(candidate.asLong());
        for (BlockPos center : candidates) {
            for (Direction direction : new Direction[]{Direction.EAST, Direction.SOUTH}) {
                List<BlockPos> row = new ArrayList<>();
                int firstOffset = -(count / 2);
                boolean complete = true;
                for (int index = 0; index < count; index++) {
                    BlockPos position = center.relative(direction, firstOffset + index);
                    if (!available.contains(position.asLong())) {
                        complete = false;
                        break;
                    }
                    row.add(position);
                }
                if (complete) return row;
            }
        }
        return List.copyOf(candidates.subList(0, count));
    }

    private static List<BlockPos> candidateSites(ServerLevel level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        for (int radius = 0; radius <= 8; radius++) {
            for (int vertical = 0; vertical <= 8; vertical++) {
                for (int height : vertical == 0 ? new int[]{0} : new int[]{vertical, -vertical}) {
                    for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                        for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                            if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) continue;
                            BlockPos position = origin.offset(offsetX, height, offsetZ);
                            if (isAvailable(level, position)) result.add(position);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isAvailable(ServerLevel level, BlockPos position) {
        return !level.isOutsideBuildHeight(position)
                && level.hasChunkAt(position)
                && level.getWorldBorder().isWithinBounds(position)
                && level.isEmptyBlock(position)
                && level.isEmptyBlock(position.above())
                && level.getFluidState(position).isEmpty()
                && level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP);
    }

    private static List<ItemStack> rewardChests(CompoundTag record) {
        List<ItemStack> rewards = new ArrayList<>();
        for (Tag value : record.getList(CHESTS_KEY, Tag.TAG_COMPOUND)) {
            ItemStack reward = ItemStack.of((CompoundTag) value);
            if (!reward.isEmpty()) rewards.add(reward);
        }
        // Compatibility with victories queued by versions that produced one combined chest.
        if (rewards.isEmpty() && record.contains("Chest", Tag.TAG_COMPOUND)) {
            ItemStack legacy = ItemStack.of(record.getCompound("Chest"));
            if (!legacy.isEmpty()) rewards.add(legacy);
        }
        return rewards;
    }

    private static boolean alreadyDelivered(ServerLevel level, CompoundTag record, UUID identifier, int expected) {
        if (expected == 1 && record.contains("Site")) {
            BlockPos previous = BlockPos.of(record.getLong("Site"));
            if (!level.hasChunkAt(previous)) return false;
            return level.getBlockEntity(previous) instanceof ManorLootChestBlockEntity chest && chest.belongsTo(identifier);
        }
        long[] encoded = record.getLongArray(SITES_KEY);
        if (encoded.length != expected) return false;
        for (long value : encoded) {
            BlockPos previous = BlockPos.of(value);
            if (!level.hasChunkAt(previous)
                    || !(level.getBlockEntity(previous) instanceof ManorLootChestBlockEntity chest)
                    || !chest.belongsTo(identifier)) return false;
        }
        return true;
    }

    private static boolean deliver(ServerLevel level, CompoundTag record) {
        UUID identifier = record.getUUID("Id");
        List<ItemStack> rewards = rewardChests(record);
        if (rewards.isEmpty()) return false;
        if (alreadyDelivered(level, record, identifier, rewards.size())) return true;

        BlockPos origin = BlockPos.of(record.getLong("Origin"));
        List<BlockPos> sites = findSites(level, origin, rewards.size());
        if (!sites.isEmpty()) {
            List<BlockPos> placed = new ArrayList<>();
            for (int index = 0; index < rewards.size(); index++) {
                BlockPos site = sites.get(index);
                if (!level.setBlockAndUpdate(site, FTBlocks.MANOR_LOOT_CHEST.get().defaultBlockState())) {
                    rollback(level, placed);
                    return false;
                }
                placed.add(site);
                if (!(level.getBlockEntity(site) instanceof ManorLootChestBlockEntity chest)) {
                    rollback(level, placed);
                    return false;
                }
                ItemStack reward = rewards.get(index);
                chest.load(reward.getOrCreateTag().getCompound("BlockEntityTag"));
                chest.setVictory(identifier);
                level.playSound(null, site, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.55F, 1.35F + index * 0.1F);
            }

            long[] encoded = new long[sites.size()];
            for (int index = 0; index < sites.size(); index++) encoded[index] = sites.get(index).asLong();
            record.putLongArray(SITES_KEY, encoded);
            if (sites.size() == 1) record.putLong("Site", sites.get(0).asLong());
            BlockPos anchor = sites.get(sites.size() / 2);
            String message = sites.size() == 1
                    ? "message.ftboss.manor_victory.chest"
                    : "message.ftboss.manor_victory.chests";
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(Vec3.atCenterOf(anchor)) <= 128 * 128) {
                    player.sendSystemMessage(Component.translatable(message, anchor.getX(), anchor.getY(), anchor.getZ()));
                }
            }
            FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} placed {} categorized chests near {}",
                    identifier, sites.size(), anchor);
            return true;
        }

        for (Tag value : record.getList("Participants", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (!entry.hasUUID("Id")) continue;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getUUID("Id"));
            if (player == null || !player.isAlive() || player.isSpectator()) continue;
            for (ItemStack reward : rewards) if (!givePacked(player, reward)) return false;
            player.sendSystemMessage(Component.translatable(
                    rewards.size() == 1 ? "message.ftboss.manor_victory.portable" : "message.ftboss.manor_victory.portables"));
            FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} delivered {} packed chests to {}",
                    identifier, rewards.size(), player.getUUID());
            return true;
        }
        return false;
    }

    private static void rollback(ServerLevel level, List<BlockPos> placed) {
        for (BlockPos position : placed) {
            if (level.getBlockEntity(position) instanceof ManorLootChestBlockEntity chest) chest.clearContent();
            level.removeBlock(position, false);
        }
    }

    static boolean givePacked(ServerPlayer player, ItemStack reward) {
        if (player.getInventory().add(reward)) return true;
        ItemEntity dropped = player.drop(reward, false);
        if (dropped == null) return false;
        dropped.setThrower(player.getUUID());
        dropped.setTarget(player.getUUID());
        dropped.setUnlimitedLifetime();
        dropped.setInvulnerable(true);
        dropped.setNoGravity(true);
        dropped.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    public static void tick(MinecraftServer server) {
        ManorVictoryData data = ManorVictoryData.get(server);
        var iterator = data.pending.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            CompoundTag record = entry.getValue();
            ResourceLocation dimension = ResourceLocation.tryParse(record.getString("Dimension"));
            if (dimension == null) continue;
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
            if (level == null) continue;
            long now = level.getGameTime();
            long remaining = record.getLong("Ready") - now;
            if (remaining > 0) {
                if (now % 20 == 0) broadcast(level, record, (int) Math.max(0, ManorVictoryGeometry.DURATION - remaining));
                continue;
            }
            if (now % 20 != 0) continue;
            if (deliver(level, record)) {
                data.delivered.add(entry.getKey());
                iterator.remove();
            }
            data.setDirty();
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) tick(event.getServer());
    }
}