package net.farmingtales.ftboss.boss;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * 【隐藏 Boss 挑战确认】(2026-09-13 v78 按用户要求)。
 *
 * <p>入场演出**之前**先警告 + 让玩家自己选:
 * <pre>
 *   §c§l【隐藏 Boss】难度极高 —— 您必须强度已经毕业了才建议挑战！
 *   §7请选择:  §a§l[挑战]   §c§l[放弃]
 * </pre>
 * 玩家点聊天里的按钮 -> 执行 {@code /ftboss challenge} / {@code /ftboss giveup}(聊天按钮就是发命令)。
 * <ul>
 *   <li>先点【挑战】再点【确认开战】: 两次确认后给 96 格内玩家补给, 然后才开始入场演出;</li>
 *   <li>点【放弃】: 取消, 什么都不发生(Boss 不会出现);</li>
 *   <li>60 秒没人选: 自动过期(同样不开始), 并提示一次。</li>
 * </ul>
 *
 * <p><b>2026-09-14 v113 按用户要求「接受拒绝只能由触发彩蛋玩家选择」</b>:
 * 触发者(戴「甜苹果庄园」头衔、把苹果爵士逼到"头衔共鸣"的那位, 见
 * {@code AppleBossEntity.ascendToNextBoss})才有 {@code [挑战]/[放弃]} 按钮;
 * 附近其他人只收到"本场彩蛋由 X 触发, 由他决定"的通知。别的玩家就算自己敲
 * {@code /ftboss challenge} 也只会被提示"只能由触发者选", **不会消耗掉这次确认**。
 * 触发者中途离线 -> 确认直接作废(免得其余人干等 60 秒)。
 *
 * <p>伤害吸收用原版"伤害吸收"效果实现: 等级 100 时按原版公式给 {@code (amp+1)*4 = 400} 点,
 * 效果 10 分钟后自然消失、护盾一并收走 —— 不需要自己记账。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class HiddenBossChallenge {

    /** 警告发给多大范围内的玩家 */
    public static final double WARN_RADIUS = 96.0;
    /** 没人选择时的过期时间(60 秒) */
    public static final int TIMEOUT_TICKS = 60 * 20;
    /** 赠送金苹果数量("一组" = 64) */
    public static final int GIFT_APPLES = 64;
    /** 赠送的伤害吸收点数 */
    public static final int ABSORPTION_POINTS = 400;
    /** 伤害吸收持续时间(10 分钟) */
    public static final int ABSORPTION_TICKS = 10 * 60 * 20;

    private static final class Offer {
        final ServerLevel sl;
        final Vec3 anchor;
        final Vec3 from;
        final String[] lines;
        long expireTick;
        /** v113: 触发彩蛋的那位玩家 —— **只有他**能点[挑战]/[放弃] */
        UUID chooser;
        String chooserName;
        UUID token = UUID.randomUUID();
        boolean finalConfirmation;
        ManorDifficulty difficulty = ManorDifficulty.NORMAL;

        Offer(ServerLevel sl, Vec3 anchor, Vec3 from, String[] lines, long expireTick,
              UUID chooser, String chooserName) {
            this.sl = sl;
            this.anchor = anchor;
            this.from = from;
            this.lines = lines;
            this.expireTick = expireTick;
            this.chooser = chooser;
            this.chooserName = chooserName;
        }
    }

    private static Offer pending = null;

    private HiddenBossChallenge() {}

    /** 有人正在被问(避免重复弹) */
    public static boolean awaiting() { return pending != null; }

    static boolean cancelForCreative(ServerLevel level) {
        if (pending == null || pending.sl != level) return false;
        Offer offer = pending;
        pending = null;
        net.farmingtales.ftboss.sound.BossMusic.stopManorCue("创造道具取消挑战");
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(offer.anchor) <= WARN_RADIUS * WARN_RADIUS) {
                player.sendSystemMessage(Component.literal("§7【甜苹果庄园】本次挑战已被创造道具取消，原确认按钮失效。"));
            }
        }
        return true;
    }

    /**
     * 弹出"是否挑战隐藏 Boss"的确认(警告 + 可点按钮)。点了【挑战】才走 {@link HiddenBossIntro#start}。
     *
     * @param chooser 触发彩蛋的那位玩家(**只有他能选**, 见类注释 v113)
     * @param anchor  战场锚点(玩家位置) / from 演出出场起点(Boss 位置)
     */
    public static void ask(ServerLevel sl, Player chooser, Vec3 anchor, Vec3 from, String... lines) {
        if (sl == null) return;
        /* 2026-09-13 v93: 不在主世界就直接不弹 —— 隐藏 Boss 只能主世界召唤, 弹了也点不了 */
        if (!SummonGate.isAllowedDim(sl)) {
            FTBossMod.LOGGER.info("[FTBoss][Challenge] 「{}」不是主世界 -> 不弹隐藏 Boss 挑战确认",
                    SummonGate.dimName(sl));
            return;
        }
        long now = sl.getGameTime();
        UUID chooserId = chooser == null ? null : chooser.getUUID();
        String who = chooser == null ? "?" : chooser.getName().getString();
        pending = new Offer(sl, anchor, from, lines, now + TIMEOUT_TICKS, chooserId, who);
        if (chooser instanceof ServerPlayer serverPlayer) {
            pending.difficulty = BossChallengePreference.selected(serverPlayer);
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] 已弹出隐藏 Boss 挑战确认(60 秒内由 {} 选择), 锚点 {},{},{}",
                who, (int) anchor.x, (int) anchor.y, (int) anchor.z);
        /* ★ 2026-09-13 晚按用户要求: **玩家开始选择的时候就把庄园战双轨放起来**。
           点【挑战】就接着播(不重启), 点【放弃】或 60 秒超时再停。 */
        net.farmingtales.ftboss.sound.BossMusic.beginManorPhase(sl, anchor);
        /* ★ 2026-09-14 v113 按用户要求「接受拒绝只能由触发彩蛋玩家选择」:
           按钮只发给触发者, 附近其他人只收到"由他决定"的通知(没有按钮)。 */
        boolean sent = false;
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(anchor.x, anchor.y, anchor.z) > WARN_RADIUS * WARN_RADIUS) continue;
            if (chooserId == null || chooserId.equals(p.getUUID())) {
                sendPrompt(p, pending);          // chooserId == null(理论上不会) -> 退回老行为: 人人都能选
                sent = true;
            } else {
                sendNotice(p, who);
            }
            sl.playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.MASTER, 1.0f, 0.7f);
        }
        /* 触发者万一站在 96 格外(理论上不会: 锚点就是他的位置) -> 也得把按钮递到他手里 */
        if (!sent && chooserId != null && sl.getServer() != null) {
            ServerPlayer cs = sl.getServer().getPlayerList().getPlayer(chooserId);
            if (cs != null) sendPrompt(cs, pending);
        }
    }

    private static Component difficultyDetails() {
        return Component.literal("§4" + ManorDifficulty.IMPOSSIBLE.label() + "\n§c" + ManorDifficulty.WARNING
                + "\n§7启用限伤、指定增益禁用与反逃课规则"
                + "\n§7保留常规掉落与胜利奖励，不发放庄园成就或通关头衔");
    }

    private static Component choice(String label, String command, Component details) {
        return Component.literal(label).withStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, details)));
    }

    private static void sendPrompt(ServerPlayer player, Offer offer) {
        boolean unlocked = !offer.finalConfirmation && ManorDifficulty.unlocked(player);
        boolean finale = offer.difficulty == ManorDifficulty.IMPOSSIBLE;
        player.sendSystemMessage(Component.literal(offer.finalConfirmation
                ? "§6§l【最终确认 2/2】§r§f当前难度：" + offer.difficulty.label()
                : "§6§l【甜苹果庄园】§r§f选择难度（1/2）"));
        if (offer.finalConfirmation) {
            if (finale) {
                player.sendSystemMessage(Component.literal("§c" + ManorDifficulty.WARNING));
                player.sendSystemMessage(Component.literal("§7限伤 · 指定增益禁用 · 反逃课 §8[悬浮查看规则]")
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, difficultyDetails()))));
            }
            player.sendSystemMessage(Component.literal("§7确认后发放补给并开始演出：§f金苹果 ×" + GIFT_APPLES
                    + "§7、§f伤害吸收 " + ABSORPTION_POINTS + " 点（10 分钟）"));
        } else {
            player.sendSystemMessage(Component.literal("§7高强度连续战斗，建议装备与补给齐全后挑战。"));
            if (unlocked) player.sendSystemMessage(Component.literal("§c" + ManorDifficulty.IMPOSSIBLE.label() + "：" + ManorDifficulty.WARNING));
        }
        String command = "/ftboss challenge " + (offer.finalConfirmation ? "confirm "
                : finale ? "finale " : "normal ") + offer.token;
        String primaryLabel = offer.finalConfirmation ? "§a§l[ 确认开战 2/2 ]"
                : finale ? "§4§l[ " + ManorDifficulty.IMPOSSIBLE.label() + " 1/2 · 石像预设 ]"
                : "§a§l[ 普通难度 1/2 · 石像预设 ]";
        var buttons = Component.literal("§7请选择： ").append(choice(primaryLabel, command,
                Component.literal(offer.finalConfirmation ? "§7立即发放补给，开始入场演出"
                        : "§7使用石像预设难度进入最终确认；此时不开战，也不发放补给")));
        if (!offer.finalConfirmation) {
            buttons.append(Component.literal("  "));
            if (finale) {
                buttons.append(choice("§a[ 临时改选普通难度 ]", "/ftboss challenge normal " + offer.token,
                        Component.literal("§7只修改本次庄园挑战，不改变石像预设")));
            } else if (unlocked) {
                buttons.append(choice("§4§l[ " + ManorDifficulty.IMPOSSIBLE.label() + " 1/2 ]",
                        "/ftboss challenge finale " + offer.token, difficultyDetails()));
            } else {
                buttons.append(Component.literal("§8[ " + ManorDifficulty.IMPOSSIBLE.label() + "·未解锁 ]")
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("§7完整通关普通庄园挑战后解锁")))));
            }
        }
        buttons.append(Component.literal("  ")).append(choice("§c[ 放弃 ]", "/ftboss giveup " + offer.token,
                Component.literal("§7取消本次挑战，不召唤 Boss")));
        player.sendSystemMessage(buttons);
        sendSurrenderReminder(player);
        player.sendSystemMessage(Component.literal("§8仅触发者可选；每步 60 秒，超时取消。"));
    }

    private static void sendSurrenderReminder(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§7同一庄园 Boss 下连续死亡 §e" + ManorMercy.ASK_EVERY
                + " 次§7可选择继续/投降；§e仅召唤者§7可决定整场投降，投降不算通关、不发胜利宝箱。")
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.literal("§7选择继续会清零该 Boss 的死亡计数")))));
    }

    private static void sendNotice(ServerPlayer player, String chooserName) {
        player.sendSystemMessage(Component.literal("§6【甜苹果庄园】§7等待触发者 §e" + chooserName
                + " §7选择难度并确认开战，其他玩家无需操作。"));
        sendSurrenderReminder(player);
    }

    /* ---------------- 命令 / 过期 ---------------- */

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("ftboss")
                .then(Commands.literal("challenge").executes(ctx ->
                        accept(ctx.getSource().getPlayerOrException(), null, false))
                        .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(ctx ->
                                accept(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "confirmation"), false)))
                        .then(Commands.literal("normal")
                                .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(ctx ->
                                        accept(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "confirmation"),
                                                false, ManorDifficulty.NORMAL))))
                        .then(Commands.literal("impossible")
                                .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(ctx ->
                                        accept(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "confirmation"),
                                                false, ManorDifficulty.IMPOSSIBLE))))
                        .then(Commands.literal("finale")
                                .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(ctx ->
                                        accept(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "confirmation"),
                                                false, ManorDifficulty.IMPOSSIBLE))))
                        .then(Commands.literal("confirm")
                                .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(ctx ->
                                        accept(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "confirmation"), true)))))
                .then(Commands.literal("giveup").executes(ctx ->
                        decline(ctx.getSource().getPlayerOrException(), null))
                        .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(ctx ->
                                decline(ctx.getSource().getPlayerOrException(), UuidArgument.getUuid(ctx, "confirmation"))))));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        pending = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || pending == null) return;
        ServerLevel sl = pending.sl;
        if (sl.getGameTime() < pending.expireTick) {
            /* ★ v113: 触发者中途离线 -> 这次确认直接作废。
               按钮只在他手里, 别人点不了, 没必要让全场干等满 60 秒。 */
            if (pending.chooser != null && sl.getServer() != null
                    && sl.getServer().getPlayerList().getPlayer(pending.chooser) == null) {
                Offer gone = pending;
                pending = null;
                net.farmingtales.ftboss.sound.BossMusic.stopManorCue("触发者离线");
                for (ServerPlayer p : sl.players()) {
                    if (p.distanceToSqr(gone.anchor.x, gone.anchor.y, gone.anchor.z) > WARN_RADIUS * WARN_RADIUS) continue;
                    p.sendSystemMessage(Component.literal("§7【甜苹果庄园】触发者 §e" + gone.chooserName
                            + " §7已离线 —— 本次挑战确认作废。"));
                }
                FTBossMod.LOGGER.info("[FTBoss][Challenge] 触发者 {} 已离线, 挑战确认作废", gone.chooserName);
            }
            return;
        }
        Offer dead = pending;
        pending = null;
        net.farmingtales.ftboss.sound.BossMusic.stopManorCue("60 秒未选择, 超时");
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(dead.anchor.x, dead.anchor.y, dead.anchor.z) > WARN_RADIUS * WARN_RADIUS) continue;
            p.sendSystemMessage(Component.literal("§7【甜苹果庄园】§8挑战确认已超时(60 秒未选择) —— 本次不会开始。"));
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] 挑战确认超时, 取消");
    }

    private static Offer currentOffer(ServerPlayer p, UUID token) {
        Offer o = pending;
        if (o == null) {
            p.sendSystemMessage(Component.literal("§7【甜苹果庄园】现在没有待确认的挑战。"));
            return null;
        }
        /* ★ 2026-09-14 v113 按用户要求「接受拒绝只能由触发彩蛋玩家选择」:
           别人点了(自己敲命令也算)只提示, **不消耗掉这次确认** —— 触发者仍然可以选。 */
        if (o.chooser != null && !o.chooser.equals(p.getUUID())) {
            p.sendSystemMessage(Component.literal("§7【甜苹果庄园】本场彩蛋由 §e" + o.chooserName
                    + " §7触发 —— §c只能由他来选择§7。"));
            return null;
        }
        if (o.sl.getGameTime() >= o.expireTick) {
            pending = null;
            net.farmingtales.ftboss.sound.BossMusic.stopManorCue("挑战确认超时");
            p.sendSystemMessage(Component.literal("§7【甜苹果庄园】挑战确认已超时，本次不会开始。"));
            return null;
        }
        if (token != null && !token.equals(o.token)) {
            p.sendSystemMessage(Component.literal("§7【甜苹果庄园】旧确认按钮已失效，请使用本次最新的按钮。"));
            return null;
        }
        return o;
    }

    private static int accept(ServerPlayer p, UUID token, boolean finalConfirmation) {
        return accept(p, token, finalConfirmation, null);
    }

    private static int accept(ServerPlayer p, UUID token, boolean finalConfirmation, ManorDifficulty selection) {
        Offer o = currentOffer(p, token);
        if (o == null) return 0;
        if (selection != null && o.finalConfirmation) {
            sendPrompt(p, o);
            return 0;
        }
        ManorDifficulty difficulty = selection == null ? o.difficulty : selection;
        if (difficulty == ManorDifficulty.IMPOSSIBLE && !ManorDifficulty.unlocked(p)) {
            p.sendSystemMessage(Component.literal("§c【" + ManorDifficulty.IMPOSSIBLE.label() + "·未解锁】§7请先完整通关普通庄园挑战。"));
            return 0;
        }
        if (!p.isAlive() || p.isSpectator() || p.serverLevel() != o.sl) {
            p.sendSystemMessage(Component.literal("§c【甜苹果庄园】请以存活的非旁观状态返回触发维度，再确认挑战。"));
            return 0;
        }
        /* 2026-09-13 v93 按用户要求「全部 boss 都必须在主世界召唤」:
           隐藏 Boss(甜苹果庄园)也是 Boss —— 不在主世界就不放行, 挑战确认原地作废。
           (正常流程下 o.sl 一定已经是主世界: 触发它的苹果爵士本身就被这条闸挡在别的维度之外了。) */
        if (!SummonGate.allow(o.sl, p, "隐藏 Boss(甜苹果庄园)")) {
            pending = null;
            return 0;
        }
        if (finalConfirmation) {
            if (!o.finalConfirmation || token == null) {
                p.sendSystemMessage(Component.literal("§7【甜苹果庄园】请先完成第一次确认，再点击最新的【确认开战 2/2】。"));
                return 0;
            }
        } else {
            if (!o.finalConfirmation) {
                o.chooser = p.getUUID();
                o.chooserName = p.getName().getString();
                o.finalConfirmation = true;
                o.difficulty = difficulty;
                o.token = UUID.randomUUID();
                o.expireTick = o.sl.getGameTime() + TIMEOUT_TICKS;
                FTBossMod.LOGGER.info("[FTBoss][Challenge] {} 完成首次确认，等待第二次确认", p.getName().getString());
                sendPrompt(p, o);
                return 1;
            }
            sendPrompt(p, o);
            return 0;
        }
        pending = null;
        giftToPlayers(o.sl, o.anchor);
        HiddenBossIntro.start(o.sl, o.anchor, o.from, HiddenBossIntro.DEFAULT_VARIANT, p.getUUID(), o.difficulty, o.lines);
        /* ★ 2026-09-19 v164 按用户要求：**触发彩蛋 Boss 就解锁一个进度**（标题「farbic的记忆……」）。
           时刻就选在这里 —— 两次确认都过了、入场演出刚开始的那一瞬间，也就是"彩蛋 Boss 真的被触发了"。
           只发给发起者本人（在场其他人不重复算），进度本身是 hidden（拿到才在进度列表里出现）。 */
        if (o.difficulty == ManorDifficulty.NORMAL) grantAdvancement(p, "easter_egg_memory");
        for (ServerPlayer q : o.sl.players()) {
            if (q.distanceToSqr(o.anchor.x, o.anchor.y, o.anchor.z) > WARN_RADIUS * WARN_RADIUS) continue;
            q.sendSystemMessage(Component.literal("§6【甜苹果庄园·" + o.difficulty.title() + "】§f挑战开始！"
                    + (o.difficulty == ManorDifficulty.IMPOSSIBLE ? "§c" + ManorDifficulty.WARNING : "§7祝你好运。")));
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] {} 完成两次确认 -> 开始入场演出，难度={}", p.getName().getString(), o.difficulty.id());
        return 1;
    }

    /**
     * 发放一个本模组的进度（advancement）。
     *
     * <p>本模组的进度统一写成 {@code data/ftboss/advancements/<id>.json}，判定条件用
     * {@code minecraft:impossible}（永远不会被原版自己触发），只能由代码在这里手动
     * {@code award(advancement, "triggered")} —— 这样进度就能钉在任意自定义时机上。
     * 数据包还没加载（或 id 写错）时只记一条日志，绝不让挑战流程崩。
     */
    private static void grantAdvancement(ServerPlayer player, String id) {
        try {
            var advancement = player.server.getAdvancements()
                    .getAdvancement(new net.minecraft.resources.ResourceLocation(FTBossMod.MOD_ID, id));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][进度] 找不到进度 {}:{}", FTBossMod.MOD_ID, id);
                return;
            }
            player.getAdvancements().award(advancement, "triggered");
            FTBossMod.LOGGER.info("[FTBoss][进度] {} 解锁进度 {}({})",
                    player.getName().getString(), advancement.getDisplay().getTitle().getString(), id);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][进度] 发放 {} 失败: {}", id, t.toString());
        }
    }

    private static int decline(ServerPlayer p, UUID token) {
        Offer o = currentOffer(p, token);
        if (o == null) return 0;
        pending = null;
        /* ★ 玩家放弃 -> 停掉刚才在"选择阶段"就起播的庄园战双轨 */
        net.farmingtales.ftboss.sound.BossMusic.stopManorCue("玩家放弃");
        for (ServerPlayer q : o.sl.players()) {
            if (q.distanceToSqr(o.anchor.x, o.anchor.y, o.anchor.z) > WARN_RADIUS * WARN_RADIUS) continue;
            q.sendSystemMessage(Component.literal("§7【甜苹果庄园】已放弃本次挑战。"));
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] {} 放弃了隐藏 Boss 挑战", p.getName().getString());
        return 1;
    }

    /** 进场补给: 一组金苹果 + 400 点伤害吸收(10 分钟) */
    private static void giftToPlayers(ServerLevel sl, Vec3 anchor) {
        int n = 0;
        for (ServerPlayer p : sl.players()) {
            if (p.distanceToSqr(anchor.x, anchor.y, anchor.z) > WARN_RADIUS * WARN_RADIUS) continue;
            ItemStack apples = new ItemStack(Items.GOLDEN_APPLE, GIFT_APPLES);
            if (!p.getInventory().add(apples)) p.drop(apples, false);
            /* 原版伤害吸收: 等级 100 -> (100) * 4 = 400 点; 10 分钟后效果消失, 护盾一起收走 */
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TICKS,
                    ABSORPTION_POINTS / 4 - 1, false, true, true));
            p.displayClientMessage(Component.literal(
                    "§a【甜苹果庄园】补给: §f金苹果 x" + GIFT_APPLES + " §7+ §f伤害吸收 " + ABSORPTION_POINTS + " 点(10 分钟)"), false);
            n++;
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] 已发放挑战补给: {} 名玩家(金苹果 x{}, 伤害吸收 {} 点 {} 分钟)",
                n, GIFT_APPLES, ABSORPTION_POINTS, ABSORPTION_TICKS / 20 / 60);
    }
}
