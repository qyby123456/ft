package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【蛋蛋 · 末影封锁】(2026-09-17 按用户要求「给蛋蛋加一个禁止玩家用末影珍珠传送的效果」)。
 *
 * <p>蛋蛋(variant 1)是庄园里的末影系 Boss —— 她自己就在靠末影珍珠/瞬移满场跑,
 * 所以她的领域里**不许玩家也用末影珍珠作弊**: 在 {@value #RANGE} 格内, 玩家
 * <b>扔不出</b>末影珍珠、<b>也传不过去</b>。
 *
 * <p>两条拦截各有分工, 缺一不可:
 * <ol>
 *   <li>{@link PlayerInteractEvent.RightClickItem}(可取消) —— 起手就拦:
 *       {@code ServerPlayerGameMode.useItem} 拿到非空结果后**直接 return**, 不会调用
 *       {@code ItemStack#use}, 所以珍珠既不会被扔出去、也不会被消耗(玩家背包里还在, 只是按下没反应)。
 *       只靠下面那条的话珍珠会被白白吃掉。</li>
 *   <li>{@link EntityTeleportEvent.EnderPearl}(可取消) —— 兜底:
 *       凡是**已经在飞**的珍珠(在领域外扔出、飞进来落地; 或发射器/别的 mod 扔的),
 *       落地结算时再判一次。取消后原版 {@code ThrownEnderpearl.onHit} 会整段跳过
 *       传送与摔落伤害, 直接 discard 掉那颗珍珠。</li>
 * </ol>
 *
 * <p>判定只看"附近有没有一只**真在战斗**的蛋蛋": 同维度、{@code variant == 1}、
 * 活着且不是迷你分身/不是被冻结的入场演出体。所以终章那九位(全体 {@code setNoAi(true)}
 * 站在空中当"身体")不会挂上这个封锁。
 *
 * <p><b>2026-09-18 补漏</b>：上面两条钩子只认**普通末影珍珠**，而本模组自己的
 * 珍珠位移（暗影剑右键的原版珍珠、裂瞳投珠的自研 {@code ShadowPearlEntity}）
 * 一条都不走它们 —— 实测 487 个日志里本封锁<b>一次都没触发过</b>。
 * 所以又加了 {@link #blockThrow} / {@link #blockLanding} 两个入口，
 * 由武器侧显式调用（见下面"模组自带珍珠位移"那段注释）。
 *
 * <p>与 {@link net.farmingtales.ftboss.boss.ManorBan} 无关 —— 那是庄园的禁术/禁食清单。
 */
public final class DandanPearlBan {

    /** 封锁半径(格): 蛋蛋周围这么远内不许用末影珍珠传送 */
    public static final double RANGE = 48.0;

    /** 提示限流: 玩家 UUID -> 上次提示的游戏刻(每 2 秒最多念一次) */
    private static final Map<UUID, Long> NOTICE = new ConcurrentHashMap<>();

    private DandanPearlBan() {}

    /** 这名玩家此刻是否处在某只蛋蛋的末影封锁里(给事件用, 也可给别的机制复用) */
    public static boolean isSealed(ServerPlayer player) {
        if (player == null || !player.isAlive() || player.isSpectator()) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;
        return nearestSeal(level, player) != null;
    }

    /** 半径内最近的一只"真在战斗"的蛋蛋; 没有就 null */
    private static EasterBossEntity nearestSeal(ServerLevel level, ServerPlayer player) {
        EasterBossEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (EasterBossEntity boss : level.getEntitiesOfClass(EasterBossEntity.class,
                player.getBoundingBox().inflate(RANGE))) {
            if (boss.getVariant() != EasterBossEntity.DANDAN_VARIANT || boss.isMini()
                    || boss.isNoAi() || !boss.isAlive() || boss.isRemoved()) continue;
            double distance = boss.distanceToSqr(player);
            if (distance > RANGE * RANGE || distance >= bestDistance) continue;
            bestDistance = distance;
            best = boss;
        }
        return best;
    }

    /**
     * 甩一句"被封锁了"的反馈: 屏幕中上方 + 聊天栏留痕 + 末影人传送音(反向音调)。
     * 同一玩家 2 秒内只念一次, 免得连点时刷屏。
     */
    public static void notice(ServerPlayer player, String what) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return;
        long now = level.getGameTime();
        Long last = NOTICE.get(player.getUUID());
        if (last != null && now - last < 40L) return;
        if (NOTICE.size() > 64) NOTICE.entrySet().removeIf(entry -> now - entry.getValue() >= 40L);
        NOTICE.put(player.getUUID(), now);
        player.displayClientMessage(Component.literal("§5蛋蛋 §7的末影封锁: §d" + what + " §7被禁止"), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.7f, 0.6f);
        FTBossMod.LOGGER.info("[FTBoss][Easter] 蛋蛋末影封锁拦截 {} 的{}", player.getName().getString(), what);
    }

    /** ① 起手拦截: 蛋蛋领域内玩家按右键扔不出末影珍珠(珍珠也不会被消耗) */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        try {
            if (event.getLevel().isClientSide()) return;
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            if (!event.getItemStack().is(Items.ENDER_PEARL)) return;
            if (!isSealed(player)) return;
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            notice(player, "末影珍珠投掷");
        } catch (Throwable failure) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 蛋蛋末影封锁(投掷)异常: {}", failure.toString());
        }
    }

    /** ② 落地兜底: 已经在飞的末影珍珠, 在蛋蛋领域内一律不生效(不传送、不算摔落伤害) */
    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        try {
            ServerPlayer player = event.getPlayer();
            if (player == null || player.level().isClientSide()) return;
            if (!isSealed(player)) return;
            event.setCanceled(true);
            notice(player, "末影珍珠传送");
        } catch (Throwable failure) {
            FTBossMod.LOGGER.warn("[FTBoss][Easter] 蛋蛋末影封锁(传送)异常: {}", failure.toString());
        }
    }

    // ------------------------------------------------------------ 模组自带珍珠位移
    /* ★ 2026-09-18 补的漏洞（查"末影封锁从未触发"时发现）：
       上面两条钩子只认**普通末影珍珠**，于是本模组自己的珍珠位移完全绕开了封锁 ——

       ① 暗影剑右键：{@code BlackCleaverAndShadow.ShadowSword#use} 里是
          {@code new ThrownEnderpearl(level, player)}，玩家手里拿的是**剑**不是珍珠，
          所以 {@code RightClickItem} 那条的 {@code is(Items.ENDER_PEARL)} 永远不成立。
          好在那颗是**真的原版 ThrownEnderpearl**，落地时原版会走
          {@code ForgeEventFactory.onEnderPearlLand}(已用 javap 核过：整个 Forge 里
          只有 {@code ThrownEnderpearl} 与 {@code ForgeEventFactory} 引用这个事件类)，
          所以第 ② 条能拦住它的传送 —— 但它自己的落点 AOE 是
          {@code ShadowPearlWatcher} 在珍珠被 discard 后补算的，得单独拦（见下面 sealWatcher）。

       ② 裂瞳投珠（09-15 第二版起）：{@code ShadowCastManager} 生成的是自研实体
          {@code ShadowPearlEntity}，它根本不是 {@code ThrownEnderpearl} 子类，
          落地时直接 {@code ServerPlayer.teleportTo(...)}（见 {@code ShadowSwordItem.teleportStrike}），
          **既不触发原版传送事件、也没经过任何珍珠判定** —— 两条钩子都拦不到。
          所以起手与落地各补一道（见下面 sealEntity）。 */

    /**
     * 起手拦截（模组武器版）：{@code true} = 这次珍珠动作应当被封锁掉。
     *
     * <p>给"手里不是珍珠、但会生成珍珠"的自研武器用 —— 例如暗影剑右键。
     * 调用方应在 {@code true} 时<b>既不生成珍珠、也不吃冷却</b>，保持与"按了没反应"一致。
     */
    public static boolean blockThrow(ServerPlayer player, String what) {
        if (!isSealed(player)) return false;
        notice(player, what);
        return true;
    }

    /**
     * 落地拦截（模组武器版）：珍珠已经在飞、玩家此刻在封锁里 ——
     * 只报一次提示并返回 {@code true}，调用方应<b>跳过 AOE 与传送</b>（与第 ② 条"整段跳过"一致）。
     */
    public static boolean blockLanding(ServerPlayer player, String what) {
        if (!isSealed(player)) return false;
        notice(player, what);
        return true;
    }
}
