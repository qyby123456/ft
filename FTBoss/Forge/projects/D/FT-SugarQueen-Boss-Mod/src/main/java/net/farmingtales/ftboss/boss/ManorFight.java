package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.skill.ManorRecallBoundaryPacket;
import net.farmingtales.ftboss.util.ManorTeleportRules;
import net.farmingtales.ftboss.util.ManorCombatRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 甜苹果庄园战场：独立血条、随机轮换、召回边界与胜利结算。
 * 全部主战 Boss 被击败后直接结算，不进入额外战斗阶段。
 */
public class ManorFight {

    /** 一位被打死之后, 隔多久上下一位(tick) */
    public static final int NEXT_DELAY = 60;

    /**
     * v70: 当前这位**不在世界里**多久才认定它真的没了(30 秒)。
     *
     * <p>区块卸载时实体是 {@code setRemoved(RemovalReason.UNLOADED_TO_CHUNK)}, {@code isRemoved()} 同样为 true;
     * 玩家死亡/跑远时区块会卸载, 若把它当"已击败"就会**进度凭空 +1**, 而玩家回来时它还在(见 23:47 日志)。
     * 所以查不到实体只累计 tick, 超过这个宽限期才当已击败(并告警)。
     */
    public static final int UNLOAD_GRACE_TICKS = 30 * 20;
    /** 召回冷却(同一玩家 5 秒最多被拉一次) */
    public static final int RECALL_CD = 5 * 20;
    /** 六区块(96 格)内继续显示庄园血条/重挂资格；真正的召回边界是 76 格。 */
    private static final double SIX_CHUNKS = 6.0 * 16.0;

    /* ==================== 2026-09-13 v94 按用户要求重写「离得太远就拉回来」 ====================
       用户原话:「玩家离 boss 过远时的传送玩家有点失效, 当玩家离 boss 超过 76 格时,
       或 boss 未索敌到玩家 10 秒, 则传送玩家到 boss 身边或传送 boss 过来, 取决于谁离场地更近」。

       旧实现的两个毛病:
         ① 触发条件看的是**玩家离场地(锚点)** 的 128 格, 不是"玩家离 Boss" —— 玩家站在场地边上、
            Boss 在场地另一头(相距 40 格)时, 谁都不触发; 反过来玩家离锚点 130 格但 Boss 就在旁边
            也会被硬拉。② 只会拉玩家, Boss 被钓出场地时靠 v60 那条"离锚点 64 格"兜底,
            于是在场地边缘来回拔河。
       新规则:
         · 触发: 玩家离**在场上那位 Boss** > {@value #STRAY_FROM_BOSS} 格, 或者那位 Boss
           连续 {@value #NO_TARGET_RECALL_TICKS} tick 没锁定到**玩家**;
         · 动作: 拿两个人的"离场地距离"比一比, **离场地更远的那个挪到另一个身边** ——
           玩家跑出去了 -> 把玩家拽到 Boss 身边(原味「哼，想逃？」);
           Boss 被拖出场地 -> 把 Boss 拉回玩家身边。
       这样"场地"始终是锚, 谁跑偏谁回来, 不会出现"Boss 被一路钓走"。 */
    /** 玩家离在场上那位 Boss 超过这个距离(格)就触发召回 */
    public static final double STRAY_FROM_BOSS = ManorTeleportRules.STRAY_FROM_BOSS;
    /** 在场上那位 Boss 连续这么多 tick 没锁定到玩家 -> 触发召回 */
    public static final int NO_TARGET_RECALL_TICKS = 10 * 20;
    /** 庄园进度血条的显示半径：六区块，不作为传送授权依据。 */
    public static final double ATTACH_RANGE = SIX_CHUNKS;

    /** 上次召回时间(按玩家存) —— v112: 写入时顺手淘汰过期项(以前只增不减) */
    private static final java.util.Map<UUID, Long> LAST_RECALL = new java.util.concurrent.ConcurrentHashMap<>();
    /** 召回记忆最多留这么多玩家 */
    private static final int RECALL_CAP = 64;

    private static void pruneRecall(long now) {
        if (LAST_RECALL.size() <= RECALL_CAP) return;
        LAST_RECALL.entrySet().removeIf(en -> now - en.getValue() > 6000L);
        if (LAST_RECALL.size() > RECALL_CAP) LAST_RECALL.clear();
    }

    /** 血条标题 */
    public static final String BAR_NAME = "甜苹果庄园";

    private static final List<Fight> ACTIVE = new CopyOnWriteArrayList<>();
    private static MinecraftServer activeServer;
    private static final int RELOCATION_COOLDOWN = 10 * 20;

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
        Vec3 anchor;   // v148: 允许搬迁战场(原来是 final)
        /** v51: 已经击败的变体(进度条 = 这个集合的大小) */
        final java.util.Set<Integer> defeated = new java.util.LinkedHashSet<>();
        /** v51: 当前上场的那位(变体 + 实体 id) */
        int currentVariant = -1;
        UUID currentId = null;
        /**
         * v53: **当前这位的实体引用**(不能只靠 currentId + getEntity() 判断死活) ——
         * 玩家跑远、区块被卸载时 {@code sl.getEntity(uuid)} 会返回 null, 于是被误判成"已击败"
         * (用户反馈的"超出加载距离导致判断 boss 死亡")。手里捏着引用就不会误判:
         * 区块没加载时这个对象仍然 isAlive(), 只有真死了/真被移除才算数。
         */
        EasterBossEntity current = null;
        /** v53: 连续"查不到实体但引用还活着"的 tick 数(用于判断被卸载) */
        int unloadedTicks = 0;
        /** v94: 在场上那位连续多少 tick 没锁定到**玩家**(满 {@link #NO_TARGET_RECALL_TICKS} 触发召回) */
        int noTargetTicks = 0;
        /** 玩家越过召回边界后的倒计时；不持久化，重载后重新给出完整提示。 */
        final Map<UUID, Integer> pendingPlayerRecalls = new HashMap<>();
        /** v51: 死后还要等多少 tick 才上下一位(0 = 可以上) */
        int nextDelay = 0;
        int phase = 0;
        /** 还没上过场的变体(打乱顺序, 抽完为止 —— 不会再出现重复) */
        final Deque<Integer> pool = new ArrayDeque<>();
        /** 当前在场的主战 Boss。 */
        final List<UUID> onStage = new ArrayList<>();

        Fight(ResourceKey<Level> dim, Vec3 anchor, UUID summonerId) {
            this.dim = dim;
            this.summonerId = summonerId;
            if (summonerId != null) this.participants.add(summonerId);
            this.anchor = anchor;
            this.lastPosition = anchor;
            /* v51: 大血条改成"已击败 n/9"的进度条 */
            this.bar = new ServerBossEvent(Component.literal(progressTitle(0)),
                    BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
            this.bar.setProgress(0.0f);
            this.bar.setVisible(true);
            refillPool(null);
        }

        void refillPool(Integer keepOut) {
            List<Integer> all = new ArrayList<>();
            /* 2026-09-12 v30: 麻雀(支援位)不进轮换
               2026-09-13 v76: 名单改成显式的 {@link EasterBossEntity#MANOR_VARIANTS}
               (加入雪黎后 0~8 九人 + 10 雪黎, 中间跳过 9 麻雀, 不再是连续区间) */
            for (int i : EasterBossEntity.MANOR_VARIANTS) {
                if (defeated.contains(i)) continue;                 // v51: 打过的不会再上
                if (keepOut != null && i == keepOut.intValue()) continue;
                all.add(i);
            }
            Collections.shuffle(all);
            pool.clear();
            pool.addAll(all);
        }

        int nextVariant() {
            if (pool.isEmpty()) refillPool(null);                    // 兜底(正常不会空)
            Integer v = pool.pollFirst();
            return v == null ? 0 : v;
        }
    }

    /** v51: 进度条标题 */
    private static String progressTitle(int done) {
        return "§c§l" + BAR_NAME + " §7已击败 §c" + done + "§7/§c" + EasterBossEntity.MANOR_COUNT;
    }

    /** v51: 刷新进度条(已击败 n/9) */
    private static void updateBar(Fight f) {
        int done = f.defeated.size();
        f.bar.setName(Component.literal(progressTitle(done)
                + f.difficulty.badge()));
        f.bar.setProgress(Math.min(1.0f, done / (float) EasterBossEntity.MANOR_COUNT));
    }

    /* ---------------- 对外接口 ---------------- */

    private static void initialize(MinecraftServer server) {
        if (activeServer == server) return;
        ACTIVE.forEach(fight -> {
            fight.pendingPlayerRecalls.clear();
            fight.bar.removeAllPlayers();
        });
        ACTIVE.clear();
        /* ★ 2026-09-24：换了服务器实例 —— 旧的 ServerLevel 连同票据一起没了，只清记账。 */
        net.farmingtales.ftboss.boss.ai.ManorArenaChunks.reset();
        LAST_RECALL.clear();
        ManorMercy.reset();
        lastPump = -1L;
        lastShredPump = -1L;
        activeServer = server;
        ManorMercy.restore(ManorFightData.get(server).deaths());
        for (Tag entry : ManorFightData.get(server).active()) {
            CompoundTag record = (CompoundTag) entry;
            try {
                var dim = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(record.getString("Dimension")));
                if (!record.hasUUID("Id") || ManorFightData.get(server).retired(record.getUUID("Id"))) continue;
                Fight fight = new Fight(dim, new Vec3(record.getDouble("X"), record.getDouble("Y"), record.getDouble("Z")),
                        record.hasUUID("Summoner") ? record.getUUID("Summoner") : null);
                fight.id = record.getUUID("Id");
                fight.difficulty = ManorDifficulty.fromId(record.getString("Difficulty"));
                fight.currentId = record.hasUUID("Current") ? record.getUUID("Current") : null;
                if (fight.currentId != null) fight.onStage.add(fight.currentId);
                fight.currentVariant = record.getInt("Variant");
                fight.lastPosition = new Vec3(record.getDouble("BossX"), record.getDouble("BossY"), record.getDouble("BossZ"));
                for (int variant : record.getIntArray("Defeated")) if (isManorVariant(variant)) fight.defeated.add(variant);
                for (Tag player : record.getList("Participants", Tag.TAG_COMPOUND)) {
                    CompoundTag value = (CompoundTag) player;
                    if (value.hasUUID("Id")) fight.participants.add(value.getUUID("Id"));
                }
                fight.refillPool(fight.currentVariant);
                fight.nextDelay = fight.defeated.contains(fight.currentVariant) ? NEXT_DELAY : 0;
                ACTIVE.add(fight);
                updateBar(fight);
                FTBossMod.LOGGER.info("[FTBoss][Manor] 恢复战场 {}，已击败 {}/{}", fight.id, fight.defeated.size(), EasterBossEntity.MANOR_COUNT);
            } catch (RuntimeException failure) {
                FTBossMod.LOGGER.warn("[FTBoss][Manor] 忽略损坏的战场记录", failure);
            }
        }
        /* ★ 2026-09-24：从存档恢复的战场同样要按住场地区块 ——
           否则刚进游戏玩家还在别处时，战场就又走"查不到实体 → 宽限期 → 整场结束"那条路。 */
        for (Fight fight : ACTIVE) {
            ServerLevel restored = server.getLevel(fight.dim);
            if (restored != null) {
                net.farmingtales.ftboss.boss.ai.ManorArenaChunks.hold(restored, fight.anchor);
            }
        }
    }

    private static void persist() {
        if (activeServer == null) return;
        ListTag records = new ListTag();
        for (Fight fight : ACTIVE) {
            CompoundTag record = new CompoundTag();
            record.putUUID("Id", fight.id);
            record.putString("Difficulty", fight.difficulty.id());
            record.putString("Dimension", fight.dim.location().toString());
            if (fight.summonerId != null) record.putUUID("Summoner", fight.summonerId);
            if (fight.currentId != null) record.putUUID("Current", fight.currentId);
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

    private static Fight find(UUID id) {
        if (id == null) return null;
        for (Fight fight : ACTIVE) if (fight.id.equals(id)) return fight;
        return null;
    }

    public static ManorDifficulty difficulty(UUID id) {
        Fight fight = find(id);
        return fight == null ? ManorDifficulty.NORMAL : fight.difficulty;
    }

    public static boolean isImpossibleFor(Player player) {
        if (player == null) return false;
        for (Fight fight : ACTIVE) {
            if (fight.difficulty == ManorDifficulty.IMPOSSIBLE && fight.dim.equals(player.level().dimension())
                    && (fight.participants.contains(player.getUUID())
                    || player.distanceToSqr(fight.anchor.x, fight.anchor.y, fight.anchor.z) <= ATTACH_RANGE * ATTACH_RANGE)) return true;
        }
        return false;
    }

    public static UUID fightId(Entity member) {
        return ManorFightMembers.fightId(ManorFightMembers.resolve(member));
    }

    /** 当前战场的锚点，供范围约束使用；不属于庄园战场时返回空。 */
    public static Vec3 anchorOf(UUID id) {
        Fight fight = find(id);
        return fight == null ? null : fight.anchor;
    }

    public static UUID currentBoss(UUID id) {
        Fight fight = find(id);
        return fight == null ? null : fight.currentId;
    }

    public static UUID summoner(UUID id) {
        Fight fight = find(id);
        return fight == null ? null : fight.summonerId;
    }

    public static boolean hasActive(ServerLevel level) {
        initialize(level.getServer());
        return ACTIVE.stream().anyMatch(fight -> fight.dim.equals(level.dimension()));
    }

    public static boolean hasActive(UUID id) { return find(id) != null; }

    public static int currentVariant(UUID id) {
        Fight fight = find(id);
        return fight == null ? -1 : fight.currentVariant;
    }

    public static boolean canSurrender(ServerPlayer player, UUID id) {
        Fight fight = find(id);
        return fight != null && ManorCombatRules.canSurrender(fight.summonerId, player.getUUID(), fight.id, id);
    }

    public static boolean surrender(ServerPlayer player, UUID id) {
        if (!canSurrender(player, id)) return false;
        Fight fight = find(id);
        ServerLevel level = player.server.getLevel(fight.dim);
        if (level == null) return false;
        endFight(level, fight, false, "召唤者投降");
        return true;
    }

    /** 极限模式玩家确定死亡时，在死亡结算前无奖励结束其所属庄园战。 */
    public static boolean abortForHardcore(ServerPlayer player, UUID attributedFight) {
        if (player == null) return false;
        initialize(player.server);
        Fight fight = find(attributedFight);
        if (fight != null && !fight.dim.equals(player.level().dimension())) fight = null;
        if (fight == null) {
            for (Fight candidate : ACTIVE) {
                if (candidate.dim.equals(player.level().dimension())
                        && candidate.participants.contains(player.getUUID())) {
                    fight = candidate;
                    break;
                }
            }
        }
        if (fight == null) return HiddenBossIntro.stopForHardcore(player);
        ServerLevel level = player.server.getLevel(fight.dim);
        if (level == null) return false;
        endFight(level, fight, false, "极限模式玩家死亡前中止");
        return true;
    }

    public static boolean stopForCreative(ServerPlayer player) {
        if (player == null || !player.isCreative()) return false;
        ServerLevel level = player.serverLevel();
        initialize(level.getServer());
        boolean stopped = HiddenBossChallenge.cancelForCreative(level);
        for (Fight fight : new ArrayList<>(ACTIVE)) {
            if (!fight.dim.equals(level.dimension())) continue;
            endFight(level, fight, false, "创造道具中止");
            stopped = true;
        }
        return HiddenBossIntro.stopForCreative(level) || stopped;
    }

    public static void participate(Entity member, ServerPlayer player) {
        Fight fight = find(fightId(member));
        if (fight != null && fight.participants.add(player.getUUID())) persist();
    }

    public static void attackedBy(EasterBossEntity member, ServerPlayer player) {
        Fight fight = find(fightId(member));
        if (fight == null) return;
        if (fight.summonerId == null) {
            fight.summonerId = player.getUUID();
            for (Entity entity : members(player.server, fight)) {
                if (entity instanceof EasterBossEntity boss) boss.setManorSummonerId(fight.summonerId);
            }
            persist();
        }
        if (fight.participants.add(player.getUUID())) persist();
    }

    private static List<Entity> members(MinecraftServer server, Fight fight) {
        List<Entity> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (ManorFightMembers.fightId(entity) == null) {
                    Entity owner = ManorFightMembers.resolve(entity);
                    if (owner != entity) ManorFightMembers.inherit(owner, entity);
                }
                if (fight.id.equals(ManorFightMembers.fightId(entity))) result.add(entity);
            }
        }
        return result;
    }

    private static void bind(Fight fight, EasterBossEntity boss) {
        ManorFightMembers.bind(boss, fight.id);
        boss.setChallengeDifficulty(fight.difficulty);
        SparrowSupport.noteFightMember(boss);
        boss.setManorSummonerId(fight.summonerId);
        if (!HiddenBossIntro.isActing(boss.getUUID())) {
            boss.setNoAi(false);
            boss.setInvulnerable(false);
        }
    }

    private static void endFight(ServerLevel level, Fight fight, boolean victory, String reason) {
        if (ManorFightData.get(level.getServer()).retired(fight.id)) return;
        syncRecallBoundary(level, fight, false);
        ManorTransition.cancel(level, fight.id);
        /* ★ v175 按用户要求新增「战胜庄园的成就：回忆的终结」。
           放在这里（而不是 ManorVictory.begin 里）有两个原因：
             ① 这里是**胜利的唯一判定点** —— 上面的 retired() 早退保证一场只结一次，
                而 begin() 里的 known() 是"奖励是否已发放"的口径，与"成就"不是一回事；
             ② 发放要用本场持久化的参战名单 fight.participants（召唤者 + 中途加入者），
                它就在手边，不必再按距离扫世界。 */
        if (victory) {
            ManorVictory.begin(level, fight.id, fight.anchor, fight.participants);
            /* ★ v184 按用户要求「把甜苹果普通难度获得的头衔和终结难度头衔互换一下」。
               互换后：
                 · **普通难度** → 头衔「12号位」（另照旧记征服者 + 「回忆的终结」成就）；
                 · **终结难度** → 头衔「甜苹果庄园园长」；它仍然**不发成就**
                   （用户从未要求改这一点，`ManorDifficulty.WARNING` 的
                    "不会获取成就" 也仍然成立），走只打头衔勾的那条路。 */
            if (fight.difficulty == ManorDifficulty.NORMAL) {
                for (UUID player : fight.participants) ManorVictoryData.get(level.getServer()).recordConqueror(player);
                // 「回忆的终结」成就只在普通难度发（这条口径没变）——
                // 它内部会一并勾上「12号位」头衔。
                BossAdvancements.onManorNormalVictory(level, fight.participants);
            } else {
                // 终结难度：只发「甜苹果庄园园长」头衔，不碰成就。
                BossAdvancements.onManorImpossibleVictory(level, fight.participants);
            }
        }
        ManorFightData.get(level.getServer()).retire(fight.id);
        ImpossibleSparrowSupport.finish(level.getServer(), fight.id);
        ACTIVE.remove(fight);
        /* ★ 2026-09-24：这一维度没有别的庄园战了就把场地区块常加载放掉（恢复自然卸载）。 */
        if (ACTIVE.stream().noneMatch(other -> other.dim.equals(level.dimension()))) {
            net.farmingtales.ftboss.boss.ai.ManorArenaChunks.release(level);
        }
        fight.pendingPlayerRecalls.clear();
        fight.bar.setVisible(false);
        fight.bar.removeAllPlayers();
        ManorMercy.clearFight(fight.id);
        for (Entity entity : members(level.getServer(), fight)) {
            if (entity instanceof LivingEntity living && entity.level() instanceof ServerLevel memberLevel) {
                net.farmingtales.ftboss.sound.BossMusic.release(memberLevel, living);
            }
            HiddenBossIntro.cancel(entity.getUUID());
            entity.discard();
        }
        for (UUID participant : fight.participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            LAST_RECALL.remove(participant);
            if (player == null) continue;
            ArmorShred.clear(player);
            if (victory) {
                player.sendSystemMessage(Component.literal(fight.difficulty == ManorDifficulty.IMPOSSIBLE
                        ? "§4【甜苹果庄园·" + fight.difficulty.title() + "】§f挑战完成。§7本难度不发放庄园成就或通关头衔。"
                        : "§6【甜苹果庄园】§f普通难度通关！§7" + ManorDifficulty.IMPOSSIBLE.label() + "已解锁，可在下次挑战时选择。"));
            } else {
                player.sendSystemMessage(Component.literal("§6【甜苹果庄园·" + fight.difficulty.title()
                        + "】§7挑战已结束（" + reason + "），不计击败、无掉落。"));
            }
        }
        SparrowSupport.forgetSparrow();
        BossBars.sweep(level.getServer());
        persist();
        FTBossMod.LOGGER.info("[FTBoss][Manor] 战场 {} 结束：{}，胜利={}", fight.id, reason, victory);
    }

    /** 该 Boss 是否属于庄园战场(主阶段不管伤害; 终章/最后的 Boss 阶段才接管) */
    public static boolean isManaged(LivingEntity le) {
        for (Fight f : ACTIVE) {
            if (f.onStage.contains(le.getUUID())) return true;
        }
        return false;
    }

    /** 旧终章查询的兼容入口，当前没有对应战斗阶段。 */
    @Deprecated
    public static boolean isFinaleMember(LivingEntity le) {
        return false;
    }

    public static boolean isPlayerFighting(Player p) {
        if (p == null) return false;
        for (Fight f : ACTIVE) {
            if (!f.dim.equals(p.level().dimension())) continue;
            if (p.distanceToSqr(f.anchor.x, f.anchor.y, f.anchor.z) <= ATTACH_RANGE * ATTACH_RANGE) return true;
        }
        return false;
    }

    public static boolean isFlightBanned(Player player) {
        if (player == null) return false;
        for (Fight fight : ACTIVE) {
            if (!fight.dim.equals(player.level().dimension())) continue;
            if (net.farmingtales.ftboss.util.FullFightFlightRules.contains(player.getX(), player.getZ(),
                    fight.anchor.x, fight.anchor.z, fight.participants.contains(player.getUUID()))) return true;
        }
        return false;
    }

    /** 由入场演出接管: 演出那位就是第一位上场的(它有自己的 1000 血和自己的血条) */
    public static void adopt(ServerLevel sl, EasterBossEntity boss) {
        if (sl == null || boss == null) return;
        initialize(sl.getServer());
        if (HiddenBossIntro.isActing(boss.getUUID()) || !SummonGate.isAllowedDim(sl)) return;
        UUID previous = ManorFightMembers.fightId(boss);
        if (ManorFightData.get(sl.getServer()).retired(previous)) { boss.discard(); return; }
        clear(sl.dimension());
        Fight f = new Fight(sl.dimension(), boss.position(), boss.getManorSummonerId());
        f.difficulty = boss.getChallengeDifficulty();
        if (previous != null) f.id = previous;
        bind(f, boss);
        f.currentVariant = boss.getVariant();
        f.currentId = boss.getUUID();
        f.current = boss;
        f.onStage.add(boss.getUUID());
        f.refillPool(boss.getVariant());
        ACTIVE.add(f);
        /* ★ 2026-09-24 按用户要求「庄园 boss 战的场地区块保持加载」：开战就按住战场区块，
           免得玩家死亡/跑远/换维度时区块卸载，Boss 被当"卸载"处理（30 秒宽限期后整场结束）。 */
        net.farmingtales.ftboss.boss.ai.ManorArenaChunks.hold(sl, f.anchor);
        for (Entity entity : sl.getEntities().getAll()) {
            if (entity instanceof EasterBossEntity sparrow && sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT
                    && f.summonerId != null && f.summonerId.equals(sparrow.getManorSummonerId())) bind(f, sparrow);
        }
        /* 2026-09-12 v48: 开场先清掉上一场残留的彩蛋 Boss, 免得"两位同时在场" */
        purgeStale(sl, boss.getUUID(), true);
        SparrowSupport.beginStage(sl, boss);
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(boss) <= ATTACH_RANGE * ATTACH_RANGE) { f.bar.addPlayer(p); }
        }
        updateBar(f);
        persist();
        FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={} 血量={}",
                EasterBossEntity.NAMES[boss.getVariant()], (int) boss.getMaxHealth());
    }

    /** 直接开一场(生成首位) */
    public static void start(ServerLevel sl, Vec3 pos, int variant) {
        start(sl, pos, variant, null);
    }

    public static void start(ServerLevel sl, Vec3 pos, int variant, UUID summonerId) {
        start(sl, pos, variant, summonerId, ManorDifficulty.NORMAL);
    }

    public static void start(ServerLevel sl, Vec3 pos, int variant, UUID summonerId, ManorDifficulty difficulty) {
        if (sl == null) return;
        initialize(sl.getServer());
        if (!SummonGate.isAllowedDim(sl)) return;
        clear(sl.dimension());
        Fight f = new Fight(sl.dimension(), pos, summonerId);
        f.difficulty = difficulty == null ? ManorDifficulty.NORMAL : difficulty;
        f.refillPool(variant);
        ACTIVE.add(f);
        /* ★ 2026-09-24：同上 —— 指令/召唤物开的庄园战也按住战场区块。 */
        net.farmingtales.ftboss.boss.ai.ManorArenaChunks.hold(sl, f.anchor);
        for (Entity entity : sl.getEntities().getAll()) {
            if (entity instanceof EasterBossEntity sparrow && sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT
                    && summonerId != null && summonerId.equals(sparrow.getManorSummonerId())) bind(f, sparrow);
        }
        /* 2026-09-12 v48: 开局清场 —— 保留麻雀(它是支援位, 演出刚把它登记进来) */
        purgeStale(sl, null, true);
        spawnVariant(sl, f, variant);
        updateBar(f);
        FTBossMod.LOGGER.info("[FTBoss][Manor] 庄园战场开始(独立血条): 首发={}", EasterBossEntity.NAMES[variant]);
    }

    /** 清掉某个维度的战场(不结算) */
    public static void clear(ResourceKey<Level> dim) {
        if (activeServer == null) return;
        for (Fight fight : ACTIVE) {
            if (!fight.dim.equals(dim)) continue;
            ServerLevel level = activeServer.getLevel(dim);
            if (level != null) endFight(level, fight, false, "战场重置");
        }
    }

    /* ============ 2026-09-13 晚: 修「玩家重进存档后顶上的进度血条消失」 ============
     * 根因: {@link ServerBossEvent} 的玩家列表里存的是**登出前那个 ServerPlayer 对象**。
     * 重进存档后是个**全新对象**, 必须重新 {@code addPlayer} 才会补发 ADD 包, 否则
     * 客户端那边根本没有这条血条。所以补两条兜底:
     *   ① 登录时 {@link #reattach} 主动重挂;
     *   ② 每 40t {@link #recoverNear} 扫一遍 —— **整个游戏重启**会把静态内存态
     *      ({@code ACTIVE} / {@code Fight.bar}) 全丢掉, 那时场上还站着庄园 Boss
     *      却没血条也不轮换, 这里就地重建一场。 */

    /**
     * 【v148 按用户要求】Boss 连续掉入虚空 -> 把**整块战场搬到随机 48 格以外**。
     *
     * <p>搬迁内容: {@code Fight.anchor}(战场中心, 决定"玩家算不算在打庄园战"的 ATTACH_RANGE 判定)
     * 以及终章火柱的 {@code center}。取随机方向、48~64 格、Y 用高度图的地表高度,
     * 保证不会又搬进虚空/岩层里。返回搬了几块战场。
     */
    public static boolean rescue(EasterBossEntity boss) {
        if (!(boss.level() instanceof ServerLevel level)) return false;
        Fight fight = find(fightId(boss));
        long now = level.getGameTime();
        if (fight != null) {
            if (fight.lastVoidFall == Long.MIN_VALUE || now - fight.lastVoidFall > 600) fight.voidFalls = 0;
            fight.lastVoidFall = now;
            fight.voidFalls++;
            if (fight.voidFalls >= 2 && ManorCombatRules.relocationReady(now, fight.lastRelocation, RELOCATION_COOLDOWN)
                    && relocate(level, fight, boss)) return true;
        }
        ServerPlayer summoner = boss.getManorSummonerId() == null ? null
                : level.getServer().getPlayerList().getPlayer(boss.getManorSummonerId());
        Vec3 preferred = boss.canTeleportWith(summoner) ? summoner.position() : fight == null ? boss.position() : fight.anchor;
        Vec3 safe = ManorSafeTeleport.find(level, boss, preferred, 16);
        if (safe == null && fight != null) safe = ManorSafeTeleport.find(level, boss, fight.anchor, 24);
        if (safe != null) {
            boss.prepareArenaTransfer();
            ManorSafeTeleport.move(level, boss, safe);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 坠空救援 {} {} -> {}", boss.getName().getString(), boss.getUUID(), safe);
            return true;
        }
        if (fight != null) endFight(level, fight, false, "附近没有安全落点");
        else {
            boss.discard();
            FTBossMod.LOGGER.warn("[FTBoss][Manor] 无主 Boss {} 无安全落点，取消而非计为击败", boss.getUUID());
        }
        return false;
    }

    private static boolean relocate(ServerLevel level, Fight fight, EasterBossEntity trigger) {
        fight.lastRelocation = level.getGameTime();
        List<Entity> entities = members(level.getServer(), fight);
        ServerPlayer summoner = fight.summonerId == null ? null : level.getServer().getPlayerList().getPlayer(fight.summonerId);
        if (summoner != null && summoner.isAlive() && !summoner.isSpectator() && summoner.level() == level) entities.add(summoner);
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double distance = 52.0 + level.random.nextDouble() * 20.0;
            Vec3 candidate = fight.anchor.add(Math.cos(angle) * distance, 0.0, Math.sin(angle) * distance);
            Vec3 anchor = ManorSafeTeleport.find(level, trigger, candidate, 3);
            if (anchor == null || anchor.subtract(fight.anchor).horizontalDistanceSqr() < 48.0 * 48.0) continue;
            Map<Entity, Vec3> destinations = new LinkedHashMap<>();
            boolean safe = true;
            int ordinal = 0;
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity) || !entity.isAlive() || entity.level() != level) continue;
                double offset = ordinal++ * 2.4;
                Vec3 preferred = entity == trigger ? anchor : anchor.add(Math.cos(offset) * 4.0, 0.0, Math.sin(offset) * 4.0);
                Vec3 destination = ManorSafeTeleport.find(level, entity, preferred, 5);
                if (destination == null) { safe = false; break; }
                destinations.put(entity, destination);
            }
            if (!safe) continue;
            fight.anchor = anchor;
            fight.voidFalls = 0;
            /* ★ 2026-09-24：战场搬走了 —— 场地区块常加载跟着挪（hold 是幂等的，会顺手放掉旧场地）。 */
            net.farmingtales.ftboss.boss.ai.ManorArenaChunks.hold(level, fight.anchor);
            for (var destination : destinations.entrySet()) {
                if (destination.getKey() instanceof EasterBossEntity boss) boss.prepareArenaTransfer();
                ManorSafeTeleport.move(level, destination.getKey(), destination.getValue());
            }
            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity)) entity.discard();
            }
            if (fight.current != null) fight.lastPosition = fight.current.position();
            persist();
            if (summoner != null) summoner.displayClientMessage(Component.literal("§6【庄园】§7战场已安全搬迁，继续挑战。"), true);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 整场安全搬迁 {} -> {}，成员 {}", fight.id, anchor, destinations.size());
            return true;
        }
        FTBossMod.LOGGER.warn("[FTBoss][Manor] 战场 {} 未找到安全搬迁点，保留原场地", fight.id);
        return false;
    }

    /** 当前这场庄园战的战场中心(没有就返回 null) */
    public static Vec3 anchorOf(ServerLevel sl) {
        for (Fight f : ACTIVE) if (f.dim.equals(sl.dimension())) return f.anchor;
        return null;
    }

    public static Vec3 anchorOf(UUID id) {
        Fight fight = find(id);
        return fight == null ? null : fight.anchor;
    }

    /** 玩家重新登录 / 刚进世界: 把庄园进度血条重新挂到他身上 */
    public static void reattach(ServerPlayer p) {
        if (p == null) return;
        for (Fight f : ACTIVE) {
            if (!f.dim.equals(p.level().dimension())) continue;
            if (p.distanceToSqr(f.anchor.x, f.anchor.y, f.anchor.z) > ATTACH_RANGE * ATTACH_RANGE) continue;
            f.bar.removePlayer(p);          // 幂等: 先摘(通常本来就不在)再挂, 保证一定补发 ADD
            f.bar.addPlayer(p);
            f.bar.setVisible(true);
            updateBar(f);                    // 顺带把标题/进度重推一次
            FTBossMod.LOGGER.info("[FTBoss][Manor] 玩家 {} 重进存档 -> 已把庄园进度血条重挂上去(进度 {}/{})",
                    p.getName().getString(), f.defeated.size(), EasterBossEntity.MANOR_COUNT);
        }
    }

    /** 这个变体是不是庄园轮换名单里的(麻雀等支援位不在名单里, 不会被误判) */
    private static boolean isManorVariant(int v) {
        for (int i : EasterBossEntity.MANOR_VARIANTS) if (i == v) return true;
        return false;
    }

    /**
     * 每 40t 兜底: 有玩家附近站着**没有战场托管**的庄园 Boss, 就重建战场。
     * <p>触发场景基本只有一个 —— **重开游戏**导致静态内存态丢失。只扫玩家附近,
     * 开销很小; 麻雀(variant 9, 不在轮换名单)和迷你分身都会被跳过。
     */
    public static void recoverNear(MinecraftServer server) {
        if (server == null) return;
        initialize(server);
        for (ServerLevel level : server.getAllLevels()) {
            if (!SummonGate.isAllowedDim(level)) continue;
            boolean active = ACTIVE.stream().anyMatch(fight -> fight.dim.equals(level.dimension()));
            if (active) continue;
            boolean adopted = false;
            for (ServerPlayer player : level.players()) {
                for (EasterBossEntity boss : level.getEntitiesOfClass(EasterBossEntity.class, player.getBoundingBox().inflate(160.0))) {
                    if (!boss.isAlive() || boss.isRemoved() || net.farmingtales.ftboss.boss.ai.DoraPursuitClone.skipRecovery(boss) || !isManorVariant(boss.getVariant())
                            || HiddenBossIntro.isActing(boss.getUUID())) continue;
                    UUID previous = ManorFightMembers.fightId(boss);
                    if (ManorFightData.get(server).retired(previous)) { boss.discard(); continue; }
                    adopt(level, boss);
                    FTBossMod.LOGGER.info("[FTBoss][Manor] 收编无托管 Boss {}；召唤者={}", boss.getUUID(), boss.getManorSummonerId());
                    adopted = true;
                    break;
                }
                if (adopted) break;
            }
        }
    }

    /**
     * 【清场】(2026-09-12 v48 按用户反馈"一个 Boss 没死另一个 Boss 就进场了")
     *
     * <p>真凶不是轮换(轮换是"共享血条掉 10% 换一位", 换的时候旧的一定被 discard 掉了),
     * 而是**上一场没打完就残留下来**的彩蛋 Boss: 它们不在本场名单里, 却继续站在场地里追着玩家打,
     * 于是看起来像"场上同时有两位"。开局/开场/结束各清一次即可。
     *
     * <p>保留: 本场在编的(onStage) / 迷你分身(自己会到期) / keepSparrow=true 时的麻雀(支援位)。
     *
     * @param keep         额外保留的实体(例如刚入场演完、正要 adopt 的那位)
     * @param keepSparrow  是否放过麻雀(战斗进行中要放过; 结算时不再保留)
     */
    public static int purgeStale(ServerLevel sl, UUID keep, boolean keepSparrow) {
        if (sl == null) return 0;
        List<Entity> stale = new ArrayList<>();
        for (Entity entity : sl.getEntities().getAll()) {
            if (ManorFightData.get(sl.getServer()).retired(ManorFightMembers.fightId(entity))) stale.add(entity);
        }
        stale.forEach(Entity::discard);
        return stale.size();
    }

    /** 兼容旧调用；伤害始终由当前实体管线处理。 */
    @Deprecated
    public static boolean onDamage(ServerLevel level, EasterBossEntity boss, float amount,
                                   net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    /* ---------------- 内部 ---------------- */

    private static void spawnVariant(ServerLevel sl, Fight f, int variant) {
        EasterBossEntity e = FTEntities.EASTER_BOSS.get().create(sl);
        if (e == null) { endFight(sl, f, false, "无法生成下一位 Boss"); return; }
        double yaw = (float) (Math.toDegrees(Math.atan2(f.anchor.z - f.anchor.z, 1.0)));
        e.moveTo(f.anchor.x, f.anchor.y, f.anchor.z, (float) yaw, 0f);
        e.setVariant(variant);
        e.setManorSummonerId(f.summonerId);
        bind(f, e);
        e.prepareManorEntryBuffs();
        e.setNoAi(false);
        if (!sl.addFreshEntity(e)) { endFight(sl, f, false, "下一位 Boss 生成被取消"); return; }
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
        sl.playSound(null, f.anchor.x, f.anchor.y, f.anchor.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.4f, 0.7f);
        /* 2026-09-12 v51 独立血条: 播报"第几位上场 + 自己多少血 + 已击败多少" */
        FTBossMod.LOGGER.info("[FTBoss][Manor] 上场: {} (独立血量 {}, 已击败 {}/{})",
                EasterBossEntity.NAMES[variant], (int) e.getMaxHealth(),
                f.defeated.size(), EasterBossEntity.MANOR_COUNT);
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(f.anchor.x, f.anchor.y, f.anchor.z) <= ATTACH_RANGE * ATTACH_RANGE) {
                p.sendSystemMessage(Component.literal("§c【甜苹果庄园】§7第 §c"
                        + (f.defeated.size() + 1) + "§7 位上场：§c" + EasterBossEntity.NAMES[variant]
                        + " §8(已击败 " + f.defeated.size() + "/" + EasterBossEntity.MANOR_COUNT + ")"));
                /* v120 按用户要求「每个 boss 召唤不需要提示血量文版」: 上面那句去掉了「自己 X 血」 */
            }
        }
    }

    /** v51: 当前这位死了 -> 记一次"已击败", 刷进度条, 3 秒后上下一位(九位都打完就进最后的 Boss) */
    private static void onCurrentDefeated(ServerLevel sl, Fight f) {
        syncRecallBoundary(sl, f, false);
        SparrowSupport.endStage(sl, f.id);
        if (f.currentVariant >= 0 && f.defeated.add(f.currentVariant)) {
            ManorMercy.clearFight(f.id);
            persist();
            updateBar(f);
            FTBossMod.LOGGER.info("[FTBoss][Manor] 已击败 {} ({}/{})",
                    EasterBossEntity.NAMES[f.currentVariant], f.defeated.size(), EasterBossEntity.MANOR_COUNT);
            for (ServerPlayer p : sl.players()) {
                if (p.distanceToSqr(f.anchor) <= ATTACH_RANGE * ATTACH_RANGE) {
                    p.sendSystemMessage(Component.literal("§6【甜苹果庄园】§7" + EasterBossEntity.NAMES[f.currentVariant]
                            + " §7已被击败！§8(进度 " + f.defeated.size() + "/" + EasterBossEntity.MANOR_COUNT + ")"));
                }
            }
        }
        f.onStage.clear();
        f.pendingPlayerRecalls.clear();
        f.currentId = null;
        f.current = null;
    }

    private static void finish(ServerLevel sl, Fight f) {
        endFight(sl, f, true, "全部主战 Boss 已击败");
    }

    /** 每 tick: 维护血条的玩家范围 + 给在场者一点存在感粒子 */
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
    public static final class Ticker {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
            if (activeServer != event.getServer()) return;
            persist();
            HiddenBossIntro.cancelAll();
            ACTIVE.forEach(fight -> {
                fight.pendingPlayerRecalls.clear();
                fight.bar.removeAllPlayers();
            });
            ACTIVE.clear();
            LAST_RECALL.clear();
            ManorMercy.reset();
            activeServer = null;
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent e) {
            if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
            pumpAll();
        }
    }

    /** 上次泵过的刻 */
    private static long lastPump = -1L;
    /** v112: 护甲侵蚀清理用的独立刻戳(它必须与"战场上有没有 Boss"无关) */
    private static long lastShredPump = -1L;

    /** 战场推进(同样由 EasterBossEntity.tick 直接泵, 不再单靠事件总线) */
    public static void pumpAll() {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        initialize(server);
        long now = server.overworld().getGameTime();
        /* v112 修复: 功底「护甲侵蚀」的过期清理**不能**被"场上没有 Boss"挡住 ——
           原来这一行在 `if (ACTIVE.isEmpty()) return;` 之后, 于是战斗一结束/功底一死就再也不清理,
           玩家身上最多 -25 护甲的属性修饰符一直留着(会话内永久), STATE 里的 UUID 也不回收。
           现在它有自己的刻戳, 每 tick 跑一次(STATE 为空时函数内部直接 return, 开销可忽略)。 */
        if (now != lastShredPump) {
            lastShredPump = now;
            ArmorShred.tick(server);
        }
        if (ACTIVE.isEmpty()) return;
        if (now == lastPump) return;
        lastPump = now;
        for (Fight f : ACTIVE) {
                ServerLevel sl = server.getLevel(f.dim);
                if (sl == null) continue;
                f.phase++;
                if (f.currentVariant >= 0) {
                    /* 2026-09-12 v51 独立血条: 当前这位**死了**才上下一位，全部主战 Boss 被击败后结算。
                       死后再等 NEXT_DELAY(3 秒)让玩家看清播报/拾取, 然后开场。
                       v53 修正: 判定死活**看实体引用**, 不看 sl.getEntity(uuid) ——
                       玩家跑远/区块卸载时后者会返回 null, 以前会被误判成"已经死了"而直接换下一位。
                       v70 修「第一个 Boss 活着却被记成已击败」(用户反馈, 23:47 日志):
                       区块卸载时实体是 setRemoved(UNLOADED_TO_CHUNK), isRemoved() 也是 true ——
                       旧代码把它当"已击败", 于是玩家一死(区块票据失效)进度就 +1; 玩家复活后区块
                       重新加载, 这位 Boss 以**新实例**回来继续打(日志里重新出现"装备武器+穿腾炎甲"
                       这一对, 和刚上场时一模一样), 最后被当"名单外的多余 Boss"清掉。
                       现在: ①只认**真死**(血量<=0 或 isDeadOrDying); ②每 tick 按 UUID 重新绑定
                       世界里的实例(区块重载会换对象); ③查不到实体只累计 unloadedTicks,
                       超过 UNLOAD_GRACE_TICKS 才按已击败处理并告警。*/
                    EasterBossEntity cur = f.current;
                    if (f.currentId != null && sl.getEntity(f.currentId) == null
                            && !f.defeated.contains(f.currentVariant) && f.unloadedTicks % 100 == 20) {
                        sl.getChunk(net.minecraft.core.BlockPos.containing(f.lastPosition));
                    }
                    if (f.currentId != null) {
                        var inWorld = sl.getEntity(f.currentId);
                        if (inWorld instanceof EasterBossEntity le && le != cur) {
                            cur = le;                                    // 区块重载 -> 换成世界里的新实例
                            f.current = le;
                            bind(f, le);
                        }
                    }
                    boolean dead = f.defeated.contains(f.currentVariant)
                            || (cur != null && (cur.isDeadOrDying() || cur.getHealth() <= 0.0f));
                    boolean inWorld = cur != null && sl.getEntity(cur.getUUID()) == cur;
                    if (inWorld && !dead) {
                        f.lastPosition = cur.position();
                        if (f.phase % 20 == 0 && !SparrowSupport.hasVisited(cur)) SparrowSupport.beginStage(sl, cur);
                    }
                    if (dead || inWorld) {
                        f.unloadedTicks = 0;
                    } else {
                        f.unloadedTicks++;
                        if (f.unloadedTicks == UNLOAD_GRACE_TICKS + 1) {
                            FTBossMod.LOGGER.warn("[FTBoss][Manor] {} 连续 {} tick 不在世界里(区块一直没加载)"
                                             + " -> 取消本场，不计击败",
                                    EasterBossEntity.NAMES[f.currentVariant], f.unloadedTicks);
                        }
                    }
                    if (!dead && f.unloadedTicks > UNLOAD_GRACE_TICKS) {
                        endFight(sl, f, false, "Boss 卸载后无法恢复");
                        continue;
                    }
                    boolean gone = ManorCombatRules.defeated(dead, f.unloadedTicks);
                    if (gone) {
                        if (f.nextDelay <= 0) {
                            onCurrentDefeated(sl, f);
                            f.nextDelay = NEXT_DELAY;
                            if (f.defeated.size() < EasterBossEntity.MANOR_COUNT) {
                                ManorTransition.prepare(sl, f.id, f.anchor, f.pool.peekFirst() == null ? -1 : f.pool.peekFirst(), f.nextDelay);
                            }
                        } else if (--f.nextDelay <= 0) {
                            f.nextDelay = 0;
                            /* v61: 上下一位之前先清场 —— 保证场上不会同时出现两位满血庄园 Boss */
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
                if (f.phase % 20 == 1) syncRecallBoundary(sl, f, true);
                /* 2026-09-12 v53 按用户要求: 玩家离场地太远就**拉回场地** ——
                   既防止他把区块带走导致 Boss 被卸载(上面那条"误判死亡"), 也顺手防逃课。 */
                recallStrayPlayers(sl, f);
                if (!ACTIVE.contains(f)) continue;
                if (f.phase % 100 == 0) persist();
                for (ServerPlayer stale : new ArrayList<>(f.bar.getPlayers())) {
                    if (stale.hasDisconnected() || stale.connection == null) f.bar.removePlayer(stale);
                }
                for (ServerPlayer p : sl.players()) {
                    if (p.hasDisconnected() || p.connection == null) continue;
                    boolean near = p.distanceToSqr(f.anchor) <= ATTACH_RANGE * ATTACH_RANGE;
                    if (near && !f.bar.getPlayers().contains(p)) { f.bar.addPlayer(p); }
                    else if (!near && f.bar.getPlayers().contains(p)) f.bar.removePlayer(p);
                }
                for (UUID id : f.onStage) {
                    var ent = sl.getEntity(id);
                    if (ent instanceof EasterBossEntity eb && eb.isAlive()) {
                        /* 在场者脚下持续冒一点烟, 方便玩家找到"正在打哪一位" */
                        if (f.phase % 10 == 0) {
                            sl.sendParticles(ParticleTypes.CRIT, eb.getX(), eb.getY() + 1.0, eb.getZ(),
                                    2, 0.3, 0.5, 0.3, 0.02);
                        }
                        eb.getBossBar().setVisible(true);
                    }
                }
            }
        }

    /** 兼容旧调用名 */
    public static void pump() { pumpAll(); }

    /**
     * Keeps only actual participants subscribed to the moving 76-block warning fence. The same conditions
     * that pause ordinary recall (sky casting and Qizai's committed casts) also hide the fence, so the visual
     * never promises a teleport while that rule is temporarily disabled.
     */
    private static void syncRecallBoundary(ServerLevel level, Fight fight, boolean requestedActive) {
        EasterBossEntity boss = fight.current;
        boolean active = requestedActive && fight.nextDelay <= 0 && boss != null
                && boss.isAlive() && !boss.isRemoved() && boss.level() == level
                && !boss.isXuanlanSkyCasting() && !boss.isQizaiUninterruptibleCasting();
        ManorRecallBoundaryPacket packet = active
                ? new ManorRecallBoundaryPacket(fight.id, level.dimension().location(), boss.getId(), boss.getUUID(),
                        (float) STRAY_FROM_BOSS,
                        Math.max(0, Math.min(1, fight.noTargetTicks / (float) NO_TARGET_RECALL_TICKS)), true)
                : ManorRecallBoundaryPacket.stop(fight.id, level.dimension().location());
        for (UUID participant : fight.participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(participant);
            if (player != null) ManorRecallBoundaryPacket.send(player, packet);
        }
    }

    /**
     * 2026-09-13 v94 按用户要求重写: 「玩家离 Boss 超过 {@value #STRAY_FROM_BOSS} 格」或
     * 「Boss 连续 {@value #NO_TARGET_RECALL_TICKS} tick 没索敌到玩家」-> 把两人凑到一起,
     * **谁离场地更远就挪谁**(玩家跑了就把玩家拽到 Boss 身边; Boss 被拖出场地就把 Boss 拉回玩家身边)。
     *
     * <p>顺带保住 v53 的两个目的: ① 玩家不会因为跑远把区块带走、让在场上那位 Boss 被卸载(会被误判成"已死");
     * ② 防逃课。
     */
    private static void recallStrayPlayers(ServerLevel sl, Fight f) {
        EasterBossEntity boss = f.current;
        if (boss == null || boss.isRemoved() || !boss.isAlive()) { f.noTargetTicks = 0; return; }
        /* 高空演出与祁崽两门承诺施放会主动清目标；此时回场会误判脱战，传送玩家并通过
           prepareArenaTransfer 强制掐掉法术。施法完成前完全暂停这条普通防逃课召回。 */
        if (boss.isXuanlanSkyCasting() || boss.isQizaiUninterruptibleCasting()) {
            f.noTargetTicks = 0;
            f.stalledTicks = 0;
            f.stalledPosition = null;
            return;
        }
        var server = sl.getServer();
        if (server == null) return;
        /* 2026-09-19 防逃课扩容(用户口径 A): 拴绳对**全体参战者**生效, 不再只拉召唤者。
           名单只认本场持久化的 {@link Fight#participants} —— 掉线/死亡/旁观跳过,
           名单外的人(旁观、路人、旧存档无身份者)一概不动, **绝不**按"最近玩家"补位。 */
        var members = new ArrayList<ServerPlayer>();
        for (UUID id : f.participants) {
            ServerPlayer member = server.getPlayerList().getPlayer(id);
            if (member != null && member.isAlive() && !member.isSpectator()) members.add(member);
        }
        if (members.isEmpty()) { f.noTargetTicks = 0; return; }
        long now = sl.getGameTime();

        /* --- 触发条件 ②: 在场上那位连续 10 秒没锁定到**任何参战者**(打小怪不算) --- */
        LivingEntity lock = boss.getTarget();
        boolean lockedOnMember = lock instanceof ServerPlayer locked
                && ManorTeleportRules.isParticipant(f.participants, locked.getUUID());
        if (lockedOnMember) {
            f.noTargetTicks = 0;
        } else {
            f.noTargetTicks++;
        }
        boolean noTarget = f.noTargetTicks >= NO_TARGET_RECALL_TICKS;

        /* 卡住脱困仍旧只挪 Boss: 以"离 Boss 最近的参战者"为参照(单人局就是召唤者) */
        ServerPlayer reference = members.get(0);
        for (ServerPlayer member : members) {
            if (member.distanceToSqr(boss) < reference.distanceToSqr(boss)) reference = member;
        }
        boolean obstructed = reference.level() == sl && !boss.isPerformingSkill()
                && (!sl.noCollision(boss) || (lockedOnMember && !boss.hasLineOfSight(reference)));
        if (!obstructed || f.stalledPosition == null || boss.distanceToSqr(f.stalledPosition) > 1.0) {
            f.stalledTicks = 0;
            f.stalledPosition = boss.position();
        } else {
            f.stalledTicks++;
        }

        if (f.stalledTicks >= 200) {
            if (teleportBossTo(boss, reference, f)) {
                LAST_RECALL.put(reference.getUUID(), now);
                f.stalledTicks = 0;
                f.stalledPosition = boss.position();
                FTBossMod.LOGGER.info("[FTBoss][Manor] {} 长时间被障碍阻挡，安全脱困", boss.getUUID());
            }
            return;
        }

        /* 逐个参战者判定: 超过 {@value ManorTeleportRules#STRAY_FROM_BOSS} 格、或全场 10 秒没被索敌 ->
           先给玩家倒计时和越界提示，再把人和 Boss 凑到一起。每位参战者各自一条 5 秒冷却。 */
        for (ServerPlayer member : members) {
            boolean sameDimension = member.level().dimension().equals(f.dim);
            double distance = sameDimension ? member.distanceTo(boss) : Double.POSITIVE_INFINITY;
            boolean outsideBoundary = !sameDimension || distance > STRAY_FROM_BOSS;
            boolean nearBoundary = sameDimension && !noTarget
                    && distance >= ManorTeleportRules.RECALL_WARNING_DISTANCE
                    && distance <= STRAY_FROM_BOSS;
            if (nearBoundary && f.phase % 20 == 0) showRecallBoundaryWarning(member, distance);
            ManorTeleportRules.Recall recall = ManorTeleportRules.recall(sameDimension, member.distanceToSqr(boss),
                    member.distanceToSqr(f.anchor), boss.distanceToSqr(f.anchor), noTarget);
            if (recall == ManorTeleportRules.Recall.NONE) {
                if (f.pendingPlayerRecalls.remove(member.getUUID()) != null) {
                    member.displayClientMessage(Component.literal("§a【庄园】§7已回到战场范围，传送倒计时取消。"), true);
                }
                continue;
            }
            Long last = LAST_RECALL.get(member.getUUID());
            if (last != null && now - last < RECALL_CD) continue;
            if (recall == ManorTeleportRules.Recall.PLAYER_TO_BOSS) {
                Integer pending = f.pendingPlayerRecalls.get(member.getUUID());
                if (pending == null) {
                    pending = ManorTeleportRules.RECALL_COUNTDOWN_TICKS;
                    f.pendingPlayerRecalls.put(member.getUUID(), pending);
                    showRecallCountdown(member, pending, distance, outsideBoundary);
                    continue;
                }
                if (pending > 0) {
                    pending--;
                    f.pendingPlayerRecalls.put(member.getUUID(), pending);
                    if (pending > 0 && pending % 20 == 0) showRecallCountdown(member, pending, distance, outsideBoundary);
                    continue;
                }
            } else {
                f.pendingPlayerRecalls.remove(member.getUUID());
            }
            boolean moved = recall == ManorTeleportRules.Recall.PLAYER_TO_BOSS
                    ? teleportPlayerToBoss(member, boss, sameDimension ? null : sl, f)
                    : teleportBossTo(boss, member, f);
            if (!moved) {
                if (recall == ManorTeleportRules.Recall.PLAYER_TO_BOSS) {
                    f.pendingPlayerRecalls.put(member.getUUID(), 20);
                    member.displayClientMessage(Component.literal("§c【庄园】§7暂时找不到安全落点，1 秒后重试传送。"), true);
                }
                continue;
            }
            LAST_RECALL.put(member.getUUID(), now);
            f.pendingPlayerRecalls.remove(member.getUUID());
            pruneRecall(now);
            f.noTargetTicks = 0;
            /* Boss 已经被挪到某位参战者身边后本轮就收手, 免得几位玩家把它拽成拔河 */
            if (recall == ManorTeleportRules.Recall.BOSS_TO_PLAYER) return;
        }
    }

    private static void showRecallBoundaryWarning(ServerPlayer player, double distance) {
        player.displayClientMessage(Component.literal("§e【庄园】§7正在接近战场边界（距离 Boss §e"
                + formatRecallDistance(distance) + "§7 / §e" + formatRecallDistance(STRAY_FROM_BOSS)
                + "§7 格），请返回范围内。"), true);
    }

    private static void showRecallCountdown(ServerPlayer player, int ticks, double distance, boolean outsideBoundary) {
        int seconds = Math.max(1, (int) Math.ceil(ticks / 20.0));
        String location = Double.isFinite(distance) ? "距离 Boss §e" + formatRecallDistance(distance) + "§7 格" : "已离开 Boss 所在维度";
        String reason = outsideBoundary ? "§c超出战场范围！" : "§6Boss 暂未锁定你。";
        player.displayClientMessage(Component.literal("§c§l【庄园】§r " + reason + "§7" + location
                + "，将在 §c" + seconds + "§7 秒后传送回去。"), true);
    }

    private static String formatRecallDistance(double distance) {
        return Double.isFinite(distance) ? String.format(java.util.Locale.ROOT, "%.1f", distance) : "--";
    }

    /** 把玩家送到 Boss 身边 3 格(跨维度时也一起换维度)，倒计时结束后才执行。 */
    private static boolean teleportPlayerToBoss(ServerPlayer p, EasterBossEntity boss, ServerLevel crossDimTarget, Fight f) {
        if (!ManorTeleportRules.isParticipant(f.participants, p.getUUID())) return false;
        if (boss.isQizaiUninterruptibleCasting()) return false;
        ServerLevel level = (ServerLevel) boss.level();
        Vec3 safe = ManorSafeTeleport.find(level, p, boss.position().add(3.0, 0.0, 0.0), 8);
        if (safe == null) return false;
        boss.prepareArenaTransfer();
        ManorSafeTeleport.move(level, p, safe);
        p.displayClientMessage(Component.literal("§c§l【庄园】已将你传送回 Boss 身边。"), true);
        FTBossMod.LOGGER.info("[FTBoss][Manor] 玩家安全召回 {} -> {}", p.getName().getString(), safe);
        return true;
    }

    /** 把 Boss 送到某个参战者身边 3 格(带传送门粒子/音效) */
    private static boolean teleportBossTo(EasterBossEntity boss, ServerPlayer p, Fight f) {
        if (!ManorTeleportRules.isParticipant(f.participants, p.getUUID())) return false;
        if (boss.isQizaiUninterruptibleCasting()) return false;
        if (!boss.canTeleportToFightMember(p)) return false;
        ServerLevel level = (ServerLevel) boss.level();
        Vec3 safe = ManorSafeTeleport.find(level, boss, p.position().add(3.0, 0.0, 0.0), 8);
        if (safe == null) return false;
        boss.prepareArenaTransfer();
        ManorSafeTeleport.move(level, boss, safe);
        FTBossMod.LOGGER.info("[FTBoss][Manor] Boss 安全召回 {} -> {}", boss.getUUID(), safe);
        return true;
    }

    /**
     * 2026-09-12 v61 按用户反馈「还有活着的 boss 就进入下一阶段了，boss 甚至满血」:
     * 上下一位**之前**先清场 —— 场上除了本场在编的那一位之外, 任何"庄园彩蛋 Boss"都撤掉。
     *
     * <p>这样从机制上杜绝"两位庄园 Boss 同时在场"(不管它是漏清的残留、还是上一场留下的),
     * 也顺手解决"玩家看到满血 Boss 还在, 进度却推进了"的观感。
     * 麻雀(支援位)与迷你分身不在清理范围内。
     */
    private static int clearStrayManorBosses(ServerLevel sl, Fight f) {
        int removed = 0;
        for (Entity entity : members(sl.getServer(), f)) {
            if (!(entity instanceof EasterBossEntity boss) || boss.isMini()
                    || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT || f.onStage.contains(boss.getUUID())) continue;
            entity.discard();
            removed++;
        }
        return removed;
    }

    /* 2026-09-13 v94: 原来的 recallBoss()(Boss 离锚点 64 格就拉回锚点)已删除 ——
       它的职责被新的 recallStrayPlayers 完全接管: 玩家离 Boss > 76 格就触发, 而那时
       "Boss 离场地更远" 的分支会把 Boss 拉回**玩家**身边(玩家通常就在场地里),
       比"拉回锚点"更准, 也不会再出现"玩家在场地另一头、Boss 被拽回中心"的拔河。 */
}
