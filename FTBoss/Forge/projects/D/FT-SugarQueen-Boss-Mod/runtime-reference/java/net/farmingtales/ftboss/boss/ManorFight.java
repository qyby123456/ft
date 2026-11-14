/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerBossEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.BossEvent$BossBarColor
 *  net.minecraft.world.BossEvent$BossBarOverlay
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppingEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ArmorShred;
import net.farmingtales.ftboss.boss.BossAdvancements;
import net.farmingtales.ftboss.boss.BossBarVisibility;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.HiddenBossChallenge;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ImpossibleSparrowSupport;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.boss.ManorSafeTeleport;
import net.farmingtales.ftboss.boss.ManorTransition;
import net.farmingtales.ftboss.boss.ManorVictory;
import net.farmingtales.ftboss.boss.ManorVictoryData;
import net.farmingtales.ftboss.boss.SparrowSupport;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.boss.ai.ManorArenaChunks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.ManorRecallBoundaryPacket;
import net.farmingtales.ftboss.sound.BossMusic;
import net.farmingtales.ftboss.util.FullFightFlightRules;
import net.farmingtales.ftboss.util.ManorCombatRules;
import net.farmingtales.ftboss.util.ManorTeleportRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
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
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ManorFight {
    public static final int NEXT_DELAY = 60;
    public static final int UNLOAD_GRACE_TICKS = 600;
    public static final int RECALL_CD = 100;
    private static final double SIX_CHUNKS = 96.0;
    public static final double STRAY_FROM_BOSS = 76.0;
    public static final int NO_TARGET_RECALL_TICKS = 200;
    public static final double ATTACH_RANGE = 96.0;
    private static final Map<UUID, Long> LAST_RECALL = new ConcurrentHashMap<UUID, Long>();
    private static final int RECALL_CAP = 64;
    public static final String BAR_NAME = "\u751c\u82f9\u679c\u5e84\u56ed";
    private static final List<Fight> ACTIVE = new CopyOnWriteArrayList<Fight>();
    private static MinecraftServer activeServer;
    private static final int RELOCATION_COOLDOWN = 200;
    private static long lastPump;
    private static long lastShredPump;

    private static void pruneRecall(long now) {
        if (LAST_RECALL.size() <= 64) {
            return;
        }
        LAST_RECALL.entrySet().removeIf(en -> now - (Long)en.getValue() > 6000L);
        if (LAST_RECALL.size() > 64) {
            LAST_RECALL.clear();
        }
    }

    private static String progressTitle(int done) {
        return "\u00a7c\u00a7l\u751c\u82f9\u679c\u5e84\u56ed \u00a77\u5df2\u51fb\u8d25 \u00a7c" + done + "\u00a77/\u00a7c" + EasterBossEntity.MANOR_COUNT;
    }

    private static void updateBar(Fight f) {
        int done = f.defeated.size();
        f.bar.m_6456_((Component)Component.m_237113_((String)(ManorFight.progressTitle(done) + f.difficulty.badge())));
        f.bar.m_142711_(Math.min(1.0f, (float)done / (float)EasterBossEntity.MANOR_COUNT));
    }

    private static void initialize(MinecraftServer server) {
        if (activeServer == server) {
            return;
        }
        ACTIVE.forEach(fight -> {
            fight.pendingPlayerRecalls.clear();
            fight.bar.m_7706_();
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
                ResourceKey dim = ResourceKey.m_135785_((ResourceKey)Registries.f_256858_, (ResourceLocation)new ResourceLocation(record.m_128461_("Dimension")));
                if (!record.m_128403_("Id") || ManorFightData.get(server).retired(record.m_128342_("Id"))) continue;
                Fight fight2 = new Fight((ResourceKey<Level>)dim, new Vec3(record.m_128459_("X"), record.m_128459_("Y"), record.m_128459_("Z")), record.m_128403_("Summoner") ? record.m_128342_("Summoner") : null);
                fight2.id = record.m_128342_("Id");
                fight2.difficulty = ManorDifficulty.fromId(record.m_128461_("Difficulty"));
                UUID uUID = fight2.currentId = record.m_128403_("Current") ? record.m_128342_("Current") : null;
                if (fight2.currentId != null) {
                    fight2.onStage.add(fight2.currentId);
                }
                fight2.currentVariant = record.m_128451_("Variant");
                fight2.lastPosition = new Vec3(record.m_128459_("BossX"), record.m_128459_("BossY"), record.m_128459_("BossZ"));
                for (int variant : record.m_128465_("Defeated")) {
                    if (!ManorFight.isManorVariant(variant)) continue;
                    fight2.defeated.add(variant);
                }
                Object object = record.m_128437_("Participants", 10).iterator();
                while (object.hasNext()) {
                    Tag player = (Tag)object.next();
                    CompoundTag value = (CompoundTag)player;
                    if (!value.m_128403_("Id")) continue;
                    fight2.participants.add(value.m_128342_("Id"));
                }
                fight2.refillPool(fight2.currentVariant);
                fight2.nextDelay = fight2.defeated.contains(fight2.currentVariant) ? 60 : 0;
                ACTIVE.add(fight2);
                ManorFight.updateBar(fight2);
                FTBossMod.LOGGER.info("[FTBoss][Manor] \u6062\u590d\u6218\u573a {}\uff0c\u5df2\u51fb\u8d25 {}/{}", new Object[]{fight2.id, fight2.defeated.size(), EasterBossEntity.MANOR_COUNT});
            }
            catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Manor] \u5ffd\u7565\u635f\u574f\u7684\u6218\u573a\u8bb0\u5f55", (Throwable)failure);
            }
        }
        for (Fight fight3 : ACTIVE) {
            ServerLevel restored = server.m_129880_(fight3.dim);
            if (restored == null) continue;
            ManorArenaChunks.hold(restored, fight3.anchor);
        }
    }

    private static void persist() {
        if (activeServer == null) {
            return;
        }
        ListTag records = new ListTag();
        for (Fight fight : ACTIVE) {
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
                players.add((Object)value);
            }
            record.m_128365_("Participants", (Tag)players);
            records.add((Object)record);
        }
        ManorFightData.get(activeServer).store(records);
    }

    private static Fight find(UUID id) {
        if (id == null) {
            return null;
        }
        for (Fight fight : ACTIVE) {
            if (!fight.id.equals(id)) continue;
            return fight;
        }
        return null;
    }

    public static ManorDifficulty difficulty(UUID id) {
        Fight fight = ManorFight.find(id);
        return fight == null ? ManorDifficulty.NORMAL : fight.difficulty;
    }

    public static boolean isImpossibleFor(Player player) {
        if (player == null) {
            return false;
        }
        for (Fight fight : ACTIVE) {
            if (fight.difficulty != ManorDifficulty.IMPOSSIBLE || !fight.dim.equals((Object)player.m_9236_().m_46472_()) || !fight.participants.contains(player.m_20148_()) && !(player.m_20275_(fight.anchor.f_82479_, fight.anchor.f_82480_, fight.anchor.f_82481_) <= 9216.0)) continue;
            return true;
        }
        return false;
    }

    public static UUID fightId(Entity member) {
        return ManorFightMembers.fightId(ManorFightMembers.resolve(member));
    }

    public static UUID currentBoss(UUID id) {
        Fight fight = ManorFight.find(id);
        return fight == null ? null : fight.currentId;
    }

    public static UUID summoner(UUID id) {
        Fight fight = ManorFight.find(id);
        return fight == null ? null : fight.summonerId;
    }

    public static boolean hasActive(ServerLevel level) {
        ManorFight.initialize(level.m_7654_());
        return ACTIVE.stream().anyMatch(fight -> fight.dim.equals((Object)level.m_46472_()));
    }

    public static boolean hasActive(UUID id) {
        return ManorFight.find(id) != null;
    }

    public static int currentVariant(UUID id) {
        Fight fight = ManorFight.find(id);
        return fight == null ? -1 : fight.currentVariant;
    }

    public static boolean canSurrender(ServerPlayer player, UUID id) {
        Fight fight = ManorFight.find(id);
        return fight != null && ManorCombatRules.canSurrender(fight.summonerId, player.m_20148_(), fight.id, id);
    }

    public static boolean surrender(ServerPlayer player, UUID id) {
        if (!ManorFight.canSurrender(player, id)) {
            return false;
        }
        Fight fight = ManorFight.find(id);
        ServerLevel level = player.f_8924_.m_129880_(fight.dim);
        if (level == null) {
            return false;
        }
        ManorFight.endFight(level, fight, false, "\u53ec\u5524\u8005\u6295\u964d");
        return true;
    }

    public static boolean abortForHardcore(ServerPlayer player, UUID attributedFight) {
        if (player == null) {
            return false;
        }
        ManorFight.initialize(player.f_8924_);
        Fight fight = ManorFight.find(attributedFight);
        if (fight != null && !fight.dim.equals((Object)player.m_9236_().m_46472_())) {
            fight = null;
        }
        if (fight == null) {
            for (Fight candidate : ACTIVE) {
                if (!candidate.dim.equals((Object)player.m_9236_().m_46472_()) || !candidate.participants.contains(player.m_20148_())) continue;
                fight = candidate;
                break;
            }
        }
        if (fight == null) {
            return HiddenBossIntro.stopForHardcore(player);
        }
        ServerLevel level = player.f_8924_.m_129880_(fight.dim);
        if (level == null) {
            return false;
        }
        ManorFight.endFight(level, fight, false, "\u6781\u9650\u6a21\u5f0f\u73a9\u5bb6\u6b7b\u4ea1\u524d\u4e2d\u6b62");
        return true;
    }

    public static boolean stopForCreative(ServerPlayer player) {
        if (player == null || !player.m_7500_()) {
            return false;
        }
        ServerLevel level = player.m_284548_();
        ManorFight.initialize(level.m_7654_());
        boolean stopped = HiddenBossChallenge.cancelForCreative(level);
        for (Fight fight : new ArrayList<Fight>(ACTIVE)) {
            if (!fight.dim.equals((Object)level.m_46472_())) continue;
            ManorFight.endFight(level, fight, false, "\u521b\u9020\u9053\u5177\u4e2d\u6b62");
            stopped = true;
        }
        return HiddenBossIntro.stopForCreative(level) || stopped;
    }

    public static void participate(Entity member, ServerPlayer player) {
        Fight fight = ManorFight.find(ManorFight.fightId(member));
        if (fight != null && fight.participants.add(player.m_20148_())) {
            ManorFight.persist();
        }
    }

    public static void attackedBy(EasterBossEntity member, ServerPlayer player) {
        Fight fight = ManorFight.find(ManorFight.fightId((Entity)member));
        if (fight == null) {
            return;
        }
        if (fight.summonerId == null) {
            fight.summonerId = player.m_20148_();
            for (Entity entity : ManorFight.members(player.f_8924_, fight)) {
                if (!(entity instanceof EasterBossEntity)) continue;
                EasterBossEntity boss = (EasterBossEntity)entity;
                boss.setManorSummonerId(fight.summonerId);
            }
            ManorFight.persist();
        }
        if (fight.participants.add(player.m_20148_())) {
            ManorFight.persist();
        }
    }

    private static List<Entity> members(MinecraftServer server, Fight fight) {
        ArrayList<Entity> result = new ArrayList<Entity>();
        for (ServerLevel level : server.m_129785_()) {
            for (Entity entity : level.m_142646_().m_142273_()) {
                Entity owner;
                if (ManorFightMembers.fightId(entity) == null && (owner = ManorFightMembers.resolve(entity)) != entity) {
                    ManorFightMembers.inherit(owner, entity);
                }
                if (!fight.id.equals(ManorFightMembers.fightId(entity))) continue;
                result.add(entity);
            }
        }
        return result;
    }

    private static void bind(Fight fight, EasterBossEntity boss) {
        ManorFightMembers.bind((Entity)boss, fight.id);
        boss.setChallengeDifficulty(fight.difficulty);
        SparrowSupport.noteFightMember((Entity)boss);
        boss.setManorSummonerId(fight.summonerId);
        if (!HiddenBossIntro.isActing(boss.m_20148_())) {
            boss.m_21557_(false);
            boss.m_20331_(false);
        }
    }

    private static void endFight(ServerLevel level, Fight fight, boolean victory, String reason) {
        if (ManorFightData.get(level.m_7654_()).retired(fight.id)) {
            return;
        }
        ManorFight.syncRecallBoundary(level, fight, false);
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
        if (ACTIVE.stream().noneMatch(other -> other.dim.equals((Object)level.m_46472_()))) {
            ManorArenaChunks.release(level);
        }
        fight.pendingPlayerRecalls.clear();
        BossBarVisibility.visible(fight.bar, false);
        fight.bar.m_7706_();
        ManorMercy.clearFight(fight.id);
        for (Entity entity : ManorFight.members(level.m_7654_(), fight)) {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                Level level2 = entity.m_9236_();
                if (level2 instanceof ServerLevel) {
                    ServerLevel memberLevel = (ServerLevel)level2;
                    BossMusic.release(memberLevel, living);
                }
            }
            HiddenBossIntro.cancel(entity.m_20148_());
            entity.m_146870_();
        }
        for (UUID participant : fight.participants) {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(participant);
            LAST_RECALL.remove(participant);
            if (player == null) continue;
            ArmorShred.clear((LivingEntity)player);
            if (victory) {
                player.m_213846_((Component)Component.m_237113_((String)(fight.difficulty == ManorDifficulty.IMPOSSIBLE ? "\u00a74\u3010\u751c\u82f9\u679c\u5e84\u56ed\u00b7" + fight.difficulty.title() + "\u3011\u00a7f\u6311\u6218\u5b8c\u6210\u3002\u00a77\u672c\u96be\u5ea6\u4e0d\u53d1\u653e\u5e84\u56ed\u6210\u5c31\u6216\u901a\u5173\u5934\u8854\u3002" : "\u00a76\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a7f\u666e\u901a\u96be\u5ea6\u901a\u5173\uff01\u00a77" + ManorDifficulty.IMPOSSIBLE.label() + "\u5df2\u89e3\u9501\uff0c\u53ef\u5728\u4e0b\u6b21\u6311\u6218\u65f6\u9009\u62e9\u3002")));
                continue;
            }
            player.m_213846_((Component)Component.m_237113_((String)("\u00a76\u3010\u751c\u82f9\u679c\u5e84\u56ed\u00b7" + fight.difficulty.title() + "\u3011\u00a77\u6311\u6218\u5df2\u7ed3\u675f\uff08" + reason + "\uff09\uff0c\u4e0d\u8ba1\u51fb\u8d25\u3001\u65e0\u6389\u843d\u3002")));
        }
        SparrowSupport.forgetSparrow();
        BossBars.sweep(level.m_7654_());
        ManorFight.persist();
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u6218\u573a {} \u7ed3\u675f\uff1a{}\uff0c\u80dc\u5229={}", new Object[]{fight.id, reason, victory});
    }

    public static boolean isManaged(LivingEntity le) {
        for (Fight f : ACTIVE) {
            if (!f.onStage.contains(le.m_20148_())) continue;
            return true;
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
        }
        for (Fight f : ACTIVE) {
            if (!f.dim.equals((Object)p.m_9236_().m_46472_()) || !(p.m_20275_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_) <= 9216.0)) continue;
            return true;
        }
        return false;
    }

    public static void adopt(ServerLevel sl, EasterBossEntity boss) {
        if (sl == null || boss == null) {
            return;
        }
        ManorFight.initialize(sl.m_7654_());
        if (HiddenBossIntro.isActing(boss.m_20148_()) || !SummonGate.isAllowedDim((Level)sl)) {
            return;
        }
        UUID previous = ManorFightMembers.fightId((Entity)boss);
        if (ManorFightData.get(sl.m_7654_()).retired(previous)) {
            boss.m_146870_();
            return;
        }
        ManorFight.clear((ResourceKey<Level>)sl.m_46472_());
        Fight f = new Fight((ResourceKey<Level>)sl.m_46472_(), boss.m_20182_(), boss.getManorSummonerId());
        f.difficulty = boss.getChallengeDifficulty();
        if (previous != null) {
            f.id = previous;
        }
        ManorFight.bind(f, boss);
        f.currentVariant = boss.getVariant();
        f.currentId = boss.m_20148_();
        f.current = boss;
        f.onStage.add(boss.m_20148_());
        f.refillPool(boss.getVariant());
        ACTIVE.add(f);
        ManorArenaChunks.hold(sl, f.anchor);
        for (Entity entity : sl.m_142646_().m_142273_()) {
            EasterBossEntity sparrow;
            if (!(entity instanceof EasterBossEntity) || (sparrow = (EasterBossEntity)entity).getVariant() != 9 || f.summonerId == null || !f.summonerId.equals(sparrow.getManorSummonerId())) continue;
            ManorFight.bind(f, sparrow);
        }
        ManorFight.purgeStale(sl, boss.m_20148_(), true);
        SparrowSupport.beginStage(sl, boss);
        for (ServerPlayer p : sl.m_6907_()) {
            if (!(p.m_20280_((Entity)boss) <= 9216.0)) continue;
            BossBarVisibility.addPlayer(f.bar, p);
        }
        ManorFight.updateBar(f);
        ManorFight.persist();
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u5e84\u56ed\u6218\u573a\u5f00\u59cb(\u72ec\u7acb\u8840\u6761): \u9996\u53d1={} \u8840\u91cf={}", (Object)EasterBossEntity.NAMES[boss.getVariant()], (Object)((int)boss.m_21233_()));
    }

    public static void start(ServerLevel sl, Vec3 pos, int variant) {
        ManorFight.start(sl, pos, variant, null);
    }

    public static void start(ServerLevel sl, Vec3 pos, int variant, UUID summonerId) {
        ManorFight.start(sl, pos, variant, summonerId, ManorDifficulty.NORMAL);
    }

    public static void start(ServerLevel sl, Vec3 pos, int variant, UUID summonerId, ManorDifficulty difficulty) {
        if (sl == null) {
            return;
        }
        ManorFight.initialize(sl.m_7654_());
        if (!SummonGate.isAllowedDim((Level)sl)) {
            return;
        }
        ManorFight.clear((ResourceKey<Level>)sl.m_46472_());
        Fight f = new Fight((ResourceKey<Level>)sl.m_46472_(), pos, summonerId);
        f.difficulty = difficulty == null ? ManorDifficulty.NORMAL : difficulty;
        f.refillPool(variant);
        ACTIVE.add(f);
        ManorArenaChunks.hold(sl, f.anchor);
        for (Entity entity : sl.m_142646_().m_142273_()) {
            EasterBossEntity sparrow;
            if (!(entity instanceof EasterBossEntity) || (sparrow = (EasterBossEntity)entity).getVariant() != 9 || summonerId == null || !summonerId.equals(sparrow.getManorSummonerId())) continue;
            ManorFight.bind(f, sparrow);
        }
        ManorFight.purgeStale(sl, null, true);
        ManorFight.spawnVariant(sl, f, variant);
        ManorFight.updateBar(f);
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u5e84\u56ed\u6218\u573a\u5f00\u59cb(\u72ec\u7acb\u8840\u6761): \u9996\u53d1={}", (Object)EasterBossEntity.NAMES[variant]);
    }

    public static void clear(ResourceKey<Level> dim) {
        if (activeServer == null) {
            return;
        }
        for (Fight fight : ACTIVE) {
            ServerLevel level;
            if (!fight.dim.equals(dim) || (level = activeServer.m_129880_(dim)) == null) continue;
            ManorFight.endFight(level, fight, false, "\u6218\u573a\u91cd\u7f6e");
        }
    }

    public static boolean rescue(EasterBossEntity boss) {
        ServerPlayer summoner;
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        Fight fight = ManorFight.find(ManorFight.fightId((Entity)boss));
        long now = level2.m_46467_();
        if (fight != null) {
            if (fight.lastVoidFall == Long.MIN_VALUE || now - fight.lastVoidFall > 600L) {
                fight.voidFalls = 0;
            }
            fight.lastVoidFall = now;
            ++fight.voidFalls;
            if (fight.voidFalls >= 2 && ManorCombatRules.relocationReady(now, fight.lastRelocation, 200) && ManorFight.relocate(level2, fight, boss)) {
                return true;
            }
        }
        ServerPlayer serverPlayer = summoner = boss.getManorSummonerId() == null ? null : level2.m_7654_().m_6846_().m_11259_(boss.getManorSummonerId());
        Vec3 preferred = boss.canTeleportWith((LivingEntity)summoner) ? summoner.m_20182_() : (fight == null ? boss.m_20182_() : fight.anchor);
        Vec3 safe = ManorSafeTeleport.find(level2, (Entity)boss, preferred, 16);
        if (safe == null && fight != null) {
            safe = ManorSafeTeleport.find(level2, (Entity)boss, fight.anchor, 24);
        }
        if (safe != null) {
            boss.prepareArenaTransfer();
            ManorSafeTeleport.move(level2, (Entity)boss, safe);
            FTBossMod.LOGGER.info("[FTBoss][Manor] \u5760\u7a7a\u6551\u63f4 {} {} -> {}", new Object[]{boss.m_7755_().getString(), boss.m_20148_(), safe});
            return true;
        }
        if (fight != null) {
            ManorFight.endFight(level2, fight, false, "\u9644\u8fd1\u6ca1\u6709\u5b89\u5168\u843d\u70b9");
        } else {
            boss.m_146870_();
            FTBossMod.LOGGER.warn("[FTBoss][Manor] \u65e0\u4e3b Boss {} \u65e0\u5b89\u5168\u843d\u70b9\uff0c\u53d6\u6d88\u800c\u975e\u8ba1\u4e3a\u51fb\u8d25", (Object)boss.m_20148_());
        }
        return false;
    }

    private static boolean relocate(ServerLevel level, Fight fight, EasterBossEntity trigger) {
        ServerPlayer summoner;
        fight.lastRelocation = level.m_46467_();
        List<Entity> entities = ManorFight.members(level.m_7654_(), fight);
        ServerPlayer serverPlayer = summoner = fight.summonerId == null ? null : level.m_7654_().m_6846_().m_11259_(fight.summonerId);
        if (summoner != null && summoner.m_6084_() && !summoner.m_5833_() && summoner.m_9236_() == level) {
            entities.add((Entity)summoner);
        }
        for (int attempt = 0; attempt < 24; ++attempt) {
            double angle = level.f_46441_.m_188500_() * Math.PI * 2.0;
            double distance = 52.0 + level.f_46441_.m_188500_() * 20.0;
            Vec3 candidate = fight.anchor.m_82520_(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
            Vec3 anchor = ManorSafeTeleport.find(level, (Entity)trigger, candidate, 3);
            if (anchor == null || anchor.m_82546_(fight.anchor).m_165925_() < 2304.0) continue;
            LinkedHashMap<Entity, Vec3> destinations = new LinkedHashMap<Entity, Vec3>();
            boolean safe = true;
            int ordinal = 0;
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity) || !entity.m_6084_() || entity.m_9236_() != level) continue;
                double offset = (double)ordinal++ * 2.4;
                Vec3 preferred = entity == trigger ? anchor : anchor.m_82520_(Math.cos(offset) * 4.0, 0.0, Math.sin(offset) * 4.0);
                Vec3 destination = ManorSafeTeleport.find(level, entity, preferred, 5);
                if (destination == null) {
                    safe = false;
                    break;
                }
                destinations.put(entity, destination);
            }
            if (!safe) continue;
            fight.anchor = anchor;
            fight.voidFalls = 0;
            ManorArenaChunks.hold(level, fight.anchor);
            for (Map.Entry entry : destinations.entrySet()) {
                Object k = entry.getKey();
                if (k instanceof EasterBossEntity) {
                    EasterBossEntity boss = (EasterBossEntity)k;
                    boss.prepareArenaTransfer();
                }
                ManorSafeTeleport.move(level, (Entity)entry.getKey(), (Vec3)entry.getValue());
            }
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) continue;
                entity.m_146870_();
            }
            if (fight.current != null) {
                fight.lastPosition = fight.current.m_20182_();
            }
            ManorFight.persist();
            if (summoner != null) {
                summoner.m_5661_((Component)Component.m_237113_((String)"\u00a76\u3010\u5e84\u56ed\u3011\u00a77\u6218\u573a\u5df2\u5b89\u5168\u642c\u8fc1\uff0c\u7ee7\u7eed\u6311\u6218\u3002"), true);
            }
            FTBossMod.LOGGER.info("[FTBoss][Manor] \u6574\u573a\u5b89\u5168\u642c\u8fc1 {} -> {}\uff0c\u6210\u5458 {}", new Object[]{fight.id, anchor, destinations.size()});
            return true;
        }
        FTBossMod.LOGGER.warn("[FTBoss][Manor] \u6218\u573a {} \u672a\u627e\u5230\u5b89\u5168\u642c\u8fc1\u70b9\uff0c\u4fdd\u7559\u539f\u573a\u5730", (Object)fight.id);
        return false;
    }

    public static Vec3 anchorOf(ServerLevel sl) {
        for (Fight f : ACTIVE) {
            if (!f.dim.equals((Object)sl.m_46472_())) continue;
            return f.anchor;
        }
        return null;
    }

    public static Vec3 anchorOf(UUID id) {
        Fight fight = ManorFight.find(id);
        return fight == null ? null : fight.anchor;
    }

    public static void reattach(ServerPlayer p) {
        if (p == null) {
            return;
        }
        for (Fight f : ACTIVE) {
            if (!f.dim.equals((Object)p.m_9236_().m_46472_()) || p.m_20275_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_) > 9216.0) continue;
            f.bar.m_6539_(p);
            BossBarVisibility.addPlayer(f.bar, p);
            BossBarVisibility.visible(f.bar, true);
            ManorFight.updateBar(f);
            FTBossMod.LOGGER.info("[FTBoss][Manor] \u73a9\u5bb6 {} \u91cd\u8fdb\u5b58\u6863 -> \u5df2\u628a\u5e84\u56ed\u8fdb\u5ea6\u8840\u6761\u91cd\u6302\u4e0a\u53bb(\u8fdb\u5ea6 {}/{})", new Object[]{p.m_7755_().getString(), f.defeated.size(), EasterBossEntity.MANOR_COUNT});
        }
    }

    private static boolean isManorVariant(int v) {
        for (int i : EasterBossEntity.MANOR_VARIANTS) {
            if (i != v) continue;
            return true;
        }
        return false;
    }

    public static void recoverNear(MinecraftServer server) {
        if (server == null) {
            return;
        }
        ManorFight.initialize(server);
        block0: for (ServerLevel level : server.m_129785_()) {
            boolean active;
            if (!SummonGate.isAllowedDim((Level)level) || (active = ACTIVE.stream().anyMatch(fight -> fight.dim.equals((Object)level.m_46472_())))) continue;
            boolean adopted = false;
            for (ServerPlayer player : level.m_6907_()) {
                for (EasterBossEntity boss : level.m_45976_(EasterBossEntity.class, player.m_20191_().m_82400_(160.0))) {
                    if (!boss.m_6084_() || boss.m_213877_() || boss.isMini() || !ManorFight.isManorVariant(boss.getVariant()) || HiddenBossIntro.isActing(boss.m_20148_())) continue;
                    UUID previous = ManorFightMembers.fightId((Entity)boss);
                    if (ManorFightData.get(server).retired(previous)) {
                        boss.m_146870_();
                        continue;
                    }
                    ManorFight.adopt(level, boss);
                    FTBossMod.LOGGER.info("[FTBoss][Manor] \u6536\u7f16\u65e0\u6258\u7ba1 Boss {}\uff1b\u53ec\u5524\u8005={}", (Object)boss.m_20148_(), (Object)boss.getManorSummonerId());
                    adopted = true;
                    break;
                }
                if (!adopted) continue;
                continue block0;
            }
        }
    }

    public static int purgeStale(ServerLevel sl, UUID keep, boolean keepSparrow) {
        if (sl == null) {
            return 0;
        }
        ArrayList<Entity> stale = new ArrayList<Entity>();
        for (Entity entity : sl.m_142646_().m_142273_()) {
            if (!ManorFightData.get(sl.m_7654_()).retired(ManorFightMembers.fightId(entity))) continue;
            stale.add(entity);
        }
        stale.forEach(Entity::m_146870_);
        return stale.size();
    }

    @Deprecated
    public static boolean onDamage(ServerLevel level, EasterBossEntity boss, float amount, DamageSource source) {
        return false;
    }

    private static void spawnVariant(ServerLevel sl, Fight f, int variant) {
        EasterBossEntity e = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).m_20615_((Level)sl);
        if (e == null) {
            ManorFight.endFight(sl, f, false, "\u65e0\u6cd5\u751f\u6210\u4e0b\u4e00\u4f4d Boss");
            return;
        }
        double yaw = (float)Math.toDegrees(Math.atan2(f.anchor.f_82481_ - f.anchor.f_82481_, 1.0));
        e.m_7678_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_, (float)yaw, 0.0f);
        e.setVariant(variant);
        e.setManorSummonerId(f.summonerId);
        ManorFight.bind(f, e);
        e.prepareManorEntryBuffs();
        e.m_21557_(false);
        if (!sl.m_7967_((Entity)e)) {
            ManorFight.endFight(sl, f, false, "\u4e0b\u4e00\u4f4d Boss \u751f\u6210\u88ab\u53d6\u6d88");
            return;
        }
        f.onStage.add(e.m_20148_());
        f.currentVariant = variant;
        f.currentId = e.m_20148_();
        f.current = e;
        f.nextDelay = 0;
        f.unloadedTicks = 0;
        f.lastPosition = e.m_20182_();
        ManorFight.syncRecallBoundary(sl, f, true);
        SparrowSupport.beginStage(sl, e);
        ManorFight.persist();
        ManorTransition.arrive(sl, f.id, e.m_20182_(), variant);
        sl.m_6263_(null, f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_, SoundEvents.f_11852_, SoundSource.HOSTILE, 1.4f, 0.7f);
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u4e0a\u573a: {} (\u72ec\u7acb\u8840\u91cf {}, \u5df2\u51fb\u8d25 {}/{})", new Object[]{EasterBossEntity.NAMES[variant], (int)e.m_21233_(), f.defeated.size(), EasterBossEntity.MANOR_COUNT});
        for (ServerPlayer p : sl.m_6907_()) {
            if (!(p.m_20275_(f.anchor.f_82479_, f.anchor.f_82480_, f.anchor.f_82481_) <= 9216.0)) continue;
            p.m_213846_((Component)Component.m_237113_((String)("\u00a7c\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a77\u7b2c \u00a7c" + (f.defeated.size() + 1) + "\u00a77 \u4f4d\u4e0a\u573a\uff1a\u00a7c" + EasterBossEntity.NAMES[variant] + " \u00a78(\u5df2\u51fb\u8d25 " + f.defeated.size() + "/" + EasterBossEntity.MANOR_COUNT + ")")));
        }
    }

    private static void onCurrentDefeated(ServerLevel sl, Fight f) {
        ManorFight.syncRecallBoundary(sl, f, false);
        SparrowSupport.endStage(sl, f.id);
        if (f.currentVariant >= 0 && f.defeated.add(f.currentVariant)) {
            ManorMercy.clearFight(f.id);
            ManorFight.persist();
            ManorFight.updateBar(f);
            FTBossMod.LOGGER.info("[FTBoss][Manor] \u5df2\u51fb\u8d25 {} ({}/{})", new Object[]{EasterBossEntity.NAMES[f.currentVariant], f.defeated.size(), EasterBossEntity.MANOR_COUNT});
            for (ServerPlayer p : sl.m_6907_()) {
                if (!(p.m_20238_(f.anchor) <= 9216.0)) continue;
                p.m_213846_((Component)Component.m_237113_((String)("\u00a76\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a77" + EasterBossEntity.NAMES[f.currentVariant] + " \u00a77\u5df2\u88ab\u51fb\u8d25\uff01\u00a78(\u8fdb\u5ea6 " + f.defeated.size() + "/" + EasterBossEntity.MANOR_COUNT + ")")));
            }
        }
        f.onStage.clear();
        f.pendingPlayerRecalls.clear();
        f.currentId = null;
        f.current = null;
    }

    private static void finish(ServerLevel sl, Fight f) {
        ManorFight.endFight(sl, f, true, "\u5168\u90e8\u4e3b\u6218 Boss \u5df2\u51fb\u8d25");
    }

    public static void pumpAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ManorFight.initialize(server);
        long now = server.m_129783_().m_46467_();
        if (now != lastShredPump) {
            lastShredPump = now;
            ArmorShred.tick(server);
        }
        if (ACTIVE.isEmpty()) {
            return;
        }
        if (now == lastPump) {
            return;
        }
        lastPump = now;
        ManorArenaChunks.beginFrame();
        for (Fight f : ACTIVE) {
            ServerLevel sl = server.m_129880_(f.dim);
            if (sl == null) continue;
            ++f.phase;
            if (f.currentVariant >= 0) {
                boolean inWorld;
                EasterBossEntity le;
                Entity inWorld2;
                EasterBossEntity cur = f.current;
                if (f.currentId != null && sl.m_8791_(f.currentId) == null && !f.defeated.contains(f.currentVariant) && f.unloadedTicks % 100 == 20) {
                    sl.m_46865_(BlockPos.m_274446_((Position)f.lastPosition));
                }
                if (f.currentId != null && (inWorld2 = sl.m_8791_(f.currentId)) instanceof EasterBossEntity && (le = (EasterBossEntity)inWorld2) != cur) {
                    cur = le;
                    f.current = le;
                    ManorFight.bind(f, le);
                }
                boolean dead = f.defeated.contains(f.currentVariant) || cur != null && (cur.m_21224_() || cur.m_21223_() <= 0.0f);
                boolean bl = inWorld = cur != null && sl.m_8791_(cur.m_20148_()) == cur;
                if (inWorld && !dead) {
                    f.lastPosition = cur.m_20182_();
                    if (f.phase % 20 == 0 && !SparrowSupport.hasVisited((LivingEntity)cur)) {
                        SparrowSupport.beginStage(sl, cur);
                    }
                }
                if (dead || inWorld) {
                    f.unloadedTicks = 0;
                } else {
                    ++f.unloadedTicks;
                    if (f.unloadedTicks == 601) {
                        FTBossMod.LOGGER.warn("[FTBoss][Manor] {} \u8fde\u7eed {} tick \u4e0d\u5728\u4e16\u754c\u91cc(\u533a\u5757\u4e00\u76f4\u6ca1\u52a0\u8f7d) -> \u53d6\u6d88\u672c\u573a\uff0c\u4e0d\u8ba1\u51fb\u8d25", (Object)EasterBossEntity.NAMES[f.currentVariant], (Object)f.unloadedTicks);
                    }
                }
                if (!dead && f.unloadedTicks > 600) {
                    ManorFight.endFight(sl, f, false, "Boss \u5378\u8f7d\u540e\u65e0\u6cd5\u6062\u590d");
                    continue;
                }
                boolean gone = ManorCombatRules.defeated(dead, f.unloadedTicks);
                if (gone) {
                    if (f.nextDelay <= 0) {
                        ManorFight.onCurrentDefeated(sl, f);
                        f.nextDelay = 60;
                        if (f.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                            ManorTransition.prepare(sl, f.id, f.anchor, f.pool.peekFirst() == null ? -1 : f.pool.peekFirst(), f.nextDelay);
                        }
                    } else if (--f.nextDelay <= 0) {
                        f.nextDelay = 0;
                        ManorFight.clearStrayManorBosses(sl, f);
                        if (f.defeated.size() >= EasterBossEntity.MANOR_COUNT) {
                            ManorFight.finish(sl, f);
                        } else {
                            ManorFight.spawnVariant(sl, f, f.nextVariant());
                        }
                    }
                }
                if (f.nextDelay > 0 && f.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                    ManorTransition.prepare(sl, f.id, f.anchor, f.pool.peekFirst() == null ? -1 : f.pool.peekFirst(), f.nextDelay);
                }
            }
            if (f.phase % 20 == 1) {
                ManorFight.syncRecallBoundary(sl, f, true);
            }
            ManorArenaChunks.track(sl, f.anchor);
            ManorFight.recallStrayPlayers(sl, f);
            if (!ACTIVE.contains(f)) continue;
            if (f.phase % 100 == 0) {
                ManorFight.persist();
            }
            for (ServerPlayer stale : new ArrayList(f.bar.m_8324_())) {
                if (!stale.m_9232_() && stale.f_8906_ != null) continue;
                f.bar.m_6539_(stale);
            }
            for (ServerPlayer p : sl.m_6907_()) {
                boolean near;
                if (p.m_9232_() || p.f_8906_ == null) continue;
                boolean bl = near = p.m_20238_(f.anchor) <= 9216.0;
                if (near && !f.bar.m_8324_().contains(p)) {
                    BossBarVisibility.addPlayer(f.bar, p);
                    continue;
                }
                if (near || !f.bar.m_8324_().contains(p)) continue;
                f.bar.m_6539_(p);
            }
            for (UUID id : f.onStage) {
                EasterBossEntity eb;
                Entity ent = sl.m_8791_(id);
                if (!(ent instanceof EasterBossEntity) || !(eb = (EasterBossEntity)ent).m_6084_()) continue;
                if (f.phase % 10 == 0) {
                    sl.m_8767_((ParticleOptions)ParticleTypes.f_123797_, eb.m_20185_(), eb.m_20186_() + 1.0, eb.m_20189_(), 2, 0.3, 0.5, 0.3, 0.02);
                }
                BossBarVisibility.visible(eb.getBossBar(), true);
            }
        }
        ManorArenaChunks.flush();
    }

    public static void pump() {
        ManorFight.pumpAll();
    }

    private static void syncRecallBoundary(ServerLevel level, Fight fight, boolean requestedActive) {
        EasterBossEntity boss = fight.current;
        boolean active = requestedActive && fight.nextDelay <= 0 && boss != null && boss.m_6084_() && !boss.m_213877_() && boss.m_9236_() == level && !boss.isXuanlanSkyCasting() && !boss.isQizaiUninterruptibleCasting();
        ManorRecallBoundaryPacket packet = active ? new ManorRecallBoundaryPacket(fight.id, level.m_46472_().m_135782_(), boss.m_19879_(), boss.m_20148_(), 76.0f, Math.max(0.0f, Math.min(1.0f, (float)fight.noTargetTicks / 200.0f)), true) : ManorRecallBoundaryPacket.stop(fight.id, level.m_46472_().m_135782_());
        for (UUID participant : fight.participants) {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(participant);
            if (player == null) continue;
            ManorRecallBoundaryPacket.send(player, packet);
        }
    }

    private static void recallStrayPlayers(ServerLevel sl, Fight f) {
        boolean obstructed;
        ServerPlayer locked;
        boolean lockedOnMember;
        EasterBossEntity boss = f.current;
        if (boss == null || boss.m_213877_() || !boss.m_6084_()) {
            f.noTargetTicks = 0;
            return;
        }
        if (boss.isXuanlanSkyCasting() || boss.isQizaiUninterruptibleCasting()) {
            f.noTargetTicks = 0;
            f.stalledTicks = 0;
            f.stalledPosition = null;
            return;
        }
        MinecraftServer server = sl.m_7654_();
        if (server == null) {
            return;
        }
        ArrayList<ServerPlayer> members = new ArrayList<ServerPlayer>();
        for (UUID id : f.participants) {
            ServerPlayer member = server.m_6846_().m_11259_(id);
            if (member == null || !member.m_6084_() || member.m_5833_()) continue;
            members.add(member);
        }
        if (members.isEmpty()) {
            f.noTargetTicks = 0;
            return;
        }
        long now = sl.m_46467_();
        LivingEntity lock = boss.m_5448_();
        boolean bl = lockedOnMember = lock instanceof ServerPlayer && ManorTeleportRules.isParticipant(f.participants, (locked = (ServerPlayer)lock).m_20148_());
        f.noTargetTicks = lockedOnMember ? 0 : ++f.noTargetTicks;
        boolean noTarget = f.noTargetTicks >= 200;
        ServerPlayer reference = (ServerPlayer)members.get(0);
        for (ServerPlayer member : members) {
            if (!(member.m_20280_((Entity)boss) < reference.m_20280_((Entity)boss))) continue;
            reference = member;
        }
        boolean bl2 = obstructed = reference.m_9236_() == sl && !boss.isPerformingSkill() && (!sl.m_45786_((Entity)boss) || lockedOnMember && !boss.m_142582_((Entity)reference));
        if (!obstructed || f.stalledPosition == null || boss.m_20238_(f.stalledPosition) > 1.0) {
            f.stalledTicks = 0;
            f.stalledPosition = boss.m_20182_();
        } else {
            ++f.stalledTicks;
        }
        if (f.stalledTicks >= 200) {
            if (ManorFight.teleportBossTo(boss, reference, f)) {
                LAST_RECALL.put(reference.m_20148_(), now);
                f.stalledTicks = 0;
                f.stalledPosition = boss.m_20182_();
                FTBossMod.LOGGER.info("[FTBoss][Manor] {} \u957f\u65f6\u95f4\u88ab\u969c\u788d\u963b\u6321\uff0c\u5b89\u5168\u8131\u56f0", (Object)boss.m_20148_());
            }
            return;
        }
        for (ServerPlayer member : members) {
            boolean moved;
            ManorTeleportRules.Recall recall;
            boolean nearBoundary;
            boolean sameDimension = member.m_9236_().m_46472_().equals(f.dim);
            double distance = sameDimension ? (double)member.m_20270_((Entity)boss) : Double.POSITIVE_INFINITY;
            boolean outsideBoundary = !sameDimension || distance > 76.0;
            boolean bl3 = nearBoundary = sameDimension && !noTarget && distance >= 68.0 && distance <= 76.0;
            if (nearBoundary && f.phase % 20 == 0) {
                ManorFight.showRecallBoundaryWarning(member, distance);
            }
            if ((recall = ManorTeleportRules.recall(sameDimension, member.m_20280_((Entity)boss), member.m_20238_(f.anchor), boss.m_20238_(f.anchor), noTarget)) == ManorTeleportRules.Recall.NONE) {
                if (f.pendingPlayerRecalls.remove(member.m_20148_()) == null) continue;
                member.m_5661_((Component)Component.m_237113_((String)"\u00a7a\u3010\u5e84\u56ed\u3011\u00a77\u5df2\u56de\u5230\u6218\u573a\u8303\u56f4\uff0c\u4f20\u9001\u5012\u8ba1\u65f6\u53d6\u6d88\u3002"), true);
                continue;
            }
            Long last = LAST_RECALL.get(member.m_20148_());
            if (last != null && now - last < 100L) continue;
            if (recall == ManorTeleportRules.Recall.PLAYER_TO_BOSS) {
                Integer pending = f.pendingPlayerRecalls.get(member.m_20148_());
                if (pending == null) {
                    pending = 100;
                    f.pendingPlayerRecalls.put(member.m_20148_(), pending);
                    ManorFight.showRecallCountdown(member, pending, distance, outsideBoundary);
                    continue;
                }
                if (pending > 0) {
                    Integer n = pending;
                    pending = pending - 1;
                    f.pendingPlayerRecalls.put(member.m_20148_(), pending);
                    if (pending <= 0 || pending % 20 != 0) continue;
                    ManorFight.showRecallCountdown(member, pending, distance, outsideBoundary);
                    continue;
                }
            } else {
                f.pendingPlayerRecalls.remove(member.m_20148_());
            }
            boolean bl4 = recall == ManorTeleportRules.Recall.PLAYER_TO_BOSS ? ManorFight.teleportPlayerToBoss(member, boss, sameDimension ? null : sl, f) : (moved = ManorFight.teleportBossTo(boss, member, f));
            if (!moved) {
                if (recall != ManorTeleportRules.Recall.PLAYER_TO_BOSS) continue;
                f.pendingPlayerRecalls.put(member.m_20148_(), 20);
                member.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u3010\u5e84\u56ed\u3011\u00a77\u6682\u65f6\u627e\u4e0d\u5230\u5b89\u5168\u843d\u70b9\uff0c1 \u79d2\u540e\u91cd\u8bd5\u4f20\u9001\u3002"), true);
                continue;
            }
            LAST_RECALL.put(member.m_20148_(), now);
            f.pendingPlayerRecalls.remove(member.m_20148_());
            ManorFight.pruneRecall(now);
            f.noTargetTicks = 0;
            if (recall != ManorTeleportRules.Recall.BOSS_TO_PLAYER) continue;
            return;
        }
    }

    private static void showRecallBoundaryWarning(ServerPlayer player, double distance) {
        player.m_5661_((Component)Component.m_237113_((String)("\u00a7e\u3010\u5e84\u56ed\u3011\u00a77\u6b63\u5728\u63a5\u8fd1\u6218\u573a\u8fb9\u754c\uff08\u8ddd\u79bb Boss \u00a7e" + ManorFight.formatRecallDistance(distance) + "\u00a77 / \u00a7e" + ManorFight.formatRecallDistance(76.0) + "\u00a77 \u683c\uff09\uff0c\u8bf7\u8fd4\u56de\u8303\u56f4\u5185\u3002")), true);
    }

    private static void showRecallCountdown(ServerPlayer player, int ticks, double distance, boolean outsideBoundary) {
        int seconds = Math.max(1, (int)Math.ceil((double)ticks / 20.0));
        String location = Double.isFinite(distance) ? "\u8ddd\u79bb Boss \u00a7e" + ManorFight.formatRecallDistance(distance) + "\u00a77 \u683c" : "\u5df2\u79bb\u5f00 Boss \u6240\u5728\u7ef4\u5ea6";
        String reason = outsideBoundary ? "\u00a7c\u8d85\u51fa\u6218\u573a\u8303\u56f4\uff01" : "\u00a76Boss \u6682\u672a\u9501\u5b9a\u4f60\u3002";
        player.m_5661_((Component)Component.m_237113_((String)("\u00a7c\u00a7l\u3010\u5e84\u56ed\u3011\u00a7r " + reason + "\u00a77" + location + "\uff0c\u5c06\u5728 \u00a7c" + seconds + "\u00a77 \u79d2\u540e\u4f20\u9001\u56de\u53bb\u3002")), true);
    }

    private static String formatRecallDistance(double distance) {
        return Double.isFinite(distance) ? String.format(Locale.ROOT, "%.1f", distance) : "--";
    }

    private static boolean teleportPlayerToBoss(ServerPlayer p, EasterBossEntity boss, ServerLevel crossDimTarget, Fight f) {
        if (!ManorTeleportRules.isParticipant(f.participants, p.m_20148_())) {
            return false;
        }
        if (boss.isQizaiUninterruptibleCasting()) {
            return false;
        }
        ServerLevel level = (ServerLevel)boss.m_9236_();
        Vec3 safe = ManorSafeTeleport.find(level, (Entity)p, boss.m_20182_().m_82520_(3.0, 0.0, 0.0), 8);
        if (safe == null) {
            return false;
        }
        boss.prepareArenaTransfer();
        ManorSafeTeleport.move(level, (Entity)p, safe);
        p.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u00a7l\u3010\u5e84\u56ed\u3011\u5df2\u5c06\u4f60\u4f20\u9001\u56de Boss \u8eab\u8fb9\u3002"), true);
        FTBossMod.LOGGER.info("[FTBoss][Manor] \u73a9\u5bb6\u5b89\u5168\u53ec\u56de {} -> {}", (Object)p.m_7755_().getString(), (Object)safe);
        return true;
    }

    private static boolean teleportBossTo(EasterBossEntity boss, ServerPlayer p, Fight f) {
        if (!ManorTeleportRules.isParticipant(f.participants, p.m_20148_())) {
            return false;
        }
        if (boss.isQizaiUninterruptibleCasting()) {
            return false;
        }
        if (!boss.canTeleportToFightMember(p)) {
            return false;
        }
        ServerLevel level = (ServerLevel)boss.m_9236_();
        Vec3 safe = ManorSafeTeleport.find(level, (Entity)boss, p.m_20182_().m_82520_(3.0, 0.0, 0.0), 8);
        if (safe == null) {
            return false;
        }
        boss.prepareArenaTransfer();
        ManorSafeTeleport.move(level, (Entity)boss, safe);
        FTBossMod.LOGGER.info("[FTBoss][Manor] Boss \u5b89\u5168\u53ec\u56de {} -> {}", (Object)boss.m_20148_(), (Object)safe);
        return true;
    }

    private static int clearStrayManorBosses(ServerLevel sl, Fight f) {
        int removed = 0;
        for (Entity entity : ManorFight.members(sl.m_7654_(), f)) {
            EasterBossEntity boss;
            if (!(entity instanceof EasterBossEntity) || (boss = (EasterBossEntity)entity).isMini() || boss.getVariant() == 9 || f.onStage.contains(boss.m_20148_())) continue;
            entity.m_146870_();
            ++removed;
        }
        return removed;
    }

    static {
        lastPump = -1L;
        lastShredPump = -1L;
    }

    public static boolean isFlightBanned(Player player) {
        if (player == null) {
            return false;
        }
        for (Fight fight : ACTIVE) {
            if (!fight.dim.equals((Object)player.m_9236_().m_46472_()) || !FullFightFlightRules.contains(player.m_20185_(), player.m_20189_(), fight.anchor.f_82479_, fight.anchor.f_82481_, fight.participants.contains(player.m_20148_()))) continue;
            return true;
        }
        return false;
    }

    private static final class Fight {
        final ResourceKey<Level> dim;
        final ServerBossEvent bar;
        UUID summonerId;
        ManorDifficulty difficulty = ManorDifficulty.NORMAL;
        UUID id = UUID.randomUUID();
        final Set<UUID> participants = new HashSet<UUID>();
        Vec3 lastPosition;
        Vec3 stalledPosition;
        int stalledTicks;
        long lastRelocation = Long.MIN_VALUE;
        int voidFalls;
        long lastVoidFall = Long.MIN_VALUE;
        Vec3 anchor;
        final Set<Integer> defeated = new LinkedHashSet<Integer>();
        int currentVariant = -1;
        UUID currentId = null;
        EasterBossEntity current = null;
        int unloadedTicks = 0;
        int noTargetTicks = 0;
        final Map<UUID, Integer> pendingPlayerRecalls = new HashMap<UUID, Integer>();
        int nextDelay = 0;
        int phase = 0;
        final Deque<Integer> pool = new ArrayDeque<Integer>();
        final List<UUID> onStage = new ArrayList<UUID>();

        Fight(ResourceKey<Level> dim, Vec3 anchor, UUID summonerId) {
            this.dim = dim;
            this.summonerId = summonerId;
            if (summonerId != null) {
                this.participants.add(summonerId);
            }
            this.anchor = anchor;
            this.lastPosition = anchor;
            this.bar = new ServerBossEvent((Component)Component.m_237113_((String)ManorFight.progressTitle(0)), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
            this.bar.m_142711_(0.0f);
            BossBarVisibility.visible(this.bar, true);
            this.refillPool(null);
        }

        void refillPool(Integer keepOut) {
            ArrayList<Integer> all = new ArrayList<Integer>();
            for (int i : EasterBossEntity.MANOR_VARIANTS) {
                if (this.defeated.contains(i) || keepOut != null && i == keepOut) continue;
                all.add(i);
            }
            Collections.shuffle(all);
            this.pool.clear();
            this.pool.addAll(all);
        }

        int nextVariant() {
            Integer v;
            if (this.pool.isEmpty()) {
                this.refillPool(null);
            }
            return (v = this.pool.pollFirst()) == null ? 0 : v;
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss")
    public static final class Ticker {
        @SubscribeEvent
        public static void onStopping(ServerStoppingEvent event) {
            if (activeServer != event.getServer()) {
                return;
            }
            ManorFight.persist();
            HiddenBossIntro.cancelAll();
            ACTIVE.forEach(fight -> {
                fight.pendingPlayerRecalls.clear();
                fight.bar.m_7706_();
            });
            ACTIVE.clear();
            ManorArenaChunks.reset();
            LAST_RECALL.clear();
            ManorMercy.reset();
            activeServer = null;
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent e) {
            if (e.phase != TickEvent.Phase.END) {
                return;
            }
            ManorFight.pumpAll();
        }
    }
}

