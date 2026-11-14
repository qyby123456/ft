/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.skill.ShadowCastHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ShadowCastManager {
    public static final int FIRE_TICK = 7;
    public static final int END_TICK = 18;
    public static final int FIRE_COOLDOWN = 40;
    public static final int CANCEL_COOLDOWN = 4;
    public static final float PEARL_SPEED = 1.9f;
    private static final Map<UUID, CastSession> ACTIVE = new HashMap<UUID, CastSession>();
    private static long nextCastId = 1L;

    private ShadowCastManager() {
    }

    public static boolean begin(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (stack.m_41619_() || !player.m_6084_() || player.m_5833_() || player.m_6117_() || ACTIVE.containsKey(player.m_20148_()) || player.m_36335_().m_41519_(stack.m_41720_())) {
            return false;
        }
        CastSession session = new CastSession(nextCastId++, hand, stack, player.m_284548_().m_46467_());
        ACTIVE.put(player.m_20148_(), session);
        ShadowCastHooks.started(player, session.castId, hand, session.startTick);
        return true;
    }

    public static void tick(ServerPlayer player) {
        CastSession session = ACTIVE.get(player.m_20148_());
        if (session == null) {
            return;
        }
        if (!player.m_6084_() || player.m_213877_() || player.m_5833_()) {
            ShadowCastManager.abort(player, "invalid_player");
            return;
        }
        long elapsed = player.m_284548_().m_46467_() - session.startTick;
        if (session.phase == Phase.CHARGING) {
            if (!ShadowCastManager.isHoldingOriginalStack(player, session) || !ShadowCastManager.isUsingOriginalStack(player, session)) {
                ShadowCastManager.abort(player, "use_interrupted");
                return;
            }
            if (elapsed >= 7L) {
                ShadowCastManager.fireOnce(player, session);
            }
        } else if (!(session.phase != Phase.RELEASED || elapsed <= 7L && ShadowCastManager.isUsingOriginalStack(player, session))) {
            session.phase = Phase.RECOVERING;
        }
        if (elapsed >= 18L) {
            ShadowCastManager.finish(player, session);
        }
    }

    public static void release(ServerPlayer player, ItemStack stack) {
        CastSession session = ACTIVE.get(player.m_20148_());
        if (session == null || session.originalStack != stack) {
            return;
        }
        if (!session.fired) {
            ShadowCastManager.abort(player, "released_before_fire");
        } else {
            session.phase = Phase.RECOVERING;
        }
    }

    private static void fireOnce(ServerPlayer player, CastSession session) {
        if (session.fired) {
            return;
        }
        session.fired = true;
        session.phase = Phase.RELEASED;
        player.m_36335_().m_41524_(session.originalStack.m_41720_(), 40);
        ShadowCastHooks.fired(player, session.castId, session.hand, session.originalStack);
    }

    private static void finish(ServerPlayer player, CastSession session) {
        ACTIVE.remove(player.m_20148_());
        if (ShadowCastManager.isUsingOriginalStack(player, session)) {
            player.m_5810_();
        }
        ShadowCastHooks.ended(player, session.castId, session.fired);
    }

    public static void abort(ServerPlayer player, String reason) {
        CastSession session = ACTIVE.remove(player.m_20148_());
        if (session == null) {
            return;
        }
        if (!session.fired) {
            player.m_36335_().m_41524_(session.originalStack.m_41720_(), 4);
        }
        if (ShadowCastManager.isUsingOriginalStack(player, session)) {
            player.m_5810_();
        }
        ShadowCastHooks.aborted(player, session.castId, session.fired, reason);
    }

    private static boolean isHoldingOriginalStack(ServerPlayer player, CastSession session) {
        return player.m_21120_(session.hand) == session.originalStack;
    }

    private static boolean isUsingOriginalStack(ServerPlayer player, CastSession session) {
        return player.m_6117_() && player.m_7655_() == session.hand && player.m_21211_() == session.originalStack;
    }

    public static void clear() {
        ACTIVE.clear();
        nextCastId = 1L;
    }

    private static final class CastSession {
        private final long castId;
        private final InteractionHand hand;
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

    public static enum Phase {
        CHARGING,
        RELEASED,
        RECOVERING;

    }
}

