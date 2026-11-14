package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "ftboss"
)
public final class ManorVictory {
   private static final String CHESTS_KEY = "Chests";
   private static final String SITES_KEY = "Sites";

   private ManorVictory() {
   }

   public static boolean begin(ServerLevel level, UUID identifier, Vec3 center, Set<UUID> participants) {
      ManorVictoryData data = ManorVictoryData.get(level.m_7654_());
      if (data.known(identifier)) {
         return false;
      } else {
         List<ItemStack> rewards = ManorVictoryLoot.packedChests(identifier);
         CompoundTag record = new CompoundTag();
         record.m_128362_("Id", identifier);
         record.m_128359_("Dimension", level.m_46472_().m_135782_().toString());
         BlockPos origin = BlockPos.m_274446_(center);
         List<BlockPos> safeSites = findSites(level, origin, rewards.size());
         if (!safeSites.isEmpty()) {
            origin = safeSites.get(safeSites.size() / 2);
         }

         record.m_128356_("Origin", origin.m_121878_());
         record.m_128356_("Ready", level.m_46467_() + 120L);
         ListTag chests = new ListTag();

         for (ItemStack reward : rewards) {
            chests.add(reward.m_41739_(new CompoundTag()));
         }

         record.m_128365_("Chests", chests);
         ListTag audience = new ListTag();

         for (UUID participant : participants) {
            CompoundTag player = new CompoundTag();
            player.m_128362_("Id", participant);
            audience.add(player);
         }

         record.m_128365_("Participants", audience);
         data.pending.put(identifier, record);
         data.m_77762_();
         broadcast(level, record, 0);
         level.m_5594_(null, origin, SoundEvents.f_12496_, SoundSource.BLOCKS, 0.8F, 1.15F);

         for (UUID participant : participants) {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(participant);
            if (player != null && player.f_8906_ != null && player.m_9236_() == level) {
               player.f_8906_.m_9829_(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               player.f_8906_.m_9829_(new ClientboundSetTitleTextPacket(Component.m_237115_("message.ftboss.manor_victory.title")));
               player.m_213846_(Component.m_237115_("message.ftboss.manor_victory.soon"));
            }
         }

         FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} queued, {} categorized reward chests", identifier, rewards.size());
         return true;
      }
   }

   private static void broadcast(ServerLevel level, CompoundTag record, int age) {
      Vec3 center = Vec3.m_82539_(BlockPos.m_122022_(record.m_128454_("Origin")));
      ManorVictoryPacket packet = new ManorVictoryPacket(record.m_128342_("Id"), level.m_46472_().m_135782_(), center, age);

      for (ServerPlayer player : level.m_6907_()) {
         if (!(player instanceof FakePlayer)
            && player.f_8906_ != null
            && player.f_8906_.f_9742_.channel() != null
            && player.m_20238_(center) <= 16384.0
            && FTNetwork.CHANNEL.isRemotePresent(player.f_8906_.f_9742_)) {
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
         }
      }
   }

   public static BlockPos findSite(ServerLevel level, BlockPos origin) {
      List<BlockPos> candidates = candidateSites(level, origin);
      return candidates.isEmpty() ? null : candidates.get(0);
   }

   private static List<BlockPos> findSites(ServerLevel level, BlockPos origin, int count) {
      if (count <= 0) {
         return List.of();
      } else {
         List<BlockPos> candidates = candidateSites(level, origin);
         if (candidates.size() < count) {
            return List.of();
         } else if (count == 1) {
            return List.of(candidates.get(0));
         } else {
            Set<Long> available = new HashSet<>();

            for (BlockPos candidate : candidates) {
               available.add(candidate.m_121878_());
            }

            for (BlockPos center : candidates) {
               for (Direction direction : new Direction[]{Direction.EAST, Direction.SOUTH}) {
                  List<BlockPos> row = new ArrayList<>();
                  int firstOffset = -(count / 2);
                  boolean complete = true;

                  for (int index = 0; index < count; index++) {
                     BlockPos position = center.m_5484_(direction, firstOffset + index);
                     if (!available.contains(position.m_121878_())) {
                        complete = false;
                        break;
                     }

                     row.add(position);
                  }

                  if (complete) {
                     return row;
                  }
               }
            }

            return List.copyOf(candidates.subList(0, count));
         }
      }
   }

   private static List<BlockPos> candidateSites(ServerLevel level, BlockPos origin) {
      List<BlockPos> result = new ArrayList<>();

      for (int radius = 0; radius <= 8; radius++) {
         for (int vertical = 0; vertical <= 8; vertical++) {
            for (int height : vertical == 0 ? new int[]{0} : new int[]{vertical, -vertical}) {
               for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                  for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                     if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) == radius) {
                        BlockPos position = origin.m_7918_(offsetX, height, offsetZ);
                        if (isAvailable(level, position)) {
                           result.add(position);
                        }
                     }
                  }
               }
            }
         }
      }

      return result;
   }

   private static boolean isAvailable(ServerLevel level, BlockPos position) {
      return !level.m_151570_(position)
         && level.m_46805_(position)
         && level.m_6857_().m_61937_(position)
         && level.m_46859_(position)
         && level.m_46859_(position.m_7494_())
         && level.m_6425_(position).m_76178_()
         && level.m_8055_(position.m_7495_()).m_60783_(level, position.m_7495_(), Direction.UP);
   }

   private static List<ItemStack> rewardChests(CompoundTag record) {
      List<ItemStack> rewards = new ArrayList<>();

      for (Tag value : record.m_128437_("Chests", 10)) {
         ItemStack reward = ItemStack.m_41712_((CompoundTag)value);
         if (!reward.m_41619_()) {
            rewards.add(reward);
         }
      }

      if (rewards.isEmpty() && record.m_128425_("Chest", 10)) {
         ItemStack legacy = ItemStack.m_41712_(record.m_128469_("Chest"));
         if (!legacy.m_41619_()) {
            rewards.add(legacy);
         }
      }

      return rewards;
   }

   private static boolean alreadyDelivered(ServerLevel level, CompoundTag record, UUID identifier, int expected) {
      if (expected == 1 && record.m_128441_("Site")) {
         BlockPos previous = BlockPos.m_122022_(record.m_128454_("Site"));
         if (!level.m_46805_(previous)) {
            return false;
         } else {
            if (level.m_7702_(previous) instanceof ManorLootChestBlockEntity chest && chest.belongsTo(identifier)) {
               return true;
            }

            return false;
         }
      } else {
         long[] encoded = record.m_128467_("Sites");
         if (encoded.length != expected) {
            return false;
         } else {
            for (long value : encoded) {
               BlockPos previous = BlockPos.m_122022_(value);
               if (!level.m_46805_(previous) || !(level.m_7702_(previous) instanceof ManorLootChestBlockEntity chest) || !chest.belongsTo(identifier)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static boolean deliver(ServerLevel level, CompoundTag record) {
      UUID identifier = record.m_128342_("Id");
      List<ItemStack> rewards = rewardChests(record);
      if (rewards.isEmpty()) {
         return false;
      } else if (alreadyDelivered(level, record, identifier, rewards.size())) {
         return true;
      } else {
         BlockPos origin = BlockPos.m_122022_(record.m_128454_("Origin"));
         List<BlockPos> sites = findSites(level, origin, rewards.size());
         if (sites.isEmpty()) {
            for (Tag value : record.m_128437_("Participants", 10)) {
               CompoundTag entry = (CompoundTag)value;
               if (entry.m_128403_("Id")) {
                  ServerPlayer player = level.m_7654_().m_6846_().m_11259_(entry.m_128342_("Id"));
                  if (player != null && player.m_6084_() && !player.m_5833_()) {
                     for (ItemStack reward : rewards) {
                        if (!givePacked(player, reward)) {
                           return false;
                        }
                     }

                     player.m_213846_(
                        Component.m_237115_(rewards.size() == 1 ? "message.ftboss.manor_victory.portable" : "message.ftboss.manor_victory.portables")
                     );
                     FTBossMod.LOGGER
                        .info("[FTBoss][ManorReward] Victory {} delivered {} packed chests to {}", new Object[]{identifier, rewards.size(), player.m_20148_()});
                     return true;
                  }
               }
            }

            return false;
         } else {
            List<BlockPos> placed = new ArrayList<>();

            for (int index = 0; index < rewards.size(); index++) {
               BlockPos site = sites.get(index);
               if (!level.m_46597_(site, ((Block)FTBlocks.MANOR_LOOT_CHEST.get()).m_49966_())) {
                  rollback(level, placed);
                  return false;
               }

               placed.add(site);
               if (!(level.m_7702_(site) instanceof ManorLootChestBlockEntity chest)) {
                  rollback(level, placed);
                  return false;
               }

               ItemStack var20 = rewards.get(index);
               chest.m_142466_(var20.m_41784_().m_128469_("BlockEntityTag"));
               chest.setVictory(identifier);
               level.m_5594_(null, site, SoundEvents.f_11736_, SoundSource.BLOCKS, 0.55F, 1.35F + (float)index * 0.1F);
            }

            long[] encoded = new long[sites.size()];

            for (int index = 0; index < sites.size(); index++) {
               encoded[index] = sites.get(index).m_121878_();
            }

            record.m_128388_("Sites", encoded);
            if (sites.size() == 1) {
               record.m_128356_("Site", sites.get(0).m_121878_());
            }

            BlockPos anchor = sites.get(sites.size() / 2);
            String message = sites.size() == 1 ? "message.ftboss.manor_victory.chest" : "message.ftboss.manor_victory.chests";

            for (ServerPlayer player : level.m_6907_()) {
               if (player.m_20238_(Vec3.m_82512_(anchor)) <= 16384.0) {
                  player.m_213846_(Component.m_237110_(message, new Object[]{anchor.m_123341_(), anchor.m_123342_(), anchor.m_123343_()}));
               }
            }

            FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} placed {} categorized chests near {}", new Object[]{identifier, sites.size(), anchor});
            return true;
         }
      }
   }

   private static void rollback(ServerLevel level, List<BlockPos> placed) {
      for (BlockPos position : placed) {
         if (level.m_7702_(position) instanceof ManorLootChestBlockEntity chest) {
            chest.m_6211_();
         }

         level.m_7471_(position, false);
      }
   }

   static boolean givePacked(ServerPlayer player, ItemStack reward) {
      if (player.m_150109_().m_36054_(reward)) {
         return true;
      } else {
         ItemEntity dropped = player.m_36176_(reward, false);
         if (dropped == null) {
            return false;
         } else {
            dropped.m_32052_(player.m_20148_());
            dropped.m_266426_(player.m_20148_());
            dropped.m_149678_();
            dropped.m_20331_(true);
            dropped.m_20242_(true);
            dropped.m_20256_(Vec3.f_82478_);
            return true;
         }
      }
   }

   public static void tick(MinecraftServer server) {
      ManorVictoryData data = ManorVictoryData.get(server);
      Iterator<Entry<UUID, CompoundTag>> iterator = data.pending.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, CompoundTag> entry = iterator.next();
         CompoundTag record = entry.getValue();
         ResourceLocation dimension = ResourceLocation.m_135820_(record.m_128461_("Dimension"));
         if (dimension != null) {
            ServerLevel level = server.m_129880_(ResourceKey.m_135785_(Registries.f_256858_, dimension));
            if (level != null) {
               long now = level.m_46467_();
               long remaining = record.m_128454_("Ready") - now;
               if (remaining > 0L) {
                  if (now % 20L == 0L) {
                     broadcast(level, record, (int)Math.max(0L, 120L - remaining));
                  }
               } else if (now % 20L == 0L) {
                  if (deliver(level, record)) {
                     data.delivered.add(entry.getKey());
                     iterator.remove();
                  }

                  data.m_77762_();
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onTick(ServerTickEvent event) {
      if (event.phase == Phase.END) {
         tick(event.getServer());
      }
   }
}
