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
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
        f.bar.setName(Component.literal(progressTitle(done) + f.difficulty.badge()));
        f.bar.setProgress(Math.min(1.0F, (float)done / (float)EasterBossEntity.MANOR_COUNT));
    }

    private static void initialize(MinecraftServer server) {
        if (activeServer != server) {
            ACTIVE.forEach(fightx -> {
                fightx.pendingPlayerRecalls.clear();
                fightx.bar.removeAllPlayers();
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
                    ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(record.getString("Dimension")));
                    if (record.hasUUID("Id") && !ManorFightData.get(server).retired(record.getUUID("Id"))) {
                        ManorFight.Fight fight = new ManorFight.Fight(
                            dim,
                            new Vec3(record.getDouble("X"), record.getDouble("Y"), record.getDouble("Z")),
                            record.hasUUID("Summoner") ? record.getUUID("Summoner") : null
                        );
                        fight.id = record.getUUID("Id");
                        fight.difficulty = ManorDifficulty.fromId(record.getString("Difficulty"));
                        fight.currentId = record.hasUUID("Current") ? record.getUUID("Current") : null;
                        if (fight.currentId != null) {
                            fight.onStage.add(fight.currentId);
                        }

                        fight.currentVariant = record.getInt("Variant");
                        fight.lastPosition = new Vec3(record.getDouble("BossX"), record.getDouble("BossY"), record.getDouble("BossZ"));

                        for (int variant : record.getIntArray("Defeated")) {
                            if (isManorVariant(variant)) {
                                fight.defeated.add(variant);
                            }
                        }

                        for (Tag player : record.getList("Participants", 10)) {
                            CompoundTag value = (CompoundTag)player;
                            if (value.hasUUID("Id")) {
                                fight.participants.add(value.getUUID("Id"));
                            }
                        }

                        fight.refillPool(fight.currentVariant);
                        fight.nextDelay = fight.defeated.contains(fight.currentVariant) ? 60 : 0;
                        ACTIVE.add(fight);
                        updateBar(fight);
                        FTBossMod.LOGGER.info("[FTBoss][Manor] 恢复战场 {}，已击败 {}/{}", fight.id, fight.defeated.size(), EasterBossEntity.MANOR_COUNT);
                    }
                } catch (RuntimeException var10) {
                    FTBossMod.LOGGER.warn("[FTBoss][Manor] 忽略损坏的战场记录", (Throwable)var10);
                }
            }

            for (ManorFight.Fight fight : ACTIVE) {
                ServerLevel restored = server.getLevel(fight.dim);
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
                record.putUUID("Id", fight.id);
                record.putString("Difficulty", fight.difficulty.id());
                record.putString("Dimension", fight.dim.location().toString());
                if (fight.summonerId != null) {
                    record.putUUID("Summoner", fight.summonerId);
                }

                if (fight.currentId != null) {
                    record.putUUID("Current", fight.currentId);
                }

                record.putInt("Variant", fight.currentVariant);
                record.putDouble("X", fight.anchor.x);
                record.putDouble("Y", fight.anchor.y);
                record.putDouble("Z", fight.anchor.z);
                record.putDouble("BossX", fight.lastPosition.x);
                record.putDouble("BossY", fight.lastPosition.y);
                record.putDouble("BossZ", fight.lastPosition.z);
                record.putIntArray("Defeated", fight.defeated.stream().mapToInt(Integer::intValue).toArray());
                ListTag players = new ListTag();

                for (UUID player : fight.participants) {
                    CompoundTag value = new CompoundTag();
                    value.putUUID("Id", player);
                    players.add(value);
                }

                record.put("Participants", players);
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
                    && fight.dim.equals(player.level().dimension())
                    && (fight.participants.contains(player.getUUID()) || player.distanceToSqr(fight.anchor.x, fight.anchor.y, fight.anchor.z) <= 9216.0)) {
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
        initialize(level.getServer());
        return ACTIVE.stream().anyMatch(fight -> fight.dim.equals(level.dimension()));
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
        return fight != null && ManorCombatRules.canSurrender(fight.summonerId, player.getUUID(), fight.id, id);
    }

    public static boolean surrender(ServerPlayer player, UUID id) {
        if (!canSurrender(player, id)) {
            return false;
        } else {
            ManorFight.Fight fight = find(id);
            ServerLevel level = player.server.getLevel(fight.dim);
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
            initialize(player.server);
            ManorFight.Fight fight = find(attributedFight);
            if (fight != null && !fight.dim.equals(player.level().dimension())) {
                fight = null;
            }

            if (fight == null) {
                for (ManorFight.Fight candidate : ACTIVE) {
                    if (candidate.dim.equals(player.level().dimension()) && candidate.participants.contains(player.getUUID())) {
                        fight = candidate;
                        break;
                    }
                }
            }

            if (fight == null) {
                return HiddenBossIntro.stopForHardcore(player);
            } else {
                ServerLevel level = player.server.getLevel(fight.dim);
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
        if (player != null && player.isCreative()) {
            ServerLevel level = player.serverLevel();
            initialize(level.getServer());
            boolean stopped = HiddenBossChallenge.cancelForCreative(level);

            for (ManorFight.Fight fight : new ArrayList<>(ACTIVE)) {
                if (fight.dim.equals(level.dimension())) {
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
        if (fight != null && fight.participants.add(player.getUUID())) {
            persist();
        }
    }

    public static void attackedBy(EasterBossEntity member, ServerPlayer player) {
        ManorFight.Fight fight = find(fightId(member));
        if (fight != null) {
            if (fight.summonerId == null) {
                fight.summonerId = player.getUUID();

                for (Entity entity : members(player.server, fight)) {
                    if (entity instanceof EasterBossEntity boss) {
                        boss.setManorSummonerId(fight.summonerId);
                    }
                }

                persist();
            }

            if (fight.participants.add(player.getUUID())) {
                persist();
            }
        }
    }

    private static List<Entity> members(MinecraftServer server, ManorFight.Fight fight) {
        List<Entity> result = new ArrayList<>();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
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
        if (!HiddenBossIntro.isActing(boss.getUUID())) {
            boss.setNoAi(false);
            boss.setInvulnerable(false);
        }
    }

    private static void endFight(ServerLevel level, ManorFight.Fight fight, boolean victory, String reason) {
        if (!ManorFightData.get(level.getServer()).retired(fight.id)) {
            syncRecallBoundary(level, fight, false);
            ManorTransition.cancel(level, fight.id);
            if (victory) {
                ManorVictory.begin(level, fight.id, fight.anchor, fight.participants);
                if (fight.difficulty == ManorDifficulty.NORMAL) {
                    for (UUID player : fight.participants) {
                        ManorVictoryData.get(level.getServer()).recordConqueror(player);
                    }

                    BossAdvancements.onManorNormalVictory(level, fight.participants);
                } else {
                    BossAdvancements.onManorImpossibleVictory(level, fight.participants);
                }
            }

            ManorFightData.get(level.getServer()).retire(fight.id);
            ImpossibleSparrowSupport.finish(level.getServer(), fight.id);
            ACTIVE.remove(fight);
            if (ACTIVE.stream().noneMatch(other -> other.dim.equals(level.dimension()))) {
                ManorArenaChunks.release(level);
            }

            fight.pendingPlayerRecalls.clear();
            BossBarVisibility.visible(fight.bar, false);
            fight.bar.removeAllPlayers();
            ManorMercy.clearFight(fight.id);

            for (Entity entity : members(level.getServer(), fight)) {
                if (entity instanceof LivingEntity living && entity.level() instanceof ServerLevel memberLevel) {
                    BossMusic.release(memberLevel, living);
                }

                HiddenBossIntro.cancel(entity.getUUID());
                entity.discard();
            }

            for (UUID participant : fight.participants) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
                LAST_RECALL.remove(participant);
                if (player != null) {
                    ArmorShred.clear(player);
                    if (victory) {
                        player.sendSystemMessage(
                            Component.literal(
                                fight.difficulty == ManorDifficulty.IMPOSSIBLE
                                    ? "§4【甜苹果庄园·" + fight.difficulty.title() + "】§f挑战完成。§7本难度不发放庄园成就或通关头衔。"
                                    : "§6【甜苹果庄园】§f普通难度通关！§7" + ManorDifficulty.IMPOSSIBLE.label() + "已解锁，可在下次挑战时选择。"
                            )
                        );
                    } else {
                        player.sendSystemMessage(Component.literal("§6【甜苹果庄园·" + fight.difficulty.title() + "】§7挑战已结束（" + reason + "），不计击败、无掉落。"));
                    }
                }
            }

            SparrowSupport.forgetSparrow();
            BossBars.sweep(level.getServer());
            persist();
            FTBossMod.LOGGER.info("[FTBoss][Manor] 战场 {} 结束：{}，胜利={}", fight.id, reason, victory);
        }
    }

    public static boolean isManaged(LivingEntity le) {
        for (ManorFight.Fight f : ACTIVE) {
            if (f.onStage.contains(le.getUUID())) {
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
                if (f.dim.equals(p.level().dimension()) && p.distanceToSqr(f.anchor.x, f.anchor.y, f.anchor.z) <= 9216.0) {
                    return true;
                }
            }

            return false;
        }
    }

    public static void adopt(ServerLevel sl, EasterBossEntity boss) {
        if (sl != null && boss != null) {
            initialize(sl.getServer());
            if (!HiddenBossIntro.isActing(boss.getUUID()) && SummonGate.isAllowedDim(sl)) {
                UUID previous = ManorFightMembers.fightId(boss);
                if (ManorFightData.get(sl.getServer()).retired(previous)) {
                    boss.discard();
                } else {
                    clear(sl.dimension());
                    ManorFight.Fight f = new ManorFight.Fight(sl.dimension(), boss.position(), boss.getManorSummonerId());
                    f.difficulty = boss.getChallengeDifficulty();
                    if (previous != null) {
                        f.id = previous;
                    }

                    bind(f, boss);
                    f.currentVariant = boss.getVariant();
                    f.currentId = boss.getUUID();
                    f.current = boss;
                    f.onStage.add(boss.getUUID());
                    f.refillPool(boss.getVariant());
                    ACTIVE.add(f);
                    ManorArenaChunks.hold(sl, f.anchor);

                    for (Entity entity : sl.getEntities().getAll()) {
                        if (entity instanceof EasterBossEntity) {
                            EasterBossEntity sparrow = (EasterBossEntity)entity;
                            if (sparrow.getVariant() == 9 && f.summonerId != null && f.summonerId.equals(sparrow.getManorSummonerId())) {
                                bind(f, sparrow);
                            }
                        }
                    }

                    purgeStale(sl, boss.getUUID(), true);
                    SparrowSupport.beginStage(sl, boss);

                    for (ServerPlayer p : sl.players()) {
                        if (p.distanceToSqr(boss) <= 9216.0) {
                            BossBarVisibility.addPlayer(f.bar, p);
                        }
                    }

                    updateBar(f);
                    persist();
                    FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={} 血量={}", EasterBossEntity.NAMES[boss.getVariant()], (int)boss.getMaxHealth());
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
            initialize(sl.getServer());
            if (SummonGate.isAllowedDim(sl)) {
                clear(sl.dimension());
                ManorFight.Fight f = new ManorFight.Fight(sl.dimension(), pos, summonerId);
                f.difficulty = difficulty == null ? ManorDifficulty.NORMAL : difficulty;
                f.refillPool(variant);
                ACTIVE.add(f);
                ManorArenaChunks.hold(sl, f.anchor);

                for (Entity entity : sl.getEntities().getAll()) {
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
                    ServerLevel level = activeServer.getLevel(dim);
                    if (level != null) {
                        endFight(level, fight, false, "战场重置");
                    }
                }
            }
        }
    }

    public static boolean rescue(EasterBossEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return false;
        } else {
            ManorFight.Fight var8 = find(fightId(boss));
            long now = level.getGameTime();
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

            ServerPlayer summoner = boss.getManorSummonerId() == null ? null : level.getServer().getPlayerList().getPlayer(boss.getManorSummonerId());
            Vec3 preferred = boss.canTeleportWith(summoner) ? summoner.position() : (var8 == null ? boss.position() : var8.anchor);
            Vec3 safe = ManorSafeTeleport.find(level, boss, preferred, 16);
            if (safe == null && var8 != null) {
                safe = ManorSafeTeleport.find(level, boss, var8.anchor, 24);
            }

            if (safe != null) {
                boss.prepareArenaTransfer();
                ManorSafeTeleport.move(level, boss, safe);
                FTBossMod.LOGGER.info("[FTBoss][Manor] 坠空救援 {} {} -> {}", boss.getName().getString(), boss.getUUID(), safe);
                return true;
            } else {
                if (var8 != null) {
                    endFight(level, var8, false, "附近没有安全落点");
                } else {
                    boss.discard();
                    FTBossMod.LOGGER.warn("[FTBoss][Manor] 无主 Boss {} 无安全落点，取消而非计为击败", boss.getUUID());
                }

                return false;
            }
        }
    }

    private static boolean relocate(ServerLevel level, ManorFight.Fight fight, EasterBossEntity trigger) {
        fight.lastRelocation = level.getGameTime();
        List<Entity> entities = members(level.getServer(), fight);
        ServerPlayer summoner = fight.summonerId == null ? null : level.getServer().getPlayerList().getPlayer(fight.summonerId);
        if (summoner != null && summoner.isAlive() && !summoner.isSpectator() && summoner.level() == level) {
            entities.add(summoner);
        }

        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double distance = 52.0 + level.random.nextDouble() * 20.0;
            Vec3 candidate = fight.anchor.add(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
            Vec3 anchor = ManorSafeTeleport.find(level, trigger, candidate, 3);
            if (anchor != null && !(anchor.subtract(fight.anchor).horizontalDistanceSqr() < 2304.0)) {
                Map<Entity, Vec3> destinations = new LinkedHashMap<>();
                boolean safe = true;
                int ordinal = 0;

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity && entity.isAlive() && entity.level() == level) {
                        double offset = (double)(ordinal++) * 2.4;
                        Vec3 preferred = entity == trigger ? anchor : anchor.add(Math.cos(offset) * 4.0, 0.0, Math.sin(offset) * 4.0);
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
                            entityx.discard();
                        }
                    }

                    if (fight.current != null) {
                        fight.lastPosition = fight.current.position();
                    }

                    persist();
                    if (summoner != null) {
                        summoner.displayClientMessage(Component.literal("§6【庄园】§7战场已安全搬迁，继续挑战。"), true);
                    }

                    FTBossMod.LOGGER.info("[FTBoss][Manor] 整场安全搬迁 {} -> {}，成员 {}", fight.id, anchor, destinations.size());
                    return true;
                }
            }
        }

        FTBossMod.LOGGER.warn("[FTBoss][Manor] 战场 {} 未找到安全搬迁点，保留原场地", fight.id);
        return false;
    }

    public static Vec3 anchorOf(ServerLevel sl) {
        for (ManorFight.Fight f : ACTIVE) {
            if (f.dim.equals(sl.dimension())) {
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
                if (f.dim.equals(p.level().dimension()) && !(p.distanceToSqr(f.anchor.x, f.anchor.y, f.anchor.z) > 9216.0)) {
                    f.bar.removePlayer(p);
                    BossBarVisibility.addPlayer(f.bar, p);
                    BossBarVisibility.visible(f.bar, true);
                    updateBar(f);
                    FTBossMod.LOGGER
                        .info("[FTBoss][Manor] 玩家 {} 重进存档 -> 已把庄园进度血条重挂上去(进度 {}/{})", p.getName().getString(), f.defeated.size(), EasterBossEntity.MANOR_COUNT);
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

            for (ServerLevel level : server.getAllLevels()) {
                if (SummonGate.isAllowedDim(level)) {
                    boolean active = ACTIVE.stream().anyMatch(fight -> fight.dim.equals(level.dimension()));
                    if (!active) {
                        boolean adopted = false;

                        for (ServerPlayer player : level.players()) {
                            for (EasterBossEntity boss : level.getEntitiesOfClass(EasterBossEntity.class, player.getBoundingBox().inflate(160.0))) {
                                if (boss.isAlive()
                                    && !boss.isRemoved()
                                    && !boss.isMini()
                                    && isManorVariant(boss.getVariant())
                                    && !HiddenBossIntro.isActing(boss.getUUID())) {
                                    UUID previous = ManorFightMembers.fightId(boss);
                                    if (!ManorFightData.get(server).retired(previous)) {
                                        adopt(level, boss);
                                        FTBossMod.LOGGER.info("[FTBoss][Manor] 收编无托管 Boss {}；召唤者={}", boss.getUUID(), boss.getManorSummonerId());
                                        adopted = true;
                                        break;
                                    }

                                    boss.discard();
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

            for (Entity entity : sl.getEntities().getAll()) {
                if (ManorFightData.get(sl.getServer()).retired(ManorFightMembers.fightId(entity))) {
                    stale.add(entity);
                }
            }

            stale.forEach(Entity::discard);
            return stale.size();
        }
    }

    @Deprecated
    public static boolean onDamage(ServerLevel level, EasterBossEntity boss, float amount, DamageSource source) {
        return false;
    }

    private static void spawnVariant(ServerLevel sl, ManorFight.Fight f, int variant) {
        EasterBossEntity e = (EasterBossEntity)((EntityType)FTEntities.EASTER_BOSS.get()).create(sl);
        if (e == null) {
            endFight(sl, f, false, "无法生成下一位 Boss");
        } else {
            double yaw = (double)((float)Math.toDegrees(Math.atan2(f.anchor.z - f.anchor.z, 1.0)));
            e.moveTo(f.anchor.x, f.anchor.y, f.anchor.z, (float)yaw, 0.0F);
            e.setVariant(variant);
            e.setManorSummonerId(f.summonerId);
            bind(f, e);
            ((ManorEntryBuffAccess)e).prepareManorEntryBuffs();
            e.setNoAi(false);
            if (!sl.addFreshEntity(e)) {
                endFight(sl, f, false, "下一位 Boss 生成被取消");
            } else {
                f.onStage.add(e.getUUID());
                f.currentVariant = variant;
                f.currentId = e.getUUID();
                f.current = e;
                f.nextDelay = 0;
                f.unloadedTicks = 0;
                f.lastPosition = e.position();
                syncRecallBoundary(sl, f, true);
                SparrowSupport.beginStage(sl, e);
                persist();
                ManorTransition.arrive(sl, f.id, e.position(), variant);
                sl.playSound(null, f.anchor.x, f.anchor.y, f.anchor.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4F, 0.7F);
                FTBossMod.LOGGER
                    .info(
                        "[FTBoss][Manor] 上场: {} (独立血量 {}, 已击败 {}/{})",
                        EasterBossEntity.NAMES[variant],
                        (int)e.getMaxHealth(),
                        f.defeated.size(),
                        EasterBossEntity.MANOR_COUNT
                    );

                for (ServerPlayer p : sl.players()) {
                    if (p.distanceToSqr(f.anchor.x, f.anchor.y, f.anchor.z) <= 9216.0) {
                        p.sendSystemMessage(
                            Component.literal(
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
            FTBossMod.LOGGER.info("[FTBoss][Manor] 已击败 {} ({}/{})", EasterBossEntity.NAMES[f.currentVariant], f.defeated.size(), EasterBossEntity.MANOR_COUNT);

            for (ServerPlayer p : sl.players()) {
                if (p.distanceToSqr(f.anchor) <= 9216.0) {
                    p.sendSystemMessage(
                        Component.literal(
                            "§6【甜苹果庄园】§7"
                                + EasterBossEntity.NAMES[f.currentVariant]
                                + " §7已被击败！§8(进度 "
                                + f.defeated.size()
                                + "/"
                                + EasterBossEntity.MANOR_COUNT
                                + ")"
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
            long now = server.overworld().getGameTime();
            if (now != lastShredPump) {
                lastShredPump = now;
                ArmorShred.tick(server);
            }

            if (!ACTIVE.isEmpty()) {
                if (now != lastPump) {
                    lastPump = now;
                    ManorArenaChunks.beginFrame();

                    for (ManorFight.Fight f : ACTIVE) {
                        ServerLevel sl = server.getLevel(f.dim);
                        if (sl != null) {
                            f.phase++;
                            if (f.currentVariant >= 0) {
                                EasterBossEntity cur = f.current;
                                if (f.currentId != null
                                    && sl.getEntity(f.currentId) == null
                                    && !f.defeated.contains(f.currentVariant)
                                    && f.unloadedTicks % 100 == 20) {
                                    sl.getChunk(BlockPos.containing(f.lastPosition));
                                }

                                if (f.currentId != null && sl.getEntity(f.currentId) instanceof EasterBossEntity le && le != cur) {
                                    cur = le;
                                    f.current = le;
                                    bind(f, le);
                                }

                                boolean dead = f.defeated.contains(f.currentVariant) || cur != null && (cur.isDeadOrDying() || cur.getHealth() <= 0.0F);
                                boolean inWorld = cur != null && sl.getEntity(cur.getUUID()) == cur;
                                if (inWorld && !dead) {
                                    f.lastPosition = cur.position();
                                    if (f.phase % 20 == 0 && !SparrowSupport.hasVisited(cur)) {
                                        SparrowSupport.beginStage(sl, cur);
                                    }
                                }

                                if (!dead && !inWorld) {
                                    f.unloadedTicks++;
                                    if (f.unloadedTicks == 601) {
                                        FTBossMod.LOGGER
                                            .warn(
                                                "[FTBoss][Manor] {} 连续 {} tick 不在世界里(区块一直没加载) -> 取消本场，不计击败",
                                                EasterBossEntity.NAMES[f.currentVariant],
                                                f.unloadedTicks
                                            );
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

                                for (ServerPlayer stale : new ArrayList<>(f.bar.getPlayers())) {
                                    if (stale.hasDisconnected() || stale.connection == null) {
                                        f.bar.removePlayer(stale);
                                    }
                                }

                                for (ServerPlayer p : sl.players()) {
                                    if (!p.hasDisconnected() && p.connection != null) {
                                        boolean near = p.distanceToSqr(f.anchor) <= 9216.0;
                                        if (near && !f.bar.getPlayers().contains(p)) {
                                            BossBarVisibility.addPlayer(f.bar, p);
                                        } else if (!near && f.bar.getPlayers().contains(p)) {
                                            f.bar.removePlayer(p);
                                        }
                                    }
                                }

                                for (UUID id : f.onStage) {
                                    Entity ent = sl.getEntity(id);
                                    if (ent instanceof EasterBossEntity) {
                                        EasterBossEntity eb = (EasterBossEntity)ent;
                                        if (eb.isAlive()) {
                                            if (f.phase % 10 == 0) {
                                                sl.sendParticles(ParticleTypes.CRIT, eb.getX(), eb.getY() + 1.0, eb.getZ(), 2, 0.3, 0.5, 0.3, 0.02);
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
            && boss.isAlive()
            && !boss.isRemoved()
            && boss.level() == level
            && !boss.isXuanlanSkyCasting()
            && !boss.isQizaiUninterruptibleCasting();
        ManorRecallBoundaryPacket packet = active
            ? new ManorRecallBoundaryPacket(
                fight.id,
                level.dimension().location(),
                boss.getId(),
                boss.getUUID(),
                76.0F,
                Math.max(0.0F, Math.min(1.0F, (float)fight.noTargetTicks / 200.0F)),
                true
            )
            : ManorRecallBoundaryPacket.stop(fight.id, level.dimension().location());

        for (UUID participant : fight.participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player != null) {
                ManorRecallBoundaryPacket.send(player, packet);
            }
        }
    }

    private static void recallStrayPlayers(ServerLevel sl, ManorFight.Fight f) {
        EasterBossEntity boss = f.current;
        if (boss != null && !boss.isRemoved() && boss.isAlive()) {
            if (!boss.isXuanlanSkyCasting() && !boss.isQizaiUninterruptibleCasting()) {
                MinecraftServer server = sl.getServer();
                if (server != null) {
                    ArrayList<ServerPlayer> members = new ArrayList<>();

                    for (UUID id : f.participants) {
                        ServerPlayer member = server.getPlayerList().getPlayer(id);
                        if (member != null && member.isAlive() && !member.isSpectator()) {
                            members.add(member);
                        }
                    }

                    if (members.isEmpty()) {
                        f.noTargetTicks = 0;
                    } else {
                        long now;
                        boolean var10000;
                        label182: {
                            now = sl.getGameTime();
                            if (boss.getTarget() instanceof ServerPlayer locked && ManorTeleportRules.isParticipant(f.participants, locked.getUUID())) {
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
                            if (member.distanceToSqr(boss) < reference.distanceToSqr(boss)) {
                                reference = member;
                            }
                        }

                        boolean obstructed = reference.level() == sl
                            && !boss.isPerformingSkill()
                            && (!sl.noCollision(boss) || lockedOnMember && !boss.hasLineOfSight(reference));
                        if (obstructed && f.stalledPosition != null && !(boss.distanceToSqr(f.stalledPosition) > 1.0)) {
                            f.stalledTicks++;
                        } else {
                            f.stalledTicks = 0;
                            f.stalledPosition = boss.position();
                        }

                        if (f.stalledTicks >= 200) {
                            if (teleportBossTo(boss, reference, f)) {
                                LAST_RECALL.put(reference.getUUID(), now);
                                f.stalledTicks = 0;
                                f.stalledPosition = boss.position();
                                FTBossMod.LOGGER.info("[FTBoss][Manor] {} 长时间被障碍阻挡，安全脱困", boss.getUUID());
                            }
                        } else {
                            for (ServerPlayer memberx : members) {
                                boolean sameDimension = memberx.level().dimension().equals(f.dim);
                                double distance = sameDimension ? (double)memberx.distanceTo(boss) : Double.POSITIVE_INFINITY;
                                boolean outsideBoundary = !sameDimension || distance > 76.0;
                                boolean nearBoundary = sameDimension && !noTarget && distance >= 68.0 && distance <= 76.0;
                                if (nearBoundary && f.phase % 20 == 0) {
                                    showRecallBoundaryWarning(memberx, distance);
                                }

                                Recall recall = ManorTeleportRules.recall(
                                    sameDimension, memberx.distanceToSqr(boss), memberx.distanceToSqr(f.anchor), boss.distanceToSqr(f.anchor), noTarget
                                );
                                if (recall == Recall.NONE) {
                                    if (f.pendingPlayerRecalls.remove(memberx.getUUID()) != null) {
                                        memberx.displayClientMessage(Component.literal("§a【庄园】§7已回到战场范围，传送倒计时取消。"), true);
                                    }
                                } else {
                                    Long last = LAST_RECALL.get(memberx.getUUID());
                                    if (last == null || now - last >= 100L) {
                                        if (recall == Recall.PLAYER_TO_BOSS) {
                                            Integer pending = f.pendingPlayerRecalls.get(memberx.getUUID());
                                            if (pending == null) {
                                                pending = 100;
                                                f.pendingPlayerRecalls.put(memberx.getUUID(), pending);
                                                showRecallCountdown(memberx, pending, distance, outsideBoundary);
                                                continue;
                                            }

                                            if (pending > 0) {
                                                pending = pending - 1;
                                                f.pendingPlayerRecalls.put(memberx.getUUID(), pending);
                                                if (pending > 0 && pending % 20 == 0) {
                                                    showRecallCountdown(memberx, pending, distance, outsideBoundary);
                                                }
                                                continue;
                                            }
                                        } else {
                                            f.pendingPlayerRecalls.remove(memberx.getUUID());
                                        }

                                        boolean moved = recall == Recall.PLAYER_TO_BOSS
                                            ? teleportPlayerToBoss(memberx, boss, sameDimension ? null : sl, f)
                                            : teleportBossTo(boss, memberx, f);
                                        if (!moved) {
                                            if (recall == Recall.PLAYER_TO_BOSS) {
                                                f.pendingPlayerRecalls.put(memberx.getUUID(), 20);
                                                memberx.displayClientMessage(Component.literal("§c【庄园】§7暂时找不到安全落点，1 秒后重试传送。"), true);
                                            }
                                        } else {
                                            LAST_RECALL.put(memberx.getUUID(), now);
                                            f.pendingPlayerRecalls.remove(memberx.getUUID());
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
        player.displayClientMessage(
            Component.literal("§e【庄园】§7正在接近战场边界（距离 Boss §e" + formatRecallDistance(distance) + "§7 / §e" + formatRecallDistance(76.0) + "§7 格），请返回范围内。"), true
        );
    }

    private static void showRecallCountdown(ServerPlayer player, int ticks, double distance, boolean outsideBoundary) {
        int seconds = Math.max(1, (int)Math.ceil((double)ticks / 20.0));
        String location = Double.isFinite(distance) ? "距离 Boss §e" + formatRecallDistance(distance) + "§7 格" : "已离开 Boss 所在维度";
        String reason = outsideBoundary ? "§c超出战场范围！" : "§6Boss 暂未锁定你。";
        player.displayClientMessage(Component.literal("§c§l【庄园】§r " + reason + "§7" + location + "，将在 §c" + seconds + "§7 秒后传送回去。"), true);
    }

    private static String formatRecallDistance(double distance) {
        return Double.isFinite(distance) ? String.format(Locale.ROOT, "%.1f", distance) : "--";
    }

    private static boolean teleportPlayerToBoss(ServerPlayer p, EasterBossEntity boss, ServerLevel crossDimTarget, ManorFight.Fight f) {
        if (!ManorTeleportRules.isParticipant(f.participants, p.getUUID())) {
            return false;
        } else if (boss.isQizaiUninterruptibleCasting()) {
            return false;
        } else {
            ServerLevel level = (ServerLevel)boss.level();
            Vec3 safe = ManorSafeTeleport.find(level, p, boss.position().add(3.0, 0.0, 0.0), 8);
            if (safe == null) {
                return false;
            } else {
                boss.prepareArenaTransfer();
                ManorSafeTeleport.move(level, p, safe);
                p.displayClientMessage(Component.literal("§c§l【庄园】已将你传送回 Boss 身边。"), true);
                FTBossMod.LOGGER.info("[FTBoss][Manor] 玩家安全召回 {} -> {}", p.getName().getString(), safe);
                return true;
            }
        }
    }

    private static boolean teleportBossTo(EasterBossEntity boss, ServerPlayer p, ManorFight.Fight f) {
        if (!ManorTeleportRules.isParticipant(f.participants, p.getUUID())) {
            return false;
        } else if (boss.isQizaiUninterruptibleCasting()) {
            return false;
        } else if (!boss.canTeleportToFightMember(p)) {
            return false;
        } else {
            ServerLevel level = (ServerLevel)boss.level();
            Vec3 safe = ManorSafeTeleport.find(level, boss, p.position().add(3.0, 0.0, 0.0), 8);
            if (safe == null) {
                return false;
            } else {
                boss.prepareArenaTransfer();
                ManorSafeTeleport.move(level, boss, safe);
                FTBossMod.LOGGER.info("[FTBoss][Manor] Boss 安全召回 {} -> {}", boss.getUUID(), safe);
                return true;
            }
        }
    }

    private static int clearStrayManorBosses(ServerLevel sl, ManorFight.Fight f) {
        int removed = 0;

        for (Entity entity : members(sl.getServer(), f)) {
            if (entity instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)entity;
                if (!boss.isMini() && boss.getVariant() != 9 && !f.onStage.contains(boss.getUUID())) {
                    entity.discard();
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

        Fight(ResourceKey<Level> dim, Vec3 anchor, UUID summonerId) {
            this.dim = dim;
            this.summonerId = summonerId;
            if (summonerId != null) {
                this.participants.add(summonerId);
            }

            this.anchor = anchor;
            this.lastPosition = anchor;
            this.bar = new ServerBossEvent(Component.literal(ManorFight.progressTitle(0)), BossBarColor.RED, BossBarOverlay.NOTCHED_10);
            this.bar.setProgress(0.0F);
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

    @EventBusSubscriber(
        modid = "ftboss"
    )
    public static final class Ticker {
        @SubscribeEvent
        public static void onStopping(ServerStoppingEvent event) {
            if (ManorFight.activeServer == event.getServer()) {
                ManorFight.persist();
                HiddenBossIntro.cancelAll();
                ManorFight.ACTIVE.forEach(fight -> {
                    fight.pendingPlayerRecalls.clear();
                    fight.bar.removeAllPlayers();
                });
                ManorFight.ACTIVE.clear();
                ManorArenaChunks.reset();
                ManorFight.LAST_RECALL.clear();
                ManorMercy.reset();
                ManorFight.activeServer = null;
            }
        }

        @SubscribeEvent
        public static void onServerTick(Post event) {
            ManorFight.pumpAll();
        }
    }
}
