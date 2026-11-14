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
import net.minecraftforge.server.ServerLifecycleHooks;

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

   private static void pruneRecall(long now) {
      if (LAST_RECALL.size() > 64) {
         LAST_RECALL.entrySet().removeIf(en -> now - en.getValue() > 6000L);
         if (LAST_RECALL.size() > 64) {
            LAST_RECALL.clear();
         }
      }
   }

   private static String progressTitle(int done) {
      return "§c§l甜苹果庄园 §7已击败 §c" + done + "§7/§c" + EasterBossEntity.MANOR_COUNT;
   }

   private static void updateBar(ManorFight.Fight f) {
      int done = f.defeated.size();
      f.bar.m_6456_(Component.m_237113_(progressTitle(done) + f.difficulty.badge()));
      f.bar.m_142711_(Math.min(1.0F, (float)done / (float)EasterBossEntity.MANOR_COUNT));
   }

   private static void initialize(MinecraftServer server) {
      if (activeServer != server) {
         ACTIVE.forEach(fightx -> {
            fightx.pendingPlayerRecalls.clear();
            fightx.bar.m_7706_();
         });
         ACTIVE.clear();
         ManorArenaChunks.reset();
         LAST_RECALL.clear();
         ManorMercy.reset();
         lastPump = -1L;
         lastShredPump = -1L;
         activeServer = server;
         ManorMercy.restore(ManorFightData.get(server).deaths());

         for (Tag entry : ManorFightData.get(server).active()) {
            CompoundTag record = (CompoundTag)entry;

            try {
               ResourceKey<Level> dim = ResourceKey.m_135785_(Registries.f_256858_, new ResourceLocation(record.m_128461_("Dimension")));
               if (record.m_128403_("Id") && !ManorFightData.get(server).retired(record.m_128342_("Id"))) {
                  ManorFight.Fight fight = new ManorFight.Fight(
                     dim,
                     new Vec3(record.m_128459_("X"), record.m_128459_("Y"), record.m_128459_("Z")),
                     record.m_128403_("Summoner") ? record.m_128342_("Summoner") : null
                  );
                  fight.id = record.m_128342_("Id");
                  fight.difficulty = ManorDifficulty.fromId(record.m_128461_("Difficulty"));
                  fight.currentId = record.m_128403_("Current") ? record.m_128342_("Current") : null;
                  if (fight.currentId != null) {
                     fight.onStage.add(fight.currentId);
                  }

                  fight.currentVariant = record.m_128451_("Variant");
                  fight.lastPosition = new Vec3(record.m_128459_("BossX"), record.m_128459_("BossY"), record.m_128459_("BossZ"));

                  for (int variant : record.m_128465_("Defeated")) {
                     if (isManorVariant(variant)) {
                        fight.defeated.add(variant);
                     }
                  }

                  for (Tag player : record.m_128437_("Participants", 10)) {
                     CompoundTag value = (CompoundTag)player;
                     if (value.m_128403_("Id")) {
                        fight.participants.add(value.m_128342_("Id"));
                     }
                  }

                  fight.refillPool(fight.currentVariant);
                  fight.nextDelay = fight.defeated.contains(fight.currentVariant) ? 60 : 0;
                  ACTIVE.add(fight);
                  updateBar(fight);
                  FTBossMod.LOGGER.info("[FTBoss][Manor] 恢复战场 {}，已击败 {}/{}", new Object[]{fight.id, fight.defeated.size(), EasterBossEntity.MANOR_COUNT});
               }
            } catch (RuntimeException var10) {
               FTBossMod.LOGGER.warn("[FTBoss][Manor] 忽略损坏的战场记录", var10);
            }
         }

         for (ManorFight.Fight fight : ACTIVE) {
            ServerLevel restored = server.m_129880_(fight.dim);
            if (restored != null) {
               ManorArenaChunks.hold(restored, fight.anchor);
            }
         }
      }
   }

   private static void persist() {
      if (activeServer != null) {
         ListTag records = new ListTag();

         for (ManorFight.Fight fight : ACTIVE) {
            CompoundTag record = new CompoundTag();
            record.m_128362_("Id", fight.id);
            record.m_128359_("Difficulty", fight.difficulty.id());
            record.m_128359_("Dimension", fight.dim.m_135782_().toString());
            if (fight.summonerId != null) {
               record.m_128362_("Summoner", fight.summonerId);
            }

            if (fight.currentId != null) {
               record.m_128362_("Current", fight.currentId);
            }

            record.m_128405_("Variant", fight.currentVariant);
            record.m_128347_("X", fight.anchor.f_82479_);
            record.m_128347_("Y", fight.anchor.f_82480_);
            record.m_128347_("Z", fight.anchor.f_82481_);
            record.m_128347_("BossX", fight.lastPosition.f_82479_);
            record.m_128347_("BossY", fight.lastPosition.f_82480_);
            record.m_128347_("BossZ", fight.lastPosition.f_82481_);
            record.m_128385_("Defeated", fight.defeated.stream().mapToInt(Integer::intValue).toArray());
            ListTag players = new ListTag();

            for (UUID player : fight.participants) {
               CompoundTag value = new CompoundTag();
               value.m_128362_("Id", player);
               players.add(value);
            }

            record.m_128365_("Participants", players);
            records.add(record);
         }

         ManorFightData.get(activeServer).store(records);
      }
   }

   private static ManorFight.Fight find(UUID id) {
      if (id == null) {
         return null;
      } else {
         for (ManorFight.Fight fight : ACTIVE) {
            if (fight.id.equals(id)) {
               return fight;
            }
         }

         return null;
      }
   }

   public static ManorDifficulty difficulty(UUID id) {
      ManorFight.Fight fight = find(id);
      return fight == null ? ManorDifficulty.NORMAL : fight.difficulty;
   }

   public static boolean isImpossibleFor(Player player) {
      if (player == null) {
         return false;
      } else {
         for (ManorFight.Fight fight : ACTIVE) {
            if (fight.difficulty == ManorDifficulty.IMPOSSIBLE
               && fight.dim.equals(player.m_9236_().m_46472_())
               && (
                  fight.participants.contains(player.m_20148_())
                     || player.m_20275_(fight.anchor.f_82479_, fight.anchor.f_82480_, fight.anchor.f_82481_) <= 9216.0
               )) {
               return true;
            }
         }

         return false;
      }
   }

   public static UUID fightId(Entity member) {
      return ManorFightMembers.fightId(ManorFightMembers.resolve(member));
   }

   public static UUID currentBoss(UUID id) {
      ManorFight.Fight fight = find(id);
      return fight == null ? null : fight.currentId;
   }

   public static UUID summoner(UUID id) {
      ManorFight.Fight fight = find(id);
      return fight == null ? null : fight.summonerId;
   }

   public static boolean hasActive(ServerLevel level) {
      initialize(level.m_7654_());
      return ACTIVE.stream().anyMatch(fight -> fight.dim.equals(level.m_46472_()));
   }

   public static boolean hasActive(UUID id) {
      return find(id) != null;
   }

   public static int currentVariant(UUID id) {
      ManorFight.Fight fight = find(id);
      return fight == null ? -1 : fight.currentVariant;
   }

   public static boolean canSurrender(ServerPlayer player, UUID id) {
      ManorFight.Fight fight = find(id);
      return fight != null && ManorCombatRules.canSurrender(fight.summonerId, player.m_20148_(), fight.id, id);
   }

   public static boolean surrender(ServerPlayer player, UUID id) {
      if (!canSurrender(player, id)) {
         return false;
      } else {
         ManorFight.Fight fight = find(id);
         ServerLevel level = player.f_8924_.m_129880_(fight.dim);
         if (level == null) {
            return false;
         } else {
            endFight(level, fight, false, "召唤者投降");
            return true;
         }
      }
   }

   public static boolean abortForHardcore(ServerPlayer player, UUID attributedFight) {
      if (player == null) {
         return false;
      } else {
         initialize(player.f_8924_);
         ManorFight.Fight fight = find(attributedFight);
         if (fight != null && !fight.dim.equals(player.m_9236_().m_46472_())) {
            fight = null;
         }

         if (fight == null) {
            for (ManorFight.Fight candidate : ACTIVE) {
               if (candidate.dim.equals(player.m_9236_().m_46472_()) && candidate.participants.contains(player.m_20148_())) {
                  fight = candidate;
                  break;
               }
            }
         }

         if (fight == null) {
            return HiddenBossIntro.stopForHardcore(player);
         } else {
            ServerLevel level = player.f_8924_.m_129880_(fight.dim);
            if (level == null) {
               return false;
            } else {
               endFight(level, fight, false, "极限模式玩家死亡前中止");
               return true;
            }
         }
      }
   }

   public static boolean stopForCreative(ServerPlayer player) {
      if (player != null && player.m_7500_()) {
         ServerLevel level = player.m_284548_();
         initialize(level.m_7654_());
         boolean stopped = HiddenBossChallenge.cancelForCreative(level);

         for (ManorFight.Fight fight : new ArrayList<>(ACTIVE)) {
            if (fight.dim.equals(level.m_46472_())) {
               endFight(level, fight, false, "创造道具中止");
               stopped = true;
            }
         }

         return HiddenBossIntro.stopForCreative(level) || stopped;
      } else {
         return false;
      }
   }

   public static void participate(Entity member, ServerPlayer player) {
      ManorFight.Fight fight = find(fightId(member));
      if (fight != null && fight.participants.add(player.m_20148_())) {
         persist();
      }
   }

   public static void attackedBy(EasterBossEntity member, ServerPlayer player) {
      ManorFight.Fight fight = find(fightId(member));
      if (fight != null) {
         if (fight.summonerId == null) {
            fight.summonerId = player.m_20148_();

            for (Entity entity : members(player.f_8924_, fight)) {
               if (entity instanceof EasterBossEntity boss) {
                  boss.setManorSummonerId(fight.summonerId);
               }
            }

            persist();
         }

         if (fight.participants.add(player.m_20148_())) {
            persist();
         }
      }
   }

   private static List<Entity> members(MinecraftServer server, ManorFight.Fight fight) {
      List<Entity> result = new ArrayList<>();

      for (ServerLevel level : server.m_129785_()) {
         for (Entity entity : level.m_142646_().m_142273_()) {
            if (ManorFightMembers.fightId(entity) == null) {
               Entity owner = ManorFightMembers.resolve(entity);
               if (owner != entity) {
                  ManorFightMembers.inherit(owner, entity);
               }
            }

            if (fight.id.equals(ManorFightMembers.fightId(entity))) {
               result.add(entity);
            }
         }
      }

      return result;
   }

   private static void bind(ManorFight.Fight fight, EasterBossEntity boss) {
      ManorFightMembers.bind(boss, fight.id);
      boss.setChallengeDifficulty(fight.difficulty);
      SparrowSupport.noteFightMember(boss);
      boss.setManorSummonerId(fight.summonerId);
      if (!HiddenBossIntro.isActing(boss.m_20148_())) {
         boss.m_21557_(false);
         boss.m_20331_(false);
      }
   }

   private static void endFight(ServerLevel level, ManorFight.Fight fight, boolean victory, String reason) {
      if (!ManorFightData.get(level.m_7654_()).retired(fight.id)) {
         syncRecallBoundary(level, fight, false);
         ManorTransition.cancel(level, fight.id);
         if (victory) {
            ManorVictory.begin(level, fight.id, fight.anchor, fight.participants);
            if (fight.difficulty == ManorDifficulty.NORMAL) {
               for (UUID player : fight.participants) {
                  ManorVictoryData.get(level.m_7654_()).recordConqueror(player);
               }

               BossAdvancements.onManorNormalVictory(level, fight.participants);
            } else {
               BossAdvancements.onManorImpossibleVictory(level, fight.participants);
            }
         }

         ManorFightData.get(level.m_7654_()).retire(fight.id);
         ImpossibleSparrowSupport.finish(level.m_7654_(), fight.id);
         ACTIVE.remove(fight);
         if (ACTIVE.stream().noneMatch(other -> other.dim.equals(level.m_46472_()))) {
            ManorArenaChunks.release(level);
         }

         fight.pendingPlayerRecalls.clear();
         BossBarVisibility.visible(fight.bar, false);
         fight.bar.m_7706_();
         ManorMercy.clearFight(fight.id);

         for (Entity entity : members(level.m_7654_(), fight)) {
            if (entity instanceof LivingEntity living && entity.m_9236_() instanceof ServerLevel memberLevel) {
               BossMusic.release(memberLevel, living);
            }

            HiddenBossIntro.cancel(entity.m_20148_());
            entity.m_146870_();
         }

         for (UUID participant : fight.participants) {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(participant);
            LAST_RECALL.remove(participant);
            if (player != null) {
               ArmorShred.clear(player);
               if (victory) {
                  player.m_213846_(
                     Component.m_237113_(
                        fight.difficulty == ManorDifficulty.IMPOSSIBLE
                           ? "§4【甜苹果庄园·" + fight.difficulty.title() + "】§f挑战完成。§7本难度不发放庄园成就或通关头衔。"
                           : "§6【甜苹果庄园】§f普通难度通关！§7" + ManorDifficulty.IMPOSSIBLE.label() + "已解锁，可在下次挑战时选择。"
                     )
                  );
               } else {
                  player.m_213846_(Component.m_237113_("§6【甜苹果庄园·" + fight.difficulty.title() + "】§7挑战已结束（" + reason + "），不计击败、无掉落。"));
               }
            }
         }

         SparrowSupport.forgetSparrow();
         BossBars.sweep(level.m_7654_());
         persist();
         FTBossMod.LOGGER.info("[FTBoss][Manor] 战场 {} 结束：{}，胜利={}", new Object[]{fight.id, reason, victory});
      }
   }

   public static boolean isManaged(LivingEntity le) {
      for (ManorFight.Fight f : ACTIVE) {
         if (f.onStage.contains(le.m_20148_())) {
            return true;
         }
      }

      return false;
   }

   @Deprecated
   public static boolean isFinaleMember(LivingEntity le) {
      return false;
   }

   public static boolean isPlayerFighting(Player p) {
      if (p == null) {
         return false;
      } else {
         for (ManorFight.Fight f : ACTIVE) {
            if (f.dim.equals(p.m_9236_().m_46472_()) && p.m_20275_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_) <= 9216.0) {
               return true;
            }
         }

         return false;
      }
   }

   public static void adopt(ServerLevel sl, EasterBossEntity boss) {
      if (sl != null && boss != null) {
         initialize(sl.m_7654_());
         if (!HiddenBossIntro.isActing(boss.m_20148_()) && SummonGate.isAllowedDim(sl)) {
            UUID previous = ManorFightMembers.fightId(boss);
            if (ManorFightData.get(sl.m_7654_()).retired(previous)) {
               boss.m_146870_();
            } else {
               clear(sl.m_46472_());
               ManorFight.Fight f = new ManorFight.Fight(sl.m_46472_(), boss.m_20182_(), boss.getManorSummonerId());
               f.difficulty = boss.getChallengeDifficulty();
               if (previous != null) {
                  f.id = previous;
               }

               bind(f, boss);
               f.currentVariant = boss.getVariant();
               f.currentId = boss.m_20148_();
               f.current = boss;
               f.onStage.add(boss.m_20148_());
               f.refillPool(boss.getVariant());
               ACTIVE.add(f);
               ManorArenaChunks.hold(sl, f.anchor);

               for (Entity entity : sl.m_142646_().m_142273_()) {
                  if (entity instanceof EasterBossEntity) {
                     EasterBossEntity sparrow = (EasterBossEntity)entity;
                     if (sparrow.getVariant() == 9 && f.summonerId != null && f.summonerId.equals(sparrow.getManorSummonerId())) {
                        bind(f, sparrow);
                     }
                  }
               }

               purgeStale(sl, boss.m_20148_(), true);
               SparrowSupport.beginStage(sl, boss);

               for (ServerPlayer p : sl.m_6907_()) {
                  if (p.m_20280_(boss) <= 9216.0) {
                     BossBarVisibility.addPlayer(f.bar, p);
                  }
               }

               updateBar(f);
               persist();
               FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={} 血量={}", EasterBossEntity.NAMES[boss.getVariant()], (int)boss.m_21233_());
            }
         }
      }
   }

   public static void start(ServerLevel sl, Vec3 pos, int variant) {
      start(sl, pos, variant, null);
   }

   public static void start(ServerLevel sl, Vec3 pos, int variant, UUID summonerId) {
      start(sl, pos, variant, summonerId, ManorDifficulty.NORMAL);
   }

   public static void start(ServerLevel sl, Vec3 pos, int variant, UUID summonerId, ManorDifficulty difficulty) {
      if (sl != null) {
         initialize(sl.m_7654_());
         if (SummonGate.isAllowedDim(sl)) {
            clear(sl.m_46472_());
            ManorFight.Fight f = new ManorFight.Fight(sl.m_46472_(), pos, summonerId);
            f.difficulty = difficulty == null ? ManorDifficulty.NORMAL : difficulty;
            f.refillPool(variant);
            ACTIVE.add(f);
            ManorArenaChunks.hold(sl, f.anchor);

            for (Entity entity : sl.m_142646_().m_142273_()) {
               if (entity instanceof EasterBossEntity) {
                  EasterBossEntity sparrow = (EasterBossEntity)entity;
                  if (sparrow.getVariant() == 9 && summonerId != null && summonerId.equals(sparrow.getManorSummonerId())) {
                     bind(f, sparrow);
                  }
               }
            }

            purgeStale(sl, null, true);
            spawnVariant(sl, f, variant);
            updateBar(f);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={}", EasterBossEntity.NAMES[variant]);
         }
      }
   }

   public static void clear(ResourceKey<Level> dim) {
      if (activeServer != null) {
         for (ManorFight.Fight fight : ACTIVE) {
            if (fight.dim.equals(dim)) {
               ServerLevel level = activeServer.m_129880_(dim);
               if (level != null) {
                  endFight(level, fight, false, "战场重置");
               }
            }
         }
      }
   }

   public static boolean rescue(EasterBossEntity boss) {
      if (!(boss.m_9236_() instanceof ServerLevel level)) {
         return false;
      } else {
         ManorFight.Fight var8 = find(fightId(boss));
         long now = level.m_46467_();
         if (var8 != null) {
            if (var8.lastVoidFall == Long.MIN_VALUE || now - var8.lastVoidFall > 600L) {
               var8.voidFalls = 0;
            }

            var8.lastVoidFall = now;
            var8.voidFalls++;
            if (var8.voidFalls >= 2 && ManorCombatRules.relocationReady(now, var8.lastRelocation, 200) && relocate(level, var8, boss)) {
               return true;
            }
         }

         ServerPlayer summoner = boss.getManorSummonerId() == null ? null : level.m_7654_().m_6846_().m_11259_(boss.getManorSummonerId());
         Vec3 preferred = boss.canTeleportWith(summoner) ? summoner.m_20182_() : (var8 == null ? boss.m_20182_() : var8.anchor);
         Vec3 safe = ManorSafeTeleport.find(level, boss, preferred, 16);
         if (safe == null && var8 != null) {
            safe = ManorSafeTeleport.find(level, boss, var8.anchor, 24);
         }

         if (safe != null) {
            boss.prepareArenaTransfer();
            ManorSafeTeleport.move(level, boss, safe);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 坠空救援 {} {} -> {}", new Object[]{boss.m_7755_().getString(), boss.m_20148_(), safe});
            return true;
         } else {
            if (var8 != null) {
               endFight(level, var8, false, "附近没有安全落点");
            } else {
               boss.m_146870_();
               FTBossMod.LOGGER.warn("[FTBoss][Manor] 无主 Boss {} 无安全落点，取消而非计为击败", boss.m_20148_());
            }

            return false;
         }
      }
   }

   private static boolean relocate(ServerLevel level, ManorFight.Fight fight, EasterBossEntity trigger) {
      fight.lastRelocation = level.m_46467_();
      List<Entity> entities = members(level.m_7654_(), fight);
      ServerPlayer summoner = fight.summonerId == null ? null : level.m_7654_().m_6846_().m_11259_(fight.summonerId);
      if (summoner != null && summoner.m_6084_() && !summoner.m_5833_() && summoner.m_9236_() == level) {
         entities.add(summoner);
      }

      for (int attempt = 0; attempt < 24; attempt++) {
         double angle = level.f_46441_.m_188500_() * Math.PI * 2.0;
         double distance = 52.0 + level.f_46441_.m_188500_() * 20.0;
         Vec3 candidate = fight.anchor.m_82520_(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
         Vec3 anchor = ManorSafeTeleport.find(level, trigger, candidate, 3);
         if (anchor != null && !(anchor.m_82546_(fight.anchor).m_165925_() < 2304.0)) {
            Map<Entity, Vec3> destinations = new LinkedHashMap<>();
            boolean safe = true;
            int ordinal = 0;

            for (Entity entity : entities) {
               if (entity instanceof LivingEntity && entity.m_6084_() && entity.m_9236_() == level) {
                  double offset = (double)(ordinal++) * 2.4;
                  Vec3 preferred = entity == trigger ? anchor : anchor.m_82520_(Math.cos(offset) * 4.0, 0.0, Math.sin(offset) * 4.0);
                  Vec3 destination = ManorSafeTeleport.find(level, entity, preferred, 5);
                  if (destination == null) {
                     safe = false;
                     break;
                  }

                  destinations.put(entity, destination);
               }
            }

            if (safe) {
               fight.anchor = anchor;
               fight.voidFalls = 0;
               ManorArenaChunks.hold(level, fight.anchor);

               for (Entry<Entity, Vec3> destination : destinations.entrySet()) {
                  if (destination.getKey() instanceof EasterBossEntity boss) {
                     boss.prepareArenaTransfer();
                  }

                  ManorSafeTeleport.move(level, destination.getKey(), destination.getValue());
               }

               for (Entity entityx : entities) {
                  if (!(entityx instanceof LivingEntity)) {
                     entityx.m_146870_();
                  }
               }

               if (fight.current != null) {
                  fight.lastPosition = fight.current.m_20182_();
               }

               persist();
               if (summoner != null) {
                  summoner.m_5661_(Component.m_237113_("§6【庄园】§7战场已安全搬迁，继续挑战。"), true);
               }

               FTBossMod.LOGGER.info("[FTBoss][Manor] 整场安全搬迁 {} -> {}，成员 {}", new Object[]{fight.id, anchor, destinations.size()});
               return true;
            }
         }
      }

      FTBossMod.LOGGER.warn("[FTBoss][Manor] 战场 {} 未找到安全搬迁点，保留原场地", fight.id);
      return false;
   }

   public static Vec3 anchorOf(ServerLevel sl) {
      for (ManorFight.Fight f : ACTIVE) {
         if (f.dim.equals(sl.m_46472_())) {
            return f.anchor;
         }
      }

      return null;
   }

   public static Vec3 anchorOf(UUID id) {
      ManorFight.Fight fight = find(id);
      return fight == null ? null : fight.anchor;
   }

   public static void reattach(ServerPlayer p) {
      if (p != null) {
         for (ManorFight.Fight f : ACTIVE) {
            if (f.dim.equals(p.m_9236_().m_46472_()) && !(p.m_20275_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_) > 9216.0)) {
               f.bar.m_6539_(p);
               BossBarVisibility.addPlayer(f.bar, p);
               BossBarVisibility.visible(f.bar, true);
               updateBar(f);
               FTBossMod.LOGGER
                  .info(
                     "[FTBoss][Manor] 玩家 {} 重进存档 -> 已把庄园进度血条重挂上去(进度 {}/{})",
                     new Object[]{p.m_7755_().getString(), f.defeated.size(), EasterBossEntity.MANOR_COUNT}
                  );
            }
         }
      }
   }

   private static boolean isManorVariant(int v) {
      for (int i : EasterBossEntity.MANOR_VARIANTS) {
         if (i == v) {
            return true;
         }
      }

      return false;
   }

   public static void recoverNear(MinecraftServer server) {
      if (server != null) {
         initialize(server);

         for (ServerLevel level : server.m_129785_()) {
            if (SummonGate.isAllowedDim(level)) {
               boolean active = ACTIVE.stream().anyMatch(fight -> fight.dim.equals(level.m_46472_()));
               if (!active) {
                  boolean adopted = false;

                  for (ServerPlayer player : level.m_6907_()) {
                     for (EasterBossEntity boss : level.m_45976_(EasterBossEntity.class, player.m_20191_().m_82400_(160.0))) {
                        if (boss.m_6084_()
                           && !boss.m_213877_()
                           && !boss.isMini()
                           && isManorVariant(boss.getVariant())
                           && !HiddenBossIntro.isActing(boss.m_20148_())) {
                           UUID previous = ManorFightMembers.fightId(boss);
                           if (!ManorFightData.get(server).retired(previous)) {
                              adopt(level, boss);
                              FTBossMod.LOGGER.info("[FTBoss][Manor] 收编无托管 Boss {}；召唤者={}", boss.m_20148_(), boss.getManorSummonerId());
                              adopted = true;
                              break;
                           }

                           boss.m_146870_();
                        }
                     }

                     if (adopted) {
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   public static int purgeStale(ServerLevel sl, UUID keep, boolean keepSparrow) {
      if (sl == null) {
         return 0;
      } else {
         List<Entity> stale = new ArrayList<>();

         for (Entity entity : sl.m_142646_().m_142273_()) {
            if (ManorFightData.get(sl.m_7654_()).retired(ManorFightMembers.fightId(entity))) {
               stale.add(entity);
            }
         }

         stale.forEach(Entity::m_146870_);
         return stale.size();
      }
   }

   @Deprecated
   public static boolean onDamage(ServerLevel level, EasterBossEntity boss, float amount, DamageSource source) {
      return false;
   }

   private static void spawnVariant(ServerLevel sl, ManorFight.Fight f, int variant) {
      EasterBossEntity e = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_(sl);
      if (e == null) {
         endFight(sl, f, false, "无法生成下一位 Boss");
      } else {
         double yaw = (double)((float)Math.toDegrees(Math.atan2(f.anchor.f_82481_ - f.anchor.f_82481_, 1.0)));
         e.m_7678_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_, (float)yaw, 0.0F);
         e.setVariant(variant);
         e.setManorSummonerId(f.summonerId);
         bind(f, e);
         e.prepareManorEntryBuffs();
         e.m_21557_(false);
         if (!sl.m_7967_(e)) {
            endFight(sl, f, false, "下一位 Boss 生成被取消");
         } else {
            f.onStage.add(e.m_20148_());
            f.currentVariant = variant;
            f.currentId = e.m_20148_();
            f.current = e;
            f.nextDelay = 0;
            f.unloadedTicks = 0;
            f.lastPosition = e.m_20182_();
            syncRecallBoundary(sl, f, true);
            SparrowSupport.beginStage(sl, e);
            persist();
            ManorTransition.arrive(sl, f.id, e.m_20182_(), variant);
            sl.m_6263_(null, f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.4F, 0.7F);
            FTBossMod.LOGGER
               .info(
                  "[FTBoss][Manor] 上场: {} (独立血量 {}, 已击败 {}/{})",
                  new Object[]{EasterBossEntity.NAMES[variant], (int)e.m_21233_(), f.defeated.size(), EasterBossEntity.MANOR_COUNT}
               );

            for (ServerPlayer p : sl.m_6907_()) {
               if (p.m_20275_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_) <= 9216.0) {
                  p.m_213846_(
                     Component.m_237113_(
                        "§c【甜苹果庄园】§7第 §c"
                           + (f.defeated.size() + 1)
                           + "§7 位上场：§c"
                           + EasterBossEntity.NAMES[variant]
                           + " §8(已击败 "
                           + f.defeated.size()
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

   private static void onCurrentDefeated(ServerLevel sl, ManorFight.Fight f) {
      syncRecallBoundary(sl, f, false);
      SparrowSupport.endStage(sl, f.id);
      if (f.currentVariant >= 0 && f.defeated.add(f.currentVariant)) {
         ManorMercy.clearFight(f.id);
         persist();
         updateBar(f);
         FTBossMod.LOGGER
            .info("[FTBoss][Manor] 已击败 {} ({}/{})", new Object[]{EasterBossEntity.NAMES[f.currentVariant], f.defeated.size(), EasterBossEntity.MANOR_COUNT});

         for (ServerPlayer p : sl.m_6907_()) {
            if (p.m_20238_(f.anchor) <= 9216.0) {
               p.m_213846_(
                  Component.m_237113_(
                     "§6【甜苹果庄园】§7" + EasterBossEntity.NAMES[f.currentVariant] + " §7已被击败！§8(进度 " + f.defeated.size() + "/" + EasterBossEntity.MANOR_COUNT + ")"
                  )
               );
            }
         }
      }

      f.onStage.clear();
      f.pendingPlayerRecalls.clear();
      f.currentId = null;
      f.current = null;
   }

   private static void finish(ServerLevel sl, ManorFight.Fight f) {
      endFight(sl, f, true, "全部主战 Boss 已击败");
   }

   public static void pumpAll() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server != null) {
         initialize(server);
         long now = server.m_129783_().m_46467_();
         if (now != lastShredPump) {
            lastShredPump = now;
            ArmorShred.tick(server);
         }

         if (!ACTIVE.isEmpty()) {
            if (now != lastPump) {
               lastPump = now;
               ManorArenaChunks.beginFrame();

               for (ManorFight.Fight f : ACTIVE) {
                  ServerLevel sl = server.m_129880_(f.dim);
                  if (sl != null) {
                     f.phase++;
                     if (f.currentVariant >= 0) {
                        EasterBossEntity cur = f.current;
                        if (f.currentId != null && sl.m_8791_(f.currentId) == null && !f.defeated.contains(f.currentVariant) && f.unloadedTicks % 100 == 20) {
                           sl.m_46865_(BlockPos.m_274446_(f.lastPosition));
                        }

                        if (f.currentId != null && sl.m_8791_(f.currentId) instanceof EasterBossEntity le && le != cur) {
                           cur = le;
                           f.current = le;
                           bind(f, le);
                        }

                        boolean dead = f.defeated.contains(f.currentVariant) || cur != null && (cur.m_21224_() || cur.m_21223_() <= 0.0F);
                        boolean inWorld = cur != null && sl.m_8791_(cur.m_20148_()) == cur;
                        if (inWorld && !dead) {
                           f.lastPosition = cur.m_20182_();
                           if (f.phase % 20 == 0 && !SparrowSupport.hasVisited(cur)) {
                              SparrowSupport.beginStage(sl, cur);
                           }
                        }

                        if (!dead && !inWorld) {
                           f.unloadedTicks++;
                           if (f.unloadedTicks == 601) {
                              FTBossMod.LOGGER
                                 .warn("[FTBoss][Manor] {} 连续 {} tick 不在世界里(区块一直没加载) -> 取消本场，不计击败", EasterBossEntity.NAMES[f.currentVariant], f.unloadedTicks);
                           }
                        } else {
                           f.unloadedTicks = 0;
                        }

                        if (!dead && f.unloadedTicks > 600) {
                           endFight(sl, f, false, "Boss 卸载后无法恢复");
                           continue;
                        }

                        boolean gone = ManorCombatRules.defeated(dead, f.unloadedTicks);
                        if (gone) {
                           if (f.nextDelay <= 0) {
                              onCurrentDefeated(sl, f);
                              f.nextDelay = 60;
                              if (f.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                                 ManorTransition.prepare(sl, f.id, f.anchor, f.pool.peekFirst() == null ? -1 : f.pool.peekFirst(), f.nextDelay);
                              }
                           } else if (--f.nextDelay <= 0) {
                              f.nextDelay = 0;
                              clearStrayManorBosses(sl, f);
                              if (f.defeated.size() >= EasterBossEntity.MANOR_COUNT) {
                                 finish(sl, f);
                              } else {
                                 spawnVariant(sl, f, f.nextVariant());
                              }
                           }
                        }

                        if (f.nextDelay > 0 && f.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                           ManorTransition.prepare(sl, f.id, f.anchor, f.pool.peekFirst() == null ? -1 : f.pool.peekFirst(), f.nextDelay);
                        }
                     }

                     if (f.phase % 20 == 1) {
                        syncRecallBoundary(sl, f, true);
                     }

                     ManorArenaChunks.track(sl, f.anchor);
                     recallStrayPlayers(sl, f);
                     if (ACTIVE.contains(f)) {
                        if (f.phase % 100 == 0) {
                           persist();
                        }

                        for (ServerPlayer stale : new ArrayList(f.bar.m_8324_())) {
                           if (stale.m_9232_() || stale.f_8906_ == null) {
                              f.bar.m_6539_(stale);
                           }
                        }

                        for (ServerPlayer p : sl.m_6907_()) {
                           if (!p.m_9232_() && p.f_8906_ != null) {
                              boolean near = p.m_20238_(f.anchor) <= 9216.0;
                              if (near && !f.bar.m_8324_().contains(p)) {
                                 BossBarVisibility.addPlayer(f.bar, p);
                              } else if (!near && f.bar.m_8324_().contains(p)) {
                                 f.bar.m_6539_(p);
                              }
                           }
                        }

                        for (UUID id : f.onStage) {
                           Entity ent = sl.m_8791_(id);
                           if (ent instanceof EasterBossEntity) {
                              EasterBossEntity eb = (EasterBossEntity)ent;
                              if (eb.m_6084_()) {
                                 if (f.phase % 10 == 0) {
                                    sl.m_8767_(ParticleTypes.f_123797_, eb.m_20185_(), eb.m_20186_() + 1.0, eb.m_20189_(), 2, 0.3, 0.5, 0.3, 0.02);
                                 }

                                 BossBarVisibility.visible(eb.getBossBar(), true);
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
   }

   public static void pump() {
      pumpAll();
   }

   private static void syncRecallBoundary(ServerLevel level, ManorFight.Fight fight, boolean requestedActive) {
      EasterBossEntity boss = fight.current;
      boolean active = requestedActive
         && fight.nextDelay <= 0
         && boss != null
         && boss.m_6084_()
         && !boss.m_213877_()
         && boss.m_9236_() == level
         && !boss.isXuanlanSkyCasting()
         && !boss.isQizaiUninterruptibleCasting();
      ManorRecallBoundaryPacket packet = active
         ? new ManorRecallBoundaryPacket(
            fight.id,
            level.m_46472_().m_135782_(),
            boss.m_19879_(),
            boss.m_20148_(),
            76.0F,
            Math.max(0.0F, Math.min(1.0F, (float)fight.noTargetTicks / 200.0F)),
            true
         )
         : ManorRecallBoundaryPacket.stop(fight.id, level.m_46472_().m_135782_());

      for (UUID participant : fight.participants) {
         ServerPlayer player = level.m_7654_().m_6846_().m_11259_(participant);
         if (player != null) {
            ManorRecallBoundaryPacket.send(player, packet);
         }
      }
   }

   private static void recallStrayPlayers(ServerLevel sl, ManorFight.Fight f) {
      EasterBossEntity boss = f.current;
      if (boss != null && !boss.m_213877_() && boss.m_6084_()) {
         if (!boss.isXuanlanSkyCasting() && !boss.isQizaiUninterruptibleCasting()) {
            MinecraftServer server = sl.m_7654_();
            if (server != null) {
               ArrayList<ServerPlayer> members = new ArrayList<>();

               for (UUID id : f.participants) {
                  ServerPlayer member = server.m_6846_().m_11259_(id);
                  if (member != null && member.m_6084_() && !member.m_5833_()) {
                     members.add(member);
                  }
               }

               if (members.isEmpty()) {
                  f.noTargetTicks = 0;
               } else {
                  long now;
                  boolean var10000;
                  label182: {
                     now = sl.m_46467_();
                     if (boss.m_5448_() instanceof ServerPlayer locked && ManorTeleportRules.isParticipant(f.participants, locked.m_20148_())) {
                        var10000 = true;
                        break label182;
                     }

                     var10000 = false;
                  }

                  boolean lockedOnMember = var10000;
                  if (lockedOnMember) {
                     f.noTargetTicks = 0;
                  } else {
                     f.noTargetTicks++;
                  }

                  boolean noTarget = f.noTargetTicks >= 200;
                  ServerPlayer reference = members.get(0);

                  for (ServerPlayer member : members) {
                     if (member.m_20280_(boss) < reference.m_20280_(boss)) {
                        reference = member;
                     }
                  }

                  boolean obstructed = reference.m_9236_() == sl
                     && !boss.isPerformingSkill()
                     && (!sl.m_45786_(boss) || lockedOnMember && !boss.m_142582_(reference));
                  if (obstructed && f.stalledPosition != null && !(boss.m_20238_(f.stalledPosition) > 1.0)) {
                     f.stalledTicks++;
                  } else {
                     f.stalledTicks = 0;
                     f.stalledPosition = boss.m_20182_();
                  }

                  if (f.stalledTicks >= 200) {
                     if (teleportBossTo(boss, reference, f)) {
                        LAST_RECALL.put(reference.m_20148_(), now);
                        f.stalledTicks = 0;
                        f.stalledPosition = boss.m_20182_();
                        FTBossMod.LOGGER.info("[FTBoss][Manor] {} 长时间被障碍阻挡，安全脱困", boss.m_20148_());
                     }
                  } else {
                     for (ServerPlayer memberx : members) {
                        boolean sameDimension = memberx.m_9236_().m_46472_().equals(f.dim);
                        double distance = sameDimension ? (double)memberx.m_20270_(boss) : Double.POSITIVE_INFINITY;
                        boolean outsideBoundary = !sameDimension || distance > 76.0;
                        boolean nearBoundary = sameDimension && !noTarget && distance >= 68.0 && distance <= 76.0;
                        if (nearBoundary && f.phase % 20 == 0) {
                           showRecallBoundaryWarning(memberx, distance);
                        }

                        Recall recall = ManorTeleportRules.recall(
                           sameDimension, memberx.m_20280_(boss), memberx.m_20238_(f.anchor), boss.m_20238_(f.anchor), noTarget
                        );
                        if (recall == Recall.NONE) {
                           if (f.pendingPlayerRecalls.remove(memberx.m_20148_()) != null) {
                              memberx.m_5661_(Component.m_237113_("§a【庄园】§7已回到战场范围，传送倒计时取消。"), true);
                           }
                        } else {
                           Long last = LAST_RECALL.get(memberx.m_20148_());
                           if (last == null || now - last >= 100L) {
                              if (recall == Recall.PLAYER_TO_BOSS) {
                                 Integer pending = f.pendingPlayerRecalls.get(memberx.m_20148_());
                                 if (pending == null) {
                                    pending = 100;
                                    f.pendingPlayerRecalls.put(memberx.m_20148_(), pending);
                                    showRecallCountdown(memberx, pending, distance, outsideBoundary);
                                    continue;
                                 }

                                 if (pending > 0) {
                                    pending = pending - 1;
                                    f.pendingPlayerRecalls.put(memberx.m_20148_(), pending);
                                    if (pending > 0 && pending % 20 == 0) {
                                       showRecallCountdown(memberx, pending, distance, outsideBoundary);
                                    }
                                    continue;
                                 }
                              } else {
                                 f.pendingPlayerRecalls.remove(memberx.m_20148_());
                              }

                              boolean moved = recall == Recall.PLAYER_TO_BOSS
                                 ? teleportPlayerToBoss(memberx, boss, sameDimension ? null : sl, f)
                                 : teleportBossTo(boss, memberx, f);
                              if (!moved) {
                                 if (recall == Recall.PLAYER_TO_BOSS) {
                                    f.pendingPlayerRecalls.put(memberx.m_20148_(), 20);
                                    memberx.m_5661_(Component.m_237113_("§c【庄园】§7暂时找不到安全落点，1 秒后重试传送。"), true);
                                 }
                              } else {
                                 LAST_RECALL.put(memberx.m_20148_(), now);
                                 f.pendingPlayerRecalls.remove(memberx.m_20148_());
                                 pruneRecall(now);
                                 f.noTargetTicks = 0;
                                 if (recall == Recall.BOSS_TO_PLAYER) {
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
            f.noTargetTicks = 0;
            f.stalledTicks = 0;
            f.stalledPosition = null;
         }
      } else {
         f.noTargetTicks = 0;
      }
   }

   private static void showRecallBoundaryWarning(ServerPlayer player, double distance) {
      player.m_5661_(
         Component.m_237113_("§e【庄园】§7正在接近战场边界（距离 Boss §e" + formatRecallDistance(distance) + "§7 / §e" + formatRecallDistance(76.0) + "§7 格），请返回范围内。"), true
      );
   }

   private static void showRecallCountdown(ServerPlayer player, int ticks, double distance, boolean outsideBoundary) {
      int seconds = Math.max(1, (int)Math.ceil((double)ticks / 20.0));
      String location = Double.isFinite(distance) ? "距离 Boss §e" + formatRecallDistance(distance) + "§7 格" : "已离开 Boss 所在维度";
      String reason = outsideBoundary ? "§c超出战场范围！" : "§6Boss 暂未锁定你。";
      player.m_5661_(Component.m_237113_("§c§l【庄园】§r " + reason + "§7" + location + "，将在 §c" + seconds + "§7 秒后传送回去。"), true);
   }

   private static String formatRecallDistance(double distance) {
      return Double.isFinite(distance) ? String.format(Locale.ROOT, "%.1f", distance) : "--";
   }

   private static boolean teleportPlayerToBoss(ServerPlayer p, EasterBossEntity boss, ServerLevel crossDimTarget, ManorFight.Fight f) {
      if (!ManorTeleportRules.isParticipant(f.participants, p.m_20148_())) {
         return false;
      } else if (boss.isQizaiUninterruptibleCasting()) {
         return false;
      } else {
         ServerLevel level = (ServerLevel)boss.m_9236_();
         Vec3 safe = ManorSafeTeleport.find(level, p, boss.m_20182_().m_82520_(3.0, 0.0, 0.0), 8);
         if (safe == null) {
            return false;
         } else {
            boss.prepareArenaTransfer();
            ManorSafeTeleport.move(level, p, safe);
            p.m_5661_(Component.m_237113_("§c§l【庄园】已将你传送回 Boss 身边。"), true);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 玩家安全召回 {} -> {}", p.m_7755_().getString(), safe);
            return true;
         }
      }
   }

   private static boolean teleportBossTo(EasterBossEntity boss, ServerPlayer p, ManorFight.Fight f) {
      if (!ManorTeleportRules.isParticipant(f.participants, p.m_20148_())) {
         return false;
      } else if (boss.isQizaiUninterruptibleCasting()) {
         return false;
      } else if (!boss.canTeleportToFightMember(p)) {
         return false;
      } else {
         ServerLevel level = (ServerLevel)boss.m_9236_();
         Vec3 safe = ManorSafeTeleport.find(level, boss, p.m_20182_().m_82520_(3.0, 0.0, 0.0), 8);
         if (safe == null) {
            return false;
         } else {
            boss.prepareArenaTransfer();
            ManorSafeTeleport.move(level, boss, safe);
            FTBossMod.LOGGER.info("[FTBoss][Manor] Boss 安全召回 {} -> {}", boss.m_20148_(), safe);
            return true;
         }
      }
   }

   private static int clearStrayManorBosses(ServerLevel sl, ManorFight.Fight f) {
      int removed = 0;

      for (Entity entity : members(sl.m_7654_(), f)) {
         if (entity instanceof EasterBossEntity) {
            EasterBossEntity boss = (EasterBossEntity)entity;
            if (!boss.isMini() && boss.getVariant() != 9 && !f.onStage.contains(boss.m_20148_())) {
               entity.m_146870_();
               removed++;
            }
         }
      }

      return removed;
   }

   public static boolean isFlightBanned(Player var0) {
      if (var0 == null) {
         return false;
      } else {
         for (ManorFight.Fight var2 : ACTIVE) {
            if (var2.dim.equals(var0.m_9236_().m_46472_())
               && FullFightFlightRules.contains(
                  var0.m_20185_(), var0.m_20189_(), var2.anchor.f_82479_, var2.anchor.f_82481_, var2.participants.contains(var0.m_20148_())
               )) {
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

      Fight(ResourceKey<Level> dim, Vec3 anchor, UUID summonerId) {
         this.dim = dim;
         this.summonerId = summonerId;
         if (summonerId != null) {
            this.participants.add(summonerId);
         }

         this.anchor = anchor;
         this.lastPosition = anchor;
         this.bar = new ServerBossEvent(Component.m_237113_(ManorFight.progressTitle(0)), BossBarColor.RED, BossBarOverlay.NOTCHED_10);
         this.bar.m_142711_(0.0F);
         BossBarVisibility.visible(this.bar, true);
         this.refillPool(null);
      }

      void refillPool(Integer keepOut) {
         List<Integer> all = new ArrayList<>();

         for (int i : EasterBossEntity.MANOR_VARIANTS) {
            if (!this.defeated.contains(i) && (keepOut == null || i != keepOut)) {
               all.add(i);
            }
         }

         Collections.shuffle(all);
         this.pool.clear();
         this.pool.addAll(all);
      }

      int nextVariant() {
         if (this.pool.isEmpty()) {
            this.refillPool(null);
         }

         Integer v = this.pool.pollFirst();
         return v == null ? 0 : v;
      }
   }
}
