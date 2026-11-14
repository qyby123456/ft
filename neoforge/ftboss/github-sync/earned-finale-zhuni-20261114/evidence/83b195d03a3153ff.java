package net.farmingtales.ftboss.boss;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ManorArenaChunks;
import net.farmingtales.ftboss.boss.ai.ManorEntryBuffAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.ManorRecallBoundaryPacket;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.FullFightFlightRules;
import net.farmingtales.ftboss.util.ManorCombatRules;
import net.farmingtales.ftboss.util.ManorTeleportRules;
import net.farmingtales.ftboss.util.ManorTeleportRules.Recall;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ManorFight {
   public static final int NEXT_DELAY = 60;
   public static final int UNLOAD_GRACE_TICKS = 600;
   public static final int RECALL_CD = 100;
   private static final double SIX_CHUNKS = 96.0;
   public static final double STRAY_FROM_BOSS = 76.0;
   public static final int NO_TARGET_RECALL_TICKS = 200;
   public static final double ATTACH_RANGE = 96.0;
   private static final Map<UUID, Long> LAST_RECALL = new ConcurrentHashMap<>();
   private static final int RECALL_CAP = 64;
   public static final String BAR_NAME = "甜苹果庄园";
   private static final List<ManorFight.Fight> ACTIVE = new CopyOnWriteArrayList<>();
   private static MinecraftServer activeServer;
   private static final int RELOCATION_COOLDOWN = 200;
   private static long lastPump = -1L;
   private static long lastShredPump = -1L;

   private static void pruneRecall(long var0) {
      if (LAST_RECALL.size() > 64) {
         LAST_RECALL.entrySet().removeIf(var2 -> var0 - var2.getValue() > 6000L);
         if (LAST_RECALL.size() > 64) {
            LAST_RECALL.clear();
         }
      }
   }

   private static String progressTitle(int var0) {
      return "§c§l甜苹果庄园 §7已击败 §c" + var0 + "§7/§c" + EasterBossEntity.MANOR_COUNT;
   }

   private static void updateBar(ManorFight.Fight var0) {
      int var1 = var0.defeated.size();
      var0.bar.setName(Component.literal(progressTitle(var1) + var0.difficulty.badge()));
      var0.bar.setProgress(Math.min(1.0F, (float)var1 / (float)EasterBossEntity.MANOR_COUNT));
   }

   private static void initialize(MinecraftServer var0) {
      if (activeServer != var0) {
         ACTIVE.forEach(var0x -> {
            var0x.pendingPlayerRecalls.clear();
            var0x.bar.removeAllPlayers();
         });
         ACTIVE.clear();
         ManorArenaChunks.reset();
         LAST_RECALL.clear();
         ManorMercy.reset();
         lastPump = -1L;
         lastShredPump = -1L;
         activeServer = var0;
         ManorMercy.restore(ManorFightData.get(var0).deaths());

         for (Tag var2 : ManorFightData.get(var0).active()) {
            CompoundTag var3 = (CompoundTag)var2;

            try {
               ResourceKey var4 = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(var3.getString("Dimension")));
               if (var3.hasUUID("Id") && !ManorFightData.get(var0).retired(var3.getUUID("Id"))) {
                  ManorFight.Fight var5 = new ManorFight.Fight(
                     var4, new Vec3(var3.getDouble("X"), var3.getDouble("Y"), var3.getDouble("Z")), var3.hasUUID("Summoner") ? var3.getUUID("Summoner") : null
                  );
                  var5.id = var3.getUUID("Id");
                  var5.difficulty = ManorDifficulty.fromId(var3.getString("Difficulty"));
                  var5.currentId = var3.hasUUID("Current") ? var3.getUUID("Current") : null;
                  if (var5.currentId != null) {
                     var5.onStage.add(var5.currentId);
                  }

                  var5.currentVariant = var3.getInt("Variant");
                  var5.lastPosition = new Vec3(var3.getDouble("BossX"), var3.getDouble("BossY"), var3.getDouble("BossZ"));

                  for (int var9 : var3.getIntArray("Defeated")) {
                     if (isManorVariant(var9)) {
                        var5.defeated.add(var9);
                     }
                  }

                  for (Tag var15 : var3.getList("Participants", 10)) {
                     CompoundTag var16 = (CompoundTag)var15;
                     if (var16.hasUUID("Id")) {
                        var5.participants.add(var16.getUUID("Id"));
                     }
                  }

                  var5.refillPool(var5.currentVariant);
                  var5.nextDelay = var5.defeated.contains(var5.currentVariant) ? 60 : 0;
                  ACTIVE.add(var5);
                  updateBar(var5);
                  FTBossMod.LOGGER.info("[FTBoss][Manor] 恢复战场 {}，已击败 {}/{}", new Object[]{var5.id, var5.defeated.size(), EasterBossEntity.MANOR_COUNT});
               }
            } catch (RuntimeException var10) {
               FTBossMod.LOGGER.warn("[FTBoss][Manor] 忽略损坏的战场记录", var10);
            }
         }

         for (ManorFight.Fight var12 : ACTIVE) {
            ServerLevel var13 = var0.getLevel(var12.dim);
            if (var13 != null) {
               ManorArenaChunks.hold(var13, var12.anchor);
            }
         }
      }
   }

   private static void persist() {
      if (activeServer != null) {
         ListTag var0 = new ListTag();

         for (ManorFight.Fight var2 : ACTIVE) {
            CompoundTag var3 = new CompoundTag();
            var3.putUUID("Id", var2.id);
            var3.putString("Difficulty", var2.difficulty.id());
            var3.putString("Dimension", var2.dim.location().toString());
            if (var2.summonerId != null) {
               var3.putUUID("Summoner", var2.summonerId);
            }

            if (var2.currentId != null) {
               var3.putUUID("Current", var2.currentId);
            }

            var3.putInt("Variant", var2.currentVariant);
            var3.putDouble("X", var2.anchor.x);
            var3.putDouble("Y", var2.anchor.y);
            var3.putDouble("Z", var2.anchor.z);
            var3.putDouble("BossX", var2.lastPosition.x);
            var3.putDouble("BossY", var2.lastPosition.y);
            var3.putDouble("BossZ", var2.lastPosition.z);
            var3.putIntArray("Defeated", var2.defeated.stream().mapToInt(Integer::intValue).toArray());
            ListTag var4 = new ListTag();

            for (UUID var6 : var2.participants) {
               CompoundTag var7 = new CompoundTag();
               var7.putUUID("Id", var6);
               var4.add(var7);
            }

            var3.put("Participants", var4);
            var0.add(var3);
         }

         ManorFightData.get(activeServer).store(var0);
      }
   }

   private static ManorFight.Fight find(UUID var0) {
      if (var0 == null) {
         return null;
      } else {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.id.equals(var0)) {
               return var2;
            }
         }

         return null;
      }
   }

   public static ManorDifficulty difficulty(UUID var0) {
      ManorFight.Fight var1 = find(var0);
      return var1 == null ? ManorDifficulty.NORMAL : var1.difficulty;
   }

   public static boolean isImpossibleFor(Player var0) {
      if (var0 == null) {
         return false;
      } else {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.difficulty == ManorDifficulty.IMPOSSIBLE
               && var2.dim.equals(var0.level().dimension())
               && (var2.participants.contains(var0.getUUID()) || var0.distanceToSqr(var2.anchor.x, var2.anchor.y, var2.anchor.z) <= 9216.0)) {
               return true;
            }
         }

         return false;
      }
   }

   public static UUID fightId(Entity var0) {
      return ManorFightMembers.fightId(ManorFightMembers.resolve(var0));
   }

   public static UUID currentBoss(UUID var0) {
      ManorFight.Fight var1 = find(var0);
      return var1 == null ? null : var1.currentId;
   }

   public static UUID summoner(UUID var0) {
      ManorFight.Fight var1 = find(var0);
      return var1 == null ? null : var1.summonerId;
   }

   public static boolean hasActive(ServerLevel var0) {
      initialize(var0.getServer());
      return ACTIVE.stream().anyMatch(var1 -> var1.dim.equals(var0.dimension()));
   }

   public static boolean hasActive(UUID var0) {
      return find(var0) != null;
   }

   public static int currentVariant(UUID var0) {
      ManorFight.Fight var1 = find(var0);
      return var1 == null ? -1 : var1.currentVariant;
   }

   public static boolean canSurrender(ServerPlayer var0, UUID var1) {
      ManorFight.Fight var2 = find(var1);
      return var2 != null && ManorCombatRules.canSurrender(var2.summonerId, var0.getUUID(), var2.id, var1);
   }

   public static boolean surrender(ServerPlayer var0, UUID var1) {
      if (!canSurrender(var0, var1)) {
         return false;
      } else {
         ManorFight.Fight var2 = find(var1);
         ServerLevel var3 = var0.server.getLevel(var2.dim);
         if (var3 == null) {
            return false;
         } else {
            endFight(var3, var2, false, "召唤者投降");
            return true;
         }
      }
   }

   public static boolean abortForHardcore(ServerPlayer var0, UUID var1) {
      if (var0 == null) {
         return false;
      } else {
         initialize(var0.server);
         ManorFight.Fight var2 = find(var1);
         if (var2 != null && !var2.dim.equals(var0.level().dimension())) {
            var2 = null;
         }

         if (var2 == null) {
            for (ManorFight.Fight var4 : ACTIVE) {
               if (var4.dim.equals(var0.level().dimension()) && var4.participants.contains(var0.getUUID())) {
                  var2 = var4;
                  break;
               }
            }
         }

         if (var2 == null) {
            return HiddenBossIntro.stopForHardcore(var0);
         } else {
            ServerLevel var5 = var0.server.getLevel(var2.dim);
            if (var5 == null) {
               return false;
            } else {
               endFight(var5, var2, false, "极限模式玩家死亡前中止");
               return true;
            }
         }
      }
   }

   public static boolean stopForCreative(ServerPlayer var0) {
      if (var0 != null && var0.isCreative()) {
         ServerLevel var1 = var0.serverLevel();
         initialize(var1.getServer());
         boolean var2 = HiddenBossChallenge.cancelForCreative(var1);

         for (ManorFight.Fight var4 : new ArrayList<>(ACTIVE)) {
            if (var4.dim.equals(var1.dimension())) {
               endFight(var1, var4, false, "创造道具中止");
               var2 = true;
            }
         }

         return HiddenBossIntro.stopForCreative(var1) || var2;
      } else {
         return false;
      }
   }

   public static void participate(Entity var0, ServerPlayer var1) {
      ManorFight.Fight var2 = find(fightId(var0));
      if (var2 != null && var2.participants.add(var1.getUUID())) {
         persist();
      }
   }

   public static void attackedBy(EasterBossEntity var0, ServerPlayer var1) {
      ManorFight.Fight var2 = find(fightId(var0));
      if (var2 != null) {
         if (var2.summonerId == null) {
            var2.summonerId = var1.getUUID();

            for (Entity var4 : members(var1.server, var2)) {
               if (var4 instanceof EasterBossEntity var5) {
                  var5.setManorSummonerId(var2.summonerId);
               }
            }

            persist();
         }

         if (var2.participants.add(var1.getUUID())) {
            persist();
         }
      }
   }

   private static List<Entity> members(MinecraftServer var0, ManorFight.Fight var1) {
      ArrayList var2 = new ArrayList();

      for (ServerLevel var4 : var0.getAllLevels()) {
         for (Entity var6 : var4.getEntities().getAll()) {
            if (ManorFightMembers.fightId(var6) == null) {
               Entity var7 = ManorFightMembers.resolve(var6);
               if (var7 != var6) {
                  ManorFightMembers.inherit(var7, var6);
               }
            }

            if (var1.id.equals(ManorFightMembers.fightId(var6))) {
               var2.add(var6);
            }
         }
      }

      return var2;
   }

   private static void bind(ManorFight.Fight var0, EasterBossEntity var1) {
      ManorFightMembers.bind(var1, var0.id);
      var1.setChallengeDifficulty(var0.difficulty);
      SparrowSupport.noteFightMember(var1);
      var1.setManorSummonerId(var0.summonerId);
      if (!HiddenBossIntro.isActing(var1.getUUID())) {
         var1.setNoAi(false);
         var1.setInvulnerable(false);
      }
   }

   private static void endFight(ServerLevel var0, ManorFight.Fight var1, boolean var2, String var3) {
      if (!ManorFightData.get(var0.getServer()).retired(var1.id)) {
         syncRecallBoundary(var0, var1, false);
         ManorTransition.cancel(var0, var1.id);
         if (var2) {
            ManorVictory.begin(var0, var1.id, var1.anchor, var1.participants);
            if (var1.difficulty != ManorDifficulty.NORMAL) {
               BossAdvancements.onManorImpossibleVictory(var0, var1.participants);
            } else {
               for (UUID var5 : var1.participants) {
                  ManorVictoryData.get(var0.getServer()).recordConqueror(var5);
               }

               BossAdvancements.onManorNormalVictory(var0, var1.participants);
            }
         }

         ManorFightData.get(var0.getServer()).retire(var1.id);
         ImpossibleSparrowSupport.finish(var0.getServer(), var1.id);
         ACTIVE.remove(var1);
         if (ACTIVE.stream().noneMatch(var1x -> var1x.dim.equals(var0.dimension()))) {
            ManorArenaChunks.release(var0);
         }

         var1.pendingPlayerRecalls.clear();
         BossBarVisibility.visible(var1.bar, false);
         var1.bar.removeAllPlayers();
         ManorMercy.clearFight(var1.id);

         for (Entity var11 : members(var0.getServer(), var1)) {
            if (var11 instanceof LivingEntity var6 && var11.level() instanceof ServerLevel var7) {
               BossMusic.release(var7, var6);
            }

            HiddenBossIntro.cancel(var11.getUUID());
            var11.discard();
         }

         for (UUID var12 : var1.participants) {
            ServerPlayer var13 = var0.getServer().getPlayerList().getPlayer(var12);
            LAST_RECALL.remove(var12);
            if (var13 != null) {
               ArmorShred.clear(var13);
               if (var2) {
                  var13.sendSystemMessage(
                     Component.literal(
                        var1.difficulty == ManorDifficulty.IMPOSSIBLE
                           ? "§4【甜苹果庄园·" + var1.difficulty.title() + "】§f挑战完成。§7本难度不发放庄园成就或通关头衔。"
                           : "§6【甜苹果庄园】§f普通难度通关！§7" + ManorDifficulty.IMPOSSIBLE.label() + "已解锁，可在下次挑战时选择。"
                     )
                  );
               } else {
                  var13.sendSystemMessage(Component.literal("§6【甜苹果庄园·" + var1.difficulty.title() + "】§7挑战已结束（" + var3 + "），不计击败、无掉落。"));
               }
            }
         }

         SparrowSupport.forgetSparrow();
         BossBars.sweep(var0.getServer());
         persist();
         FTBossMod.LOGGER.info("[FTBoss][Manor] 战场 {} 结束：{}，胜利={}", new Object[]{var1.id, var3, var2});
      }
   }

   public static boolean isManaged(LivingEntity var0) {
      for (ManorFight.Fight var2 : ACTIVE) {
         if (var2.onStage.contains(var0.getUUID())) {
            return true;
         }
      }

      return false;
   }

   @Deprecated
   public static boolean isFinaleMember(LivingEntity var0) {
      return false;
   }

   public static boolean isPlayerFighting(Player var0) {
      if (var0 == null) {
         return false;
      } else {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.dim.equals(var0.level().dimension()) && var0.distanceToSqr(var2.anchor.x, var2.anchor.y, var2.anchor.z) <= 9216.0) {
               return true;
            }
         }

         return false;
      }
   }

   public static void adopt(ServerLevel var0, EasterBossEntity var1) {
      if (var0 != null && var1 != null) {
         initialize(var0.getServer());
         if (!HiddenBossIntro.isActing(var1.getUUID()) && SummonGate.isAllowedDim(var0)) {
            UUID var2 = ManorFightMembers.fightId(var1);
            if (ManorFightData.get(var0.getServer()).retired(var2)) {
               var1.discard();
            } else {
               clear(var0.dimension());
               ManorFight.Fight var3 = new ManorFight.Fight(var0.dimension(), var1.position(), var1.getManorSummonerId());
               var3.difficulty = var1.getChallengeDifficulty();
               if (var2 != null) {
                  var3.id = var2;
               }

               bind(var3, var1);
               var3.currentVariant = var1.getVariant();
               var3.currentId = var1.getUUID();
               var3.current = var1;
               var3.onStage.add(var1.getUUID());
               var3.refillPool(var1.getVariant());
               ACTIVE.add(var3);
               ManorArenaChunks.hold(var0, var3.anchor);

               for (Entity var5 : var0.getEntities().getAll()) {
                  if (var5 instanceof EasterBossEntity) {
                     EasterBossEntity var6 = (EasterBossEntity)var5;
                     if (var6.getVariant() == 9 && var3.summonerId != null && var3.summonerId.equals(var6.getManorSummonerId())) {
                        bind(var3, var6);
                     }
                  }
               }

               purgeStale(var0, var1.getUUID(), true);
               SparrowSupport.beginStage(var0, var1);

               for (ServerPlayer var8 : var0.players()) {
                  if (var8.distanceToSqr(var1) <= 9216.0) {
                     BossBarVisibility.addPlayer(var3.bar, var8);
                  }
               }

               updateBar(var3);
               persist();
               FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={} 血量={}", EasterBossEntity.NAMES[var1.getVariant()], (int)var1.getMaxHealth());
            }
         }
      }
   }

   public static void start(ServerLevel var0, Vec3 var1, int var2) {
      start(var0, var1, var2, null);
   }

   public static void start(ServerLevel var0, Vec3 var1, int var2, UUID var3) {
      start(var0, var1, var2, var3, ManorDifficulty.NORMAL);
   }

   public static void start(ServerLevel var0, Vec3 var1, int var2, UUID var3, ManorDifficulty var4) {
      if (var0 != null) {
         initialize(var0.getServer());
         if (SummonGate.isAllowedDim(var0)) {
            clear(var0.dimension());
            ManorFight.Fight var5 = new ManorFight.Fight(var0.dimension(), var1, var3);
            var5.difficulty = var4 == null ? ManorDifficulty.NORMAL : var4;
            var5.refillPool(var2);
            ACTIVE.add(var5);
            ManorArenaChunks.hold(var0, var5.anchor);

            for (Entity var7 : var0.getEntities().getAll()) {
               if (var7 instanceof EasterBossEntity) {
                  EasterBossEntity var8 = (EasterBossEntity)var7;
                  if (var8.getVariant() == 9 && var3 != null && var3.equals(var8.getManorSummonerId())) {
                     bind(var5, var8);
                  }
               }
            }

            purgeStale(var0, null, true);
            spawnVariant(var0, var5, var2);
            updateBar(var5);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={}", EasterBossEntity.NAMES[var2]);
         }
      }
   }

   public static void clear(ResourceKey<Level> var0) {
      if (activeServer != null) {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.dim.equals(var0)) {
               ServerLevel var3 = activeServer.getLevel(var0);
               if (var3 != null) {
                  endFight(var3, var2, false, "战场重置");
               }
            }
         }
      }
   }

   public static boolean rescue(EasterBossEntity var0) {
      if (!(var0.level() instanceof ServerLevel var1)) {
         return false;
      } else {
         ManorFight.Fight var8 = find(fightId(var0));
         long var3 = var1.getGameTime();
         if (var8 != null) {
            if (var8.lastVoidFall == Long.MIN_VALUE || var3 - var8.lastVoidFall > 600L) {
               var8.voidFalls = 0;
            }

            var8.lastVoidFall = var3;
            var8.voidFalls++;
            if (var8.voidFalls >= 2 && ManorCombatRules.relocationReady(var3, var8.lastRelocation, 200) && relocate(var1, var8, var0)) {
               return true;
            }
         }

         ServerPlayer var5 = var0.getManorSummonerId() == null ? null : var1.getServer().getPlayerList().getPlayer(var0.getManorSummonerId());
         Vec3 var6 = var0.canTeleportWith(var5) ? var5.position() : (var8 == null ? var0.position() : var8.anchor);
         Vec3 var7 = ManorSafeTeleport.find(var1, var0, var6, 16);
         if (var7 == null && var8 != null) {
            var7 = ManorSafeTeleport.find(var1, var0, var8.anchor, 24);
         }

         if (var7 != null) {
            var0.prepareArenaTransfer();
            ManorSafeTeleport.move(var1, var0, var7);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 坠空救援 {} {} -> {}", new Object[]{var0.getName().getString(), var0.getUUID(), var7});
            return true;
         } else {
            if (var8 != null) {
               endFight(var1, var8, false, "附近没有安全落点");
            } else {
               var0.discard();
               FTBossMod.LOGGER.warn("[FTBoss][Manor] 无主 Boss {} 无安全落点，取消而非计为击败", var0.getUUID());
            }

            return false;
         }
      }
   }

   private static boolean relocate(ServerLevel var0, ManorFight.Fight var1, EasterBossEntity var2) {
      var1.lastRelocation = var0.getGameTime();
      List var3 = members(var0.getServer(), var1);
      ServerPlayer var4 = var1.summonerId == null ? null : var0.getServer().getPlayerList().getPlayer(var1.summonerId);
      if (var4 != null && var4.isAlive() && !var4.isSpectator() && var4.level() == var0) {
         var3.add(var4);
      }

      for (int var5 = 0; var5 < 24; var5++) {
         double var6 = var0.random.nextDouble() * Math.PI * 2.0;
         double var8 = 52.0 + var0.random.nextDouble() * 20.0;
         Vec3 var10 = var1.anchor.add(Math.cos(var6) * var8, 0.0, Math.sin(var6) * var8);
         Vec3 var11 = ManorSafeTeleport.find(var0, var2, var10, 3);
         if (var11 != null && !(var11.subtract(var1.anchor).horizontalDistanceSqr() < 2304.0)) {
            LinkedHashMap var12 = new LinkedHashMap();
            boolean var13 = true;
            int var14 = 0;

            for (Entity var16 : var3) {
               if (var16 instanceof LivingEntity && var16.isAlive() && var16.level() == var0) {
                  double var17 = (double)(var14++) * 2.4;
                  Vec3 var19 = var16 == var2 ? var11 : var11.add(Math.cos(var17) * 4.0, 0.0, Math.sin(var17) * 4.0);
                  Vec3 var20 = ManorSafeTeleport.find(var0, var16, var19, 5);
                  if (var20 == null) {
                     var13 = false;
                     break;
                  }

                  var12.put(var16, var20);
               }
            }

            if (var13) {
               var1.anchor = var11;
               var1.voidFalls = 0;
               ManorArenaChunks.hold(var0, var1.anchor);

               for (Entry var23 : var12.entrySet()) {
                  if (var23.getKey() instanceof EasterBossEntity var25) {
                     var25.prepareArenaTransfer();
                  }

                  ManorSafeTeleport.move(var0, (Entity)var23.getKey(), (Vec3)var23.getValue());
               }

               for (Entity var24 : var3) {
                  if (!(var24 instanceof LivingEntity)) {
                     var24.discard();
                  }
               }

               if (var1.current != null) {
                  var1.lastPosition = var1.current.position();
               }

               persist();
               if (var4 != null) {
                  var4.displayClientMessage(Component.literal("§6【庄园】§7战场已安全搬迁，继续挑战。"), true);
               }

               FTBossMod.LOGGER.info("[FTBoss][Manor] 整场安全搬迁 {} -> {}，成员 {}", new Object[]{var1.id, var11, var12.size()});
               return true;
            }
         }
      }

      FTBossMod.LOGGER.warn("[FTBoss][Manor] 战场 {} 未找到安全搬迁点，保留原场地", var1.id);
      return false;
   }

   public static Vec3 anchorOf(ServerLevel var0) {
      for (ManorFight.Fight var2 : ACTIVE) {
         if (var2.dim.equals(var0.dimension())) {
            return var2.anchor;
         }
      }

      return null;
   }

   public static Vec3 anchorOf(UUID var0) {
      ManorFight.Fight var1 = find(var0);
      return var1 == null ? null : var1.anchor;
   }

   public static void reattach(ServerPlayer var0) {
      if (var0 != null) {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.dim.equals(var0.level().dimension()) && !(var0.distanceToSqr(var2.anchor.x, var2.anchor.y, var2.anchor.z) > 9216.0)) {
               var2.bar.removePlayer(var0);
               BossBarVisibility.addPlayer(var2.bar, var0);
               BossBarVisibility.visible(var2.bar, true);
               updateBar(var2);
               FTBossMod.LOGGER
                  .info(
                     "[FTBoss][Manor] 玩家 {} 重进存档 -> 已把庄园进度血条重挂上去(进度 {}/{})",
                     new Object[]{var0.getName().getString(), var2.defeated.size(), EasterBossEntity.MANOR_COUNT}
                  );
            }
         }
      }
   }

   private static boolean isManorVariant(int var0) {
      for (int var4 : EasterBossEntity.MANOR_VARIANTS) {
         if (var4 == var0) {
            return true;
         }
      }

      return false;
   }

   public static void recoverNear(MinecraftServer var0) {
      if (var0 != null) {
         initialize(var0);

         for (ServerLevel var2 : var0.getAllLevels()) {
            if (SummonGate.isAllowedDim(var2)) {
               boolean var3 = ACTIVE.stream().anyMatch(var1 -> var1.dim.equals(var2.dimension()));
               if (!var3) {
                  boolean var4 = false;

                  for (ServerPlayer var6 : var2.players()) {
                     for (EasterBossEntity var8 : var2.getEntitiesOfClass(EasterBossEntity.class, var6.getBoundingBox().inflate(160.0))) {
                        if (var8.isAlive()
                           && !var8.isRemoved()
                           && !var8.isMini()
                           && isManorVariant(var8.getVariant())
                           && !HiddenBossIntro.isActing(var8.getUUID())) {
                           UUID var9 = ManorFightMembers.fightId(var8);
                           if (!ManorFightData.get(var0).retired(var9)) {
                              adopt(var2, var8);
                              FTBossMod.LOGGER.info("[FTBoss][Manor] 收编无托管 Boss {}；召唤者={}", var8.getUUID(), var8.getManorSummonerId());
                              var4 = true;
                              break;
                           }

                           var8.discard();
                        }
                     }

                     if (var4) {
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   public static int purgeStale(ServerLevel var0, UUID var1, boolean var2) {
      if (var0 == null) {
         return 0;
      } else {
         ArrayList var3 = new ArrayList();

         for (Entity var5 : var0.getEntities().getAll()) {
            if (ManorFightData.get(var0.getServer()).retired(ManorFightMembers.fightId(var5))) {
               var3.add(var5);
            }
         }

         var3.forEach(Entity::discard);
         return var3.size();
      }
   }

   @Deprecated
   public static boolean onDamage(ServerLevel var0, EasterBossEntity var1, float var2, DamageSource var3) {
      return false;
   }

   private static void spawnVariant(ServerLevel var0, ManorFight.Fight var1, int var2) {
      EasterBossEntity var3 = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).create(var0);
      if (var3 == null) {
         endFight(var0, var1, false, "无法生成下一位 Boss");
      } else {
         double var4 = (double)((float)Math.toDegrees(Math.atan2(var1.anchor.z - var1.anchor.z, 1.0)));
         var3.moveTo(var1.anchor.x, var1.anchor.y, var1.anchor.z, (float)var4, 0.0F);
         var3.setVariant(var2);
         var3.setManorSummonerId(var1.summonerId);
         bind(var1, var3);
         ((ManorEntryBuffAccess)var3).prepareManorEntryBuffs();
         var3.setNoAi(false);
         if (!var0.addFreshEntity(var3)) {
            endFight(var0, var1, false, "下一位 Boss 生成被取消");
         } else {
            var1.onStage.add(var3.getUUID());
            var1.currentVariant = var2;
            var1.currentId = var3.getUUID();
            var1.current = var3;
            var1.nextDelay = 0;
            var1.unloadedTicks = 0;
            var1.lastPosition = var3.position();
            syncRecallBoundary(var0, var1, true);
            SparrowSupport.beginStage(var0, var3);
            persist();
            ManorTransition.arrive(var0, var1.id, var3.position(), var2);
            var0.playSound(null, var1.anchor.x, var1.anchor.y, var1.anchor.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 0.7F);
            FTBossMod.LOGGER
               .info(
                  "[FTBoss][Manor] 上场: {} (独立血量 {}, 已击败 {}/{})",
                  new Object[]{EasterBossEntity.NAMES[var2], (int)var3.getMaxHealth(), var1.defeated.size(), EasterBossEntity.MANOR_COUNT}
               );

            for (ServerPlayer var7 : var0.players()) {
               if (var7.distanceToSqr(var1.anchor.x, var1.anchor.y, var1.anchor.z) <= 9216.0) {
                  var7.sendSystemMessage(
                     Component.literal(
                        "§c【甜苹果庄园】§7第 §c"
                           + (var1.defeated.size() + 1)
                           + "§7 位上场：§c"
                           + EasterBossEntity.NAMES[var2]
                           + " §8(已击败 "
                           + var1.defeated.size()
                           + "/"
                           + EasterBossEntity.MANOR_COUNT
                           + ")"
                     )
                  );
               }
            }
         }
      }
   }

   private static void onCurrentDefeated(ServerLevel var0, ManorFight.Fight var1) {
      syncRecallBoundary(var0, var1, false);
      SparrowSupport.endStage(var0, var1.id);
      if (var1.currentVariant >= 0 && var1.defeated.add(var1.currentVariant)) {
         ManorMercy.clearFight(var1.id);
         persist();
         updateBar(var1);
         FTBossMod.LOGGER
            .info(
               "[FTBoss][Manor] 已击败 {} ({}/{})", new Object[]{EasterBossEntity.NAMES[var1.currentVariant], var1.defeated.size(), EasterBossEntity.MANOR_COUNT}
            );

         for (ServerPlayer var3 : var0.players()) {
            if (var3.distanceToSqr(var1.anchor) <= 9216.0) {
               var3.sendSystemMessage(
                  Component.literal(
                     "§6【甜苹果庄园】§7"
                        + EasterBossEntity.NAMES[var1.currentVariant]
                        + " §7已被击败！§8(进度 "
                        + var1.defeated.size()
                        + "/"
                        + EasterBossEntity.MANOR_COUNT
                        + ")"
                  )
               );
            }
         }
      }

      var1.onStage.clear();
      var1.pendingPlayerRecalls.clear();
      var1.currentId = null;
      var1.current = null;
   }

   private static void finish(ServerLevel var0, ManorFight.Fight var1) {
      endFight(var0, var1, true, "全部主战 Boss 已击败");
   }

   public static void pumpAll() {
      MinecraftServer var0 = ServerLifecycleHooks.getCurrentServer();
      if (var0 != null) {
         initialize(var0);
         long var1 = var0.overworld().getGameTime();
         if (var1 != lastShredPump) {
            lastShredPump = var1;
            ArmorShred.tick(var0);
         }

         if (!ACTIVE.isEmpty() && var1 != lastPump) {
            lastPump = var1;
            ManorArenaChunks.beginFrame();

            for (ManorFight.Fight var4 : ACTIVE) {
               ServerLevel var5 = var0.getLevel(var4.dim);
               if (var5 != null) {
                  var4.phase++;
                  if (var4.currentVariant >= 0) {
                     EasterBossEntity var6 = var4.current;
                     if (var4.currentId != null
                        && var5.getEntity(var4.currentId) == null
                        && !var4.defeated.contains(var4.currentVariant)
                        && var4.unloadedTicks % 100 == 20) {
                        var5.getChunk(BlockPos.containing(var4.lastPosition));
                     }

                     if (var4.currentId != null && var5.getEntity(var4.currentId) instanceof EasterBossEntity var7 && var7 != var6) {
                        var6 = var7;
                        var4.current = var7;
                        bind(var4, var7);
                     }

                     boolean var13 = var4.defeated.contains(var4.currentVariant) || var6 != null && (var6.isDeadOrDying() || var6.getHealth() <= 0.0F);
                     boolean var17 = var6 != null && var5.getEntity(var6.getUUID()) == var6;
                     if (var17 && !var13) {
                        var4.lastPosition = var6.position();
                        if (var4.phase % 20 == 0 && !SparrowSupport.hasVisited(var6)) {
                           SparrowSupport.beginStage(var5, var6);
                        }
                     }

                     if (!var13 && !var17) {
                        var4.unloadedTicks++;
                        if (var4.unloadedTicks == 601) {
                           FTBossMod.LOGGER
                              .warn(
                                 "[FTBoss][Manor] {} 连续 {} tick 不在世界里(区块一直没加载) -> 取消本场，不计击败", EasterBossEntity.NAMES[var4.currentVariant], var4.unloadedTicks
                              );
                        }
                     } else {
                        var4.unloadedTicks = 0;
                     }

                     if (!var13 && var4.unloadedTicks > 600) {
                        endFight(var5, var4, false, "Boss 卸载后无法恢复");
                        continue;
                     }

                     boolean var9 = ManorCombatRules.defeated(var13, var4.unloadedTicks);
                     if (var9) {
                        if (var4.nextDelay <= 0) {
                           onCurrentDefeated(var5, var4);
                           var4.nextDelay = 60;
                           if (var4.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                              ManorTransition.prepare(var5, var4.id, var4.anchor, var4.pool.peekFirst() == null ? -1 : var4.pool.peekFirst(), var4.nextDelay);
                           }
                        } else if (--var4.nextDelay <= 0) {
                           var4.nextDelay = 0;
                           clearStrayManorBosses(var5, var4);
                           if (var4.defeated.size() >= EasterBossEntity.MANOR_COUNT) {
                              finish(var5, var4);
                           } else {
                              spawnVariant(var5, var4, var4.nextVariant());
                           }
                        }
                     }

                     if (var4.nextDelay > 0 && var4.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                        ManorTransition.prepare(var5, var4.id, var4.anchor, var4.pool.peekFirst() == null ? -1 : var4.pool.peekFirst(), var4.nextDelay);
                     }
                  }

                  if (var4.phase % 20 == 1) {
                     syncRecallBoundary(var5, var4, true);
                  }

                  ManorArenaChunks.track(var5, var4.anchor);
                  recallStrayPlayers(var5, var4);
                  if (ACTIVE.contains(var4)) {
                     if (var4.phase % 100 == 0) {
                        persist();
                     }

                     for (ServerPlayer var14 : new ArrayList(var4.bar.getPlayers())) {
                        if (var14.hasDisconnected() || var14.connection == null) {
                           var4.bar.removePlayer(var14);
                        }
                     }

                     for (ServerPlayer var15 : var5.players()) {
                        if (!var15.hasDisconnected() && var15.connection != null) {
                           boolean var18 = var15.distanceToSqr(var4.anchor) <= 9216.0;
                           if (var18 && !var4.bar.getPlayers().contains(var15)) {
                              BossBarVisibility.addPlayer(var4.bar, var15);
                           } else if (!var18 && var4.bar.getPlayers().contains(var15)) {
                              var4.bar.removePlayer(var15);
                           }
                        }
                     }

                     for (UUID var16 : var4.onStage) {
                        Entity var19 = var5.getEntity(var16);
                        if (var19 instanceof EasterBossEntity) {
                           EasterBossEntity var20 = (EasterBossEntity)var19;
                           if (var20.isAlive()) {
                              if (var4.phase % 10 == 0) {
                                 var5.sendParticles(ParticleTypes.CRIT, var20.getX(), var20.getY() + 1.0, var20.getZ(), 2, 0.3, 0.5, 0.3, 0.02);
                              }

                              BossBarVisibility.visible(var20.getBossBar(), true);
                           }
                        }
                     }
                  }
               }
            }

            ManorArenaChunks.flush();
         }
      }
   }

   public static void pump() {
      pumpAll();
   }

   private static void syncRecallBoundary(ServerLevel var0, ManorFight.Fight var1, boolean var2) {
      EasterBossEntity var3 = var1.current;
      boolean var4 = var2
         && var1.nextDelay <= 0
         && var3 != null
         && var3.isAlive()
         && !var3.isRemoved()
         && var3.level() == var0
         && !var3.isXuanlanSkyCasting()
         && !var3.isQizaiUninterruptibleCasting();
      ManorRecallBoundaryPacket var5 = var4
         ? new ManorRecallBoundaryPacket(
            var1.id, var0.dimension().location(), var3.getId(), var3.getUUID(), 76.0F, Math.max(0.0F, Math.min(1.0F, (float)var1.noTargetTicks / 200.0F)), true
         )
         : ManorRecallBoundaryPacket.stop(var1.id, var0.dimension().location());

      for (UUID var7 : var1.participants) {
         ServerPlayer var8 = var0.getServer().getPlayerList().getPlayer(var7);
         if (var8 != null) {
            ManorRecallBoundaryPacket.send(var8, var5);
         }
      }
   }

   private static void recallStrayPlayers(ServerLevel var0, ManorFight.Fight var1) {
      EasterBossEntity var2 = var1.current;
      if (var2 == null || var2.isRemoved() || !var2.isAlive()) {
         var1.noTargetTicks = 0;
      } else if (!var2.isXuanlanSkyCasting() && !var2.isQizaiUninterruptibleCasting()) {
         MinecraftServer var3 = var0.getServer();
         if (var3 != null) {
            ArrayList var4 = new ArrayList();

            for (UUID var6 : var1.participants) {
               ServerPlayer var7 = var3.getPlayerList().getPlayer(var6);
               if (var7 != null && var7.isAlive() && !var7.isSpectator()) {
                  var4.add(var7);
               }
            }

            if (var4.isEmpty()) {
               var1.noTargetTicks = 0;
            } else {
               long var22;
               boolean var23;
               label178: {
                  var22 = var0.getGameTime();
                  if (var2.getTarget() instanceof ServerPlayer var8 && ManorTeleportRules.isParticipant(var1.participants, var8.getUUID())) {
                     var23 = true;
                     break label178;
                  }

                  var23 = false;
               }

               if (var23) {
                  var1.noTargetTicks = 0;
               } else {
                  var1.noTargetTicks++;
               }

               boolean var24 = var1.noTargetTicks >= 200;
               ServerPlayer var10 = (ServerPlayer)var4.get(0);

               for (ServerPlayer var12 : var4) {
                  if (var12.distanceToSqr(var2) < var10.distanceToSqr(var2)) {
                     var10 = var12;
                  }
               }

               boolean var25 = var10.level() == var0 && !var2.isPerformingSkill() && (!var0.noCollision(var2) || var23 && !var2.hasLineOfSight(var10));
               if (var25 && var1.stalledPosition != null && !(var2.distanceToSqr(var1.stalledPosition) > 1.0)) {
                  var1.stalledTicks++;
               } else {
                  var1.stalledTicks = 0;
                  var1.stalledPosition = var2.position();
               }

               if (var1.stalledTicks >= 200) {
                  if (teleportBossTo(var2, var10, var1)) {
                     LAST_RECALL.put(var10.getUUID(), var22);
                     var1.stalledTicks = 0;
                     var1.stalledPosition = var2.position();
                     FTBossMod.LOGGER.info("[FTBoss][Manor] {} 长时间被障碍阻挡，安全脱困", var2.getUUID());
                  }
               } else {
                  for (ServerPlayer var13 : var4) {
                     boolean var14 = var13.level().dimension().equals(var1.dim);
                     double var15 = var14 ? (double)var13.distanceTo(var2) : Double.POSITIVE_INFINITY;
                     boolean var17 = !var14 || var15 > 76.0;
                     boolean var18 = var14 && !var24 && var15 >= 68.0 && var15 <= 76.0;
                     if (var18 && var1.phase % 20 == 0) {
                        showRecallBoundaryWarning(var13, var15);
                     }

                     Recall var19 = ManorTeleportRules.recall(
                        var14, var13.distanceToSqr(var2), var13.distanceToSqr(var1.anchor), var2.distanceToSqr(var1.anchor), var24
                     );
                     if (var19 == Recall.NONE) {
                        if (var1.pendingPlayerRecalls.remove(var13.getUUID()) != null) {
                           var13.displayClientMessage(Component.literal("§a【庄园】§7已回到战场范围，传送倒计时取消。"), true);
                        }
                     } else {
                        Long var20 = LAST_RECALL.get(var13.getUUID());
                        if (var20 == null || var22 - var20 >= 100L) {
                           if (var19 == Recall.PLAYER_TO_BOSS) {
                              Integer var21 = var1.pendingPlayerRecalls.get(var13.getUUID());
                              if (var21 == null) {
                                 var21 = 100;
                                 var1.pendingPlayerRecalls.put(var13.getUUID(), var21);
                                 showRecallCountdown(var13, var21, var15, var17);
                                 continue;
                              }

                              if (var21 > 0) {
                                 var21 = var21 - 1;
                                 var1.pendingPlayerRecalls.put(var13.getUUID(), var21);
                                 if (var21 > 0 && var21 % 20 == 0) {
                                    showRecallCountdown(var13, var21, var15, var17);
                                 }
                                 continue;
                              }
                           } else {
                              var1.pendingPlayerRecalls.remove(var13.getUUID());
                           }

                           boolean var29 = var19 == Recall.PLAYER_TO_BOSS
                              ? teleportPlayerToBoss(var13, var2, var14 ? null : var0, var1)
                              : teleportBossTo(var2, var13, var1);
                           if (!var29) {
                              if (var19 == Recall.PLAYER_TO_BOSS) {
                                 var1.pendingPlayerRecalls.put(var13.getUUID(), 20);
                                 var13.displayClientMessage(Component.literal("§c【庄园】§7暂时找不到安全落点，1 秒后重试传送。"), true);
                              }
                           } else {
                              LAST_RECALL.put(var13.getUUID(), var22);
                              var1.pendingPlayerRecalls.remove(var13.getUUID());
                              pruneRecall(var22);
                              var1.noTargetTicks = 0;
                              if (var19 == Recall.BOSS_TO_PLAYER) {
                                 return;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         var1.noTargetTicks = 0;
         var1.stalledTicks = 0;
         var1.stalledPosition = null;
      }
   }

   private static void showRecallBoundaryWarning(ServerPlayer var0, double var1) {
      var0.displayClientMessage(
         Component.literal("§e【庄园】§7正在接近战场边界（距离 Boss §e" + formatRecallDistance(var1) + "§7 / §e" + formatRecallDistance(76.0) + "§7 格），请返回范围内。"), true
      );
   }

   private static void showRecallCountdown(ServerPlayer var0, int var1, double var2, boolean var4) {
      int var5 = Math.max(1, (int)Math.ceil((double)var1 / 20.0));
      String var6 = Double.isFinite(var2) ? "距离 Boss §e" + formatRecallDistance(var2) + "§7 格" : "已离开 Boss 所在维度";
      String var7 = var4 ? "§c超出战场范围！" : "§6Boss 暂未锁定你。";
      var0.displayClientMessage(Component.literal("§c§l【庄园】§r " + var7 + "§7" + var6 + "，将在 §c" + var5 + "§7 秒后传送回去。"), true);
   }

   private static String formatRecallDistance(double var0) {
      return Double.isFinite(var0) ? String.format(Locale.ROOT, "%.1f", var0) : "--";
   }

   private static boolean teleportPlayerToBoss(ServerPlayer var0, EasterBossEntity var1, ServerLevel var2, ManorFight.Fight var3) {
      if (!ManorTeleportRules.isParticipant(var3.participants, var0.getUUID())) {
         return false;
      } else if (var1.isQizaiUninterruptibleCasting()) {
         return false;
      } else {
         ServerLevel var4 = (ServerLevel)var1.level();
         Vec3 var5 = ManorSafeTeleport.find(var4, var0, var1.position().add(3.0, 0.0, 0.0), 8);
         if (var5 == null) {
            return false;
         } else {
            var1.prepareArenaTransfer();
            ManorSafeTeleport.move(var4, var0, var5);
            var0.displayClientMessage(Component.literal("§c§l【庄园】已将你传送回 Boss 身边。"), true);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 玩家安全召回 {} -> {}", var0.getName().getString(), var5);
            return true;
         }
      }
   }

   private static boolean teleportBossTo(EasterBossEntity var0, ServerPlayer var1, ManorFight.Fight var2) {
      if (!ManorTeleportRules.isParticipant(var2.participants, var1.getUUID())) {
         return false;
      } else if (var0.isQizaiUninterruptibleCasting()) {
         return false;
      } else if (!var0.canTeleportToFightMember(var1)) {
         return false;
      } else {
         ServerLevel var3 = (ServerLevel)var0.level();
         Vec3 var4 = ManorSafeTeleport.find(var3, var0, var1.position().add(3.0, 0.0, 0.0), 8);
         if (var4 == null) {
            return false;
         } else {
            var0.prepareArenaTransfer();
            ManorSafeTeleport.move(var3, var0, var4);
            FTBossMod.LOGGER.info("[FTBoss][Manor] Boss 安全召回 {} -> {}", var0.getUUID(), var4);
            return true;
         }
      }
   }

   private static int clearStrayManorBosses(ServerLevel var0, ManorFight.Fight var1) {
      int var2 = 0;

      for (Entity var4 : members(var0.getServer(), var1)) {
         if (var4 instanceof EasterBossEntity) {
            EasterBossEntity var5 = (EasterBossEntity)var4;
            if (!var5.isMini() && var5.getVariant() != 9 && !var1.onStage.contains(var5.getUUID())) {
               var4.discard();
               var2++;
            }
         }
      }

      return var2;
   }

   public static boolean isFlightBanned(Player var0) {
      if (var0 == null) {
         return false;
      } else {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.dim.equals(var0.level().dimension())
               && FullFightFlightRules.contains(var0.getX(), var0.getZ(), var2.anchor.x, var2.anchor.z, var2.participants.contains(var0.getUUID()))) {
               return true;
            }
         }

         return false;
      }
   }

   private static final class Fight {
      final ResourceKey<Level> dim;
      final ServerBossEvent bar;
      UUID summonerId;
      ManorDifficulty difficulty = ManorDifficulty.NORMAL;
      UUID id = UUID.randomUUID();
      final Set<UUID> participants = new HashSet<>();
      Vec3 lastPosition;
      Vec3 stalledPosition;
      int stalledTicks;
      long lastRelocation = Long.MIN_VALUE;
      int voidFalls;
      long lastVoidFall = Long.MIN_VALUE;
      Vec3 anchor;
      final Set<Integer> defeated = new LinkedHashSet<>();
      int currentVariant = -1;
      UUID currentId = null;
      EasterBossEntity current = null;
      int unloadedTicks = 0;
      int noTargetTicks = 0;
      final Map<UUID, Integer> pendingPlayerRecalls = new HashMap<>();
      int nextDelay = 0;
      int phase = 0;
      final Deque<Integer> pool = new ArrayDeque<>();
      final List<UUID> onStage = new ArrayList<>();

      Fight(ResourceKey<Level> var1, Vec3 var2, UUID var3) {
         this.dim = var1;
         this.summonerId = var3;
         if (var3 != null) {
            this.participants.add(var3);
         }

         this.anchor = var2;
         this.lastPosition = var2;
         this.bar = new ServerBossEvent(Component.literal(ManorFight.progressTitle(0)), BossBarColor.RED, BossBarOverlay.NOTCHED_10);
         this.bar.setProgress(0.0F);
         BossBarVisibility.visible(this.bar, true);
         this.refillPool(null);
      }

      void refillPool(Integer var1) {
         ArrayList var2 = new ArrayList();

         for (int var6 : EasterBossEntity.MANOR_VARIANTS) {
            if (!this.defeated.contains(var6) && (var1 == null || var6 != var1)) {
               var2.add(var6);
            }
         }

         Collections.shuffle(var2);
         this.pool.clear();
         this.pool.addAll(var2);
      }

      int nextVariant() {
         if (this.pool.isEmpty()) {
            this.refillPool(null);
         }

         Integer var1 = this.pool.pollFirst();
         return var1 == null ? 0 : var1;
      }
   }
}
