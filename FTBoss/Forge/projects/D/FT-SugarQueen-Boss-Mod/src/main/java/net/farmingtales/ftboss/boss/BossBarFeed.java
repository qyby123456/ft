package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.farmingtales.ftboss.network.BossBarNetwork;
import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class BossBarFeed {
   public static final double BAR_RANGE = 48.0;
   public static final double MINION_RADIUS = 32.0;
   public static final int SEND_INTERVAL = 2;
   private static final Set<UUID> SENT = new HashSet<>();
   private static final byte[] NO_VARIANTS = new byte[0];
   private static boolean failureLogged;

   private BossBarFeed() {
   }

   public static void reset() {
      SENT.clear();
      failureLogged = false;
   }

   public static void tick(MinecraftServer server) {
      if (server != null && server.getTickCount() % 2 == 0) {
         try {
            pump(server);
         } catch (Throwable var2) {
            if (!failureLogged) {
               failureLogged = true;
               FTBossMod.LOGGER.warn("[FTBoss][血条] 主题血条同步失败，已保留原版血条: {}", var2.toString());
            }
         }
      }
   }

   private static void pump(MinecraftServer server) {
      ArrayList<BossBarFeed.Snapshot> snapshots = new ArrayList<>(4);
      BossBars.forEach(server, boss -> {
         BossBarFeed.Snapshot snapshotx = snapshot(server, boss);
         if (snapshotx != null) {
            snapshots.add(snapshotx);
         }
      });
      HashSet<UUID> online = new HashSet<>();

      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
         online.add(player.getUUID());
         ArrayList<BossBarFeedPacket.Entry> entries = new ArrayList<>(snapshots.size());

         for (BossBarFeed.Snapshot snapshot : snapshots) {
            if (snapshot.boss.level() == player.level() && !(snapshot.boss.distanceToSqr(player) > 2304.0)) {
               entries.add(
                  new BossBarFeedPacket.Entry(
                     snapshot.entityId,
                     snapshot.themeId,
                     snapshot.ratio,
                     snapshot.hp,
                     snapshot.maxHp,
                     snapshot.phase,
                     snapshot.minions,
                     snapshot.finale,
                     snapshot.cast,
                     snapshot.manorVariant,
                     snapshot.manorCount,
                     snapshot.defeatedMask,
                     snapshot.variants
                  )
               );
            }
         }

         boolean hadBars = SENT.contains(player.getUUID());
         if (entries.isEmpty()) {
            if (!hadBars) {
               continue;
            }

            SENT.remove(player.getUUID());
         } else {
            SENT.add(player.getUUID());
         }

         BossBarNetwork.send(player, new BossBarFeedPacket(List.copyOf(entries)));
      }

      SENT.retainAll(online);
   }

   private static BossBarFeed.Snapshot snapshot(MinecraftServer server, LivingEntity boss) {
      if (boss != null && !boss.level().isClientSide && boss.isAlive() && !boss.isRemoved()) {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType());
         if (key != null && "ftboss".equals(key.getNamespace())) {
            BossBarTheme theme = BossBarTheme.byEntityPath(key.getPath());
            if (theme == null) {
               return null;
            } else {
               float max = boss.getMaxHealth();
               float ratio = max <= 0.0F ? 0.0F : BossBarTheme.clamp01(boss.getHealth() / max);
               int phase = theme.phaseIndex(ratio);
               boolean finale = NonManorFinaleDifficulty.isFinale(boss);
               int manorVariant = -1;
               int manorCount = 0;
               int defeatedMask = 0;
               byte[] variants = NO_VARIANTS;
               if (boss instanceof EasterBossEntity manor) {
                  finale |= manor.isImpossibleChallenge();
                  manorVariant = manor.getVariant();
                  manorCount = EasterBossEntity.MANOR_COUNT;
                  defeatedMask = manorDefeatedMask(server, boss);
                  variants = new byte[EasterBossEntity.MANOR_VARIANTS.length];

                  for (int i = 0; i < variants.length; i++) {
                     variants[i] = (byte)EasterBossEntity.MANOR_VARIANTS[i];
                  }
               }

               return new BossBarFeed.Snapshot(
                  boss,
                  boss.getId(),
                  theme.id(),
                  ratio,
                  displayHp(boss.getHealth()),
                  displayHp(max),
                  phase,
                  countMinions(boss),
                  finale,
                  castProgress(boss),
                  manorVariant,
                  manorCount,
                  defeatedMask,
                  variants
               );
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static int displayHp(float hp) {
      return !Float.isNaN(hp) && !(hp <= 0.0F) ? Math.min(Integer.MAX_VALUE, Math.round(hp)) : 0;
   }

   private static int countMinions(LivingEntity boss) {
      try {
         int count = 0;

         for (Mob mob : boss.level().getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(32.0))) {
            if (mob != boss && !mob.isRemoved() && mob.isAlive()) {
               ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
               if (key != null && "ftboss".equals(key.getNamespace()) && !BossBarTheme.hasTheme(key.getPath())) {
                  count++;
               }
            }
         }

         return count;
      } catch (Throwable var5) {
         return 0;
      }
   }

   private static int manorDefeatedMask(MinecraftServer server, LivingEntity boss) {
      try {
         UUID fightId = ManorFight.fightId(boss);
         if (fightId == null) {
            return 0;
         }

         for (Tag tag : ManorFightData.get(server).active()) {
            if (tag instanceof CompoundTag entry && entry.hasUUID("Id") && fightId.equals(entry.getUUID("Id"))) {
               int mask = 0;

               for (int variant : entry.getIntArray("Defeated")) {
                  if (variant >= 0 && variant < 32) {
                     mask |= 1 << variant;
                  }
               }

               return mask;
            }
         }
      } catch (Throwable var11) {
      }

      return 0;
   }

   private static float castProgress(LivingEntity boss) {
      try {
         long now = boss.level().getGameTime();

         for (DataValue<?> value : boss.getEntityData().getNonDefaultValues()) {
            Object raw = value.value();
            if (raw instanceof CompoundTag) {
               CompoundTag tag = (CompoundTag)raw;
               if (tag.contains("duration") && tag.contains("start")) {
                  int duration = tag.getInt("duration");
                  long start = tag.getLong("start");
                  long age = now - start;
                  if (duration > 0 && start > 0L && age >= 0L && age < (long)duration && (tag.getBoolean("cast") || tag.getString("name").contains("cast"))) {
                     return BossBarTheme.clamp01((float)age / (float)duration);
                  }
               }
            }
         }
      } catch (Throwable var12) {
      }

      return 0.0F;
   }

   private static record Snapshot(
      LivingEntity boss,
      int entityId,
      String themeId,
      float ratio,
      int hp,
      int maxHp,
      int phase,
      int minions,
      boolean finale,
      float cast,
      int manorVariant,
      int manorCount,
      int defeatedMask,
      byte[] variants
   ) {
   }
}
