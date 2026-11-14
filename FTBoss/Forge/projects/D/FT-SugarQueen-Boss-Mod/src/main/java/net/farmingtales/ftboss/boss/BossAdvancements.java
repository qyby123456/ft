package net.farmingtales.ftboss.boss;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

/**
 * 【Boss 进度（成就）】的统一发放口径 —— 2026-09-19 v172 按用户要求新增：
 * 「给全部非庄园 boss 做一个触发 boss 战的成就和战胜 boss 的成就。做一个获得血腥契约的成就」。
 *
 * <p>覆盖的 8 只**非庄园** Boss（庄园十位 / 隐藏 Boss 不算在内）：
 * <pre>
 *   ftboss:sugar_queen      糖霜女皇
 *   ftboss:meat_mountain    屠夫
 *   ftboss:tea_tree         茶树古树（沙耶）
 *   ftboss:wine_barrel      酒桶王
 *   ftboss:rat_boss         鼠群
 *   ftboss:defect_core      故障机器人
 *   ftboss:benger           边牧犬王
 *   ftboss:tide_fish_boss   潮宴鱼王
 * </pre>
 *
 * <p><b>进度文件约定</b>（沿用本模组已有的做法，见 {@code HiddenBossChallenge.grantAdvancement}）：
 * 每个进度写在 {@code data/ftboss/advancements/<id>.json}，判定条件一律用
 * {@code minecraft:impossible}（原版永远不会自己触发），只能由本类在自定义时机手动发放
 * （{@code PlayerAdvancements.award}）—— 这样进度能钉在任意时刻上。
 *
 * <p>本类只做<b>两件事</b>：
 * <ul>
 *   <li>{@link #onBossSummoned} —— 非庄园 Boss **进入世界**的那一刻（触发战），
 *       由 {@code BossHandler} 的 {@code EntityJoinLevelEvent} 调用；</li>
 *   <li>{@link #onBossDefeated} —— 非庄园 Boss **被击杀**的那一刻（战胜），
 *       由 {@link net.farmingtales.ftboss.boss.ai.BossMercy#onBossDefeated} 调用（那里已经有
 *       96 格内的玩家名单，正好复用）。</li>
 * </ul>
 *
 * <p><b>怎么知道该发给谁</b>：召唤类 Boss 身上都带 {@code ManorFightMembers} 的归属标记
 * （{@code ManorFightMembers.owner(entity)}），取它就是召唤者；取不到时退化为
 * "附近 32 格内最近的那名玩家"。归属标记**不会**被"别人的 Boss"蹭到，
 * 所以不会出现"路过就被算通关"。
 */
public final class BossAdvancements {

    /**
     * 十个实体 id 的完整清单 —— 与「石像档位」那张表一一对应
     * （{@code BossHandler.tintFor} 的 case 1..10 就是这十只，各配一个召唤道具）。
     *
     * <p>★ v173 修正：原来是 8 只，**漏了 `apple_boss`（苹果爵士）与 `compost_boss`（答辩超人）**。
     * 用户指出「非庄园的 10 个 boss 都要做」后，按石像档位表点清：
     * <pre>
     *   1 糖霜女皇 sugar_queen     6 故障机器人 defect_core
     *   2 屠夫     meat_mountain   7 答辩超人   compost_boss
     *   3 沙耶     tea_tree        8 苹果爵士   apple_boss
     *   4 酒桶王   wine_barrel     9 潮宴鱼王   tide_fish_boss
     *   5 鼠群     rat_boss       10 边牧犬王   benger
     * </pre>
     * 庄园十位走 {@code EasterBossEntity} 的变体号（{@code MANOR_VARIANTS}，10 人），**不算**。
     *
     * <p>顺序：前 8 个保持 v172 的原顺序（否则已存在的进度 parent 会指向不存在的 id），
     * 新增项一律追加在末尾。
     */
    public static final List<String> NON_MANOR_BOSSES = List.of(
            "sugar_queen", "meat_mountain", "tea_tree", "wine_barrel",
            "rat_boss", "defect_core", "benger", "tide_fish_boss",
            "apple_boss", "compost_boss");

    /**
     * 【八王尽伏】的门槛 ——「食物系」那五位（= 柠檬猫「喊帮手」会召来的阵容，
     * 也是 {@code LemonSacrifice.bossProfile} 的白名单）。
     *
     * <p>★ v174 调整：v173 时它和「美食的俘虏」都要求十位全战胜，**两个成就完全重复**。
     * 现在分工：本成就作为**中期里程碑**（这五位是主线顺序里的前五场硬仗），
     * 十位全清留给 {@code food_boss_slayer_all}（美食的俘虏）。名字里的"八王"是历史遗留，
     * 未改 id 以免已存在的引用失效。
     */
    public static final List<String> SIGNATURE_BOSSES = List.of(
            "sugar_queen", "meat_mountain", "tea_tree", "wine_barrel", "rat_boss");

    /** 召唤时用的归属标记读取半径（取不到 owner 时的兜底） */
    private static final double NEARBY_RADIUS = 32.0;

    /**
     * 「战斗全清」总成就的名单位 —— 十位非庄园 Boss 全都要（{@link #NON_MANOR_BOSSES}）。
     *
     * <p>v174 分工（见 {@link #SIGNATURE_BOSSES} 的说明）：
     * <ul>
     *   <li>{@code boss_slayer_all}（八王尽伏）—— 门槛 = 五位食物系 Boss（中期里程碑）；</li>
     *   <li>{@code food_boss_slayer_all}（美食的俘虏）—— 门槛 = 十位全战胜（终局）。</li>
     * </ul>
     */
    public static final List<String> COMPLETION_BOSSES = NON_MANOR_BOSSES;

    /**
     * 【庄园凯旋】成就 id —— v175 按用户要求新增「战胜庄园的成就：回忆的终结」。
     *
     * <p>它**不属于** {@link #NON_MANOR_BOSSES} 那套"每位 Boss 一条"的结构：
     * 庄园是十连战，本条只在整个庄园被清空时发一次，因此单独成一个常量，
     * 也不参与 八王尽伏 / 美食的俘虏 的门槛计算（那两个只数非庄园十位）。
     */
    public static final String MANOR_VICTORY = "memories_end";

    /**
     * 【庄园凯旋 · 终结难度】成就 id —— 2026-09-21 按用户要求新增
     * 「给终结难度胜利增加一个进度：卧槽，外挂！」。
     *
     * <p>与 {@link #MANOR_VICTORY} **互不替代**：那条**只在普通难度**发
     * （{@code ManorDifficulty.WARNING} 写明"该难度几乎无法通过而且不会获取成就"），
     * 这条**只在终结难度**发。所以"终结难度通关"终于有了自己看得见的进度，
     * 而不是像以前那样只默默打一个隐形头衔进度（{@code title_manor_impossible}）。
     */
    public static final String MANOR_IMPOSSIBLE_VICTORY = "manor_impossible_victory";

    private BossAdvancements() {}

    /** 这个实体是不是我们要发进度的非庄园 Boss（庄园十位一律返回 null）。 */
    public static String bossId(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof net.farmingtales.ftboss.entity.RatBossEntity rat && rat.isMinion()) return null;
        if (entity instanceof net.farmingtales.ftboss.entity.EasterBossEntity) return null;   // 庄园/隐藏 Boss 不算
        EntityType<?> type = entity.getType();
        if (type == null) return null;
        ResourceLocation key = EntityType.getKey(type);
        if (key == null || !FTBossMod.MOD_ID.equals(key.getNamespace())) return null;
        return NON_MANOR_BOSSES.contains(key.getPath()) ? key.getPath() : null;
    }

    /** 触发进度的 id：{@code boss_<id>_spotted}；战胜进度的 id：{@code boss_<id>_slain}。 */
    public static String spottedId(String bossId) {
        return "boss_" + bossId + "_spotted";
    }

    public static String slainId(String bossId) {
        return "boss_" + bossId + "_slain";
    }

    /**
     * 非庄园 Boss 进入世界 -> 给召唤者发放「触发 boss 战」进度。
     *
     * @param entity 刚进入世界的实体（不是非庄园 Boss 时直接返回）
     */
    public static void onBossSummoned(Entity entity) {
        String id = bossId(entity);
        if (id == null) return;
        if (!(entity.level() instanceof ServerLevel level)) return;
        ServerPlayer owner = owner(entity, level);
        if (owner == null) return;
        award(owner, spottedId(id));
    }

    /**
     * 非庄园 Boss 被击杀 -> 给 96 格内的玩家发放「战胜 boss」进度，
     * 全部八只都拿到时再补发总成就 {@code boss_slayer_all}。
     */
    public static void onBossDefeated(LivingEntity boss) {
        String id = bossId(boss);
        if (id == null) return;
        if (!(boss.level() instanceof ServerLevel level)) return;
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(96));
        for (ServerPlayer p : players) {
            award(p, slainId(id));
            /* 两个总成就，门槛不同（v174 分工，避免语义重复）：
               boss_slayer_all（八王尽伏）= 五位食物系 Boss —— 中期里程碑；
               food_boss_slayer_all（美食的俘虏）= 十位非庄园 Boss 全清 —— 终局。
               ★ v175：**拿到"美食的俘虏"的那一刻**才跟着发头衔 ——
               `awardIfAllSlain` 返回"这次是否真的发了"，
               于是没集齐的人不会被白发头衔（命令侧也有同样的门槛校验）。
               先于旧版无条件调用 `grantTitle` 是错的，这里已纠正。 */
            awardIfAllSlain(p, SIGNATURE_BOSSES, "boss_slayer_all");
            if (awardIfAllSlain(p, COMPLETION_BOSSES, "food_boss_slayer_all")) {
                grantTitle(p, "title_food_boss_slayer_all");
            }
        }
    }

    /**
     * 【庄园凯旋 · 普通难度】-> 给本场参战名单发放「回忆的终结」成就 + 「12号位」头衔。
     *
     * <p>★ v175 按用户要求：「加一个战胜庄园的成就：回忆的终结」。
     * ★ v184 按用户要求「把甜苹果普通难度获得的头衔和终结难度头衔互换一下」：
     * 本条现在发的是 **「12号位」**（原先发「甜苹果庄园园长」，已换到终结难度那边）。
     *
     * <p><b>为什么挂在庄园胜利上、而不是某一只 Boss 的死亡上</b>：庄园不是"一只 Boss"，
     * 而是一场**十连战**（{@code EasterBossEntity.MANOR_VARIANTS} 十位轮换，血条就是
     * "已击败 N/10"）。{@code ManorFight.finish()} 只在**十位全部击败**时才被调用，
     * 再走 {@code endFight(..., victory=true, ...)} —— 也就是说玩家手上有这个成就时，
     * 十位庄园 Boss 确实一个不剩。发在这一刻既符合"战胜庄园"的字面意思，
     * 也天然是"整场打完"的唯一收束点（Boss 各自的死亡事件有十次，都不代表庄园被清）。
     *
     * <p><b>发给谁</b>：{@code ManorFight.Fight.participants} —— 本场持久化的参战名单
     * （召唤者 + 中途加入者，见 {@code ManorFight} 里 {@code participants.add}）。
     * 与"击败 Boss 给 96 格内玩家"不同，这里必须用名单：胜利结算时战斗场地已经要清理，
     * 按距离取人会漏掉刚刚倒下/被传走的队友，而名单是持久化过的、掉线重连也在里面。
     * 玩家不在线就跳过（不发离线补发）。
     *
     * @param level        胜利所在维度
     * @param participants 本场参战玩家 UUID 名单
     */
    public static void onManorNormalVictory(ServerLevel level, Set<UUID> participants) {
        if (level == null || participants == null || participants.isEmpty()) return;
        for (UUID id : participants) {
            ServerPlayer player = level.getServer() == null
                    ? null : level.getServer().getPlayerList().getPlayer(id);
            if (player == null) continue;
            award(player, MANOR_VICTORY);
            /* 战胜庄园的同时把这一难度的头衔发掉。
               玩家在这一刻必然满足门槛（上面刚发的就是门槛成就），
               所以这里直接发；头衔进度自身是幂等的（打过勾就不再打）。 */
            grantTitle(player, "title_memories_end");
        }
    }

    /**
     * 【庄园凯旋 · 终结难度】-> 给本场参战名单发放「甜苹果庄园园长」**头衔**。
     *
     * <p>★ v183 起本方法负责"终结难度的头衔"；★ v184 互换后它发的是
     * **「甜苹果庄园园长」**（原先发「12号位」，已换到普通难度那边）。
     *
     * <p><b>为什么单独开一个方法、而不并进 {@link #onManorNormalVictory}</b>：
     * 「回忆的终结」那条成就**故意只在普通难度发**（{@code ManorDifficulty.WARNING} 写明
     * "该难度几乎无法通过而且不会获取成就"，{@code ManorFight} 里也是
     * {@code difficulty == NORMAL} 才 {@code award}）。终结难度通关**不该**补发那个成就，
     * 但**该给头衔** —— 于是这里只打头衔功能进度、完全不碰成就。
     * （v184 互换的只是"发哪个头衔"，这条"终结难度不发成就"的口径没变。）
     *
     * <p>门槛口径（与 {@code TitleCommand} 里补发时用的判据保持一致）：
     * 这个头衔的唯一取得方式就是"通关终结难度"，所以**不做额外校验**；
     * 头衔进度本身幂等（打过勾就不再打），重复通关也不会重复触发。
     *
     * @param level        胜利所在维度
     * @param participants 本场参战玩家 UUID 名单
     */
    public static void onManorImpossibleVictory(ServerLevel level, Set<UUID> participants) {
        if (level == null || participants == null || participants.isEmpty()) return;
        for (UUID id : participants) {
            ServerPlayer player = level.getServer() == null
                    ? null : level.getServer().getPlayerList().getPlayer(id);
            if (player == null) continue;
            /* ★ 2026-09-21 用户要求：终结难度通关也要有一条**看得见**的进度（卧槽，外挂！）。 */
            award(player, MANOR_IMPOSSIBLE_VICTORY);
            grantTitle(player, "title_manor_impossible");
        }
    }

    /**
     * 给定的一组 Boss 全部战胜过才发对应成就（没齐时静默返回，不刷日志）。
     *
     * @param ids       要检查的 Boss id 列表
     * @param advancementId 全齐时发放的进度 id
     * @return true = 门槛已满足（本次调用把成就发了，或玩家早就拿过）；
     *         false = 还有 Boss 没战胜 / 数据包缺失 —— 调用方据此决定要不要跟着发头衔
     */
    private static boolean awardIfAllSlain(ServerPlayer player, List<String> ids, String advancementId) {
        try {
            var advancements = player.server.getAdvancements();
            for (String id : ids) {
                Advancement slain = advancements.getAdvancement(new ResourceLocation(FTBossMod.MOD_ID, slainId(id)));
                if (slain == null || !player.getAdvancements().getOrStartProgress(slain).isDone()) return false;
            }
            award(player, advancementId);
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][进度] 成就判定失败({}): {}", advancementId, t.toString());
            return false;
        }
    }

    /** 召唤者：优先取归属标记（召唤道具/克隆体都会打），取不到就退化为最近玩家。 */
    private static ServerPlayer owner(Entity entity, ServerLevel level) {
        try {
            Entity marked = ManorFightMembers.owner(entity);
            if (marked instanceof ServerPlayer sp) return sp;
            if (marked != null && level.getServer() != null) {
                ServerPlayer online = level.getServer().getPlayerList().getPlayer(marked.getUUID());
                if (online != null) return online;
            }
            if (entity instanceof Mob mob) {
                /* ServerLevel.getNearestPlayer 声明返回的是 **Player**（1.20.1 javap 核实），
                   实际在服务端一定是 ServerPlayer —— 但仍要 instanceof 收窄，别硬转。 */
                Player nearest = level.getNearestPlayer(mob, NEARBY_RADIUS);
                if (nearest instanceof ServerPlayer sp) return sp;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * 发放一个本模组的进度。数据包没加载（或 id 写错）时只记一条日志，绝不抛给调用方。
     *
     * <p>与 {@code HiddenBossChallenge.grantAdvancement} 同一套做法；那边保留原样不动
     * （彩蛋进度有自己的时机），这里只是把同一套逻辑抽出来给 Boss 进度复用。
     */
    public static void award(ServerPlayer player, String id) {
        if (player == null) return;
        try {
            Advancement advancement = player.server.getAdvancements()
                    .getAdvancement(new ResourceLocation(FTBossMod.MOD_ID, id));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][进度] 找不到进度 {}:{}", FTBossMod.MOD_ID, id);
                return;
            }
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (progress.isDone()) return;   // 已拿过就别刷日志
            /* ★ 单条件进度（除总成就外都是）用 award(adv, "triggered")；
               两个总成就有多条 criteria 且**没有**叫 "triggered" 的那条。
               1.20.1 的 PlayerAdvancements **只有** award(Advancement, String) 一个重载
               （javap 核实过，没有"一次给全部 criteria"的重载），所以对多条件进度
               就逐条 award —— 每给一条，原版自己会检查 requirements 是否全部满足。
               ★ 聊天栏播报**不用我们操心**：javap 反编译核实过原版 award() 的完整流程 ——
                 它先存 wasDone = progress.isDone()，grantProgress 之后若
                 "进进度之前未完成 && grant 成功了 && 现在 isDone()"，就自己发一条
                 `chat.type.advancement.<frame>` 并 PlayerList.broadcastSystemMessage(组件, false)
                 （全员可见；末位参数是"是否写服务器控制台"，false = 不写，正常）。
                 上面循环的**第一次** award 调用就已经走完这套判定（requirements 的
                 检查在 grantProgress 内部实时完成），所以本次 v175 只补数据包侧
                 announce_to_chat 开关，java 侧一行播报都不加 —— 加了反而会让
                 单条件进度在聊天栏出现两条一模一样的。 */
            for (String criterion : advancement.getCriteria().keySet()) {
                player.getAdvancements().award(advancement, criterion);
            }
            String title = advancement.getDisplay() == null ? id : advancement.getDisplay().getTitle().getString();
            FTBossMod.LOGGER.info("[FTBoss][进度] {} 解锁进度 {}({})",
                    player.getName().getString(), title, id);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][进度] 发放 {} 失败: {}", id, t.toString());
        }
    }

    /* ==================== v175：头衔（Titles 模组整合） ==================== */

    /**
     * 「门槛进度 -> 头衔进度」对照表。
     *
     * <p>★ v175 按用户要求「触发成就来发放」（参考头衔模组自己的做法）：
     * 头衔**不**由我们直接塞进能力，而是走"给一个专门用来发头衔的进度打 criterion"这条路 ——
     * 这正是 Titles 模组自己给 `captain` / `melon_lord` 这类特殊头衔用的办法
     * （它有 `AdvancementEventHandler.grantCriterion`，进度里那条 criterion 就叫
     * `code_triggered`，JSON 里 `trigger` 写 `minecraft:impossible`，
     * 即原版永远不会自己触发、只能由代码打勾）。
     *
     * <p>为什么不让"门槛进度"本身直接去触发头衔（那个更省事，v175 第一版就是这么做的）：
     * 那个链路依赖 `AdvancementEarnEvent`，而它**只在"进度由未完成变成完成"的那一次**才发 ——
     * 已经拿过成就的存档（尤其是装本模组之前就拿到的）永远不会再触发，
     * 头衔就永远拿不到。拆成独立进度后，"补发"变成一件随时可做的事
     * （{@link #replayTitle}），而且与"玩家自己完成成就"那条路互不干扰。
     *
     * <p>头衔进度**故意没有 display**：它是纯粹的功能件，不该出现在进度界面里
     * （没有 display 的进度原版不会显示、也不会发飘窗/聊天播报）。
     *
     * <p>键 = 头衔进度 id（{@code ftboss:title_<x>}），值 = **用来判断"该不该补发"的门槛进度 id**
     * （非全限定；取 null 表示"没有可查的门槛"，见 {@link #titleGate} 的说明）。
     */
    private static final java.util.Map<String, String> TITLE_GATES = java.util.Map.of(
            /* ★ v184 互换：普通难度通关现在发「12号位」。
               它挂在「回忆的终结」成就下 —— 那条成就**只在普通难度发**，
               所以用成就当门槛正好等价于"通没通关普通难度"。 */
            "title_memories_end", MANOR_VICTORY,
            "title_food_boss_slayer_all", "food_boss_slayer_all", // 十位非庄园全清 -> 美食的俘虏
            /* ★ v184 互换：终结难度通关现在发「甜苹果庄园园长」。
               门槛值是**空串**而不是某个成就 id —— 因为"通关终结难度"这件事
                 **没有对应的成就**（成就故意只发普通难度），也就没有可查的进度。
                 TitleCommand 见到空门槛就"不做校验、允许补发"（见那边的注释）。 */
            "title_manor_impossible", ""
    );

    public static java.util.Set<String> titleAdvancementIds() {
        return TITLE_GATES.keySet();
    }

    /**
     * 这个头衔进度对应的**门槛进度 id**。
     *
     * @return 具体进度 id；{@code null} = 这张表里没登记这个头衔；
     *         **空串** = 已登记但"没有可查的门槛"（如「甜苹果庄园园长」——
     *         它现在归**终结难度**，而终结难度**没有对应成就**），
     *         调用方（{@code TitleCommand}）应对空串**跳过校验**。
     */
    public static String titleGate(String titleAdvancementId) {
        return TITLE_GATES.get(titleAdvancementId);
    }

    /**
     * 发放头衔：给 {@code ftboss:title_<x>} 打上 {@code code_triggered} 这条 criterion。
     *
     * <p>打完这一勾，原版会走完 `PlayerAdvancements.award` 的"未完成 -> 完成"分支并发出
     * {@code AdvancementEarnEvent}，Titles 模组的 `AdvancementEventHandler` 随即
     * 拿 `advancement.getId()` 去 `TitleManager.unlockTitle` 查表 ——
     * 而我们的头衔文件（`data/titles/titles/_ftboss/<x>.json`）注册出来的 id
     * 正好就是这个进度 id，于是头衔自动到手。
     *
     * <p>本方法**完全不依赖** Titles 模组：没装它时，这里只是把一个隐形的功能进度打完，
     * 玩家侧毫无感知、也不会报错。
     *
     * @return true = 本次真的完成了该进度（因此会触发头衔）；false = 之前就已经拿过
     */
    public static boolean grantTitle(ServerPlayer player, String titleAdvancementId) {
        if (player == null || titleAdvancementId == null) return false;
        try {
            Advancement advancement = player.server.getAdvancements()
                    .getAdvancement(new ResourceLocation(FTBossMod.MOD_ID, titleAdvancementId));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][头衔] 找不到头衔进度 {}:{}", FTBossMod.MOD_ID, titleAdvancementId);
                return false;
            }
            if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                /* criterion 名固定叫 code_triggered（与 Titles 模组自己的做法一致）。 */
                player.getAdvancements().award(advancement, "code_triggered");
            }
            return true;
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][头衔] 发放 {} 失败: {}", titleAdvancementId, t.toString());
            return false;
        }
    }

    /**
     * 补发头衔：**先收回再打勾**，强制重放一次"未完成 -> 完成"，从而重新触发
     * {@code AdvancementEarnEvent}（因此头衔会被重新查表并解锁）。
     *
     * <p>为什么需要它：头衔只在"完成的那一刻"发一次。已经拿过成就的存档，
     * 或者装头衔文件之前就完成的存档，都不会再触发 —— 这条命令就是那种情况的补救。
     *
     * <p>已用 javap 核实：`PlayerAdvancements.revoke()` **不会**发
     * {@code onAdvancementEarnedEvent}（它只发 ProgressEvent），
     * 所以"收回 -> 重打"净效果就是**恰好触发一次**头衔发放，不会重复。
     *
     * <p>只操作我们自己的隐形功能进度，**不动玩家的成就**，对玩家侧完全无感。
     *
     * @return true = 重放成功；false = 数据包缺失/异常
     */
    public static boolean replayTitle(ServerPlayer player, String titleAdvancementId) {
        if (player == null || titleAdvancementId == null) return false;
        try {
            Advancement advancement = player.server.getAdvancements()
                    .getAdvancement(new ResourceLocation(FTBossMod.MOD_ID, titleAdvancementId));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][头衔] 找不到头衔进度 {}:{}", FTBossMod.MOD_ID, titleAdvancementId);
                return false;
            }
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            for (String criterion : advancement.getCriteria().keySet()) {
                if (progress.getCriterion(criterion) != null && progress.getCriterion(criterion).isDone()) {
                    player.getAdvancements().revoke(advancement, criterion);
                }
            }
            return grantTitle(player, titleAdvancementId);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][头衔] 重放 {} 失败: {}", titleAdvancementId, t.toString());
            return false;
        }
    }

    /**
     * 玩家是否已经拿到某条"门槛进度"（例如 `food_boss_slayer_all`）。
     * 命令侧用它来判断"有没有资格补发头衔"。
     */
    public static boolean hasEarned(ServerPlayer player, String advancementId) {
        if (player == null || advancementId == null) return false;
        try {
            Advancement advancement = player.server.getAdvancements()
                    .getAdvancement(new ResourceLocation(FTBossMod.MOD_ID, advancementId));
            return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
        } catch (Throwable t) {
            return false;
        }
    }
}
