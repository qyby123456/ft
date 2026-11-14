package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.butcherskills.ButcherSummonFx;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;
import net.farmingtales.ftboss.boss.ai.ButcherMinionMeleeGoal;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.util.BossCollision;
import net.farmingtales.ftboss.util.BossCollision.SpawnMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class MeatBoss {
   private static final double MAX_HP = 380.0;
   private static final double DESPAWN_RANGE = 50.0;
   private static final int SUMMON_CD = 360;
   private static final int SUMMON_CD_P2 = 240;
   private static final int[][] WAVES = new int[][]{{2, 0}, {2, 1}, {3, 1}, {3, 1}};
   private static final int NO_PLAYER_GRACE = 1200;
   private static int noPlayerTicks = 0;
   private static final String[] MINION_NAMES = new String[]{"屠夫学徒", "腌肉尸壳", "屠夫队长·精英"};
   private static final ChatFormatting[] MINION_COLORS = new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GOLD, ChatFormatting.RED};
   private static boolean active = false;
   private static ServerLevel level;
   private static UUID bossUUID;
   private static UUID ownerUUID;
   private static boolean phase2 = false;
   private static boolean finalMode = false;
   private static boolean totemUsed = false;
   private static int tick = 0;
   private static int summonCd = 160;
   private static int waveIdx = 0;
   private static final List<UUID> minions = new ArrayList<>();

   private static boolean anyPlayerPresentOrDying(Entity var0) {
      if (level == null) {
         return false;
      } else {
         for (ServerPlayer var2 : level.players()) {
            if (!var2.isAlive()) {
               return true;
            }

            if (var2.distanceToSqr(var0) <= 2500.0) {
               return true;
            }
         }

         return false;
      }
   }

   private MeatBoss() {
   }

   private static boolean spawnClone(ServerPlayer var0) {
      try {
         ServerLevel var1 = var0.serverLevel();
         if (((EntityType)FTEntities.MEAT_MOUNTAIN.get()).create(var1) instanceof MeatMountainEntity var3) {
            Vec3 var4 = var0.getLookAngle();
            Vec3 var5 = var0.position().add(var4.x * 3.0, 0.0, var4.z * 3.0);
            var3.setCloneButcher(true);
            var3.equipMeatLeg();
            var3.setCustomName(Component.literal("§c§l").append(Component.translatable("boss.ftboss.butcher")).append(Component.literal("§7·复制体")));
            var3.setCustomNameVisible(true);
            var3.setHealth(380.0F);
            if (!BossCollision.place(var1, var3, new Vec3(var5.x, var0.getY(), var5.z), SpawnMode.GROUND, var0.getYRot() + 180.0F)) {
               BossCollision.tellNoRoom(var0, "屠夫复制体");
               return false;
            } else {
               var3.setPersistenceRequired();
               if (!ButcherSummonFx.addWithFx(var1,var3)) {
                  BossCollision.tellNoRoom(var0, "屠夫复制体");
                  return false;
               } else {
                  var1.playSound(null, var5.x, var5.y, var5.z, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0F, 0.8F);
                  var0.displayClientMessage(Component.literal("§c屠夫复制体加入战斗！"), true);
                  FTBossMod.LOGGER.info("[FTBoss] 屠夫复制体加入战斗 @ {} {} {}", new Object[]{var5.x, var5.y, var5.z});
                  return true;
               }
            }
         } else {
            return false;
         }
      } catch (Throwable var6) {
         FTBossMod.LOGGER.warn("[FTBoss] 屠夫复制体召唤失败: {}", var6.toString());
         return false;
      }
   }

   public static boolean tryStart(ServerPlayer var0) {
      if (!SummonGate.allow(var0.serverLevel(), var0, "屠夫")) {
         return false;
      } else {
         if (active && !trackedBossAlive()) {
            FTBossMod.LOGGER.info("[FTBoss] 屠夫状态自愈: 登记的实体已不在场, 清理后允许重新召唤");
            cleanup("自愈: 登记的实体不在场");
         }

         if (active) {
            return spawnClone(var0);
         } else {
            try {
               ServerLevel var1 = var0.serverLevel();
               if (((EntityType)FTEntities.MEAT_MOUNTAIN.get()).create(var1) instanceof MeatMountainEntity var3) {
                  int var4 = purgeStrays(var0.getServer());
                  if (var4 > 0) {
                     FTBossMod.LOGGER.info("[FTBoss] 屠夫召唤前清理残留实体 {} 只", var4);
                  }

                  Vec3 var5 = var0.getLookAngle();
                  Vec3 var6 = var0.position().add(var5.x * 2.5, 0.0, var5.z * 2.5);
                  if (!BossCollision.place(var1, var3, new Vec3(var6.x, var0.getY(), var6.z), SpawnMode.GROUND, var0.getYRot() + 180.0F)) {
                     BossCollision.tellNoRoom(var0, "屠夫");
                     return false;
                  } else {
                     var3.setCustomName(Component.literal("§c§l").append(Component.translatable("boss.ftboss.butcher")));
                     var3.setCustomNameVisible(true);
                     var3.setHealth(380.0F);
                     var3.setPersistenceRequired();
                     var3.equipMeatLeg();
                     level = var1;
                     bossUUID = var3.getUUID();
                     ownerUUID = var0.getUUID();
                     active = true;
                     phase2 = false;
                     finalMode = false;
                     totemUsed = false;
                     tick = 0;
                     summonCd = 160;
                     waveIdx = 0;
                     noPlayerTicks = 0;
                     minions.clear();
                     if (!ButcherSummonFx.addWithFx(var1,var3)) {
                        cleanup("入世界失败(addFreshEntity 返回 false)");
                        BossCollision.tellNoRoom(var0, "屠夫");
                        return false;
                     } else {
                        var1.playSound(null, var6.x, var6.y, var6.z, SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.2F, 0.7F);
                        FTBossMod.LOGGER
                           .info(
                              "[FTBoss] 屠夫 召唤 @ {} {} {} (实体id={}, 入世界后可按UUID取回={})",
                              new Object[]{
                                 String.format("%.1f", var6.x),
                                 String.format("%.1f", var6.y),
                                 String.format("%.1f", var6.z),
                                 var3.getId(),
                                 var1.getEntity(bossUUID) == var3
                              }
                           );
                        return true;
                     }
                  }
               } else {
                  var0.displayClientMessage(Component.literal("§c屠夫实体创建失败"), true);
                  FTBossMod.LOGGER.warn("[FTBoss] 屠夫召唤失败: create() 未返回屠夫实体");
                  return false;
               }
            } catch (Throwable var7) {
               FTBossMod.LOGGER.error("[FTBoss] 屠夫召唤异常: ", var7);
               cleanup("召唤过程抛异常");
               var0.displayClientMessage(Component.literal("§c屠夫召唤异常, 详见日志"), true);
               return false;
            }
         }
      }
   }

   private static boolean trackedBossAlive() {
      if (level != null && bossUUID != null) {
         if (level.getEntity(bossUUID) instanceof MeatMountainEntity var1 && !var1.isRemoved() && var1.isAlive()) {
            return true;
         }

         return false;
      } else {
         return false;
      }
   }

   public static int purgeStrays(MinecraftServer var0) {
      if (var0 == null) {
         return 0;
      } else {
         ArrayList<MeatMountainEntity> var1 = new ArrayList();

         for (ServerLevel var3 : var0.getAllLevels()) {
            for (Entity var5 : var3.getAllEntities()) {
               if (var5 instanceof MeatMountainEntity) {
                  MeatMountainEntity var6 = (MeatMountainEntity)var5;
                  if (!var6.getUUID().equals(bossUUID) && !var6.isCloneButcher()) {
                     var1.add(var6);
                  }
               }
            }
         }

         for (MeatMountainEntity var8 : var1) {
            var8.hideBossBar();
            var8.discard();
         }

         return var1.size();
      }
   }

   public static void tick() {
      if (active && level != null) {
         try {
            tick++;
            Entity var0 = level.getEntity(bossUUID);
            if (var0 == null) {
               cleanup("实体不在已加载区块(被卸载/未同步)");
            } else {
               if (!(var0 instanceof Mob var1) || var1.isRemoved()) {
                  cleanup("实体已被移除");
                  return;
               }

               if (var1.getHealth() <= 0.0F) {
                  cleanup("血量归零但未触发死亡逻辑");
               } else {
                  LivingEntity var2 = BossTargets.combatTarget(var1, 30.0);
                  if (var2 == null) {
                     if (anyPlayerPresentOrDying(var1)) {
                        noPlayerTicks = 0;
                     } else if (++noPlayerTicks > 1200) {
                        cleanup("附近长时间(60 秒)无玩家");
                     }
                  } else {
                     noPlayerTicks = 0;
                     double var3 = (double)var1.distanceTo(var2);
                     var1.setTarget(var2);
                     if (phase2) {
                        summonCd--;
                     }

                     if (phase2 && summonCd <= 0) {
                        summonCd = 240;
                        int[] var5 = WAVES[waveIdx % WAVES.length];
                        waveIdx++;
                        if (summonWave(var5[0], var5[1], var2) > 0) {
                           broadcast("§c屠夫 §7嚎叫一声，屠夫学徒们围了上来…");
                        }
                     }

                     if (tick % 5 == 0) {
                        minions.removeIf(id -> {
                           Entity summon = level.getEntity(id);
                           return summon == null || !summon.isAlive() || summon.isRemoved();
                        });

                        for (UUID var6 : minions) {
                           Entity var7 = level.getEntity(var6);
                           if (var7 instanceof Mob) {
                              Mob var8 = (Mob)var7;
                              if (var8.isAlive()) {
                                 var8.setTarget(var8.isAlliedTo(var2) ? null : var2);
                              }
                           }
                        }
                     }

                     if (tick % 10 == 0) {
                        int var11 = (int)Math.max(0.0, Math.ceil((double)var1.getHealth()));

                        for (ServerPlayer var13 : level.players()) {
                           if (var13.distanceToSqr(var1) < 2500.0) {
                              var13.displayClientMessage(Component.translatable("msg.ftboss.butcher_hp", new Object[]{var11, (int)var1.getMaxHealth()}), true);
                           }
                        }
                     }

                     if (tick % 100 == 0) {
                        FTBossMod.LOGGER
                           .info(
                              "[FTBoss] 屠夫: 血={}/{} 距离={} 波次CD={}",
                              new Object[]{(int)var1.getHealth(), (int)var1.getMaxHealth(), String.format("%.1f", var3), summonCd}
                           );
                     }
                  }
               }
            }
         } catch (Throwable var9) {
            FTBossMod.LOGGER.error("[FTBoss] 屠夫战斗异常: ", var9);
         }
      }
   }

   public static float onMeatHitPlayer(ServerPlayer var0, float var1) {
      return var1;
   }

   private static int summonWave(int var0, int var1, LivingEntity var2) {
      var0 = ButcherCombatRules.summonCount(var0, minions.size());
      int var3 = 0;
      int var4 = 0;

      while (var3 < var0 && var4 < var0 * 5) {
         var4++;

         try {
            double var5 = Math.random() * Math.PI * 2.0;
            double var7 = 3.0 + Math.random() * 6.0;
            double var9 = bossX() + Math.cos(var5) * var7;
            double var11 = bossZ() + Math.sin(var5) * var7;
            double var13 = findSafeY(var9, var11);
            if (var13 != Double.MIN_VALUE) {
               boolean var15 = var3 < var1;
               int var16 = var15 ? 2 : var3 % 2;
               Mob var17 = createMinion(var16);
               if (var17 != null) {
                  var17.teleportTo(var9, var13, var11);
                  var17.setCustomName(Component.literal(MINION_NAMES[var16]).withStyle(MINION_COLORS[var16]));
                  var17.setCustomNameVisible(true);
                  var17.setGlowingTag(true);
                  MinionBlessing.mark(var17, bossUUID);
                  boostMinion(var17, var16);
                  if (ButcherSummonFx.addWithFx(level,var17)) {
                     minions.add(var17.getUUID());
                     if (var2 != null) {
                        var17.setTarget(var2);
                     }

                     var3++;
                  }
               }
            }
         } catch (Throwable var18) {
            FTBossMod.LOGGER.error("[FTBoss] 屠夫波次召唤异常: ", var18);
         }
      }

      return var3;
   }

   private static Mob createMinion(int var0) {
      try {
         return var0 == 1 ? (Mob)EntityType.HUSK.create(level) : (Mob)EntityType.VINDICATOR.create(level);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static void boostMinion(Mob var0, int var1) {
      var0.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ButcherCombatRules.minionHealth(var1));
      var0.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ButcherCombatRules.minionDamage(var1));
      var0.getAttribute(Attributes.ARMOR).setBaseValue(ButcherCombatRules.minionArmor(var1));
      var0.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(ButcherCombatRules.minionSpeed(var1));
      var0.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(var1 == 2 ? 0.7 : 0.4);
      var0.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(48.0);
      var0.setHealth(var0.getMaxHealth());
      var0.setPersistenceRequired();
      var0.setCanPickUpLoot(false);
      if (var0 instanceof PathfinderMob var2) {
         var0.goalSelector.addGoal(1, new ButcherMinionMeleeGoal(var2, var1));
      }

      if (var1 != 1) {
         ItemStack var3 = new ItemStack(var1 == 2 ? Items.DIAMOND_AXE : Items.IRON_AXE);
         var3.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), var1 == 2 ? 4 : 3);
         var0.setItemSlot(EquipmentSlot.MAINHAND, var3);
         var0.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
      }

      if (var1 == 2) {
         var0.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 999999, 0, false, false));
      }
   }

   private static double bossX() {
      Entity var0 = level.getEntity(bossUUID);
      return var0 != null ? var0.getX() : 0.0;
   }

   private static double bossY() {
      Entity var0 = level.getEntity(bossUUID);
      return var0 != null ? var0.getY() : 100.0;
   }

   private static double bossZ() {
      Entity var0 = level.getEntity(bossUUID);
      return var0 != null ? var0.getZ() : 0.0;
   }

   private static double findSafeY(double var0, double var2) {
      int var4 = (int)Math.floor(bossY());

      for (int var5 = 0; var5 >= -14; var5--) {
         int var6 = var4 + var5;
         if (isSafe(var0, var6, var2)) {
            return (double)var6 + 0.6;
         }
      }

      for (int var7 = 1; var7 <= 14; var7++) {
         int var8 = var4 + var7;
         if (isSafe(var0, var8, var2)) {
            return (double)var8 + 0.6;
         }
      }

      return Double.MIN_VALUE;
   }

   private static boolean isSafe(double var0, int var2, double var3) {
      BlockPos var5 = BlockPos.containing(var0, (double)var2, var3);
      return level.isEmptyBlock(var5) && level.isEmptyBlock(var5.above()) && !level.isEmptyBlock(var5.below());
   }

   public static boolean tryPopTotem(Mob var0) {
      if (LemonSummonRewards.suppressed(var0)) {
         return false;
      } else {
         if (var0 instanceof MeatMountainEntity var1 && ButcherPhases.advance(var1)) {
            return true;
         }

         return false;
      }
   }

   private static void broadcast(String var0) {
      FTBossMod.LOGGER.info("[FTBoss] {}", var0);
   }

   public static void onBossDeath(Mob var0) {
      if (!LemonSummonRewards.suppressed(var0)) {
         if (active && var0 != null && var0.getUUID().equals(bossUUID)) {
            try {
               double var1 = var0.getX();
               double var3 = var0.getY();
               double var5 = var0.getZ();
               int var7 = 12 + level.random.nextInt(16);
               int var8 = 8 + level.random.nextInt(8);
               level.addFreshEntity(new ItemEntity(level, var1, var3, var5, new ItemStack(Items.COOKED_PORKCHOP, var7)));
               level.addFreshEntity(new ItemEntity(level, var1, var3 + 0.3, var5, new ItemStack(Items.COOKED_BEEF, var8)));
               level.addFreshEntity(new ItemEntity(level, var1, var3 + 0.6, var5, new ItemStack(((Block)FTBlocks.STATUE_BUTCHER.get()).asItem(), 1)));
               if (level.random.nextFloat() < 0.3F) {
                  ItemStack var9 = new ItemStack(Items.PAPER);
                  var9.set(DataComponents.CUSTOM_NAME, Component.literal("§6秘制烤肉配方"));
                  level.addFreshEntity(new ItemEntity(level, var1, var3 + 0.9, var5, var9));
               }

               level.addFreshEntity(new ExperienceOrb(level, var1, var3, var5, 150));
               level.playSound(null, var1, var3, var5, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
               broadcast("§c屠夫 §7倒下了——掉了一地烤肉和一座石像…");
               FTBossMod.LOGGER.info("[FTBoss] 屠夫 被击败");
            } catch (Throwable var13) {
               FTBossMod.LOGGER.error("[FTBoss] 屠夫死亡掉落异常: ", var13);
            } finally {
               cleanup("被击败");
            }
         }
      }
   }

   public static void cleanup(String var0) {
      if (active) {
         FTBossMod.LOGGER.info("[FTBoss] 屠夫 清场: {}", var0);
      }

      active = false;
      if (level != null) {
         for (UUID var2 : minions) {
            Entity var3 = level.getEntity(var2);
            if (var3 != null) {
               var3.discard();
            }
         }

         minions.clear();
         if (bossUUID != null) {
            Entity var4 = level.getEntity(bossUUID);
            if (var4 != null) {
               if (var4 instanceof MeatMountainEntity var5) {
                  var5.hideBossBar();
               }

               var4.discard();
            }
         }
      }

      bossUUID = null;
      ownerUUID = null;
      level = null;
      tick = 0;
      finalMode = false;
      totemUsed = false;
   }

   public static void cleanup() {
      cleanup("未注明(外部调用)");
   }

   public static boolean isActive() {
      return active;
   }

   public static boolean isBoss(Entity var0) {
      return active && var0 != null && var0.getUUID().equals(bossUUID);
   }

   public static UUID ownerIdOf() {
      return ownerUUID;
   }
   public static void onPhaseChanged(MeatMountainEntity var0, int var1) {
      if (isBoss(var0)) {
         phase2 = var1 == 2;
         finalMode = var1 == 3;
         summonCd = 160;
         waveIdx = 0;
         if (!phase2 && level != null) {
            for (UUID var3 : minions) {
               Entity var4 = level.getEntity(var3);
               if (var4 != null) {
                  var4.discard();
               }
            }

            minions.clear();
         }
      }
   }
}
