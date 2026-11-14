package net.farmingtales.ftboss.boss;

import com.google.common.collect.ImmutableList;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * 【麻雀出没】: 单人庄园随机安排四至十场支援；每增加一名有效参战玩家，
 * 每位 Boss 的登场额度增加一次，并提高其他场次的支援机会。
 *
 * <p>正式庄园按存档中的波次计划入场；独立彩蛋 Boss 每 {@value #CHECK_INTERVAL} tick
 * 检查附近交战玩家，单人基准概率为 {@value #CHANCE}，多人由
 * {@link SparrowMultiplayerSupport} 提升。所有路径保留全局同时一只限制。
 * 麻雀本身是 {@link EasterBossEntity} 的第 {@value EasterBossEntity#SPARROW_VARIANT} 号变体,
 * 带自己的皮肤(maque) —— 所以渲染/注册全都复用现成管线。
 */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class SparrowSupport {

    /** 判定间隔 —— v118 按用户反馈「麻雀好像不进场了」: 30 秒 -> 10 秒
        (原来每 30 秒才掷一次 22%, 期望要等两分多钟; 庄园里一位 Boss 往往几十秒就被打死, 于是基本见不到她) */
    public static final int CHECK_INTERVAL = 20 * 10;
    /** 每次判定的出现概率 —— v118: 0.22 -> 0.5(配合上面缩短的间隔, 期望约 20 秒进场) */
    public static final double CHANCE = 0.5;
    /** 玩家 48 格内有 Boss 才算"正在打 Boss" */
    public static final double FIGHT_RANGE = 48.0;
    /** 96 格内已有麻雀就不再刷 */
    public static final double DUPLICATE_RANGE = 96.0;
    /**
     * 麻雀血量。
     *
     * <p>2026-09-12 v35 按用户要求定为 500；
     * ★ v176 按用户要求「麻雀血量减少到 250 点」→ **250**。
     *
     * <p>这个常量是**唯一出处**：{@link #spawnSparrow} 与
     * {@code EasterBossEntity.supportSparrow}(入场演出生成的那只)都从这里取值，
     * 所以只改这一处，两条生成路径都会同步。
     */
    public static final float SPARROW_HEALTH = 500.0f;
    /** 2026-09-12 v36 按用户确认: **任意单场 Boss 战**都可能出现麻雀(不再要求两位 Boss) */
    public static final int MIN_BOSSES = 1;

    private static long lastPump = -1L;
    /** 2026-09-12 v41 按用户反馈"为什么召唤好几次": 全局只允许**一只**麻雀在场(所有玩家共用) */
    private static java.util.UUID aliveSparrow = null;

    private static final String VISITED = "ftboss_sparrow_visited";
    private static final String STAGE = "ftboss_sparrow_stage";

    private SparrowSupport() {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        tick(ServerLifecycleHooks.getCurrentServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        lastPump = -1L;
        aliveSparrow = null;
    }

    /** 主逻辑(同时被 EasterBossEntity.tick 驱动; 用刻戳去重) */
    public static void tick(MinecraftServer server) {
        if (server == null) return;
        long now = server.overworld().getGameTime();
        if (now == lastPump) return;
        lastPump = now;
        if (now % CHECK_INTERVAL != 0) return;
        /* 2026-09-12 v41: 已经有一只麻雀在场就不刷第二只(跨玩家全局唯一)
           2026-09-13 v102 按用户要求「同一场 boss 战里麻雀不要出现两只」——**改成直接扫世界**:
           原来只认 aliveSparrow 这一个 UUID, 有两个漏洞:
             ① 那只麻雀所在区块没加载时 lv.getEntity(uuid) 返回 null -> 被当成"不在了" -> 又刷一只;
             ② 庄园战场结束会 forgetSparrow(), 可那只麻雀可能还活着(90 秒寿命) -> 下一场又刷一只。
           现在以"世界里还活着几只"为准, 记账只用来打日志。 */
        int alive = countAliveSparrows(server);
        if (alive > 0) {
            if (alive > 1) {
                FTBossMod.LOGGER.warn("[FTBoss][Easter] 场上同时有 {} 只麻雀(应按规则只允许 1 只) —— 已阻止继续刷新", alive);
            }
            return;
        }
        aliveSparrow = null;
        for (ServerLevel sl : server.getAllLevels()) {
            for (ServerPlayer p : sl.players()) {
                if (!p.isAlive() || p.isSpectator()) continue;
                /* 2026-09-12 v35 按用户要求: 麻雀随机出现在**两个以上 Boss 的战斗**里
                   2026-09-13 v84 按用户反馈「没进 Boss 战也召唤了麻雀」: 光"附近有 Boss 实体"不够 ——
                   还必须是**真的在交战**: 玩家在庄园战场内, 或这只 Boss 正把这个玩家当目标。
                   (以前一只没人管的遗留/自己召唤的苹果爵士站在旁边就能把麻雀招来。)
                   ★★★ 2026-09-14 v106 按用户反馈「麻雀出现在非隐藏 boss 的战斗」★★★
                   麻雀是**隐藏 Boss(甜苹果庄园)专属的场外支援**, 但上面这条判据对"任意本模组 Boss"
                   都成立 —— 日志里它在糖霜女皇/沙耶/故障机器人的战斗里都刷过。
                   现在只认**真・彩蛋 Boss(庄园十人)**: 非隐藏 Boss 的战斗一律不刷。 */
                java.util.List<LivingEntity> bosses = bossesNear(sl, p);
                if (bosses.size() < MIN_BOSSES) continue;
                LivingEntity candidate = null;
                for (LivingEntity boss : bosses) {
                    if (hasVisited(boss)) continue;
                    java.util.UUID fight = ManorFightMembers.fightId(boss);
                    int stage = ManorFight.currentVariant(fight);
                    if (stage >= 0 && ManorFight.difficulty(fight) != ManorDifficulty.IMPOSSIBLE
                            && !SparrowMultiplayerSupport.planned(ManorFightData.get(server), fight, stage)) continue;
                    if ((ManorFight.isManaged(boss) && ManorFight.isPlayerFighting(p))
                            || net.farmingtales.ftboss.boss.ai.FightState.isEngagedWith(boss, p)) {
                        candidate = boss;
                        break;
                    }
                }
                if (candidate == null) continue;
                if (ManorFight.currentVariant(ManorFightMembers.fightId(candidate)) < 0
                        && SparrowMultiplayerSupport.trial(sl.random.nextDouble(), candidate) > CHANCE) continue;
                spawnSparrow(sl, candidate);
                return;   // 一次判定最多来一只
            }
        }
    }

    public static boolean hasVisited(java.util.UUID fightId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && ManorFightData.get(server).sparrowVisited(fightId);
    }

    public static boolean hasVisited(LivingEntity boss) {
        Entity member = ManorFightMembers.resolve(boss);
        if (member == null || !(member.level() instanceof ServerLevel level)) return false;
        java.util.UUID fight = ManorFightMembers.fightId(member);
        ManorFightData data = ManorFightData.get(level.getServer());
        if (member instanceof EasterBossEntity manor && manor.isImpossibleChallenge()) return data.retired(fight);
        int stage = ManorFight.currentVariant(fight);
        if (stage >= 0) return SparrowMultiplayerSupport.exhausted(data.retired(fight) || data.sparrowVisited(fight, stage)
                || member.getPersistentData().getBoolean(VISITED), boss);
        return SparrowMultiplayerSupport.exhausted(data.retired(fight) || data.sparrowVisited(fight)
                || member.getPersistentData().getBoolean(VISITED), boss);
    }

    public static void noteFightMember(Entity member) {
        SparrowMultiplayerSupport.note(member);
        if (!(member.level() instanceof ServerLevel level)) return;
        if ((member instanceof EasterBossEntity boss && boss.getVariant() == EasterBossEntity.SPARROW_VARIANT)
                || member.getPersistentData().getBoolean(VISITED)) {
            member.getPersistentData().putBoolean(VISITED, true);
            java.util.UUID fight = ManorFightMembers.fightId(member);
            ManorFightData data = ManorFightData.get(level.getServer());
            int stage = member.getPersistentData().contains(STAGE)
                    ? member.getPersistentData().getInt(STAGE) : ManorFight.currentVariant(fight);
            if (stage >= 0) {
                member.getPersistentData().putInt(STAGE, stage);
                data.visitSparrow(fight, stage);
            } else data.visitSparrow(fight);
        }
    }

    public static void beginStage(ServerLevel level, EasterBossEntity boss) {
        if (boss.isImpossibleChallenge()) {
            ImpossibleSparrowSupport.ensure(level, boss);
            return;
        }
        java.util.UUID fight = ManorFightMembers.fightId(boss);
        if (fight == null || !boss.getUUID().equals(ManorFight.currentBoss(fight))) return;
        ManorFightData data = ManorFightData.get(level.getServer());
        data.sparrowPlan(fight);
        noteFightMember(boss);
        for (Entity entity : ImmutableList.copyOf(level.getAllEntities())) {
            if (!(entity instanceof EasterBossEntity sparrow) || !sparrow.isAlive() || sparrow.isRemoved()
                    || sparrow.getVariant() != EasterBossEntity.SPARROW_VARIANT
                    || !fight.equals(ManorFightMembers.fightId(sparrow))) continue;
            if (sparrow.getPersistentData().contains(STAGE)
                    && sparrow.getPersistentData().getInt(STAGE) != boss.getVariant()) {
                sparrow.discard();
                continue;
            }
            noteFightMember(sparrow);
            boss.getPersistentData().putBoolean(VISITED, true);
        }
        if (SparrowMultiplayerSupport.planned(data, fight, boss.getVariant())) spawnSparrow(level, boss);
    }

    public static void endStage(ServerLevel level, java.util.UUID fight) {
        if (ManorFight.difficulty(fight) == ManorDifficulty.IMPOSSIBLE) return;
        for (Entity entity : ImmutableList.copyOf(level.getAllEntities())) {
            if (entity instanceof EasterBossEntity sparrow && sparrow.getVariant() == EasterBossEntity.SPARROW_VARIANT
                    && fight.equals(ManorFightMembers.fightId(sparrow))) sparrow.discard();
        }
    }

    /**
     * 2026-09-13 v102 按用户要求「同一场 boss 战里麻雀不要出现两只」:
     * **直接数世界里还活着几只麻雀**(跨维度全扫), 不再依赖单个 UUID 记账 ——
     * 那只麻雀所在区块没加载时 {@code getEntity(uuid)} 会返回 null, 记账法会误判成"不在了"。
     * 每 {@value #CHECK_INTERVAL} tick 才跑一次, 扫全部实体的开销可以接受。
     */
    public static int countAliveSparrows(MinecraftServer server) {
        if (server == null) return 0;
        int n = 0;
        for (ServerLevel lv : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity e : ImmutableList.copyOf(lv.getAllEntities())) {
                if (!(e instanceof EasterBossEntity eb)) continue;
                if (eb.getVariant() != EasterBossEntity.SPARROW_VARIANT) continue;
                if (!eb.isAlive() || eb.isRemoved()) continue;
                int stage = ManorFight.currentVariant(ManorFightMembers.fightId(eb));
                if (!eb.isImpossibleChallenge() && stage >= 0 && eb.getPersistentData().contains(STAGE)
                        && eb.getPersistentData().getInt(STAGE) != stage) {
                    eb.discard();
                    continue;
                }
                noteFightMember(eb);
                n++;
            }
        }
        return n;
    }

    /** 玩家附近的隐藏 Boss 候选列表；交战状态在调用方判断，麻雀自己不算。 */
    private static java.util.List<LivingEntity> bossesNear(ServerLevel sl, ServerPlayer p) {
        java.util.List<LivingEntity> out = new java.util.ArrayList<>();
        for (EasterBossEntity boss : sl.getEntitiesOfClass(EasterBossEntity.class, p.getBoundingBox().inflate(FIGHT_RANGE))) {
            if (!boss.isAlive() || boss.isMini() || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT
                    || HiddenBossIntro.isActing(boss.getUUID())) continue;
            out.add(boss);
        }
        return out;
    }

    /** 2026-09-12 v44: 让入场演出生成的那只麻雀也登记进来(免得判定又刷第二只) */
    public static void noteSparrow(EasterBossEntity sparrow) {
        aliveSparrow = sparrow.getUUID();
        noteFightMember(sparrow);
    }

    /** 2026-09-12 v48: 庄园战场结束时清掉登记 —— 否则下次判定以为"麻雀还在场", 永远不再刷新的 */
    public static void forgetSparrow() {
        aliveSparrow = null;
    }

    /** 召唤一只麻雀到 Boss 上方 6 格 */
    public static void spawnSparrow(ServerLevel sl, LivingEntity boss) {
        if (net.farmingtales.ftboss.boss.ai.XuanlanEncounterLimits.blocksSupport(boss)) return;
        if (sl == null || !(boss instanceof EasterBossEntity manorBoss) || boss.level() != sl
                || !boss.isAlive() || boss.isRemoved() || manorBoss.isMini()
                || manorBoss.getVariant() == EasterBossEntity.SPARROW_VARIANT
                || HiddenBossIntro.isActing(boss.getUUID()) || hasVisited(boss)) return;
        java.util.UUID fight = ManorFightMembers.fightId(boss);
        int stage = ManorFight.currentVariant(fight);
        if (stage >= 0 && (!boss.getUUID().equals(ManorFight.currentBoss(fight))
                || !manorBoss.isImpossibleChallenge() && !SparrowMultiplayerSupport.planned(ManorFightData.get(sl.getServer()), fight, stage))) return;
        /* v102: 兜底再查一次 —— 只要场上还有活着的麻雀就绝不生成第二只(不管是谁调进来的) */
        if (ImpossibleSparrowSupport.reserved(sl.getServer()) || countAliveSparrows(sl.getServer()) > 0) {
            FTBossMod.LOGGER.info("[FTBoss][Easter] 场上已有麻雀, 跳过本次召唤");
            return;
        }
        EasterBossEntity s = FTEntities.EASTER_BOSS.get().create(sl);
        if (s == null) return;
        double ang = sl.random.nextDouble() * Math.PI * 2.0;
        double x = boss.getX() + Math.cos(ang) * 6.0;
        double z = boss.getZ() + Math.sin(ang) * 6.0;
        double y = boss.getY() + 6.0;
        s.moveTo(x, y, z, sl.random.nextFloat() * 360.0f, 0.0f);
        s.setVariant(EasterBossEntity.SPARROW_VARIANT);
        net.farmingtales.ftboss.boss.ManorFightMembers.inherit(boss, s);
        s.setPersistenceRequired();
        /* 2026-09-12 v41: 名字常显 —— 一眼能确认进来的是"麻雀"而不是别的 Boss(排查"进场的是紫菜") */
        s.setCustomName(Component.literal("§e麻雀"));
        s.setCustomNameVisible(true);
        var hp = s.getAttribute(Attributes.MAX_HEALTH);
        float health = ImpossibleSparrowSupport.health(s);
        if (hp != null) hp.setBaseValue(health);
        s.setHealth(health);
        if (!sl.addFreshEntity(s)) {
            ImpossibleSparrowSupport.failedSpawn(s);
            return;
        }
        SparrowMultiplayerSupport.spawned(boss, s);
        Entity member = ManorFightMembers.resolve(boss);
        member.getPersistentData().putBoolean(VISITED, true);
        noteFightMember(member);
        noteSparrow(s);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀进场: variant={} 名字={} 血量={} @{},{},{}",
                s.getVariant(), s.getName().getString(), (int) health, (int) x, (int) y, (int) z);
        sl.sendParticles(ParticleTypes.CLOUD, x, y + 0.5, z, 30, 0.5, 0.5, 0.5, 0.08);
        sl.playSound(null, x, y, z, SoundEvents.PARROT_AMBIENT, SoundSource.HOSTILE, 1.4f, 1.2f);
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(boss) <= 96.0 * 96.0) {
                p.sendSystemMessage(Component.literal(
                        "§e【麻雀】§7一只麻雀飞进了战场 —— 它在给 Boss §c回血 / 上增益 / 清减益§7！"));
            }
        }
        String bossName = boss instanceof EasterBossEntity eb
                ? EasterBossEntity.NAMES[eb.getVariant()] : boss.getName().getString();
        FTBossMod.LOGGER.info("[FTBoss][Easter] 麻雀随机加入战斗, 支援 {}", bossName);
    }
}
