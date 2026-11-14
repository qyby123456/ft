package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro.Act;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.ManorIntroTimeline;
import net.farmingtales.ftboss.util.PresentationClock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class HiddenBossIntro {
   public static final double DISTANCE = 15.0;
   public static final int DEFAULT_VARIANT = 9;
   public static final int INTRO_MIN_TICKS = 920;
   private static final double VIEW_RADIUS_SQUARED = 9216.0;
   private static final int GROUND_SEARCH_UP = 4;
   private static final int GROUND_SEARCH_DOWN = 32;
   private static final List<Act> ACTIVE = new ArrayList<>();
   private static int lastPump = -1;

   private HiddenBossIntro() {
   }

   public static void start(ServerLevel var0, Vec3 var1, Vec3 var2, String... var3) {
      start(var0, var1, var2, 9, var3);
   }

   public static void start(ServerLevel var0, Vec3 var1, Vec3 var2, int var3, String... var4) {
      start(var0, var1, var2, var3, null, var4);
   }

   public static void start(ServerLevel var0, Vec3 var1, Vec3 var2, UUID var3, String... var4) {
      start(var0, var1, var2, 9, var3, var4);
   }

   public static void start(ServerLevel var0, Vec3 var1, Vec3 var2, int var3, UUID var4, String... var5) {
      start(var0, var1, var2, var3, var4, ManorDifficulty.NORMAL, var5);
   }

   public static void start(ServerLevel var0, Vec3 var1, Vec3 var2, int var3, UUID var4, ManorDifficulty var5, String... var6) {
      if (var0 != null && !isActive() && SummonGate.isAllowedDim(var0) && !ManorFight.hasActive(var0)) {
         Vec3 var7 = var2.subtract(var1);
         if (var7.lengthSqr() < 1.0E-4) {
            var7 = new Vec3(0.0, 0.0, 1.0);
         }

         var7 = var7.normalize();
         double var8 = var1.x + var7.x * 15.0;
         double var10 = var1.z + var7.z * 15.0;
         EasterBossEntity var12 = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).create(var0);
         if (var12 == null) {
            BossMusic.stopManorCue("入场实体创建失败");
         } else {
            var12.setVariant(var3);
            var12.setChallengeDifficulty(var5);
            var12.setManorSummonerId(var4);
            Vec3 var13 = findGround(var0, var12, new Vec3(var8, var2.y, var10));
            if (var13 == null) {
               var13 = findGround(var0, var12, var1);
            }

            if (var13 == null) {
               BossMusic.stopManorCue("附近没有可站立的演出地面");

               for (ServerPlayer var15 : var0.players()) {
                  if (var15.distanceToSqr(var1) <= 9216.0) {
                     var15.sendSystemMessage(Component.literal("§c【甜苹果庄园】附近没有可站立的演出地面，请在平坦地面重新触发。"));
                  }
               }
            } else {
               var8 = var13.x;
               var10 = var13.z;
               double var14 = var13.y;
               float var16 = (float)Math.toDegrees(Math.atan2(var1.z - var10, var1.x - var8)) - 90.0F;
               var12.moveTo(var8, var14, var10, var16, 0.0F);
               var12.setInvulnerable(true);
               var12.setNoAi(true);
               var12.setManorIntroHidden(var3 == 9);
               if (!var0.addFreshEntity(var12)) {
                  BossMusic.stopManorCue("入场实体生成被取消");
               } else {
                  ServerBossEvent var17 = new ServerBossEvent(
                     Component.literal("§c§l甜苹果庄园" + var12.getChallengeDifficulty().badge()), BossBarColor.RED, BossBarOverlay.PROGRESS
                  );
                  var17.setProgress(0.0F);
                  var12.getBossBar().setVisible(false);
                  Act var18 = new Act(var0, var12, var17, var6);
                  ACTIVE.add(var18);
                  BossMusic.beginManorPhase(var0, var1);
                  BossMusic.playManorCue(var0, var12.position());
                  syncViewers(var0, var18, 0L);
                  var0.playSound(null, var8, var14, var10, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0F, 0.7F);
                  FTBossMod.LOGGER.info("[FTBoss][ManorIntro] start id={} duration={}ms; client mesh, no particles/crystal entities", var18.id, 46000L);
               }
            }
         }
      }
   }

   private static Vec3 findGround(ServerLevel var0, EasterBossEntity var1, Vec3 var2) {
      BlockPos var3 = BlockPos.containing(var2);
      int var4 = Math.min(var0.getMaxBuildHeight() - 1, var3.getY() + 4);
      int var5 = Math.max(var0.getMinBuildHeight(), var3.getY() - 32);
      Vec3 var6 = null;
      double var7 = Double.POSITIVE_INFINITY;

      for (int var9 = var4; var9 >= var5; var9--) {
         BlockPos var10 = new BlockPos(var3.getX(), var9, var3.getZ());
         if (var0.hasChunkAt(var10)) {
            BlockState var11 = var0.getBlockState(var10);
            if (var11.getFluidState().isEmpty()) {
               for (AABB var13 : var11.getCollisionShape(var0, var10).toAabbs()) {
                  double var14 = var2.x - (double)var10.getX();
                  double var16 = var2.z - (double)var10.getZ();
                  if (!(var14 < var13.minX) && !(var14 > var13.maxX) && !(var16 < var13.minZ) && !(var16 > var13.maxZ)) {
                     double var18 = (double)var9 + var13.maxY;
                     double var20 = Math.abs(var18 - var2.y);
                     if (!(var20 >= var7)) {
                        AABB var22 = var1.getBoundingBox().move(var2.x - var1.getX(), var18 - var1.getY(), var2.z - var1.getZ());
                        if (var0.noCollision(var1, var22) && !var0.containsAnyLiquid(var22)) {
                           var6 = new Vec3(var2.x, var18, var2.z);
                           var7 = var20;
                        }
                     }
                  }
               }
            }
         }
      }

      return var6;
   }

   private static void send(ServerPlayer var0, Act var1, long var2, boolean var4) {
      PacketDistributor.sendToPlayer(var0, new ManorIntroPacket(var1.id, var1.dimension.location(), var1.position, var2, var4), new CustomPacketPayload[0]);
   }

   private static void syncViewers(ServerLevel var0, Act var1, long var2) {
      boolean var4 = var2 >= var1.nextSyncMillis;
      HashSet var5 = new HashSet();

      for (ServerPlayer var7 : var0.players()) {
         if (!(var7.distanceToSqr(var1.position) > 9216.0)) {
            var5.add(var7.getUUID());
            var1.bar.addPlayer(var7);
            if (var1.viewers.add(var7.getUUID()) || var4) {
               send(var7, var1, var2, true);
            }
         }
      }

      for (UUID var11 : new HashSet(var1.viewers)) {
         if (!var5.contains(var11)) {
            ServerPlayer var8 = var0.getServer().getPlayerList().getPlayer(var11);
            if (var8 != null) {
               var1.bar.removePlayer(var8);
               send(var8, var1, var2, false);
            }

            var1.viewers.remove(var11);
         }
      }

      for (ServerPlayer var12 : new ArrayList(var1.bar.getPlayers())) {
         if (!var5.contains(var12.getUUID())) {
            var1.bar.removePlayer(var12);
         }
      }

      if (var4) {
         var1.nextSyncMillis = var2 + 2000L;
      }
   }

   private static void clear(MinecraftServer var0, Act var1, long var2) {
      ServerLevel var4 = var0.getLevel(var1.dimension);
      if (var4 != null && var4.getEntity(var1.id) instanceof EasterBossEntity var5) {
         var5.setManorIntroHidden(false);
      }

      var1.bar.removeAllPlayers();

      for (UUID var9 : var1.viewers) {
         ServerPlayer var7 = var0.getPlayerList().getPlayer(var9);
         if (var7 != null) {
            send(var7, var1, var2, false);
         }
      }

      var1.viewers.clear();
   }

   public static void pumpAll() {
      MinecraftServer var0 = ServerLifecycleHooks.getCurrentServer();
      if (var0 != null && !ACTIVE.isEmpty() && lastPump != var0.getTickCount()) {
         lastPump = var0.getTickCount();

         for (Act var2 : new ArrayList<>(ACTIVE)) {
            long var3 = Math.max(0L, PresentationClock.millis() - var2.startMillis);
            ServerLevel var5 = var0.getLevel(var2.dimension);
            if (var5 != null && var5.getEntity(var2.id) instanceof EasterBossEntity var6 && var6.isAlive()) {
               var2.position = var6.position();
               var2.bar.setProgress(ManorIntroTimeline.progress(var3));
               if (var3 >= var2.nextSyncMillis) {
                  syncViewers(var5, var2, var3);
               }

               while (var2.sent < var2.lines.length && var3 >= ManorIntroTimeline.dialogueMillis(var2.sent, var2.lines.length)) {
                  String var10 = var2.lines[var2.sent++];
                  if (var10 != null && !var10.isEmpty()) {
                     for (ServerPlayer var9 : var5.players()) {
                        if (var2.viewers.contains(var9.getUUID())) {
                           var9.sendSystemMessage(Component.literal(var10));
                        }
                     }
                  }
               }

               if (var3 >= 46000L) {
                  clear(var0, var2, var3);
                  ACTIVE.remove(var2);
                  var6.setInvulnerable(false);
                  var6.setNoAi(false);
                  if (var6.getVariant() == 9) {
                     EasterBossEntity.supportSparrow(var5, var6);
                     int var11 = EasterBossEntity.MANOR_VARIANTS[var6.getRandom().nextInt(EasterBossEntity.MANOR_COUNT)];
                     ManorFight.start(var5, var6.position(), var11, var6.getManorSummonerId(), var6.getChallengeDifficulty());

                     for (ServerPlayer var13 : var5.players()) {
                        if (var13.distanceToSqr(var6) <= 9216.0) {
                           var13.sendSystemMessage(Component.literal("§e【麻雀】§7留在场地支援！§c【甜苹果庄园】§7第一位上场：§c" + EasterBossEntity.NAMES[var11]));
                        }
                     }
                  } else {
                     var6.getBossBar().setVisible(false);
                     ManorFight.adopt(var5, var6);
                  }

                  var5.playSound(null, var6.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.0F, 0.8F);
                  BossMusic.startManorBattle(var5, var6.position());
                  FTBossMod.LOGGER.info("[FTBoss][ManorIntro] complete id={} elapsed={}ms -> battle", var2.id, var3);
               }
            } else {
               clear(var0, var2, var3);
               ACTIVE.remove(var2);
               BossMusic.stopManorCue("入场实体已移除");
            }
         }
      }
   }

   public static void pump() {
      pumpAll();
   }

   public static boolean isActive() {
      return !ACTIVE.isEmpty();
   }

   public static void cancelAll() {
      for (Act var1 : new ArrayList<>(ACTIVE)) {
         cancel(var1.id);
      }
   }

   public static boolean stopForHardcore(ServerPlayer var0) {
      if (var0 == null) {
         return false;
      } else {
         ServerLevel var1 = var0.serverLevel();
         boolean var2 = false;

         for (Act var4 : new ArrayList<>(ACTIVE)) {
            if (var4.dimension.equals(var1.dimension())) {
               Entity var6 = var1.getEntity(var4.id);
               if (var6 instanceof EasterBossEntity) {
                  EasterBossEntity var5 = (EasterBossEntity)var6;
                  if (var0.getUUID().equals(var5.getManorSummonerId())) {
                     ManorFightData.get(var1.getServer()).retire(var4.id);
                     cancel(var4.id);
                     BossMusic.release(var1, var5);
                     var5.discard();
                     var2 = true;
                  }
               }
            }
         }

         return var2;
      }
   }

   static boolean stopForCreative(ServerLevel var0) {
      boolean var1 = false;

      for (Act var3 : new ArrayList<>(ACTIVE)) {
         if (var3.dimension.equals(var0.dimension())) {
            ManorFightData.get(var0.getServer()).retire(var3.id);
            cancel(var3.id);
            if (var0.getEntity(var3.id) instanceof EasterBossEntity var4) {
               BossMusic.release(var0, var4);
               var4.discard();
            }

            var1 = true;
         }
      }

      return var1;
   }

   public static void cancel(UUID var0) {
      MinecraftServer var1 = ServerLifecycleHooks.getCurrentServer();
      if (var1 != null) {
         for (Act var3 : new ArrayList<>(ACTIVE)) {
            if (var3.id.equals(var0)) {
               clear(var1, var3, Math.max(0L, PresentationClock.millis() - var3.startMillis));
               ACTIVE.remove(var3);
               BossMusic.stopManorCue("战场取消");
            }
         }
      }
   }

   public static boolean isActing(UUID var0) {
      for (Act var2 : ACTIVE) {
         if (var2.id.equals(var0)) {
            return true;
         }
      }

      return false;
   }
}
