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
import net.farmingtales.ftboss.skill.ManorVictoryPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "ftboss"
)
public final class ManorVictory {
   private static final String CHESTS_KEY = "Chests";
   private static final String SITES_KEY = "Sites";

   private ManorVictory() {
   }

   public static boolean begin(ServerLevel var0, UUID var1, Vec3 var2, Set<UUID> var3) {
      ManorVictoryData var4 = ManorVictoryData.get(var0.getServer());
      if (var4.known(var1)) {
         return false;
      } else {
         List var5 = ManorVictoryLoot.packedChests(var0.registryAccess(), var1);
         CompoundTag var6 = new CompoundTag();
         var6.putUUID("Id", var1);
         var6.putString("Dimension", var0.dimension().location().toString());
         BlockPos var7 = BlockPos.containing(var2);
         List var8 = findSites(var0, var7, var5.size());
         if (!var8.isEmpty()) {
            var7 = (BlockPos)var8.get(var8.size() / 2);
         }

         var6.putLong("Origin", var7.asLong());
         var6.putLong("Ready", var0.getGameTime() + 120L);
         ListTag var9 = new ListTag();

         for (ItemStack var11 : var5) {
            var9.add(var11.save(var0.registryAccess()));
         }

         var6.put("Chests", var9);
         ListTag var14 = new ListTag();

         for (UUID var12 : var3) {
            CompoundTag var13 = new CompoundTag();
            var13.putUUID("Id", var12);
            var14.add(var13);
         }

         var6.put("Participants", var14);
         var4.pending.put(var1, var6);
         var4.setDirty();
         broadcast(var0, var6, 0);
         var0.playSound(null, var7, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 0.8F, 1.15F);

         for (UUID var17 : var3) {
            ServerPlayer var18 = var0.getServer().getPlayerList().getPlayer(var17);
            if (var18 != null && var18.connection != null && var18.level() == var0) {
               var18.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
               var18.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.ftboss.manor_victory.title")));
               var18.sendSystemMessage(Component.translatable("message.ftboss.manor_victory.soon"));
            }
         }

         FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} queued, {} categorized reward chests", var1, var5.size());
         return true;
      }
   }

   private static void broadcast(ServerLevel var0, CompoundTag var1, int var2) {
      Vec3 var3 = Vec3.atBottomCenterOf(BlockPos.of(var1.getLong("Origin")));
      ManorVictoryPacket var4 = new ManorVictoryPacket(var1.getUUID("Id"), var0.dimension().location(), var3, var2);

      for (ServerPlayer var6 : var0.players()) {
         if (!(var6 instanceof FakePlayer) && var6.connection != null && var6.distanceToSqr(var3) <= 16384.0) {
            PacketDistributor.sendToPlayer(var6, var4, new CustomPacketPayload[0]);
         }
      }
   }

   public static BlockPos findSite(ServerLevel var0, BlockPos var1) {
      List var2 = candidateSites(var0, var1);
      return var2.isEmpty() ? null : (BlockPos)var2.get(0);
   }

   private static List<BlockPos> findSites(ServerLevel var0, BlockPos var1, int var2) {
      if (var2 <= 0) {
         return List.of();
      } else {
         List var3 = candidateSites(var0, var1);
         if (var3.size() < var2) {
            return List.of();
         } else if (var2 == 1) {
            return List.of((BlockPos)var3.get(0));
         } else {
            HashSet var4 = new HashSet();

            for (BlockPos var6 : var3) {
               var4.add(var6.asLong());
            }

            for (BlockPos var17 : var3) {
               for (Direction var10 : new Direction[]{Direction.EAST, Direction.SOUTH}) {
                  ArrayList var11 = new ArrayList();
                  int var12 = -(var2 / 2);
                  boolean var13 = true;

                  for (int var14 = 0; var14 < var2; var14++) {
                     BlockPos var15 = var17.relative(var10, var12 + var14);
                     if (!var4.contains(var15.asLong())) {
                        var13 = false;
                        break;
                     }

                     var11.add(var15);
                  }

                  if (var13) {
                     return var11;
                  }
               }
            }

            return List.copyOf(var3.subList(0, var2));
         }
      }
   }

   private static List<BlockPos> candidateSites(ServerLevel var0, BlockPos var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = 0; var3 <= 8; var3++) {
         for (int var4 = 0; var4 <= 8; var4++) {
            for (int var8 : var4 == 0 ? new int[]{0} : new int[]{var4, -var4}) {
               for (int var9 = -var3; var9 <= var3; var9++) {
                  for (int var10 = -var3; var10 <= var3; var10++) {
                     if (Math.max(Math.abs(var9), Math.abs(var10)) == var3) {
                        BlockPos var11 = var1.offset(var9, var8, var10);
                        if (isAvailable(var0, var11)) {
                           var2.add(var11);
                        }
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   private static boolean isAvailable(ServerLevel var0, BlockPos var1) {
      return !var0.isOutsideBuildHeight(var1)
         && var0.hasChunkAt(var1)
         && var0.getWorldBorder().isWithinBounds(var1)
         && var0.isEmptyBlock(var1)
         && var0.isEmptyBlock(var1.above())
         && var0.getFluidState(var1).isEmpty()
         && var0.getBlockState(var1.below()).isFaceSturdy(var0, var1.below(), Direction.UP);
   }

   private static List<ItemStack> rewardChests(ServerLevel var0, CompoundTag var1) {
      ArrayList var2 = new ArrayList();

      for (Tag var4 : var1.getList("Chests", 10)) {
         ItemStack var5 = ItemStack.parseOptional(var0.registryAccess(), (CompoundTag)var4);
         if (!var5.isEmpty()) {
            var2.add(var5);
         }
      }

      if (var2.isEmpty() && var1.contains("Chest", 10)) {
         ItemStack var6 = ItemStack.parseOptional(var0.registryAccess(), var1.getCompound("Chest"));
         if (!var6.isEmpty()) {
            var2.add(var6);
         }
      }

      return var2;
   }

   private static boolean alreadyDelivered(ServerLevel var0, CompoundTag var1, UUID var2, int var3) {
      if (var3 == 1 && var1.contains("Site")) {
         BlockPos var13 = BlockPos.of(var1.getLong("Site"));
         if (!var0.hasChunkAt(var13)) {
            return false;
         } else {
            if (var0.getBlockEntity(var13) instanceof ManorLootChestBlockEntity var14 && var14.belongsTo(var2)) {
               return true;
            }

            return false;
         }
      } else {
         long[] var4 = var1.getLongArray("Sites");
         if (var4.length != var3) {
            return false;
         } else {
            for (long var8 : var4) {
               BlockPos var10 = BlockPos.of(var8);
               if (!var0.hasChunkAt(var10) || !(var0.getBlockEntity(var10) instanceof ManorLootChestBlockEntity var11) || !var11.belongsTo(var2)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static boolean deliver(ServerLevel var0, CompoundTag var1) {
      UUID var2 = var1.getUUID("Id");
      List var3 = rewardChests(var0, var1);
      if (var3.isEmpty()) {
         return false;
      } else if (alreadyDelivered(var0, var1, var2, var3.size())) {
         return true;
      } else {
         BlockPos var4 = BlockPos.of(var1.getLong("Origin"));
         List var5 = findSites(var0, var4, var3.size());
         if (var5.isEmpty()) {
            for (Tag var14 : var1.getList("Participants", 10)) {
               CompoundTag var17 = (CompoundTag)var14;
               if (var17.hasUUID("Id")) {
                  ServerPlayer var19 = var0.getServer().getPlayerList().getPlayer(var17.getUUID("Id"));
                  if (var19 != null && var19.isAlive() && !var19.isSpectator()) {
                     for (ItemStack var24 : var3) {
                        if (!givePacked(var19, var24)) {
                           return false;
                        }
                     }

                     var19.sendSystemMessage(
                        Component.translatable(var3.size() == 1 ? "message.ftboss.manor_victory.portable" : "message.ftboss.manor_victory.portables")
                     );
                     FTBossMod.LOGGER
                        .info("[FTBoss][ManorReward] Victory {} delivered {} packed chests to {}", new Object[]{var2, var3.size(), var19.getUUID()});
                     return true;
                  }
               }
            }

            return false;
         } else {
            ArrayList var6 = new ArrayList();

            for (int var7 = 0; var7 < var3.size(); var7++) {
               BlockPos var8 = (BlockPos)var5.get(var7);
               if (!var0.setBlockAndUpdate(var8, ((Block)FTBlocks.MANOR_LOOT_CHEST.get()).defaultBlockState())) {
                  rollback(var0, var6);
                  return false;
               }

               var6.add(var8);
               if (!(var0.getBlockEntity(var8) instanceof ManorLootChestBlockEntity var9)) {
                  rollback(var0, var6);
                  return false;
               }

               ItemStack var20 = (ItemStack)var3.get(var7);
               CustomData var11 = (CustomData)var20.get(DataComponents.BLOCK_ENTITY_DATA);
               if (var11 != null) {
                  var9.loadWithComponents(var11.copyTag(), var0.registryAccess());
               }

               var9.setVictory(var2);
               var0.playSound(null, var8, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.55F, 1.35F + (float)var7 * 0.1F);
            }

            long[] var13 = new long[var5.size()];

            for (int var15 = 0; var15 < var5.size(); var15++) {
               var13[var15] = ((BlockPos)var5.get(var15)).asLong();
            }

            var1.putLongArray("Sites", var13);
            if (var5.size() == 1) {
               var1.putLong("Site", ((BlockPos)var5.get(0)).asLong());
            }

            BlockPos var16 = (BlockPos)var5.get(var5.size() / 2);
            String var18 = var5.size() == 1 ? "message.ftboss.manor_victory.chest" : "message.ftboss.manor_victory.chests";

            for (ServerPlayer var23 : var0.players()) {
               if (var23.distanceToSqr(Vec3.atCenterOf(var16)) <= 16384.0) {
                  var23.sendSystemMessage(Component.translatable(var18, new Object[]{var16.getX(), var16.getY(), var16.getZ()}));
               }
            }

            FTBossMod.LOGGER.info("[FTBoss][ManorReward] Victory {} placed {} categorized chests near {}", new Object[]{var2, var5.size(), var16});
            return true;
         }
      }
   }

   private static void rollback(ServerLevel var0, List<BlockPos> var1) {
      for (BlockPos var3 : var1) {
         if (var0.getBlockEntity(var3) instanceof ManorLootChestBlockEntity var4) {
            var4.clearContent();
         }

         var0.removeBlock(var3, false);
      }
   }

   static boolean givePacked(ServerPlayer var0, ItemStack var1) {
      if (var0.getInventory().add(var1)) {
         return true;
      } else {
         ItemEntity var2 = var0.drop(var1, false);
         if (var2 == null) {
            return false;
         } else {
            var2.setThrower(var0);
            var2.setTarget(var0.getUUID());
            var2.setUnlimitedLifetime();
            var2.setInvulnerable(true);
            var2.setNoGravity(true);
            var2.setDeltaMovement(Vec3.ZERO);
            return true;
         }
      }
   }

   public static void tick(MinecraftServer var0) {
      ManorVictoryData var1 = ManorVictoryData.get(var0);
      Iterator var2 = var1.pending.entrySet().iterator();

      while (var2.hasNext()) {
         Entry var3 = (Entry)var2.next();
         CompoundTag var4 = (CompoundTag)var3.getValue();
         ResourceLocation var5 = ResourceLocation.tryParse(var4.getString("Dimension"));
         if (var5 != null) {
            ServerLevel var6 = var0.getLevel(ResourceKey.create(Registries.DIMENSION, var5));
            if (var6 != null) {
               long var7 = var6.getGameTime();
               long var9 = var4.getLong("Ready") - var7;
               if (var9 > 0L) {
                  if (var7 % 20L == 0L) {
                     broadcast(var6, var4, (int)Math.max(0L, 120L - var9));
                  }
               } else if (var7 % 20L == 0L) {
                  if (deliver(var6, var4)) {
                     var1.delivered.add((UUID)var3.getKey());
                     var2.remove();
                  }

                  var1.setDirty();
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onTick(Post var0) {
      tick(var0.getServer());
   }
}
