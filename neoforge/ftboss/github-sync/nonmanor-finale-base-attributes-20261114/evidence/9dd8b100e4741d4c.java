package net.farmingtales.ftboss.boss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.CandyStatueBlock;
import net.farmingtales.ftboss.boss.NonManorFinale.Pending;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

public final class NonManorFinale {
   public static final double ATTRIBUTE_MULTIPLIER = 2.0;
   public static final int CAST_ACCELERATION = 2;
   public static final int PARALLEL_SPELLS = 2;
   private static final String FINALE = "ftboss:non_manor_finale";
   private static final String PROFILE = "ftboss:non_manor_finale_profile";
   private static final String STATUE_POS = "ftboss:non_manor_finale_statue_pos";
   private static final String STATUE_DIM = "ftboss:non_manor_finale_statue_dim";
   private static final String SUMMONER = "ftboss:non_manor_finale_summoner";
   private static final String BADGED = "ftboss:non_manor_finale_badged";
   private static final String GOLDEN_DONE = "ftboss:non_manor_finale_golden_done";
   private static final String FINALE_ITEM = "ftboss:non_manor_finale_summon";
   private static final String ITEM_PROFILE = "ftboss:non_manor_finale_summon_profile";
   private static final String ITEM_STATUE_POS = "ftboss:non_manor_finale_summon_statue_pos";
   private static final String ITEM_STATUE_DIM = "ftboss:non_manor_finale_summon_statue_dim";
   private static final int PENDING_TICKS = 40;
   private static final ResourceLocation ATTRIBUTE_ID = ResourceLocation.fromNamespaceAndPath("ftboss", "non_manor_finale_double");
   private static final Map<UUID, Pending> PENDING = new HashMap<>();

   private NonManorFinale() {
   }

   public static void useSummonOnStatue(RightClickBlock var0) {
      if (!var0.getLevel().isClientSide
         && var0.getHand() == InteractionHand.MAIN_HAND
         && var0.getEntity() instanceof ServerPlayer var1
         && var0.getLevel() instanceof ServerLevel var2) {
         BlockState var10 = var2.getBlockState(var0.getPos());
         if (var10.getBlock() instanceof CandyStatueBlock var4 && var4.ownTint() > 0) {
            ItemStack var11 = var1.getMainHandItem();
            if (!isSummonItem(var11)) {
               return;
            }

            int var6 = matchingTint(var4, var11);
            if (var6 <= 0) {
               var1.displayClientMessage(Component.translatable("message.ftboss.finale.wrong_statue"), true);
               finish(var0);
               return;
            }

            BlockPos var7 = var10.getValue(CandyStatueBlock.HALF) == DoubleBlockHalf.UPPER ? var0.getPos().below() : var0.getPos();
            if (isFinaleSummon(var11)) {
               var11.getItem().use(var2, var1, InteractionHand.MAIN_HAND);
               finish(var0);
               return;
            }

            ItemStack var8 = var11.copyWithCount(1);
            ItemData.edit(var8, var3 -> {
               var3.putBoolean("ftboss:non_manor_finale_summon", true);
               var3.putInt("ftboss:non_manor_finale_summon_profile", var6);
               var3.putLong("ftboss:non_manor_finale_summon_statue_pos", var7.asLong());
               var3.putString("ftboss:non_manor_finale_summon_statue_dim", var2.dimension().location().toString());
            });
            var8.set(DataComponents.CUSTOM_NAME, Component.literal("终结·").withStyle(ChatFormatting.GOLD).append(var11.getHoverName().copy()));
            var8.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            var8.set(
               DataComponents.LORE,
               new ItemLore(
                  List.of(
                     Component.translatable("tooltip.ftboss.finale_summon.power").withStyle(ChatFormatting.DARK_RED),
                     Component.translatable("tooltip.ftboss.finale_summon.use").withStyle(ChatFormatting.GRAY)
                  )
               )
            );
            if (var11.getCount() == 1) {
               var1.setItemInHand(InteractionHand.MAIN_HAND, var8);
            } else {
               var11.shrink(1);
               if (!var1.getInventory().add(var8)) {
                  var1.drop(var8, false);
               }
            }

            var2.playSound(null, var7, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.5F);
            var2.sendParticles(ParticleTypes.ENCHANT, (double)var7.getX() + 0.5, (double)var7.getY() + 1.0, (double)var7.getZ() + 0.5, 45, 0.6, 0.9, 0.6, 0.12);
            var1.sendSystemMessage(
               Component.translatable("message.ftboss.finale.item_upgraded", new Object[]{bossName(var6)})
                  .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD})
            );
            FTBossMod.LOGGER.info("[FTBoss][终结难度] {} 的召唤物已在石像 {} 升级", bossName(var6), var7);
            finish(var0);
            return;
         }
      }
   }

   public static void prepareSummon(Level var0, Player var1, InteractionHand var2) {
      if (var0 instanceof ServerLevel var3 && var1 instanceof ServerPlayer var4) {
         PENDING.remove(var1.getUUID());
         ItemStack var5 = var1.getItemInHand(var2);
         if (!isFinaleSummon(var5)) {
            return;
         }

         CompoundTag var6 = ItemData.nbt(var5);
         if (var6 == null) {
            return;
         }

         int var7 = var6.getInt("ftboss:non_manor_finale_summon_profile");
         if (var7 > 0 && profileForItem(var5) == var7) {
            ResourceLocation var8 = ResourceLocation.tryParse(var6.getString("ftboss:non_manor_finale_summon_statue_dim"));
            if (var8 != null && var8.equals(var3.dimension().location()) && var6.contains("ftboss:non_manor_finale_summon_statue_pos")) {
               BlockPos var9 = BlockPos.of(var6.getLong("ftboss:non_manor_finale_summon_statue_pos"));
               HashSet var10 = new HashSet();

               for (LivingEntity var12 : var3.getEntitiesOfClass(LivingEntity.class, boxAround(var4.blockPosition(), 96.0))) {
                  if (profileOf(var12) == var7) {
                     var10.add(var12.getUUID());
                  }
               }

               PENDING.put(var1.getUUID(), new Pending(var1.getUUID(), var7, var8, var9, var10, var3.getGameTime() + 40L));
               var4.displayClientMessage(Component.translatable("message.ftboss.finale.item_armed", new Object[]{bossName(var7)}), true);
               return;
            }

            var4.sendSystemMessage(Component.translatable("message.ftboss.finale.item_invalid").withStyle(ChatFormatting.RED));
            return;
         }

         return;
      }
   }

   public static boolean isFinaleSummon(ItemStack var0) {
      return var0 != null && !var0.isEmpty() && ItemData.getBoolean(var0, "ftboss:non_manor_finale_summon");
   }

   public static void onJoin(EntityJoinLevelEvent var0) {
      if (!(var0.getLevel() instanceof ServerLevel var1) || !(var0.getEntity() instanceof LivingEntity var2)) {
         return;
      }

      if (isFinale(var2)) {
         applyAttributes(var2, false);
      } else {
         int var8 = profileOf(var2);
         if (var8 > 0 && !var0.loadedFromDisk()) {
            if (!PENDING.isEmpty()) {
               for (Pending var5 : List.copyOf(PENDING.values())) {
                  if (!var5.activated && var5.profile == var8 && var5.dimension.equals(var1.dimension().location()) && !var5.existing.contains(var2.getUUID())) {
                     if (var1.getGameTime() > var5.expiresAt) {
                        PENDING.remove(var5.player, var5);
                     } else {
                        ServerPlayer var6 = var1.getServer().getPlayerList().getPlayer(var5.player);
                        if (var6 != null && var6.level() == var1 && !(var6.distanceToSqr(var2) > 4096.0)) {
                           activate(var2, var5);
                           var5.activated = true;
                           PENDING.remove(var5.player, var5);
                           return;
                        }
                     }
                  }
               }
            }

            ServerPlayer var9 = nearestFinalePlayer(var1, var2);
            if (var9 != null) {
               activateFromPreference(var2, var9, var8);
            }
         }
      }
   }

   public static void onServerTick(Post var0) {
      if (!PENDING.isEmpty()) {
         for (Pending var2 : List.copyOf(PENDING.values())) {
            ServerLevel var3 = var0.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, var2.dimension));
            if (var3 != null && var3.getGameTime() <= var2.expiresAt) {
               ServerPlayer var4 = var0.getServer().getPlayerList().getPlayer(var2.player);
               if (var4 != null && var4.level() == var3) {
                  for (LivingEntity var6 : var3.getEntitiesOfClass(LivingEntity.class, boxAround(var2.statue, 64.0))) {
                     if (!var2.activated
                        && !var2.existing.contains(var6.getUUID())
                        && profileOf(var6) == var2.profile
                        && !isFinale(var6)
                        && !(var4.distanceToSqr(var6) > 4096.0)) {
                        activate(var6, var2);
                        var2.activated = true;
                        PENDING.remove(var2.player, var2);
                        break;
                     }
                  }
               }
            } else {
               PENDING.remove(var2.player, var2);
            }
         }
      }
   }

   public static void confirmedDeath(LivingEntity var0) {
      if (var0.level() instanceof ServerLevel var1 && isFinale(var0) && profileOf(var0) > 0) {
         CompoundTag var10 = var0.getPersistentData();
         if (var10.getBoolean("ftboss:non_manor_finale_golden_done")) {
            return;
         }

         ResourceLocation var3 = ResourceLocation.tryParse(var10.getString("ftboss:non_manor_finale_statue_dim"));
         if (var3 != null && var10.contains("ftboss:non_manor_finale_statue_pos")) {
            ServerLevel var4 = var1.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, var3));
            if (var4 == null) {
               return;
            }

            BlockPos var5 = BlockPos.of(var10.getLong("ftboss:non_manor_finale_statue_pos"));
            BlockState var6 = var4.getBlockState(var5);
            int var7 = var10.getInt("ftboss:non_manor_finale_profile");
            if (var6.getBlock() instanceof CandyStatueBlock var8 && var8.ownTint() == var7) {
               CandyStatueBlock.setGolden(var4, var5, true);
               var10.putBoolean("ftboss:non_manor_finale_golden_done", true);
               var4.playSound(null, var5, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.4F, 1.25F);
               var4.sendParticles(
                  ParticleTypes.TOTEM_OF_UNDYING, (double)var5.getX() + 0.5, (double)var5.getY() + 1.0, (double)var5.getZ() + 0.5, 80, 0.7, 1.0, 0.7, 0.12
               );
               if (var10.hasUUID("ftboss:non_manor_finale_summoner")) {
                  ServerPlayer var11 = var1.getServer().getPlayerList().getPlayer(var10.getUUID("ftboss:non_manor_finale_summoner"));
                  if (var11 != null) {
                     var11.sendSystemMessage(
                        Component.translatable("message.ftboss.finale.statue_golden", new Object[]{bossName(var7)}).withStyle(ChatFormatting.GOLD)
                     );
                  }
               }

               FTBossMod.LOGGER.info("[FTBoss][终结难度] {} 被击败，石像 {} 已升级为金色", bossName(var7), var5);
               return;
            }

            return;
         }

         return;
      }
   }

   public static void onLeave(EntityLeaveLevelEvent var0) {
      if (var0.getLevel() instanceof ServerLevel
         && var0.getEntity() instanceof LivingEntity var1
         && var0.getEntity().getRemovalReason() == RemovalReason.KILLED) {
         confirmedDeath(var1);
      }
   }

   private static AABB boxAround(BlockPos var0, double var1) {
      return new AABB(
         (double)var0.getX() - var1,
         (double)var0.getY() - var1,
         (double)var0.getZ() - var1,
         (double)var0.getX() + 1.0 + var1,
         (double)var0.getY() + 1.0 + var1,
         (double)var0.getZ() + 1.0 + var1
      );
   }

   private static ServerPlayer nearestFinalePlayer(ServerLevel var0, LivingEntity var1) {
      ServerPlayer var2 = null;
      double var3 = Double.MAX_VALUE;

      for (ServerPlayer var6 : var0.getEntitiesOfClass(
         ServerPlayer.class, var1.getBoundingBox().inflate(16.0), var0x -> var0x.isAlive() && !var0x.isSpectator()
      )) {
         double var7 = var6.distanceToSqr(var1);
         if (var7 < var3) {
            var3 = var7;
            var2 = var6;
         }
      }

      return var2 != null && BossChallengePreference.selected(var2) == ManorDifficulty.IMPOSSIBLE ? var2 : null;
   }

   private static void activateFromPreference(LivingEntity var0, ServerPlayer var1, int var2) {
      CompoundTag var3 = var0.getPersistentData();
      var3.putBoolean("ftboss:non_manor_finale", true);
      var3.putInt("ftboss:non_manor_finale_profile", var2);
      var3.putUUID("ftboss:non_manor_finale_summoner", var1.getUUID());
      finishActivation(var0, var1, var2, "石像默认难度");
   }

   private static void activate(LivingEntity var0, Pending var1) {
      CompoundTag var2 = var0.getPersistentData();
      var2.putBoolean("ftboss:non_manor_finale", true);
      var2.putInt("ftboss:non_manor_finale_profile", var1.profile);
      var2.putLong("ftboss:non_manor_finale_statue_pos", var1.statue.asLong());
      var2.putString("ftboss:non_manor_finale_statue_dim", var1.dimension.toString());
      var2.putUUID("ftboss:non_manor_finale_summoner", var1.player);
      ServerPlayer var3 = var0.level().getServer().getPlayerList().getPlayer(var1.player);
      finishActivation(var0, var3, var1.profile, "终结召唤物");
   }

   private static void finishActivation(LivingEntity var0, ServerPlayer var1, int var2, String var3) {
      CompoundTag var4 = var0.getPersistentData();
      applyAttributes(var0, true);
      if (!var4.getBoolean("ftboss:non_manor_finale_badged")) {
         Component var5 = var0.getCustomName() == null ? var0.getName() : var0.getCustomName();
         var0.setCustomName(var5.copy().append(Component.literal(" [终结]").withStyle(ChatFormatting.GOLD)));
         var0.setCustomNameVisible(true);
         var4.putBoolean("ftboss:non_manor_finale_badged", true);
      }

      if (var1 != null) {
         var1.sendSystemMessage(
            Component.translatable("message.ftboss.finale.started", new Object[]{bossName(var2)})
               .withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD})
         );
      }

      AttributeInstance var7 = var0.getAttribute(Attributes.ATTACK_DAMAGE);
      AttributeInstance var6 = var0.getAttribute(Attributes.MOVEMENT_SPEED);
      FTBossMod.LOGGER
         .info(
            "[FTBoss][终结难度] 已通过{}强化 {} ({})：生命={}/{}，攻击={}，移速={}，施法速度×2，并行施法×2",
            new Object[]{
               var3,
               bossName(var2),
               var0.getUUID(),
               var0.getHealth(),
               var0.getMaxHealth(),
               var7 == null ? "--" : var7.getValue(),
               var6 == null ? "--" : var6.getValue()
            }
         );
   }

   public static void tick(LivingEntity var0) {
      if (isFinale(var0)) {
         applyAttributes(var0, false);
      }
   }

   private static void applyAttributes(LivingEntity var0, boolean var1) {
      float var2 = var0.getMaxHealth();
      float var3 = var2 > 0.0F ? var0.getHealth() / var2 : 1.0F;
      boolean var4 = false;
      AttributeInstance var5 = var0.getAttribute(Attributes.MAX_HEALTH);
      if (var5 != null && var5.getModifier(ATTRIBUTE_ID) == null) {
         var5.addPermanentModifier(new AttributeModifier(ATTRIBUTE_ID, 1.0, Operation.ADD_MULTIPLIED_TOTAL));
         var4 = true;
      }

      for (AttributeInstance var7 : List.copyOf(var0.getAttributes().getSyncableAttributes())) {
         if (var7.getModifier(ATTRIBUTE_ID) == null) {
            var7.addPermanentModifier(new AttributeModifier(ATTRIBUTE_ID, 1.0, Operation.ADD_MULTIPLIED_TOTAL));
            var4 = true;
         }
      }

      if (var1) {
         var0.setHealth(var0.getMaxHealth());
      } else if (var4) {
         var0.setHealth(Math.max(1.0F, Math.min(var0.getMaxHealth(), var3 * var0.getMaxHealth())));
      }
   }

   public static boolean isFinale(LivingEntity var0) {
      return var0 != null && var0.getPersistentData().getBoolean("ftboss:non_manor_finale");
   }

   public static int acceleratedTicks(LivingEntity var0, int var1) {
      return isFinale(var0) && var1 > 0 ? (var1 + 2 - 1) / 2 : var1;
   }

   public static int parallelSpells(LivingEntity var0) {
      return isFinale(var0) ? 2 : 1;
   }

   public static int matchingTint(CandyStatueBlock var0, ItemStack var1) {
      if (var0 != null && var1 != null) {
         int var2 = var0.ownTint();
         Item var3 = summonItem(var2);
         return var3 != null && var1.is(var3) ? var2 : 0;
      } else {
         return 0;
      }
   }

   private static int profileForItem(ItemStack var0) {
      if (var0 != null && !var0.isEmpty()) {
         for (int var1 = 1; var1 <= 10; var1++) {
            Item var2 = summonItem(var1);
            if (var2 != null && var0.is(var2)) {
               return var1;
            }
         }

         return 0;
      } else {
         return 0;
      }
   }

   public static boolean isSummonItem(ItemStack var0) {
      if (var0 != null && !var0.isEmpty()) {
         for (int var1 = 1; var1 <= 10; var1++) {
            Item var2 = summonItem(var1);
            if (var2 != null && var0.is(var2)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static Item summonItem(int var0) {
      return switch (var0) {
         case 1 -> (Item)FTBossItems.SUGAR_QUEEN_CRYSTAL.get();
         case 2 -> (Item)FTBossItems.MEAT_HEART.get();
         case 3 -> (Item)FTBossItems.TEA_SEED.get();
         case 4 -> (Item)FTBossItems.WINE_CHARM.get();
         case 5 -> (Item)FTBossItems.RAT_BAIT.get();
         case 6 -> (Item)FTBossItems.DEFECT_CORE.get();
         case 7 -> (Item)FTBossItems.COMPOST_SUMMON.get();
         case 8 -> (Item)FTBossItems.FORBIDDEN_APPLE.get();
         case 9 -> (Item)FTBossItems.TIDE_BANQUET.get();
         case 10 -> (Item)FTBossItems.BENGER_WHISTLE.get();
         default -> null;
      };
   }

   private static int profileOf(Entity var0) {
      if (var0 == null) {
         return 0;
      } else if (var0.getType() == FTEntities.SUGAR_QUEEN.get() && var0 instanceof SugarQueenEntity) {
         return 1;
      } else if (var0.getType() == FTEntities.MEAT_MOUNTAIN.get() && var0 instanceof MeatMountainEntity) {
         return 2;
      } else if (var0.getType() == FTEntities.TEA_TREE.get() && var0 instanceof TeaTreeBossEntity) {
         return 3;
      } else if (var0.getType() == FTEntities.WINE_BARREL.get() && var0 instanceof WineBarrelBossEntity) {
         return 4;
      } else {
         if (var0.getType() == FTEntities.RAT_BOSS.get() && var0 instanceof RatBossEntity var1 && !var1.isMinion()) {
            return 5;
         }

         if (var0.getType() == FTEntities.DEFECT_CORE.get() && var0 instanceof DefectCoreEntity) {
            return 6;
         } else if (var0.getType() == FTEntities.COMPOST_BOSS.get() && var0 instanceof CompostBossEntity) {
            return 7;
         } else if (var0.getType() == FTEntities.APPLE_BOSS.get() && var0 instanceof AppleBossEntity) {
            return 8;
         } else if (var0.getType() == FTEntities.TIDE_FISH_BOSS.get() && var0 instanceof TideFishBossEntity) {
            return 9;
         } else {
            return var0.getType() == FTEntities.BENGER.get() && var0 instanceof BengerEntity ? 10 : 0;
         }
      }
   }

   private static String bossName(int var0) {
      return switch (var0) {
         case 1 -> "糖霜女皇";
         case 2 -> "屠夫";
         case 3 -> "沙耶";
         case 4 -> "酒桶王";
         case 5 -> "鼠群首领";
         case 6 -> "故障机器人";
         case 7 -> "炫彩答辩超人";
         case 8 -> "苹果爵士";
         case 9 -> "潮宴鱼王";
         case 10 -> "边牧犬王 benger";
         default -> "Boss";
      };
   }

   private static void finish(RightClickBlock var0) {
      var0.setCanceled(true);
      var0.setCancellationResult(InteractionResult.SUCCESS);
   }
}
