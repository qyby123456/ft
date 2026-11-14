package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro.Act;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ManorIntroPacket;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.ManorIntroTimeline;
import net.farmingtales.ftboss.util.PresentationClock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

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

   public static void start(ServerLevel level, Vec3 anchor, Vec3 from, String... lines) {
      start(level, anchor, from, 9, lines);
   }

   public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, String... lines) {
      start(level, anchor, from, variant, null, lines);
   }

   public static void start(ServerLevel level, Vec3 anchor, Vec3 from, UUID summonerId, String... lines) {
      start(level, anchor, from, 9, summonerId, lines);
   }

   public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, UUID summonerId, String... lines) {
      start(level, anchor, from, variant, summonerId, ManorDifficulty.NORMAL, lines);
   }

   public static void start(ServerLevel level, Vec3 anchor, Vec3 from, int variant, UUID summonerId, ManorDifficulty difficulty, String... lines) {
      if (level != null && !isActive() && SummonGate.isAllowedDim(level) && !ManorFight.hasActive(level)) {
         Vec3 direction = from.m_82546_(anchor);
         if (direction.m_82556_() < 1.0E-4) {
            direction = new Vec3(0.0, 0.0, 1.0);
         }

         direction = direction.m_82541_();
         double posX = anchor.f_82479_ + direction.f_82479_ * 15.0;
         double posZ = anchor.f_82481_ + direction.f_82481_ * 15.0;
         EasterBossEntity boss = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_(level);
         if (boss == null) {
            BossMusic.stopManorCue("入场实体创建失败");
         } else {
            boss.setVariant(variant);
            boss.setChallengeDifficulty(difficulty);
            boss.setManorSummonerId(summonerId);
            Vec3 grounded = findGround(level, boss, new Vec3(posX, from.f_82480_, posZ));
            if (grounded == null) {
               grounded = findGround(level, boss, anchor);
            }

            if (grounded == null) {
               BossMusic.stopManorCue("附近没有可站立的演出地面");

               for (ServerPlayer player : level.m_6907_()) {
                  if (player.m_20238_(anchor) <= 9216.0) {
                     player.m_213846_(Component.m_237113_("§c【甜苹果庄园】附近没有可站立的演出地面，请在平坦地面重新触发。"));
                  }
               }
            } else {
               posX = grounded.f_82479_;
               posZ = grounded.f_82481_;
               double posY = grounded.f_82480_;
               float yaw = (float)Math.toDegrees(Math.atan2(anchor.f_82481_ - posZ, anchor.f_82479_ - posX)) - 90.0F;
               boss.m_7678_(posX, posY, posZ, yaw, 0.0F);
               boss.m_20331_(true);
               boss.m_21557_(true);
               boss.setManorIntroHidden(variant == 9);
               boss.prepareManorEntryBuffs();
               if (!level.m_7967_(boss)) {
                  BossMusic.stopManorCue("入场实体生成被取消");
               } else {
                  ServerBossEvent bar = new ServerBossEvent(
                     Component.m_237113_("§c§l甜苹果庄园" + boss.getChallengeDifficulty().badge()), BossBarColor.RED, BossBarOverlay.PROGRESS
                  );
                  bar.m_142711_(0.0F);
                  BossBarVisibility.visible(boss.getBossBar(), false);
                  Act act = new Act(level, boss, bar, lines);
                  ACTIVE.add(act);
                  BossMusic.beginManorPhase(level, anchor);
                  BossMusic.playManorCue(level, boss.m_20182_());
                  syncViewers(level, act, 0L);
                  level.m_6263_(null, posX, posY, posZ, SoundEvents.f_11736_, SoundSource.HOSTILE, 1.0F, 0.7F);
                  FTBossMod.LOGGER.info("[FTBoss][ManorIntro] start id={} duration={}ms; client mesh, no particles/crystal entities", act.id, 46000L);
               }
            }
         }
      }
   }

   private static Vec3 findGround(ServerLevel level, EasterBossEntity boss, Vec3 preferred) {
      BlockPos column = BlockPos.m_274446_(preferred);
      int highest = Math.min(level.m_151558_() - 1, column.m_123342_() + 4);
      int lowest = Math.max(level.m_141937_(), column.m_123342_() - 32);
      Vec3 closest = null;
      double closestDistance = Double.POSITIVE_INFINITY;

      for (int blockY = highest; blockY >= lowest; blockY--) {
         BlockPos floor = new BlockPos(column.m_123341_(), blockY, column.m_123343_());
         if (level.m_46805_(floor)) {
            BlockState state = level.m_8055_(floor);
            if (state.m_60819_().m_76178_()) {
               for (AABB surface : state.m_60812_(level, floor).m_83299_()) {
                  double localX = preferred.f_82479_ - (double)floor.m_123341_();
                  double localZ = preferred.f_82481_ - (double)floor.m_123343_();
                  if (!(localX < surface.f_82288_) && !(localX > surface.f_82291_) && !(localZ < surface.f_82290_) && !(localZ > surface.f_82293_)) {
                     double feetY = (double)blockY + surface.f_82292_;
                     double distance = Math.abs(feetY - preferred.f_82480_);
                     if (!(distance >= closestDistance)) {
                        AABB body = boss.m_20191_()
                           .m_82386_(preferred.f_82479_ - boss.m_20185_(), feetY - boss.m_20186_(), preferred.f_82481_ - boss.m_20189_());
                        if (level.m_45756_(boss, body) && !level.m_46855_(body)) {
                           closest = new Vec3(preferred.f_82479_, feetY, preferred.f_82481_);
                           closestDistance = distance;
                        }
                     }
                  }
               }
            }
         }
      }

      return closest;
   }

   private static void send(ServerPlayer player, Act act, long elapsedMillis, boolean active) {
      FTNetwork.CHANNEL
         .send(PacketDistributor.PLAYER.with(() -> player), new ManorIntroPacket(act.id, act.dimension.m_135782_(), act.position, elapsedMillis, active));
   }

   private static void syncViewers(ServerLevel level, Act act, long elapsedMillis) {
      boolean sync = elapsedMillis >= act.nextSyncMillis;
      Set<UUID> nearby = new HashSet<>();

      for (ServerPlayer player : level.m_6907_()) {
         if (!(player.m_20238_(act.position) > 9216.0)) {
            nearby.add(player.m_20148_());
            BossBarVisibility.addPlayer(act.bar, player);
            if (act.viewers.add(player.m_20148_()) || sync) {
               send(player, act, elapsedMillis, true);
            }
         }
      }

      for (UUID viewer : new HashSet(act.viewers)) {
         if (!nearby.contains(viewer)) {
            ServerPlayer playerx = level.m_7654_().m_6846_().m_11259_(viewer);
            if (playerx != null) {
               act.bar.m_6539_(playerx);
               send(playerx, act, elapsedMillis, false);
            }

            act.viewers.remove(viewer);
         }
      }

      for (ServerPlayer playerx : new ArrayList(act.bar.m_8324_())) {
         if (!nearby.contains(playerx.m_20148_())) {
            act.bar.m_6539_(playerx);
         }
      }

      if (sync) {
         act.nextSyncMillis = elapsedMillis + 2000L;
      }
   }

   private static void clear(MinecraftServer server, Act act, long elapsedMillis) {
      ServerLevel level = server.m_129880_(act.dimension);
      if (level != null && level.m_8791_(act.id) instanceof EasterBossEntity boss) {
         boss.setManorIntroHidden(false);
      }

      act.bar.m_7706_();

      for (UUID viewer : act.viewers) {
         ServerPlayer player = server.m_6846_().m_11259_(viewer);
         if (player != null) {
            send(player, act, elapsedMillis, false);
         }
      }

      act.viewers.clear();
   }

   public static void pumpAll() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null && !ACTIVE.isEmpty() && lastPump != server.m_129921_()) {
         lastPump = server.m_129921_();

         for (Act act : new ArrayList<>(ACTIVE)) {
            long elapsedMillis = Math.max(0L, PresentationClock.millis() - act.startMillis);
            ServerLevel level = server.m_129880_(act.dimension);
            if (level != null && level.m_8791_(act.id) instanceof EasterBossEntity boss && boss.m_6084_()) {
               act.position = boss.m_20182_();
               act.bar.m_142711_(ManorIntroTimeline.progress(elapsedMillis));
               if (elapsedMillis >= act.nextSyncMillis) {
                  syncViewers(level, act, elapsedMillis);
               }

               while (act.sent < act.lines.length && elapsedMillis >= ManorIntroTimeline.dialogueMillis(act.sent, act.lines.length)) {
                  String text = act.lines[act.sent++];
                  if (text != null && !text.isEmpty()) {
                     for (ServerPlayer player : level.m_6907_()) {
                        if (act.viewers.contains(player.m_20148_())) {
                           player.m_213846_(Component.m_237113_(text));
                        }
                     }
                  }
               }

               if (elapsedMillis >= 46000L) {
                  clear(server, act, elapsedMillis);
                  ACTIVE.remove(act);
                  boss.prepareManorEntryBuffs();
                  boss.m_20331_(false);
                  boss.m_21557_(false);
                  if (boss.getVariant() == 9) {
                     EasterBossEntity.supportSparrow(level, boss);
                     int first = EasterBossEntity.MANOR_VARIANTS[boss.m_217043_().m_188503_(EasterBossEntity.MANOR_COUNT)];
                     ManorFight.start(level, boss.m_20182_(), first, boss.getManorSummonerId(), boss.getChallengeDifficulty());

                     for (ServerPlayer playerx : level.m_6907_()) {
                        if (playerx.m_20280_(boss) <= 9216.0) {
                           playerx.m_213846_(Component.m_237113_("§e【麻雀】§7留在场地支援！§c【甜苹果庄园】§7第一位上场：§c" + EasterBossEntity.NAMES[first]));
                        }
                     }
                  } else {
                     BossBarVisibility.visible(boss.getBossBar(), false);
                     ManorFight.adopt(level, boss);
                  }

                  level.m_5594_(null, boss.m_20183_(), SoundEvents.f_11739_, SoundSource.HOSTILE, 1.0F, 0.8F);
                  BossMusic.startManorBattle(level, boss.m_20182_());
                  FTBossMod.LOGGER.info("[FTBoss][ManorIntro] complete id={} elapsed={}ms -> battle", act.id, elapsedMillis);
               }
            } else {
               clear(server, act, elapsedMillis);
               ACTIVE.remove(act);
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
      for (Act act : new ArrayList<>(ACTIVE)) {
         cancel(act.id);
      }
   }

   public static boolean stopForHardcore(ServerPlayer player) {
      if (player == null) {
         return false;
      } else {
         ServerLevel level = player.m_284548_();
         boolean stopped = false;

         for (Act act : new ArrayList<>(ACTIVE)) {
            if (act.dimension.equals(level.m_46472_())) {
               Entity var6 = level.m_8791_(act.id);
               if (var6 instanceof EasterBossEntity) {
                  EasterBossEntity boss = (EasterBossEntity)var6;
                  if (player.m_20148_().equals(boss.getManorSummonerId())) {
                     ManorFightData.get(level.m_7654_()).retire(act.id);
                     cancel(act.id);
                     BossMusic.release(level, boss);
                     boss.m_146870_();
                     stopped = true;
                  }
               }
            }
         }

         return stopped;
      }
   }

   static boolean stopForCreative(ServerLevel level) {
      boolean stopped = false;

      for (Act act : new ArrayList<>(ACTIVE)) {
         if (act.dimension.equals(level.m_46472_())) {
            ManorFightData.get(level.m_7654_()).retire(act.id);
            cancel(act.id);
            if (level.m_8791_(act.id) instanceof EasterBossEntity boss) {
               BossMusic.release(level, boss);
               boss.m_146870_();
            }

            stopped = true;
         }
      }

      return stopped;
   }

   public static void cancel(UUID entityId) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         for (Act act : new ArrayList<>(ACTIVE)) {
            if (act.id.equals(entityId)) {
               clear(server, act, Math.max(0L, PresentationClock.millis() - act.startMillis));
               ACTIVE.remove(act);
               BossMusic.stopManorCue("战场取消");
            }
         }
      }
   }

   public static boolean isActing(UUID id) {
      for (Act act : ACTIVE) {
         if (act.id.equals(id)) {
            return true;
         }
      }

      return false;
   }
}
