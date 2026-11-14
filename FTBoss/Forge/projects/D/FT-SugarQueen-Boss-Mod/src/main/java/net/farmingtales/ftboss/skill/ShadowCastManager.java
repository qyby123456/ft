package net.farmingtales.ftboss.skill;

import com.mojang.logging.LogUtils;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 【裂瞳投珠】服务器状态机（本轮：长按蓄力 + 第 7 tick 发射 + 提前松手取消）。
 *
 * 规则（用户 2026-09-15 定）：
 *   长按右键开始蓄力 → 第 7 tick 发射 → 第 18 tick 结束；发射前松手=取消；发射后松手=保留结果进入收束；
 *   一次使用过程最多发射一次；正常冷却 40t（发射时开始），取消冷却 4t。
 *   IDLE 用"没有活动记录"表示；同一玩家同时只允许一次施法。
 *
 * ⚠ 历史残留注释已清理：早期版本 `ShadowCastHooks.fired` 只打日志不生成投射物，
 *   现在**已经在 `fired()` 里真正生成 {@code ShadowPearlEntity}**（第 7 tick、沿视线、从眼前出射）。
 *   物品类在**客户端返回 pass**（避免原版摆臂）但**仍 startUsingItem**，
 *   否则客户端松手不会发 release 包，取消逻辑收不到。详见 ShadowSwordItem#use。
 */
public final class ShadowCastManager {

    public static final int FIRE_TICK = 7;
    public static final int END_TICK = 18;
    public static final int FIRE_COOLDOWN = 40;
    public static final int CANCEL_COOLDOWN = 4;
    /** 珍珠初速(格/tick)。 */
    public static final float PEARL_SPEED = 1.9F;

    public enum Phase { CHARGING, RELEASED, RECOVERING }

    private static final Map<UUID, CastSession> ACTIVE = new HashMap<>();
    private static long nextCastId = 1L;

    private ShadowCastManager() {
    }

    private static final class CastSession {
        private final long castId;
        private final InteractionHand hand;
        /** 保存**物品栈引用**：换成另一把同级剑也必须取消旧施法（不能只比 item 类型）。 */
        private final ItemStack originalStack;
        private final long startTick;
        private Phase phase = Phase.CHARGING;
        private boolean fired;

        private CastSession(long castId, InteractionHand hand, ItemStack originalStack, long startTick) {
            this.castId = castId;
            this.hand = hand;
            this.originalStack = originalStack;
            this.startTick = startTick;
        }
    }

    public static boolean begin(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() || !player.isAlive() || player.isSpectator() || player.isUsingItem()
                || ACTIVE.containsKey(player.getUUID())
                || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
        }
        CastSession session = new CastSession(nextCastId++, hand, stack, player.serverLevel().getGameTime());
        ACTIVE.put(player.getUUID(), session);
        ShadowCastHooks.started(player, session.castId, hand, session.startTick);
        return true;
    }

    public static void tick(ServerPlayer player) {
        CastSession session = ACTIVE.get(player.getUUID());
        if (session == null) {
            return;
        }
        if (!player.isAlive() || player.isRemoved() || player.isSpectator()) {
            abort(player, "invalid_player");
            return;
        }
        long elapsed = player.serverLevel().getGameTime() - session.startTick;

        if (session.phase == Phase.CHARGING) {
            // 先判中断再判发射：服务器已收到松手时，本次施法取消。
            if (!isHoldingOriginalStack(player, session) || !isUsingOriginalStack(player, session)) {
                abort(player, "use_interrupted");
                return;
            }
            if (elapsed >= FIRE_TICK) {
                fireOnce(player, session);
            }
        } else if (session.phase == Phase.RELEASED
                && (elapsed > FIRE_TICK || !isUsingOriginalStack(player, session))) {
            session.phase = Phase.RECOVERING;
        }

        if (elapsed >= END_TICK) {
            finish(player, session);
        }
    }

    /** 松手（releaseUsing 调用）。 */
    public static void release(ServerPlayer player, ItemStack stack) {
        CastSession session = ACTIVE.get(player.getUUID());
        if (session == null || session.originalStack != stack) {
            return;
        }
        if (!session.fired) {
            abort(player, "released_before_fire");
        } else {
            session.phase = Phase.RECOVERING;
        }
    }

    private static void fireOnce(ServerPlayer player, CastSession session) {
        if (session.fired) {
            return;
        }
        session.fired = true;                       // 先标记再发射，防重复生成
        session.phase = Phase.RELEASED;
        player.getCooldowns().addCooldown(session.originalStack.getItem(), FIRE_COOLDOWN);
        ShadowCastHooks.fired(player, session.castId, session.hand, session.originalStack);
    }

    private static void finish(ServerPlayer player, CastSession session) {
        ACTIVE.remove(player.getUUID());            // 先移除，避免 stopUsingItem 回调重复处理
        if (isUsingOriginalStack(player, session)) {
            player.stopUsingItem();
        }
        ShadowCastHooks.ended(player, session.castId, session.fired);
    }

    public static void abort(ServerPlayer player, String reason) {
        CastSession session = ACTIVE.remove(player.getUUID());
        if (session == null) {
            return;
        }
        if (!session.fired) {
            player.getCooldowns().addCooldown(session.originalStack.getItem(), CANCEL_COOLDOWN);
        }
        if (isUsingOriginalStack(player, session)) {
            player.stopUsingItem();
        }
        ShadowCastHooks.aborted(player, session.castId, session.fired, reason);
    }

    private static boolean isHoldingOriginalStack(ServerPlayer player, CastSession session) {
        return player.getItemInHand(session.hand) == session.originalStack;
    }

    private static boolean isUsingOriginalStack(ServerPlayer player, CastSession session) {
        return player.isUsingItem() && player.getUsedItemHand() == session.hand
                && player.getUseItem() == session.originalStack;
    }

    public static void clear() {
        ACTIVE.clear();
        nextCastId = 1L;
    }
}

/** 事件：玩家 tick 驱动状态机；登出/死亡/换维度取消；服务器停止清表。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class ShadowCastEvents {

    private ShadowCastEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            ShadowCastManager.tick(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShadowCastManager.abort(player, "logout");
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShadowCastManager.abort(player, "death");
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShadowCastManager.abort(player, "dimension_changed");
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ShadowCastManager.clear();
    }
}

/** 钩子：触发 GeckoLib 动画 + **生成暗影珍珠投射物**（第 7 tick，服务器唯一一次）。 */
final class ShadowCastHooks {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ShadowCastHooks() {
    }

    private static void anim(ServerPlayer player, InteractionHand hand, String name) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ShadowSwordItem sword) {
            sword.triggerSkillAnim(player, stack, name);
        }
    }

    static void started(ServerPlayer player, long castId, InteractionHand hand, long startTick) {
        LOGGER.info("[ShadowCast] START player={} id={} hand={} tick={}",
                player.getGameProfile().getName(), castId, hand, startTick);
        anim(player, hand, "cast");
    }

    static void fired(ServerPlayer player, long castId, InteractionHand hand, ItemStack stack) {
        LOGGER.info("[ShadowCast] FIRE player={} id={} hand={} tick={}",
                player.getGameProfile().getName(), castId, hand, player.serverLevel().getGameTime());
        // ★ 服务器唯一一次生成(第 7 tick): 沿视线、从玩家眼前出射; 不读渲染后的骨骼世界坐标。
        {
            /* ★ 2026-09-18 补：蛋蛋的末影封锁对自研珍珠原本完全无效（见 ShadowPearlEntity#impact 的注释）。
               这里在**起手刚要点火**时先问一次，封锁内就直接不生成 —— 省掉一次白扔的完整施法，
               表现与"暗影剑右键被拦"一致。落地那一道仍然保留（防"圈外扔、飞进圈"）。 */
            if (net.farmingtales.ftboss.boss.ai.DandanPearlBan.blockThrow(player, "裂瞳投珠 · 末影珍珠投掷")) {
                return;
            }
            net.farmingtales.ftboss.entity.ShadowPearlEntity pearl =
                    new net.farmingtales.ftboss.entity.ShadowPearlEntity(player.serverLevel(), player);
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    ShadowCastManager.PEARL_SPEED, 0.0F);
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            net.minecraft.world.phys.Vec3 eye = player.getEyePosition();
            pearl.setPos(eye.x + look.x * 0.6D, eye.y - 0.12D, eye.z + look.z * 0.6D);
            player.serverLevel().addFreshEntity(pearl);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.ENDER_PEARL_THROW,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.85F);
        }
        //   本轮**不生成原版末影珍珠**，也不在客户端动画回调里生成投射物。
    }

    static void ended(ServerPlayer player, long castId, boolean fired) {
        LOGGER.info("[ShadowCast] END player={} id={} fired={} tick={}",
                player.getGameProfile().getName(), castId, fired, player.serverLevel().getGameTime());
        // cast 动画自然结束 -> 待机控制器接管，无需额外触发。
    }

    static void aborted(ServerPlayer player, long castId, boolean fired, String reason) {
        LOGGER.info("[ShadowCast] ABORT player={} id={} fired={} reason={} tick={}",
                player.getGameProfile().getName(), castId, fired, reason, player.serverLevel().getGameTime());
        // 未发射才播 cancel（全部回中立、聚能球立刻隐藏）；已发射不撤销珍珠，只收束。
        if (!fired) {
            anim(player, player.getUsedItemHand(), "cancel");
        }
    }
}
