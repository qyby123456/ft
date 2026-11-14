/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.syncher.SynchedEntityData$DataValue
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.farmingtales.ftboss.network.BossBarNetwork;
import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class BossBarFeed {
    public static final double BAR_RANGE = 48.0;
    public static final double MINION_RADIUS = 32.0;
    public static final int SEND_INTERVAL = 2;
    private static final Set<UUID> SENT = new HashSet<UUID>();
    private static final byte[] NO_VARIANTS = new byte[0];
    private static boolean failureLogged;

    private BossBarFeed() {
    }

    public static void reset() {
        SENT.clear();
        failureLogged = false;
    }

    public static void tick(MinecraftServer server) {
        block3: {
            if (server != null && server.m_129921_() % 2 == 0) {
                try {
                    BossBarFeed.pump(server);
                }
                catch (Throwable var2) {
                    if (failureLogged) break block3;
                    failureLogged = true;
                    FTBossMod.LOGGER.warn("[FTBoss][\u8840\u6761] \u4e3b\u9898\u8840\u6761\u540c\u6b65\u5931\u8d25\uff0c\u5df2\u4fdd\u7559\u539f\u7248\u8840\u6761: {}", (Object)var2.toString());
                }
            }
        }
    }

    private static void pump(MinecraftServer server) {
        ArrayList snapshots = new ArrayList(4);
        BossBars.forEach(server, boss -> {
            Snapshot snapshotx = BossBarFeed.snapshot(server, boss);
            if (snapshotx != null) {
                snapshots.add(snapshotx);
            }
        });
        HashSet<UUID> online = new HashSet<UUID>();
        for (ServerPlayer player : server.m_6846_().m_11314_()) {
            online.add(player.m_20148_());
            ArrayList<BossBarFeedPacket.Entry> entries = new ArrayList<BossBarFeedPacket.Entry>(snapshots.size());
            for (Snapshot snapshot : snapshots) {
                if (snapshot.boss.m_9236_() != player.m_9236_() || snapshot.boss.m_20280_((Entity)player) > 2304.0) continue;
                entries.add(new BossBarFeedPacket.Entry(snapshot.entityId, snapshot.themeId, snapshot.ratio, snapshot.hp, snapshot.maxHp, snapshot.phase, snapshot.minions, snapshot.finale, snapshot.cast, snapshot.manorVariant, snapshot.manorCount, snapshot.defeatedMask, snapshot.variants));
            }
            boolean hadBars = SENT.contains(player.m_20148_());
            if (entries.isEmpty()) {
                if (!hadBars) continue;
                SENT.remove(player.m_20148_());
            } else {
                SENT.add(player.m_20148_());
            }
            BossBarNetwork.send(player, new BossBarFeedPacket(List.copyOf(entries)));
        }
        SENT.retainAll(online);
    }

    /*
     * WARNING - void declaration
     */
    private static Snapshot snapshot(MinecraftServer minecraftServer, LivingEntity livingEntity) {
        void boss;
        if (BossBarVisibility.summoned((Entity)livingEntity)) {
            return null;
        }
        if (boss != null && !boss.m_9236_().f_46443_ && boss.m_6084_() && !boss.m_213877_()) {
            ResourceLocation key = BuiltInRegistries.f_256780_.m_7981_((Object)boss.m_6095_());
            if (key != null && "ftboss".equals(key.m_135827_())) {
                BossBarTheme theme = BossBarTheme.byEntityPath(key.m_135815_());
                if (theme == null) {
                    return null;
                }
                float max = boss.m_21233_();
                float ratio = max <= 0.0f ? 0.0f : BossBarTheme.clamp01(boss.m_21223_() / max);
                int phase = theme.phaseIndex(ratio);
                boolean finale = NonManorFinaleDifficulty.isFinale((LivingEntity)boss);
                int manorVariant = -1;
                int manorCount = 0;
                int defeatedMask = 0;
                byte[] variants = NO_VARIANTS;
                if (boss instanceof EasterBossEntity) {
                    MinecraftServer server;
                    EasterBossEntity manor = (EasterBossEntity)boss;
                    finale |= manor.isImpossibleChallenge();
                    manorVariant = manor.getVariant();
                    manorCount = EasterBossEntity.MANOR_COUNT;
                    defeatedMask = BossBarFeed.manorDefeatedMask(server, (LivingEntity)boss);
                    variants = new byte[EasterBossEntity.MANOR_VARIANTS.length];
                    for (int i = 0; i < variants.length; ++i) {
                        variants[i] = (byte)EasterBossEntity.MANOR_VARIANTS[i];
                    }
                }
                return new Snapshot((LivingEntity)boss, boss.m_19879_(), theme.id(), ratio, BossBarFeed.displayHp(boss.m_21223_()), BossBarFeed.displayHp(max), phase, BossBarFeed.countMinions((LivingEntity)boss), finale, BossBarFeed.castProgress((LivingEntity)boss), manorVariant, manorCount, defeatedMask, variants);
            }
            return null;
        }
        return null;
    }

    private static int displayHp(float hp) {
        return !Float.isNaN(hp) && !(hp <= 0.0f) ? Math.min(Integer.MAX_VALUE, Math.round(hp)) : 0;
    }

    private static int countMinions(LivingEntity boss) {
        try {
            int count = 0;
            for (Mob mob : boss.m_9236_().m_45976_(Mob.class, boss.m_20191_().m_82400_(32.0))) {
                ResourceLocation key;
                if (mob == boss || mob.m_213877_() || !mob.m_6084_() || (key = BuiltInRegistries.f_256780_.m_7981_((Object)mob.m_6095_())) == null || !"ftboss".equals(key.m_135827_()) || BossBarTheme.hasTheme(key.m_135815_())) continue;
                ++count;
            }
            return count;
        }
        catch (Throwable var5) {
            return 0;
        }
    }

    private static int manorDefeatedMask(MinecraftServer server, LivingEntity boss) {
        try {
            UUID fightId = ManorFight.fightId((Entity)boss);
            if (fightId == null) {
                return 0;
            }
            for (Tag tag : ManorFightData.get(server).active()) {
                CompoundTag entry;
                if (!(tag instanceof CompoundTag) || !(entry = (CompoundTag)tag).m_128403_("Id") || !fightId.equals(entry.m_128342_("Id"))) continue;
                int mask = 0;
                for (int variant : entry.m_128465_("Defeated")) {
                    if (variant < 0 || variant >= 32) continue;
                    mask |= 1 << variant;
                }
                return mask;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0;
    }

    private static float castProgress(LivingEntity boss) {
        try {
            long now = boss.m_9236_().m_46467_();
            for (SynchedEntityData.DataValue value : boss.m_20088_().m_252804_()) {
                CompoundTag tag;
                Object raw = value.f_252525_();
                if (!(raw instanceof CompoundTag) || !(tag = (CompoundTag)raw).m_128441_("duration") || !tag.m_128441_("start")) continue;
                int duration = tag.m_128451_("duration");
                long start = tag.m_128454_("start");
                long age = now - start;
                if (duration <= 0 || start <= 0L || age < 0L || age >= (long)duration || !tag.m_128471_("cast") && !tag.m_128461_("name").contains("cast")) continue;
                return BossBarTheme.clamp01((float)age / (float)duration);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0.0f;
    }

    private record Snapshot(LivingEntity boss, int entityId, String themeId, float ratio, int hp, int maxHp, int phase, int minions, boolean finale, float cast, int manorVariant, int manorCount, int defeatedMask, byte[] variants) {
    }
}

